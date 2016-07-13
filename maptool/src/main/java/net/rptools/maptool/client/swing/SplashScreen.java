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

package net.rptools.maptool.client.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import net.rptools.maptool.util.CreateVersionedInstallSplash;

public class SplashScreen extends JFrame {
	private static int imgWidth = 490;
	private static int imgHeight = 290;

	public SplashScreen(final String versionText) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final JFXPanel fxPanel = new JFXPanel();

		setUndecorated(true);
		add(fxPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			// Jamz: Remove border, change to transparent background
			// Done in a try/catch because this can bomb on linux...
			setBackground(new java.awt.Color(0, 0, 0, 0));
		} catch (Exception e) {
			setBackground(new java.awt.Color(0, 0, 0));
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel, versionText);
				int w = imgWidth;
				int h = imgHeight;
				int x = (screenSize.width - w) / 2;
				int y = (screenSize.height - h) / 2;
				setBounds(x, y, imgWidth, imgHeight);
				setVisible(true);
			}
		});
	}

	private static void initFX(JFXPanel fxPanel, String versionText) {
		// This method is invoked on the JavaFX thread
		Group root = new Group();
		Scene scene = new Scene(root, Color.TRANSPARENT);

		Image splashImage = SwingFXUtils.toFXImage(CreateVersionedInstallSplash.createLaunchSplash("Launching... v" + versionText), null);
		ImageView splashView = new ImageView(splashImage);
		imgWidth = (int) splashImage.getWidth();
		imgHeight = (int) splashImage.getHeight();
		root.getChildren().add(splashView);

		fxPanel.setScene(scene);
	}

	public void hideSplashScreen() {
		setVisible(false);
		dispose();
	}
}
