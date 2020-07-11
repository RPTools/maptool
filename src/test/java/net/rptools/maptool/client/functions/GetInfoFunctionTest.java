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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import net.rptools.maptool.util.SysInfoProvider;
import net.rptools.parser.ParserException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GetInfoFunctionTest {

  @Test
  public void debugInfo() throws ParserException {
    getInfoFunction function = getInfoFunction.getInstance();
    List<Object> params = Collections.singletonList("debug");

    JsonObject json = (JsonObject) function.childEvaluate(null, null, "ignored", params);

    assertNotNull(json);

    JsonObject maptool = json.get("maptool").getAsJsonObject();
    assertEquals("DEVELOPMENT", maptool.get("version").getAsString());
    assertNotNull(maptool.get("max mem avail"));
    assertNotNull(maptool.get("max mem used"));

    JsonObject java = json.get("java").getAsJsonObject();
    assertNotNull(java.get("vendor"));
    assertNotNull(java.get("home"));
    assertNotNull(java.get("version"));

    JsonObject locale = json.get("locale").getAsJsonObject();
    assertNotNull(locale.get("country"));
    assertNotNull(locale.get("language"));
    assertNotNull(locale.get("locale"));
    assertNotNull(locale.get("variant"));

    JsonObject os = json.get("os").getAsJsonObject();
    assertNotNull(os.get("name"));
    assertNotNull(os.get("version"));
    assertNotNull(os.get("arch"));
  }

  @Test
  public void setSysInfoJsonProvider() throws ParserException {
    getInfoFunction function = getInfoFunction.getInstance();

    function.setSysInfoProvider(getDummyProvider());

    assertNull(function.childEvaluate(null, null, "ignored", Collections.singletonList("debug")));
  }

  @NotNull
  private SysInfoProvider getDummyProvider() {
    return new SysInfoProvider() {
      @Override
      public JsonObject getSysInfoJSON() {
        return null;
      }

      @Override
      public List<String> getInfo() {
        return null;
      }
    };
  }
}
