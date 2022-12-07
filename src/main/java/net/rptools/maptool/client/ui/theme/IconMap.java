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

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import net.rptools.lib.image.ImageUtil;

public class IconMap {
  public enum Icons {
    STATUSBAR_SERVER_CONNECTED,
    STATUSBAR_SERVER_DISCONNECTED,
    STATUSBAR_SERVER_RUNNING,
    STATUSBAR_TRANSMIT_ON,
    STATUSBAR_TRANSMIT_OFF,
    STATUSBAR_RECEIVE_ON,
    STATUSBAR_RECEIVE_OFF,
    STATUSBAR_ASSET_CACHE,
    STATUSBAR_FREE_SPACE,
    STATUSBAR_IMAGE_CACHE,
    TOOLBAR_POINTERTOOL_ON,
    TOOLBAR_POINTERTOOL_OFF,
    TOOLBAR_POINTERTOOL_POINTER,
    TOOLBAR_POINTERTOOL_MEASURE,
    TOOLBAR_POINTERTOOL_AI_ON,
    TOOLBAR_POINTERTOOL_AI_OFF,
    TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON,
    TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF,
    TOOLBAR_DRAW_ON,
    TOOLBAR_DRAW_OFF,
    TOOLBAR_DRAW_DELETE,
    TOOLBAR_DRAW_FREEHAND,
    TOOLBAR_DRAW_LINE,
    TOOLBAR_DRAW_BOX,
    TOOLBAR_DRAW_OVAL,
    TOOLBAR_DRAW_TEXT,
    TOOLBAR_DRAW_DIAMOND,
    TOOLBAR_TEMPLATE_ON,
    TOOLBAR_TEMPLATE_OFF,
    TOOLBAR_TEMPLATE_RADIUS,
    TOOLBAR_TEMPLATE_RADIUS_CELL,
    TOOLBAR_TEMPLATE_CONE,
    TOOLBAR_TEMPLATE_LINE,
    TOOLBAR_TEMPLATE_LINE_CELL,
    TOOLBAR_TEMPLATE_BURST,
    TOOLBAR_TEMPLATE_BLAST,
    TOOLBAR_TEMPLATE_WALL,
    TOOLBAR_FOG_ON,
    TOOLBAR_FOG_OFF,
    TOOLBAR_FOG_EXPOSE_BOX,
    TOOLBAR_FOG_EXPOSE_OVAL,
    TOOLBAR_FOG_EXPOSE_POLYGON,
    TOOLBAR_FOG_EXPOSE_FREEHAND,
    TOOLBAR_FOG_EXPOSE_DIAMOND,
    TOOLBAR_TOPOLOGY_ON,
    TOOLBAR_TOPOLOGY_OFF,
    TOOLBAR_TOPOLOGY_BOX,
    TOOLBAR_TOPOLOGY_BOX_HOLLOW,
    TOOLBAR_TOPOLOGY_OVAL,
    TOOLBAR_TOPOLOGY_OVAL_HOLLOW,
    TOOLBAR_TOPOLOGY_POLYGON,
    TOOLBAR_TOPOLOGY_POLYLINE,
    TOOLBAR_TOPOLOGY_CROSS,
    TOOLBAR_TOPOLOGY_DIAMOND,
    TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW,
    TOOLBAR_TOPOLOGY_TYPE_VBL_ON,
    TOOLBAR_TOPOLOGY_TYPE_VBL_OFF,
    TOOLBAR_TOPOLOGY_TYPE_PIT_ON,
    TOOLBAR_TOPOLOGY_TYPE_PIT_OFF,
    TOOLBAR_TOPOLOGY_TYPE_HILL_ON,
    TOOLBAR_TOPOLOGY_TYPE_HILL_OFF,
    TOOLBAR_TOPOLOGY_TYPE_MBL_ON,
    TOOLBAR_TOPOLOGY_TYPE_MBL_OFF,
    TOOLBAR_VOLUME_ON,
    TOOLBAR_VOLUME_OFF,
    TOOLBAR_TOKENSELECTION_ME_ON,
    TOOLBAR_TOKENSELECTION_ME_OFF,
    TOOLBAR_TOKENSELECTION_ALL_ON,
    TOOLBAR_TOKENSELECTION_ALL_OFF,
    TOOLBAR_TOKENSELECTION_PC_ON,
    TOOLBAR_TOKENSELECTION_PC_OFF,
    TOOLBAR_TOKENSELECTION_NPC_ON,
    TOOLBAR_TOKENSELECTION_NPC_OFF,
    TOOLBAR_ZONE,
  }

