/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.framework;

import de.jadebringer.maptool.framework.macro.AddFrameworkMacro;
import de.jadebringer.maptool.framework.macro.CallMacro;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

public class FrameworkInitializer {

	public final static void init() {
		Parser parser = new Parser();
		try {
			MacroManager.registerMacro(new AddFrameworkMacro());
			MacroManager.registerMacro(new CallMacro());

			// problem is the following gets cleared after every campaign load
			UserDefinedMacroFunctions.getInstance().defineFunction(parser, "jb_addFramework", "jb_addFramework", false, true);
			// So we still need the AddFrameworkFunction class added to the global function array
		} catch (ParserException e) {
			// ignore any exception
		}
	}

}
