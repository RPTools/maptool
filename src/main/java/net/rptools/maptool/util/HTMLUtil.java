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

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class HTMLUtil {
  private static Parser markDownParser;
  private static HtmlRenderer htmlRenderer;

  public static String markDownToHtml(String markdown) {
    if (markDownParser == null) {
      var options = new MutableDataSet();
      options.set(
          Parser.EXTENSIONS,
          Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
      markDownParser = Parser.builder(options).build();
      htmlRenderer = HtmlRenderer.builder(options).build();
    }
    var document = markDownParser.parse(markdown);
    return htmlRenderer.render(document);
  }

  public static String htmlize(String input, String type) {
    if (StringUtils.isEmpty(input)) {
      return "";
    }

    return switch (type) {
      case SyntaxConstants.SYNTAX_STYLE_NONE -> plaintextToHtml(input);
      case SyntaxConstants.SYNTAX_STYLE_HTML -> input;
      case SyntaxConstants.SYNTAX_STYLE_MARKDOWN -> markDownToHtml(input);
      default -> input;
    };
  }

  private static String plaintextToHtml(String plainText) {
    plainText = plainText.replaceAll("\n\n", "<p>");
    plainText = plainText.replaceAll("\n", "\n<br>\n");
    return plainText;
  }
}
