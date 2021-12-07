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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.rptools.CaseInsensitiveHashMap;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.transferable.TokenTransferData;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.ZoneRenderer.SelectionSet;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This object represents the placeable objects on a map. For example an icon that represents a
 * character would exist as an {@link Asset} (the image itself) and a location and scale.
 */

// Lee: made tokens cloneable
public class Token extends BaseModel implements Cloneable {

  private static final Logger log = LogManager.getLogger(Token.class);

  /** The unique GUID of the token. */
  private GUID id = new GUID();

  public static final String FILE_EXTENSION = "rptok";
  public static final String FILE_THUMBNAIL = "thumbnail";
  public static final String FILE_THUMBNAIL_LARGE = "thumbnail_large";

  public static final String NAME_USE_FILENAME = "Use Filename";
  public static final String NAME_USE_CREATURE = "Use \"Creature\"";

  public static final String NUM_INCREMENT = "Increment";
  public static final String NUM_RANDOM = "Random";

  public static final String NUM_ON_NAME = "Name";
  public static final String NUM_ON_GM = "GM Name";
  public static final String NUM_ON_BOTH = "Both";

  public static final String LIB_TOKEN_PREFIX = "lib:";

  private boolean beingImpersonated = false;
  private GUID exposedAreaGUID;

  /** the only way to make Gson apply strict evaluation to JsonObjects, apparently. see #2396 */
  private static final TypeAdapter<JsonObject> strictGsonObjectAdapter =
      new Gson().getAdapter(JsonObject.class);

  public boolean getAllowURIAccess() {
    if (allowURIAccess && !isLibToken()) {
      allowURIAccess = false;
    }
    return allowURIAccess;
  }

  public void setAllowURIAccess(boolean allowURIAccess) {
    if (isLibToken()) {
      this.allowURIAccess = allowURIAccess;
    } else {
      this.allowURIAccess = false;
    }
  }

  public boolean isLibToken() {
    return isValidLibTokenName(name);
  }

  public static boolean isValidLibTokenName(String name) {
    return name.toLowerCase().startsWith(LIB_TOKEN_PREFIX);
  }

  public enum TokenShape {
    TOP_DOWN(),
    CIRCLE(),
    SQUARE(),
    FIGURE();

    private final String displayName;

