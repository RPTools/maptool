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
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 * 
 */
public class TokensLib extends TwoArgFunction {
	public static MapToolVariableResolver resolver;
		
	public static LuaValue exposed() {
		checkTrusted("exposed");
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		return toLua(zone
				.getTokensFiltered(new FindTokenFunctions.ExposedFilter(zone)));
	}

	public static LuaValue all() {
		checkTrusted("all");
		return toLua(MapTool.getFrame().getCurrentZoneRenderer().getZone()
				.getTokensFiltered(new FindTokenFunctions.AllFilter()));
	}

	public static LuaValue pc() {
		checkTrusted("pc");
		return toLua(MapTool.getFrame().getCurrentZoneRenderer().getZone()
				.getTokensFiltered(new FindTokenFunctions.PCFilter()));
	}

	public static LuaValue npc() {
		checkTrusted("npc");
		return toLua(MapTool.getFrame().getCurrentZoneRenderer().getZone()
				.getTokensFiltered(new FindTokenFunctions.NPCFilter()));
	}

	public static LuaValue selected() {
		return toLua(MapTool.getFrame().getCurrentZoneRenderer()
				.getSelectedTokensList());
	}

	public static LuaValue impersonated() {
		Token t;
		GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
		if (guid != null)
			t = MapTool.getFrame().getCurrentZoneRenderer().getZone()
					.getToken(guid);
		else
			t = MapTool
					.getFrame()
					.getCurrentZoneRenderer()
					.getZone()
					.resolveToken(
							MapTool.getFrame().getCommandPanel().getIdentity());
		if (t == null) {
			return LuaValue.NIL;
		}
		return new MapToolToken(t, true);
	}

	public static LuaValue withState(LuaValue val) {
		checkTrusted("withState");
		return toLua(MapTool
				.getFrame()
				.getCurrentZoneRenderer()
				.getZone()
				.getTokensFiltered(
						new FindTokenFunctions.StateFilter(val.checkjstring())));
	}

	public static LuaValue ownedBy(LuaValue val) {
		checkTrusted("ownedBy");
		return toLua(MapTool
				.getFrame()
				.getCurrentZoneRenderer()
				.getZone()
				.getTokensFiltered(
						new FindTokenFunctions.OwnedFilter(val.checkjstring())));
	}

	public static LuaValue visible(LuaValue val) {
		ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
		Zone zone = zoneRenderer.getZone();
		List<Token> tokenList = new ArrayList<Token>();
		for (GUID id : zoneRenderer.getVisibleTokenSet()) {
			tokenList.add(zone.getToken(id));
		}
		return toLua(tokenList);
	}

	public static LuaValue inLayers(LuaValue val) {
		checkTrusted("inLayers");
		ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
		Zone zone = zoneRenderer.getZone();
		JSONArray layers = new JSONArray();
		if (val.istable()) {
			for (LuaValue v : arrayIterate(val.checktable())) {
				layers.add(v.checkjstring());
			}
		} else {
			layers.add(val.checkstring());
		}
		return toLua(zone.getTokensFiltered(new FindTokenFunctions.LayerFilter(
				layers)));
	}

	public static LuaValue resolve(LuaValue val) {
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		Token token = zone.resolveToken(val.checkjstring());
		if (token != null) {
			return new MapToolToken(token);
		}
		return LuaValue.NIL;
	}
	
	public static LuaValue image(LuaValue val, LuaValue size) {
		try {
			Token t = TokenImage.findImageToken(val.checkjstring(), "tokens.getImage");
			if (t != null && t.getImageAssetId() != null) {
				StringBuilder assetId = new StringBuilder("asset://");
				assetId.append(t.getImageAssetId());
				if (!size.isnil()) {
					assetId.append("-");
					int i = Math.max(size.checkint(), 1);
					assetId.append(i);
				}
				return valueOf(assetId.toString());
			}
			return valueOf("");
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
	
	public static LuaValue find(LuaValue val) {
		checkTrusted("getTokens");
		Token t = null;
		if (resolver != null) {
			t = resolver.getTokenInContext();
		}
		try {
			Object json = LuaConverters.toJson(val);
			if (val.isnil()) {
				return toLua(FindTokenFunctions.getTokens(t, new JSONObject()));
			}
			if (json instanceof JSONObject) {
				return toLua(FindTokenFunctions.getTokens(t, (JSONObject)json));
			} else {
				throw new ParserException("Not a table: " + val.toString());
			}
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		
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

	public static final class Tokens0 extends OneArgFunction {
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

	public static final class Tokens1 extends OneArgFunction {
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
	
	public static final class Tokens2 extends TwoArgFunction {
		public LuaValue call(LuaValue arg, LuaValue arg2) {
			switch (opcode) {
			case 0:
				return image(arg, arg2);
			}
			return NIL;
		}
	}

	
	
	
	Globals globals;

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable t = new LuaTable();
		bind(t, Tokens1.class, new String[] { "withState", "ownedBy", "visible", "inLayers", "resolve", "find", "addAllToInitiative", "addAllPCsToInitiative", "addAllNPCsToInitiative"});
		bind(t, Tokens0.class, new String[] { "exposed", "all", "pc", "npc", "selected", "impersonated", });
		bind(t, Tokens2.class, new String[] { "image" });
		env.set("tokens", t);
		env.get("package").get("loaded").set("tokens", t);
		return t;
	}

	public static void checkTrusted(String cls) {
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new LuaError(new ParserException(I18N.getText(
						"macro.function.general.noPerm", "tokens." + cls)));
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
	}

}
