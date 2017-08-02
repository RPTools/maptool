/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.commons.io.FilenameUtils;

import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.io.PackedFile;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.TokenUtil;
import net.rptools.tokentool.ui.OverlayManagementDialog;
import net.rptools.tokentool.ui.TokenToolFrame;
import net.rptools.tokentool.util.RegionSelector;

import com.sun.imageio.plugins.png.PNGMetadata;

public class AppActions {
	private static final String PROP_VERSION = "version";
	private static final String ASSET_DIR = "assets/";
	private static final int THUMB_SIZE = 100;
	public static final String FILE_THUMBNAIL = "thumbnail";

	public static final Action EXIT_APP = new AbstractAction() {

		{
			putValue(Action.NAME, "Exit");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {

			System.exit(0);
		}
	};

	public static final Action SHOW_ABOUT = new AbstractAction() {

		{
			putValue(Action.NAME, "About");
		}

		public void actionPerformed(ActionEvent e) {

			TokenTool.getFrame().showAboutDialog();
		}
	};

	/**
	 * Action triggered to paste a token image from clipboard
	 * 
	 * @author cif
	 */
	public static final Action PASTE_CLIPBOARD = new AbstractAction() {
		{
			putValue(Action.NAME, "Paste token from clipboard");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

		}

		public void actionPerformed(ActionEvent e) {
			BufferedImage img = AppCopyPaste.getClipboardBufferedImage();
			if (img != null) {
				try {
					TokenTool.getFrame().getTokenCompositionPanel().setToken(img);
				} catch (Exception ae) {
					ae.printStackTrace();
				}

			}
		}
	};

	/**
	 * Action triggered to copy token image to clipboard
	 * 
	 * @author cif
	 */
	public static final Action COPY_CLIPBOARD = new AbstractAction() {
		{
			putValue(Action.NAME, "Copy token to clipboard");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

		}

		public void actionPerformed(ActionEvent e) {
			try {
				AppCopyPaste.setClipboardBufferedImage(TokenTool.getFrame().getTokenCompositionPanel().getComposedToken());
			} catch (Exception ex) {
				System.out.println(ex.getMessage());

			}
		}
	};

	public static final Action SCREEN_CAP = new AbstractAction() {

		{
			putValue(Action.NAME, "Screen Capture");
		}

		private Rectangle bounds = new Rectangle(100, 100, 600, 400);

		public void actionPerformed(ActionEvent e) {

			RegionSelector selector = new RegionSelector();
			selector.run(bounds);
			bounds = selector.getBounds();

			if (bounds.width > 0 && bounds.height > 0) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							BufferedImage image = new Robot().createScreenCapture(bounds);
							TokenTool.getFrame().getTokenCompositionPanel().setToken(image);
						} catch (AWTException ae) {
							ae.printStackTrace();
						}
					}
				});
			}
		}
	};

	public static final Action SHOW_OVERLAY_MANAGEMENT_DIALOG = new AbstractAction() {

		{
			putValue(Action.NAME, "Manage Overlays");
		}

		public void actionPerformed(ActionEvent e) {

			new OverlayManagementDialog(TokenTool.getFrame()).setVisible(true);
		}
	};

	public static final Action SAVE_TOKEN = new AbstractAction() {
		{
			putValue(Action.NAME, "Save Token");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		}

		public void actionPerformed(java.awt.event.ActionEvent e) {

			new Thread() {
				public void run() {
					File rptok_file = TokenTool.getFrame().showSaveDialog(true);
					saveToken(rptok_file, false);
				}
			}.start();

		}
	};

	public static void saveToken(File rptok_file, boolean overwrite) {
		if (rptok_file != null) {
			if (!rptok_file.getName().toUpperCase().endsWith(".RPTOK")) {
				rptok_file = new File(rptok_file.getAbsolutePath() + ".rptok");
			}

			if (rptok_file.exists() && !overwrite) {
				if (!TokenTool.confirm("File exists.  Overwrite?")) {
					return;
				} else {
					rptok_file.delete();
				}
			}

			String tokenName = FilenameUtils.removeExtension(rptok_file.getName());

			try {
				// Write out the token image first or aka POG image.
				File tokenImageFile = File.createTempFile("tokenImage", ".png");

				// PW: This code addes the pHYs chunk to the
				// output png file with X & Y dpi set.
				BufferedImage tokenImg = TokenTool.getFrame().getComposedToken();
				BufferedImage portraitImg = TokenTool.getFrame().getTokenCompositionPanel().getBaseImage();

				ImageWriter writer = getImageWriterBySuffix("png");
				// Created object for outputStream so we can properly close it! No longer locks .png files until app closes!
				ImageOutputStream ios = ImageIO.createImageOutputStream(tokenImageFile);
				writer.setOutput(ios);
				ImageWriteParam param = writer.getDefaultWriteParam();

				PNGMetadata png = new PNGMetadata();
				// 39.375 inches per meter
				// I'm using the image width for the DPI under
				// the assumption that the token fits within
				// one cell.
				int resX = (int) (tokenImg.getWidth() * 39.375f);
				png.pHYs_pixelsPerUnitXAxis = resX;
				png.pHYs_pixelsPerUnitYAxis = resX;
				png.pHYs_unitSpecifier = 1; // Meters - alternative is "unknown"
				png.pHYs_present = true;

				writer.write(null, new IIOImage(tokenImg, null, png), param);
				ios.close();

				// Now write out the Portrait image, here we'll use JPEG to save space
				File portraitImageFile = File.createTempFile("portraitImage", ".jpg");
				writer.reset();
				writer = getImageWriterBySuffix("jpg");
				ios = ImageIO.createImageOutputStream(portraitImageFile);
				writer.setOutput(ios);
				param = writer.getDefaultWriteParam();

				writer.write(null, new IIOImage(portraitImg, null, null), param);
				writer.dispose();
				ios.close();

				// Lets create the token!
				Token _token = new Token();
				Asset tokenImage = null;
				tokenImage = AssetManager.createAsset(tokenImageFile);
				AssetManager.putAsset(tokenImage);
				_token = new Token(tokenName, tokenImage.getId());
				_token.setGMName(tokenName);

				// Jamz: Below calls not needed, creates extra entries in XML preventing token image from changing inside MapTool
				// _token.setImageAsset(tokenImage.getName());
				// _token.setImageAsset(tokenImage.getName(), tokenImage.getId());

				// set the image shape
				Image image = ImageIO.read(tokenImageFile);
				_token.setShape(TokenUtil.guessTokenType(image));

				// set the height/width, fixes dragging to stamp layer issue
				_token.setHeight(tokenImg.getHeight());
				_token.setWidth(tokenImg.getWidth());

				// set the portrait image asset
				Asset portrait = AssetManager.createAsset(portraitImageFile); // Change for portrait
				AssetManager.putAsset(portrait);
				_token.setPortraitImage(portrait.getId());

				// Time to write out the .rptok token file...
				PackedFile pakFile = null;
				try {
					pakFile = new PackedFile(rptok_file);
					saveAssets(_token.getAllImageAssets(), pakFile);
					pakFile.setContent(_token);
					BufferedImage thumb = ImageUtil.createCompatibleImage(image, THUMB_SIZE, THUMB_SIZE, null);
					pakFile.putFile(FILE_THUMBNAIL, ImageUtil.imageToBytes(thumb, "png"));
					pakFile.setProperty(PROP_VERSION, TokenToolFrame.VERSION);
					pakFile.save();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (pakFile != null)
						pakFile.close();
					tokenImageFile.delete();
					portraitImageFile.delete();
				}

			} catch (IOException ioe) {
				ioe.printStackTrace();
				TokenTool.showError("Unable to write image: " + ioe);
			}
		}
	}

	private static void saveAssets(Collection<MD5Key> assetIds, PackedFile pakFile) throws IOException {
		// Special handling of assets: XML file to describe the Asset, but binary file for the image data
		pakFile.getXStream().processAnnotations(Asset.class);

		for (MD5Key assetId : assetIds) {
			if (assetId == null)
				continue;

			// And store the asset elsewhere
			// As of 1.3.b64, assets are written in binary to allow them to be readable
			// when a campaign file is unpacked.
			Asset asset = AssetManager.getAsset(assetId);
			if (asset == null) {
				// log.error("AssetId " + assetId + " not found while saving?!");
				System.out.println("AssetId " + assetId + " not found while saving?!");
				continue;
			}
			pakFile.putFile(ASSET_DIR + assetId + "." + asset.getImageExtension(), asset.getImage());
			pakFile.putFile(ASSET_DIR + assetId, asset); // Does not write the image
		}
	}

	public static final Action SAVE_TOKEN_IMAGE = new AbstractAction() {
		{
			putValue(Action.NAME, "Save Token as Image");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
		}

		public void actionPerformed(java.awt.event.ActionEvent e) {

			new Thread() {
				public void run() {

					File file = TokenTool.getFrame().showSaveDialog(false);
					if (file != null) {

						if (!file.getName().toUpperCase().endsWith(".PNG")) {
							file = new File(file.getAbsolutePath() + ".png");
						}
						if (file.exists()) {
							if (!TokenTool.confirm("File exists.  Overwrite?")) {
								return;
							}
						}

						try {
							// PW: This code addes the pHYs chunk to the
							// output png file with X & Y dpi set.
							BufferedImage img = TokenTool.getFrame().getComposedToken();
							ImageWriter writer = getImageWriterBySuffix("png");
							ImageOutputStream ios = ImageIO.createImageOutputStream(file);
							writer.setOutput(ios);
							ImageWriteParam param = writer.getDefaultWriteParam();
							PNGMetadata png = new PNGMetadata();
							// 39.375 inches per meter
							// I'm using the image width for the DPI under
							// the assumption that the token fits within
							// one cell.
							int resX = (int) (img.getWidth() * 39.375f);
							png.pHYs_pixelsPerUnitXAxis = resX;
							png.pHYs_pixelsPerUnitYAxis = resX;
							png.pHYs_unitSpecifier = 1; // Meters - alternative is "unknown"
							png.pHYs_present = true;

							// TBD save the location and dimensions of the base, so that MapTool
							// can scale this token so that the base fits within the grid while leaving
							// the rest to perhaps overflow!

							writer.write(null, new IIOImage(img, null, png), param);
							writer.dispose();
							ios.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							TokenTool.showError("Unable to write image: " + ioe);
						}
					}
				}
			}.start();
		}
	};

	// Grabbed this from some code off the Sun Java pages
	public static ImageWriter getImageWriterBySuffix(String suffix) throws IOException {
		Iterator writers = ImageIO.getImageWritersBySuffix(suffix);
		if (!writers.hasNext())
			throw new IOException("woops, no writers for " + suffix);
		return (ImageWriter) writers.next();
	}
}
