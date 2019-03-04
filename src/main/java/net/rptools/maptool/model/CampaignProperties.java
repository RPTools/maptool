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

public class CampaignProperties {
  public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";

  private Map<String, List<TokenProperty>> tokenTypeMap;
  private List<String> remoteRepositoryList;
  private Map<String, Map<GUID, LightSource>> lightSourcesMap;
  private Map<String, LookupTable> lookupTableMap;
  private Map<String, SightType> sightTypeMap;

  private String defaultSightType;

  private Map<String, BooleanTokenOverlay> tokenStates;
  private Map<String, BarTokenOverlay> tokenBars;
  private Map<String, String> characterSheets;

  /** Flag indicating that owners have special permissions */
  private boolean initiativeOwnerPermissions = AppPreferences.getInitOwnerPermissions();

  /** Flag indicating that owners can only move tokens when they have initiative */
  private boolean initiativeMovementLock = AppPreferences.getInitLockMovement();

  public CampaignProperties() {
    init();
  }

  public CampaignProperties(CampaignProperties properties) {
    tokenTypeMap = new HashMap<String, List<TokenProperty>>();
    for (Entry<String, List<TokenProperty>> entry : properties.tokenTypeMap.entrySet()) {
      List<TokenProperty> typeList = new ArrayList<TokenProperty>();
      typeList.addAll(properties.tokenTypeMap.get(entry.getKey()));

      tokenTypeMap.put(entry.getKey(), typeList);
    }
    remoteRepositoryList = new ArrayList<String>(properties.remoteRepositoryList);

    lookupTableMap = new HashMap<String, LookupTable>();
    if (properties.lookupTableMap != null) {
      lookupTableMap.putAll(properties.lookupTableMap);
    }
    defaultSightType = properties.defaultSightType;
    sightTypeMap = new HashMap<String, SightType>();
    if (properties.sightTypeMap != null) {
      sightTypeMap.putAll(properties.sightTypeMap);
    }
    // TODO: This doesn't feel right, should we deep copy, or does this do that automatically ?
    lightSourcesMap = new TreeMap<String, Map<GUID, LightSource>>(properties.lightSourcesMap);

    // TODO: fix for when old campaigns have been loaded into b33+
    tokenStates = new LinkedHashMap<String, BooleanTokenOverlay>();
    if (properties.tokenStates == null || properties.tokenStates.isEmpty()) {
      properties.initTokenStatesMap();
    }
    for (BooleanTokenOverlay overlay : properties.tokenStates.values()) {
      overlay = (BooleanTokenOverlay) overlay.clone();
      tokenStates.put(overlay.getName(), overlay);
    } // endfor

    tokenBars = new LinkedHashMap<String, BarTokenOverlay>();
    if (properties.tokenBars == null || properties.tokenBars.isEmpty()) {
      properties.initTokenBarsMap();
    }
    for (BarTokenOverlay overlay : properties.tokenBars.values()) {
      overlay = (BarTokenOverlay) overlay.clone();
      tokenBars.put(overlay.getName(), overlay);
    } // endfor

    initiativeOwnerPermissions = properties.initiativeOwnerPermissions;
    initiativeMovementLock = properties.initiativeMovementLock;

    characterSheets = new HashMap<String, String>();
    if (properties.characterSheets == null || properties.characterSheets.isEmpty()) {
      properties.initCharacterSheetsMap();
    }
    for (String type : properties.characterSheets.keySet()) {
      characterSheets.put(type, properties.characterSheets.get(type));
    }
  }

  public void mergeInto(CampaignProperties properties) {
    if (tokenTypeMap != null) {
      // This will replace any dups
      properties.tokenTypeMap.putAll(tokenTypeMap);
    }
    if (remoteRepositoryList != null) {
      // Need to cull out dups
      for (String repo : properties.remoteRepositoryList) {
        if (!remoteRepositoryList.contains(repo)) {
          remoteRepositoryList.add(repo);
        }
      }
    }
    if (lightSourcesMap != null) {
      properties.lightSourcesMap.putAll(lightSourcesMap);
    }
    if (lookupTableMap != null) {
      properties.lookupTableMap.putAll(lookupTableMap);
    }
    if (sightTypeMap != null) {
      properties.sightTypeMap.putAll(sightTypeMap);
    }
    if (tokenStates != null) {
      properties.tokenStates.putAll(tokenStates);
    }
    if (tokenBars != null) {
      properties.tokenBars.putAll(tokenBars);
    }
  }

  public Map<String, List<TokenProperty>> getTokenTypeMap() {
    if (tokenTypeMap == null) {
      initTokenTypeMap();
    }
    return tokenTypeMap;
  }

  public Map<String, SightType> getSightTypeMap() {
    if (sightTypeMap == null) {
      initSightTypeMap();
    }
    return sightTypeMap;
  }

