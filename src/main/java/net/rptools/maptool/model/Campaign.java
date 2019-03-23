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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.rptools.lib.MD5Key;
import net.rptools.lib.net.Location;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.CampaignExportDialog;
import net.rptools.maptool.client.ui.ExportDialog;
import net.rptools.maptool.client.ui.ToolbarPanel;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.token.ImageTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;

/**
 * This object contains {@link Zone}s and {@link Asset}s that make up a campaign as well as links to
 * a variety of other campaign characteristics (campaign macros, properties, lookup tables, and so
 * on).
 *
 * <p>Roughly this is equivalent to multiple tabs that will appear on the client and all of the
 * images that will appear on it (and also campaign macro buttons).
 */
public class Campaign {
  /** The only built-in property type is "Basic". Any others are user-defined. */
  public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";

  private GUID id = new GUID();
  private Map<GUID, Zone> zones = Collections.synchronizedMap(new LinkedHashMap<GUID, Zone>());

  @SuppressWarnings("unused")
  private static transient ExportDialog exportInfo =
      null; // transient so it is not written out; entire element ignore when reading

  private static ExportDialog
      exportDialog; // this is the new export dialog (different name for upward compatibility)
  private static CampaignExportDialog campaignExportDialog;

  // Static data isn't written to the campaign file when saved; these two fields hold the output
  // location and type, and the
  // settings of all JToggleButton objects (JRadioButtons and JCheckBoxes).
  private Location exportLocation; // FJE 2011-01-14
  private Map<String, Boolean>
      exportSettings; // the state of each checkbox/radiobutton for the Export>ScreenshotAs dialog

  private CampaignProperties campaignProperties = new CampaignProperties();
  private transient boolean isBeingSerialized;

  // campaign macro button properties. these are saved along with the campaign.
  // as of 1.3b32
  private List<MacroButtonProperties> macroButtonProperties =
      new ArrayList<MacroButtonProperties>();
  // need to have a counter for additions to macroButtonProperties array
  // otherwise deletions/insertions from/to that array will go out of sync
  private int macroButtonLastIndex = 0;

  // DEPRECATED: As of 1.3b20 these are now in campaignProperties, but are here for backward
  // compatibility
  private Map<String, List<TokenProperty>> tokenTypeMap;
  private List<String> remoteRepositoryList;

  private Map<String, Map<GUID, LightSource>> lightSourcesMap;
  private Map<String, LookupTable> lookupTableMap;

  // DEPRECATED: as of 1.3b19 here to support old serialized versions
  // private Map<GUID, LightSource> lightSourceMap;

  /**
   * This flag indicates whether the manual fog tools have been used in this campaign while a server
   * is not running. See {@link ToolbarPanel#createFogPanel()} for details.
   *
   * <p>
   *
   * <ul>
   *   <li>null - server never started for this campaign
   *   <li>false - server started and IndividualFog == off
   *   <li>true - server started and IndividualFog == on
   * </ul>
   */
  private Boolean hasUsedFogToolbar = null;

  public Campaign() {
    macroButtonLastIndex = 0;
    macroButtonProperties = new ArrayList<MacroButtonProperties>();
  }

  private void checkCampaignPropertyConversion() {
    if (campaignProperties == null) {
      campaignProperties = new CampaignProperties();
    }
    if (tokenTypeMap != null) {
      campaignProperties.setTokenTypeMap(tokenTypeMap);
      tokenTypeMap = null;
    }
    if (remoteRepositoryList != null) {
      campaignProperties.setRemoteRepositoryList(remoteRepositoryList);
      remoteRepositoryList = null;
    }
    if (lightSourcesMap != null) {
      campaignProperties.setLightSourcesMap(lightSourcesMap);
      lightSourcesMap = null;
    }
    if (lookupTableMap != null) {
      campaignProperties.setLookupTableMap(lookupTableMap);
      lookupTableMap = null;
    }
  }

  public List<String> getRemoteRepositoryList() {
    checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
    return campaignProperties.getRemoteRepositoryList();
  }

