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
package net.rptools.maptool.client;

import java.awt.Color;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.utilities.RandomSuffixFactory;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.StringUtil;

public class MapToolUtil {
  private static final Random RAND = new SecureRandom();

  private static RandomSuffixFactory randomSuffixFactory = new RandomSuffixFactory();
  private static AtomicInteger nextTokenId = new AtomicInteger(1);

  /** The map of color names to color values */
  private static final Map<String, Color> COLOR_MAP = new TreeMap<String, Color>();

  private static final Map<String, Color> COLOR_MAP_HTML = new HashMap<String, Color>();

  /** Set up the color map */
  static {
    // Built-in Java colors that happen to match the values used by HTML...
    COLOR_MAP.put("black", Color.BLACK);
    COLOR_MAP.put("blue", Color.BLUE);
    COLOR_MAP.put("cyan", Color.CYAN);
    COLOR_MAP.put("gray", Color.GRAY);
    COLOR_MAP.put("magenta", Color.MAGENTA);
    COLOR_MAP.put("red", Color.RED);
    COLOR_MAP.put("white", Color.WHITE);
    COLOR_MAP.put("yellow", Color.YELLOW);

    // The built-in Java colors that DO NOT match the HTML colors...
    COLOR_MAP.put("darkgray", new Color(0xA9, 0xA9, 0xA9)); // Color.DARK_GRAY
    COLOR_MAP.put("green", new Color(0x00, 0x80, 0x00)); // Color.GREEN
    COLOR_MAP.put("lightgray", new Color(0xD3, 0xD3, 0xD3)); // Color.LIGHT_GRAY
    COLOR_MAP.put("orange", new Color(0xFF, 0xA5, 0x00)); // Color.ORANGE
    COLOR_MAP.put("pink", new Color(0xFF, 0xC0, 0xCB)); // Color.PINK

    // And the HTML colors that don't exist at all as built-in Java values...
    COLOR_MAP.put("aqua", new Color(0x00, 0xFF, 0xFF)); // same as Color.CYAN
    COLOR_MAP.put("fuchsia", new Color(0xFF, 0x00, 0xFF)); // same as Color.MAGENTA
    COLOR_MAP.put("lime", new Color(0xBF, 0xFF, 0x00));
    COLOR_MAP.put("maroon", new Color(0x80, 0x00, 0x00));
    COLOR_MAP.put("navy", new Color(0x00, 0x00, 0x80));
    COLOR_MAP.put("olive", new Color(0x80, 0x80, 0x00));
    COLOR_MAP.put("purple", new Color(0x80, 0x00, 0x80));
    COLOR_MAP.put("silver", new Color(0xC0, 0xC0, 0xC0));
    COLOR_MAP.put("teal", new Color(0x00, 0x80, 0x80));

    // Additional Gray colors
    COLOR_MAP.put("gray25", new Color(0x3F, 0x3F, 0x3F));
    COLOR_MAP.put("gray50", new Color(0x6F, 0x7F, 0x7F));
    COLOR_MAP.put("gray75", new Color(0xBF, 0xBF, 0xBF));

    /*
     * These are valid HTML colors. When getFontColor() is called, if one of these is selected then the name is returned. When another value is selected, the Color is converted to the '#112233f'
     * notation and returned instead -- even if it's a name in COLOR_MAP, above.
     */
    String[] html = {
      "black", "white", "fuchsia", "aqua", "silver", "red", "lime", "blue", "yellow", "gray",
      "purple", "maroon", "navy", "olive", "green", "teal"
    };
    for (String s : html) {
      Color c = COLOR_MAP.get(s);
      assert c != null : "HTML color not in predefined list?";
      COLOR_MAP_HTML.put(s, c);
    }
  }

  public static int getRandomNumber(int max) {
    return getRandomNumber(0, max);
  }

  public static int getRandomNumber(int min, int max) {
    return RAND.nextInt(max - min + 1) + min;
  }

  public static float getRandomRealNumber(float max) {
    return getRandomRealNumber(0, max);
  }

  public static float getRandomRealNumber(float min, float max) {
    return (max - min) * RAND.nextFloat() + min;
  }

  public static boolean percentageCheckAbove(int percentage) {
    return RAND.nextDouble() * 100 > percentage;
  }

  public static boolean percentageCheckBelow(int percentage) {
    return RAND.nextDouble() * 100 < percentage;
  }

  private static final Pattern NAME_PATTERN = Pattern.compile("^(.*)\\s+(\\d+)\\s*$");

