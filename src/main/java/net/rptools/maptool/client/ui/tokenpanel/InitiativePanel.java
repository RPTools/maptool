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
package net.rptools.maptool.client.ui.tokenpanel;

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.InitiativeListModel;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.zones.InitiativeListChanged;
import net.rptools.maptool.model.zones.TokensAdded;
import net.rptools.maptool.model.zones.TokensChanged;
import net.rptools.maptool.model.zones.TokensRemoved;

/**
 * This panel shows the initiative order inside of MapTools.
 *
 * @author Jay
 */
public class InitiativePanel extends JPanel
    implements PropertyChangeListener, ListSelectionListener {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Model containing all of the tokens in this initiative. */
  private InitiativeList list;

  /** The model used to display a list in the panel; */
  private final InitiativeListModel model;

  /** Component that displays the round */
  private final JLabel round;

  /** Component that displays the initiative list. */
  private final JList<TokenInitiative> displayList;

  /** Flag indicating that token images are shown in the list. */
  private boolean showTokens = AppPreferences.getInitShowTokens();

  /**
   * Flag indicating that token states are shown in the list. Only valid if {@link #showTokens} is
   * <code>true</code>.
   */
  private boolean showTokenStates = AppPreferences.getInitShowTokenStates();

  /** Flag indicating that initiative state is shown in the list. */
  private boolean showInitState = AppPreferences.getInitShowInitiative();

  /**
   * Flag indicating that two lines are used for initiative stated. It is only valid if {@link
   * #showInitState} is <code>true</code>.
   */
  private boolean initStateSecondLine = AppPreferences.getInitShow2ndLine();

  /** The zone data being displayed. */
  private Zone zone;

  /** The component that contains the initiative menu. */
  private final JPopupMenu popupMenu;

  /** The menu item that tells the GM if NPC's are visible. */
  private JCheckBoxMenuItem hideNPCMenuItem;

  /**
   * The menu item that tells the GM if players can change the initiative when working with tokens
   * they own.
   */
  private JCheckBoxMenuItem ownerPermissionsMenuItem;

  /**
   * The menu item that tells the GM if players can only move their tokens when it is their turn.
   */
  private JCheckBoxMenuItem movementLockMenuItem;

  /**
   * Flag indicating that the owners of tokens have been granted permission to restricted actions
   * when they own the token.
   */
  private boolean ownerPermissions;

  /** Flag indicating that the owners of tokens can only move their tokens when it is their turn. */
  private boolean movementLock;

  /** Whether the {@link #SORT_LIST_ACTION} should use the reversed (Ascending) order */
  private boolean initUseReverseSort;

  /**
   * The Next/Previous buttons can be disabled to prevent bypass of a framework's custom initiative
   * functions
   */
  private boolean initPanelButtonsDisabled;

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/
  /** Setup the menu */
  public InitiativePanel() {

    // Build the form and add it's component
    setLayout(new BorderLayout());

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    add(toolBar, BorderLayout.NORTH);

    popupMenu = new JPopupMenu();
    toolBar.add(
        SwingUtil.makePopupMenuButton(
            new JButton(RessourceManager.getSmallIcon(Icons.ACTION_SETTINGS)),
            () -> popupMenu,
            false));

    toolBar.add(new TextlessButton(PREV_ACTION));
    toolBar.add(new TextlessButton(TOGGLE_HOLD_ACTION));
    toolBar.add(new TextlessButton(NEXT_ACTION));
    toolBar.add(new TextlessButton(RESET_COUNTER_ACTION));

    round = new JLabel("", SwingConstants.LEFT);
    toolBar.add(Box.createHorizontalGlue());
    toolBar.add(round);
    toolBar.add(Box.createHorizontalStrut(8));

    // ensure that the preferred width is enough to show the round counter in fullscreen
    round.setText(I18N.getText("initPanel.round") + "WWW");
    round.setPreferredSize(round.getMinimumSize());
    round.setText("");

    ownerPermissions = MapTool.getCampaign().isInitiativeOwnerPermissions();
    movementLock = MapTool.getCampaign().isInitiativeMovementLock();
    initUseReverseSort = MapTool.getCampaign().isInitiativeUseReverseSort();
    initPanelButtonsDisabled = MapTool.getCampaign().isInitiativePanelButtonsDisabled();

    // Set up the list with an empty model
    displayList = new JList<TokenInitiative>();
    model = new InitiativeListModel();
    displayList.setModel(model);
    setList(new InitiativeList(null));
    displayList.setCellRenderer(new InitiativeListCellRenderer(this));

    // Dragging is only for GM
    displayList.setTransferHandler(new InitiativeTransferHandler(this));
    displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    displayList.addListSelectionListener(this);
    displayList.addMouseListener(new MouseHandler());
    add(new JScrollPane(displayList), BorderLayout.CENTER);

    // Set the keyboard mapping
    InputMap imap = displayList.getInputMap();
    imap.put(KeyStroke.getKeyStroke("DELETE"), "REMOVE_TOKEN_ACTION");
    ActionMap map = displayList.getActionMap();
    map.put("REMOVE_TOKEN_ACTION", REMOVE_TOKEN_ACTION);

    // Set action text and icons
    PREV_ACTION.putValue(Action.SMALL_ICON, RessourceManager.getSmallIcon(Icons.ACTION_PREVIOUS));
    TOGGLE_HOLD_ACTION.putValue(
        Action.SMALL_ICON, RessourceManager.getSmallIcon(Icons.ACTION_PAUSE));
    NEXT_ACTION.putValue(Action.SMALL_ICON, RessourceManager.getSmallIcon(Icons.ACTION_NEXT));
    RESET_COUNTER_ACTION.putValue(
        Action.SMALL_ICON, RessourceManager.getSmallIcon(Icons.ACTION_RESET));

    I18N.setAction("initPanel.sort", SORT_LIST_ACTION);
    I18N.setAction("initPanel.toggleHold", TOGGLE_HOLD_ACTION);
    I18N.setAction("initPanel.makeCurrent", MAKE_CURRENT_ACTION);
    I18N.setAction("initPanel.setInitState", SET_INIT_STATE_VALUE);
    I18N.setAction("initPanel.clearInitState", CLEAR_INIT_STATE_VALUE);
    I18N.setAction("initPanel.showTokens", SHOW_TOKENS_ACTION);
    I18N.setAction("initPanel.showTokenStates", SHOW_TOKEN_STATES_ACTION);
    I18N.setAction("initPanel.showInitStates", SHOW_INIT_STATE);
    I18N.setAction("initPanel.initStateSecondLine", INIT_STATE_SECOND_LINE);
    I18N.setAction("initPanel.toggleReverseSort", TOGGLE_REVERSE_INIT_SORT_ORDER);
    I18N.setAction("initPanel.toggleHideNPCs", TOGGLE_HIDE_NPC_ACTION);
    I18N.setAction("initPanel.togglePanelButtonsDisabled", TOGGLE_PANEL_BUTTONS_DISABLED_ACTION);
    I18N.setAction("initPanel.addPCs", ADD_PCS_ACTION);
    I18N.setAction("initPanel.addAll", ADD_ALL_ACTION);
    I18N.setAction("initPanel.remove", REMOVE_TOKEN_ACTION);
    I18N.setAction("initPanel.removeAll", REMOVE_ALL_ACTION);
    I18N.setAction("initPanel.remove", REMOVE_TOKEN_ACTION);
    I18N.setAction("initPanel.toggleOwnerPermissions", TOGGLE_OWNER_PERMISSIONS_ACTION);
    I18N.setAction("initPanel.toggleMovementLock", TOGGLE_MOVEMENT_LOCK_ACTION);
    I18N.setAction("initPanel.round", RESET_COUNTER_ACTION);
    I18N.setAction("initPanel.next", NEXT_ACTION);
    I18N.setAction("initPanel.prev", PREV_ACTION);
    updateView();

    new MapToolEventBus().getMainEventBus().register(this);
  }

  private static class TextlessButton extends JButton {
    TextlessButton(Action action) {
      setHideActionText(true);
      setAction(action);
      getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
      getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none");
    }
  }
  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Update the view after the connection has been created. This allows the menus to be tailored for
   * GM's and Player's properly
   */
  public void updateView() {
    displayList.setDragEnabled(hasGMPermission());

    // Set up the buttons
    PREV_ACTION.setEnabled(hasGMPermission() && !isInitPanelButtonsDisabled());
    RESET_COUNTER_ACTION.setEnabled(hasGMPermission());
    NEXT_ACTION.setEnabled(
        !isInitPanelButtonsDisabled()
            && (hasGMPermission()
                || (ownerPermissions && hasOwnerPermission(list.getCurrentToken()))));

    // Set up the menu
    popupMenu.removeAll();
    if (hasGMPermission()) {
      popupMenu.add(new JMenuItem(SORT_LIST_ACTION));
      JCheckBoxMenuItem reverseSort = new JCheckBoxMenuItem(TOGGLE_REVERSE_INIT_SORT_ORDER);
      reverseSort.setSelected(initUseReverseSort);
      popupMenu.add(reverseSort);
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(MAKE_CURRENT_ACTION));
    } // endif
    popupMenu.add(new JMenuItem(SET_INIT_STATE_VALUE));
    popupMenu.add(new JMenuItem(CLEAR_INIT_STATE_VALUE));
    popupMenu.addSeparator();
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(SHOW_TOKENS_ACTION);
    item.setSelected(showTokens);
    popupMenu.add(item);
    item = new JCheckBoxMenuItem(SHOW_TOKEN_STATES_ACTION);
    item.setSelected(showTokenStates);
    popupMenu.add(item);
    item = new JCheckBoxMenuItem(SHOW_INIT_STATE);
    item.setSelected(showInitState);
    popupMenu.add(item);
    item = new JCheckBoxMenuItem(INIT_STATE_SECOND_LINE);
    item.setSelected(initStateSecondLine);
    popupMenu.add(item);
    if (hasGMPermission()) {
      hideNPCMenuItem = new JCheckBoxMenuItem(TOGGLE_HIDE_NPC_ACTION);
      hideNPCMenuItem.setSelected(list != null && list.isHideNPC());
      popupMenu.add(hideNPCMenuItem);
      item = new JCheckBoxMenuItem(TOGGLE_PANEL_BUTTONS_DISABLED_ACTION);
      item.setSelected(initPanelButtonsDisabled);
      popupMenu.add(item);
      ownerPermissionsMenuItem = new JCheckBoxMenuItem(TOGGLE_OWNER_PERMISSIONS_ACTION);
      ownerPermissionsMenuItem.setSelected(list != null && ownerPermissions);
      popupMenu.add(ownerPermissionsMenuItem);
      movementLockMenuItem = new JCheckBoxMenuItem(TOGGLE_MOVEMENT_LOCK_ACTION);
      movementLockMenuItem.setSelected(list != null && movementLock);
      popupMenu.add(movementLockMenuItem);
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(ADD_PCS_ACTION));
      popupMenu.add(new JMenuItem(ADD_ALL_ACTION));
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(REMOVE_TOKEN_ACTION));
      popupMenu.add(new JMenuItem(REMOVE_ALL_ACTION));
    } else if (ownerPermissions) {
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(REMOVE_TOKEN_ACTION));
    } // endif
    valueChanged(null);
  }

  /** Remove all of the tokens from the model and clear round and current */
  public void clearTokens() {
    list.clearModel();
  }

  /** Update list containing tokens in initiative. Make sure the token references match the zone. */
  public void update() {
    list.update();
  }

  private void updateRound() {
    if (list.getRound() > 0) round.setText(I18N.getText("initPanel.round") + " " + list.getRound());
    else round.setText("");
  }

  /**
   * @return Getter for list
   */
  public InitiativeList getList() {
    return list;
  }

  /**
   * @param theList Setter for the list to set
   */
  public void setList(InitiativeList theList) {
    // Remove the old list
    if (list == theList) return;
    if (list != null) list.removePropertyChangeListener(this);

    // Add the new one
    list = theList;
    if (list != null) {
      list.addPropertyChangeListener(this);
      updateRound();
    }
    EventQueue.invokeLater(
        () -> {
          model.setList(list);
          NEXT_ACTION.setEnabled(
              !isInitPanelButtonsDisabled()
                  && (hasGMPermission() || hasOwnerPermission(list.getCurrentToken())));
          if (list.getCurrent() >= 0) {
            int index = model.getDisplayIndex(list.getCurrent());
            if (index >= 0) displayList.ensureIndexIsVisible(index);
          }
        });
  }

  /**
   * @return Getter for showTokens
   */
  public boolean isShowTokens() {
    return showTokens;
  }

  /**
   * @return Getter for showTokenStates
   */
  public boolean isShowTokenStates() {
    return showTokenStates;
  }

  /**
   * @return Getter for showInitState
   */
  public boolean isShowInitState() {
    return showInitState;
  }

  /**
   * @return Getter for model
   */
  public InitiativeListModel getModel() {
    return model;
  }

  /**
   * Set the zone that we are currently working on.
   *
   * @param aZone The new zone
   */
  public void setZone(Zone aZone) {
    // Clean up listeners
    if (aZone == zone) return;
    zone = aZone;

    // Older campaigns didn't have a list, make sure this one does
    InitiativeList list = (zone != null) ? zone.getInitiativeList() : new InitiativeList(null);
    if (list == null) {
      list = new InitiativeList(zone);
      zone.setInitiativeList(list);
    } // endif

    // Set the list and actions
    setList(list);
    displayList.getSelectionModel().clearSelection();
    updateView();
  }

  /**
   * See if the current player has permission to execute owner restricted actions.
   *
   * @param token Check this token's ownership. If this value is <code>null</code> then <code>false
   *     </code> is returned.
   * @return The value <code>true</code> if this player has permission for restricted actions.
   */
  public boolean hasOwnerPermission(Token token) {
    if (token == null) return false;
    if (hasGMPermission()) return true;
    if (ownerPermissions
        && (!MapTool.getServerPolicy().useStrictTokenManagement()
            || token.isOwner(MapTool.getPlayer().getName()))) return true;
    return false;
  }

  /**
   * See if the current player has permission to execute GM restricted actions. This is <b>not</b>
   * related to so-called <i>trusted macros</i> in MTscript.
   *
   * @return The value <code>true</code> if this player has permission for all actions.
   */
  public boolean hasGMPermission() {
    return (MapTool.getPlayer() == null || MapTool.getPlayer().isGM());
  }

  /**
   * @return Getter for ownerPermissions
   */
  public boolean isOwnerPermissions() {
    return ownerPermissions;
  }

  /**
   * @param anOwnerPermissions Setter for ownerPermissions
   */
  public void setOwnerPermissions(boolean anOwnerPermissions) {
    ownerPermissions = anOwnerPermissions;
    updateView();
  }

  /**
   * @return Getter for MovementLock
   */
  public boolean isMovementLock() {
    return movementLock;
  }

  /**
   * @param anMovementLock Setter for MovementLock
   */
  public void setMovementLock(boolean anMovementLock) {
    movementLock = anMovementLock;
  }

  public boolean isInitUseReverseSort() {
    return initUseReverseSort;
  }

  public void setInitUseReverseSort(boolean anInitUseReverseSort) {
    initUseReverseSort = anInitUseReverseSort;
  }

  public boolean isInitPanelButtonsDisabled() {
    return initPanelButtonsDisabled;
  }

  /**
   * Updates the "Disable Panel Buttons" setting, and tweaks the Next/Previous button tooltips
   * appropriately. Updates the view.
   *
   * @param initPanelButtonsDisabled
   */
  public void setInitPanelButtonsDisabled(boolean initPanelButtonsDisabled) {
    this.initPanelButtonsDisabled = initPanelButtonsDisabled;
    if (this.initPanelButtonsDisabled) {
      NEXT_ACTION.putValue(Action.SHORT_DESCRIPTION, I18N.getText("initPanel.buttonsAreDisabled"));
      PREV_ACTION.putValue(Action.SHORT_DESCRIPTION, I18N.getText("initPanel.buttonsAreDisabled"));
    } else {
      I18N.setAction("initPanel.next", NEXT_ACTION);
      I18N.setAction("initPanel.prev", PREV_ACTION);
    }
    updateView();
  }

  /**
   * Returns true if the passed token can not be moved because it is not the current token.
   *
   * @param token The token being checked.
   * @return <code>true</code> if token movement is locked.
   */
  public boolean isMovementLocked(Token token) {
    if (!movementLock || list == null || list.getSize() == 0) return false;
    if (model.getCurrentTokenInitiative() == null) return true;
    if (model.getCurrentTokenInitiative().getToken() == token) return false;
    return true;
  }

  /**
   * @return Getter for initStateSecondLine
   */
  public boolean isInitStateSecondLine() {
    return initStateSecondLine;
  }

  /**
   * @param initStateSecondLine Setter for initStateSecondLine
   */
  public void setInitStateSecondLine(boolean initStateSecondLine) {
    this.initStateSecondLine = initStateSecondLine;
  }

  @Subscribe
  private void onInitiativeListChanged(InitiativeListChanged event) {
    final var list = event.initiativeList();
    if (list.getZone() != zone) {
      return;
    }

    int oldSize = model.getSize();
    setList(list);
    if (oldSize != model.getSize()) displayList.getSelectionModel().clearSelection();
  }

  @Subscribe
  private void onTokensAdded(TokensAdded event) {
    if (event.zone() != zone) {
      return;
    }
    model.updateModel();
  }

  @Subscribe
  private void onTokensRemoved(TokensRemoved event) {
    if (event.zone() != zone) {
      return;
    }
    model.updateModel();
  }

  @Subscribe
  private void onTokensChanged(TokensChanged event) {
    if (event.zone() != zone) {
      return;
    }
    model.updateModel();
  }

  /*---------------------------------------------------------------------------------------------
   * ListSelectionListener Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e != null && e.getValueIsAdjusting()) return;
    TokenInitiative ti = displayList.getSelectedValue();
    boolean enabled = ti != null && hasOwnerPermission(ti.getToken());
    CLEAR_INIT_STATE_VALUE.setEnabled(enabled);
    SET_INIT_STATE_VALUE.setEnabled(enabled);
    TOGGLE_HOLD_ACTION.setEnabled(enabled);
    MAKE_CURRENT_ACTION.setEnabled(enabled && ti != list.getCurrentTokenInitiative());

    REMOVE_TOKEN_ACTION.setEnabled(enabled);
    NEXT_ACTION.setEnabled(
        !isInitPanelButtonsDisabled()
            && (hasGMPermission() || hasOwnerPermission(list.getCurrentToken())));
  }

  /*---------------------------------------------------------------------------------------------
   * PropertyChangeListener Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(InitiativeList.ROUND_PROP)) {
      updateRound();
    } else if (evt.getPropertyName().equals(InitiativeList.CURRENT_PROP)) {
      Token t = list.getCurrentToken();
      if (t == null) return;
      String s = I18N.getText("initPanel.displayMessage", t.getName());
      if (InitiativeListModel.isTokenVisible(t, list.isHideNPC())
          && t.getType() != Type.NPC
          && AppPreferences.isShowInitGainMessage()) MapTool.addMessage(TextMessage.say(null, s));
      displayList.ensureIndexIsVisible(model.getDisplayIndex(list.getCurrent()));
      NEXT_ACTION.setEnabled(
          !isInitPanelButtonsDisabled() && hasOwnerPermission(list.getCurrentToken()));
    } else if (evt.getPropertyName().equals(InitiativeList.TOKENS_PROP)) {
      if ((evt.getOldValue() == null && evt.getNewValue() instanceof TokenInitiative)
          || (evt.getNewValue() == null & evt.getOldValue() instanceof TokenInitiative))
        displayList.getSelectionModel().clearSelection();
    } else if (evt.getPropertyName().equals(InitiativeList.HIDE_NPCS_PROP)) {
      displayList.getSelectionModel().clearSelection();
    } else if (evt.getPropertyName().equals(InitiativeList.OWNER_PERMISSIONS_PROP)) {
      updateView();
    } // endif
  }

  /*---------------------------------------------------------------------------------------------
   * Menu Actions
   *-------------------------------------------------------------------------------------------*/

  /** This action will advance initiative to the next token in the list. */
  public final Action NEXT_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          list.nextInitiative();
        }
      };

  /** This action will reverse initiative to the previous token in the list. */
  public final Action PREV_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          list.prevInitiative();
        }
      };

  /** This action will remove the selected token from the list. */
  public final Action REMOVE_TOKEN_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          TokenInitiative ti = displayList.getSelectedValue();
          if (ti == null) return;
          int index = list.indexOf(ti);
          list.removeToken(index);
        }
      };

  /** This action will turn the selected token's initiative on and off. */
  public final Action TOGGLE_HOLD_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          TokenInitiative ti = displayList.getSelectedValue();
          if (ti == null) return;
          ti.setHolding(!ti.isHolding());
        }
      };

  /** This action will make the selected token the current token. */
  public final Action MAKE_CURRENT_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          TokenInitiative ti = displayList.getSelectedValue();
          if (ti == null) return;
          list.setCurrent(list.indexOf(ti));
        }
      };

  /** This action toggles the display of token images. */
  public final Action SHOW_TOKENS_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          showTokens = ((JCheckBoxMenuItem) e.getSource()).isSelected();
          displayList.setCellRenderer(
              new InitiativeListCellRenderer(
                  InitiativePanel.this)); // Regenerates the size of each row.
          AppPreferences.setInitShowTokens(showTokens);
        }
      };

  /** This action toggles the display of token images. */
  public final Action SHOW_TOKEN_STATES_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          showTokenStates = ((JCheckBoxMenuItem) e.getSource()).isSelected();
          displayList.setCellRenderer(
              new InitiativeListCellRenderer(
                  InitiativePanel.this)); // Regenerates the size of each row.
          AppPreferences.setInitShowTokenStates(showTokenStates);
        }
      };

  /** This action toggles the display of token images. */
  public final Action SHOW_INIT_STATE =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          showInitState = ((JCheckBoxMenuItem) e.getSource()).isSelected();
          displayList.setCellRenderer(
              new InitiativeListCellRenderer(
                  InitiativePanel.this)); // Regenerates the size of each row.
          AppPreferences.setInitShowInitiative(showInitState);
        }
      };

  /** This action toggles the display of token images. */
  public final Action INIT_STATE_SECOND_LINE =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          initStateSecondLine = ((JCheckBoxMenuItem) e.getSource()).isSelected();
          displayList.setCellRenderer(
              new InitiativeListCellRenderer(
                  InitiativePanel.this)); // Regenerates the size of each row.
          AppPreferences.setInitShow2ndLine(initStateSecondLine);
        }
      };

  /** This action sorts the tokens in the list. */
  public final Action SORT_LIST_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          list.sort(initUseReverseSort);
        }
      };

  /** Toggle the Use Reverse Sort Order preference */
  public final Action TOGGLE_REVERSE_INIT_SORT_ORDER =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          initUseReverseSort = ((JCheckBoxMenuItem) e.getSource()).isSelected();
          MapTool.getCampaign().setInitiativeUseReverseSort(initUseReverseSort);
          MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
        }
      };

  /** Enable/Disable the Next & Previous buttons on the Panel */
  public final Action TOGGLE_PANEL_BUTTONS_DISABLED_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // use the setter to make sure related changes are applied to the panel
          setInitPanelButtonsDisabled(((JCheckBoxMenuItem) e.getSource()).isSelected());
          MapTool.getCampaign().setInitiativePanelButtonsDisabled(isInitPanelButtonsDisabled());
          MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
        }
      };

  /** This action will set the initiative state of the currently selected token. */
  public final Action SET_INIT_STATE_VALUE =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          TokenInitiative ti = displayList.getSelectedValue();
          if (ti == null) return;
          Token token = ti.getToken();
          String sName = (token == null) ? "" : token.getName();
          if (hasGMPermission()
              && token != null
              && token.getGMName() != null
              && token.getGMName().trim().length() != 0)
            sName += " (" + token.getGMName().trim() + ")";
          String s = I18N.getText("initPanel.enterState", sName);
          String input = JOptionPane.showInputDialog(s, ti.getState());
          if (input == null) return;
          ti.setState(input.trim());
        }
      };

  /** This action will clear the initiative state of the currently selected token. */
  public final Action CLEAR_INIT_STATE_VALUE =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          TokenInitiative ti = displayList.getSelectedValue();
          if (ti == null) return;
          ti.setState(null);
        }
      };

  /** This action will remove all tokens from the initiative panel. */
  public final Action REMOVE_ALL_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          clearTokens();
        }
      };

  /** This action will add all tokens in the zone to this initiative panel. */
  public final Action ADD_ALL_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          list.insertTokens(list.getZone().getTokensOnLayer(Zone.Layer.TOKEN));
        }
      };

  /** This action will add all PC tokens in the zone to this initiative panel. */
  public final Action ADD_PCS_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          List<Token> tokens = new ArrayList<Token>();
          for (Token token : list.getZone().getTokensOnLayer(Zone.Layer.TOKEN)) {
            if (token.getType() == Type.PC) tokens.add(token);
          } // endfor
          list.insertTokens(tokens);
        }
      };

  /** This action will hide all initiative items with NPC tokens from players */
  public final Action TOGGLE_HIDE_NPC_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          list.setHideNPC(!list.isHideNPC());
          if (list.isHideNPC() != hideNPCMenuItem.isSelected())
            hideNPCMenuItem.setSelected(list.isHideNPC());
        }
      };

  /**
   * This action will toggle the flag that allows players to modify the init for tokens they own.
   */
  public final Action TOGGLE_OWNER_PERMISSIONS_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          boolean op = !MapTool.getCampaign().isInitiativeOwnerPermissions();
          if (ownerPermissionsMenuItem != null) ownerPermissionsMenuItem.setSelected(op);
          MapTool.getCampaign().setInitiativeOwnerPermissions(op);
          MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
        }
      };

  /**
   * This action will toggle the flag that allows players to only move tokens when it is their turn.
   */
  public final Action TOGGLE_MOVEMENT_LOCK_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          boolean mvLock = !MapTool.getCampaign().isInitiativeMovementLock();
          if (movementLockMenuItem != null) movementLockMenuItem.setSelected(mvLock);
          MapTool.getCampaign().setInitiativeMovementLock(mvLock);
          MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
        }
      };

  /** This action will reset the round counter for the initiative panel. */
  public final Action RESET_COUNTER_ACTION =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!MapTool.getPlayer().isGM()) {
            return;
          }

          list.startUnitOfWork();
          list.setRound(-1);
          list.setCurrent(-1);
          list.finishUnitOfWork();
        }
      };

  /*---------------------------------------------------------------------------------------------
   * DoubleClickHandler Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * Handle a double click and context menu on the list of the table.
   *
   * @author jgorrell
   * @version $Revision$ $Date$ $Author$
   */
  private class MouseHandler extends MouseAdapter {

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {

      if (model.getSize() == 0) {
        return;
      }

      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        SwingUtilities.invokeLater(
            () -> {
              if (displayList.getSelectedValue() != null) {
                // Show the selected token on the map.
                Token token = displayList.getSelectedValue().getToken();
                ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
                if (renderer == null
                    || token == null
                    || (token.getLayer() != Zone.Layer.TOKEN && !MapTool.getPlayer().isGM())
                    || !AppUtil.playerOwns(token)) {
                  return;
                }

                renderer.centerOnAndSetSelected(token);
                renderer.maybeForcePlayersView();
              }
            });
      } else if (SwingUtilities.isRightMouseButton(e)) {
        TokenInitiative ti =
            displayList.getModel().getElementAt(displayList.locationToIndex(e.getPoint()));
        if (ti == null) {
          return;
        }
        displayList.setSelectedIndex(model.getDisplayIndex(list.indexOf(ti)));
        // TODO Can I use hasOwnerPermission(ti.getToken()) here instead?
        if (!hasGMPermission()
            && ti.getToken() != null
            && !ti.getToken().isOwner(MapTool.getPlayer().getName())) return;
        Set<GUID> tokens = Collections.singleton(ti.getId());
        Set<TokenInitiative> tis = Collections.singleton(ti);
        new InitiativeTokenPopupMenu(
                tokens,
                tis,
                e.getX(),
                e.getY(),
                MapTool.getFrame().getCurrentZoneRenderer(),
                ti.getToken(),
                ti)
            .showPopup(displayList);
      } // endif
    }
  }
}