    TokenShape() {
      displayName = I18N.getString("Token.TokenShape." + name());
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  /** Type of character: PC or NPC. */
  public enum Type {
    PC(),
    NPC();

    private final String displayName;

    Type() {
      displayName = I18N.getString("Token.Type." + name());
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  /** Type of update for the token. */
  public enum Update {
    setState,
    setAllStates,
    setPropertyType,
    setPC,
    setNPC,
    setLayer,
    setLayerShape,
    setShape,
    setSnapToScale,
    setSnapToGrid,
    setSnapToGridAndXY,
    setFootprint,
    setProperty,
    resetProperty,
    setZOrder,
    setFacing,
    clearAllOwners,
    setOwnedByAll,
    addOwner,
    setScaleX,
    setScaleY,
    setScaleXY,
    setNotes,
    setGMNotes,
    saveMacro,
    saveMacroList,
    deleteMacro,
    setX,
    setY,
    setXY,
    setHaloColor,
    setLabel,
    setName,
    setGMName,
    setVisible,
    setVisibleOnlyToOwner,
    setIsAlwaysVisible,
    setTokenOpacity,
    setTerrainModifier,
    setTerrainModifierOperation,
    setTerrainModifiersIgnored,
    setVBL,
    setImageAsset,
    setPortraitImage,
    setCharsheetImage,
    setLayout,
    clearLightSources,
    removeLightSource,
    addLightSource,
    setHasSight,
    setSightType,
    flipX,
    flipY,
    flipIso,
    setSpeechName
  }

  public static final Comparator<Token> NAME_COMPARATOR =
      (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());

  private final Map<String, MD5Key> imageAssetMap;
  private String currentImageAsset;

  private int x;
  private int y;
  private int z;

  private int anchorX;
  private int anchorY;

  private double sizeScale = 1;

  private int lastX;
  private int lastY;
  private Path<? extends AbstractPoint> lastPath;

  // Lee: for use in added path calculations
  private transient ZonePoint tokenOrigin = null;
  private boolean snapToScale = true; // Whether the scaleX and scaleY represent snap-to-grid
  // measurements

  // These are the original image width and height
  private int width;
  private int height;
  private int isoWidth;
  private int isoHeight;

  private double scaleX = 1;
  private double scaleY = 1;

  private Map<String, GUID> sizeMap;

  private boolean snapToGrid = true; // Whether the token snaps to the current grid or is free
  // floating

  private boolean isVisible = true;
  private boolean visibleOnlyToOwner = false;

  private int vblColorSensitivity = -1;
  private int alwaysVisibleTolerance = 2; // Default for # of regions (out of 9) that must be seen
  // before token is shown over FoW
  private boolean isAlwaysVisible = false; // Controls whether a Token is shown over VBL
  private Area vbl;

  private String name;
  private Set<String> ownerList;

  private int ownerType;

  private static final int OWNER_TYPE_ALL = 1;
  private static final int OWNER_TYPE_LIST = 0;

  private String tokenShape;
  private String tokenType;
  private String layer;
  private transient Zone.Layer actualLayer;

  private String propertyType = Campaign.DEFAULT_TOKEN_PROPERTY_TYPE;

  private Integer facing = null;

  private Integer haloColorValue;
  private transient Color haloColor;

  private Integer visionOverlayColorValue;
  private transient Color visionOverlayColor;

  // Jamz: allow token alpha channel modification
  private float tokenOpacity = 1.0f;

  private String speechName = "";

  /** Terrain Modifier Operations */
  public enum TerrainModifierOperation {
    NONE(), // Default, no terrain modifications to pathfinding cost
    MULTIPLY(), // All tokens with this type are added together and multiplied against the Cell cost
    ADD(), // All tokens with this type are added together and added to the cell cost
    BLOCK(), // Movement through tokens with this type are blocked just as if they had VBL
    FREE(); // Any cell with a token of this type in it has ALL movement costs removed

    private final String displayName;

    TerrainModifierOperation() {
      displayName = I18N.getString("Token.TerrainModifierOperation." + name());
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  // Jamz: modifies A* cost of other tokens
  private double terrainModifier = 0.0d;
  private TerrainModifierOperation terrainModifierOperation = TerrainModifierOperation.NONE;
  private Set<TerrainModifierOperation> terrainModifiersIgnored =
      new HashSet<>(Collections.singletonList(TerrainModifierOperation.NONE));

  private boolean isFlippedX;
  private boolean isFlippedY;
  private Boolean isFlippedIso;

  private MD5Key charsheetImage;
  private MD5Key portraitImage;

  private List<AttachedLightSource> lightSourceList;
  private String sightType;
  private boolean hasSight;
  private Boolean hasImageTable;
  private String imageTableName;

  private String label;

  /** The notes that are displayed for this token. */
  private String notes;

  private String gmNotes;

  private String gmName;

  /**
   * A state properties for this token. This allows state to be added that can change appearance of
   * the token.
   */
  private Map<String, Object> state;

  /** Properties */
  // I screwed up. propertyMap was HashMap<String,Object> in pre-1.3b70 (?)
  // and became a CaseInsensitiveHashMap<Object> thereafter. So in order to
  // be able to load old tokens, we need to read in the original data type and
  // copy the elements into the new data type. But because the name didn't
  // change (that was the screw up) we have special code in readResolve() to
  // help XStream move the data around.
  private Map<String, Object> propertyMap; // 1.3b77 and earlier

  private CaseInsensitiveHashMap<Object> propertyMapCI;

  private Map<String, String> macroMap;
  private Map<Integer, MacroButtonProperties> macroPropertiesMap;

  private Map<String, String> speechMap;

  private HeroLabData heroLabData;

  private boolean allowURIAccess = false;

  /**
   * Constructor from another token, with the option to keep the token id
   *
   * @param token the token to copy
   * @param keepId should the Id be kept
   */
  public Token(Token token, boolean keepId) {
    this(token);
    if (keepId) {
      this.setId(token.getId());
    }
  }

  /**
   * Constructor from another token. The token id is not kept.
   *
   * @param token the token to copy
   */
  public Token(Token token) {
    this(token.name, token.getImageAssetId());
    currentImageAsset = token.currentImageAsset;

    x = token.x;
    y = token.y;
    z = token.z;

    // These properties shouldn't be transferred, they are more transient and relate to token
    // history, not to new tokens
    // lastX = token.lastX;
    // lastY = token.lastY;
    // lastPath = token.lastPath;

    snapToScale = token.snapToScale;
    width = token.width;
    height = token.height;
    isoWidth = token.isoWidth;
    isoHeight = token.isoHeight;
    scaleX = token.scaleX;
    scaleY = token.scaleY;
    facing = token.facing;
    tokenShape = token.tokenShape;
    tokenType = token.tokenType;
    haloColorValue = token.haloColorValue;

    snapToGrid = token.snapToGrid;
    isVisible = token.isVisible;
    visibleOnlyToOwner = token.visibleOnlyToOwner;

    vblColorSensitivity = token.vblColorSensitivity;
    alwaysVisibleTolerance = token.alwaysVisibleTolerance;
    isAlwaysVisible = token.isAlwaysVisible;
    vbl = token.vbl;

    name = token.name;
    notes = token.notes;
    gmName = token.gmName;
    gmNotes = token.gmNotes;
    label = token.label;

    isFlippedX = token.isFlippedX;
    isFlippedY = token.isFlippedY;
    isFlippedIso = token.isFlippedIso;

    layer = token.layer;

    visionOverlayColor = token.visionOverlayColor;

    charsheetImage = token.charsheetImage;
    portraitImage = token.portraitImage;
    anchorX = token.anchorX;
    anchorY = token.anchorY;
    sizeScale = token.sizeScale;
    sightType = token.sightType;
    hasSight = token.hasSight;
    propertyType = token.propertyType;
    hasImageTable = token.hasImageTable;
    imageTableName = token.imageTableName;

    if (isoWidth == 0) {
      isoWidth = width;
    }

    if (isoHeight == 0) {
      isoHeight = height;
    }

    ownerType = token.ownerType;
    if (token.ownerList != null) {
      ownerList = new HashSet<String>();
      ownerList.addAll(token.ownerList);
    }
    if (token.lightSourceList != null) {
      lightSourceList = new ArrayList<AttachedLightSource>(token.lightSourceList);
    }
    if (token.state != null) {
      state.putAll(token.state);
    }
    if (token.propertyMapCI != null) {
      getPropertyMap().clear();
      getPropertyMap().putAll(token.propertyMapCI);
    }
    if (token.macroPropertiesMap != null) { // Deep copy of the macros
      macroPropertiesMap = new HashMap<>(token.macroPropertiesMap.size());
      token.macroPropertiesMap.forEach(
          (key, value) ->
              macroPropertiesMap.put(key, new MacroButtonProperties(this, key, value, false)));
    }
    // convert old-style macros
    if (token.macroMap != null) {
      macroMap = new HashMap<String, String>(token.macroMap);
      loadOldMacros();
    }
    if (token.speechMap != null) {
      speechMap = new HashMap<String, String>(token.speechMap);
    }
    if (token.imageAssetMap != null) {
      imageAssetMap.putAll(token.imageAssetMap);
    }
    if (token.sizeMap != null) {
      sizeMap = new HashMap<>(token.sizeMap);
    }

    exposedAreaGUID = token.exposedAreaGUID;

    heroLabData = token.heroLabData;
    tokenOpacity = token.tokenOpacity;
    terrainModifier = token.terrainModifier;
    terrainModifierOperation = token.terrainModifierOperation;

    if (token.terrainModifiersIgnored != null) {
      terrainModifiersIgnored = new HashSet<>(token.terrainModifiersIgnored);
    }

    speechName = token.speechName;
    allowURIAccess = token.allowURIAccess;
  }

  public Token() {
    imageAssetMap = new HashMap<String, MD5Key>();
  }

  public Token(MD5Key assetID) {
    this("", assetID);
  }

  public Token(String name, MD5Key assetId) {
    this.name = name;
    state = new HashMap<String, Object>();
    imageAssetMap = new HashMap<String, MD5Key>();

    // NULL key is the default
    imageAssetMap.put(null, assetId);

    // convert old-style macros
    if (macroMap != null) {
      loadOldMacros();
    }
  }

  /**
   * This token object has just been imported on a map and needs to have most of its internal data
   * wiped clean. This prevents a token from being imported that makes use of the wrong property
   * types, vision types, ownership, macros, and so on. Basically anything related to the
   * presentation of the token on-screen + the two notes fields is kept. Note that the sightType is
   * set to the campaign's default sight type, and the property type is not changed at all. This
   * will usually be correct since the default sight is what most tokens have and the property type
   * is probably specific to the campaign -- hopefully the properties were set up before the
   * token/map was imported.
   */
  public void imported() {
    // anchorX, anchorY?
    beingImpersonated = false;
    // hasSight?
    // height?
    lastPath = null;
    lastX = lastY = 0;
    // lightSourceList?
    macroMap = null;
    // macroPropertiesMap = null;
    ownerList = null;
    // propertyMapCI = null;
    // propertyType = "Basic";
    /**
     * Lee: why shouldn't propertyType be set to what the framework uses? In case of multiple
     * propertyType, give a choice; or incorporate in the Campaign Properties window a marker for
     * what is default for new tokens.
     */
    propertyType = getPropertyType();

    /**
     * Jamz: Like propertyType, why shouldn't sight be kept if it matches exists? Many creatures
     * with DarkVision get reset and it makes it painful. I'm turning off this reset for now. If
     * there are complaints/reasons, maybe the Import Dialog needs to be expanded to include
     * checkboxes for these items...
     */

    // Try and silently catch any errors if there is an issue with sightType...
    try {
      if (!MapTool.getCampaign().getCampaignProperties().getSightTypeMap().containsKey(sightType)) {
        sightType = MapTool.getCampaign().getCampaignProperties().getDefaultSightType();
      }
    } catch (Exception e) {
      sightType = MapTool.getCampaign().getCampaignProperties().getDefaultSightType();
      e.printStackTrace();
    }
  }

  public void setHasSight(boolean hasSight) {
    this.hasSight = hasSight;
  }

  public void setHasImageTable(boolean hasImageTable) {
    if (hasImageTable) {
      this.hasImageTable = true;
    } else {
      this.hasImageTable = null;
    }
  }

  public void setImageTableName(String imageTableName) {
    this.imageTableName = imageTableName;
  }

  public void setWidth(int width) {
    if (isFlippedIso()) {
      isoWidth = width;
    } else {
      this.width = width;
    }
  }

  public void setHeight(int height) {
    if (isFlippedIso()) {
      isoHeight = height;
    } else {
      this.height = height;
    }
  }

  public int getWidth() {
    if (isFlippedIso() && isoWidth != 0) {
      return isoWidth;
    } else {
      return width;
    }
  }

  public int getHeight() {
    if (isFlippedIso() && isoHeight != 0) {
      return isoHeight;
    } else {
      return height;
    }
  }

  public boolean isMarker() {
    return isStamp()
        && (!StringUtil.isEmpty(notes) || !StringUtil.isEmpty(gmNotes) || portraitImage != null);
  }

  public String getPropertyType() {
    return propertyType;
  }

  public void setPropertyType(String propertyType) {
    this.propertyType = propertyType;
  }

  public String getGMNotes() {
    if (MapTool.getPlayer().isGM() || MapTool.getParser().isMacroTrusted()) {
      return gmNotes;
    } else {
      return "";
    }
  }

  public void setGMNotes(String notes) {
    gmNotes = notes;
  }

  public String getGMName() {
    if (MapTool.getPlayer().isGM() || MapTool.getParser().isMacroTrusted()) {
      return gmName;
    } else {
      return "";
    }
  }

  public void setGMName(String name) {
    gmName = name;
  }

  public boolean hasHalo() {
    return haloColorValue != null;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setHaloColor(Color color) {
    if (color != null) {
      haloColorValue = color.getRGB();
    } else {
      haloColorValue = null;
    }
    haloColor = color;
  }

  public Color getHaloColor() {
    if (haloColor == null && haloColorValue != null) {
      haloColor = new Color(haloColorValue);
    }
    return haloColor;
  }

  public float getTokenOpacity() {
    if (tokenOpacity <= 0.0f) {
      tokenOpacity = 1.0f;
    }

    return tokenOpacity;
  }

  /**
   * Set the token opacity from a string trimmed to [0.05f, 1.0f]
   *
   * @param alpha the String of the opacity value.
   * @return the float of the opacity
   */
  public float setTokenOpacity(String alpha) {
    return setTokenOpacity(Float.parseFloat(alpha));
  }

  /**
   * Set the token opacity from a float trimmed to [0.05f, 1.0f]
   *
   * @param alpha the float of the opacity.
   * @return the float of the opacity trimmed.
   */
  public float setTokenOpacity(float alpha) {
    if (alpha > 1.0f) {
      alpha = 1.0f;
    }
    if (alpha <= 0.0f) {
      alpha = 0.05f;
    }

    tokenOpacity = alpha;

    return tokenOpacity;
  }

  /**
   * Returns the name to be displayed in speech and thought bubbles.
   *
   * @return the name to be displayed in speech and thought bubbles/
   */
  public String getSpeechName() {
    return speechName;
  }

  /**
   * Sets the name to be displayed in speech and thought bubbles.
   *
   * @param name the name to be displayed.
   */
  public void setSpeechName(String name) {
    speechName = name;
  }

  public double getTerrainModifier() {
    return terrainModifier;
  }

  public double setTerrainModifier(double modifier) {
    terrainModifier = modifier;
    return terrainModifier;
  }

  public TerrainModifierOperation getTerrainModifierOperation() {
    // This should only happen on existing campaigns. For those tokens,
    // the default was a multiplier of 1.0f so we will set those to 0 and operation NONE
    if (terrainModifierOperation == null) {
      if (terrainModifier != 1) {
        terrainModifierOperation = TerrainModifierOperation.MULTIPLY;
      } else {
        terrainModifier = 0.0d;
        terrainModifierOperation = TerrainModifierOperation.NONE;
      }
    }

    return terrainModifierOperation;
  }

  public void setTerrainModifierOperation(TerrainModifierOperation terrainModifierOperation) {
    this.terrainModifierOperation = terrainModifierOperation;
  }

  public Set<TerrainModifierOperation> getTerrainModifiersIgnored() {
    if (terrainModifiersIgnored == null) {
      terrainModifiersIgnored = new HashSet<>();
    }

    if (terrainModifiersIgnored.isEmpty()) {
      terrainModifiersIgnored.add(TerrainModifierOperation.NONE);
    }

    return terrainModifiersIgnored;
  }

  public void setTerrainModifiersIgnored(Set<TerrainModifierOperation> terrainModifiersIgnored) {
    this.terrainModifiersIgnored = terrainModifiersIgnored;

    if (this.terrainModifiersIgnored.contains(TerrainModifierOperation.NONE)) {
      terrainModifiersIgnored.clear();
      this.terrainModifiersIgnored.add(TerrainModifierOperation.NONE);
    }
  }

  public boolean isObjectStamp() {
    return getLayer() == Zone.Layer.OBJECT;
  }

  public boolean isGMStamp() {
    return getLayer() == Zone.Layer.GM;
  }

  public boolean isBackgroundStamp() {
    return getLayer() == Zone.Layer.BACKGROUND;
  }

  public boolean isOnTokenLayer() {
    return getLayer() == Zone.Layer.TOKEN;
  }

  public boolean isStamp() {
    switch (getLayer()) {
      case BACKGROUND:
      case OBJECT:
      case GM:
        return true;
      default:
        break;
    }
    return false;
  }

  public boolean isToken() {
    return getLayer() == Zone.Layer.TOKEN;
  }

  public TokenShape getShape() {
    try {
      return tokenShape != null ? TokenShape.valueOf(tokenShape) : TokenShape.SQUARE; // TODO:
      // make
      // this
      // a psf
    } catch (IllegalArgumentException iae) {
      return TokenShape.SQUARE;
    }
  }

  public void setShape(TokenShape type) {
    this.tokenShape = type.name();
  }

  public Type getType() {
    try {
      // TODO: make this a psf
      return tokenType != null ? Type.valueOf(tokenType) : Type.NPC;
    } catch (IllegalArgumentException iae) {
      tokenType = Type.NPC.name();
      return Type.NPC;
    }
  }

  /**
   * Sets the token's type. Sets hasSight to true if the new type is PC.
   *
   * @param type The new type
   */
  public void setType(Type type) {
    tokenType = type.name();
    if (type == Type.PC) {
      hasSight = true;
    }
  }

  public Zone.Layer getLayer() {
    try {
      if (actualLayer == null) {
        actualLayer = layer != null ? Zone.Layer.valueOf(layer) : Zone.Layer.TOKEN;
      }
      return actualLayer;
    } catch (IllegalArgumentException iae) {
      return Zone.Layer.TOKEN;
    }
  }

  public void setLayer(Zone.Layer layer) {
    this.layer = layer.name();
    actualLayer = layer;
  }

  public boolean hasFacing() {
    return facing != null;
  }

  public void setFacing(Integer facing) {
    while (facing != null && (facing > 180 || facing < -179)) {
      facing += facing > 180 ? -360 : 0;
      facing += facing < -179 ? 360 : 0;
    }
    this.facing = facing;
  }

  /**
   * Facing is in the map space where 0 degrees is along the X axis to the right and proceeding CCW
   * for positive angles.
   *
   * <p>Round/Square tokens that have no facing set, return null. Top Down tokens default to -90.
   *
   * @return null or angle in degrees
   */
  public Integer getFacing() {
    return facing;
  }

  /**
   * This returns the rotation of the facing of the token from the default facing of down or -90.
   * Positive for CW and negative for CCW.
   *
   * @return angle in degrees
   */
  public Integer getFacingInDegrees() {
    if (facing == null) {
      return 0;
    } else {
      return -(facing + 90);
    }
  }

  public Integer getFacingInRealDegrees() {
    if (facing == null) {
      return 270;
    }

    if (facing >= 0) {
      return facing;
    } else {
      return facing + 360;
    }
  }

  public boolean getHasSight() {
    return hasSight;
  }

  public boolean getHasImageTable() {
    if (hasImageTable != null) {
      return hasImageTable;
    }
    return false;
  }

  public String getImageTableName() {
    return imageTableName;
  }

  public void addLightSource(LightSource source, Direction direction) {
    if (lightSourceList == null) {
      lightSourceList = new ArrayList<AttachedLightSource>();
    }
    if (!lightSourceList.contains(source)) {
      lightSourceList.add(new AttachedLightSource(source, direction));
    }
  }

  public void removeLightSourceType(LightSource.Type lightType) {
    if (lightSourceList != null) {
      for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext(); ) {
        AttachedLightSource als = i.next();
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource != null && lightSource.getType() == lightType) {
          i.remove();
        }
      }
    }
  }

  public void removeGMAuras() {
    if (lightSourceList != null) {
      for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext(); ) {
        AttachedLightSource als = i.next();
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource != null) {
          List<Light> lights = lightSource.getLightList();
          for (Light light : lights) {
            if (light != null && light.isGM()) {
              i.remove();
            }
          }
        }
      }
    }
  }

  public void removeOwnerOnlyAuras() {
    if (lightSourceList != null) {
      for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext(); ) {
        AttachedLightSource als = i.next();
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource != null) {
          List<Light> lights = lightSource.getLightList();
          for (Light light : lights) {
            if (light.isOwnerOnly()) {
              i.remove();
            }
          }
        }
      }
    }
  }

