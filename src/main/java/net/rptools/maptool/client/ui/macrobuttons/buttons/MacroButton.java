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
package net.rptools.maptool.client.ui.macrobuttons.buttons;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.macrobuttons.MacroButtonHotKeyManager;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.AbstractButtonGroup;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.ButtonGroup;
import net.rptools.maptool.client.ui.macrobuttons.panels.AbstractMacroPanel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

/**
 * <code>MacroButton</code>s that extend this class use {@link MacroButtonProperties}.
 *
 * <p>These buttons are used in Macro Button Panel in the UI.
 */
public class MacroButton extends JButton implements MouseListener {
  private final MacroButtonProperties properties;
  private final MacroButtonHotKeyManager hotKeyManager;
  private final ButtonGroup buttonGroup;
  private final AbstractMacroPanel panel;
  private final String panelClass;
  private final GUID tokenId;
  private static final Insets buttonInsets = new Insets(2, 2, 2, 2);
  private DragSource dragSource;
  private DragGestureListener dgListener;
  private DragSourceListener dsListener;

  private static final Pattern MACRO_LABEL = Pattern.compile("^(/\\w+\\s+)(.*)$");

  public MacroButton(MacroButtonProperties properties, ButtonGroup buttonGroup) {
    this(properties, buttonGroup, null);
  }

  public MacroButton(MacroButtonProperties properties, ButtonGroup buttonGroup, Token token) {
    this.properties = properties;
    this.buttonGroup = buttonGroup;
    this.panel = buttonGroup.getPanel();
    this.panelClass = buttonGroup.getPanel().getPanelClass();
    if (token == null) {
      this.tokenId = null;
    } else {
      this.tokenId = token.getId();
    }
    this.properties.setTokenId(this.tokenId);
    this.properties.setSaveLocation(this.panelClass);
    // we have to call setColor() and setText() here since properties only hold "dumb" data.
    setColor(properties.getColorKey());
    setText(getButtonText());
    hotKeyManager = new MacroButtonHotKeyManager(this);
    hotKeyManager.assignKeyStroke(properties.getHotKey());
    setMargin(buttonInsets);
    makeDraggable(DragSource.DefaultCopyDrop);
    addMouseListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public MacroButtonProperties getProperties() {
    return properties;
  }

  public MacroButtonHotKeyManager getHotKeyManager() {
    return hotKeyManager;
  }

  public GUID getTokenId() {
    return tokenId;
  }

  public Token getToken() {
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone z = (zr == null ? null : zr.getZone());
    return z == null ? null : z.getToken(tokenId);
  }

  public AbstractButtonGroup getButtonGroup() {
    return buttonGroup;
  }

  public String getPanelClass() {
    return panelClass;
  }

  public void setColor(String colorKey) {
    if ("default".equals(colorKey)) {
      updateUI(); // Reset to Theme default
    } else {
      setBackground(MapToolUtil.getColor(colorKey));
    }
  }

  /*
   * Get the text for the macro button by filtering out label macro (if any), and add hotkey hint (if any)
   */
  public String getButtonText() {
    String buttonLabel;
    String label = properties.getLabel();
    Matcher m = MACRO_LABEL.matcher(label);
    if (m.matches()) buttonLabel = m.group(2);
    else buttonLabel = label;

    String div = "<div style='font-size: " + properties.getFontSize() + "; text-align: center'>";
    String formatButtonLabel =
        switch (properties.getFontColorKey()) {
          case "", "default" -> "<p style='" + getMinWidth() + getMaxWidth() + "'>" + buttonLabel;
          default -> "<p style='color: "
              + properties.getFontColorAsHtml()
              + "; "
              + getMinWidth()
              + getMaxWidth()
              + "'>"
              + buttonLabel;
        };

    // if there is no hotkey (HOTKEY[0]) then no need to add hint
    String hotKey = properties.getHotKey();
    String result = null;
    if (hotKey.equals(MacroButtonHotKeyManager.HOTKEYS[0]) || !properties.getDisplayHotKey())
      result = "<html>" + div + formatButtonLabel;
    else
      result =
          "<html>" + div + formatButtonLabel + "<font style='font-size:0.8em'> (" + hotKey + ")";
    return result;
  }

  public String getMinWidth() {
    // the min-width style doesn't appear to work in the current java, so I'm
    // using width instead.
    String newMinWidth = properties.getMinWidth();
    if (newMinWidth != null && !newMinWidth.equals("")) {
      return " width:" + newMinWidth + ";";
      // return " min-width:"+newMinWidth+";";
    }
    return "";
  }

  public String getMaxWidth() {
    // doesn't appear to work in current java, leaving it in just in case
    // it is supported in the future
    String newMaxWidth = properties.getMaxWidth();
    if (newMaxWidth != null && !newMaxWidth.equals("")) {
      return " max-width:" + newMaxWidth + ";";
    }
    return "";
  }

  // Override these mouse events in subclasses to specify component specific behavior.
  public void mouseClicked(MouseEvent event) {}

  public void mousePressed(MouseEvent event) {}

  public void mouseReleased(MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      // If any of the following 3 conditions are correct we want to run it against all selected
      // tokens,
      // Shift is held down while clicking the button, the button has apply to selected tokens set,
      // or its a common macro button
      if (SwingUtil.isShiftDown(event)
          || properties.getApplyToTokens()
          || properties.getCommonMacro()) {
        if (MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList().size() > 0) {
          properties.executeMacro(
              MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList());
        }
      } else {
        properties.executeMacro();
      }
    } else if (SwingUtilities.isRightMouseButton(event)) {
      if (getPanelClass().equals("GlobalPanel")) {
        new MacroButtonPopupMenu(this, panelClass, false).show(this, event.getX(), event.getY());
      } else if (getPanelClass().equals("CampaignPanel")) {
        if (MapTool.getPlayer().isGM()) {
          new MacroButtonPopupMenu(this, panelClass, false).show(this, event.getX(), event.getY());
        } else {
          if (properties.getAllowPlayerEdits()) {
            new MacroButtonPopupMenu(this, panelClass, false)
                .show(this, event.getX(), event.getY());
          }
        }
      } else if (getPanelClass().equals("GmPanel")) {
        if (MapTool.getPlayer().isGM()) {
          new MacroButtonPopupMenu(this, panelClass, false).show(this, event.getX(), event.getY());
        }
      } else if (getPanelClass().equals("SelectionPanel")
          || getPanelClass().equals("ImpersonatePanel")) {
        if (MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(properties)) {
          new MacroButtonPopupMenu(this, panelClass, true).show(this, event.getX(), event.getY());
        } else {
          new MacroButtonPopupMenu(this, panelClass, false).show(this, event.getX(), event.getY());
        }
      }
    }
  }

