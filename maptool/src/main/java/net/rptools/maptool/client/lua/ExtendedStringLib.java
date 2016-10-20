package net.rptools.maptool.client.lua;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.StringFunctions;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ExtendedStringLib extends StringLib {
	MapToolVariableResolver res;
	public ExtendedStringLib(MapToolVariableResolver res) {
		this.res = res;
	}
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue result = super.call(modname, env);
		LuaTable table = instance;
		table.set("endsWith", new EndsWith());
		table.set("startsWith", new StartsWith());
		table.set("replace", new Replace());
		table.set("indexOf", new IndexOf());
		table.set("lastIndexOf", new LastIndexOf());
		table.set("matches", new Matches());
		table.set("trim", new Trim());
		table.set("strfind", new StrFind());
		table.set("formatM", new Format(res));
		table.set("split", new ToTable());
		table.set("toTable", new ToTable());
		LuaString.s_metatable = tableOf( new LuaValue[] { INDEX, table } );
		return result;
	}
		
	static class EndsWith extends TwoArgFunction {
		public LuaValue call(LuaValue string, LuaValue find) {
			return LuaValue.valueOf(string.checkjstring().endsWith(find.checkjstring()));
		}
	}
	static class StartsWith extends TwoArgFunction {
		public LuaValue call(LuaValue string, LuaValue find) {
			return LuaValue.valueOf(string.checkjstring().startsWith(find.checkjstring()));
		}
	}
	static class Matches extends TwoArgFunction {
		public LuaValue call(LuaValue string, LuaValue pattern) {
			return LuaValue.valueOf(string.checkjstring().matches(pattern.checkjstring()));
		}
	}
	static class Trim extends OneArgFunction {
		public LuaValue call(LuaValue string) {
			return LuaValue.valueOf(string.checkjstring().trim());
		}
	}
	static class ToTable extends TwoArgFunction {
		public LuaValue call(LuaValue string, LuaValue pattern) {
			LuaTable result = new LuaTable();
			for (String s: string.checkjstring().split(pattern.checkjstring())) {
				result.insert(0, LuaValue.valueOf(s));
			}
			return result;
		}
	}
	static class Replace extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			LuaValue string = args.arg(1);
			LuaValue find = args.arg(2);
			LuaValue replacement = args.arg(3);
			LuaValue times = args.arg(4);
			if (times.isnumber()) {
				return LuaValue.valueOf(StringFunctions.getInstance().replace(string.checkjstring(), find.checkjstring(), replacement.checkjstring(), times.toint()));
			} else {
				return LuaValue.valueOf(string.checkjstring().replaceAll(find.checkjstring(), replacement.checkjstring()));
			}
			
			
		}
	}
	static class Format extends VarArgFunction {
		private VariableResolver res;
		public Format(VariableResolver res) {
			super();
			this.res = res;
		}
		public LuaValue invoke(Varargs args) {
			LuaValue string = args.arg(1);
			List<Object> list = new ArrayList<Object>();
			for (int i = 2; i < args.narg(); i++) {
				list.add(LuaConverters.toObj(args.arg(i)));
			}
			try {
				return LuaValue.valueOf(StringFunctions.getInstance().format(string.checkjstring(), res, list));
			} catch (ParserException e) {
				throw new LuaError(e);
			}
			
			
		}
	}
	static class IndexOf extends ThreeArgFunction {
		public LuaValue call(LuaValue string, LuaValue find, LuaValue start) {
			if (start.isnumber()) {
				return LuaValue.valueOf(string.checkjstring().indexOf(find.checkjstring(), start.toint()));
			}
			return LuaValue.valueOf(string.checkjstring().indexOf(find.checkjstring()));
		}
	}
	static class LastIndexOf extends ThreeArgFunction {
		public LuaValue call(LuaValue string, LuaValue find, LuaValue start) {
			if (start.isnumber()) {
				return LuaValue.valueOf(string.checkjstring().lastIndexOf(find.checkjstring(), start.toint()));
			}
			return LuaValue.valueOf(string.checkjstring().lastIndexOf(find.checkjstring()));
		}
	}
	static class StrFind extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			Pattern p = Pattern.compile(args.checkjstring(2));
			Matcher m = p.matcher(args.checkjstring(1));
			int found = 0;
			LuaTable result = new LuaTable();
			while (m.find()) {
				result.insert(0, new StrGroupResult(m.toMatchResult()));
				found++;
//				LuaTable groups = new LuaTable();
//				for (int i = 1; i < m.groupCount() + 1; i++) {
//					LuaTable match = new LuaTable();
//					match.rawset("value", m.group(i) == null ? NIL : valueOf(m.group(i)));
//					match.rawset("start", valueOf(m.start(i)));
//					match.rawset("end", valueOf(m.end(i)));
//					groups.rawset(valueOf(i), match);
//					
//				}
//				LuaTable match = new LuaTable();
//				match.rawset("value", m.group(i) == null ? NIL : valueOf(m.group()));
//				match.rawset("start", valueOf(m.start()));
//				match.rawset("end", valueOf(m.end()));
//				groups.rawset(valueOf(0), match);
//				result.insert(0, value);
			}
			return varargsOf(LuaValue.valueOf(found), LuaValue.valueOf(m.groupCount()), result);
		}
	}
	static class StrGroupResult extends VarArgFunction {
		private MatchResult m;
		public StrGroupResult(MatchResult m) {
			this.m = m;
		}
		@Override
		public Varargs invoke(Varargs arg) {
			if (arg.arg(1).isint() && arg.toint(1) > 0) {
				int i = arg.toint(1);
				if (i <= m.groupCount()) {
					return varargsOf(m.group(i) == null ? NIL : valueOf(m.group(i)), valueOf(m.start(i)), valueOf(m.end(i)));
				}
				return NONE;
			}
			return varargsOf(m.group() == null ? NIL : valueOf(m.group()), valueOf(m.start()), valueOf(m.end()));
		}
		
	}
}