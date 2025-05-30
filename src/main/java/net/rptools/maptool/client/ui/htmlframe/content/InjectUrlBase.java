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
import org.jsoup.nodes.Element;

/**
 * This class injects a base URL into the HTML document. The base URL is used to resolve relative
 * URLs in the document, allowing for correct linking of resources such as images, scripts, and
 * stylesheets.
 */
public class InjectUrlBase implements PreprocessorOperations {
  /** The base URL to inject into the HTML document. */
  private final String baseUrl;

  /**
   * Constructor for the InjectUrlBase class.
   *
   * @param baseUrl The base URL to inject into the HTML document.
   */
  public InjectUrlBase(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @Override
  public void apply(@Nonnull Document document) {
    var head = document.select("head").first();
    if (head == null) {
      head = document.appendElement("head");
    }
    if (head.children().isEmpty()) {
      // If the head is empty, append the base element directly
      head.appendElement("base").attr("href", baseUrl);
    } else {
      // Otherwise, insert the base element before the first child of the head
      head.child(0).before(new Element("base").attr("href", baseUrl));
    }
  }

  @Override
  public int getPriority() {
    return Integer.MAX_VALUE; // Highest priority value to ensure it runs last
  }
}
