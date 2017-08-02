/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.ui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImageToggleButton;
import net.rptools.tokentool.AppState;
import net.rptools.tokentool.TokenTool;

import com.jeta.forms.components.panel.FormPanel;

public class ControlPanel extends JPanel {

	private JSpinner widthSpinner;
	private JSpinner heightSpinner;
	private JSlider transparencySlider;
	private JToggleButton lockToggleButton;
	private JComboBox overlayCombo;
	private JButton zoomOutButton;
	private JButton zoomInButton;
	private JButton zoomOutFastButton;
	private JButton zoomInFastButton;
	private JCheckBox solidBackgroundCheckBox;
	private JCheckBox baseCheckBox;
	private JSlider fudgeSlider;
	private JLabel fudgeValueLabel;
	private JLabel transparencyValueLabel;
	private JComboBox sizesCombo; // Combo box to pick from 6 pre-defined sizes
	private JTextField overlayHeightField; // Display the selected overlay's height
	private JTextField overlayWidthField; // Display the selected overlay's width

	// CIF's "Save as incremental file numbering"
	private JLabel jlblFileName;
	private JTextField jtfNamePrefix;
	private JTextField jtfFileNumber;
	private JCheckBox jcbUseFileNumbering;
	private boolean useFileNumbering = false;

	// Save as .rptok Token
	private JCheckBox dragAsTokenCheckBox;

	// Added color picker
	private JButton jbtnColorPicker;
	private ColorChooserDialog colorChooser;

	private FormPanel formPanel;

	public ControlPanel() {
		setLayout(new GridLayout());

		formPanel = new FormPanel("net/rptools/tokentool/forms/controlPanel.jfrm");

		getWidthSpinner();
		getHeightSpinner();
		getTransparencySlider();
		getOverlayCombo();
		getStockSizesCombo();
		getZoomOutButton();
		getZoomInButton();
		getZoomOutFastButton();
		getZoomInFastButton();
		getSolidBackgroundCheckBox().setSelected(true);
		getBaseCheckBox();
		getFudgeFactorSlider();
		formPanel.getFormAccessor().replaceBean("lockToggle", getLockToggle());
		getOverlayWidthField();
		getOverlayHeightField();
		getUseFileNumbering();
		getFileNumberField();
		getNamePrefixField();
		getNameLabel();
		getdragAsToken();
		updateLabels();
		getColorPickerButton();
		add(formPanel);
	}

	public JLabel getTransparencyValueLabel() {
		if (transparencyValueLabel == null) {
			transparencyValueLabel = formPanel.getLabel("transparencyValue");
		}
		return transparencyValueLabel;
	}

	public JLabel getFudgeValueLabel() {
		if (fudgeValueLabel == null) {
			fudgeValueLabel = formPanel.getLabel("fudgeValue");
		}
		return fudgeValueLabel;
	}

