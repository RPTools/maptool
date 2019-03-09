package de.jadebringer.maptool.framework.jadebringer_midwinter;

import java.util.LinkedList;
import java.util.List;

import de.jadebringer.maptool.framework.Framework;
import de.jadebringer.maptool.framework.jadebringer_midwinter.functions.PingFunction;
import net.rptools.parser.function.Function;

public class JadebringerMidwinterFramework implements Framework {
	
	private List<Function> functions = new LinkedList<Function>();

	public JadebringerMidwinterFramework() {
		functions.add(PingFunction.getInstance());
	}
	
	@Override
	public List<Function> getFunctions() {
		return functions;
	}
	
	
}
