/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */
public class LuaConverters {
	//fromObj
	//toJSON
	//fromJSON
	/**
	 * 
	 */
	public static Object toObj(LuaValue val) {
		if (val.isnil()) return null;
		if (val.isinttype()) return new BigDecimal(val.checkint());
		return val.tojstring();
	}
	
	public static Object toObjNoNull(LuaValue val) {
		if (val.isnil()) return "null";
		if (val.isinttype()) return BigDecimal.valueOf(val.checkint());
		return val.tojstring();
	}
	
	public static boolean isTrue(LuaValue val) {
		if (val.isnil()) return false;
		if (val.isboolean()) return val.checkboolean();
		if (val.isinttype()) return val.checkint() > 0;
		try {
			Integer i = Integer.valueOf(val.tojstring());
			return i > 0;
		} catch (NumberFormatException e) {
			
		}
		return false;
	}
	
	public static BigDecimal toNum(LuaValue val) {
		if (val.isnil()) return BigDecimal.valueOf(0);
		if (val.isboolean()) return val.checkboolean() ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
		if (val.isinttype()) return BigDecimal.valueOf(val.checkint());
		try {
			Integer i = Integer.valueOf(val.tojstring());
			BigDecimal.valueOf(i);
		} catch (NumberFormatException e) {
			
		}
		return BigDecimal.valueOf(0);
	}
	
	public static Token getToken(LuaValue val) {
		if (val instanceof MapToolToken) {
			Token t =((MapToolToken) val).getToken();
			if (t == null) throw new LuaError("Can't find token");
			return t;
		}
		String nameOrId = val.tojstring();
		try {
			Token t = FindTokenFunctions.findToken(nameOrId, MapTool.getFrame().getCurrentZoneRenderer().getZone().getName());
			if (t == null) throw new LuaError("Can't find token" + nameOrId);
			return t;
		} catch (ClassCastException e) {
			throw new LuaError(I18N.getText("macro.function.general.argumentTypeT", 2, "a token function"));
		}
	}
	
