/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.lua.LuaConverters;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 *
 */
public class FromStr extends VarArgFunction {
	@Override
	public Varargs invoke(Varargs args) {
		LuaValue val = args.arg(1);
		LuaValue listSep = args.arg(2);
		LuaValue propSep = args.arg(3);
		LuaValue forceList = args.arg(4);
		LuaValue forceProp = args.arg(5);
		return LuaConverters.fromStr(val.checkstring().tojstring(), listSep.isstring() || listSep.isnumber() ? listSep.tojstring() : ",", propSep.isstring() || propSep.isnumber() ? propSep.tojstring() : ";", forceList.isboolean() ? forceList.checkboolean() : (listSep.isstring() || listSep.isnumber()), forceProp.isboolean() ? forceProp.checkboolean() : (propSep.isstring() || propSep.isnumber()));
	}

}
