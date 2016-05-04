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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SplashScreen extends JFrame {
	private Image splashImage;

	private int imgWidth, imgHeight;

	public SplashScreen(String imgName, final String versionText) {
		this(imgName, versionText, 195, 60, new Color(27, 85, 139));
	}

	public SplashScreen(String imgName, final String versionText, int versionTextX, int versionTextY) {
		this(imgName, versionText, versionTextX, versionTextY, new Color(27, 85, 139));
	}

	public SplashScreen(String imgName, final String versionText, final int versionTextX, final int versionTextY, final Color versionColor) {
		setUndecorated(true);
		loadSplashImage(imgName);

		setContentPane(new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(splashImage, 0, 0, this);
				g.setColor(versionColor);
				g.setFont(new Font("SansSerif", Font.BOLD, 18));
				g.drawString("v" + versionText, versionTextX, versionTextY);
			}
		});
	}

	public void loadSplashImage(String imgName) {
		MediaTracker tracker = new MediaTracker(this);
		splashImage = Toolkit.getDefaultToolkit().createImage(
				SplashScreen.class.getClassLoader().getResource(imgName));
		tracker.addImage(splashImage, 0);
		try {
			tracker.waitForAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		imgWidth = splashImage.getWidth(this);
		imgHeight = splashImage.getHeight(this);
	}

	public void showSplashScreen() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = imgWidth;
		int h = imgHeight;
		int x = (screenSize.width - w) / 2;
		int y = (screenSize.height - h) / 2;
		setBounds(x, y, w, h);
		setBackground(new Color(0, 0, 0));

		try {
			// Jamz: Remove border, change to transparent background
			// Done in a try/catch because this can bomb on linux...
			setBackground(new Color(0, 0, 0, 0));
		} catch (Exception e) {
			e.printStackTrace();
		}

		setVisible(true);
	}

	public void hideSplashScreen() {
		setVisible(false);
		dispose();

	}

}
