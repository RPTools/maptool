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
package net.rptools.maptool.model.sheet.stats;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.rptools.lib.AwtUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.events.TokenHoverEnter;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.HTMLUtil;
import net.rptools.maptool.util.ImageManager;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class that extracts and provides the information needed to render a stat sheet. */
@SuppressWarnings("unused")
public class StatSheetContext {
  private static final Logger log = LogManager.getLogger(StatSheetContext.class);

  /** Class that represents a token property on a stat sheet. */
  public static class Property {
    /** Name of the property. */
    private final String name;

    /** Display Name of the property. */
    private final String displayName;

    /** Value of the property. */
    private final Object value;

    /** True if the property is GM only. */
    private final boolean gmOnly;

    /** The short name of the property. */
    private final String shortName;

    /**
     * Creates a new instance of the class.
     *
     * @param name Name of the property.
     * @param displayName Display Name of the property.
     * @param value Value of the property.
     * @param gmOnly True if the property is GM only.
     * @implNote GM only properties are only extracted if the player is a GM.
     */
    Property(String name, String displayName, String shortName, Object value, boolean gmOnly) {
      this.name = name;
      this.displayName = Objects.requireNonNullElse(displayName, name);
      this.shortName = shortName;
      this.value = value;
      this.gmOnly = gmOnly;
    }

    /**
     * Returns the name of the property.
     *
     * @return The name of the property.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the display name of the property.
     *
     * @return The display name of the property.
     */
    public String getDisplayName() {
      return displayName;
    }

    /**
     * Returns the value of the property.
     *
     * @return The value of the property.
     */
    public Object getValue() {
      return value;
    }

    /**
     * Returns true if the property is GM only.
     *
     * @return True if the property is GM only.
     */
    public boolean getGmOnly() {
      return gmOnly;
    }

    /**
     * Returns the short name of the property.
     *
     * @return The short name of the property.
     */
    public String getShortName() {
      return shortName;
    }
  }

  /** The name of the token. */
  private final String name;

  /** The GM name of the token. */
  private final String gmName;

  /** The label of the token. */
  private final String label;

  /** The image asset of the token. */
  private final MD5Key imageAsset;

  /** The portrait asset of the token. */
  private final MD5Key portraitAsset;

  /** The handout asset of the token. */
  private final MD5Key handoutAsset;

  /** The width of the portrait on the stat sheet. */
  private final int portraitWidth;

  /** The height of the portrait stat sheet. */
  private final int portraitHeight;

  /** The location of the stat sheet. */
  private final String statSheetLocation;

  /** The properties of the token. */
  private final List<Property> properties = new ArrayList<>();

  /** The bars shown on the token. */
  private final List<Map<String, Object>> bars = new ArrayList<>();

  /** The states set on the token. */
  private final List<Map<String, Object>> states = new ArrayList<>();

  /** The notes of the token. */
  private final String notes;

  /** The token notes type. */
  private final String notesType;

  /** The GM notes of the token. */
  private final String gmNotes;

  /** The token's GM notes type. */
  private final String gmNotesType;

  /** The speech name of the token. */
  private final String speechName;

  /** The token type. */
  private final String tokenType;

  /** True if the player is a GM. */
  private final boolean gm;

  /** Hover event */
  private final TokenHoverEnter event;

