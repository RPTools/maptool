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

import com.google.protobuf.StringValue;
import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.token.ColorDotTokenOverlay;
import net.rptools.maptool.client.ui.token.DiamondTokenOverlay;
import net.rptools.maptool.client.ui.token.ImageTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.OTokenOverlay;
import net.rptools.maptool.client.ui.token.ShadedTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TriangleTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoToneBarTokenOverlay;
import net.rptools.maptool.client.ui.token.XTokenOverlay;
import net.rptools.maptool.client.ui.token.YieldTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.sheet.stats.StatSheetLocation;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import net.rptools.maptool.server.proto.CampaignPropertiesDto;
import net.rptools.maptool.server.proto.LightSourceListDto;
import net.rptools.maptool.server.proto.TokenPropertyListDto;

public class CampaignProperties implements Serializable {

  private static final String PROP_PREFIX = "Default.campaign.tokenProperty.name.";
  private static final String SHORT_PROP_PREFIX = "Default.campaign.tokenProperty.name.short.";

  /** The property type to fall back to for the default when none is defined. */
  private static final String FALLBACK_DEFAULT_TOKEN_PROPERTY_TYPE =
      I18N.getText("Default.campaign.tokenPropertyType");

  /** The default property type for tokens. */
  private String defaultTokenPropertyType = FALLBACK_DEFAULT_TOKEN_PROPERTY_TYPE;

  private Map<String, List<TokenProperty>> tokenTypeMap = new HashMap<>();

  /** Mapping between property types and default stat sheets for them. */
  private Map<String, StatSheetProperties> tokenTypeStatSheetMap = new HashMap<>();

  private List<String> remoteRepositoryList = new ArrayList<>();

  /**
   * @deprecated Only present for serialization. Instead use {@link #categorizedLights} (outside of
   *     {@link #readResolve()} and {@code #writeReplace()}).
   */
  @Deprecated private Map<String, Map<GUID, LightSource>> lightSourcesMap = new TreeMap<>();

  private transient @Nonnull CategorizedLights categorizedLights = new CategorizedLights();

  /**
   * @deprecated Only present for serialization. Instead use {@link #sights} (outside of {@link
   *     #readResolve()} and {@code #writeReplace()})
   */
  @Deprecated private Map<String, SightType> sightTypeMap = new LinkedHashMap<>();

  private transient @Nonnull Sights sights = new Sights();

  private Map<String, LookupTable> lookupTableMap = new HashMap<>();

  private String defaultSightType;

  private Map<String, BooleanTokenOverlay> tokenStates = new LinkedHashMap<>();
  private Map<String, BarTokenOverlay> tokenBars = new LinkedHashMap<>();
  private Map<String, String> characterSheets = new HashMap<>();

  /** Flag indicating that owners have special permissions */
  private boolean initiativeOwnerPermissions =
      AppPreferences.initiativePanelAllowsOwnerPermissions.get();

  /** Flag indicating that owners can only move tokens when they have initiative */
  private boolean initiativeMovementLock = AppPreferences.initiativeMovementLocked.get();

  /** Whether the default initiative sort order is reversed */
  private boolean initiativeUseReverseSort = false;

  /** Whether the Next/Previous buttons are disabled on the Initiative Panel */
  private boolean initiativePanelButtonsDisabled = false;

  /**
   * Returns the default property type for tokens.
   *
   * @return the default property type.
   */
  public String getDefaultTokenPropertyType() {
    return defaultTokenPropertyType;
  }

  /**
   * Sets the default property type for tokens.
   *
   * @param def the default property type.
   */
  public void setDefaultTokenPropertyType(String def) {
    defaultTokenPropertyType = def;
  }

  public CampaignProperties() {}

