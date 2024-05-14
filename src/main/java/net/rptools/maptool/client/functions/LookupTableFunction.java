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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LookupTableFunction extends AbstractFunction {

  public LookupTableFunction() {
    super(
        0,
        5,
        "tbl",
        "table",
        "tblImage",
        "tableImage",
        "getTableNames",
        "getTableRoll",
        "setTableRoll",
        "clearTable",
        "loadTable",        // bulk table import
        "addTableEntry",
        "deleteTableEntry",
        "createTable",
        "deleteTable",
        "getTableVisible",
        "setTableVisible",
        "getTableAccess",
        "setTableAccess",
        "getTableImage",
        "setTableImage",
        "copyTable",
        "getTableEntry",
        "setTableEntry",
        "resetTablePicks",
        "getTablePickOnce",
        "setTablePickOnce",
        "getTablePicksLeft");
  }

  /** The singleton instance. */
  private static final LookupTableFunction instance = new LookupTableFunction();

  /**
   * Gets the instance of TableLookup.
   *
   * @return the TableLookup.
   */
  public static LookupTableFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String function, List<Object> params)
      throws ParserException {

    if ("getTableNames".equalsIgnoreCase(function)) {
      return getTableNames(params);
    } else if ("getTableVisible".equalsIgnoreCase(function)) {
      return getTableVisible(function, params);
    } else if ("setTableVisible".equalsIgnoreCase(function)) {
      return setTableVisible(function, params);
    } else if ("getTableAccess".equalsIgnoreCase(function)) {
      return getTableAccess(function, params);
    } else if ("setTableAccess".equalsIgnoreCase(function)) {
      return setTableAccess(function, params);
    } else if ("getTableRoll".equalsIgnoreCase(function)) {
      return getTableRoll(function, params);
    } else if ("setTableRoll".equalsIgnoreCase(function)) {
      return setTableRoll(function, params);
    } else if ("clearTable".equalsIgnoreCase(function)) {
      return clearTable(function, params);
    } else if ("loadTable".equalsIgnoreCase(function)) {
      return loadTable(function, params);
    } else if ("addTableEntry".equalsIgnoreCase(function)) {
      return addTableEntry(function, params);
    } else if ("deleteTableEntry".equalsIgnoreCase(function)) {
      return deleteTableEntry(function, params);
    } else if ("createTable".equalsIgnoreCase(function)) {
      return createTable(function, params);
    } else if ("deleteTable".equalsIgnoreCase(function)) {
      return deleteTable(function, params);
    } else if ("getTableImage".equalsIgnoreCase(function)) {
      return getTableImage(function, params);
    } else if ("setTableImage".equalsIgnoreCase(function)) {
      return setTableImage(function, params);
    } else if ("copyTable".equalsIgnoreCase(function)) {
      return copyTable(function, params);
    } else if ("setTableEntry".equalsIgnoreCase(function)) {
      return setTableEntry(function, params);
    } else if ("getTableEntry".equalsIgnoreCase(function)) {
      return getTableEntry(function, params);
    } else if ("resetTablePicks".equalsIgnoreCase(function)) {
      return resetTablePicks(function, params);
    } else if ("setTablePickOnce".equalsIgnoreCase(function)) {
      return setTablePickOnce(function, params);
    } else if ("getTablePickOnce".equalsIgnoreCase(function)) {
      return getTablePickOnce(function, params);
    } else if ("getTablePicksLeft".equalsIgnoreCase(function)) {
      return getTablePicksLeft(function, params);
    } else { // if tbl, table, tblImage or tableImage
      return tbl_and_table(function, params);
    }
  }

  private @Nullable Serializable tbl_and_table(String function, List<Object> params) throws ParserException {
    FunctionUtil.checkNumberParam(function, params, 1, 3);
    String name = params.get(0).toString();

    String roll = null;
    if (params.size() > 1) {
      roll = params.get(1).toString().isEmpty() ? null : params.get(1).toString();
    }

    LookupTable lookupTable = checkTableAccess(function, name);
    LookupEntry result = lookupTable.getLookup(roll);
    if (result == null) {
      return null;
    }

    assert result.getValue() != null;
    if (result.getValue().equals(LookupTable.NO_PICKS_LEFT)) {
      return result.getValue();
    }

    if (function.equalsIgnoreCase("table") || function.equalsIgnoreCase("tbl")) {
      String val = result.getValue();
      try {
        return new BigDecimal(val);
      } catch (NumberFormatException nfe) {
        return val;
      }
    } else if (function.equalsIgnoreCase("tableImage") || function.equalsIgnoreCase("tblImage")) { // We want the image URI through tblImage or tableImage
      if (result.getImageId() == null) {
        return "";
      }
      StringBuilder assetId = new StringBuilder("asset://");
      assetId.append(result.getImageId().toString());

      if (params.size() > 2) {
        try {
          Integer imageSize = FunctionUtil.paramAsInteger(function, params, 2, false);
          int i = Math.max(imageSize, 1); // Constrain to a minimum of 1
          assetId.append("-");
          assetId.append(i);
        } catch (ParserException pe) {
          throw new ParserException(
              I18N.getText("macro.function.LookupTableFunctions.invalidSize", function));
        }
      }
      return assetId.toString();
    } else {
      throw new ParserException(I18N.getText("macro.function.general.unknownFunction", function));
    }
  }

  /**
   * Returns the lookupTable object identified by <code>name</code> if
   * it exists in the campaign.  Otherwise, throws <code>ParserException</code>.
   * @param function name of MTscript function (for error message)
   * @param name name of table
   * @return table identified by <code>name</code>
   * @throws ParserException Thrown for any access errors to the named table (unknown, access prohibited, or table doesn't exist)
   */
  private LookupTable checkTableAccess(String function, String name) throws ParserException {
    LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(name);
    if (!MapTool.getPlayer().isGM() && !lookupTable.getAllowLookup()) {
      if (lookupTable.getVisible()) {
        throw new ParserException(
            function + "(): " + I18N.getText("msg.error.tableUnknown") + name);
      } else {
        throw new ParserException(
            function + "(): " + I18N.getText("msg.error.tableAccessProhibited") + ": " + name);
      }
    }
    if (lookupTable == null) {
      throw new ParserException(
          I18N.getText("macro.function.LookupTableFunctions.unknownTable", function, name));
    }
    return lookupTable;
  }

  private int getTablePicksLeft(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("getTablePicksLeft", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    return lookupTable.getPicksLeft();
  }

  private @NotNull BigDecimal getTablePickOnce(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("getTablePickOnce", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    return lookupTable.getPickOnce() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private @NotNull BigDecimal setTablePickOnce(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("setTablePickOnce", params, 2, 2);
    String name = params.get(0).toString();
    String pickonce = params.get(1).toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.setPickOnce(FunctionUtil.getBooleanValue(pickonce));
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return lookupTable.getPickOnce() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Resets the entries on a table in regards to whether such entries have been
   * selected already.  This is used in the PickOnce family of tables.
   *
   * resetTablePicks(tblName) - reset all entries on a table
   * resetTablePicks(tblName, entriesToReset) - reset specific entries from a String List with "," delim
   * resetTablePicks(tblName, entriesToReset, delim) - use custom delimiter
   * resetTablePicks(tblName, entriesToReset, "json") - entriesToReset is a JsonArray
   *
   * @param function name of the MTscript function (used for error messages)
   * @param params list of function parameters
   * @throws ParserException Thrown when an invalid or missing parameter is supplied
   */
  private @NotNull String resetTablePicks(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam(function, params, 1, 3);
    String tblName = params.get(0).toString();
    LookupTable lookupTable = checkTableAccess(tblName, function);
    if (params.size() > 1) {
      String delim = (params.size() > 2) ? params.get(2).toString() : ",";
      List<String> entriesToReset;
      if (delim.equalsIgnoreCase("json")) {
        JsonArray jsonArray = FunctionUtil.paramAsJsonArray(function, params, 1);
        entriesToReset =
            JSONMacroFunctions.getInstance()
                .getJsonArrayFunctions()
                .jsonArrayToListOfStrings(jsonArray);
      } else {
        entriesToReset = StrListFunctions.toList(params.get(1).toString(), delim);
      }
      lookupTable.reset(entriesToReset);
    } else {
      lookupTable.reset();
    }
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull Object getTableEntry(String function, List<Object> params) throws ParserException {
    FunctionUtil.checkNumberParam(function, params, 2, 2);
    String name = params.get(0).toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    String roll = params.get(1).toString();
    LookupEntry entry = lookupTable.getLookupDirect(roll);
    if (entry == null) return "";

    int rollInt = Integer.parseInt(roll);
    if (rollInt < entry.getMin() || rollInt > entry.getMax())
      return "";

    JsonObject entryDetails = new JsonObject();
    entryDetails.addProperty("min", entry.getMin());
    entryDetails.addProperty("max", entry.getMax());
    entryDetails.addProperty("value", entry.getValue());
    entryDetails.addProperty("picked", entry.getPicked());

    MD5Key imageId = entry.getImageId();
    if (imageId != null) {
      entryDetails.addProperty("assetid", "asset://" + imageId.toString());
    } else {
      entryDetails.addProperty("assetid", "");
    }
    return entryDetails;
  }

  private int setTableEntry(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("setTableEntry", params, 3, 4);
    String name = params.get(0).toString();
    String roll = params.get(1).toString();
    String result = params.get(2).toString();
    MD5Key imageId = null;
    if (params.size() == 4) {
      imageId = FunctionUtil.getAssetKeyFromString(params.get(3).toString());
    }
    LookupTable lookupTable = checkTableAccess(name, function);
    LookupEntry entry = lookupTable.getLookup(roll);
    if (entry == null) return 0;
    int rollInt = Integer.parseInt(roll);
    if (rollInt < entry.getMin() || rollInt > entry.getMax())
      return 0;
    List<LookupEntry> oldlist = new ArrayList<>(lookupTable.getEntryList());
    lookupTable.clearEntries();
    for (LookupEntry e : oldlist) {
      if (e != entry) {
        lookupTable.addEntry(e.getMin(), e.getMax(), e.getValue(), e.getImageId());
      } else {
        if (imageId == null) imageId = e.getImageId();

        lookupTable.addEntry(e.getMin(), e.getMax(), result, imageId);
      }
    }
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return 1;
  }

  private @NotNull String copyTable(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("copyTable", params, 2, 2);
    String oldName = params.get(0).toString();
    String newName = params.get(1).toString();
    LookupTable oldTable = checkTableAccess(oldName, function);
    LookupTable newTable = new LookupTable(oldTable);
    newTable.setName(newName);
    MapTool.getCampaign().getLookupTableMap().put(newName, newTable);
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull String setTableImage(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("setTableImage", params, 2, 2);
    String name = params.get(0).toString();
    MD5Key asset = FunctionUtil.getAssetKeyFromString(params.get(1).toString());
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.setTableImage(asset);
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull Serializable getTableImage(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("getTableImage", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    MD5Key img = lookupTable.getTableImage();
    // Returning null causes an NPE when output is dumped to chat.
    return Objects.requireNonNullElse(img, "");
  }

  private @NotNull String deleteTable(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("deleteTable", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    MapTool.getCampaign().getLookupTableMap().remove(name);
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull String createTable(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("createTable", params, 3, 4);
    String name = params.get(0).toString();
    String visible = params.get(1).toString();
    String lookups = params.get(2).toString();
    MD5Key asset = null;
    if (params.size() > 3) {
      asset = FunctionUtil.getAssetKeyFromString(params.get(3).toString());
    }
    LookupTable lookupTable = new LookupTable();
    lookupTable.setName(name);
    lookupTable.setVisible(FunctionUtil.getBooleanValue(visible));
    lookupTable.setAllowLookup(FunctionUtil.getBooleanValue(lookups));
    if (asset != null) lookupTable.setTableImage(asset);
    MapTool.getCampaign().getLookupTableMap().put(name, lookupTable);
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull String deleteTableEntry(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("deleteTableEntry", params, 2, 2);
    String name = params.get(0).toString();
    String roll = params.get(1).toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    LookupEntry entry = lookupTable.getLookup(roll);
    if (entry != null) {
      List<LookupEntry> oldlist = new ArrayList<>(lookupTable.getEntryList());
      lookupTable.clearEntries();
      oldlist.stream()
          .filter((e) -> (e != entry))
          .forEachOrdered(
              (e) -> lookupTable.addEntry(e.getMin(), e.getMax(), e.getValue(), e.getImageId()));
    }
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull String addTableEntry(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("addTableEntry", params, 4, 5);
    String name = params.get(0).toString();
    String min = params.get(1).toString();
    String max = params.get(2).toString();
    String value = params.get(3).toString();
    MD5Key asset = null;
    if (params.size() > 4) {
      asset = FunctionUtil.getAssetKeyFromString(params.get(4).toString());
    }
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.addEntry(Integer.parseInt(min), Integer.parseInt(max), value, asset);
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  private @NotNull String clearTable(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("clearTable", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.clearEntries();
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return "";
  }

  /**
   * Loads bulk data into a table.  The table must already exist.  Usage:
   * <p>
   * {@code [h: loadTable(name, jsondata)]}
   * </p>
   * <p>
   * The {@code jsondata} must be a JSON Array containing table entries, and
   * each table entry must be a JSON Array containing:
   * <ol>
   *  <li>an integer representing the lower range of the die roll (decimals are truncated),</li>
   *  <li>an integer representing the upper range of the die roll (decimals are truncated),</li>
   *  <li>a String acting as the content of the row, and</li>
   *  <li>a String acting as an image reference (as {@code asset://}, {@code image:}, or {@code MD5Key})</li>
   * </ol>
   * </p>
   * @param function name of this MTscript function, "{@code loadTable}"
   * @param params parameters of the MTscript function
   * @return a number indicating the successful record count, or
   * a {@link JsonArray} of all elements that failed to load
   * @throws ParserException Thrown when syntax errors are detected in the input data.
   * This includes negative numbers in either of the numeric fields of a table entry,
   * a lower die roll that is greater than the upper die roll of a table entry and vice versa,
   * missing fields, too many fields, and potentially many more.
   */
  private @NotNull JsonElement loadTable(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam(function, params, 3, 3);
    String name = params.get(0).toString();
    LookupTable lookupTable = checkTableAccess(name, function);

    Object data = params.get(1);
    if (data instanceof String) {
      data = com.google.gson.JsonParser.parseString(data.toString());
    }
    if (data instanceof JsonArray json) {
      JsonArray errorRows = new JsonArray();
      long counter = 0, rowindex = 0;
      // Verify that all records are themselves JSONArray objects and each has 4 fields
      for (JsonElement row : json) {
        try {
          if (row instanceof JsonArray array && array.size() == 4) {
            int min = array.get(0).getAsInt();
            int max = array.get(1).getAsInt();
            String value = array.get(2).getAsString();
            String image = array.get(3).getAsString();
            if (min >= 1 && max >= min && !value.isEmpty()) {
              // Image is allowed to be empty, but if given, it must be an asset ID
              MD5Key imageID = null;
              if (!image.isEmpty()) {
                imageID = FunctionUtil.getAssetKeyFromString(image);
              }
              // All checks complete, add this row to the table
              lookupTable.addEntry(min, max, value, imageID);
              counter++;
            }
          }
        } catch (UnsupportedOperationException|NumberFormatException|IllegalStateException e) {
          // Make a list of all failing rows to return to the user.
          errorRows.add(rowindex);
        }
        rowindex++;
      }
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return counter == rowindex ? new JsonPrimitive(counter) : errorRows;
    }
    // FIXME Report error in standardized format
    throw new ParserException("Second parameter must be a JSON Array");
  }

  private String setTableRoll(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("setTableRoll", params, 2, 2);
    String name = params.get(0).toString();
    String roll = params.get(1).toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.setRoll(roll);
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return lookupTable.getDefaultRoll();
  }

  private String getTableRoll(String function, List<Object> params) throws ParserException {
    FunctionUtil.checkNumberParam("getTableRoll", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    return lookupTable.getDefaultRoll();
  }

  private @NotNull BigDecimal setTableAccess(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("setTableAccess", params, 2, 2);
    String name = params.get(0).toString();
    String access = params.get(1).toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.setAllowLookup(FunctionUtil.getBooleanValue(access));
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return lookupTable.getAllowLookup() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private @NotNull BigDecimal getTableAccess(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("getTableAccess", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    return lookupTable.getAllowLookup() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private @NotNull BigDecimal setTableVisible(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("setTableVisible", params, 2, 2);
    String name = params.get(0).toString();
    String visible = params.get(1).toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    lookupTable.setVisible(FunctionUtil.getBooleanValue(visible));
    MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
    return lookupTable.getVisible() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private @NotNull BigDecimal getTableVisible(String function, List<Object> params) throws ParserException {
    checkTrusted(function);
    FunctionUtil.checkNumberParam("getTableVisible", params, 1, 1);
    String name = params.getFirst().toString();
    LookupTable lookupTable = checkTableAccess(name, function);
    return lookupTable.getVisible() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  private Object getTableNames(List<Object> params) throws ParserException {
    FunctionUtil.checkNumberParam("getTableNames", params, 0, 1);
    String delim = ",";
    if (!params.isEmpty()) {
      delim = params.getFirst().toString();
    }
    if ("json".equalsIgnoreCase(delim)) {
      JsonArray jsonArray = new JsonArray();
      getTableList(MapTool.getPlayer().isGM()).forEach(jsonArray::add);
      return jsonArray;
    }
    return StringUtils.join(getTableList(MapTool.getPlayer().isGM()), delim);
  }

  /**
   * Checks whether or not the function is trusted
   *
   * @param functionName Name of the macro function
   * @throws ParserException Returns trust error message and function name
   */
  private void checkTrusted(String functionName) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
  }

  /**
   * * If GM return all tables Otherwise only return visible tables
   *
   * @param isGm boolean Does the calling function has GM privileges
   * @return a list of table names
   */
  private List<String> getTableList(boolean isGm) {
    List<String> tables = new ArrayList<>();
    if (isGm) tables.addAll(MapTool.getCampaign().getLookupTableMap().keySet());
    else
      MapTool.getCampaign().getLookupTableMap().values().stream()
          .filter(LookupTable::getVisible)
          .forEachOrdered((lt) -> tables.add(lt.getName()));
    return tables;
  }
}
