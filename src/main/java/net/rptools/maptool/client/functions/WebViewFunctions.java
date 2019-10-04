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
  public Object childEvaluate(Parser parser, String name, List<Object> args) throws ParserException {
    if ("webview.loadURL".equalsIgnoreCase(name)) {
      WebViewFrame.loadURL(args.get(0).toString(), args.get(1).toString());
    } else if("webview.show".equalsIgnoreCase(name)) {
      WebViewFrame.show(args.get(0).toString());
    } else if ("webview.hide".equalsIgnoreCase(name)) {
      WebViewFrame.hide(args.get(0).toString());
    }
    return "";
  }
}
