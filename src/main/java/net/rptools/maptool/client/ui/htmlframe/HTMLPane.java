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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.parser.ParserException;
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
  private static final String CSS_RULE_BODY =
      "body { font-family: sans-serif; font-size: %dpt; background: #ECE9D8}";
  /** The default rule for the div tag. */
  private static final String CSS_RULE_DIV = "div {margin-bottom: 5px}";
  /** The default rule for the span tag. */
  private static final String CSS_RULE_SPAN = "span.roll {background:#efefef}";

  /** Replacement for the HyperlinkListener, to better handle hyperlink clicks. */
  private final class HyperlinkMouseListener extends MouseAdapter {
    @Override
    public void mouseReleased(MouseEvent e) {
      // Triggered by a  mouse button release, which is much more lenient than a mouse click
      Element h = getHyperlinkElement(e);
      if (h != null && e.getButton() == MouseEvent.BUTTON1) {
        Object attribute = h.getAttributes().getAttribute(HTML.Tag.A);
        if (attribute instanceof AttributeSet) {
          AttributeSet set = (AttributeSet) attribute;
          String href = (String) set.getAttribute(HTML.Attribute.HREF);
          if (href != null) {
            String href2 = href.trim().toLowerCase();
            if (href2.startsWith("macro")) {
              // run as macroLink;
              SwingUtilities.invokeLater(() -> MacroLinkFunction.runMacroLink(href));
            } else if (href2.startsWith("#")) {
              scrollToReference(href.substring(1)); // scroll to the anchor
              setCursor(editorKit.getDefaultCursor()); // replace cursor, or it will stay as a hand
            } else if (!href2.startsWith("javascript")) {
              // non-macrolink, non-anchor link, non-javascript code
              MapTool.showDocument(href); // show in usual browser
            }
          }
        }
      }
    }

    /**
     * Returns the hyperlink element from a mouse event.
     *
     * @param event the mouse event triggering the hyperlink
     * @return the document element corresponding to the link
     */
    private Element getHyperlinkElement(MouseEvent event) {
      JEditorPane editor = (JEditorPane) event.getSource();
      int pos = editor.getUI().viewToModel2D(editor, event.getPoint(), new Position.Bias[1]);
      if (pos >= 0 && editor.getDocument() instanceof HTMLDocument) {
        HTMLDocument hdoc = (HTMLDocument) editor.getDocument();
        Element elem = hdoc.getCharacterElement(pos);
        if (elem.getAttributes().getAttribute(HTML.Tag.A) != null) {
          return elem;
        }
      }
      return null;
    }
  }

  public HTMLPane() {
    editorKit = new HTMLPaneEditorKit(this);
    setEditorKit(editorKit);
    setContentType("text/html");
    setEditable(false);

    addMouseListener(new HyperlinkMouseListener()); // not a HyperlinkListener

    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public void addActionListener(ActionListener listener) {
    actionListeners = AWTEventMulticaster.add(actionListeners, listener);
  }

  public void removeActionListener(ActionListener listener) {
    actionListeners = AWTEventMulticaster.remove(actionListeners, listener);
  }

  /**
   * Handle a submit.
   *
   * @param method The method of the submit.
   * @param action The action for the submit.
   * @param data The data from the form.
   */
  void doSubmit(String method, String action, String data) {
    if (actionListeners != null) {
      if (log.isDebugEnabled()) {
        log.debug(
            "submit event: method='" + method + "' action='" + action + "' data='" + data + "'");
      }
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
      if (log.isDebugEnabled()) {
        log.debug("changeTitle event: " + title);
      }
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
      if (log.isDebugEnabled()) {
        log.debug("registerMacro event: type='" + type + "' link='" + link + "'");
      }
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
      if (log.isDebugEnabled()) {
        log.debug("metaTag found: name='" + name + "' content='" + content + "'");
      }
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

      style.addRule(String.format(CSS_RULE_BODY, AppPreferences.getFontSize()));
      style.addRule(CSS_RULE_DIV);
      style.addRule(CSS_RULE_SPAN);
      parse.parse(new StringReader(text), new ParserCallBack(), true);
    } catch (IOException e) {
      // Do nothing, we should not get an io exception on string
    }
    if (log.isDebugEnabled()) {
      log.debug("setting text in HTMLPane: " + text);
    }
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
      if (log.isTraceEnabled()) {
        log.trace("handleError called in client.ui.htmlframe.HTMLPane.ParserCallBack: " + errorMsg);
      }
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
            String cssText = MapTool.getParser().getTokenLibMacro(vals[0], vals[1]);
            HTMLDocument document = (HTMLDocument) getDocument();
            StyleSheet style = document.getStyleSheet();
            style.loadRules(new StringReader(cssText), null);
          } catch (ParserException e) {
            // Do nothing
          } catch (IOException e) {
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
