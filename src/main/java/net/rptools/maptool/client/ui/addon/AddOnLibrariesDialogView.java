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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.AppActions.MapPreviewFileChooser;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.JLabelHyperLinkListener;
import net.rptools.maptool.client.ui.ViewAssetDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;

/** Dialog for managing add-on libraries. */
public class AddOnLibrariesDialogView extends JDialog {

  private JPanel contentPane;
  private JButton buttonRemove;
  private JButton buttonClose;
  private JTabbedPane tabbedPane;
  private JTable addOnLibraryTable;
  private JTextPane addOnDescriptionTextPane;
  private JButton buttonAdd;
  private JLabel addOnNameLabel;
  private JLabel addOnVersionLabel;
  private JLabel addOnAuthorsLabel;
  private JLabel addOnNamespaceLabel;
  private JLabel addOnShortDescLabel;
  private JLabel addOnWebsiteLabel;
  private JLabel addOnGitUrlLabel;
  private JLabel addOnLicenseLabel;
  private JButton viewReadMeFileButton;
  private JButton viewLicenseFileButton;
  private JButton copyThemeCSS;
  private JButton copyStatSheetThemeButton;
  private JCheckBox enableExternalAddOnCheckBox;
  private JTable externalAddonTable;
  private JButton createAddonSkeletonButton;
  private JTextField directoryTextField;
  private JButton browseButton;
  private JButton exportAddOn;

  private LibraryInfo selectedAddOn;

  /** Creates a new instance of the dialog. */
  public AddOnLibrariesDialogView() {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonClose);
    addOnLibraryTable.setModel(new AddOnLibrariesTableModel());
    addOnLibraryTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

    externalAddonTable.setModel(new ExternalAddOnLibrariesTableModel());
    externalAddonTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

    buttonRemove.setEnabled(false);
    addOnLibraryTable
        .getSelectionModel()
        .addListSelectionListener(
            evt -> {
              if (evt.getValueIsAdjusting()) {
                return;
              }
              int selectedRow = addOnLibraryTable.getSelectedRow();
              var model = (AddOnLibrariesTableModel) addOnLibraryTable.getModel();
              if (selectedRow == -1) {
                buttonRemove.setEnabled(false);
                selectedAddOnChanged(null);
              } else {
                buttonRemove.setEnabled(true);
                selectedAddOnChanged(model.getAddOn(selectedRow));
              }
            });

    buttonRemove.addActionListener(
        e -> {
          int row = addOnLibraryTable.getSelectedRow();
          if (row != -1) {
            var model = (AddOnLibrariesTableModel) addOnLibraryTable.getModel();
            var addon = model.getAddOn(row);
            if (MapTool.confirm("library.dialog.delete.confirm", addon.name())) {
              new LibraryManager().removeAddOnLibrary(addon.namespace());
              selectedAddOnChanged(null);
              model.fireTableDataChanged();
            }
          }
        });

    buttonClose.addActionListener(e -> onClose());
    buttonAdd.addActionListener(e -> addAddOnLibrary());

    viewReadMeFileButton.setEnabled(false);
    viewLicenseFileButton.setEnabled(false);

