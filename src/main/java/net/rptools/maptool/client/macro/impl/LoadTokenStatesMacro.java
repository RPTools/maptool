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

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import javax.swing.JFileChooser;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.language.I18N;

/**
 * Load the token states from a file.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
@MacroDefinition(
  name = "loadtokenstates",
  aliases = {"tsl"},
  description = "loadtokenstates.description"
)
public class LoadTokenStatesMacro implements Macro {
  /** @see net.rptools.maptool.client.macro.Macro#execute(java.lang.String) */
  @SuppressWarnings("unchecked")
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    // Was the token states file passed?
    File aliasFile = null;
    if (macro.length() > 0) {
      aliasFile = new File(macro);
    } else {
      // Ask the user for the token states file
      JFileChooser chooser = MapTool.getFrame().getLoadFileChooser();
      chooser.setDialogTitle(I18N.getText("loadtokenstates.dialogTitle"));
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) return;
      aliasFile = chooser.getSelectedFile();
    } // endif

    // Make it an XML file if type isn't set, check for existance
    if (aliasFile.getName().indexOf(".") < 0)
      aliasFile = new File(aliasFile.getAbsolutePath() + "-tokenStates.xml");
    if (!aliasFile.exists()) {
      MapTool.addLocalMessage(I18N.getText("loadtokenstates.cantFindFile", aliasFile));
      return;
    } // endif

    // Read the serialized set of states
    try {
      XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(aliasFile)));
      List<BooleanTokenOverlay> overlays = (List<BooleanTokenOverlay>) decoder.readObject();
      decoder.close();
      for (BooleanTokenOverlay overlay : overlays) {
        MapTool.getCampaign().getTokenStatesMap().put(overlay.getName(), overlay);
      } // endfor
      MapTool.addLocalMessage(I18N.getText("loadtokenstates.loaded", overlays.size()));
    } catch (FileNotFoundException e) {
      MapTool.addLocalMessage(
          I18N.getText("loadtokenstates.cantFindFile", I18N.getText("msg.error.fileNotFound")));
    } // endtry
  }
}
