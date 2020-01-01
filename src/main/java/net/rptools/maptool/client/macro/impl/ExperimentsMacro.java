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

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;

@MacroDefinition(
    name = "experiments",
    aliases = {"exp", "exper"},
    description = "experiments.description")
public class ExperimentsMacro implements Macro {
  @Override
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    // There is only one at the moment, if more are added this will need to be more flexible.
    System.err.println("here!");
    macro = macro.trim();
    if (macro.length() == 0) {
      displayUsage();
    } else {
      String[] args = macro.split("\\s+");
      if ("webapp".equalsIgnoreCase(args[0])) {
        if (args.length < 2) {
          displayUsage();
        } else {
          if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
              String[] dirArgs = args[i].split("=");
              if (dirArgs.length != 2) {
                displayUsage();
                return;
              }
              MapTool.getWebAppServer().addResourceDir(dirArgs[0], dirArgs[1]);
            }
          }
          try {
            int port = Integer.parseInt(args[1]);
            if (MapTool.getWebAppServer().hasStarted()) {
              MapTool.addLocalMessage(I18N.getText("webapp.serverAlreadyRunning"));
            } else {
              MapTool.startWebAppServer(port);
            }
          } catch (NumberFormatException e) {
            displayUsage();
            return;
          }
        }
      }
    }
  }

  private void displayUsage() {
    StringBuffer sb = new StringBuffer();
    sb.append("<table border=1><tr><td><b>")
        .append(I18N.getText("experiments.listTitle"))
        .append("</b></td></tr>");
    sb.append("<tr><td>webapp &lt;port&gt;</td></tr>");
    MapTool.addLocalMessage(I18N.getText(sb.toString()));
  }
}
