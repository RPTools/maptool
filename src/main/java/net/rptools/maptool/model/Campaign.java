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

import com.google.protobuf.BoolValue;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.rptools.lib.MD5Key;
import net.rptools.lib.net.Location;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.ToolbarPanel;
import net.rptools.maptool.client.ui.exportdialog.ExportDialog;
import net.rptools.maptool.client.ui.macrobuttons.panels.AbstractMacroPanel;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.token.ImageTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import net.rptools.maptool.server.proto.CampaignDto;

/**
 * This object contains {@link Zone}s and {@link Asset}s that make up a campaign as well as links to
 * a variety of other campaign characteristics (campaign macros, properties, lookup tables, and so
 * on).
 *
 * <p>Roughly this is equivalent to multiple tabs that will appear on the client and all of the
 * images that will appear on it (and also campaign macro buttons).
 */
public class Campaign {
  private GUID id = new GUID();

  /** The {@link Zone}s that make up this {@code Campaign}. */
  private final Map<GUID, Zone> zones =
      Collections.synchronizedMap(new LinkedHashMap<GUID, Zone>());

  private String name; // the name of the campaign, to be displayed in the MapToolFrame title bar

  private static ExportDialog exportDialog =
      ExportDialog
          .getInstance(); // this is the new export dialog (different name for upward compatibility)

  // Static data isn't written to the campaign file when saved; these two fields hold the output
  // location and type, and the
  // settings of all JToggleButton objects (JRadioButtons and JCheckBoxes).
  private Location exportLocation; // FJE 2011-01-14
  private Map<String, Boolean> exportSettings =
      new HashMap<>(); // the state of each checkbox/radiobutton for the Export>ScreenshotAs dialog

  private CampaignProperties campaignProperties = new CampaignProperties();
  private transient boolean isBeingSerialized;

  // campaign macro button properties. these are saved along with the campaign.
  // as of 1.3b32
  private List<MacroButtonProperties> macroButtonProperties;
  // need to have a counter for additions to macroButtonProperties array
  // otherwise deletions/insertions from/to that array will go out of sync
  private int macroButtonLastIndex = 0;
  private int gmMacroButtonLastIndex = 0;

  // campaign GM macro button properties. these are saved along with the campaign.
  // as of 1.5.6
  private List<MacroButtonProperties> gmMacroButtonProperties;

  // DEPRECATED: As of 1.3b20 these are now in campaignProperties, but are here for backward
  // compatibility
  private Map<String, List<TokenProperty>> tokenTypeMap;

  private List<String> remoteRepositoryList;

  private Map<String, Map<GUID, LightSource>> lightSourcesMap;
  private Map<String, LookupTable> lookupTableMap;

  // DEPRECATED: as of 1.3b19 here to support old serialized versions
  // private Map<GUID, LightSource> lightSourceMap;

  /**
   * Record to hold the arguments for rename token type functionality.
   *
   * @param from the name to rename from.
   * @param to the name to rename to.
   */
  public record RenamePropertyType(@Nonnull String from, @Nonnull String to) {}

  /**
   * This flag indicates whether the manual fog tools have been used in this campaign while a server
   * is not running. See {@link ToolbarPanel} for details.
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
    name = "Default";
    macroButtonLastIndex = 0;
    gmMacroButtonLastIndex = 0;
    macroButtonProperties = new ArrayList<MacroButtonProperties>();
    gmMacroButtonProperties = new ArrayList<MacroButtonProperties>();
  }

  private Object readResolve() {
    if (exportSettings == null) {
      exportSettings = new HashMap<>();
    }

    return this;
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

  /**
   * Create a new campaign with an old campaign's properties.
   *
   * @param campaign The campaign to copy from.
   */
  public Campaign(Campaign campaign) {
    id = campaign.getId();
    name = campaign.getName();

    /*
     * Don't forget that since these are new zones AND new tokens created here from the old one,
     * if you have any data that needs to transfer over you will need to manually copy it
     * as is done below for the campaign properties and macro buttons. Iteration over a synchronized
     *  map must lock the map.
     */
    Map<GUID, Zone> zonesToCopy;
    synchronized (zones) {
      zonesToCopy = new LinkedHashMap<>(campaign.zones);
    }
    for (Entry<GUID, Zone> entry : zonesToCopy.entrySet()) {
      Zone copy = new Zone(entry.getValue(), true);
      zones.put(copy.getId(), copy);
    }
    campaignProperties = new CampaignProperties(campaign.campaignProperties);
    macroButtonProperties =
        new ArrayList<MacroButtonProperties>(campaign.getMacroButtonPropertiesArray());
    gmMacroButtonProperties =
        new ArrayList<MacroButtonProperties>(campaign.getGmMacroButtonPropertiesArray());
  }

