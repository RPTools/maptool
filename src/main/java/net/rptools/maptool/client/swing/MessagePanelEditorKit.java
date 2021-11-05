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
package net.rptools.maptool.client.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class MessagePanelEditorKit extends HTMLEditorKit {

  private ViewFactory viewFactory = new MessagePanelViewFactory();

  private ImageLoaderCache imageCache = new ImageLoaderCache();

  private boolean macroLinkTTips = true;

  public MessagePanelEditorKit() {
    viewFactory = new MessagePanelViewFactory();
  }

  public void setUseMacroLinkToolTips(boolean show) {
    macroLinkTTips = show;
  }

  private LinkController linkHandler = new LinkController2();

  @Override
  public ViewFactory getViewFactory() {
    return viewFactory;
  }

  public void flush() {
    imageCache.flush();
  }

  private class MessagePanelViewFactory extends HTMLFactory {

    @Override
    public View create(Element elem) {

      AttributeSet attrs = elem.getAttributes();
      Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
      Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) {
        HTML.Tag kind = (HTML.Tag) o;
        if (kind == HTML.Tag.INPUT || kind == HTML.Tag.SELECT || kind == HTML.Tag.TEXTAREA) {
          return new SubmitFormView(elem);
        }
        if (kind == HTML.Tag.IMG) {
          return new MessagePanelImageView(elem, imageCache);
        }
        if (kind == HTML.Tag.CONTENT) {
          return new TooltipView(elem, macroLinkTTips);
        }
      }

      return super.create(elem);
    }
  }

  @Override
  public void install(JEditorPane c) {
    super.install(c);
    for (MouseListener listener : c.getMouseListeners()) {
      if (listener instanceof LinkController) {
        c.removeMouseListener(listener); // remove the link handler
      }
    }
    c.addMouseListener(linkHandler); // add the modified one
  }

  @Override
  public void deinstall(JEditorPane c) {
    c.removeMouseListener(linkHandler); // remove the modified link handler
    super.deinstall(c);
  }

  /** Modify the link controller so that links are triggered on mouse release */
  private static class LinkController2 extends LinkController
      implements MouseMotionListener, Serializable {
    @Override
    public void mouseClicked(MouseEvent e) {} // don't activate links by clicks

    @Override
    public void mouseReleased(MouseEvent e) {
      super.mouseClicked(e); // activate links by mouse release instead
    }
  }
}
