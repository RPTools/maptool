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

import com.twelvemonkeys.image.ResampleOp;
import java.awt.*;

import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.localisedObject.LocalEnumListItem;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AppPreferenceEnums {
  public enum ShowTokenNumbering implements LocalEnumListItem.EnumPreferenceItem<ShowTokenNumbering> {
    NAME("Preferences.combo.tokens.numbering.name"),
    GM("Preferences.combo.tokens.numbering.gm"),
    BOTH("Preferences.combo.tokens.numbering.both");

    private final String i18nKey;
    private final String displayName;

    ShowTokenNumbering(String key) {
      i18nKey = key;
      displayName = I18N.getString(key);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull ShowTokenNumbering getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.tokenNumberDisplay.set(newValue.name().toLowerCase());
    }
  }

  public enum UvttLosImportType implements LocalEnumListItem.EnumPreferenceItem<UvttLosImportType> {
    Walls("uvttLosImportType.walls"),
    Masks("uvttLosImportType.masks"),
    Prompt("uvttLosImportType.prompt");

    private final String displayName;
    private final String i18nKey;

    UvttLosImportType(String key) {
      i18nKey = key;
      displayName = I18N.getString(key);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull UvttLosImportType getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.uvttLosImportType.set(AppPreferences.UvttLosImportType.valueOf(newValue.name()));
    }
  }

  // Based off vision type enum in Zone.java, this could easily get tossed somewhere else if
  // preferred.
  public enum MapSortType implements LocalEnumListItem.EnumPreferenceItem<MapSortType> {
    DISPLAY_NAME(),
    GM_NAME();

    private final String i18nKey;
    private final String displayName;

    MapSortType() {
      i18nKey = "mapSortType." + name();
      displayName = I18N.getString(i18nKey);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull MapSortType getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.mapSortType.set(AppPreferences.MapSortType.valueOf(newValue.name()));
    }
  }

  public enum RenderQuality implements LocalEnumListItem.EnumPreferenceItem<RenderQuality> {
    LOW_SCALING("Preferences.combo.render.low"),
    PIXEL_ART_SCALING("Preferences.combo.render.pixel"),
    MEDIUM_SCALING("Preferences.combo.render.medium"),
    HIGH_SCALING("Preferences.combo.render.high");
    private final String i18nKey;
    private final String displayName;

    RenderQuality(String key) {
      i18nKey = key;
      displayName = I18N.getString(key);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull RenderQuality getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.renderQuality.set(net.rptools.lib.image.RenderQuality.valueOf(newValue.name()));
    }

    public void setRenderingHints(Graphics2D g) {
      switch (this) {
        case LOW_SCALING, PIXEL_ART_SCALING -> {
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        case MEDIUM_SCALING -> {
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        }
        case HIGH_SCALING -> {
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
      }
    }

    public void setShrinkRenderingHints(Graphics2D d) {
      switch (this) {
        case LOW_SCALING, PIXEL_ART_SCALING -> {
          d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
          d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        case MEDIUM_SCALING -> {
          d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        }
        case HIGH_SCALING -> {
          d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
      }
    }

    public int getResampleOpFilter() {
      return switch (this) {
        case LOW_SCALING, PIXEL_ART_SCALING -> ResampleOp.FILTER_POINT;
        case MEDIUM_SCALING -> ResampleOp.FILTER_TRIANGLE;
        case HIGH_SCALING -> ResampleOp.FILTER_QUADRATIC;
      };
    }
  }

  public enum NumberTokenDuplicateMethod implements LocalEnumListItem.EnumPreferenceItem<NumberTokenDuplicateMethod> {
    INCREMENT("Preferences.combo.tokens.duplicate.increment"),
    RANDOM("Preferences.combo.tokens.duplicate.random");

    private final String i18nKey;
    private final String displayName;

    NumberTokenDuplicateMethod(String key) {
      i18nKey = key;
      displayName = I18N.getString(key);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull NumberTokenDuplicateMethod getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.duplicateTokenNumber.set(StringUtils.capitalize(newValue.name()));
    }
  }

  public enum NewTokenNameUse implements LocalEnumListItem.EnumPreferenceItem<NewTokenNameUse> {
    FILE_NAME("Preferences.combo.tokens.naming.fileName", null),
    CREATURE("Preferences.combo.tokens.naming.creature", "Token.name.creature");

    private final String i18nKey;
    private final String displayName;

    NewTokenNameUse(String key, @Nullable String i18nArgKey) {
      i18nKey = key;
      if (i18nArgKey == null) {
        displayName = I18N.getText(key);
      } else {
        displayName = I18N.getText(i18nKey, I18N.getText(i18nArgKey));
      }
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull NewTokenNameUse getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.newTokenNaming.set(newValue.equals(FILE_NAME) ? "Use Filename" : "Creature");
    }
  }

  public enum UIIconType implements LocalEnumListItem.EnumPreferenceItem<UIIconType> {
    CLASSIC("Icon.type.classic"),
    ROD_TAKEHARA("Icon.type.RodTakehara");
    private final String i18nKey;
    private final String displayName;

    UIIconType(String key) {
      i18nKey = key;
      displayName = I18N.getString(i18nKey);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull UIIconType getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public String toString() {
      return displayName;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.iconTheme.set(newValue.equals(CLASSIC) ? "Classic" : "Rod Takehara");
    }
  }

  public enum WalkerMetric implements LocalEnumListItem.EnumPreferenceItem<WalkerMetric> {
    NO_DIAGONALS(),
    MANHATTAN(),
    ONE_TWO_ONE(),
    ONE_ONE_ONE();

    private final String displayName;
    private final String i18nKey;

    WalkerMetric() {
      i18nKey = "WalkerMetric." + name();
      displayName = I18N.getString(i18nKey);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull WalkerMetric getValue() {
      return valueOf(name());
    }

    /**
     * @return the lookup key for UI use
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.movementMetric.set(net.rptools.maptool.client.walker.WalkerMetric.valueOf(newValue.name()));
    }
  }

  public enum GridType implements LocalEnumListItem.EnumPreferenceItem<GridType> {
    HEX_HORI(GridFactory.HEX_HORI, "Preferences.combo.maps.grid.hexHori"),
    HEX_VERT(GridFactory.HEX_VERT, "Preferences.combo.maps.grid.hexVert"),
    NONE(GridFactory.NONE, "MapPropertiesDialog.image.noGrid"),
    SQUARE(GridFactory.SQUARE, "Preferences.combo.maps.grid.square"),
    ISOMETRIC(GridFactory.ISOMETRIC, "Preferences.combo.maps.grid.isometric");

    private final String i18nKey;
    private final String type;
    private final String displayName;

    GridType(String type, String key) {
      this.type = type;
      i18nKey = key;
      this.displayName = I18N.getText(i18nKey);
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.defaultGridType.set(valueOf(newValue.name()).type);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull GridType getValue() {
      return valueOf(name());
    }
    /**
     * @return the key to look up the localised display value
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }
  }

  /** The vision type (OFF, DAY, NIGHT). */
  public enum VisionType implements LocalEnumListItem.EnumPreferenceItem<VisionType> {
    OFF(),
    DAY(),
    NIGHT();

    private final String displayName;
    private final String i18nKey;

    VisionType() {
      i18nKey = "visionType." + name();
      displayName = I18N.getString(i18nKey);
    }
    
    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull VisionType getValue() {
      return valueOf(name());
    }
    
    /**
     * @return the key to look up the localised display value
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public @NotNull String getDisplayName() {
      return displayName;
    }

    @Override
    public void updatePreference(Enum<?> newValue) {
      AppPreferences.defaultVisionType.set(valueOf(VisionType.class, newValue.name()));
    }
  }
}
