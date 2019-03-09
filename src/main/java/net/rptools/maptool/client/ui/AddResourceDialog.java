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
package net.rptools.maptool.client.ui;

import com.jidesoft.swing.FolderChooser;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppSetup;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.RemoteFileDownloader;
import net.rptools.maptool.client.WebDownloader;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.language.I18N;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

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
    super("net/rptools/maptool/client/ui/forms/addResourcesDialog.xml");

    setPreferredSize(new Dimension(550, 300));

    panelInit();
  }

  public boolean getInstall() {
    return install;
  }

  public void showDialog() {
    dialog = new GenericDialog("Add Resource to Library", MapTool.getFrame(), this);

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

    tabPane
        .getModel()
        .addChangeListener(
            new ChangeListener() {
              public void stateChanged(ChangeEvent e) {
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
              }
            });
  }

  public void initLocalDirectoryButton() {
    final JButton button = (JButton) getComponent("localDirectoryButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

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
          }
        });
  }

  public void initInstallButton() {
    JButton button = (JButton) getComponent("installButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            install = true;
            if (commit()) {
              close();
            }
          }
        });
  }

  public void initCancelButton() {
    JButton button = (JButton) getComponent("cancelButton");
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            close();
          }
        });
  }

  private void downloadLibraryList() {
    if (downloadLibraryListInitiated) {
      return;
    }

    // This pattern is safe because it is only called on the EDT
    downloadLibraryListInitiated = true;

    new SwingWorker<Object, Object>() {
      ListModel model;

      @Override
      protected Object doInBackground() throws Exception {
        String result = null;
        try {
          WebDownloader downloader = new WebDownloader(new URL(LIBRARY_LIST_URL));
          result = downloader.read();
        } finally {
          if (result == null) {
            model = new MessageListModel(I18N.getText("dialog.addresource.errorDownloading"));
            return null;
          }
        }
        DefaultListModel listModel = new DefaultListModel();

        // Create a list to compare against for dups
        List<String> libraryNameList = new ArrayList<String>();
        for (File file : AppPreferences.getAssetRoots()) {
          libraryNameList.add(file.getName());
        }
        // Generate the list
        try {
          BufferedReader reader =
              new BufferedReader(
                  new InputStreamReader(new ByteArrayInputStream(result.getBytes())));
          String line = null;
          while ((line = reader.readLine()) != null) {
            LibraryRow row = new LibraryRow(line);

            // Don't include if we've already got it
            if (libraryNameList.contains(row.name)) {
              continue;
            }
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
        getLibraryList().setModel(model);
      }
    }.execute();
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
        try {
          AppSetup.installLibrary(FileUtil.getNameWithoutExtension(root), root);
        } catch (MalformedURLException e) {
          log.error("Bad path url: " + root.getPath(), e);
          MapTool.showMessage(
              "dialog.addresource.warn.badpath",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        } catch (IOException e) {
          log.error("IOException adding local root: " + root.getPath(), e);
          MapTool.showMessage(
              "dialog.addresource.warn.badpath",
              "Error",
              JOptionPane.ERROR_MESSAGE,
              model.getLocalDirectory());
          return false;
        }
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
      protected Object doInBackground() throws Exception {
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

  private static class LibraryRow {
    private final String name;
    private String path;
    private final int size;

    public LibraryRow(String name, String path, int size) {
      this.name = name.trim();
      this.path = path.trim();
      this.size = size;
    }

    public LibraryRow(String row) {
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

  private class MessageListModel extends AbstractListModel {
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
}