    // call onClose() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onClose();
          }
        });

    // call onClose() on ESCAPE
    contentPane.registerKeyboardAction(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onClose();
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    addOnGitUrlLabel.addMouseListener(new JLabelHyperLinkListener(addOnGitUrlLabel));
    addOnWebsiteLabel.addMouseListener(new JLabelHyperLinkListener(addOnWebsiteLabel));

    viewLicenseFileButton.addActionListener(
        l -> {
          if (selectedAddOn != null) {
            viewLicenseFile(selectedAddOn);
          }
        });

    viewReadMeFileButton.addActionListener(
        l -> {
          if (selectedAddOn != null) {
            viewReadMeFile(selectedAddOn);
          }
        });

    tabbedPane.addChangeListener(
        l -> {
          if (tabbedPane.getSelectedIndex() == 0) {
            buttonAdd.setEnabled(true);
            if (addOnLibraryTable.getSelectedRow() != -1) {
              buttonRemove.setEnabled(true);
            }
          } else {
            buttonAdd.setEnabled(false);
            buttonRemove.setEnabled(false);
          }
        });

    copyThemeCSS.addActionListener(
        e -> {
          new LibraryManager()
              .getLibrary(AppConstants.MT_BUILTIN_ADD_ON_NAMESPACE)
              .ifPresent(
                  library -> {
                    URI uri = URI.create(AppConstants.MT_THEME_CSS);
                    String themeCss = null;
                    try {
                      themeCss = library.readAsString(uri.toURL()).get();
                    } catch (InterruptedException | ExecutionException | IOException ex) {
                      throw new RuntimeException(ex);
                    }
                    Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(themeCss), null);
                  });
        });

    copyStatSheetThemeButton.addActionListener(
        e -> {
          new LibraryManager()
              .getLibrary(AppConstants.MT_BUILTIN_ADD_ON_NAMESPACE)
              .ifPresent(
                  library -> {
                    URI uri = URI.create(AppConstants.MT_THEME_STAT_SHEET_CSS);
                    String themeCss = null;
                    try {
                      themeCss = library.readAsString(uri.toURL()).get();
                    } catch (InterruptedException | ExecutionException | IOException ex) {
                      throw new RuntimeException(ex);
                    }
                    Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(themeCss), null);
                  });
        });

    createAddonSkeletonButton.addActionListener(
        e -> {
          createAddonSkeleton();
        });

    enableExternalAddOnCheckBox.addActionListener(
        e -> {
          setExternalAddOnControlsEnabled(enableExternalAddOnCheckBox.isSelected());
          directoryTextField.setEnabled(enableExternalAddOnCheckBox.isSelected());
          new LibraryManager()
              .setExternalLibrariesEnabled(enableExternalAddOnCheckBox.isSelected());
        });

    browseButton.addActionListener(
        e -> {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(I18N.getText("library.dialog.import.title"));
        });

    LibraryManager libraryManager = new LibraryManager();
    enableExternalAddOnCheckBox.setSelected(libraryManager.externalLibrariesEnabled());
    setExternalAddOnControlsEnabled(enableExternalAddOnCheckBox.isSelected());
  }

  private void setExternalAddOnControlsEnabled(boolean selected) {
    externalAddonTable.setEnabled(enableExternalAddOnCheckBox.isSelected());
    browseButton.setEnabled(enableExternalAddOnCheckBox.isSelected());
  }

  private void createAddonSkeleton() {
    LibraryManager library = new LibraryManager();
    try {
      library.registerExternalAddOnLibrary(new AddOnLibraryImporter()
              .importFromDirectory(Path.of(directoryTextField.getText())));
    } catch (IOException e) {
      MapTool.showError("library.import.ioError", e);
    }
  }

  /**
   * Updates the dialog with the information for the selected add-on. If no add-on is selected (null
   * passed), the dialog is cleared.
   *
   * @param addOn the selected add-on.
   */
  private void selectedAddOnChanged(LibraryInfo addOn) {
    selectedAddOn = addOn;
    if (addOn == null) {
      addOnNameLabel.setText(null);
      addOnVersionLabel.setText(null);
      addOnAuthorsLabel.setText(null);
      addOnNamespaceLabel.setText(null);
      addOnShortDescLabel.setText(null);
      addOnWebsiteLabel.setText(null);
      addOnGitUrlLabel.setText(null);
      addOnLicenseLabel.setText(null);
      addOnDescriptionTextPane.setText(null);
      viewLicenseFileButton.setEnabled(false);
      viewReadMeFileButton.setEnabled(false);
    } else {
      addOnNameLabel.setText(addOn.name());
      addOnVersionLabel.setText(addOn.version());
      addOnAuthorsLabel.setText(String.join(",", addOn.authors()));
      addOnNamespaceLabel.setText(addOn.namespace());
      addOnShortDescLabel.setText(addOn.shortDescription());
      addOnWebsiteLabel.setText(addOn.website());
      addOnGitUrlLabel.setText(addOn.gitUrl());
      addOnLicenseLabel.setText(addOn.license());
      addOnDescriptionTextPane.setText(addOn.description());
      viewLicenseFileButton.setEnabled(
          addOn.licenseFile() != null && !addOn.licenseFile().isEmpty());
      viewReadMeFileButton.setEnabled(addOn.readMeFile() != null && !addOn.readMeFile().isEmpty());
    }
  }

  /** Closes the dialog. */
  private void onClose() {
    dispose();
  }

  /** Add an add-on library to the library manager. */
  private void addAddOnLibrary() {
    JFileChooser chooser = new MapPreviewFileChooser();
    chooser.setDialogTitle(I18N.getText("library.dialog.import.title"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileFilter(AddOnLibraryImporter.getAddOnLibraryFileFilter());

    if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
      File libFile = chooser.getSelectedFile();
      try {
        var addOnLibrary = new AddOnLibraryImporter().importFromFile(libFile);
        var libraryManager = new LibraryManager();
        String namespace = addOnLibrary.getNamespace().get();
        if (libraryManager.addOnLibraryExists(addOnLibrary.getNamespace().get())) {
          if (!MapTool.confirm(I18N.getText("library.error.addOnLibraryExists", namespace))) {
            return;
          }
          libraryManager.deregisterAddOnLibrary(namespace);
        }
        libraryManager.reregisterAddOnLibrary(addOnLibrary);
      } catch (IOException | InterruptedException | ExecutionException e) {
        MapTool.showError("library.import.ioError", e);
      }
      ((AddOnLibrariesTableModel) addOnLibraryTable.getModel()).fireTableDataChanged();
    }
  }

  private void viewLicenseFile(LibraryInfo libInfo) {
    Optional<Library> lib = new LibraryManager().getLibrary(libInfo.namespace());
    lib.ifPresent(
        library ->
            library
                .getLicenseAsset()
                .thenAccept(
                    a ->
                        a.ifPresent(
                            asset -> new ViewAssetDialog(asset, "License", 640, 480).showModal())));
  }

  private void viewReadMeFile(LibraryInfo libInfo) {
    Optional<Library> lib = new LibraryManager().getLibrary(libInfo.namespace());
    lib.ifPresent(
        library ->
            library
                .getReadMeAsset()
                .thenAccept(
                    a ->
                        a.ifPresent(
                            asset -> new ViewAssetDialog(asset, "License", 640, 480).showModal())));
  }

  public static void main(String[] args) {
    AddOnLibrariesDialogView dialog = new AddOnLibrariesDialogView();
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
