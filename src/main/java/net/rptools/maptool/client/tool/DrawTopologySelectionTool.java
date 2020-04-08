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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.*;
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

    // Mouse listener added to manually handle mouse events
    super.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            if (isAvailable()) {
              getModel().setArmed(true); // change icon to "pressed down"
            }
            ToolTipManager.sharedInstance().mouseEntered(e); // display the tooltip
          }

          @Override
          public void mouseExited(MouseEvent e) {
            getModel().setArmed(false); // undo "pressed down" effect
            ToolTipManager.sharedInstance().mouseExited(e); // hide the tooltip
          }

          /**
           * When the mouse is pressed, manually changes the selected status and icon. This approach
           * is recommended by Dr. Heinz M. Kabutz to handle tri-state buttons.
           *
           * @param e the mouse event
           */
          @Override
          public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && isAvailable()) {
              nextState();
            }
          }
        });
  }

  /** Set the Selection Tool to the next state (Combined -> MBL -> VBL -> Combined). */
  public void nextState() {
    TopologyMode currentTopologyMode;

    if (isSelected()) {
      // If Combined, switch to MBL (unselected)
      setSelected(false);
      currentTopologyMode = TopologyMode.MBL;
      setActionCommand(TopologyMode.MBL.toString());
      setIcon(mblImageIcon);
    } else {
      if (getActionCommand().equals(TopologyMode.MBL.toString())) {
        // If MBL, switch to VBL (unselected)
        currentTopologyMode = TopologyMode.VBL;
        setActionCommand(TopologyMode.VBL.toString());
        setIcon(vblImageIcon);
      } else {
        // If VBL, switch to Combined (selected)
        setSelected(true);
        currentTopologyMode = TopologyMode.COMBINED;
        setActionCommand(TopologyMode.COMBINED.toString());
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

  /**
   * Prevents the addition of any MouseListener.
   *
   * @param l the MouseListener to ignore
   */
  @Override
  public void addMouseListener(MouseListener l) {}
}
