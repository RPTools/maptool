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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import javax.swing.*;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.ImageBorder;
import org.javatuples.Triplet;

public class RessourceManager {
  private static final String IMAGE_DIR = "net/rptools/maptool/client/image/";
  private static final String ICON_DIR = "net/rptools/maptool/client/icons/";
  private static final HashMap<Icons, String> classicIcons =
      new HashMap<>() {
        {
          // This icons don't exist in classic.
          put(Icons.ACTION_EXPORT, "");
          put(Icons.ACTION_IMPORT, "");
          put(Icons.PROPERTIES_TABLE_ALPHABETIC, "");
          put(Icons.PROPERTIES_TABLE_CATEGORIES, "");
          put(Icons.PROPERTIES_TABLE_COLLAPSE, "");
          put(Icons.PROPERTIES_TABLE_EXPAND, "");
          put(Icons.PROPERTIES_TABLE_HIDE_DESCRIPTION, "");

          put(Icons.ACTION_CANCEL, IMAGE_DIR + "cancel_sm.png");
          put(Icons.ACTION_CLOSE, IMAGE_DIR + "collapse.png");
          put(Icons.ACTION_COPY, IMAGE_DIR + "page_copy.png");
          put(Icons.ACTION_DELETE, IMAGE_DIR + "delete.png");
          put(Icons.ACTION_EDIT, IMAGE_DIR + "pencil.png");
          put(Icons.ACTION_NEW, IMAGE_DIR + "add.png");
          put(Icons.ACTION_NEW_SMALL, IMAGE_DIR + "add_sm.png");
          put(Icons.ACTION_NEXT, IMAGE_DIR + "arrow_right.png");
          put(Icons.ACTION_NEXT_TOKEN, IMAGE_DIR + "arrow_right.png");
          put(Icons.ACTION_OPEN, IMAGE_DIR + "expand.png");
          put(Icons.ACTION_PAUSE, IMAGE_DIR + "arrow_hold.png");
          put(Icons.ACTION_PREVIOUS, IMAGE_DIR + "arrow_left.png");
          put(Icons.ACTION_PREVIOUS_TOKEN, IMAGE_DIR + "arrow_left.png");
          put(Icons.ACTION_RESET, IMAGE_DIR + "arrow_rotate_clockwise.png");
          put(Icons.ACTION_RESET_TOKEN_SELECTION, IMAGE_DIR + "arrow_rotate_clockwise.png");
          put(Icons.ACTION_SELECT_ALL_TOKENS, IMAGE_DIR + "arrow_out.png");
          put(Icons.ACTION_SELECT_NO_TOKENS, IMAGE_DIR + "arrow_in_red.png");
          put(Icons.ACTION_SETTINGS, IMAGE_DIR + "arrow_menu.png");
          put(Icons.ADD_RESSOURCE_LOCAL, IMAGE_DIR + "folder.png");
          put(Icons.ADD_RESSOURCE_RPTOOLS, IMAGE_DIR + "rptools_icon.png");
          put(Icons.ADD_RESSOURCE_WEB, IMAGE_DIR + "download.png");
          put(Icons.ASSETPANEL_HEROLABS, IMAGE_DIR + "hero-lab-icon.png");
          put(Icons.ASSETPANEL_HEROLABS_FOLDER, IMAGE_DIR + "hero_lab_folder.png");
          put(Icons.ASSETPANEL_PDF, IMAGE_DIR + "pdf_icon.png");
          put(Icons.ASSETPANEL_PDF_FOLDER, IMAGE_DIR + "pdf_folder.png");
          put(Icons.ASSETPANEL_SEARCH, IMAGE_DIR + "zoom.png");
          put(Icons.CHAT_HIDE_TYPING_NOTIFICATION, IMAGE_DIR + "chatNotifyOff.png");
          put(Icons.CHAT_NOTIFICATION, IMAGE_DIR + "chat-blue.png");
          put(Icons.CHAT_SCROLL_LOCK_OFF, IMAGE_DIR + "comments_delete.png");
          put(Icons.CHAT_SCROLL_LOCK_ON, IMAGE_DIR + "comments.png");
          put(Icons.CHAT_SHOW_TYPING_NOTIFICATION, IMAGE_DIR + "chatNotifyOn.png");
          put(Icons.CHAT_SMILEY, IMAGE_DIR + "smiley/emsmile.png");
          put(Icons.COLORPICKER_CAP_ROUND, IMAGE_DIR + "round_cap.png");
          put(Icons.COLORPICKER_CAP_SQUARE, IMAGE_DIR + "square_cap.png");
          put(Icons.COLORPICKER_ERASER, IMAGE_DIR + "eraser.png");
          put(Icons.COLORPICKER_OPACITY, IMAGE_DIR + "contrast_high.png");
          put(Icons.COLORPICKER_PENCIL, IMAGE_DIR + "pencil.png");
          put(Icons.COLORPICKER_PEN_WIDTH, IMAGE_DIR + "paintbrush.png");
          put(Icons.COLORPICKER_SNAP_OFF, IMAGE_DIR + "freehand.png");
          put(Icons.COLORPICKER_SNAP_ON, IMAGE_DIR + "shape_handles.png");
          put(Icons.DRAWPANEL_AREA_DRAW, IMAGE_DIR + "tool/drawpanel-poly.png");
          put(Icons.DRAWPANEL_AREA_ERASE, IMAGE_DIR + "tool/drawpanel-poly-erase.png");
          put(Icons.DRAWPANEL_ELLIPSE_DRAW, IMAGE_DIR + "tool/drawpanel-ellipse.png");
          put(Icons.DRAWPANEL_ELLIPSE_ERASE, IMAGE_DIR + "tool/drawpanel-ellipse-erase.png");
          put(Icons.DRAWPANEL_LINE_DRAW, IMAGE_DIR + "tool/drawpanel-line.png");
          put(Icons.DRAWPANEL_LINE_ERASE, IMAGE_DIR + "tool/drawpanel-line-erase.png");
          put(Icons.DRAWPANEL_POLYGON_DRAW, IMAGE_DIR + "tool/drawpanel-poly.png");
          put(Icons.DRAWPANEL_POLYGON_ERASE, IMAGE_DIR + "tool/drawpanel-poly-erase.png");
          put(Icons.DRAWPANEL_RECTANGLE_DRAW, IMAGE_DIR + "tool/drawpanel-rectangle.png");
          put(Icons.DRAWPANEL_RECTANGLE_ERASE, IMAGE_DIR + "tool/drawpanel-rectangle-erase.png");
          put(Icons.DRAWPANEL_TEMPLATE_BLAST, IMAGE_DIR + "tool/drawpanel-temp-blue-square.png");
          put(Icons.DRAWPANEL_TEMPLATE_BURST, IMAGE_DIR + "tool/drawpanel-temp-blue-burst.png");
          put(Icons.DRAWPANEL_TEMPLATE_CONE, IMAGE_DIR + "tool/drawpanel-temp-blue-cone.png");
          put(
              Icons.DRAWPANEL_TEMPLATE_LINE,
              IMAGE_DIR + "tool/drawpanel-temp-blue-vertex-line.png");
          put(
              Icons.DRAWPANEL_TEMPLATE_LINECELL,
              IMAGE_DIR + "tool/drawpanel-temp-blue-cell-line.png");
          put(
              Icons.DRAWPANEL_TEMPLATE_RADIUS,
              IMAGE_DIR + "tool/drawpanel-temp-blue-vertex-radius.png");
          put(
              Icons.DRAWPANEL_TEMPLATE_RADIUSCELL,
              IMAGE_DIR + "tool/drawpanel-temp-blue-cell-radius.png");
          put(Icons.DRAWPANEL_TEMPLATE_WALL, IMAGE_DIR + "tool/drawpanel-temp-blue-wall.png");
          put(Icons.EDIT_TOKEN_COLOR_PICKER, IMAGE_DIR + "color-picker-32.png");
          put(Icons.EDIT_TOKEN_HEROLAB, IMAGE_DIR + "hero-lab-icon-small.png");
          put(Icons.EDIT_TOKEN_REFRESH_OFF, IMAGE_DIR + "refresh_off_arrows_small.png");
          put(Icons.EDIT_TOKEN_REFRESH_ON, IMAGE_DIR + "refresh_arrows_small.png");
          put(Icons.GRID_HEX_HORIZONTAL, IMAGE_DIR + "gridHorizontalHex.png");
          put(Icons.GRID_HEX_VERTICAL, IMAGE_DIR + "gridVerticalHex.png");
          put(Icons.GRID_ISOMETRIC, IMAGE_DIR + "gridIsometric.png");
          put(Icons.GRID_NONE, IMAGE_DIR + "cross.png");
          put(Icons.GRID_SQUARE, IMAGE_DIR + "gridSquare.png");
          put(Icons.INITIATIVE_CURRENT_INDICATOR, IMAGE_DIR + "currentIndicator.png");
          put(Icons.MAPTOOL, IMAGE_DIR + "maptool_icon.png");
          put(Icons.MENU_DOCUMENTATION, IMAGE_DIR + "book_open.png");
          put(Icons.MENU_FORUMS, IMAGE_DIR + "marker.png");
          put(Icons.MENU_FRAMEWORKS, IMAGE_DIR + "minilogo.png");
          put(Icons.MENU_NETWORK_SETUP, IMAGE_DIR + "download.png");
          put(Icons.MENU_SCRIPTING, IMAGE_DIR + "pencil.png");
          put(Icons.MENU_SHOW_GRIDS, IMAGE_DIR + "grid.gif");
          put(Icons.MENU_SHOW_TOKEN_NAMES, IMAGE_DIR + "names.png");
          put(Icons.MENU_TUTORIALS, IMAGE_DIR + "tutorial.jpg");
          put(Icons.STATUSBAR_ASSET_CACHE, IMAGE_DIR + "asset-status.png");
          put(Icons.STATUSBAR_FREE_SPACE, IMAGE_DIR + "disk-space.png");
          put(Icons.STATUSBAR_IMAGE_CACHE, IMAGE_DIR + "thumbnail-status.png");
          put(Icons.STATUSBAR_PLAYERS_DONE_LOADING, IMAGE_DIR + "currentIndicator.png");
          put(Icons.STATUSBAR_PLAYERS_LOADING, IMAGE_DIR + "loading.png");
          put(Icons.STATUSBAR_RECEIVE_OFF, IMAGE_DIR + "activityOff.png");
          put(Icons.STATUSBAR_RECEIVE_ON, IMAGE_DIR + "receiveOn.png");
          put(Icons.STATUSBAR_SERVER_CONNECTED, IMAGE_DIR + "computer_on.png");
          put(Icons.STATUSBAR_SERVER_DISCONNECTED, IMAGE_DIR + "computer_off.png");
          put(Icons.STATUSBAR_SERVER_RUNNING, IMAGE_DIR + "computer_server.png");
          put(Icons.STATUSBAR_TRANSMIT_OFF, IMAGE_DIR + "activityOff.png");
          put(Icons.STATUSBAR_TRANSMIT_ON, IMAGE_DIR + "transmitOn.png");
          put(Icons.TOOLBAR_DRAW_BOX, IMAGE_DIR + "tool/draw-blue-box.png");
          put(Icons.TOOLBAR_DRAW_DELETE, IMAGE_DIR + "delete.png");
          put(Icons.TOOLBAR_DRAW_DIAMOND, IMAGE_DIR + "tool/draw-blue-diamond.png");
          put(Icons.TOOLBAR_DRAW_FREEHAND, IMAGE_DIR + "tool/draw-blue-freehndlines.png");
          put(Icons.TOOLBAR_DRAW_LINE, IMAGE_DIR + "tool/draw-blue-strtlines.png");
          put(Icons.TOOLBAR_DRAW_OFF, IMAGE_DIR + "tool/draw-blue-off.png");
          put(Icons.TOOLBAR_DRAW_ON, IMAGE_DIR + "tool/draw-blue.png");
          put(Icons.TOOLBAR_DRAW_OVAL, IMAGE_DIR + "tool/draw-blue-circle.png");
          put(Icons.TOOLBAR_DRAW_TEXT, IMAGE_DIR + "tool/text-blue.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_BOX, IMAGE_DIR + "tool/fog-blue-rect.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_DIAMOND, IMAGE_DIR + "tool/fog-blue-diamond.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_FREEHAND, IMAGE_DIR + "tool/fog-blue-free.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_OVAL, IMAGE_DIR + "tool/fog-blue-oval.png");
          put(Icons.TOOLBAR_FOG_EXPOSE_POLYGON, IMAGE_DIR + "tool/fog-blue-poly.png");
          put(Icons.TOOLBAR_FOG_OFF, IMAGE_DIR + "tool/fog-blue-off.png");
          put(Icons.TOOLBAR_FOG_ON, IMAGE_DIR + "tool/fog-blue.png");
          put(Icons.TOOLBAR_HIDE_OFF, IMAGE_DIR + "tool/upArrow.png");
          put(Icons.TOOLBAR_HIDE_ON, IMAGE_DIR + "tool/downArrow.png");
          put(Icons.TOOLBAR_POINTERTOOL_AI_OFF, IMAGE_DIR + "tool/ai-blue-off.png");
          put(Icons.TOOLBAR_POINTERTOOL_AI_ON, IMAGE_DIR + "tool/ai-blue-green.png");
          put(Icons.TOOLBAR_POINTERTOOL_MEASURE, IMAGE_DIR + "tool/ruler-blue.png");
          put(Icons.TOOLBAR_POINTERTOOL_OFF, IMAGE_DIR + "tool/pointer-blue-off.png");
          put(Icons.TOOLBAR_POINTERTOOL_ON, IMAGE_DIR + "tool/pointer-blue.png");
          put(Icons.TOOLBAR_POINTERTOOL_POINTER, IMAGE_DIR + "tool/pointer-blue.png");
          put(Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF, IMAGE_DIR + "tool/ignore-vbl-on-move.png");
          put(Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON, IMAGE_DIR + "tool/use-vbl-on-move.png");
          put(Icons.TOOLBAR_TEMPLATE_BLAST, IMAGE_DIR + "tool/temp-blue-square.png");
          put(Icons.TOOLBAR_TEMPLATE_BURST, IMAGE_DIR + "tool/temp-blue-burst.png");
          put(Icons.TOOLBAR_TEMPLATE_CONE, IMAGE_DIR + "tool/temp-blue-cone.png");
          put(Icons.TOOLBAR_TEMPLATE_LINE, IMAGE_DIR + "tool/temp-blue-vertex-line.png");
          put(Icons.TOOLBAR_TEMPLATE_LINE_CELL, IMAGE_DIR + "tool/temp-blue-cell-line.png");
          put(Icons.TOOLBAR_TEMPLATE_OFF, IMAGE_DIR + "tool/temp-blue-off.png");
          put(Icons.TOOLBAR_TEMPLATE_ON, IMAGE_DIR + "tool/temp-blue.png");
          put(Icons.TOOLBAR_TEMPLATE_RADIUS, IMAGE_DIR + "tool/temp-blue-vertex-radius.png");
          put(Icons.TOOLBAR_TEMPLATE_RADIUS_CELL, IMAGE_DIR + "tool/temp-blue-cell-radius.png");
          put(Icons.TOOLBAR_TEMPLATE_WALL, IMAGE_DIR + "tool/temp-blue-wall.png");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_OFF, IMAGE_DIR + "tool/select-all-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_ON, IMAGE_DIR + "tool/select-all-blue.png");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_OFF, IMAGE_DIR + "tool/select-me-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_ON, IMAGE_DIR + "tool/select-me-blue.png");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_OFF, IMAGE_DIR + "tool/select-npc-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_ON, IMAGE_DIR + "tool/select-npc-blue.png");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_OFF, IMAGE_DIR + "tool/select-pc-blue-off.png");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_ON, IMAGE_DIR + "tool/select-pc-blue.png");
          put(Icons.TOOLBAR_TOPOLOGY_BOX, IMAGE_DIR + "tool/top-blue-rect.png");
          put(Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW, IMAGE_DIR + "tool/top-blue-hrect.png");
          put(Icons.TOOLBAR_TOPOLOGY_CROSS, IMAGE_DIR + "tool/top-blue-cross.png");
          put(Icons.TOOLBAR_TOPOLOGY_DIAMOND, IMAGE_DIR + "tool/top-blue-diamond.png");
          put(Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW, IMAGE_DIR + "tool/top-blue-hdiamond.png");
          put(Icons.TOOLBAR_TOPOLOGY_OFF, IMAGE_DIR + "tool/eye-blue-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_ON, IMAGE_DIR + "tool/eye-blue.png");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL, IMAGE_DIR + "tool/top-blue-oval.png");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW, IMAGE_DIR + "tool/top-blue-hoval.png");
          put(Icons.TOOLBAR_TOPOLOGY_POLYGON, IMAGE_DIR + "tool/top-blue-poly.png");
          put(Icons.TOOLBAR_TOPOLOGY_POLYLINE, IMAGE_DIR + "tool/top-blue-free.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF, IMAGE_DIR + "tool/hill-vbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON, IMAGE_DIR + "tool/hill-vbl-only.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF, IMAGE_DIR + "tool/mbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON, IMAGE_DIR + "tool/mbl-only.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF, IMAGE_DIR + "tool/pit-vbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON, IMAGE_DIR + "tool/pit-vbl-only.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF, IMAGE_DIR + "tool/wall-vbl-only-off.png");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON, IMAGE_DIR + "tool/wall-vbl-only.png");
          put(Icons.TOOLBAR_VOLUME_OFF, IMAGE_DIR + "audio/mute.png");
          put(Icons.TOOLBAR_VOLUME_ON, IMAGE_DIR + "audio/volume.png");
          put(Icons.TOOLBAR_ZONE, IMAGE_DIR + "tool/btn-world.png");
          put(Icons.TOOLBAR_ZONE_NOT_VISIBLE, IMAGE_DIR + "notvisible.png");
          put(Icons.WINDOW_CAMPAIGN_MACROS, IMAGE_DIR + "campaign_panel.png");
          put(Icons.WINDOW_CHAT, IMAGE_DIR + "application.png");
          put(Icons.WINDOW_CONNECTIONS, IMAGE_DIR + "computer.png");
          put(Icons.WINDOW_DRAW_EXPLORER, IMAGE_DIR + "eye.png");
          put(Icons.WINDOW_GLOBAL_MACROS, IMAGE_DIR + "global_panel.png");
          put(Icons.WINDOW_GM_MACROS, IMAGE_DIR + "campaign_panel.png");
          put(Icons.WINDOW_HTML, IMAGE_DIR + "application.png");
          put(Icons.WINDOW_IMPERSONATED_MACROS, IMAGE_DIR + "impersonate.png");
          put(Icons.WINDOW_INITIATIVE, IMAGE_DIR + "initiativePanel.png");
          put(Icons.WINDOW_LIBRARY, IMAGE_DIR + "book_open.png");
          put(Icons.WINDOW_LOG, IMAGE_DIR + "log4j_icon.png");
          put(Icons.WINDOW_MAP_EXPLORER, IMAGE_DIR + "eye.png");
          put(Icons.WINDOW_SELECTED_TOKEN, IMAGE_DIR + "cursor.png");
          put(Icons.WINDOW_TABLES, IMAGE_DIR + "layers.png");
        }
      };

  private static final HashMap<Images, String> images =
      new HashMap<>() {
        {
          put(Images.BOX_BLUE, IMAGE_DIR + "blueLabelbox.png");
          put(Images.BOX_DARK_GRAY, IMAGE_DIR + "darkGreyLabelbox.png");
          put(Images.BOX_GRAY, IMAGE_DIR + "grayLabelbox.png");
          put(Images.BROKEN, IMAGE_DIR + "broken.png");
          put(Images.CURSOR_LOOK_HERE, IMAGE_DIR + "look_here.png");
          put(Images.CURSOR_POINTER, IMAGE_DIR + "arrow.png");
          put(Images.CURSOR_THOUGHT, IMAGE_DIR + "thought.png");
          put(Images.DECORATION_HEROLABS, IMAGE_DIR + "hero-lab-decoration.png");
          put(Images.DECORATION_RPTOK, IMAGE_DIR + "rptokIcon.png");
          put(Images.EMPTY, IMAGE_DIR + "empty.png");
          put(Images.GRID_BORDER_HEX, IMAGE_DIR + "hexBorder.png");
          put(Images.GRID_BORDER_ISOMETRIC, IMAGE_DIR + "hexBorder.png");
          put(Images.GRID_BORDER_SQUARE, IMAGE_DIR + "whiteBorder.png");
          put(Images.GRID_BORDER_SQUARE_RED, IMAGE_DIR + "grid-square-red.png");
          put(Images.HEROLABS_PORTRAIT, IMAGE_DIR + "powered_by_hero_lab_small.png");
          put(Images.HEROLABS_TOKEN, IMAGE_DIR + "hero-lab-token.png");
          put(Images.LIGHT_SOURCE, IMAGE_DIR + "lightbulb.png");
          put(Images.LOOKUP_TABLE_DEFAULT, IMAGE_DIR + "document.png");
          put(Images.MAPTOOL_DOCK, IMAGE_DIR + "maptool-dock-icon.png");
          put(Images.MAPTOOL_LOGO, IMAGE_DIR + "maptool-logo.png");
          put(Images.MAPTOOL_LOGO_MINI, IMAGE_DIR + "minilogo.png");
          put(Images.MAPTOOL_SPLASH, IMAGE_DIR + "maptool_splash_template.png");
          put(Images.MEASURE, IMAGE_DIR + "cursor-tape-measure.png");
          put(Images.RESIZE, IMAGE_DIR + "resize.png");
          put(Images.TEXTURE_PANEL, IMAGE_DIR + "panelTexture.jpg");
          put(Images.TEXTURE_SQUARES, IMAGE_DIR + "squaresTexture.png");
          put(Images.TEXTURE_TRANSPARENT, IMAGE_DIR + "transparent.png");
          put(Images.UNKNOWN, IMAGE_DIR + "unknown.png");
          put(Images.ZONE_RENDERER_BLOCK_MOVE, IMAGE_DIR + "block_move.png");
          put(Images.ZONE_RENDERER_CELL_WAYPOINT, IMAGE_DIR + "redDot.png");
          put(Images.ZONE_RENDERER_STACK_IMAGE, IMAGE_DIR + "stack.png");
        }
      };

  private static final HashMap<Borders, String> borders =
      new HashMap<>() {
        {
          put(Borders.BLUE, IMAGE_DIR + "border/blue");
          put(Borders.FOW_TOOLS, IMAGE_DIR + "border/shadow");
          put(Borders.GRAY, IMAGE_DIR + "border/gray");
          put(Borders.GRAY2, IMAGE_DIR + "border/gray2");
          put(Borders.GREEN, IMAGE_DIR + "border/green");
          put(Borders.HIGHLIGHT, IMAGE_DIR + "border/highlight");
          put(Borders.PURPLE, IMAGE_DIR + "border/purple");
          put(Borders.RED, IMAGE_DIR + "border/red");
          put(Borders.SHADOW, IMAGE_DIR + "border/shadow");
          put(Borders.YELLOW, IMAGE_DIR + "border/fowtools");
        }
      };

  private static final String ROD_ICONS = ICON_DIR + "rod_takehara/";
  private static final HashMap<Icons, String> rodIcons =
      new HashMap<>() {
        {
          put(Icons.ACTION_COPY, ROD_ICONS + "edit/Duplicate.svg");
          put(Icons.ACTION_DELETE, ROD_ICONS + "edit/Delete.svg");
          put(Icons.ACTION_EDIT, ROD_ICONS + "edit/Edit.svg");
          put(Icons.ACTION_EXPORT, ROD_ICONS + "edit/Export.svg");
          put(Icons.ACTION_IMPORT, ROD_ICONS + "edit/Import.svg");
          put(Icons.ACTION_NEW, ROD_ICONS + "edit/New.svg");
          put(Icons.ACTION_NEXT, ROD_ICONS + "initiative/Next Initiative.svg");
          put(Icons.ACTION_NEXT_TOKEN, ROD_ICONS + "misc/Select Previous Token.svg");
          put(Icons.ACTION_PAUSE, ROD_ICONS + "initiative/Toggle Hold Initiative.svg");
          put(Icons.ACTION_PREVIOUS, ROD_ICONS + "initiative/Previous Initiative.svg");
          put(Icons.ACTION_PREVIOUS_TOKEN, ROD_ICONS + "misc/Select Next Token.svg");
          put(Icons.ACTION_RESET, ROD_ICONS + "initiative/Reset Round.svg");
          put(
              Icons.ACTION_RESET_TOKEN_SELECTION,
              ROD_ICONS + "misc/Revert to previous selection (tokens).svg");
          put(Icons.ACTION_SELECT_ALL_TOKENS, ROD_ICONS + "misc/Select All Tokens.svg");
          put(Icons.ACTION_SELECT_NO_TOKENS, ROD_ICONS + "misc/Deselect All Tokens.svg");
          put(Icons.ACTION_SETTINGS, ROD_ICONS + "initiative/Initiative Settings.svg");
          put(Icons.CHAT_HIDE_TYPING_NOTIFICATION, ROD_ICONS + "misc/Hide Typing notification.svg");
          put(Icons.CHAT_SCROLL_LOCK_ON, ROD_ICONS + "misc/Scroll Lock.svg");
          put(Icons.CHAT_SHOW_TYPING_NOTIFICATION, ROD_ICONS + "misc/Show Typing notification.svg");
          put(Icons.EDIT_TOKEN_COLOR_PICKER, ROD_ICONS + "misc/Colour Selection (eye dropper).svg");
          put(Icons.MENU_DOCUMENTATION, ROD_ICONS + "menu/Documentation.svg");
          put(Icons.MENU_FORUMS, ROD_ICONS + "menu/Forums.svg");
          put(Icons.MENU_FRAMEWORKS, ROD_ICONS + "menu/Frameworks.svg");
          put(Icons.MENU_NETWORK_SETUP, ROD_ICONS + "menu/Network Setup.svg");
          put(Icons.MENU_SCRIPTING, ROD_ICONS + "menu/Macro Scripting.svg");
          put(Icons.MENU_SHOW_GRIDS, ROD_ICONS + "menu/Show Grids.svg");
          put(Icons.MENU_SHOW_TOKEN_NAMES, ROD_ICONS + "menu/Show Token Names.svg");
          put(Icons.MENU_TUTORIALS, ROD_ICONS + "menu/Tutorials.svg");
          put(Icons.PROPERTIES_TABLE_ALPHABETIC, ROD_ICONS + "misc/Alphabetic.svg");
          put(Icons.PROPERTIES_TABLE_CATEGORIES, ROD_ICONS + "misc/Categorised.svg");
          put(Icons.PROPERTIES_TABLE_COLLAPSE, ROD_ICONS + "misc/Collapse.svg");
          put(Icons.PROPERTIES_TABLE_EXPAND, ROD_ICONS + "misc/Expand.svg");
          put(
              Icons.PROPERTIES_TABLE_HIDE_DESCRIPTION,
              ROD_ICONS + "misc/Show - Hide Description Area.svg");
          put(Icons.STATUSBAR_ASSET_CACHE, ROD_ICONS + "bottom/Assets Cache.svg");
          put(Icons.STATUSBAR_FREE_SPACE, ROD_ICONS + "bottom/Free Space.svg");
          put(Icons.STATUSBAR_IMAGE_CACHE, ROD_ICONS + "bottom/Image Thumbs Cache.svg");
          put(Icons.STATUSBAR_PLAYERS_DONE_LOADING, ROD_ICONS + "misc/Select All Tokens.svg");
          put(Icons.STATUSBAR_PLAYERS_LOADING, ROD_ICONS + "bottom/Assets Cache.svg");
          put(Icons.STATUSBAR_RECEIVE_OFF, ROD_ICONS + "bottom/Receive Data - Inactive.svg");
          put(Icons.STATUSBAR_RECEIVE_ON, ROD_ICONS + "bottom/Receive Data - Active.svg");
          put(Icons.STATUSBAR_SERVER_CONNECTED, ROD_ICONS + "bottom/Server Status - Connected.svg");
          put(
              Icons.STATUSBAR_SERVER_DISCONNECTED,
              ROD_ICONS + "bottom/Server Status - Disconected.svg");
          put(Icons.STATUSBAR_SERVER_RUNNING, ROD_ICONS + "bottom/Server Status - Running.svg");
          put(Icons.STATUSBAR_TRANSMIT_OFF, ROD_ICONS + "bottom/Send Data - Inactive.svg");
          put(Icons.STATUSBAR_TRANSMIT_ON, ROD_ICONS + "bottom/Send Data - Active.svg");
          put(Icons.TOOLBAR_DRAW_BOX, ROD_ICONS + "ribbon/Draw Rectangle_2.svg");
          put(Icons.TOOLBAR_DRAW_DELETE, ROD_ICONS + "ribbon/Delete Drawing.svg");
          put(Icons.TOOLBAR_DRAW_DIAMOND, ROD_ICONS + "ribbon/Draw Diamond_2.svg");
          put(Icons.TOOLBAR_DRAW_FREEHAND, ROD_ICONS + "ribbon/Draw Freehand Lines.svg");
          put(Icons.TOOLBAR_DRAW_LINE, ROD_ICONS + "ribbon/Draw Straight Lines.svg");
          put(Icons.TOOLBAR_DRAW_OFF, ROD_ICONS + "ribbon/Drawing Tools.svg");
          put(Icons.TOOLBAR_DRAW_ON, ROD_ICONS + "ribbon/Drawing Tools.svg");
          put(Icons.TOOLBAR_DRAW_OVAL, ROD_ICONS + "ribbon/Draw Oval_2.svg");
          put(Icons.TOOLBAR_DRAW_TEXT, ROD_ICONS + "ribbon/Add Text Label to Map.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_BOX, ROD_ICONS + "ribbon/Draw Rectangle.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_DIAMOND, ROD_ICONS + "ribbon/Draw Diamond.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_FREEHAND, ROD_ICONS + "ribbon/Draw Freehand.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_OVAL, ROD_ICONS + "ribbon/Draw Oval.svg");
          put(Icons.TOOLBAR_FOG_EXPOSE_POLYGON, ROD_ICONS + "ribbon/Draw Polygon.svg");
          put(Icons.TOOLBAR_FOG_OFF, ROD_ICONS + "ribbon/Fog of War Tools.svg");
          put(Icons.TOOLBAR_FOG_ON, ROD_ICONS + "ribbon/Fog of War Tools.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_AI_OFF,
              ROD_ICONS + "ribbon/Pathing MBL - VBL (AI) - OFF.svg");
          put(Icons.TOOLBAR_POINTERTOOL_AI_ON, ROD_ICONS + "ribbon/Pathing MBL - VBL (AI).svg");
          put(Icons.TOOLBAR_POINTERTOOL_MEASURE, ROD_ICONS + "ribbon/Measure Distance.svg");
          put(Icons.TOOLBAR_POINTERTOOL_OFF, ROD_ICONS + "ribbon/Interaction Tools.svg");
          put(Icons.TOOLBAR_POINTERTOOL_ON, ROD_ICONS + "ribbon/Interaction Tools.svg");
          put(Icons.TOOLBAR_POINTERTOOL_POINTER, ROD_ICONS + "ribbon/Pointer Tool.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF,
              ROD_ICONS + "ribbon/Pathing MBL - OFF.svg");
          put(Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON, ROD_ICONS + "ribbon/Pathing MBL.svg");
          put(Icons.TOOLBAR_TEMPLATE_BLAST, ROD_ICONS + "ribbon/Blast Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_BURST, ROD_ICONS + "ribbon/Burst Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_CONE, ROD_ICONS + "ribbon/Cone Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_LINE, ROD_ICONS + "ribbon/Line Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_LINE_CELL,
              ROD_ICONS + "ribbon/Line Template Centered on Grid.svg");
          put(Icons.TOOLBAR_TEMPLATE_OFF, ROD_ICONS + "ribbon/Cone Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_ON, ROD_ICONS + "ribbon/Cone Template.svg");
          put(Icons.TOOLBAR_TEMPLATE_RADIUS, ROD_ICONS + "ribbon/Radius Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS_CELL,
              ROD_ICONS + "ribbon/Radius Template Centered on Grid.svg");
          put(Icons.TOOLBAR_TEMPLATE_WALL, ROD_ICONS + "ribbon/Wall Line Template.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_OFF, ROD_ICONS + "ribbon/All.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ALL_ON, ROD_ICONS + "ribbon/All.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_OFF, ROD_ICONS + "ribbon/Me.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_ME_ON, ROD_ICONS + "ribbon/Me.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_OFF, ROD_ICONS + "ribbon/NPC.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_NPC_ON, ROD_ICONS + "ribbon/NPC.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_OFF, ROD_ICONS + "ribbon/PC.svg");
          put(Icons.TOOLBAR_TOKENSELECTION_PC_ON, ROD_ICONS + "ribbon/PC.svg");
          put(Icons.TOOLBAR_TOPOLOGY_BOX, ROD_ICONS + "ribbon/Draw Rectangle.svg");
          put(Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW, ROD_ICONS + "ribbon/Draw Hollow Rectangle.svg");
          put(Icons.TOOLBAR_TOPOLOGY_CROSS, ROD_ICONS + "ribbon/Draw Cross.svg");
          put(Icons.TOOLBAR_TOPOLOGY_DIAMOND, ROD_ICONS + "ribbon/Draw Diamond.svg");
          put(Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW, ROD_ICONS + "ribbon/Draw Hollow Diamond.svg");
          put(Icons.TOOLBAR_TOPOLOGY_OFF, ROD_ICONS + "ribbon/Vision Blocking Layer Tools.svg");
          put(Icons.TOOLBAR_TOPOLOGY_ON, ROD_ICONS + "ribbon/Vision Blocking Layer Tools.svg");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL, ROD_ICONS + "ribbon/Draw Oval.svg");
          put(Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW, ROD_ICONS + "ribbon/Draw Hollow Oval.svg");
          put(Icons.TOOLBAR_TOPOLOGY_POLYGON, ROD_ICONS + "ribbon/Draw Polygon.svg");
          put(Icons.TOOLBAR_TOPOLOGY_POLYLINE, ROD_ICONS + "ribbon/Draw Poly Line.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF, ROD_ICONS + "ribbon/Draw Hill VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON, ROD_ICONS + "ribbon/Draw Hill VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF, ROD_ICONS + "ribbon/Draw MBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON, ROD_ICONS + "ribbon/Draw MBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF, ROD_ICONS + "ribbon/Draw Pit VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON, ROD_ICONS + "ribbon/Draw Pit VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF, ROD_ICONS + "ribbon/Draw Wall VBL.svg");
          put(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON, ROD_ICONS + "ribbon/Draw Wall VBL.svg");
          put(Icons.TOOLBAR_VOLUME_OFF, ROD_ICONS + "ribbon/Mute - OFF.svg");
          put(Icons.TOOLBAR_VOLUME_ON, ROD_ICONS + "ribbon/Mute - ON.svg");
          put(Icons.TOOLBAR_ZONE, ROD_ICONS + "ribbon/Select Map.svg");
          put(Icons.WINDOW_CAMPAIGN_MACROS, ROD_ICONS + "windows/Campaign Macros.svg");
          put(Icons.WINDOW_CHAT, ROD_ICONS + "windows/Chat.svg");
          put(Icons.WINDOW_CONNECTIONS, ROD_ICONS + "windows/Connections.svg");
          put(Icons.WINDOW_DRAW_EXPLORER, ROD_ICONS + "windows/Draw Explorer.svg");
          put(Icons.WINDOW_GLOBAL_MACROS, ROD_ICONS + "windows/Global Macros.svg");
          put(Icons.WINDOW_GM_MACROS, ROD_ICONS + "windows/GM Macros.svg");
          put(Icons.WINDOW_IMPERSONATED_MACROS, ROD_ICONS + "windows/Impersonated Macros.svg");
          put(Icons.WINDOW_INITIATIVE, ROD_ICONS + "windows/Initiative.svg");
          put(Icons.WINDOW_LIBRARY, ROD_ICONS + "windows/Library.svg");
          put(Icons.WINDOW_MAP_EXPLORER, ROD_ICONS + "windows/Map Explorer.svg");
          put(Icons.WINDOW_SELECTED_TOKEN, ROD_ICONS + "windows/Selected Token Macros.svg");
          put(Icons.WINDOW_TABLES, ROD_ICONS + "windows/Tables.svg");
        }
      };
  public static final String ROD_TAKEHARA = "Rod Takehara";
  public static final String CLASSIC = "Classic";

  private static HashMap<Triplet<String, Integer, Integer>, ImageIcon> iconCache = new HashMap<>();
  private static HashMap<String, BufferedImage> imageCache = new HashMap<>();
  private static HashMap<String, javafx.scene.image.Image> fxImageCache = new HashMap<>();
  private static HashMap<String, ImageBorder> borderCache = new HashMap<>();

  public static ImageBorder getBorder(Borders border) {
    return getFromHashMapsAndCache(
        border, borderCache, path -> new ImageBorder(path), path -> path, borders);
  }

  public static javafx.scene.image.Image getFxImage(Images image) {
    return getFromHashMapsAndCache(
        image, fxImageCache, path -> new javafx.scene.image.Image(path), path -> path, images);
  }

  public static BufferedImage getImage(Images image) {
    return getFromHashMapsAndCache(
        image,
        imageCache,
        (path) -> {
          try {
            return ImageUtil.getCompatibleImage(path);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        },
        path -> path,
        images);
  }

  private static ImageIcon getIcon(Icons icon, int widthAndHeight) {
    return getIcon(icon, widthAndHeight, widthAndHeight);
  }

  public static int smallIconSize = 16;
  public static int bigIconSize = 32;

  public static ImageIcon getSmallIcon(Icons icon) {
    return getIcon(icon, smallIconSize);
  }

  public static ImageIcon getBigIcon(Icons icon) {
    return getIcon(icon, bigIconSize);
  }

  private static ImageIcon getIcon(Icons icon, int width, int height) {
    var iconPaths = classicIcons;
    switch (AppPreferences.getIconTheme()) {
      case ROD_TAKEHARA -> iconPaths = rodIcons;
    }

    return getFromHashMapsAndCache(
        icon,
        iconCache,
        iconPath -> {
          try {
            if (iconPath.endsWith(".svg")) {
              return new FlatSVGIcon(iconPath, width, height);
            } else {
              // for non-svg we assume that they already have to correct size, unless they are to
              // big
              var image = ImageUtil.getImage(iconPath);
              if (image.getWidth(null) > width || image.getHeight(null) > height)
                image = ImageUtil.createCompatibleImage(image, width, height, null);
              return new ImageIcon(image);
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        },
        iconPath -> Triplet.with(iconPath, width, height),
        iconPaths,
        classicIcons);
  }

  private interface Creator<K, V> {
    V create(K key);
  }

  private static <KEY, CACHEKEY, RESULT> RESULT getFromHashMapsAndCache(
      KEY key,
      HashMap<CACHEKEY, RESULT> cache,
      Creator<String, RESULT> creator,
      Creator<String, CACHEKEY> transformer,
      HashMap<KEY, String>... maps) {
    String ressourcePath = null;
    for (var map : maps) {
      if (map.containsKey(key)) {
        ressourcePath = map.get(key);
        break;
      }
    }
    if (ressourcePath == null) {
      return null;
    }

    CACHEKEY cachekey = transformer.create(ressourcePath);
    if (cache.containsKey(cachekey)) {
      return cache.get(cachekey);
    }

    RESULT ressourceObject = creator.create(ressourcePath);
    cache.put(cachekey, ressourceObject);
    return ressourceObject;
  }

  public static void main(String[] args) {
    checkMissingFiles();
    checkMissingIcons(classicIcons, rodIcons);
    for (var img : Set.of(images.values())) System.out.println(img);
  }

  private static void checkMissingIcons(
      HashMap<Icons, String> classicIcons, HashMap<Icons, String> rodIcons) {
    for (var key : classicIcons.keySet()) {
      if (rodIcons.containsKey(key)) continue;

      System.out.println(
          key + " missing from iconset. File for classic icon is :" + classicIcons.get(key));
    }
  }

  public static void checkMissingFiles() {
    String basedir = "C:\\Users\\tkunze\\Source\\maptool\\src\\main\\resources\\";
    for (String value : Set.copyOf(images.values())) {
      var source = Path.of(basedir, value);
      var target = Path.of(basedir, IMAGE_DIR, "images", source.getFileName().toString());

      if (Files.notExists(source)) {
        System.out.println(value + " is missing!");
      }
    }
    for (String value : Set.copyOf(classicIcons.values())) {
      var source = Path.of(basedir, value);

      if (Files.notExists(source)) {
        System.out.println(value + " is missing!");
      }
    }
    for (String value : Set.copyOf(rodIcons.values())) {
      var source = Path.of(basedir, value);

      if (Files.notExists(source)) {
        System.out.println(value + " is missing!");
      }
    }
  }
}