  public GUID getId() {
    return id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * This is a workaround to avoid the renderer and the serializer interating on the drawables at
   * the same time
   *
   * @return true if currently being serialized
   */
  public boolean isBeingSerialized() {
    return isBeingSerialized;
  }

  /**
   * This is a workaround to avoid the renderer and the serializer interating on the drawables at
   * the same time
   *
   * @param isBeingSerialized the new value of isBeingSerialized, should be true if the object is
   *     being serialized
   */
  public void setBeingSerialized(boolean isBeingSerialized) {
    this.isBeingSerialized = isBeingSerialized;
  }

  public List<String> getTokenTypes() {
    List<String> list = new ArrayList<String>(getTokenTypeMap().keySet());
    Collections.sort(list);
    return list;
  }

  /**
   * Returns the default Stat Sheet ID for the specified token property type.
   *
   * @param tokenProperty the token property type to get the sheet ID for.
   * @return the properties of the Stat Sheet.
   */
  public StatSheetProperties getTokenTypeDefaultSheetId(String tokenProperty) {
    return campaignProperties.getTokenTypeDefaultStatSheet(tokenProperty);
  }

  /**
   * Sets the default Stat Sheet ID for the specified token property type.
   *
   * @param tokenProperty the token property type to set the sheet ID of.
   * @param sheetId the Stat Sheet properties.
   */
  public void setTokenTypeDefaultSheetId(String tokenProperty, StatSheetProperties sheetId) {
    campaignProperties.setTokenTypeDefaultStatSheet(tokenProperty, sheetId);
  }

  /**
   * Sets the default property type for tokens.
   *
   * @param def the default property type.
   */
  public void setDefaultTokenPropertyType(String def) {
    campaignProperties.setDefaultTokenPropertyType(def);
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
   * @return the {@link Map} of token types
   */
  public Map<String, List<TokenProperty>> getTokenTypeMap() {
    checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
    return campaignProperties.getTokenTypeMap();
  }

  /**
   * Convenience method that calls {@link #getSightTypeMap()} and returns the value for the key
   * <code>type</code>.
   *
   * @param type the String corresponding to the SightType.
   * @return the SightType.
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
   * @return the {@link Map} of {@link SightType}s
   */
  public Map<String, SightType> getSightTypeMap() {
    checkCampaignPropertyConversion();
    return campaignProperties.getSightTypeMap();
  }

  /**
   * Stub that calls <code>campaignProperties.getLookupTableMap()</code>.
   *
   * @return the {@link Map} of {@link LookupTable}s types
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
   * Stub that calls <code>campaignProperties.getLightSourcesMap()</code>.
   *
   * @return the {@link Map} of between lightSourceIds and {@link LightSource}s
   */
  public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
    checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
    return campaignProperties.getLightSourcesMap();
  }

  /**
   * Convenience method that calls {@link #getLightSourcesMap()} and returns the value for the key
   * <code>type</code>.
   *
   * @param type the key
   * @return the {@link Map} of between lightSourceIds and {@link LightSource}s for a specifict type
   */
  public Map<GUID, LightSource> getLightSourceMap(String type) {
    return getLightSourcesMap().get(type);
  }

  /**
   * Stub that calls <code>campaignProperties.getTokenStatesMap()</code>.
   *
   * @return the {@link Map} for token states
   */
  public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
    return campaignProperties.getTokenStatesMap();
  }

  /**
   * Stub that calls <code>campaignProperties.getTokenBarsMap()</code>.
   *
   * @return the {@link Map} for token bars
   */
  public Map<String, BarTokenOverlay> getTokenBarsMap() {
    return campaignProperties.getTokenBarsMap();
  }

  public void setId(GUID id) {
    this.id = id;
  }

