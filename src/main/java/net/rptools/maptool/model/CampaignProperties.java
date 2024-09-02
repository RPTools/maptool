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
import java.util.*;
import java.util.Map.Entry;
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
import net.rptools.maptool.server.proto.FootprintListDto;
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

  private Map<String, List<TokenFootprint>> gridFootprints = new HashMap<>();

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
    if (properties.gridFootprints != null) {
      gridFootprints.clear();
      gridFootprints.putAll(properties.gridFootprints);
    }
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
    if (properties.gridFootprints != null) {
      properties.gridFootprints.putAll(gridFootprints);
    } else {
      properties.gridFootprints = new HashMap<>(gridFootprints);
    }
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
    initTokenFootprints();
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

  private void initSightTypeMap() {
    sightTypeMap.clear();

    final var types =
        new SightType[] {
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
                  List.of(new Light(ShapeType.CIRCLE, 0, 60, 0, 0, null, 100, false, false)))),
        };

    for (SightType st : types) {
      sightTypeMap.put(st.getName(), st);
    }
    defaultSightType = types[0].getName();
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

  protected List<TokenFootprint> loadFootprints(
      String path, net.rptools.maptool.model.TokenFootprint.OffsetTranslator... translators) {
    List<TokenFootprint> result = null;
    try {
      Object obj = net.rptools.lib.FileUtil.objFromResource(path);
      @SuppressWarnings("unchecked")
      List<TokenFootprint> footprintList = (List<TokenFootprint>) obj;
      for (TokenFootprint footprint : footprintList) {
        for (net.rptools.maptool.model.TokenFootprint.OffsetTranslator ot : translators) {
          footprint.addOffsetTranslator(ot);
        }
      }
      result = footprintList;
    } catch (IOException ioe) {
      MapTool.showError("Could not load VHex Grid footprints", ioe);
    }
    return result;
  }

  public void resetTokenFootprints() {
    initTokenFootprints(true);
  }

  private void initTokenFootprints() {
    initTokenFootprints(false);
  }

  private void initTokenFootprints(boolean reset) {
    if (!gridFootprints.isEmpty() && !reset) {
      return;
    } else if (!gridFootprints.isEmpty()) {
      gridFootprints.clear();
    }
    // Potential for importing defaults from app preferences instead.

    setGridFootprints(
        "Horizontal Hex",
        loadFootprints(
            "net/rptools/maptool/model/hexGridHorizFootprints.xml",
            (originPoint, offsetPoint) -> {
              if (Math.abs(originPoint.y) % 2 == 1 && Math.abs(offsetPoint.y) % 2 == 0) {
                offsetPoint.x++;
              }
            }));
    setGridFootprints(
        "Vertical Hex",
        loadFootprints(
            "net/rptools/maptool/model/hexGridVertFootprints.xml",
            (originPoint, offsetPoint) -> {
              if (Math.abs(originPoint.x) % 2 == 1 && Math.abs(offsetPoint.x) % 2 == 0) {
                offsetPoint.y++;
              }
            }));
    setGridFootprints(
        "None", loadFootprints("net/rptools/maptool/model/gridlessGridFootprints.xml"));
    setGridFootprints(
        "Square", loadFootprints("net/rptools/maptool/model/squareGridFootprints.xml"));
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

  public Map<String, List<TokenFootprint>> getGridFootprints() {
    return gridFootprints;
  }

  public void setGridFootprints(String gridType, List<TokenFootprint> footprintList) {
    gridFootprints.put(gridType, footprintList);
  }

  public void setGridFootprint(String footprintName, String gridType, TokenFootprint newPrint) {
    if (!gridFootprints.containsKey(gridType)) {
      gridFootprints.put(gridType, new ArrayList<TokenFootprint>());
    }
    List<TokenFootprint> allFootprints = new ArrayList(gridFootprints.get(gridType));
    if (!allFootprints.isEmpty()) {
      for (var i = 0; i < allFootprints.size(); i++) {
        String testName = allFootprints.get(i).getName();
        if (Objects.equals(testName, footprintName)) {
          allFootprints.set(i, newPrint);
          return;
        }
      }
    }
    allFootprints.add(newPrint);
    setGridFootprints(gridType, allFootprints);
  }

  public void removeGridFootprint(String gridtype, String name) {
    if (!gridFootprints.containsKey(gridtype) || gridFootprints.get(gridtype).isEmpty()) {
      return;
    } else {
      List<TokenFootprint> allFootprints = new ArrayList(gridFootprints.get(gridtype));
      int removeIndex = -1;
      for (var i = 0; i < allFootprints.size(); i++) {
        String testName = allFootprints.get(i).getName();
        if (Objects.equals(testName, name)) {
          removeIndex = i;
          break;
        }
      }
      if (removeIndex != -1) {
        allFootprints.remove(removeIndex);
        setGridFootprints(gridtype, allFootprints);
      }
    }
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
    dto.getGridFootprints()
        .forEach(
            (k, v) -> {
              List<TokenFootprint> newList = new ArrayList<>();
              v.getFootprintList()
                  .forEach(
                      (ik, iv) -> {
                        TokenFootprint newPrint = TokenFootprint.fromDto(iv);
                        newList.add(newPrint);
                      });
              props.gridFootprints.put(k, newList);
            });

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
    gridFootprints.forEach(
        (k, v) -> {
          var subDTO = FootprintListDto.newBuilder();
          v.forEach(
              (f) -> {
                subDTO.putFootprintList(f.getName(), f.toDto());
              });
          dto.putGridFootprints(k, subDTO.build());
        });
    return dto.build();
  }
}