  public void setSightTypeMap(Map<String, SightType> map) {
    if (map != null) {
      sightTypeMap = map;
    }
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
  public void setTokenTypeMap(Map<String, List<TokenProperty>> map) {
    tokenTypeMap = map;
  }

  public List<TokenProperty> getTokenPropertyList(String tokenType) {
    return getTokenTypeMap().get(tokenType);
  }

  public List<String> getRemoteRepositoryList() {
    if (remoteRepositoryList == null) {
      initRemoteRepositoryList();
    }
    return remoteRepositoryList;
  }

  public void setRemoteRepositoryList(List<String> list) {
    remoteRepositoryList = list;
  }

  public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
    if (lightSourcesMap == null) {
      initLightSourcesMap();
    }
    return lightSourcesMap;
  }

  public void setLightSourcesMap(Map<String, Map<GUID, LightSource>> map) {
    lightSourcesMap = map;
  }

  public Map<String, LookupTable> getLookupTableMap() {
    if (lookupTableMap == null) {
      initLookupTableMap();
    }
    return lookupTableMap;
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
  public void setLookupTableMap(Map<String, LookupTable> map) {
    lookupTableMap = map;
  }

  public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
    if (tokenStates == null) {
      initTokenStatesMap();
    }
    return tokenStates;
  }

  public void setTokenStatesMap(Map<String, BooleanTokenOverlay> map) {
    tokenStates = map;
  }

  public Map<String, BarTokenOverlay> getTokenBarsMap() {
    if (tokenBars == null) {
      initTokenBarsMap();
    }
    return tokenBars;
  }

  public void setTokenBarsMap(Map<String, BarTokenOverlay> map) {
    tokenBars = map;
  }

  private void init() {
    initLookupTableMap();
    initLightSourcesMap();
    initRemoteRepositoryList();
    initTokenTypeMap();
    initSightTypeMap();
    initTokenStatesMap();
    initTokenBarsMap();
    initCharacterSheetsMap();
  }

  private void initLookupTableMap() {
    if (lookupTableMap != null) {
      return;
    }
    lookupTableMap = new HashMap<String, LookupTable>();
  }

  private void initLightSourcesMap() {
    if (lightSourcesMap != null) {
      return;
    }
    lightSourcesMap = new TreeMap<String, Map<GUID, LightSource>>();

    try {
      Map<String, List<LightSource>> map = LightSource.getDefaultLightSources();
      for (String key : map.keySet()) {
        Map<GUID, LightSource> lightSourceMap = new LinkedHashMap<GUID, LightSource>();
        for (LightSource source : map.get(key)) {
          lightSourceMap.put(source.getId(), source);
        }
        lightSourcesMap.put(key, lightSourceMap);
      }
    } catch (IOException ioe) {
      MapTool.showError("CampaignProperties.error.initLightSources", ioe);
    }
  }

  private void initRemoteRepositoryList() {
    if (remoteRepositoryList != null) {
      return;
    }
    remoteRepositoryList = new ArrayList<String>();
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
    sightTypeMap = new HashMap<String, SightType>();
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
    if (tokenTypeMap != null) {
      return;
    }
    tokenTypeMap = new HashMap<String, List<TokenProperty>>();

    List<TokenProperty> list = new ArrayList<TokenProperty>();
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

    tokenTypeMap.put(DEFAULT_TOKEN_PROPERTY_TYPE, list);
  }

  private void initTokenStatesMap() {
    tokenStates = new LinkedHashMap<String, BooleanTokenOverlay>();
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
    tokenBars = new LinkedHashMap<String, BarTokenOverlay>();
    tokenBars.put(
        "Health", new TwoToneBarTokenOverlay("Health", new Color(0x20b420), Color.BLACK, 6));
  }

  private void initCharacterSheetsMap() {
    characterSheets = new HashMap<String, String>();
    characterSheets.put("Basic", "net/rptools/maptool/client/ui/forms/basicCharacterSheet.xml");
  }

  public Set<MD5Key> getAllImageAssets() {
    Set<MD5Key> set = new HashSet<MD5Key>();

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

  /** @return Getter for initiativeOwnerPermissions */
  public boolean isInitiativeOwnerPermissions() {
    return initiativeOwnerPermissions;
  }

  /** @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions */
  public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
    this.initiativeOwnerPermissions = initiativeOwnerPermissions;
  }

  /** @return Getter for initiativeMovementLock */
  public boolean isInitiativeMovementLock() {
    return initiativeMovementLock;
  }

  /** @param initiativeOwnerPermissions Setter for initiativeMovementLock */
  public void setInitiativeMovementLock(boolean initiativeMovementLock) {
    this.initiativeMovementLock = initiativeMovementLock;
  }

  /**
   * Getter for characterSheets. Only called by {@link Campaign#getCharacterSheets()} and that
   * function is never used elsewhere within MapTool. Yet. ;-)
   */
  public Map<String, String> getCharacterSheets() {
    if (characterSheets == null) initCharacterSheetsMap();
    return characterSheets;
  }

  /** @param characterSheets Setter for characterSheets */
  public void setCharacterSheets(Map<String, String> characterSheets) {
    this.characterSheets = characterSheets;
  }
}
