package net.rptools.maptool.client.functions;

import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppActions.ClientAction;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;

@MacroDefinition(
    name = "about",
    aliases = {"a"},
    description = "slashabout.description")

public class AboutMacro implements Macro {


  @Override
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    ((ClientAction) AppActions.SHOW_ABOUT).execute(null);
  }
}
