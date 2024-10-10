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

import com.jidesoft.swing.FolderChooser;
import io.sentry.Sentry;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppSetup;
import net.rptools.maptool.client.AppStatePersisted;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.RemoteFileDownloader;
import net.rptools.maptool.client.WebDownloader;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AddResourceDialog extends AbeillePanel<AddResourceDialog.Model> {
  private static final long serialVersionUID = -1709712124453405062L;

  private static final Logger log = LogManager.getLogger(AddResourceDialog.class);

  private static final String LIBRARY_URL = "http://library.rptools.net/1.3";
  private static final String LIBRARY_LIST_URL = LIBRARY_URL + "/listArtPacks";

  public enum Tab {
    LOCAL,
    WEB,
    RPTOOLS
  }

  private GenericDialog dialog;
  private Model model;
  private boolean downloadLibraryListInitiated;

  private boolean install = false;

  public AddResourceDialog() {
    super(new AddRessourcesDialogView().getRootComponent());

    setPreferredSize(new Dimension(550, 300));

    panelInit();
  }

  public boolean getInstall() {
    return install;
  }

  public void showDialog() {
    dialog = new GenericDialog(I18N.getText("action.addIconSelector"), MapTool.getFrame(), this);

    model = new Model();

    bind(model);

    getRootPane().setDefaultButton(getInstallButton());
    dialog.showDialog();
  }

  @Override
  public Model getModel() {
    return model;
  }

  public JButton getInstallButton() {
    return (JButton) getComponent("installButton");
  }

  public JTextField getBrowseTextField() {
    return (JTextField) getComponent("@localDirectory");
  }

  public JList getLibraryList() {
    return (JList) getComponent("@rptoolsList");
  }

  public void initLibraryList() {
    JList list = getLibraryList();
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    list.setModel(new MessageListModel(I18N.getText("dialog.addresource.downloading")));
  }

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
              switch (tabPane.getSelectedIndex()) {
                case 0:
                  model.tab = Tab.LOCAL;
                  break;
                case 1:
                  model.tab = Tab.WEB;
                  break;
                case 2:
                  model.tab = Tab.RPTOOLS;
                  downloadLibraryList();
                  break;
              }
            });
  }

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

  public void initInstallButton() {
    JButton button = (JButton) getComponent("installButton");
    button.addActionListener(
        e -> {
          install = true;
          if (commit()) {
            close();
          }
        });
  }

  public void initCancelButton() {
    JButton button = (JButton) getComponent("cancelButton");
    button.addActionListener(e -> close());
  }

  private void downloadLibraryList() {
    if (downloadLibraryListInitiated) {
      return;
    }

    // This pattern is safe because it is only called on the EDT
    downloadLibraryListInitiated = true;

    try {
      DownloadListWorker worker =
          new DownloadListWorker(
              getLibraryList(),
              new WebDownloader(new URL(LIBRARY_LIST_URL)),
              AppStatePersisted.getAssetRoots());
      worker.execute();
    } catch (MalformedURLException e) {
      MapTool.showMessage(
          "dialog.addresource.error.malformedurl",
          "Error",
          JOptionPane.ERROR_MESSAGE,
          LIBRARY_LIST_URL);
    }
  }

  @Override
  public boolean commit() {
    if (!super.commit()) {
      return false;
    }

    // Add the resource
    final List<LibraryRow> rowList = new ArrayList<LibraryRow>();

    switch (model.getTab()) {
      case LOCAL:
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

      case WEB:
        if (StringUtils.isEmpty(model.getUrlName())) {
          MapTool.showMessage(
              "dialog.addresource.warn.musthavename",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        }
        // validate the url format so that we don't hit it later
        try {
          new URL(model.getUrl());
        } catch (MalformedURLException e) {
          MapTool.showMessage(
              "dialog.addresource.warn.invalidurl",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getUrl());
          return false;
        }
        rowList.add(new LibraryRow(model.getUrlName(), model.getUrl(), -1));
        break;

      case RPTOOLS:
        Object[] selectedRows = getLibraryList().getSelectedValuesList().toArray();

        if (selectedRows == null || selectedRows.length == 0) {
          MapTool.showMessage(
              "dialog.addresource.warn.mustselectone", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        for (Object obj : selectedRows) {
          // Somehow a String is being returned instead of a LibraryRow object
          // in some cases.  See issue #343 on GitHub.
          if (obj instanceof String) {
            MapTool.showMessage(
                "dialog.addresource.warn.badresourceid", "Error", JOptionPane.ERROR_MESSAGE, obj);
            Sentry.capture("Add Resource to Library Error\nResource: " + obj);
            // Move on to next one...
            continue;
          }
          LibraryRow row = (LibraryRow) obj;

          // validate the url format
          row.path = LIBRARY_URL + "/" + row.path;
          try {
            new URL(row.path);
          } catch (MalformedURLException e) {
            MapTool.showMessage(
                "dialog.addresource.warn.invalidurl", "Error", JOptionPane.ERROR_MESSAGE, row.path);
            return false;
          }
          rowList.add(row);
        }
        break;
    }

    new SwingWorker<Object, Object>() {
      @Override
      protected Object doInBackground() {
        for (LibraryRow row : rowList) {
          try {
            RemoteFileDownloader downloader = new RemoteFileDownloader(new URL(row.path));
            File tmpFile = downloader.read();
            AppSetup.installLibrary(row.name, tmpFile.toURI().toURL());
            tmpFile.delete();
          } catch (IOException e) {
            log.error("Error downloading library: " + e, e);
            MapTool.showInformation("dialog.addresource.warn.couldnotload");
          }
        }
        return null;
      }
    }.execute();
    return true;
  }

  private void close() {
    unbind();
    dialog.closeDialog();
  }

  public static class Model {
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
}

class LibraryRow {
  final String name;
  String path;
  final int size;

  LibraryRow(String name, String path, int size) {
    this.name = name.trim();
    this.path = path.trim();
    this.size = size;
  }

  LibraryRow(String row) {
    String[] data = row.split("\\|");

    name = data[0].trim();
    path = data[1].trim();
    size = Integer.parseInt(data[2]);
  }

  @Override
  public String toString() {
    return "<html><b>" + name + "</b> <i>(" + getSizeString() + ")</i>";
  }

  private String getSizeString() {
    NumberFormat format = NumberFormat.getNumberInstance();
    if (size < 1000) {
      return format.format(size) + " bytes";
    }
    if (size < 1000000) {
      return format.format(size / 1000) + " k";
    }
    return format.format(size / 1000000) + " mb";
  }
}

class MessageListModel extends AbstractListModel {
  private final String message;

  public MessageListModel(String message) {
    this.message = message;
  }

  public Object getElementAt(int index) {
    return message;
  }

  public int getSize() {
    return 1;
  }
}

class DownloadListWorker extends SwingWorker<Object, Object> {
  private static final Logger log = LogManager.getLogger(DownloadListWorker.class);
  private ListModel model;
  private JList<LibraryRow> jList;
  private WebDownloader downloader;
  private Set<File> assetRoots;

  DownloadListWorker(JList<LibraryRow> jList, WebDownloader downloader, Set<File> assetRoots) {
    this.jList = jList;
    this.downloader = downloader;
    this.assetRoots = assetRoots;
  }

  @Override
  protected Object doInBackground() throws Exception {
    String result = null;
    try {
      result = downloader.read();
    } finally {
      if (result == null) {
        model = new MessageListModel(I18N.getText("dialog.addresource.errorDownloading"));
      }
    }
    DefaultListModel<LibraryRow> listModel = new DefaultListModel<>();

    // Create a list to compare against for dups
    List<String> libraryNameList = new ArrayList<>();
    for (File file : assetRoots) {
      libraryNameList.add(file.getName());
    }

    try {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes())));
      List<LibraryRow> tempRows = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        LibraryRow row = new LibraryRow(line);

        // Don't include if we've already got it
        if (!libraryNameList.contains(row.name)) {
          tempRows.add(row);
        }
      }
      tempRows.sort(Comparator.comparing(o -> o.name));
      for (LibraryRow row : tempRows) {
        listModel.addElement(row);
      }
      model = listModel;
    } catch (Throwable t) {
      log.error("unable to parse library list", t);
      model = new MessageListModel(I18N.getText("dialog.addresource.errorDownloading"));
    }
    return null;
  }

  @Override
  protected void done() {
    jList.setModel(model);
  }
}
