package net.rptools.maptool.client.ui.webviewframe;


import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

public class WebViewFrame extends DockableFrame {

  private static Map<String, WebViewFrame> webViewFrameMap = new HashMap<>();

  private String name;
  private String url;
  private WebView webView;
  private JFXPanel jfxPanel;

  public static WebViewFrame loadURL(String name, String url) {
    WebViewFrame webViewFrame;
    if (webViewFrameMap.containsKey(name)) {
      webViewFrame = webViewFrameMap.get(name);
    } else {
      webViewFrame = new WebViewFrame(name);
      webViewFrameMap.put(name, webViewFrame);
    }

    webViewFrame.getDockingManager().showFrame(name);
    webViewFrame.setVisible(true);
    webViewFrame.loadURL(url);

    return webViewFrame;
  }


  public static void show(String name) throws ParserException {
    WebViewFrame webViewFrame = webViewFrameMap.get(name);
    if (webViewFrame == null) {
      throw new ParserException(I18N.getText("macro.function.webview.nowebview ", name));
    }

    webViewFrame.getDockingManager().showFrame(name);
  }



  public static void hide(String name) throws ParserException {
    WebViewFrame webViewFrame = webViewFrameMap.get(name);
    if (webViewFrame == null) {
      throw new ParserException(I18N.getText("macro.function.webview.nowebview ", name));
    }

    webViewFrame.getDockingManager().hideFrame(name);
  }


  private WebViewFrame(String name) {
    super(name);
    this.name = name;
    jfxPanel = new JFXPanel();
    this.add(jfxPanel);

    getContext().setInitMode(DockContext.STATE_FLOATING);
    MapTool.getFrame().getDockingManager().addFrame(this);


    Platform.runLater(() -> initWebView(jfxPanel));
  }

  private void initWebView(JFXPanel jfxPanel) {
    StackPane root = new StackPane();
    webView = new WebView();
    root.getChildren().add(webView);
    Scene scene = new Scene(root);
    jfxPanel.setScene(scene);
  }

  private void loadURL(String url) {
    Platform.runLater(() -> webView.getEngine().load(url));
  }


}
