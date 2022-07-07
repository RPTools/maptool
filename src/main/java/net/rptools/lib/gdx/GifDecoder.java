package net.rptools.lib.gdx;

import static java.lang.System.arraycopy;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.rptools.lib.image.ImageUtil;

/*
 * Copyright 2014 Dhyan Blum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * A decoder capable of processing a GIF data stream to render the graphics contained in it. This
 * implementation follows the official <A HREF="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF
 * specification</A>.
 *
 * <p>Example usage:
 *
 * <p>
 *
 * <pre>
 * final GifImage gifImage = GifDecoder.read(int[] data);
 * final int width = gifImage.getWidth();
 * final int height = gifImage.getHeight();
 * final int frameCount = gifImage.getFrameCount();
 * for (int i = 0; i < frameCount; i++) {
 * 	final BufferedImage image = gifImage.getFrame(i);
 * 	final int delay = gif.getDelay(i);
 * }
 * </pre>
 *
 * @author Dhyan Blum
 * @version 1.09 November 2017
 */
public final class GifDecoder {
  private GifImage image;

  public GifDecoder(InputStream is) throws IOException {
    image = read(is);
  }

  public GifDecoder() {}

  static final class BitReader {
    private int bitPos; // Next bit to read
    private int numBits; // Number of bits to read
    private int bitMask; // Use to kill unwanted higher bits
    private byte[] in; // Data array

    // To avoid costly bounds checks, 'in' needs 2 more 0-bytes at the end
    private final void init(final byte[] in) {
      this.in = in;
      bitPos = 0;
    }

    private final int read() {
      // Byte indices: (bitPos / 8), (bitPos / 8) + 1, (bitPos / 8) + 2
      int i = bitPos >>> 3; // Byte = bit / 8
      // Bits we'll shift to the right, AND 7 is the same as MODULO 8
      final int rBits = bitPos & 7;
      // Byte 0 to 2, AND to get their unsigned values
      final int b0 = in[i++] & 0xFF, b1 = in[i++] & 0xFF, b2 = in[i] & 0xFF;
      // Glue the bytes together, don't do more shifting than necessary
      final int buf = ((b2 << 8 | b1) << 8 | b0) >>> rBits;
      bitPos += numBits;
      return buf & bitMask; // Kill the unwanted higher bits
    }

    private final void setNumBits(final int numBits) {
      this.numBits = numBits;
      bitMask = (1 << numBits) - 1;
    }
  }

  static final class CodeTable {
    private final int[][] tbl; // Maps codes to lists of colors
    private int initTableSize; // Number of colors +2 for CLEAR + EOI
    private int initCodeSize; // Initial code size
    private int initCodeLimit; // First code limit
    private int codeSize; // Current code size, maximum is 12 bits
    private int nextCode; // Next available code for a new entry
    private int nextCodeLimit; // Increase codeSize when nextCode == limit
    private BitReader br; // Notify when code sizes increases

    public CodeTable() {
      tbl = new int[4096][1];
    }

    private final int add(final int[] indices) {
      if (nextCode < 4096) {
        if (nextCode == nextCodeLimit && codeSize < 12) {
          codeSize++; // Max code size is 12
          br.setNumBits(codeSize);
          nextCodeLimit = (1 << codeSize) - 1; // 2^codeSize - 1
        }
        tbl[nextCode++] = indices;
      }
      return codeSize;
    }

    private final int clear() {
      codeSize = initCodeSize;
      br.setNumBits(codeSize);
      nextCodeLimit = initCodeLimit;
      nextCode = initTableSize; // Don't recreate table, reset pointer
      return codeSize;
    }

    private final void init(final GifFrame fr, final int[] activeColTbl, final BitReader br) {
      this.br = br;
      final int numColors = activeColTbl.length;
      initCodeSize = fr.firstCodeSize;
      initCodeLimit = (1 << initCodeSize) - 1; // 2^initCodeSize - 1
      initTableSize = fr.endOfInfoCode + 1;
      nextCode = initTableSize;
      for (int c = numColors - 1; c >= 0; c--) {
        tbl[c][0] = activeColTbl[c]; // Translated color
      } // A gap may follow with no colors assigned if numCols < CLEAR
      tbl[fr.clearCode] = new int[] {fr.clearCode}; // CLEAR
      tbl[fr.endOfInfoCode] = new int[] {fr.endOfInfoCode}; // EOI
      // Locate transparent color in code table and set to 0
      if (fr.transpColFlag && fr.transpColIndex < numColors) {
        tbl[fr.transpColIndex][0] = 0;
      }
    }
  }

