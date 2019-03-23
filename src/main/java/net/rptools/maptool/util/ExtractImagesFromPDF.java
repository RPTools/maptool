/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;

/**
 * Extract all images from a PDF using Apache's PdfBox 2.0 This will also walk through all
 * annotations and extract those images as well which is key, some interactive PDF's, such as from
 * Paizo, store different versions of maps as button icons, which will not normally extract using
 * other methods.
 *
 * @author Jamz
 */
public final class ExtractImagesFromPDF {
  private static final Logger log = LogManager.getLogger(ExtractImagesFromPDF.class);

  private static final List<String> JPEG =
      Arrays.asList(COSName.DCT_DECODE.getName(), COSName.DCT_DECODE_ABBREVIATION.getName());

  private static final boolean DIRECT_JPEG =
      false; // Forces the direct extraction of JPEG images regardless of colorspace.
  private String prefix;
  private String outDir;

  private static final File tmpDir =
      AppUtil.getTmpDir(); // new File(System.getProperty("java.io.tmpdir"));
  private File finalTempDir;

  private static Set<String> imageTracker =
      new HashSet<String>(); // static Set tracks extracted images across Threads.

  private List<File> fileList;
  private int imageCounter = 1;
  private int pageCount = 0;
  private String pageNumberFormat = "%01d";
  private String pdfFileHash = null;
  private PDDocument document = null;

  public ExtractImagesFromPDF(File pdfFile, boolean forceRescan) throws IOException {
    prefix = FileUtil.getNameWithoutExtension(pdfFile.getName());
    outDir = tmpDir + "/" + prefix + "/";
    pdfFileHash = outDir + "hash_" + pdfFile.hashCode() + ".txt";
    finalTempDir = new File(outDir);

    if (forceRescan) FileUtils.deleteQuietly(finalTempDir);

    finalTempDir.mkdirs();

    if (isExtracted() && !forceRescan) return;

    document = PDDocument.load(pdfFile);

    fileList = new ArrayList<File>();
    pageCount = document.getNumberOfPages();

    if (pageCount >= 100) {
      pageNumberFormat = "%03d";
    } else if (pageCount >= 10) {
      pageNumberFormat = "%02d";
    }
  }

  public void markComplete(boolean isInterupted) {
    imageCounter = 0;
    imageTracker.clear();

    if (!isInterupted) {
      FileOutputStream out;

      try {
        out = new FileOutputStream(pdfFileHash);
        out.flush();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(finalTempDir)));
  }

