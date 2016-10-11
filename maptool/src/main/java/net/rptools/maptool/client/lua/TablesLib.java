/**
 * 
 */
package net.rptools.maptool.client.lua;

import static net.rptools.maptool.client.lua.LuaConverters.arrayIterate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.TokenImage;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 * 
 */
public class TablesLib extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		// TODO Auto-generated method stub
		return null;
	}
	//TODO
	/*	
	public static LuaValue exposed() {
		checkTrusted("exposed");
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		return toLua(zone
				.getTokensFiltered(new FindTokenFunctions.ExposedFilter(zone)));
	}
	
	public static LuaValue create(LuaValue tableName, LuaValue visible, LuaValue accessible, LuaValue imageId) {
		checkTrusted("create");
		return 
	}
	
	public static LuaValue addAllToInitiative(Boolean pc, LuaValue duplicates) {
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
				throw new LuaError(new ParserException(I18N.getText("macro.function.initiative.mustBeGM", "addAll" + (pc!=null? (pc.booleanValue() ? "PCs":"NPCs") :"")+"ToInitiative")));
		}
		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
		boolean allowDuplicates = false;
		if (duplicates.isboolean()) {
			allowDuplicates = duplicates.toboolean();
		} 
		List<Token> tokens = new ArrayList<Token>();
		for (Token token : list.getZone().getTokens())
			if ((pc == null || token.getType() == Type.PC && pc.booleanValue() || token.getType() == Type.NPC && !pc.booleanValue())
					&& (allowDuplicates || list.indexOf(token).isEmpty())) {
				tokens.add(token);
			} 
		list.insertTokens(tokens);
		return LuaValue.valueOf(tokens.size());
	}

	public static final class Tables0 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch (opcode) {
			case 0:
				return exposed();
			case 1:
				return all();
			case 2:
				return pc();
			case 3:
				return npc();
			case 4:
				return selected();
			case 5:
				return impersonated();
			}
			return NIL;
		}
	}

	public static final class Tables1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch (opcode) {
			case 0:
				return withState(arg);
			case 1:
				return ownedBy(arg);
			case 2:
				return visible(arg);
			case 3:
				return inLayers(arg);
			case 4:
				return resolve(arg);
			case 5:
				return find(arg);
			case 6:
				return addAllToInitiative(null, arg);
			case 7:
				return addAllToInitiative(true, arg);
			case 8:
				return addAllToInitiative(false, arg);
			}
			return NIL;
		}
	}
	
	public static final class Tables2 extends TwoArgFunction {
		public LuaValue call(LuaValue arg, LuaValue arg2) {
			switch (opcode) {
			case 0:
				return image(arg, arg2);
			}
			return NIL;
		}
	}
	
	public static final class Tables3 extends ThreeArgFunction {
		public LuaValue call(LuaValue arg, LuaValue arg2, LuaValue arg3) {
			switch (opcode) {
			case 0:
				return image(varargs.arg1(),varargs.arg(2),varargs.arg(3),varargs.arg(4));
			}
			return NIL;
		}
	}
	
	public static final class Tables4 extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs varargs) {
			switch (opcode) {
			case 0:
				return create(varargs.arg1(),varargs.arg(2),varargs.arg(3),varargs.arg(4));
			}
			return NIL;
		}
	}

	
	
	
	Globals globals;

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable t = new LuaTable();
		bind(t, Tables0.class, new String[] { });
		bind(t, Tables1.class, new String[] { });
		bind(t, Tables2.class, new String[] { });
		bind(t, Tables3.class, new String[] { });
		bind(t, Tables4.class, new String[] { });
		env.set("tokens", t);
		env.get("package").get("loaded").set("tokens", t);
		return t;
	}

	public static void checkTrusted(String cls) {
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new LuaError(new ParserException(I18N.getText(
						"macro.function.general.noPerm", "tables." + cls)));
			}
		}

	}

	private static LuaValue toLua(Collection<? extends Token> tokens) {
		LuaTable result = new LuaTable();
		for (Token t : tokens) {
			if (resolver != null && t == resolver.getTokenInContext()) result.insert(0, new MapToolToken(t, true));
			else result.insert(0, new MapToolToken(t));
		}
		return result;
	}*/

}
