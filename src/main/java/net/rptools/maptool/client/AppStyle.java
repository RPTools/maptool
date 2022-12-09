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
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImageBorder;

/** @author trevor */
public class AppStyle {

  public static ImageBorder border = ImageBorder.GRAY;
  public static ImageBorder selectedBorder = ImageBorder.RED;
  public static ImageBorder selectedStampBorder = ImageBorder.BLUE;
  public static ImageBorder selectedUnownedBorder = AppConstants.GREEN_BORDER;
  public static ImageBorder miniMapBorder = AppConstants.GRAY_BORDER;
  public static ImageBorder shadowBorder = AppConstants.SHADOW_BORDER;
  public static ImageBorder commonMacroBorder = AppConstants.HIGHLIGHT_BORDER;

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
  public static BufferedImage notVisible;
  public static BufferedImage cellWaypointImage;
  public static BufferedImage blockMoveImage;
  public static BufferedImage stackImage;
  public static BufferedImage chatImage;
  public static BufferedImage cancelButton;
  public static BufferedImage addButton;
  public static BufferedImage panelTexture;
  public static BufferedImage squaresTexture;
  public static BufferedImage lookupTableDefaultImage;
  public static BufferedImage resize;
  public static BufferedImage lightSourceIcon;

  static {
    try {
      // Set defaults
      notVisible = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/notvisible.png");
      cellWaypointImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/redDot.png");
      blockMoveImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/block_move.png");
      stackImage = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/stack.png");
      chatImage = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/chat-blue.png");
      panelTexture =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/panelTexture.jpg");
      squaresTexture =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/squaresTexture.png");

      cancelButton = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/cancel_sm.png");
      addButton = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/add_sm.png");

      lookupTableDefaultImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/document.png");
      resize = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/resize.png");
      lightSourceIcon =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/lightbulb.png");
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
