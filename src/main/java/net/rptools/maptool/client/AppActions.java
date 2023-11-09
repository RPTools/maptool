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
package net.rptools.maptool.client;

import com.jidesoft.docking.DockableFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.boardtool.BoardTool;
import net.rptools.maptool.client.tool.gridtool.GridTool;
import net.rptools.maptool.client.ui.*;
import net.rptools.maptool.client.ui.MapToolFrame.MTFrame;
import net.rptools.maptool.client.ui.addon.AddOnLibrariesDialogView;
import net.rptools.maptool.client.ui.addresource.AddResourceDialog;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
import net.rptools.maptool.client.ui.assetpanel.Directory;
import net.rptools.maptool.client.ui.campaignproperties.CampaignPropertiesDialog;
import net.rptools.maptool.client.ui.connectioninfodialog.ConnectionInfoDialog;
import net.rptools.maptool.client.ui.connections.ClientConnectionPanel;
import net.rptools.maptool.client.ui.connecttoserverdialog.ConnectToServerDialog;
import net.rptools.maptool.client.ui.connecttoserverdialog.ConnectToServerDialogPreferences;
import net.rptools.maptool.client.ui.exportdialog.ExportDialog;
import net.rptools.maptool.client.ui.htmlframe.HTMLOverlayManager;
import net.rptools.maptool.client.ui.io.*;
import net.rptools.maptool.client.ui.io.FTPTransferObject.Direction;
import net.rptools.maptool.client.ui.mappropertiesdialog.MapPropertiesDialog;
import net.rptools.maptool.client.ui.players.PlayerDatabaseDialog;
import net.rptools.maptool.client.ui.preferencesdialog.PreferencesDialog;
import net.rptools.maptool.client.ui.startserverdialog.StartServerDialog;
import net.rptools.maptool.client.ui.startserverdialog.StartServerDialogPreferences;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.transferprogressdialog.TransferProgressDialog;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.utilities.DungeonDraftImporter;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.campaign.CampaignManager;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.player.*;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabaseFactory.PlayerDatabaseType;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.util.*;
import net.rptools.maptool.util.PersistenceUtil.PersistedCampaign;
import net.rptools.maptool.util.PersistenceUtil.PersistedMap;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class acts as a container for a wide variety of {@link Action}s that are used throughout the
 * application. Most of these are added to the main frame menu, but some are added dynamically as
 * needed, sometimes to the frame menu but also to the context menu (the "right-click menu").
 *
 * <p>Each object instantiated from {@link DefaultClientAction} should have an initializer that
 * calls {@link ClientAction#init(String)} and passes the base message key from the properties file.
 * This base message key will be used to locate the text that should appear on the menu item as well
 * as the accelerator, mnemonic, and short description strings. (See the {@link I18N} class for more
 * details on how the key is used.
 *
 * <p>In addition, each object should override {@link ClientAction#isAvailable()} and return true if
 * the application is in a state where the Action should be enabled. (The default is <code>true
 * </code>.)
 *
 * <p>Last is the {@link ClientAction#execute(ActionEvent)} method. It is passed the {@link
 * ActionEvent} object that triggered this Action as a parameter. It should perform the necessary
 * work to accomplish the effect of the Action.
 */
public class AppActions {
  private static final Logger log = LogManager.getLogger(AppActions.class);

  /**
   * Hold copies of tokens for a future paste.
   *
   * <p>Note that the tokens stored in this set have had their positions modified to be measured
   * relative to a reference token. This is inconsistent with the coordinate system for
   * `ZonePoint`s, so be sure not to assume these token positions cna be used as `ZonePoint`s
   * without further adjustment.
   */
  private static Set<Token> tokenCopySet = null;

  public static final int menuShortcut = getMenuShortcutKeyMask();
  private static boolean keepIdsOnPaste = false;

  private static int getMenuShortcutKeyMask() {
    int key = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    String prop = System.getProperty("os.name", "unknown");
    if ("darwin".equalsIgnoreCase(prop)) {
      // TODO Should we install our own AWTKeyStroke class? If we do it should only be if menu
      // shortcut is CTRL...
      if (key == InputEvent.CTRL_DOWN_MASK) key = InputEvent.META_DOWN_MASK;
      /*
       * In order for OpenJDK to work on Mac OS X, the user must have the X11 package installed unless they're running headless. However, in order for the Command key to work, the X11
       * Preferences must be set to "Enable the Meta Key" in X11 applications. Essentially, if this option is turned on, the Command key (called Meta in X11) will be intercepted by the X11
       * package and not sent to the application. The next step for MapTool will be better integration with the Mac desktop to eliminate the X11 menu altogether.
       */
    }
    return key;
  }

  /** This action will rotate through the PC tokens owned by the player. */
  public static final Action NEXT_TOKEN =
      new ZoneClientAction() {
        {
          init("menu.nextToken");
        }

        @Override
        protected void executeAction() {
          Token chosenOne = null;
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          List<Token> myPlayers = new ArrayList<Token>();
          for (Token t : renderer.getZone().getPlayerTokens()) {
            if (AppUtil.playerOwns(t) && t.isVisible() && renderer.getZone().isTokenVisible(t))
              myPlayers.add(t);
          }
          if (myPlayers.size() > 0) {
            // We want to wrap round the list of player tokens.
            // But this process only selects 1 player token.
            if (renderer.getSelectedTokensList().size() > 0) {
              Token selt = renderer.getSelectedTokensList().get(0);
              if (myPlayers.contains(selt)) chosenOne = selt;
            }
            if (chosenOne != null) {
              for (int i = 0; i < myPlayers.size(); i++) {
                if (myPlayers.get(i).equals(chosenOne)) {
                  if (i < myPlayers.size() - 1) chosenOne = myPlayers.get(i + 1);
                  else chosenOne = myPlayers.get(0);
                  break;
                }
              }
            } else {
              chosenOne = myPlayers.get(0);
            }
            // Move to chosen token
            if (chosenOne != null) {
              renderer.centerOnAndSetSelected(chosenOne);
            }
          }
        }
      };

  public static final Action MRU_LIST =
      new DefaultClientAction() {
        {
          init("menu.recent");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isHostingServer() || MapTool.isPersonalServer();
        }

        @Override
        protected void executeAction() {
          // Do nothing
        }
      };

  public static final ClientAction EXPORT_SCREENSHOT =
      new ZoneClientAction() {
        {
          init("action.exportScreenShotAs");
        }

        @Override
        protected void executeAction() {
          try {
            ExportDialog d = MapTool.getCampaign().getExportDialog();
            d.setVisible(true);
            MapTool.getCampaign().setExportDialog(d);
          } catch (Exception ex) {
            MapTool.showError("Cannot create the ExportDialog object", ex);
          }
        }
      };

  public static final Action EXPORT_SCREENSHOT_LAST_LOCATION =
      new ZoneClientAction() {
        {
          init("action.exportScreenShot");
        }

        @Override
        protected void executeAction() {
          ExportDialog d = MapTool.getCampaign().getExportDialog();
          if (d == null || d.getExportLocation() == null || d.getExportSettings() == null) {
            // Can't do a save.. so try "save as"
            EXPORT_SCREENSHOT.executeAction();
          } else {
            try {
              d.screenCapture();
            } catch (Exception ex) {
              MapTool.showError("msg.error.failedExportingImage", ex);
            }
          }
        }
      };

  public static final Action EXPORT_CAMPAIGN_REPO =
      new AdminClientAction() {

        {
          init("admin.exportCampaignRepo");
        }

        @Override
        protected void executeAction() {

          JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();

          boolean tryAgain = true;
          while (tryAgain) {
            // Get target location
            if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
              return;
            }
            var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
            var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
            if (saveDir.startsWith(installDir)) {
              MapTool.showWarning("msg.warning.exportRepoToInstallDir");
            } else {
              tryAgain = false;
            }
          }

          // Default extension
          File selectedFile = chooser.getSelectedFile();
          if (!selectedFile.getName().toUpperCase().endsWith(".ZIP")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".zip");
          }

          if (selectedFile.exists()) {
            if (!MapTool.confirm("msg.confirm.fileExists")) {
              return;
            }
          }

          // Create index
          Campaign campaign = MapTool.getCampaign();
          Set<Asset> assetSet = new HashSet<Asset>();
          for (Zone zone : campaign.getZones()) {

            for (MD5Key key : zone.getAllAssetIds()) {
              assetSet.add(AssetManager.getAsset(key));
            }
          }

          // Export to temp location
          File tmpFile =
              new File(
                  AppUtil.getAppHome("tmp").getAbsolutePath()
                      + "/"
                      + System.currentTimeMillis()
                      + ".export");

          try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmpFile));

            StringBuilder builder = new StringBuilder();
            for (Asset asset : assetSet) {

              // Index it
              builder
                  .append(asset.getMD5Key())
                  .append(" assets/")
                  .append(asset.getMD5Key())
                  .append("\n");
              // Save it
              ZipEntry entry = new ZipEntry("assets/" + asset.getMD5Key().toString());
              out.putNextEntry(entry);
              out.write(asset.getData());
            }

            // Handle the index
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            GZIPOutputStream gzout = new GZIPOutputStream(bout);
            gzout.write(builder.toString().getBytes());
            gzout.close();

            ZipEntry entry = new ZipEntry("index.gz");
            out.putNextEntry(entry);
            out.write(bout.toByteArray());
            out.closeEntry();

            out.close();

            // Move to new location
            File mvFile =
                new File(
                    AppUtil.getAppHome("tmp").getAbsolutePath() + "/" + selectedFile.getName());
            if (selectedFile.exists()) {
              FileUtil.copyFile(selectedFile, mvFile);
              selectedFile.delete();
            }

            FileUtil.copyFile(tmpFile, selectedFile);
            tmpFile.delete();

            if (mvFile.exists()) {
              mvFile.delete();
            }

          } catch (IOException ioe) {
            MapTool.showError(I18N.getString("msg.error.failedExportingCampaignRepo"), ioe);
            return;
          }

          MapTool.showInformation("msg.confirm.campaignExported");
        }
      };

  public static final Action UPDATE_CAMPAIGN_REPO =
      new DeveloperClientAction() {
        {
          init("admin.updateCampaignRepo");
        }

        /**
         * This action performs a repository update by comparing the assets in the current campaign
         * against all assets in all repositories and uploading assets to one of the repositories
         * and creating a replacement <b>index.gz</b> which is also uploaded.
         *
         * <p>For the purposes of this action, only the FTP protocol is supported. The primary
         * reason for this has to do with the way images will be uploaded. If HTTP were used and a
         * single file sent, there would need to be a script on the remote end that knew how to
         * unpack the file correctly. This cannot be assumed in the general case.
         *
         * <p>Using FTP, we can upload individual files to particular directories. While this same
         * approach could be used for HTTP, once again the user would have to install some kind of
         * upload script on the server side. This again makes HTTP impractical and FTP more
         * "user-friendly".
         *
         * <p><b>Implementation.</b> This method first makes a list of all known repositories from
         * the campaign properties. They are presented to the user who then selects one as the
         * destination for new assets to be uploaded. A list of assets currently used in the
         * campaign is then generated and compared against the index files of all repositories from
         * the previous list. Any new assets are aggregated and the user is presented with a summary
         * of the images to be uploaded, including file size. The user enters FTP connection
         * information and the upload begins as a separate thread. (Perhaps the Transfer Window can
         * be used to keep track of the uploading process?)
         *
         * <p><b>Optimizations.</b> At some point, creating the list of assets could be spun into
         * another thread, although there's probably not much value there. Or the FTP information
         * could be collected at the beginning and as assets are checked they could immediately
         * begin uploading with the summary including all assets, even those already uploaded.
         *
         * <p>My review of FTP client libraries brought me to <a href=
         * "http://www.javaworld.com/javaworld/jw-04-2003/jw-0404-ftp.html"> this extensive review
         * of FTP libraries</a> If we're going to do much more with FTP, <b>Globus GridFTP</b> looks
         * good, but the library itself is 2.7MB.
         */
        @Override
        protected void executeAction() {
          /*
           * 1. Ask the user to select repositories which should be considered. 2. Ask the user for FTP upload information.
           */
          UpdateRepoDialog urd;

          Campaign campaign = MapTool.getCampaign();
          CampaignProperties props = campaign.getCampaignProperties();

          urd =
              new UpdateRepoDialog(
                  MapTool.getFrame(),
                  props.getRemoteRepositoryList(),
                  MapTool.getCampaign().getExportDialog().getExportLocation());
          urd.pack();
          urd.setVisible(true);
          if (urd.getStatus() == JOptionPane.CANCEL_OPTION) {
            return;
          }
          MapTool.getCampaign().getExportDialog().setExportLocation(urd.getFTPLocation());

          /*
           * 3. Check all assets against the repository indices and build a new list from those that are not found.
           */
          Map<MD5Key, Asset> missing =
              AssetManager.findAllAssetsNotInRepositories(urd.getSelectedRepositories());

          /*
           * 4. Give the user a summary and ask for permission to begin the upload. I'm going to display a listbox and let the user click on elements of the list in order to see a preview to the
           * right. But there's no plan to make it a CheckBoxList. (Wouldn't be _that_ tough, however.)
           */
          if (!MapTool.confirm(I18N.getText("msg.confirm.aboutToBeginFTP", missing.size() + 1)))
            return;

          /*
           * 5. Build the index as we go, but add the images to FTP to a queue handled by another thread. Add a progress bar of some type or use the Transfer Status window.
           */
          try {
            File topdir = urd.getDirectory();
            File dir = new File(urd.isCreateSubdir() ? getFormattedDate(null) : ".");

            Map<String, String> repoEntries = new HashMap<String, String>(missing.size());
            FTPClient ftp = new FTPClient(urd.getHostname(), urd.getUsername(), urd.getPassword());

            // Enabling this means the upload begins immediately upon the first queued entry
            ftp.setEnabled(true);
            ProgressBarList pbl = new ProgressBarList(MapTool.getFrame(), ftp, missing.size() + 1);

            for (Map.Entry<MD5Key, Asset> entry : missing.entrySet()) {
              String remote = entry.getKey().toString();
              repoEntries.put(remote, new File(dir, remote).getPath());
              ftp.addToQueue(
                  new FTPTransferObject(
                      Direction.FTP_PUT, entry.getValue().getData(), dir, remote));
            }
            // We're done with "missing", so empty it now.
            missing.clear();

            // Handle the index
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            String saveTo = urd.getSaveToRepository();
            // When this runs our local 'repoindx' is updated. If the FTP upload later fails,
            // it doesn't really matter much because the assets are already there. However,
            // if our local cache is ever downloaded again, we'll "forget" that the assets are
            // on the server. It sounds like it might be nice to have some way to resync
            // the local system with the FTP server. But it's probably better to let the user
            // do it manually.
            byte[] index = AssetManager.updateRepositoryMap(saveTo, repoEntries);
            repoEntries.clear();
            GZIPOutputStream gzout = new GZIPOutputStream(bout);
            gzout.write(index);
            gzout.close();
            ftp.addToQueue(
                new FTPTransferObject(Direction.FTP_PUT, bout.toByteArray(), topdir, "index.gz"));
          } catch (IOException ioe) {
            MapTool.showError("msg.error.failedUpdatingCampaignRepo", ioe);
            return;
          }
        }

        private String getFormattedDate(Date d) {
          // Use today's date as the directory on the FTP server. This doesn't affect players'
          // ability to download it and might help the user determine what was uploaded to
          // their site and why. It can't hurt. :)
          SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
          df.applyPattern("yyyy-MM-dd");
          return df.format(d == null ? new Date() : d);
        }
      };

  /** This is the menu option that forces clients to display the GM's current map. */
  public static final Action ENFORCE_ZONE =
      new ZoneAdminClientAction() {

        {
          init("action.enforceZone");
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return;
          }

          MapTool.serverCommand().enforceZone(renderer.getZone().getId());
        }
      };

  public static final Action RESTORE_DEFAULT_IMAGES =
      new DefaultClientAction() {

        {
          init("action.restoreDefaultImages");
        }

        @Override
        protected void executeAction() {
          try {
            AppSetup.installDefaultTokens();

            // TODO: Remove this hardwiring
            File unzipDir =
                new File(AppConstants.UNZIP_DIR.getAbsolutePath() + File.separator + "Default");
            MapTool.getFrame().addAssetRoot(unzipDir);
            AssetManager.searchForImageReferences(unzipDir, AppConstants.IMAGE_FILE_FILTER);

          } catch (IOException ioe) {
            MapTool.showError("msg.error.failedAddingDefaultImages", ioe);
          }
        }
      };

  public static final Action ADD_DEFAULT_TABLES =
      new DefaultClientAction() {

        {
          init("action.addDefaultTables");
        }

        @Override
        protected void executeAction() {
          try {
            // Load the defaults
            InputStream in =
                AppActions.class
                    .getClassLoader()
                    .getResourceAsStream("net/rptools/maptool/client/defaultTables.mtprops");
            CampaignProperties properties = PersistenceUtil.loadCampaignProperties(in);
            in.close();

            // Make sure the images have been installed
            // Just pick a table and spot check
            LookupTable lookupTable = properties.getLookupTableMap().values().iterator().next();
            if (!AssetManager.hasAsset(lookupTable.getTableImage())) {
              AppSetup.installDefaultTokens();
            }

            MapTool.getCampaign().mergeCampaignProperties(properties);

            MapTool.getFrame().repaint();

          } catch (IOException ioe) {
            MapTool.showError("msg.error.failedAddingDefaultTables", ioe);
          }
        }
      };

  public static final Action RENAME_ZONE =
      new ZoneAdminClientAction() {

        {
          init("action.renameMap");
        }

        @Override
        protected void executeAction() {
          Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
          String oldName = zone.getName();
          if (oldName == null) oldName = "";
          String msg = I18N.getText("msg.confirm.renameMap", oldName);
          String name = JOptionPane.showInputDialog(MapTool.getFrame(), msg, oldName);
          if (name != null) {
            zone.setName(name);
            MapTool.serverCommand().renameZone(zone.getId(), name);
            MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
          }
        }
      };

  public static final Action SHOW_FULLSCREEN =
      new DefaultClientAction() {

        {
          init("action.fullscreen");
        }

        @Override
        protected void executeAction() {

          if (MapTool.getFrame().isFullScreen()) {
            MapTool.getFrame().showWindowed();
          } else {
            MapTool.getFrame().showFullScreen();
          }
        }
      };

  public static final Action TOGGLE_FULLSCREEN_TOOLS =
      new DefaultClientAction() {
        {
          init("action.toggleFullScreenTools");
        }

        @Override
        public boolean isSelected() {
          return AppState.isFullScreenUIEnabled();
        }

        @Override
        protected void executeAction() {
          AppState.setFullScreenUIEnabled(!AppState.isFullScreenUIEnabled());

          var frame = MapTool.getFrame();
          if (AppState.isFullScreenUIEnabled()
              && !frame.areFullScreenToolsShown()
              && frame.isFullScreen()) {
            frame.showFullScreenTools();
          } else if (!AppState.isFullScreenUIEnabled() && frame.areFullScreenToolsShown()) {
            frame.hideFullScreenTools();
          }
          frame.refresh();
        }
      };

  public static final Action SHOW_CONNECTION_INFO =
      new DefaultClientAction() {
        {
          init("action.showConnectionInfo");
        }

        @Override
        public boolean isAvailable() {
          return super.isAvailable() && (MapTool.isPersonalServer() || MapTool.isHostingServer());
        }

        @Override
        protected void executeAction() {

          if (MapTool.getServer() == null) {
            return;
          }

          ConnectionInfoDialog dialog = new ConnectionInfoDialog(MapTool.getServer());
          dialog.setVisible(true);
        }
      };

  public static final Action SHOW_PREFERENCES =
      new DefaultClientAction() {
        {
          init("action.preferences");
        }

        @Override
        protected void executeAction() {

          // Probably don't have to create a new one each time
          PreferencesDialog dialog = new PreferencesDialog();
          dialog.setVisible(true);
        }
      };

  public static final Action SAVE_MESSAGE_HISTORY =
      new DefaultClientAction() {
        {
          init("action.saveMessageHistory");
        }

        @Override
        protected void executeAction() {
          JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
          chooser.setDialogTitle(I18N.getText("msg.title.saveMessageHistory"));
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

          if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
          }
          File saveFile = chooser.getSelectedFile();
          if (!saveFile.getName().contains(".")) {
            saveFile = new File(saveFile.getAbsolutePath() + ".html");
          }
          if (saveFile.exists() && !MapTool.confirm("msg.confirm.fileExists")) {
            return;
          }

          try {
            String messageHistory = MapTool.getFrame().getCommandPanel().getMessageHistory();
            FileUtils.writeByteArrayToFile(saveFile, messageHistory.getBytes());
          } catch (IOException ioe) {
            MapTool.showError(I18N.getString("msg.error.failedSavingMessageHistory"), ioe);
          }
        }
      };

  public static final ClientAction UNDO_PER_MAP =
      new ZoneClientAction() {
        {
          init("action.undoDrawing");
          isAvailable(); // XXX FJE Is this even necessary?
        }

        @Override
        protected void executeAction() {
          Zone z = MapTool.getFrame().getCurrentZoneRenderer().getZone();
          z.undoDrawable();
          isAvailable();
          REDO_PER_MAP
              .isAvailable(); // XXX FJE Calling these forces the update, but won't the framework
          // call them?
        }

        @Override
        public boolean isAvailable() {
          boolean result = false;
          MapToolFrame mtf = MapTool.getFrame();
          if (mtf != null) {
            ZoneRenderer zr = mtf.getCurrentZoneRenderer();
            if (zr != null) {
              Zone z = zr.getZone();
              result = z.canUndo();
            }
          }
          setEnabled(result);
          return isEnabled();
        }
      };

  public static final ClientAction REDO_PER_MAP =
      new ZoneClientAction() {
        {
          init("action.redoDrawing");
          isAvailable(); // XXX Is this even necessary?
        }

        @Override
        protected void executeAction() {
          Zone z = MapTool.getFrame().getCurrentZoneRenderer().getZone();
          z.redoDrawable();
          isAvailable();
          UNDO_PER_MAP.isAvailable();
        }

        @Override
        public boolean isAvailable() {
          boolean result = false;
          MapToolFrame mtf = MapTool.getFrame();
          if (mtf != null) {
            ZoneRenderer zr = mtf.getCurrentZoneRenderer();
            if (zr != null) {
              Zone z = zr.getZone();
              result = z.canRedo();
            }
          }
          setEnabled(result);
          return isEnabled();
        }
      };

  /*
   * public static final DefaultClientAction UNDO_DRAWING = new DefaultClientAction() { { init("action.undoDrawing"); isAvailable(); // XXX FJE Is this even necessary? }
   *
   * @Override public void execute(ActionEvent e) { DrawableUndoManager.getInstance().undo(); isAvailable(); REDO_DRAWING.isAvailable(); // XXX FJE Calling these forces the update, but won't the
   * framework call them? }
   *
   * @Override public boolean isAvailable() { setEnabled(DrawableUndoManager.getInstance().getUndoManager().canUndo()); return isEnabled(); } };
   *
   * public static final DefaultClientAction REDO_DRAWING = new DefaultClientAction() { { init("action.redoDrawing"); isAvailable(); // XXX Is this even necessary? }
   *
   * @Override public void execute(ActionEvent e) { DrawableUndoManager.getInstance().redo(); isAvailable(); UNDO_DRAWING.isAvailable(); }
   *
   * @Override public boolean isAvailable() { setEnabled(DrawableUndoManager.getInstance().getUndoManager().canRedo()); return isEnabled(); } };
   */

  public static final ClientAction CLEAR_DRAWING =
      new ZoneClientAction() {
        {
          init("action.clearDrawing");
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return;
          }
          Zone.Layer layer = renderer.getActiveLayer();
          if (!MapTool.confirm("msg.confirm.clearAllDrawings", layer)) {
            return;
          }
          // LATER: Integrate this with the undo stuff
          // FJE ServerMethodHandler.clearAllDrawings() now empties the DrawableUndoManager as well.
          MapTool.serverCommand().clearAllDrawings(renderer.getZone().getId(), layer);
        }
      };

  public static final ClientAction CUT_TOKENS =
      new ZoneClientAction() {
        {
          init("action.cutTokens");
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          Set<GUID> selectedSet = renderer.getSelectedTokenSet();
          cutTokens(renderer.getZone(), selectedSet);
        }
      };

  /**
   * Cut tokens in the set from the given zone.
   *
   * <p>If no tokens are deleted (because the incoming set is empty, because none of the tokens in
   * the set exist in the zone, or because the user doesn't have permission to delete the tokens)
   * then the {@link MapTool#SND_INVALID_OPERATION} sound is played.
   *
   * <p>If any tokens<i>are</i> deleted, then the selection set for the zone is cleared.
   *
   * @param zone the {@link Zone} the tokens belong to.
   * @param tokenSet a {code Set} containing ght ID's of the tokens to cut.
   */
  public static void cutTokens(Zone zone, Set<GUID> tokenSet) {
    cutOrDeleteTokens(true, zone, tokenSet);
  }

  /**
   * Delete tokens in the set from the given zone.
   *
   * <p>If no tokens are deleted (because the incoming set is empty, because none of the tokens in
   * the set exist in the zone, or because the user doesn't have permission to delete the tokens)
   * then the {@link MapTool#SND_INVALID_OPERATION} sound is played.
   *
   * <p>If any tokens <i>are</i> deleted, then the selection set for the zone is cleared.
   *
   * @param zone the {@link Zone} the tokens belong to.
   * @param tokenSet a {code Set} containing ght ID's of the tokens to cut.
   */
  public static void deleteTokens(Zone zone, Set<GUID> tokenSet) {
    cutOrDeleteTokens(false, zone, tokenSet);
  }

  /**
   * Cut or Delete tokens in the set from the given zone.
   *
   * <p>If no tokens are deleted (because the incoming set is empty, because none of the tokens in
   * the set exist in the zone, or because the user doesn't have permission to delete the tokens)
   * then the {@link MapTool#SND_INVALID_OPERATION} sound is played.
   *
   * <p>If any tokens <i>are</i> deleted, then the selection set for the zone is cleared.
   *
   * @param copy whether the tokens should be copied and deleted (cut) or just deleted
   * @param zone the {@link Zone} the tokens belong to.
   * @param tokenSet a {code Set} containing ght ID's of the tokens to cut.
   */
  public static void cutOrDeleteTokens(Boolean copy, Zone zone, Set<GUID> tokenSet) {
    // Only cut if some tokens are selected. Don't want to accidentally
    // lose what might already be in the clipboard.
    List<GUID> tokensToRemove = new ArrayList<>();
    if (!tokenSet.isEmpty()) {
      if (copy) {
        copyTokens(tokenSet);
      }
      // add tokens to delete to the list
      for (GUID tokenGUID : tokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token != null && AppUtil.playerOwns(token)) {
          tokensToRemove.add(tokenGUID);
        }
      }
    }
    if (!tokensToRemove.isEmpty()) {
      MapTool.serverCommand().removeTokens(zone.getId(), tokensToRemove);
      MapTool.getFrame()
          .getCurrentZoneRenderer()
          .getSelectionModel()
          .replaceSelection(Collections.emptyList());
      if (copy) {
        keepIdsOnPaste = true; // pasted tokens should have same ids as cut ones
      }
    } else {
      MapTool.playSound(MapTool.SND_INVALID_OPERATION);
    }
  }

  public static final ClientAction COPY_TOKENS =
      new ZoneClientAction() {
        {
          init("action.copyTokens");
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          copyTokens(renderer.getSelectedTokenSet());
        }
      };

  /**
   * Copies the given set of tokens to a holding area (not really the "clipboard") so that they can
   * be pasted back in again later. This is the highest level function in that it determines token
   * ownership (only owners can copy/cut tokens).
   *
   * @param tokenSet the set of tokens to copy; if empty, plays the {@link
   *     MapTool#SND_INVALID_OPERATION} sound.
   */
  public static void copyTokens(Set<GUID> tokenSet) {
    List<Token> tokenList = null;
    boolean anythingCopied = false;
    if (!tokenSet.isEmpty()) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      Zone zone = renderer.getZone();
      tokenCopySet = new HashSet<Token>();
      tokenList = new ArrayList<Token>();

      for (GUID guid : tokenSet) {
        Token token = zone.getToken(guid);
        if (token != null && AppUtil.playerOwns(token)) {
          anythingCopied = true;
          tokenList.add(token);
        }
      }
    }
    // Only cut if some tokens are selected. Don't want to accidentally
    // lose what might already be in the clipboard.
    if (anythingCopied) {
      copyTokens(tokenList);
    } else {
      MapTool.playSound(MapTool.SND_INVALID_OPERATION);
    }
  }

  private static Grid gridCopiedFrom = null;

  /**
   * Copies the given set of tokens to a holding area (not really the "clipboard") so that they can
   * be pasted back in again later. This method ignores token ownership and operates on the entire
   * list. A token's (x,y) offset from the first token in the set is preserved so that relative
   * positions are preserved when they are pasted back in later.
   *
   * <p>Here are the criteria for how copy/paste of tokens should work:
   *
   * <ol>
   *   <li><b>Both maps are gridless.</b><br>
   *       This case is very simple since there's no need to convert anything to cell coordinates
   *       and back again.
   *       <ul>
   *         <li>All tokens have their relative pixel offsets saved and reproduced when pasted back
   *             in.
   *       </ul>
   *   <li><b>Both maps have grids.</b><br>
   *       This scheme will preserve proper spacing on the Token layer (for tokens) and on the
   *       Object and Background layers (for stamps). The spacing will NOT be correct when there's a
   *       mix of snapToGrid tokens and non-snapToGrid tokens, but I don't see any way to correct
   *       that. (Well, we could calculate a percentage distance from the token in the extreme
   *       corners of the pasted set and use that percentage to calculate a pixel location. Seems
   *       like a lot of work for not much payoff.)
   *       <ul>
   *         <li>For all tokens that are snapToGrid, the relative distances between tokens should be
   *             kept in "cell" units when copied. That way they can be pasted back in with the
   *             relative cell spacing reproduced.
   *         <li>For all tokens that are not snapToGrid, their relative pixel offsets should be
   *             saved and reproduced when the tokens are pasted.
   *       </ul>
   *   <li><b>The source map is gridless and the destination has a grid.</b><br>
   *       This one is essentially identical to the first case.
   *       <ul>
   *         <li>All tokens are copied with relative pixel offsets. When pasted, those relative
   *             offsets are used for all non-snapToGrid tokens, but snapToGrid tokens have the
   *             relative pixel offsets applied and then are "snapped" into the correct cell
   *             location.
   *       </ul>
   *   <li><b>The source map has a grid and the destination is gridless.</b><br>
   *       This one is essentially identical to the first case.
   *       <ul>
   *         <li>All tokens have their relative pixel distances saved and those offsets are
   *             reproduced when pasted.
   *       </ul>
   * </ol>
   *
   * @param tokenList the list of tokens to copy; if empty, plays the {@link
   *     MapTool#SND_INVALID_OPERATION} sound.
   */
  public static void copyTokens(List<Token> tokenList) {
    // Only cut if some tokens are selected. Don't want to accidentally
    // lose what might already be in the clipboard.
    if (!tokenList.isEmpty()) {
      if (tokenCopySet != null) {
        tokenCopySet.clear(); // Just to help out the garbage collector a little bit
      }

      Token topLeft = tokenList.get(0);
      tokenCopySet = new HashSet<Token>();
      for (Token originalToken : tokenList) {
        if (originalToken.getY() < topLeft.getY() || originalToken.getX() < topLeft.getX()) {
          topLeft = originalToken;
        }
        Token newToken =
            new Token(originalToken, true); // keep same ids. Changed on paste if need be.
        tokenCopySet.add(newToken);
      }
      /*
       * Normalize. For gridless maps, keep relative pixel distances. For gridded maps, keep relative cell spacing. Since we're going to keep relative positions, we can just modify the (x,y)
       * coordinates of all tokens by subtracting the position of the one in 'topLeft'. On paste we can use the saved 'gridCopiedFrom' to determine whether to use pixel distances or convert to
       * cell distances.
       */
      Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      try {
        gridCopiedFrom = (Grid) zone.getGrid().clone();
      } catch (CloneNotSupportedException e) {
        MapTool.showError("This can't happen as all grids MUST implement Cloneable!", e);
      }
      int x = topLeft.getX();
      int y = topLeft.getY();
      for (Token token : tokenCopySet) {
        // Save all token locations as relative pixel offsets. They'll be made absolute when pasting
        // them back in.
        token.setX(token.getX() - x);
        token.setY(token.getY() - y);
      }
      keepIdsOnPaste = false; // if last operation is Copy, don't keep token ids.
    } else {
      MapTool.playSound(MapTool.SND_INVALID_OPERATION);
    }
  }

  public static final ClientAction PASTE_TOKENS =
      new ZoneClientAction() {
        {
          init("action.pasteTokens");
        }

        @Override
        public boolean isAvailable() {
          return super.isAvailable() && tokenCopySet != null;
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return;
          }
          ScreenPoint screenPoint = renderer.getPointUnderMouse();
          if (screenPoint == null) {
            // Pick the middle of the map
            screenPoint = ScreenPoint.fromZonePoint(renderer, renderer.getCenterPoint());
          }
          ZonePoint zonePoint = screenPoint.convertToZone(renderer);
          pasteTokens(zonePoint, renderer.getActiveLayer());
          keepIdsOnPaste = false; // once pasted, subsequent paste should have new ids
          renderer.repaint();
        }
      };

  /**
   * Pastes tokens from {@link #tokenCopySet} into the current zone at the specified location on the
   * given layer. See {@link #copyTokens(List)} for details of how the copy/paste operations work
   * with respect to grid type on the source and destination zones.
   *
   * @param destination ZonePoint specifying where to paste; normally this is unchanged from the
   *     MouseEvent
   * @param layer the Zone.Layer that specifies which layer to paste onto
   */
  private static void pasteTokens(ZonePoint destination, Layer layer) {
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    Grid grid = zone.getGrid();

    boolean snapToGrid = false;
    Token topLeft = null;

    for (Token origToken : tokenCopySet) {
      if (topLeft == null
          || origToken.getY() < topLeft.getY()
          || origToken.getX() < topLeft.getX()) {
        topLeft = origToken;
      }
      snapToGrid |= origToken.isSnapToGrid();
    }
    boolean newZoneSupportsSnapToGrid = grid.getCapabilities().isSnapToGridSupported();
    boolean gridCopiedFromSupportsSnapToGrid =
        gridCopiedFrom.getCapabilities().isSnapToGridSupported();
    if (snapToGrid && newZoneSupportsSnapToGrid) {
      CellPoint cellPoint = grid.convert(destination);
      destination = grid.convert(cellPoint);
    }
    // Create a set of all tokenExposedAreaGUID's to make searching by GUID much faster.
    Set<GUID> allTokensSet = null;
    {
      List<Token> allTokensList = zone.getAllTokens();
      if (!allTokensList.isEmpty()) {
        allTokensSet = new HashSet<GUID>(allTokensList.size());
        for (Token token : allTokensList) {
          allTokensSet.add(token.getExposedAreaGUID());
        }
      }
    }
    List<Token> tokenList = new ArrayList<Token>(tokenCopySet);
    tokenList.sort(Token.COMPARE_BY_ZORDER);
    List<String> failedPaste = new ArrayList<String>(tokenList.size());

    for (Token origToken : tokenList) {
      Token token = new Token(origToken, keepIdsOnPaste); // keep id if first paste since cut

      // need this here to get around times when a token is copied and pasted into the
      // same zone, such as a framework "template"
      if (allTokensSet != null && allTokensSet.contains(token.getExposedAreaGUID())) {
        GUID guid = new GUID();
        token.setExposedAreaGUID(guid);
        ExposedAreaMetaData meta = zone.getExposedAreaMetaData(guid);
        // 'meta' references the object already stored in the zone's HashMap (it was created if
        // necessary).
        meta.addToExposedAreaHistory(meta.getExposedAreaHistory());
        MapTool.serverCommand()
            .updateExposedAreaMeta(zone.getId(), token.getExposedAreaGUID(), meta);
      }

      ZonePoint tokenOffset;
      if (newZoneSupportsSnapToGrid && gridCopiedFromSupportsSnapToGrid && token.isSnapToGrid()) {
        // Convert (x,y) offset to a cell offset using the grid from the zone where the tokens were
        // copied from. Note that the token coordinates are relative to the "top-left" token's
        // position, so they can't be used as a ZonePoint without first adding back the grid offset.
        CellPoint cp =
            gridCopiedFrom.convert(
                new ZonePoint(
                    token.getX() + gridCopiedFrom.getOffsetX(),
                    token.getY() + gridCopiedFrom.getOffsetY()));
        ZonePoint zp = grid.convert(cp);
        tokenOffset = new ZonePoint(zp.x - grid.getOffsetX(), zp.y - grid.getOffsetY());
      } else {
        // For gridless sources, gridless destinations, or tokens that are not SnapToGrid: just use
        // the pixel offsets
        tokenOffset = new ZonePoint(token.getX(), token.getY());
      }
      token.setX(tokenOffset.x + destination.x);
      token.setY(tokenOffset.y + destination.y);

      // paste into correct layer
      token.setLayer(layer);

      // check the token's name and change it, if necessary
      // XXX Merge this with the drag/drop code in ZoneRenderer.addTokens().
      boolean tokenNeedsNewName = false;
      if (MapTool.getPlayer().isGM()) {
        // For GMs, only change the name of NPCs. It's possible that we should be changing the name
        // of PCs as well
        // since macros don't work properly when multiple tokens have the same name, but if we
        // changed it without
        // asking it could be seriously confusing. Yet we don't want to popup a confirmation every
        // time the GM pastes either. :(
        tokenNeedsNewName = token.getType() != Token.Type.PC;
      } else {
        // For Players, check to see if the name is already in use. If it is already in use, make
        // sure the current Player
        // owns the token being duplicated (to avoid subtle ways of manipulating someone else's
        // token!).
        Token tokenNameUsed = zone.getTokenByName(token.getName());
        if (tokenNameUsed != null) {
          if (!AppUtil.playerOwns(tokenNameUsed)) {
            failedPaste.add(token.getName());
            continue;
          }
          tokenNeedsNewName = true;
        }
      }
      if (tokenNeedsNewName) {
        String newName = MapToolUtil.nextTokenId(zone, token, true);
        token.setName(newName);
      }
      MapTool.serverCommand().putToken(zone.getId(), token);
    }
    if (!failedPaste.isEmpty()) {
      String mesg = I18N.getText("Token.error.unableToPaste", failedPaste);
      TextMessage msg = TextMessage.gmMe(null, mesg);
      MapTool.addMessage(msg);
    }
  }

  public static final Action REMOVE_ASSET_ROOT =
      new DefaultClientAction() {
        {
          init("action.removeAssetRoot");
        }

        @Override
        protected void executeAction() {
          AssetPanel assetPanel = MapTool.getFrame().getAssetPanel();
          Directory dir = assetPanel.getSelectedAssetRoot();

          if (dir == null) {
            MapTool.showError("msg.error.mustSelectAssetGroupFirst");
            return;
          }
          if (!assetPanel.isAssetRoot(dir)) {
            MapTool.showError("msg.error.mustSelectRootGroup");
            return;
          }
          AppPreferences.removeAssetRoot(dir.getPath());
          assetPanel.removeAssetRoot(dir);
        }
      };

  // Jamz: Force a directory to rescan
  public static final Action RESCAN_NODE =
      new DefaultClientAction() {
        {
          init("action.rescanNode");
        }

        @Override
        protected void executeAction() {
          AssetPanel assetPanel = MapTool.getFrame().getAssetPanel();
          Directory dir = assetPanel.getSelectedAssetRoot();

          if (dir == null) {
            MapTool.showError("msg.error.mustSelectAssetGroupFirst");
            return;
          }

          assetPanel.rescanImagePanelDir(dir);
        }
      };

  public static final Action WHISPER_PLAYER =
      new DefaultClientAction() {
        {
          init("whisper.command");
        }

        @Override
        protected void executeAction() {
          ClientConnectionPanel panel = MapTool.getFrame().getConnectionPanel();
          Player selectedPlayer = panel.getSelectedPlayer();

          if (selectedPlayer == null) {
            MapTool.showError("msg.error.mustSelectPlayerFirst");
            return;
          }
          try {
            JTextPane chatBox = MapTool.getFrame().getCommandPanel().getCommandTextArea();
            String enterText = I18N.getText("whisper.enterText");
            chatBox.replaceSelection(
                String.format("[w(\"%s\"): \"%s\"]", selectedPlayer.getName(), enterText));
            String chatBoxText =
                chatBox.getDocument().getText(0, chatBox.getDocument().getLength());
            int start = chatBoxText.indexOf(enterText);
            chatBox.select(start, start + enterText.length());
            chatBox.requestFocusInWindow();
          } catch (BadLocationException e1) {
            // e1.printStackTrace();
          }
        }
      };

  public static final Action BOOT_CONNECTED_PLAYER =
      new DefaultClientAction() {
        {
          init("action.bootConnectedPlayer");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
        }

        @Override
        protected void executeAction() {
          ClientConnectionPanel panel = MapTool.getFrame().getConnectionPanel();
          Player selectedPlayer = panel.getSelectedPlayer();

          if (selectedPlayer == null) {
            MapTool.showError("msg.error.mustSelectPlayerFirst");
            return;
          }
          if (MapTool.getPlayer().equals(selectedPlayer)) {
            MapTool.showError("msg.error.cantBootSelf");
            return;
          }
          if (MapTool.isPlayerConnected(selectedPlayer.getName())) {
            String msg = I18N.getText("msg.confirm.bootPlayer", selectedPlayer.getName());
            if (MapTool.confirm(msg)) {
              MapTool.serverCommand().bootPlayer(selectedPlayer.getName());
              msg = I18N.getText("msg.info.playerBooted", selectedPlayer.getName());
              MapTool.showInformation(msg);
              return;
            }
          }
          MapTool.showError("msg.error.failedToBoot");
        }
      };

  /**
   * This is the menu item that lets the GM override the typing notification toggle on the clients
   */
  public static final Action TOGGLE_ENFORCE_NOTIFICATION =
      new AdminClientAction() {
        {
          init("action.enforceNotification");
        }

        @Override
        public boolean isSelected() {
          return AppState.isNotificationEnforced();
        }

        @Override
        protected void executeAction() {
          AppState.setNotificationEnforced(!AppState.isNotificationEnforced());
          MapTool.serverCommand().enforceNotification(AppState.isNotificationEnforced());
        }
      };

  /** This is the menu option that forces the player view to continuously track the GM view. */
  public static final Action TOGGLE_LINK_PLAYER_VIEW =
      new AdminClientAction() {
        {
          init("action.linkPlayerView");
        }

        @Override
        public boolean isSelected() {
          return AppState.isPlayerViewLinked();
        }

        @Override
        protected void executeAction() {

          AppState.setPlayerViewLinked(!AppState.isPlayerViewLinked());
          MapTool.getFrame().getCurrentZoneRenderer().maybeForcePlayersView();
        }
      };

  public static final Action TOGGLE_SHOW_PLAYER_VIEW =
      new AdminClientAction() {
        {
          init("action.showPlayerView");
        }

        @Override
        public boolean isSelected() {
          return AppState.isShowAsPlayer();
        }

        @Override
        protected void executeAction() {

          AppState.setShowAsPlayer(!AppState.isShowAsPlayer());
          MapTool.getFrame().refresh();
        }
      };

  public static final Action TOGGLE_SHOW_LIGHT_SOURCES =
      new AdminClientAction() {
        {
          init("action.showLightSources");
        }

        @Override
        public boolean isSelected() {
          return AppState.isShowLightSources();
        }

        @Override
        protected void executeAction() {

          AppState.setShowLightSources(!AppState.isShowLightSources());
          MapTool.getFrame().refresh();
        }
      };

  public static final Action TOGGLE_COLLECT_PROFILING_DATA =
      new DefaultClientAction() {
        {
          init("action.collectPerformanceData");
        }

        @Override
        public boolean isSelected() {
          return AppState.isCollectProfilingData();
        }

        @Override
        protected void executeAction() {
          AppState.setCollectProfilingData(!AppState.isCollectProfilingData());
          MapTool.getProfilingNoteFrame().setVisible(AppState.isCollectProfilingData());
        }
      };

  public static final Action TOGGLE_LOG_CONSOLE =
      new DefaultClientAction() {
        {
          init("action.openLogConsole");
        }

        @Override
        public boolean isSelected() {
          return AppState.isLoggingToConsole();
        }

        @Override
        protected void executeAction() {
          AppState.setLoggingToConsole(!AppState.isLoggingToConsole());
          MapTool.getLogConsoleNoteFrame().setVisible(AppState.isLoggingToConsole());
        }
      };
  public static final Action TOGGLE_SHOW_TEXT_LABELS =
      new DefaultClientAction() {
        {
          init("action.showTextLabels");
        }

        @Override
        public boolean isSelected() {
          return AppState.getShowTextLabels();
        }

        @Override
        protected void executeAction() {

          AppState.setShowTextLabels(!AppState.getShowTextLabels());
          if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
            MapTool.getFrame().getCurrentZoneRenderer().repaint();
          }
        }
      };

  public static final Action TOGGLE_SHOW_MOVEMENT_MEASUREMENTS =
      new DefaultClientAction() {
        {
          init("action.showMovementMeasures");
        }

        @Override
        public boolean isSelected() {
          return AppState.getShowMovementMeasurements();
        }

        @Override
        protected void executeAction() {

          AppState.setShowMovementMeasurements(!AppState.getShowMovementMeasurements());
          if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
            MapTool.getFrame().getCurrentZoneRenderer().repaint();
          }
        }
      };

  public static final Action COPY_ZONE =
      new ZoneAdminClientAction() {
        {
          init("action.copyZone");
        }

        @Override
        protected void executeAction() {
          Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
          // XXX Perhaps ask the user if the copied map should have its GEA and/or TEA cleared? An
          // imported map would ask...
          String zoneName =
              JOptionPane.showInputDialog(
                  I18N.getText("dialog.copyZone.msg"),
                  I18N.getText("dialog.copyZone.initial", zone.getName()));
          if (zoneName != null) {
            Zone zoneCopy = new Zone(zone);
            zoneCopy.setName(zoneName);
            MapTool.addZone(zoneCopy);
          }
        }
      };

  public static final Action REMOVE_ZONE =
      new ZoneAdminClientAction() {
        {
          init("action.removeZone");
        }

        @Override
        protected void executeAction() {
          if (!MapTool.confirm("msg.confirm.removeZone")) {
            return;
          }
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          MapTool.removeZone(renderer.getZone());
        }
      };

  public static final Action SHOW_ABOUT =
      new DefaultClientAction() {
        {
          init("action.showAboutDialog");
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame().showAboutDialog();
        }
      };

  /** This is the menu option that warps all clients views to the current GM's view. */
  public static final Action ENFORCE_ZONE_VIEW =
      new ZoneAdminClientAction() {
        {
          init("action.enforceView");
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return;
          }
          renderer.forcePlayersView();
        }
      };

  /** This is the menu option turns the lumens overlay on and off. */
  public static final Action TOGGLE_LUMENS_OVERLAY =
      new ZoneClientAction() {
        {
          init("action.showLumensOverlay");
        }

        @Override
        public boolean isSelected() {
          return AppState.isShowLumensOverlay();
        }

        @Override
        protected void executeAction() {
          AppState.setShowLumensOverlay(!AppState.isShowLumensOverlay());
          MapTool.getFrame().refresh();
        }
      };

  /** This is the menu option turns the lumens overlay on and off. */
  public static final Action TOGGLE_SHOW_LIGHTS =
      new ZoneClientAction() {
        {
          init("action.showLights");
        }

        @Override
        public boolean isSelected() {
          return AppState.isShowLights();
        }

        @Override
        protected void executeAction() {
          AppState.setShowLights(!AppState.isShowLights());
          MapTool.getFrame().refresh();
        }
      };

  /** Start entering text into the chat field */
  public static final String CHAT_COMMAND_ID = "action.sendChat";

  public static final Action CHAT_COMMAND =
      new DefaultClientAction() {
        {
          init(CHAT_COMMAND_ID);
        }

        @Override
        protected void executeAction() {
          if (!MapTool.getFrame().isCommandPanelVisible()) {
            MapTool.getFrame().showCommandPanel();
            MapTool.getFrame().getCommandPanel().startChat();
          } else {
            MapTool.getFrame().hideCommandPanel();
          }
        }
      };

  public static final String COMMAND_UP_ID = "action.commandUp";

  public static final String COMMAND_DOWN_ID = "action.commandDown";

  /** Start entering text into the chat field */
  public static final String ENTER_COMMAND_ID = "action.runMacro";

  public static final Action ENTER_COMMAND =
      new DefaultClientAction() {
        {
          init(ENTER_COMMAND_ID, false);
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame().getCommandPanel().startMacro();
        }
      };

  /** Action tied to the chat field to commit the command. */
  public static final String COMMIT_COMMAND_ID = "action.commitCommand";

  public static final Action COMMIT_COMMAND =
      new DefaultClientAction() {
        {
          init(COMMIT_COMMAND_ID);
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame().getCommandPanel().commitCommand();
        }
      };

  /** Action tied to the chat field to commit the command. */
  public static final String CANCEL_COMMAND_ID = "action.cancelCommand";

  public static final Action CANCEL_COMMAND =
      new DefaultClientAction() {
        {
          init(CANCEL_COMMAND_ID);
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame().getCommandPanel().cancelCommand();
        }
      };

  /** Action to insert a newline into the chat input field */
  public static final String NEWLINE_COMMAND_ID = "action.newlineCommand";

  public static final Action NEWLINE_COMMAND =
      new DefaultClientAction() {
        {
          init(NEWLINE_COMMAND_ID);
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame().getCommandPanel().insertNewline();
        }
      };

  public static final Action ADJUST_GRID =
      new ZoneAdminClientAction() {
        {
          init("action.adjustGrid");
        }

        @Override
        protected void executeAction() {

          MapTool.getFrame().getToolbox().setSelectedTool(GridTool.class);
        }
      };

  public static final Action ADJUST_BOARD =
      new ZoneAdminClientAction() {
        {
          init("action.adjustBoard");
        }

        @Override
        protected void executeAction() {

          if (MapTool.getFrame().getCurrentZoneRenderer().getZone().getMapAssetId() != null) {
            MapTool.getFrame().getToolbox().setSelectedTool(BoardTool.class);
          } else {
            MapTool.showInformation(I18N.getText("action.error.noMapBoard"));
          }
        }
      };

  private static TransferProgressDialog transferProgressDialog;
  public static final Action SHOW_TRANSFER_WINDOW =
      new DefaultClientAction() {
        {
          init("msg.info.showTransferWindow");
        }

        @Override
        protected void executeAction() {

          if (transferProgressDialog == null) {
            transferProgressDialog = new TransferProgressDialog();
          }

          if (transferProgressDialog.isShowing()) {
            return;
          }

          transferProgressDialog.showDialog();
        }
      };

  public static final Action TOGGLE_GRID =
      new DefaultClientAction() {
        {
          init("action.showGrid");
          putValue(Action.SMALL_ICON, RessourceManager.getSmallIcon(Icons.MENU_SHOW_GRIDS));
        }

        @Override
        public boolean isSelected() {
          return AppState.isShowGrid();
        }

        @Override
        protected void executeAction() {
          AppState.setShowGrid(!AppState.isShowGrid());
          if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
            MapTool.getFrame().getCurrentZoneRenderer().repaint();
          }
        }
      };

  public static final Action TOGGLE_COORDINATES =
      new DefaultClientAction() {
        {
          init("action.showCoordinates");
        }

        @Override
        public boolean isAvailable() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          return renderer != null
              && renderer.getZone().getGrid().getCapabilities().isCoordinatesSupported();
        }

        @Override
        public boolean isSelected() {
          return AppState.isShowCoordinates();
        }

        @Override
        protected void executeAction() {

          AppState.setShowCoordinates(!AppState.isShowCoordinates());

          MapTool.getFrame().getCurrentZoneRenderer().repaint();
        }
      };

  public static final Action TOGGLE_ZOOM_LOCK =
      new DefaultClientAction() {
        {
          init("action.zoomLock");
          putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        }

        @Override
        public boolean isAvailable() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          return renderer != null;
        }

        @Override
        public boolean isSelected() {
          return AppState.isZoomLocked();
        }

        @Override
        protected void executeAction() {
          AppState.setZoomLocked(!AppState.isZoomLocked());
          MapTool.getFrame().getZoomStatusBar().update(); // So the textfield becomes grayed out
        }
      };

  public static final Action TOGGLE_FOG =
      new ZoneAdminClientAction() {
        {
          init("action.enableFogOfWar");
        }

        @Override
        public boolean isSelected() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return false;
          }

          return renderer.getZone().hasFog();
        }

        @Override
        protected void executeAction() {

          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return;
          }

          Zone zone = renderer.getZone();
          zone.setHasFog(!zone.hasFog());

          MapTool.serverCommand().setZoneHasFoW(zone.getId(), zone.hasFog());

          renderer.repaint();
        }
      };

  // Lee: this sets the revealing of FoW only at waypoints.
  public static final Action TOGGLE_WAYPOINT_FOG_REVEAL =
      new ZoneAdminClientAction() {
        {
          init("action.revealFogAtWaypoints");
        }

        @Override
        public boolean isAvailable() {
          return ((ZoneAdminClientAction) TOGGLE_FOG).isSelected();
        }

        @Override
        public boolean isSelected() {
          if (isAvailable())
            return MapTool.getFrame()
                .getCurrentZoneRenderer()
                .getZone()
                .getWaypointExposureToggle();
          return false;
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame()
              .getCurrentZoneRenderer()
              .getZone()
              .setWaypointExposureToggle(!this.isSelected());
        }
      };

  public static final Action RESTORE_FOG =
      new ZoneAdminClientAction() {
        {
          init("action.restoreFogOfWar");
        }

        @Override
        protected void executeAction() {
          if (!MapTool.confirm("msg.confirm.restoreFoW")) {
            return;
          }

          FogUtil.restoreFoW(MapTool.getFrame().getCurrentZoneRenderer());
        }
      };

  public static class SetVisionType extends ZoneAdminClientAction {
    private final VisionType visionType;

    public SetVisionType(VisionType visionType) {
      this.visionType = visionType;
      init("visionType." + visionType.name());
    }

    @Override
    public boolean isSelected() {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      if (renderer == null) {
        return false;
      }

      return renderer.getZone().getVisionType() == visionType;
    }

    @Override
    protected void executeAction() {

      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      if (renderer == null) {
        return;
      }

      Zone zone = renderer.getZone();

      if (zone.getVisionType() != visionType) {

        zone.setVisionType(visionType);

        MapTool.serverCommand().setVisionType(zone.getId(), visionType);

        renderer.flushFog();
        renderer.flushLight();
        renderer.repaint();
      }
    }
  }

  public static final Action TOGGLE_SHOW_TOKEN_NAMES =
      new DefaultClientAction() {
        {
          init("action.showNames");
          putValue(Action.SMALL_ICON, RessourceManager.getSmallIcon(Icons.MENU_SHOW_TOKEN_NAMES));
        }

        @Override
        protected void executeAction() {

          AppState.setShowTokenNames(!AppState.isShowTokenNames());
          if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
            MapTool.getFrame().getCurrentZoneRenderer().repaint();
          }
        }
      };

  public static final Action TOGGLE_CURRENT_ZONE_VISIBILITY =
      new ZoneAdminClientAction() {

        {
          init("action.hideMap");
        }

        @Override
        public boolean isSelected() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return false;
          }
          return renderer.getZone().isVisible();
        }

        @Override
        protected void executeAction() {

          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer == null) {
            return;
          }

          // TODO: consolidate this code with ZonePopupMenu
          Zone zone = renderer.getZone();
          zone.setVisible(!zone.isVisible());

          MapTool.serverCommand().setZoneVisibility(zone.getId(), zone.isVisible());
          MapTool.getFrame().getZoneMiniMapPanel().flush();
          MapTool.getFrame().repaint();
        }
      };

  public static final Action NEW_CAMPAIGN =
      new AdminClientAction() {
        {
          init("action.newCampaign");
        }

        /**
         * Displays a modal dialog asking Yes/No whether a new campaign should be started; this is
         * here because MapTool doesn't have a confirmImpl() that allows the default button to be
         * selected via a parameter.
         *
         * @return true if the select button is Yes, false for anything else
         */
        private boolean confirmNewCampaign() {
          String msg = I18N.getText("msg.confirm.newCampaign");
          log.debug(msg);
          Object[] options = {
            I18N.getText("msg.title.messageDialog.yes"), I18N.getText("msg.title.messageDialog.no")
          };
          String title = I18N.getText("msg.title.messageDialogConfirm");
          int val =
              JOptionPane.showOptionDialog(
                  MapTool.getFrame(),
                  msg,
                  title,
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE,
                  null,
                  options,
                  options[1]);

          return val == JOptionPane.YES_OPTION;
        }

        @Override
        protected void executeAction() {

          if (!confirmNewCampaign()) {
            return;
          }

          new CampaignManager().clearCampaignData();

          Campaign campaign = CampaignFactory.createBasicCampaign();
          AppState.setCampaignFile(null);
          MapTool.setCampaign(campaign);
          MapTool.serverCommand().setCampaign(campaign);

          ImageManager.flush();
          MapTool.getFrame()
              .setCurrentZoneRenderer(
                  MapTool.getFrame().getZoneRenderer(campaign.getZones().get(0)));
        }
      };

  /**
   * Note that the ZOOM actions are defined as DefaultClientAction types. This allows the {@link
   * ClientAction#getKeyStroke()} method to be invoked where otherwise it couldn't be.
   *
   * <p>(Well, it <i>could be</i> if we cast this object to the right type everywhere else but
   * that's just tedious. And what is tedious is error-prone. :))
   */
  public static final DefaultClientAction ZOOM_IN =
      new DefaultClientAction() {
        {
          init("action.zoomIn", false);
        }

        @Override
        public boolean isAvailable() {
          return !AppState.isZoomLocked();
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer != null) {
            Dimension size = renderer.getSize();
            renderer.zoomIn(size.width / 2, size.height / 2);
            renderer.maybeForcePlayersView();
          }
        }
      };

  public static final DefaultClientAction ZOOM_OUT =
      new DefaultClientAction() {
        {
          init("action.zoomOut", false);
        }

        @Override
        public boolean isAvailable() {
          return !AppState.isZoomLocked();
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer != null) {
            Dimension size = renderer.getSize();
            renderer.zoomOut(size.width / 2, size.height / 2);
            renderer.maybeForcePlayersView();
          }
        }
      };

  public static final DefaultClientAction ZOOM_RESET =
      new DefaultClientAction() {
        private Double lastZoom;

        {
          init("action.zoom100", false);
        }

        @Override
        public boolean isAvailable() {
          return !AppState.isZoomLocked();
        }

        @Override
        protected void executeAction() {
          ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (renderer != null) {
            // Revert to last zoom if we have one, but don't if the user has manually
            // changed the scale since the last reset zoom (one to one index)
            if (lastZoom != null
                && renderer.getScale() == renderer.getZoneScale().getOneToOneScale()) {
              // Go back to the previous zoom
              renderer.setScale(lastZoom);

              // But make sure the next time we'll go back to 1:1
              lastZoom = null;
            } else {
              lastZoom = renderer.getScale();
              renderer.zoomReset(renderer.getWidth() / 2, renderer.getHeight() / 2);
            }
            renderer.maybeForcePlayersView();
          }
        }
      };

  public static final Action TOGGLE_ZONE_SELECTOR =
      new DefaultClientAction() {
        {
          init("action.showMapSelector");
        }

        @Override
        public boolean isSelected() {
          return MapTool.getFrame().getZoneMiniMapPanel().isVisible();
        }

        @Override
        protected void executeAction() {
          JComponent panel = MapTool.getFrame().getZoneMiniMapPanel();
          panel.setVisible(!panel.isVisible());
        }
      };

  public static final Action TOGGLE_MOVEMENT_LOCK =
      new AdminClientAction() {
        {
          init("action.toggleMovementLock");
        }

        @Override
        public boolean isSelected() {
          return MapTool.getServerPolicy().isMovementLocked();
        }

        @Override
        protected void executeAction() {

          ServerPolicy policy = MapTool.getServerPolicy();
          policy.setIsMovementLocked(!policy.isMovementLocked());

          MapTool.updateServerPolicy(policy);
        }
      };

  /** Toggle to enable / disable player use of the token editor. */
  public static final Action TOGGLE_TOKEN_EDITOR_LOCK =
      new AdminClientAction() {
        {
          init("action.toggleTokenEditorLock");
        }

        @Override
        public boolean isSelected() {
          return MapTool.getServerPolicy().isTokenEditorLocked();
        }

        @Override
        protected void executeAction() {

          ServerPolicy policy = MapTool.getServerPolicy();
          policy.setIsTokenEditorLocked(!policy.isTokenEditorLocked());

          MapTool.updateServerPolicy(policy);
        }
      };

  public static final Action START_SERVER =
      new ClientAction() {
        {
          init("action.serverStart");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isPersonalServer();
        }

        @Override
        protected void executeAction() {
          runBackground(
              () -> {
                if (!MapTool.isPersonalServer()) {
                  MapTool.showError("msg.error.alreadyRunningServer");
                  return;
                }

                // TODO: Need to shut down the existing server first;
                StartServerDialog dialog = new StartServerDialog();
                dialog.showDialog();

                if (!dialog.accepted()) // Results stored in Preferences.userRoot()
                return;

                StartServerDialogPreferences serverProps =
                    new StartServerDialogPreferences(); // data retrieved from
                // Preferences.userRoot()
                if (serverProps.getPort() == 0 || serverProps.getPort() > 65535) {
                  MapTool.showError("ServerDialog.error.port.outOfRange");
                  return;
                }

                ServerPolicy policy = new ServerPolicy();
                policy.setAutoRevealOnMovement(serverProps.isAutoRevealOnMovement());
                policy.setUseStrictTokenManagement(serverProps.getUseStrictTokenOwnership());
                policy.setGmRevealsVisionForUnownedTokens(
                    serverProps.getGmRevealsVisionForUnownedTokens());
                policy.setPlayersCanRevealVision(serverProps.getPlayersCanRevealVision());
                policy.setUseIndividualViews(serverProps.getUseIndividualViews());
                policy.setPlayersReceiveCampaignMacros(
                    serverProps.getPlayersReceiveCampaignMacros());
                policy.setHiddenMapSelectUI(serverProps.getMapSelectUIHidden());
                policy.setIsTokenEditorLocked(serverProps.getLockTokenEditOnStart());
                policy.setIsMovementLocked(serverProps.getLockPlayerMovementOnStart());
                policy.setDisablePlayerAssetPanel(serverProps.getPlayerLibraryLock());

                // Tool Tips for unformatted inline rolls.
                policy.setUseToolTipsForDefaultRollFormat(
                    serverProps.getUseToolTipsForUnformattedRolls());

                // my addition
                // Note: Restricted impersonation setting is the opposite of its label
                // (Unrestricted when checked and restricted when unchecked)
                policy.setRestrictedImpersonation(!serverProps.getRestrictedImpersonation());
                policy.setMovementMetric(serverProps.getMovementMetric());
                boolean useIF =
                    serverProps.getUseIndividualViews() && serverProps.getUseIndividualFOW();
                policy.setUseIndividualFOW(useIF);

                String gmPassword;
                String playerPassword;

                if (!serverProps.getUsePasswordFile()) {
                  gmPassword = serverProps.getGMPassword();
                  playerPassword = serverProps.getPlayerPassword();
                } else {
                  gmPassword = new PasswordGenerator().getPassword();
                  playerPassword = new PasswordGenerator().getPassword();
                }

                ServerConfig config =
                    new ServerConfig(
                        serverProps.getUsername(),
                        gmPassword,
                        playerPassword,
                        serverProps.getPort(),
                        serverProps.getRPToolsName(),
                        "localhost",
                        serverProps.getUseEasyConnect(),
                        serverProps.getUseWebRtc());

                // Use the existing campaign
                Campaign campaign = MapTool.getCampaign();

                boolean failed = false;
                try {
                  ServerDisconnectHandler.disconnectExpected = true;
                  MapTool.stopServer();

                  // Use UPnP to open port in router
                  if (serverProps.getUseUPnP()) {
                    UPnPUtil.openPort(serverProps.getPort());
                  }
                  // Right now set this is set to whatever the last server settings were. If we
                  // wanted to turn it on and
                  // leave it turned on, the line would change to:
                  // campaign.setHasUsedFogToolbar(useIF || campaign.hasUsedFogToolbar());
                  campaign.setHasUsedFogToolbar(useIF);

                  PlayerDatabaseFactory.setServerConfig(config);
                  if (serverProps.getUsePasswordFile()) {
                    PlayerDatabaseFactory.setCurrentPlayerDatabase(
                        PlayerDatabaseType.PASSWORD_FILE);
                    PasswordFilePlayerDatabase db =
                        (PasswordFilePlayerDatabase)
                            PlayerDatabaseFactory.getCurrentPlayerDatabase();
                    db.initialize();
                    if (serverProps.getRole() == Role.GM) {
                      db.addTemporaryPlayer(
                          dialog.getUsernameTextField().getText(), Role.GM, gmPassword);
                    } else {
                      db.addTemporaryPlayer(
                          dialog.getUsernameTextField().getText(), Role.PLAYER, playerPassword);
                    }
                  } else {
                    PlayerDatabaseFactory.setCurrentPlayerDatabase(PlayerDatabaseType.DEFAULT);
                  }
                  PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
                  // Make a copy of the campaign since we don't coordinate local changes well ...
                  // yet

                  /*
                   * JFJ 2010-10-27 The below creates a NEW campaign with a copy of the existing campaign. However, this is NOT a full copy. In the constructor called below, each zone from the
                   * previous campaign(ie, the one passed in) is recreated. This means that only some items for that campaign, zone(s), and token's are copied over when you start a new server
                   * instance.
                   *
                   * You need to modify either Campaign(Campaign) or Zone(Zone) to get any data you need to persist from the pre-server campaign to the post server start up campaign.
                   */
                  MapTool.startServer(
                      dialog.getUsernameTextField().getText(),
                      config,
                      policy,
                      campaign,
                      playerDatabase,
                      true);

                  // Connect to server
                  Player.Role playerType = (Player.Role) dialog.getRoleCombo().getSelectedItem();
                  Runnable onConnected =
                      () -> {
                        // connecting
                        MapTool.getFrame()
                            .getConnectionStatusPanel()
                            .setStatus(ConnectionStatusPanel.Status.server);
                        MapTool.addLocalMessage(
                            MessageUtil.getFormattedSystemMsg(
                                I18N.getText("msg.info.startServer")));
                      };

                  if (playerType == Player.Role.GM) {
                    MapTool.createConnection(
                        config,
                        new LocalPlayer(
                            dialog.getUsernameTextField().getText(), playerType, gmPassword),
                        onConnected);
                  } else {
                    MapTool.createConnection(
                        config,
                        new LocalPlayer(
                            dialog.getUsernameTextField().getText(), playerType, playerPassword),
                        onConnected);
                  }
                } catch (UnknownHostException uh) {
                  MapTool.showError("msg.error.invalidLocalhost", uh);
                  failed = true;
                } catch (IOException ioe) {
                  MapTool.showError("msg.error.failedConnect", ioe);
                  failed = true;
                } catch (NoSuchAlgorithmException
                    | InvalidAlgorithmParameterException
                    | InvalidKeySpecException
                    | NoSuchPaddingException
                    | InvalidKeyException
                    | ExecutionException
                    | InterruptedException e) {
                  MapTool.showError("msg.error.initializeCrypto", e);
                  failed = true;
                } catch (PasswordDatabaseException pwde) {
                  MapTool.showError(pwde.getMessage());
                  failed = true;
                }

                if (failed) {
                  try {
                    MapTool.startPersonalServer(campaign);
                  } catch (IOException
                      | NoSuchAlgorithmException
                      | InvalidKeySpecException
                      | ExecutionException
                      | InterruptedException e) {
                    MapTool.showError("msg.error.failedStartPersonalServer", e);
                  }
                }
              });
        }
      };

  public static final Action CONNECT_TO_SERVER =
      new ClientAction() {
        {
          init("action.clientConnect");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isPersonalServer();
        }

        @Override
        protected void executeAction() {
          if (MapTool.isCampaignDirty() && !MapTool.confirm("msg.confirm.loseChanges")) return;

          final ConnectToServerDialog dialog = new ConnectToServerDialog();
          dialog.showDialog();
          if (!dialog.accepted()) {
            return;
          }

          ServerDisconnectHandler.disconnectExpected = true;
          LOAD_MAP.setSeenWarning(false);
          MapTool.stopServer();

          // Install a temporary gimped campaign until we get the one from the
          // server
          final Campaign oldCampaign = MapTool.getCampaign();
          MapTool.setCampaign(new Campaign());

          // connecting
          MapTool.getFrame()
              .getConnectionStatusPanel()
              .setStatus(ConnectionStatusPanel.Status.connected);

          // Show the user something interesting until we've got the campaign
          // Look in ClientMethodHandler.setCampaign() for the corresponding
          // hideGlassPane
          StaticMessageDialog progressDialog =
              new StaticMessageDialog(I18N.getText("msg.info.connecting"));
          MapTool.getFrame().showFilledGlassPane(progressDialog);

          runBackground(
              () -> {
                boolean failed = false;
                try {
                  ConnectToServerDialogPreferences prefs = new ConnectToServerDialogPreferences();
                  ServerConfig config =
                      new ServerConfig(
                          prefs.getUsername(),
                          "",
                          "",
                          dialog.getPort(),
                          prefs.getServerName(),
                          dialog.getServer(),
                          dialog.getUseWebRTC());

                  String password =
                      prefs.getUsePublicKey()
                          ? new PasswordGenerator().getPassword()
                          : prefs.getPassword();
                  MapTool.createConnection(
                      config,
                      new LocalPlayer(prefs.getUsername(), prefs.getRole(), password),
                      () -> {
                        MapTool.getFrame().hideGlassPane();
                        MapTool.getFrame()
                            .showFilledGlassPane(
                                new StaticMessageDialog(I18N.getText("msg.info.campaignLoading")));
                      });

                } catch (UnknownHostException e1) {
                  MapTool.showError("msg.error.unknownHost", e1);
                  failed = true;
                } catch (IOException e1) {
                  MapTool.showError("msg.error.failedLoadCampaign", e1);
                  failed = true;
                } catch (NoSuchAlgorithmException
                    | InvalidKeySpecException
                    | ExecutionException
                    | InterruptedException e1) {
                  MapTool.showError("msg.error.initializeCrypto", e1);
                  failed = true;
                }
                if (failed) {
                  MapTool.getFrame().hideGlassPane();
                  try {
                    MapTool.startPersonalServer(oldCampaign);
                  } catch (IOException
                      | NoSuchAlgorithmException
                      | InvalidKeySpecException
                      | ExecutionException
                      | InterruptedException e) {
                    MapTool.showError("msg.error.failedStartPersonalServer", e);
                  }
                }
              });
        }
      };

  public static final Action DISCONNECT_FROM_SERVER =
      new ClientAction() {
        {
          init("action.clientDisconnect");
        }

        @Override
        public boolean isAvailable() {
          return !MapTool.isPersonalServer();
        }

        @Override
        protected void executeAction() {
          if (MapTool.isHostingServer() && !MapTool.confirm("msg.confirm.hostingDisconnect"))
            return;
          disconnectFromServer();
        }
      };

  public static void disconnectFromServer() {
    Campaign campaign;
    if (MapTool.isHostingServer()) {
      campaign = MapTool.getCampaign();
    } else {
      campaign = CampaignFactory.createBasicCampaign();
      new CampaignManager().clearCampaignData();
    }
    ServerDisconnectHandler.disconnectExpected = true;
    LOAD_MAP.setSeenWarning(false);
    MapTool.stopServer();
    MapTool.disconnect();
    MapTool.getFrame().getToolbarPanel().getMapselect().setVisible(true);
    MapTool.getFrame().getToolbarPanel().setTokenSelectionGroupEnabled(true);

    try {
      MapTool.startPersonalServer(campaign);
    } catch (IOException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | ExecutionException
        | InterruptedException e) {
      MapTool.showError("msg.error.failedStartPersonalServer", e);
    }
  }

  public static final Action PLAYER_DATABASE =
      new DefaultClientAction() {
        {
          init("action.playerDatabase");
        }

        @Override
        public boolean isAvailable() {
          return PlayerDatabaseFactory.getCurrentPlayerDatabase()
              instanceof PersistedPlayerDatabase;
        }

        @Override
        protected void executeAction() {
          new PlayerDatabaseDialog().show();
        }
      };

  public static final Action LOAD_CAMPAIGN =
      new DefaultClientAction() {
        {
          init("action.loadCampaign");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isHostingServer() || MapTool.isPersonalServer();
        }

        @Override
        protected void executeAction() {
          if (MapTool.isCampaignDirty() && !MapTool.confirm("msg.confirm.loseChanges")) return;
          JFileChooser chooser = new CampaignPreviewFileChooser();
          chooser.setDialogTitle(I18N.getText("msg.title.loadCampaign"));
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setFileFilter(MapTool.getFrame().getCmpgnFileFilter());

          if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File campaignFile = chooser.getSelectedFile();
            loadCampaign(campaignFile);
          }
        }
      };

  private static class CampaignPreviewFileChooser extends PreviewPanelFileChooser {
    private static final long serialVersionUID = -6566116259521360428L;

    CampaignPreviewFileChooser() {
      super();
      addChoosableFileFilter(MapTool.getFrame().getCmpgnFileFilter());
    }

    @Override
    protected File getImageFileOfSelectedFile() {
      if (getSelectedFile() == null) {
        return null;
      }
      return PersistenceUtil.getCampaignThumbnailFile(getSelectedFile().getName());
    }
  }

  public static void loadCampaign(final File campaignFile) {

    // By default all SwingWorkers run sequentially off the AWT event thread
    // Until we reconfigure that (load/save is really not something that's
    // needed to run in parallel though) we have to check the lock here for
    // good measure as otherwise nothing happens while the UI stays responsive
    // and the SwingWorker loading task silently waits for its turn.
    if (AppState.testBackgroundTaskLock()) {
      MapTool.showError("msg.error.failedLoadCampaignLock");
      return;
    }

    var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
    var openDir = campaignFile.toPath().getParent().toAbsolutePath();
    if (openDir.startsWith(installDir)) {
      MapTool.showWarning("msg.warning.loadCampaignFromInstallDir");
    }

    new CampaignLoader(campaignFile).execute();
  }

  /**
   * Loader class that encapsulates UI interaction for status, error reporting, and the async
   * loading
   */
  private static class CampaignLoader extends SwingWorker<PersistedCampaign, String> {
    private File campaignFile;
    private int maxWaitForLock = 30;

    public CampaignLoader(File campaignFile) {
      this.campaignFile = campaignFile;
    }

    /**
     * The asynchronous actions happening in the background. All UI interactions are published to
     * the AWT event dispatcher thread through the SwingWorker publish/process pattern.
     *
     * @return the loaded campaign
     * @throws Exception
     */
    @Override
    protected PersistedCampaign doInBackground() throws Exception {

      // wait for auto save to complete
      publish(I18N.getText("msg.autosave.wait", maxWaitForLock));
      AppState.acquireBackgroundTaskLock(maxWaitForLock);
      publish(I18N.getText("msg.info.campaignLoading"));

      try {
        // Before we do anything, let's back it up
        if (MapTool.getBackupManager() != null) {
          MapTool.getBackupManager().backup(campaignFile);
        }
        // Load
        return PersistenceUtil.loadCampaign(campaignFile);
      } finally {
        AppState.releaseBackgroundTaskLock();
      }
    }

    @Override
    protected void process(List<String> updates) {
      MapTool.getFrame()
          .showFilledGlassPane(new StaticMessageDialog(updates.get(updates.size() - 1)));
    }

    @Override
    protected void done() {

      MapTool.getFrame().hideGlassPane();
      try {
        PersistedCampaign campaign = get();

        ImageManager.flush(); // Clear out the old campaign's images

        AppState.setCampaignFile(campaignFile);
        AppPreferences.setLoadDir(campaignFile.getParentFile());
        AppMenuBar.getMruManager().addMRUCampaign(campaignFile);
        campaign.campaign.setName(AppState.getCampaignName()); // Update campaign name

        MapTool.serverCommand().setCampaign(campaign.campaign);

        MapTool.setCampaign(campaign.campaign, campaign.currentZoneId);
        ZoneRenderer current = MapTool.getFrame().getCurrentZoneRenderer();
        if (current != null) {
          if (campaign.currentView != null) {
            current.setZoneScale(campaign.currentView);
          }
          current.getZoneScale().reset();
        }
        MapTool.getAutoSaveManager().tidy();

        // UI related stuff
        MapTool.getFrame().getCommandPanel().clearAllIdentities();
        MapTool.getFrame().resetPanels();

      } catch (Throwable t) {
        if (t.getCause() instanceof AppState.FailedToAcquireLockException) {
          MapTool.showError("msg.error.failedLoadCampaignLock");
        } else {
          MapTool.showError("msg.error.failedLoadCampaign", t);
        }
      }
    }
  }

  /**
   * This is the integrated load/save interface that allows individual components of the
   * application's dataset to be saved to an external file. The goal is to allow specific maps and
   * tokens, campaign properties (sight, light, token props), and layers + their contents to be
   * saved through a single unified interface.
   */
  public static final Action LOAD_SAVE =
      new DeveloperClientAction() {
        {
          init("action.loadSaveDialog");
        }

        @Override
        protected void executeAction() {
          LoadSaveImpl impl = new LoadSaveImpl();
          impl.saveApplication(); // All the work is done here
        }
      };

  public static final Action SAVE_CAMPAIGN =
      new DefaultClientAction() {
        {
          init("action.saveCampaign");
        }

        @Override
        public boolean isAvailable() {
          return (MapTool.isHostingServer() || MapTool.getPlayer().isGM());
        }

        @Override
        protected void executeAction() {
          doSaveCampaign(null);
        }
      };

  public static final Action SAVE_CAMPAIGN_AS =
      new DefaultClientAction() {
        {
          init("action.saveCampaignAs");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
        }

        @Override
        protected void executeAction() {
          doSaveCampaignAs(null);
        }
      };

  public static void doSaveCampaign(Runnable onSuccess) {
    if (AppState.getCampaignFile() == null) {
      doSaveCampaignAs(onSuccess);
      return;
    }
    var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
    var saveDir = AppState.getCampaignFile().toPath().getParent().toAbsolutePath();
    if (saveDir.startsWith(installDir)) {
      MapTool.showWarning("msg.warning.saveCampaignToInstallDir");
      doSaveCampaignAs(onSuccess);
      return;
    }
    doSaveCampaign(AppState.getCampaignFile(), onSuccess);
  }

  private static void doSaveCampaign(final File file, Runnable onSuccess) {

    if (AppState.testBackgroundTaskLock()) {
      MapTool.showError("msg.error.failedSaveCampaignLock");
      return;
    }
    new CampaignSaver(file, onSuccess).execute();
  }

  private static class CampaignSaver extends SwingWorker<Object, String> {

    private File file;
    private Runnable onSuccess;
    private int maxWaitForLock = 30;

    public CampaignSaver(File file, Runnable onSuccess) {
      this.file = file;
      this.onSuccess = onSuccess;
    }

    @Override
    protected Object doInBackground() throws Exception {

      AppState.acquireBackgroundTaskLock(maxWaitForLock);

      publish(I18N.getText("msg.info.campaignSaving"));

      try {
        long start = System.currentTimeMillis();
        PersistenceUtil.saveCampaign(MapTool.getCampaign(), file);

        publish(I18N.getString("msg.info.campaignSaved"));

        // Minimum display time so people can see the message
        Thread.sleep(Math.max(0, 500 - (System.currentTimeMillis() - start)));

      } finally {
        AppState.releaseBackgroundTaskLock();
      }

      return null;
    }

    @Override
    protected void process(List<String> list) {
      MapTool.getFrame().showFilledGlassPane(new StaticMessageDialog(list.get(list.size() - 1)));
    }

    @Override
    protected void done() {
      MapTool.getFrame().hideGlassPane();

      try {
        get();
        MapTool.getFrame().setStatusMessage(I18N.getString("msg.info.campaignSaved"));
        AppMenuBar.getMruManager().addMRUCampaign(AppState.getCampaignFile());
        if (onSuccess != null) {
          onSuccess.run();
        }
      } catch (Throwable t) {
        if (t.getCause() instanceof AppState.FailedToAcquireLockException)
          MapTool.showError("msg.error.failedSaveCampaignLock");
        else MapTool.showError("msg.error.failedSaveCampaign", t.getCause());
      }
    }
  }

  public static void doSaveCampaignAs(Runnable onSuccess) {
    boolean tryAgain = true;
    while (tryAgain) {
      JFileChooser chooser = MapTool.getFrame().getSaveCmpgnFileChooser();
      int saveStatus = chooser.showSaveDialog(MapTool.getFrame());
      if (saveStatus == JFileChooser.APPROVE_OPTION) {
        var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
        var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
        if (saveDir.startsWith(installDir)) {
          MapTool.showWarning("msg.warning.saveCampaignToInstallDir");
        } else {
          tryAgain = false;
          saveAndUpdateCampaignName(chooser.getSelectedFile(), onSuccess);
        }
      } else {
        tryAgain = false;
      }
    }
  }

  private static void saveAndUpdateCampaignName(File selectedFile, Runnable onSuccess) {
    File campaignFile = getFileWithExtension(selectedFile, AppConstants.CAMPAIGN_FILE_EXTENSION);
    if (campaignFile.exists() && !MapTool.confirm("msg.confirm.overwriteExistingCampaign")) {
      return;
    }
    doSaveCampaign(campaignFile, onSuccess);
    AppState.setCampaignFile(campaignFile);
    AppPreferences.setSaveDir(campaignFile.getParentFile());
    AppMenuBar.getMruManager().addMRUCampaign(AppState.getCampaignFile());
    if (MapTool.isHostingServer() || MapTool.isPersonalServer()) {
      MapTool.serverCommand().setCampaignName(AppState.getCampaignName());
    }
  }

  private static File getFileWithExtension(File file, String extension) {
    if (!file.getName().toLowerCase().endsWith(extension)) {
      file = new File(file.getAbsolutePath() + extension);
    }
    return file;
  }

  public static final DeveloperClientAction SAVE_MAP_AS =
      new DeveloperClientAction() {
        {
          init("action.saveMapAs");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.getFrame().getCurrentZoneRenderer() != null
              && (MapTool.isHostingServer()
                  || (MapTool.getPlayer() != null && MapTool.getPlayer().isGM()));
        }

        @Override
        protected void executeAction() {
          ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
          JFileChooser chooser = MapTool.getFrame().getSaveMapFileChooser();
          chooser.setFileFilter(MapTool.getFrame().getMapFileFilter());
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setSelectedFile(new File(zr.getZone().getName()));
          boolean tryAgain = true;
          while (tryAgain) {
            if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
              break;
            }
            File mapFile = chooser.getSelectedFile();
            var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
            var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
            if (saveDir.startsWith(installDir)) {
              MapTool.showWarning("msg.warning.saveMapToInstallDir");
            } else {
              tryAgain = false;
              try {
                mapFile = getFileWithExtension(mapFile, AppConstants.MAP_FILE_EXTENSION);
                if (mapFile.exists()) {
                  if (!MapTool.confirm("msg.confirm.fileExists")) {
                    return;
                  }
                }
                PersistenceUtil.saveMap(zr.getZone(), mapFile);
                AppPreferences.setSaveMapDir(mapFile.getParentFile());
                MapTool.showInformation("msg.info.mapSaved");
              } catch (IOException ioe) {
                MapTool.showError("msg.error.failedSaveMap", ioe);
              }
            }
          }
        }
      };

  public abstract static class LoadMapAction extends DeveloperClientAction {
    private boolean seenWarning = false;

    public boolean getSeenWarning() {
      return seenWarning;
    }

    public void setSeenWarning(boolean s) {
      seenWarning = s;
    }
  }

  /**
   * LOAD_MAP is the Action used to implement the loading of an externally stored map into the
   * current campaign. This Action is only available when the current application is either hosting
   * a server or is not connected to a server.
   *
   * <p>Property used from <b>i18n.properties</b> is <code>action.loadMap</code>
   *
   * @author FJE
   */
  public static final LoadMapAction LOAD_MAP =
      new LoadMapAction() {
        {
          init("action.loadMap");
        }

        @Override
        public boolean isAvailable() {
          // return MapTool.isHostingServer() || MapTool.isPersonalServer();
          // I'd like to be able to use this instead as it's less restrictive, but it's safer to
          // disallow for now.
          return MapTool.isHostingServer()
              || (MapTool.getPlayer() != null && MapTool.getPlayer().isGM());
        }

        @Override
        protected void executeAction() {
          boolean isConnected = !MapTool.isHostingServer() && !MapTool.isPersonalServer();
          JFileChooser chooser = new MapPreviewFileChooser();
          chooser.setDialogTitle(I18N.getText("msg.title.loadMap"));
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setFileFilter(MapTool.getFrame().getMapFileFilter());

          if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
            new MapLoader(chooser.getSelectedFile()).execute();
          }
        }
      };

  public static final ClientAction IMPORT_DUNGEON_DRAFT_MAP =
      new ClientAction() {
        {
          init("action.import.dungeondraft");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isHostingServer()
              || (MapTool.getPlayer() != null && MapTool.getPlayer().isGM());
        }

        @Override
        protected void executeAction() {
          boolean isConnected = !MapTool.isHostingServer() && !MapTool.isPersonalServer();
          JFileChooser chooser = new MapPreviewFileChooser();
          chooser.setDialogTitle(I18N.getText("action.import.dungeondraft.dialog.title"));
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setFileFilter(MapTool.getFrame().getDungeonDraftFilter());

          if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File ddFile = chooser.getSelectedFile();
            try {
              new DungeonDraftImporter(ddFile).importVTT();
            } catch (IOException ioException) {
              MapTool.showError("dungeondraft.import.ioError", ioException);
            }
          }
        }
      };

  public static class MapPreviewFileChooser extends PreviewPanelFileChooser {
    public MapPreviewFileChooser() {
      super();
      addChoosableFileFilter(MapTool.getFrame().getMapFileFilter());
    }

    @Override
    protected File getImageFileOfSelectedFile() {
      if (getSelectedFile() == null) {
        return null;
      }
      return PersistenceUtil.getCampaignThumbnailFile(getSelectedFile().getName());
    }
  }

  private static class MapLoader extends SwingWorker<PersistedMap, String> {

    private File mapFile;

    public MapLoader(File mapFile) {
      this.mapFile = mapFile;
    }

    @Override
    protected PersistedMap doInBackground() throws Exception {
      publish(I18N.getText("msg.info.mapLoading"));
      return PersistenceUtil.loadMap(mapFile);
    }

    @Override
    protected void done() {

      MapTool.getFrame().hideGlassPane();

      try {
        PersistedMap map = get();
        AppPreferences.setLoadDir(mapFile.getParentFile());
        if ((map.zone.getExposedArea() != null && !map.zone.getExposedArea().isEmpty())
            || (map.zone.getExposedAreaMetaData() != null
                && !map.zone.getExposedAreaMetaData().isEmpty())) {
          boolean ok =
              MapTool.confirm(
                  "<html>Map contains exposed areas of fog.<br>Do you want to reset all of the fog?");
          if (ok) {
            // This fires a ModelChangeEvent, but that shouldn't matter
            map.zone.clearExposedArea(false);
          }
        }
        MapTool.addZone(map.zone);

      } catch (Exception ioe) {
        MapTool.showError(ioe.getMessage(), ioe);
      }

      MapTool.getAutoSaveManager().tidy();

      // Flush the images associated with the current
      // campaign
      // Do this juuuuuust before we get ready to show the
      // new campaign, since we
      // don't want the old campaign reloading images
      // while we loaded the new campaign
      // XXX (FJE) Is this call even needed for loading
      // maps? Probably not...
      ImageManager.flush();
    }

    @Override
    protected void process(List<String> list) {
      MapTool.getFrame()
          .showFilledGlassPane(new StaticMessageDialog(I18N.getText(list.get(list.size() - 1))));
    }
  }

  public static final Action CAMPAIGN_PROPERTIES =
      new DefaultClientAction() {
        {
          init("action.campaignProperties");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.getPlayer().isGM();
        }

        @Override
        protected void executeAction() {
          Campaign campaign = MapTool.getCampaign();

          // TODO: There should probably be only one of these
          CampaignPropertiesDialog dialog = new CampaignPropertiesDialog(MapTool.getFrame());
          dialog.setCampaign(campaign);
          dialog.setVisible(true);
          if (dialog.getStatus() == CampaignPropertiesDialog.Status.CANCEL) {
            return;
          }
          // TODO: Make this pass all properties, but we don't have that
          // framework yet, so send what we know the old fashioned way
          MapTool.serverCommand().updateCampaign(campaign.getCampaignProperties());
        }
      };

  public static class GridSizeAction extends DefaultClientAction {
    private final int size;

    public GridSizeAction(int size) {
      putValue(Action.NAME, Integer.toString(size));
      this.size = size;
    }

    @Override
    public boolean isSelected() {
      return AppState.getGridSize() == size;
    }

    @Override
    protected void executeAction() {
      AppState.setGridSize(size);
      MapTool.getFrame().refresh();
    }
  }

  public static class DownloadRemoteLibraryAction extends DefaultClientAction {
    private final URL url;

    public DownloadRemoteLibraryAction(URL url) {
      this.url = url;
    }

    @Override
    protected void executeAction() {
      if (!MapTool.confirm("confirm.downloadRemoteLibrary", url)) {
        return;
      }
      final RemoteFileDownloader downloader = new RemoteFileDownloader(url, MapTool.getFrame());
      new SwingWorker<Object, Object>() {
        @Override
        protected Object doInBackground() {
          try {
            File dataFile = downloader.read();
            if (dataFile == null) {
              // Canceled
              return null;
            }
            // Success
            String libraryName = FileUtil.getNameWithoutExtension(url);
            AppSetup.installLibrary(libraryName, dataFile.toURI().toURL());
          } catch (IOException e) {
            log.error("Could not download remote library: " + e, e);
          }
          return null;
        }

        @Override
        protected void done() {}
      }.execute();
    }
  }

  private static final int QUICK_MAP_ICON_SIZE = 25;

  public static class QuickMapAction extends AdminClientAction {
    private MD5Key assetId;

    public QuickMapAction(String name, File imagePath) {
      try {
        Asset asset = Asset.createImageAsset(name, FileUtils.readFileToByteArray(imagePath));
        assetId = asset.getMD5Key();

        // Make smaller
        BufferedImage iconImage =
            new BufferedImage(QUICK_MAP_ICON_SIZE, QUICK_MAP_ICON_SIZE, Transparency.OPAQUE);
        Image image = MapTool.getThumbnailManager().getThumbnail(imagePath);

        Graphics2D g = iconImage.createGraphics();
        g.drawImage(image, 0, 0, QUICK_MAP_ICON_SIZE, QUICK_MAP_ICON_SIZE, null);
        g.dispose();

        putValue(Action.SMALL_ICON, new ImageIcon(iconImage));
        putValue(Action.NAME, name);

        // Put it in the cache for easy access
        AssetManager.putAsset(asset);

        // But don't use up any extra memory
        AssetManager.removeAsset(asset.getMD5Key());
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      getActionList().add(this);
    }

    @Override
    protected void executeAction() {
      runBackground(
          () -> {
            Asset asset = AssetManager.getAsset(assetId);

            Zone zone = ZoneFactory.createZone();
            zone.setBackgroundPaint(new DrawableTexturePaint(asset.getMD5Key()));
            zone.setName(asset.getName());

            MapTool.addZone(zone);
          });
    }
  }

  public static final Action NEW_MAP =
      new AdminClientAction() {
        {
          init("action.newMap");
        }

        @Override
        protected void executeAction() {
          runBackground(
              () -> {
                Zone zone = ZoneFactory.createZone();
                MapPropertiesDialog newMapDialog =
                    MapPropertiesDialog.createMapPropertiesDialog(MapTool.getFrame());
                newMapDialog.setZone(zone);

                newMapDialog.setVisible(true);

                if (newMapDialog.getStatus() == MapPropertiesDialog.Status.OK) {
                  MapTool.addZone(zone);
                }
              });
        }
      };

  public static final Action EDIT_MAP =
      new ZoneAdminClientAction() {
        {
          init("action.editMap");
        }

        @Override
        protected void executeAction() {
          runBackground(
              () -> {
                Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
                MapPropertiesDialog newMapDialog =
                    MapPropertiesDialog.createMapPropertiesDialog(MapTool.getFrame());
                newMapDialog.setZone(zone);
                newMapDialog.setVisible(true);
                // Too many things can change to send them 1 by 1 to the client... just resend the
                // zone
                // MapTool.serverCommand().setBoard(zone.getId(), zone.getMapAssetId(),
                // zone.getBoardX(), zone.getBoardY());
                MapTool.serverCommand().removeZone(zone.getId());
                MapTool.serverCommand().putZone(zone);
                // MapTool.getFrame().getCurrentZoneRenderer().flush();
                MapTool.getFrame()
                    .setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
              });
        }
      };

  public static final Action GATHER_DEBUG_INFO =
      new DefaultClientAction() {
        {
          init("action.gatherDebugInfo");
        }

        @Override
        protected void executeAction() {
          SysInfoDialog.createAndShowGUI((String) getValue(Action.NAME));
        }
      };

  public static final Action ADD_RESOURCE_TO_LIBRARY =
      new DefaultClientAction() {
        {
          init("action.addIconSelector");
        }

        @Override
        protected void executeAction() {
          runBackground(
              () -> {
                AddResourceDialog dialog = new AddResourceDialog();
                dialog.showDialog();
              });
        }
      };

  public static final Action VIEW_ADD_ON_LIBRARIES =
      new DefaultClientAction() {
        {
          init("action.addOnLibraries");
        }

        @Override
        public boolean isAvailable() {
          return MapTool.isHostingServer()
              || (MapTool.getPlayer() != null && MapTool.getPlayer().isGM());
        }

        @Override
        protected void executeAction() {
          var dialog = new AddOnLibrariesDialogView();
          dialog.pack();
          SwingUtil.centerOver(dialog, MapTool.getFrame());
          dialog.setVisible(true);
        }
      };

  public static final Action EXIT =
      new DefaultClientAction() {
        {
          init("action.exit");
        }

        @Override
        protected void executeAction() {
          if (!MapTool.getFrame().confirmClose()) {
            return;
          } else {
            MapTool.getFrame().closingMaintenance();
          }
        }
      };

  /** Toggle the drawing of measurements. */
  public static final Action TOGGLE_DRAW_MEASUREMENTS =
      new DefaultClientAction() {
        {
          init("action.toggleDrawMeasurements");
        }

        @Override
        public boolean isSelected() {
          return MapTool.getFrame().isPaintDrawingMeasurement();
        }

        @Override
        protected void executeAction() {
          MapTool.getFrame()
              .setPaintDrawingMeasurement(!MapTool.getFrame().isPaintDrawingMeasurement());
        }
      };

  /** Toggle drawing straight lines at double width on the line tool. */
  public static final Action TOGGLE_DOUBLE_WIDE =
      new DefaultClientAction() {
        {
          init("action.toggleDoubleWide");
        }

        @Override
        public boolean isSelected() {
          return AppState.useDoubleWideLine();
        }

        @Override
        protected void executeAction() {
          AppState.setUseDoubleWideLine(!AppState.useDoubleWideLine());
          if (MapTool.getFrame() != null && MapTool.getFrame().getCurrentZoneRenderer() != null)
            MapTool.getFrame().getCurrentZoneRenderer().repaint();
        }
      };

  /** Class representing the turn on / turn off action of an overlay. */
  public static class ToggleOverlayAction extends ClientAction {
    private final HTMLOverlayManager overlayManager;

    /**
     * Creates a toggle action from an overlayManager.
     *
     * @param overlayManager the overlayManager to toggle
     */
    public ToggleOverlayAction(HTMLOverlayManager overlayManager) {
      this.overlayManager = overlayManager;
    }

    @Override
    public boolean isSelected() {
      return overlayManager.isVisible();
    }

    @Override
    public boolean isAvailable() {
      return true;
    }

    @Override
    protected void executeAction() {
      overlayManager.setVisible(!isSelected());
    }
  }

  public static class ToggleWindowAction extends ClientAction {
    private final MTFrame mtFrame;

    public ToggleWindowAction(MTFrame mtFrame) {
      this.mtFrame = mtFrame;
      init(mtFrame.getPropertyName());
    }

    @Override
    public boolean isSelected() {
      return MapTool.getFrame().getFrame(mtFrame).isShowing();
    }

    @Override
    public boolean isAvailable() {
      return true;
    }

    @Override
    protected void executeAction() {
      DockableFrame frame = MapTool.getFrame().getFrame(mtFrame);
      if (frame.isShowing()) {
        MapTool.getFrame().getDockingManager().hideFrame(mtFrame.name());
      } else {
        MapTool.getFrame().getDockingManager().showFrame(mtFrame.name());
      }
    }
  }

  private static List<ClientAction> actionList;

  private static List<ClientAction> getActionList() {
    if (actionList == null) {
      actionList = new ArrayList<ClientAction>();
    }
    return actionList;
  }

  public static void updateActions() {
    for (ClientAction action : actionList) {
      action.setEnabled(action.isAvailable());
    }
    MapTool.getFrame().getToolbox().updateTools();
  }

  public abstract static class ClientAction extends AbstractAction {
    /** Does the code need to guard against bug https://bugs.openjdk.java.net/browse/JDK-8208712. */
    private static final boolean NEEDS_GUARD;

    static {
      String prop = System.getProperty("os.name");
      NEEDS_GUARD =
          "Mac OS X"
              .equals(
                  prop); // MapTool doesnt run on version 8 or less of JDK so no need to check that
    }

    /**
     * The last time this action was called via accelerator key. This will only ever be set if
     * {@link #NEEDS_GUARD} is <code>true</code> and the menu item is a JMenuCheckBoxItem
     */
    private long lastAccelInvoke;

    public final void execute(ActionEvent e) {
      if (NEEDS_GUARD && (e != null) && (e.getSource() instanceof JCheckBoxMenuItem)) {
        if (e.getModifiers() == 0) {
          if (TimeUnit.MILLISECONDS.toSeconds(e.getWhen() - lastAccelInvoke) < 1) {
            return; // Nothing to do as its due to the JDK bug
            // https://bugs.openjdk.java.net/browse/JDK-8208712
          }
        } else if ((e.getModifiers() & menuShortcut) != 0) {
          lastAccelInvoke = e.getWhen();
        }
      }
      executeAction();
    }

    public void init(String key) {
      init(key, true);
    }

    public void init(String key, boolean addMenuShortcut) {
      I18N.setAction(key, this, addMenuShortcut);
      getActionList().add(this);
    }

    /**
     * This convenience function returns the KeyStroke that represents the accelerator key used by
     * the Action. This function can return <code>null</code> because not all Actions have an
     * associated accelerator key defined, but it is currently only called by methods that reference
     * the {CUT,COPY,PASTE}_TOKEN Actions.
     *
     * @return KeyStroke associated with the Action or <code>null</code>
     */
    public final KeyStroke getKeyStroke() {
      return (KeyStroke) getValue(Action.ACCELERATOR_KEY);
    }

    public abstract boolean isAvailable();

    public boolean isSelected() {
      return false;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
      execute(e);
      // System.out.println(getValue(Action.NAME));
      updateActions();
    }

    protected abstract void executeAction();

    public void runBackground(final Runnable r) {
      new Thread(
              () -> {
                r.run();

                updateActions();
              })
          .start();
    }
  }

  /**
   * This class simply provides an implementation for <code>isAvailable()</code> that returns <code>
   * true</code> if the current player is a GM.
   */
  public abstract static class AdminClientAction extends ClientAction {
    @Override
    public boolean isAvailable() {
      return MapTool.getPlayer().isGM();
    }
  }

  /**
   * This class simply provides an implementation for <code>isAvailable()</code> that returns <code>
   * true</code> if the current player is a GM and there is a ZoneRenderer current.
   */
  public abstract static class ZoneAdminClientAction extends AdminClientAction {
    @Override
    public boolean isAvailable() {
      return super.isAvailable() && MapTool.getFrame().getCurrentZoneRenderer() != null;
    }
  }

  /**
   * This class simply provides an implementation for <code>isAvailable()</code> that returns <code>
   * true</code> if there is a ZoneRenderer current.
   */
  public abstract static class ZoneClientAction extends ClientAction {
    @Override
    public boolean isAvailable() {
      return MapTool.getFrame().getCurrentZoneRenderer() != null;
    }
  }

  /**
   * This class simply provides an implementation for <code>isAvailable()</code> that returns <code>
   * true</code>.
   */
  public abstract static class DefaultClientAction extends ClientAction {
    @Override
    public boolean isAvailable() {
      return true;
    }
  }

  /** This class provides an action that displays a url from I18N */
  public static class OpenUrlAction extends DefaultClientAction {
    public OpenUrlAction(String key) {
      // The init() method will load the "key", "key.accel", and "key.description".
      // The value of "key" will be used as the menu text, the accelerator is not used,
      // and the description will be the destination URL. Only the Help menu uses these objects and
      // only the Help menu expects that field to be set...
      init(key);
    }

    @Override
    protected void executeAction() {
      if (getValue(Action.SHORT_DESCRIPTION) != null)
        MapTool.showDocument((String) getValue(Action.SHORT_DESCRIPTION));
    }
  }

  /**
   * This class simply provides an implementation for <code>isAvailable()</code> that returns <code>
   * true</code> if the system property MAPTOOL_DEV is not set to "false". This allows it to contain
   * the version number of the compatible build for debugging purposes. For example, if I'm working
   * on a patch to 1.3.b54, I can set MAPTOOL_DEV to 1.3.b54 in order to test it against a 1.3.b54
   * client.
   */
  @SuppressWarnings("serial")
  public abstract static class DeveloperClientAction extends ClientAction {
    @Override
    public boolean isAvailable() {
      return System.getProperty("MAPTOOL_DEV") != null
          && !"false".equals(System.getProperty("MAPTOOL_DEV"));
    }
  }

  public static class OpenMRUCampaign extends AbstractAction {
    private final File campaignFile;

    public OpenMRUCampaign(File file, int position) {
      campaignFile = file;
      String label = position + " " + campaignFile.getName();
      putValue(Action.NAME, label);

      if (position <= 9) {
        int keyCode = KeyStroke.getKeyStroke(Integer.toString(position)).getKeyCode();
        putValue(Action.MNEMONIC_KEY, keyCode);
      }
      // Use the saved campaign thumbnail as a tooltip
      File thumbFile = PersistenceUtil.getCampaignThumbnailFile(campaignFile.getName());
      String htmlTip;

      if (thumbFile.exists()) {
        URL url = null;
        try {
          url = thumbFile.toURI().toURL();
        } catch (MalformedURLException e) {
          // Can this even happen?
          MapTool.showWarning("Converting File to URL threw an exception?!", e);
          return;
        }
        htmlTip = "<html><img src=\"" + url.toExternalForm() + "\"></html>";
        // The above should really be something like:
        // htmlTip = new Node("html").addChild("img").attr("src", thumbFile.toURI().toURL()).end();
        // The idea being that each method returns a proper value that allows them to be chained.
      } else {
        htmlTip = I18N.getText("msg.info.noCampaignPreview");
      }

      /*
       * There is some extra space appearing to the right of the images, which sounds similar to what was reported in this bug (bottom half):
       * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5047379 Removing the mnemonic will remove this extra space.
       */
      putValue(Action.SHORT_DESCRIPTION, htmlTip);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      if (MapTool.isCampaignDirty() && !MapTool.confirm("msg.confirm.loseChanges")) return;
      AppActions.loadCampaign(campaignFile);
    }
  }
}
