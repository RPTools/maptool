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

import java.awt.*;
import java.util.Collections;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class HTMLPanelRenderer extends JTextPane {
  private static final long serialVersionUID = -7535450508528232780L;

  private final CellRendererPane rendererPane = new CellRendererPane();
  private final StyleSheet styleSheet;
  private Dimension size;

  public HTMLPanelRenderer() {
    setContentType("text/html");
    setEditable(false);
    setDoubleBuffered(false);

    styleSheet = ((HTMLDocument) getDocument()).getStyleSheet();
    styleSheet.addRule("body { font-family: sans-serif; font-size: 11pt}");
    rendererPane.add(this);
    Document document = getDocument();

    // Use a little bit of black magic to get our images to display correctly
    // TODO: Need a way to flush this cache
    HTMLPanelImageCache imageCache = new HTMLPanelImageCache();
    document.putProperty("imageCache", imageCache);
  }

  public void addStyleSheetRule(String rule) {
    styleSheet.addRule(rule);
  }

  public void attach(JComponent c) {
    c.add(rendererPane);
  }

  public void detach(JComponent c) {
    c.remove(rendererPane);
  }

  public Dimension setText(String t, int maxWidth, int maxHeight) {
    setText(t);
    setSize(maxWidth, maxHeight);
    size = getPreferredSize();
    size.width = Math.min(size.width, maxWidth);
    return size;
  }

  public void render(Graphics g, int x, int y) {
    rendererPane.paintComponent(g, this, null, x, y, size.width, size.height);
  }

  @Override
  public boolean contains(Point p) {
    return super.contains(p.x - getX(), p.y - getY());
  }

  public void clickAt(Point p) {
    Point localPoint = new Point(p.x - getX(), p.y - getY());
    Desktop desktop = java.awt.Desktop.getDesktop();
    Document doc = getDocument();
    int pos = viewToModel2D(localPoint);

    if (pos >= 0) {
      HTMLDocument hdoc = (HTMLDocument) doc;
      HTMLDocument.RunElement el = (HTMLDocument.RunElement) hdoc.getCharacterElement(pos);

      String href = null;
      for (Object name : Collections.list(el.getAttributeNames())) {
        Object value = el.getAttribute(name);
        if (!(value instanceof SimpleAttributeSet)) continue;

        SimpleAttributeSet attributes = (SimpleAttributeSet) value;
        href = (String) attributes.getAttribute(HTML.Attribute.HREF);
      }

      if (href != null) {
        try {
          java.net.URI uri = new java.net.URI(href);
          desktop.browse(uri);
        } catch (Exception ev) {
          System.err.println(ev.getMessage());
        }
      }
    }
  }
}
