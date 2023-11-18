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
import java.io.IOException;
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
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
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
import net.rptools.maptool.model.sheet.stats.StatSheetLocation;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import net.rptools.maptool.server.proto.CampaignPropertiesDto;
import net.rptools.maptool.server.proto.LightSourceListDto;
import net.rptools.maptool.server.proto.TokenPropertyListDto;

public class CampaignProperties {

  /** The property type to fall back to for the default when none is defined. */
  private static final String FALLBACK_DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";

  /** The default property type for tokens. */
  private String defaultTokenPropertyType = FALLBACK_DEFAULT_TOKEN_PROPERTY_TYPE;

  private Map<String, List<TokenProperty>> tokenTypeMap = new HashMap<>();

  /** Mapping between property types and default stat sheets for them. */
  private Map<String, StatSheetProperties> tokenTypeStatSheetMap = new HashMap<>();

  private List<String> remoteRepositoryList = new ArrayList<>();
  private Map<String, Map<GUID, LightSource>> lightSourcesMap = new TreeMap<>();
  private Map<String, LookupTable> lookupTableMap = new HashMap<>();
  private Map<String, SightType> sightTypeMap = new HashMap<>();

  private String defaultSightType;

  private Map<String, BooleanTokenOverlay> tokenStates = new LinkedHashMap<>();
  private Map<String, BarTokenOverlay> tokenBars = new LinkedHashMap<>();
  private Map<String, String> characterSheets = new HashMap<>();

  /** Flag indicating that owners have special permissions */
  private boolean initiativeOwnerPermissions = AppPreferences.getInitOwnerPermissions();

  /** Flag indicating that owners can only move tokens when they have initiative */
  private boolean initiativeMovementLock = AppPreferences.getInitLockMovement();

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
    sightTypeMap.putAll(properties.sightTypeMap);
    // TODO: This doesn't feel right, should we deep copy, or does this do that automatically ?
    lightSourcesMap.putAll(properties.lightSourcesMap);

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
    properties.lightSourcesMap.putAll(lightSourcesMap);
    properties.lookupTableMap.putAll(lookupTableMap);
    properties.sightTypeMap.putAll(sightTypeMap);
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

  public Map<String, SightType> getSightTypeMap() {
    return sightTypeMap;
  }

  public void setSightTypeMap(Map<String, SightType> map) {
    if (map != null) {
      sightTypeMap.clear();
      sightTypeMap.putAll(map);
    }
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
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

  public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
    return lightSourcesMap;
  }

  public void setLightSourcesMap(Map<String, Map<GUID, LightSource>> map) {
    lightSourcesMap.clear();
    lightSourcesMap.putAll(map);
  }

  public Map<String, LookupTable> getLookupTableMap() {
    return lookupTableMap;
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
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
    initLightSourcesMap();
    initTokenTypeMap();
    initSightTypeMap();
    initTokenStatesMap();
    initTokenBarsMap();
    initCharacterSheetsMap();
  }

  private void initLightSourcesMap() {
    if (!lightSourcesMap.isEmpty()) {
      return;
    }

    try {
      Map<String, List<LightSource>> map = LightSource.getDefaultLightSources();
      for (var entry : map.entrySet()) {
        String key = entry.getKey();
        Map<GUID, LightSource> lightSourceMap = new LinkedHashMap<>();
        for (LightSource source : entry.getValue()) {
          lightSourceMap.put(source.getId(), source);
        }
        lightSourcesMap.put(key, lightSourceMap);
      }
    } catch (IOException ioe) {
      MapTool.showError("CampaignProperties.error.initLightSources", ioe);
    }
  }

  public String getDefaultSightType() {
    return defaultSightType;
  }

