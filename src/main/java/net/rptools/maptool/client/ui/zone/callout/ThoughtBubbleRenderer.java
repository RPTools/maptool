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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;

/** Class that implements the renderer for a callout for a speech bubble. */
public class ThoughtBubbleRenderer extends AbstractCalloutRenderer {

  /** Offsets to from the standard anchor point to the actual top left of the callout. */
  private static final Map<CalloutPopupLocation, Offset> OFFSETS =
      Map.of(
          CalloutPopupLocation.TOP_LEFT, new Offset(0, 2),
          CalloutPopupLocation.TOP, new Offset(0, 10),
          CalloutPopupLocation.TOP_RIGHT, new Offset(0, 2),
          CalloutPopupLocation.LEFT, new Offset(18, -6),
          CalloutPopupLocation.CENTER, new Offset(0, 0),
          CalloutPopupLocation.RIGHT, new Offset(-18, -6),
          CalloutPopupLocation.BOTTOM_LEFT, new Offset(0, -2),
          CalloutPopupLocation.BOTTOM, new Offset(0, -14),
          CalloutPopupLocation.BOTTOM_RIGHT, new Offset(0, -2));

  /** The default anchor point for the callout when none is specified. */
  private static final CalloutPopupLocation DEFAULT_POPUP_LOCATION =
      CalloutPopupLocation.BOTTOM_LEFT;
  /** The default color used for rendering text if none is specified. */
  private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
  /** The default color used to rendering the background if none is specified. */
  private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
  /** The color used to render the shadows for the callout. */
  private static final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.5f);

  /** The x offset for the shadow. */
  private static final int SHADOW_OFFSET_X = 1;
  /** The y offset for the shadow. */
  private static final int SHADOW_OFFSET_Y = 3;

  /**
   * The maximum number of lines before the callout is rendered as a rounded rectangle instead of an
   * oval.
   */
  private static final int MAX_OVAL_LINES = 5;
  /** The arc width to use for the rounded rectangle. */
  private static final int DEFAULT_ARC_WIDTH = 75;
  /** The arc height to use for the rounded rectangle. */
  private static final int DEFAULT_ARC_HEIGHT = 75;

  /** The default color used to render the outline of the callout if nont is specified. */
  private static final Color DEFAULT_OUTLINE_COLOR = Color.BLACK;

  /** The arguments used to build the callout. */
  private final CalloutArguments calloutArguments;

  /**
   * Creates a new {@link ThoughtBubbleRenderer}.
   *
   * @param zoneRenderer the {@link ZoneRenderer} the callout is rendered for.
   * @param g2d the {@link Graphics2D} graphics context used to render the callout.
   * @param sp the {@link ScreenPoint} where the callout is rendered.
   * @param arguments the {@link CalloutArguments} for this callout.
   */
  public ThoughtBubbleRenderer(
      ZoneRenderer zoneRenderer, Graphics2D g2d, ScreenPoint sp, CalloutArguments arguments) {
    super(
        zoneRenderer,
        g2d,
        sp,
        arguments.getPopupLocationOr(DEFAULT_POPUP_LOCATION),
        OFFSETS.get(arguments.getPopupLocationOr(DEFAULT_POPUP_LOCATION)).x(),
        OFFSETS.get(arguments.getPopupLocationOr(DEFAULT_POPUP_LOCATION)).y(),
        arguments.getText());

    calloutArguments = arguments;
  }

  /**
   * Calculates the triangle for the callout tail.
   *
   * @param x populated with the X co-ordinates for the center of the bubbles.
   * @param y populated with the Y co-ordinates for the center of the bubbles.
   * @param shadowX populated with the X co-ordinates for the shadow of the center of the bubbles.
   * @param shadowY populated with the Y co-ordinates for the shadow of the center of the bubbles.
   * @param width populated with the widths of the bubbles.
   * @param height populated with the heights of the bubbles.
   * @param halfX the halfway x offset.
   * @param halfY the halfway y offset.
   * @param minX the minimum X co-ordinate of the content area of the callout.
   * @param minY the minimum Y co-ordinate of the content area of the callout.
   * @param width the width of the content area of the callout.
   * @param height the height of the content area of the callout.
   */
  private void calculateTail(
      int[] x,
      int[] y,
      int[] shadowX,
      int[] shadowY,
      int[] width,
      int[] height,
      int halfX,
      int halfY,
      int minX,
      int minY,
      int maxX,
      int maxY) {
    CalloutPopupLocation calloutPopupLocation = getCalloutPopupLocation();
    switch (calloutPopupLocation) {
      case TOP_LEFT:
        x[0] = minX + 4;
        y[0] = minY + 10;
        width[0] = 20;
        height[0] = 10;
        x[1] = minX + 3;
        y[1] = minY + 8;
        width[1] = 10;
        height[1] = 5;
        x[2] = minX;
        y[2] = minY + 6;
        width[2] = 6;
        height[2] = 3;
        break;
      case TOP:
        x[0] = halfX - 10;
        y[0] = minY - 8;
        width[0] = 20;
        height[0] = 10;
        x[1] = halfX - 5;
        y[1] = minY - 12;
        width[1] = 10;
        height[1] = 5;
        x[2] = halfX - 3;
        y[2] = minY - 14;
        width[2] = 6;
        height[2] = 3;
        break;
      case TOP_RIGHT:
        x[0] = maxX - 24;
        y[0] = minY + 10;
        width[0] = 20;
        height[0] = 10;
        x[1] = maxX - 13;
        y[1] = minY + 8;
        width[1] = 10;
        height[1] = 5;
        x[2] = maxX - 6;
        y[2] = minY + 6;
        width[2] = 6;
        height[2] = 3;
        break;
      case LEFT:
        x[0] = minX - 10;
        y[0] = halfY - 5;
        width[0] = 20;
        height[0] = 10;
        x[1] = minX - 16;
        y[1] = halfY;
        width[1] = 10;
        height[1] = 5;
        x[2] = minX - 18;
        y[2] = halfY + 2;
        width[2] = 6;
        height[2] = 3;
        break;
      case RIGHT:
        x[0] = maxX - 10;
        y[0] = halfY - 5;
        width[0] = 20;
        height[0] = 10;
        x[1] = maxX + 8;
        y[1] = halfY;
        width[1] = 10;
        height[1] = 5;
        x[2] = maxX + 14;
        y[2] = halfY + 2;
        width[2] = 6;
        height[2] = 3;
        break;
      case BOTTOM_LEFT:
        x[0] = minX + 4;
        y[0] = maxY - 14;
        width[0] = 20;
        height[0] = 10;
        x[1] = minX + 3;
        y[1] = maxY - 4;
        width[1] = 10;
        height[1] = 5;
        x[2] = minX;
        y[2] = maxY + 2;
        width[2] = 6;
        height[2] = 3;
        break;
      case BOTTOM:
        x[0] = halfX - 10;
        y[0] = maxY - 4;
        width[0] = 20;
        height[0] = 10;
        x[1] = halfX - 5;
        y[1] = maxY + 6;
        width[1] = 10;
        height[1] = 5;
        x[2] = halfX - 3;
        y[2] = maxY + 12;
        width[2] = 6;
        height[2] = 3;
        break;
      case BOTTOM_RIGHT:
        x[0] = maxX - 24;
        y[0] = maxY - 14;
        width[0] = 20;
        height[0] = 10;
        x[1] = maxX - 13;
        y[1] = maxY - 4;
        width[1] = 10;
        height[1] = 5;
        x[2] = maxX - 6;
        y[2] = maxY + 2;
        width[2] = 6;
        height[2] = 3;
        break;
    }

    for (int i = 0; i < x.length; i++) {
      shadowX[i] = x[i] + SHADOW_OFFSET_X;
      shadowY[i] = y[i] + SHADOW_OFFSET_Y;
    }
  }

  @Override
  public void render() {
    Graphics2D g = (Graphics2D) getGraphics().create();
    Rectangle2D contentBounds = getContentBounds();

    int minX = (int) contentBounds.getX();
    int minY = (int) contentBounds.getY();
    int width = (int) contentBounds.getWidth();
    int height = (int) contentBounds.getHeight();
    int maxX = minX + width;
    int maxY = minY + height;
    int halfX = minX + width / 2;
    int halfY = minY + height / 2;

    int numberLines = calloutArguments.getText().size();

    Color backgroundColor = calloutArguments.getBackgroundColorOr(DEFAULT_BACKGROUND_COLOR);
    Color textColor = calloutArguments.getTextColorOr(DEFAULT_TEXT_COLOR);
    Color outlineColor = calloutArguments.getOutlineColorOr(DEFAULT_OUTLINE_COLOR);

    CalloutPopupLocation calloutPopupLocation = getCalloutPopupLocation();

    int[] tailBubbleX = {0, 0, 0};
    int[] tailBubbleY = {0, 0, 0};
    int[] tailBubbleWidth = {0, 0, 0};
    int[] tailBubbleHeight = {0, 0, 0};
    int[] shadowTailBubbleX = {0, 0, 0};
    int[] shadowTailBubbleY = {0, 0, 0};

    calculateTail(
        tailBubbleX,
        tailBubbleY,
        shadowTailBubbleX,
        shadowTailBubbleY,
        tailBubbleWidth,
        tailBubbleHeight,
        halfX,
        halfY,
        minX,
        minY,
        maxX,
        maxY);

    // First draw shadow offset
    g.setPaint(SHADOW_COLOR);
    if (numberLines > MAX_OVAL_LINES) {
      g.fillRoundRect(
          minX + SHADOW_OFFSET_X,
          minY + SHADOW_OFFSET_Y,
          width,
          height,
          DEFAULT_ARC_WIDTH,
          DEFAULT_ARC_HEIGHT);
    } else {
      g.fillOval(minX + SHADOW_OFFSET_X, minY + SHADOW_OFFSET_Y, width, height);
    }

    if (calloutPopupLocation != CalloutPopupLocation.CENTER) {
      for (int i = 0; i < tailBubbleX.length; i++) {
        g.fillOval(
            shadowTailBubbleX[i], shadowTailBubbleY[i], tailBubbleWidth[i], tailBubbleHeight[i]);
      }
    }

    if (numberLines > MAX_OVAL_LINES) {
      g.setPaint(backgroundColor);
      g.fillRoundRect(minX, minY, width, height, DEFAULT_ARC_WIDTH, DEFAULT_ARC_HEIGHT);
      g.setPaint(outlineColor);
      g.drawRoundRect(minX, minY, width, height, DEFAULT_ARC_WIDTH, DEFAULT_ARC_HEIGHT);
    } else {
      g.setPaint(backgroundColor);
      g.fillOval(minX, minY, width, height);
      g.setPaint(outlineColor);
      g.drawOval(minX, minY, width, height);
    }

    if (calloutPopupLocation != CalloutPopupLocation.CENTER) {
      for (int i = 0; i < tailBubbleX.length; i++) {
        g.setColor(backgroundColor);
        g.fillOval(tailBubbleX[i], tailBubbleY[i], tailBubbleWidth[i], tailBubbleHeight[i]);
        g.setColor(outlineColor);
        g.drawOval(tailBubbleX[i], tailBubbleY[i], tailBubbleWidth[i], tailBubbleHeight[i]);
      }
    }

    renderText(textColor);
  }

  /** Record used to hold the offset from the top left corner. */
  private static record Offset(int x, int y) {}
}
