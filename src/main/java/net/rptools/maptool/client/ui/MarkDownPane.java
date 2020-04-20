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
package net.rptools.maptool.client.ui;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import net.rptools.maptool.client.ui.htmlframe.HTMLWebViewManager;

public class MarkDownPane extends AnchorPane {

  private final ParserEmulationProfile parserEmulationProfile;

  private final HTMLWebViewManager htmlWebViewManager = new HTMLWebViewManager();

  public MarkDownPane(ParserEmulationProfile profile) {

    htmlWebViewManager.setupWebView(new WebView());
    WebView webView = htmlWebViewManager.getWebView();

    this.getChildren().add(webView);
    AnchorPane.setTopAnchor(webView, 0.0);
    AnchorPane.setBottomAnchor(webView, 0.0);
    AnchorPane.setLeftAnchor(webView, 0.0);
    AnchorPane.setRightAnchor(webView, 0.0);

    parserEmulationProfile = profile;
  }

  public void setText(String markDownText) {
    MutableDataHolder options = new MutableDataSet();
    options.setFrom(parserEmulationProfile);

    Parser parser = Parser.builder(options).build();
    HtmlRenderer renderer = HtmlRenderer.builder(options).build();

    Node document = parser.parse(markDownText);

    WebView webView = htmlWebViewManager.getWebView();
    webView.getEngine().loadContent(renderer.render(document));
  }
}
