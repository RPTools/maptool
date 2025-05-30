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
import net.rptools.maptool.client.ui.htmlframe.HTMLWebViewManager.JavaBridge;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 * This class injects the JavaScript API and the java bridge into the HTML document. It ensures that
 * the java bridge and the API is initialised before any other scripts run.
 */
public class InjectJSAPi implements PreprocessorOperations {

  /** JS to initialize the Java bridge. Needs to be the first script of the page. */
  private static final String SCRIPT_BRIDGE =
      String.format("window.status = '%s'; window.status = '';", JavaBridge.BRIDGE_VALUE);

  @Override
  public void apply(@Nonnull Document document) {
    var head = document.select("head").first();
    if (head == null) {
      head = document.appendElement("head");
    }

    var javaBridgeKludge =
        new Element(Tag.valueOf("script"), "")
            .attr("type", "text/javascript")
            .appendChild(new DataNode(SCRIPT_BRIDGE));
    if (head.children().isEmpty()) {
      head.appendChild(javaBridgeKludge);
    } else {
      head.child(0).before(javaBridgeKludge);
    }
  }

  @Override
  public int getPriority() {
    return Integer.MAX_VALUE; // Highest priority value to ensure it runs last
  }
}