  final class GifFrame {
    // Graphic control extension (optional)
    // Disposal: 0=NO_ACTION, 1=NO_DISPOSAL, 2=RESTORE_BG, 3=RESTORE_PREV
    private int disposalMethod; // 0-3 as above, 4-7 undefined
    private boolean transpColFlag; // 1 Bit
    private int delay; // Unsigned, LSByte first, n * 1/100 * s
    private int transpColIndex; // 1 Byte
    // Image descriptor
    private int x; // Position on the canvas from the left
    private int y; // Position on the canvas from the top
    private int w; // May be smaller than the base image
    private int h; // May be smaller than the base image
    private int wh; // width * height
    private boolean hasLocColTbl; // Has local color table? 1 Bit
    private boolean interlaceFlag; // Is an interlace image? 1 Bit

    @SuppressWarnings("unused")
    private boolean sortFlag; // True if local colors are sorted, 1 Bit

    private int sizeOfLocColTbl; // Size of the local color table, 3 Bits
    private int[] localColTbl; // Local color table (optional)
    // Image data
    private int firstCodeSize; // LZW minimum code size + 1 for CLEAR & EOI
    private int clearCode;
    private int endOfInfoCode;
    private byte[] data; // Holds LZW encoded data
    private BufferedImage img; // Full drawn image, not just the frame area
  }

  public final class GifImage {
    public String header; // Bytes 0-5, GIF87a or GIF89a
    private int w; // Unsigned 16 Bit, least significant byte first
    private int h; // Unsigned 16 Bit, least significant byte first
    private int wh; // Image width * image height
    public boolean hasGlobColTbl; // 1 Bit
    public int colorResolution; // 3 Bits
    public boolean sortFlag; // True if global colors are sorted, 1 Bit
    public int sizeOfGlobColTbl; // 2^(val(3 Bits) + 1), see spec
    public int bgColIndex; // Background color index, 1 Byte
    public int pxAspectRatio; // Pixel aspect ratio, 1 Byte
    public int[] globalColTbl; // Global color table
    private final List<GifFrame> frames = new ArrayList<GifFrame>(64);
    public String appId = ""; // 8 Bytes at in[i+3], usually "NETSCAPE"
    public String appAuthCode = ""; // 3 Bytes at in[i+11], usually "2.0"
    public int repetitions = 0; // 0: infinite loop, N: number of loops
    private BufferedImage img = null; // Currently drawn frame
    private int[] prevPx = null; // Previous frame's pixels
    private final BitReader bits = new BitReader();
    private final CodeTable codes = new CodeTable();
    private Graphics2D g;

    private final int[] decode(final GifFrame fr, final int[] activeColTbl) {
      codes.init(fr, activeColTbl, bits);
      bits.init(fr.data); // Incoming codes
      final int clearCode = fr.clearCode, endCode = fr.endOfInfoCode;
      final int[] out = new int[wh]; // Target image pixel array
      final int[][] tbl = codes.tbl; // Code table
      int outPos = 0; // Next pixel position in the output image array
      codes.clear(); // Init code table
      bits.read(); // Skip leading clear code
      int code = bits.read(); // Read first code
      int[] pixels = tbl[code]; // Output pixel for first code
      arraycopy(pixels, 0, out, outPos, pixels.length);
      outPos += pixels.length;
      try {
        while (true) {
          final int prevCode = code;
          code = bits.read(); // Get next code in stream
          if (code == clearCode) { // After a CLEAR table, there is
            codes.clear(); // no previous code, we need to read
            code = bits.read(); // a new one
            pixels = tbl[code]; // Output pixels
            arraycopy(pixels, 0, out, outPos, pixels.length);
            outPos += pixels.length;
            continue; // Back to the loop with a valid previous code
          } else if (code == endCode) {
            break;
          }
          final int[] prevVals = tbl[prevCode];
          final int[] prevValsAndK = new int[prevVals.length + 1];
          arraycopy(prevVals, 0, prevValsAndK, 0, prevVals.length);
          if (code < codes.nextCode) { // Code table contains code
            pixels = tbl[code]; // Output pixels
            arraycopy(pixels, 0, out, outPos, pixels.length);
            outPos += pixels.length;
            prevValsAndK[prevVals.length] = tbl[code][0]; // K
          } else {
            prevValsAndK[prevVals.length] = prevVals[0]; // K
            arraycopy(prevValsAndK, 0, out, outPos, prevValsAndK.length);
            outPos += prevValsAndK.length;
          }
          codes.add(prevValsAndK); // Previous indices + K
        }
      } catch (final ArrayIndexOutOfBoundsException e) {
      }
      return out;
    }

