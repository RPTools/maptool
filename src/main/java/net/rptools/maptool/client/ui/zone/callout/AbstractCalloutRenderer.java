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
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;

/** Abstract class containing reusable logic for call out rendering. */
public abstract class AbstractCalloutRenderer implements CalloutRenderer {

  /** The Graphics context to render the callout with. */
  private final Graphics2D g2d;
  /** The zone render that this callout will be rendered in. */
  private final ZoneRenderer zoneRenderer;
  /** Padding to use either side of the text that is rendered. */
  private final int widthPadding;
  /** The lines of text to be rendered. */
  private final List<String> lines;
  /** The width of the maximum text line to be rendered. */
  private final int textWidth;
  /** The width of the content area. */
  private final int contentWidth;
  /** The height of a single line. */
  private final int lineHeight;
  /** The padding size for the height of the content area. */
  private final int heightPadding;
  /** The height of the content area. */
  private final int contentHeight;
  /** The anchor location of the callout. */
  private final CalloutPopupLocation calloutPopupLocation;
  /** The X co-ordinate of the top left of the callout. */
  private final int topLeftX;
  /** The Y co-ordinate of the top left of the callout. */
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
    /** The font metrics for the graphics context and zone renderer font. */
    FontMetrics fontMetrics = g2d.getFontMetrics(zoneRenderer.getFont());
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
    int textHeight = fontMetrics.getHeight();
    int lineSpacing = Math.min(10, textHeight / 4);
    lineHeight = textHeight + lineSpacing;

    // Calculate content padding

    contentWidth = textWidth + 2 * widthPadding;
    heightPadding = textHeight * 2;
    contentHeight = textHeight * text.size() + 2 * heightPadding + lineSpacing * (text.size() - 1);

    this.calloutPopupLocation = popupLocation;

    topLeftX =
        (int)
            (sp.getX()
                - (contentWidth * this.calloutPopupLocation.getWidthMultiplier())
                + leftMargin);
    topLeftY =
        (int)
            (sp.getY()
                - (contentHeight * this.calloutPopupLocation.getHeightMultiplier())
                + topMargin);
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

  /**
   * Returns the lines to render in the callout.
   *
   * @return the lines to render.
   */
  protected List<String> getLines() {
    return lines;
  }

  /**
   * Returns the padding for sides of the callout content.
   *
   * @return the padding for the sides of the callout content.
   */
  protected int getWidthPadding() {
    return widthPadding;
  }

  /**
   * Returns the X co-ordinate of the top left corner of the callout.
   *
   * @return the X co-ordinate of the top left corner of the callout.
   */
  protected int getTopLeftX() {
    return topLeftX;
  }

  /**
   * Returns the Y co-ordinate of the top left corner of the callout.
   *
   * @return the Y co-ordinate of the top left corner of the callout.
   */
  protected int getTopLeftY() {
    return topLeftY;
  }

  /**
   * Returns the width of the content area for the callout.
   *
   * @return the width of the content are for the callout.
   */
  protected int getContentWidth() {
    return contentWidth;
  }

  /**
   * Returns the height of the content area for the callout.
   *
   * @return the height of the content are for the callout.
   */
  protected int getContentHeight() {
    return contentHeight;
  }

  /**
   * Returns the bounds of the content area for the callout.
   *
   * @return the bounds of the content area for the callout.
   */
  protected Rectangle2D getContentBounds() {
    return new Rectangle2D.Double(
        getTopLeftX(), getTopLeftY(), getContentWidth(), getContentHeight());
  }

  /**
   * Returns the anchor location for the callout.
   *
   * @return the anchor location for the callout.
   */
  protected CalloutPopupLocation getCalloutPopupLocation() {
    return calloutPopupLocation;
  }

  /**
   * Renders the text inside the content area,
   *
   * @param paint the {@link Paint} used to render the text.
   */
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