  /**
   * Creates a new instance of the class.
   *
   * @param hoverEvent the token hover event to build the stat-sheet for.
   * @param player The player to extract the information for.
   * @param location The location of the stat sheet.
   */
  public StatSheetContext(TokenHoverEnter hoverEvent, Player player, StatSheetLocation location) {
    this.event = hoverEvent;
    var token = event.token();

    final boolean playerOwns = AppUtil.playerOwns(token);
    final boolean playerIsGm = player.isGM();

    name = token.getName();
    tokenType = token.getType().name();

    /* Combined list of Bar and State names */
    final List<String> OVERLAY_NAMES =
        Stream.concat(
                MapTool.getCampaign().getTokenBarsMap().keySet().stream(),
                MapTool.getCampaign().getTokenStatesMap().keySet().stream())
            .toList();

    for (String stateName : OVERLAY_NAMES) {
      Object stateValue = token.getState(stateName);
      if (stateValue != null) {
        addBarOrState(stateName, stateValue, playerOwns, playerIsGm);
      }
    }

    if (playerIsGm) {
      gmName = token.getGMName();
      gmNotes = token.getGMNotes();
      gmNotesType = token.getNotesType();
      gm = true;
    } else {
      gmName = null;
      gmNotes = null;
      gmNotesType = null;
      gm = false;
    }
    notes = playerOwns ? token.getNotes() : null;
    notesType = playerOwns ? token.getNotesType() : null;
    speechName = token.getSpeechName();

    handoutAsset = token.getCharsheetImage();

    if (AppPreferences.showPortrait.get()) {
      imageAsset = token.getImageAssetId();
      portraitAsset = token.getPortraitImage();
    } else {
      imageAsset = null;
      portraitAsset = null;
    }
    label = token.getLabel();
    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    MapTool.getCampaign()
        .getCampaignProperties()
        .getTokenPropertyList(token.getPropertyType())
        .forEach(
            tp -> {
              if (tp.getStatSheetViewPermission().hasPermission(player, token)) {
                Object value = token.getEvaluatedProperty(resolver, tp.getName());
                //noinspection ConstantValue
                if (value == null || value instanceof String sValue && sValue.isBlank()) {
                  return;
                }
                properties.add(
                    new Property(
                        tp.getName(),
                        tp.getDisplayName(),
                        tp.getShortName(),
                        value,
                        tp.isGMOnly()));
              }
            });

    Dimension dim;
    if (token.getPortraitImage() != null) {
      dim = getImageDimensions.apply(token.getPortraitImage());
    } else {
      dim = getImageDimensions.apply(token.getImageAssetId());
    }
    AwtUtil.constrainTo(dim, AppPreferences.portraitSize.get());
    portraitWidth = dim.width;
    portraitHeight = dim.height;

    statSheetLocation =
        switch (location) {
          case TOP_LEFT -> "statSheet-topLeft";
          case TOP_RIGHT -> "statSheet-topRight";
          case BOTTOM_LEFT -> "statSheet-bottomLeft";
          case BOTTOM_RIGHT -> "statSheet-bottomRight";
          case TOP -> "statSheet-top";
          case BOTTOM -> "statSheet-bottom";
          case LEFT -> "statSheet-left";
          case RIGHT -> "statSheet-right";
        };
  }

  private static final Function<MD5Key, Dimension> getImageDimensions =
      md5Key -> {
        BufferedImage image = ImageManager.getImage(md5Key);
        return new Dimension(image.getWidth(), image.getHeight());
      };

  /** Comparator for sorting State Groups */
  private static final Comparator<Map<String, Object>> stateComparator =
      (o1, o2) -> {
        String s1 = o1.get("group").toString();
        String s2 = o2.get("group").toString();
        // for different groups use natural order by group value
        int result = Collator.getInstance().compare(s1, s2);
        if (result != 0) {
          return result;
        }
        // for the same group, use the "order" value - should always be present
        if (Objects.equals(s1, s2)
            && o1.get("order") instanceof Integer i1
            && o2.get("order") instanceof Integer i2) {
          return i1.compareTo(i2);
        }
        return 0; // should never reach this point
      };

