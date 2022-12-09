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
package net.rptools.maptool.client.ui.theme;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.*;
import net.rptools.lib.image.ImageUtil;
import org.javatuples.Triplet;

public class IconMap {
  private static final String CLASSIC_ICON_DIR = "net/rptools/maptool/client/image";
  private static final HashMap<Icons, String> classicIcons =
      new HashMap<>() {
        {
          put(Icons.ACTION_NEW, CLASSIC_ICON_DIR + "/add.png");
          put(Icons.ACTION_EDIT, CLASSIC_ICON_DIR + "/pencil.png");
          put(Icons.ACTION_DELETE, CLASSIC_ICON_DIR + "/delete.png");
          put(Icons.ACTION_COPY, CLASSIC_ICON_DIR + "/page_copy.png");
          //    put(Icons.ACTION_IMPORT, "");
          //    put(Icons.ACTION_EXPORT, "");
          put(Icons.ACTION_SETTINGS, CLASSIC_ICON_DIR + "/arrow_menu.png");
          put(Icons.ACTION_NEXT, CLASSIC_ICON_DIR + "/arrow_right.png");
          put(Icons.ACTION_PREVIOUS, CLASSIC_ICON_DIR + "/arrow_left.png");
          put(Icons.ACTION_RESET, CLASSIC_ICON_DIR + "/arrow_rotate_clockwise.png");
          put(Icons.ACTION_PAUSE, CLASSIC_ICON_DIR + "/arrow_hold.png");
          put(Icons.ACTION_SELECT_ALL_TOKENS, CLASSIC_ICON_DIR + "/arrow_out.png");
          put(Icons.ACTION_SELECT_NO_TOKENS, CLASSIC_ICON_DIR + "/arrow_in_red.png");
          put(Icons.ACTION_NEXT_TOKEN, CLASSIC_ICON_DIR + "/arrow_right.png");
          put(Icons.ACTION_PREVIOUS_TOKEN, CLASSIC_ICON_DIR + "/arrow_left.png");
          put(Icons.ACTION_RESET_TOKEN_SELECTION, CLASSIC_ICON_DIR + "/arrow_rotate_clockwise.png");
          put(Icons.CHAT_SHOW_TYPING_NOTIFICATION, CLASSIC_ICON_DIR + "/chatNotifyOn.png");
          put(Icons.CHAT_HIDE_TYPING_NOTIFICATION, CLASSIC_ICON_DIR + "/chatNotifyOff.png");
          put(Icons.CHAT_SCROLL_LOCK_ON, CLASSIC_ICON_DIR + "/comments.png");
          put(Icons.CHAT_SCROLL_LOCK_OFF, CLASSIC_ICON_DIR + "/comments_delete.png");
          put(Icons.EDIT_TOKEN_HEROLAB, CLASSIC_ICON_DIR + "/hero-lab-icon-small.png");
          put(Icons.EDIT_TOKEN_COLOR_PICKER, CLASSIC_ICON_DIR + "/color-picker-32.png");
          put(Icons.EDIT_TOKEN_REFRESH_ON, CLASSIC_ICON_DIR + "/refresh_arrows_small.png");
          put(Icons.EDIT_TOKEN_REFRESH_OFF, CLASSIC_ICON_DIR + "/refresh_off_arrows_small.png");
          put(Icons.MENU_DOCUMENTATION, CLASSIC_ICON_DIR + "/book_open.png");
          put(Icons.MENU_FORUMS, CLASSIC_ICON_DIR + "/marker.png");
          put(Icons.MENU_FRAMEWORKS, CLASSIC_ICON_DIR + "/minilogo.png");
          put(Icons.MENU_SCRIPTING, CLASSIC_ICON_DIR + "/pencil.png");
          put(Icons.MENU_NETWORK_SETUP, CLASSIC_ICON_DIR + "/download.png");
          put(Icons.MENU_SHOW_GRIDS, CLASSIC_ICON_DIR + "/grid.gif");
          put(Icons.MENU_SHOW_TOKEN_NAMES, CLASSIC_ICON_DIR + "/names.png");
          put(Icons.MENU_TUTORIALS, CLASSIC_ICON_DIR + "/tutorial.jpg");
          // put(Icons.PROPERTIES_TABLE_ALPHABETIC, "");
          //  put(Icons.PROPERTIES_TABLE_CATEGORIES,"");
          //  put(Icons.PROPERTIES_TABLE_COLLAPSE,"");
          //  put(Icons.PROPERTIES_TABLE_EXPAND,"");
          //   put(Icons.PROPERTIES_TABLE_HIDE_DESCRIPTION,"");
          put(Icons.STATUSBAR_SERVER_DISCONNECTED, CLASSIC_ICON_DIR + "/computer_off.png");
          put(Icons.STATUSBAR_SERVER_CONNECTED, CLASSIC_ICON_DIR + "/computer_on.png");
          put(Icons.STATUSBAR_SERVER_RUNNING, CLASSIC_ICON_DIR + "/computer_server.png");
          put(Icons.STATUSBAR_TRANSMIT_ON, CLASSIC_ICON_DIR + "/transmitOn.png");
          put(Icons.STATUSBAR_TRANSMIT_OFF, CLASSIC_ICON_DIR + "/activityOff.png");
          put(Icons.STATUSBAR_RECEIVE_ON, CLASSIC_ICON_DIR + "/receiveOn.png");
          put(Icons.STATUSBAR_RECEIVE_OFF, CLASSIC_ICON_DIR + "/activityOff.png");
          put(Icons.STATUSBAR_ASSET_CACHE, CLASSIC_ICON_DIR + "/asset-status.png");
          put(Icons.STATUSBAR_FREE_SPACE, CLASSIC_ICON_DIR + "/disk-space.png");
          put(Icons.STATUSBAR_IMAGE_CACHE, CLASSIC_ICON_DIR + "/thumbnail-status.png");
          put(Icons.TOOLBAR_POINTERTOOL_ON, CLASSIC_ICON_DIR + "/tool/pointer-blue.png");
          put(Icons.TOOLBAR_POINTERTOOL_OFF, CLASSIC_ICON_DIR + "/tool/pointer-blue-off.png");
          put(Icons.TOOLBAR_POINTERTOOL_POINTER, CLASSIC_ICON_DIR + "/tool/pointer-blue.png");
          put(Icons.TOOLBAR_POINTERTOOL_MEASURE, CLASSIC_ICON_DIR + "/tool/ruler-blue.png");
          put(Icons.TOOLBAR_POINTERTOOL_AI_ON, CLASSIC_ICON_DIR + "/tool/ai-blue-green.png");
          put(Icons.TOOLBAR_POINTERTOOL_AI_OFF, CLASSIC_ICON_DIR + "/tool/ai-blue-off.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON,
              CLASSIC_ICON_DIR + "/tool/use-vbl-on-move.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF,
              CLASSIC_ICON_DIR + "/tool/ignore-vbl-on-move.png");
          put(Icons.TOOLBAR_DRAW_ON, CLASSIC_ICON_DIR + "/tool/draw-blue.png");
          put(Icons.TOOLBAR_DRAW_OFF, CLASSIC_ICON_DIR + "/tool/draw-blue-off.png");
          put(Icons.TOOLBAR_DRAW_DELETE, CLASSIC_ICON_DIR + "/delete.png");
          put(Icons.TOOLBAR_DRAW_FREEHAND, CLASSIC_ICON_DIR + "/tool/draw-blue-freehndlines.png");
          put(Icons.TOOLBAR_DRAW_LINE, CLASSIC_ICON_DIR + "/tool/draw-blue-strtlines.png");
          put(Icons.TOOLBAR_DRAW_BOX, CLASSIC_ICON_DIR + "/tool/draw-blue-box.png");
          put(Icons.TOOLBAR_DRAW_OVAL, CLASSIC_ICON_DIR + "/tool/draw-blue-circle.png");
          put(Icons.TOOLBAR_DRAW_TEXT, CLASSIC_ICON_DIR + "/tool/text-blue.png");
          put(Icons.TOOLBAR_DRAW_DIAMOND, CLASSIC_ICON_DIR + "/tool/draw-blue-diamond.png");
          put(Icons.TOOLBAR_TEMPLATE_ON, CLASSIC_ICON_DIR + "/tool/temp-blue.png");
          put(Icons.TOOLBAR_TEMPLATE_OFF, CLASSIC_ICON_DIR + "/tool/temp-blue-off.png");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS,
              CLASSIC_ICON_DIR + "/tool/temp-blue-vertex-radius.png");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS_CELL,
              CLASSIC_ICON_DIR + "/tool/temp-blue-cell-radius.png");
          put(Icons.TOOLBAR_TEMPLATE_CONE, CLASSIC_ICON_DIR + "/tool/temp-blue-cone.png");
          put(Icons.TOOLBAR_TEMPLATE_LINE, CLASSIC_ICON_DIR + "/tool/temp-blue-vertex-line.png");
          put(Icons.TOOLBAR_TEMPLATE_LINE_CELL, CLASSIC_ICON_DIR + "/tool/temp-blue-cell-line.png");
          put(Icons.TOOLBAR_TEMPLATE_BURST, CLASSIC_ICON_DIR + "/tool/temp-blue-burst.png");
          put(Icons.TOOLBAR_TEMPLATE_BLAST, CLASSIC_ICON_DIR + "/tool/temp-blue-square.png");
          put(Icons.TOOLBAR_TEMPLATE_WALL, CLASSIC_ICON_DIR + "/tool/temp-blue-wall.png");
          put(Icons.TOOLBAR_FOG_ON, CLASSIC_ICON_DIR + "/tool/fog-blue.png");
          put(Icons.TOOLBAR_FOG_OFF, CLASSIC_ICON_DIR + "/tool/fog-blue-off.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_BOX, CLASSIC_ICON_DIR + "/tool/fog-blue-rect.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_OVAL, CLASSIC_ICON_DIR + "/tool/fog-blue-oval.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_POLYGON, CLASSIC_ICON_DIR + "/tool/fog-blue-poly.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_FREEHAND, CLASSIC_ICON_DIR + "/tool/fog-blue-free.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_DIAMOND, CLASSIC_ICON_DIR + "/tool/fog-blue-diamond.png");
          put(Icons.TOOLBAR_TOPOLOGY_ON, CLASSIC_ICON_DIR + "/tool/eye-blue.png");
          put(Icons.TOOLBAR_TOPOLOGY_OFF, CLASSIC_ICON_DIR + "/tool/eye-blue-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_BOX, CLASSIC_ICON_DIR + "/tool/top-blue-rect.png");
          put(Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW, CLASSIC_ICON_DIR + "/tool/top-blue-hrect.png");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL, CLASSIC_ICON_DIR + "/tool/top-blue-oval.png");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW, CLASSIC_ICON_DIR + "/tool/top-blue-hoval.png");
          put(Icons.TOOLBAR_TOPOLOGY_POLYGON, CLASSIC_ICON_DIR + "/tool/top-blue-poly.png");
          put(Icons.TOOLBAR_TOPOLOGY_POLYLINE, CLASSIC_ICON_DIR + "/tool/top-blue-free.png");
          put(Icons.TOOLBAR_TOPOLOGY_CROSS, CLASSIC_ICON_DIR + "/tool/top-blue-cross.png");
          put(Icons.TOOLBAR_TOPOLOGY_DIAMOND, CLASSIC_ICON_DIR + "/tool/top-blue-diamond.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW,
              CLASSIC_ICON_DIR + "/tool/top-blue-hdiamond.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON, CLASSIC_ICON_DIR + "/tool/wall-vbl-only.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF,
              CLASSIC_ICON_DIR + "/tool/wall-vbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON, CLASSIC_ICON_DIR + "/tool/hill-vbl-only.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF,
              CLASSIC_ICON_DIR + "/tool/hill-vbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON, CLASSIC_ICON_DIR + "/tool/pit-vbl-only.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF, CLASSIC_ICON_DIR + "/tool/pit-vbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON, CLASSIC_ICON_DIR + "/tool/mbl-only.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF, CLASSIC_ICON_DIR + "/tool/mbl-only-off.png");
          put(Icons.TOOLBAR_VOLUME_ON, CLASSIC_ICON_DIR + "/audio/volume.png");
          put(Icons.TOOLBAR_VOLUME_OFF, CLASSIC_ICON_DIR + "/audio/mute.png");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_ON, CLASSIC_ICON_DIR + "/tool/select-all-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ALL_OFF,
              CLASSIC_ICON_DIR + "/tool/select-all-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_ON, CLASSIC_ICON_DIR + "/tool/select-me-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ME_OFF,
              CLASSIC_ICON_DIR + "/tool/select-me-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_ON, CLASSIC_ICON_DIR + "/tool/select-pc-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_PC_OFF,
              CLASSIC_ICON_DIR + "/tool/select-pc-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_ON, CLASSIC_ICON_DIR + "/tool/select-npc-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_NPC_OFF,
              CLASSIC_ICON_DIR + "/tool/select-npc-blue-off.png");
          put(Icons.TOOLBAR_ZONE, CLASSIC_ICON_DIR + "/tool/btn-world.png");
          put(Icons.WINDOW_CONNECTIONS, CLASSIC_ICON_DIR + "/computer.png");
          put(Icons.WINDOW_MAP_EXPLORER, CLASSIC_ICON_DIR + "/eye.png");
          put(Icons.WINDOW_DRAW_EXPLORER, CLASSIC_ICON_DIR + "/eye.png");
          put(Icons.WINDOW_LIBRARY, CLASSIC_ICON_DIR + "/book_open.png");
          put(Icons.WINDOW_CHAT, CLASSIC_ICON_DIR + "/application.png");
          put(Icons.WINDOW_TABLES, CLASSIC_ICON_DIR + "/layers.png");
          put(Icons.WINDOW_INITIATIVE, CLASSIC_ICON_DIR + "/initiativePanel.png");
          put(Icons.WINDOW_GLOBAL_MACROS, CLASSIC_ICON_DIR + "/global_panel.png");
          put(Icons.WINDOW_CAMPAIGN_MACROS, CLASSIC_ICON_DIR + "/campaign_panel.png");
          put(Icons.WINDOW_GM_MACROS, CLASSIC_ICON_DIR + "/campaign_panel.png");
          put(Icons.WINDOW_SELECTED_TOKEN, CLASSIC_ICON_DIR + "/cursor.png");
          put(Icons.WINDOW_IMPERSONATED_MACROS, CLASSIC_ICON_DIR + "/impersonate.png");
          put(Icons.WINDOW_HTML, CLASSIC_ICON_DIR + "/application.png");
        }
      };

  private static final String ROD_ICON_DIR = CLASSIC_ICON_DIR + "/icons/rod_takehara";
  private static final HashMap<Icons, String> rodIcons =
      new HashMap<>() {
        {
          put(Icons.ACTION_NEW, ROD_ICON_DIR + "/edit/New.svg");
          put(Icons.ACTION_EDIT, ROD_ICON_DIR + "/edit/Edit.svg");
          put(Icons.ACTION_DELETE, ROD_ICON_DIR + "/edit/Delete.svg");
          put(Icons.ACTION_COPY, ROD_ICON_DIR + "/edit/Duplicate.svg");
          put(Icons.ACTION_IMPORT, ROD_ICON_DIR + "/edit/Import.svg");
          put(Icons.ACTION_EXPORT, ROD_ICON_DIR + "/edit/Export.svg");
          put(Icons.ACTION_SETTINGS, ROD_ICON_DIR + "/initiative/Initiative Settings.svg");
          put(Icons.ACTION_NEXT, ROD_ICON_DIR + "/initiative/Next Initiative.svg");
          put(Icons.ACTION_PREVIOUS, ROD_ICON_DIR + "/initiative/Previous Initiative.svg");
          put(Icons.ACTION_RESET, ROD_ICON_DIR + "/initiative/Reset Round.svg");
          put(Icons.ACTION_PAUSE, ROD_ICON_DIR + "/initiative/Toggle Hold Initiative.svg");
          put(Icons.ACTION_SELECT_ALL_TOKENS, ROD_ICON_DIR + "/misc/Select All Tokens.svg");
          put(Icons.ACTION_SELECT_NO_TOKENS, ROD_ICON_DIR + "/misc/Deselect All Tokens.svg");
          put(Icons.ACTION_NEXT_TOKEN, ROD_ICON_DIR + "/misc/Select Previous Token.svg");
          put(Icons.ACTION_PREVIOUS_TOKEN, ROD_ICON_DIR + "/misc/Select Next Token.svg");
          put(
              Icons.ACTION_RESET_TOKEN_SELECTION,
              ROD_ICON_DIR + "/misc/Revert to previous selection (tokens).svg");
          put(
              Icons.CHAT_SHOW_TYPING_NOTIFICATION,
              ROD_ICON_DIR + "/misc/Show Typing notification.svg");
          put(
              Icons.CHAT_HIDE_TYPING_NOTIFICATION,
              ROD_ICON_DIR + "/misc/Hide Typing notification.svg");
          put(Icons.CHAT_SCROLL_LOCK_ON, ROD_ICON_DIR + "/misc/Scroll Lock.svg");
          // put(Icons.CHAT_SCROLL_LOCK_OFF,
          // ROD_ICON_DIR + "/misc/Scroll Lock.svg");
          // put(Icons.EDIT_TOKEN_HEROLAB,
          // ROD_ICON_DIR + "/hero-lab-icon-small.png");
          put(
              Icons.EDIT_TOKEN_COLOR_PICKER,
              ROD_ICON_DIR + "/misc/Colour Selection (eye dropper).svg");
          // put(Icons.EDIT_TOKEN_REFRESH_ON,
          // ROD_ICON_DIR + "/refresh_arrows_small.png");
          // put(Icons.EDIT_TOKEN_REFRESH_OFF,
          // ROD_ICON_DIR + "/refresh_off_arrows_small.png");
          put(Icons.MENU_DOCUMENTATION, ROD_ICON_DIR + "/menu/Documentation.svg");
          put(Icons.MENU_FORUMS, ROD_ICON_DIR + "/menu/Forums.svg");
          put(Icons.MENU_FRAMEWORKS, ROD_ICON_DIR + "/menu/Frameworks.svg");
          put(Icons.MENU_SCRIPTING, ROD_ICON_DIR + "/menu/Macro Scripting.svg");
          put(Icons.MENU_NETWORK_SETUP, ROD_ICON_DIR + "/menu/Network Setup.svg");
          put(Icons.MENU_SHOW_GRIDS, ROD_ICON_DIR + "/menu/Show Grids.svg");
          put(Icons.MENU_SHOW_TOKEN_NAMES, ROD_ICON_DIR + "/menu/Show Token Names.svg");
          put(Icons.MENU_TUTORIALS, ROD_ICON_DIR + "/menu/Tutorials.svg");
          put(Icons.PROPERTIES_TABLE_ALPHABETIC, ROD_ICON_DIR + "/misc/Alphabetic.svg");
          put(Icons.PROPERTIES_TABLE_CATEGORIES, ROD_ICON_DIR + "/misc/Categorised.svg");
          put(Icons.PROPERTIES_TABLE_COLLAPSE, ROD_ICON_DIR + "/misc/Collapse.svg");
          put(Icons.PROPERTIES_TABLE_EXPAND, ROD_ICON_DIR + "/misc/Expand.svg");
          put(
              Icons.PROPERTIES_TABLE_HIDE_DESCRIPTION,
              ROD_ICON_DIR + "/misc/Show - Hide Description Area.svg");
          put(
              Icons.STATUSBAR_SERVER_DISCONNECTED,
              ROD_ICON_DIR + "/bottom/Server Status - Disconected.svg");
          put(
              Icons.STATUSBAR_SERVER_CONNECTED,
              ROD_ICON_DIR + "/bottom/Server Status - Connected.svg");
          put(Icons.STATUSBAR_SERVER_RUNNING, ROD_ICON_DIR + "/bottom/Server Status - Running.svg");
          put(Icons.STATUSBAR_TRANSMIT_ON, ROD_ICON_DIR + "/bottom/Send Data - Active.svg");
          put(Icons.STATUSBAR_TRANSMIT_OFF, ROD_ICON_DIR + "/bottom/Send Data - Inactive.svg");
          put(Icons.STATUSBAR_RECEIVE_ON, ROD_ICON_DIR + "/bottom/Receive Data - Active.svg");
          put(Icons.STATUSBAR_RECEIVE_OFF, ROD_ICON_DIR + "/bottom/Receive Data - Inactive.svg");
          put(Icons.STATUSBAR_ASSET_CACHE, ROD_ICON_DIR + "/bottom/Assets Cache.svg");
          put(Icons.STATUSBAR_FREE_SPACE, ROD_ICON_DIR + "/bottom/Free Space.svg");
          put(Icons.STATUSBAR_IMAGE_CACHE, ROD_ICON_DIR + "/bottom/Image Thumbs Cache.svg");
          put(Icons.TOOLBAR_POINTERTOOL_ON, ROD_ICON_DIR + "/ribbon/Interaction Tools.svg");
          put(Icons.TOOLBAR_POINTERTOOL_OFF, ROD_ICON_DIR + "/ribbon/Interaction Tools.svg");
          put(Icons.TOOLBAR_POINTERTOOL_POINTER, ROD_ICON_DIR + "/ribbon/Pointer Tool.svg");
          put(Icons.TOOLBAR_POINTERTOOL_MEASURE, ROD_ICON_DIR + "/ribbon/Measure Distance.svg");
          put(Icons.TOOLBAR_POINTERTOOL_AI_ON, ROD_ICON_DIR + "/ribbon/Pathing MBL - VBL (AI).svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_AI_OFF,
              ROD_ICON_DIR + "/ribbon/Pathing MBL - VBL (AI) - OFF.svg");
          put(Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON, ROD_ICON_DIR + "/ribbon/Pathing MBL.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF,
              ROD_ICON_DIR + "/ribbon/Pathing MBL - OFF.svg");
          put(Icons.TOOLBAR_DRAW_ON, ROD_ICON_DIR + "/ribbon/Drawing Tools.svg");
          put(Icons.TOOLBAR_DRAW_OFF, ROD_ICON_DIR + "/ribbon/Drawing Tools.svg");
          put(Icons.TOOLBAR_DRAW_DELETE, ROD_ICON_DIR + "/ribbon/Delete Drawing.svg");
          put(Icons.TOOLBAR_DRAW_FREEHAND, ROD_ICON_DIR + "/ribbon/Draw Freehand Lines.svg");
          put(Icons.TOOLBAR_DRAW_LINE, ROD_ICON_DIR + "/ribbon/Draw Straight Lines.svg");
          put(Icons.TOOLBAR_DRAW_BOX, ROD_ICON_DIR + "/ribbon/Draw Rectangle_2.svg");
          put(Icons.TOOLBAR_DRAW_OVAL, ROD_ICON_DIR + "/ribbon/Draw Oval_2.svg");
          put(Icons.TOOLBAR_DRAW_TEXT, ROD_ICON_DIR + "/ribbon/Add Text Label to Map.svg");
          put(Icons.TOOLBAR_DRAW_DIAMOND, ROD_ICON_DIR + "/ribbon/Draw Diamond_2.svg");
          put(Icons.TOOLBAR_TEMPLATE_ON, ROD_ICON_DIR + "/ribbon/Cone Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_OFF, ROD_ICON_DIR + "/ribbon/Cone Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_RADIUS, ROD_ICON_DIR + "/ribbon/Radius Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS_CELL,
              ROD_ICON_DIR + "/ribbon/Radius Template Centered on Grid.svg");
          put(Icons.TOOLBAR_TEMPLATE_CONE, ROD_ICON_DIR + "/ribbon/Cone Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_LINE, ROD_ICON_DIR + "/ribbon/Line Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_LINE_CELL,
              ROD_ICON_DIR + "/ribbon/Line Template Centered on Grid.svg");
          put(Icons.TOOLBAR_TEMPLATE_BURST, ROD_ICON_DIR + "/ribbon/Burst Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_BLAST, ROD_ICON_DIR + "/ribbon/Blast Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_WALL, ROD_ICON_DIR + "/ribbon/Wall Line Template.svg");
          put(Icons.TOOLBAR_FOG_ON, ROD_ICON_DIR + "/ribbon/Fog of War Tools.svg");
          put(Icons.TOOLBAR_FOG_OFF, ROD_ICON_DIR + "/ribbon/Fog of War Tools.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_BOX, ROD_ICON_DIR + "/ribbon/Draw Rectangle.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_OVAL, ROD_ICON_DIR + "/ribbon/Draw Oval.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_POLYGON, ROD_ICON_DIR + "/ribbon/Draw Polygon.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_FREEHAND, ROD_ICON_DIR + "/ribbon/Draw Freehand.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_DIAMOND, ROD_ICON_DIR + "/ribbon/Draw Diamond.svg");
          put(Icons.TOOLBAR_TOPOLOGY_ON, ROD_ICON_DIR + "/ribbon/Vision Blocking Layer Tools.svg");
          put(Icons.TOOLBAR_TOPOLOGY_OFF, ROD_ICON_DIR + "/ribbon/Vision Blocking Layer Tools.svg");
          put(Icons.TOOLBAR_TOPOLOGY_BOX, ROD_ICON_DIR + "/ribbon/Draw Rectangle.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW,
              ROD_ICON_DIR + "/ribbon/Draw Hollow Rectangle.svg");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL, ROD_ICON_DIR + "/ribbon/Draw Oval.svg");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW, ROD_ICON_DIR + "/ribbon/Draw Hollow Oval.svg");
          put(Icons.TOOLBAR_TOPOLOGY_POLYGON, ROD_ICON_DIR + "/ribbon/Draw Polygon.svg");
          put(Icons.TOOLBAR_TOPOLOGY_POLYLINE, ROD_ICON_DIR + "/ribbon/Draw Poly Line.svg");
          put(Icons.TOOLBAR_TOPOLOGY_CROSS, ROD_ICON_DIR + "/ribbon/Draw Cross.svg");
          put(Icons.TOOLBAR_TOPOLOGY_DIAMOND, ROD_ICON_DIR + "/ribbon/Draw Diamond.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW,
              ROD_ICON_DIR + "/ribbon/Draw Hollow Diamond.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON, ROD_ICON_DIR + "/ribbon/Draw Wall VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF, ROD_ICON_DIR + "/ribbon/Draw Wall VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON, ROD_ICON_DIR + "/ribbon/Draw Hill VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF, ROD_ICON_DIR + "/ribbon/Draw Hill VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON, ROD_ICON_DIR + "/ribbon/Draw Pit VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF, ROD_ICON_DIR + "/ribbon/Draw Pit VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON, ROD_ICON_DIR + "/ribbon/Draw MBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF, ROD_ICON_DIR + "/ribbon/Draw MBL.svg");
          put(Icons.TOOLBAR_VOLUME_ON, ROD_ICON_DIR + "/ribbon/Mute - ON.svg");
          put(Icons.TOOLBAR_VOLUME_OFF, ROD_ICON_DIR + "/ribbon/Mute - OFF.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_ON, ROD_ICON_DIR + "/ribbon/All.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_OFF, ROD_ICON_DIR + "/ribbon/All.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_ON, ROD_ICON_DIR + "/ribbon/Me.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_OFF, ROD_ICON_DIR + "/ribbon/Me.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_ON, ROD_ICON_DIR + "/ribbon/PC.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_OFF, ROD_ICON_DIR + "/ribbon/PC.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_ON, ROD_ICON_DIR + "/ribbon/NPC.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_OFF, ROD_ICON_DIR + "/ribbon/NPC.svg");
          put(Icons.TOOLBAR_ZONE, ROD_ICON_DIR + "/ribbon/Select Map.svg");
          put(Icons.WINDOW_CONNECTIONS, ROD_ICON_DIR + "/windows/Connections.svg");
          put(Icons.WINDOW_MAP_EXPLORER, ROD_ICON_DIR + "/windows/Map Explorer.svg");
          put(Icons.WINDOW_DRAW_EXPLORER, ROD_ICON_DIR + "/windows/Draw Explorer.svg");
          put(Icons.WINDOW_LIBRARY, ROD_ICON_DIR + "/windows/Library.svg");
          put(Icons.WINDOW_CHAT, ROD_ICON_DIR + "/windows/Chat.svg");
          put(Icons.WINDOW_TABLES, ROD_ICON_DIR + "/windows/Tables.svg");
          put(Icons.WINDOW_INITIATIVE, ROD_ICON_DIR + "/windows/Initiative.svg");
          put(Icons.WINDOW_GLOBAL_MACROS, ROD_ICON_DIR + "/windows/Global Macros.svg");
          put(Icons.WINDOW_CAMPAIGN_MACROS, ROD_ICON_DIR + "/windows/Campaign Macros.svg");
          put(Icons.WINDOW_GM_MACROS, ROD_ICON_DIR + "/windows/GM Macros.svg");
          put(Icons.WINDOW_SELECTED_TOKEN, ROD_ICON_DIR + "/windows/Selected Token Macros.svg");
          put(Icons.WINDOW_IMPERSONATED_MACROS, ROD_ICON_DIR + "/windows/Impersonated Macros.svg");
          // put(Icons.WINDOW_HTML, "");
        }
      };

  public static final String ROD_TAKEHARA = "Rod Takehara";
  public static final String CLASSIC = "Classic";

  private static String selectedIconSet = ROD_TAKEHARA;

  private static HashMap<Triplet<String, Integer, Integer>, ImageIcon> iconCache = new HashMap<>();

  private static ImageIcon getIcon(Icons icon, int widthAndHeight) {
    return getIcon(icon, widthAndHeight, widthAndHeight);
  }

  public static ImageIcon getSmallIcon(Icons icon) {
    return getIcon(icon, smallIconSize);
  }

  public static ImageIcon getBigIcon(Icons icon) {
    return getIcon(icon, bigIconSize);
  }

  public static int smallIconSize = 16;
  public static int bigIconSize = 32;

  private static ImageIcon getIcon(Icons icon, int width, int height) {
    try {
      String iconPath = null;
      if (selectedIconSet.equals(ROD_TAKEHARA) && rodIcons.containsKey(icon)) {
        iconPath = rodIcons.get(icon);
      } else if (classicIcons.containsKey(icon)) {
        iconPath = classicIcons.get(icon);
      }

      if (iconPath == null) return null;

      var key = Triplet.with(iconPath, width, height);
      if (iconCache.containsKey(key)) return iconCache.get(key);

      ImageIcon imageIcon = null;
      if (iconPath.endsWith(".svg")) {
        imageIcon = new FlatSVGIcon(iconPath, width, height);
        // var image = ImageUtil.getImage(iconPath);
        // imageIcon = new ImageIcon(ImageUtil.createCompatibleImage(image, width, height, null));
      } else {
        // for non-svg we assume that they already have to correct size
        var image = ImageUtil.getCompatibleImage(iconPath);
        imageIcon = new ImageIcon(image);
      }
      iconCache.put(key, imageIcon);
      return imageIcon;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