  /**
   * Determine what the name of the new token should be. This method tries to choose a token name
   * which is (a) unique and (b) adheres to a numeric sequence.
   *
   * @param zone the map that the token is being placed onto
   * @param token the new token to be named
   * @param force if {@code false} a new name will not be generated unless the token naming
   *     prefrence in {@link AppPreferences} is {@link Token#NAME_USE_CREATURE}.
   * @return the new token's algorithmically generated name
   */
  public static String nextTokenId(Zone zone, Token token, boolean force) {
    boolean isToken = token.getLayer().isTokenLayer();
    String baseName = token.getName();
    String newName;
    Integer newNum = null;

    if (isToken && AppPreferences.newTokenNaming.get().equals(Token.NAME_USE_CREATURE)) {
      newName = I18N.getString("Token.name.creature");
    } else if (!force) {
      return baseName;
    } else if (baseName == null) {
      int nextId = nextTokenId.getAndIncrement();
      char ch = (char) ('a' + MapTool.getPlayerList().indexOf(MapTool.getPlayer()));
      return ch + Integer.toString(nextId);
    } else {
      baseName = baseName.trim();
      Matcher m = NAME_PATTERN.matcher(baseName);
      if (m.find()) {
        newName = m.group(1);
        try {
          newNum = Integer.parseInt(m.group(2));
        } catch (NumberFormatException nfe) {
          /*
           * This exception happens if the number is too big to fit inside an integer. In this case, we use the original name as the filename and assign a new number as the suffix.
           */
          newName = baseName;
        }
      } else {
        newName = baseName;
      }
    }
    boolean random =
        (isToken && AppPreferences.duplicateTokenNumber.get().equals(Token.NUM_RANDOM));

    var tokenNumberDisplay = AppPreferences.tokenNumberDisplay.get();
    boolean addNumToGM = !tokenNumberDisplay.equals(Token.NUM_ON_NAME);
    boolean addNumToName = !tokenNumberDisplay.equals(Token.NUM_ON_GM);

    /*
     * If the token already has a number suffix, if the preferences indicate that token numbering should be random and this token is on the Token layer, or if the token already exists somewhere on
     * this map, then we need to choose a new name.
     */
    if (newNum != null || random || zone.getTokenByName(newName) != null) {

      if (random) {
        do {
          newNum = randomSuffixFactory.nextSuffixForToken(newName);
        } while (nameIsDuplicate(zone, newName, newNum, addNumToName, addNumToGM));

      } else {
        newNum = zone.findFreeNumber(addNumToName ? newName : null, addNumToGM);
      }

      if (addNumToName) {
        newName += " ";
        newName += newNum;
      }

      // GM names just get a number
      if (addNumToGM) {
        token.setGMName(Integer.toString(newNum));
      }
    }
    return newName;
  }

  private static boolean nameIsDuplicate(
      Zone zone, String newName, Integer newNum, boolean playerName, boolean gmName) {
    boolean result = false;

    if (playerName) {
      result = zone.getTokenByName(newName + " " + newNum) != null;
    }
    if (gmName) {
      result = zone.getTokenByGMName(Integer.toString(newNum)) != null;
    }
    return result;
  }

  public static boolean isDebugEnabled() {
    return System.getProperty("MAPTOOL_DEV") != null;
  }

  public static boolean isValidColor(String name) {
    return COLOR_MAP.containsKey(name);
  }

  public static boolean isHtmlColor(String name) {
    return COLOR_MAP_HTML.containsKey(name);
  }

  /**
   * Returns a {@link Color} object if the parameter can be evaluated as a color. This includes a
   * text search against a list of known colors (case-insensitive; see {@link #COLOR_MAP}) and
   * conversion of the string into a color using {@link Color#decode(String)}. Invalid strings cause
   * <code>COLOR_MAP.get("black")</code> to be returned. Calls {@link #convertStringToColor(String)}
   * if the parameter is not a recognized color name.
   *
   * @param name a recognized color name or an integer color value in octal or hexadecimal form
   *     (such as <code>#123</code>, <code>0x112233</code>, or <code>0X111222333</code>)
   * @return the corresponding Color object or {@link Color#BLACK} if not in a recognized format
   */
  public static Color getColor(String name) {
    name = name.trim().toLowerCase();
    Color c = COLOR_MAP.get(name);
    if (c != null) return c;
    c = convertStringToColor(name);
    return c;
  }

  /**
   * Converts the incoming string value to a Color object and stores <code>val</code> and the Color
   * as a key/value pair in a cache. The incoming string may start with a <code>#</code> to indicate
   * a numeric color value in CSS format. Any errors cause {@link #COLOR_MAP} <code>.get("black")
   * </code> to be returned.
   *
   * @param val color value to interpret
   * @return Color object
   */
  private static Color convertStringToColor(String val) {
    Color c;
    if (StringUtil.isEmpty(val)) {
      c = COLOR_MAP.get("black");
    } else {
      try {
        c = Color.decode(val);
        COLOR_MAP.put(val.toLowerCase(), c);
      } catch (NumberFormatException nfe) {
        c = COLOR_MAP.get("black");
      }
    }
    return c;
  }

  public static Set<String> getColorNames() {
    return COLOR_MAP.keySet();
  }

  public static void uploadTexture(DrawablePaint paint) {
    if (paint == null) {
      return;
    }
    if (paint instanceof DrawableTexturePaint) {
      Asset asset = ((DrawableTexturePaint) paint).getAsset();
      uploadAsset(asset);
    }
  }

  public static void uploadAsset(Asset asset) {
    if (asset == null) {
      return;
    }
    if (!AssetManager.hasAsset(asset.getMD5Key())) {
      AssetManager.putAsset(asset);
    }
    if (!MapTool.isHostingServer() && !MapTool.getCampaign().containsAsset(asset.getMD5Key())) {
      MapTool.serverCommand().putAsset(asset);
    }
  }
}
