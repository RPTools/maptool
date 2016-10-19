/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.MapToolLineParser.OptionType;
import net.rptools.maptool.client.ReadOnlyLuaTable;
import net.rptools.maptool.client.lua.misc.Broadcast;
import net.rptools.maptool.client.lua.misc.PrintRoll;
import net.rptools.maptool.model.Player;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 * 
 */
public class ChatLib extends TwoArgFunction {
	private MapToolVariableResolver resolver;
	private Globals globals;
	public ChatLib(MapToolVariableResolver resolver, Globals globals) {
		super();
		this.resolver = resolver;
		this.globals = globals;
	}
	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable t = new LuaTable();
		t.set("broadcast", new Broadcast());
		t.set("hidden", new PrintRoll(resolver, false, OptionType.HIDDEN, globals));
		t.set("result", new PrintRoll(resolver, false, OptionType.RESULT, globals));
		t.set("expanded", new PrintRoll(resolver, false, OptionType.EXPANDED, globals));
		t.set("unformatted", new PrintRoll(resolver, false, OptionType.UNFORMATTED, globals));
		t.set("tooltip", new PrintRoll(resolver, false, OptionType.TOOLTIP, globals));
		t.set("gm", new PrintRoll(resolver, false, OptionType.GM, globals));
		t.set("self", new PrintRoll(resolver, false, OptionType.SELF, globals));
		t.set("whisper", new PrintRoll(resolver, false, OptionType.WHISPER, globals));
		t.set("gmtt", new PrintRoll(resolver, false, OptionType.GMTT, globals));
		t.set("selftt", new PrintRoll(resolver, false, OptionType.SELFTT, globals));
		LuaTable players = new LuaTable();
		for (Player p: MapTool.getPlayerList()) {
			players.insert(0, valueOf(p.getName()));
		}
		t.set("players", new ReadOnlyLuaTable(players));
		t.set("player", valueOf(MapTool.getPlayer().getName()));
		env.set("chat", t);
		env.get("package").get("loaded").set("chat", t);
		return t;
	}
}
