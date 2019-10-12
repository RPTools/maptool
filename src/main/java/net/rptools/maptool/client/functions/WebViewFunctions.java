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

import java.util.List;
import net.rptools.maptool.client.ui.webviewframe.WebViewFrame;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class WebViewFunctions extends AbstractFunction {

  private static final WebViewFunctions instance = new WebViewFunctions();

  public static WebViewFunctions getInstance() {
    return instance;
  }

  private WebViewFunctions() {
    super(1, 2, "webview.loadURL", "webview.show", "webview.hide");
  }

  @Override
  public Object childEvaluate(Parser parser, String name, List<Object> args)
      throws ParserException {
    if ("webview.loadURL".equalsIgnoreCase(name)) {
      WebViewFrame.loadURL(args.get(0).toString(), args.get(1).toString());
    } else if ("webview.show".equalsIgnoreCase(name)) {
      WebViewFrame.show(args.get(0).toString());
    } else if ("webview.hide".equalsIgnoreCase(name)) {
      WebViewFrame.hide(args.get(0).toString());
    }
    return "";
  }
}
