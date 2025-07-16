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
package net.rptools.maptool.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterables;
import net.rptools.maptool.model.CategorizedLights;
import net.rptools.maptool.model.LightSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuraSyntaxTest {
  private AuraSyntax parser;

  @BeforeEach
  public void setUp() {
    parser = new AuraSyntax();
  }

  @Test
  public void testSingleCategory() {
    var originals = new CategorizedLights();
    var input =
        """
                D20
                ----
                Candle - 5: circle 5 10#000000
                """;
    var result = parser.parseCategorizedAuras(input, originals);
    assertFalse(result.isEmpty());

    var optionalCategory = result.getCategory("D20");
    assertTrue(optionalCategory.isPresent());
    var category = optionalCategory.get();

    assertEquals("D20", category.name());

    var lights = category.lights();
    assertEquals(1, lights.size());

    var lightsArray = lights.toArray(LightSource[]::new);
    assertEquals("Candle - 5", lightsArray[0].getName());
  }

  @Test
  public void testD20DefaultsMultipleCategories() {
    var originals = new CategorizedLights();
    var input =
        """
                D20
                ----
                Candle - 5: circle 5 10#000000
                Lamp - 15: circle 15 30#000000
                Torch - 20: circle 20 40#000000
                Everburning - 20: circle 20 40#000000
                Lantern, Hooded - 30: circle 30 60#000000
                Sunrod - 30: circle 30 60#000000

                Generic
                ----
                5: circle 5
                15: circle 15
                20: circle 20
                30: circle 30
                40: circle 40
                60: circle 60
                """;

    var result = parser.parseCategorizedAuras(input, originals);
    assertFalse(result.isEmpty());

    // The parsing and collection should be order-preserving.

    var categories = Iterables.toArray(result.getCategories(), CategorizedLights.Category.class);
    assertEquals(2, categories.length);
    assertEquals("D20", categories[0].name());
    assertEquals("Generic", categories[1].name());

    var d20 = categories[0];
    var d20Lights = d20.lights().toArray(LightSource[]::new);
    assertEquals(6, d20Lights.length);
    assertEquals("Candle - 5", d20Lights[0].getName());
    assertEquals("Lamp - 15", d20Lights[1].getName());
    assertEquals("Torch - 20", d20Lights[2].getName());
    assertEquals("Everburning - 20", d20Lights[3].getName());
    assertEquals("Lantern, Hooded - 30", d20Lights[4].getName());
    assertEquals("Sunrod - 30", d20Lights[5].getName());

    var generic = categories[1];
    var genericLights = generic.lights().toArray(LightSource[]::new);
    assertEquals("5", genericLights[0].getName());
    assertEquals("15", genericLights[1].getName());
    assertEquals("20", genericLights[2].getName());
    assertEquals("30", genericLights[3].getName());
    assertEquals("40", genericLights[4].getName());
    assertEquals("60", genericLights[5].getName());
  }
}
