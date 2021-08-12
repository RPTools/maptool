package net.rptools.maptool.box2d;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import net.rptools.lib.image.ImageUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

// from https://stackoverflow.com/questions/8933893/convert-each-animated-gif-frame-to-a-separate-bufferedimage

public class GifDecoder2 {
  public GifDecoder2(InputStream is) throws IOException {
    frames = readGif(is);
  }

  private ImageFrame[] readGif(InputStream stream) throws IOException {
    ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);

    ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
    reader.setInput(ImageIO.createImageInputStream(stream));

    int lastx = 0;
    int lasty = 0;

    int width = -1;
    int height = -1;

    IIOMetadata metadata = reader.getStreamMetadata();

    Color backgroundColor = null;

    if(metadata != null) {
      IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

      NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
      NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

      if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0){
        IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

        if (screenDescriptor != null){
          width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
          height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
        }
      }

      if (globalColorTable != null && globalColorTable.getLength() > 0){
        IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

        if (colorTable != null) {
          String bgIndex = colorTable.getAttribute("backgroundColorIndex");

          IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
          while (colorEntry != null) {
            if (colorEntry.getAttribute("index").equals(bgIndex)) {
              int red = Integer.parseInt(colorEntry.getAttribute("red"));
              int green = Integer.parseInt(colorEntry.getAttribute("green"));
              int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

              backgroundColor = new Color(red, green, blue);
              break;
            }

            colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
          }
        }
      }
    }

    BufferedImage master = null;
    boolean hasBackround = false;

    for (int frameIndex = 0;; frameIndex++) {
      BufferedImage image;
      try{
        image = reader.read(frameIndex);
      }catch (IndexOutOfBoundsException io){
        break;
      }

      if (width == -1 || height == -1){
        width = image.getWidth();
        height = image.getHeight();
      }

      IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
      IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
      NodeList children = root.getChildNodes();

      int delay = Integer.valueOf(gce.getAttribute("delayTime"));

      String disposal = gce.getAttribute("disposalMethod");

      if (master == null){
        master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        master.createGraphics().setColor(backgroundColor);
        master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

        hasBackround = image.getWidth() == width && image.getHeight() == height;

        master.createGraphics().drawImage(image, 0, 0, null);
      }else{
        int x = 0;
        int y = 0;

        for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++){
          Node nodeItem = children.item(nodeIndex);

          if (nodeItem.getNodeName().equals("ImageDescriptor")){
            NamedNodeMap map = nodeItem.getAttributes();

            x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
            y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
          }
        }

        if (disposal.equals("restoreToPrevious")){
          BufferedImage from = null;
          for (int i = frameIndex - 1; i >= 0; i--){
            if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0){
              from = frames.get(i).getImage();
              break;
            }
          }

          {
            ColorModel model = from.getColorModel();
            boolean alpha = from.isAlphaPremultiplied();
            WritableRaster raster = from.copyData(null);
            master = new BufferedImage(model, raster, alpha, null);
          }
        }else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null){
          if (!hasBackround || frameIndex > 1){
            master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).getWidth(), frames.get(frameIndex - 1).getHeight());
          }
        }
        master.createGraphics().drawImage(image, x, y, null);

        lastx = x;
        lasty = y;
      }

      {
        BufferedImage copy;

        {
          ColorModel model = master.getColorModel();
          boolean alpha = master.isAlphaPremultiplied();
          WritableRaster raster = master.copyData(null);
          copy = new BufferedImage(model, raster, alpha, null);
        }
        frames.add(new ImageFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
      }

      master.flush();
    }
    reader.dispose();

    return frames.toArray(new ImageFrame[frames.size()]);
  }

  private static class ImageFrame {
    private final int delay;
    private final BufferedImage image;
    private final String disposal;
    private final int width, height;

    public ImageFrame (BufferedImage image, int delay, String disposal, int width, int height){
      this.image = image;
      this.delay = delay;
      this.disposal = disposal;
      this.width = width;
      this.height = height;
    }

    public ImageFrame (BufferedImage image){
      this.image = image;
      this.delay = -1;
      this.disposal = null;
      this.width = -1;
      this.height = -1;
    }

    public BufferedImage getImage() {
      return image;
    }

    public int getDelay() {
      return delay;
    }

    public String getDisposal() {
      return disposal;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }
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
    return frames[i].getDelay();
  }

  private Pixmap getFrame(int i) throws IOException {
    var bytes = ImageUtil.imageToBytes(frames[i].getImage(), "png");
    // without imageutil there seem to be some issues with tranparency  for some images.
    // (black background instead of tranparent)
    //var bytes = AssetManager.getAsset(key).getImage();
    return new Pixmap(bytes, 0, bytes.length);
  }

  private ImageFrame[] frames;

  private int getFrameCount() {
    return frames.length;
  }

  public static Animation<TextureRegion> loadGIFAnimation(Animation.PlayMode playMode, InputStream is) {
    try {
      var dec = new GifDecoder2(is);
      return dec.getAnimation(playMode);

    } catch (Exception e) {
      return null;
    }
  }
}
