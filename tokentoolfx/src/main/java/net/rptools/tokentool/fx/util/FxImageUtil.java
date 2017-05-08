package net.rptools.tokentool.fx.util;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NodeList;

import com.twelvemonkeys.image.ResampleOp;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageReader;
import com.twelvemonkeys.imageio.plugins.psd.PSDMetadata;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class FxImageUtil {
	private static final int THUMB_SIZE = 100;
	private static final int COLOR_THRESHOLD = 1;

	public static ImageView getOverlayThumb(ImageView thumbView, Path filePath) throws IOException {
		return getImage(thumbView, filePath, true, THUMB_SIZE);
	}

	public static ImageView getOverlayImage(ImageView thumbView, Path overlayFileURI) throws IOException {
		return getImage(thumbView, overlayFileURI, true, 0);
	}

	public static ImageView getMaskImage(ImageView thumbView, Path overlayFileURI) throws IOException {
		return getImage(thumbView, overlayFileURI, false, 0);
	}

	private static ImageView getImage(ImageView thumbView, final Path filePath, final boolean overlayWanted, final int THUMB_SIZE) throws IOException {
		Image thumb = null;
		String fileURL = filePath.toUri().toURL().toString();

		if (FxImageUtil.SUPPORTED_IMAGE_FILE_FILTER.accept(null, fileURL)) {
			if (THUMB_SIZE <= 0)
				thumb = processMagenta(new Image(fileURL), COLOR_THRESHOLD, overlayWanted);
			else
				thumb = processMagenta(new Image(fileURL, THUMB_SIZE, THUMB_SIZE, true, true), COLOR_THRESHOLD, overlayWanted);
		} else if (FxImageUtil.PSD_FILE_FILTER.accept(null, fileURL)) {
			ImageInputStream is = null;
			PSDImageReader reader = null;
			int imageIndex = 1;

			// Mask layer should always be layer 1 and overlay image on layer 2. Note, layer 0 will be a combined layer composite
			if (overlayWanted)
				imageIndex = 2;

			File file = filePath.toFile();

			try {
				is = ImageIO.createImageInputStream(file);
				if (is == null || is.length() == 0) {
					System.out.println("Image is null");
				}

				Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
				if (iterator == null || !iterator.hasNext()) {
					throw new IOException("Image file format not supported by ImageIO: " + filePath);
				}

				reader = (PSDImageReader) iterator.next();
				reader.setInput(is);
				BufferedImage thumbBI;
				thumbBI = reader.read(imageIndex);

				if (thumbBI != null) {
					if (overlayWanted) {
						IIOMetadata metadata = reader.getImageMetadata(0);
						IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(PSDMetadata.NATIVE_METADATA_FORMAT_NAME);
						NodeList layerInfos = root.getElementsByTagName("LayerInfo");

						// Layer index corresponds to imageIndex - 1 in the reader
						IIOMetadataNode maskLayerInfo = (IIOMetadataNode) layerInfos.item(0);
						IIOMetadataNode overlayLayerInfo = (IIOMetadataNode) layerInfos.item(1);

						// Get the width & height of the Mask layer so we can create the overlay the same size
						int width = Integer.parseInt(maskLayerInfo.getAttribute("bottom")) - Integer.parseInt(maskLayerInfo.getAttribute("top"));
						int height = Integer.parseInt(maskLayerInfo.getAttribute("right")) - Integer.parseInt(maskLayerInfo.getAttribute("left"));

						// Get layer offsets, PhotoShop PSD layers can have different widths/heights and all images start at 0,0 with a layer offset applied
						int x = Integer.parseInt(overlayLayerInfo.getAttribute("left"));
						int y = Integer.parseInt(overlayLayerInfo.getAttribute("top"));

						// Lets pad the overlay with transparency to make it the same size as the mask
						thumb = resizeCanvas(SwingFXUtils.toFXImage(thumbBI, null), width, height, x, y);
					} else {
						thumb = SwingFXUtils.toFXImage(thumbBI, null);
					}

					if (THUMB_SIZE > 0) {
						thumbView.setFitWidth(THUMB_SIZE);
						thumbView.setPreserveRatio(true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// Dispose reader in finally block to avoid memory leaks
				reader.dispose();
				is.close();
			}
		}

		thumbView.setImage(thumb);

		return thumbView;
	}

	/*
	 * Resize the overall image width/height without scaling the actual image, eg resize the canvas
	 */
	private static Image resizeCanvas(Image imageSource, int newWidth, int newHeight, int offsetX, int offsetY) {
		int sourceWidth = (int) imageSource.getWidth();
		int sourceHeight = (int) imageSource.getHeight();

		WritableImage outputImage = new WritableImage(newWidth, newHeight);
		PixelReader pixelReader = imageSource.getPixelReader();
		PixelWriter pixelWriter = outputImage.getPixelWriter();
		WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();

		int[] buffer = new int[sourceWidth * sourceHeight];
		pixelReader.getPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
		pixelWriter.setPixels(offsetX, offsetY, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);

		return outputImage;
	}

	/*
	 * Return the intersection between the source image and the mask. Note, the mask does not need to be magenta anymore, any non-transparent pixel is considering a mask
	 */
	private static Image clipImageWithMask(Image imageSource, Image imageMask) {
		int imageWidth = (int) imageMask.getWidth();
		int imageHeight = (int) imageMask.getHeight();

		WritableImage outputImage = new WritableImage(imageWidth, imageHeight);
		PixelReader pixelReader_Mask = imageMask.getPixelReader();
		PixelReader pixelReader_Source = imageSource.getPixelReader();
		PixelWriter pixelWriter = outputImage.getPixelWriter();

		for (int readY = 0; readY < imageHeight; readY++) {
			for (int readX = 0; readX < imageWidth; readX++) {
				Color pixelColor = pixelReader_Mask.getColor(readX, readY);

				if (pixelColor.equals(Color.TRANSPARENT))
					pixelWriter.setColor(readX, readY, pixelReader_Source.getColor(readX, readY));
			}
		}

		return outputImage;
	}

	/*
	 * Crop image to smallest width/height based on transparency
	 */
	private static Image autoCropImage(Image imageSource) {
		ImageView croppedImageView = new ImageView(imageSource);
		PixelReader pixelReader = imageSource.getPixelReader();

		int imageWidth = (int) imageSource.getWidth();
		int imageHeight = (int) imageSource.getHeight();
		int minX = imageWidth, minY = imageHeight, maxX = 0, maxY = 0;

		// Find the first and last pixels that are not transparent to create a bounding viewport
		for (int readY = 0; readY < imageHeight; readY++) {
			for (int readX = 0; readX < imageWidth; readX++) {
				Color pixelColor = pixelReader.getColor(readX, readY);

				if (!pixelColor.equals(Color.TRANSPARENT)) {
					if (readX < minX)
						minX = readX;
					if (readX > maxX)
						maxX = readX;

					if (readY < minY)
						minY = readY;
					if (readY > maxY)
						maxY = readY;
				}
			}
		}

		// Create a viewport to clip the image using snapshot 
		Rectangle2D viewPort = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
		SnapshotParameters parameter = new SnapshotParameters();
		parameter.setViewport(viewPort);
		parameter.setFill(Color.TRANSPARENT);

		return croppedImageView.snapshot(parameter, null);
	}

	public static Image composePreview(StackPane compositeTokenPane, Color bgColor, ImageView portraitImageView, ImageView maskImageView, ImageView overlayImageView, boolean useAsBase) {
		// Process layout as maskImage may have changed size if the overlay was changed
		compositeTokenPane.layout();

		SnapshotParameters parameter = new SnapshotParameters();
		Image finalImage = null;

		if (useAsBase) {
			// Snapshot the whole compositeTokenpane to capture portrait and overlay and then crop the image to it's smallest size. 
			// The layers have already been blended together so no other work needed.
			parameter.setFill(Color.TRANSPARENT);
			finalImage = autoCropImage(compositeTokenPane.snapshot(parameter, null));
		} else {
			// We need to clip the portrait image first then blend the overlay image over it
			// We will first get a snapshot of the portrait equal to the mask overlay image width/height
			double x, y, width, height;

			x = maskImageView.getParent().getLayoutX();
			y = maskImageView.getParent().getLayoutY();
			width = maskImageView.getFitWidth();
			height = maskImageView.getFitHeight();

			Rectangle2D viewPort = new Rectangle2D(x, y, width, height);
			Rectangle2D maskViewPort = new Rectangle2D(1, 1, width, height);
			WritableImage newImage = new WritableImage((int) width, (int) height);
			WritableImage newMaskImage = new WritableImage((int) width, (int) height);

			ImageView overlayCopyImageView = new ImageView();
			ImageView clippedImageView = new ImageView();
			Group blend;

			//			System.out.println("Viewport: " + viewPort);
			parameter.setViewport(viewPort);
			parameter.setFill(bgColor);
			portraitImageView.snapshot(parameter, newImage);

			parameter.setViewport(maskViewPort);
			parameter.setFill(Color.TRANSPARENT);
			maskImageView.setVisible(true);
			maskImageView.snapshot(parameter, newMaskImage);
			maskImageView.setVisible(false);

			clippedImageView.setFitWidth(width);
			clippedImageView.setFitHeight(height);
			clippedImageView.setImage(clipImageWithMask(newImage, newMaskImage));

			// Our masked portrait image is now stored in clippedImageView, lets now blend the overlay image over it
			// We'll create a temporary group to hold our temporary ImageViews's and blend them and take a snapshot
			overlayCopyImageView.setImage(overlayImageView.getImage());
			overlayCopyImageView.setFitWidth(overlayImageView.getFitWidth());
			overlayCopyImageView.setFitHeight(overlayImageView.getFitHeight());

			blend = new Group(clippedImageView, overlayCopyImageView);

			// Last, we'll clean up any excess transparent edges by cropping it
			finalImage = autoCropImage(blend.snapshot(parameter, null));
		}

		return finalImage;
	}

	public static double getScaleXRatio(ImageView imageView) {
		//		System.out.println("imageView.getBoundsInParent().getWidth(): " + imageView.getBoundsInParent().getWidth());
		//		System.out.println("imageView.getImage().getWidth(): " + imageView.getImage().getWidth());
		return imageView.getBoundsInParent().getWidth() / imageView.getImage().getWidth();
	}

	public static double getScaleYRatio(ImageView imageView) {
		return imageView.getBoundsInParent().getHeight() / imageView.getImage().getHeight();
	}

	/*
	 * This is for Legacy support but can cause magenta bleed on edges if there is transparency overlap. The preferred overlay storage is now PhotoShop PSD format with layer 1 containing the mask and
	 * layer 2 containing the image
	 */
	private static Image processMagenta(Image inputImage, int colorThreshold, boolean overlayWanted) {
		int imageWidth = (int) inputImage.getWidth();
		int imageHeight = (int) inputImage.getHeight();

		WritableImage outputImage = new WritableImage(imageWidth, imageHeight);
		PixelReader pixelReader = inputImage.getPixelReader();
		PixelWriter pixelWriter = outputImage.getPixelWriter();

		for (int readY = 0; readY < imageHeight; readY++) {
			for (int readX = 0; readX < imageWidth; readX++) {
				Color pixelColor = pixelReader.getColor(readX, readY);

				if (isMagenta(pixelColor, COLOR_THRESHOLD) == overlayWanted)
					pixelWriter.setColor(readX, readY, Color.TRANSPARENT);
				else
					pixelWriter.setColor(readX, readY, pixelColor);

			}
		}

		return outputImage;
	}

	// Using some fudge factor...
	private static boolean isMagenta(Color color, int fudge) {
		if (color.equals(Color.MAGENTA))
			return true;

		double r = color.getRed();
		double g = color.getGreen();
		double b = color.getBlue();

		if (Math.abs(r - b) > fudge)
			return false;

		if (g > r - fudge || g > b - fudge)
			return false;

		return true;
	}

	/*
	 * These are the supported types used in the new Image class
	 */
	public static final FilenameFilter SUPPORTED_IMAGE_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			name = name.toLowerCase();

			return name.endsWith(".png") ||
					name.endsWith(".gif") ||
					name.endsWith(".jpg") ||
					name.endsWith(".jpeg") ||
					name.endsWith(".bmp");
		}
	};

	/*
	 * PSD Support using com.twelvemonkeys.imageio
	 */
	public static final FilenameFilter PSD_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".psd");
		}
	};

}
