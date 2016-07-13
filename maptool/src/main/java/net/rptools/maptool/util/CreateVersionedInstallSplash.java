/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.rptools.maptool.client.swing.SplashScreen;

public class CreateVersionedInstallSplash extends Application {
	private static String resourceImage = "net/rptools/maptool/client/image/maptool_splash_template_nerps.png";
	private static String imageOutputFilename = "../build-resources/jWrapper/maptool_installing_splash.png";
	private static String versionText = "Dev-Build";
	private static final String FONT_RESOURCE = "/net/rptools/maptool/client/fonts/Horta.ttf";
	private static Font versionFont;

	public static void main(String[] args) {
		Options cmdOptions = new Options();
		cmdOptions.addOption("s", "source", true, "Source image to add version string to.");
		cmdOptions.addOption("o", "output", true, "Output /path/image to write to.");
		cmdOptions.addOption("v", "version", true, "Version text to add to image.");

		// Parameters that can be overridden via command line options...
		resourceImage = getCommandLineOption(cmdOptions, "source", resourceImage, args);
		imageOutputFilename = getCommandLineOption(cmdOptions, "output", imageOutputFilename, args);
		versionText = getCommandLineOption(cmdOptions, "version", versionText, args);

		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		final File splashFile = new File(imageOutputFilename);
		BufferedImage buffImage = createLaunchSplash("Installing... v" + versionText);

		try {
			System.out.println("Version: " + versionText);
			System.out.println("Source: " + resourceImage);
			System.out.println("Output: " + splashFile.getCanonicalPath());

			ImageIO.write(buffImage, "png", splashFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	public static BufferedImage createLaunchSplash(String versionText) {
		final ImageIcon splashIcon = new ImageIcon(SplashScreen.class.getClassLoader().getResource(resourceImage));
		final Color versionColor = Color.rgb(3, 78, 149, 1); // Color.rgb(27, 85, 139, 1)

		final int imgWidth = 490;
		final int imgHeight = 290;
		final int versionTextX = 48;
		final int versionTextY = 37;

		InputStream is = SplashScreen.class.getResourceAsStream(FONT_RESOURCE);
		versionFont = Font.loadFont(is, 28);

		BufferedImage buffImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = buffImage.createGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2d.setRenderingHints(rh);
		g2d.drawImage(splashIcon.getImage(), 0, 0, null);

		// Adding glow twice to make it more pronounced...
		g2d.drawImage(textToImage(versionText, Color.WHITESMOKE, 28, true), versionTextX, versionTextY, null);
		g2d.drawImage(textToImage(versionText, Color.WHITESMOKE, 28, true), versionTextX, versionTextY, null);
		g2d.drawImage(textToImage(versionText, versionColor, 28, false), versionTextX, versionTextY, null);
		g2d.dispose();

		return buffImage;
	}

	private static BufferedImage textToImage(String text, Color fontColor, int fontSize, boolean addGlow) {
		Text versionText = new Text(0, 0, text);
		versionText.setFill(fontColor);
		versionText.setFont(versionFont);

		if (addGlow) {
			Effect glow = new Glow(1.0);
			versionText.setEffect(glow);
		}

		Stage stage = new Stage(StageStyle.TRANSPARENT);
		Group root = new Group();
		Scene scene = new Scene(root);
		SnapshotParameters sp = new SnapshotParameters();

		sp.setFill(Color.TRANSPARENT);
		stage.setScene(scene);
		root.getChildren().add(versionText);

		WritableImage img = root.snapshot(sp, null);

		return SwingFXUtils.fromFXImage(img, null);
	}

	private static String getCommandLineOption(Options options, String searchValue, String defaultValue, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(searchValue)) {
				return cmd.getOptionValue(searchValue);
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return defaultValue;
	}
}
