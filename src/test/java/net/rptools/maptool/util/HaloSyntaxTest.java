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
import net.rptools.maptool.model.CategorizedHalos;
import net.rptools.maptool.model.Halo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HaloSyntaxTest {
  private HaloSyntax parser;

  @BeforeEach
  public void setUp() {
    parser = new HaloSyntax();
  }

  @Test
  public void testSingleCategory() {
    var originals = new CategorizedHalos();
    var input =
        """
                        Colored
                        ----
                        Red: #ff0000
                        """;
    var result = parser.parseCategorizedHalos(input, originals);
    assertFalse(result.isEmpty());

    var optionalCategory = result.getCategory("Colored");
    assertTrue(optionalCategory.isPresent());
    var category = optionalCategory.get();

    assertEquals("Colored", category.name());

    var halos = category.halos();
    assertEquals(1, halos.size());

    var halosArray = halos.toArray(Halo[]::new);
    assertEquals("Red", halosArray[0].getName());
  }

  @Test
  public void testMultipleCategories() {
    var originals = new CategorizedHalos();
    var input =
        """
                        Colored
                        ----
                        Red: #ff0000
                        Yellow: #ffff00
                        Green: #00ff00
                        Cyan: #00ffff
                        Blue: #0000ff
                        Magenta: #ff00ff

                        Test
                        ----
                        Circle 5 Red: circle 5#ff0000
                        Triangle 10 Red: triangle 10#ff0000
                        Square 15 Red: square 15#ff0000
                        """;

    var result = parser.parseCategorizedHalos(input, originals);
    assertFalse(result.isEmpty());

    // The parsing and collection should be order-preserving.

    var categories = Iterables.toArray(result.getCategories(), CategorizedHalos.Category.class);
    assertEquals(2, categories.length);
    assertEquals("Colored", categories[0].name());
    assertEquals("Test", categories[1].name());

    var coloredCategory = categories[0];
    var coloredHalos = coloredCategory.halos().toArray(Halo[]::new);
    assertEquals(6, coloredHalos.length);
    assertEquals("Red", coloredHalos[0].getName());
    assertEquals("Yellow", coloredHalos[1].getName());
    assertEquals("Green", coloredHalos[2].getName());
    assertEquals("Cyan", coloredHalos[3].getName());
    assertEquals("Blue", coloredHalos[4].getName());
    assertEquals("Magenta", coloredHalos[5].getName());

    var testCategory = categories[1];
    var testHalos = testCategory.halos().toArray(Halo[]::new);
    assertEquals("Circle 5 Red", testHalos[0].getName());
    assertEquals("Triangle 10 Red", testHalos[1].getName());
    assertEquals("Square 15 Red", testHalos[2].getName());
  }
}
