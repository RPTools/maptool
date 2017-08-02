/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
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
