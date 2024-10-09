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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;

/** Class that extracts and provides the information needed to render a stat sheet. */
public class StatSheetContext {

  /** Class that represents a token property on a stat sheet. */
  static class Property {
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
     * @note GM only properties are only extracted if the player is a GM.
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

  /** The width of the portrait on the stat sheet. */
  private final int portraitWidth;

  /** The height of the portrait stat sheet. */
  private final int portraitHeight;

  /** The location of the stat sheet. */
  private final String statSheetLocation;

  /** The properties of the token. */
  private final List<Property> properties = new ArrayList<>();

  /** The notes of the token. */
  private final String notes;

  /** The notes type of the token. */
  private final String notesType;

  /** The GM notes of the token. */
  private final String gmNotes;

  /** The GM notes type of the token. */
  private final String gmNotesType;

  /** The speech name of the token. */
  private final String speechName;

  /** The type of the token. */
  private final String tokenType;

  /** True if the player is a GM. */
  private final boolean gm;

  /**
   * Creates a new instance of the class.
   *
   * @param token The token to extract the information from.
   * @param player The player to extract the information for.
   * @param location The location of the stat sheet.
   */
  public StatSheetContext(Token token, Player player, StatSheetLocation location) {

    name = token.getName();
    tokenType = token.getType().name();

    if (player.isGM()) {
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
    notes = AppUtil.playerOwns(token) ? token.getNotes() : null;
    notesType = AppUtil.playerOwns(token) ? token.getNotesType() : null;
    speechName = token.getSpeechName();

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
              if (tp.isShowOnStatSheet()) {
                if (tp.isGMOnly() && !MapTool.getPlayer().isGM()) {
                  return;
                }

                if (tp.isOwnerOnly() && !AppUtil.playerOwns(token)) {
                  return;
                }

                Object value = token.getEvaluatedProperty(resolver, tp.getName());
                if (value == null) {
                  return;
                }

                if (value instanceof String svalue) {
                  if (svalue.isBlank()) {
                    return;
                  }
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
      var image = ImageManager.getImage(token.getPortraitImage());
      dim = new Dimension(image.getWidth(), image.getHeight());
    } else {
      var image = ImageManager.getImage(token.getImageAssetId());
      dim = new Dimension(image.getWidth(), image.getHeight());
    }
    SwingUtil.constrainTo(dim, AppPreferences.portraitSize.get());
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
   * Returns the css class for the location of the stat sheet.
   *
   * @return The css class for the location of the stat sheet.
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
      return StringUtil.htmlize(notes, notesType);
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
      return StringUtil.htmlize(gmNotes, gmNotesType);
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
   * Returns the type of the token.
   *
   * @return The type of the token.
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
}
