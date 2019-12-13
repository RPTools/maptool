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
package net.rptools.maptool.model;

import java.awt.geom.Area;

/** Stores the exposed area of a token. */
public class ExposedAreaMetaData {
  /** Area exposed so far. */
  private Area exposedAreaHistory;

  public ExposedAreaMetaData() {
    exposedAreaHistory = new Area();
  }

  public ExposedAreaMetaData(Area area) {
    exposedAreaHistory = new Area(area);
  }

  public Area getExposedAreaHistory() {
    // if (exposedAreaHistory == null) {
    // exposedAreaHistory = new Area();
    // }
    return exposedAreaHistory;
  }

  public void addToExposedAreaHistory(Area newArea) {
    if (newArea != null && !newArea.isEmpty()) {
      exposedAreaHistory.add(newArea);
    }
  }

  public void removeExposedAreaHistory(Area newArea) {
    if (newArea != null && !newArea.isEmpty()) {
      exposedAreaHistory.subtract(newArea);
    }
  }

  public void clearExposedAreaHistory() {
    exposedAreaHistory = new Area();
  }
}
