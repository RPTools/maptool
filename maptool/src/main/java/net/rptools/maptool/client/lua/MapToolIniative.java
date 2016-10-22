package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ReadOnlyLuaTable;
import net.rptools.maptool.client.lua.misc.Next;
import net.rptools.maptool.client.lua.misc.SortIniative;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.InitiativeListModel;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class MapToolIniative extends LuaTable {
	MapToolVariableResolver resolver;
	public MapToolIniative(MapToolVariableResolver res) {
		resolver = res;
		super.rawset(valueOf("current"), valueOf(-1));
		super.rawset(valueOf("round"), valueOf(-1));
		super.rawset(valueOf("map"), valueOf(""));
		super.rawset(valueOf("tokens"), new LuaTable());
		super.rawset(valueOf("next"), new Next());
		super.rawset(valueOf("sort"), new SortIniative());
	}

	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	public void set(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(LuaValue key, LuaValue value) {
		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
		
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
				throw new LuaError(new ParserException(I18N.getText("macro.function.initiative.mustBeGM", "setCurrentInitiative")));
		}
		if (key.isstring()) {
			if (key.checkjstring().equals("current")) {
				list.setCurrent(value.checkint());
			} else if (key.checkjstring().equals("round")) {
				list.setRound(value.checkint());
			}
		}
		error("table is read-only, except for current");
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();		
		boolean hideNPCs = list.isHideNPC();
		if (key.isstring()) {
			if (key.checkjstring().equals("current")) {
				Token t = list.getToken(list.getCurrent());
				if (!MapTool.getParser().isMacroTrusted() && !InitiativeListModel.isTokenVisible(t, hideNPCs)) {
					return valueOf(-1);
				}
				return valueOf(list.getCurrent());
			} else if (key.checkjstring().equals("token")) {
				Token t = list.getToken(list.getCurrent());
				if (!MapTool.getParser().isMacroTrusted() && !InitiativeListModel.isTokenVisible(t, hideNPCs)) {
					return NIL;
				}
				if (t == null) {
					return NIL;
				}
				return new MapToolToken(t, resolver != null && t == resolver.getTokenInContext(), resolver);
			} else if (key.checkjstring().equals("round")) {
				return valueOf(list.getRound());
			} else if (key.checkjstring().equals("map")) {
				return valueOf(list.getZone().getName());
			} else if (key.checkjstring().equals("tokens")) {
				LuaTable table = new LuaTable();
				for (TokenInitiative ti : list.getTokens()) {
					if (!MapTool.getParser().isMacroTrusted() && !InitiativeListModel.isTokenVisible(ti.getToken(), hideNPCs))
						continue;
					LuaTable tiL = new LuaTable();
					tiL.rawset("holding", valueOf(ti.isHolding()));
					tiL.rawset("initiative", ti.getState() == null ? NIL : valueOf(ti.getState()));
					tiL.rawset("tokenId", ti.getId() == null ? NIL : valueOf(ti.getId().toString()));
					tiL.rawset("token", new MapToolToken(ti.getToken(), resolver != null && ti.getToken() == resolver.getTokenInContext(), resolver));
					table.insert(0, new ReadOnlyLuaTable(tiL, false));
				}
				return new ReadOnlyLuaTable(table, false);
			}
		}
		return super.rawget(key);
	}

	public String tojstring() {
		return "Iniative";
	}

	@Override
	public LuaValue tostring() {
		return LuaValue.valueOf(tojstring());
	}

	@Override
	public LuaString checkstring() {
		return LuaValue.valueOf(tojstring());
	}

	@Override
	public String toString() {
		return tojstring();
	}
}

