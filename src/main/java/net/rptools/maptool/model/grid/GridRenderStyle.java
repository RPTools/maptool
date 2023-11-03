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

  GridRenderStyle(double scale, double edgelength) {
    update(scale, edgelength);
  }

  private double lastScale = 1;
  private double lastEdgeLength = 100;
  public GridLineStyle lineStyle = new GridLineStyle(GridLineStyleType.INTERSECTION_MIDPOINT);
  public GridDrawBlendComposite blendComposite = GridDrawBlendComposite.NONE;
  public float opacity = 0.8f; // max opacity
  public double lineOffset = 0;
  public double lineWeight = AppState.getGridLineWeight();
  private final float[] fixedLineWidths = new float[] {5f, 4f, 3f, 2f, 1f};
  private float[] lineWidths = fixedLineWidths; // widths scaled to zoom setting
  public Color colour2 = AppPreferences.getDefaultGridColor();
  public Color colour1 = new ColourTools(colour2).setTransparency(0.5f);

  public Color colour4 =
      new ColourTools(colour1).getComplementaryColor(); // default to a contrasting colour
  public Color colour5 = new ColourTools(colour4).setTransparency(0.5f);

  private final Color colour3 =
      new ColourTools(colour1).mixColors(colour4, 0.5f); // find a colour perceptually between both

  private final Color[] colours = {colour5, colour4, colour3, colour2, colour1};
  private BasicStroke[] strokes;
  public boolean exposedOnly = true;
  public boolean overFog = false;
  public boolean softEdge = true;
  public boolean twoColour = true;

  public void update(double scale, double edgelength) {
    // log.debug("Update GridRenderStyle - last scale: " + lastScale + ", last edge length: " +
    // lastEdgeLength + ", new scale: " + scale + ", new edge length: " + edgelength);
    if (edgelength != lastEdgeLength || lineStyle.getSideLength() == -1) {
      lineStyle.setSideLength(edgelength);
      lastEdgeLength = edgelength;
    }
    if (lastScale != scale) {
      // log.debug("Update GridRenderStyle -  scaleLineWidths(scale)");
      scaleLineWidths(scale);
    }
    if (edgelength != lastEdgeLength || lastScale != scale || strokes == null) {
      setBasicStrokes();
    }
  }

  public Color[] getColours() {
    return colours;
  }

  public void scaleLineWidths(double newScale) {
    // log.debug("scale line widths");
    // we don't want lines to disappear or get ridiculously big so limit the acceptable range
    double tmpScale = constrainToRange(newScale, 0.334, 5.0);
    // log.info("Last: " + lastScale + " New: " + newScale + " Constrained: " + tmpScale);
    // only change things if it is different
    if (tmpScale != lastScale) {
      lastScale = tmpScale;
      // scale the line widths to match.
      float[] newArr = new float[5];
      for (int count = 0; count < 5; count++) {
        float newVal = (float) (lastScale * lineWeight / 2 * fixedLineWidths[count]);
        newArr[count] = newVal;
        // log.info("Line widths: " + count + " " + newArr[count]);
      }
      this.lineWidths = newArr;
    }
  }

  public BasicStroke[] setBasicStrokes() {
    // log.debug("Create array of Basic Strokes");
    this.strokes = new BasicStroke[5];
    for (int i = 0; i < 5; i++) {
      // log.info("setBasicStrokes - Line width: " + lineWidths[i]);
      strokes[i] =
          new BasicStroke(
              lineWidths[i],
              BasicStroke.CAP_ROUND,
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
  private final ColorSpace cs = this.getColorSpace();

  ColourTools(Color c) {
    super(c.hashCode());
    this.brightness = perceivedLightness(c);
  }

  ColourTools(float[] comp) {
    super(ColorSpace.getInstance(ColorSpace.CS_sRGB), comp, 0f);
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

  public Color setTransparency(float v) {
    float[] comp = new float[3];
    this.getColorComponents(comp);
    return new Color(cs, comp, v);
  }
}
