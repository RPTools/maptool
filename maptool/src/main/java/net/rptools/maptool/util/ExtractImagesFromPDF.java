/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.maptool.util;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;

/**
 * Extracts the images from a PDF file.
 *
 * @author Ben Litchfield
 */
public final class ExtractImagesFromPDF {
	private static final List<String> JPEG = Arrays.asList(
			COSName.DCT_DECODE.getName(),
			COSName.DCT_DECODE_ABBREVIATION.getName());

	private static final boolean DIRECT_JPEG = false; // Forces the direct extraction of JPEG images regardless of colorspace.
	private String prefix; // "gianstlayer_maps";
	private String outDir; // "D:/Development/Workspace - RPTools 1.4.x/PDFbox-Test-2.0/test_pdfs/" + prefix + "/";

	private static final File tmpDir = AppUtil.getTmpDir(); //new File(System.getProperty("java.io.tmpdir"));
	private File finalTempDir;

	private static Set<String> imageTracker = new HashSet<String>(); // static Set tracks extracted images across Threads.
	private List<File> fileList;
	private int imageCounter = 1;
	private int pageCount = 0;
	private String pageNumberFormat = "%01d";
	private String pdfFileHash = null;
	private PDDocument document = null;

	public ExtractImagesFromPDF(File pdfFile, boolean forceRescan) throws IOException {
		//System.out.println("***************************************************************************");
		//System.out.println("ExtractImagesFromPDF called...");

		prefix = FileUtil.getNameWithoutExtension(pdfFile.getName());
		outDir = tmpDir + "/" + prefix + "/";
		pdfFileHash = outDir + "hash_" + pdfFile.hashCode() + ".txt";
		finalTempDir = new File(outDir);

		if (forceRescan)
			FileUtils.deleteQuietly(finalTempDir);

		finalTempDir.mkdirs();

		if (isExtracted() && !forceRescan)
			return;

		//document = PDDocument.load(pdfFile, MemoryUsageSetting.setupMixed((1024^2))); // doesn't seem to help?
		document = PDDocument.load(pdfFile);
		//		AccessPermission ap = document.getCurrentAccessPermission();
		//		if (!ap.canExtractContent()) {
		//			throw new IOException("You do not have permission to extract images.");
		//		}

		fileList = new ArrayList<File>();
		pageCount = document.getNumberOfPages();

		if (pageCount >= 100) {
			pageNumberFormat = "%03d";
		} else if (pageCount >= 10) {
			pageNumberFormat = "%02d";
		}
	}

