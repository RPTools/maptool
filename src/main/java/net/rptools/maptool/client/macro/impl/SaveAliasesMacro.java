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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.language.I18N;
import org.apache.commons.io.FileUtils;

@MacroDefinition(
  name = "savealiases",
  aliases = {},
  description = "savealiases.description"
)
public class SaveAliasesMacro implements Macro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    File aliasFile = null;
    if (macro.length() > 0) {
      aliasFile = new File(macro);
    } else {
      JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
      chooser.setDialogTitle("savealiases.dialogTitle");
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      aliasFile = chooser.getSelectedFile();
    }
    if (aliasFile.getName().indexOf(".") < 0) {
      aliasFile = new File(aliasFile.getAbsolutePath() + ".alias");
    }
    if (aliasFile.exists() && !MapTool.confirm(I18N.getText("msg.confirm.fileExists"))) {
      return;
    }

    try {
      StringBuilder builder = new StringBuilder();
      builder
          .append("# ")
          .append(I18N.getText("savealiases.created"))
          .append(" ")
          .append(new SimpleDateFormat().format(new Date()))
          .append("\n\n");

      Map<String, String> aliasMap = MacroManager.getAliasMap();
      List<String> aliasList = new ArrayList<String>();
      aliasList.addAll(aliasMap.keySet());
      Collections.sort(aliasList);
      for (String key : aliasList) {
        String value = aliasMap.get(key);
        builder
            .append(key)
            .append(":")
            .append(value)
            .append("\n"); // LATER: this character should be externalized and shared with the load
        // alias macro
      }
      FileUtils.writeByteArrayToFile(aliasFile, builder.toString().getBytes("UTF-8"));

      MapTool.addLocalMessage(I18N.getText("aliases.saved"));
    } catch (FileNotFoundException fnfe) {
      MapTool.addLocalMessage(
          I18N.getText("savealiases.couldNotSave", I18N.getText("msg.error.fileNotFound")));
    } catch (IOException ioe) {
      MapTool.addLocalMessage(I18N.getText("savealiases.couldNotSave", ioe));
    }
  }
}
