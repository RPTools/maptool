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
package net.rptools.maptool.client.swing.htmleditorsplit;

import java.awt.*;
import java.awt.event.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.StringUtil;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlEditorSplitGui {
  private JPanel rootPanel;
  private JTabbedPane tabPanel;
  private JPanel textTab;
  private JPanel htmlTab;
  private CollapsibleSectionPanel sectionPanel;
  private RTextScrollPane textScrollPane;
  private RSyntaxTextArea sourceTextArea;
  private JFXPanel jfxPanel;
  private JComboBox textTypeComboBox;

  public JComponent getRootComponent() {
    return rootPanel;
  }

  private HTMLEditor htmlEditor;

  private SearchListener searchListener =
      new SearchListener() {

        @Override
        public void searchEvent(SearchEvent e) {
          SearchEvent.Type type = e.getType();
          SearchContext context = e.getSearchContext();
          SearchResult result;

          switch (type) {
            default: // Prevent FindBugs warning later
            case MARK_ALL:
              result = SearchEngine.markAll(sourceTextArea, context);
              break;
            case FIND:
              result = SearchEngine.find(sourceTextArea, context);
              if (!result.wasFound() || result.isWrapped()) {
                UIManager.getLookAndFeel().provideErrorFeedback(sourceTextArea);
              }
              break;
            case REPLACE:
              result = SearchEngine.replace(sourceTextArea, context);
              if (!result.wasFound() || result.isWrapped()) {
                UIManager.getLookAndFeel().provideErrorFeedback(sourceTextArea);
              }
              break;
            case REPLACE_ALL:
              result = SearchEngine.replaceAll(sourceTextArea, context);
              break;
          }
        }

        @Override
        public String getSelectedText() {
          return sourceTextArea.getSelectedText();
        }
      };

  private String text;

  public HtmlEditorSplitGui() {
    // This has to be done here after the RSyntaxTextarea is set via setViewportView.
    // Otherwise, there will be no line numbers or folding indicators.
    textScrollPane.setFoldIndicatorEnabled(true);
    textScrollPane.setLineNumbersEnabled(true);

    textTypeComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_NONE);
    textTypeComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_HTML);
    textTypeComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
    textTypeComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            setTextStyle((String) e.getItem());
          }
        });

    LanguageSupportFactory.get().register(sourceTextArea);
    var findToolBar = new FindToolBar(searchListener);
    SearchContext context = findToolBar.getSearchContext();
    var replaceToolBar = new ReplaceToolBar(searchListener);
    replaceToolBar.setSearchContext(context);

    sectionPanel.addBottomComponent(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), findToolBar);
    sectionPanel.addBottomComponent(
        KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK), replaceToolBar);

    jfxPanel.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              // consume the event to avoid closing the dialog
              e.consume();
            }
          }
        });

    Platform.runLater(
        () -> {
          htmlEditor = new HTMLEditor();
          WebView webView = (WebView) htmlEditor.lookup("WebView");
          webView.setContextMenuEnabled(false);
          var menu = new ContextMenu();

          MenuItem sendToChatItem =
              new MenuItem(I18N.getString("EditTokenDialog.menu.notes.sendChat"));
          sendToChatItem.setOnAction(
              e12 -> {
                String selectedText = getSelectedText();
                if (selectedText == null) {
                  selectedText = getText();
                }
                // TODO: Combine this with the code in MacroButton
                JTextComponent commandArea =
                    MapTool.getFrame().getCommandPanel().getCommandTextArea();

                String finalSelectedText = selectedText;
                SwingUtilities.invokeLater(
                    () -> {
                      commandArea.setText(commandArea.getText() + finalSelectedText);
                      commandArea.requestFocusInWindow();
                    });
              });
          menu.getItems().add(sendToChatItem);

          MenuItem sendAsEmoteItem =
              new MenuItem(I18N.getString("EditTokenDialog.menu.notes.sendEmit"));
          sendAsEmoteItem.setOnAction(
              e1 -> {
                String selectedText = getSelectedText();
                if (selectedText == null) {
                  selectedText = getText();
                }
                // TODO: Combine this with the code in MacroButton
                String finalSelectedText = selectedText;
                SwingUtilities.invokeLater(
                    () -> {
                      MapTool.getFrame()
                          .getCommandPanel()
                          .commitCommand("/emit " + finalSelectedText);
                      MapTool.getFrame()
                          .getCommandPanel()
                          .getCommandTextArea()
                          .requestFocusInWindow();
                    });
              });
          menu.getItems().add(sendAsEmoteItem);
          htmlEditor.setContextMenu(menu);
          Scene scene = new Scene(htmlEditor);
          htmlEditor.setHtmlText(text);
          jfxPanel.setScene(scene);
        });

    tabPanel.addChangeListener(
        e -> {
          var selectedTab = tabPanel.getSelectedComponent();
          if (selectedTab.equals(htmlTab)) {
            Platform.runLater(
                () -> {
                  if (sourceTextArea
                      .getSyntaxEditingStyle()
                      .equals(SyntaxConstants.SYNTAX_STYLE_MARKDOWN)) {
                    htmlEditor.setHtmlText(StringUtil.markDownToHtml(sourceTextArea.getText()));
                  } else {
                    htmlEditor.setHtmlText(sourceTextArea.getText());
                  }
                });
          } else if (!htmlEditor.isDisabled()) {
            var html = htmlEditor.getHtmlText();
            Document doc = Jsoup.parse(html);
            sourceTextArea.setText(doc.body().html());
          }
        });
    setTextStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
  }

  public void setText(String text) {
    this.text = text;
    sourceTextArea.setText(text);
    Platform.runLater(
        () -> {
          if (htmlEditor != null) {
            htmlEditor.setHtmlText(text == null ? "" : text);
          }
        });
  }

  public void setTextStyle(String style) {
    if (style.equals(SyntaxConstants.SYNTAX_STYLE_HTML)) {
      Platform.runLater(() -> htmlEditor.setDisable(false));
      tabPanel.setEnabledAt(1, true);
    } else if (style.equals(SyntaxConstants.SYNTAX_STYLE_MARKDOWN)) {
      Platform.runLater(() -> htmlEditor.setDisable(true));
      tabPanel.setEnabledAt(1, true);
    } else {
      tabPanel.setSelectedIndex(0);
      tabPanel.setEnabledAt(1, false);
    }
    sourceTextArea.setSyntaxEditingStyle(style);
    textTypeComboBox.setSelectedItem(style);
  }

  public String getTextStyle() {
    return sourceTextArea.getSyntaxEditingStyle();
  }

  public String getText() {
    // first select text tab to force conversion of html
    tabPanel.setSelectedComponent(textTab);
    return sourceTextArea.getText();
  }

  public static final String SELECT_TEXT =
      "(function getSelectionText() {\n"
          + "    var text = \"\";\n"
          + "    if (window.getSelection) {\n"
          + "        text = window.getSelection().toString();\n"
          + "    } else if (document.selection && document.selection.type != \"Control\") {\n"
          + "        text = document.selection.createRange().text;\n"
          + "    }\n"
          + "    if (window.getSelection) {\n"
          + "      if (window.getSelection().empty) {  // Chrome\n"
          + "        window.getSelection().empty();\n"
          + "      } else if (window.getSelection().removeAllRanges) {  // Firefox\n"
          + "        window.getSelection().removeAllRanges();\n"
          + "      }\n"
          + "    } else if (document.selection) {  // IE?\n"
          + "      document.selection.empty();\n"
          + "    }"
          + "    return text;\n"
          + "})()";

  public String getSelectedText() {
    var selectedTab = tabPanel.getSelectedComponent();
    if (selectedTab.equals(htmlTab)) {
      WebView webView = (WebView) htmlEditor.lookup("WebView");
      if (webView == null) {
        return htmlEditor.getHtmlText();
      }
      WebEngine engine = webView.getEngine();
      Object selection = engine.executeScript(SELECT_TEXT);
      return selection.toString();
    } else {
      return sourceTextArea.getSelectedText();
    }
  }

  public void setEnabled(boolean enabled) {
    sourceTextArea.setEnabled(enabled);
    Platform.runLater(() -> htmlEditor.setDisable(!enabled));
    tabPanel.setEnabled(enabled);
  }
}
