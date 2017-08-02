/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.rptools.lib.FileUtil;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.AboutDialog;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.swing.preference.WindowPreferences;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.AppMenuBar;
import net.rptools.tokentool.TokenTool;

public class TokenToolFrame extends JFrame {

	private TokenCompositionPanel compositionPanel;
	private TokenPreviewPanel previewPanel;
	private ControlPanel controlPanel;
	private JFileChooser saveChooser;
	private AboutDialog aboutDialog;
	public static String VERSION = "";

	public TokenToolFrame() {

		super("TokenTool");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setFocusTraversalPolicy(new TokenToolFocusTraversalPolicy());

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		try {
			setIconImage(ImageUtil.getImage("net/rptools/tokentool/image/minilogo.png"));
		} catch (IOException ioe) {
			System.err.println("Could not load icon image");
		}

		// ABOUT
		try {
			String credits = null;
			try {
				credits = new String(FileUtil.loadResource("net/rptools/tokentool/credits.html"));
			} catch (IllegalArgumentException iae) {
				credits = "Could not be loaded";
			}

			String version = "DEVELOPMENT";
			try {
				if (getClass().getClassLoader().getResource("net/rptools/tokentool/version.txt") != null) {
					version = new String(FileUtil.loadResource("net/rptools/tokentool/version.txt"));
				}
			} catch (IllegalArgumentException iae) {
				version = "Could not load version";
			}

			VERSION = version;
			credits = credits.replace("%VERSION%", version);
			Image logo = ImageUtil.getImage("net/rptools/lib/image/rptools-logo.png");

			aboutDialog = new AboutDialog(this, logo, credits);
		} catch (IOException ioe) {
			// This won't happen
		}

		// COMPOSE
		add(BorderLayout.CENTER, createCenterPanel());
		add(BorderLayout.EAST, createEastPanel());

		setJMenuBar(new AppMenuBar());

		saveChooser = new JFileChooser();

		setSize(400, 300);
		SwingUtil.centerOnScreen(this);
		new WindowPreferences(AppConstants.APP_NAME, "mainFrame", this);
	}

	private JPanel createCenterPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel wrapperPanel = new JPanel(new GridLayout());
		wrapperPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

		wrapperPanel.add(getTokenCompositionPanel());

		panel.add(BorderLayout.CENTER, wrapperPanel);

		return panel;
	}

	private JPanel createEastPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		JPanel previewWrapper = new JPanel(new GridLayout());
		previewWrapper.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		previewWrapper.add(getPreviewPanel());

		panel.add(BorderLayout.NORTH, previewWrapper);
		panel.add(BorderLayout.CENTER, new JScrollPane(getControlPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

		return panel;
	}

	public ControlPanel getControlPanel() {

		if (controlPanel == null) {
			controlPanel = new ControlPanel();
		}

		return controlPanel;
	}

	public TokenPreviewPanel getPreviewPanel() {
		if (previewPanel == null) {
			previewPanel = new TokenPreviewPanel();
			getTokenCompositionPanel().addChangeObserver(previewPanel);
		}

		return previewPanel;
	}

	public TokenCompositionPanel getTokenCompositionPanel() {
		if (compositionPanel == null) {
			compositionPanel = new TokenCompositionPanel();
		}

		return compositionPanel;
	}

	public void showAboutDialog() {
		aboutDialog.setVisible(true);
	}

	public BufferedImage getComposedToken() {
		return compositionPanel.getComposedToken();
	}

	public File showSaveDialog(boolean isToken) {
		saveChooser.resetChoosableFileFilters();
		String tokenName = TokenTool.getFrame().getControlPanel().getNamePrefix();

		if (isToken) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter("MapTool Tokens (*.rptok)", "rptok");
			saveChooser.setSelectedFile(new File(tokenName));
			saveChooser.setFileFilter(filter);
		} else {
			FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images (*.png)", "png");
			saveChooser.setSelectedFile(new File(tokenName));
			saveChooser.setFileFilter(filter);
		}

		int action = saveChooser.showSaveDialog(TokenTool.getFrame());
		return action == JFileChooser.APPROVE_OPTION ? saveChooser.getSelectedFile() : null;
	}
}