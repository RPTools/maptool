package net.rptools.maptool.client.macro.impl;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.drawing.DrawableNoisePaint;


@MacroDefinition(
    name = "texturenoise",
    aliases = {"tn"},
    description = "texturenoise.description"
)
public class TextureNoise implements Macro {

  @Override
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    if (macro.length() == 0) {
      MapTool.addLocalMessage(I18N.getText("texturenoise.currentvals", zr.getNoiseAlpha(), zr.getNoiseSeed()));
      MapTool.addLocalMessage(I18N.getText("texturenoise.usage"));
    } else {
      String args[] = macro.split("\\s+");

      float alpha;
      try {
        alpha = Float.parseFloat(args[0]);
      } catch (NumberFormatException e) {
        MapTool.addLocalMessage(I18N.getText("texturenoise.usage"));
        return;
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
