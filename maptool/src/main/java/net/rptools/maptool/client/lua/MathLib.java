/**
 * 
 */
package net.rptools.maptool.client.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JseMathLib;

/**
 * @author Maluku
 *
 */

public class MathLib extends JseMathLib {
	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue math = super.call(modname, env);
		math.set("hypot", new hypot());
		return math;
	}
	
	static final class hypot extends BinaryOp { protected double call(double d, double o) { return Math.hypot(d, o); } }
}
