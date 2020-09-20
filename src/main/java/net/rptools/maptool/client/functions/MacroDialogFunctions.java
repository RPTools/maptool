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
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.htmlframe.HTMLDialog;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
import net.rptools.maptool.client.ui.htmlframe.HTMLOverlayManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class MacroDialogFunctions extends AbstractFunction {
  private static final MacroDialogFunctions instance = new MacroDialogFunctions();

  private MacroDialogFunctions() {
    super(
        1,
        5,
        "isDialogVisible",
        "isFrameVisible",
        "isOverlayRegistered",
        "closeDialog",
        "resetFrame",
        "closeFrame",
        "closeOverlay",
        "getFrameProperties",
        "getDialogProperties",
        "getOverlayProperties",
        "runJsFunction");
  }

  public static MacroDialogFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("isDialogVisible")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      return HTMLFrameFactory.isVisible(false, parameters.get(0).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equals("isFrameVisible")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      return HTMLFrameFactory.isVisible(true, parameters.get(0).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equalsIgnoreCase("isOverlayRegistered")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      String name = parameters.get(0).toString();
      return isOverlayRegistered(name) ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    if (functionName.equals("closeDialog")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      HTMLFrameFactory.close(false, parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("closeFrame")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      HTMLFrameFactory.close(true, parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("closeOverlay")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      String name = parameters.get(0).toString();
      removeOverlay(name);
      return "";
    }
    if (functionName.equals("resetFrame")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      HTMLFrame.center(parameters.get(0).toString());
      return "";
    }
    if (functionName.equals("getFrameProperties")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      Optional<JsonObject> props = HTMLFrame.getFrameProperties(parameters.get(0).toString());
      if (props.isPresent()) return props.get();
      else return "";
    }
    if (functionName.equals("getDialogProperties")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      Optional<JsonObject> props = HTMLDialog.getDialogProperties(parameters.get(0).toString());
      if (props.isPresent()) return props.get();
      else return "";
    }
    if (functionName.equalsIgnoreCase("getOverlayProperties")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      String name = parameters.get(0).toString();
      return getOverlayProperties(name);
    }
    if (functionName.equalsIgnoreCase("runJsFunction")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 4, 5);
      String name = parameters.get(0).toString();
      String type = parameters.get(1).toString().trim().toLowerCase();
      String func = parameters.get(2).toString();
      String thisArg = parameters.get(3).toString();
      JsonArray argsArray;
      if (parameters.size() > 4) {
        argsArray = FunctionUtil.paramAsJsonArray(functionName, parameters, 4);
      } else {
        argsArray = new JsonArray();
      }
      runJsFunction(name, type, func, thisArg, argsArray);
      return "";
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
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
        jarr.add(overlay.getProperties());
      }
      return jarr;
    } else {
      HTMLOverlayManager overlay = MapTool.getFrame().getOverlayPanel().getOverlay(name);
      if (overlay != null) {
        return overlay.getProperties();
      } else {
        return "";
      }
    }
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
   * Verify the function and thisarg identifier, then run the script.
   *
   * @param name the name of the frame, dialog or overlay
   * @param type the type of the element - eithe frame, dialog or overlay
   * @param func the name of the function
   * @param thisArg the thisarg argument
   * @param argsArray the arguments of the function
   * @throws ParserException if the name, type, function or thisarg are incorrect
   */
  private void runJsFunction(
      String name, String type, String func, String thisArg, JsonArray argsArray)
      throws ParserException {
    String fName = "runJsFunction";

    // Valid regex match for an identifier.
    Pattern idPattern = Pattern.compile("^[a-zA-Z_$][0-9a-zA-Z_$.]*$");

    // Check validity of function namepublic boolean runScript
    if (!idPattern.matcher(func).matches()) {
      throw new ParserException(I18N.getText("msg.error.dialog.js.id", fName, func));
    }
    // Check validity of thisarg
    if (!idPattern.matcher(thisArg).matches()) {
      throw new ParserException(I18N.getText("msg.error.dialog.js.id", fName, thisArg));
    }

    // Create the script
    String script = func + ".apply(" + thisArg + "," + argsArray.toString() + ");";

    // Execute the script
    boolean executed;
    if (type.equals("frame") || type.equals("frame5")) {
      executed = HTMLFrame.runScript(name, script);
    } else if (type.equals("dialog") || type.equals("dialog5")) {
      executed = HTMLDialog.runScript(name, script);
    } else if (type.equals("overlay")) {
      executed = MapTool.getFrame().getOverlayPanel().runScript(name, script);
    } else {
      throw new ParserException(I18N.getText("msg.error.dialog.js.type", fName, type));
    }
    if (!executed) {
      throw new ParserException(I18N.getText("msg.error.dialog.js.name", fName, type, name));
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
