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
package net.rptools.maptool.client.ui.zone;

import java.awt.*;
import java.awt.geom.*;
import java.util.Objects;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.grid.Grid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** These settings represent specific settings for the map and are just a stand-in */
class ClientZoneGridSettings extends GridRenderStyle {
  public ClientZoneGridSettings(ZoneRenderer renderer) {
    GUID zoneId = renderer.getZone().getId();
    /*try {
      clientGridSettings = AppState.getClientGridSettings();
      this = clientGridSettings.getZoneGridSettings(zoneId); }
    catch(Exception ex) { this = clientGridSettings.getZoneGridSettings("default"); }

    }*/
    secondColour = new Color(renderer.getZone().getGridColor());
  }
}

public final class GridRenderer {
  static GridRenderStyle renderStyle;
  private static final Logger log = LogManager.getLogger();
  static GridType gridType;
  static double gridSize;
  static double gridOffsetX;
  static double gridOffsetY;

  static float varyWeight(double weight, double scale) {
    return (float) Math.max(weight * scale, 0.25f);
  }

  public static void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds, Grid grid) {
    draw(renderer, g, bounds, grid, new ClientZoneGridSettings(renderer));
  }

  /**
   * TODO: add constants/preferences/menu items/class/etc. for settings TODO: create hierarchical
   * check for default style, GM preset style, client zone-specific style, client override style,
   * etc. etc.
   */
  public static void draw(
      ZoneRenderer renderer, Graphics2D g, Rectangle bounds, Grid grid, GridRenderStyle style) {

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    ClientZoneGridSettings settings = new ClientZoneGridSettings(renderer);
    // temp assign for dev
    style = settings;

    if (style.exposedOnly) {
      AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_IN);
      g.setComposite(ac);
    }
    gridType = grid.gridType;

    boolean softEdge = settings.softEdge; // fade edges
    GridDrawLineStyle lineStyle = settings.lineStyle; // DASHED/DOTTED/INTERSECTION/etc.
    GridDrawBlendComposite gridBlend = settings.blendComposite; // for Multiply/Divide/etc.
    boolean gridExposed = settings.exposedOnly;
    double gridWeight = settings.lineWeight;
    boolean twoColour = settings.twoColour;
    float opacity = settings.opacity;

    double scale = renderer.getScale();
    gridSize = renderer.getScaledGridSize();
    // set the grid colour opacity
    Color gridColour = settings.firstColour;
    float[] colourFields = new float[4];
    colourFields[3] = opacity;
    gridColour.getColorComponents(colourFields);

    float sideLength = (float) (gridSize * gridType.edgeMultiplier());
    float[][] dashArray = getDashArrays(sideLength, lineStyle);

    double offX = renderer.getViewOffsetX() % gridSize + gridOffsetX * scale;
    double offY = renderer.getViewOffsetY() % gridSize + gridOffsetY * scale;

    double startCol = (bounds.x / gridSize) * gridSize;
    double startRow = (bounds.y / gridSize) * gridSize;
    Grid.GridCell cell = grid.getGridCell();

    Shape shp;
    shp =
        settings.lineOffset > 0
            ? cell.getScaledCellShape(scale)
            : cell.getScaledCellHalfShape(scale);
    int cap = BasicStroke.CAP_BUTT;
    int join = BasicStroke.JOIN_MITER;
    float miterLimit = 10f;
    float dashOffset = 0;
    int loopCounter = 0;
    double stepSize = (softEdge ? 1 : gridWeight);
    for (double thisWeight = gridWeight; thisWeight > 0; thisWeight -= stepSize) {
      g.setColor(
          new Color(
              colourFields[0],
              colourFields[1],
              colourFields[2],
              colourFields[3] / (float) gridWeight));
      float strokeWidth = varyWeight(thisWeight, scale);
      BasicStroke stroke =
          new BasicStroke(strokeWidth, cap, join, miterLimit, dashArray[loopCounter], dashOffset);
      g.setStroke(stroke);

      if (Objects.equals(gridType, "SQUARE")) {
        AffineTransform oldAT = g.getTransform();
        // draw the grid lines
        for (double col = startCol;
            col < bounds.x + bounds.width + sideLength;
            col += cell.colSpacing) {
          AffineTransform currentAT = oldAT;
          currentAT.translate(col, 0);
          for (double row = startRow;
              row < bounds.y + bounds.height + sideLength;
              row += cell.rowSpacing) {
            currentAT.translate(0, sideLength);
            g.setTransform(currentAT);
            g.draw(shp);
          }
        }
      }
      if (gridType == GridType.HEX_H || gridType == GridType.HEX_V) {

        AffineTransform oldAt = g.getTransform();

        for (double col = startCol - offX * 2f;
            col < bounds.x + bounds.width + cell.dimensions.getWidth();
            col += cell.colSpacing) {
          g.setTransform(oldAt);
          g.translate(col, 0f);
          for (double row = startRow - offY;
              row < bounds.y + bounds.height + sideLength;
              row += cell.rowSpacing * 2f) {
            //  hex.height/2f :
            if (gridType.horizontal()) {
              g.translate(0f, cell.dimensions.getHeight() * 2f);
            } else {
              g.translate(0f, Math.abs(col) % 2 == 0 ? 0f : cell.dimensions.getHeight() / 2f);
            }
            g.draw(shp);
          }
        }

        g.setTransform(oldAt);
      }
    }
  }

  private static float[][] getDashArrays(float sideLength, GridDrawLineStyle lineStyle) {
    // define the dash array
    float dashUnit = sideLength / 32;
    float[][] dashArrays;
    switch (lineStyle) {
      case SOLID -> dashArrays =
          new float[][] {{sideLength}, {sideLength}, {sideLength}, {sideLength}, {sideLength}};
      case DASHED -> dashArrays =
          new float[][] {
            {1.1f * dashUnit, 1.8f * dashUnit, 1.1f * dashUnit},
            {0.9f * dashUnit, 2.2f * dashUnit, 0.9f * dashUnit},
            {0.8f * dashUnit, 2.4f * dashUnit, 0.8f * dashUnit},
            {0.6f * dashUnit, 2.6f * dashUnit, 0.6f * dashUnit},
            {0.5f * dashUnit, 2.8f * dashUnit, 0.6f * dashUnit}
          };
      case DOTTED -> dashArrays =
          new float[][] {
            {dashUnit, dashUnit},
            {0.94f * dashUnit, 1.06f * dashUnit},
            {0.88f * dashUnit, 1.12f * dashUnit},
            {0.82f * dashUnit, 1.18f * dashUnit},
            {0.76f * dashUnit, 1.24f * dashUnit}
          };
      case INTERSECTION -> dashArrays =
          new float[][] {
            {2.0f * dashUnit, 28 * dashUnit, 2.0f * dashUnit, 0},
            {4.0f * dashUnit, 24 * dashUnit, 4.0f * dashUnit, 0},
            {5.5f * dashUnit, 21 * dashUnit, 5.5f * dashUnit, 0},
            {7 * dashUnit, 18 * dashUnit, 7 * dashUnit, 0},
            {9 * dashUnit, 14 * dashUnit, 9 * dashUnit, 0}
          };
      default -> throw new IllegalStateException("Unexpected value: " + lineStyle);
    }
    return dashArrays;
  }
}