	public static LuaValue fromObj(Object o) {
		if (o instanceof String) {
			return LuaValue.valueOf(o.toString());
		}
		if (o instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal)o;
			if ((bd.scale() > 0 || bd.stripTrailingZeros().scale() > 0) && bd.longValue() < Integer.MAX_VALUE && bd.longValue() > Integer.MIN_VALUE) {
				return LuaValue.valueOf(bd.intValue());
			}
			return LuaValue.valueOf(bd.doubleValue());
		}
		if (o instanceof Boolean) {
			return LuaValue.valueOf(((Boolean) o).booleanValue());
		}
		if (o instanceof Integer) {
			return LuaValue.valueOf(((Number) o).intValue());
		}
		if (o instanceof Number) {
			return LuaValue.valueOf(((Number) o).doubleValue());
		}
		if (o != null) {
			return LuaValue.valueOf(o.toString());
		}
		return LuaValue.NIL;
	}
	
	public static LuaValue fromJson(Object obj) {
		Object im = JSONMacroFunctions.asJSON(obj);
		if (im instanceof JSONObject) {
			return fromJson((JSONObject)im, new HashSet<Object>());
		} else if (im instanceof JSONArray) {
			return fromJson((JSONArray)im, new HashSet<Object>());
		}
		return fromObj(im);
		
	}
	
	public static LuaTable fromJson(JSONObject obj) {
		return fromJson(obj, new HashSet<Object>());
	}
	
	private static LuaTable fromJson(JSONObject obj, Set<Object> seen) {
		LuaTable result = new LuaTable();
		for (Object keyObj: obj.keySet()) {
			String key = StringUtils.defaultString(ObjectUtils.toString(keyObj));
			Object val = obj.get(keyObj);
			if (val instanceof JSONObject) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.rawset(key, fromJson((JSONObject)val, seen));
			} else if (val instanceof JSONArray) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.rawset(key, fromJson((JSONArray)val, seen));
			} else {
				result.rawset(key, fromObj(val));
			}
		}
		return result;
	}
	
	public static LuaTable fromJson(JSONArray obj) {
		return fromJson(obj, new HashSet<Object>());
	}
	
	private static LuaTable fromJson(JSONArray obj, Set<Object> seen) {
		LuaTable result = new LuaTable();
		for (Object val: obj) {
			if (val instanceof JSONObject) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.insert(0, fromJson((JSONObject)val, seen));
			} else if (val instanceof JSONArray) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.insert(0, fromJson((JSONArray)val, seen));
			} else {
				result.insert(0, fromObj(val));
			}
		}
		return result;
	}
	
	public static Object toJson(LuaValue val) {
		return toJson(val, new HashSet<LuaValue>());
	}
	
	private static Object toJson(LuaValue val, Set<LuaValue> seen) {
		if (val.istable()) {
			if (seen.contains(val)) {
				return "";
			}
			seen.add(val);
			LuaTable table = val.checktable();
			Varargs args = table.next(LuaValue.NIL);
			boolean isArray = true;
			List<Object> array = new ArrayList<Object>(table.length());
			Map<String, Object> map = new HashMap<String, Object>();
			while (args.narg() > 1) {
				LuaValue key = args.arg(1);
				LuaValue value = args.arg(2);
				if (!key.isint()) {
					isArray = false;
				}
				Object result = toJson(value, seen);
				if (isArray) {
					array.add(result);
				}
				map.put(key.tojstring(), result);
				args = table.next(key);
			}
			if (isArray) {
				JSONArray res = new JSONArray();
				res.addAll(array);
				return res;
			}
			JSONObject res = new JSONObject();
			res.putAll(map);
			return res;
		}
		else if (val.isnumber()) {
			if (val.isint()) {
				return BigDecimal.valueOf(val.checkint());
			}
			try {
				return BigDecimal.valueOf(val.checkdouble());
			} catch (NumberFormatException nfe) {
				return "";
			}
		}
		else if (val.isstring()) {
			return val.tojstring();
		}
		else if (val.isnil()) {
			return "";
		}
		else if (val.isboolean()) {
			return val.checkboolean();
		}
		return val.tojstring();
	}
	
	public static Object ensureJSONorString(LuaValue val) {
		if (val.istable()) {
			return toJson(val);
		}
		return val.tojstring();
	}
	
	public static Iterable<LuaValue> iterate(final LuaTable table) {
		return new Iterable<LuaValue>() {
			@Override
			public Iterator<LuaValue> iterator() {
				return new Iterator<LuaValue>() {
					Varargs m_next = table.next(LuaValue.NIL);
					@Override
					public boolean hasNext() {
						return !m_next.isnil(1);
					}
					@Override
					public LuaValue next() {
						LuaValue val = m_next.arg(2);
						m_next = table.next(m_next.arg1());
						return val;
					}
					
				};
			}
		};
	}
	public static Iterable<LuaValue> arrayIterate(final LuaTable table) {
		return new Iterable<LuaValue>() {
			@Override
			public Iterator<LuaValue> iterator() {
				return new Iterator<LuaValue>() {
					Varargs m_next = table.inext(LuaValue.valueOf(0));
					@Override
					public boolean hasNext() {
						return !m_next.isnil(1);
					}
					@Override
					public LuaValue next() {
						LuaValue val = m_next.arg(2);
						m_next = table.inext(m_next.arg1());
						return val;
					}
					
				};
			}
		};
	}
	public static Iterable<LuaValue> keyIterate(final LuaTable table) {
		return new Iterable<LuaValue>() {
			@Override
			public Iterator<LuaValue> iterator() {
				return new Iterator<LuaValue>() {
					Varargs m_next = table.next(LuaValue.NIL);
					@Override
					public boolean hasNext() {
						return !m_next.isnil(1);
					}
					@Override
					public LuaValue next() {
						LuaValue val = m_next.arg(1);
						m_next = table.next(m_next.arg1());
						return val;
					}
					
				};
			}
		};
	}
}
