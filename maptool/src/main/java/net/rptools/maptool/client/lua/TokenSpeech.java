/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.util.Collection;

import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */
public class TokenSpeech extends LuaTable {
	private MapToolToken token;
	public TokenSpeech(MapToolToken token) {
		this.token = token;
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	@Override
	public LuaValue rawget(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getSpeech"))); 
		}
		String text = token.getToken().getSpeech(key.checkjstring());
		if (text != null) {
			return LuaValue.valueOf(text);
		}
		return LuaValue.NIL;
	}
	@Override
	public LuaValue rawget(int key) {
		return rawget(LuaValue.valueOf(key));
	}
	public void rawset(int key, LuaValue value) {  
		rawset(LuaValue.valueOf(key), value);
	}
	public void rawset(LuaValue key, LuaValue value) { 
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setSpeech"))); 
		}
		token.getToken().setSpeech(key.checkjstring(), value.checkjstring());
	}
	public LuaValue remove(int pos) { return error("speech can not be deleted"); }
	@Override
	public int length() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getSpeechNames"))); 
		}
		return token.getToken().getSpeechNames().size();
	}
	@Override
	public Varargs next(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getSpeechNames"))); 
		}
		Collection<String> values = token.getToken().getSpeechNames();
		boolean found = false;
		String name=null;
		if (key.isnil()) {
			found = true;
		}
		else {
			name = key.checkjstring();
		}
		for (String source: values) {
			if (found) {
				return varargsOf(valueOf(source), valueOf(token.getToken().getSpeech(source)));
			}
			if (!found && source.equals(name)) {
				found = true;
			}
		}
		return NIL;
	}
}
