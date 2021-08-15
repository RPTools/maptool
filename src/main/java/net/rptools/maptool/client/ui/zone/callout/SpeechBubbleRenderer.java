package net.rptools.maptool.client.ui.zone.callout;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

public class SpeechBubbleRenderer extends AbstractCalloutRenderer {


  private final static Map<CalloutPopupLocation, Offset> OFFSETS = Map.of(
      CalloutPopupLocation.TOP_LEFT, new Offset(0, 0),
      CalloutPopupLocation.TOP, new Offset(0, 20),
      CalloutPopupLocation.TOP_RIGHT, new Offset(0, 0),
      CalloutPopupLocation.LEFT, new Offset(20, 0),
      CalloutPopupLocation.CENTER, new Offset(0, 0),
      CalloutPopupLocation.RIGHT, new Offset(-20, 0),
      CalloutPopupLocation.BOTTOM_LEFT, new Offset(0, 0),
      CalloutPopupLocation.BOTTOM, new Offset(0, -20),
      CalloutPopupLocation.BOTTOM_RIGHT, new Offset(0, 0)
  );

  private static final CalloutPopupLocation DEFAULT_POPUP_LOCATION =
      CalloutPopupLocation.BOTTOM_LEFT;
  private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
  private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
  private static final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.5f);

  private static final int SHADOW_OFFSET_X = 1;
  private static final int SHADOW_OFFSET_Y = 3;

  private static final int MAX_OVAL_LINES = 5;
  private static final int DEFAULT_ARC_WIDTH = 75;
  private static final int DEFAULT_ARC_HEIGHT = 75;

  private final CalloutArguments calloutArguments;


  /**
   * Creates a new {@link SpeechBubbleRenderer}.
   * @param zoneRenderer the {@link ZoneRenderer} the callout is rendered for.
   * @param g2d the {@link Graphics2D} graphics context used to render the callout.
   * @param sp the {@link ScreenPoint} where the callout is rendered.
   * @param arguments the {@link CalloutArguments} for this callout.
   */
  public SpeechBubbleRenderer(ZoneRenderer zoneRenderer, Graphics2D g2d, ScreenPoint sp,
      CalloutArguments arguments) {
    super(
        zoneRenderer,
        g2d,
        sp,
        arguments.getPopupLocationOr(DEFAULT_POPUP_LOCATION),
        OFFSETS.get(arguments.getPopupLocationOr(DEFAULT_POPUP_LOCATION)).x(),
        OFFSETS.get(arguments.getPopupLocationOr(DEFAULT_POPUP_LOCATION)).y(),
         arguments.getText()
    );

    calloutArguments = arguments;
  }

  private void calculateTail(int[] tailX, int[] tailY, int[] tailXShadow,
      int[] tailYShadow, int minX,
      int minY,
      int width,
      int height) {
    int halfX = minX + width / 2;
    int halfY = minY + height / 2;
    int maxX = minX + width;
    int maxY = minY + height;


    switch (getCalloutPopupLocation()) {
      case TOP_LEFT -> {
        tailX[0] = minX;
        tailX[1] = minX + 20;
        tailX[2] = minX + 70;
        tailY[0] = minY;
        tailY[1] = halfY;
        tailY[2] = halfY;
      }
      case TOP -> {
        tailX[0] = halfX;
        tailX[1] = halfX + 20;
        tailX[2] = halfX + 70;
        tailY[0] = minY - 20;
        tailY[1] = halfY;
        tailY[2] = halfY;
      }
      case TOP_RIGHT -> {
        tailX[0] = maxX;
        tailX[1] = maxX - 20;
        tailX[2] = maxX - 70;
        tailY[0] = minY;
        tailY[1] = halfY;
        tailY[2] = halfY;
      }
      case LEFT -> {
        tailX[0] = minX - 20;
        tailX[1] = halfX;
        tailX[2] = halfX;
        tailY[0] = halfY;
        tailY[1] = Math.max(halfY - 25, minY);
        tailY[2] = halfY + 15;
      }
      case RIGHT -> {
        tailX[0] = maxX + 20;
        tailX[1] = halfX;
        tailX[2] = halfX;
        tailY[0] = halfY;
        tailY[1] = Math.max(halfY - 25, minY);
        tailY[2] = halfY + 15;
      }
      case BOTTOM_LEFT -> {
        tailX[0] = minX;
        tailX[1] = minX + 20;
        tailX[2] = minX +70;
        tailY[0] = maxY;
        tailY[1] = halfY;
        tailY[2] = halfY;
      }
      case BOTTOM -> {
        tailX[0] = halfX;
        tailX[1] = halfX + 20;
        tailX[2] = halfX + 70;
        tailY[0] = maxY + 20;
        tailY[1] = halfY;
        tailY[2] = halfY;
      }
      case BOTTOM_RIGHT -> {
        tailX[0] = maxX;
        tailX[1] = maxX - 20;
        tailX[2] = maxX - 70;
        tailY[0] = maxY;
        tailY[1] = halfY;
        tailY[2] = halfY;
      }
    }

    tailXShadow[0] = tailX[0] + SHADOW_OFFSET_X;
    tailXShadow[1] = tailX[1] + SHADOW_OFFSET_X;
    tailXShadow[2] = tailX[2] + SHADOW_OFFSET_X;

    tailYShadow[0] = tailY[0] + SHADOW_OFFSET_Y;
    tailYShadow[1] = tailY[1] + SHADOW_OFFSET_Y;
    tailYShadow[2] = tailY[2] + SHADOW_OFFSET_Y;
  }


  @Override
  public void render() {
    Graphics2D g = (Graphics2D) getGraphics().create();
    Rectangle2D contentBounds = getContentBounds();

    int minX = (int) contentBounds.getX();
    int minY = (int) contentBounds.getY();
    int width = (int) contentBounds.getWidth();
    int height = (int) contentBounds.getHeight();
    int numberLines = calloutArguments.getText().size();

    Color backgroundColor = calloutArguments.getBackgroundColorOr(DEFAULT_BACKGROUND_COLOR);
    Color textColor = calloutArguments.getTextColorOr(DEFAULT_TEXT_COLOR);

    // First draw shadow offset
    g.setPaint(SHADOW_COLOR);
    if (numberLines > MAX_OVAL_LINES) {
      g.fillRoundRect(minX + SHADOW_OFFSET_X, minY + SHADOW_OFFSET_Y, width, height,
          DEFAULT_ARC_WIDTH, DEFAULT_ARC_HEIGHT);
    } else {
      g.fillOval(minX + SHADOW_OFFSET_X, minY + SHADOW_OFFSET_Y, width, height);
    }

    CalloutPopupLocation calloutPopupLocation = getCalloutPopupLocation();

    if (getCalloutPopupLocation() != CalloutPopupLocation.CENTER) {
      int [] tailX = { 0, 0, 0 };
      int [] tailY = { 0, 0, 0 };
      int[] tailXShadow  = { 0, 0, 0 };
      int[] tailYShadow = { 0, 0, 0 };

      calculateTail(tailX, tailY, tailXShadow, tailYShadow, minX, minY, width, height);

      g.fillPolygon(tailXShadow, tailYShadow, 3);

      g.setPaint(backgroundColor);
      g.fillPolygon(tailX, tailY, 3);
    }

    g.setPaint(backgroundColor);
    if (numberLines > MAX_OVAL_LINES) {
      g.fillRoundRect(minX, minY, width, height, DEFAULT_ARC_WIDTH, DEFAULT_ARC_HEIGHT);
    } else {
      g.fillOval(minX, minY, width, height);
    }
    renderText(textColor);

  }

  private static record Offset(int x, int y) {}
}
