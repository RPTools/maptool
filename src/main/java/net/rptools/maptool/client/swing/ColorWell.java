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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;

@SuppressWarnings("serial")
public class ColorWell extends JComponent {

  private Paint paint;

  private MouseAdapter mouseAdapter;

  private final List<ActionListener> listeners = new LinkedList<>();

  public ColorWell() {
    this(Color.BLACK);
  }

  public ColorWell(Paint paint) {
    this.paint = paint;
    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    setPreferredSize(new Dimension(16, 16));
    initDefaultMouseAdapter();
  }

  public void setMouseAdapter(MouseAdapter adapter) {
    if (mouseAdapter != null) removeMouseListener(mouseAdapter);

    if (adapter != null) {
      addMouseListener(adapter);
      mouseAdapter = adapter;
    } else {
      initDefaultMouseAdapter();
    }
  }

  private void initDefaultMouseAdapter() {
    mouseAdapter =
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            var currentColor = getColor();
            if (currentColor == null) {
              currentColor = Color.BLACK;
            }

            var result =
                JColorChooser.showDialog(
                    SwingUtilities.getWindowAncestor(ColorWell.this),
                    I18N.getString("PaintChooser.title"),
                    currentColor);
            if (result != null) {
              setPaint(result);
              notifyListeners();
            }
          }
        };
    addMouseListener(mouseAdapter);
  }

  public Paint getPaint() {
    return paint;
  }

  public void setPaint(Paint paint) {
    this.paint = paint;
    repaint();
  }

  public Color getColor() {
    if (paint instanceof Color color) {
      return color;
    }
    return null;
  }

  public void setColor(Color color) {
    setPaint(color);
  }

  public void addActionListener(ActionListener listener) {
    if (listeners.contains(listener)) return;

    listeners.add(listener);
  }

  public void removeActionListener(ActionListener listener) {
    if (!listeners.contains(listener)) return;

    listeners.remove(listener);
  }

  private void notifyListeners() {
    for (var listener : listeners) {
      listener.actionPerformed(
          new ActionEvent(ColorWell.this, ActionEvent.ACTION_PERFORMED, getName()));
    }
  }

  @Override
  protected void paintComponent(Graphics g) {

    var rect = g.getClipBounds();
    g.setColor(getBackground());
    g.fillRect(rect.x, rect.y, rect.width, rect.height);

    if (paint != null) {
      ((Graphics2D) g).setPaint(paint);
      g.fillRect(rect.x, rect.y, rect.width, rect.height);
    } else {
      BufferedImage texture = RessourceManager.getImage(Images.TEXTURE_TRANSPARENT);
      TexturePaint tp = new TexturePaint(texture, new Rectangle(0, 0, 28, 28));
      ((Graphics2D) g).setPaint(tp);
      g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }
  }
}
