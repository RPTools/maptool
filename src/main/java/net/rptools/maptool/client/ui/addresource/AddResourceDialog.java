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
package net.rptools.maptool.client.ui.addresource;

import com.google.common.collect.ImmutableList;
import com.jidesoft.swing.FolderChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppSetup;
import net.rptools.maptool.client.AppStatePersisted;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.GenericDialogFactory;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.library.Library;
import net.rptools.maptool.util.library.LibraryUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class AddResourceDialog extends AbeillePanel<AddResourceDialog.Model> {
  private enum Tab {
    LOCAL,
    WEB,
    RPTOOLS
  }

  private final GenericDialogFactory dialogFactory =
      GenericDialog.getFactory()
          .setDialogTitle(I18N.getText("action.addIconSelector"))
          .addButton(ButtonKind.CANCEL)
          .setCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  private boolean downloadLibraryListInitiated;

  public AddResourceDialog() {
    super(new AddResourceDialogView().getRootComponent());
    setPreferredSize(new Dimension(550, 300));
    panelInit();
    dialogFactory
        .setContent(this)
        .addButton(ButtonKind.INSTALL)
        .setDefaultButton(ButtonKind.INSTALL);
  }

  public void showDialog() {
    bind(new Model());
    dialogFactory.display();
  }

  private JTextField getBrowseTextField() {
    return (JTextField) getComponent("@localDirectory");
  }

  private JLabel getDownloadingLabel() {
    return getLabel("downloadingLabel");
  }

  private Container getLibraryListPane() {
    return (Container) getComponent("rptoolsListPane");
  }

  private JTable getLibraryTable() {
    return (JTable) getComponent("libraryTable");
  }

  private void setLibraryListVisible(boolean visible) {
    // Actually show or hide the scroll pane that contains it.
    getLibraryListPane().setVisible(visible);
  }

  @SuppressWarnings("unused")
  public void initLibraryTable() {
    JTable table = getLibraryTable();
    table.setModel(new LibraryTableModel(List.of()));
    table.setDefaultRenderer(Long.class, new FileSizeCellRenderer());
    table.getTableHeader().setReorderingAllowed(false);
    setLibraryListVisible(false);

    // The library name needs most of the real estate, while the author name and library size don't
    // need nearly as much. At the same time, we want to let the user resize columns as they wish.
    // So to start, we have our list of default proportions that we enforce. But once the user gets
    // involved, we stop using them by clearing the weights.
    var columnWeights = new ArrayList<>(List.of(0.6, 0.25, 0.15));
    var columnModel = table.getColumnModel();
    table.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            int total = table.getWidth();
            for (int i = 0; i < columnWeights.size(); ++i) {
              columnModel.getColumn(i).setPreferredWidth((int) (total * columnWeights.get(i)));
            }
          }
        });
    // Let the user resize columns to modify the default weights.
    columnModel.addColumnModelListener(
        new TableColumnModelListener() {
          @Override
          public void columnMarginChanged(ChangeEvent e) {
            if (table.getTableHeader().getResizingColumn() != null) {
              // User-initiated event. Time to stop enforcing our default proportions.
              columnWeights.clear();
            }
          }

          @Override
          public void columnAdded(TableColumnModelEvent e) {}

          @Override
          public void columnRemoved(TableColumnModelEvent e) {}

          @Override
          public void columnMoved(TableColumnModelEvent e) {}

          @Override
          public void columnSelectionChanged(ListSelectionEvent e) {}
        });
  }

  @SuppressWarnings("unused")
  public void initDownloadingLabel() {
    var label = getDownloadingLabel();
    label.setForeground(Color.BLACK);
    label.setText(I18N.getText("dialog.addresource.downloading"));
    label.setVisible(true);
  }

  @SuppressWarnings("unused")
  public void initTabPane() {

    final JTabbedPane tabPane = (JTabbedPane) getComponent("tabPane");

    tabPane.setIconAt(0, RessourceManager.getBigIcon(Icons.ADD_RESSOURCE_LOCAL));
    tabPane.setIconAt(1, RessourceManager.getBigIcon(Icons.ADD_RESSOURCE_WEB));
    tabPane.setIconAt(2, RessourceManager.getBigIcon(Icons.ADD_RESSOURCE_RPTOOLS));
    tabPane
        .getModel()
        .addChangeListener(
            e -> {
              // Hmmm, this is fragile (breaks if the order changes) rethink this later
              var model = getModel();
              switch (tabPane.getSelectedIndex()) {
                case 0 -> model.tab = Tab.LOCAL;
                case 1 -> model.tab = Tab.WEB;
                case 2 -> {
                  model.tab = Tab.RPTOOLS;
                  downloadLibraryList();
                }
              }
            });
  }

  @SuppressWarnings("unused")
  public void initLocalDirectoryButton() {
    final JButton button = (JButton) getComponent("localDirectoryButton");
    button.addActionListener(
        e -> {
          FolderChooser folderChooser = new FolderChooser();
          folderChooser.setCurrentDirectory(
              MapTool.getFrame().getLoadFileChooser().getCurrentDirectory());
          folderChooser.setRecentListVisible(false);
          folderChooser.setFileHidingEnabled(true);
          folderChooser.setDialogTitle(I18N.getText("msg.title.loadAssetTree"));

          int result = folderChooser.showOpenDialog(button.getTopLevelAncestor());
          if (result == FolderChooser.APPROVE_OPTION) {
            File root = folderChooser.getSelectedFolder();
            getBrowseTextField().setText(root.getAbsolutePath());
          }
        });
  }

  private void downloadLibraryList() {
    if (downloadLibraryListInitiated) {
      return;
    }

    // This pattern is safe because it is only called on the EDT
    downloadLibraryListInitiated = true;

    var downloadingLabel = getDownloadingLabel();
    var libraryTable = getLibraryTable();

    LibraryUtils.downloadLibraryList()
        .whenCompleteAsync(
            (librariesString, t) -> {
              List<Library> libraries;
              if (t != null) {
                downloadingLabel.setText(I18N.getText("dialog.addresource.errorDownloading"));
                downloadingLabel.setForeground(new Color(255, 56, 0));
                downloadingLabel.setVisible(true);
                setLibraryListVisible(false);

                libraries = List.of();
              } else {
                downloadingLabel.setVisible(false);
                setLibraryListVisible(true);

                // No need to show the user libraries they already have.
                var assetRoots =
                    AppStatePersisted.getAssetRoots().stream().map(File::getName).toList();

                libraries =
                    LibraryUtils.parseLibraryList(librariesString).stream()
                        .filter(l -> !assetRoots.contains(l.name()))
                        .sorted(Comparator.comparing(Library::name))
                        .toList();
              }
              libraryTable.setModel(new LibraryTableModel(libraries));
            },
            SwingUtilities::invokeLater);
  }

  @Override
  public boolean commit() {
    if (!super.commit()) {
      return false;
    }

    // Add the resource
    final List<Library> rowList = new ArrayList<>();

    var model = getModel();
    switch (model.getTab()) {
      case LOCAL -> {
        if (StringUtils.isEmpty(model.getLocalDirectory())) {
          MapTool.showMessage(
              "dialog.addresource.warn.filenotfound",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        }
        File root = new File(model.getLocalDirectory());
        if (!root.exists()) {
          MapTool.showMessage(
              "dialog.addresource.warn.filenotfound",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        }
        if (!root.isDirectory()) {
          MapTool.showMessage(
              "dialog.addresource.warn.directoryrequired",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        }
        AppSetup.installLibrary(FileUtil.getNameWithoutExtension(root), root);
        return true;
      }
      case WEB -> {
        if (StringUtils.isEmpty(model.getUrlName())) {
          MapTool.showMessage(
              "dialog.addresource.warn.musthavename",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        }
        // validate the url format so that we don't hit it later
        URL url;
        try {
          url = new URI(model.getUrl()).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
          MapTool.showMessage(
              "dialog.addresource.warn.invalidurl",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getUrl());
          return false;
        }
        rowList.add(new Library(model.getUrlName(), url, -1, null));
      }

      case RPTOOLS -> {
        var table = getLibraryTable();
        var selectedRows = table.getSelectedRows();

        if (selectedRows == null || selectedRows.length == 0) {
          MapTool.showMessage(
              "dialog.addresource.warn.mustselectone", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }

        var tableModel = (LibraryTableModel) getLibraryTable().getModel();
        for (var i : selectedRows) {
          rowList.add(tableModel.getRowAt(i));
        }
      }
    }

    LibraryUtils.downloadAndInstall(rowList)
        .thenAcceptAsync(
            downloadResult -> {
              if (!downloadResult.failures().isEmpty()) {
                MapTool.showError(
                    formatLibraryInstallMessage(
                        I18N.getText("dialog.addresource.error.couldNotInstallLibraries"),
                        downloadResult.failures()));
              }
            },
            SwingUtilities::invokeLater);

    return true;
  }

  private static String formatLibraryInstallMessage(String main, List<Library> libraries) {
    var messageBuilder = new StringBuilder().append("<html><body>").append(main).append("<ul>");
    for (var library : libraries) {
      messageBuilder.append("<li>").append(library.name());
    }
    messageBuilder.append("</ul></body></html>");
    return messageBuilder.toString();
  }

  protected static class Model {
    private String localDirectory;
    private String urlName;
    private String url;
    private Tab tab = Tab.LOCAL;

    public String getLocalDirectory() {
      return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
      this.localDirectory = localDirectory;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public Tab getTab() {
      return tab;
    }

    public void setTab(Tab tab) {
      this.tab = tab;
    }

    public String getUrlName() {
      return urlName;
    }

    public void setUrlName(String urlName) {
      this.urlName = urlName;
    }
  }

  private static final class LibraryTableModel extends AbstractTableModel {
    private static final int NAME_COLUMN_INDEX = 0;
    private static final int AUTHOR_COLUMN_INDEX = 1;
    private static final int SIZE_COLUMN_INDEX = 2;
    private final List<Library> rowList;

    private final List<String> columnNames =
        List.of(
            I18N.getText("dialog.addresource.column.libraryName"),
            I18N.getText("dialog.addresource.column.libraryAuthor"),
            I18N.getText("dialog.addresource.column.librarySize"));

    public LibraryTableModel(List<Library> rowList) {
      this.rowList = ImmutableList.copyOf(rowList);
    }

    public Library getRowAt(int rowIndex) {
      return rowList.get(rowIndex);
    }

    public int getColumnCount() {
      return columnNames.size();
    }

    public int getRowCount() {
      return rowList.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      var row = getRowAt(rowIndex);

      return switch (columnIndex) {
        case NAME_COLUMN_INDEX -> row.name();
        case AUTHOR_COLUMN_INDEX ->
            Objects.requireNonNullElseGet(
                row.author(), () -> I18N.getText("dialog.addresource.unknownAuthor"));
        case SIZE_COLUMN_INDEX -> row.size();
        default -> "";
      };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      // Data is not editable.
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return switch (columnIndex) {
        case NAME_COLUMN_INDEX -> String.class;
        case AUTHOR_COLUMN_INDEX -> String.class;
        case SIZE_COLUMN_INDEX -> Long.class;
        default -> String.class;
      };
    }

    @Override
    public String getColumnName(int columnIndex) {
      return columnNames.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }
  }

  /** Displays file sizes in a human readable format, right-aligned. */
  private static final class FileSizeCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      String text;
      if (value instanceof Long l) {
        text = FileUtils.byteCountToDisplaySize(l);
      } else if (value == null) {
        text = "";
      } else {
        text = value.toString();
      }
      setText(text);
      setHorizontalAlignment(SwingConstants.RIGHT);

      return this;
    }
  }
}