  public Campaign(Campaign campaign) {
    zones = Collections.synchronizedMap(new LinkedHashMap<GUID, Zone>());

    /*
     * JFJ 2010-10-27 Don't forget that since these are new zones AND new tokens created here from the old one, if you have any data that needs to transfer over you will need to manually copy it
     * as is done below for the campaign properties and macro buttons.
     */
    for (Entry<GUID, Zone> entry : campaign.zones.entrySet()) {
      Zone copy = new Zone(entry.getValue());
      zones.put(copy.getId(), copy);
    }
    campaignProperties = new CampaignProperties(campaign.campaignProperties);
    macroButtonProperties =
        new ArrayList<MacroButtonProperties>(campaign.getMacroButtonPropertiesArray());
  }

  public GUID getId() {
    return id;
  }

  /**
   * This is a workaround to avoid the renderer and the serializer interating on the drawables at
   * the same time
   */
  public boolean isBeingSerialized() {
    return isBeingSerialized;
  }

  /**
   * This is a workaround to avoid the renderer and the serializer interating on the drawables at
   * the same time
   */
  public void setBeingSerialized(boolean isBeingSerialized) {
    this.isBeingSerialized = isBeingSerialized;
  }

  public List<String> getTokenTypes() {
    List<String> list = new ArrayList<String>(getTokenTypeMap().keySet());
    Collections.sort(list);
    return list;
  }

  public List<String> getSightTypes() {
    List<String> list = new ArrayList<String>(getSightTypeMap().keySet());
    Collections.sort(list);
    return list;
  }

  public void setSightTypes(List<SightType> typeList) {
    checkCampaignPropertyConversion();
    Map<String, SightType> map = new HashMap<String, SightType>();
    for (SightType sightType : typeList) {
      map.put(sightType.getName(), sightType);
    }
    campaignProperties.setSightTypeMap(map);
  }

  public List<TokenProperty> getTokenPropertyList(String tokenType) {
    return getTokenTypeMap().containsKey(tokenType)
        ? getTokenTypeMap().get(tokenType)
        : new ArrayList<TokenProperty>();
  }

  public void putTokenType(String name, List<TokenProperty> propertyList) {
    getTokenTypeMap().put(name, propertyList);
  }

  /**
   * Stub that calls <code>campaignProperties.getTokenTypeMap()</code>.
   *
   * @return
   */
  public Map<String, List<TokenProperty>> getTokenTypeMap() {
    checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
    return campaignProperties.getTokenTypeMap();
  }

  /**
   * Convenience method that calls {@link #getSightTypeMap()} and returns the value for the key
   * <code>type</code>.
   *
   * @return
   */
  public SightType getSightType(String type) {
    return getSightTypeMap()
        .get(
            (type != null && getSightTypeMap().containsKey(type))
                ? type
                : campaignProperties.getDefaultSightType());
  }

  /**
   * Stub that calls <code>campaignProperties.getSightTypeMap()</code>.
   *
   * @return
   */
  public Map<String, SightType> getSightTypeMap() {
    checkCampaignPropertyConversion();
    return campaignProperties.getSightTypeMap();
  }

  /**
   * Stub that calls <code>campaignProperties.getLookupTableMap()</code>.
   *
   * @return
   */
  public Map<String, LookupTable> getLookupTableMap() {
    checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
    return campaignProperties.getLookupTableMap();
  }

  public List<String> getLookupTables() {
    List<String> list = new ArrayList<String>(getLookupTableMap().keySet());
    Collections.sort(list);
    return list;
  }

  /**
   * Convenience method that iterates through {@link #getLightSourcesMap()} and returns the value
   * for the key <code>lightSourceId</code>.
   *
   * @return
   */
  public LightSource getLightSource(GUID lightSourceId) {

    for (Map<GUID, LightSource> map : getLightSourcesMap().values()) {
      if (map.containsKey(lightSourceId)) {
        return map.get(lightSourceId);
      }
    }
    return null;
  }