  /**
   * Returns an <code>ArrayList</code> of all available <code>Zone</code>s from the <code>zones
   * </code> <code>LinkedHashMap</code>.
   *
   * @return a list of zones
   */
  public List<Zone> getZones() {
    synchronized (zones) { // Must lock synchronized map while iterating over contents.
      return new ArrayList<Zone>(zones.values());
    }
  }

  /**
   * Return the <code>Zone</code> with the given GUID.
   *
   * @param id the id to look for
   * @return the Zone for the id
   */
  public Zone getZone(GUID id) {
    return zones.get(id);
  }

  /**
   * Create an entry for the given <code>Zone</code> in the map, using <code>zone</code>'s {@link
   * Zone#getId()} method.
   *
   * @param zone the zone to put into zones.
   */
  public void putZone(Zone zone) {
    zones.put(zone.getId(), zone);
  }

  public void removeAllZones() {
    zones.clear();
  }

  /**
   * Remove a zone from zones.
   *
   * @param id the GUID of the zone.
   */
  public void removeZone(GUID id) {
    zones.remove(id);
  }

  public boolean containsAsset(Asset asset) {
    return containsAsset(asset.getMD5Key());
  }

  public boolean containsAsset(MD5Key key) {
    Collection<Zone> zonesToCheck;
    synchronized (zones) { // iteration over synchronized map must lock the map
      zonesToCheck = zones.values();
    }

    for (Zone zone : zonesToCheck) {
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
    return hasUsedFogToolbar != null && hasUsedFogToolbar;
  }

  public void setHasUsedFogToolbar(boolean b) {
    hasUsedFogToolbar = b;
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
   *
   * @return a copy of the properties
   */
  public CampaignProperties getCampaignProperties() {
    return new CampaignProperties(campaignProperties);
  }

  /**
   * Getter for the array of Campaign macros
   *
   * @return the Campaign macros
   */
  public List<MacroButtonProperties> getMacroButtonPropertiesArray() {
    if (macroButtonProperties == null) {
      // macroButtonProperties is null if you are loading an old campaign file < 1.3b32
      macroButtonProperties = new ArrayList<MacroButtonProperties>();
    }
    return macroButtonProperties;
  }

  /**
   * Setter for the list of Campaign macros
   *
   * @param properties the List of Campaign Macros
   */
  public void setMacroButtonPropertiesArray(List<MacroButtonProperties> properties) {
    macroButtonProperties = properties;
  }

  /**
   * Getter for the array of GM macros
   *
   * @return the GM macros
   */
  public List<MacroButtonProperties> getGmMacroButtonPropertiesArray() {
    if (gmMacroButtonProperties == null) {
      // gmMacroButtonProperties is null if you are loading an old campaign file < 1.5.6
      gmMacroButtonProperties = new ArrayList<MacroButtonProperties>();
    }
    return gmMacroButtonProperties;
  }

  /**
   * Setter for the list of GM macros
   *
   * @param properties the List of GM Macros
   */
  public void setGmMacroButtonPropertiesArray(List<MacroButtonProperties> properties) {
    gmMacroButtonProperties = properties;
  }

  /**
   * Adds multiple MacroButtonProperties to the GM or Campaign macro panel, starting at the next
   * appropriate index.
   *
   * @param toSave the list of button properties to add
   * @param gmPanel true for the GM panel, false for the Campaign panel.
   */
  public void addMacroButtonPropertiesAtNextIndex(
      List<MacroButtonProperties> toSave, boolean gmPanel) {
    List<MacroButtonProperties> macroButtonList =
        gmPanel ? gmMacroButtonProperties : macroButtonProperties;
    int lastIndex = gmPanel ? gmMacroButtonLastIndex : macroButtonLastIndex;
    // populate the lookup table and confirm lastIndex
    for (MacroButtonProperties prop : macroButtonList) {
      int curIndex = prop.getIndex();
      if (curIndex > lastIndex) {
        lastIndex = curIndex;
      }
    }
    // set the indexes and add to list
    for (MacroButtonProperties newProp : toSave) {
      newProp.setIndex(++lastIndex);
    }
    macroButtonList.addAll(toSave);

    // update the ButtonLastIndex prop as appropriate
    if (gmPanel) {
      gmMacroButtonLastIndex = lastIndex;
    } else {
      macroButtonLastIndex = lastIndex;
    }

    AbstractMacroPanel macroPanel =
        gmPanel ? MapTool.getFrame().getGmPanel() : MapTool.getFrame().getCampaignPanel();
    macroPanel.reset();
  }

  /**
   * Saves a button in the Campaign or GM panel.
   *
   * @param properties the properties of the button to save
   * @param gmPanel whether the button is in the GM panel.
   */
  public void saveMacroButtonProperty(MacroButtonProperties properties, boolean gmPanel) {
    List<MacroButtonProperties> macroButtonList =
        gmPanel ? gmMacroButtonProperties : macroButtonProperties;
    AbstractMacroPanel macroPanel =
        gmPanel ? MapTool.getFrame().getGmPanel() : MapTool.getFrame().getCampaignPanel();

    for (MacroButtonProperties prop : macroButtonList) {
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
        prop.setDisplayHotKey(properties.getDisplayHotKey());
        prop.setCompareIncludeLabel(properties.getCompareIncludeLabel());
        prop.setCompareAutoExecute(properties.getCompareAutoExecute());
        prop.setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
        prop.setCompareGroup(properties.getCompareGroup());
        prop.setCompareSortPrefix(properties.getCompareSortPrefix());
        prop.setCompareCommand(properties.getCompareCommand());

        macroPanel.reset();
        return;
      }
    }
    macroButtonList.add(properties);
    macroPanel.reset();
  }

  public int getMacroButtonNextIndex() {
    for (MacroButtonProperties prop : macroButtonProperties) {
      if (prop.getIndex() > macroButtonLastIndex) {
        macroButtonLastIndex = prop.getIndex();
      }
    }
    return ++macroButtonLastIndex;
  }

  public int getGmMacroButtonNextIndex() {
    for (MacroButtonProperties prop : gmMacroButtonProperties) {
      if (prop.getIndex() > gmMacroButtonLastIndex) {
        gmMacroButtonLastIndex = prop.getIndex();
      }
    }
    return ++gmMacroButtonLastIndex;
  }

  public void deleteMacroButton(MacroButtonProperties properties) {
    macroButtonProperties.remove(properties);
    MapTool.getFrame().getCampaignPanel().reset();
  }

  public void deleteGmMacroButton(MacroButtonProperties properties) {
    gmMacroButtonProperties.remove(properties);
    MapTool.getFrame().getGmPanel().reset();
  }

  /**
   * This method iterates through all Zones, TokenStates, TokenBars, and LookupTables and writes the
   * keys into a new, empty set. That set is the return value.
   *
   * @return a set of MD5 keys
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

  /**
   * @return Getter for initiativeOwnerPermissions
   */
  public boolean isInitiativeOwnerPermissions() {
    return campaignProperties != null && campaignProperties.isInitiativeOwnerPermissions();
  }

  /**
   * @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions
   */
  public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
    campaignProperties.setInitiativeOwnerPermissions(initiativeOwnerPermissions);
  }

