/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.script.javascript.api;

import java.util.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import org.graalvm.polyglot.HostAccess;

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

  @HostAccess.Export
  public String version() {
    return MapTool.getVersion();
  }

  @HostAccess.Export
  public boolean fullScreen() {
    return MapTool.getFrame().isFullScreen();
  }

  @HostAccess.Export
  public long timeInMs() {
    return System.currentTimeMillis();
  }

  @HostAccess.Export
  public Date timeDate() {
    return Calendar.getInstance().getTime();
  }

  @HostAccess.Export
  public JSMap libraryTokens() {
    Map<String, Object> libInfo = new HashMap<>();
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
    return new JSMap(libInfo);
  }

  @HostAccess.Export
  public JSList userDefinedFunctions() {
    return JSList.fromArray(UserDefinedMacroFunctions.getInstance().getAliases());
  }

  public String clientId() {
    return MapTool.getClientId();
  }
}
