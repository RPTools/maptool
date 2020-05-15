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
package net.rptools.maptool.client.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.htmlframe.HTMLDialog;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
import net.rptools.maptool.client.ui.htmlframe.HTMLOverlayManager;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class MacroDialogFunctions extends AbstractFunction {
  private static final MacroDialogFunctions instance = new MacroDialogFunctions();

  private MacroDialogFunctions() {
    super(
        1,
        1,
        "isDialogVisible",
        "isFrameVisible",
        "isOverlayRegistered",
        "closeDialog",
        "resetFrame",
        "closeFrame",
        "closeOverlay",
        "getFrameProperties",
        "getDialogProperties",
        "getOverlayProperties");
  }

  public static MacroDialogFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("isDialogVisible")) {
      return HTMLFrameFactory.isVisible(false, parameters.get(0).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equals("isFrameVisible")) {
      return HTMLFrameFactory.isVisible(true, parameters.get(0).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equalsIgnoreCase("isOverlayRegistered")) {
      String name = parameters.get(0).toString();
      return isOverlayRegistered(name) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equals("closeDialog")) {
      HTMLFrameFactory.close(false, parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("closeFrame")) {
      HTMLFrameFactory.close(true, parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("closeOverlay")) {
      String name = parameters.get(0).toString();
      removeOverlay(name);
      return "";
    }
    if (functionName.equals("resetFrame")) {
      HTMLFrame.center(parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("getFrameProperties")) {
      return HTMLFrame.getFrameProperties(parameters.get(0).toString());
    }
    if (functionName.equals("getDialogProperties")) {
      return HTMLDialog.getDialogProperties(parameters.get(0).toString());
    }
    if (functionName.equalsIgnoreCase("getOverlayProperties")) {
      String name = parameters.get(0).toString();
      return getOverlayProperties(name);
    }

    return null;
  }

  /**
   * Returns the overlay properties. If the name is found, returns a json object of the properties;
   * if the name is "*", returns a json array of all the overlays; if the name is not found, returns
   * an empty string.
   *
   * @param name the name of the overlay, or a "*" for all overlays
   * @return either a json array, a json object, or an empty string
   */
  private Object getOverlayProperties(String name) {
    if (name.equals("*")) {
      ConcurrentSkipListSet<HTMLOverlayManager> overlays =
          MapTool.getFrame().getOverlayPanel().getOverlays();
      JsonArray jarr = new JsonArray();
      for (HTMLOverlayManager overlay : overlays) {
        jarr.add(getOverlayProperties(overlay));
      }
      return jarr;
    } else {
      HTMLOverlayManager overlay = MapTool.getFrame().getOverlayPanel().getOverlay(name);
      if (overlay != null) {
        return getOverlayProperties(overlay);
      } else {
        return "";
      }
    }
  }

  /**
   * Returns a JsonObject with the properties of the overlay. Includes name, zorder, and visible.
   *
   * @param overlay the overlay to get the properties from.
   * @return the properties
   */
  private JsonObject getOverlayProperties(HTMLOverlayManager overlay) {
    JsonObject jobj = new JsonObject();
    jobj.addProperty("name", overlay.getName());
    jobj.addProperty("zorder", overlay.getZOrder());
    jobj.addProperty("visible", overlay.isVisible() ? BigDecimal.ONE : BigDecimal.ZERO);
    return jobj;
  }

  /**
   * Removes one overlay, or all of them.
   *
   * @param name the name of the overlay, or "*" if removing all overlays.
   */
  private void removeOverlay(String name) {
    if (name.equals("*")) {
      MapTool.getFrame().getOverlayPanel().removeAllOverlays();
    } else {
      MapTool.getFrame().getOverlayPanel().removeOverlay(name);
    }
  }

  /**
   * Returns whether the overlay is visible.
   *
   * @param name the name of the overlay
   * @return true if it is visible, false otherwise
   */
  private boolean isOverlayRegistered(String name) {
    return MapTool.getFrame().getOverlayPanel().isRegistered(name);
  }
}
