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

  public static BufferedImage tokenInvisible;
  public static BufferedImage notVisible;

  public static BufferedImage cellWaypointImage;
  public static BufferedImage validMoveImage;
  public static BufferedImage blockMoveImage;

  public static BufferedImage stackImage;

  public static BufferedImage markerImage;

  public static Color selectionBoxOutline = Color.black;
  public static Color selectionBoxFill = Color.blue;

  public static Color resizeBoxOutline = Color.red;
  public static Color resizeBoxFill = Color.yellow;

  public static BufferedImage chatImage;
  public static BufferedImage chatScrollImage;
  public static BufferedImage chatScrollLockImage;
  public static BufferedImage chatNotifyImage;

  public static BufferedImage showTypingNotification;
  public static BufferedImage hideTypingNotification;

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

  public static BufferedImage boundedBackgroundTile;

  public static BufferedImage cancelButton;
  public static BufferedImage addButton;

  public static BufferedImage panelTexture;
  public static BufferedImage squaresTexture;
  public static BufferedImage lookupTableDefaultImage;

  public static BufferedImage resourceLibraryImage;
  public static BufferedImage mapExplorerImage;
  public static BufferedImage connectionsImage;
  public static BufferedImage chatPanelImage;
  public static BufferedImage globalPanelImage;
  public static BufferedImage campaignPanelImage;
  public static BufferedImage selectionPanelImage;
  public static BufferedImage impersonatePanelImage;
  public static BufferedImage tablesPanelImage;
  public static BufferedImage initiativePanelImage;

  public static BufferedImage resize;

  public static BufferedImage arrowOut;
  public static BufferedImage arrowRotateClockwise;
  public static BufferedImage arrowIn;
  public static BufferedImage arrowMenu;
  public static BufferedImage arrowRight;
  public static BufferedImage arrowLeft;
  public static BufferedImage arrowHold;

  public static BufferedImage lightSourceIcon;

  static {
    try {
      // Set defaults
      tokenInvisible =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/icon_invisible.png");
      notVisible = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/notvisible.png");
      cellWaypointImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/redDot.png");
      validMoveImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/valid-move-arrow.png");
      blockMoveImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/block_move.png");
      stackImage = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/stack.png");
      markerImage = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/marker.png");
      chatImage = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/chat-blue.png");
      chatScrollImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/comments.png");
      chatScrollLockImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/comments_delete.png");
      chatNotifyImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/chat-red.png");

      // Typing notification icons added by Rumble
      showTypingNotification =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/chatNotifyOn.png");
      hideTypingNotification =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/chatNotifyOff.png");

      boundedBackgroundTile =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/Black.png");
      panelTexture =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/panelTexture.jpg");
      squaresTexture =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/squaresTexture.png");

      cancelButton = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/cancel_sm.png");
      addButton = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/add_sm.png");

      lookupTableDefaultImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/document.png");

      resourceLibraryImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/book_open.png");
      mapExplorerImage = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/eye.png");
      connectionsImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/computer.png");
      chatPanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/application.png");
      globalPanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/global_panel.png");
      campaignPanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/campaign_panel.png");
      selectionPanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/cursor.png");
      impersonatePanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/impersonate.png");
      tablesPanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/layers.png");
      initiativePanelImage =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/initiativePanel.png");

      resize = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/resize.png");

      arrowOut = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow_out.png");
      arrowRotateClockwise =
          ImageUtil.getCompatibleImage(
              "net/rptools/maptool/client/image/arrow_rotate_clockwise.png");
      arrowIn = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow_in_red.png");
      arrowRight = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow_right.png");
      arrowLeft = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow_left.png");
      arrowMenu = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow_menu.png");
      arrowHold = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow_hold.png");

      lightSourceIcon =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/lightbulb.png");
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
