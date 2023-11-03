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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import net.rptools.maptool.client.AppActions.MapPreviewFileChooser;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.JLabelHyperLinkListener;
import net.rptools.maptool.client.ui.ViewAssetDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryManager;
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

  private LibraryInfo selectedAddOn;

  /** Creates a new instance of the dialog. */
  public AddOnLibrariesDialogView() {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonClose);
    addOnLibraryTable.setModel(new AddOnLibrariesTableModel());
    addOnLibraryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

  {
    // GUI initializer generated by IntelliJ IDEA GUI Designer
    // >>> IMPORTANT!! <<<
    // DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }
  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
   * call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    contentPane = new JPanel();
    contentPane.setLayout(
        new GridLayoutManager(2, 1, new Insets(10, 5, 10, 5), -1, -1, true, false));
    contentPane.setMinimumSize(new Dimension(800, 400));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(
        panel1,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            new Dimension(800, -1),
            new Dimension(800, 450),
            null,
            0,
            false));
    tabbedPane = new JTabbedPane();
    panel1.add(
        tabbedPane,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            new Dimension(700, -1),
            new Dimension(800, 200),
            null,
            0,
            false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    tabbedPane.addTab(
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.library.label"),
        panel2);
    final JSplitPane splitPane1 = new JSplitPane();
    splitPane1.setOrientation(0);
    panel2.add(
        splitPane1,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null,
            new Dimension(800, 200),
            null,
            0,
            false));
    final JScrollPane scrollPane1 = new JScrollPane();
    scrollPane1.setMinimumSize(new Dimension(800, 100));
    scrollPane1.setName("");
    scrollPane1.setPreferredSize(new Dimension(800, 420));
    splitPane1.setLeftComponent(scrollPane1);
    addOnLibraryTable = new JTable();
    scrollPane1.setViewportView(addOnLibraryTable);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(6, 7, new Insets(0, 0, 0, 0), -1, -1));
    panel3.setMinimumSize(new Dimension(800, 100));
    panel3.setPreferredSize(new Dimension(800, 250));
    splitPane1.setRightComponent(panel3);
    final JLabel label1 = new JLabel();
    Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
    if (label1Font != null) label1.setFont(label1Font);
    this.$$$loadLabelText$$$(
        label1,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.name"));
    panel3.add(
        label1,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    addOnNameLabel = new JLabel();
    addOnNameLabel.setText("");
    panel3.add(
        addOnNameLabel,
        new GridConstraints(
            0,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(250, -1),
            null,
            null,
            0,
            false));
    final JLabel label2 = new JLabel();
    Font label2Font = this.$$$getFont$$$(null, Font.BOLD, -1, label2.getFont());
    if (label2Font != null) label2.setFont(label2Font);
    this.$$$loadLabelText$$$(
        label2,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.version"));
    panel3.add(
        label2,
        new GridConstraints(
            0,
            2,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    addOnVersionLabel = new JLabel();
    addOnVersionLabel.setText("");
    panel3.add(
        addOnVersionLabel,
        new GridConstraints(
            0,
            3,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(200, -1),
            null,
            null,
            0,
            false));
    final JLabel label3 = new JLabel();
    Font label3Font = this.$$$getFont$$$(null, Font.BOLD, -1, label3.getFont());
    if (label3Font != null) label3.setFont(label3Font);
    this.$$$loadLabelText$$$(
        label3,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.namespace"));
    panel3.add(
        label3,
        new GridConstraints(
            1,
            0,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            new Dimension(73, 19),
            null,
            0,
            false));
    addOnNamespaceLabel = new JLabel();
    addOnNamespaceLabel.setText("");
    panel3.add(
        addOnNamespaceLabel,
        new GridConstraints(
            1,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(250, -1),
            new Dimension(33, 19),
            null,
            0,
            false));
    final JLabel label4 = new JLabel();
    Font label4Font = this.$$$getFont$$$(null, Font.BOLD, -1, label4.getFont());
    if (label4Font != null) label4.setFont(label4Font);
    this.$$$loadLabelText$$$(
        label4,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.website"));
    panel3.add(
        label4,
        new GridConstraints(
            2,
            0,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    addOnShortDescLabel = new JLabel();
    addOnShortDescLabel.setText("");
    panel3.add(
        addOnShortDescLabel,
        new GridConstraints(
            1,
            3,
            1,
            2,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(200, -1),
            null,
            null,
            0,
            false));
    addOnWebsiteLabel = new JLabel();
    addOnWebsiteLabel.setText("");
    panel3.add(
        addOnWebsiteLabel,
        new GridConstraints(
            2,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(250, -1),
            null,
            null,
            0,
            false));
    final JLabel label5 = new JLabel();
    Font label5Font = this.$$$getFont$$$(null, Font.BOLD, -1, label5.getFont());
    if (label5Font != null) label5.setFont(label5Font);
    this.$$$loadLabelText$$$(
        label5,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.license"));
    panel3.add(
        label5,
        new GridConstraints(
            4,
            0,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    final JScrollPane scrollPane2 = new JScrollPane();
    panel3.add(
        scrollPane2,
        new GridConstraints(
            5,
            0,
            1,
            7,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            null,
            new Dimension(1, 20),
            null,
            0,
            false));
    addOnDescriptionTextPane = new JTextPane();
    addOnDescriptionTextPane.setMinimumSize(new Dimension(1, 100));
    scrollPane2.setViewportView(addOnDescriptionTextPane);
    final JLabel label6 = new JLabel();
    Font label6Font = this.$$$getFont$$$(null, Font.BOLD, -1, label6.getFont());
    if (label6Font != null) label6.setFont(label6Font);
    this.$$$loadLabelText$$$(
        label6,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.shortDescription"));
    panel3.add(
        label6,
        new GridConstraints(
            1,
            2,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            new Dimension(107, 19),
            null,
            0,
            false));
    addOnLicenseLabel = new JLabel();
    addOnLicenseLabel.setText("");
    panel3.add(
        addOnLicenseLabel,
        new GridConstraints(
            4,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(250, -1),
            null,
            null,
            0,
            false));
    final JLabel label7 = new JLabel();
    label7.setEnabled(true);
    Font label7Font = this.$$$getFont$$$(null, Font.BOLD, -1, label7.getFont());
    if (label7Font != null) label7.setFont(label7Font);
    this.$$$loadLabelText$$$(
        label7,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.giturl"));
    panel3.add(
        label7,
        new GridConstraints(
            3,
            0,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    addOnGitUrlLabel = new JLabel();
    addOnGitUrlLabel.setText("");
    panel3.add(
        addOnGitUrlLabel,
        new GridConstraints(
            3,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(250, -1),
            null,
            null,
            0,
            false));
    viewLicenseFileButton = new JButton();
    this.$$$loadButtonText$$$(
        viewLicenseFileButton,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.licenseFile"));
    panel3.add(
        viewLicenseFileButton,
        new GridConstraints(
            3,
            2,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            1,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    viewReadMeFileButton = new JButton();
    this.$$$loadButtonText$$$(
        viewReadMeFileButton,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.readMeFile"));
    panel3.add(
        viewReadMeFileButton,
        new GridConstraints(
            4,
            2,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            1,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    final JLabel label8 = new JLabel();
    Font label8Font = this.$$$getFont$$$(null, Font.BOLD, -1, label8.getFont());
    if (label8Font != null) label8.setFont(label8Font);
    this.$$$loadLabelText$$$(
        label8,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.addon.authors"));
    panel3.add(
        label8,
        new GridConstraints(
            2,
            2,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    addOnAuthorsLabel = new JLabel();
    addOnAuthorsLabel.setText("");
    panel3.add(
        addOnAuthorsLabel,
        new GridConstraints(
            2,
            3,
            1,
            3,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            new Dimension(200, -1),
            null,
            null,
            0,
            false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
    tabbedPane.addTab(
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.development.label"),
        panel4);
    final Spacer spacer1 = new Spacer();
    panel4.add(
        spacer1,
        new GridConstraints(
            3,
            1,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            1,
            null,
            null,
            null,
            0,
            false));
    final Spacer spacer2 = new Spacer();
    panel4.add(
        spacer2,
        new GridConstraints(
            4,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_VERTICAL,
            1,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            null,
            null,
            null,
            0,
            false));
    final JLabel label9 = new JLabel();
    this.$$$loadLabelText$$$(
        label9,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.copy.title"));
    panel4.add(
        label9,
        new GridConstraints(
            2,
            0,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    copyThemeCSS = new JButton();
    this.$$$loadButtonText$$$(
        copyThemeCSS,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.copyMTThemeCSS"));
    panel4.add(
        copyThemeCSS,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    copyStatSheetThemeButton = new JButton();
    this.$$$loadButtonText$$$(
        copyStatSheetThemeButton,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.copyMTStatSheetTheme"));
    panel4.add(
        copyStatSheetThemeButton,
        new GridConstraints(
            1,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(
        panel5,
        new GridConstraints(
            1,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null,
            null,
            null,
            0,
            false));
    buttonRemove = new JButton();
    buttonRemove.setBorderPainted(true);
    this.$$$loadButtonText$$$(
        buttonRemove,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.table.remove"));
    buttonRemove.setVerifyInputWhenFocusTarget(false);
    panel5.add(
        buttonRemove,
        new GridConstraints(
            0,
            1,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            1,
            false));
    buttonClose = new JButton();
    this.$$$loadButtonText$$$(
        buttonClose,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.button.close"));
    panel5.add(
        buttonClose,
        new GridConstraints(
            0,
            2,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false));
    buttonAdd = new JButton();
    buttonAdd.setActionCommand("Add Library");
    this.$$$loadButtonText$$$(
        buttonAdd,
        this.$$$getMessageFromBundle$$$(
            "net/rptools/maptool/language/i18n", "library.dialog.button.add"));
    panel5.add(
        buttonAdd,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            1,
            false));
  }

  /**
   * @noinspection ALL
   */
  private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
    if (currentFont == null) return null;
    String resultName;
    if (fontName == null) {
      resultName = currentFont.getName();
    } else {
      Font testFont = new Font(fontName, Font.PLAIN, 10);
      if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
        resultName = fontName;
      } else {
        resultName = currentFont.getName();
      }
    }
    Font font =
        new Font(
            resultName,
            style >= 0 ? style : currentFont.getStyle(),
            size >= 0 ? size : currentFont.getSize());
    boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
    Font fontWithFallback =
        isMac
            ? new Font(font.getFamily(), font.getStyle(), font.getSize())
            : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
    return fontWithFallback instanceof FontUIResource
        ? fontWithFallback
        : new FontUIResource(fontWithFallback);
  }

  private static Method $$$cachedGetBundleMethod$$$ = null;

  private String $$$getMessageFromBundle$$$(String path, String key) {
    ResourceBundle bundle;
    try {
      Class<?> thisClass = this.getClass();
      if ($$$cachedGetBundleMethod$$$ == null) {
        Class<?> dynamicBundleClass =
            thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
        $$$cachedGetBundleMethod$$$ =
            dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
      }
      bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
    } catch (Exception e) {
      bundle = ResourceBundle.getBundle(path);
    }
    return bundle.getString(key);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) break;
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) break;
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }
}
