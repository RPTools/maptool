/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */

public class Macro extends LuaTable 
//	implements IRepresent Bad choice, since it needs to be able to be converted into a strProp or JSON list for setProps 
	{
	public class MacroCompare extends LuaTable {
		public LuaValue setmetatable(LuaValue metatable) {
			return error("table is read-only");
		}

		public void set(int key, LuaValue value) {
			error("table is read-only");
		}

		public void rawset(LuaValue key, LuaValue value) {
			if (key.isint()) {
				rawset(key.toint(), value);
				return;
			}
			error("table is read-only");
		}

		private List<String> makeList() {
			MacroButtonProperties mbp = token.getToken().getMacro(macro, true);
			if (mbp == null) {
				error("Mo Macro at " + toString());
			}
			List<String> result = new ArrayList<String>();
			if (mbp.getCompareGroup())
				result.add("group");
			if (mbp.getCompareSortPrefix())
				result.add("sortPrefix");
			if (mbp.getCompareCommand())
				result.add("command");
			if (mbp.getCompareIncludeLabel())
				result.add("includeLabel");
			if (mbp.getCompareAutoExecute())
				result.add("autoExecute");
			if (mbp.getCompareApplyToSelectedTokens())
				result.add("applyToSelected");
			return result;
		}

		@Override
		public void insert(int pos, LuaValue value) {
			String comp = value.checkjstring();
			MacroButtonProperties mbp = token.getToken().getMacro(macro, true);
			if (mbp == null) {
				error("Mo Macro at " + toString());
			}
			if (comp.equalsIgnoreCase("group")) {
				mbp.setCompareGroup(true);
			} else if (comp.equalsIgnoreCase("sortPrefix")) {
				mbp.setCompareSortPrefix(true);
			} else if (comp.equalsIgnoreCase("command")) {
				mbp.setCompareCommand(true);
			} else if (comp.equalsIgnoreCase("includeLabel")) {
				mbp.setCompareIncludeLabel(true);
			} else if (comp.equalsIgnoreCase("autoExecute")) {
				mbp.setCompareAutoExecute(true);
			} else if (comp.equalsIgnoreCase("applyToSelected")) {
				mbp.setCompareApplyToSelectedTokens(true);
			}
			save();
		}

		public LuaValue remove(int pos) {
			List<String> list = makeList();
			MacroButtonProperties mbp = token.getToken().getMacro(macro, true);
			if (pos >= 0 && pos < list.size()) {
				String comp = list.get(pos);
				if (comp.equalsIgnoreCase("group")) {
					mbp.setCompareGroup(false);
				} else if (comp.equalsIgnoreCase("sortPrefix")) {
					mbp.setCompareSortPrefix(false);
				} else if (comp.equalsIgnoreCase("command")) {
					mbp.setCompareCommand(false);
				} else if (comp.equalsIgnoreCase("includeLabel")) {
					mbp.setCompareIncludeLabel(false);
				} else if (comp.equalsIgnoreCase("autoExecute")) {
					mbp.setCompareAutoExecute(false);
				} else if (comp.equalsIgnoreCase("applyToSelected")) {
					mbp.setCompareApplyToSelectedTokens(false);
				}
				save();
				return valueOf(comp);
			}
			return NIL;
		}

		@Override
		public void rawset(int key, LuaValue value) {
			remove(key);
			if (!value.isnil()) {
				insert(0, value);
			}
		}

		@Override
		public LuaValue rawget(int key) {
			List<String> list = makeList();
			if (key < list.size() && key >= 0) {
				return valueOf(list.get(key));
			}
			return NIL;
		}

		@Override
		public int length() {
			return makeList().size();
		}
	}

	private int macro;
	private final MapToolToken token;

	public Macro(MapToolToken token, int macroindex) {
		this.macro = macroindex;
		this.token = token;
		init();
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
		setProp(key, value);
		save();
	}

	public void save() {
		MacroButtonProperties mac = token.getToken().getMacro(macro, true);
		if (mac != null) {
			mac.save();
		}
	}

	public void setProp(LuaValue key, LuaValue value) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "macro.setProps")));
		}
		MacroButtonProperties mbp = token.getToken().getMacro(macro, true);
		if (mbp == null) {
			error("Mo Macro at " + toString());
		}
		if (!mbp.getAllowPlayerEdits() && !MapTool.getParser().isMacroTrusted()) {
			MapTool.addLocalMessage("Warning: You can not edit macro button " + mbp.getLabel() + " index = " + mbp.getIndex() + " on " + token.getToken().getName());
			return;
		}
		if (key.isstring()) {
			String k = key.checkjstring();
			setProp(mbp, key, value);
			if ("index".equalsIgnoreCase(k)) { //This does not seem smart...
				macro = value.checkint();
			}
		}
	}

	public static void setProp(MacroButtonProperties mbp, LuaValue key, LuaValue value) {
		if (key.isstring()) {
			String k = key.checkjstring();

			if ("autoexecute".equalsIgnoreCase(k)) {
				mbp.setAutoExecute(value.checkboolean());
			} else if ("color".equalsIgnoreCase(k)) {
				mbp.setColorKey(value.checkjstring());
			} else if ("fontColor".equalsIgnoreCase(k)) {
				mbp.setFontColorKey(value.checkjstring());
			} else if ("fontSize".equalsIgnoreCase(k)) {
				mbp.setFontSize(value.checkjstring());
			} else if ("group".equalsIgnoreCase(k)) {
				mbp.setGroup(value.checkjstring());
			} else if ("includeLabel".equalsIgnoreCase(k)) {
				mbp.setIncludeLabel(value.checkboolean());
			} else if ("sortBy".equalsIgnoreCase(k)) {
				mbp.setSortby(value.checkjstring());
			} else if ("index".equalsIgnoreCase(k)) { //This does not seem smart...
				mbp.setIndex(value.checkint());
			} else if ("label".equalsIgnoreCase(k)) {
				mbp.setLabel(value.checkjstring());
			} else if ("minWidth".equalsIgnoreCase(k)) {
				mbp.setMinWidth(value.checkjstring());
			} else if ("maxWidth".equalsIgnoreCase(k)) {
				mbp.setMaxWidth(value.checkjstring());
			} else if ("playerEditable".equalsIgnoreCase(k)) {
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new LuaError(new ParserException("setMacroProps(): You do not have permission to change player editable status"));
				}
				mbp.setAllowPlayerEdits(value.checkboolean());
			} else if ("command".equals(k)) {
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new LuaError(new ParserException("setMacroProps(): You do not have permision to change the macro command."));
				}
				mbp.setCommand(value.checkjstring());
			} else if ("tooltip".equalsIgnoreCase(k)) {
				if (value.checkjstring().trim().length() == 0) {
					mbp.setToolTip(null);
				} else {
					mbp.setToolTip(value.checkjstring());
				}
			} else if ("applyToSelected".equalsIgnoreCase(k)) {
				mbp.setApplyToTokens(value.checkboolean());
			} else if ("compare".equalsIgnoreCase(k)) {
				// First set everything to false as script will specify what is compared
				mbp.setCompareGroup(false);
				mbp.setCompareSortPrefix(false);
				mbp.setCompareCommand(false);
				mbp.setCompareIncludeLabel(false);
				mbp.setCompareAutoExecute(false);
				mbp.setCompareApplyToSelectedTokens(false);
				if (value.istable()) {
					for (LuaValue co : LuaConverters.arrayIterate(value.checktable())) {
						String comp = co.toString();
						if (comp.equalsIgnoreCase("group")) {
							mbp.setCompareGroup(true);
						} else if (comp.equalsIgnoreCase("sortPrefix")) {
							mbp.setCompareSortPrefix(true);
						} else if (comp.equalsIgnoreCase("command")) {
							mbp.setCompareCommand(true);
						} else if (comp.equalsIgnoreCase("includeLabel")) {
							mbp.setCompareIncludeLabel(true);
						} else if (comp.equalsIgnoreCase("autoExecute")) {
							mbp.setCompareAutoExecute(true);
						} else if (comp.equalsIgnoreCase("applyToSelected")) {
							mbp.setCompareApplyToSelectedTokens(true);
						}
					}
				}
			} else {
				error("Unknown Token Prop: " + k);
			}
			return;
		}
		error("Unknown Token Prop: " + key.toString());
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	public void init() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "macro.getProps")));
		}
		MacroButtonProperties mbp = token.getToken().getMacro(macro, true);
		if (mbp != null) {
			super.rawset(valueOf("autoexecute"), valueOf(mbp.getAutoExecute()));
			super.rawset(valueOf("color"), valueOf(mbp.getColorKey()));
			super.rawset(valueOf("fontColor"), valOf(mbp.getFontColorKey()));
			super.rawset(valueOf("fontSize"), valOf(mbp.getFontSize()));
			super.rawset(valueOf("group"), valOf(mbp.getGroup()));
			super.rawset(valueOf("includeLabel"), valueOf(mbp.getIncludeLabel()));
			super.rawset(valueOf("sortBy"), valOf(mbp.getSortby()));
			super.rawset(valueOf("index"), valueOf(mbp.getIndex()));
			super.rawset(valueOf("label"), valOf(mbp.getLabel()));
			super.rawset(valueOf("minWidth"), valOf(mbp.getMinWidth()));
			super.rawset(valueOf("maxWidth"), valOf(mbp.getMaxWidth()));
			super.rawset(valueOf("playerEditable"), valOf(mbp.getAllowPlayerEdits()));
			super.rawset(valueOf("command"), valOf(mbp.getCommand()));
			super.rawset(valueOf("tooltip"), valOf(mbp.getToolTip()));
			super.rawset(valueOf("applyToSelected"), valOf(mbp.getApplyToTokens()));
			super.rawset(valueOf("compare"), new MacroCompare());
		}
	}

	private LuaValue valOf(String s) {
		if (s == null)
			return NIL;
		return valueOf(s);
	}

	private LuaValue valOf(Boolean b) {
		if (b == null)
			return NIL;
		return valueOf(b.booleanValue());
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		init();
		return super.rawget(key);
	}

	@Override
	public Varargs next(LuaValue key) {
		if (key == NIL) {
			init();
		}
		return super.next(key);
	}

	@Override
	public Varargs invoke(Varargs args) {
		return call(args.arg(1));
	}

	public String tojstring() {
		return "Macro index " + macro + " for " + token.toString();
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

//	@Override
//	public Object export() {
//		try {
//			MacroButtonProperties mbp = token.getToken().getMacro(macro, true);
//			if (mbp != null) {
//				return mbp.getLabel() + "@" + token.getToken().getName();
//			}
//		} catch (Exception e) {
//		}
//		return null;
//	}
}
