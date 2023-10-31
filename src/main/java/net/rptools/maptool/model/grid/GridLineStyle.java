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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridLineStyle {
  protected static final Logger log = LogManager.getLogger();
  GridLineStyleType lineStyleType = GridLineStyleType.SOLID;
  private static float dashFactor = 1;
  private static float edgeLength = -1;
  private float lastScale = 1;
  private float[][] scaledDashes = lineStyleType.dashArray;

  public GridLineStyle(GridLineStyleType styleType) {
    this.lineStyleType = styleType;
  }

  private void updateDashArrays() {
    log.debug("Scale dash array values");
    float[][] tmpArray = lineStyleType.dashArray;
    for (int i = 0; i < 5; i++) {
      int l = tmpArray[i].length;
      for (int ii = 0; ii < l; ii++) {
        tmpArray[i][ii] = dashFactor * lineStyleType.dashArray[i][ii];
      }
    }
    scaledDashes = tmpArray;
  }

  public void setSideLength(float sideLength_) {
    log.debug("Update Line Style -  lineStyle.setSideLength");
    if (edgeLength != sideLength_ || scaledDashes == null) {
      lastScale = sideLength_ / edgeLength;
      edgeLength = sideLength_;
      dashFactor = edgeLength / 32;
      scaledDashes = lineStyleType.dashArray;
      updateDashArrays();
    }
  }

  public float getSideLength() {
    return edgeLength;
  }

  public float[] getLineDashArray(int i) {
    return scaledDashes[i];
  }
}
