/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.functions;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.jadebringer.maptool.framework.Framework;
import de.jadebringer.maptool.framework.FrameworkInitializer;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.Function;
import net.rptools.parser.function.ParameterException;

/**
 * 
 * @author oliver.szymanski
 *
 */
public class AddFrameworkFunction implements Function {
	private static final AddFrameworkFunction instance = new AddFrameworkFunction();
	private final int minParameters;
	private final int maxParameters;
	private final boolean deterministic;

	static {
		FrameworkInitializer.init();
	}
	
	private AddFrameworkFunction() {
		this.minParameters = 1;
		this.maxParameters = UNLIMITED_PARAMETERS;
		this.deterministic = true;
		frameworkFunctions.add(this);
		frameworkFunctionsAliasMap.put("jb_addFramework", this);
	}

	private List<Function> frameworkFunctions = new LinkedList<>();
	private Map<String, Function> frameworkFunctionsAliasMap = new HashMap<>();
	
	public List<Function> getFrameworkFunctions() {
		return frameworkFunctions;
	}
	
	public Map<String, Function> getFrameworkFunctionsAliasMap() {
		return frameworkFunctionsAliasMap;
	}


	public static AddFrameworkFunction getInstance() {
		return instance;
	}

	public Object childEvaluate(Parser parser, String functionName,
			List<Object> parameters) throws ParserException {
		if (functionName.equals("jb_addFramework")) {
			this.checkParameters(parameters);

			if (!MapTool.getParser().isMacroTrusted()) {
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
			}

			if (parameters.size() < 1) {
				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
			}

			List<String> newFunctionNames = new LinkedList<String>();
			
			for (Object frameworkName : parameters) {
				
				try {
					Framework framework = (Framework) Class.forName(frameworkName.toString()).getDeclaredConstructor().newInstance();
					Collection<? extends Function> functions = framework.getFunctions();
					
					for(Function function : functions) {
						frameworkFunctions.add(function);
						for(String alias : function.getAliases()) {
							newFunctionNames.add(alias);
							frameworkFunctionsAliasMap.put(alias, function);
						}
							
					}
				} catch (Exception e) {
					throw new ParserException(e);
				}
			}
			
			return "<br>"+(newFunctionNames.stream()
					.collect(Collectors.joining(", "))) + " framework functions defined.";
 		} else {
 			Function function = frameworkFunctionsAliasMap.get(functionName);
 			
 			if (function != null) {
 				return function.evaluate(parser, functionName, parameters);
 			}
 		}

		return BigDecimal.ZERO;
	}

	public String[] getAliases() {
		String[] aliases = new String[frameworkFunctionsAliasMap.keySet().size()];
		
		aliases = frameworkFunctionsAliasMap.keySet().toArray(aliases);
		if (aliases == null) {
			return new String[0];
		} else {
			return aliases;
		}
	}
	
	public int getMinimumParameterCount() {
		return this.minParameters;
	}

	public int getMaximumParameterCount() {
		return this.maxParameters;
	}

	public boolean isDeterministic() {
		return this.deterministic;
	}

	public void checkParameters(List<Object> parameters) throws ParameterException {
		int pCount = parameters == null ? 0 : parameters.size();
		if (pCount < this.minParameters || this.maxParameters != -1 && parameters.size() > this.maxParameters) {
			throw new ParameterException(String.format("Invalid number of parameters %d, expected %s", pCount,
					this.formatExpectedParameterString()));
		}
	}
	
	public final Object evaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		return this.childEvaluate(parser, functionName, parameters);
	}
	
	private String formatExpectedParameterString() {
		if (this.minParameters == this.maxParameters) {
			return String.format("exactly %d parameter(s)", this.maxParameters);
		}
		if (this.maxParameters == -1) {
			return String.format("at least %d parameters", this.minParameters);
		}
		return String.format("between %d and %d parameters", this.minParameters, this.maxParameters);
	}

}
