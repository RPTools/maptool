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
package net.rptools.maptool.client.ui.lookuptable;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.util.ImageManager;

/** Render icons in a table column which are proportionally scaled to size constraints. */
public class TableColumnIconRenderer extends DefaultTableCellRenderer {

  private final int maxWidth;
  private final int maxHeight;
  private static final int IMAGE_PADDING = 1;

  /**
   * Constructor takes the size constraints, typically based on the table's row and column
   * dimensions.
   *
   * @param maxWidth the maximum width to fit the icon (i.e. the preferred column width)
   * @param maxHeight the maximum height to fit the icon (i.e. the table rowHeight)
   */
  public TableColumnIconRenderer(int maxWidth, int maxHeight) {
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    // Center the icon horizontally and vertically within the cell
    setHorizontalAlignment(JLabel.CENTER);
    setVerticalAlignment(JLabel.CENTER);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);

    // Clean up text from parent class
    setText("");
    setIcon(null);

    LookupTable lookupTable = (LookupTable) value;
    Image image = RessourceManager.getImage(Images.LOOKUP_TABLE_DEFAULT);
    if (lookupTable.getTableImage() != null) {
      image = ImageManager.getImage(lookupTable.getTableImage(), table);
    }

    ImageIcon scaledIcon = getScaledIcon(image);
    setIcon(scaledIcon);

    return this;
  }

  /**
   * For a given image return a scaled image icon proportionally scaled to size constraints.
   *
   * @param srcImage the image to scale
   * @return the scaled image icon
   */
  private ImageIcon getScaledIcon(Image srcImage) {
    int originalWidth = srcImage.getWidth(null);
    int originalHeight = srcImage.getHeight(null);

    // Safety check if image isn't loaded yet
    if (originalWidth <= 0 || originalHeight <= 0) {
      return null;
    }

    // adjust for padding
    int availableWidth = Math.max(1, maxWidth - IMAGE_PADDING * 2);
    int availableHeight = Math.max(1, maxHeight - IMAGE_PADDING * 2);

    // Determine the limiting scale factor
    double widthRatio = (double) availableWidth / originalWidth;
    double heightRatio = (double) availableHeight / originalHeight;
    double scale = Math.min(widthRatio, heightRatio);

    // Don't upscale if the image is already smaller than the cell
    if (scale > 1.0) {
      scale = 1.0;
    }

    int targetWidth = (int) (originalWidth * scale);
    int targetHeight = (int) (originalHeight * scale);

    // High-performance Graphics2D scaling instead of getScaledInstance()
    // Prevents table scrolling lag
    BufferedImage resized =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = resized.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
    g2.dispose();

    return new ImageIcon(resized);
  }
}
