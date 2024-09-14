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
package net.rptools.maptool.util;

import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class HTMLUtil {

  /**
   * Parses the HTML in the string and sets the base to the correct location.
   *
   * @param htmlString the HTML to parse.
   * @param url the origin URL to set the base relative to
   * @return the fixed-up HTML
   */
  public static String fixHTMLBase(String htmlString, URL url) {
    var document = Jsoup.parse(htmlString);
    var head = document.select("head").first();
    if (head != null) {
      String baseURL = url.toExternalForm().replaceFirst("\\?.*", "");
      baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
      var baseElement = new Element(Tag.valueOf("base"), "").attr("href", baseURL);
      if (head.children().isEmpty()) {
        head.appendChild(baseElement);
      } else {
        head.child(0).before(baseElement);
      }

      htmlString = document.html();
    }
    return htmlString;
  }
}
