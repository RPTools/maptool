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
package net.rptools.maptool.client.ui.macrobuttons.buttongroups;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.MacroButtonHotKeyManager;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.macrobuttons.buttons.TransferData;
import net.rptools.maptool.client.ui.macrobuttons.buttons.TransferableMacroButton;
import net.rptools.maptool.client.ui.macrobuttons.panels.AbstractMacroPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;

@SuppressWarnings("serial")
public class ButtonGroup extends AbstractButtonGroup {

  // constructor for creating a normal button group
  public ButtonGroup(
      List<MacroButtonProperties> propertiesList,
      String group,
      AbstractMacroPanel panel,
      GUID tokenId,
      AreaGroup area) {
    setArea(area);
    setPropertiesList(propertiesList);
    setPanel(panel);
    setPanelClass(panel.getPanelClass());
    setGroupClass("ButtonGroup");
    setGroupLabel(group);
    setTokenId(tokenId);
    setMacroGroup(group);
    addMouseListener(this);
    drawButtons();
  }

  protected void drawButtons() {
    List<MacroButtonProperties> propertiesList = getPropertiesList();
    String panelClass = getPanelClass();
    setOpaque(false);
    if (getGroupLabel().equals("")) {
      setBorder(new ThumbnailedBorder(null, null)); // no label or icon, just solid border
    } else {
      setBorder(new ThumbnailedBorder(null, getGroupLabel()));
    }
    if (propertiesList.isEmpty()) {
      add(new JLabel(I18N.getText("component.areaGroup.macro.noMacros")));
    } else {
      Collections.sort(propertiesList);
      for (MacroButtonProperties prop : propertiesList) {
        if (panelClass.equals("GlobalPanel") || panelClass.equals("CampaignPanel")) {
          add(new MacroButton(prop, this));
        } else if (panelClass.equals("ImpersonatePanel") || panelClass.equals("SelectionPanel")) {
          add(new MacroButton(prop, this, getToken()));
        }
      }
    }
    dt = new DropTarget(this, this);
    setLayout(new FlowLayout(FlowLayout.LEFT));
    revalidate();
    repaint();
  }

  public void drop(DropTargetDropEvent event) {
    // System.out.println("BG: drop!");
    String panelClass = getPanelClass();

    try {
      Transferable t = event.getTransferable();
      TransferData data =
          (TransferData) t.getTransferData(TransferableMacroButton.macroButtonFlavor);
      if (data == null) {
        return;
      }
      // create a temporary MacroButtonProperties object to hold the transferred data until we
      // figure out where it goes
      MacroButtonProperties tempProperties =
          new MacroButtonProperties(
              data.index,
              data.colorKey,
              MacroButtonHotKeyManager.HOTKEYS[0], // don't reuse the hot key
              data.command,
              data.label,
              data.group,
              data.sortby,
              data.autoExecute,
              data.includeLabel,
              data.applyToTokens,
              data.fontColorKey,
              data.fontSize,
              data.minWidth,
              data.maxWidth,
              data.toolTip);

      if (panelClass.equals("GlobalPanel")) {
        event.acceptDrop(event.getDropAction());
        tempProperties.setGroup(
            getMacroGroup()); // assign the group you are dropping it into, rather than the original
        if (!tempProperties.isDuplicateMacro("GlobalPanel", null)) {
          new MacroButtonProperties(panelClass, MacroButtonPrefs.getNextIndex(), tempProperties);
        }
      } else if (panelClass.equals("CampaignPanel")) {
        event.acceptDrop(event.getDropAction());
        tempProperties.setGroup(
            getMacroGroup()); // assign the group you are dropping it into, rather than the original
        if (!tempProperties.isDuplicateMacro("CampaignPanel", null)) {
          new MacroButtonProperties(
              panelClass, MapTool.getCampaign().getMacroButtonNextIndex(), tempProperties);
        }
      } else if (panelClass.equals("SelectionPanel")) {
        if (getArea() != null) {
          if (getArea()
              .getGroupLabel()
              .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
            event.acceptDrop(event.getDropAction());
            tempProperties.setGroup(
                getMacroGroup()); // assign the group you are dropping it into, rather than the
            // original
            for (Token nextToken :
                MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
              if (!tempProperties.isDuplicateMacro("Token", nextToken)) {
                new MacroButtonProperties(nextToken, nextToken.getMacroNextIndex(), tempProperties);
              }
            }
          } else if (getToken() != null) {
            // this is a token group, copy macro to token
            event.acceptDrop(event.getDropAction());
            tempProperties.setGroup(
                getMacroGroup()); // assign the group you are dropping it into, rather than the
            // original
            Token token = getToken();
            if (!tempProperties.isDuplicateMacro("Token", token)) {
              new MacroButtonProperties(token, token.getMacroNextIndex(), tempProperties);
            }
          }
        }
      } else if (getToken() != null) {
        // this is a token group, copy macro to token
        event.acceptDrop(event.getDropAction());
        tempProperties.setGroup(
            getMacroGroup()); // assign the group you are dropping it into, rather than the original
        Token token = getToken();
        if (!tempProperties.isDuplicateMacro("Token", token)) {
          new MacroButtonProperties(token, token.getMacroNextIndex(), tempProperties);
        }
      } else {
        // if this happens, it's a bug
        throw new Exception(I18N.getText("msg.error.macro.buttonGroupDnDFail"));
      }
      // System.out.println("drop accepted");
      event.dropComplete(true);
    } catch (Exception e) {
      e.printStackTrace();
      event.dropComplete(false);
    }
  }

