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
package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

public class TokenListCellRenderer extends DefaultListCellRenderer {

  private BufferedImage image;
  private String name;

  @Override
  public Component getListCellRendererComponent(
      JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (value instanceof Token) {
      Token token = (Token) value;
      image = ImageManager.getImage(token.getImageAssetId(), this);
      name = token.getName();

      setText(" "); // hack to keep the row height the right size
    }
    return this;
  }

  @Override
  protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    if (image != null) {

      Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
      SwingUtil.constrainTo(imageSize, getSize().height);
      g.drawImage(image, 0, 0, imageSize.width, imageSize.height, this);
      g.drawString(name, imageSize.width + 2, g.getFontMetrics().getAscent());
    }
  }
}