  public void close() {
    if (document != null) {
      try {
        document.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public File getTempDir() {
    return finalTempDir;
  }

  public boolean isExtracted() {
    File fileCheck = new File(pdfFileHash);

    if (fileCheck.exists()) return true;
    else return false;
  }

  public int getPageCount() {
    close();
    return pageCount;
  }

  public List<File> extractPage(int pageNumber) throws IOException {
    // getPage by index, not actual "Page Number"
    PDPage page = document.getPage(pageNumber - 1);
    extractAnnotationImages(page, pageNumber, outDir + prefix + "%s");
    getImagesFromResources(page.getResources(), pageNumber);

    close();

    return fileList;
  }

  private void getImagesFromResources(PDResources resources, int pageNumber) throws IOException {
    // Testing various Pathfinder PDF's, various page elements like borders and backgrounds
    // generally come first...
    // ...so lets sort them to the bottom and get the images we really want to the top of the
    // TilePane!
    ArrayList<COSName> xObjectNamesReversed = new ArrayList<>();

    for (COSName xObjectName : resources.getXObjectNames()) {
      xObjectNamesReversed.add(xObjectName);
    }

    Collections.reverse(xObjectNamesReversed);

    for (COSName xObjectName : xObjectNamesReversed) {
      PDXObject xObject = resources.getXObject(xObjectName);

      if (xObject instanceof PDFormXObject) {
        getImagesFromResources(((PDFormXObject) xObject).getResources(), pageNumber);
      } else if (xObject instanceof PDImageXObject) {
        log.debug("Extracting image... " + xObjectName.getName());

        String pageNumberFormatted = String.format(pageNumberFormat, pageNumber);
        String fileName = outDir + prefix + "_page " + pageNumberFormatted + "_" + imageCounter++;
        write2file((PDImageXObject) xObject, fileName);
      }
    }
  }

  /*
   * Jamz: A note on what we are doing here...
   *
   * Paizo's Interactive PDF's (amongst others) are sneaky and put map images in the PDF as a "button" with an image resource. So we need to walk through all the forms to find the buttons, then walk
   * through all the button resources for the images. Also, a 'Button Down' may hold the 'Grid' version of the map and 'Button Up' may hold the 'Non-Grid' version. There may also be Player vs GM
   * versions of each for a total of up to 4 images per button!
   *
   * This is the REAL beauty of this function as currently no other tools outside of Full Acrobat extracts these raw images!
   *
   */
  private void extractAnnotationImages(PDPage page, int pageNumber, String pageFormat)
      throws IOException {
    int imgCount = 1;
    String pageNumberFormatted = String.format(pageNumberFormat, pageNumber);

    for (PDAnnotation annotation : page.getAnnotations()) {
      String annotationFormat =
          annotation.getAnnotationName() != null && annotation.getAnnotationName().length() > 0
              ? String.format(
                  pageFormat,
                  "_page " + pageNumberFormatted + "_" + annotation.getAnnotationName() + "%s",
                  "%s")
              : String.format(
                  pageFormat, "_page " + pageNumberFormatted + "_" + imgCount++ + "%s", "%s");
      extractAnnotationImages(annotation, annotationFormat);
    }
  }

  private void extractAnnotationImages(PDAnnotation annotation, String annotationFormat)
      throws IOException {
    PDAppearanceDictionary appearance = annotation.getAppearance();

    if (appearance == null) return;

    extractAnnotationImages(
        appearance.getDownAppearance(), String.format(annotationFormat, "-Down%s", "%s"));
    extractAnnotationImages(
        appearance.getNormalAppearance(), String.format(annotationFormat, "-Normal%s", "%s"));
    extractAnnotationImages(
        appearance.getRolloverAppearance(), String.format(annotationFormat, "-Rollover%s", "%s"));
  }

  public void extractAnnotationImages(PDAppearanceEntry appearance, String appearanceFormat)
      throws IOException {
    PDResources resources = appearance.getAppearanceStream().getResources();
    if (resources == null) return;

    for (COSName cosname : resources.getXObjectNames()) {
      PDXObject xObject = resources.getXObject(cosname);

      String xObjectFormat = String.format(appearanceFormat, "-" + cosname.getName() + "%s", "%s");
      if (xObject instanceof PDFormXObject)
        extractAnnotationImages((PDFormXObject) xObject, xObjectFormat);
      else if (xObject instanceof PDImageXObject)
        extractAnnotationImages((PDImageXObject) xObject, xObjectFormat);
    }
  }

  public void extractAnnotationImages(PDFormXObject form, String imageFormat) throws IOException {
    PDResources resources = form.getResources();
    if (resources == null) return;

    for (COSName cosname : resources.getXObjectNames()) {
      PDXObject xObject = resources.getXObject(cosname);

      String xObjectFormat = String.format(imageFormat, "-" + cosname.getName() + "%s", "%s");
      if (xObject instanceof PDFormXObject)
        extractAnnotationImages((PDFormXObject) xObject, xObjectFormat);
      else if (xObject instanceof PDImageXObject)
        extractAnnotationImages((PDImageXObject) xObject, xObjectFormat);
    }
  }

  public void extractAnnotationImages(PDImageXObject image, String imageFormat) throws IOException {
    String filename = String.format(imageFormat, "", "");
    write2file(image, filename);
  }

  /**
   * Writes the image to a file with the filename + an appropriate suffix, like "Image.jpg". The
   * suffix is automatically set by the
   *
   * @param filename the filename
   * @throws IOException When something wrong with the corresponding file.
   */
  private void write2file(PDImage pdImage, String filename) throws IOException {
    String pdfSuffix = pdImage.getSuffix();
    String fileSuffix = pdfSuffix;

    if (pdfSuffix == null) {
      fileSuffix = "png";
    }
    if (pdfSuffix.equalsIgnoreCase("jpx") || pdfSuffix.equalsIgnoreCase("tiff")) {
      fileSuffix = "jpg";
    }

    filename += "." + fileSuffix;
    BufferedImage image = pdImage.getImage();
    MD5Key md5Key = new MD5Key(ImageUtil.imageToBytes(image, fileSuffix));

    // Lets see if we can find dupes...
    if (imageTracker.contains(md5Key.toString())) {
      log.debug("*** Skipping Duplicate image [" + filename + "]");
      filename += md5Key.toString() + "." + fileSuffix;
      return;
    } else {
      imageTracker.add(md5Key.toString());
    }

    FileOutputStream out = null;
    File fileCheck = null;
    try {
      fileCheck = new File(filename);
      if (fileCheck.exists()) {
        filename += md5Key.toString() + "." + fileSuffix;
        return;
      }

      out = new FileOutputStream(filename);

      if (image != null) {
        if (pdfSuffix.equalsIgnoreCase("jpg")) {
          String colorSpaceName = pdImage.getColorSpace().getName();
          if (DIRECT_JPEG
              || PDDeviceGray.INSTANCE.getName().equals(colorSpaceName)
              || PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName)) {
            // RGB or Gray colorspace: get and write the unmodifiedJPEG stream
            InputStream data = pdImage.createInputStream(JPEG);
            IOUtils.copy(data, out);
            IOUtils.closeQuietly(data);
          } else {
            // for CMYK and other "unusual" colorspaces, the JPEG will be converted
            ImageIO.write(image, fileSuffix, out);
          }
        } else {
          // Most likely a JPEG2000, lets try writing it out...
          ImageIO.write(image, fileSuffix, out);
        }
        fileList.add(new File(filename));
      }
      out.flush();
      image.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
}