  public Insets getInsets() {
    if (getGroupLabel().equals("")) {
      return new Insets(3, 3, 3, 0);
    } else {
      return new Insets(16, 3, 3, 0);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    FlowLayout layout = (FlowLayout) getLayout();
    Insets insets = getInsets();
    // This isn't exact, but hopefully it's close enough
    int availableWidth = getPanel().getAvailableWidth() - insets.left - insets.right - 10;
    int height = insets.top + insets.bottom + layout.getVgap();
    int rowHeight = 0;
    int rowWidth = insets.left + layout.getHgap() + insets.right;
    for (Component c : getComponents()) {
      Dimension cSize = c.getPreferredSize();
      if (rowWidth + cSize.width + layout.getHgap() - 5 > availableWidth && rowWidth > 0) {
        height += rowHeight + layout.getVgap();
        rowHeight = 0;
        rowWidth = insets.left + layout.getHgap() + insets.right;
      }
      rowWidth += cSize.width + layout.getHgap();
      rowHeight = Math.max(cSize.height, rowHeight);
    }
    height += rowHeight;
    Dimension prefSize = new Dimension(availableWidth, height);
    return prefSize;
  }

  /*
   * The following version of getPreferredSize allows multiple small groups to flow on one row. However it has some glitches in calculating sizes, and makes it harder to find a spot to
   * right-click/drop things on since it doesn't extend the full width of the panel. Not using until the kinks are worked out.
   *
   * @Override public Dimension getPreferredSize() { FlowLayout layout = (FlowLayout) getLayout(); Insets insets = getInsets(); int availableWidth = getPanel().getAvailableWidth() - insets.left -
   * insets.right; // This isn't exact, but hopefully it's close enough int height = insets.top + insets.bottom + layout.getVgap(); int width = 0; int rowHeight = 0; int rowWidth = insets.left +
   * layout.getHgap() + insets.right; int maxRowWidth = 0; for (Component c : getComponents()) { Dimension cSize = c.getPreferredSize(); if (rowWidth + cSize.width + layout.getHgap() + 15 >
   * availableWidth && rowWidth > 0) { maxRowWidth = Math.max(maxRowWidth, rowWidth); height += rowHeight + layout.getVgap(); System.out.println("***** "+getPanelClass()+":"+getGroupClass()+":"+
   * getGroupLabel()+" New Row, Size: "+maxRowWidth+", "+height); rowHeight = 0; rowWidth = insets.left + layout.getHgap() + insets.right; } rowWidth += cSize.width + layout.getHgap(); rowHeight =
   * Math.max(cSize.height, rowHeight); } height += rowHeight; maxRowWidth = Math.max(maxRowWidth, rowWidth); System.out.println("***** "+getPanelClass()+":"+getGroupClass()+":"+
   * getGroupLabel()+" New Row, Size: "+maxRowWidth+", "+height); width = maxRowWidth; // always use the full width for the general area Dimension prefSize = new Dimension(width, height);
   * System.out.println("***** "+getPanelClass()+":"+getGroupClass()+":"+ getGroupLabel()+" PREFERRED SIZE: "+width+", "+height); return prefSize; }
   */

  public List<MacroButton> getButtons() {
    List<MacroButton> myButtons = new ArrayList<MacroButton>();
    for (int buttonCount = 0; buttonCount < this.getComponentCount(); buttonCount++) {
      if (this.getComponent(buttonCount).getClass() == MacroButton.class) {
        myButtons.add((MacroButton) this.getComponent(buttonCount));
      }
    }
    return myButtons;
  }
}
