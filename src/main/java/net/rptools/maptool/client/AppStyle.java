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

import java.awt.Color;
import java.awt.Font;
import net.rptools.maptool.client.swing.ImageBorder;
import net.rptools.maptool.client.ui.theme.Borders;
import net.rptools.maptool.client.ui.theme.IconMap;

/** @author trevor */
public class AppStyle {

  public static ImageBorder border = IconMap.getBorder(Borders.GRAY2);
  public static ImageBorder selectedBorder = IconMap.getBorder(Borders.RED);
  public static ImageBorder selectedStampBorder = IconMap.getBorder(Borders.BLUE);
  public static ImageBorder selectedUnownedBorder = IconMap.getBorder(Borders.GREEN);
  public static ImageBorder miniMapBorder = IconMap.getBorder(Borders.GRAY);
  public static ImageBorder shadowBorder = IconMap.getBorder(Borders.SHADOW);
  public static ImageBorder commonMacroBorder = IconMap.getBorder(Borders.HIGHLIGHT);
  public static Font labelFont = Font.decode("serif-NORMAL-12");
  public static Color selectionBoxOutline = Color.black;
  public static Color selectionBoxFill = Color.blue;
  public static Color resizeBoxOutline = Color.red;
  public static Color resizeBoxFill = Color.yellow;
  public static Color topologyColor = new Color(0, 0, 255, 128);
  public static Color topologyAddColor = new Color(255, 0, 0, 128);
  public static Color topologyRemoveColor = new Color(255, 255, 255, 128);
  public static Color hillVblColor = new Color(0, 255, 255, 128);
  public static Color pitVblColor = new Color(104, 255, 0, 128);
  public static Color topologyTerrainColor = new Color(255, 0, 255, 128);
  public static Color tokenTopologyColor = new Color(255, 255, 0, 128);
  public static Color tokenHillVblColor = new Color(255, 136, 0, 128);
  public static Color tokenPitVblColor = new Color(255, 0, 0, 128);
  public static Color tokenMblColor = new Color(255, 128, 255, 128);
}