    private final int[] deinterlace(final int[] src, final GifFrame fr) {
      final int w = fr.w, h = fr.h, wh = fr.wh;
      final int[] dest = new int[src.length];
      // Interlaced images are organized in 4 sets of pixel lines
      final int set2Y = (h + 7) >>> 3; // Line no. = ceil(h/8.0)
      final int set3Y = set2Y + ((h + 3) >>> 3); // ceil(h-4/8.0)
      final int set4Y = set3Y + ((h + 1) >>> 2); // ceil(h-2/4.0)
      // Sets' start indices in source array
      final int set2 = w * set2Y, set3 = w * set3Y, set4 = w * set4Y;
      // Line skips in destination array
      final int w2 = w << 1, w4 = w2 << 1, w8 = w4 << 1;
      // Group 1 contains every 8th line starting from 0
      int from = 0, to = 0;
      for (; from < set2; from += w, to += w8) {
        arraycopy(src, from, dest, to, w);
      } // Group 2 contains every 8th line starting from 4
      for (to = w4; from < set3; from += w, to += w8) {
        arraycopy(src, from, dest, to, w);
      } // Group 3 contains every 4th line starting from 2
      for (to = w2; from < set4; from += w, to += w4) {
        arraycopy(src, from, dest, to, w);
      } // Group 4 contains every 2nd line starting from 1 (biggest group)
      for (to = w; from < wh; from += w, to += w2) {
        arraycopy(src, from, dest, to, w);
      }
      return dest; // All pixel lines have now been rearranged
    }

    private final void drawFrame(final GifFrame fr) {
      // Determine the color table that will be active for this frame
      final int[] activeColTbl = fr.hasLocColTbl ? fr.localColTbl : globalColTbl;
      // Get pixels from data stream
      int[] pixels = decode(fr, activeColTbl);
      if (fr.interlaceFlag) {
        pixels = deinterlace(pixels, fr); // Rearrange pixel lines
      }
      // Create image of type 2=ARGB for frame area
      final BufferedImage frame = new BufferedImage(fr.w, fr.h, 2);
      arraycopy(pixels, 0, ((DataBufferInt) frame.getRaster().getDataBuffer()).getData(), 0, fr.wh);
      // Draw frame area on top of working image
      g.drawImage(frame, fr.x, fr.y, null);

      // Visualize frame boundaries during testing
      // if (DEBUG_MODE) {
      // if (prev != null) {
      // g.setColor(Color.RED); // Previous frame color
      // g.drawRect(prev.x, prev.y, prev.w - 1, prev.h - 1);
      // }
      // g.setColor(Color.GREEN); // New frame color
      // g.drawRect(fr.x, fr.y, fr.w - 1, fr.h - 1);
      // }

      // Keep one copy as "previous frame" in case we need to restore it
      prevPx = new int[wh];
      arraycopy(((DataBufferInt) img.getRaster().getDataBuffer()).getData(), 0, prevPx, 0, wh);

      // Create another copy for the end user to not expose internal state
      fr.img = new BufferedImage(w, h, 2); // 2 = ARGB
      arraycopy(prevPx, 0, ((DataBufferInt) fr.img.getRaster().getDataBuffer()).getData(), 0, wh);

      // Handle disposal of current frame
      if (fr.disposalMethod == 2) {
        // Restore to background color (clear frame area only)
        g.clearRect(fr.x, fr.y, fr.w, fr.h);
      } else if (fr.disposalMethod == 3 && prevPx != null) {
        // Restore previous frame
        arraycopy(prevPx, 0, ((DataBufferInt) img.getRaster().getDataBuffer()).getData(), 0, wh);
      }
    }

