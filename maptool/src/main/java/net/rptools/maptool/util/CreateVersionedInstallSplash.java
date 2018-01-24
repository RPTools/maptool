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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.rptools.maptool.client.swing.SplashScreen;

public class CreateVersionedInstallSplash extends Application {
	private static String resourceImage = "net/rptools/maptool/client/image/maptool_splash_template.png";
	private static String installImageOutputFilename = "build-resources/jWrapper/maptool_installing_splash.png";
	private static String webImageOutputPath = "build/release-";
	private static String versionText = "Dev-Build";
	private static final String FONT_RESOURCE = "/net/rptools/maptool/client/fonts/Horta.ttf";
	private static Font versionFont;
	private static String rootPrefix;

	public static void main(String[] args) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			StringBuilder prefix = new StringBuilder("../");
			for (int i = 30; i-- > 0;) {
				try {
					input = new FileInputStream(prefix.toString() + "gradle.properties");
					break;
				} catch (Exception e) {
					prefix.append("../");
				}
			}
			if (input == null) {
				System.err.println("Couldn't find 'gradle.properties' file in any parent (30 levels searched).");
				System.exit(1);
			}
			rootPrefix = prefix.toString();
			System.out.println("Root prefix: " + rootPrefix);
			// load a properties file
			prop.load(input);

			versionText = prop.getProperty("buildVersion");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Options cmdOptions = new Options();
		cmdOptions.addOption("s", "source", true, "Source image to add version string to.");
		cmdOptions.addOption("o", "output", true, "Output /path/image to write to.");
		cmdOptions.addOption("v", "version", true, "Version text to add to image.");
		cmdOptions.addOption("w", "web_output", true, "Output path for upload to web server");

		// Parameters that can be overridden via command line options...
		resourceImage = getCommandLineOption(cmdOptions, "source", resourceImage, args);
		installImageOutputFilename = getCommandLineOption(cmdOptions, "output", installImageOutputFilename, args);
		versionText = getCommandLineOption(cmdOptions, "version", versionText, args);
		webImageOutputPath = getCommandLineOption(cmdOptions, "web_output", null, args);

		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			updateWebVersion(versionText);
			final File installSplashFile = new File(rootPrefix + installImageOutputFilename);
			System.out.println("Version: " + versionText);
			System.out.println("Source: " + resourceImage);
			System.out.println("Output: " + installSplashFile.getCanonicalPath());
			BufferedImage installImage = createLaunchSplash("Installing... " + "v" + versionText);
			ImageIO.write(installImage, "png", installSplashFile);

			if (webImageOutputPath != null) {
				final File webSplashFile = new File(rootPrefix + webImageOutputPath + versionText + "/MapTool-splash.png");
				System.out.println("Web Output: " + webSplashFile);
				BufferedImage webImage = createLaunchSplash("v" + versionText);
				ImageIO.write(webImage, "png", webSplashFile);
			}

		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}

		System.exit(0);
	}

	private static void updateWebVersion(String versionText) {
		try {
			File releaseDir = new File(rootPrefix + "build/release-" + versionText);
			if (releaseDir.isDirectory())
				deleteRecursively(releaseDir);
			if (!releaseDir.mkdirs()) {
				System.err.println("Error: Unable to create directory path [" + releaseDir + "]");
				System.exit(2);
			}
			System.out.println("Created directory " + releaseDir);
			FileWriter fstream = new FileWriter(rootPrefix + "build/release-" + versionText + "/MapTool-version.js");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("var mtVersion = '" + versionText + "';\n");
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void deleteRecursively(File f) {
		Path directory = Paths.get(f.toURI());
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			System.err.println("Error: Failed trying to delete [" + f + "]");
			System.exit(2);
		}
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
			e1.printStackTrace();
		}
		return defaultValue;
	}
}