	public void markComplete() {
		imageCounter = 0;
		imageTracker.clear();
		FileOutputStream out;

		try {
			out = new FileOutputStream(pdfFileHash);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(finalTempDir)));
	}

	public void close() {
		if (document != null) {
			try {
				document.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public File getTempDir() {
		return finalTempDir;
	}

	public boolean isExtracted() {
		File fileCheck = new File(pdfFileHash);

		if (fileCheck.exists())
			return true;
		else
			return false;
	}

	public int getPageCount() {
		close();
		return pageCount;
	}

	/*
	 * public void extractAll_nope() throws IOException { PDDocumentCatalog
	 * catalog = document.getDocumentCatalog(); int imgCount = 1; int counter =
	 * 1;
	 * MapTool.getFrame().getAssetPanel().setImagePanelProgressMax(pageCount);
	 * 
	 * for (Object pageObj : catalog.getPages()) { PDPage page = (PDPage)
	 * pageObj; PDResources resources = page.getResources();
	 * 
	 * MapTool.getFrame().getAssetPanel().setImagePanelProgress(counter++);
	 * 
	 * for (COSName cosname : resources.getXObjectNames()) { System.out.println(
	 * "Parsing COS: " + cosname.getName());
	 * 
	 * if (resources.isImageXObject(cosname)) { PDXObject xObject =
	 * resources.getXObject(cosname); write2file((PDImageXObject) xObject,
	 * outDir + prefix + "_" + imgCount++, DIRECT_JPEG); System.out.println(
	 * "Writing PDImageXObject"); }
	 * 
	 * }
	 * 
	 * } }
	 */

	public List<File> extractPage(int pageNumber) throws IOException {
		// getPage by index, not actual "Page Number"
		PDPage page = document.getPage(pageNumber - 1);
		ImageGraphicsEngine extractor = new ImageGraphicsEngine(page);
		extractor.run(pageNumber);
		extractAnnotationImages(page, pageNumber, outDir + prefix + "%s");

		//PDResources resources = page.getResources();

		close();

		return fileList;
	}

	public File extractAll() throws IOException {
		int pageNumber = 1;
		//MapTool.getFrame().getAssetPanel().setImagePanelProgressMax(pageCount);

		if (!isExtracted()) {
			for (PDPage page : document.getPages()) {
				ImageGraphicsEngine extractor = new ImageGraphicsEngine(page);
				extractor.run(pageNumber);
				extractAnnotationImages(page, pageNumber, outDir + prefix + "%s");

				//MapTool.getFrame().getAssetPanel().setImagePanelProgress(pageNumber++);
			}
		}

		close();
		return finalTempDir;
	}

	/*
	 * Jamz: A note on what we are doing here...
	 * 
	 * Paizo's Interactive PDF's (amongst others) are sneaky and put map images
	 * in the PDF as a "button" with an image resource. So we need to walk
	 * through all the forms to find the buttons, then walk through all the
	 * button resources for the images. Also, a 'Button Down' may hold the
	 * 'Grid' version of the map and 'Button Up' may hold the 'Non-Grid'
	 * version. There may also be Player vs GM versions of each for a total of
	 * up to 4 images per button!
	 * 
	 * This is the REAL beauty of this function as currently no other tools
	 * outside of Full Acrobat extracts these raw images!
	 * 
	 */
	private void extractAnnotationImages(PDPage page, int pageNumber, String pageFormat) throws IOException {
		int imgCount = 1;
		String pageNumberFormatted = String.format(pageNumberFormat, pageNumber);

		for (PDAnnotation annotation : page.getAnnotations()) {
			String annotationFormat = annotation.getAnnotationName() != null && annotation.getAnnotationName().length() > 0
					? String.format(pageFormat, "_page " + pageNumberFormatted + "_" + annotation.getAnnotationName() + "%s", "%s")
					: String.format(pageFormat, "_page " + pageNumberFormatted + "_" + imgCount++ + "%s", "%s");
			extractAnnotationImages(annotation, annotationFormat);
		}
	}

	private void extractAnnotationImages(PDAnnotation annotation, String annotationFormat) throws IOException {
		PDAppearanceDictionary appearance = annotation.getAppearance();

		if (appearance == null)
			return;

		extractAnnotationImages(appearance.getDownAppearance(), String.format(annotationFormat, "-Down%s", "%s"));
		extractAnnotationImages(appearance.getNormalAppearance(), String.format(annotationFormat, "-Normal%s", "%s"));
		extractAnnotationImages(appearance.getRolloverAppearance(), String.format(annotationFormat, "-Rollover%s", "%s"));
	}

	public void extractAnnotationImages(PDAppearanceEntry appearance, String appearanceFormat) throws IOException {
		PDResources resources = appearance.getAppearanceStream().getResources();
		if (resources == null)
			return;

		//resources.isImageXObject(name)
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
		if (resources == null)
			return;

		//resources.isImageXObject(name)
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
		//String filename = String.format(imageFormat, "", image.getSuffix());
		String filename = String.format(imageFormat, "", "");
		write2file(image, filename);
	}

	// Instead of capturing the draw method we will use it to write to a file
	private class ImageGraphicsEngine extends PDFGraphicsStreamEngine {
		int pageNumber;

		protected ImageGraphicsEngine(PDPage page) throws IOException {
			super(page);
		}

		public void run(int pageNumber) throws IOException {
			this.pageNumber = pageNumber;
			processPage(getPage());
		}

		@Override
		public void drawImage(PDImage pdImage) throws IOException {
			if (pdImage instanceof PDImageXObject) {
				//PDImageXObject xobject = (PDImageXObject) pdImage;

				// save image
				String pageNumberFormatted = String.format(pageNumberFormat, pageNumber);
				String fileName = outDir + prefix + "_page " + pageNumberFormatted + "_" + imageCounter++;
				write2file(pdImage, fileName);
			}
		}

		@Override
		public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3)
				throws IOException {

		}

		@Override
		public void clip(int windingRule) throws IOException {

		}

		@Override
		public void moveTo(float x, float y) throws IOException {

		}

		@Override
		public void lineTo(float x, float y) throws IOException {

		}

		@Override
		public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
				throws IOException {

		}

		@Override
		public Point2D getCurrentPoint() throws IOException {
			return new Point2D.Float(0, 0);
		}

		@Override
		public void closePath() throws IOException {

		}

		@Override
		public void endPath() throws IOException {

		}

		@Override
		public void strokePath() throws IOException {

		}

		@Override
		public void fillPath(int windingRule) throws IOException {

		}

		@Override
		public void fillAndStrokePath(int windingRule) throws IOException {

		}

		@Override
		public void shadingFill(COSName shadingName) throws IOException {

		}
	}

	/**
	 * Writes the image to a file with the filename + an appropriate suffix,
	 * like "Image.jpg". The suffix is automatically set by the
	 * 
	 * @param filename
	 *            the filename
	 * @throws IOException
	 *             When somethings wrong with the corresponding file.
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
			//System.out.println("*** Skipping Duplicate image [" + filename + "]");
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
				System.out.println("*** Found Duplicate file [" + filename + "]");
				filename += md5Key.toString() + "." + fileSuffix;
				return;
			}

			//System.out.println("Saving image: " + filename);
			out = new FileOutputStream(filename);

			if (image != null) {
				if (pdfSuffix.equalsIgnoreCase("jpg")) {
					String colorSpaceName = pdImage.getColorSpace().getName();
					if (DIRECT_JPEG || PDDeviceGray.INSTANCE.getName().equals(colorSpaceName) ||
							PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName)) {
						// RGB or Gray colorspace: get and write the unmodifiedJPEG stream
						InputStream data = pdImage.createInputStream(JPEG);
						IOUtils.copy(data, out);
						IOUtils.closeQuietly(data);
					} else {
						// for CMYK and other "unusual" colorspaces, the JPEG will be converted
						ImageIOUtil.writeImage(image, fileSuffix, out);
					}
				} else {
					// Most likely a JPEG2000, lets try writing it out...
					ImageIOUtil.writeImage(image, fileSuffix, out);
				}
				fileList.add(new File(filename));
			}
			out.flush();
			image.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}