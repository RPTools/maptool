/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */
public class InsertionOrderLuaTable extends LuaTable {
	List<LuaValue> order = new ArrayList<LuaValue>(); 

	public void set(int key, LuaValue value) {
		LuaValue k = valueOf(key);
		if (!order.contains(k)) {
			if (!value.isnil()) 
				order.add(k);
		} else {
			if (value.isnil()) 
				order.remove(k);
		}
		super.set(key, value);
	}

	public void rawset(int key, LuaValue value) {
		LuaValue k = valueOf(key);
		if (!order.contains(k)) {
			if (!value.isnil()) 
				order.add(k);
		} else {
			if (value.isnil()) 
				order.remove(k);
		}
		super.rawset(key, value);
	}

	public void rawset(LuaValue key, LuaValue value) {
		if (!key.isnil()) {
			if (!order.contains(key)) {
				if (!value.isnil()) 
					order.add(key);
			} else {
				if (value.isnil()) 
					order.remove(key);
			}
		}
		super.rawset(key, value);
	}
	@Override
	public Varargs next(LuaValue key) {
		int i = order.indexOf(key);
		if (i >= order.size() - 1) {
			return NONE;
		}
		return varargsOf(order.get(i + 1), rawget(order.get(i + 1)));
	}

}
