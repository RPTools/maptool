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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import net.miginfocom.swing.MigLayout;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.functions.TokenBarFunction;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ExposedAreaMetaData;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.library.token.LibTokenConverter;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.util.FunctionUtil;

public class TokenPopupMenu extends AbstractTokenPopupMenu {
  private static final long serialVersionUID = -622385975780832588L;

  public TokenPopupMenu(
      Set<GUID> selectedTokenSet, int x, int y, ZoneRenderer renderer, Token tokenUnderMouse) {
    super(selectedTokenSet, x, y, renderer, tokenUnderMouse);

    add(new SetFacingAction());
    add(new ClearFacingAction());
    add(new StartMoveAction());
    addOwnedItem(new ImpersonateAction());
    addOwnedItem(createSizeMenu());
    addOwnedItem(createMacroMenu());
    addOwnedItem(createSpeechMenu());
    addOwnedItem(createStateMenu());
    addOwnedItem(createBarMenu());
    addOwnedItem(createInitiativeMenu());
    if (MapTool.getFrame().getInitiativePanel().hasOwnerPermission(tokenUnderMouse))
      add(new ChangeInitiativeState("initiative.menu.addToInitiative"));
    addOwnedItem(createFlipMenu());
    if (getTokenUnderMouse().getCharsheetImage() != null
        && AppUtil.playerOwns(getTokenUnderMouse())) {
      add(new ShowHandoutAction());
    }
    add(createHaloMenu());
    addOwnedItem(createArrangeMenu());
    addGMItem(createChangeToMenu(Zone.Layer.values()));
    add(new JSeparator());

    /*
     * This adds the expose menu to token right click when the player is GM and the server setting is set to use individual FOW
     */
    if (MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isUseIndividualFOW()) {
      add(createExposedFOWMenu());
    }
    if (MapTool.getPlayer().isGM() || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
      add(createExposeMenu());
      // if (MapTool.getPlayer().isGM()) {
      // addGMItem(createVisionMenu());
      // }
      // add(new JSeparator());
    }
    addOwnedItem(createLightSourceMenu());
    add(new JSeparator());

    addToggledItem(new ShowPathsAction(), renderer.isPathShowing(tokenUnderMouse));
    addToggledItem(
        new SnapToGridAction(tokenUnderMouse.isSnapToGrid(), renderer),
        tokenUnderMouse.isSnapToGrid());
    addToggledGMItem(new VisibilityAction(), tokenUnderMouse.isVisible());
    addToggledGMItem(
        new AlwaysVisibleAction(tokenUnderMouse.isAlwaysVisible(), renderer),
        tokenUnderMouse.isAlwaysVisible());

    add(new JSeparator());

    add(new JMenuItem(new CutAction()));
    add(new JMenuItem(new CopyAction()));
    add(new JMenuItem(new DeleteAction()));
    add(new JSeparator());

    add(new RevertLastMoveAction());
    if (selectedTokenSet.size() == 1) {
      if (getTokenUnderMouse().getName().toLowerCase().startsWith("lib:")) {
        add(new ExportLibTokenAsAddOnAction(getTokenUnderMouse()));
      }
    }
    add(new ShowPropertiesDialogAction().asJMenuItem());
    addOwnedItem(new SaveAction());
  }

  protected JMenu createMacroMenu() {
    if (selectedTokenSet.size() != 1 || getTokenUnderMouse().getMacroNames(true).size() == 0) {
      return null;
    }
    JMenu macroMenu = new JMenu("Macros");
    List<MacroButtonProperties> macroList = getTokenUnderMouse().getMacroList(true);
    String group = "";
    Collections.sort(macroList);
    Map<String, JMenu> groups = new TreeMap<String, JMenu>();
    for (MacroButtonProperties macro : macroList) {
      group = macro.getGroup();
      group = (group.isEmpty() ? " General" : group); // leading space makes it come first
      JMenu submenu = groups.get(group);
      if (submenu == null) {
        submenu = new JMenu(group);
        groups.put(group, submenu);
      }
      submenu.add(new RunMacroAction(macro.getLabel(), macro));
    }
    // Add the group menus in alphabetical order
    for (JMenu submenu : groups.values()) macroMenu.add(submenu);

    return macroMenu;
  }