  // @formatter:off
  private static final Object[][] starter =
      new Object[][] {
        // Sight Type Name					Dist		Mult		Arc		LtSrc		Shape				Scale
        {"Normal", 0.0, 1.0, 0, null, null, false},
        {"Lowlight", 0.0, 2.0, 0, null, null, false},
        {"Grid Vision", 0.0, 1.0, 0, null, ShapeType.GRID, true},
        {"Square Vision", 0.0, 1.0, 0, null, ShapeType.SQUARE, false},
        {"Normal Vision - Short Range", 10.0, 1.0, 0, null, ShapeType.CIRCLE, true},
        {"Conic Vision", 0.0, 1.0, 120, null, ShapeType.CONE, false},
        {"Darkvision", 0.0, 1.0, 0, null, null, true},
      };

  // @formatter:on

  private void initSightTypeMap() {
    sightTypeMap.clear();
    for (Object[] row : starter) {
      SightType st =
          new SightType(
              (String) row[0],
              (Double) row[2],
              (LightSource) row[4],
              (ShapeType) row[5],
              (Integer) row[3],
              (boolean) row[6]);
      st.setDistance(((Double) row[1]).floatValue());
      sightTypeMap.put((String) row[0], st);
    }
    SightType dv = sightTypeMap.get("Darkvision");
    try {
      dv.setPersonalLightSource(LightSource.getDefaultLightSources().get("Generic").get(5));
      // sightTypeMap.put("Darkvision & Lowlight", new SightType("Darkvision", 2,
      // LightSource.getDefaultLightSources().get("Generic").get(4)));
    } catch (IOException e) {
      MapTool.showError("CampaignProperties.error.noGenericLight", e);
    }
    defaultSightType = (String) starter[0][0];
  }

  private void initTokenTypeMap() {
    if (!tokenTypeMap.isEmpty()) {
      return;
    }

    List<TokenProperty> list = new ArrayList<>();
    list.add(new TokenProperty("Strength", "Str"));
    list.add(new TokenProperty("Dexterity", "Dex"));
    list.add(new TokenProperty("Constitution", "Con"));
    list.add(new TokenProperty("Intelligence", "Int"));
    list.add(new TokenProperty("Wisdom", "Wis"));
    list.add(new TokenProperty("Charisma", "Char"));
    list.add(new TokenProperty("HP", true, true, false));
    list.add(new TokenProperty("AC", true, true, false));
    list.add(new TokenProperty("Defense", "Def"));
    list.add(new TokenProperty("Movement", "Mov"));
    list.add(new TokenProperty("Elevation", "Elv", true, false, false));
    list.add(new TokenProperty("Description", "Des"));

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

  protected Object readResolve() {
    if (tokenTypeMap == null) {
      tokenTypeMap = new HashMap<>();
    }
    if (remoteRepositoryList == null) {
      remoteRepositoryList = new ArrayList<>();
    }
    if (lightSourcesMap == null) {
      lightSourcesMap = new TreeMap<>();
    }
    if (lookupTableMap == null) {
      lookupTableMap = new HashMap<>();
    }
    if (sightTypeMap == null) {
      sightTypeMap = new HashMap<>();
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
              var map = new HashMap<GUID, LightSource>();
              v.getLightSourcesList()
                  .forEach(
                      l -> {
                        var lightSource = LightSource.fromDto(l);
                        map.put(lightSource.getId(), lightSource);
                      });
              props.lightSourcesMap.put(k, map);
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
              props.sightTypeMap.put(sightType.getName(), sightType);
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
    lightSourcesMap.forEach(
        (k, v) ->
            dto.putLightSources(
                k,
                LightSourceListDto.newBuilder()
                    .addAllLightSources(
                        v.values().stream().map(LightSource::toDto).collect(Collectors.toList()))
                    .build()));
    dto.addAllRemoteRepositories(remoteRepositoryList);
    dto.addAllLookupTables(
        lookupTableMap.values().stream().map(LookupTable::toDto).collect(Collectors.toList()));
    dto.addAllSightTypes(
        sightTypeMap.values().stream().map(SightType::toDto).collect(Collectors.toList()));
    dto.setDefaultTokenPropertyType(StringValue.of(defaultTokenPropertyType));
    return dto.build();
  }
}