    /**
     * Returns the background color of the first frame in this GIF image. If the frame has a local
     * color table, the returned color will be from that table. If not, the color will be from the
     * global color table. Returns 0 if there is neither a local nor a global color table.
     *
     * <p>Index of the current frame, 0 to N-1
     *
     * @return 32 bit ARGB color in the form 0xAARRGGBB
     */
    public final int getBackgroundColor() {
      final GifFrame frame = frames.get(0);
      if (frame.hasLocColTbl) {
        return frame.localColTbl[bgColIndex];
      } else if (hasGlobColTbl) {
        return globalColTbl[bgColIndex];
      }
      return 0;
    }

    /**
     * If not 0, the delay specifies how many hundredths (1/100) of a second to wait before
     * displaying the frame <i>after</i> the current frame.
     *
     * @param index Index of the current frame, 0 to N-1
     * @return Delay as number of hundredths (1/100) of a second
     */
    public final int getDelay(final int index) {
      return frames.get(index).delay;
    }

    /**
     * @param index Index of the frame to return as image, starting from 0. For incremental calls
     *     such as [0, 1, 2, ...] the method's run time is O(1) as only one frame is drawn per call.
     *     For random access calls such as [7, 12, ...] the run time is O(N+1) with N being the
     *     number of previous frames that need to be drawn before N+1 can be drawn on top. Once a
     *     frame has been drawn it is being cached and the run time is more or less O(0) to retrieve
     *     it from the list.
     * @return A BufferedImage for the specified frame.
     */
    public final BufferedImage getFrame(final int index) {
      if (img == null) { // Init
        img = new BufferedImage(w, h, 2); // 2 = ARGB
        g = img.createGraphics();
        g.setBackground(new Color(0, true)); // Transparent color
      }
      GifFrame fr = frames.get(index);
      if (fr.img == null) {
        // Draw all frames until and including the requested frame
        for (int i = 0; i <= index; i++) {
          fr = frames.get(i);
          if (fr.img == null) {
            drawFrame(fr);
          }
        }
      }
      return fr.img;
    }

    /** @return The number of frames contained in this GIF image */
    public final int getFrameCount() {
      return frames.size();
    }

    /** @return The height of the GIF image */
    public final int getHeight() {
      return h;
    }

    /** @return The width of the GIF image */
    public final int getWidth() {
      return w;
    }
  }

  static final boolean DEBUG_MODE = false;

  /**
   * @param in Raw image data as a byte[] array
   * @return A GifImage object exposing the properties of the GIF image.
   * @throws IOException If the image violates the GIF specification or is truncated.
   */
  public static final GifImage read(final byte[] in) throws IOException {
    final GifDecoder decoder = new GifDecoder();
    final GifImage img = decoder.new GifImage();
    GifFrame frame = null; // Currently open frame
    int pos = readHeader(in, img); // Read header, get next byte position
    pos = readLogicalScreenDescriptor(img, in, pos);
    if (img.hasGlobColTbl) {
      img.globalColTbl = new int[img.sizeOfGlobColTbl];
      pos = readColTbl(in, img.globalColTbl, pos);
    }
    while (pos < in.length) {
      final int block = in[pos] & 0xFF;
      switch (block) {
        case 0x21: // Extension introducer
          if (pos + 1 >= in.length) {
            throw new IOException("Unexpected end of file.");
          }
          switch (in[pos + 1] & 0xFF) {
            case 0xFE: // Comment extension
              pos = readTextExtension(in, pos);
              break;
            case 0xFF: // Application extension
              pos = readAppExt(img, in, pos);
              break;
            case 0x01: // Plain text extension
              frame = null; // End of current frame
              pos = readTextExtension(in, pos);
              break;
            case 0xF9: // Graphic control extension
              if (frame == null) {
                frame = decoder.new GifFrame();
                img.frames.add(frame);
              }
              pos = readGraphicControlExt(frame, in, pos);
              break;
            default:
              throw new IOException("Unknown extension at " + pos);
          }
          break;
        case 0x2C: // Image descriptor
          if (frame == null) {
            frame = decoder.new GifFrame();
            img.frames.add(frame);
          }
          pos = readImgDescr(frame, in, pos);
          if (frame.hasLocColTbl) {
            frame.localColTbl = new int[frame.sizeOfLocColTbl];
            pos = readColTbl(in, frame.localColTbl, pos);
          }
          pos = readImgData(frame, in, pos);
          frame = null; // End of current frame
          break;
        case 0x3B: // GIF Trailer
          return img; // Found trailer, finished reading.
        default:
          // Unknown block. The image is corrupted. Strategies: a) Skip
          // and wait for a valid block. Experience: It'll get worse. b)
          // Throw exception. c) Return gracefully if we are almost done
          // processing. The frames we have so far should be error-free.
          final double progress = 1.0 * pos / in.length;
          if (progress < 0.9) {
            throw new IOException("Unknown block at: " + pos);
          }
          pos = in.length; // Exit loop
      }
    }
    return img;
  }