  private static final HashMap<Icons, String> classicIcons =
      new HashMap<>() {
        {
          put(
              Icons.STATUSBAR_SERVER_DISCONNECTED,
              "net/rptools/maptool/client/image/computer_off.png");
          put(Icons.STATUSBAR_SERVER_CONNECTED, "net/rptools/maptool/client/image/computer_on.png");
          put(
              Icons.STATUSBAR_SERVER_RUNNING,
              "net/rptools/maptool/client/image/computer_server.png");
          put(Icons.STATUSBAR_TRANSMIT_ON, "net/rptools/maptool/client/image/transmitOn.png");
          put(Icons.STATUSBAR_TRANSMIT_OFF, "net/rptools/maptool/client/image/activityOff.png");
          put(Icons.STATUSBAR_RECEIVE_ON, "net/rptools/maptool/client/image/receiveOn.png");
          put(Icons.STATUSBAR_RECEIVE_OFF, "net/rptools/maptool/client/image/activityOff.png");
          put(Icons.STATUSBAR_ASSET_CACHE, "net/rptools/maptool/client/image/asset-status.png");
          put(Icons.STATUSBAR_FREE_SPACE, "net/rptools/maptool/client/image/disk-space.png");
          put(Icons.STATUSBAR_IMAGE_CACHE, "net/rptools/maptool/client/image/thumbnail-status.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_ON,
              "net/rptools/maptool/client/image/tool/pointer-blue.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_OFF,
              "net/rptools/maptool/client/image/tool/pointer-blue-off.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_POINTER,
              "net/rptools/maptool/client/image/tool/pointer-blue.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_MEASURE,
              "net/rptools/maptool/client/image/tool/ruler-blue.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_AI_ON,
              "net/rptools/maptool/client/image/tool/ai-blue-green.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_AI_OFF,
              "net/rptools/maptool/client/image/tool/ai-blue-off.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON,
              "net/rptools/maptool/client/image/tool/use-vbl-on-move.png");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF,
              "net/rptools/maptool/client/image/tool/ignore-vbl-on-move.png");
          put(Icons.TOOLBAR_DRAW_ON, "net/rptools/maptool/client/image/tool/draw-blue.png");
          put(Icons.TOOLBAR_DRAW_OFF, "net/rptools/maptool/client/image/tool/draw-blue-off.png");
          put(Icons.TOOLBAR_DRAW_DELETE, "net/rptools/maptool/client/image/delete.png");
          put(
              Icons.TOOLBAR_DRAW_FREEHAND,
              "net/rptools/maptool/client/image/tool/draw-blue-freehndlines.png");
          put(
              Icons.TOOLBAR_DRAW_LINE,
              "net/rptools/maptool/client/image/tool/draw-blue-strtlines.png");
          put(Icons.TOOLBAR_DRAW_BOX, "net/rptools/maptool/client/image/tool/draw-blue-box.png");
          put(
              Icons.TOOLBAR_DRAW_OVAL,
              "net/rptools/maptool/client/image/tool/draw-blue-circle.png");
          put(Icons.TOOLBAR_DRAW_TEXT, "net/rptools/maptool/client/image/tool/text-blue.png");
          put(
              Icons.TOOLBAR_DRAW_DIAMOND,
              "net/rptools/maptool/client/image/tool/draw-blue-diamond.png");
          put(Icons.TOOLBAR_TEMPLATE_ON, "net/rptools/maptool/client/image/tool/temp-blue.png");
          put(
              Icons.TOOLBAR_TEMPLATE_OFF,
              "net/rptools/maptool/client/image/tool/temp-blue-off.png");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS,
              "net/rptools/maptool/client/image/tool/temp-blue-vertex-radius.png");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS_CELL,
              "net/rptools/maptool/client/image/tool/temp-blue-cell-radius.png");
          put(
              Icons.TOOLBAR_TEMPLATE_CONE,
              "net/rptools/maptool/client/image/tool/temp-blue-cone.png");
          put(
              Icons.TOOLBAR_TEMPLATE_LINE,
              "net/rptools/maptool/client/image/tool/temp-blue-vertex-line.png");
          put(
              Icons.TOOLBAR_TEMPLATE_LINE_CELL,
              "net/rptools/maptool/client/image/tool/temp-blue-cell-line.png");
          put(
              Icons.TOOLBAR_TEMPLATE_BURST,
              "net/rptools/maptool/client/image/tool/temp-blue-burst.png");
          put(
              Icons.TOOLBAR_TEMPLATE_BLAST,
              "net/rptools/maptool/client/image/tool/temp-blue-square.png");
          put(
              Icons.TOOLBAR_TEMPLATE_WALL,
              "net/rptools/maptool/client/image/tool/temp-blue-wall.png");
          put(Icons.TOOLBAR_FOG_ON, "net/rptools/maptool/client/image/tool/fog-blue.png");
          put(Icons.TOOLBAR_FOG_OFF, "net/rptools/maptool/client/image/tool/fog-blue-off.png");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_BOX,
              "net/rptools/maptool/client/image/tool/fog-blue-rect.png");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_OVAL,
              "net/rptools/maptool/client/image/tool/fog-blue-oval.png");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_POLYGON,
              "net/rptools/maptool/client/image/tool/fog-blue-poly.png");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_FREEHAND,
              "net/rptools/maptool/client/image/tool/fog-blue-free.png");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_DIAMOND,
              "net/rptools/maptool/client/image/tool/fog-blue-diamond.png");
          put(Icons.TOOLBAR_TOPOLOGY_ON, "net/rptools/maptool/client/image/tool/eye-blue.png");
          put(Icons.TOOLBAR_TOPOLOGY_OFF, "net/rptools/maptool/client/image/tool/eye-blue-off.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_BOX,
              "net/rptools/maptool/client/image/tool/top-blue-rect.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW,
              "net/rptools/maptool/client/image/tool/top-blue-hrect.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_OVAL,
              "net/rptools/maptool/client/image/tool/top-blue-oval.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW,
              "net/rptools/maptool/client/image/tool/top-blue-hoval.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_POLYGON,
              "net/rptools/maptool/client/image/tool/top-blue-poly.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_POLYLINE,
              "net/rptools/maptool/client/image/tool/top-blue-free.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_CROSS,
              "net/rptools/maptool/client/image/tool/top-blue-cross.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_DIAMOND,
              "net/rptools/maptool/client/image/tool/top-blue-diamond.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW,
              "net/rptools/maptool/client/image/tool/top-blue-hdiamond.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON,
              "net/rptools/maptool/client/image/tool/wall-vbl-only.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF,
              "net/rptools/maptool/client/image/tool/wall-vbl-only-off.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON,
              "net/rptools/maptool/client/image/tool/hill-vbl-only.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF,
              "net/rptools/maptool/client/image/tool/hill-vbl-only-off.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON,
              "net/rptools/maptool/client/image/tool/pit-vbl-only.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF,
              "net/rptools/maptool/client/image/tool/pit-vbl-only-off.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON,
              "net/rptools/maptool/client/image/tool/mbl-only.png");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF,
              "net/rptools/maptool/client/image/tool/mbl-only-off.png");
          put(Icons.TOOLBAR_VOLUME_ON, "net/rptools/maptool/client/image/audio/volume.png");
          put(Icons.TOOLBAR_VOLUME_OFF, "net/rptools/maptool/client/image/audio/mute.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ALL_ON,
              "net/rptools/maptool/client/image/tool/select-all-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ALL_OFF,
              "net/rptools/maptool/client/image/tool/select-all-blue-off.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ME_ON,
              "net/rptools/maptool/client/image/tool/select-me-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ME_OFF,
              "net/rptools/maptool/client/image/tool/select-me-blue-off.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_PC_ON,
              "net/rptools/maptool/client/image/tool/select-pc-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_PC_OFF,
              "net/rptools/maptool/client/image/tool/select-pc-blue-off.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_NPC_ON,
              "net/rptools/maptool/client/image/tool/select-npc-blue.png");
          put(
              Icons.TOOLBAR_TOKENSELECTION_NPC_OFF,
              "net/rptools/maptool/client/image/tool/select-npc-blue-off.png");
          put(Icons.TOOLBAR_ZONE, "net/rptools/maptool/client/image/tool/btn-world.png");
        }
      };

  private static final HashMap<Icons, String> rodIcons =
      new HashMap<>() {
        {
          put(
              Icons.STATUSBAR_SERVER_DISCONNECTED,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Server Status - Disconected.svg");
          put(
              Icons.STATUSBAR_SERVER_CONNECTED,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Server Status - Connected.svg");
          put(
              Icons.STATUSBAR_SERVER_RUNNING,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Server Status - Running.svg");
          put(
              Icons.STATUSBAR_TRANSMIT_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Send Data - Active.svg");
          put(
              Icons.STATUSBAR_TRANSMIT_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Send Data - Inactive.svg");
          put(
              Icons.STATUSBAR_RECEIVE_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Receive Data - Active.svg");
          put(
              Icons.STATUSBAR_RECEIVE_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Receive Data - Inactive.svg");
          put(
              Icons.STATUSBAR_ASSET_CACHE,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Assets Cache.svg");
          put(
              Icons.STATUSBAR_FREE_SPACE,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Free Space.svg");
          put(
              Icons.STATUSBAR_IMAGE_CACHE,
              "net/rptools/maptool/client/image/icons/rod_takehara/bottom/Image Thumbs Cache.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Interaction Tools.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Interaction Tools.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_POINTER,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Pointer Tool.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_MEASURE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Measure Distance.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_AI_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Pathing MBL - VBL (AI).svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_AI_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Pathing MBL - VBL (AI) - OFF.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Pathing MBL.svg");
          put(
              Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Pathing MBL - OFF.svg");
          put(
              Icons.TOOLBAR_DRAW_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Drawing Tools.svg");
          put(
              Icons.TOOLBAR_DRAW_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Drawing Tools.svg");
          put(
              Icons.TOOLBAR_DRAW_DELETE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Delete Drawing.svg");
          put(
              Icons.TOOLBAR_DRAW_FREEHAND,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Freehand Lines.svg");
          put(
              Icons.TOOLBAR_DRAW_LINE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Straight Lines.svg");
          put(
              Icons.TOOLBAR_DRAW_BOX,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Rectangle_2.svg");
          put(
              Icons.TOOLBAR_DRAW_OVAL,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Oval_2.svg");
          put(
              Icons.TOOLBAR_DRAW_TEXT,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Add Text Label to Map.svg");
          put(
              Icons.TOOLBAR_DRAW_DIAMOND,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Diamond_2.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Cone Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Cone Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Radius Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_RADIUS_CELL,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Radius Template Centered on Grid.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_CONE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Cone Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_LINE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Line Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_LINE_CELL,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Line Template Centered on Grid.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_BURST,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Burst Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_BLAST,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Blast Template.svg");
          put(
              Icons.TOOLBAR_TEMPLATE_WALL,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Wall Line Template.svg");
          put(
              Icons.TOOLBAR_FOG_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Fog of War Tools.svg");
          put(
              Icons.TOOLBAR_FOG_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Fog of War Tools.svg");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_BOX,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Rectangle.svg");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_OVAL,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Oval.svg");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_POLYGON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Polygon.svg");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_FREEHAND,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Freehand.svg");
          put(
              Icons.TOOLBAR_FOG_EXPOSE_DIAMOND,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Diamond.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Vision Blocking Layer Tools.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Vision Blocking Layer Tools.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_BOX,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Rectangle.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Hollow Rectangle.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_OVAL,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Oval.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Hollow Oval.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_POLYGON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Polygon.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_POLYLINE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Poly Line.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_CROSS,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Cross.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_DIAMOND,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Diamond.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Hollow Diamond.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Wall VBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Wall VBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Hill VBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Hill VBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Pit VBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw Pit VBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw MBL.svg");
          put(
              Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Draw MBL.svg");
          put(
              Icons.TOOLBAR_VOLUME_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Mute - ON.svg");
          put(
              Icons.TOOLBAR_VOLUME_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Mute - OFF.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ALL_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/All.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ALL_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/All.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ME_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Me.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_ME_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Me.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_PC_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/PC.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_PC_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/PC.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_NPC_ON,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/NPC.svg");
          put(
              Icons.TOOLBAR_TOKENSELECTION_NPC_OFF,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/NPC.svg");
          put(
              Icons.TOOLBAR_ZONE,
              "net/rptools/maptool/client/image/icons/rod_takehara/ribbon/Select Map.svg");
        }
      };

  public static final String ROD_TAKEHARA = "Rod Takehara";
  public static final String CLASSIC = "Classic";

  private static String selectedIconSet = ROD_TAKEHARA;

  public static Image getIcon(Icons icon, int widthAndHeight) {
    return getIcon(icon, widthAndHeight, widthAndHeight);
  }

  public static Image getIcon(Icons icon, int width, int height) {
    try {
      var iconPath = "net/rptools/maptool/client/image/unknown.png";
      if (selectedIconSet.equals(ROD_TAKEHARA) && rodIcons.containsKey(icon)) {
        iconPath = rodIcons.get(icon);
      } else if (classicIcons.containsKey(icon)) {
        iconPath = classicIcons.get(icon);
      }

      return ImageUtil.getImage(iconPath).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
