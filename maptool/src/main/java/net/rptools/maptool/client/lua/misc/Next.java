package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.tokenpanel.InitiativePanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class Next extends ZeroArgFunction {

	@Override
	public LuaValue call() {
		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
		InitiativePanel ip = MapTool.getFrame().getInitiativePanel();
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!ip.hasGMPermission() && (list.getCurrent() <= 0 || !ip.hasOwnerPermission(list.getTokenInitiative(list.getCurrent()).getToken()))) {
				String message = I18N.getText("macro.function.initiative.gmOnly", "nextInitiative");
				if (ip.isOwnerPermissions())
					message = I18N.getText("macro.function.initiative.gmOrOwner", "nextInitiative");
				throw new LuaError(new ParserException(message));
			} // endif
		}
		list.nextInitiative();
		return valueOf(list.getCurrent());
	}

}