  protected JMenu createSpeechMenu() {
    if (selectedTokenSet.size() != 1 || getTokenUnderMouse().getSpeechNames().size() == 0) {
      return null;
    }
    JMenu menu = new JMenu("Speech");
    List<String> keyList = new ArrayList<String>(getTokenUnderMouse().getSpeechNames());
    Collections.sort(keyList);
    for (String key : keyList) {
      menu.add(new SayAction(key, getTokenUnderMouse().getSpeech(key)));
    }
    return menu;
  }

  private JMenu createExposeMenu() {
    JMenu menu = new JMenu(I18N.getText("token.popup.menu.fow.expose"));
    menu.add(new ExposeVisibleAreaAction());
    menu.add(new ExposeLastPathAction());
    if (MapTool.getPlayer().getRole() == Role.GM) {
      menu.add(new ExposeVisibleAreaOnlyAction());
    }
    menu.setEnabled(getTokenUnderMouse().getHasSight());
    return menu;
  }

  private JMenu createExposedFOWMenu() {
    String viewMenu = I18N.getText("token.popup.menu.fow");
    JMenu menu = new JMenu(viewMenu);
    // menu.add(new AddGlobalExposedAreaAction());
    menu.add(new AddPartyExposedAreaAction());

    Zone zone = getRenderer().getZone();
    List<Token> tokens = zone.getTokensOnLayer(Zone.Layer.TOKEN);
    if (tokens != null && !tokens.isEmpty()) {
      String tokenViewMenu = I18N.getText("token.popup.menu.fow.tokens");
      JMenu subMenu = new JMenu(tokenViewMenu);
      int subItemCount = 0;
      for (Token tok : tokens) {
        if (tok.getHasSight() && tok.isVisible()) {
          ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tok.getExposedAreaGUID());
          if (!meta.getExposedAreaHistory().isEmpty()) {
            subMenu.add(new AddTokensExposedAreaAction(tok.getId()));
            subItemCount++;
          }
        }
      }
      if (subItemCount != 0) {
        menu.add(subMenu);
      }
    }
    menu.addSeparator();
    menu.add(new ClearSelectedExposedAreaAction());
    return menu;
  }

  private class AddTokensExposedAreaAction extends AbstractAction {
    private static final long serialVersionUID = 8452765509474109699L;

    private final GUID tokID;

    public AddTokensExposedAreaAction(GUID theTokId) {
      tokID = theTokId;
      Token sourceToken = getRenderer().getZone().getToken(tokID);
      String tokensView = I18N.getText("token.popup.menu.fow.tokens.view", sourceToken.getName());
      I18N.setAction(tokensView, this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = getRenderer().getZone();
      Token sourceToken = zone.getToken(tokID);
      ExposedAreaMetaData sourceMeta =
          zone.getExposedAreaMetaData(sourceToken.getExposedAreaGUID());
      for (GUID tok : selectedTokenSet) {
        Token targetToken = zone.getToken(tok);
        ExposedAreaMetaData targetMeta =
            zone.getExposedAreaMetaData(targetToken.getExposedAreaGUID());
        targetMeta.addToExposedAreaHistory(sourceMeta.getExposedAreaHistory());
        getRenderer().flush(targetToken);
        zone.setExposedAreaMetaData(targetToken.getExposedAreaGUID(), targetMeta);
        MapTool.serverCommand()
            .updateExposedAreaMeta(zone.getId(), targetToken.getExposedAreaGUID(), targetMeta);
      }
      getRenderer().repaint();
    }
  }

  /**
   * XXX If this object is supposed to merge all exposed areas together and apply that to the
   * currently selected tokens, why is it using a nested loop? Should one loop be used to create the
   * exposed area object, then a second (non-nested) loop be used to modify the exposed area of all
   * selected tokens?
   */
  private class AddPartyExposedAreaAction extends AbstractAction {
    private static final long serialVersionUID = 3672180436608883849L;

    public AddPartyExposedAreaAction() {
      I18N.setAction("token.popup.menu.fow.party", this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ZoneRenderer renderer = getRenderer();
      Zone zone = renderer.getZone();

      // This is ALL tokens; perhaps it should be owned tokens? Or just PC tokens? Or only those
      // with HasSight?
      // Or can players not use this feature at all so the above don't matter?
      List<Token> allToks = zone.getTokensOnLayer(Zone.Layer.TOKEN);

      // First create an Area that includes the exposed areas of all tokens
      Area tokenArea = new Area();
      for (Token tokenSource : allToks) {
        ExposedAreaMetaData sourceMeta =
            zone.getExposedAreaMetaData(tokenSource.getExposedAreaGUID());
        tokenArea.add(sourceMeta.getExposedAreaHistory());
      }
      // Now go back and add that Area to all selected tokens
      for (GUID tok : selectedTokenSet) {
        Token token = zone.getToken(tok);
        GUID tGUID = token.getExposedAreaGUID();
        ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tGUID);
        meta.addToExposedAreaHistory(tokenArea);
        renderer.flush(token);
        zone.setExposedAreaMetaData(tGUID, meta);
        MapTool.serverCommand().updateExposedAreaMeta(zone.getId(), tGUID, meta);
      }
      renderer.repaint();
    }
  }

  @SuppressWarnings("unused")
  private class AddGlobalExposedAreaAction extends AbstractAction {
    private static final long serialVersionUID = -3558008167872719635L;

    public AddGlobalExposedAreaAction() {
      I18N.setAction("token.popup.menu.fow.global", this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = getRenderer().getZone();
      Area area = zone.getExposedArea();
      for (GUID tok : selectedTokenSet) {
        Token token = zone.getToken(tok);
        ExposedAreaMetaData meta = zone.getExposedAreaMetaData(token.getExposedAreaGUID());
        meta.addToExposedAreaHistory(area);
        getRenderer().flush(token);
        zone.setExposedAreaMetaData(token.getExposedAreaGUID(), meta);
        MapTool.serverCommand()
            .updateExposedAreaMeta(zone.getId(), token.getExposedAreaGUID(), meta);
      }
      getRenderer().repaint();
    }
  }

  private class ClearSelectedExposedAreaAction extends AbstractAction {
    private static final long serialVersionUID = 7969000504336361693L;

    public ClearSelectedExposedAreaAction() {
      I18N.setAction("token.popup.menu.fow.clearselected", this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (MapTool.getServerPolicy().isUseIndividualFOW()) {
        Zone zone = getRenderer().getZone();
        for (GUID tok : selectedTokenSet) {
          Token token = zone.getToken(tok);
          ExposedAreaMetaData meta = zone.getExposedAreaMetaData(token.getExposedAreaGUID());
          meta.clearExposedAreaHistory();
          getRenderer().flush(token);
          zone.setExposedAreaMetaData(token.getExposedAreaGUID(), meta);
          MapTool.serverCommand()
              .updateExposedAreaMeta(zone.getId(), token.getExposedAreaGUID(), meta);
        }
      }
      getRenderer().repaint();
    }
  }

  private class ExposeVisibleAreaAction extends AbstractAction {
    private static final long serialVersionUID = 1773049658219864418L;

    public ExposeVisibleAreaAction() {
      I18N.setAction("token.popup.menu.expose.visible", this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      FogUtil.exposeVisibleArea(getRenderer(), selectedTokenSet, true);
      getRenderer().repaint();
    }
  }

  private class ExposeVisibleAreaOnlyAction extends AbstractAction {
    private static final long serialVersionUID = 7889640443069061220L;

    public ExposeVisibleAreaOnlyAction() {
      I18N.setAction("token.popup.menu.expose.currentonly", this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      FogUtil.exposePCArea(getRenderer());
    }
  }

  private class ExposeLastPathAction extends AbstractAction {
    private static final long serialVersionUID = 6840373835089920277L;

    public ExposeLastPathAction() {
      I18N.setAction("token.popup.menu.expose.lastpath", this, true);
      setEnabled(getTokenUnderMouse().getLastPath() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      FogUtil.exposeLastPath(getRenderer(), selectedTokenSet);
      getRenderer().repaint();
    }
  }

  protected JMenu createHaloMenu() {
    return createColorAreaMenu(
        "token.popup.menu.halo",
        getTokenUnderMouse().getHaloColor(),
        SetHaloAction.class,
        SetColorChooserAction.class);
  }

  private JMenu createColorAreaMenu(
      String title,
      Color selectedColor,
      Class<SetHaloAction> standardColorActionClass,
      Class<SetColorChooserAction> customColorActionClass) {
    JMenu haloMenu = new JMenu(I18N.getText(title));
    try {
      Constructor<SetHaloAction> standardColorActionConstructor =
          standardColorActionClass.getConstructor(
              TokenPopupMenu.class, ZoneRenderer.class, Set.class, Color.class, String.class);
      Constructor<SetColorChooserAction> customColorActionConstructor =
          customColorActionClass.getConstructor(
              TokenPopupMenu.class, ZoneRenderer.class, Set.class, String.class);

      JCheckBoxMenuItem noneMenu =
          new JCheckBoxMenuItem(
              standardColorActionConstructor.newInstance(
                  this, getRenderer(), selectedTokenSet, null, I18N.getText("Color.none")));
      JCheckBoxMenuItem customMenu =
          new JCheckBoxMenuItem(
              customColorActionConstructor.newInstance(
                  this, getRenderer(), selectedTokenSet, I18N.getText("Color.custom")));

      if (selectedColor == null) {
        noneMenu.setSelected(true);
      } else {
        customMenu.setSelected(true);
      }
      haloMenu.add(noneMenu);
      haloMenu.add(customMenu);
      haloMenu.add(new JSeparator());

      Set<String> colorNames = MapToolUtil.getColorNames();
      for (String name : colorNames) {
        Color bgColor = MapToolUtil.getColor(name);
        Color fgColor = ColorComboBoxRenderer.selectForegroundColor(bgColor);
        String displayName = I18N.getString("Color.".concat(name));
        if (displayName == null) {
          displayName = name;
        }
        JCheckBoxMenuItem item =
            new JCheckBoxMenuItem(
                standardColorActionConstructor.newInstance(
                    this, getRenderer(), selectedTokenSet, bgColor, displayName));

        if (bgColor.equals(selectedColor)) {
          item.setSelected(true);
          customMenu.setSelected(false);
        }
        haloMenu.add(item);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return haloMenu;
  }

  protected JMenu createBarMenu() {
    List<BarTokenOverlay> overlays =
        new ArrayList<BarTokenOverlay>(MapTool.getCampaign().getTokenBarsMap().values());
    if (overlays.isEmpty()) {
      return null;
    }
    JMenu stateMenu = I18N.createMenu("defaultTool.barMenu");
    overlays.sort(BarTokenOverlay.COMPARATOR);
    for (BarTokenOverlay overlay : overlays) {
      createBarItem(overlay.getName(), stateMenu, getTokenUnderMouse());
    } // endfor
    return stateMenu;
  }

  protected JMenu createStateMenu() {
    // Create the base menu
    JMenu stateMenu = I18N.createMenu("defaultTool.stateMenu");
    stateMenu.add(new ChangeStateAction("clear"));
    stateMenu.addSeparator();
    List<BooleanTokenOverlay> overlays =
        new ArrayList<BooleanTokenOverlay>(MapTool.getCampaign().getTokenStatesMap().values());
    overlays.sort(BooleanTokenOverlay.COMPARATOR);

    // Create the group menus first so that they can be placed at the top of the state menu
    Map<String, JMenu> groups = new TreeMap<String, JMenu>();
    for (BooleanTokenOverlay overlay : overlays) {
      String group = overlay.getGroup();
      if (group != null && (group = group.trim()).length() != 0) {
        JMenu menu = groups.get(group);
        if (menu == null) {
          menu = new JMenu(group);
          groups.put(group, menu);
        } // endif
      } // endif
    } // endfor

    // Add the group menus in alphabetical order
    for (JMenu menu : groups.values()) stateMenu.add(menu);

    // Give each overlay a button in the proper menu
    for (BooleanTokenOverlay overlay : overlays) {
      String group = overlay.getGroup();
      JMenu menu = stateMenu;
      if (group != null && (group = group.trim()).length() != 0) menu = groups.get(group);
      createStateItem(overlay.getName(), menu, getTokenUnderMouse());
    } // endfor
    return stateMenu;
  }

  private JMenu createInitiativeMenu() {
    JMenu initiativeMenu = I18N.createMenu("initiative.menu");
    boolean isOwner =
        MapTool.getFrame().getInitiativePanel().hasOwnerPermission(getTokenUnderMouse());
    if (isOwner) {
      initiativeMenu.add(new ChangeInitiativeState("initiative.menu.add"));
      initiativeMenu.add(new ChangeInitiativeState("initiative.menu.remove"));
      initiativeMenu.addSeparator();
    } // endif
    initiativeMenu.add(new JMenuItem(new ChangeInitiativeState("initiative.menu.resume")));
    initiativeMenu.add(new JMenuItem(new ChangeInitiativeState("initiative.menu.hold")));
    initiativeMenu.addSeparator();
    initiativeMenu.add(new JMenuItem(new ChangeInitiativeState("initiative.menu.setState")));
    initiativeMenu.add(new JMenuItem(new ChangeInitiativeState("initiative.menu.clearState")));

    // Enable by state if only one token selected.
    if (selectedTokenSet.size() == 1) {
      List<Integer> list =
          MapTool.getFrame().getInitiativePanel().getList().indexOf(getTokenUnderMouse());
      int index = list.isEmpty() ? -1 : list.get(0);
      if (index >= 0) {
        if (isOwner) initiativeMenu.getMenuComponent(0).setEnabled(false);
        boolean hold =
            MapTool.getFrame().getInitiativePanel().getList().getTokenInitiative(index).isHolding();
        if (hold) {
          initiativeMenu.getMenuComponent(isOwner ? 4 : 1).setEnabled(false);
        } else {
          initiativeMenu.getMenuComponent(isOwner ? 3 : 0).setEnabled(false);
        }
      } else {
        if (isOwner) initiativeMenu.getMenuComponent(1).setEnabled(false);
        initiativeMenu.getMenuComponent(isOwner ? 4 : 3).setEnabled(false);
        initiativeMenu.getMenuComponent(isOwner ? 3 : 0).setEnabled(false);
      } // endif
    } // endif
    return initiativeMenu;
  }

  protected void addOwnedToggledItem(Action action, boolean checked) {
    if (action == null) {
      return;
    }
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
    item.setSelected(checked);
    item.setEnabled(tokensAreOwned());
    add(item);
  }

  @Override
  public void showPopup(JComponent component) {
    show(component, x, y);
  }

  @SuppressWarnings("unused")
  private class PlayerOwnershipMenu extends JCheckBoxMenuItem implements ActionListener {
    private static final long serialVersionUID = -6109869878632628827L;

    private final Set<GUID> tokenSet;
    private final Zone zone;
    private final boolean selected;
    private final String name;

    public PlayerOwnershipMenu(String name, boolean selected, Set<GUID> tokenSet, Zone zone) {
      super(name, selected);
      this.tokenSet = tokenSet;
      this.zone = zone;
      this.selected = selected;
      this.name = name;

      addActionListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
      for (GUID guid : tokenSet) {
        Token token = zone.getToken(guid);

        if (selected) {
          for (Player player : MapTool.getPlayerList()) {
            token.addOwner(player.getName());
          }
          token.removeOwner(name);
        } else {
          token.addOwner(name);
        }
        MapTool.serverCommand().putToken(zone.getId(), token);
      }
      MapTool.getFrame().updateTokenTree();
    }
  }

  /**
   * Create a radio button menu item for a particuar state
   *
   * @param state Create the item for this state
   * @param menu The menu containing all items.
   * @return A menu item for the passed state.
   */
  private JCheckBoxMenuItem createStateItem(String state, JMenu menu, Token token) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(new ChangeStateAction(state));
    Object value = token.getState(state);
    if (FunctionUtil.getBooleanValue(value)) item.setSelected(true);
    menu.add(item);
    return item;
  }

  private JMenuItem createBarItem(String bar, JMenu menu, Token token) {
    JMenuItem item = new JMenuItem(new ChangeBarAction(bar));
    Object value = token.getState(bar);
    int percent = (int) (TokenBarFunction.getBigDecimalValue(value).doubleValue() * 100);
    item.setText(bar + " (" + percent + "%)");
    menu.add(item);
    return item;
  }

  private class SetHaloAction extends AbstractAction {
    private static final long serialVersionUID = 936075111485618012L;

    protected Color color;
    protected Set<GUID> tokenSet;
    protected ZoneRenderer renderer;

    public SetHaloAction(ZoneRenderer renderer, Set<GUID> tokenSet, Color color, String name) {
      this.color = color;
      this.tokenSet = tokenSet;
      this.renderer = renderer;

      putValue(Action.NAME, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = renderer.getZone();
      for (GUID guid : tokenSet) {
        Token token = zone.getToken(guid);

        if (!AppUtil.playerOwns(token)) {
          continue;
        }
        updateToken(token, color);
        MapTool.serverCommand().putToken(zone.getId(), token);
      }
      MapTool.getFrame().updateTokenTree();
      renderer.repaint();
    }

    protected void updateToken(Token token, Color color) {
      token.setHaloColor(color);
    }
  }

  private class SetColorChooserAction extends AbstractAction {
    private static final long serialVersionUID = 2212977067043864272L;

    protected Color currentColor;
    protected Set<GUID> tokenSet;
    protected ZoneRenderer renderer;

    // private final String title = "Choose Halo Color";

    public SetColorChooserAction(ZoneRenderer renderer, Set<GUID> tokenSet, String name) {
      this.tokenSet = tokenSet;
      this.renderer = renderer;
      this.currentColor = renderer.getZone().getToken(tokenSet.iterator().next()).getHaloColor();
      putValue(Action.NAME, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Color color = showColorChooserDialog();
      if (color != null) {
        Zone zone = renderer.getZone();
        for (GUID guid : tokenSet) {
          Token token = zone.getToken(guid);

          if (!AppUtil.playerOwns(token)) {
            continue;
          }
          updateToken(token, color);
          MapTool.serverCommand().putToken(zone.getId(), token);
        }
        MapTool.getFrame().updateTokenTree();
        renderer.repaint();
      }
    }

    protected Color showColorChooserDialog() {
      return JColorChooser.showDialog(
          MapTool.getFrame().getContentPane(), "Choose Halo Color", currentColor);
    }

    protected void updateToken(Token token, Color color) {
      token.setHaloColor(color);
    }
  }

  @SuppressWarnings("unused")
  private class SetVisionOverlayColorChooserAction extends SetColorChooserAction {
    private static final long serialVersionUID = 5809668032069953020L;

    public SetVisionOverlayColorChooserAction(
        ZoneRenderer renderer, Set<GUID> tokenSet, String name) {
      super(renderer, tokenSet, name);
      this.currentColor =
          renderer.getZone().getToken(tokenSet.iterator().next()).getVisionOverlayColor();
    }

    @Override
    protected Color showColorChooserDialog() {
      return JColorChooser.showDialog(
          MapTool.getFrame().getContentPane(), "Choose Vision Overlay Color", currentColor);
    }

    @Override
    protected void updateToken(Token token, Color color) {
      token.setVisionOverlayColor(color);
    }
  }

  @SuppressWarnings("unused")
  private class SetVisionOverlayColorAction extends SetHaloAction {
    private static final long serialVersionUID = 5116100872119403176L;

    public SetVisionOverlayColorAction(
        ZoneRenderer renderer, Set<GUID> tokenSet, Color color, String name) {
      super(renderer, tokenSet, color, name);
    }

    @Override
    protected void updateToken(Token token, Color color) {
      token.setVisionOverlayColor(color);
    }
  }

  private class ChangeBarAction extends AbstractAction {
    private static final long serialVersionUID = 3992963841229973540L;

    public ChangeBarAction(String bar) {
      putValue(ACTION_COMMAND_KEY, bar);
      putValue(NAME, bar);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String name = (String) getValue(NAME);
      JSlider slider = new JSlider(0, 100);
      JCheckBox hide = new JCheckBox(I18N.getText("EditTokenDialog.checkbox.state.hide"));
      hide.putClientProperty("JSlider", slider);
      hide.addChangeListener(
          e1 -> {
            JSlider js = (JSlider) ((JCheckBox) e1.getSource()).getClientProperty("JSlider");
            js.setEnabled(!((JCheckBox) e1.getSource()).isSelected());
          });
      slider.setPaintLabels(true);
      slider.setPaintTicks(true);
      slider.setMajorTickSpacing(20);
      slider.createStandardLabels(20);
      slider.setMajorTickSpacing(10);
      if (getTokenUnderMouse().getState(name) == null) {
        hide.setSelected(true);
        slider.setEnabled(false);
        slider.setValue(100);
      } else {
        hide.setSelected(false);
        slider.setEnabled(true);
        slider.setValue(
            (int)
                (TokenBarFunction.getBigDecimalValue(getTokenUnderMouse().getState(name))
                        .doubleValue()
                    * 100));
      }

      JPanel barPanel = new JPanel(new MigLayout("wrap 2"));
      barPanel.add(new JLabel(name + ":"));
      barPanel.add(slider, "span 1 2");
      barPanel.add(hide);

      if (JOptionPane.showOptionDialog(
              MapTool.getFrame(),
              barPanel,
              "Set " + name + " Value",
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.PLAIN_MESSAGE,
              null,
              null,
              null)
          == JOptionPane.OK_OPTION) {
        Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
        for (GUID tokenGUID : selectedTokenSet) {
          Token token = zone.getToken(tokenGUID);
          BigDecimal val = hide.isSelected() ? null : new BigDecimal(slider.getValue() / 100.0);
          token.setState(name, val);
          MapTool.serverCommand().putToken(zone.getId(), token);
        }
      }
    }
  }

  /** Internal class used to handle token state changes. */
  private class ChangeStateAction extends AbstractAction {
    private static final long serialVersionUID = 8403066587828844564L;

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
    @Override
    public void actionPerformed(ActionEvent aE) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      for (GUID tokenGUID : selectedTokenSet) {

        Token token = renderer.getZone().getToken(tokenGUID);
        if (aE.getActionCommand().equals("clear")) {
          for (String state : MapTool.getCampaign().getTokenStatesMap().keySet())
            token.setState(state, null);
        } else {
          token.setState(
              aE.getActionCommand(),
              ((JCheckBoxMenuItem) aE.getSource()).isSelected() ? Boolean.TRUE : null);
        } // endif
        MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
      } // endfor
      renderer.repaint();
    }
  }

  private class ChangeInitiativeState extends AbstractAction {
    private static final long serialVersionUID = -5968571073361988758L;

    String name;

    public ChangeInitiativeState(String aName) {
      name = aName;
      I18N.setAction(aName, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = getRenderer().getZone();
      InitiativeList init = MapTool.getFrame().getInitiativePanel().getList();
      String input = null;
      if (name.equals("initiative.menu.setState")) {
        input = JOptionPane.showInputDialog(I18N.getText("initiative.menu.enterState"));
        if (input == null) return;
        input = input.trim();
      } // endif
      for (GUID id : selectedTokenSet) {
        Token token = zone.getToken(id);
        Integer[] list = init.indexOf(token).toArray(new Integer[0]);
        if (name.equals("initiative.menu.add") || name.equals("initiative.menu.addToInitiative")) {
          init.insertToken(-1, token);
        } else {
          for (int i = list.length - 1; i >= 0; i--) {
            int index = list[i];
            if (index == -1) {
              continue;
            }
            if (name.equals("initiative.menu.remove")) {
              init.removeToken(index);
            } else if (name.equals("initiative.menu.hold")) {
              init.getTokenInitiative(index).setHolding(true);
            } else if (name.equals("initiative.menu.resume")) {
              init.getTokenInitiative(index).setHolding(false);
            } else if (name.equals("initiative.menu.setState")) {
              init.getTokenInitiative(index).setState(input);
            } else if (name.equals("initiative.menu.clearState")) {
              init.getTokenInitiative(index).setState(null);
            } // endif
          } // endif
        } // endfor
      } // endfor
    }
  }

  @SuppressWarnings("unused")
  private class AllOwnershipAction extends AbstractAction {
    private static final long serialVersionUID = -2995489619896660807L;

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = getRenderer().getZone();

      for (GUID tokenGUID : selectedTokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token != null) {
          token.setOwnedByAll(true);
          MapTool.serverCommand().putToken(zone.getId(), token);
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private class RemoveAllOwnershipAction extends AbstractAction {
    private static final long serialVersionUID = -6767778461889310579L;

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = getRenderer().getZone();

      for (GUID tokenGUID : selectedTokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token != null) {
          token.clearAllOwners();
          MapTool.serverCommand().putToken(zone.getId(), token);
        }
      }
    }
  }

  private class ShowPathsAction extends AbstractAction {
    private static final long serialVersionUID = 5704307506738212375L;

    public ShowPathsAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.move.path"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = getRenderer().getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        getRenderer().showPath(token, !getRenderer().isPathShowing(getTokenUnderMouse()));
      }
      getRenderer().repaint();
    }
  }

  private class RevertLastMoveAction extends AbstractAction {
    private static final long serialVersionUID = 8967703198797674025L;

    public RevertLastMoveAction() {
      putValue(Action.NAME, I18N.getText("token.popup.menu.move.revertlast"));

      // Only available if there is a last move
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = getRenderer().getZone().getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        if (token.getLastPath() == null) {
          setEnabled(false);
          break;
        }
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Zone zone = getRenderer().getZone();
      for (GUID tokenGUID : selectedTokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token == null) {
          continue;
        }
        Path<?> path = token.getLastPath();
        if (path == null) {
          continue;
        }
        // Get the start cell of the last move
        // TODO: I don't like this hard wiring, find a better way
        ZonePoint zp = null;
        if (path.getCellPath().get(0) instanceof CellPoint) {
          zp = zone.getGrid().convert((CellPoint) path.getCellPath().get(0));
        } else {
          zp = (ZonePoint) path.getCellPath().get(0);
        }
        // Relocate
        token.setX(zp.x);
        token.setY(zp.y);

        // Do it again to cancel out the last move position
        token.setX(zp.x);
        token.setY(zp.y);

        // No more last path
        token.setLastPath(null);

        MapTool.serverCommand().putToken(zone.getId(), token);
      }
      getRenderer().repaint();
    }
  }

  private class ExportLibTokenAsAddOnAction extends AbstractAction {

    private final Token libToken;

    ExportLibTokenAsAddOnAction(Token libToken) {
      putValue(Action.NAME, I18N.getString("token.popup.menu.export.libTokenAddon"));
      this.libToken = libToken;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MapTool.showInformation("library.export.information");
      var dirChooser = new JFileChooser();
      dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int option = dirChooser.showSaveDialog(MapTool.getFrame());
      if (option == JFileChooser.APPROVE_OPTION) {
        File file = dirChooser.getSelectedFile();
        if (!file.exists()) {
          file.mkdirs();
        }
        boolean empty = false;
        try (var directory = Files.newDirectoryStream(file.toPath())) {
          empty = !directory.iterator().hasNext();
        } catch (IOException ie) {
          MapTool.showError(
              I18N.getText("library.export.errorReadingDirectory", file.getAbsolutePath()), ie);
        }
        if (!empty) {
          MapTool.showInformation(
              I18N.getText("library.export.error.overwrite", file.getAbsolutePath()));
          return;
        }
        new LibTokenConverter(file, libToken).convert();
      }
    }
  }

  public class RunMacroAction extends AbstractAction {
    private static final long serialVersionUID = -5836981653612993828L;

    private final MacroButtonProperties macro;

    public RunMacroAction(String key, MacroButtonProperties macro) {
      putValue(Action.NAME, key);
      this.macro = macro;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Set<Token> guidSet = new HashSet<Token>();
      for (GUID tokenID : selectedTokenSet) {
        guidSet.add(getRenderer().getZone().getToken(tokenID));
      }
      macro.executeMacro(guidSet);
    }
  }

  public class SayAction extends AbstractAction {
    private static final long serialVersionUID = -4560161692286043464L;
    private final String speech;

    public SayAction(String key, String speech) {
      putValue(Action.NAME, key);
      this.speech = speech;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String identity = getTokenUnderMouse().getName();
      MapTool.getFrame().getCommandPanel().commitCommand("/im " + identity + ":" + speech);
    }
  }
}
