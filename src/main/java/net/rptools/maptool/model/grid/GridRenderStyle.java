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
package net.rptools.maptool.model.grid;

import static net.rptools.maptool.client.MapToolUtil.constrainToRange;

import java.awt.*;
import java.awt.color.ColorSpace;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridRenderStyle {
  protected static final Logger log = LogManager.getLogger();

  GridRenderStyle() {
    update(lastScale, lastEdgeLength);
  }

  GridRenderStyle(float scale, float edgelength) {
    update(scale, edgelength);
  }

  private float lastScale = 1;
  private float lastEdgeLength = 100;
  public GridDrawLineStyle lineStyle = GridDrawLineStyle.INTERSECTION_MIDPOINT;
  public GridDrawBlendComposite blendComposite = GridDrawBlendComposite.NONE;
  public float opacity = 0.8f; // max opacity
  public double lineOffset = 0;
  public double lineWeight = AppState.getGridLineWeight();
  private final float[] fixedLineWidths = new float[] {0.3f, 0.5f, 0.78f, 1.0f, 1.2f};
  private float[] lineWidths = fixedLineWidths; // widths scaled to zoom setting
  public Color topColour = AppPreferences.getDefaultGridColor();
  public Color underColour =
      new ColourTools(topColour).getComplementaryColor(); // default to a contrasting colour
  private Color midColour =
      new ColourTools(topColour)
          .mixColors(underColour, 0.5f); // find a colour perceptually between both

  private Color[] colours = {underColour, underColour, midColour, topColour, topColour};
  private BasicStroke[] strokes;
  public boolean exposedOnly = true;
  public boolean overFog = false;
  public boolean softEdge = true;
  public boolean twoColour = true;

  public void update(float scale, float edgelength) {
    if (edgelength != lastEdgeLength || lineStyle.getSideLength() == -1) {
      lineStyle.setSideLength(edgelength);
      lastEdgeLength = edgelength;
    }
    if (lastScale != scale) {
      scaleLineWidths(scale);
    }
    if (edgelength != lastEdgeLength || lastScale != scale || strokes == null) {
      setBasicStrokes();
    }
  }

  public void kachunkachunk(Graphics2D g, Shape s) {
    for (int i = 0; i < 5; i++) {
      g.setColor(colours[i]);
      g.setStroke(strokes[i]);
      g.draw(s);
    }
  }

  public Color[] getColours() {
    return colours;
  }

  public void scaleLineWidths(float newScale) {
    // we don't want lines to disappear or get ridiculously big so limit the acceptable range
    float tmpScale = constrainToRange(newScale, 0.334f, 15.0f);
    log.debug("Last: " + lastScale + " New: " + newScale  + " Constrained: " + tmpScale);
    // only change things if it is different
    if (tmpScale != lastScale) {
      lastScale = tmpScale;
      // scale the line widths to match.
      for (int count = 0; count < 5; count++) {
        lineWidths[count] = (float) (lastScale * lineWeight * fixedLineWidths[count]);
      }
    }
    log.debug("Line widths: " + lineWidths.toString());
  }

  public BasicStroke[] setBasicStrokes() {
    this.strokes = new BasicStroke[5];
    for (int i = 0; i < 5; i++) {
      log.debug("setBasicStrokes - Line dash array: " + lineStyle.getLineDashArray(i).toString());
      log.debug("setBasicStrokes - Line width: " + lineWidths[i]);
      strokes[i] =
          new BasicStroke(
              lineWidths[i],
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER,
              10f,
              lineStyle.getLineDashArray(i),
              0f);
    }
    return strokes;
  }

  public BasicStroke[] getBasicStrokes() {
    return strokes;
  }
}

class ColourTools extends Color {
  public double getBrightness() {
    return brightness;
  }

  double brightness;
  private final ColorSpace cs;

  ColourTools(Color c) {
    super(c.hashCode());
    cs = c.getColorSpace();
    this.brightness = perceivedLightness(c);
  }

  ColourTools(float[] comp) {
    super(ColorSpace.getInstance(ColorSpace.CS_sRGB), comp, 0f);
    cs = this.getColorSpace();
    this.brightness = perceivedLightness(this);
  }

  public Color getComplementaryColor() {
    return getComplementaryColor(this);
  }

  public Color getComplementaryColor(Color colorToInvert) {
    float[] rgb = new float[3];
    float[] hsv = new float[3];
    colorToInvert.getColorComponents(rgb);
    Color.RGBtoHSB((int) rgb[0] * 255, (int) rgb[1] * 255, (int) (rgb[2] * 255), hsv);
    hsv[0] = (hsv[0] + 180) % 360;
    Color tmpColour = new Color(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
    return new Color(
        tmpColour.getRed(), tmpColour.getGreen(), tmpColour.getBlue(), colorToInvert.getAlpha());
  }

  private double sRGBtoLin(double colorChannel) {
    // https://stackoverflow.com/questions/596216/formula-to-determine-perceived-brightness-of-rgb-color
    if (colorChannel <= 0.04045) {
      return colorChannel / 12.92;
    } else {
      return Math.pow(((colorChannel + 0.055) / 1.055), 2.4);
    }
  }

  private double perceivedLightness(Color colour) {
    // https://stackoverflow.com/questions/596216/formula-to-determine-perceived-brightness-of-rgb-color
    float[] s = new float[4];
    colour.getComponents(cs, s);
    double vR = s[0] / 255;
    double vG = s[1] / 255;
    double vB = s[2] / 255;
    double Y = (0.2126 * sRGBtoLin(vR) + 0.7152 * sRGBtoLin(vG) + 0.0722 * sRGBtoLin(vB));
    if (Y <= (216 / 24389)) {
      return Y * (24389 / 27);
    } else {
      return Math.pow(Y, (1 / 3)) * 116 - 16;
    }
  }

  public Color mixColors(Color color2, double ratio) {
    return mixColors(this, color2, ratio);
  }

  public Color mixColors(Color color1, Color color2, double ratio) {
    Double b1 = new ColourTools(color1).getBrightness();
    Double b2 = new ColourTools(color2).getBrightness();
    double percent = ratio * b1 / (b1 + b2);
    double inverse_percent = (1 - ratio) * b2 / (b1 + b2);

    int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
    int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
    int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
    int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inverse_percent);
    return new Color(redPart, greenPart, bluePart, alphaPart);
  }
}
