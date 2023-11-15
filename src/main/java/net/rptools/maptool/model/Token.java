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
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
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
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.rptools.CaseInsensitiveHashMap;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.transferable.TokenTransferData;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.zone.renderer.SelectionSet;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.TerrainModifierOperationDto;
import net.rptools.maptool.server.proto.TokenDto;
import net.rptools.maptool.server.proto.TokenPropertyValueDto;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;
import net.rptools.maptool.util.TokenUtil;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * This object represents the placeable objects on a map. For example an icon that represents a
 * character would exist as an {@link Asset} (the image itself) and a location and scale.
 */

// Lee: made tokens cloneable
public class Token implements Cloneable {

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
  private GUID exposedAreaGUID = new GUID();

  /** The stat sheet properties for the token. */
  @Nullable private StatSheetProperties statSheet;

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
    setTopology,
    setImageAsset,
    setPortraitImage,
    setCharsheetImage,
    setLayout,
    createUniqueLightSource,
    deleteUniqueLightSource,
    clearLightSources,
    removeLightSource,
    addLightSource,
    setHasSight,
    setSightType,
    flipX,
    flipY,
    flipIso,
    setSpeechName,
    removeFacing
  }

  public static final Comparator<Token> NAME_COMPARATOR =
      (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());

  private final Map<String, MD5Key> imageAssetMap = new HashMap<>();
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

  private Map<String, GUID> sizeMap = new HashMap<>();

  private boolean snapToGrid = true; // Whether the token snaps to the current grid or is free
  // floating

  private boolean isVisible = true;
  private boolean visibleOnlyToOwner = false;
  private int vblColorSensitivity = -1;
  private int alwaysVisibleTolerance = 2; // Default for # of regions (out of 9) that must be seen
  // before token is shown over FoW
  private boolean isAlwaysVisible = false; // Controls whether a Token is shown over VBL
  private Area vbl;
  private Area hillVbl;
  private Area pitVbl;
  private Area coverVbl;
  private Area mbl;

  private String name = "";
  private Set<String> ownerList = new HashSet<>();

  private int ownerType;

  private static final int OWNER_TYPE_ALL = 1;
  private static final int OWNER_TYPE_LIST = 0;

  private String tokenShape = TokenShape.SQUARE.toString();
  private String tokenType = Type.NPC.toString();
  private String layer = Zone.Layer.getDefaultPlayerLayer().toString();
  private transient Zone.Layer actualLayer;

  private String propertyType =
      MapTool.getCampaign().getCampaignProperties().getDefaultTokenPropertyType();

  private Integer facing = null;

  private Integer haloColorValue;
  private transient Color haloColor;

  private Integer visionOverlayColorValue;
  private transient Color visionOverlayColor;

  // Jamz: allow token alpha channel modification
  private @Nonnull Float tokenOpacity = 1.0f;

  private String speechName = "";

  /** Terrain Modifier Operations */
  public enum TerrainModifierOperation {
    NONE(), // Default, no terrain modifications to pathfinding cost
    MULTIPLY(), // All tokens with this type are added together and multiplied against the Cell cost
    ADD(), // All tokens with this type are added together and added to the cell cost
    BLOCK(), // Movement through tokens with this type are blocked just as if they had MBL
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
  private Boolean isFlippedIso = false;

  private MD5Key charsheetImage;
  private MD5Key portraitImage;

  private Map<GUID, LightSource> uniqueLightSources = new LinkedHashMap<>();
  /**
   * All light sources attached to the token.
   *
   * <p>The elements should be unique, i.e., no two should reference the same light source.
   */
  private List<AttachedLightSource> lightSourceList = new ArrayList<>();

  private String sightType;
  private boolean hasSight;
  private Boolean hasImageTable = false;
  private String imageTableName;

  private String label;

  /** The notes that are displayed for this token. */
  private String notes;

  private String notesType = SyntaxConstants.SYNTAX_STYLE_NONE;

  private String gmNotes;
  private String gmNotesType = SyntaxConstants.SYNTAX_STYLE_NONE;

  private String gmName;

  /**
   * A state properties for this token. This allows state to be added that can change appearance of
   * the token.
   */
  private final Map<String, Object> state = new HashMap<>();

  /** Properties */
  // I screwed up. propertyMap was HashMap<String,Object> in pre-1.3b70 (?)
  // and became a CaseInsensitiveHashMap<Object> thereafter. So in order to
  // be able to load old tokens, we need to read in the original data type and
  // copy the elements into the new data type. But because the name didn't
  // change (that was the screw up) we have special code in readResolve() to
  // help XStream move the data around.
  private Map<String, Object> propertyMap; // 1.3b77 and earlier

  private CaseInsensitiveHashMap<Object> propertyMapCI = new CaseInsensitiveHashMap<>();

  private Map<String, String> macroMap;
  private Map<Integer, MacroButtonProperties> macroPropertiesMap = new HashMap<>();

  private Map<String, String> speechMap = new HashMap<>();

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
    hillVbl = token.hillVbl;
    pitVbl = token.pitVbl;
    coverVbl = token.coverVbl;
    mbl = token.mbl;

    name = token.name;
    notes = token.notes;
    notesType = token.notesType;
    gmName = token.gmName;
    gmNotes = token.gmNotes;
    gmNotesType = token.gmNotesType;
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
    ownerList.addAll(token.ownerList);

    uniqueLightSources.putAll(token.uniqueLightSources);
    lightSourceList.addAll(token.lightSourceList);

    state.putAll(token.state);
    getPropertyMap().clear();
    getPropertyMap().putAll(token.propertyMapCI);
    // Deep copy of the macros
    token.macroPropertiesMap.forEach(
        (key, value) ->
            macroPropertiesMap.put(key, new MacroButtonProperties(this, key, value, false)));

    // convert old-style macros
    if (token.macroMap != null) {
      macroMap = new HashMap<>(token.macroMap);
      loadOldMacros();
    }
    speechMap.putAll(token.speechMap);
    imageAssetMap.putAll(token.imageAssetMap);
    sizeMap.putAll(token.sizeMap);

    exposedAreaGUID = token.exposedAreaGUID;

    heroLabData = token.heroLabData;
    tokenOpacity = token.tokenOpacity;
    terrainModifier = token.terrainModifier;
    terrainModifierOperation = token.terrainModifierOperation;
    terrainModifiersIgnored.addAll(token.terrainModifiersIgnored);
    speechName = token.speechName != null ? token.speechName : "";
    allowURIAccess = token.allowURIAccess;
    statSheet = token.statSheet;
  }

  public Token() {}

  public Token(String name, MD5Key assetId) {
    this.name = name;

    // NULL key is the default
    imageAssetMap.put(null, assetId);

    // convert old-style macros
    if (macroMap != null) {
      loadOldMacros();
    }

    propertyType = MapTool.getCampaign().getCampaignProperties().getDefaultTokenPropertyType();
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
    ownerList.clear();
    // propertyMapCI = null;
    // propertyType = "Basic";
    /**
     * Lee: why shouldn't propertyType be set to what the framework uses? In case of multiple
     * propertyType, give a choice; or incorporate in the Campaign Properties window a marker for
     * what is default for new tokens.
     */
    propertyType = MapTool.getCampaign().getCampaignProperties().getDefaultTokenPropertyType();

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
    this.hasImageTable = hasImageTable;
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
    return getLayer().isMarkerLayer()
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

  public String getGmNotesType() {
    return gmNotesType;
  }

  public void setGmNotesType(String type) {
    gmNotesType = type;
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

  /**
   * @return The token opacity, in the range [0.0f, 1.0f].
   */
  public float getTokenOpacity() {
    return tokenOpacity;
  }

  /**
   * Set the token opacity from a float trimmed to [0.0f, 1.0f]
   *
   * @param alpha the float of the opacity.
   */
  public void setTokenOpacity(float alpha) {
    if (alpha > 1.0f) {
      alpha = 1.0f;
    }
    if (alpha < 0.0f) {
      alpha = 0.0f;
    }

    tokenOpacity = alpha;
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
    if (terrainModifiersIgnored.isEmpty()) {
      terrainModifiersIgnored.add(TerrainModifierOperation.NONE);
    }

    return terrainModifiersIgnored;
  }

  public void setTerrainModifiersIgnored(Set<TerrainModifierOperation> terrainModifiersIgnored) {
    this.terrainModifiersIgnored.clear();
    this.terrainModifiersIgnored.addAll(terrainModifiersIgnored);

    if (this.terrainModifiersIgnored.contains(TerrainModifierOperation.NONE)) {
      terrainModifiersIgnored.clear();
      this.terrainModifiersIgnored.add(TerrainModifierOperation.NONE);
    }
  }

  public TokenShape getShape() {
    try {
      return TokenShape.valueOf(tokenShape);
    } catch (IllegalArgumentException iae) {
      tokenShape = TokenShape.SQUARE.name();
      return TokenShape.SQUARE;
    }
  }

  public void setShape(TokenShape type) {
    this.tokenShape = type.name();
  }

  /**
   * Sets the shape based on the token's layer and image.
   *
   * @return The shape that was decided, possibly the same shape as before.
   */
  public TokenShape guessAndSetShape() {
    var shape = Token.TokenShape.TOP_DOWN;
    if (getLayer().supportsGuessingTokenShape()) {
      Image image = ImageManager.getImage(getImageAssetId());
      if (image != null && image != ImageManager.TRANSFERING_IMAGE) {
        shape = TokenUtil.guessTokenType(image);
      }
    }
    setShape(shape);
    return shape;
  }

  public Type getType() {
    try {
      return Type.valueOf(tokenType);
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
        actualLayer = Zone.Layer.valueOf(layer);
      }
      return actualLayer;
    } catch (IllegalArgumentException iae) {
      return Zone.Layer.getDefaultPlayerLayer();
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

  public @Nonnull Collection<LightSource> getUniqueLightSources() {
    return uniqueLightSources.values();
  }

  public @Nullable LightSource getUniqueLightSource(GUID lightSourceId) {
    return uniqueLightSources.getOrDefault(lightSourceId, null);
  }

  public void addUniqueLightSource(LightSource source) {
    uniqueLightSources.put(source.getId(), source);
  }

  public void removeUniqueLightSource(GUID lightSourceId) {
    uniqueLightSources.remove(lightSourceId);
  }

  public void addLightSource(GUID lightSourceId) {
    if (lightSourceList.stream().anyMatch(source -> source.matches(lightSourceId))) {
      // Avoid duplicates.
      return;
    }
    lightSourceList.add(new AttachedLightSource(lightSourceId));
  }

  public void removeLightSourceType(LightSource.Type lightType) {
    for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext(); ) {
      AttachedLightSource als = i.next();
      LightSource lightSource = als.resolve(this, MapTool.getCampaign());
      if (lightSource != null && lightSource.getType() == lightType) {
        i.remove();
      }
    }
  }

  public void removeGMAuras() {
    for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext(); ) {
      AttachedLightSource als = i.next();
      LightSource lightSource = als.resolve(this, MapTool.getCampaign());
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

  public void removeOwnerOnlyAuras() {
    for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext(); ) {
      AttachedLightSource als = i.next();
      LightSource lightSource = als.resolve(this, MapTool.getCampaign());
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

  public boolean hasOwnerOnlyAuras() {
    for (AttachedLightSource als : lightSourceList) {
      LightSource lightSource = als.resolve(this, MapTool.getCampaign());
      if (lightSource != null) {
        List<Light> lights = lightSource.getLightList();
        for (Light light : lights) {
          if (light.isOwnerOnly()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean hasGMAuras() {
    for (AttachedLightSource als : lightSourceList) {
      LightSource lightSource = als.resolve(this, MapTool.getCampaign());
      if (lightSource != null) {
        List<Light> lights = lightSource.getLightList();
        for (Light light : lights) {
          if (light.isGM()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean hasLightSourceType(LightSource.Type lightType) {
    for (AttachedLightSource als : lightSourceList) {
      LightSource lightSource = als.resolve(this, MapTool.getCampaign());
      if (lightSource != null && lightSource.getType() == lightType) {
        return true;
      }
    }
    return false;
  }

  public void removeLightSource(GUID lightSourceId) {
    lightSourceList.removeIf(als -> als.matches(lightSourceId));
  }

  /** Clear the lightSourceList */
  public void clearLightSources() {
    lightSourceList.clear();
  }

  public boolean hasLightSource(LightSource source) {
    if (source.getId() == null) {
      // Shouldn't happen as this method should only be used with non-personal lights.
      return false;
    }

    for (AttachedLightSource als : lightSourceList) {
      if (als.matches(source.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return false if lightSourceList is null or empty, and true otherwise
   */
  public boolean hasLightSources() {
    return !lightSourceList.isEmpty();
  }

  public List<AttachedLightSource> getLightSources() {
    return Collections.unmodifiableList(lightSourceList);
  }

  public synchronized void addOwner(String playerId) {
    ownerType = OWNER_TYPE_LIST;
    ownerList.add(playerId);
  }

  /**
   * @return true if the token is owned by all or has explicit owners.
   */
  public synchronized boolean hasOwners() {
    return ownerType == OWNER_TYPE_ALL || !ownerList.isEmpty();
  }

  public synchronized void removeOwner(String playerId) {
    ownerType = OWNER_TYPE_LIST;
    ownerList.remove(playerId);
  }

  public synchronized void setOwnedByAll(boolean ownedByAll) {
    if (ownedByAll) {
      ownerType = OWNER_TYPE_ALL;
      ownerList.clear();
    } else {
      ownerType = OWNER_TYPE_LIST;
    }
  }

  /**
   * @return the set of owner names of the token.
   */
  public Set<String> getOwners() {
    return Collections.unmodifiableSet(ownerList);
  }

  public boolean isOwnedByAll() {
    return ownerType == OWNER_TYPE_ALL;
  }

  public synchronized void clearAllOwners() {
    ownerList.clear();
  }

  public synchronized boolean isOwner(String playerId) {
    return (ownerType == OWNER_TYPE_ALL || ownerList.contains(playerId));
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
      List<Token> tokensList = curZone.getAllTokens();

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
    Set<MD5Key> assetSet = new HashSet<>(imageAssetMap.values());
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

  /**
   * @return Returns the snapScale.
   */
  public boolean isSnapToScale() {
    return snapToScale;
  }

  /**
   * @param snapScale The snapScale to set.
   */
  public void setSnapToScale(boolean snapScale) {
    this.snapToScale = snapScale;
  }

  public void setVisible(boolean visible) {
    this.isVisible = visible;
  }

  /**
   * @return isVisible
   */
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * @return the visibleOnlyToOwner
   */
  public boolean isVisibleOnlyToOwner() {
    return visibleOnlyToOwner;
  }

  /**
   * @param visibleOnlyToOwner the visibleOnlyToOwner to set
   */
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
   * Return the area of the token for the requested type of topology.
   *
   * @param topologyType The type of topology to return.
   * @return the current topology of the token.
   */
  public Area getTopology(Zone.TopologyType topologyType) {
    return switch (topologyType) {
      case WALL_VBL -> vbl;
      case HILL_VBL -> hillVbl;
      case PIT_VBL -> pitVbl;
      case COVER_VBL -> coverVbl;
      case MBL -> mbl;
    };
  }

  /**
   * Transform the token's topology according to the token's scale, position, rotation and flipping.
   *
   * @param topologyType The type of topology to transform.
   * @return the transformed topology for the token
   */
  public Area getTransformedTopology(Zone.TopologyType topologyType) {
    return getTransformedTopology(getTopology(topologyType));
  }

  /**
   * Set the topology of the given type for the token.
   *
   * <p>If no topology remains on the token, set {@link #vblColorSensitivity} to -1.
   *
   * @param topologyType The type of topology to set.
   * @param topology the topology area to set.
   */
  public void setTopology(Zone.TopologyType topologyType, @Nullable Area topology) {
    switch (topologyType) {
      case WALL_VBL -> vbl = topology;
      case HILL_VBL -> hillVbl = topology;
      case PIT_VBL -> pitVbl = topology;
      case COVER_VBL -> coverVbl = topology;
      case MBL -> mbl = topology;
    }
    ;

    if (!hasAnyTopology()) {
      vblColorSensitivity = -1;
    }
  }

  /**
   * Return the existence of the requested type of topology.
   *
   * @param topologyType The type of topology to check for.
   * @return true if the token has the given type of topology.
   */
  public boolean hasTopology(Zone.TopologyType topologyType) {
    return getTopology(topologyType) != null;
  }

  /**
   * Return the existence of any type of topology.
   *
   * @return true if the token has any kind of topology.
   */
  public boolean hasAnyTopology() {
    return Arrays.stream(Zone.TopologyType.values())
        .map(this::getTopology)
        .anyMatch(Objects::nonNull);
  }

  /**
   * This method transforms an area (meant to be one of the token's topologies) with
   * AffineTransformations applied for scale, position, rotation, &amp; flipping.
   *
   * @param areaToTransform The area to apply transformations tos.
   * @return the transformed area for the token
   * @author Jamz
   * @since 1.4.1.5
   */
  public Area getTransformedTopology(Area areaToTransform) {
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

  public void setIsAlwaysVisible(boolean isAlwaysVisible) {
    this.isAlwaysVisible = isAlwaysVisible;
  }

  public void toggleIsAlwaysVisible() {
    isAlwaysVisible = !isAlwaysVisible;
  }

  public String getName() {
    return name;
  }

  public Rectangle getBounds(Zone zone) {
    Grid grid = zone.getGrid();
    TokenFootprint footprint = getFootprint(grid);
    Rectangle footprintBounds =
        footprint.getBounds(grid, grid.convert(new ZonePoint(getX(), getY())));

    double w;
    double h;

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
      if (getLayer().anchorSnapToGridAtCenter()) {
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
      if (!getLayer().anchorSnapToGridAtCenter() || isSnapToScale() || getLayer().isTokenLayer()) {
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
    if (grid.getCapabilities().isSnapToGridSupported() || !getLayer().anchorSnapToGridAtCenter()) {
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
    if (grid.getCapabilities().isSnapToGridSupported() || !getLayer().anchorSnapToGridAtCenter()) {
      if (!getLayer().anchorSnapToGridAtCenter() || isSnapToScale()) {
        TokenFootprint footprint = getFootprint(grid);
        Rectangle footprintBounds = footprint.getBounds(grid);
        double footprintOffsetX = 0;
        double footprintOffsetY = 0;
        if (getLayer().anchorSnapToGridAtCenter()) {
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

  /**
   * @return the String of the sightType
   */
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

  public List<String> getSetStates() {
    List<String> setStates = new ArrayList<String>();
    for (Map.Entry<String, Object> entry : state.entrySet()) {
      if (entry.getValue() instanceof Boolean) {
        if ((Boolean) entry.getValue()) {
          setStates.add(entry.getKey());
        }
      }
    }
    return setStates;
  }

  /**
   * Set the value of state for this Token.
   *
   * @param aState The property to set.
   * @param aValue The new value for the property.
   * @return The original value of the state, if any.
   */
  public Object setState(String aState, Object aValue) {
    // the GUI sends null to mean remove a state/bar
    if (aValue == null) {
      return state.remove(aState);
    }
    // setBarVisible sends a boolean to show/hide a bar
    if (aValue instanceof Boolean) {
      if ((Boolean) aValue) {
        return state.put(aState, aValue);
      } else {
        return state.remove(aState);
      }
    }
    // Either enable a state or set the value of a bar
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

    // // Short name ?
    // if (value == null) {
    // for (EditTokenProperty property :
    // MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(getPropertyType())) {
    // if (property.getShortName().equals(key)) {
    // value = getPropertyMap().get(property.getShortName().toUpperCase());
    // }
    // }
    // }
    return getPropertyMap().get(key);
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
      log.debug(
          "Evaluating property: '{}' for token {} ({})----------------------------------------------------------------------------------",
          key,
          getName(),
          getId());
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

  /**
   * @return all property names, all in lowercase.
   */
  public Set<String> getPropertyNames() {
    return getPropertyMap().keySet();
  }

  /**
   * @return all property names, preserving their case.
   */
  public Set<String> getPropertyNamesRaw() {
    return getPropertyMap().keySetRaw();
  }

  private CaseInsensitiveHashMap<Object> getPropertyMap() {
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
    log.debug("Token.loadOldMacros() set up {} new macros.", macroPropertiesMap.size());
  }

  public int getMacroNextIndex() {
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
    List<MacroButtonProperties> list = new ArrayList<>();
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
      if (macro.getLabel().trim().length() == 0 || macro.getCommand().trim().length() == 0) {
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
    List<String> list = new ArrayList<>();
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
    Map<String, Object> matches = new HashMap<>(state);
    for (Map.Entry<String, Object> entry : state.entrySet()) {
      if (!value.equals(entry.getValue())) {
        matches.remove(entry.getKey());
      }
    }
    return matches.keySet();
  }

  /**
   * @return Getter for notes
   */
  public String getNotes() {
    return notes;
  }

  /**
   * @param aNotes Setter for notes
   */
  public void setNotes(String aNotes) {
    notes = aNotes;
  }

  public String getNotesType() {
    return notesType;
  }

  public void setNotesType(String type) {
    notesType = type;
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
    this.isFlippedIso = isFlippedIso;
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

  /**
   * @return the scale of the token layout
   */
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
    ownerList.addAll(td.getPlayers());
    ownerType =
        getInt(
            td,
            TokenTransferData.OWNER_TYPE,
            ownerList.isEmpty() ? OWNER_TYPE_ALL : OWNER_TYPE_LIST);
    tokenShape = (String) td.get(TokenTransferData.TOKEN_TYPE);
    facing = td.getFacing();
    notes = (String) td.get(TokenTransferData.NOTES);
    gmNotes = (String) td.get(TokenTransferData.GM_NOTES);
    gmName = (String) td.get(TokenTransferData.GM_NAME);

    propertyType = MapTool.getCampaign().getCampaignProperties().getDefaultTokenPropertyType();

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
    macroMap = new HashMap<>();
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
    List<MacroButtonProperties> tempMacros = new ArrayList<>(getMacroList(true));

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
    List<MacroButtonProperties> tempMacros = new ArrayList<>(getMacroList(true));
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

  protected Object readResolve() {
    // FJE: If the propertyMap field has something in it, it could be:
    // a pre-1.3b66 token that contains a HashMap<?,?>, or
    // a pre-1.3b78 token that actually has the CaseInsensitiveHashMap<?>.
    // Newer tokens will use propertyMapCI so we only need to make corrections
    // if the old field has data in it.
    if (propertyMapCI == null) {
      propertyMapCI = new CaseInsensitiveHashMap<>();
    }
    if (propertyMap != null) {
      propertyMapCI.putAll(propertyMap);
      propertyMap.clear(); // It'll never be written out, but we should free the memory.
      propertyMap = null;
    }
    // 1.3 b77
    if (exposedAreaGUID == null) {
      exposedAreaGUID = new GUID();
    }

    // Fix for pre 1.11.3 campaigns and token size issues
    if (sizeMap != null && sizeMap.size() > 0) {
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
    }

    if (ownerList == null) {
      ownerList = new HashSet<>();
    }
    if (uniqueLightSources == null) {
      uniqueLightSources = new LinkedHashMap<>();
    }

    // Remove null and duplicate attached light sources.
    List<AttachedLightSource> lightSources =
        Objects.requireNonNullElseGet(lightSourceList, ArrayList::new);
    lightSourceList = new ArrayList<>();
    final var seenGuids = new HashSet<GUID>();
    for (final var source : lightSources) {
      if (source != null && !seenGuids.contains(source.getId())) {
        lightSourceList.add(source);
        seenGuids.add(source.getId());
      }
    }

    if (macroPropertiesMap == null) {
      macroPropertiesMap = new HashMap<>();
    }
    if (speechMap == null) {
      speechMap = new HashMap<>();
    }
    if (isFlippedIso == null) {
      isFlippedIso = false;
    }
    if (hasImageTable == null) {
      hasImageTable = false;
    }
    if (tokenShape == null) {
      tokenShape = TokenShape.SQUARE.toString();
    }
    if (tokenType == null) {
      tokenType = Type.NPC.toString();
    }
    if (speechName == null) {
      speechName = "";
    }
    if (terrainModifiersIgnored == null) {
      terrainModifiersIgnored =
          new HashSet<>(Collections.singletonList(TerrainModifierOperation.NONE));
    }
    if (terrainModifierOperation == null) {
      terrainModifierOperation = TerrainModifierOperation.NONE;
    }
    if (sizeMap == null) {
      sizeMap = new HashMap<>();
    }

    // Check to make sure lastPath has valid data
    if (lastPath != null && lastPath.getCellPath().isEmpty()) {
      lastPath = null;
    }

    if (notesType == null) {
      notesType = SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    if (gmNotesType == null) {
      gmNotesType = SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    // Pre 1.13
    if (tokenOpacity == null) {
      tokenOpacity = 1.f;
    }
    tokenOpacity = Math.max(0.f, Math.min(tokenOpacity, 1.f));

    return this;
  }

  /**
   * @param exposedAreaGUID the exposedAreaGUID to set
   */
  public void setExposedAreaGUID(GUID exposedAreaGUID) {
    this.exposedAreaGUID = exposedAreaGUID;
  }

  /**
   * @return the exposedAreaGUID
   */
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

  /**
   * @return is token an image/lib token
   */
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
   * @param zone The zone where the token is
   * @param update The method to be used
   * @param parameters An array of parameters
   */
  public void updateProperty(Zone zone, Update update, List<TokenPropertyValueDto> parameters) {
    boolean lightChanged = false;
    boolean macroChanged = false;
    boolean panelLookChanged = false; // appearance of token in a panel changed
    switch (update) {
      case setState:
        var state = parameters.get(0).getStringValue();
        var stateValue = parameters.get(1);
        if (stateValue.hasBoolValue()) setState(state, stateValue.getBoolValue());
        else setState(state, BigDecimal.valueOf(stateValue.getDoubleValue()));
        break;
      case setAllStates:
        stateValue = parameters.get(0);
        if (stateValue.hasBoolValue()) setAllStates(stateValue.getBoolValue());
        else setAllStates(BigDecimal.valueOf(stateValue.getDoubleValue()));
        break;
      case setPropertyType:
        setPropertyType(parameters.get(0).getStringValue());
        break;
      case setPC:
        setType(Type.PC);
        break;
      case setNPC:
        setType(Type.NPC);
        break;
      case setLayer:
        setLayer(Zone.Layer.valueOf(parameters.get(0).getStringValue()));
        break;
      case setLayerShape:
        setLayer(Zone.Layer.valueOf(parameters.get(0).getStringValue()));
        setShape(TokenShape.valueOf(parameters.get(1).getStringValue()));
        break;
      case setShape:
        setShape(TokenShape.valueOf(parameters.get(0).getStringValue()));
        break;
      case setSnapToScale:
        setSnapToScale(parameters.get(0).getBoolValue());
        break;
      case setSnapToGrid:
        setSnapToGrid(parameters.get(0).getBoolValue());
        break;
      case setSnapToGridAndXY:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setSnapToGrid(parameters.get(0).getBoolValue());
        setX(parameters.get(1).getIntValue());
        setY(parameters.get(2).getIntValue());
        break;
      case setFootprint:
        setSnapToScale(true);
        setFootprint(
            Grid.fromDto(parameters.get(0).getGrid()),
            TokenFootprint.fromDto(parameters.get(1).getTokenFootPrint()));
        break;
      case setProperty:
        setProperty(parameters.get(0).getStringValue(), parameters.get(1).getStringValue());
        break;
      case resetProperty:
        resetProperty(parameters.get(0).getStringValue());
        break;
      case setZOrder:
        setZOrder(parameters.get(0).getIntValue());
        zone.sortZOrder(); // update new ZOrder
        break;
      case setFacing:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setFacing(parameters.get(0).getIntValue());
        break;
      case removeFacing:
        setFacing(null);
        break;
      case clearAllOwners:
        clearAllOwners();
        panelLookChanged = true;
        break;
      case setOwnedByAll:
        setOwnedByAll(parameters.get(0).getBoolValue());
        panelLookChanged = true;
        break;
      case addOwner:
        addOwner(parameters.get(0).getStringValue());
        break;
      case setScaleX:
        setSnapToScale(false);
        setScaleX(parameters.get(0).getDoubleValue());
        break;
      case setScaleY:
        setSnapToScale(false);
        setScaleY(parameters.get(0).getDoubleValue());
        break;
      case setScaleXY:
        setSnapToScale(false);
        setScaleX(parameters.get(0).getDoubleValue());
        setScaleY(parameters.get(1).getDoubleValue());
        break;
      case setNotes:
        setNotes(parameters.get(0).getStringValue());
        break;
      case setGMNotes:
        setGMNotes(parameters.get(0).getStringValue());
        break;
      case setX:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setX(parameters.get(0).getIntValue());
        break;
      case setY:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setY(parameters.get(0).getIntValue());
        break;
      case setXY:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setX(parameters.get(0).getIntValue());
        setY(parameters.get(1).getIntValue());
        break;
      case setHaloColor:
        setHaloColor(
            parameters.size() > 0 ? new Color(parameters.get(0).getIntValue(), true) : null);
        break;
      case setLabel:
        setLabel(parameters.get(0).getStringValue());
        break;
      case setName:
        setName(parameters.get(0).getStringValue());
        panelLookChanged = true;
        break;
      case setGMName:
        setGMName(parameters.get(0).getStringValue());
        panelLookChanged = true;
        break;
      case setSpeechName:
        setSpeechName(parameters.get(0).getStringValue());
        break;
      case setVisible:
        setVisible(parameters.get(0).getBoolValue());
        break;
      case setVisibleOnlyToOwner:
        setVisibleOnlyToOwner(parameters.get(0).getBoolValue());
        break;
      case setIsAlwaysVisible:
        setIsAlwaysVisible(parameters.get(0).getBoolValue());
        break;
      case setTokenOpacity:
        setTokenOpacity(Float.parseFloat(parameters.get(0).getStringValue()));
        break;
      case setTerrainModifier:
        setTerrainModifier(parameters.get(0).getDoubleValue());
        break;
      case setTerrainModifierOperation:
        setTerrainModifierOperation(
            TerrainModifierOperation.valueOf(parameters.get(0).getStringValue()));
        break;
      case setTerrainModifiersIgnored:
        setTerrainModifiersIgnored(
            parameters.get(0).getStringValues().getValuesList().stream()
                .map(TerrainModifierOperation::valueOf)
                .collect(Collectors.toSet()));
        break;
      case setTopology:
        {
          final var topologyType = Zone.TopologyType.valueOf(parameters.get(0).getTopologyType());
          setTopology(topologyType, Mapper.map(parameters.get(1).getArea()));
          if (!hasTopology(topologyType)) { // if topology removed
            zone.tokenTopologyChanged(); // if token lost topology, TOKEN_CHANGED won't update
            // topology
          }
          break;
        }
      case setImageAsset:
        setImageAsset(
            parameters.get(0).hasStringValue() ? parameters.get(0).getStringValue() : null,
            new MD5Key(parameters.get(1).getStringValue()));
        panelLookChanged = true;
        break;
      case setPortraitImage:
        if (parameters.get(0).hasStringValue() && !parameters.get(0).getStringValue().isEmpty()) {
          setPortraitImage(new MD5Key(parameters.get(0).getStringValue()));
        } else {
          setPortraitImage(null);
        }
        break;
      case setCharsheetImage:
        if (parameters.get(0).hasStringValue() && !parameters.get(0).getStringValue().isEmpty()) {
          setCharsheetImage(new MD5Key(parameters.get(0).getStringValue()));
        } else {
          setCharsheetImage(null);
        }
        break;
      case setLayout:
        setSizeScale(parameters.get(0).getDoubleValue());
        setAnchor(parameters.get(1).getIntValue(), parameters.get(2).getIntValue());
        break;
      case createUniqueLightSource:
        lightChanged = true;
        addUniqueLightSource(LightSource.fromDto(parameters.get(0).getLightSource()));
        break;
      case deleteUniqueLightSource:
        lightChanged = true;
        removeUniqueLightSource(GUID.valueOf(parameters.get(0).getLightSourceId()));
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
        removeLightSource(GUID.valueOf(parameters.get(0).getLightSourceId()));
        break;
      case addLightSource:
        lightChanged = true;
        addLightSource(GUID.valueOf(parameters.get(0).getLightSourceId()));
        break;
      case setHasSight:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setHasSight(parameters.get(0).getBoolValue());
        break;
      case setSightType:
        if (hasLightSources()) {
          lightChanged = true;
        }
        setSightType(parameters.get(0).getStringValue());
        break;
      case saveMacro:
        saveMacro(MacroButtonProperties.fromDto(parameters.get(0).getMacros().getMacros(0)));
        macroChanged = true;
        break;
      case saveMacroList:
        saveMacroList(
            parameters.get(0).getMacros().getMacrosList().stream()
                .map(MacroButtonProperties::fromDto)
                .collect(Collectors.toList()),
            parameters.get(1).getBoolValue());
        macroChanged = true;
        break;
      case deleteMacro:
        deleteMacro(parameters.get(0).getIntValue());
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

  public static Token fromDto(TokenDto dto) {
    var token = new Token();
    token.id = GUID.valueOf(dto.getId());
    token.beingImpersonated = dto.getBeingImpersonated();
    token.exposedAreaGUID = GUID.valueOf(dto.getExposedAreaGuid());
    var assetMap = dto.getImageAssetMapMap();
    for (var key : assetMap.keySet()) {
      var nullKey = key.equals("") ? null : key;
      token.imageAssetMap.put(nullKey, new MD5Key(assetMap.get(key)));
    }
    token.currentImageAsset =
        dto.hasCurrentImageAsset() ? dto.getCurrentImageAsset().getValue() : null;
    token.lastX = dto.getLastX();
    token.x = dto.getX();
    token.lastY = dto.getLastY();
    token.y = dto.getY();
    token.z = dto.getZ();
    token.anchorX = dto.getAnchorX();
    token.anchorY = dto.getAnchorY();
    token.sizeScale = dto.getSizeScale();
    token.lastPath = dto.hasLastPath() ? Path.fromDto(dto.getLastPath()) : null;
    token.snapToScale = dto.getSnapToScale();
    token.width = dto.getWidth();
    token.height = dto.getHeight();
    token.isoWidth = dto.getIsoWidth();
    token.isoHeight = dto.getIsoHeight();
    token.scaleX = dto.getScaleX();
    token.scaleY = dto.getScaleY();
    dto.getSizeMapMap().forEach((k, v) -> token.sizeMap.put(k, GUID.valueOf(v)));
    token.snapToGrid = dto.getSnapToGrid();
    token.isVisible = dto.getIsVisible();
    token.visibleOnlyToOwner = dto.getVisibleOnlyToOwner();
    token.vblColorSensitivity = dto.getVblColorSensitivity();
    token.alwaysVisibleTolerance = dto.getAlwaysVisibleTolerance();
    token.isAlwaysVisible = dto.getIsAlwaysVisible();
    token.vbl = dto.hasVbl() ? Mapper.map(dto.getVbl()) : null;
    token.hillVbl = dto.hasHillVbl() ? Mapper.map(dto.getHillVbl()) : null;
    token.pitVbl = dto.hasPitVbl() ? Mapper.map(dto.getPitVbl()) : null;
    token.coverVbl = dto.hasCoverVbl() ? Mapper.map(dto.getCoverVbl()) : null;
    token.mbl = dto.hasMbl() ? Mapper.map(dto.getMbl()) : null;
    token.name = dto.getName();
    token.ownerList.addAll(dto.getOwnerListList());
    token.ownerType = dto.getOwnerType();
    token.tokenShape = dto.getTokenShape();
    token.tokenType = dto.getTokenType();
    token.layer = dto.getLayer();
    token.propertyType = dto.getPropertyType();
    token.facing = dto.hasFacing() ? dto.getFacing().getValue() : null;
    token.haloColorValue = dto.hasHaloColor() ? dto.getHaloColor().getValue() : null;
    token.visionOverlayColorValue =
        dto.hasVisionOverlayColor() ? dto.getVisionOverlayColor().getValue() : null;
    token.tokenOpacity = dto.getTokenOpacity();
    token.speechName = dto.getSpeechName();
    token.terrainModifier = dto.getTerrainModifier();
    token.terrainModifierOperation =
        Token.TerrainModifierOperation.valueOf(dto.getTerrainModifierOperation().name());
    token.terrainModifiersIgnored.addAll(
        dto.getTerrainModifiersIgnoredList().stream()
            .map(m -> Token.TerrainModifierOperation.valueOf(m.name()))
            .collect(Collectors.toList()));
    token.isFlippedX = dto.getIsFlippedX();
    token.isFlippedY = dto.getIsFlippedY();
    token.isFlippedIso = dto.getIsFlippedIso();
    token.charsheetImage =
        dto.hasCharsheetImage() ? new MD5Key(dto.getCharsheetImage().getValue()) : null;
    token.portraitImage =
        dto.hasPortraitImage() ? new MD5Key(dto.getPortraitImage().getValue()) : null;

    dto.getUniqueLightSourcesList().stream()
        .map(LightSource::fromDto)
        .forEach(source -> token.uniqueLightSources.put(source.getId(), source));
    token.lightSourceList.addAll(
        dto.getLightSourcesList().stream()
            .map(AttachedLightSource::fromDto)
            .collect(Collectors.toList()));
    token.sightType = dto.hasSightType() ? dto.getSightType().getValue() : null;
    token.hasSight = dto.getHasSight();
    token.hasImageTable = dto.getHasImageTable();
    token.imageTableName = dto.hasImageTableName() ? dto.getImageTableName().getValue() : null;
    token.label = dto.hasLabel() ? dto.getLabel().getValue() : null;
    token.notes = dto.hasNotes() ? dto.getNotes().getValue() : "";
    token.gmNotes = dto.hasGmNotes() ? dto.getGmNotes().getValue() : "";
    token.gmName = dto.hasGmName() ? dto.getGmName().getValue() : "";
    token.notesType = dto.getNotesType();
    token.gmNotesType = dto.getGmNotesType();

    dto.getStateMap()
        .forEach(
            (key, stateDto) -> {
              switch (stateDto.getStateTypeCase()) {
                case BOOL_VALUE -> {
                  var value = stateDto.getBoolValue();
                  token.setState(key, value ? Boolean.TRUE : null);
                }
                case DOUBLE_VALUE -> token.setState(key, new BigDecimal(stateDto.getDoubleValue()));
                default -> log.warn("unknown state type:" + stateDto.getStateTypeCase());
              }
            });
    dto.getPropertiesMap().forEach((k, v) -> token.propertyMapCI.put(k, v.equals("") ? null : v));
    dto.getMacroPropertiesMap()
        .forEach(
            (key, value) ->
                token.macroPropertiesMap.put(key, MacroButtonProperties.fromDto(value)));
    token.speechMap.putAll(dto.getSpeechMap());
    token.heroLabData = dto.hasHeroLabData() ? HeroLabData.fromDto(dto.getHeroLabData()) : null;
    token.allowURIAccess = dto.getAllowUriAccess();
    if (dto.hasStatSheetProperties()) {
      token.statSheet = StatSheetProperties.fromDto(dto.getStatSheetProperties());
    }
    return token;
  }

  public TokenDto toDto() {
    var dto = TokenDto.newBuilder();
    dto.setId(id.toString());
    dto.setBeingImpersonated(beingImpersonated);
    dto.setExposedAreaGuid(exposedAreaGUID.toString());
    for (var key : imageAssetMap.keySet()) {
      var notNullKey = key == null ? "" : key;
      dto.putImageAssetMap(notNullKey, imageAssetMap.get(key).toString());
    }
    if (currentImageAsset != null) {
      dto.setCurrentImageAsset(StringValue.of(currentImageAsset));
    }
    dto.setLastX(lastX);
    dto.setX(x);
    dto.setLastY(lastY);
    dto.setY(y);
    dto.setZ(z);
    dto.setAnchorX(anchorX);
    dto.setAnchorY(anchorY);
    dto.setSizeScale(sizeScale);
    if (lastPath != null) {
      dto.setLastPath(lastPath.toDto());
    }
    dto.setSnapToScale(snapToScale);
    dto.setWidth(width);
    dto.setHeight(height);
    dto.setIsoWidth(isoWidth);
    dto.setIsoHeight(isoHeight);
    dto.setScaleX(scaleX);
    dto.setScaleY(scaleY);
    sizeMap.forEach((k, v) -> dto.putSizeMap(k, v.toString()));
    dto.setSnapToGrid(snapToGrid);
    dto.setIsVisible(isVisible);
    dto.setVisibleOnlyToOwner(visibleOnlyToOwner);
    dto.setVblColorSensitivity(vblColorSensitivity);
    dto.setAlwaysVisibleTolerance(alwaysVisibleTolerance);
    dto.setIsAlwaysVisible(isAlwaysVisible);
    if (vbl != null) {
      dto.setVbl(Mapper.map(vbl));
    }
    if (hillVbl != null) {
      dto.setHillVbl(Mapper.map(hillVbl));
    }
    if (pitVbl != null) {
      dto.setPitVbl(Mapper.map(pitVbl));
    }
    if (coverVbl != null) {
      dto.setCoverVbl(Mapper.map(coverVbl));
    }
    if (mbl != null) {
      dto.setMbl(Mapper.map(mbl));
    }
    dto.setName(name);
    dto.addAllOwnerList(ownerList);
    dto.setOwnerType(ownerType);
    dto.setTokenShape(tokenShape);
    dto.setTokenType(tokenType);
    dto.setLayer(layer);
    dto.setPropertyType(propertyType);
    if (facing != null) {
      dto.setFacing(Int32Value.of(facing));
    }
    if (haloColorValue != null) {
      dto.setHaloColor(Int32Value.of(haloColorValue));
    }
    if (visionOverlayColorValue != null) {
      dto.setVisionOverlayColor(Int32Value.of(visionOverlayColorValue));
    }
    dto.setTokenOpacity(tokenOpacity);
    dto.setSpeechName(speechName);
    dto.setTerrainModifier(terrainModifier);
    dto.setTerrainModifierOperation(
        TerrainModifierOperationDto.valueOf(terrainModifierOperation.name()));
    dto.addAllTerrainModifiersIgnored(
        terrainModifiersIgnored.stream()
            .map(m -> TerrainModifierOperationDto.valueOf(m.name()))
            .collect(Collectors.toList()));
    dto.setIsFlippedX(isFlippedX);
    dto.setIsFlippedY(isFlippedY);
    dto.setIsFlippedIso(isFlippedIso);
    if (charsheetImage != null) {
      dto.setCharsheetImage(StringValue.of(charsheetImage.toString()));
    }
    if (portraitImage != null) {
      dto.setPortraitImage(StringValue.of(portraitImage.toString()));
    }
    dto.addAllUniqueLightSources(
        uniqueLightSources.values().stream().map(LightSource::toDto).collect(Collectors.toList()));
    dto.addAllLightSources(
        lightSourceList.stream().map(AttachedLightSource::toDto).collect(Collectors.toList()));
    if (sightType != null) {
      dto.setSightType(StringValue.of(sightType));
    }
    dto.setHasSight(hasSight);
    dto.setHasImageTable(hasImageTable);
    if (imageTableName != null) {
      dto.setImageTableName(StringValue.of(imageTableName));
    }
    if (label != null) {
      dto.setLabel(StringValue.of(label));
    }
    if (notes != null) {
      dto.setNotes(StringValue.of(notes));
    }
    dto.setNotesType(notesType);
    if (gmNotes != null) {
      dto.setGmNotes(StringValue.of(gmNotes));
    }
    dto.setGmNotesType(gmNotesType);
    if (gmName != null) {
      dto.setGmName(StringValue.of(gmName));
    }
    state.forEach(
        (key, state) -> {
          if (state instanceof Boolean bstate) {
            dto.putState(key, TokenDto.State.newBuilder().setBoolValue(bstate).build());
          } else if (state instanceof Number nstate) {
            dto.putState(
                key, TokenDto.State.newBuilder().setDoubleValue(nstate.doubleValue()).build());
          } else {
            log.warn("unknown state type:" + state.getClass());
          }
        });
    propertyMapCI.forEach((k, v) -> dto.putProperties(k, v != null ? v.toString() : ""));
    macroPropertiesMap.forEach((k, v) -> dto.putMacroProperties(k, v.toDto()));
    dto.putAllSpeech(speechMap);
    if (heroLabData != null) {
      dto.setHeroLabData(heroLabData.toDto());
    }
    dto.setAllowUriAccess(allowURIAccess);
    if (statSheet != null) {
      dto.setStatSheetProperties(StatSheetProperties.toDto(statSheet));
    }
    return dto.build();
  }

  /**
   * Returns the Stat Sheet properties for this token. If no stat sheet is set, the default stat for
   * the token type is returned.
   *
   * @return The of the stat sheet for this token.
   */
  public StatSheetProperties getStatSheet() {
    if (statSheet == null) {
      return MapTool.getCampaign().getTokenTypeDefaultSheetId(propertyType);
    }
    return statSheet;
  }

  /**
   * Sets the id of the stat sheet for this token. If null, the token will use the default stat
   * sheet for the token type.
   *
   * @param statSheet the stat sheet properties for this token.
   */
  public void setStatSheet(StatSheetProperties statSheet) {
    this.statSheet = statSheet;
  }

  /**
   * Returns if the token is using the default stat sheet for its token type.
   *
   * @return <code>true</code> if using the default stat sheet.
   */
  public boolean usingDefaultStatSheet() {
    return statSheet == null;
  }

  /** Use the default stat sheet for the tokens token type. */
  public void useDefaultStatSheet() {
    setStatSheet(null);
  }
}
