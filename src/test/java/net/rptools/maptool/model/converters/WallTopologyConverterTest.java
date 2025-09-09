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
package net.rptools.maptool.model.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.thoughtworks.xstream.XStream;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.rptools.maptool.model.topology.WallTopology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that {@link WallTopologyConverter} can be used correctly with {@link XStream} for
 * serializing and deserializing.
 */
public class WallTopologyConverterTest {
  private XStream xStream;

  @BeforeEach
  void setUp() {
    xStream = new XStream();
    XStream.setupDefaultSecurity(xStream);
    xStream.allowTypesByWildcard(new String[] {"net.rptools.**", "java.awt.**", "sun.awt.**"});
    xStream.registerConverter(new WallTopologyConverter(xStream));
  }

  @ParameterizedTest
  @MethodSource
  void testSerializeDeserialize(WallTopology topology) {
    var xml = xStream.toXML(topology);
    var result = xStream.fromXML(xml);
    assertInstanceOf(WallTopology.class, result);
    var newTopology = (WallTopology) result;

    var originalVertices = topology.getVertices().collect(Collectors.toSet());
    var originalWalls = topology.getWalls().collect(Collectors.toSet());
    var newVertices = newTopology.getVertices().collect(Collectors.toSet());
    var newWalls = newTopology.getWalls().collect(Collectors.toSet());

    assertEquals(originalVertices, newVertices);
    assertEquals(originalWalls, newWalls);
  }

  static List<Arguments> testSerializeDeserialize() {
    var result = new ArrayList<Arguments>();

    {
      var topology = new WallTopology();
      result.add(arguments(named("empty walls", topology)));
    }
    {
      var topology = new WallTopology();
      topology.brandNewWall();
      result.add(arguments(named("single default wall", topology)));
    }
    {
      var topology = new WallTopology();
      topology.string(
          new Point2D.Double(0, 0),
          builder -> {
            builder.push(new Point2D.Double(100, 0));
            builder.push(new Point2D.Double(100, 100));
            builder.push(new Point2D.Double(50, 0));
          });
      result.add(arguments(named("wall string", topology)));
    }

    return result;
  }

  @Test
  void testDeserializeEmptyVerticesAndWalls() {
    var xml =
        """
      <net.rptools.maptool.model.topology.WallTopology>
        <vertices />
        <walls />
      </net.rptools.maptool.model.topology.WallTopology>
    """;

    var result = xStream.fromXML(xml);
    assertInstanceOf(WallTopology.class, result);
    var newTopology = (WallTopology) result;

    assertEquals(Set.of(), newTopology.getVertices().collect(Collectors.toSet()));
    assertEquals(Set.of(), newTopology.getWalls().collect(Collectors.toSet()));
  }

  @Test
  void testDeserializeEmptyWalls() {
    // This is not something that is supposed to appear in the XML, but we might as well handle it.
    // There are no walls, meaning the vertices are all dangling and shouldn't be there either.
    // This also tests that the implicit collection for walls is safe to deserialize when empty.

    var xml =
        """
      <net.rptools.maptool.model.topology.WallTopology>
        <vertices>
          <vertex id="549C67BA47264D52B45AE9F56189697A" x="0.0" y="0.0"/>
          <vertex id="72E33F5BEBDE4151B94A1EFD3C9599A1" x="1000.0" y="0.0"/>
        </vertices>
        <walls />
      </net.rptools.maptool.model.topology.WallTopology>
    """;

    var result = xStream.fromXML(xml);
    assertInstanceOf(WallTopology.class, result);
    var newTopology = (WallTopology) result;

    // The vertices in the XML are dangling, so are automatically removed.
    assertEquals(Set.of(), newTopology.getVertices().collect(Collectors.toSet()));
    assertEquals(Set.of(), newTopology.getWalls().collect(Collectors.toSet()));
  }
}
