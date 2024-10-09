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
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.ui.commandpanel.MessagePanel;
import net.rptools.maptool.model.library.LibraryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Represents the panel holding the HTMLPaneEditorKit for HTML3.2. */
@SuppressWarnings("serial")
public class HTMLPane extends JEditorPane {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLPane.class);

  /** The action listeners for the container. */
  private ActionListener actionListeners;

  /** The editorKit that handles the HTML. */
  private final HTMLPaneEditorKit editorKit;

  /** The default rule for the body tag. */
  private static final String CSS_RULE_BODY = "body { font-family: sans-serif; font-size: %dpt; }";

  /** The default rule for the div tag. */
  private static final String CSS_RULE_DIV = "div {margin-bottom: 5px}";

  /** The default rule for the span tag. */
  private static final String CSS_RULE_SPAN = "span.roll {background:#efefef}";

  public HTMLPane() {
    editorKit = new HTMLPaneEditorKit(this);
    setEditorKit(editorKit);
    setContentType("text/html");
    setEditable(false);

    addHyperlinkListener(
        e -> {
          log.debug("Responding to hyperlink event: {} {}", e.getEventType(), e);
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (e.getURL() != null) {
              MapTool.showDocument(e.getURL().toString());
            } else if (e.getDescription().startsWith("#")) {
              scrollToReference(e.getDescription().substring(1)); // scroll to the anchor
            } else {
              Matcher m = MessagePanel.URL_PATTERN.matcher(e.getDescription());
              if (m.matches() && m.group(1).equalsIgnoreCase("macro")) {
                MacroLinkFunction.runMacroLink(e.getDescription());
              }
            }
          }
        });

    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * @return the rule for the body tag
   */
  public String getRuleBody() {
    return String.format(CSS_RULE_BODY, AppPreferences.fontSize.get());
  }

  public void addActionListener(ActionListener listener) {
    actionListeners = AWTEventMulticaster.add(actionListeners, listener);
  }

  public void removeActionListener(ActionListener listener) {
    actionListeners = AWTEventMulticaster.remove(actionListeners, listener);
  }

  /**
   * Set the default cursor of the editor kit.
   *
   * @param cursor the cursor to set
   */
  public void setEditorKitDefaultCursor(Cursor cursor) {
    editorKit.setDefaultCursor(cursor);
  }

  /**
   * Flush the pane, set the new html, and set the caret to zero.
   *
   * @param html the html to set
   * @param scrollReset whether the scrollbar should be reset
   */
  public void updateContents(final String html, boolean scrollReset) {
    EventQueue.invokeLater(
        () -> {
          DefaultCaret caret = (DefaultCaret) getCaret();
          caret.setUpdatePolicy(
              scrollReset ? DefaultCaret.UPDATE_WHEN_ON_EDT : DefaultCaret.NEVER_UPDATE);
          editorKit.flush();
          setText(html);
          if (scrollReset) {
            setCaretPosition(0);
          }
        });
  }

  /** Flushes any caching for the panel. */
  public void flush() {
    EventQueue.invokeLater(editorKit::flush);
  }

  /**
   * Handle a submit.
   *
   * @param method The method of the submit.
   * @param action The action for the submit.
   * @param data The data from the form.
   */
  public void doSubmit(String method, String action, String data) {
    if (actionListeners != null) {
      log.debug("submit event: method='{}' action='{}' data='{}'", method, action, data);
      actionListeners.actionPerformed(
          new HTMLActionEvent.FormActionEvent(this, method, action, data));
    }
  }

  /**
   * Handle a change in title.
   *
   * @param title The title to change to.
   */
  private void doChangeTitle(String title) {
    if (actionListeners != null) {
      log.debug("changeTitle event: {}", title);
      actionListeners.actionPerformed(new HTMLActionEvent.ChangeTitleActionEvent(this, title));
    }
  }

  /**
   * Handle a request to register a macro callback.
   *
   * @param type The type of event.
   * @param link The link to the macro.
   */
  private void doRegisterMacro(String type, String link) {
    if (actionListeners != null) {
      log.debug("registerMacro event: type='{}' link='{}'", type, link);
      actionListeners.actionPerformed(
          new HTMLActionEvent.RegisterMacroActionEvent(this, type, link));
    }
  }

  /**
   * Handle any meta tag information in the html.
   *
   * @param name the name of the meta tag.
   * @param content the content of the meta tag.
   */
  private void handleMetaTag(String name, String content) {
    if (actionListeners != null) {
      log.debug("metaTag found: name='{}' content='{}'", name, content);
      actionListeners.actionPerformed(new HTMLActionEvent.MetaTagActionEvent(this, name, content));
    }
  }

  @Override
  public void setText(String text) {
    // Set up the default style sheet

    HTMLDocument document = (HTMLDocument) getDocument();

    StyleSheet style = document.getStyleSheet();

    HTMLEditorKit.Parser parse = editorKit.getParser();
    try {
      super.setText("");
      Enumeration<?> snames = style.getStyleNames();
      List<String> styleNames = new ArrayList<String>();

      while (snames.hasMoreElements()) {
        styleNames.add(snames.nextElement().toString());
      }

      for (String s : styleNames) {
        style.removeStyle(s);
      }

      style.addRule(getRuleBody());
      style.addRule(CSS_RULE_DIV);
      style.addRule(CSS_RULE_SPAN);
      parse.parse(new StringReader(text), new ParserCallBack(), true);
    } catch (IOException e) {
      // Do nothing, we should not get an io exception on string
    }
    log.debug("setting text in HTMLPane: {}", text);
    super.setText(HTMLPanelInterface.fixHTML(text));
  }

  /** Class that deals with html parser callbacks. */
  class ParserCallBack extends HTMLEditorKit.ParserCallback {
    private final Stack<HTML.Tag> tagStack = new Stack<HTML.Tag>();

    @Override
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
      tagStack.push(tag);
      if (tag == HTML.Tag.LINK) {
        handleLinkTag(attributes);
      } else if (tag == HTML.Tag.META) {
        handleMetaTag(attributes);
      }
    }

    @Override
    public void handleEndTag(HTML.Tag tag, int position) {
      tagStack.pop();
    }

    @Override
    public void handleText(char[] text, int position) {
      if (tagStack.peek() == HTML.Tag.TITLE) {
        doChangeTitle(String.valueOf(text));
      }
    }

    @Override
    public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
      if (tag == HTML.Tag.LINK) {
        handleLinkTag(attributes);
      } else if (tag == HTML.Tag.META) {
        handleMetaTag(attributes);
      }
    }

    @Override
    public void handleError(String errorMsg, int pos) {
      log.trace("handleError called in client.ui.htmlframe.HTMLPane.ParserCallBack: {}", errorMsg);
    }

    /**
     * Handles meta tags.
     *
     * @param attributes the attributes for the tag.
     */
    void handleMetaTag(MutableAttributeSet attributes) {
      Object name = attributes.getAttribute(HTML.Attribute.NAME);
      Object content = attributes.getAttribute(HTML.Attribute.CONTENT);

      if (name != null && content != null) {
        HTMLPane.this.handleMetaTag(name.toString(), content.toString());
      }
    }

    /**
     * Handles all the actions for a HTML Link tag.
     *
     * @param attributes The attributes for the tag.
     */
    void handleLinkTag(MutableAttributeSet attributes) {
      Object rel = attributes.getAttribute(HTML.Attribute.REL);
      Object type = attributes.getAttribute(HTML.Attribute.TYPE);
      Object href = attributes.getAttribute(HTML.Attribute.HREF);

      if (rel != null && type != null && href != null) {
        if (rel.toString().equalsIgnoreCase("stylesheet")) {
          String[] vals = href.toString().split("@");
          if (vals.length != 2) {
            return;
          }
          try {
            var lib = new LibraryManager().getLibrary(vals[1].substring(4));
            if (lib.isPresent()) {
              var library = lib.get();
              var macroInfo = library.getMTScriptMacroInfo(vals[0]).get();

              if (macroInfo.isPresent()) {
                String cssText = macroInfo.get().macro();
                HTMLDocument document = (HTMLDocument) getDocument();
                StyleSheet style = document.getStyleSheet();
                style.loadRules(new StringReader(cssText), null);
              }
            }
          } catch (IOException | ExecutionException | InterruptedException e) {
            // Do nothing
          }
        } else if (type.toString().equalsIgnoreCase("macro")) {
          if (rel.toString().equalsIgnoreCase("onChangeImpersonated")) {
            doRegisterMacro("onChangeImpersonated", href.toString());
          } else if (rel.toString().equalsIgnoreCase("onChangeSelection")) {
            doRegisterMacro("onChangeSelection", href.toString());
          } else if (rel.toString().equalsIgnoreCase("onChangeToken")) {
            doRegisterMacro("onChangeToken", href.toString());
          }
        }
      }
    }
  }
}
