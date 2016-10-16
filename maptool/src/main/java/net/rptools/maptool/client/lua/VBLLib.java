/**
 * 
 */
package net.rptools.maptool.client.lua;

import static net.rptools.maptool.client.functions.VBL_Functions.drawCircleVBL;
import static net.rptools.maptool.client.functions.VBL_Functions.drawCrossVBL;
import static net.rptools.maptool.client.functions.VBL_Functions.drawPolygonVBL;
import static net.rptools.maptool.client.functions.VBL_Functions.drawRectangleVBL;
import static net.rptools.maptool.client.functions.VBL_Functions.getAreaPoints;
import static net.rptools.maptool.client.functions.VBL_Functions.getVBL;

import java.awt.geom.Area;

import net.rptools.common.expression.Result;
import net.rptools.common.expression.RunData;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.client.functions.VBL_Functions.Shape;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import net.sf.json.JSONObject;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 * 
 */
public class VBLLib extends TwoArgFunction {

	private static LuaValue draw(Varargs args, boolean erase) {
		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
		if (!MapTool.getParser().isMacroPathTrusted())
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", erase ? "VBL.erase" : "VBL.draw")));
		
		for (int i = 1; i <= args.narg(); i++) {
			
			Object arg = LuaConverters.toJson(args.arg(i)).toString().toLowerCase();
			arg = JSONMacroFunctions.convertToJSON((String) arg);
			if (!(arg instanceof JSONObject)) {
				argerror(i, "Table or JSON-String expected");
			}
			JSONObject vblObject = (JSONObject) arg;
			try {
				Shape vblShape = Shape.valueOf(vblObject.getString("shape").toUpperCase());
				switch (vblShape) {
				case RECTANGLE:
					drawRectangleVBL(renderer, vblObject, erase);
					break;
				case POLYGON:
					drawPolygonVBL(renderer, vblObject, erase);
					break;
				case CROSS:
					drawCrossVBL(renderer, vblObject, erase);
					break;
				case CIRCLE:
					drawCircleVBL(renderer, vblObject, erase);
					break;
				}
			} catch (ParserException e) {
				throw new LuaError(e);
			}
		}
		return NONE;
	}
	
	private static LuaValue getV(Varargs args, boolean simple) {
		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
		if (!MapTool.getParser().isMacroPathTrusted())
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", simple ? "VBL.getSimple" : "VBL.get")));
		Area vblArea = null;
		for (int i = 1; i <= args.narg(); i++) {
			Object arg = LuaConverters.toJson(args.arg(i)).toString().toLowerCase();
			arg = JSONMacroFunctions.convertToJSON((String) arg);
			if (!(arg instanceof JSONObject)) {
				argerror(i, "Table or JSON-String expected");
			}
			JSONObject vblObject = (JSONObject) arg;
			try {
				if (vblArea == null) {
					vblArea = getVBL(renderer, vblObject);
				} else {
					vblArea.add(getVBL(renderer, vblObject));
				}
			} catch (ParserException e) {
				throw new LuaError(e);
			}
		}
		return LuaConverters.fromJson(getAreaPoints(vblArea, simple));
	}
	
		
	public static final class VBL extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			if (!RunData.hasCurrent()) {
				RunData.setCurrent(new RunData(new Result("")));
			}
			switch (opcode) {
				case 0:
					return draw(args, false);
				case 1:
					return draw(args, true);
				case 2:
					return getV(args, false);
				case 3:
					return getV(args, true);
			}
			return NIL;
		}
	}

	Globals globals;

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable t = new LuaTable();
		bind(t, VBL.class, new String[] {"draw", "erase", "get", "getSimple"});
		env.set("VBL", t);
		env.get("package").get("loaded").set("VBL", t);
		return t;
	}

	

	public static void checkTrusted(String cls) {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText(
					"macro.function.general.noPerm", "VBL." + cls)));
		}

	}
}
