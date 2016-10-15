/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 * 
 */
public class UILib extends TwoArgFunction {

	public static LuaValue closeDialog(LuaValue arg) {
		HTMLFrameFactory.close(false, arg.checkjstring());
		return NIL;
	}
	
	public static LuaValue closeFrame(LuaValue arg) {
		HTMLFrameFactory.close(true, arg.checkjstring());
		return NIL;
	}

	public static LuaValue frame(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		HTMLFrameFactory.show(arg.checkjstring(), true, arg3.isnil() ? "" : arg3.checkjstring(), arg2.checkjstring());
		return NIL;
	}

	public static LuaValue dialog(LuaValue arg, LuaValue arg2, LuaValue arg3) {
		HTMLFrameFactory.show(arg.checkjstring(), false, arg3.isnil() ? "" : arg3.checkjstring(), arg2.checkjstring());
		return NIL;
	}

	public static LuaValue isFrameVisible(LuaValue arg) {
		return LuaValue.valueOf(HTMLFrameFactory.isVisible(true, arg.checkjstring()));
	}

	public static LuaValue isDialogVisible(LuaValue arg) {
		return LuaValue.valueOf(HTMLFrameFactory.isVisible(false, arg.checkjstring()));
	}

	public static LuaValue resetFrame(LuaValue arg) {
		HTMLFrame.center(arg.checkjstring());
		return NIL;
	}
	
	public static final class UI1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch (opcode) {
			case 0:
				return closeDialog(arg);
			case 1:
				return closeFrame(arg);
			case 2:
				return resetFrame(arg);
			case 3:
				return isDialogVisible(arg);
			case 4:
				return isFrameVisible(arg);
			}
			return NIL;
		}
	}
	
	public static final class UI3 extends ThreeArgFunction {
		public LuaValue call(LuaValue arg, LuaValue arg2, LuaValue arg3) {
			switch (opcode) {
			case 0:
				return dialog(arg, arg2, arg3);
			case 1:
				return frame(arg, arg2, arg3);
			}
			return NIL;
		}
	}

	Globals globals;

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable t = new LuaTable();
		bind(t, UI1.class, new String[] { "closeDialog", "closeFrame", "resetFrame", "isDialogVisible", "isFrameVisible", });
		bind(t, UI3.class, new String[] { "dialog", "frame", });
		env.set("UI", t);
		env.get("package").get("loaded").set("UI", t);
		return t;
	}

	

	public static void checkTrusted(String cls) {
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new LuaError(new ParserException(I18N.getText(
						"macro.function.general.noPerm", "ui." + cls)));
			}
		}

	}
}
