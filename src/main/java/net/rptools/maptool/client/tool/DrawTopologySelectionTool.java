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
package net.rptools.maptool.client.tool;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.ImageIcon;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone.TopologyMode;

/**
 * A tri-state button to control which topology layer the Topology Tools draw to The current options
 * are VBL, MBL, and Both.
 */
public class DrawTopologySelectionTool extends DefaultTool {

  private ImageIcon vblImageIcon = new ImageIcon();
  private ImageIcon mblImageIcon = new ImageIcon();
  private ImageIcon combinedImageIcon = new ImageIcon();

  public DrawTopologySelectionTool() {
    try {
      vblImageIcon =
          new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/tool/vbl-only.png"));
      mblImageIcon =
          new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/tool/mbl-only.png"));
      combinedImageIcon =
          new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/tool/mbl-vbl-on.png"));

      setIcon(vblImageIcon);
      setSelectedIcon(combinedImageIcon);

      switch (AppPreferences.getTopologyDrawingMode()) {
        case VBL:
          setActionCommand(TopologyMode.VBL.toString());
          setIcon(vblImageIcon);
          break;
        case MBL:
          setActionCommand(TopologyMode.MBL.toString());
          setIcon(mblImageIcon);
          break;
        case COMBINED:
          setActionCommand(TopologyMode.VBL.toString());
          setIcon(vblImageIcon);
          setSelected(true);
          break;
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TopologyMode currentTopologyMode;

    // Not a huge fan of this code but needed a TriStateButton and TriStateCheckbox from JIDE
    // doesn't handle custom images very well. Also, trying to avoid JIDE were possible
    // in case we move to FX in the future.
    if (isSelected()) {
      if (getActionCommand().equals(TopologyMode.MBL.toString())) {
        currentTopologyMode = TopologyMode.VBL;
        setActionCommand(TopologyMode.VBL.toString());
        setSelected(false);
        setIcon(vblImageIcon);
      } else {
        currentTopologyMode = TopologyMode.COMBINED;
      }
    } else {
      // Toggle unselected state between VBL/MBL
      if (getActionCommand().equals(TopologyMode.VBL.toString())) {
        currentTopologyMode = TopologyMode.MBL;
        setActionCommand(TopologyMode.MBL.toString());
        setIcon(mblImageIcon);
      } else {
        currentTopologyMode = TopologyMode.VBL;
        setActionCommand(TopologyMode.VBL.toString());
        setIcon(vblImageIcon);
      }
    }

    AppPreferences.setTopologyDrawingMode(currentTopologyMode);

    MapTool.getFrame().getCurrentZoneRenderer().getZone().setTopologyMode(currentTopologyMode);
  }

  @Override
  public String getTooltip() {
    return "tools.draw_topoloy_selection.tooltip";
  }

  @Override
  public String getInstructions() {
    return ""; // Not used, only displayed for currentTool
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  public boolean hasGroup() {
    return false;
  }
}
