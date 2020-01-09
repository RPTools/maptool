package net.rptools.maptool.client.macro.impl;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;

@MacroDefinition(
    name = "version",
    aliases = {"v"},
    description = "slashversion.description")

/**
 * This class represents the /version command run from the chat panel.
 */
public class VersionMacro implements Macro {

  @Override
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    String versionString = MapTool.getVersion();
    if ("unspecified".equalsIgnoreCase(versionString)) {
      versionString += " (development build)";
    }
    MapTool.addLocalMessage(I18N.getText("slashversion.message", versionString));
  }
}
