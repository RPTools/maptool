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
package net.rptools.maptool.client.ui.zone.callout;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

/** Abstract class containing reusable logic for call out rendering. */
public abstract class AbstractCalloutRenderer implements CalloutRenderer {

  /** The Graphics context to render the callout with. */
  private final Graphics2D g2d;
  /** The zone render that this callout will be rendered in. */
  private final ZoneRenderer zoneRenderer;
  /** The font metrics for the graphics context and zone renderer font. */
  private final FontMetrics fontMetrics;

  private final int widthPadding;
  private final List<String> lines;
  private final int textWidth;
  private final int contentWidth;
  private final int textHeight;
  private final int lineHeight;
  private final int heightPadding;
  private final int lineSpacing;
  private final int contentHeight;
  private final int leftMargin;
  private final int topMargin;
  private final CalloutPopupLocation calloutPopupLocation;
  private final int topLeftX;
  private final int topLeftY;

  /**
   * Creates a new {@code AbstractCalloutRenderer}.
   *
   * @param zrederer the {@link ZoneRenderer} that this call out is rendered in.
   * @param g the {@link Graphics2D} graphics context used to render the callout.
   * @param sp the {@link ScreenPoint} where the callout is rendered.
   * @param text The list of Strings to render.
   * @param popupLocation the {@link CalloutPopupLocation} where the callout is rendered in relation
   *     to the screen point.
   * @param leftMargin the x offset from the left margin where the content bounds start.
   * @param topMargin the y offset from the top margin where the content bounds start.
   */
  protected AbstractCalloutRenderer(
      ZoneRenderer zrederer,
      Graphics2D g,
      ScreenPoint sp,
      CalloutPopupLocation popupLocation,
      int leftMargin,
      int topMargin,
      List<String> text) {
    g2d = g;
    zoneRenderer = zrederer;
    fontMetrics = g2d.getFontMetrics(zoneRenderer.getFont());
    widthPadding = SwingUtilities.computeStringWidth(fontMetrics, "M") * 3;
    lines = List.copyOf(text);

    // Calculate text sizes
    FontMetrics metrics = g2d.getFontMetrics();
    int tWidth = 0;
    for (String line : lines) {
      int w = SwingUtilities.computeStringWidth(metrics, line);
      tWidth = Math.max(w, tWidth);
    }
    textWidth = tWidth;
    textHeight = fontMetrics.getHeight();
    lineSpacing = Math.min(10, textHeight / 4);
    lineHeight = textHeight + lineSpacing;

    // Calculate content padding

    contentWidth = textWidth + 2 * widthPadding;
    heightPadding = textHeight * 2;
    contentHeight = textHeight * text.size() + 2 * heightPadding + lineSpacing * (text.size() - 1);
    this.leftMargin = leftMargin;
    this.topMargin = topMargin;

    this.calloutPopupLocation = popupLocation;

    topLeftX =
        (int)
            (sp.getX()
                - (contentWidth * this.calloutPopupLocation.getWidthMultiplier())
                + this.leftMargin);
    topLeftY =
        (int)
            (sp.getY()
                - (contentHeight * this.calloutPopupLocation.getHeightMultiplier())
                + this.topMargin);
  }

  /**
   * Returns the {@link Graphics2D} graphics context used to render the callout.
   *
   * @return the graphics context.
   */
  protected Graphics2D getGraphics() {
    return g2d;
  }

  /**
   * Returns the {@link ZoneRenderer} that the callout is rendered for.
   *
   * @return the zone renderer.
   */
  protected ZoneRenderer getZoneRenderer() {
    return zoneRenderer;
  }

  /**
   * Returns the maximum text width for the strings that make up the text.
   *
   * @return the maximum width of the text.
   */
  protected int getTextWidth() {
    return textWidth;
  }

  protected List<String> getLines() {
    return lines;
  }

  protected int getWidthPadding() {
    return widthPadding;
  }

  protected int getTopLeftX() {
    return topLeftX;
  }

  protected int getTopLeftY() {
    return topLeftY;
  }

  protected int getContentWidth() {
    return contentWidth;
  }

  protected int getContentHeight() {
    return contentHeight;
  }

  protected Rectangle2D getContentBounds() {
    return new Rectangle2D.Double(
        getTopLeftX(), getTopLeftY(), getContentWidth(), getContentHeight());
  }

  protected CalloutPopupLocation getCalloutPopupLocation() {
    return calloutPopupLocation;
  }

  protected void renderText(Paint paint) {
    Graphics2D g = (Graphics2D) g2d.create();
    g.setPaint(paint);
    Rectangle2D contentBounds = getContentBounds();
    int y = (int) contentBounds.getY() + heightPadding + lineHeight / 2;
    int x = (int) contentBounds.getX() + widthPadding;
    for (String line : lines) {
      g.drawString(line, x, y);
      y += lineHeight;
    }
  }
}
