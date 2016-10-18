/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.client.functions.StrPropFunctions;
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
	static BitSet dontNeedEncoding;
	static {
		dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++) {
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++) {
			dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++) {
			dontNeedEncoding.set(i);
		}
		dontNeedEncoding.set(' '); /*
									 * encoding a space to a + is done in the
									 * encode() method
									 */
		dontNeedEncoding.set('-');
		dontNeedEncoding.set('_');
		dontNeedEncoding.set('.');
		dontNeedEncoding.set('*');
	}
	private static final String keyValuePatt = "\\s*([\\w .]+)\\s*=\\s*(.*)";
	private static final Pattern keyValueParser = Pattern.compile(keyValuePatt);
	//fromObj
	//toJSON
	//fromJSON
	/**
	 * 
	 */
	public static Object toObj(LuaValue val) {
		if (val.isnil())
			return null;
		if (val.isinttype())
			return new BigDecimal(val.checkint());
		return val.tojstring();
	}

	public static Object toObjNoNull(LuaValue val) {
		if (val.isnil())
			return "null";
		if (val.isinttype())
			return BigDecimal.valueOf(val.checkint());
		return val.tojstring();
	}

	public static boolean isTrue(LuaValue val) {
		if (val.isnil())
			return false;
		if (val.isboolean())
			return val.checkboolean();
		if (val.isinttype())
			return val.checkint() > 0;
		try {
			Integer i = Integer.valueOf(val.tojstring());
			return i > 0;
		} catch (NumberFormatException e) {

		}
		return false;
	}

	public static BigDecimal toNum(LuaValue val) {
		if (val.isnil())
			return BigDecimal.valueOf(0);
		if (val.isboolean())
			return val.checkboolean() ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
		if (val.isinttype())
			return BigDecimal.valueOf(val.checkint());
		try {
			Integer i = Integer.valueOf(val.tojstring());
			BigDecimal.valueOf(i);
		} catch (NumberFormatException e) {

		}
		return BigDecimal.valueOf(0);
	}

	public static Token getToken(LuaValue val) {
		if (val instanceof MapToolToken) {
			Token t = ((MapToolToken) val).getToken();
			if (t == null)
				throw new LuaError("Can't find token");
			return t;
		}
		String nameOrId = val.tojstring();
		try {
			Token t = FindTokenFunctions.findToken(nameOrId, MapTool.getFrame().getCurrentZoneRenderer().getZone().getName());
			if (t == null)
				throw new LuaError("Can't find token" + nameOrId);
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
			BigDecimal bd = (BigDecimal) o;
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
		if (obj instanceof JSONObject) {
			return fromJson((JSONObject) obj, new HashSet<Object>());
		} else if (obj instanceof JSONArray) {
			return fromJson((JSONArray) obj, new HashSet<Object>());
		}
		if (obj instanceof String) {
			Object im = JSONMacroFunctions.convertToJSON((String) obj);
			if (im instanceof JSONObject) {
				return fromJson((JSONObject) im, new HashSet<Object>());
			} else if (im instanceof JSONArray) {
				return fromJson((JSONArray) im, new HashSet<Object>());
			}
		}
		return fromObj(obj);

	}

	public static LuaTable fromJson(JSONObject obj) {
		return fromJson(obj, new HashSet<Object>());
	}

	private static LuaTable fromJson(JSONObject obj, Set<Object> seen) {
		LuaTable result = new LuaTable();
		for (Object keyObj : obj.keySet()) {
			String key = StringUtils.defaultString(ObjectUtils.toString(keyObj));
			Object val = obj.get(keyObj);
			if (val instanceof JSONObject) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.rawset(key, fromJson((JSONObject) val, seen));
			} else if (val instanceof JSONArray) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.rawset(key, fromJson((JSONArray) val, seen));
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
		for (Object val : obj) {
			if (val instanceof JSONObject) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.insert(0, fromJson((JSONObject) val, seen));
			} else if (val instanceof JSONArray) {
				if (seen.contains(val)) {
					continue; //Ignore
				}
				seen.add(val);
				result.insert(0, fromJson((JSONArray) val, seen));
			} else {
				result.insert(0, fromObj(val));
			}
		}
		return result;
	}

	public static Object toJson(LuaValue val) {
		if (val.isboolean()) {
			return val.toboolean() ? BigDecimal.ONE : BigDecimal.ZERO;
		}
		return toJson(val, new HashSet<LuaValue>());
	}

	private static Object toJson(LuaValue val, Set<LuaValue> seen) {
		if (val instanceof IRepresent) {
			return ((IRepresent) val).export();
		}
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
		} else if (val.isnumber()) {
			if (val.isint()) {
				return BigDecimal.valueOf(val.checkint());
			}
			try {
				return BigDecimal.valueOf(val.checkdouble());
			} catch (NumberFormatException nfe) {
				return "";
			}
		} else if (val.isstring()) {
			return val.tojstring();
		} else if (val.isnil()) {
			return "";
		} else if (val.isboolean()) {
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

	public static String toStr(LuaValue val, String listSep, String propSep) {
		return toStr(val, new HashSet<LuaValue>(), listSep, propSep);
	}
	
	private static String toStr(LuaValue val, Set<LuaValue> seen, String listSep, String propSep) {
		if (val instanceof IRepresent) {
			return ObjectUtils.toString(((IRepresent) val).export());
		}
		if (val.istable()) {
			if (seen.contains(val)) {
				return "";
			}
			seen.add(val);
			LuaTable table = val.checktable();
			Varargs args = table.next(LuaValue.NIL);
			boolean isArray = true;
			List<String> array = new ArrayList<String>(table.length());
			Map<String, String> map = new HashMap<String, String>();
			while (args.narg() > 1) {
				LuaValue key = args.arg(1);
				LuaValue value = args.arg(2);
				if (!key.isint()) {
					isArray = false;
				}
				String result = toStr(value, seen, listSep, propSep);
				
				if (isArray) {
					array.add(result);
				}
				String k = key.tojstring();
				if (!unsafeSep(listSep) && !unsafeSep(propSep) && (k.contains(listSep) || k.contains(propSep) || !k.matches("^[\\w .]+$"))) {
					k = encode(k, true, listSep, propSep, ".", "-", ",", "*");
					k.replace("%", ".");
				}
				map.put(k, result);
				args = table.next(key);
			}
			if (isArray) {
				for (int i = 0; i < array.size(); i++) {
					if (!unsafeSep(listSep) && (array.get(i).contains(listSep) || array.get(i).contains("%"))) {
						array.set(i, encode(array.get(i), true, listSep));
					}
				}
				return StringUtils.join(array, listSep);
			} else {
				array.clear();
				for (Entry<String, String> e: map.entrySet()) {
					String result = e.getValue();
					if (!unsafeSep(propSep) && (result.contains(propSep) || result.contains("%"))) {
						result = encode(result, true, propSep);
					}
					array.add(e.getKey() + "= " + result);
				}
			}
			return StringUtils.join(array, propSep);
		} else if (val.isnumber() || val.isstring()) {
			return val.tojstring();
		} else if (val.isnil()) {
			return "";
		} else if (val.isboolean()) {
			return val.checkboolean() ? "1" : "0";
		}
		return val.tojstring();
	}
	
	public static Map<String, String> parse(String props, String delim) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		String delimPatt;
		if (delim.equals("")) {
			delimPatt = ";";
		} else {
			delimPatt = StrPropFunctions.fullyQuoteString(delim); // XXX Why are we not using \\Q...\\E instead?
		}
		// Changed to allow spaces within keys, although spaces on either end of keys or
		// values will be trimmed.  http://forums.rptools.net/viewtopic.php?f=3&t=23841
		// Added "." to allowed key names since variable names can contain dots.
		final String entryPatt = "\\s*([\\w .]+\\s*=.*?)" + delimPatt + "|([\\w.]+\\s*=.*)";
		final Pattern entryParser = Pattern.compile(entryPatt);

		// Extract the keys and values already in the props string.
		// Save the old keys so we can rebuild the props string in the same order.
		boolean lastEntry = false;
		Matcher entryMatcher = entryParser.matcher(props);
		while (entryMatcher.find()) {
			if (!lastEntry) {
				//	    		String entry = entryMatcher.group();
				String entry = entryMatcher.group(1);
				if (entry == null) {
					entry = entryMatcher.group(2);
					// We're here because there was no trailing delimiter in this match.
					// In this case, the next match will be empty, but we don't want to grab it.
					// We would grab the final empty match if the string ended with the delimiter,
					// so this flag will prevent that.
					lastEntry = true;
				}
				//				private static final String keyValuePatt = "([\\w .]+)=(.*)";
				Matcher keyValueMatcher = keyValueParser.matcher(entry);
				if (keyValueMatcher.find()) {
					String propKey = keyValueMatcher.group(1).trim();
					String propValue = keyValueMatcher.group(2).trim();
					map.put(propKey, propValue);
				}
			}
		}
		return map;
	}
	
	
	
	public static LuaValue fromStr(String val, String listSep, String propSep) {
		String str = val;
		if (str.contains("=") || str.contains(propSep) || str.matches("[^=]+\\.[0-9A-F]{2}.+")) {
			LuaTable result = new InsertionOrderLuaTable();
			Map<String, String> parse = parse(str, propSep);
			if (!parse.isEmpty()) {
				for (Entry<String, String> e: parse.entrySet()) {
					String key = e.getKey();
					if (key.matches("[^=]+[0-9A-F]{2}.+")) {
						try {
							key = decode(key.replace(".", "%"), true);
						} catch (LuaError le) {
						}
					}
					result.set(key, fromStr(trydecode(e.getValue()), listSep, propSep));
				}
				return result;
			}
		} 
		if (str.contains(listSep)) {
			LuaTable result = new LuaTable();
			for (String e: StringUtils.splitByWholeSeparator(str, listSep)) {
				result.insert(0, fromStr(trydecode(e), listSep, propSep));
			}
			return result;
		}
		str=trydecode(str);
		try {
			return LuaValue.valueOf(Integer.parseInt(str));
		} catch (NumberFormatException e) {
		}
		try {
			return LuaValue.valueOf(Double.parseDouble(str));
		} catch (NumberFormatException e) {
		}
		return LuaValue.valueOf(str);
	}
	
	private static String trydecode(String str) {
		if (str.contains("%")) {
			try {
				return decode(str, true);
			} catch (LuaError e) {
			}
		}
		return str;
	}

	private static boolean unsafeSep(String sep) {
		if (sep == null) {
			return true;
		}
		if (sep.matches("^[0-9A-F]{2}$")) return true;
		if (sep.matches("^%[0-9A-F]{2}$")) return true;
		if (sep.equals("%")) return true;
		if (sep.equals("0")) return true;
		if (sep.equals("1")) return true;
		if (sep.equals("2")) return true;
		if (sep.equals("3")) return true;
		if (sep.equals("4")) return true;
		if (sep.equals("5")) return true;
		if (sep.equals("6")) return true;
		if (sep.equals("7")) return true;
		if (sep.equals("8")) return true;
		if (sep.equals("9")) return true;
		if (sep.equals("A")) return true;
		if (sep.equals("B")) return true;
		if (sep.equals("C")) return true;
		if (sep.equals("D")) return true;
		if (sep.equals("E")) return true;
		if (sep.equals("F")) return true;
		return false;
	}

	public static String encode(String str, boolean replaceSemi, String... additionalEncodes) {
		String encoded = str;
		if (replaceSemi) {
			encoded = encoded.replaceAll(";", "&semi;");
		}
		try {
			encoded = URLEncoder.encode(encoded, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new LuaError(e);
		}
		for (String bad: additionalEncodes) {
			for (byte b: bad.getBytes()) {
				if (dontNeedEncoding.get(b)) {
					encoded.replace(""+Character.valueOf((char) b), "%"+Integer.toHexString(b).toUpperCase());
				}
			}
		}
		return encoded;
	}
	
	public static String decode(String str, boolean replaceSemi) {
		String decoded = str;
		try {
			decoded = URLDecoder.decode(decoded, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new LuaError(e);
		}
		if (replaceSemi) {
			decoded = decoded.replaceAll("&semi;", ";");
		}
		return decoded;
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

	public static String toString(LuaValue arg) {
		if (arg instanceof IRepresent) {
			return ObjectUtils.toString(((IRepresent) arg).export());
		}
		return ObjectUtils.toString(arg);
	}
}