  public CampaignProperties(CampaignProperties properties) {
    for (Entry<String, List<TokenProperty>> entry : properties.tokenTypeMap.entrySet()) {
      List<TokenProperty> typeList = new ArrayList<>(properties.tokenTypeMap.get(entry.getKey()));

      tokenTypeMap.put(entry.getKey(), typeList);
    }
    tokenTypeStatSheetMap.putAll(properties.tokenTypeStatSheetMap);

    remoteRepositoryList.addAll(properties.remoteRepositoryList);

    lookupTableMap.putAll(properties.lookupTableMap);
    defaultSightType = properties.defaultSightType;
    sights = new Sights(properties.sights);
    categorizedLights = new CategorizedLights(properties.categorizedLights);

    for (BooleanTokenOverlay overlay : properties.tokenStates.values()) {
      overlay = (BooleanTokenOverlay) overlay.clone();
      tokenStates.put(overlay.getName(), overlay);
    } // endfor

    for (BarTokenOverlay overlay : properties.tokenBars.values()) {
      overlay = (BarTokenOverlay) overlay.clone();
      tokenBars.put(overlay.getName(), overlay);
    } // endfor

    initiativeOwnerPermissions = properties.initiativeOwnerPermissions;
    initiativeMovementLock = properties.initiativeMovementLock;
    initiativeUseReverseSort = properties.initiativeUseReverseSort;
    initiativePanelButtonsDisabled = properties.initiativePanelButtonsDisabled;

    for (String type : properties.characterSheets.keySet()) {
      characterSheets.put(type, properties.characterSheets.get(type));
    }
    defaultTokenPropertyType = properties.defaultTokenPropertyType;
  }

  public void mergeInto(CampaignProperties properties) {
    // This will replace any dups
    properties.tokenTypeMap.putAll(tokenTypeMap);
    properties.tokenTypeStatSheetMap.putAll(tokenTypeStatSheetMap);

    // Need to cull out dups
    for (String repo : properties.remoteRepositoryList) {
      if (!remoteRepositoryList.contains(repo)) {
        remoteRepositoryList.add(repo);
      }
    }

    properties.categorizedLights.addAll(categorizedLights);
    properties.lookupTableMap.putAll(lookupTableMap);
    properties.sights.addAll(sights);
    properties.tokenStates.putAll(tokenStates);
    properties.tokenBars.putAll(tokenBars);
    properties.defaultTokenPropertyType = defaultTokenPropertyType;
  }

  public Map<String, List<TokenProperty>> getTokenTypeMap() {
    return tokenTypeMap;
  }

  /**
   * Returns the default stat sheet details for a token property type.
   *
   * @param propertyType the token property type to get the details for.
   * @return the stat sheet details.
   */
  public StatSheetProperties getTokenTypeDefaultStatSheet(String propertyType) {
    return tokenTypeStatSheetMap.getOrDefault(
        propertyType,
        new StatSheetProperties(
            StatSheetManager.LEGACY_STATSHEET_ID, StatSheetLocation.BOTTOM_LEFT));
  }

  /**
   * Sets the default stat sheet details for a token property type.
   *
   * @param propertyType the token property type to set the details for.
   * @param statSheetProperties the stat sheet properties.
   */
  public void setTokenTypeDefaultStatSheet(
      String propertyType, StatSheetProperties statSheetProperties) {
    if (statSheetProperties == null) {
      tokenTypeStatSheetMap.remove(propertyType);
    } else {
      tokenTypeStatSheetMap.put(propertyType, statSheetProperties);
    }
  }

  public Sights getSightTypes() {
    return new Sights(sights);
  }

  public void setSightTypes(Sights newSights) {
    if (newSights != null) {
      sights = new Sights(newSights);
    }
  }

  public void setTokenTypeMap(Map<String, List<TokenProperty>> map) {
    tokenTypeMap.clear();
    tokenTypeMap.putAll(map);
  }

  public List<TokenProperty> getTokenPropertyList(String tokenType) {
    return getTokenTypeMap().get(tokenType);
  }

  public List<String> getRemoteRepositoryList() {
    return remoteRepositoryList;
  }

  public void setRemoteRepositoryList(List<String> list) {
    remoteRepositoryList.clear();
    remoteRepositoryList.addAll(list);
  }

  public CategorizedLights getLightSources() {
    return new CategorizedLights(categorizedLights);
  }

  public void setLightSources(CategorizedLights newLights) {
    categorizedLights = new CategorizedLights(newLights);
  }

  public Map<String, LookupTable> getLookupTableMap() {
    return lookupTableMap;
  }

  public void setLookupTableMap(Map<String, LookupTable> map) {
    lookupTableMap.clear();
    lookupTableMap.putAll(map);
  }