  /**
   * Stub that calls <code>campaignProperties.getLightSourcesMap()</code>.
   *
   * @return
   */
  public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
    checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
    return campaignProperties.getLightSourcesMap();
  }

  /**
   * Convenience method that calls {@link #getLightSourcesMap()} and returns the value for the key
   * <code>type</code>.
   *
   * @return
   */
  public Map<GUID, LightSource> getLightSourceMap(String type) {
    return getLightSourcesMap().get(type);
  }

  /**
   * Stub that calls <code>campaignProperties.getTokenStatesMap()</code>.
   *
   * @return
   */
  public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
    return campaignProperties.getTokenStatesMap();
  }

  /**
   * Stub that calls <code>campaignProperties.getTokenBarsMap()</code>.
   *
   * @return
   */
  public Map<String, BarTokenOverlay> getTokenBarsMap() {
    return campaignProperties.getTokenBarsMap();
  }

  /*
   * public void setExportInfo(ExportInfo exportInfo) { this.exportInfo = exportInfo; }
   *
   * public ExportInfo getExportInfo() { return exportInfo; }
   */

  public void setId(GUID id) {
    this.id = id;
  }

  /**
   * Returns an <code>ArrayList</code> of all available <code>Zone</code>s from the <code>zones
   * </code> <code>LinkedHashMap</code>.
   *
   * @return
   */
  public List<Zone> getZones() {
    return new ArrayList<Zone>(zones.values());
  }

  /**
   * Return the <code>Zone</code> with the given GUID.
   *
   * @param id
   * @return
   */
  public Zone getZone(GUID id) {
    return zones.get(id);
  }

  /**
   * Create an entry for the given <code>Zone</code> in the map, using <code>zone</code>'s {@link
   * Zone#getId()} method.
   *
   * @param zone
   */
  public void putZone(Zone zone) {
    zones.put(zone.getId(), zone);
  }

  public void removeAllZones() {
    zones.clear();
  }

  public void removeZone(GUID id) {
    zones.remove(id);
  }

  public boolean containsAsset(Asset asset) {
    return containsAsset(asset.getId());
  }

  public boolean containsAsset(MD5Key key) {
    for (Zone zone : zones.values()) {
      Set<MD5Key> assetSet = zone.getAllAssetIds();
      if (assetSet.contains(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether a server has been started using this campaign and, if so, whether the IndividualFog
   * feature was turned on at the time. This method returns <code>true</code> IFF a server has been
   * started with the IF feature turned on.
   *
   * @return <code>true</code> if IF feature has ever been used; <code>false</code> otherwise
   */
  public boolean hasUsedFogToolbar() {
    return hasUsedFogToolbar == null ? false : hasUsedFogToolbar.booleanValue();
  }

  public void setHasUsedFogToolbar(boolean b) {
    hasUsedFogToolbar = new Boolean(b);
  }

  public void mergeCampaignProperties(CampaignProperties properties) {
    properties.mergeInto(campaignProperties);
  }

  public void replaceCampaignProperties(CampaignProperties properties) {
    campaignProperties = new CampaignProperties(properties);
  }

  /**
   * Get a copy of the properties. This is for persistence. Modification of the properties do not
   * affect this campaign
   */
  public CampaignProperties getCampaignProperties() {
    return new CampaignProperties(campaignProperties);
  }

  public List<MacroButtonProperties> getMacroButtonPropertiesArray() {
    if (macroButtonProperties == null) {
      // macroButtonProperties is null if you are loading an old campaign file < 1.3b32
      macroButtonProperties = new ArrayList<MacroButtonProperties>();
    }
    return macroButtonProperties;
  }

  public void setMacroButtonPropertiesArray(List<MacroButtonProperties> properties) {
    macroButtonProperties = properties;
  }

  public void saveMacroButtonProperty(MacroButtonProperties properties) {
    // find the matching property in the array
    // TODO: hashmap? or equals()? or what?
    for (MacroButtonProperties prop : macroButtonProperties) {
      if (prop.getIndex() == properties.getIndex()) {
        prop.setColorKey(properties.getColorKey());
        prop.setAutoExecute(properties.getAutoExecute());
        prop.setCommand(properties.getCommand());
        prop.setHotKey(properties.getHotKey());
        prop.setIncludeLabel(properties.getIncludeLabel());
        prop.setApplyToTokens(properties.getApplyToTokens());
        prop.setLabel(properties.getLabel());
        prop.setGroup(properties.getGroup());
        prop.setSortby(properties.getSortby());
        prop.setFontColorKey(properties.getFontColorKey());
        prop.setFontSize(properties.getFontSize());
        prop.setMinWidth(properties.getMinWidth());
        prop.setMaxWidth(properties.getMaxWidth());
        prop.setToolTip(properties.getToolTip());
        prop.setAllowPlayerEdits(properties.getAllowPlayerEdits());
        MapTool.getFrame().getCampaignPanel().reset();
        return;
      }
    }
    macroButtonProperties.add(properties);
    MapTool.getFrame().getCampaignPanel().reset();
  }

  public int getMacroButtonNextIndex() {
    for (MacroButtonProperties prop : macroButtonProperties) {
      if (prop.getIndex() > macroButtonLastIndex) {
        macroButtonLastIndex = prop.getIndex();
      }
    }
    return ++macroButtonLastIndex;
  }

  public void deleteMacroButton(MacroButtonProperties properties) {
    macroButtonProperties.remove(properties);
    MapTool.getFrame().getCampaignPanel().reset();
  }

  /**
   * This method iterates through all Zones, TokenStates, TokenBars, and LookupTables and writes the
   * keys into a new, empty set. That set is the return value.
   *
   * @return
   */
  public Set<MD5Key> getAllAssetIds() {

    // Maps (tokens are implicit)
    Set<MD5Key> assetSet = new HashSet<MD5Key>();
    for (Zone zone : getZones()) {
      assetSet.addAll(zone.getAllAssetIds());
    }

    // States
    for (BooleanTokenOverlay overlay : getCampaignProperties().getTokenStatesMap().values()) {
      if (overlay instanceof ImageTokenOverlay) {
        assetSet.add(((ImageTokenOverlay) overlay).getAssetId());
      }
    }

    // Bars
    for (BarTokenOverlay overlay : getCampaignProperties().getTokenBarsMap().values()) {
      if (overlay instanceof SingleImageBarTokenOverlay) {
        assetSet.add(((SingleImageBarTokenOverlay) overlay).getAssetId());
      } else if (overlay instanceof TwoImageBarTokenOverlay) {
        assetSet.add(((TwoImageBarTokenOverlay) overlay).getTopAssetId());
        assetSet.add(((TwoImageBarTokenOverlay) overlay).getBottomAssetId());
      } else if (overlay instanceof MultipleImageBarTokenOverlay) {
        assetSet.addAll(Arrays.asList(((MultipleImageBarTokenOverlay) overlay).getAssetIds()));
      } // endif
    }

    // Tables
    for (LookupTable table : getCampaignProperties().getLookupTableMap().values()) {
      assetSet.addAll(table.getAllAssetIds());
    }

    return assetSet;
  }

  /** @return Getter for initiativeOwnerPermissions */
  public boolean isInitiativeOwnerPermissions() {
    return campaignProperties != null ? campaignProperties.isInitiativeOwnerPermissions() : false;
  }

  /** @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions */
  public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
    campaignProperties.setInitiativeOwnerPermissions(initiativeOwnerPermissions);
  }

  /** @return Getter for initiativeMovementLock */
  public boolean isInitiativeMovementLock() {
    return campaignProperties != null ? campaignProperties.isInitiativeMovementLock() : false;
  }

  /** @param initiativeMovementLock Setter for initiativeMovementLock */
  public void setInitiativeMovementLock(boolean initiativeMovementLock) {
    campaignProperties.setInitiativeMovementLock(initiativeMovementLock);
  }

  /** @return Getter for characterSheets */
  public Map<String, String> getCharacterSheets() {
    return getCampaignProperties().getCharacterSheets();
  }

  public ExportDialog getExportDialog() {
    if (exportDialog == null) {
      try {
        exportDialog = new ExportDialog();
      } catch (Exception e) {
        return null;
      }
    }
    // TODO: Ugh, what a kludge. This needs to be refactored so that the settings are separate from
    // the dialog
    // and easily accessible from elsewhere. I want separate XML files in the .cmpgn file eventually
    // so that
    // will be a good time to do this.
    exportDialog.setExportSettings(exportSettings);
    exportDialog.setExportLocation(exportLocation);
    return exportDialog;
  }

  public void setExportDialog(ExportDialog d) {
    exportDialog = d;
    exportSettings = d.getExportSettings();
    exportLocation = d.getExportLocation();
  }

  public CampaignExportDialog getExportCampaignDialog() {
    if (campaignExportDialog == null) {
      try {
        campaignExportDialog = new CampaignExportDialog();
      } catch (Exception e) {
        return null;
      }
    }

    return campaignExportDialog;
  }

  public void setExportCampaignDialog(CampaignExportDialog d) {
    campaignExportDialog = d;
  }
}
