/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.framework.macro;

import java.util.LinkedList;
import java.util.List;

import de.jadebringer.maptool.functions.AddFrameworkFunction;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.parser.ParserException;

/**
 * Macro to support adding frameworks by using /jb_addFramework chat command
 * 
 * @author oliver.szymanski
 */
@MacroDefinition(
		name = "jb_addFramework",
		aliases = { "jb_addFramework" },
		description = "addFramework.description",
		expandRolls = false)
public class AddFrameworkMacro implements Macro {
	public void execute(MacroContext context, String macroParameter, MapToolMacroContext executionContext) {
		if (macroParameter == null || macroParameter.trim().length() == 0) {
			return;
		}

		List<Object> parameters = new LinkedList<>();		
		for(String frameworkName : macroParameter.split(",")) {
			parameters.add(frameworkName.trim());
		}
		
		try {
			AddFrameworkFunction.getInstance().childEvaluate(null , "addFramework", parameters);
		} catch (ParserException e) {
			// ignore exception
		}
	}

}
