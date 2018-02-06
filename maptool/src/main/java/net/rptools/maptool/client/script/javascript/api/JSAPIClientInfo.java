/*
 * This software Copyright by the RPTools.net development team, and licensed
 * under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this source Code. If not, see <http://www.gnu.org/licenses/>
 */
package net.rptools.maptool.client.script.javascript.api;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

import java.util.*;

public class JSAPIClientInfo {
	public boolean faceEdge() {
		return AppPreferences.getFaceEdge();
	}

	public boolean faceVertex() {
		return AppPreferences.getFaceVertex();
	}

	public int portraitSize() {
		return AppPreferences.getPortraitSize();
	}

	public boolean showStatSheet() {
		return AppPreferences.getShowStatSheet();
	}

	public String version() {
		return MapTool.getVersion();
	}

	public boolean fullScreen() {
		return MapTool.getFrame().isFullScreen();
	}

	public long timeInMs() {
		return System.currentTimeMillis();
	}

	public Date timeDate() {
		return Calendar.getInstance().getTime();
	}

	public Map<String, String> libraryTokens() {
		Map<String, String> libInfo = new HashMap<>();
		for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
			Zone zone = zr.getZone();
			for (Token token : zone.getTokens()) {
				if (token.getName().toLowerCase().startsWith("lib:")) {
					if (token.getProperty("libversion") != null) {
						libInfo.put(token.getName(), token.getProperty("libversion").toString());
					} else {
						libInfo.put(token.getName(), "unknown");
					}
				}
			}
		}
		return libInfo;
	}

	public Collection<String> userDefinedFunctions() {
		return Arrays.asList(UserDefinedMacroFunctions.getInstance().getAliases());
	}

	public String clientId() {
		return MapTool.getClientId();
	}

}
