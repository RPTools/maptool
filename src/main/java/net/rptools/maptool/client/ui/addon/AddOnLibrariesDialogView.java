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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import net.rptools.maptool.client.AppActions.MapPreviewFileChooser;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.JLabelHyperLinkListener;
import net.rptools.maptool.client.ui.ViewAssetDialog;
import net.rptools.maptool.client.ui.addon.creator.MTLibCreator;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.addon.ExternalLibraryInfo;

/** Dialog for managing add-on libraries. */
public class AddOnLibrariesDialogView extends JDialog {

  /** Removes an add-on library. */
  private JButton buttonRemove;

  /** Closes the dialog. */
  private JButton buttonClose;

  /** The tabbed pane for the dialog. */
  private JTabbedPane tabbedPane;

  /** Table containing the imported add-ons */
  private JTable addOnLibraryTable;

  /** The text pane for the add-on description. */
  private JTextPane addOnDescriptionTextPane;

  /** Adds an add-on library. */
  private JButton buttonAdd;

  /** The name of the selected add-on. */
  private JLabel addOnNameLabel;

  /** The version of the selected add-on. */
  private JLabel addOnVersionLabel;

  /** The authors of the selected add-on. */
  private JLabel addOnAuthorsLabel;

  /** The namespace of the selected add-on. */
  private JLabel addOnNamespaceLabel;

  /** The short description of the selected add-on. */
  private JLabel addOnShortDescLabel;

  /** The website of the selected add-on. */
  private JLabel addOnWebsiteLabel;

  /** The git URL of the selected add-on. */
  private JLabel addOnGitUrlLabel;

  /** The license of the selected add-on. */
  private JLabel addOnLicenseLabel;

  /** The button for viewing the README file of the selected add-on. */
  private JButton viewReadMeFileButton;

  /** The button for viewing the license file of the selected add-on. */
  private JButton viewLicenseFileButton;

  /** The button for copying the theme CSS. */
  private JButton copyThemeCSS;

  /** The button for copying the stat sheet theme. */
  private JButton copyStatSheetThemeButton;

  /** The checkbox for enabling external add-ons. */
  private JCheckBox enableExternalAddOnCheckBox;

  /** The table for external add-ons. */
  private JTable externalAddonTable;

  /** The button for creating an add-on skeleton. */
  private JButton createAddonSkeletonButton;

  /** The text field for the add-on development directory. */
  private JTextField directoryTextField;

  /** The button for browsing to select the add-on development directory. */
  private JButton browseButton;

  /** The content pane for the dialog. */
  private JPanel contentPane;

  /** Creates a .mtlib (zip) file for the selected add-on. */
  private JButton createMTLibButton;

  /** The information for the selected add-on. */
  private LibraryInfo selectedAddOn;

  /** The model for the external add-on libraries table. */
  private final ExternalAddOnLibrariesTableModel externalAddOnLibrariesTableModel;

  /** The model for the add-on libraries table. */
  private final AddOnLibrariesTableModel addOnLibrariesTableModel;

  /** The currently selected external add-on. */
  private ExternalLibraryInfo selectedExternalAddon;

  /** Creates a new instance of the dialog. */
  public AddOnLibrariesDialogView() {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonClose);

    var eventBus = new MapToolEventBus().getMainEventBus();

    addOnLibrariesTableModel = new AddOnLibrariesTableModel();
    eventBus.register(addOnLibrariesTableModel);

    addOnLibraryTable.setModel(addOnLibrariesTableModel);
    addOnLibraryTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

    externalAddOnLibrariesTableModel = new ExternalAddOnLibrariesTableModel();
    externalAddonTable.setModel(externalAddOnLibrariesTableModel);
    eventBus.register(externalAddOnLibrariesTableModel);

    externalAddonTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    externalAddonTable.setDefaultRenderer(
        ExternalLibraryInfo.class, new ExternalAddOnImportCellEditor());
    externalAddonTable.setDefaultEditor(
        ExternalLibraryInfo.class, new ExternalAddOnImportCellEditor());
    externalAddonTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              int selectedRow = externalAddonTable.getSelectedRow();
              if (selectedRow == -1) {
                createMTLibButton.setEnabled(false);
                selectedExternalAddon = null;
              } else {
                createMTLibButton.setEnabled(true);
                selectedExternalAddon =
                    (ExternalLibraryInfo)
                        externalAddOnLibrariesTableModel.getValueAt(selectedRow, 6);
              }
            });

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
        e -> onClose(),
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
        e ->
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
                    }));

    copyStatSheetThemeButton.addActionListener(
        e ->
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
                    }));

    createAddonSkeletonButton.addActionListener(e -> createAddonSkeleton());

    enableExternalAddOnCheckBox.addActionListener(
        e -> {
          setExternalAddOnControlsEnabled(enableExternalAddOnCheckBox.isSelected());
          directoryTextField.setEnabled(enableExternalAddOnCheckBox.isSelected());
          try {
            new LibraryManager()
                .setExternalLibrariesEnabled(enableExternalAddOnCheckBox.isSelected());
          } catch (IOException ex) {
            // do nothing
          }
          AppPreferences.setExternalLibraryManagerEnabled(enableExternalAddOnCheckBox.isSelected());
        });

    browseButton.addActionListener(
        e -> {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(I18N.getText("library.dialog.import.title"));
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.showOpenDialog(MapTool.getFrame());
          if (chooser.getSelectedFile() != null) {
            directoryTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            AppPreferences.setExternalAddOnLibrariesPath(
                chooser.getSelectedFile().getAbsolutePath());
          }
        });

    createMTLibButton.addActionListener(
        e -> {
          if (selectedExternalAddon == null) {
            return;
          }

          var fileChooser = new JFileChooser();
          fileChooser.setFileFilter(
              new FileNameExtensionFilter(
                  I18N.getText("library.dialog.addon.fileFilter"), "mtlib"));
          if (fileChooser.showSaveDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
            var outputPath = fileChooser.getSelectedFile().toPath();
            if (!outputPath.toString().endsWith(".mtlib")) {
              outputPath = outputPath.resolveSibling(outputPath.getFileName() + ".mtlib");
            }
            var creator =
                new MTLibCreator(
                    selectedExternalAddon.backingDirectory(),
                    outputPath.getParent(),
                    outputPath.getFileName().toString());
            creator.create();
          }
        });

    createMTLibButton.setEnabled(false);

    LibraryManager libraryManager = new LibraryManager();
    enableExternalAddOnCheckBox.setSelected(libraryManager.externalLibrariesEnabled());
    directoryTextField.setText(AppPreferences.getExternalAddOnLibrariesPath());
    setExternalAddOnControlsEnabled(enableExternalAddOnCheckBox.isSelected());
    if (enableExternalAddOnCheckBox.isSelected()) {
      refreshLibraries();
    }
    pack();
  }

  /** Refreshes the external add-on libraries table information. */
  private void refreshLibraries() {
    var model = (ExternalAddOnLibrariesTableModel) externalAddonTable.getModel();
    model.refresh();
  }

  /**
   * Sets the enabled state of the external add-on controls.
   *
   * @param selected the state to set.
   */
  private void setExternalAddOnControlsEnabled(boolean selected) {
    externalAddonTable.setEnabled(selected);
    browseButton.setEnabled(selected);
  }

  /** Creates a new add-on skeleton. */
  private void createAddonSkeleton() {
    var dialog = new CreateNewAddonDialog();
    dialog.pack();
    SwingUtil.centerOver(dialog, MapTool.getFrame());
    dialog.setVisible(true);
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
    var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.unregister(externalAddOnLibrariesTableModel);
    eventBus.unregister(addOnLibrariesTableModel);
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

  /**
   * Views the license file for the given library.
   *
   * @param libInfo the library to view the license file for.
   */
  private void viewLicenseFile(LibraryInfo libInfo) {
    Optional<Library> lib = new LibraryManager().getLibrary(libInfo.namespace());
    lib.ifPresent(
        library ->
            library
                .getLicenseAsset()
                .thenAccept(
                    a ->
                        a.ifPresent(
                            asset ->
                                new ViewAssetDialog(
                                        asset,
                                        I18N.getText("library.dialog.addon.license"),
                                        640,
                                        480)
                                    .showModal())));
  }

  /**
   * Views the README file for the given library.
   *
   * @param libInfo the library to view the README file for.
   */
  private void viewReadMeFile(LibraryInfo libInfo) {
    Optional<Library> lib = new LibraryManager().getLibrary(libInfo.namespace());
    lib.ifPresent(
        library ->
            library
                .getReadMeAsset()
                .thenAccept(
                    a ->
                        a.ifPresent(
                            asset ->
                                new ViewAssetDialog(
                                        asset,
                                        I18N.getText("library.dialog.addon.readme"),
                                        640,
                                        480)
                                    .showModal())));
  }
}
