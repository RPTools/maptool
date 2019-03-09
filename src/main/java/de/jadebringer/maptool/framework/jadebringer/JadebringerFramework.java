package de.jadebringer.maptool.framework.jadebringer;

import java.util.LinkedList;
import java.util.List;

import de.jadebringer.maptool.framework.Framework;
import de.jadebringer.maptool.framework.jadebringer.functions.PingFunction;
import net.rptools.parser.function.Function;

public class JadebringerFramework implements Framework {
	
	private List<Function> functions = new LinkedList<Function>();

	public JadebringerFramework() {
		functions.add(PingFunction.getInstance());
	}
	
	@Override
	public List<Function> getFunctions() {
		return functions;
	}
	
	
}
