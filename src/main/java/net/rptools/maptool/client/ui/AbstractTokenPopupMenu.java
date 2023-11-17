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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.*;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.TokenShape;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.FileUtil;
import net.rptools.maptool.util.PersistenceUtil;

public abstract class AbstractTokenPopupMenu extends JPopupMenu {
  private static final long serialVersionUID = -3741870412603226747L;

  private final ZoneRenderer renderer;
  private final Token tokenUnderMouse;
  private boolean areTokensOwned;
  int x, y;
  Set<GUID> selectedTokenSet;

  public AbstractTokenPopupMenu(
      Set<GUID> selectedTokenSet, int x, int y, ZoneRenderer renderer, Token tokenUnderMouse) {
    super();
    this.renderer = renderer;
    this.x = x;
    this.y = y;
    this.selectedTokenSet = selectedTokenSet;
    this.tokenUnderMouse = tokenUnderMouse;

    setOwnership();
  }

  protected boolean tokensAreOwned() {
    return areTokensOwned;
  }

  private void setOwnership() {
    areTokensOwned = true;
    if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = getRenderer().getZone().getToken(tokenGUID);

        if (!token.isOwner(MapTool.getPlayer().getName())) {
          areTokensOwned = false;
          break;
        }
      }
    }
  }

  protected class ShowHandoutAction extends AbstractAction {
    public ShowHandoutAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.showHandout"));
      setEnabled(getTokenUnderMouse().getCharsheetImage() != null);
    }

    public void actionPerformed(ActionEvent e) {
      AssetViewerDialog dialog =
          new AssetViewerDialog(
              getTokenUnderMouse().getName() + "'s Character Sheet",
              getTokenUnderMouse().getCharsheetImage());
      dialog.pack();
      dialog.setVisible(true);
    }
  }

  protected JMenu createLightSourceMenu() {
    JMenu menu = new JMenu(I18N.getText("panel.MapExplorer.View.LIGHT_SOURCES"));

    if (tokenUnderMouse.hasLightSources()) {
      menu.add(new ClearLightAction());

      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token.hasLightSourceType(LightSource.Type.NORMAL)) {
          menu.add(new ClearLightsOnlyAction());
        }
        if (token.hasLightSourceType(LightSource.Type.AURA)) {
          menu.add(new ClearAurasOnlyAction());
        }
        if (token.hasGMAuras()) {
          menu.add(new ClearGMAurasOnlyAction());
        }
        if (token.hasOwnerOnlyAuras()) {
          menu.add(new ClearOwnerAurasOnlyAction());
        }
      }
      menu.addSeparator();
    }

    // Add unique light sources for the token.
    {
      JMenu subMenu = createLightCategoryMenu("Unique", tokenUnderMouse.getUniqueLightSources());
      if (subMenu.getItemCount() != 0) {
        menu.add(subMenu);
        menu.addSeparator();
      }
    }

    for (Entry<String, Map<GUID, LightSource>> entry :
        MapTool.getCampaign().getLightSourcesMap().entrySet()) {
      JMenu subMenu = createLightCategoryMenu(entry.getKey(), entry.getValue().values());
      if (subMenu.getItemCount() != 0) {
        menu.add(subMenu);
      }
    }
    return menu;
  }

  protected JMenu createLightCategoryMenu(String categoryName, Collection<LightSource> sources) {
    JMenu subMenu = new JMenu(categoryName);

    List<LightSource> lightSources = new ArrayList<>(sources);
    Collections.sort(lightSources);

    for (LightSource lightSource : lightSources) {
      // Don't include light sources that don't have lights visible to the player. Note that the
      // player must be an owner to use the popup, so don't bother checking `::isOwner()`.
      boolean include =
          MapTool.getPlayer().isGM() || !lightSource.getLightList().stream().allMatch(Light::isGM);
      if (include) {
        JCheckBoxMenuItem menuItem =
            new JCheckBoxMenuItem(new ToggleLightSourceAction(lightSource));
        menuItem.setSelected(tokenUnderMouse.hasLightSource(lightSource));
        subMenu.add(menuItem);
      }
    }

    return subMenu;
  }

  protected Token getTokenUnderMouse() {
    return tokenUnderMouse;
  }

  protected JMenu createFlipMenu() {
    JMenu flipMenu = new JMenu(I18N.getText("token.popup.menu.flip"));

    flipMenu.add(
        new AbstractAction() {
          {
            putValue(NAME, I18N.getText("token.popup.menu.flip.horizontal"));
          }

          public void actionPerformed(ActionEvent e) {
            for (GUID tokenGUID : selectedTokenSet) {
              Token token = renderer.getZone().getToken(tokenGUID);
              if (token == null) {
                continue;
              }
              MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipX);
            }
            MapTool.getFrame().refresh();
          }
        });
    flipMenu.add(
        new AbstractAction() {
          {
            putValue(NAME, I18N.getText("token.popup.menu.flip.vertical"));
          }

          public void actionPerformed(ActionEvent e) {
            for (GUID tokenGUID : selectedTokenSet) {
              Token token = renderer.getZone().getToken(tokenGUID);
              if (token == null) {
                continue;
              }
              MapTool.serverCommand().updateTokenProperty(token, Token.Update.flipY);
            }
            MapTool.getFrame().refresh();
          }
        });
    flipMenu.add(
        new AbstractAction() {
          {
            putValue(NAME, I18N.getText("token.popup.menu.flip.isometric"));
          }

          public void actionPerformed(ActionEvent e) {
            for (GUID tokenGUID : selectedTokenSet) {
              Token token = renderer.getZone().getToken(tokenGUID);
              if (token == null) {
                continue;
              }
              token.setFlippedIso(!token.isFlippedIso());
              MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
            }
            MapTool.getFrame().refresh();
          }
        });
    return flipMenu;
  }

  protected JMenu createChangeToMenu(Zone.Layer... types) {
    JMenu changeTypeMenu = new JMenu(I18N.getText("token.popup.menu.change"));
    for (Zone.Layer layer : types) {
      changeTypeMenu.add(new JMenuItem(new ChangeTypeAction(layer)));
    }
    return changeTypeMenu;
  }

  protected JMenu createArrangeMenu() {
    JMenu arrangeMenu = new JMenu(I18N.getText("token.popup.menu.arrange"));
    JMenuItem bringToFrontMenuItem = new JMenuItem(I18N.getText("token.popup.menu.zorder.front"));
    bringToFrontMenuItem.addActionListener(new BringToFrontAction());

    JMenuItem sendToBackMenuItem = new JMenuItem(I18N.getText("token.popup.menu.zorder.back"));
    sendToBackMenuItem.addActionListener(new SendToBackAction());

    arrangeMenu.add(bringToFrontMenuItem);
    arrangeMenu.add(sendToBackMenuItem);

    return arrangeMenu;
  }

  protected JMenu createSizeMenu() {
    JMenu sizeMenu = new JMenu(I18N.getText("token.popup.menu.size"));

    JCheckBoxMenuItem freeSize = new JCheckBoxMenuItem(new FreeSizeAction());
    freeSize.setSelected(!tokenUnderMouse.isSnapToScale());
    sizeMenu.add(freeSize);

    JCheckBoxMenuItem resetSize = new JCheckBoxMenuItem(new ResetSizeAction());
    sizeMenu.add(resetSize);

    sizeMenu.addSeparator();

    Grid grid = renderer.getZone().getGrid();
    for (TokenFootprint footprint : grid.getFootprints()) {
      JMenuItem menuItem = new JCheckBoxMenuItem(new ChangeSizeAction(footprint));
      if (tokenUnderMouse.isSnapToScale() && tokenUnderMouse.getFootprint(grid) == footprint) {
        menuItem.setSelected(true);
      }
      sizeMenu.add(menuItem);
    }
    return sizeMenu;
  }

  protected void addGMItem(Action action) {
    if (action == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) {
      add(new JMenuItem(action));
    }
  }

  protected void addGMItem(JMenu menu) {
    if (menu == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) {
      add(menu);
    }
  }

  protected void addToggledGMItem(Action action, boolean checked) {
    if (action == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
      item.setSelected(checked);
      add(item);
    }
  }

  protected void addToggledOwnedItem(Action action, boolean checked) {
    if (action == null) {
      return;
    }
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
    item.setSelected(checked);
    item.setEnabled(areTokensOwned);
    add(item);
  }

  protected void addToggledItem(Action action, boolean checked) {
    if (action == null) {
      return;
    }
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
    item.setSelected(checked);
    add(item);
  }

  protected void addOwnedItem(Action action) {
    if (action == null) {
      return;
    }
    JMenuItem item = new JMenuItem(action);
    item.setEnabled(areTokensOwned);
    add(new JMenuItem(action));
  }

  protected void addOwnedItem(JMenu menu) {
    if (menu == null) {
      return;
    }
    menu.setEnabled(areTokensOwned);
    add(menu);
  }

  protected ZoneRenderer getRenderer() {
    return renderer;
  }

  public void showPopup(JComponent component) {
    show(component, x, y);
  }

  public class ChangeTypeAction extends AbstractAction {
    private final Zone.Layer layer;

    public ChangeTypeAction(Zone.Layer layer) {
      putValue(Action.NAME, layer.toString());
      this.layer = layer;
    }

    public void actionPerformed(ActionEvent e) {
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        token.setLayer(layer);
        if (token.getShape() != TokenShape.FIGURE) {
          token.guessAndSetShape();
        }
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
      MapTool.getFrame().updateTokenTree();
    }
  }

  public class FreeSizeAction extends AbstractAction {
    public FreeSizeAction() {
      String actionText =
          I18N.getText(
              "token.popup.menu.size"
                  + (tokenUnderMouse.getLayer().isStampLayer() ? ".free" : ".native"));
      putValue(Action.NAME, actionText);
    }

    public void actionPerformed(ActionEvent e) {
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        token.setSnapToScale(false);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class ResetSizeAction extends AbstractAction {
    public ResetSizeAction() {
      // putValue(Action.NAME, tokenUnderMouse.isStamp() ? "Free Size" : "Native Size");
      putValue(Action.NAME, I18N.getText("token.popup.menu.size.reset"));
    }

    public void actionPerformed(ActionEvent e) {
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        token.setFootprint(
            renderer.getZone().getGrid(), renderer.getZone().getGrid().getDefaultFootprint());
        token.setSnapToScale(true);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class CopyAction extends AbstractAction {
    public CopyAction() {
      putValue(Action.NAME, I18N.getText("action.copyTokens"));
    }

    public void actionPerformed(ActionEvent e) {
      AppActions.copyTokens(selectedTokenSet);
      AppActions.updateActions();
    }
  }

  public class CutAction extends AbstractAction {
    public CutAction() {
      putValue(Action.NAME, I18N.getText("action.cutTokens"));
    }

    public void actionPerformed(ActionEvent e) {
      AppActions.cutTokens(renderer.getZone(), selectedTokenSet);
      AppActions.updateActions();
    }
  }

  public class ToggleLightSourceAction extends AbstractAction {
    private final LightSource lightSource;

    public ToggleLightSourceAction(LightSource lightSource) {
      super(lightSource.getName());
      this.lightSource = lightSource;
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        if (token.hasLightSource(lightSource)) {
          token.removeLightSource(lightSource.getId());
        } else {
          token.addLightSource(lightSource.getId());
        }
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);

        renderer.repaint();
      }
    }
  }

  public class SaveAction extends AbstractAction {
    boolean saveMultipleTokens = false;

    public SaveAction() {
      super(I18N.getText("token.popup.menu.save"));

      if (selectedTokenSet.size() > 1) {
        saveMultipleTokens = true;
      }
    }

    public void actionPerformed(ActionEvent e) {
      final boolean showSaveDialog;
      boolean showOverwriteDialog = true;
      boolean overWriteFile = false;
      boolean saveAsGmName = false;
      boolean saveAsImage = false;
      boolean saveAsPortrait = false;
      File saveDirectory = null;
      final FileFilter tokenFilter = new FileNameExtensionFilter("Token", Token.FILE_EXTENSION);
      final FileFilter tokenFilterGM =
          new FileNameExtensionFilter("Token (GM Name)", Token.FILE_EXTENSION);
      final FileFilter tokenFilterImage = new FileNameExtensionFilter("Token Image", "png");
      final FileFilter tokenFilterPortrait = new FileNameExtensionFilter("Token Portrait", "png");

      if (saveMultipleTokens) {
        showSaveDialog =
            MapTool.confirm(
                "You have selected multiple tokens.\n"
                    + "Do you want to show the Save As Dialog prompt for each token?");
      } else {
        showSaveDialog = true;
      }

      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        File tokenSaveFile;

        final String tokenName = token.getName();
        final String tokenNameGM;
        if (token.getGMName() == null) tokenNameGM = tokenName;
        else if (token.getGMName().trim().isEmpty()) tokenNameGM = tokenName;
        else tokenNameGM = token.getGMName();

        // chooser.setCurrentDirectory(AppPreferences.getSaveDir());

        final JFileChooser chooser = MapTool.getFrame().getSaveTokenFileChooser();
        final File defaultFile =
            FileUtil.cleanFileName(chooser.getCurrentDirectory().toString(), tokenName, "");

        chooser.resetChoosableFileFilters();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(tokenFilter);
        chooser.addChoosableFileFilter(tokenFilterGM);
        chooser.addChoosableFileFilter(tokenFilterImage);
        chooser.addChoosableFileFilter(tokenFilterPortrait);
        chooser.setSelectedFile(defaultFile);

        chooser.addPropertyChangeListener(
            evt -> {
              if (evt.getPropertyName() == JFileChooser.FILE_FILTER_CHANGED_PROPERTY
                  && showSaveDialog) {
                if (chooser.getFileFilter() != tokenFilter) {
                  File newFileName = new File(chooser.getCurrentDirectory(), tokenNameGM);
                  System.out.println("newFileName 1: " + newFileName);
                  chooser.setSelectedFile(newFileName);
                } else {
                  File newFileName = new File(chooser.getCurrentDirectory(), tokenName);
                  System.out.println("newFileName 1: " + newFileName);
                  chooser.setSelectedFile(newFileName);
                }
              }
            });

        if (showSaveDialog) {
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

          boolean tryAgain = true;
          while (tryAgain) {
            if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
              return;
            }

            saveDirectory = chooser.getSelectedFile();
            var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
            var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
            if (saveDir.startsWith(installDir)) {
              MapTool.showWarning("msg.warning.saveTokenToInstallDir");
            } else {
              tryAgain = false;
            }
          }

          tokenSaveFile = chooser.getSelectedFile();
        } else {
          if (saveDirectory == null) {
            boolean tryAgain = true;
            while (tryAgain) {
              chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
              if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                return;
              }
              if (chooser.getFileFilter() == tokenFilterGM) {
                saveAsGmName = true;
              }
              saveDirectory = chooser.getSelectedFile();
              var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
              var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
              if (saveDir.startsWith(installDir)) {
                MapTool.showWarning("msg.warning.saveTokenToInstallDir");
              } else {
                tryAgain = false;
              }
            }
          }

          if (saveAsGmName) {
            tokenSaveFile =
                FileUtil.cleanFileName(saveDirectory.getAbsolutePath(), tokenNameGM, "");
          } else {
            tokenSaveFile = FileUtil.cleanFileName(saveDirectory.getAbsolutePath(), tokenName, "");
          }
        }

        if (chooser.getFileFilter() == tokenFilterImage) {
          saveAsImage = true;
        } else if (chooser.getFileFilter() == tokenFilterPortrait) {
          saveAsImage = true;
          saveAsPortrait = true;
        }

        // Auto-extension
        if (!saveAsImage
            && !tokenSaveFile.getName().toLowerCase().endsWith("." + Token.FILE_EXTENSION)) {
          tokenSaveFile = new File(tokenSaveFile.getAbsolutePath() + "." + Token.FILE_EXTENSION);
        }

        if (tokenSaveFile.exists() && saveMultipleTokens && showOverwriteDialog) {
          JPanel messageObj = new JPanel();
          JLabel message = new JLabel("File exists, would you like to overwrite?\n");
          JCheckBox dontAskAgainCb = new JCheckBox("Don't ask me this again.");

          dontAskAgainCb.setHorizontalAlignment(SwingConstants.LEFT);
          dontAskAgainCb.setVerticalAlignment(SwingConstants.BOTTOM);
          dontAskAgainCb.setFont(new Font("Tahoma", Font.PLAIN, 9));

          message.setHorizontalAlignment(SwingConstants.LEFT);
          message.setVerticalTextPosition(SwingConstants.TOP);

          messageObj.setLayout(new BorderLayout(0, 10));
          messageObj.add(message, BorderLayout.NORTH);
          messageObj.add(dontAskAgainCb, BorderLayout.SOUTH);

          overWriteFile =
              JOptionPane.OK_OPTION
                  == JOptionPane.showConfirmDialog(
                      MapTool.getFrame(), messageObj, "Confirmation", JOptionPane.YES_OPTION);
          showOverwriteDialog = !dontAskAgainCb.isSelected();
        } else if (tokenSaveFile.exists() && showOverwriteDialog) {
          overWriteFile = MapTool.confirm("File exists, would you like to overwrite?");
        }

        if (tokenSaveFile.exists() && !overWriteFile) continue;

        if (!MapTool.getPlayer().isGM()) {
          token.setGMNotes("");
        }
        try {
          if (saveAsImage && !saveAsPortrait) {
            PersistenceUtil.saveTokenImage(token.getImageAssetId(), tokenSaveFile);
          } else if (saveAsPortrait) {
            PersistenceUtil.saveTokenImage(
                token.getPortraitImage(),
                FileUtil.cleanFileName(tokenSaveFile + " [Portrait]", ""));
          } else {
            PersistenceUtil.saveToken(token, tokenSaveFile);
          }
          saveDirectory = tokenSaveFile.getParentFile();
        } catch (IOException ioe) {
          ioe.printStackTrace();
          MapTool.showError("Could not save token: " + ioe);
        }
      }
      if (saveDirectory != null) {
        AppPreferences.setTokenSaveDir(saveDirectory);
      }
    }
  }

  public class SetFacingAction extends AbstractAction {
    public SetFacingAction() {
      super(I18N.getText("token.popup.menu.facing.set"));
    }

    public void actionPerformed(ActionEvent e) {
      Toolbox toolbox = MapTool.getFrame().getToolbox();

      FacingTool tool = (FacingTool) toolbox.getTool(FacingTool.class);
      tool.init(tokenUnderMouse, renderer.getOwnedTokens(selectedTokenSet));

      toolbox.setSelectedTool(FacingTool.class);
    }
  }

  public class ClearFacingAction extends AbstractAction {
    public ClearFacingAction() {
      super(I18N.getString("token.popup.menu.facing.clear"));
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        token.setFacing(null);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class ClearLightsOnlyAction extends AbstractAction {
    public ClearLightsOnlyAction() {
      super(I18N.getString("token.popup.menu.lights.clear"));
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token.hasLightSourceType(LightSource.Type.NORMAL)) {
          token.removeLightSourceType(LightSource.Type.NORMAL);
        }
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class ClearAurasOnlyAction extends AbstractAction {
    public ClearAurasOnlyAction() {
      super(I18N.getString("token.popup.menu.auras.clear"));
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token.hasLightSourceType(LightSource.Type.AURA)) {
          token.removeLightSourceType(LightSource.Type.AURA);
        }
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class ClearGMAurasOnlyAction extends AbstractAction {
    public ClearGMAurasOnlyAction() {
      super("token.popup.menu.auras.clearGM");
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token.hasGMAuras()) {
          token.removeGMAuras();
        }
        renderer.flush(token);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
        renderer.getZone().putToken(token);
      }
      renderer.repaint();
    }
  }

  public class ClearOwnerAurasOnlyAction extends AbstractAction {
    public ClearOwnerAurasOnlyAction() {
      super(I18N.getText("token.popup.menu.auras.clearOwner"));
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (token.hasOwnerOnlyAuras()) {
          token.removeOwnerOnlyAuras();
        }
        renderer.flush(token);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
        renderer.getZone().putToken(token);
      }
      renderer.repaint();
    }
  }

  public class ClearLightAction extends AbstractAction {
    public ClearLightAction() {
      super(I18N.getText("token.popup.menu.lights.clearAll"));
    }

    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        token.clearLightSources();
        renderer.flush(token);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
        renderer.getZone().putToken(token);
      }
      renderer.repaint();
    }
  }

  public class SnapToGridAction extends AbstractAction {
    private final boolean snapToGrid;
    private final ZoneRenderer renderer;

    public SnapToGridAction(boolean snapToGrid, ZoneRenderer renderer) {
      super(I18N.getText("token.popup.menu.snap"));
      this.snapToGrid = snapToGrid;
      this.renderer = renderer;
    }

    public void actionPerformed(ActionEvent e) {
      for (GUID guid : selectedTokenSet) {
        Zone zone = renderer.getZone();
        Token token = zone.getToken(guid);
        if (token == null) {
          continue;
        }
        ZonePoint zp;
        if (snapToGrid) {
          zp = token.getUnsnappedPoint(zone);
        } else {
          zp = token.getSnappedPoint(zone);
        }
        // Updates both snap-to-grid and new location in one command
        Token.Update update = Token.Update.setSnapToGridAndXY;
        MapTool.serverCommand().updateTokenProperty(token, update, !snapToGrid, zp.x, zp.y);
      }
    }
  }

  /** Internal class used to handle token state changes. */
  public class ChangeStateAction extends AbstractAction {

    /**
     * Initialize a state action for a given state.
     *
     * @param state The name of the state set when this action is executed
     */
    public ChangeStateAction(String state) {
      putValue(ACTION_COMMAND_KEY, state); // Set the state command

      // Load the name, mnemonic, accelerator, and description if
      // available
      String key = "defaultTool.stateAction." + state;
      String name = net.rptools.maptool.language.I18N.getText(key);
      if (!name.equals(key)) {
        putValue(NAME, name);
        int mnemonic = I18N.getMnemonic(key);
        if (mnemonic != -1) putValue(MNEMONIC_KEY, mnemonic);
        String accel = I18N.getAccelerator(key);
        if (accel != null) putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel));
        String description = I18N.getDescription(key);
        if (description != null) putValue(SHORT_DESCRIPTION, description);
      } else {

        // Default name if no I18N set
        putValue(NAME, state);
      } // endif
    }

    /**
     * Set the state for all of the selected tokens.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent aE) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        if (aE.getActionCommand().equals("clear")) {
          // Wipe out the entire state HashMap, this is what the previous
          // code attempted to do but was failing due to the Set returned
          // by getStatePropertyNames being a non-static view into a set.
          // Removing items from the map was messing up the iteration.
          // Here, clear all states, unfortunately, including light.
          token.getStatePropertyNames().clear();
        } else {
          token.setState(
              aE.getActionCommand(),
              ((JCheckBoxMenuItem) aE.getSource()).isSelected() ? Boolean.TRUE : null);
        }
        renderer.flush(token);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class ChangeSizeAction extends AbstractAction {
    private final TokenFootprint footprint;

    public ChangeSizeAction(TokenFootprint footprint) {
      super(footprint.getLocalizedName());
      this.footprint = footprint;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event. ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = renderer.getZone().getToken(tokenGUID);
        token.setFootprint(renderer.getZone().getGrid(), footprint);
        token.setSnapToScale(true);
        renderer.flush(token);
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      }
      renderer.repaint();
    }
  }

  public class VisibilityAction extends AbstractAction {
    {
      putValue(Action.NAME, I18N.getText("token.popup.menu.visible"));
    }

    public void actionPerformed(ActionEvent e) {
      for (GUID guid : selectedTokenSet) {
        Token token = renderer.getZone().getToken(guid);
        if (token == null) {
          continue;
        }
        token.setVisible(((JCheckBoxMenuItem) e.getSource()).isSelected());
        MapTool.getFrame().updateTokenTree();
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);

        // TODO: Need a better way of indicating local changes
        renderer.getZone().putToken(token);
      }
      renderer.repaint();
    }
  }

  public class BringToFrontAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      MapTool.serverCommand().bringTokensToFront(renderer.getZone().getId(), selectedTokenSet);
      MapTool.getFrame().refresh();
    }
  }

  public class SendToBackAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      MapTool.serverCommand().sendTokensToBack(renderer.getZone().getId(), selectedTokenSet);
      MapTool.getFrame().refresh();
    }
  }

  public class ImpersonateAction extends AbstractAction {
    public ImpersonateAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.impersonate"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!AppUtil.playerOwns(tokenUnderMouse)) {
        return;
      }
      MapTool.getFrame().getCommandPanel().commitCommand("/im " + tokenUnderMouse.getId());
      MapTool.getFrame().getCommandPanel().getCommandTextArea().requestFocusInWindow();
    }
  }

  public class StartMoveAction extends AbstractAction {
    public StartMoveAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.move"));
    }

    public void actionPerformed(ActionEvent e) {
      Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
      if (selectedTokenSet.isEmpty()) {
        selectedTokenSet.addAll(renderer.getSelectedTokenSet());
      }
      if (tool instanceof PointerTool) {
        ((PointerTool) tool).startTokenDrag(tokenUnderMouse, selectedTokenSet);
      } else if (tool instanceof StampTool) {
        ((StampTool) tool).startTokenDrag(tokenUnderMouse, selectedTokenSet);
      }
    }
  }

  public class ShowPropertiesDialogAction extends AbstractAction {
    public ShowPropertiesDialogAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.edit"));

      // Jamz: Bug fix, we don't support editing multiple tokens here.
      if (selectedTokenSet.size() > 1) {
        setEnabled(false);
      } else if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isTokenEditorLocked()) {
        setEnabled(false);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MapTool.getFrame().showTokenPropertiesDialog(getTokenUnderMouse(), getRenderer());
    }

    /**
     * Converts the action to a JMenuItem, and adds a tooltip if the action is blocked.
     *
     * @return the new {@link JMenuItem}
     */
    public JMenuItem asJMenuItem() {
      JMenuItem jMenuItem = new JMenuItem(this);
      if (selectedTokenSet.size() > 1) {
        jMenuItem.setToolTipText(I18N.getText("token.popup.menu.edit.toomany.tooltip"));
      } else if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isTokenEditorLocked()) {
        jMenuItem.setToolTipText(I18N.getText("token.popup.menu.edit.notallowed.tooltip"));
      }
      return jMenuItem;
    }
  }

  public class DeleteAction extends AbstractAction {
    public DeleteAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.delete"));
    }

    public void actionPerformed(ActionEvent e) {
      // check to see if this is the required action
      if (!MapTool.confirmTokenDelete()) {
        return;
      }
      AppActions.deleteTokens(renderer.getZone(), selectedTokenSet);
    }
  }

  public class AutoResizeAction extends AbstractAction {
    public AutoResizeAction() {
      super(I18N.getText("token.popup.menu.autoresize"));

      if (selectedTokenSet.size() > 1) {
        setEnabled(false);
      }
    }

    public void actionPerformed(ActionEvent e) {
      renderer.setAutoResizeStamp(true);
      renderer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
  }

  /**
   * Menu option to toggle token visibility to always shown (over VBL/FoW)
   *
   * @author Jamz
   * @since 1.4.1.5
   */
  public class AlwaysVisibleAction extends AbstractAction {
    private final ZoneRenderer renderer;

    public AlwaysVisibleAction(boolean alwaysShow, ZoneRenderer renderer) {
      super(I18N.getText("token.popup.menu.always.visible"));
      this.renderer = renderer;
    }

    public void actionPerformed(ActionEvent e) {
      for (GUID guid : selectedTokenSet) {
        Zone zone = renderer.getZone();
        Token token = zone.getToken(guid);
        if (token != null) token.toggleIsAlwaysVisible();
      }
    }
  }
}