  /**
   * @return Getter for initiativeMovementLock
   */
  public boolean isInitiativeMovementLock() {
    return campaignProperties != null && campaignProperties.isInitiativeMovementLock();
  }

  /**
   * @param initiativeMovementLock Setter for initiativeMovementLock
   */
  public void setInitiativeMovementLock(boolean initiativeMovementLock) {
    campaignProperties.setInitiativeMovementLock(initiativeMovementLock);
  }

  public boolean isInitiativeUseReverseSort() {
    return campaignProperties != null && campaignProperties.isInitiativeUseReverseSort();
  }

  public void setInitiativeUseReverseSort(boolean initiativeUseReverseSort) {
    campaignProperties.setInitiativeUseReverseSort(initiativeUseReverseSort);
  }

  public boolean isInitiativePanelButtonsDisabled() {
    return campaignProperties != null && campaignProperties.isInitiativePanelButtonsDisabled();
  }

  public void setInitiativePanelButtonsDisabled(boolean disabled) {
    campaignProperties.setInitiativePanelButtonsDisabled(disabled);
  }

  /**
   * @return Getter for characterSheets
   */
  public Map<String, String> getCharacterSheets() {
    return getCampaignProperties().getCharacterSheets();
  }

  public ExportDialog getExportDialog() {
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

  public void initDefault() {
    campaignProperties.initDefaultProperties();
  }

  public static Campaign fromDto(CampaignDto dto) {
    var campaign = new Campaign();
    campaign.id = GUID.valueOf(dto.getId());
    campaign.name = dto.getName();
    campaign.hasUsedFogToolbar =
        dto.hasHasUsedFogToolbar() ? dto.getHasUsedFogToolbar().getValue() : null;
    campaign.campaignProperties = CampaignProperties.fromDto(dto.getProperties());
    campaign.exportLocation =
        dto.hasExportLocation() ? Location.fromDto(dto.getExportLocation()) : null;
    campaign.exportSettings.putAll(dto.getExportSettingsMap());
    campaign.macroButtonLastIndex = dto.getMacroButtonLastIndex();
    campaign.gmMacroButtonLastIndex = dto.getGmMacroButtonLastIndex();
    campaign.macroButtonProperties =
        dto.getMacroButtonPropertiesList().stream()
            .map(MacroButtonProperties::fromDto)
            .collect(Collectors.toList());
    campaign.gmMacroButtonProperties =
        dto.getGmMacroButtonPropertiesList().stream()
            .map(MacroButtonProperties::fromDto)
            .collect(Collectors.toList());
    var zoneList = dto.getZonesList().stream().map(Zone::fromDto).toList();
    zoneList.forEach(z -> campaign.zones.put(z.getId(), z));
    return campaign;
  }

  public CampaignDto toDto() {
    var dto = CampaignDto.newBuilder();
    dto.setId(id.toString());
    dto.setName(name);
    if (hasUsedFogToolbar != null) {
      dto.setHasUsedFogToolbar(BoolValue.of(hasUsedFogToolbar));
    }
    dto.setProperties(campaignProperties.toDto());
    if (exportLocation != null) {
      dto.setExportLocation(exportLocation.toDto());
    }
    dto.putAllExportSettings(exportSettings);
    dto.setMacroButtonLastIndex(macroButtonLastIndex);
    dto.setGmMacroButtonLastIndex(gmMacroButtonLastIndex);
    dto.addAllMacroButtonProperties(
        macroButtonProperties.stream()
            .map(MacroButtonProperties::toDto)
            .collect(Collectors.toList()));
    dto.addAllZones(zones.values().stream().map(Zone::toDto).collect(Collectors.toList()));
    // gmMacroButtonProperties is null if you are loading an old campaign file < 1.5.6
    if (gmMacroButtonProperties != null) {
      dto.addAllGmMacroButtonProperties(
          gmMacroButtonProperties.stream()
              .map(MacroButtonProperties::toDto)
              .collect(Collectors.toList()));
    }
    return dto.build();
  }

  /**
   * Perform a series of rename operations on the token types.
   *
   * @param rename List of rename operations to perform in order.
   */
  public void renameTokenTypes(@Nonnull List<RenamePropertyType> rename) {
    var working = new ArrayList<>(rename);
    rename.forEach(
        r -> {
          compressRenames(r, working);
        });
    rename.forEach(
        r -> {
          renameTokenTypes(r.from, r.to);
        });
  }

  /**
   * This method ensures that only a single rename occurs, e.g. if we have the renames A -> B B -> C
   *
   * <p>We transform this into A -> C B -> C
   *
   * @param rename the renaming operation to apply.
   * @param working the queued renaming operations.
   */
  private void compressRenames(RenamePropertyType rename, ArrayList<RenamePropertyType> working) {
    for (int i = 0; i < working.size(); i++) {
      var w = working.get(i);
      if (w.to.equals(rename.from)) {
        working.set(i, new RenamePropertyType(w.from, rename.to));
      }
    }
    // Finally, add the rename operation to the end of the list
    working.add(rename);
  }

  /**
   * Renames the token types on existing tokens in the campaign.
   *
   * @param oldName the token type to rename from.
   * @param newName the token type to rename to.
   */
  public void renameTokenTypes(@Nonnull String oldName, @Nonnull String newName) {
    for (Zone zone : getZones()) {
      zone.getAllTokens()
          .forEach(
              t -> {
                if (oldName.equals(t.getPropertyType())) {
                  t.setPropertyType(newName);
                  zone.putToken(t);
                }
              });
    }
  }
}
