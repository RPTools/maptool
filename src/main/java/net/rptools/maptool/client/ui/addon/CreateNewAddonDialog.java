/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.addon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.addon.creator.NewAddOnBuilder;
import net.rptools.maptool.client.ui.addon.creator.NewAddOnCreator;
import net.rptools.maptool.language.I18N;

/**
 * Dialog for creating a new Add-On library skeleton.
 */
public class CreateNewAddonDialog extends JDialog {

  /** The content pane. */
  private JPanel contentPane;

  /** OK button which will create the new Add-On. */
  private JButton buttonOK;

  /** Cancel button which will close the dialog. */
  private JButton buttonCancel;

  /** Text field for the Add-On name. */
  private JTextField nameTextField;

  /** Text field for the Add-On version. */
  private JTextField versionTextField;

  /** Check box for creating events. */
  private JCheckBox eventsCheckBox;

  /** Text pane for the Add-On description. */
  private JTextPane descriptionTextPane;

  /** Button for browsing for the directory to create the new add-on in. */
  private JButton directoryBrowseButton;

  /** Text field for the Add-On namespace. */
  private JTextField namespaceTextField;

  /** Text field for the Git URL. */
  private JTextField gitURLTextField;

  /** Text field for the website URL. */
  private JTextField websiteTextField;

  /** Text field for the license. */
  private JTextField licenseTextField;

  /** Text field for the authors. */
  private JTextField authorsTextField;

  /** Text field for the parent directory. */
  private JTextField parentDirectoryTextField;

  /** Check box for creating slash commands. */
  private JCheckBox slashCommandCheckBox;

  /** Check box for creating MTS properties. */
  private JCheckBox mtsPropCheckBox;

  /** Check box for creating UDFs. */
  private JCheckBox udfCheckBox;

  /** Text field for the short description. */
  private JTextField shortDescTextBox;

  /** Text field for the directory name. */
  private JTextField directoryTextField;

  /** Creates a new CreateNewAddonDialog. */
  public CreateNewAddonDialog() {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onOK();
          }
        });

    buttonCancel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onCancel();
          }
        });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onCancel();
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    // browse button
    directoryBrowseButton.addActionListener(
        e -> {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(I18N.getText("library.dialog.create.parentDir.title"));
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.showOpenDialog(MapTool.getFrame());
          if (chooser.getSelectedFile() != null) {
            var path = chooser.getSelectedFile().getAbsolutePath();
            parentDirectoryTextField.setText(path);
            AppPreferences.setCreateAddOnParentDir(path);
          }
        });

    var docChangeListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            validateInputs();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            validateInputs();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            validateInputs();
          }
        };

    namespaceTextField.getDocument().addDocumentListener(docChangeListener);
    nameTextField.getDocument().addDocumentListener(docChangeListener);
    versionTextField.getDocument().addDocumentListener(docChangeListener);
    parentDirectoryTextField.getDocument().addDocumentListener(docChangeListener);
    directoryTextField.getDocument().addDocumentListener(docChangeListener);
    authorsTextField.getDocument().addDocumentListener(docChangeListener);
    licenseTextField.getDocument().addDocumentListener(docChangeListener);
    shortDescTextBox.getDocument().addDocumentListener(docChangeListener);
    descriptionTextPane.getDocument().addDocumentListener(docChangeListener);

    setDefaults();

    validateInputs();
  }

  /**
   * Sets the default values for the new add-on fields.
   */
  private void setDefaults() {
    namespaceTextField.setText("net.some-example.addon");
    nameTextField.setText("Example Add-On");
    versionTextField.setText("0.0.1");
    shortDescTextBox.setText(I18N.getText("library.dialog.addon.create.shortDesc"));
    descriptionTextPane.setText(I18N.getText("library.dialog.addon.create.longDesc"));
    parentDirectoryTextField.setText(AppPreferences.getCreateAddOnParentDir());
  }

  /**
   * Validates the input fields.
   */
  private void validateInputs() {
    buttonOK.setEnabled(
        !namespaceTextField.getText().isEmpty()
            && !nameTextField.getText().isEmpty()
            && !versionTextField.getText().isEmpty()
            && !parentDirectoryTextField.getText().isEmpty()
            && !directoryTextField.getText().isEmpty()
            && !authorsTextField.getText().isEmpty());
  }

  /**
   * Handles the OK button click.
   * This will attempt to create the new add-on skeleton based on the users inputs and then close
   * the dialog.
   */
  private void onOK() {
    var parentDir = new File(parentDirectoryTextField.getText());
    if (!parentDir.exists()) {
      JOptionPane.showMessageDialog(
          this, I18N.getText("library.dialog.addon.create.noSuchDir", parentDir.toString()));
      return;
    }
    var dir = parentDir.toPath().resolve(directoryTextField.getText()).toFile();
    if (dir.exists()) {
      JOptionPane.showMessageDialog(
          this, I18N.getText("library.dialog.addon.create.dirExists", dir.toString()));
      return;
    }

    var newAddon =
        new NewAddOnBuilder()
            .setName(nameTextField.getText())
            .setVersion(versionTextField.getText())
            .setNamespace(namespaceTextField.getText())
            .setGitURL(gitURLTextField.getText())
            .setWebsite(websiteTextField.getText())
            .setLicense(licenseTextField.getText())
            .setShortDescription(shortDescTextBox.getText())
            .setDescription(descriptionTextPane.getText())
            .setAuthors(List.of(authorsTextField.getText().split(",")))
            .setCreateEvents(eventsCheckBox.isSelected())
            .setCreateSlashCommands(slashCommandCheckBox.isSelected())
            .setCreateUDFs(udfCheckBox.isSelected())
            .setCreateMTSProperties(mtsPropCheckBox.isSelected())
            .build();
    dispose();
    if (!dir.mkdirs()) {
      JOptionPane.showMessageDialog(
          this, I18N.getText("library.dialog.failedToCreateDir", dir.toString()));
      return;
    }
    try {
      new NewAddOnCreator(newAddon, dir.toPath()).create();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  /**
   * Handles the cancel button click.
   * This closes the dialog without attempting to create the new add-on skeleton.
   */
  private void onCancel() {
    dispose();
  }
}