  /**
   * @param is Image data as input stream. This method will read from the input stream's current
   *     position. It will not reset the position before reading and won't reset or close the stream
   *     afterwards. Call these methods before and after calling this method as needed.
   * @return A GifImage object exposing the properties of the GIF image.
   * @throws IOException If an I/O error occurs, the image violates the GIF specification or the GIF
   *     is truncated.
   */
  public static final GifImage read(final InputStream is) throws IOException {
    final byte[] data = new byte[is.available()];
    is.read(data, 0, data.length);
    return read(data);
  }

  /**
   * Empty application extension object
   *
   * @param in Raw data
   * @param i Index of the first byte of the application extension
   * @return Index of the first byte after this extension
   */
  static final int readAppExt(final GifImage img, final byte[] in, int i) {
    img.appId = new String(in, i + 3, 8); // should be "NETSCAPE"
    img.appAuthCode = new String(in, i + 11, 3); // should be "2.0"
    i += 14; // Go to sub-block size, it's value should be 3
    final int subBlockSize = in[i] & 0xFF;
    // The only app extension widely used is NETSCAPE, it's got 3 data bytes
    if (subBlockSize == 3) {
      // in[i+1] should have value 01, in[i+5] should be block terminator
      img.repetitions = in[i + 2] & 0xFF | in[i + 3] & 0xFF << 8; // Short
      return i + 5;
    } // Skip unknown application extensions
    while ((in[i] & 0xFF) != 0) { // While sub-block size != 0
      i += (in[i] & 0xFF) + 1; // Skip to next sub-block
    }
    return i + 1;
  }

  /**
   * @param in Raw data
   * @param colors Pre-initialized target array to store ARGB colors
   * @param i Index of the color table's first byte
   * @return Index of the first byte after the color table
   */
  static final int readColTbl(final byte[] in, final int[] colors, int i) {
    final int numColors = colors.length;
    for (int c = 0; c < numColors; c++) {
      final int a = 0xFF; // Alpha 255 (opaque)
      final int r = in[i++] & 0xFF; // 1st byte is red
      final int g = in[i++] & 0xFF; // 2nd byte is green
      final int b = in[i++] & 0xFF; // 3rd byte is blue
      colors[c] = ((a << 8 | r) << 8 | g) << 8 | b;
    }
    return i;
  }

  /**
   * Graphic control extension object
   *
   * @param in Raw data
   * @param i Index of the extension introducer
   * @return Index of the first byte after this block
   */
  static final int readGraphicControlExt(final GifFrame fr, final byte[] in, final int i) {
    fr.disposalMethod = (in[i + 3] & 0b00011100) >>> 2; // Bits 4-2
    fr.transpColFlag = (in[i + 3] & 1) == 1; // Bit 0
    fr.delay = in[i + 4] & 0xFF | (in[i + 5] & 0xFF) << 8; // 16 bit LSB
    fr.transpColIndex = in[i + 6] & 0xFF; // Byte 6
    return i + 8; // Skipped byte 7 (blockTerminator), as it's always 0x00
  }

