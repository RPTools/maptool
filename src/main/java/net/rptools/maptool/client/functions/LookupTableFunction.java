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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;

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
        "setTableEntry");
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
  public Object childEvaluate(Parser parser, String function, List<Object> params)
      throws ParserException {

    if ("getTableNames".equalsIgnoreCase(function)) {

      checkNumberOfParameters("getTableNames", params, 0, 1);
      String delim = ",";
      if (params.size() > 0) {
        delim = params.get(0).toString();
      }
      if ("json".equalsIgnoreCase(delim))
        return JSONArray.fromObject(getTableList(MapTool.getPlayer().isGM()));

      return StringUtils.join(getTableList(MapTool.getPlayer().isGM()), delim);

    } else if ("getTableVisible".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("getTableVisible", params, 1, 1);
      String name = params.get(0).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      return lookupTable.getVisible() ? "1" : "0";

    } else if ("setTableVisible".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("setTableVisible", params, 2, 2);
      String name = params.get(0).toString();
      String visible = params.get(1).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      lookupTable.setVisible(AbstractTokenAccessorFunction.getBooleanValue(visible));
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return lookupTable.getVisible() ? "1" : "0";

    } else if ("getTableAccess".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("getTableAccess", params, 1, 1);
      String name = params.get(0).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      return lookupTable.getAllowLookup() ? "1" : "0";

    } else if ("setTableAccess".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("setTableAccess", params, 2, 2);
      String name = params.get(0).toString();
      String access = params.get(1).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      lookupTable.setAllowLookup(AbstractTokenAccessorFunction.getBooleanValue(access));
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return lookupTable.getAllowLookup() ? "1" : "0";

    } else if ("getTableRoll".equalsIgnoreCase(function)) {

      checkNumberOfParameters("getTableRoll", params, 1, 1);
      String name = params.get(0).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      return lookupTable.getRoll();

    } else if ("setTableRoll".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("setTableRoll", params, 2, 2);
      String name = params.get(0).toString();
      String roll = params.get(1).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      lookupTable.setRoll(roll);
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return lookupTable.getRoll();

    } else if ("clearTable".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("clearTable", params, 1, 1);
      String name = params.get(0).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      lookupTable.clearEntries();
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return "";

    } else if ("addTableEntry".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("addTableEntry", params, 4, 5);
      String name = params.get(0).toString();
      String min = params.get(1).toString();
      String max = params.get(2).toString();
      String value = params.get(3).toString();
      MD5Key asset = null;
      if (params.size() > 4) {
        asset = getAssetFromString(params.get(4).toString());
      }
      LookupTable lookupTable = getMaptoolTable(name, function);
      lookupTable.addEntry(Integer.valueOf(min), Integer.valueOf(max), value, asset);
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return "";

    } else if ("deleteTableEntry".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("deleteTableEntry", params, 2, 2);
      String name = params.get(0).toString();
      String roll = params.get(1).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      LookupEntry entry = lookupTable.getLookup(roll);
      if (entry != null) {
        List<LookupEntry> oldlist = new ArrayList<LookupEntry>(lookupTable.getEntryList());
        lookupTable.clearEntries();
        for (LookupEntry e : oldlist)
          if (e != entry)
            lookupTable.addEntry(e.getMin(), e.getMax(), e.getValue(), e.getImageId());
      }
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return "";

    } else if ("createTable".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("createTable", params, 3, 4);
      String name = params.get(0).toString();
      String visible = params.get(1).toString();
      String lookups = params.get(2).toString();
      MD5Key asset = null;
      if (params.size() > 3) {
        asset = getAssetFromString(params.get(3).toString());
      }
      LookupTable lookupTable = new LookupTable();
      lookupTable.setName(name);
      lookupTable.setVisible(AbstractTokenAccessorFunction.getBooleanValue(visible));
      lookupTable.setAllowLookup(AbstractTokenAccessorFunction.getBooleanValue(lookups));
      if (asset != null) lookupTable.setTableImage(asset);
      MapTool.getCampaign().getLookupTableMap().put(name, lookupTable);
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return "";

    } else if ("deleteTable".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("deleteTable", params, 1, 1);
      String name = params.get(0).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      if (lookupTable != null) {
        MapTool.getCampaign().getLookupTableMap().remove(name);
        MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      }
      return "";

    } else if ("getTableImage".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("getTableImage", params, 1, 1);
      String name = params.get(0).toString();
      LookupTable lookupTable = getMaptoolTable(name, function);
      return lookupTable.getTableImage();

    } else if ("setTableImage".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("setTableImage", params, 2, 2);
      String name = params.get(0).toString();
      MD5Key asset = getAssetFromString(params.get(1).toString());
      LookupTable lookupTable = getMaptoolTable(name, function);
      lookupTable.setTableImage(asset);
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return "";

    } else if ("copyTable".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("copyTable", params, 2, 2);
      String oldName = params.get(0).toString();
      String newName = params.get(1).toString();
      LookupTable oldTable = getMaptoolTable(oldName, function);
      if (oldTable != null) {
        LookupTable newTable = new LookupTable(oldTable);
        newTable.setName(newName);
        MapTool.getCampaign().getLookupTableMap().put(newName, newTable);
        MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      }
      return "";

    } else if ("setTableEntry".equalsIgnoreCase(function)) {

      checkTrusted(function);
      checkNumberOfParameters("setTableEntry", params, 3, 4);
      String name = params.get(0).toString();
      String roll = params.get(1).toString();
      String result = params.get(2).toString();
      MD5Key imageId = null;
      if (params.size() == 4) {
        imageId = getAssetFromString(params.get(3).toString());
      }
      LookupTable lookupTable = getMaptoolTable(name, function);
      LookupEntry entry = lookupTable.getLookup(roll);
      if (entry == null) return 0; // no entry was found
      int rollInt = Integer.valueOf(roll);
      if (rollInt < entry.getMin() || rollInt > entry.getMax())
        return 0; // entry was found but doesn't match
      List<LookupEntry> oldlist = new ArrayList<LookupEntry>(lookupTable.getEntryList());
      lookupTable.clearEntries();
      for (LookupEntry e : oldlist)
        if (e != entry) {
          lookupTable.addEntry(e.getMin(), e.getMax(), e.getValue(), e.getImageId());
        } else {
          if (imageId == null) imageId = e.getImageId();

          lookupTable.addEntry(e.getMin(), e.getMax(), result, imageId);
        }
      MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
      return 1;

    } else {
      checkNumberOfParameters(function, params, 1, 3);
      String name = params.get(0).toString();

      String roll = null;
      if (params.size() > 1) {
        roll = params.get(1).toString().length() == 0 ? null : params.get(1).toString();
      }

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

      LookupEntry result = lookupTable.getLookup(roll);

      if (function.equals("table") || function.equals("tbl")) {
        String val = result.getValue();
        try {
          BigDecimal bival = new BigDecimal(val);
          return bival;
        } catch (NumberFormatException nfe) {
          return val;
        }
      } else { // We want the image URI

        if (result.getImageId() == null) {
          throw new ParserException(
              I18N.getText("macro.function.LookupTableFunctions.noImage", function, name));
        }

        BigDecimal size = null;
        if (params.size() > 2) {
          if (params.get(2) instanceof BigDecimal) {
            size = (BigDecimal) params.get(2);
          } else {
            throw new ParserException(
                I18N.getText("macro.function.LookupTableFunctions.invalidSize", function));
          }
        }

        StringBuilder assetId = new StringBuilder("asset://");
        assetId.append(result.getImageId().toString());
        if (size != null) {
          int i = Math.max(size.intValue(), 1); // Constrain to a minimum of 1
          assetId.append("-");
          assetId.append(i);
        }
        return assetId.toString();
      }
    }
  }

  /**
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  private void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
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
    List<String> tables = new ArrayList<String>();
    if (isGm) tables.addAll(MapTool.getCampaign().getLookupTableMap().keySet());
    else
      for (LookupTable lt : MapTool.getCampaign().getLookupTableMap().values()) {
        if (lt.getVisible()) tables.add(lt.getName());
      }
    return tables;
  }

  /**
   * Function to return a maptool table.
   *
   * @param tableName String containing the name of the desired table
   * @param functionName String containing the name of the calling function, used by the error
   *     message.
   * @return LookupTable The desired maptool table object
   * @throws ParserException if there were more or less parameters than allowed
   */
  private LookupTable getMaptoolTable(String tableName, String functionName)
      throws ParserException {

    LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(tableName);
    if (!MapTool.getPlayer().isGM() && !lookupTable.getAllowLookup()) {
      if (lookupTable.getVisible()) {
        throw new ParserException(
            functionName + "(): " + I18N.getText("msg.error.tableUnknown") + tableName);
      } else {
        throw new ParserException(
            functionName
                + "(): "
                + I18N.getText("msg.error.tableAccessProhibited")
                + ": "
                + tableName);
      }
    }
    if (lookupTable == null) {
      throw new ParserException(
          I18N.getText(
              "macro.function.LookupTableFunctions.unknownTable", functionName, tableName));
    }
    return lookupTable;
  }

  /**
   * Provide more consistent handling of assets. Allow assets to be passed as 32 digit numbers or
   * "asset://" urls.
   *
   * @param assetString String containing either an asset ID or asset URL.
   * @return MD5Key asset id.
   */
  private MD5Key getAssetFromString(String assetString) {
    if (assetString.toLowerCase().startsWith("asset://")) {
      String id = assetString.substring(8);
      return new MD5Key(id);
    } else {
      return new MD5Key(assetString);
    }
  }
}