  /**
   * Method for filtering overlays and adding them to the appropriate data set
   *
   * @param overlayName Name of bar or state
   * @param overlayValue Value attached to bar or state
   * @param playerOwns Used for filtering what to display
   * @param playerIsGm Used for filtering what to display
   */
  private void addBarOrState(
      String overlayName, Object overlayValue, boolean playerOwns, boolean playerIsGm) {

    AbstractTokenOverlay ato;
    if (MapTool.getCampaign().getTokenBarsMap().containsKey(overlayName)) {
      ato = MapTool.getCampaign().getTokenBarsMap().get(overlayName);
    } else {
      ato = MapTool.getCampaign().getTokenStatesMap().get(overlayName);
    }
    if (ato == null) {
      return;
    }
    if ((ato.isShowOthers() && !playerOwns)
        || (playerOwns && ato.isShowOwner())
        || (playerIsGm && ato.isShowGM())) {
      Map<String, Object> featureMap = new HashMap<>();
      featureMap.put(
          "type",
          ato.getClass()
              .getSimpleName()
              .replaceAll("BarTokenOverlay", "")
              .replaceAll("TokenOverlay", ""));
      try {
        PropertyUtilsBean pub = BeanUtilsBean.getInstance().getPropertyUtils();
        featureMap.putAll(pub.describe(ato));
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
        log.error(e);
        return;
      }
      String mName;
      Map<String, Object> aspectRatioMap = new HashMap<>();
      for (Map.Entry<String, Object> entry : featureMap.entrySet()) {
        Object value = entry.getValue();
        if (value instanceof Color color) {
          featureMap.put(
              entry.getKey(),
              String.format(
                  "rgba(%d,%d,%d,%#.3f)",
                  color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255f));
        } else if (value instanceof MD5Key id) {
          featureMap.put(entry.getKey(), String.format("asset://%s", id));
          Dimension dim = getImageDimensions.apply(id);
          aspectRatioMap.put(entry.getKey() + "AspectRatio", dim.getWidth() / dim.getHeight());
        } else if (value instanceof MD5Key[] idArray) {
          String[] strOut = new String[idArray.length];
          double[] arOut = new double[idArray.length];
          for (int i = 0; i < idArray.length; i++) {
            strOut[i] = String.format("asset://%s", idArray[i].toString());
            Dimension dim = getImageDimensions.apply(idArray[i]);
            arOut[i] = dim.getWidth() / dim.getHeight();
          }
          aspectRatioMap.put(entry.getKey() + "AspectRatio", arOut);
          featureMap.put(entry.getKey(), strOut);
        }
      }
      featureMap.putAll(aspectRatioMap);
      if (ato instanceof BarTokenOverlay) {
        featureMap.put(
            "value", overlayValue instanceof BigDecimal bd ? bd.doubleValue() : overlayValue);
        featureMap.remove("group"); // does not apply to bars
        featureMap.remove("order"); // does not apply to bars
        bars.add(featureMap);
      } else {
        states.add(featureMap);
      }
    }
  }

  /**
   * Returns the name of the token.
   *
   * @return The name of the token.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the GM name of the token.
   *
   * @return The GM name of the token.
   */
  public String getGmName() {
    return gmName;
  }

  /**
   * Returns the image asset of the token.
   *
   * @return The image asset of the token.
   */
  public String getImage() {
    return imageAsset != null ? "asset://" + imageAsset : null;
  }

  /**
   * Returns the portrait asset of the token.
   *
   * @return The portrait asset of the token.
   */
  public String getPortrait() {
    return portraitAsset != null ? "asset://" + portraitAsset : null;
  }

  /**
   * Returns the handout asset of the token.
   *
   * @return The portrait asset of the token.
   */
  public String getHandout() {
    return handoutAsset != null ? "asset://" + handoutAsset : null;
  }

  /**
   * Returns the label of the token.
   *
   * @return The label of the token.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the width of the portrait on the stat sheet.
   *
   * @return The width of the portrait on the stat sheet.
   */
  public int getPortraitWidth() {
    return portraitWidth;
  }

  /**
   * Returns the height of the portrait on the stat sheet.
   *
   * @return The height of the portrait on the stat sheet.
   */
  public int getPortraitHeight() {
    return portraitHeight;
  }

  /**
   * Returns the properties of the token.
   *
   * @return The properties of the token.
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Returns the CSS class for the location of the stat sheet.
   *
   * @return The CSS class for the location of the stat sheet.
   */
  public String getStatSheetLocation() {
    return statSheetLocation;
  }

  /**
   * Returns the notes of the token.
   *
   * @return The notes of the token.
   */
  public String getNotes() {
    if (notes != null) {
      return HTMLUtil.htmlize(notes, notesType);
    } else {
      return null;
    }
  }

  /**
   * Returns the GM notes of the token.
   *
   * @return The GM notes of the token.
   */
  public String getGmNotes() {
    if (gmNotes != null) {
      return HTMLUtil.htmlize(gmNotes, gmNotesType);
    } else {
      return null;
    }
  }

  /**
   * Returns the speech name of the token.
   *
   * @return The speech name of the token.
   */
  public String getSpeechName() {
    return speechName;
  }

  /**
   * Returns the token type.
   *
   * @return The token type.
   */
  public String getTokenType() {
    return tokenType;
  }

  /**
   * Returns true if the player is a GM.
   *
   * @return True if the player is a GM.
   */
  public boolean isGm() {
    return gm;
  }

  /**
   * @return States set on the token.
   */
  public List<Map<String, Object>> getStates() {
    states.sort(stateComparator);
    return states;
  }

  /**
   * @return Bars available on the token.
   */
  public List<Map<String, Object>> getBars() {
    return bars;
  }
}
