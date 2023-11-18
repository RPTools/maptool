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
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;

@MacroDefinition(
    name = "texturenoise",
    aliases = {"tn"},
    description = "texturenoise.description")
/**
 * This class implements the slash command for adjusting the values for noise to be applied to
 * repeating background textures.
 */
public class TextureNoise implements Macro {

  @Override
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    if (macro.length() == 0) {
      if (zr.isBgTextureNoiseFilterOn()) {
        MapTool.addLocalMessage(
            I18N.getText("texturenoise.currentValsOn", zr.getNoiseAlpha(), zr.getNoiseSeed()));
      } else {
        I18N.getText("texturenoise.currentValsOff");
      }
      MapTool.addLocalMessage(I18N.getText("texturenoise.usage"));
    } else {
      String args[] = macro.split("\\s+");

      if ("off".equalsIgnoreCase(args[0])) {
        zr.setBgTextureNoiseFilterOn(false);
        return;
      }

      if ("on".equalsIgnoreCase(args[0])) {
        zr.setBgTextureNoiseFilterOn(true);
        return;
      }

      float alpha;
      try {
        alpha = Float.parseFloat(args[0]);
      } catch (NumberFormatException e) {
        MapTool.addLocalMessage(I18N.getText("texturenoise.usage"));
        return;
      }

      // Changing noise values so make sure it is on.
      if (!zr.isBgTextureNoiseFilterOn()) {
        zr.setBgTextureNoiseFilterOn(true);
      }
      long seed;
      if (args.length == 1) {
        seed = zr.getNoiseSeed();
      } else {
        try {
          seed = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
          MapTool.addLocalMessage(I18N.getText("texturenoise.usage"));
          return;
        }
      }

      zr.setNoiseValues(seed, alpha);
    }
  }
}
