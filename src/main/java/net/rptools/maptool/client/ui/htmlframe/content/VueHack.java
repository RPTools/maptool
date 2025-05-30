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
package net.rptools.maptool.client.ui.htmlframe.content;

import javax.annotation.Nonnull;
import org.jsoup.nodes.Document;

/**
 * This class is a workaround for Vue.js applications that don't work correclty with our setup to
 * move all script tags to the end of the body.
 */
public class VueHack implements PreprocessorOperations {
  @Override
  public void apply(@Nonnull Document document) {
    // Move all script tags to the end of the body
    var body = document.select("body").first();
    if (body == null) {
      body = document.appendElement("body");
    }
    var scripts = document.select("script");
    for (var script : scripts) {
      // Remove the script from its current position
      script.remove();
      // Append it to the end of the body
      body.appendChild(script);
    }
  }

  @Override
  public int getPriority() {
    return 0;
  }
}
