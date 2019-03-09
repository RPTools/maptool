package de.jadebringer.maptool.framework.base;

import java.util.LinkedList;
import java.util.List;

import de.jadebringer.maptool.framework.Framework;
import de.jadebringer.maptool.framework.base.functions.InputFunction;
import de.jadebringer.maptool.framework.base.functions.LinkFunction;
import de.jadebringer.maptool.framework.base.functions.OutputToFunction;
import de.jadebringer.maptool.framework.base.functions.PingFunction;
import de.jadebringer.maptool.framework.base.functions.SettingsFunction;
import net.rptools.parser.function.Function;

public class BaseFramework implements Framework {
	
	private List<Function> functions = new LinkedList<Function>();

	public BaseFramework() {
		functions.add(InputFunction.getInstance());
		functions.add(PingFunction.getInstance());
		functions.add(LinkFunction.getInstance());
		functions.add(OutputToFunction.getInstance());
		functions.add(SettingsFunction.getInstance());
	}
	
	@Override
	public List<Function> getFunctions() {
		return functions;
	}
	
	
}
