package net.rptools.tokentool.fx.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class FxImageUtil {
	private static final int COLOR_THRESHOLD = -1;

	public static Image magentaToTransparency(Image inputImage, int colorThreshold) {
		int imageWidth = (int) inputImage.getWidth();
		int imageHeight = (int) inputImage.getHeight();

		WritableImage outputImage = new WritableImage(imageWidth, imageHeight);
		PixelReader pixelReader = inputImage.getPixelReader();
		PixelWriter pixelWriter = outputImage.getPixelWriter();

		for (int readY = 0; readY < imageHeight; readY++) {
			for (int readX = 0; readX < imageWidth; readX++) {
				Color pixelColor = pixelReader.getColor(readX, readY);

				if (isMagenta(pixelColor, COLOR_THRESHOLD))
					pixelWriter.setColor(readX, readY, Color.TRANSPARENT);
				else
					pixelWriter.setColor(readX, readY, pixelColor);
			}
		}

		return outputImage;
	}

	public static Image grabMask(Image overlayImage) {
		return grabMask(overlayImage, COLOR_THRESHOLD);
	}

	public static Image grabMask(Image inputImage, int colorThreshold) {
		int imageWidth = (int) inputImage.getWidth();
		int imageHeight = (int) inputImage.getHeight();

		WritableImage outputImage = new WritableImage(imageWidth, imageHeight);
		PixelReader pixelReader = inputImage.getPixelReader();
		PixelWriter pixelWriter = outputImage.getPixelWriter();

		for (int readY = 0; readY < imageHeight; readY++) {
			for (int readX = 0; readX < imageWidth; readX++) {
				Color pixelColor = pixelReader.getColor(readX, readY);

				if (!isMagenta(pixelColor, COLOR_THRESHOLD)) {
					pixelWriter.setColor(readX, readY, Color.TRANSPARENT);
				} else {
					pixelWriter.setColor(readX, readY, pixelColor);
				}
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

	public static Image composePreview(StackPane compositeTokenPane, ImageView maskImageView) {
		// Process layout as maskImage may have changed size if the overlay was changed
		compositeTokenPane.layout();

		double x = maskImageView.getParent().getLayoutX();
		double y = maskImageView.getParent().getLayoutY();
		double width = maskImageView.getImage().getWidth();
		double height = maskImageView.getImage().getHeight();

		Rectangle2D viewPort = new Rectangle2D(x, y, width, height);
		WritableImage newImage = new WritableImage((int) width, (int) height);
		SnapshotParameters parameter = new SnapshotParameters();
		parameter.setViewport(viewPort);

		// Finally, create a composite image of the Portait view and the Overlay view
		// We need to briefly turn the mask on during the snapshot...
		maskImageView.setVisible(true);
		compositeTokenPane.snapshot(parameter, newImage);
		maskImageView.setVisible(false);

		//		System.out.println("1. Viewport: " + viewPort);

		return magentaToTransparency(newImage, COLOR_THRESHOLD);
	}

}
