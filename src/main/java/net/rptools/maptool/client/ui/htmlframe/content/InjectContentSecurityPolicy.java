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

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jsoup.nodes.Document;

/**
 * This class injects a Content Security Policy (CSP) into the HTML document. The CSP is used to
 * control which resources can be loaded and executed in the document, enhancing security by
 * preventing cross-site scripting (XSS) attacks and other vulnerabilities.
 */
public class InjectContentSecurityPolicy implements PreprocessorOperations {
  /**
   * Enum that contains the content security policy directives. This is not strictly required but it
   * is better than having them in a long string as it is easier to read, maintain and document.
   */
  public enum CSPContentDirective {
    /**
     * The default-src directive is used to specify the default policy for fetching resources such
     * as JavaScript, CSS, images, fonts, AJAX requests, frames, and HTML5 media.
     */
    DEFAULT_SRC("default-src"),
    /** asset:// URL scheme */
    ASSET("asset:"),
    /** lib:// URL scheme */
    LIB("lib:"),
    /** JQuery Content Delivery Network */
    JQUERY_CDN("https://code.jquery.com"),
    /** JSDelier Content Delivery Network (bootstrap, FontAwesome, Bootswatch, Bootstrap Icons) */
    JSDELIVR_CDN("https://cdn.jsdelivr.net"),
    /** UNPKG Content Delivery Network (React, ReactDOM, React Router, etc.) */
    UNPKG_CDN("https://unpkg.com"),
    /** Cloudflare Content Delivery Network */
    CLOUDFLARE_JS_CDN("https://cdnjs.cloudflare.com"),
    /** AJAX Content Delivery Network */
    AJAX_CDN("https://ajax.googleapis.com"),
    /** Google Hosted Libraries Content Delivery Network */
    FONTS_CDN("https://fonts.googleapis.com https://fonts.gstatic.com"),
    /** Inline content */
    INLINE("'unsafe-inline'"),
    /** Evaluate content */
    EVAL("'unsafe-eval'"),
    /** Terminator for the default-src directive */
    DEFAULT_TERMINATOR(";"),
    /** Image Source Policy */
    IMG_SRC("img-src"),
    /** Image Source Policy for all images */
    IMG_ALL("*"),
    /** Image Source Policy for all images from the asset:// URL scheme */
    IMG_ASSET("asset:"),
    /** Image Source Policy for all images from the lib:// URL scheme */
    IMG_LIB("lib:"),
    /** Terminator for the img-src directive */
    IMG_TERMINATOR(";"),
    /** Font Source Policy */
    FONT_SRC("font-src"),
    /** Google Fonts */
    FONT_GOOGLE("https://fonts.gstatic.com"),
    /** Self */
    FONT_SELF("'self'");

    /**
     * Constructor for the CSPContentDirective enum.
     *
     * @param content the content of the directive
     */
    CSPContentDirective(String content) {
      this.content = content;
    }

    /**
     * Returns the content of the directive.
     *
     * @return the content of the directive
     */
    public String getContent() {
      return content;
    }

    /** The content of the directive. */
    private final String content;
  }

  /** Content Security Policy (CSP) for the HTML content. */
  private static final String META_CSP_CONTENT =
      Arrays.stream(CSPContentDirective.values())
          .map(CSPContentDirective::getContent)
          .collect(Collectors.joining(" "));

  @Override
  public void apply(@Nonnull Document document) {
    var head = document.select("head").first();
    if (head == null) {
      head = document.appendElement("head");
    }
    head.attr("Content-Security-Policy", META_CSP_CONTENT);
  }

  @Override
  public int getPriority() {
    return 0;
  }
}