  public void mouseEntered(MouseEvent event) {
    if (MapTool.getFrame().getCurrentZoneRenderer() == null) {
      return;
    }

    List<Token> selectedTokens =
        MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList();
    if (SwingUtil.isShiftDown(event) || getProperties().getApplyToTokens()) {
      MapTool.getFrame().getCurrentZoneRenderer().setHighlightCommonMacros(selectedTokens);
    } else {
      if ("SelectionPanel".equals(getPanelClass())) {
        List<Token> affectedTokens = new ArrayList<Token>();
        if (getProperties().getCommonMacro()) {
          for (Token nextSelected : selectedTokens) {
            boolean isCommonToToken = false;
            for (MacroButtonProperties nextMacro : nextSelected.getMacroList(true)) {
              if (nextMacro.hashCodeForComparison() == getProperties().hashCodeForComparison()) {
                isCommonToToken = true;
              }
            }
            if (isCommonToToken) {
              affectedTokens.add(nextSelected);
            }
          }
        } else if (getProperties().getToken() != null) {
          affectedTokens.add(getProperties().getToken());
        }
        MapTool.getFrame().getCurrentZoneRenderer().setHighlightCommonMacros(affectedTokens);
      }
    }
  }

  public void mouseExited(MouseEvent event) {
    List<Token> affectedTokens = new ArrayList<Token>();
    if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
      MapTool.getFrame().getCurrentZoneRenderer().setHighlightCommonMacros(affectedTokens);
    }
  }

  private void makeDraggable(Cursor cursor) {
    dragSource = DragSource.getDefaultDragSource();
    dgListener = new DGListener(cursor);
    dragSource.createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_COPY_OR_MOVE, dgListener);
    dsListener = new DSListener();
  }

  private class DGListener implements DragGestureListener {

    final Cursor cursor;

    public DGListener(Cursor cursor) {
      this.cursor = cursor;
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
      Transferable t =
          new TransferableMacroButton(
              MacroButton.this,
              dge.getTriggerEvent().getModifiersEx(),
              System.identityHashCode(panel));
      dge.startDrag(cursor, t, dsListener);
    }
  }

  private class DSListener implements DragSourceListener {

    public void dragEnter(DragSourceDragEvent event) {
      // System.out.println("TMB: drag enter");
      // DragSourceContext context = event.getDragSourceContext();
      // context.getComponent()
    }

    public void dragOver(DragSourceDragEvent event) {
      // System.out.println("TMB: drag over");
    }

    public void dropActionChanged(DragSourceDragEvent event) {
      // System.out.println("TMB: drop action changed");
    }

    public void dragExit(DragSourceEvent event) {
      // System.out.println("TMB: drag exit");
    }

    public void dragDropEnd(DragSourceDropEvent event) {
      // System.out.println("TMB: drag drop end");
      // js commented out for testing - MapTool.getFrame().updateSelectionPanel();
      List<Token> affectedTokens = new ArrayList<Token>();
      MapTool.getFrame().getCurrentZoneRenderer().setHighlightCommonMacros(affectedTokens);
    }
  }

  public void clearHotkey() {
    getHotKeyManager().assignKeyStroke(MacroButtonHotKeyManager.HOTKEYS[0]);
  }

  @Override
  public String getToolTipText(MouseEvent e) {
    String tt = properties.getEvaluatedToolTip();
    return tt.length() == 0 ? null : tt;
  }
}