  public boolean hasOwnerOnlyAuras() {
    if (lightSourceList != null) {
      for (AttachedLightSource als : lightSourceList) {
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource != null) {
          List<Light> lights = lightSource.getLightList();
          for (Light light : lights) {
            if (light.isOwnerOnly()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public boolean hasGMAuras() {
    if (lightSourceList != null) {
      for (AttachedLightSource als : lightSourceList) {
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource != null) {
          List<Light> lights = lightSource.getLightList();
          for (Light light : lights) {
            if (light.isGM()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public boolean hasLightSourceType(LightSource.Type lightType) {
    if (lightSourceList != null) {
      for (AttachedLightSource als : lightSourceList) {
        LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
        if (lightSource != null && lightSource.getType() == lightType) {
          return true;
        }
      }
    }
    return false;
  }

  public void removeLightSource(LightSource source) {
    if (lightSourceList == null) {
      return;
    }
    lightSourceList.removeIf(
        als ->
            als != null
                && als.getLightSourceId() != null
                && als.getLightSourceId().equals(source.getId()));
  }

  /** Clear the lightSourceList */
  public void clearLightSources() {
    if (lightSourceList == null) {
      return;
    }
    lightSourceList = null;
  }

  public boolean hasLightSource(LightSource source) {
    if (lightSourceList == null) {
      return false;
    }
    for (AttachedLightSource als : lightSourceList) {
      if (als != null
          && als.getLightSourceId() != null
          && als.getLightSourceId().equals(source.getId())) {
        return true;
      }
    }
    return false;
  }

  /** @return false if lightSourceList is null or empty, and true otherwise */
  public boolean hasLightSources() {
    return lightSourceList != null && !lightSourceList.isEmpty();
  }

  public List<AttachedLightSource> getLightSources() {
    return lightSourceList != null
        ? Collections.unmodifiableList(lightSourceList)
        : new LinkedList<AttachedLightSource>();
  }

  public synchronized void addOwner(String playerId) {
    ownerType = OWNER_TYPE_LIST;
    if (ownerList == null) {
      ownerList = new HashSet<String>();
    }
    ownerList.add(playerId);
  }

  /** @return true if the token is owned by all or has explicit owners. */
  public synchronized boolean hasOwners() {
    return ownerType == OWNER_TYPE_ALL || (ownerList != null && !ownerList.isEmpty());
  }

  public synchronized void removeOwner(String playerId) {
    ownerType = OWNER_TYPE_LIST;
    if (ownerList == null) {
      return;
    }
    ownerList.remove(playerId);
    if (ownerList.size() == 0) {
      ownerList = null;
    }
  }

  public synchronized void setOwnedByAll(boolean ownedByAll) {
    if (ownedByAll) {
      ownerType = OWNER_TYPE_ALL;
      ownerList = null;
    } else {
      ownerType = OWNER_TYPE_LIST;
    }
  }

  /** @return the set of owner names of the token. */
  public Set<String> getOwners() {
    return ownerList != null ? Collections.unmodifiableSet(ownerList) : new HashSet<>();
  }

  public boolean isOwnedByAll() {
    return ownerType == OWNER_TYPE_ALL;
  }

  public synchronized void clearAllOwners() {
    ownerList = null;
  }

  public synchronized boolean isOwner(String playerId) {
    return (ownerType == OWNER_TYPE_ALL || (ownerList != null && ownerList.contains(playerId)));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Token)) {
      return false;
    }
    return id.equals(((Token) o).id);
  }

  public void setZOrder(int z) {
    this.z = z;
  }

  public int getZOrder() {
    return z;
  }

  /**
   * Set the name of this token to the provided string.
   *
   * @param name the new name of the token
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Validate a token name by testing for duplicates. There is a potential exposure of information
   * to the player in this method: through repeated attempts to name a token they own to another
   * name, they could determine which token names the GM is already using. Fortunately, the
   * showError() call makes this extremely unlikely due to the interactive nature of a failure.
   *
   * @param name the new name of the token
   * @throws ParserException thrown if a token has the same name
   */
  public void validateName(String name) throws ParserException {
    if (!MapTool.getPlayer().isGM() && !MapTool.getParser().isMacroTrusted()) {
      Zone curZone = getZoneRenderer().getZone();
      List<Token> tokensList = curZone.getTokens();

      for (Token token : tokensList) {
        String curTokenName = token.getName();
        if (curTokenName.equalsIgnoreCase(name) && !(token.equals(this))) {
          MapTool.showError(I18N.getText("Token.error.unableToRename", name));
          throw new ParserException(I18N.getText("Token.error.unableToRename", name));
        }
      }
    }
  }

  public MD5Key getImageAssetId() {
    MD5Key assetId = imageAssetMap.get(currentImageAsset);
    if (assetId == null) {
      assetId = imageAssetMap.get(null); // default image
    }
    return assetId;
  }

  /**
   * Store the token image, and set the native Width and Height.
   *
   * @param name the name of the image.
   * @param assetId the asset MD5Key.
   */
  public void setImageAsset(String name, MD5Key assetId) {
    imageAssetMap.put(name, assetId);

    BufferedImage image = ImageManager.getImageAndWait(assetId);
    setWidth(image.getWidth(null));
    setHeight(image.getHeight(null));
  }

  public void setImageAsset(String name) {
    currentImageAsset = name;
  }

  public Set<MD5Key> getAllImageAssets() {
    Set<MD5Key> assetSet = new HashSet<MD5Key>(imageAssetMap.values());
    assetSet.add(charsheetImage);
    assetSet.add(portraitImage);

    if (heroLabData != null && heroLabData.getAllAssetIDs() != null) {
      assetSet.addAll(heroLabData.getAllAssetIDs());
    }

    assetSet.remove(null); // Clean up from any null values from above
    return assetSet;
  }

  public MD5Key getPortraitImage() {
    return portraitImage;
  }

  public void setPortraitImage(MD5Key image) {
    portraitImage = image;
  }

  public MD5Key getCharsheetImage() {
    return charsheetImage;
  }

  public void setCharsheetImage(MD5Key charsheetImage) {
    this.charsheetImage = charsheetImage;
  }

  public GUID getId() {
    return id;
  }

  public ZoneRenderer getZoneRenderer() { // Returns the ZoneRenderer the token is on
    ZoneRenderer zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    Token token = zoneRenderer.getZone().getToken(getId());

    if (token == null) {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        token = zr.getZone().getToken(getId());
        if (token != null) {
          return zr;
        }
      }
    }
    return zoneRenderer;
  }

  public void setId(GUID id) {
    this.id = id;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setX(int x) {
    lastX = this.x;
    this.x = x;
  }

  public void setY(int y) {
    lastY = this.y;
    this.y = y;
  }

  // Lee: added functions necessary for path computations
  public void setOriginPoint(ZonePoint p) {
    tokenOrigin = p;
  }

  public ZonePoint getOriginPoint() {
    if (tokenOrigin == null) {
      tokenOrigin = new ZonePoint(getX(), getY());
    }

    return tokenOrigin;
  }

  /*
   * Lee: changing this to apply new X and Y values (as end point) for the token BEFORE its path is
   * computed. Path to be saved will be computed here instead of in ZoneRenderer
   */
  public void applyMove(
      SelectionSet set,
      Path<? extends AbstractPoint> followerPath,
      int xOffset,
      int yOffset,
      Token keyToken,
      int cellOffX,
      int cellOffY) {
    setX(x + xOffset);
    setY(y + yOffset);
    lastPath =
        followerPath != null
            ? followerPath.derive(
                set,
                keyToken,
                this,
                cellOffX,
                cellOffY,
                getOriginPoint(),
                new ZonePoint(getX(), getY()))
            : null;
  }

  public void setLastPath(Path<? extends AbstractPoint> path) {
    lastPath = path;
  }

  public int getLastY() {
    return lastY;
  }

  public int getLastX() {
    return lastX;
  }

  public Path<? extends AbstractPoint> getLastPath() {
    return lastPath;
  }

  public double getScaleX() {
    return scaleX;
  }

  public double getScaleY() {
    return scaleY;
  }

  public void setScaleX(double scaleX) {
    this.scaleX = scaleX;
  }

  public void setScaleY(double scaleY) {
    this.scaleY = scaleY;
  }

  /** @return Returns the snapScale. */
  public boolean isSnapToScale() {
    return snapToScale;
  }

  /** @param snapScale The snapScale to set. */
  public void setSnapToScale(boolean snapScale) {
    this.snapToScale = snapScale;
  }

  public void setVisible(boolean visible) {
    this.isVisible = visible;
  }

  /** @return isVisible */
  public boolean isVisible() {
    return isVisible;
  }

  /** @return the visibleOnlyToOwner */
  public boolean isVisibleOnlyToOwner() {
    return visibleOnlyToOwner;
  }

  /** @param visibleOnlyToOwner the visibleOnlyToOwner to set */
  public void setVisibleOnlyToOwner(boolean visibleOnlyToOwner) {
    this.visibleOnlyToOwner = visibleOnlyToOwner;
  }

  public boolean isAlwaysVisible() {
    return isAlwaysVisible;
  }

  public void setAlwaysVisibleTolerance(int tolerance) {
    if (tolerance < 1) {
      tolerance = 1;
    }

    if (tolerance > 9) {
      tolerance = 9;
    }

    alwaysVisibleTolerance = tolerance;
  }

  public int getAlwaysVisibleTolerance() {
    if (alwaysVisibleTolerance <= 0) {
      return 2;
    } else {
      return alwaysVisibleTolerance;
    }
  }

  public void setColorSensitivity(int tolerance) {
    vblColorSensitivity = tolerance;
  }

  public int getColorSensitivity() {
    return vblColorSensitivity;
  }

  /**
   * Set the VBL of the token. If vbl null, set vblAplphaSensitivity to -1.
   *
   * @param vbl the VBL to set.
   */
  public void setVBL(Area vbl) {
    this.vbl = vbl;
    if (vbl == null) {
      vblColorSensitivity = -1;
    }
  }

  /**
   * Return the vbl area of the token
   *
   * @return the current VBL of the token
   */
  public Area getVBL() {
    return vbl;
  }

  public Area getTransformedVBL() {
    return getTransformedVBL(vbl);
  }

  /**
   * This method returns the vbl stored on the token with AffineTransformations applied for scale,
   * position, rotation, &amp; flipping.
   *
   * @param areaToTransform transformations to apply to the VBL
   * @return the transformed VBL for the token
   * @author Jamz
   * @since 1.4.1.5
   */
  public Area getTransformedVBL(Area areaToTransform) {
    if (areaToTransform == null) {
      return null;
    }

    Rectangle footprintBounds = getBounds(MapTool.getFrame().getCurrentZoneRenderer().getZone());
    Dimension imgSize = new Dimension(getWidth(), getHeight());
    SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);

    // Lets account for ISO images
    double iso_ho = 0;
    if (getShape() == TokenShape.FIGURE) {
      double th = getHeight() * (double) footprintBounds.width / getWidth();
      iso_ho = footprintBounds.height - th;
      footprintBounds =
          new Rectangle(
              footprintBounds.x, footprintBounds.y - (int) iso_ho, footprintBounds.width, (int) th);
    }

    // Lets figure in offset if image is not free size/native size aka snapToScale
    int offsetx = 0;
    int offsety = 0;
    if (isSnapToScale()) {
      offsetx =
          imgSize.width < footprintBounds.width ? (footprintBounds.width - imgSize.width) / 2 : 0;
      offsety =
          imgSize.height < footprintBounds.height
              ? (footprintBounds.height - imgSize.height) / 2
              : 0;
    }
    double tx = footprintBounds.x + offsetx;
    double ty = footprintBounds.y + offsety + iso_ho;

    // Apply the coordinate translation
    AffineTransform atArea = AffineTransform.getTranslateInstance(tx, ty);

    double scalerX = isSnapToScale() ? ((double) imgSize.width) / getWidth() : scaleX;
    double scalerY = isSnapToScale() ? ((double) imgSize.height) / getHeight() : scaleY;

    // Apply the rotation transformation...
    if (getShape() == Token.TokenShape.TOP_DOWN && hasFacing()) {
      // Find the center x,y coords of the rectangle
      double rx = getWidth() / 2.0 * scalerX - getAnchor().getX();
      double ry = getHeight() / 2.0 * scalerY - getAnchor().getY();

      atArea.concatenate(
          AffineTransform.getRotateInstance(Math.toRadians(getFacingInDegrees()), rx, ry));
    }
    // Apply the scale transformation
    atArea.concatenate(AffineTransform.getScaleInstance(scalerX, scalerY));

    // Lets account for flipped images...
    if (isFlippedX) {
      atArea.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
      atArea.concatenate(AffineTransform.getTranslateInstance(-getWidth(), 0));
    }

    if (isFlippedY) {
      atArea.concatenate(AffineTransform.getScaleInstance(1.0, -1.0));
      atArea.concatenate(AffineTransform.getTranslateInstance(0, -getHeight()));
    }

    if (isFlippedIso()) {
      return new Area(atArea.createTransformedShape(IsometricGrid.isoArea(areaToTransform)));
    }

    return new Area(atArea.createTransformedShape(areaToTransform));
  }

  /**
   * Return the existence of the token's VBL
   *
   * @return true if the token's vbl is not null, and false otherwise
   */
  public boolean hasVBL() {
    return vbl != null;
  }

  public void setIsAlwaysVisible(boolean isAlwaysVisible) {
    this.isAlwaysVisible = isAlwaysVisible;
  }

  public void toggleIsAlwaysVisible() {
    isAlwaysVisible = !isAlwaysVisible;
  }

  public String getName() {
    return name != null ? name : "";
  }

  public Rectangle getBounds(Zone zone) {
    Grid grid = zone.getGrid();
    TokenFootprint footprint = getFootprint(grid);
    Rectangle footprintBounds =
        footprint.getBounds(grid, grid.convert(new ZonePoint(getX(), getY())));

    double w = footprintBounds.width;
    double h = footprintBounds.height;

    // Sizing
    if (!isSnapToScale()) {
      w = getWidth() * getScaleX();
      h = getHeight() * getScaleY();
      if (grid.isIsometric() && getShape() == Token.TokenShape.FIGURE) {
        // Native size figure tokens need to follow iso rules
        h = (w / 2);
      }
    } else {
      w = footprintBounds.width * footprint.getScale() * sizeScale;
      h = footprintBounds.height * footprint.getScale() * sizeScale;
    }
    // Positioning
    if (!isSnapToGrid()) {
      footprintBounds.x = getX();
      footprintBounds.y = getY();
    } else {
      if (!isBackgroundStamp()) {
        // Center it on the footprint
        footprintBounds.x -= (w - footprintBounds.width) / 2;
        footprintBounds.y -= (h - footprintBounds.height) / 2;
      } else {
        // footprintBounds.x -= zone.getGrid().getSize()/2;
        // footprintBounds.y -= zone.getGrid().getSize()/2;
      }
    }
    footprintBounds.width = (int) w; // perhaps make this a double
    footprintBounds.height = (int) h;

    // Offset
    footprintBounds.x += anchorX;
    footprintBounds.y += anchorY;
    return footprintBounds;
  }

  /**
   * Returns the drag offset of the token.
   *
   * @param zone the zone where the token is dragged
   * @return a point representing the offset
   */
  public Point getDragOffset(Zone zone) {
    Grid grid = zone.getGrid();
    int offsetX, offsetY;
    if (isSnapToGrid() && grid.getCapabilities().isSnapToGridSupported()) {
      if (isBackgroundStamp() || isSnapToScale() || isOnTokenLayer()) {
        Point2D.Double centerOffset = grid.getCenterOffset();
        offsetX = getX() + (int) centerOffset.x;
        offsetY = getY() + (int) centerOffset.y;
      } else {
        Rectangle tokenBounds = getBounds(zone);
        offsetX = tokenBounds.x + tokenBounds.width / 2;
        offsetY = tokenBounds.y + tokenBounds.height / 2;
      }
    } else {
      offsetX = getX();
      offsetY = getY();
    }
    return new Point(offsetX, offsetY);
  }

  /**
   * Gets the point where the token should go, if it were to be snapped to the grid.
   *
   * @param zone the zone where the token is
   * @return the point where the token should be once snapped
   */
  public ZonePoint getSnappedPoint(Zone zone) {
    if (snapToGrid) {
      return new ZonePoint(getX(), getY());
    }
    Grid grid = zone.getGrid();
    Point2D.Double offset = getSnapToUnsnapOffset(zone);
    double newX = getX() + offset.x;
    double newY = getY() + offset.y;
    if (grid.getCapabilities().isSnapToGridSupported() || isBackgroundStamp()) {
      return grid.convert(
          grid.convert(new ZonePoint((int) Math.ceil(newX), (int) Math.ceil(newY))));
    } else {
      return new ZonePoint((int) newX, (int) newY);
    }
  }
  /**
   * Gets the point where the token should go, if it were to be unsnapped from the grid.
   *
   * @param zone the zone where the token is
   * @return the point where the token should be once unsnapped
   */
  public ZonePoint getUnsnappedPoint(Zone zone) {
    if (!snapToGrid) {
      return new ZonePoint(getX(), getY());
    } else {
      Point2D.Double offset = getSnapToUnsnapOffset(zone);
      return new ZonePoint((int) (getX() - offset.x), (int) (getY() - offset.y));
    }
  }

  /**
   * Gets the offset from the snapped position to the unsnapped position.
   *
   * @param zone the zone where the token is
   * @return a point representing the x, y offset
   */
  private Point2D.Double getSnapToUnsnapOffset(Zone zone) {
    double offsetX, offsetY;
    Rectangle tokenBounds = getBounds(zone);
    Grid grid = zone.getGrid();
    if (grid.getCapabilities().isSnapToGridSupported() || isBackgroundStamp()) {
      if (isBackgroundStamp() || isSnapToScale()) {
        TokenFootprint footprint = getFootprint(grid);
        Rectangle footprintBounds = footprint.getBounds(grid);
        double footprintOffsetX = 0;
        double footprintOffsetY = 0;
        if (!isBackgroundStamp()) {
          // Non-background tokens can have an offset from top left corner
          footprintOffsetX = tokenBounds.width - footprintBounds.width;
          footprintOffsetY = tokenBounds.height - footprintBounds.height;
        }
        double cellsX = footprintBounds.width / grid.getCellWidth();
        double cellsY = footprintBounds.height / grid.getCellHeight();
        Dimension cellOffset = grid.getCellOffset();

        offsetX = footprintOffsetX / 2.0 - cellOffset.width * cellsX;
        offsetY = footprintOffsetY / 2.0 - cellOffset.height * cellsY;
        if (grid.isHex() && "large".equalsIgnoreCase(footprint.getName())) {
          // Merudo: not sure why this special case is needed.
          offsetX = offsetX - Math.min(grid.getCellWidth(), grid.getCellHeight()) / 2;
          offsetY = offsetY - Math.min(grid.getCellWidth(), grid.getCellHeight()) / 2;
        }
      } else {
        // Free-size tokens have their position at their center, plus a grid-specific offset
        Point2D.Double centerOffset = grid.getCenterOffset();
        offsetX = tokenBounds.width / 2.0 - centerOffset.x;
        offsetY = tokenBounds.height / 2.0 - centerOffset.y;
      }
    } else {
      // Gridless non-background tokens hve their position at their center, plus half a cell
      offsetX = tokenBounds.width / 2.0 - grid.getCellWidth() / 2.0;
      offsetY = tokenBounds.height / 2.0 - grid.getCellHeight() / 2.0;
    }
    return new Point2D.Double(offsetX, offsetY);
  }

  /** @return the String of the sightType */
  public String getSightType() {
    return sightType;
  }

  public void setSightType(String sightType) {
    this.sightType = sightType;
  }

  /**
   * Calculate the foot print of a grid
   *
   * @param grid the grid to get foot print of
   * @return Returns the size.
   */
  public TokenFootprint getFootprint(Grid grid) {
    return grid.getFootprint(getSizeMap().get(grid.getClass().getName()));
  }

  public TokenFootprint setFootprint(Grid grid, TokenFootprint footprint) {
    return grid.getFootprint(getSizeMap().put(grid.getClass().getName(), footprint.getId()));
  }

  public Set<CellPoint> getOccupiedCells(Grid grid) {
    return getFootprint(grid).getOccupiedCells(grid.convert(new ZonePoint(getX(), getY())));
  }

  private Map<String, GUID> getSizeMap() {
    if (sizeMap == null) {
      sizeMap = new HashMap<>();
    }
    return sizeMap;
  }

  public boolean isSnapToGrid() {
    return snapToGrid;
  }

  public void setSnapToGrid(boolean snapToGrid) {
    this.snapToGrid = snapToGrid;
  }

  /**
   * Get a particular state property for this Token.
   *
   * @param property The name of the property being read.
   * @return Returns the current value of property.
   */
  public Object getState(String property) {
    return state.get(property);
  }

  /**
   * Set the value of state for this Token.
   *
   * @param aState The property to set.
   * @param aValue The new value for the property.
   * @return The original value of the state, if any.
   */
  public Object setState(String aState, Object aValue) {
    if (aValue == null) {
      return state.remove(aState);
    }
    return state.put(aState, aValue);
  }

  /**
   * Set the value of every state for this Token.
   *
   * @param aValue The new value for the property.
   */
  public void setAllStates(Object aValue) {
    for (Object sname : MapTool.getCampaign().getTokenStatesMap().keySet()) {
      setState(sname.toString(), aValue);
    }
  }

  public void resetProperty(String key) {
    getPropertyMap().remove(key);
  }

  public void setProperty(String key, Object value) {
    getPropertyMap().put(key, value);
  }

  public Object getProperty(String key) {
    Object value = getPropertyMap().get(key);

    // // Short name ?
    // if (value == null) {
    // for (EditTokenProperty property :
    // MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(getPropertyType())) {
    // if (property.getShortName().equals(key)) {
    // value = getPropertyMap().get(property.getShortName().toUpperCase());
    // }
    // }
    // }
    return value;
  }

  public Object getEvaluatedProperty(String key) {
    return getEvaluatedProperty(null, key);
  }

  /**
   * Returns the evaluated property corresponding to the key.
   *
   * @param resolver the variable resolver to parse code inside the property
   * @param key the key of the value
   * @return the value
   */
  public Object getEvaluatedProperty(MapToolVariableResolver resolver, String key) {
    Object val = getProperty(key);
    if (val == null) {
      // Global default ?
      List<TokenProperty> propertyList =
          MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(propertyType);
      if (propertyList != null) {
        for (TokenProperty property : propertyList) {
          if (key.equalsIgnoreCase(property.getName())) {
            val = property.getDefaultValue();
            break;
          }
        }
      }
    }
    if (val == null) {
      return "";
    }
    if (val.toString().trim().startsWith("{")) {
      /*
       * The normal Gson evaluator was too lenient in identifying JSON objects, so we had to move
       * that lower (see #1560). But we would really like to avoid the performance cost of
       * attempting to parse anything that actually is a proper JSON, so let's try a stricter
       * evaluation process here first (see #2396).
       */
      try {
        try (JsonReader reader = new JsonReader(new StringReader(val.toString()))) {
          JsonObject result = strictGsonObjectAdapter.read(reader);
          // in case of a situation like {"a": 1}{"b": 2}, the above would have stopped at the first
          // complete object.  This next line will throw an exception on finding another top-level
          // object, allowing us to move on with other evaluation.
          reader.hasNext();
          if (result.isJsonObject()) {
            return result;
          }
        }
      } catch (IOException e) {
        // deliberately ignored - continue parsing
      }
    }
    // try to convert it to a JSON array. Fixes #2057.
    if (val.toString().trim().startsWith("[")) {
      JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(val.toString());
      if (json.isJsonArray()) {
        return json;
      }
    }
    try {
      if (log.isDebugEnabled()) {
        log.debug(
            "Evaluating property: '"
                + key
                + "' for token "
                + getName()
                + "("
                + getId()
                + ")----------------------------------------------------------------------------------");
      }
      val = MapTool.getParser().parseLine(resolver, this, val.toString());
    } catch (ParserException pe) {
      log.debug("Ignoring Parse Exception, continuing to evaluate {}", key);
      val = val.toString();
    }
    if (val == null) {
      val = "";
    } else {
      // Finally we try convert it to a JSON object. Fixes #1560.
      if (val.toString().trim().startsWith("{")) {
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(val.toString());
        if (json.isJsonObject()) {
          return json;
        }
      }
    }
    return val;
  }

  /** @return all property names, all in lowercase. */
  public Set<String> getPropertyNames() {
    return getPropertyMap().keySet();
  }

  /** @return all property names, preserving their case. */
  public Set<String> getPropertyNamesRaw() {
    return getPropertyMap().keySetRaw();
  }

  private CaseInsensitiveHashMap<Object> getPropertyMap() {
    if (propertyMapCI == null) {
      propertyMapCI = new CaseInsensitiveHashMap<Object>();
    }
    return propertyMapCI;
  }

  private void loadOldMacros() {
    if (macroMap == null) {
      return;
    }
    MacroButtonProperties prop;
    for (var macro : macroMap.entrySet()) {
      prop = new MacroButtonProperties(getMacroNextIndex());
      prop.setLabel(macro.getKey());
      prop.setCommand(macro.getValue());
      prop.setApplyToTokens(true);
      macroPropertiesMap.put(prop.getIndex(), prop);
    }
    macroMap = null;
    if (log.isDebugEnabled()) {
      log.debug("Token.loadOldMacros() set up " + macroPropertiesMap.size() + " new macros.");
    }
  }

  public int getMacroNextIndex() {
    if (macroPropertiesMap == null) {
      macroPropertiesMap = new HashMap<Integer, MacroButtonProperties>();
    }
    Set<Integer> indexSet = macroPropertiesMap.keySet();
    int maxIndex = 0;
    for (int index : indexSet) {
      if (index > maxIndex) {
        maxIndex = index;
      }
    }
    return maxIndex + 1;
  }

  /**
   * Returns the macroPropertiesMap of the token, or a blank map if not permitted.
   *
   * @param secure whether there should be a check for player ownership
   * @return the map
   */
  public Map<Integer, MacroButtonProperties> getMacroPropertiesMap(boolean secure) {
    if (macroPropertiesMap == null) {
      macroPropertiesMap = new HashMap<>();
    }
    if (macroMap != null) {
      loadOldMacros();
    }
    if (secure && !AppUtil.playerOwns(this)) {
      return new HashMap<>(); // blank map
    } else {
      return macroPropertiesMap;
    }
  }

  public MacroButtonProperties getMacro(int index, boolean secure) {
    return getMacroPropertiesMap(secure).get(index);
  }

  // avoid this; it loads the first macro with this label, but there could be more than one macro
  // with that label
  public MacroButtonProperties getMacro(String label, boolean secure) {
    Set<Integer> keys = getMacroPropertiesMap(secure).keySet();
    for (int key : keys) {
      MacroButtonProperties prop = macroPropertiesMap.get(key);
      if (prop.getLabel().equals(label)) {
        return prop;
      }
    }
    return null;
  }

  /**
   * Gets the list of macros on the token.
   *
   * @param secure whether there should be a check for player ownership
   * @return the list
   */
  public List<MacroButtonProperties> getMacroList(boolean secure) {
    Set<Integer> keys = getMacroPropertiesMap(secure).keySet();
    List<MacroButtonProperties> list = new ArrayList<MacroButtonProperties>();
    for (int key : keys) {
      list.add(macroPropertiesMap.get(key));
    }
    return list;
  }

  /**
   * Add a list of macros to the token.
   *
   * @param newMacroList the macro list to add
   * @param clearOld whether the old macros at other indexes should be removed
   */
  public void saveMacroList(List<MacroButtonProperties> newMacroList, boolean clearOld) {
    if (clearOld) {
      macroPropertiesMap.clear();
    }
    for (MacroButtonProperties macro : newMacroList) {
      if (macro.getLabel() == null
          || macro.getLabel().trim().length() == 0
          || macro.getCommand().trim().length() == 0) {
        continue;
      }
      macroPropertiesMap.put(macro.getIndex(), macro);
    }
  }

  /**
   * Saves the macro on the token.
   *
   * @param prop the properties of the macro
   */
  public void saveMacro(MacroButtonProperties prop) {
    getMacroPropertiesMap(false).put(prop.getIndex(), prop);
  }

  /**
   * Deletes the macro at the given index.
   *
   * @param index the index of the macro
   */
  public void deleteMacro(int index) {
    getMacroPropertiesMap(false).remove(index);
  }

  public List<String> getMacroNames(boolean secure) {
    Set<Integer> keys = getMacroPropertiesMap(secure).keySet();
    List<String> list = new ArrayList<String>();
    for (int key : keys) {
      MacroButtonProperties prop = macroPropertiesMap.get(key);
      list.add(prop.getLabel());
    }
    return list;
  }

  public boolean hasMacros(boolean secure) {
    return !getMacroPropertiesMap(secure).isEmpty();
  }

  public void setSpeechMap(Map<String, String> map) {
    getSpeechMap().clear();
    getSpeechMap().putAll(map);
  }

  public Set<String> getSpeechNames() {
    return getSpeechMap().keySet();
  }

  public String getSpeech(String key) {
    return getSpeechMap().get(key);
  }

  public void setSpeech(String key, String value) {
    getSpeechMap().put(key, value);
  }

  private Map<String, String> getSpeechMap() {
    if (speechMap == null) {
      speechMap = new HashMap<String, String>();
    }
    return speechMap;
  }

  /**
   * Get a set containing the names of all set properties on this token.
   *
   * @return The set of state property names that have a value associated with them.
   */
  public Set<String> getStatePropertyNames() {
    return state.keySet();
  }

  /**
   * Get a set containing the names of all the states that match the passed value.
   *
   * @param value the value to look for
   * @return The set of state property names that match the passed value.
   */
  public Set<String> getStatePropertyNames(Object value) {
    Map<String, Object> matches = new HashMap(state);
    for (Map.Entry<String, Object> entry : state.entrySet()) {
      if (!value.equals(entry.getValue())) {
        matches.remove(entry.getKey());
      }
    }
    return matches.keySet();
  }

  /** @return Getter for notes */
  public String getNotes() {
    return notes;
  }

  /** @param aNotes Setter for notes */
  public void setNotes(String aNotes) {
    notes = aNotes;
  }

  public boolean isFlippedY() {
    return isFlippedY;
  }

  public void setFlippedY(boolean isFlippedY) {
    this.isFlippedY = isFlippedY;
  }

  public boolean isFlippedX() {
    return isFlippedX;
  }

  public void setFlippedX(boolean isFlippedX) {
    this.isFlippedX = isFlippedX;
  }

  public boolean isFlippedIso() {
    if (isFlippedIso != null) {
      return isFlippedIso;
    }
    return false;
  }

  public void setFlippedIso(boolean isFlippedIso) {
    if (isFlippedIso) {
      this.isFlippedIso = true;
    } else {
      this.isFlippedIso = null;
    }
  }

  public Color getVisionOverlayColor() {
    if (visionOverlayColor == null && visionOverlayColorValue != null) {
      visionOverlayColor = new Color(visionOverlayColorValue);
    }
    return visionOverlayColor;
  }

  public void setVisionOverlayColor(Color color) {
    if (color != null) {
      visionOverlayColorValue = color.getRGB();
    } else {
      visionOverlayColorValue = null;
    }
    visionOverlayColor = color;
  }

  @Override
  public String toString() {
    return "Token: " + id;
  }

  public void setAnchor(int x, int y) {
    anchorX = x;
    anchorY = y;
  }

  public Point getAnchor() {
    return new Point(anchorX, anchorY);
  }

  public int getAnchorX() {
    return anchorX;
  }

  public int getAnchorY() {
    return anchorY;
  }

  /** @return the scale of the token layout */
  public double getSizeScale() {
    return sizeScale;
  }

  /**
   * Set the scale of the token layout
   *
   * @param scale the scale of the token
   */
  public void setSizeScale(double scale) {
    sizeScale = scale;
  }

  /**
   * Convert the token into a hash map. This is used to ship all of the properties for the token to
   * other apps that do need access to the <code>Token</code> class.
   *
   * @return A map containing the properties of the token.
   */
  public TokenTransferData toTransferData() {
    TokenTransferData td = new TokenTransferData();
    td.setName(name);
    td.setPlayers(ownerList);
    td.setVisible(isVisible);
    td.setLocation(new Point(x, y));
    td.setFacing(facing);

    // Set the properties
    td.put(TokenTransferData.ID, id.toString());
    td.put(TokenTransferData.ASSET_ID, imageAssetMap.get(null));
    td.put(TokenTransferData.Z, z);
    td.put(TokenTransferData.SNAP_TO_SCALE, snapToScale);
    td.put(TokenTransferData.WIDTH, scaleX);
    td.put(TokenTransferData.HEIGHT, scaleY);
    td.put(TokenTransferData.SNAP_TO_GRID, snapToGrid);
    td.put(TokenTransferData.OWNER_TYPE, ownerType);
    td.put(TokenTransferData.VISIBLE_OWNER_ONLY, visibleOnlyToOwner);
    td.put(TokenTransferData.TOKEN_TYPE, tokenShape);
    td.put(TokenTransferData.NOTES, notes);
    td.put(TokenTransferData.GM_NOTES, gmNotes);
    td.put(TokenTransferData.GM_NAME, gmName);

    // Put all of the serializable state into the map
    for (String key : getStatePropertyNames()) {
      Object value = getState(key);
      if (value instanceof Serializable) {
        td.put(key, value);
      }
    }
    td.putAll(state);

    // Create the image from the asset and add it to the map
    Image image = ImageManager.getImageAndWait(imageAssetMap.get(null));
    if (image != null) {
      td.setToken(new ImageIcon(image)); // Image icon makes it serializable.
    }
    return td;
  }

  /**
   * Constructor to create a new token from a transfer object containing its property values. This
   * is used to read in a new token from other apps that don't have access to the <code>Token</code>
   * class.
   *
   * @param td Read the values from this transfer object.
   */
  public Token(TokenTransferData td) {
    imageAssetMap = new HashMap<String, MD5Key>();
    state = new HashMap<String, Object>();
    if (td.getLocation() != null) {
      x = td.getLocation().x;
      y = td.getLocation().y;
    }
    snapToScale = getBoolean(td, TokenTransferData.SNAP_TO_SCALE, true);
    scaleX = getInt(td, TokenTransferData.WIDTH, 1);
    scaleY = getInt(td, TokenTransferData.HEIGHT, 1);
    snapToGrid = getBoolean(td, TokenTransferData.SNAP_TO_GRID, true);
    isVisible = td.isVisible();
    visibleOnlyToOwner = getBoolean(td, TokenTransferData.VISIBLE_OWNER_ONLY, false);
    name = td.getName();
    ownerList = td.getPlayers();
    ownerType =
        getInt(
            td, TokenTransferData.OWNER_TYPE, ownerList == null ? OWNER_TYPE_ALL : OWNER_TYPE_LIST);
    tokenShape = (String) td.get(TokenTransferData.TOKEN_TYPE);
    facing = td.getFacing();
    notes = (String) td.get(TokenTransferData.NOTES);
    gmNotes = (String) td.get(TokenTransferData.GM_NOTES);
    gmName = (String) td.get(TokenTransferData.GM_NAME);

    // Get the image and portrait for the token
    Asset asset = createAssetFromIcon(td.getToken());
    if (asset != null) {
      imageAssetMap.put(null, asset.getMD5Key());
    }
    asset = createAssetFromIcon((ImageIcon) td.get(TokenTransferData.PORTRAIT));
    if (asset != null) {
      portraitImage = asset.getMD5Key();
    }

    // Get the macros
    @SuppressWarnings("unchecked")
    Map<String, Object> macros = (Map<String, Object>) td.get(TokenTransferData.MACROS);
    macroMap = new HashMap<String, String>();
    for (var entry : macros.entrySet()) {
      String macroName = entry.getKey();
      Object macro = entry.getValue();
      if (macro instanceof String) {
        macroMap.put(macroName, (String) macro);
      } else if (macro instanceof Map) {
        @SuppressWarnings("unchecked")
        MacroButtonProperties mbp = new MacroButtonProperties(this, (Map<String, String>) macro);
        getMacroPropertiesMap(false).put(mbp.getIndex(), mbp);
      } // endif
    } // endfor
    loadOldMacros();

    // Get all of the non maptool specific state
    for (String key : td.keySet()) {
      if (key.startsWith(TokenTransferData.MAPTOOL)) {
        continue;
      }
      setProperty(key, td.get(key));
    } // endfor
  }

  private Asset createAssetFromIcon(ImageIcon icon) {
    if (icon == null) {
      return null;
    }

    // Make sure there is a buffered image for it
    Image image = icon.getImage();
    if (!(image instanceof BufferedImage)) {
      image =
          new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), Transparency.TRANSLUCENT);
      Graphics2D g = ((BufferedImage) image).createGraphics();
      icon.paintIcon(null, g, 0, 0);
    }
    // Create the asset
    Asset asset = null;
    try {
      asset = Asset.createImageAsset(name, ImageUtil.imageToBytes((BufferedImage) image));
      if (!AssetManager.hasAsset(asset)) {
        AssetManager.putAsset(asset);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return asset;
  }

  /**
   * Get an integer value from the map or return the default value
   *
   * @param map Get the value from this map
   * @param propName The name of the property being read.
   * @param defaultValue The value for the property if it is not set in the map.
   * @return The value for the passed property
   */
  private static int getInt(Map<String, Object> map, String propName, int defaultValue) {
    Integer integer = (Integer) map.get(propName);
    if (integer == null) {
      return defaultValue;
    }
    return integer;
  }

  /**
   * Get a boolean value from the map or return the default value
   *
   * @param map Get the value from this map
   * @param propName The name of the property being read.
   * @param defaultValue The value for the property if it is not set in the map.
   * @return The value for the passed property
   */
  private static boolean getBoolean(
      Map<String, Object> map, String propName, boolean defaultValue) {
    Boolean bool = (Boolean) map.get(propName);
    if (bool == null) {
      return defaultValue;
    }
    return bool;
  }

  /**
   * Get the list of initiatives for the token
   *
   * @return The List of initiative
   */
  @SuppressWarnings("unchecked")
  public List<InitiativeList.TokenInitiative> getInitiatives() {
    Zone zone = getZoneRenderer().getZone();
    List<Integer> list = zone.getInitiativeList().indexOf(this);
    if (list.isEmpty()) {
      return Collections.emptyList();
    }
    List<InitiativeList.TokenInitiative> ret = new ArrayList<>(list.size());
    for (Integer index : list) {
      ret.add(zone.getInitiativeList().getTokenInitiative(index));
    }
    return ret;
  }

  /**
   * Get the first initiative of the token
   *
   * @return The first token initiative value for the token
   */
  public InitiativeList.TokenInitiative getInitiative() {
    Zone zone = getZoneRenderer().getZone();
    List<Integer> list = zone.getInitiativeList().indexOf(this);
    if (list.isEmpty()) {
      return null;
    }
    return zone.getInitiativeList().getTokenInitiative(list.get(0));
  }

  public static boolean isTokenFile(String filename) {
    return filename != null && filename.toLowerCase().endsWith(FILE_EXTENSION);
  }

  public Icon getIcon(int width, int height) {
    ImageIcon icon = new ImageIcon(ImageManager.getImageAndWait(getImageAssetId()));
    Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
    return new ImageIcon(image);
  }

  public boolean isBeingImpersonated() {
    return beingImpersonated;
  }

  public void setBeingImpersonated(boolean bool) {
    beingImpersonated = bool;
  }

  public void deleteMacroGroup(String macroGroup, Boolean secure) {
    List<MacroButtonProperties> tempMacros =
        new ArrayList<MacroButtonProperties>(getMacroList(true));

    for (MacroButtonProperties nextProp : tempMacros) {
      if (macroGroup.equals(nextProp.getGroup())) {
        getMacroPropertiesMap(secure).remove(nextProp.getIndex());
      }
    }
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
    MapTool.getFrame().resetTokenPanels();
  }

  public void deleteAllMacros(Boolean secure) {
    List<MacroButtonProperties> tempMacros =
        new ArrayList<MacroButtonProperties>(getMacroList(true));
    for (MacroButtonProperties nextProp : tempMacros) {
      // Lee: maybe erasing the command will suffice to fix the hotkey bug.
      nextProp.setCommand("");
      getMacroPropertiesMap(secure).remove(nextProp.getIndex()); // switched with above line
      // to resolve panel render
      // timing problem.
    }

    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
    MapTool.getFrame().resetTokenPanels();
  }

  public void renameMacroGroup(String oldMacroGroup, String newMacroGroup) {
    List<MacroButtonProperties> tempMacros = new ArrayList<>(getMacroList(true));

    for (MacroButtonProperties nextProp : tempMacros) {
      if (oldMacroGroup.equals(nextProp.getGroup())) {
        nextProp.setGroup(newMacroGroup);
      }
    }
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
    MapTool.getFrame().resetTokenPanels();
  }

  public static final Comparator<Token> COMPARE_BY_NAME =
      (o1, o2) -> {
        if (o1 == null || o2 == null) {
          return 0;
        }
        return o1.getName().compareTo(o2.getName());
      };
  public static final Comparator<Token> COMPARE_BY_ZORDER =
      (o1, o2) -> {
        if (o1 == null || o2 == null) {
          return 0;
        }
        return Integer.compare(o1.z, o2.z);
      };

  @Override
  protected Object readResolve() {
    super.readResolve();
    // FJE: If the propertyMap field has something in it, it could be:
    // a pre-1.3b66 token that contains a HashMap<?,?>, or
    // a pre-1.3b78 token that actually has the CaseInsensitiveHashMap<?>.
    // Newer tokens will use propertyMapCI so we only need to make corrections
    // if the old field has data in it.
    if (propertyMap != null) {
      if (propertyMap instanceof CaseInsensitiveHashMap) {
        propertyMapCI = (CaseInsensitiveHashMap<Object>) propertyMap;
      } else {
        propertyMapCI = new CaseInsensitiveHashMap<Object>();
        propertyMapCI.putAll(propertyMap);
        propertyMap.clear(); // It'll never be written out, but we should free the memory.
      }
      propertyMap = null;
    }
    // 1.3 b77
    if (exposedAreaGUID == null) {
      exposedAreaGUID = new GUID();
    }

    // Fix for pre 1.11.3 campaigns and token size issues
    Map<Object, GUID> oldSizeMap = new HashMap<>(sizeMap);
    sizeMap.clear();
    for (var entry : oldSizeMap.entrySet()) {
      var key = entry.getKey();
      if (key instanceof Class<?> cl) {
        sizeMap.put(cl.getName(), entry.getValue());
      } else {
        sizeMap.put(key.toString(), entry.getValue());
      }
    }

    return this;
  }

  /** @param exposedAreaGUID the exposedAreaGUID to set */
  public void setExposedAreaGUID(GUID exposedAreaGUID) {
    this.exposedAreaGUID = exposedAreaGUID;
  }

  /** @return the exposedAreaGUID */
  public GUID getExposedAreaGUID() {
    return exposedAreaGUID;
  }

  /**
   * Lee: this is handy, putting it in...
   *
   * @return cloned token
   */
  @Override
  public Token clone() {
    try {
      return (Token) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  /** @return is token an image/lib token */
  public boolean isImgOrLib() {
    return (getName().toLowerCase().startsWith("image:")
        || getName().toLowerCase().startsWith("lib:"));
  }

  public HeroLabData getHeroLabData() {
    return heroLabData;
  }

  public void setHeroLabData(HeroLabData heroLabData) {
    this.heroLabData = heroLabData;
  }

  /**
   * Call the relevant setter from methodName with an array of parameters Called by
   * ClientMethodHandler to deal with sent change to token
   *
   * @param update The method to be used
   * @param zone The zone where the token is
   * @param parameters An array of parameters
   */
  public void updateProperty(Zone zone, Update update, Object[] parameters) {
    boolean lightChanged = false;
    boolean macroChanged = false;
    boolean panelLookChanged = false; // appearance of token in a panel changed
    switch (update) {
      case setState:
        setState(parameters[0].toString(), parameters[1]);
        break;
      case setAllStates:
        setAllStates(parameters[0]);
        break;
      case setPropertyType:
        setPropertyType(parameters[0].toString());
        break;
      case setPC:
        setType(Type.PC);
        break;
      case setNPC:
        setType(Type.NPC);
        break;
      case setLayer:
        setLayer((Zone.Layer) parameters[0]);
        break;
      case setLayerShape:
        setLayer((Zone.Layer) parameters[0]);
        setShape((TokenShape) parameters[1]);
        break;
      case setShape:
        setShape((TokenShape) parameters[0]);
        break;
      case setSnapToScale:
        setSnapToScale((Boolean) parameters[0]);
        break;
      case setSnapToGrid:
        setSnapToGrid((Boolean) parameters[0]);
        break;
      case setSnapToGridAndXY:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setSnapToGrid((boolean) parameters[0]);
        setX((int) parameters[1]);
        setY((int) parameters[2]);
        break;
      case setFootprint:
        setSnapToScale(true);
        setFootprint((Grid) parameters[0], (TokenFootprint) parameters[1]);
        break;
      case setProperty:
        setProperty(parameters[0].toString(), parameters[1].toString());
        break;
      case resetProperty:
        resetProperty(parameters[0].toString());
        break;
      case setZOrder:
        setZOrder((int) parameters[0]);
        zone.sortZOrder(); // update new ZOrder
        break;
      case setFacing:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setFacing((Integer) parameters[0]);
        break;
      case clearAllOwners:
        clearAllOwners();
        panelLookChanged = true;
        break;
      case setOwnedByAll:
        setOwnedByAll((Boolean) parameters[0]);
        panelLookChanged = true;
        break;
      case addOwner:
        addOwner(parameters[0].toString());
        break;
      case setScaleX:
        setSnapToScale(false);
        setScaleX((double) parameters[0]);
        break;
      case setScaleY:
        setSnapToScale(false);
        setScaleY((double) parameters[0]);
        break;
      case setScaleXY:
        setSnapToScale(false);
        setScaleX((double) parameters[0]);
        setScaleY((double) parameters[1]);
        break;
      case setNotes:
        setNotes(parameters[0].toString());
        break;
      case setGMNotes:
        setGMNotes(parameters[0].toString());
        break;
      case setX:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setX((int) parameters[0]);
        break;
      case setY:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setY((int) parameters[0]);
        break;
      case setXY:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setX((int) parameters[0]);
        setY((int) parameters[1]);
        break;
      case setHaloColor:
        setHaloColor((Color) parameters[0]);
        break;
      case setLabel:
        setLabel((String) parameters[0]);
        break;
      case setName:
        setName((String) parameters[0]);
        panelLookChanged = true;
        break;
      case setGMName:
        setGMName((String) parameters[0]);
        panelLookChanged = true;
        break;
      case setSpeechName:
        setSpeechName((String) parameters[0]);
        break;
      case setVisible:
        setVisible((boolean) parameters[0]);
        break;
      case setVisibleOnlyToOwner:
        setVisibleOnlyToOwner((boolean) parameters[0]);
        break;
      case setIsAlwaysVisible:
        setIsAlwaysVisible((boolean) parameters[0]);
        break;
      case setTokenOpacity:
        setTokenOpacity((String) parameters[0]);
        break;
      case setTerrainModifier:
        setTerrainModifier((double) parameters[0]);
        break;
      case setTerrainModifierOperation:
        setTerrainModifierOperation((TerrainModifierOperation) parameters[0]);
        break;
      case setTerrainModifiersIgnored:
        setTerrainModifiersIgnored((Set<TerrainModifierOperation>) parameters[0]);
        break;
      case setVBL:
        setVBL((Area) parameters[0]);
        if (!hasVBL()) { // if VBL removed
          zone.tokenTopologyChanged(); // if token lost VBL, TOKEN_CHANGED won't update topology
        }
        break;
      case setImageAsset:
        setImageAsset((String) parameters[0], (MD5Key) parameters[1]);
        panelLookChanged = true;
        break;
      case setPortraitImage:
        setPortraitImage((MD5Key) parameters[0]);
        break;
      case setCharsheetImage:
        setCharsheetImage((MD5Key) parameters[0]);
        break;
      case setLayout:
        setSizeScale((Double) parameters[0]);
        setAnchor((int) parameters[1], (int) parameters[2]);
        break;
      case clearLightSources:
        if (hasLightSources()) {
          lightChanged = true;
        }
        clearLightSources();
        break;
      case removeLightSource:
        if (hasLightSources()) {
          lightChanged = true;
        }
        removeLightSource((LightSource) parameters[0]);
        break;
      case addLightSource:
        lightChanged = true;
        addLightSource((LightSource) parameters[0], (Direction) parameters[1]);
        break;
      case setHasSight:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setHasSight((boolean) parameters[0]);
        break;
      case setSightType:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setSightType((String) parameters[0]);
        break;
      case saveMacro:
        saveMacro((MacroButtonProperties) parameters[0]);
        macroChanged = true;
        break;
      case saveMacroList:
        saveMacroList((List<MacroButtonProperties>) parameters[0], (boolean) parameters[1]);
        macroChanged = true;
        break;
      case deleteMacro:
        deleteMacro((int) parameters[0]);
        macroChanged = true;
        break;
      case flipX:
        setFlippedX(!isFlippedX());
        break;
      case flipY:
        setFlippedY(!isFlippedY());
        break;
      case flipIso:
        setFlippedIso(!isFlippedIso());
        break;
    }
    if (lightChanged) {
      getZoneRenderer().flushLight(); // flush lights if it changed
    }
    if (macroChanged) {
      zone.tokenMacroChanged(this);
    }
    if (panelLookChanged) {
      zone.tokenPanelChanged(this);
    }
    zone.tokenChanged(this); // fire Event.TOKEN_CHANGED, which updates topology if token has VBL
  }
}