  /**
   * @param in Raw data
   * @param img The GifImage object that is currently read
   * @return Index of the first byte after this block
   * @throws IOException If the GIF header/trailer is missing, incomplete or unknown
   */
  static final int readHeader(final byte[] in, final GifImage img) throws IOException {
    if (in.length < 6) { // Check first 6 bytes
      throw new IOException("Image is truncated.");
    }
    img.header = new String(in, 0, 6);
    if (!img.header.equals("GIF87a") && !img.header.equals("GIF89a")) {
      throw new IOException("Invalid GIF header.");
    }
    return 6;
  }

  /**
   * @param fr The GIF frame to whom this image descriptor belongs
   * @param in Raw data
   * @param i Index of the first byte of this block, i.e. the minCodeSize
   * @return
   */
  static final int readImgData(final GifFrame fr, final byte[] in, int i) {
    final int fileSize = in.length;
    final int minCodeSize = in[i++] & 0xFF; // Read code size, go to block
    final int clearCode = 1 << minCodeSize; // CLEAR = 2^minCodeSize
    fr.firstCodeSize = minCodeSize + 1; // Add 1 bit for CLEAR and EOI
    fr.clearCode = clearCode;
    fr.endOfInfoCode = clearCode + 1;
    final int imgDataSize = readImgDataSize(in, i);
    final byte[] imgData = new byte[imgDataSize + 2];
    int imgDataPos = 0;
    int subBlockSize = in[i] & 0xFF;
    while (subBlockSize > 0) { // While block has data
      try { // Next line may throw exception if sub-block size is fake
        final int nextSubBlockSizePos = i + subBlockSize + 1;
        final int nextSubBlockSize = in[nextSubBlockSizePos] & 0xFF;
        arraycopy(in, i + 1, imgData, imgDataPos, subBlockSize);
        imgDataPos += subBlockSize; // Move output data position
        i = nextSubBlockSizePos; // Move to next sub-block size
        subBlockSize = nextSubBlockSize;
      } catch (final Exception e) {
        // Sub-block exceeds file end, only use remaining bytes
        subBlockSize = fileSize - i - 1; // Remaining bytes
        arraycopy(in, i + 1, imgData, imgDataPos, subBlockSize);
        imgDataPos += subBlockSize; // Move output data position
        i += subBlockSize + 1; // Move to next sub-block size
        break;
      }
    }
    fr.data = imgData; // Holds LZW encoded data
    i++; // Skip last sub-block size, should be 0
    return i;
  }

  static final int readImgDataSize(final byte[] in, int i) {
    final int fileSize = in.length;
    int imgDataPos = 0;
    int subBlockSize = in[i] & 0xFF;
    while (subBlockSize > 0) { // While block has data
      try { // Next line may throw exception if sub-block size is fake
        final int nextSubBlockSizePos = i + subBlockSize + 1;
        final int nextSubBlockSize = in[nextSubBlockSizePos] & 0xFF;
        imgDataPos += subBlockSize; // Move output data position
        i = nextSubBlockSizePos; // Move to next sub-block size
        subBlockSize = nextSubBlockSize;
      } catch (final Exception e) {
        // Sub-block exceeds file end, only use remaining bytes
        subBlockSize = fileSize - i - 1; // Remaining bytes
        imgDataPos += subBlockSize; // Move output data position
        break;
      }
    }
    return imgDataPos;
  }

  /**
   * @param fr The GIF frame to whom this image descriptor belongs
   * @param in Raw data
   * @param i Index of the image separator, i.e. the first block byte
   * @return Index of the first byte after this block
   */
  static final int readImgDescr(final GifFrame fr, final byte[] in, int i) {
    fr.x = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 1-2: left
    fr.y = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 3-4: top
    fr.w = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 5-6: width
    fr.h = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 7-8: height
    fr.wh = fr.w * fr.h;
    final byte b = in[++i]; // Byte 9 is a packed byte
    fr.hasLocColTbl = (b & 0b10000000) >>> 7 == 1; // Bit 7
    fr.interlaceFlag = (b & 0b01000000) >>> 6 == 1; // Bit 6
    fr.sortFlag = (b & 0b00100000) >>> 5 == 1; // Bit 5
    final int colTblSizePower = (b & 7) + 1; // Bits 2-0
    fr.sizeOfLocColTbl = 1 << colTblSizePower; // 2^(N+1), As per the spec
    return ++i;
  }