	public JSpinner getWidthSpinner() {
		if (widthSpinner == null) {
			widthSpinner = formPanel.getSpinner("width");
			widthSpinner.setValue(256);
			widthSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateWidth();
				}
			});
		}
		return widthSpinner;
	}

	private void updateWidth() {
		if (getLockToggle().isSelected()) {
			heightSpinner.setValue(widthSpinner.getValue());
		}

		TokenTool.getFrame().getTokenCompositionPanel().repaint();
	}

	public JSpinner getHeightSpinner() {
		if (heightSpinner == null) {
			heightSpinner = formPanel.getSpinner("height");
			heightSpinner.setValue(256);
			heightSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateHeight();
				}
			});
		}
		return heightSpinner;
	}

	private void updateHeight() {
		if (getLockToggle().isSelected()) {
			widthSpinner.setValue(heightSpinner.getValue());
		}

		TokenTool.getFrame().getTokenCompositionPanel().repaint();
	}

	private void updateLabels() {

		getTransparencyValueLabel().setText(getTransparencySlider().getValue() + "%");
		getFudgeValueLabel().setText("+/- " + getFudgeFactorSlider().getValue());
	}

	public JSlider getTransparencySlider() {
		if (transparencySlider == null) {
			transparencySlider = (JSlider) formPanel.getComponentByName("transparency");
			transparencySlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {

					AppState.compositionProperties.setTranslucency(transparencySlider.getValue() / 100.0);

					updateLabels();

					TokenTool.getFrame().getTokenCompositionPanel().fireCompositionChanged();
				}
			});
		}
		return transparencySlider;
	}

	public JSlider getFudgeFactorSlider() {
		if (fudgeSlider == null) {
			fudgeSlider = (JSlider) formPanel.getComponentByName("fudgeFactor");
			fudgeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {

					AppState.compositionProperties.setFudgeFactor(fudgeSlider.getValue());

					updateLabels();

					TokenTool.getFrame().getTokenCompositionPanel().fireCompositionChanged();
				}
			});
		}
		return fudgeSlider;
	}

	public JButton getZoomOutButton() {
		if (zoomOutButton == null) {
			zoomOutButton = (JButton) formPanel.getFormAccessor("zoomPanel").getComponentByName("zoomOut");
			zoomOutButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TokenTool.getFrame().getTokenCompositionPanel().zoomOut();
				}
			});
		}

		return zoomOutButton;
	}

	public JButton getZoomInButton() {
		if (zoomInButton == null) {
			zoomInButton = (JButton) formPanel.getFormAccessor("zoomPanel").getComponentByName("zoomIn");
			zoomInButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TokenTool.getFrame().getTokenCompositionPanel().zoomIn();
				}
			});
		}

		return zoomInButton;
	}

	public JButton getZoomOutFastButton() {
		if (zoomOutFastButton == null) {
			zoomOutFastButton = (JButton) formPanel.getFormAccessor("zoomPanel").getComponentByName("zoomOutFast");
			zoomOutFastButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TokenTool.getFrame().getTokenCompositionPanel().zoomOutFast();
				}
			});
		}

		return zoomOutFastButton;
	}

	public JButton getZoomInFastButton() {
		if (zoomInFastButton == null) {
			zoomInFastButton = (JButton) formPanel.getFormAccessor("zoomPanel").getComponentByName("zoomInFast");
			zoomInFastButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TokenTool.getFrame().getTokenCompositionPanel().zoomInFast();
				}
			});
		}

		return zoomInFastButton;
	}

	public JComboBox getOverlayCombo() {
		if (overlayCombo == null) {
			overlayCombo = formPanel.getComboBox("overlay");
			overlayCombo.setModel(new OverlayListModel());
			overlayCombo.setRenderer(new OverlayListRenderer());
			overlayCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BufferedImage overlay = ((OverlayListModel) overlayCombo.getModel()).getSelectedOverlay();
					TokenTool.getFrame().getTokenCompositionPanel().setOverlay(overlay);
					overlayWidthField.setText(Integer.toString(overlay.getWidth()));
					overlayHeightField.setText(Integer.toString(overlay.getHeight()));
				}
			});
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// Do this AFTER the UI has finished being built
					overlayCombo.setSelectedIndex(0);
				}
			});
		}
		return overlayCombo;
	}

	public JComboBox getStockSizesCombo() {
		if (sizesCombo == null) {
			sizesCombo = formPanel.getComboBox("stockSizes");
			if (sizesCombo != null) {
				sizesCombo.setSelectedIndex(2);

				sizesCombo.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int size = sizesCombo.getSelectedIndex();
						// PW: Default sizes were put into the combobox
						// in the form.
						switch (size) {
						case 0:
							heightSpinner.setValue(512);
							widthSpinner.setValue(512);
							break;
						case 1:
							heightSpinner.setValue(300);
							widthSpinner.setValue(300);
							break;
						case 2:
							heightSpinner.setValue(256);
							widthSpinner.setValue(256);
							break;
						case 3:
							heightSpinner.setValue(200);
							widthSpinner.setValue(200);
							break;
						case 4:
							heightSpinner.setValue(160);
							widthSpinner.setValue(160);
							break;
						case 5:
							heightSpinner.setValue(128);
							widthSpinner.setValue(128);
							break;
						case 6:
							heightSpinner.setValue(80);
							widthSpinner.setValue(80);
							break;
						case 7:
							heightSpinner.setValue(64);
							widthSpinner.setValue(64);
							break;
						default:
							heightSpinner.setValue(256);
							widthSpinner.setValue(256);
							break;
						}
						TokenTool.getFrame().getTokenCompositionPanel().repaint();
					}
				});
			}
		}
		return sizesCombo;
	}

	public JTextField getOverlayWidthField() {
		if (overlayWidthField == null) {
			overlayWidthField = formPanel.getTextField("overlayWidth");
			overlayWidthField.setText("256");
		}
		return overlayWidthField;
	}

	public JTextField getOverlayHeightField() {
		if (overlayHeightField == null) {
			overlayHeightField = formPanel.getTextField("overlayHeight");
			overlayHeightField.setText("256");
		}
		return overlayHeightField;
	}

	/**
	 * Get GUI text field for file name prefix
	 * 
	 * @author cif
	 * 
	 * @return
	 */
	public JTextField getNamePrefixField() {
		if (jtfNamePrefix == null) {
			jtfNamePrefix = formPanel.getTextField("jtfNamePrefix");
			jtfNamePrefix.setText("token");
			jtfNamePrefix.getDocument().addDocumentListener(new TextFieldNameListener(getFileNumberField()));
		}
		return jtfNamePrefix;
	}

	/**
	 * Set GUI text field for file name prefix
	 * 
	 * @author cif
	 * 
	 * @return
	 */
	public void setNamePrefixField(String tokenName) {
		if (jtfNamePrefix != null) {
			jtfNamePrefix.setText(tokenName);
		}
	}

	/**
	 * Get GUI label for file name prefix
	 * 
	 * @author cif
	 * 
	 * @return
	 */
	public JLabel getNameLabel() {
		if (jlblFileName == null) {
			jlblFileName = formPanel.getLabel("jlblFileName");
		}
		return jlblFileName;
	}

	/**
	 * Get GUI text field for file number
	 * 
	 * @author cif
	 * @return
	 */
	public JTextField getFileNumberField() {
		if (jtfFileNumber == null) {
			jtfFileNumber = formPanel.getTextField("jtfFileNumber");
			jtfFileNumber.setText("0");
		}
		return jtfFileNumber;
	}

	/**
	 * Get GUI text check box object to activate file numbering
	 * 
	 * @author cif
	 * @return
	 */
	public JCheckBox getUseFileNumbering() {
		if (jcbUseFileNumbering == null) {
			jcbUseFileNumbering = formPanel.getCheckBox("jcbUseFileNumbering");
			jcbUseFileNumbering.setSelected(useFileNumbering);
			jcbUseFileNumbering.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean useFN = useFileNumbering();
					jtfFileNumber.setEnabled(useFN);

					if (useFN)
						jlblFileName.setText("Filename Prefix");
					else
						jlblFileName.setText("Filename");
				}
			});
		}
		return jcbUseFileNumbering;
	}

	/**
	 * Get GUI check box object for saving as "Token"
	 * 
	 * @author Jamz
	 * @return
	 */
	public JCheckBox getdragAsToken() {
		if (dragAsTokenCheckBox == null) {
			dragAsTokenCheckBox = formPanel.getCheckBox("dragAsTokenCheckBox");
			/*
			 * dragAsTokenCheckBox.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { //no actions needed on change of checkbox... } });
			 */
		}
		return dragAsTokenCheckBox;
	}

	public JToggleButton getLockToggle() {
		if (lockToggleButton == null) {
			lockToggleButton = new ImageToggleButton("net/rptools/tokentool/image/locked.png", "net/rptools/tokentool/image/unlocked.png");
			lockToggleButton.setSelected(true);
		}
		return lockToggleButton;
	}

	public JButton getColorPickerButton() {
		if (jbtnColorPicker == null) {
			jbtnColorPicker = (JButton) formPanel.getComponentByName("jbtnColorPicker");

			try {
				Image img = ImageUtil.getImage("net/rptools/tokentool/image/colorPicker.png").getScaledInstance(20, 20, Image.SCALE_SMOOTH);

				jbtnColorPicker.setIcon(new ImageIcon(img));
				jbtnColorPicker.setText("");

				jbtnColorPicker.setBorderPainted(false);
				jbtnColorPicker.setContentAreaFilled(false);
				jbtnColorPicker.setFocusPainted(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			jbtnColorPicker.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (colorChooser == null) {
						colorChooser = new ColorChooserDialog(TokenTool.getFrame());
					} else {
						colorChooser.showGUI();
					}
				}
			});
		}

		return jbtnColorPicker;
	}

	public JCheckBox getSolidBackgroundCheckBox() {
		if (solidBackgroundCheckBox == null) {
			solidBackgroundCheckBox = formPanel.getCheckBox("solidBackground");
			solidBackgroundCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AppState.compositionProperties.setSolidBackground(solidBackgroundCheckBox.isSelected());
					TokenTool.getFrame().getTokenCompositionPanel().fireCompositionChanged();
				}
			});
		}
		return solidBackgroundCheckBox;
	}

	public JCheckBox getBaseCheckBox() {
		if (baseCheckBox == null) {
			baseCheckBox = formPanel.getCheckBox("base");
			baseCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AppState.compositionProperties.setBase(baseCheckBox.isSelected());
					TokenTool.getFrame().getTokenCompositionPanel().fireCompositionChanged();
					TokenTool.getFrame().getTokenCompositionPanel().repaint();
				}
			});
		}
		return baseCheckBox;
	}

	/**
	 * get file name prefix from text field
	 * 
	 * @author cif
	 * @return
	 */
	public String getNamePrefix() {
		return this.jtfNamePrefix.getText();
	}

	/**
	 * get file number from text field
	 * 
	 * @author cif
	 * @return
	 */
	public int getFileNumber() {
		String numberString = jtfFileNumber.getText();
		int number;
		try {
			number = Integer.parseInt(numberString);
		} catch (Exception e) {
			number = 0;
		}
		return number;
	}

	/**
	 * set fileNumber
	 * 
	 * @author cif
	 * @param number
	 */
	public void setFileNumber(int number) {
		jtfFileNumber.setText(String.valueOf(number));
	}

	/**
	 * should you use file numbering at all
	 * 
	 * @author cif
	 * @return boolean
	 */
	public boolean useFileNumbering() {
		return jcbUseFileNumbering.isSelected();
	}

	public boolean dragAsToken() {
		return dragAsTokenCheckBox.isSelected();
	}
}
