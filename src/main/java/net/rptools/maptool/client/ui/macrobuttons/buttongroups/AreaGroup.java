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
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.panels.AbstractMacroPanel;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;

public class AreaGroup extends AbstractButtonGroup {
  // constructor for creating an area group in the campaign/global panels
  public AreaGroup(
      List<MacroButtonProperties> propertiesList, String panelLabel, AbstractMacroPanel panel) {
    setPropertiesList(propertiesList);
    setPanel(panel);
    setPanelClass(panel.getPanelClass());
    setGroupClass("AreaGroup");
    setGroupLabel(panelLabel);
    setTokenId(panel.getToken());
    addMouseListener(this);
    drawArea();
  }

  // constructor for creating an area group for the impersonate/selection panels
  public AreaGroup(GUID tokenId, AbstractMacroPanel panel) {
    setTokenId(tokenId);
    setPropertiesList(getToken().getMacroList(true));
    setPanel(panel);
    setPanelClass(panel.getPanelClass());
    setGroupClass("AreaGroup");
    setGroupLabel(getTokenName(getToken()));
    addMouseListener(this);
    drawArea();
  }

  // constructor for creating an area spacer, used to take up space where an area label would be
  public AreaGroup(int height, AbstractMacroPanel panel) {
    setSpacerHeight(height);
    setPanel(panel);
    setPanelClass(panel.getPanelClass());
    setOpaque(false);
    // addMouseListener(this); don't use; the label has its own
  }

  public void drawArea() {
    if (getToken() == null && getGroupLabel().equals("")) {
      // don't put an extra border around the campaign/global panels, or if there is no label
    } else {
      ThumbnailedBorder border = createBorder(getGroupLabel());
      setBorder(border);
      add(new AreaGroup(12, getPanel())); // spacer
    }
    String lastGroup = "akjaA#$Qq4jakjj#%455jkkajDAJFAJ"; // random string
    String currentGroup = "";

    List<MacroButtonProperties> propertiesList = getPropertiesList();
    List<MacroButtonProperties> groupList = new ArrayList<MacroButtonProperties>();
    Collections.sort(propertiesList);

    if (propertiesList.isEmpty()) {
      add(new ButtonGroup(propertiesList, "", getPanel(), getTokenId(), this));
    } else {
      // build separate button groups for each user-defined group
      for (MacroButtonProperties prop : propertiesList) {
        currentGroup = prop.getGroup();
        if (!groupList.isEmpty()
            && !lastGroup.equalsIgnoreCase(
                currentGroup)) { // better to use currentGroup.equals(lastGroup) since lastGroup
          // could be initialized to null
          add(new ButtonGroup(groupList, lastGroup, getPanel(), getTokenId(), this));
          groupList.clear();
        }
        lastGroup = currentGroup;
        groupList.add(prop);
      }
      if (!groupList.isEmpty()) {
        add(new ButtonGroup(groupList, lastGroup, getPanel(), getTokenId(), this));
        groupList.clear();
      }
    }
    setLayout(new FlowLayout(FlowLayout.LEFT));
    revalidate();
    repaint();
  }

  @Override
  public void drop(DropTargetDropEvent event) {
    // System.out.println("BG: drop!");
    event.rejectDrop(); // don't accept drops in an area group, it should be in the button group
    event.dropComplete(true);
  }

  @Override
  public Insets getInsets() {
    return new Insets(0, 1, 3, 0);
  }

  @Override
  public Dimension getPreferredSize() {
    FlowLayout layout = (FlowLayout) getLayout();
    Insets insets = getInsets();
    // This isn't exact, but hopefully it's close enough
    int availableWidth = getPanel().getAvailableWidth() - insets.left - insets.right;
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

  @Override
  public void mouseReleased(MouseEvent event) {
    Token token = getToken();
    if (SwingUtilities.isRightMouseButton(event)) {
      if (getPanelClass() == "CampaignPanel" && !MapTool.getPlayer().isGM()) {
        return;
      }
      // open button group menu
      new ButtonGroupPopupMenu(getPanelClass(), this, getMacroGroup(), token)
          .show(this, event.getX(), event.getY());
    }
  }

  public List<ButtonGroup> getButtonGroups() {
    List<ButtonGroup> myButtonGroups = new ArrayList<ButtonGroup>();
    for (int buttonGroupCount = 0;
        buttonGroupCount < this.getComponentCount();
        buttonGroupCount++) {
      myButtonGroups.add((ButtonGroup) this.getComponent(buttonGroupCount));
    }
    return myButtonGroups;
  }
}
