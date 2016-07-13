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
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.rptools.maptool.util.CreateVersionedInstallSplash;

public class SplashScreen extends JFrame {
	private Image splashImage;
	private int imgWidth, imgHeight;
	private static final String FONT_RESOURCE = "/net/rptools/maptool/client/fonts/Horta.ttf";
	private static Font versionFont;

	public SplashScreen(String imgName, final String versionText) {
		this(imgName, versionText, 195, 60, new Color(27, 85, 139));
	}

	public SplashScreen(String imgName, final String versionText, int versionTextX, int versionTextY) {
		this(imgName, versionText, versionTextX, versionTextY, new Color(27, 85, 139));
	}

	public SplashScreen(String imgName, final String versionText, final int versionTextX, final int versionTextY, final Color versionColor) {
		setUndecorated(true);
		loadSplashImage(imgName);
		InputStream is = CreateVersionedInstallSplash.class.getResourceAsStream(FONT_RESOURCE);

		try {
			versionFont = Font.createFont(Font.TRUETYPE_FONT, is);
			versionFont = versionFont.deriveFont(28F);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setContentPane(new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(splashImage, 0, 0, this);
				g.setColor(versionColor);
				//g.setFont(new Font("SansSerif", Font.BOLD, 18));
				g.setFont(versionFont);
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

		try {
			Thread.sleep(10000);
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void hideSplashScreen() {
		setVisible(false);
		dispose();

	}

}
