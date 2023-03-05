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

import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import net.rptools.maptool.client.walker.astar.AStarCellPoint;

/** Converts pre-1.9.0 AStarCellPoint data to the CellPoints they should have been all along. */
public class AStarCellPointConverter implements Converter {
  @Override
  public boolean canConvert(Class type) {
    return AStarCellPoint.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    throw new UnsupportedOperationException();
  }

  /**
   * Read a CellPoint from pre-1.9.0 AStarCellPoint XML.
   *
   * <p>An AStarCellPoint that was serialized prior to 1.9.0 will have all the fields of a
   * CellPoint, since AStarCellPoint extended CellPoint back then. This implementation reads that
   * data but creates a CellPoint instances instead of an AStarCellPoint
   *
   * <p>However, if an AStarCellPoint object was serialized prior to 1.9.0, then was deserialized in
   * 1.9.0 and then serialized again, it would no longer have the CellPoint fields. There is no way
   * to recover the original CellPoint in this case, so we return null if this happens.
   *
   * @param reader The stream to read the text from.
   * @param context
   * @return A CellPoint that contains the position information of a pre-1.9.0 AStarCellPoint, or
   *     null if there is no position information.
   */
  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    CellPoint result = new CellPoint(0, 0);
    boolean hasCellPointData = false;

    final var properties =
        ImmutableMap.<String, Runnable>builder()
            .put("x", () -> result.x = (int) context.convertAnother(result, int.class))
            .put("y", () -> result.y = (int) context.convertAnother(result, int.class))
            .put(
                "distanceTraveled",
                () ->
                    result.distanceTraveled = (double) context.convertAnother(result, double.class))
            .put(
                "distanceTraveledWithoutTerrain",
                () ->
                    result.distanceTraveledWithoutTerrain =
                        (double) context.convertAnother(result, double.class))
            .put(
                "isAStarCanceled",
                () ->
                    result.setAStarCanceled(
                        (boolean) context.convertAnother(result, boolean.class)))
            .build();

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      try {
        final var name = reader.getNodeName();
        final var action = properties.get(name);
        if (action != null) {
          hasCellPointData = true;
          action.run();
        }
      } finally {
        reader.moveUp();
      }
    }

    return hasCellPointData ? result : null;
  }
}
