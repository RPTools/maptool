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
import java.util.Comparator;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.language.I18N;

@MacroDefinition(
    name = "help",
    aliases = {"h"},
    description = "help.description")
public class HelpMacro implements Macro {
  private static Comparator<Macro> MACRO_NAME_COMPARATOR =
      new Comparator<Macro>() {
        public int compare(Macro macro1, Macro macro2) {
          MacroDefinition def1 = macro1.getClass().getAnnotation(MacroDefinition.class);
          MacroDefinition def2 = macro2.getClass().getAnnotation(MacroDefinition.class);

          return def1.name().compareTo(def2.name());
        }
      };

  public void execute(
      MacroContext context, String parameter, MapToolMacroContext executionContext) {
    StringBuilder builder = new StringBuilder();

    List<Macro> macros = new ArrayList<Macro>(MacroManager.getRegisteredMacros());
    Collections.sort(macros, MACRO_NAME_COMPARATOR);

    builder.append("<table border='1'>");
    builder
        .append("<tr><td><b>")
        .append(I18N.getText("help.header.command"))
        .append("</b></td><td><b>")
        .append(I18N.getText("help.header.aliases"));
    builder
        .append("</b></td><td><b>")
        .append(I18N.getText("help.header.description"))
        .append("</b></td></tr>");
    for (Macro macro : macros) {
      MacroDefinition def = macro.getClass().getAnnotation(MacroDefinition.class);
      if (!def.hidden()) {
        builder.append("<TR>");

        builder.append("<TD>").append(def.name()).append("</TD>");

        builder.append("<td>");
        String[] aliases = def.aliases();
        if (aliases != null && aliases.length > 0) {
          for (int i = 0; i < aliases.length; i++) {
            if (i > 0) {
              builder.append(", ");
            }
            builder.append(aliases[i]);
          }
        }
        builder.append("</td>");

        // Escape HTML from the desciption
        String description =
            I18N.getText(def.description()).replace("<", "&lt;").replace(">", "&gt;");
        builder.append("<TD>").append(description).append("</td>");
      }
    }
    builder.append("</table>");
    MapTool.addLocalMessage(builder.toString());
  }
}
