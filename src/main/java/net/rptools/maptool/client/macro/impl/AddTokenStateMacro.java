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
package net.rptools.maptool.client.macro.impl;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.token.ColorDotTokenOverlay;
import net.rptools.maptool.client.ui.token.CrossTokenOverlay;
import net.rptools.maptool.client.ui.token.DiamondTokenOverlay;
import net.rptools.maptool.client.ui.token.OTokenOverlay;
import net.rptools.maptool.client.ui.token.ShadedTokenOverlay;
import net.rptools.maptool.client.ui.token.TriangleTokenOverlay;
import net.rptools.maptool.client.ui.token.XTokenOverlay;
import net.rptools.maptool.client.ui.token.YieldTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;

/**
 * Create a new token state.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
@MacroDefinition(
  name = "addtokenstate",
  aliases = {"tsa"},
  description = "addtokenstate.description"
)
public class AddTokenStateMacro implements Macro {

  /** The element that contains the token state name */
  public static final int NAME = 0;

  /** The element that contains the overlay name */
  public static final int OVERLAY = 1;

  /** The element that contains the first parameter */
  public static final int PARAM_1 = 2;

  /** The element that contains the second parameter */
  public static final int PARAM_2 = 3;

  /** The map of color names to color values */
  public static final Map<String, Quadrant> CORNER_MAP = new HashMap<String, Quadrant>();

  /** Set up the corner maps */
  static {
    CORNER_MAP.put("nw", Quadrant.NORTH_WEST);
    CORNER_MAP.put("ne", Quadrant.NORTH_EAST);
    CORNER_MAP.put("sw", Quadrant.SOUTH_WEST);
    CORNER_MAP.put("se", Quadrant.SOUTH_EAST);
  }

  /** @see net.rptools.maptool.client.macro.Macro#execute execute(java.lang.String) */
  public void execute(MacroContext context, String aMacro, MapToolMacroContext executionContext) {
    // Split the command line into an array and get the tokens
    String[] tokens = aMacro.split("\\s");
    if (tokens.length < 2) {
      MapTool.addLocalMessage(I18N.getText("addtokenstate.param"));
      return;
    } // endif
    String name = tokens[NAME];
    String overlay = tokens[OVERLAY].toLowerCase();
    String param1 = tokens.length > 2 ? tokens[PARAM_1] : null;
    String param2 = tokens.length > 3 ? tokens[PARAM_2] : null;

    // Check for a duplicate name
    if (MapTool.getCampaign().getTokenStatesMap().get(name) != null) {
      MapTool.addLocalMessage(I18N.getText("addtokenstate.exists"));
      return;
    } // endif

    BooleanTokenOverlay tokenOverlay = null;
    try {
      // The second token is the overlay name, the rest of the tokens describe its properties
      if (overlay.equals("dot")) {
        tokenOverlay = createDotOverlay(name, param1, param2);
      } else if (overlay.equals("circle")) {
        tokenOverlay = createCircleOverlay(name, param1, param2);
      } else if (overlay.equals("shade")) {
        tokenOverlay = createShadedOverlay(name, param1);
      } else if (overlay.equals("x")) {
        tokenOverlay = createXOverlay(name, param1, param2);
      } else if (overlay.equals("cross")) {
        tokenOverlay = createCrossOverlay(name, param1, param2);
      } else if (overlay.equals("diamond")) {
        tokenOverlay = createDiamondOverlay(name, param1, param2);
      } else if (overlay.equals("yield")) {
        tokenOverlay = createYieldOverlay(name, param1, param2);
      } else if (overlay.equals("triangle")) {
        tokenOverlay = createTriangleOverlay(name, param1, param2);
      } else {
        MapTool.addLocalMessage(I18N.getText("addtokenstate.noOverlyType", overlay));
        return;
      } // endif
    } catch (IllegalArgumentException e) {
      MapTool.addLocalMessage(e.getMessage());
      return;
    }
    MapTool.getCampaign().getTokenStatesMap().put(tokenOverlay.getName(), tokenOverlay);
    MapTool.addLocalMessage(I18N.getText("addtokenstate.added", tokenOverlay.getName()));
  }

  /**
   * Create a shaded overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color parameter value
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createShadedOverlay(String name, String color) {
    Color shadeColor = findColor(color);
    return new ShadedTokenOverlay(name, shadeColor);
  }

  /**
   * Create a circle overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color parameter value
   * @param width The width parameter value
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createCircleOverlay(String name, String color, String width) {
    Color circleColor = findColor(color);
    int lineWidth = findInteger(width, 5);
    return new OTokenOverlay(name, circleColor, lineWidth);
  }

  /**
   * Create a circle overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color parameter value
   * @param width The width parameter value
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createXOverlay(String name, String color, String width) {
    Color circleColor = findColor(color);
    int lineWidth = findInteger(width, 5);
    return new XTokenOverlay(name, circleColor, lineWidth);
  }

  /**
   * Create a cross overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color parameter value
   * @param width The width parameter value
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createCrossOverlay(String name, String color, String width) {
    Color circleColor = findColor(color);
    int lineWidth = findInteger(width, 5);
    return new CrossTokenOverlay(name, circleColor, lineWidth);
  }

  /**
   * Create a diamond overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color of the diamond
   * @param width The width of the diamond
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createDiamondOverlay(String name, String color, String width) {
    Color circleColor = findColor(color);
    int lineWidth = findInteger(width, 5);
    return new DiamondTokenOverlay(name, circleColor, lineWidth);
  }

  /**
   * Create a yield overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color of the yield
   * @param width The width of the yield
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createYieldOverlay(String name, String color, String width) {
    Color circleColor = findColor(color);
    int lineWidth = findInteger(width, 5);
    return new YieldTokenOverlay(name, circleColor, lineWidth);
  }

  /**
   * Create a triangle overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color of the triangle
   * @param width The width of the triangle
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createTriangleOverlay(String name, String color, String width) {
    Color circleColor = findColor(color);
    int lineWidth = findInteger(width, 5);
    return new TriangleTokenOverlay(name, circleColor, lineWidth);
  }

  /**
   * Create a dot overlay.
   *
   * @param name Name of the new overlay.
   * @param color The color parameter value
   * @param corner The corner parameter value
   * @return The new token overlay.
   */
  private BooleanTokenOverlay createDotOverlay(String name, String color, String corner) {
    Color dotColor = findColor(color);
    Quadrant dotCorner = findCorner(corner);
    return new ColorDotTokenOverlay(name, dotColor, dotCorner);
  }

  /**
   * Find the color for the passed name.
   *
   * @param name The name or hex value of a color.
   * @return The color decoded from the name or the <code>Color.RED</code> if the passed name was
   *     <code>null</code>;
   */
  private Color findColor(String name) {
    if (name == null) return Color.RED;
    try {
      return Color.decode(name);
    } catch (NumberFormatException e) {
      if (!MapToolUtil.isValidColor(name.toLowerCase())) {
        throw new IllegalArgumentException(I18N.getText("addtokenstate.invalidColor", name));
      } // endif
      return MapToolUtil.getColor(name);
    } // endtry
  }

  /**
   * Find the color for the passed name.
   *
   * @param name The decimal value of the integer.
   * @param defaultValue The default value for the number.
   * @return The number parsed from the name or the <code>defaultValue</code> if the passed name was
   *     <code>null</code> ;
   */
  private int findInteger(String name, int defaultValue) {
    if (name == null) return defaultValue;
    try {
      return Integer.parseInt(name);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(I18N.getText("addtokenstate.invalidNumber", name));
    } // endtry
  }

  /**
   * Find a corner for the passed name
   *
   * @param name The name or abbreviation of a quadrant value
   * @return The quadrant representing the passed corner name of the <code>Quadrant.SOUTH_EAST
   *     </code> if the passed name was <code>null</code>.
   */
  private Quadrant findCorner(String name) {
    try {
      if (name == null) return Quadrant.SOUTH_EAST;
      return Quadrant.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      if (!CORNER_MAP.containsKey(name.toLowerCase())) {
        throw new IllegalArgumentException(I18N.getText("addtokenstate.invalidCorner", name));
      } // endif
      return CORNER_MAP.get(name);
    } // endtry
  }
}