  public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
    return tokenStates;
  }

  public void setTokenStatesMap(Map<String, BooleanTokenOverlay> map) {
    tokenStates.clear();
    tokenStates.putAll(map);
  }

  public Map<String, BarTokenOverlay> getTokenBarsMap() {
    return tokenBars;
  }

  public void setTokenBarsMap(Map<String, BarTokenOverlay> map) {
    tokenBars.clear();
    tokenBars.putAll(map);
  }

  public void initDefaultProperties() {
    initLightSources();
    initTokenTypeMap();
    initSightTypeMap();
    initTokenStatesMap();
    initTokenBarsMap();
    initCharacterSheetsMap();
  }

  private void initLightSources() {
    if (!categorizedLights.isEmpty()) {
      return;
    }
    categorizedLights.addAllToCategory(
        I18N.getText("Default.campaign.lightSource.category.d20"),
        List.of(
            createD20LightSource(I18N.getText("Default.campaign.lightSource.light.candle"), 5),
            createD20LightSource(I18N.getText("Default.campaign.lightSource.light.lamp"), 15),
            createD20LightSource(I18N.getText("Default.campaign.lightSource.light.torch"), 20),
            createD20LightSource(
                I18N.getText("Default.campaign.lightSource.light.everburning"), 20),
            createD20LightSource(
                I18N.getText("Default.campaign.lightSource.light.lanternHooded"), 30),
            createD20LightSource(I18N.getText("Default.campaign.lightSource.light.sunrod"), 30)));
    categorizedLights.addAllToCategory(
        I18N.getText("Default.campaign.lightSource.category.generic"),
        List.of(
            createGenericLightSource(5),
            createGenericLightSource(15),
            createGenericLightSource(20),
            createGenericLightSource(30),
            createGenericLightSource(40),
            createGenericLightSource(60)));
  }

  private static LightSource createGenericLightSource(int radius) {
    return LightSource.createRegular(
        String.format("%d", radius),
        new GUID(),
        LightSource.Type.NORMAL,
        false,
        false,
        List.of(new Light(ShapeType.CIRCLE, 0, radius, 0, 360, null, 100, false, false)));
  }

  private static LightSource createD20LightSource(String name, int radius) {
    return LightSource.createRegular(
        String.format("%s - %d", name, radius),
        new GUID(),
        LightSource.Type.NORMAL,
        false,
        false,
        List.of(
            new Light(ShapeType.CIRCLE, 0, radius, 0, 360, null, 100, false, false),
            new Light(
                ShapeType.CIRCLE,
                0,
                radius * 2,
                0,
                360,
                new DrawableColorPaint(new Color(0, 0, 0, 100)),
                100,
                false,
                false)));
  }

  public String getDefaultSightType() {
    return defaultSightType;
  }

  private void initSightTypeMap() {
    sights.clear();

    final var types =
        List.of(
            new SightType("Normal", 0, 1.0, ShapeType.CIRCLE, 0, 0, 0, false, null),
            new SightType("Lowlight", 0, 2.0, ShapeType.CIRCLE, 0, 0, 0, false, null),
            new SightType("Grid Vision", 0, 1, ShapeType.GRID, 0, 0, 0, true, null),
            new SightType("Square Vision", 0, 1, ShapeType.SQUARE, 0, 0, 0, false, null),
            new SightType(
                "Normal Vision - Short Range", 10, 1.0, ShapeType.CIRCLE, 0, 0, 0, true, null),
            new SightType("Conic Vision", 0, 1.0, ShapeType.CONE, 0, 120, 0, false, null),
            new SightType(
                "Darkvision",
                0,
                1.0,
                ShapeType.CIRCLE,
                0,
                0,
                0,
                true,
                LightSource.createPersonal(
                    true,
                    false,
                    List.of(new Light(ShapeType.CIRCLE, 0, 60, 0, 0, null, 100, false, false)))));

    sights.addAll(types);
    defaultSightType = types.get(0).getName();
  }

  private void initTokenTypeMap() {
    if (!tokenTypeMap.isEmpty()) {
      return;
    }

    List<TokenProperty> list = new ArrayList<>();
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "strength"), I18N.getText(SHORT_PROP_PREFIX + "strength")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "dexterity"),
            I18N.getText(SHORT_PROP_PREFIX + "dexterity")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "constitution"),
            I18N.getText(SHORT_PROP_PREFIX + "constitution")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "intelligence"),
            I18N.getText(SHORT_PROP_PREFIX + "intelligence")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "wisdom"), I18N.getText(SHORT_PROP_PREFIX + "wisdom")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "charisma"), I18N.getText(SHORT_PROP_PREFIX + "charisma")));
    list.add(new TokenProperty(I18N.getText(PROP_PREFIX + "hp"), true, true, false));
    list.add(new TokenProperty(I18N.getText(PROP_PREFIX + "ac"), true, true, false));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "defense"), I18N.getText(SHORT_PROP_PREFIX + "defense")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "movement"), I18N.getText(SHORT_PROP_PREFIX + "movement")));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "elevation"),
            I18N.getText(SHORT_PROP_PREFIX + "elevation"),
            true,
            false,
            false));
    list.add(
        new TokenProperty(
            I18N.getText(PROP_PREFIX + "description"),
            I18N.getText(SHORT_PROP_PREFIX + "description")));

    tokenTypeMap.put(getDefaultTokenPropertyType(), list);
  }

  private void initTokenStatesMap() {
    tokenStates.clear();
    tokenStates.put("Dead", (new XTokenOverlay("Dead", Color.RED, 5)));
    tokenStates.put("Disabled", (new XTokenOverlay("Disabled", Color.GRAY, 5)));
    tokenStates.put("Hidden", (new ShadedTokenOverlay("Hidden", Color.BLACK)));
    tokenStates.put("Prone", (new OTokenOverlay("Prone", Color.BLUE, 5)));
    tokenStates.put("Incapacitated", (new OTokenOverlay("Incapacitated", Color.RED, 5)));
    tokenStates.put("Other", (new ColorDotTokenOverlay("Other", Color.RED, null)));
    tokenStates.put("Other2", (new DiamondTokenOverlay("Other2", Color.RED, 5)));
    tokenStates.put("Other3", (new YieldTokenOverlay("Other3", Color.YELLOW, 5)));
    tokenStates.put("Other4", (new TriangleTokenOverlay("Other4", Color.MAGENTA, 5)));
  }

  private void initTokenBarsMap() {
    tokenBars.clear();
    tokenBars.put(
        "Health", new TwoToneBarTokenOverlay("Health", new Color(0x20b420), Color.BLACK, 6));
  }

  private void initCharacterSheetsMap() {
    characterSheets.clear();
    characterSheets.put("Basic", "net/rptools/maptool/client/ui/forms/basicCharacterSheet.xml");
  }

  public Set<MD5Key> getAllImageAssets() {
    Set<MD5Key> set = new HashSet<>();

    // Start with the table images
    for (LookupTable table : getLookupTableMap().values()) {
      set.addAll(table.getAllAssetIds());
    }

    // States have images as well
    for (AbstractTokenOverlay overlay : getTokenStatesMap().values()) {
      if (overlay instanceof ImageTokenOverlay) set.add(((ImageTokenOverlay) overlay).getAssetId());
    }

    // Bars
    for (BarTokenOverlay overlay : getTokenBarsMap().values()) {
      if (overlay instanceof SingleImageBarTokenOverlay) {
        set.add(((SingleImageBarTokenOverlay) overlay).getAssetId());
      } else if (overlay instanceof TwoImageBarTokenOverlay) {
        set.add(((TwoImageBarTokenOverlay) overlay).getTopAssetId());
        set.add(((TwoImageBarTokenOverlay) overlay).getBottomAssetId());
      } else if (overlay instanceof MultipleImageBarTokenOverlay) {
        set.addAll(Arrays.asList(((MultipleImageBarTokenOverlay) overlay).getAssetIds()));
      }
    }
    return set;
  }

  /**
   * @return Getter for initiativeOwnerPermissions
   */
  public boolean isInitiativeOwnerPermissions() {
    return initiativeOwnerPermissions;
  }

  /**
   * @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions
   */
  public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
    this.initiativeOwnerPermissions = initiativeOwnerPermissions;
  }

  /**
   * @return Getter for initiativeMovementLock
   */
  public boolean isInitiativeMovementLock() {
    return initiativeMovementLock;
  }

  /**
   * @param initiativeMovementLock Setter for initiativeMovementLock
   */
  public void setInitiativeMovementLock(boolean initiativeMovementLock) {
    this.initiativeMovementLock = initiativeMovementLock;
  }

  public boolean isInitiativeUseReverseSort() {
    return initiativeUseReverseSort;
  }

  public void setInitiativeUseReverseSort(boolean initiativeUseReverseSort) {
    this.initiativeUseReverseSort = initiativeUseReverseSort;
  }

  public boolean isInitiativePanelButtonsDisabled() {
    return initiativePanelButtonsDisabled;
  }

  public void setInitiativePanelButtonsDisabled(boolean initiativePanelButtonsDisabled) {
    this.initiativePanelButtonsDisabled = initiativePanelButtonsDisabled;
  }

  /**
   * Getter for characterSheets. Only called by {@link Campaign#getCharacterSheets()} and that
   * function is never used elsewhere within MapTool. Yet. ;-)
   *
   * @return a Map of the characterSheets
   */
  public Map<String, String> getCharacterSheets() {
    return characterSheets;
  }

  /**
   * @param characterSheets Setter for characterSheets
   */
  public void setCharacterSheets(Map<String, String> characterSheets) {
    this.characterSheets.clear();
    this.characterSheets.putAll(characterSheets);
  }

  @Serial
  protected Object readResolve() {
    if (tokenTypeMap == null) {
      tokenTypeMap = new HashMap<>();
    }
    if (remoteRepositoryList == null) {
      remoteRepositoryList = new ArrayList<>();
    }

    categorizedLights = new CategorizedLights();
    if (lightSourcesMap != null) {
      // Import the serialized light sources.
      categorizedLights = CategorizedLights.copyOf(lightSourcesMap);
    } else {
      // We'll still need it when serializing.
      lightSourcesMap = new TreeMap<>();
    }

    sights = new Sights();
    if (sightTypeMap != null) {
      // Import the serialized light sources.
      sights = Sights.copyOf(sightTypeMap.values());
    } else {
      // We'll still need it when serializing.
      sightTypeMap = new LinkedHashMap<>();
    }

    if (lookupTableMap == null) {
      lookupTableMap = new HashMap<>();
    }
    if (tokenStates == null) {
      tokenStates = new LinkedHashMap<>();
    }
    if (tokenBars == null) {
      tokenBars = new LinkedHashMap<>();
    }
    if (characterSheets == null) {
      characterSheets = new HashMap<>();
    }

    if (tokenTypeStatSheetMap == null) {
      tokenTypeStatSheetMap = new HashMap<>();
    }

    if (defaultTokenPropertyType == null) {
      defaultTokenPropertyType = FALLBACK_DEFAULT_TOKEN_PROPERTY_TYPE;
    }
    return this;
  }

  @Serial
  protected Object writeReplace() {
    // We still use lightSourcesMap and sightTypeMap for storage. So make sure they are populated.

    lightSourcesMap.clear();
    for (var category : categorizedLights.getCategories()) {
      var map = new LinkedHashMap<GUID, LightSource>();
      for (var lightSource : category.lights()) {
        map.put(lightSource.getId(), lightSource);
      }
      lightSourcesMap.put(category.name(), map);
    }

    sightTypeMap.clear();
    for (var sightType : sights) {
      sightTypeMap.put(sightType.getName(), sightType);
    }

    return this;
  }

  public static CampaignProperties fromDto(CampaignPropertiesDto dto) {
    var props = new CampaignProperties();
    var tokenTypes = dto.getTokenTypesMap();
    tokenTypes.forEach(
        (k, v) ->
            props.tokenTypeMap.put(
                k,
                v.getPropertiesList().stream()
                    .map(TokenProperty::fromDto)
                    .collect(Collectors.toList())));
    if (dto.hasDefaultSightType()) {
      props.defaultSightType = dto.getDefaultSightType().getValue();
    }
    tokenTypes
        .keySet()
        .forEach(
            tt -> {
              var sheet = dto.getTokenTypeStatSheetMap().get(tt);
              if (sheet != null) {
                props.tokenTypeStatSheetMap.put(tt, StatSheetProperties.fromDto(sheet));
              }
            });
    dto.getTokenStatesList()
        .forEach(
            s -> {
              var overlay = BooleanTokenOverlay.fromDto(s);
              props.tokenStates.put(overlay.getName(), overlay);
            });
    dto.getTokenBarsList()
        .forEach(
            b -> {
              var overlay = BarTokenOverlay.fromDto(b);
              props.tokenBars.put(overlay.getName(), overlay);
            });
    props.characterSheets.putAll(dto.getCharacterSheetsMap());
    props.initiativeOwnerPermissions = dto.getInitiativeOwnerPermissions();
    props.initiativeMovementLock = dto.getInitiativeMovementLock();
    props.initiativeUseReverseSort = dto.getInitiativeUseReverseSort();
    props.initiativePanelButtonsDisabled = dto.getInitiativePanelButtonsDisabled();
    dto.getLightSourcesMap()
        .forEach(
            (k, v) -> {
              v.getLightSourcesList()
                  .forEach(l -> props.categorizedLights.addToCategory(k, LightSource.fromDto(l)));
            });
    props.remoteRepositoryList.addAll(dto.getRemoteRepositoriesList());
    dto.getLookupTablesList()
        .forEach(
            lt -> {
              var table = LookupTable.fromDto(lt);
              props.lookupTableMap.put(table.getName(), table);
            });
    dto.getSightTypesList()
        .forEach(
            st -> {
              var sightType = SightType.fromDto(st);
              props.sights.add(sightType);
            });

    if (dto.hasDefaultTokenPropertyType()) {
      props.defaultTokenPropertyType = dto.getDefaultTokenPropertyType().getValue();
    } else {
      props.defaultTokenPropertyType = FALLBACK_DEFAULT_TOKEN_PROPERTY_TYPE;
    }

    return props;
  }

  public CampaignPropertiesDto toDto() {
    var dto = CampaignPropertiesDto.newBuilder();
    tokenTypeMap.forEach(
        (k, v) ->
            dto.putTokenTypes(
                k,
                TokenPropertyListDto.newBuilder()
                    .addAllProperties(
                        v.stream().map(TokenProperty::toDto).collect(Collectors.toList()))
                    .build()));
    if (defaultSightType != null) {
      dto.setDefaultSightType(StringValue.of(defaultSightType));
    }
    tokenTypeStatSheetMap.forEach(
        (k, v) -> {
          if (v.id() != null) {
            var sheetPropDto = StatSheetProperties.toDto(v);
            dto.putTokenTypeStatSheet(k, sheetPropDto);
          }
        });
    dto.addAllTokenStates(
        tokenStates.values().stream().map(BooleanTokenOverlay::toDto).collect(Collectors.toList()));
    dto.addAllTokenBars(
        tokenBars.values().stream().map(BarTokenOverlay::toDto).collect(Collectors.toList()));
    dto.putAllCharacterSheets(characterSheets);
    dto.setInitiativeOwnerPermissions(initiativeOwnerPermissions);
    dto.setInitiativeMovementLock(initiativeMovementLock);
    dto.setInitiativeUseReverseSort(initiativeUseReverseSort);
    dto.setInitiativePanelButtonsDisabled(initiativePanelButtonsDisabled);

    categorizedLights
        .getCategories()
        .forEach(
            category -> {
              LightSourceListDto.Builder lightSources = LightSourceListDto.newBuilder();
              category.lights().stream()
                  .map(LightSource::toDto)
                  .forEachOrdered(lightSources::addLightSources);
              dto.putLightSources(category.name(), lightSources.build());
            });

    dto.addAllRemoteRepositories(remoteRepositoryList);
    dto.addAllLookupTables(
        lookupTableMap.values().stream().map(LookupTable::toDto).collect(Collectors.toList()));
    dto.addAllSightTypes(sights.stream().map(SightType::toDto).collect(Collectors.toList()));
    dto.setDefaultTokenPropertyType(StringValue.of(defaultTokenPropertyType));
    return dto.build();
  }
}
