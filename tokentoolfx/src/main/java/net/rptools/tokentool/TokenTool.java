/*
 * The MIT License
 * 
 * Copyright (c) 2005 David Rice, Trevor Croft
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.rptools.tokentool;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.rptools.lib.FileUtil;
import net.rptools.tokentool.ui.TokenToolFrame;

public class TokenTool {

	private static TokenToolFrame tokenToolFrame;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
		} catch (Exception e) {
			System.err.println("Exception during look and feel setup: " + e);
		}

		AppSetup.install(null);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				tokenToolFrame = new TokenToolFrame();
				tokenToolFrame.setVisible(true);
			}
		});
	}

	public static void addOverlayImage(BufferedImage image) throws IOException {
		ImageIO.write(image, "png", new File(AppConstants.OVERLAY_DIR + File.separator + System.currentTimeMillis() + ".png"));

	}

	public static void addOverlayImage(File imageFile) throws IOException {

		FileUtil.copyFile(imageFile, new File(AppConstants.OVERLAY_DIR + File.separator + imageFile.getName()));
	}

	public static TokenToolFrame getFrame() {
		return tokenToolFrame;
	}

	public static void showError(String message) {
		JOptionPane.showMessageDialog(tokenToolFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static boolean confirm(String message) {
		return JOptionPane.showConfirmDialog(tokenToolFrame, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
}
