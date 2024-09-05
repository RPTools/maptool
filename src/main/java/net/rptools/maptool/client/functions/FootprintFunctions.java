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
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.exceptions.*;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/**
 * functions for dealing with token footprint retrieval and modification.
 *
 * @author cold_ankles
 */
public class FootprintFunctions extends AbstractFunction {
  public FootprintFunctions() {
    super(
        0,
        3,
        "getTokenFootprints",
        "setTokenFootprint",
        "removeTokenFootprint",
        "getFootprintNames",
        "getGridTypes",
        "resetFootprintsToDefault");
  }

  /** The singleton instance. */
  private static final FootprintFunctions instance = new FootprintFunctions();

  /**
   * Gets the instance.
   *
   * @return the instance.
   */
  public static FootprintFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    String result = "";

    ArrayList<String> gridTypes = new ArrayList<>();
    gridTypes.add("Vertical Hex");
    gridTypes.add("Horizontal Hex");
    gridTypes.add("Square");
    gridTypes.add("None");

    try {
      if (functionName.equalsIgnoreCase("getTokenFootprints")) {
        if ((parameters.size() >= 2
            && !(parameters.get(0) instanceof String)
            && !(parameters.get(1) instanceof String))) {
          throw new ParserException(
              net.rptools.maptool.language.I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  0,
                  parameters.get(0).toString(),
                  parameters.get(1).toString()));
        } else if (parameters.isEmpty()) {
          result = getTokenFootPrints(null, null);
        } else if (parameters.size() > 1) {
          if (!gridTypes.contains(parameters.get(0).toString())) {
            throw new ParserException(
                net.rptools.maptool.language.I18N.getText(
                    "macro.function.footprintFunctions.unknownGridType",
                    parameters.get(0).toString()));
          }
          result = getTokenFootPrints(parameters.get(0).toString(), parameters.get(1).toString());
        } else {
          if (!gridTypes.contains(parameters.get(0).toString())) {
            throw new ParserException(
                net.rptools.maptool.language.I18N.getText(
                    "macro.function.footprintFunctions.unknownGridType",
                    parameters.get(0).toString()));
          }
          result = getTokenFootPrints(parameters.get(0).toString(), null);
        }
      } else if (functionName.equalsIgnoreCase("setTokenFootprint")) {
        FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
        if (!gridTypes.contains(parameters.get(0).toString())) {
          throw new ParserException(
              net.rptools.maptool.language.I18N.getText(
                  "macro.function.footprintFunctions.unknownGridType",
                  parameters.get(0).toString()));
        }
        setTokenFootprint(
            parameters.get(0).toString(),
            parameters.get(1).toString(),
            net.rptools.maptool.util.FunctionUtil.paramAsJsonObject(functionName, parameters, 2));
      } else if (functionName.equalsIgnoreCase("removeTokenFootprint")) {
        FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
        if (!gridTypes.contains(parameters.get(0).toString())) {
          throw new ParserException(
              net.rptools.maptool.language.I18N.getText(
                  "macro.function.footprintFunctions.unknownGridType",
                  parameters.get(0).toString()));
        }
        removeTokenFootprint(parameters.get(0).toString(), parameters.get(1).toString());
      } else if (functionName.equalsIgnoreCase("getFootprintNames")) {
        if ((parameters.size() >= 1 && !(parameters.get(0) instanceof String))) {
          throw new ParserException(
              net.rptools.maptool.language.I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  0,
                  parameters.get(0).toString()));
        } else if (parameters.isEmpty()) {
          result = getFootprintNames(null);
        } else {
          if (!gridTypes.contains(parameters.get(0).toString())) {
            throw new ParserException(
                net.rptools.maptool.language.I18N.getText(
                    "macro.function.footprintFunctions.unknownGridType",
                    parameters.get(0).toString()));
          }
          result = getFootprintNames(parameters.get(0).toString());
        }
      } else if (functionName.equalsIgnoreCase("getGridTypes")) {
        result = "[\"Vertical Hex\",\"Horizontal Hex\",\"Square\",\"None\"]";
      } else if (functionName.equalsIgnoreCase("resetFootprintsToDefault")) {
        resetFootprintsToDefault();
      } else {
        throw new ParserException(
            net.rptools.maptool.language.I18N.getText(
                "macro.function.general.unknownFunction", functionName));
      }
    } catch (PatternSyntaxException e) {
      throw new ParserException(e.getMessage());
    }

    return result;
  }

  /* Returns a String representing the JSON object containing footprints within the given grid type
   * or if gridType is null it returns a JSON object containing all of the above JSON objects for all grids.
   * */
  String getFootprintNames(String gridType) {
    Map<String, List<TokenFootprint>> campaignFootprints =
        net.rptools.maptool.client.MapTool.getCampaign()
            .getCampaignProperties()
            .getGridFootprints();
    if (gridType == null) {
      JsonObject asJSON = new JsonObject();
      for (var entry : campaignFootprints.entrySet()) {
        JsonArray footprintNames = new JsonArray();
        for (TokenFootprint footprint : entry.getValue()) {
          footprintNames.add(footprint.getName());
        }
        asJSON.add(entry.getKey(), footprintNames);
      }
      return asJSON.toString();
    }
    if (!campaignFootprints.containsKey(gridType)) {
      return "null";
    }
    var allFootprints = campaignFootprints.get(gridType).toArray();
    JsonArray footprintNames = new JsonArray();
    for (int i = 0; i < allFootprints.length; i++) {
      TokenFootprint footprint = (TokenFootprint) allFootprints[i];
      footprintNames.add(footprint.getName());
    }
    return footprintNames.toString();
  }

  /* Gets string representation of JSON object containing footprint data for given gridType and footprintName
  if footprint name omitted, it returns a JSON object containing all footprints for given gridType
  if gridType also omitted, it returns all the above JSON Objects for all existing gridtypes
  */
  String getTokenFootPrints(String gridType, String footprintName) {
    Map<String, List<TokenFootprint>> campaignFootprints =
        MapTool.getCampaign().getCampaignProperties().getGridFootprints();
    if (gridType == null) {
      JsonObject asJSON = new JsonObject();
      for (var entry : campaignFootprints.entrySet()) {
        JsonObject footprintListJSON = new JsonObject();
        for (TokenFootprint f : entry.getValue()) {
          footprintListJSON.add(f.getName(), FootprintToJsonObject(f));
        }
        asJSON.add(entry.getKey(), footprintListJSON);
      }
      return asJSON.toString();
    }
    if (!campaignFootprints.containsKey(gridType)) {
      return "null";
    }
    var allFootprints = campaignFootprints.get(gridType).toArray();
    if (footprintName == null) {
      // Get all footprints
      JsonObject asJSON = new JsonObject();
      for (int i = 0; i < allFootprints.length; i++) {
        TokenFootprint footprint = (TokenFootprint) allFootprints[i];
        asJSON.add(footprint.getName(), FootprintToJsonObject(footprint));
      }
      return asJSON.toString();
    } else {
      for (int i = 0; i < allFootprints.length; i++) {
        TokenFootprint footprint = (TokenFootprint) allFootprints[i];
        if (!Objects.equals(footprint.getName(), footprintName)) {
          continue;
        }
        return FootprintToJsonObject(footprint).toString();
      }
      return "null";
    }
  }

  /* sets token footprint under given name/gridtype to the footprint represented in data   */
  void setTokenFootprint(String gridtype, String name, JsonObject data) {
    var cellList = data.get("cells").getAsJsonArray();
    Point[] newCells = new Point[cellList.size()];
    for (var i = 0; i < cellList.size(); i++) {
      var cell = cellList.get(i).getAsJsonObject();
      newCells[i] = new Point(cell.get("x").getAsInt(), cell.get("y").getAsInt());
    }
    TokenFootprint newPrint =
        new TokenFootprint(name, false, data.get("scale").getAsDouble(), newCells);
    if (data.has("localizedName")) {
      newPrint.setLocalizedName(data.get("localizedName").getAsString());
    }
    if (data.has("isDefault")) {
      newPrint.setDefault(data.get("isDefault").getAsBoolean());
    }
    CampaignProperties ModifiedProperties = MapTool.getCampaign().getCampaignProperties();
    ModifiedProperties.setGridFootprint(name, gridtype, newPrint);
    MapTool.getCampaign().mergeCampaignProperties(ModifiedProperties);
  }

  /* removes the footprint named "name" under gridType*/
  void removeTokenFootprint(String gridtype, String name) {
    CampaignProperties ModifiedProperties = MapTool.getCampaign().getCampaignProperties();
    ModifiedProperties.removeGridFootprint(name, gridtype);
    MapTool.getCampaign().mergeCampaignProperties(ModifiedProperties);
  }

  /* Resets all footprints to default, discarding any/all custom or edited footprints */
  void resetFootprintsToDefault() {
    CampaignProperties ModifiedProperties = MapTool.getCampaign().getCampaignProperties();
    ModifiedProperties.resetTokenFootprints();
    MapTool.getCampaign().mergeCampaignProperties(ModifiedProperties);
  }

  public JsonObject FootprintToJsonObject(TokenFootprint footprint) {
    JsonObject jsonRep = new JsonObject();
    JsonArray occupiedString = new JsonArray();
    var cellArray = footprint.getOccupiedCells(new CellPoint(0, 0)).toArray();
    for (int j = 0; j < cellArray.length; j++) {
      CellPoint currentCell = (CellPoint) cellArray[j];
      JsonObject jsonPoint = new JsonObject();
      jsonPoint.addProperty("x", currentCell.x);
      jsonPoint.addProperty("y", currentCell.y);
      occupiedString.add(jsonPoint);
    }
    jsonRep.addProperty("name", footprint.getName());
    jsonRep.add("cells", occupiedString);
    jsonRep.addProperty("scale", footprint.getScale());
    jsonRep.addProperty("localizedName", footprint.getLocalizedName(true));
    jsonRep.addProperty("isDefault", footprint.isDefault());
    return jsonRep;
  }
}
