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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.swing;

import java.awt.Dimension;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AbstractPoint;
import net.rptools.maptool.model.Zone;

/**
 * A label located in MapTool's status bar displaying the grid coordinates of the mouse cursor's
 * current position on the current map.
 *
 * <p>Each individual map can be configured using {@link Zone.ShowCoordinates} to show or not show
 * coordinates, which this component checks during {@link #update(AbstractPoint)}.
 *
 * <p>This component's enabled/disabled states are used to provide visual feedback with icons and
 * tooltips on whether coordinates should be shown or not be shown, as some methods may set the text
 * to blank even if coordinates should be shown for reasons such as switching to a new map and so to
 * not display stale coordinates.
 */
public class CoordinateStatusBar extends JLabel {

  /** Localization tooltip for when the label is enabled (i.e. shown). */
  private final String enabledToolTip = I18N.getString("CoordinateStatusBar.mapCoordinates");

  /** Localization tooltip for when the label is disabled (i.e. not shown). */
  private final String disabledToolTip =
      I18N.getString("CoordinateStatusBar.mapCoordinates.hidden");

  /** Constructs the CoordinateStatusBar. */
  public CoordinateStatusBar() {

    // icon for when grid coordinates are to be shown
    setIcon(RessourceManager.getSmallIcon(Icons.MENU_SHOW_GRIDS));

    // icon for when grid coordinates are not to be shown
    setDisabledIcon(RessourceManager.getSmallIcon(Icons.TOOLBAR_ZONE_NOT_VISIBLE));

    // establish the minimum size based on the font and a reasonable amount of characters (even for
    // gridless grids!)
    setMinimumSize(
        new Dimension(
            getIcon().getIconWidth()
                + getIconTextGap()
                + SwingUtilities.computeStringWidth(
                    getFontMetrics(getFont()), "-1234567, -1234567"),
            getFontMetrics(getFont()).getHeight()));

    // right align as coordinates are numbers and visually consistent with the ZoomStatusBar
    setHorizontalAlignment(SwingConstants.RIGHT);

    // position text to the left of the icon
    setHorizontalTextPosition(SwingConstants.LEFT);

    // set the default tooltip and initialize the ToolTipManager
    setToolTipText(enabledToolTip);
  }

  /**
   * Get the preferred size of the CoordinateStatusBar.
   *
   * @return the preferred size.
   */
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  /**
   * Resets the CoordinateStatusBar (setting text to blank and checks show coordinate permissions)
   * by sending a {@code null} to {@link AbstractPoint} to {@link #update(AbstractPoint)}.
   */
  public void reset() {
    update((AbstractPoint) null);
  }

  /**
   * Update the CoordinateStatusBar, checking whether to show a map's coordinates for a player.
   *
   * <p>As this can be called many times during mouse dragging we only update if the state actually
   * changed to avoid unnecessary repaints.
   *
   * @param abstractPoint the point on a map (or a {@code null} {@link AbstractPoint}).
   */
  public void update(AbstractPoint abstractPoint) {

    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    if (zr != null) {
      boolean showCoordinates =
          zr.getZone().getShowCoordinates().shouldShow(MapTool.getPlayer().getRole());

      String coordinatesText =
          (abstractPoint != null && showCoordinates)
              ? abstractPoint.x + ", " + abstractPoint.y
              : "";

      if (isEnabled() != showCoordinates) {
        setEnabled(showCoordinates);
        setToolTipText(isEnabled() ? enabledToolTip : disabledToolTip);
      }
      if (!getText().equals(coordinatesText)) {
        setText(coordinatesText);
      }
    }
  }
}