  /**
   * @param img
   * @param i Start index of this block.
   * @return Index of the first byte after this block.
   */
  static final int readLogicalScreenDescriptor(final GifImage img, final byte[] in, final int i) {
    img.w = in[i] & 0xFF | (in[i + 1] & 0xFF) << 8; // 16 bit, LSB 1st
    img.h = in[i + 2] & 0xFF | (in[i + 3] & 0xFF) << 8; // 16 bit
    img.wh = img.w * img.h;
    final byte b = in[i + 4]; // Byte 4 is a packed byte
    img.hasGlobColTbl = (b & 0b10000000) >>> 7 == 1; // Bit 7
    final int colResPower = ((b & 0b01110000) >>> 4) + 1; // Bits 6-4
    img.colorResolution = 1 << colResPower; // 2^(N+1), As per the spec
    img.sortFlag = (b & 0b00001000) >>> 3 == 1; // Bit 3
    final int globColTblSizePower = (b & 7) + 1; // Bits 0-2
    img.sizeOfGlobColTbl = 1 << globColTblSizePower; // 2^(N+1), see spec
    img.bgColIndex = in[i + 5] & 0xFF; // 1 Byte
    img.pxAspectRatio = in[i + 6] & 0xFF; // 1 Byte
    return i + 7;
  }

  /**
   * @param in Raw data
   * @param pos Index of the extension introducer
   * @return Index of the first byte after this block
   */
  static final int readTextExtension(final byte[] in, final int pos) {
    int i = pos + 2; // Skip extension introducer and label
    int subBlockSize = in[i++] & 0xFF;
    while (subBlockSize != 0 && i < in.length) {
      i += subBlockSize;
      subBlockSize = in[i++] & 0xFF;
    }
    return i;
  }

  public Animation<TextureRegion> getAnimation(Animation.PlayMode playMode) throws IOException {
    int nrFrames = getFrameCount();
    Pixmap frame = getFrame(0);
    int width = frame.getWidth();
    int height = frame.getHeight();
    int vzones = (int) Math.sqrt((double) nrFrames);
    int hzones = vzones;

    while (vzones * hzones < nrFrames) vzones++;

    int v, h;

    Pixmap target = new Pixmap(width * hzones, height * vzones, Pixmap.Format.RGBA8888);

    for (h = 0; h < hzones; h++) {
      for (v = 0; v < vzones; v++) {
        int frameID = v + h * vzones;
        if (frameID < nrFrames) {
          frame = getFrame(frameID);
          target.drawPixmap(frame, h * width, v * height);
        }
      }
    }

    Texture texture = new Texture(target);
    Array<TextureRegion> texReg = new Array<TextureRegion>();

    for (h = 0; h < hzones; h++) {
      for (v = 0; v < vzones; v++) {
        int frameID = v + h * vzones;
        if (frameID < nrFrames) {
          TextureRegion tr = new TextureRegion(texture, h * width, v * height, width, height);
          texReg.add(tr);
        }
      }
    }
    float frameDuration = (float) getDelay(0);
    frameDuration /= 100; // convert milliseconds into seconds
    Animation<TextureRegion> result = new Animation<TextureRegion>(frameDuration, texReg, playMode);

    return result;
  }

  private int getDelay(int i) {
    return image.getDelay(0);
  }

  private Pixmap getFrame(int i) throws IOException {
    var bytes = ImageUtil.imageToBytes(image.getFrame(i), "png");
    // without imageutil there seem to be some issues with tranparency  for some images.
    // (black background instead of tranparent)
    // var bytes = AssetManager.getAsset(key).getImage();
    return new Pixmap(bytes, 0, bytes.length);
  }

  private int getFrameCount() {
    return image.getFrameCount();
  }

  public static Animation<TextureRegion> loadGIFAnimation(
      Animation.PlayMode playMode, InputStream is) {
    try {
      var dec = new GifDecoder(is);
      return dec.getAnimation(playMode);

    } catch (Exception e) {
      return null;
    }
  }
}
