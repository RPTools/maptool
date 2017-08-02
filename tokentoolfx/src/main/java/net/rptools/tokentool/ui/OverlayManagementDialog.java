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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.tokentool.AppSetup;
import net.rptools.tokentool.TokenTool;

public class OverlayManagementDialog extends JDialog {

	private OverlayPanel overlayImagePanel;
	private JButton deleteButton;
	private JButton restoreDefaultsButton;
	private JButton closeButton;

	public OverlayManagementDialog(JFrame owner) {
		super(owner, "Overlay Manager", true);
		init();
		super.setLocationRelativeTo(owner);
	}

	private void init() {
		setSize(300, 300);
		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, createCenterPanel());
		add(BorderLayout.SOUTH, createControlPanel());

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
		getRootPane().getActionMap().put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
	}

	@Override
	public void setVisible(boolean visible) {
		TokenTool.getFrame().getControlPanel().getOverlayCombo().setEnabled(false);
		super.setVisible(visible);

		if (!visible) {
			((OverlayListModel) TokenTool.getFrame().getControlPanel().getOverlayCombo().getModel()).refresh();
			TokenTool.getFrame().getControlPanel().getOverlayCombo().setEnabled(true);
		}
	}

	private JPanel createControlPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel detailsPanel = new JPanel(new GridLayout());
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		detailsPanel.add(new JLabel("<html><body>Drag and drop images onto the panel to add overlays</body></html>"));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getDeleteButton());
		buttonPanel.add(getRestoreDefaultsButton());
		buttonPanel.add(getCloseButton());

		panel.add(BorderLayout.NORTH, detailsPanel);
		panel.add(BorderLayout.SOUTH, buttonPanel);

		return panel;
	}

	public JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Close button pressed.");
					setVisible(false);
					dispose();
					System.out.println("Close button done.");
				}
			});
		}

		return closeButton;
	}

	public JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton("Delete");
			deleteButton.setAction(getOverlayImagePanel().DELETE_ACTION);
		}

		return deleteButton;
	}

	public JButton getRestoreDefaultsButton() {
		if (restoreDefaultsButton == null) {
			restoreDefaultsButton = new JButton("Restore Defaults");
			restoreDefaultsButton.setAction(new RestoreDefaultOverlaysAction());
		}

		return restoreDefaultsButton;
	}

	private JPanel createCenterPanel() {
		JPanel panel = new JPanel(new GridLayout());
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

		panel.add(new JScrollPane(getOverlayImagePanel()));

		return panel;
	}

	public OverlayPanel getOverlayImagePanel() {
		if (overlayImagePanel == null) {
			overlayImagePanel = new OverlayPanel();
			overlayImagePanel.setBackground(Color.white);
		}

		return overlayImagePanel;
	}

	// //
	// ACTIONS
	private class RestoreDefaultOverlaysAction extends AbstractAction {
		public RestoreDefaultOverlaysAction() {
			putValue(Action.NAME, "Restore Defaults");
		}

		public void actionPerformed(ActionEvent e) {

			try {
				AppSetup.installDefaultOverlays();

				overlayImagePanel.getModel().refresh();

				overlayImagePanel.repaint();
			} catch (IOException ioe) {
				TokenTool.showError("Unable to install default tokens: " + ioe);
			}
		}
	};

}
