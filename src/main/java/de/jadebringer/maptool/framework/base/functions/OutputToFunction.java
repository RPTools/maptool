/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.framework.base.functions;

import java.math.BigDecimal;
import java.util.List;

import de.jadebringer.maptool.framework.FunctionCaller;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * 
 * @author oliver.szymanski
 */
public class OutputToFunction extends AbstractFunction {
	public OutputToFunction() {
		super(1, 4, "jb_output");
	}

	private final static OutputToFunction instance = new OutputToFunction();

	public static OutputToFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {

		String who = FunctionCaller.getParam(parameters, 0, "GM");
		String message = FunctionCaller.getParam(parameters, 1, "");
		Object defer = FunctionCaller.getParam(parameters, 2, BigDecimal.ZERO);
		String target = FunctionCaller.getParam(parameters, 3, "impersonated");
		output(parser, who, message, FunctionCaller.toBoolean(defer), target);
		return "";
	}

	public void output(Parser parser, String who, String message) throws ParserException {
		output(parser, who, message, false, null);
	}
		
	public void output(Parser parser, String who, String message, boolean defer, String target) throws ParserException {
		LinkFunction linkFunction = LinkFunction.getInstance();
		String link = (String)linkFunction.createLink(parser, "jb_unpackArgs", who, message, target);
		linkFunction.execLink(link, false, parser, FunctionCaller.toBoolean(defer));
	}
}
