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
package net.rptools.maptool.client.macro.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.language.I18N;

/**
 * Macro to clear the message panel
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
@MacroDefinition(
    name = "alias",
    aliases = {"alias"},
    description = "alias.description",
    expandRolls = false)
public class AliasMacro implements Macro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    macro = macro.trim();

    // Request for list ?
    if (macro.length() == 0) {
      handlePrintAliases();
      return;
    }
    // Split into components
    String name = macro;
    String value = null;
    int split =
        macro.indexOf(
            " "); // LATER: this character should be externalized and shared with the load alias
    // macro
    if (split > 0) {
      name = macro.substring(0, split);
      value = macro.substring(split).trim();
    }
    MacroManager.setAlias(name, value);
    if (value != null) {
      MapTool.addLocalMessage(I18N.getText("alias.added", name));
    } else {
      MapTool.addLocalMessage(I18N.getText("alias.removed", name));
    }
  }

  private void handlePrintAliases() {
    StringBuilder builder = new StringBuilder();
    builder.append("<table border='1'>");

    builder
        .append("<tr><td><b>")
        .append(I18N.getText("alias.header"))
        .append("</b></td><td><b>")
        .append(I18N.getText("alias.commandHeader"))
        .append("</b></td></tr>");

    Map<String, String> aliasMap = MacroManager.getAliasMap();
    List<String> nameList = new ArrayList<String>();
    nameList.addAll(aliasMap.keySet());
    Collections.sort(nameList);

    for (String name : nameList) {
      String value = aliasMap.get(name);
      if (value == null) {
        continue;
      }
      value = value.replace("<", "&lt;").replace(">", "&gt;");
      builder
          .append("<tr><td>")
          .append(name)
          .append("</td><td>")
          .append(value)
          .append("</td></tr>");
    }
    builder.append("</table>");
    MapTool.addLocalMessage(builder.toString());
  }
}
