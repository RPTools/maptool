/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import static net.rptools.maptool.client.functions.ChatFunction.checkForCheating;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.LuaConverters;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class Broadcast extends TwoArgFunction {

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.TwoArgFunction#call(org.luaj.vm2.LuaValue, org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue arg, LuaValue targets) {
		List<String> list = new ArrayList<String>();
		if (targets.istable()) {
			for (LuaValue v: LuaConverters.arrayIterate(targets.checktable())) {
				 list.add(v.checkjstring());
			}
		}
		String message = checkForCheating(arg.checkjstring());
		if (message != null) {
			if (list.isEmpty()) {
				MapTool.addGlobalMessage(message);
			} else {
				MapTool.addGlobalMessage(message, list);
			}
		}
		return LuaValue.NONE;
	}

}
