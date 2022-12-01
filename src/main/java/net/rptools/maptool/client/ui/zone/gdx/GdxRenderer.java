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
package net.rptools.maptool.client.ui.zone.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.google.common.eventbus.Subscribe;
import com.jogamp.opengl.GL;
import java.awt.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.lib.gdx.GifDecoder;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImageBorder;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.ZoneActivated;
import net.rptools.maptool.client.tool.drawing.FreehandExposeTool;
import net.rptools.maptool.client.tool.drawing.OvalExposeTool;
import net.rptools.maptool.client.tool.drawing.PolygonExposeTool;
import net.rptools.maptool.client.tool.drawing.RectangleExposeTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.Tool;
import net.rptools.maptool.client.ui.token.*;
import net.rptools.maptool.client.ui.zone.DrawableLight;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.GraphicsUtil;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The coordinates in the model are y-down, x-left. The world coordinates are y-up, x-left. I moved
 * the world to the 4th quadrant of the coordinate system. So if you would draw a token t awt at
 * (x,y) you have to draw it at (x, -y - t.width)
 *
 * <p>
 */
public class GdxRenderer extends ApplicationAdapter
    implements AssetAvailableListener {

  private static final Logger log = LogManager.getLogger(GdxRenderer.class);

  public static final float POINTS_PER_BEZIER = 10f;

  private static GdxRenderer _instance;

  private final Map<String, Sprite> fetchedSprites = new HashMap<>();
  private final Map<MD5Key, Sprite> isoSprites = new HashMap<>();
  private final Map<String, TextureRegion> fetchedRegions = new HashMap<>();
  private final Map<MD5Key, Sprite> bigSprites = new HashMap<>();
  private final Map<MD5Key, Animation<TextureRegion>> animationMap = new HashMap<>();
  private final Map<MD5Key, VideoPlayer> videoPlayerMap = new HashMap<>();

  // renderFog
  private final String ATLAS = "net/rptools/maptool/client/maptool.atlas";
  private final String FONT_NORMAL = "normalFont.ttf";
  private final String FONT_DISTANCE = "distanceFont.ttf";
  private PixmapPacker packer;
  private TextureAtlas tokenAtlas;
  private boolean flushFog = true;
  // from renderToken:
  private Area visibleScreenArea;
  private Area exposedFogArea;
  private PlayerView lastView;
  private final List<ItemRenderer> itemRenderList = new LinkedList<>();

  // zone specific resources
  private Zone zone;
  private ZoneRenderer zoneRenderer;

  private int offsetX = 0;
  private int offsetY = 0;
  private float zoom = 1.0f;
  private float stateTime = 0f;
  private boolean renderZone = false;
  private boolean showAstarDebugging = false;

  // general resources
  private PerspectiveCamera cam;
  private OrthographicCamera hudCam;
  private PolygonSpriteBatch batch;
  private boolean initialized = false;
  private int width;
  private int height;
  private BitmapFont normalFont;
  private BitmapFont distanceFont;
  private float distanceFontScale = 0;
  private final CodeTimer timer = new CodeTimer("GdxRenderer.renderZone");
  private FrameBuffer backBuffer;
  private Integer fogX;
  private Integer fogY;
  private com.badlogic.gdx.assets.AssetManager manager;
  private TextureAtlas atlas;
  private Texture onePixel;
  private Texture fog;
  private Texture background;
  private ShapeDrawer drawer;
  private final GlyphLayout glyphLayout = new GlyphLayout();
  private LineTemplateDrawer lineTemplateDrawer;
  private LineCellTemplateDrawer lineCellTemplateDrawer;
  private RadiusTemplateDrawer radiusTemplateDrawer;
  private BurstTemplateDrawer burstTemplateDrawer;
  private ConeTemplateDrawer coneTemplateDrawer;
  private BlastTemplateDrawer blastTemplateDrawer;
  private RadiusCellTemplateDrawer radiusCellTemplateDrawer;
  private ShapeDrawableDrawer shapeDrawableDrawer;
  private TextRenderer textRenderer;

  private TextRenderer hudTextRenderer;
  private AreaRenderer areaRenderer;

  // temorary objects. Stored here to avoid garbage collection;
  private final Vector3 tmpWorldCoord = new Vector3();
  private final Color tmpColor = new Color();
  private final FloatArray tmpFloat = new FloatArray();
  private final Vector2 tmpVector = new Vector2();
  private final Vector2 tmpVectorOut = new Vector2();
  private final Vector2 tmpVector0 = new Vector2();
  private final Vector2 tmpVector1 = new Vector2();
  private final Vector2 tmpVector2 = new Vector2();
  private final Matrix4 tmpMatrix = new Matrix4();
  private final Area tmpArea = new Area();
  private final TiledDrawable tmpTile = new TiledDrawable();

  public GdxRenderer() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  public static GdxRenderer getInstance() {
    if (_instance == null) _instance = new GdxRenderer();
    return _instance;
  }

  @Override
  public void create() {
    // with jogl create is called every time we change the parent frame of the GLJPanel
    // e.g. change from fullcreen to window or the other way around. Reinit everthing in this case.
    if (initialized) {
      initialized = false;
      dispose();

      atlas = null;
      normalFont = null;
      distanceFont = null;
      fetchedSprites.clear();
      isoSprites.clear();
      fetchedRegions.clear();
      bigSprites.clear();
      animationMap.clear();
    }

    tokenAtlas = new TextureAtlas();
    manager = new com.badlogic.gdx.assets.AssetManager();
    loadAssets();
    packer = createPacker();

    var resolver = new InternalFileHandleResolver();
    manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
    manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

    width = Gdx.graphics.getWidth();
    height = Gdx.graphics.getHeight();

    // we don't use an OrthographicCamera here in order to be able to add 3D-Models
    cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.lookAt(0, 0, 0);

    hudCam = new OrthographicCamera();
    hudCam.setToOrtho(false);

    updateCam();

    batch = new PolygonSpriteBatch();

    backBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

    // TODO: Add it to the texture atlas
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.drawPixel(0, 0);
    onePixel = new Texture(pixmap);
    pixmap.dispose();
    TextureRegion region = new TextureRegion(onePixel, 0, 0, 1, 1);
    drawer = new ShapeDrawer(batch, region);

    lineTemplateDrawer = new LineTemplateDrawer(drawer);
    lineCellTemplateDrawer = new LineCellTemplateDrawer(drawer);
    radiusTemplateDrawer = new RadiusTemplateDrawer(drawer);
    burstTemplateDrawer = new BurstTemplateDrawer(drawer);
    coneTemplateDrawer = new ConeTemplateDrawer(drawer);
    blastTemplateDrawer = new BlastTemplateDrawer(drawer);
    radiusCellTemplateDrawer = new RadiusCellTemplateDrawer(drawer);
    shapeDrawableDrawer = new ShapeDrawableDrawer(drawer);
    areaRenderer = new AreaRenderer(drawer);

    initialized = true;
    initializeZoneResources(zone);
  }

  @Override
  public void dispose() {
    manager.dispose();
    batch.dispose();
    disposeZoneResources();
    disposeZoneTextures();
    onePixel.dispose();
    packer.updateTextureAtlas(
        atlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
    packer.dispose();
    tokenAtlas.dispose();
  }

  @Override
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
    backBuffer.dispose();
    backBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

    updateCam();
  }

  private void updateCam() {
    if (cam == null) return;

    cam.viewportWidth = width;
    cam.viewportHeight = height;

    cam.position.x = zoom * (width / 2f + offsetX);
    cam.position.y = zoom * (height / 2f * -1 + offsetY);
    cam.position.z =
        (zoom * height) / (2f * (float) Math.tan(Math.toRadians(cam.fieldOfView / 2f)));
    cam.far = 1.1f * cam.position.z;
    cam.near = 0.1f * cam.position.z;

    cam.update();

    hudCam.viewportWidth = width;
    hudCam.viewportHeight = height;
    hudCam.position.x = width / 2f;
    hudCam.position.y = height / 2f;
    hudCam.update();
  }

  @Override
  public void render() {
    var delta = Gdx.graphics.getDeltaTime();
    stateTime += delta;
    manager.finishLoading();
    packer.updateTextureAtlas(
        tokenAtlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);

    if (atlas == null) {
      atlas = manager.get(ATLAS, TextureAtlas.class);
    }

    if (normalFont == null) {
      normalFont = manager.get(FONT_NORMAL, BitmapFont.class);
      textRenderer = new TextRenderer(atlas, batch, normalFont);
      hudTextRenderer = new TextRenderer(atlas, batch, normalFont, false);
    }

    ensureCorrectDistanceFont();
    ScreenUtils.clear(Color.BLACK);
    doRendering();
  }

  @NotNull
  private PixmapPacker createPacker() {
    return new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 2, false);
  }

  private void ensureCorrectDistanceFont() {
    if (zone == null) return;

    var fontScale =
        (float) zone.getGrid().getSize() / 50; // Font size of 12 at grid size 50 is default

    if (fontScale == this.distanceFontScale && distanceFont != null) return;

    if (distanceFont != null) manager.unload(FONT_DISTANCE);

    var fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    fontParams.fontFileName = "net/rptools/maptool/client/fonts/OpenSans-Bold.ttf";
    fontParams.fontParameters.size = (int) (12 * fontScale);
    manager.load(FONT_DISTANCE, BitmapFont.class, fontParams);
    manager.finishLoading();
    distanceFont = manager.get(FONT_DISTANCE, BitmapFont.class);
    distanceFontScale = fontScale;
  }

  private void loadAssets() {
    manager.load(ATLAS, TextureAtlas.class);

    var mySmallFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    mySmallFont.fontFileName = "net/rptools/maptool/client/fonts/OpenSans-Regular.ttf";
    mySmallFont.fontParameters.size = (int) (12 * Gdx.graphics.getBackBufferScale());
    manager.load(FONT_NORMAL, BitmapFont.class, mySmallFont);
  }

  private void doRendering() {
    batch.enableBlending();
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    // this happens sometimes when starting with ide (non-debug)
    if (batch.isDrawing()) batch.end();
    batch.begin();

    if (zone == null || !renderZone) return;

    initializeTimer();
    if (zoneRenderer == null) return;

    setScale(zoneRenderer.getZoneScale());

    timer.start("paintComponent:createView");
    PlayerView playerView = zoneRenderer.getPlayerView();
    timer.stop("paintComponent:createView");

    setProjectionMatrix(cam.combined);

    renderZone(playerView);

    setProjectionMatrix(hudCam.combined);

    if (zoneRenderer.isLoading())
      hudTextRenderer.drawBoxedString(zoneRenderer.getLoadingProgress(), width / 2f, height / 2f);
    else if (MapTool.getCampaign().isBeingSerialized())
      hudTextRenderer.drawBoxedString("    Please Wait    ", width / 2f, height / 2f);

    float noteVPos = 20 * Gdx.graphics.getBackBufferScale();
    if (!zone.isVisible() && playerView.isGMView()) {
      hudTextRenderer.drawBoxedString(
          I18N.getText("zone.map_not_visible"), width / 2f, height - noteVPos);
      noteVPos += 20 * Gdx.graphics.getBackBufferScale();
    }
    if (AppState.isShowAsPlayer()) {
      hudTextRenderer.drawBoxedString(
          I18N.getText("zone.player_view"), width / 2, height - noteVPos);
    }

    hudTextRenderer.drawString(String.valueOf(Gdx.graphics.getFramesPerSecond()), 10, 10);
    hudTextRenderer.drawString(String.valueOf(batch.renderCalls), width - 10, 10);

    batch.end();
    collectTimerResults();
  }

  private void collectTimerResults() {
    if (timer.isEnabled()) {
      String results = timer.toString();
      MapTool.getProfilingNoteFrame().addText(results);
      if (log.isDebugEnabled()) {
        log.debug(results);
      }
      timer.clear();
    }
  }

  private void initializeTimer() {
    timer.setEnabled(AppState.isCollectProfilingData() || log.isDebugEnabled());
    timer.clear();
    timer.setThreshold(10);
  }

  public void invalidateCurrentViewCache() {
    flushFog = true;
    drawableLights = null;
    drawableAuras = null;
    visibleScreenArea = null;
    lastView = null;

    var zoneView = zoneRenderer.getZoneView();
    if (zoneView != null) {
      zoneView.flush();
    }
  }

  private void renderZone(PlayerView view) {
    if (zoneRenderer.isLoading() || MapTool.getCampaign().isBeingSerialized()) return;

    if (lastView != null && !lastView.equals(view)) {
      invalidateCurrentViewCache();
    }
    lastView = view;
    itemRenderList.clear();

    // Calculations
    timer.start("calcs-1");
    timer.start("ZoneRenderer-getVisibleArea");
    if (visibleScreenArea == null) {
      visibleScreenArea = zoneRenderer.getZoneView().getVisibleArea(zoneRenderer.getPlayerView());
    }
    timer.stop("ZoneRenderer-getVisibleArea");

    timer.stop("calcs-1");
    timer.start("calcs-2");
    exposedFogArea = new Area(zone.getExposedArea());
    timer.stop("calcs-2");

    renderBoard();

    if (Zone.Layer.BACKGROUND.isEnabled()) {
      List<DrawnElement> drawables = zone.getBackgroundDrawnElements();

      timer.start("drawableBackground");
      renderDrawableOverlay(view, drawables);
      timer.stop("drawableBackground");

      List<Token> background = zone.getBackgroundStamps(false);
      if (!background.isEmpty()) {
        timer.start("tokensBackground");
        renderTokens(background, view, false);
        timer.stop("tokensBackground");
      }
    }
    if (Zone.Layer.OBJECT.isEnabled()) {
      // Drawables on the object layer are always below the grid, and...
      List<DrawnElement> drawables = zone.getObjectDrawnElements();
      // if (!drawables.isEmpty()) {
      timer.start("drawableObjects");
      renderDrawableOverlay(view, drawables);
      timer.stop("drawableObjects");
      // }
    }
    timer.start("grid");

    renderGrid(view);

    timer.stop("grid");

    if (Zone.Layer.OBJECT.isEnabled()) {
      // ... Images on the object layer are always ABOVE the grid.
      List<Token> stamps = zone.getStampTokens(false);
      if (!stamps.isEmpty()) {
        timer.start("tokensStamp");
        renderTokens(stamps, view, false);
        timer.stop("tokensStamp");
      }
    }
    if (Zone.Layer.TOKEN.isEnabled()) {
      timer.start("lights");
      renderLights(view);
      timer.stop("lights");

      timer.start("auras");
      renderAuras(view);
      timer.stop("auras");
    }

    /*
     * The following sections used to handle rendering of the Hidden (i.e. "GM") layer followed by
     * the Token layer. The problem was that we want all drawables to appear below all tokens, and
     * the old configuration performed the rendering in the following order:
     *
     * <ol>
     *   <li>Render Hidden-layer tokens
     *   <li>Render Hidden-layer drawables
     *   <li>Render Token-layer drawables
     *   <li>Render Token-layer tokens
     * </ol>
     *
     * That's fine for players, but clearly wrong if the view is for the GM. We now use:
     *
     * <ol>
     *   <li>Render Token-layer drawables // Player-drawn images shouldn't obscure GM's images?
     *   <li>Render Hidden-layer drawables // GM could always use "View As Player" if needed?
     *   <li>Render Hidden-layer tokens
     *   <li>Render Token-layer tokens
     * </ol>
     */
    if (Zone.Layer.TOKEN.isEnabled()) {
      List<DrawnElement> drawables = zone.getDrawnElements();
      // if (!drawables.isEmpty()) {
      timer.start("drawableTokens");
      renderDrawableOverlay(view, drawables);
      timer.stop("drawableTokens");
      // }

      if (view.isGMView() && Zone.Layer.GM.isEnabled()) {
        drawables = zone.getGMDrawnElements();
        // if (!drawables.isEmpty()) {
        timer.start("drawableGM");
        renderDrawableOverlay(view, drawables);
        timer.stop("drawableGM");
        // }
        List<Token> stamps = zone.getGMStamps(false);
        if (!stamps.isEmpty()) {
          timer.start("tokensGM");
          renderTokens(stamps, view, false);
          timer.stop("tokensGM");
        }
      }
      List<Token> tokens = zone.getTokens(false);
      if (!tokens.isEmpty()) {
        timer.start("tokens");
        renderTokens(tokens, view, false);
        timer.stop("tokens");
      }
      timer.start("unowned movement");
      showBlockedMoves(view, zoneRenderer.getUnOwnedMovementSet(view));
      timer.stop("unowned movement");
    }

    /*
     * FJE It's probably not appropriate for labels to be above everything, including tokens. Above
     * drawables, yes. Above tokens, no. (Although in that case labels could be completely obscured.
     * Hm.)
     */
    // Drawing labels is slooooow. :(
    // Perhaps we should draw the fog first and use hard fog to determine whether labels need to be
    // drawn?
    // (This method has it's own 'timer' calls)
    if (AppState.getShowTextLabels()) {
      renderLabels(view);
    }

    // (This method has it's own 'timer' calls)
    if (zone.hasFog()) {
      renderFog(view);
    }

    if (Zone.Layer.TOKEN.isEnabled()) {
      // Jamz: If there is fog or vision we may need to re-render vision-blocking type tokens
      // For example. this allows a "door" stamp to block vision but still allow you to see the
      // door.
      List<Token> vblTokens = zone.getTokensAlwaysVisible();
      if (!vblTokens.isEmpty()) {
        timer.start("tokens - always visible");
        renderTokens(vblTokens, view, true);
        timer.stop("tokens - always visible");
      }

      // if there is fog or vision we may need to re-render figure type tokens
      // and figure tokens need sorting via alternative logic.
      List<Token> tokens = zone.getFigureTokens();
      List<Token> sortedTokens = new ArrayList<>(tokens);
      sortedTokens.sort(zone.getFigureZOrderComparator());
      if (!tokens.isEmpty()) {
        timer.start("tokens - figures");
        renderTokens(sortedTokens, view, true);
        timer.stop("tokens - figures");
      }

      timer.start("owned movement");
      showBlockedMoves(view, zoneRenderer.getOwnedMovementSet(view));
      timer.stop("owned movement");

      // Text associated with tokens being moved is added to a list to be drawn after, i.e. on top
      // of, the tokens
      // themselves.
      // So if one moving token is on top of another moving token, at least the textual identifiers
      // will be
      // visible.

      setProjectionMatrix(hudCam.combined);
      timer.start("token name/labels");
      renderRenderables();
      timer.stop("token name/labels");
      setProjectionMatrix(cam.combined);
    }

    // if (zone.visionType ...)
    if (view.isGMView()) {
      timer.start("visionOverlayGM");
      renderGMVisionOverlay(view);
      timer.stop("visionOverlayGM");
    } else {
      timer.start("visionOverlayPlayer");
      renderPlayerVisionOverlay(view);
      timer.stop("visionOverlayPlayer");
    }
  }

  private void renderPlayerVisionOverlay(PlayerView view) {
    /* //  This doesn't seem to have any effect ??
    if (zone.hasFog()) {
           Area clip = new Area(new Rectangle(getSize().width, getSize().height));

           Area viewArea = new Area(exposedFogArea);
           List<Token> tokens = view.getTokens();
           if (tokens != null && !tokens.isEmpty()) {
               for (Token tok : tokens) {
                   ExposedAreaMetaData exposedMeta = zone.getExposedAreaMetaData(tok.getExposedAreaGUID());
                   viewArea.add(exposedMeta.getExposedAreaHistory());
               }
           }
           if (!viewArea.isEmpty()) {
               clip.intersect(new Area(viewArea.getBounds2D()));
           }
           // Note: the viewArea doesn't need to be transform()'d because exposedFogArea has been
           // already.
           g2.setClip(clip);
       }*/
    renderVisionOverlay(view);
  }

  private void renderGMVisionOverlay(PlayerView view) {
    renderVisionOverlay(view);
  }

  private void renderVisionOverlay(PlayerView view) {
    var tokenUnderMouse = zoneRenderer.getTokenUnderMouse();
    Area currentTokenVisionArea = zoneRenderer.getVisibleArea(tokenUnderMouse);
    if (currentTokenVisionArea == null) {
      return;
    }
    Area combined = new Area(currentTokenVisionArea);
    ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tokenUnderMouse.getExposedAreaGUID());

    Area tmpArea = new Area(meta.getExposedAreaHistory());
    tmpArea.add(zone.getExposedArea());
    if (zone.hasFog()) {
      if (tmpArea.isEmpty()) {
        return;
      }
      combined.intersect(tmpArea);
    }
    boolean isOwner = AppUtil.playerOwns(tokenUnderMouse);
    boolean tokenIsPC = tokenUnderMouse.getType() == Token.Type.PC;
    boolean strictOwnership =
        MapTool.getServerPolicy() != null && MapTool.getServerPolicy().useStrictTokenManagement();
    boolean showVisionAndHalo = isOwner || view.isGMView() || (tokenIsPC && !strictOwnership);

    /*
     * The vision arc and optional halo-filled visible area shouldn't be shown to everyone. If we are in GM view, or if we are the owner of the token in question, or if the token is a PC and
     * strict token ownership is off... then the vision arc should be displayed.
     */
    if (showVisionAndHalo) {
      drawer.setColor(Color.WHITE);
      areaRenderer.drawArea(combined);
      renderHaloArea(combined);
    }
  }

  private void renderHaloArea(Area visible) {
    var tokenUnderMouse = zoneRenderer.getTokenUnderMouse();
    if (tokenUnderMouse == null) return;

    boolean useHaloColor =
        tokenUnderMouse.getHaloColor() != null && AppPreferences.getUseHaloColorOnVisionOverlay();
    if (tokenUnderMouse.getVisionOverlayColor() != null || useHaloColor) {
      java.awt.Color visionColor =
          useHaloColor ? tokenUnderMouse.getHaloColor() : tokenUnderMouse.getVisionOverlayColor();

      drawer.setColor(
          visionColor.getRed() / 255f,
          visionColor.getGreen() / 255f,
          visionColor.getBlue() / 255f,
          AppPreferences.getHaloOverlayOpacity() / 255f);
      areaRenderer.fillArea(visible);
    }
  }

  private void renderRenderables() {
    for (ItemRenderer renderer : itemRenderList) {
      renderer.render(cam, zoom);
    }
  }

  private void renderFog(PlayerView view) {
    Area combined = null;

    if (!flushFog
        && fogX != null
        && fogY != null
        && (fogX != zoneRenderer.getViewOffsetX() || fogY != zoneRenderer.getViewOffsetY())) {
      flushFog = true;
    }
    boolean cacheNotValid =
        (backBuffer.getColorBufferTexture().getWidth() != width
            || backBuffer.getColorBufferTexture().getHeight() != height);
    timer.start("renderFog");
    //  if (flushFog || cacheNotValid)
    {
      backBuffer.begin();
      ScreenUtils.clear(Color.CLEAR);
      batch.setBlendFunction(GL20.GL_ONE, GL20.GL_NONE);
      setProjectionMatrix(cam.combined);

      timer.start("renderFog-allocateBufferedImage");
      timer.stop("renderFog-allocateBufferedImage");
      fogX = zoneRenderer.getViewOffsetX();
      fogY = zoneRenderer.getViewOffsetY();

      timer.start("renderFog-fill");

      // Fill
      batch.setColor(Color.WHITE);
      var paint = zone.getFogPaint();
      if (paint instanceof DrawableColorPaint) {
        Color.argb8888ToColor(tmpColor, ((DrawableColorPaint) paint).getColor());
        tmpColor.set(tmpColor.r, tmpColor.g, tmpColor.b, view.isGMView() ? .6f : 1f);
        drawer.setColor(tmpColor);
        drawer.filledRectangle(
            cam.position.x - width * zoom / 2f,
            cam.position.y - height * zoom / 2f,
            width * zoom,
            height * zoom);
      } else {
        if (fog == null) {
          var texturePaint = (DrawableTexturePaint) paint;
          var image = texturePaint.getAsset().getData();
          var pix = new Pixmap(image, 0, image.length);
          fog = new Texture(pix);
          fog.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
          pix.dispose();
        }
        fillViewportWith(fog);
      }

      timer.start("renderFog-visibleArea");
      Area visibleArea = zoneRenderer.getZoneView().getVisibleArea(view);
      timer.stop("renderFog-visibleArea");

      String msg = null;
      if (timer.isEnabled()) {
        List<Token> list = view.getTokens();
        msg = "renderFog-combined(" + (list == null ? 0 : list.size()) + ")";
      }
      timer.start(msg);
      combined = zone.getExposedArea(view);
      timer.stop(msg);

      timer.start("renderFogArea");
      Area exposedArea = null;
      Area tempArea = new Area();
      boolean combinedView =
          !zoneRenderer.getZoneView().isUsingVision()
              || MapTool.isPersonalServer()
              || !MapTool.getServerPolicy().isUseIndividualFOW()
              || view.isGMView();

      drawer.setColor(Color.CLEAR);

      if (view.getTokens() != null) {
        // if there are tokens selected combine the areas, then, if individual FOW is enabled
        // we pass the combined exposed area to build the soft FOW and visible area.
        for (Token tok : view.getTokens()) {
          ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tok.getExposedAreaGUID());
          exposedArea = meta.getExposedAreaHistory();
          tempArea.add(new Area(exposedArea));
        }
        if (combinedView) {
          areaRenderer.fillArea(combined);
          renderFogArea(combined, visibleArea);
          renderFogOutline();
        } else {
          // 'combined' already includes the area encompassed by 'tempArea', so just
          // use 'combined' instead in this block of code?
          tempArea.add(combined);

          areaRenderer.fillArea(tempArea);
          renderFogArea(tempArea, visibleArea);
          renderFogOutline();
        }
      } else {
        // No tokens selected, so if we are using Individual FOW, we build up all the owned tokens
        // exposed area's to build the soft FOW.
        if (combinedView) {
          if (combined.isEmpty()) {
            combined = zone.getExposedArea();
          }
          areaRenderer.fillArea(combined);
          renderFogArea(combined, visibleArea);
          renderFogOutline();
        } else {
          Area myCombined = new Area();
          List<Token> myToks = zone.getTokens();
          for (Token tok : myToks) {
            if (!AppUtil.playerOwns(
                tok)) { // Only here if !isGMview() so should the tokens already be in
              // PlayerView.getTokens()?
              continue;
            }
            ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tok.getExposedAreaGUID());
            exposedArea = meta.getExposedAreaHistory();
            myCombined.add(new Area(exposedArea));
          }
          areaRenderer.fillArea(myCombined);
          renderFogArea(myCombined, visibleArea);
          renderFogOutline();
        }
      }
      timer.stop("renderFogArea");

      flushFog = false;
      // createScreenShot("fog");
      batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      backBuffer.end();
    }

    setProjectionMatrix(hudCam.combined);
    batch.setColor(Color.WHITE);
    batch.draw(
        backBuffer.getColorBufferTexture(), 0, 0, width, height, 0, 0, width, height, false, true);

    setProjectionMatrix(cam.combined);
    timer.stop("renderFog");
  }

  private void setProjectionMatrix(Matrix4 matrix) {
    batch.setProjectionMatrix(matrix);
    drawer.update();
  }

  private void renderFogArea(Area softFog, Area visibleArea) {
    if (zoneRenderer.getZoneView().isUsingVision()) {
      if (visibleArea != null && !visibleArea.isEmpty()) {
        drawer.setColor(0, 0, 0, AppPreferences.getFogOverlayOpacity() / 255.0f);

        // Fill in the exposed area
        areaRenderer.fillArea(softFog);

        // batch.setColor(Color.CLEAR);
        drawer.setColor(Color.CLEAR);

        areaRenderer.fillArea(visibleArea);
      } else {
        drawer.setColor(0, 0, 0, 80 / 255.0f);
        areaRenderer.fillArea(softFog);
        drawer.setColor(Color.WHITE);
      }
    } else {
      areaRenderer.fillArea(softFog);
    }
  }

  private void renderFogOutline() {
    if (visibleScreenArea == null) return;

    drawer.setColor(Color.BLACK);
    areaRenderer.drawArea(visibleScreenArea);
  }

  private void renderLabels(PlayerView view) {
    timer.start("labels-1");

    for (Label label : zone.getLabels()) {
      timer.start("labels-1.1");
      Color.argb8888ToColor(tmpColor, label.getForegroundColor().getRGB());
      if (label.isShowBackground()) {
        textRenderer.drawBoxedString(
            label.getLabel(),
            label.getX(),
            -label.getY(),
            SwingUtilities.CENTER,
            TextRenderer.Background.Gray,
            tmpColor);
      } else {
        textRenderer.drawString(label.getLabel(), label.getX(), -label.getY(), tmpColor);
      }
      timer.stop("labels-1.1");
    }
    timer.stop("labels-1");
  }

  private void showBlockedMoves(PlayerView view, Set<ZoneRenderer.SelectionSet> movementSet) {
    var selectionSetMap = zoneRenderer.getSelectionSetMap();
    if (selectionSetMap.isEmpty()) {
      return;
    }

    boolean clipInstalled = false;
    for (ZoneRenderer.SelectionSet set : movementSet) {
      Token keyToken = zone.getToken(set.getKeyToken());
      if (keyToken == null) {
        // It was removed ?
        selectionSetMap.remove(set.getKeyToken());
        continue;
      }
      // Hide the hidden layer
      if (keyToken.getLayer() == Zone.Layer.GM && !view.isGMView()) {
        continue;
      }
      ZoneWalker walker = set.getWalker();

      for (GUID tokenGUID : set.getTokens()) {
        Token token = zone.getToken(tokenGUID);

        // Perhaps deleted?
        if (token == null) {
          continue;
        }

        // Don't bother if it's not visible
        if (!token.isVisible() && !view.isGMView()) {
          continue;
        }

        // ... or if it's visible only to the owner and that's not us!
        if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
          continue;
        }

        // ... or there are no lights/visibleScreen and you are not the owner or gm and there is fow
        // or vision
        if (!view.isGMView()
            && !AppUtil.playerOwns(token)
            && visibleScreenArea == null
            && zone.hasFog()
            && zoneRenderer.getZoneView().isUsingVision()) {
          continue;
        }

        // ... or if it doesn't have an image to display. (Hm, should still show *something*?)
        Asset asset = AssetManager.getAsset(token.getImageAssetId());
        if (asset == null) {
          continue;
        }

        // OPTIMIZE: combine this with the code in renderTokens()
        java.awt.Rectangle footprintBounds = token.getBounds(zone);

        // get token image, using image table if present
        Sprite image = getSprite(token.getImageAssetId());
        if (image == null) continue;

        // Vision visibility
        boolean isOwner = view.isGMView() || AppUtil.playerOwns(token); // ||
        // set.getPlayerId().equals(MapTool.getPlayer().getName());
        if (!view.isGMView() && visibleScreenArea != null && !isOwner) {
          // FJE Um, why not just assign the clipping area at the top of the routine?
          // TODO: Path clipping
          if (!clipInstalled) {
            // Only show the part of the path that is visible
            //      Area visibleArea = new Area(g.getClipBounds());
            //      visibleArea.intersect(visibleScreenArea);

            //      g = (Graphics2D) g.create();
            //      g.setClip(new GeneralPath(visibleArea));

            clipInstalled = true;
            // System.out.println("Adding Clip: " + MapTool.getPlayer().getName());
          }
        }
        // Show path only on the key token on token layer that are visible to the owner or gm while
        // fow and vision is on
        if (token == keyToken && !token.isStamp()) {
          renderPath(
              walker != null ? walker.getPath() : set.getGridlessPath(),
              token.getFootprint(zone.getGrid()));
        }

        // Show current Blocked Movement directions for A*
        if (walker != null && (log.isDebugEnabled() || showAstarDebugging)) {
          Map<CellPoint, Set<CellPoint>> blockedMovesByTarget = walker.getBlockedMoves();
          // Color currentColor = g.getColor();
          for (var entry : blockedMovesByTarget.entrySet()) {
            var position = entry.getKey();
            var blockedMoves = entry.getValue();

            for (CellPoint point : blockedMoves) {
              ZonePoint zp = point.midZonePoint(zoneRenderer.getZone().getGrid(), position);
              double r = (zp.x - 1) * 45;
              showBlockedMoves(zp, r, getSprite("block_move"), 1.0f);
            }
          }
        }

        footprintBounds.x += set.getOffsetX();
        footprintBounds.y += set.getOffsetY();

        prepareTokenSprite(image, token, footprintBounds);
        image.draw(batch);

        // Other details
        if (token == keyToken) {
          var x = footprintBounds.x;
          var y = footprintBounds.y;
          var w = footprintBounds.width;
          var h = footprintBounds.height;

          Grid grid = zone.getGrid();
          boolean checkForFog =
              MapTool.getServerPolicy().isUseIndividualFOW()
                  && zoneRenderer.getZoneView().isUsingVision();
          boolean showLabels = isOwner;
          if (checkForFog) {
            Path<? extends AbstractPoint> path =
                set.getWalker() != null ? set.getWalker().getPath() : set.getGridlessPath();
            List<? extends AbstractPoint> thePoints = path.getCellPath();

            // now that we have the last point, we can check to see if it's gridless or not. If not
            // gridless, get the last point the token was at and see if the token's footprint is
            // inside
            // the visible area to show the label.

            if (thePoints.isEmpty()) {
              showLabels = false;
            } else {
              AbstractPoint lastPoint = thePoints.get(thePoints.size() - 1);

              java.awt.Rectangle tokenRectangle = null;
              if (lastPoint instanceof CellPoint) {
                tokenRectangle = token.getFootprint(grid).getBounds(grid, (CellPoint) lastPoint);
              } else {
                java.awt.Rectangle tokBounds = token.getBounds(zone);
                tokenRectangle = new java.awt.Rectangle();
                tokenRectangle.setBounds(
                    lastPoint.x,
                    lastPoint.y,
                    (int) tokBounds.getWidth(),
                    (int) tokBounds.getHeight());
              }
              showLabels =
                  showLabels
                      || zoneRenderer.getZoneView().getVisibleArea(view).intersects(tokenRectangle);
            }
          } else {
            boolean hasFog = zone.hasFog();
            boolean fogIntersects = exposedFogArea.intersects(footprintBounds);
            showLabels = showLabels || (visibleScreenArea == null && !hasFog); // no vision - fog
            showLabels =
                showLabels
                    || (visibleScreenArea == null && hasFog && fogIntersects); // no vision + fog
            showLabels =
                showLabels
                    || (visibleScreenArea != null
                        && visibleScreenArea.intersects(footprintBounds)
                        && fogIntersects); // vision
          }
          if (showLabels) {

            y += 10 + h;
            x += w / 2;

            if (!token.isStamp() && AppState.getShowMovementMeasurements()) {
              String distance = "";
              if (walker != null) { // This wouldn't be true unless token.isSnapToGrid() &&
                // grid.isPathingSupported()
                double distanceTraveled = walker.getDistance();
                if (distanceTraveled >= 0) {
                  distance = NumberFormat.getInstance().format(distanceTraveled);
                }
              } else {
                double c = 0;
                ZonePoint lastPoint = null;
                for (ZonePoint zp : set.getGridlessPath().getCellPath()) {
                  if (lastPoint == null) {
                    lastPoint = zp;
                    continue;
                  }
                  int a = lastPoint.x - zp.x;
                  int b = lastPoint.y - zp.y;
                  c += Math.hypot(a, b);
                  lastPoint = zp;
                }
                c /= zone.getGrid().getSize(); // Number of "cells"
                c *= zone.getUnitsPerCell(); // "actual" distance traveled
                distance = NumberFormat.getInstance().format(c);
              }
              if (!distance.isEmpty()) {
                itemRenderList.add(new LabelRenderer(distance, x, -y, textRenderer));
                y += 20;
              }
            }
            if (set.getPlayerId() != null && set.getPlayerId().length() >= 1) {
              itemRenderList.add(new LabelRenderer(set.getPlayerId(), x, -y, textRenderer));
            }
          } // showLabels
        } // token == keyToken
      }
    }
  }

  private void showBlockedMoves(ZonePoint zp, double angle, Sprite image, float size) {
    // Resize image to size of 1/4 size of grid
    var resizeWidth = (float) zone.getGrid().getCellWidth() / image.getWidth() * .25f;
    var resizeHeight = (float) zone.getGrid().getCellHeight() / image.getHeight() * .25f;

    var w = image.getWidth() * resizeWidth * size;
    var h = image.getHeight() * resizeHeight * size;

    image.setSize(w, h);
    image.setPosition(zp.x - w / 2f, -(zp.y - h / 2f));
    image.draw(batch);
  }

  /** Holds the auras from lightSourceMap after they have been combined. */
  private List<DrawableLight> drawableAuras;

  private void renderAuras(PlayerView view) {
    var alpha = AppPreferences.getAuraOverlayOpacity() / 255.0f;

    timer.start("auras-4");

    for (DrawableLight light : zoneRenderer.getZoneView().getLights(LightSource.Type.AURA)) {
      var paint = light.getPaint();
      if (paint != null && paint instanceof DrawableColorPaint) {
        var colorPaint = (DrawableColorPaint) paint;
        Color.argb8888ToColor(tmpColor, colorPaint.getColor());
        tmpColor.a = alpha;
      } else {
        tmpColor.set(1, 1, 1, 0.59f);
      }
      drawer.setColor(tmpColor);
      areaRenderer.fillArea(light.getArea());
    }

    timer.stop("auras-4");
  }

  /**
   * Cached set of lights arranged by lumens for some stability. TODO Token draw order would be
   * nice.
   */
  private List<DrawableLight> drawableLights = null;

  private void renderLights(PlayerView view) {

    // Collect and organize lights
    timer.start("renderLights:getLights");
    if (drawableLights == null) {
      timer.start("renderLights:populateCache");
      drawableLights = new ArrayList<>(zoneRenderer.getZoneView().getDrawableLights(view));
      drawableLights.removeIf(light -> light.getType() != LightSource.Type.NORMAL);
      timer.stop("renderLights:populateCache");
    }
    timer.start("renderLights:filterLights");
    final var darknessLights =
        drawableLights.stream().filter(light -> light.getLumens() < 0).toList();
    final var nonDarknessLights =
        drawableLights.stream().filter(light -> light.getLumens() >= 0).toList();
    timer.stop("renderLights:filterLights");
    timer.stop("renderLights:getLights");

    timer.start("renderLights:renderLightOverlay");
    renderLightOverlay(
        GL.GL_ALPHA,
        GL.GL_ONE_MINUS_SRC_ALPHA,
        new Color(1.0f, 1.0f, 1.0f, AppPreferences.getLightOverlayOpacity() / 255.0f),
        view.isGMView() ? null : ZoneRenderer.LightOverlayClipStyle.CLIP_TO_VISIBLE_AREA,
        nonDarknessLights,
        new java.awt.Color(255, 255, 255, 255),
        1.0f);
    timer.stop("renderLights:renderLightOverlay");

    // Players should not be able to discern the nature of the darkness, so we always render it as
    // black for them.
    timer.start("renderLights:renderDarknessOverlay");
    renderLightOverlay(
        GL.GL_ALPHA,
        GL.GL_ONE_MINUS_SRC_ALPHA,
        view.isGMView()
            ? new Color(1.0f, 1.0f, 1.0f, AppPreferences.getLightOverlayOpacity() / 255.0f)
            : Color.BLACK,
        view.isGMView() ? null : ZoneRenderer.LightOverlayClipStyle.CLIP_TO_NOT_VISIBLE_AREA,
        darknessLights,
        new java.awt.Color(0, 0, 0, 255),
        1.0f);
    timer.stop("renderLights:renderDarknessOverlay");
  }

  /**
   * Combines a set of lights into an image that is then rendered into the zone.
   *
   * @param clipStyle How to clip the overlay relative to the visible area. Set to null for no extra
   *     clipping.
   * @param lights The lights that will be rendered and blended.
   * @param defaultPaint A default paint for lights without a paint.
   * @param overlayOpacity The opacity used when rendering the final overlay on top of the zone.
   */
  private void renderLightOverlay(
      int blendSrcFunc,
      int blendDstFunc,
      Color tintColor,
      @Nullable ZoneRenderer.LightOverlayClipStyle clipStyle,
      List<DrawableLight> lights,
      Paint defaultPaint,
      float overlayOpacity) {
    if (lights.isEmpty()) {
      // No points spending resources accomplishing nothing.
      return;
    }

    // Set up a buffer image for lights to be drawn onto before the map
    timer.start("renderLightOverlay:allocateBuffer");
    backBuffer.begin();
    ScreenUtils.clear(Color.CLEAR);
    setProjectionMatrix(cam.combined);
    batch.setBlendFunction(blendSrcFunc, blendDstFunc);
    drawer.update();

    timer.stop("renderLightOverlay:allocateBuffer");
    drawer.setColor(tintColor);
    // Draw lights onto the buffer image so the map doesn't affect how they blend
    timer.start("renderLightOverlay:drawLights");
    for (var light : lights) {
      var paint = light.getPaint() != null ? light.getPaint().getPaint() : defaultPaint;

      if (paint instanceof DrawableColorPaint) {
        var colorPaint = (DrawableColorPaint) paint;
        Color.argb8888ToColor(tmpColor, colorPaint.getColor());

      } else if (paint instanceof java.awt.Color) {
        Color.argb8888ToColor(tmpColor, ((java.awt.Color) paint).getRGB());
      } else {
        System.out.println("unexpected color type");
        continue;
      }
      drawer.setColor(tmpColor);
      var areaToPaint = (Area) light.getArea().clone();
      if (clipStyle != null && visibleScreenArea != null) {
        switch (clipStyle) {
          case CLIP_TO_VISIBLE_AREA -> areaToPaint.intersect(visibleScreenArea);
          case CLIP_TO_NOT_VISIBLE_AREA -> areaToPaint.subtract(visibleScreenArea);
        }
      }
      areaRenderer.fillArea(areaToPaint);
    }
    drawer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    timer.stop("renderLightOverlay:drawLights");

    // Draw the buffer image with all the lights onto the map
    timer.start("renderLightOverlay:drawBuffer");
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    backBuffer.end();

    setProjectionMatrix(hudCam.combined);
    batch.setColor(1.0f, 1.0f, 1.0f, overlayOpacity);
    batch.draw(
        backBuffer.getColorBufferTexture(), 0, 0, width, height, 0, 0, width, height, false, true);
    batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    setProjectionMatrix(cam.combined);
    timer.stop("renderLightOverlay:drawBuffer");
  }

  private void renderGrid(PlayerView view) {
    var grid = zone.getGrid();
    var scale = (float) zoneRenderer.getScale();
    int gridSize = (int) (grid.getSize() * scale);

    if (!AppState.isShowGrid() || gridSize < ZoneRenderer.MIN_GRID_SIZE) {
      return;
    }

    setProjectionMatrix(hudCam.combined);

    if (grid instanceof GridlessGrid) {
      // do nothing
    } else if (grid instanceof HexGrid) {
      renderGrid((HexGrid) grid);
    } else if (grid instanceof SquareGrid) {
      renderGrid((SquareGrid) grid);
    } else if (grid instanceof IsometricGrid) {
      renderGrid((IsometricGrid) grid);
    }
    setProjectionMatrix(cam.combined);
  }

  private void renderGrid(HexGrid grid) {

    Color.argb8888ToColor(tmpColor, zone.getGridColor());

    drawer.setColor(tmpColor);
    var path = grid.createShape(zoneRenderer.getScale());
    areaRenderer.pathToFloatArray(path.getPathIterator(null));

    int offU = grid.getOffU(zoneRenderer);
    int offV = grid.getOffV(zoneRenderer);

    int count = 0;

    var lineWidth = AppState.getGridSize();

    for (double v = offV % (grid.getScaledMinorRadius() * 2) - (grid.getScaledMinorRadius() * 2);
        v < grid.getRendererSizeV(zoneRenderer);
        v += grid.getScaledMinorRadius()) {
      double offsetU =
          (int)
              ((count & 1) == 0
                  ? 0
                  : -(grid.getScaledEdgeProjection() + grid.getScaledEdgeLength()));
      count++;

      double start =
          offU % (2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection())
              - (2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection());
      double end =
          grid.getRendererSizeU(zoneRenderer)
              + 2 * grid.getScaledEdgeLength()
              + 2 * grid.getScaledEdgeProjection();
      double incr = 2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection();
      for (double u = start; u < end; u += incr) {
        float transX = 0;
        float transY = 0;
        if (grid instanceof HexGridVertical) {
          transX = (float) (u + offsetU);
          transY = height - (float) v;
        } else {
          transX = (float) v;
          transY = (float) (-u - offsetU) + height;
        }

        tmpMatrix.translate(transX, transY, 0);
        batch.setTransformMatrix(tmpMatrix);
        drawer.update();

        drawer.path(tmpFloat.toArray(), lineWidth, JoinType.SMOOTH, true);
        tmpMatrix.idt();
        batch.setTransformMatrix(tmpMatrix);
        drawer.update();
      }
    }
  }

  private void renderGrid(IsometricGrid grid) {
    var scale = (float) zoneRenderer.getScale();
    int gridSize = (int) (grid.getSize() * scale);

    Color.argb8888ToColor(tmpColor, zone.getGridColor());

    drawer.setColor(tmpColor);

    var x = hudCam.position.x - hudCam.viewportWidth / 2;
    var y = hudCam.position.y - hudCam.viewportHeight / 2;
    var w = hudCam.viewportWidth;
    var h = hudCam.viewportHeight;

    double isoHeight = grid.getSize() * scale;
    double isoWidth = grid.getSize() * 2 * scale;

    int offX = (int) (zoneRenderer.getViewOffsetX() % isoWidth + grid.getOffsetX() * scale) + 1;
    int offY = (int) (zoneRenderer.getViewOffsetY() % gridSize + grid.getOffsetY() * scale) + 1;

    int startCol = (int) ((int) (x / isoWidth) * isoWidth);
    int startRow = (int) ((int) (y / gridSize) * gridSize);

    for (double row = startRow; row < y + h + gridSize; row += gridSize) {
      for (double col = startCol; col < x + w + isoWidth; col += isoWidth) {
        drawHatch(grid, (int) (col + offX), h - (int) (row + offY));
      }
    }

    for (double row = startRow - (isoHeight / 2); row < y + h + gridSize; row += gridSize) {
      for (double col = startCol - (isoWidth / 2); col < x + w + isoWidth; col += isoWidth) {
        drawHatch(grid, (int) (col + offX), h - (int) (row + offY));
      }
    }
  }

  private void drawHatch(IsometricGrid grid, float x, float y) {
    double isoWidth = grid.getSize() * zoneRenderer.getScale();
    int hatchSize = isoWidth > 10 ? (int) isoWidth / 8 : 2;

    var lineWidth = AppState.getGridSize();

    drawer.line(x - (hatchSize * 2), y - hatchSize, x + (hatchSize * 2), y + hatchSize, lineWidth);
    drawer.line(x - (hatchSize * 2), y + hatchSize, x + (hatchSize * 2), y - hatchSize, lineWidth);
  }

  private void renderGrid(SquareGrid grid) {
    var scale = (float) zoneRenderer.getScale();
    float gridSize = (grid.getSize() * scale);
    Color.argb8888ToColor(tmpColor, zone.getGridColor());

    drawer.setColor(tmpColor);

    var x = hudCam.position.x - hudCam.viewportWidth / 2;
    var y = hudCam.position.y - hudCam.viewportHeight / 2;
    var w = hudCam.viewportWidth;
    var h = hudCam.viewportHeight;

    var offX = Math.round(zoneRenderer.getViewOffsetX() % gridSize + grid.getOffsetX() * scale);
    var offY = Math.round(zoneRenderer.getViewOffsetY() % gridSize + grid.getOffsetY() * scale);

    var startCol = ((int) (x / gridSize) * gridSize);
    var startRow = ((int) (y / gridSize) * gridSize);

    var lineWidth = AppState.getGridSize();

    for (float row = startRow; row < y + h + gridSize; row += gridSize)
      drawer.line(x, Math.round(h - (row + offY)), x + w, Math.round(h - (row + offY)), lineWidth);

    for (float col = startCol; col < x + w + gridSize; col += gridSize)
      drawer.line(Math.round(col + offX), y, Math.round(col + offX), y + h, lineWidth);
  }

  private void renderDrawableOverlay(PlayerView view, List<DrawnElement> drawables) {
    for (var drawable : drawables.toArray()) renderDrawable((DrawnElement) drawable);
  }

  private void renderDrawable(DrawnElement element) {
    var pen = element.getPen();
    var drawable = element.getDrawable();

    if (drawable instanceof ShapeDrawable) shapeDrawableDrawer.draw(drawable, pen);
    else if (drawable instanceof DrawablesGroup)
      for (var groupElement : ((DrawablesGroup) drawable).getDrawableList())
        renderDrawable(groupElement);
    else if (drawable instanceof RadiusCellTemplate) radiusCellTemplateDrawer.draw(drawable, pen);
    else if (drawable instanceof LineCellTemplate) lineCellTemplateDrawer.draw(drawable, pen);
    else if (drawable instanceof BlastTemplate) blastTemplateDrawer.draw(drawable, pen);
    else if (drawable instanceof ConeTemplate) coneTemplateDrawer.draw(drawable, pen);
    else if (drawable instanceof BurstTemplate) burstTemplateDrawer.draw(drawable, pen);
    else if (drawable instanceof RadiusTemplate) radiusTemplateDrawer.draw(drawable, pen);
    else if (drawable instanceof LineTemplate) lineTemplateDrawer.draw(drawable, pen);
  }

  private void renderBoard() {
    if (!zone.drawBoard()) return;

    var paint = zone.getBackgroundPaint();
    if (paint instanceof DrawableColorPaint) {
      Color.argb8888ToColor(tmpColor, ((DrawableColorPaint) paint).getColor());
      drawer.setColor(tmpColor);
      drawer.filledRectangle(
          cam.position.x - width * zoom / 2f,
          cam.position.y - height * zoom / 2f,
          width * zoom,
          height * zoom);
    } else {
      if (background == null) {
        var texturePaint = (DrawableTexturePaint) paint;
        var image = texturePaint.getAsset().getData();
        var pix = new Pixmap(image, 0, image.length);
        background = new Texture(pix);
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pix.dispose();
      }
      fillViewportWith(background);
    }

    var map = getSprite(zone.getMapAssetId());
    if (map != null) {
      map.setPosition(zone.getBoardX(), zone.getBoardY() - map.getHeight());
      map.draw(batch);
    }
  }

  private void fillViewportWith(Texture texture) {
    var w = ((int) (cam.viewportWidth * zoom / texture.getWidth()) + 4) * texture.getWidth();
    var h = ((int) (cam.viewportHeight * zoom / texture.getHeight()) + 4) * texture.getHeight();

    var startX = (cam.position.x - cam.viewportWidth * zoom / 2);
    startX = (((int) startX) / texture.getWidth()) * texture.getWidth() - texture.getWidth();

    var startY = (cam.position.y - cam.viewportHeight * zoom / 2);
    startY = (((int) startY) / texture.getHeight()) * texture.getHeight() - texture.getHeight();

    batch.draw(texture, startX, startY, 0, 0, w, h);
  }

  private void renderTokens(List<Token> tokenList, PlayerView view, boolean figuresOnly) {
    boolean isGMView = view.isGMView(); // speed things up

    if (visibleScreenArea == null) return;

    for (Token token : tokenList) {
      if (token.getShape() != Token.TokenShape.FIGURE && figuresOnly && !token.isAlwaysVisible()) {
        continue;
      }

      timer.start("tokenlist-1");
      try {
        if (token.isStamp() && zoneRenderer.isTokenMoving(token)) {
          continue;
        }
        // Don't bother if it's not visible
        // NOTE: Not going to use zone.isTokenVisible as it is very slow. In fact, it's faster
        // to just draw the tokens and let them be clipped
        if ((!token.isVisible() || token.isGMStamp()) && !isGMView) {
          continue;
        }
        if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
          continue;
        }
      } finally {
        // This ensures that the timer is always stopped
        timer.stop("tokenlist-1");
      }

      java.awt.Rectangle footprintBounds = token.getBounds(zone);
      java.awt.Rectangle origBounds = (java.awt.Rectangle) footprintBounds.clone();
      Area tokenBounds = new Area(footprintBounds);

      timer.start("tokenlist-1d");
      if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
        double sx = footprintBounds.width / 2f + footprintBounds.x - (token.getAnchor().x);
        double sy = footprintBounds.height / 2f + footprintBounds.y - (token.getAnchor().y);
        tokenBounds.transform(
            AffineTransform.getRotateInstance(
                Math.toRadians(-token.getFacing() - 90), sx, sy)); // facing
        // defaults to down, or -90 degrees
      }
      timer.stop("tokenlist-1d");

      timer.start("tokenlist-1e");
      try {

        // Vision visibility
        if (!isGMView && token.isToken() && zoneRenderer.getZoneView().isUsingVision()) {
          if (!GraphicsUtil.intersects(visibleScreenArea, tokenBounds)) {
            continue;
          }
        }
      } finally {
        // This ensures that the timer is always stopped
        timer.stop("tokenlist-1e");
      }

      // Previous path
      timer.start("renderTokens:ShowPath");
      if (zoneRenderer.getShowPathList().contains(token) && token.getLastPath() != null) {
        renderPath(token.getLastPath(), token.getFootprint(zone.getGrid()));
      }
      timer.stop("renderTokens:ShowPath");

      // get token image sprite, using image table if present
      var imageKey = token.getTokenImageAssetId();
      Sprite image = getSprite(imageKey);

      prepareTokenSprite(image, token, footprintBounds);

      // Render Halo
      if (token.hasHalo()) {
        drawer.setDefaultLineWidth(AppPreferences.getHaloLineWidth());
        Color.argb8888ToColor(tmpColor, token.getHaloColor().getRGB());
        drawer.setColor(tmpColor);
        areaRenderer.drawArea(zone.getGrid().getTokenCellArea(tokenBounds));
      }

      // Calculate alpha Transparency from token and use opacity for indicating that token is moving
      float opacity = token.getTokenOpacity();
      if (zoneRenderer.isTokenMoving(token)) opacity = opacity / 2.0f;

      Area tokenCellArea = zone.getGrid().getTokenCellArea(tokenBounds);
      Area cellArea = new Area(visibleScreenArea);
      cellArea.intersect(tokenCellArea);

      // Finally render the token image
      timer.start("tokenlist-7");
      image.setColor(1, 1, 1, opacity);
      if (!isGMView
          && zoneRenderer.getZoneView().isUsingVision()
          && (token.getShape() == Token.TokenShape.FIGURE)) {
        if (zone.getGrid().checkCenterRegion(tokenCellArea.getBounds(), visibleScreenArea)) {
          // if we can see the centre, draw the whole token
          image.draw(batch);
        } else {
          // else draw the clipped token
          paintClipped(image, tokenCellArea, cellArea);
        }
      } else if (!isGMView
          && zoneRenderer.getZoneView().isUsingVision()
          && token.isAlwaysVisible()) {
        // Jamz: Always Visible tokens will get rendered again here to place on top of FoW
        if (GraphicsUtil.intersects(visibleScreenArea, tokenCellArea)) {
          // if we can see a portion of the stamp/token, draw the whole thing, defaults to 2/9ths
          if (zone.getGrid()
              .checkRegion(
                  tokenCellArea.getBounds(),
                  visibleScreenArea,
                  token.getAlwaysVisibleTolerance())) {

            image.draw(batch);

          } else {
            // else draw the clipped stamp/token
            // This will only show the part of the token that does not have VBL on it
            // as any VBL on the token will block LOS, affecting the clipping.
            paintClipped(image, tokenCellArea, cellArea);
          }
        }
      } else {
        // fallthrough normal token rendered against visible area

        if (zoneRenderer.isTokenInNeedOfClipping(token, tokenCellArea, isGMView)) {
          paintClipped(image, tokenCellArea, cellArea);
        } else image.draw(batch);
      }
      image.setColor(Color.WHITE);
      timer.stop("tokenlist-7");

      timer.start("tokenlist-8");

      // Facing
      if (token.hasFacing()) {
        Token.TokenShape tokenType = token.getShape();
        switch (tokenType) {
          case FIGURE:
            if (token.getHasImageTable()
                && token.hasFacing()
                && AppPreferences.getForceFacingArrow() == false) {
              break;
            }
            java.awt.Shape arrow =
                getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);

            if (!zone.getGrid().isIsometric()) {
              arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
            }

            float fx = origBounds.x + origBounds.width / zoom / 2f;
            float fy = origBounds.y + origBounds.height / zoom / 2f;

            tmpMatrix.idt();
            tmpMatrix.translate(fx, -fy, 0);
            batch.setTransformMatrix(tmpMatrix);
            drawer.update();

            if (token.getFacing() < 0) drawer.setColor(Color.YELLOW);
            else drawer.setColor(1, 1, 0, 0.5f);

            var arrowArea = new Area(arrow);
            areaRenderer.fillArea(arrowArea);

            drawer.setColor(Color.DARK_GRAY);
            areaRenderer.drawArea(arrowArea);

            break;
          case TOP_DOWN:
            if (AppPreferences.getForceFacingArrow() == false) {
              break;
            }
          case CIRCLE:
            arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
            if (zone.getGrid().isIsometric()) {
              arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);
            }
            arrowArea = new Area(arrow);

            float cx = origBounds.x + origBounds.width / 2f;
            float cy = origBounds.y + origBounds.height / 2f;

            tmpMatrix.idt();
            tmpMatrix.translate(cx, -cy, 0);
            batch.setTransformMatrix(tmpMatrix);
            drawer.update();
            drawer.setColor(Color.YELLOW);

            areaRenderer.fillArea(arrowArea);
            drawer.setColor(Color.DARK_GRAY);
            drawer.setDefaultLineWidth(1);

            areaRenderer.drawArea(arrowArea);
            tmpMatrix.idt();
            batch.setTransformMatrix(tmpMatrix);
            drawer.update();
            break;
          case SQUARE:
            if (zone.getGrid().isIsometric()) {
              arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);
              cx = origBounds.x + origBounds.width / 2f;
              cy = origBounds.y + origBounds.height / 2f;
            } else {
              int facing = token.getFacing();
              arrow = getSquareFacingArrow(facing, footprintBounds.width / 2);

              cx = origBounds.x + origBounds.width / 2f;
              cy = origBounds.y + origBounds.height / 2f;

              // Find the edge of the image
              double xp = origBounds.getWidth() / 2;
              double yp = origBounds.getHeight() / 2;
              if (facing >= 45 && facing <= 135 || facing >= 225 && facing <= 315) {
                xp = (int) (yp / Math.tan(Math.toRadians(facing)));
                if (facing > 180) {
                  xp = -xp;
                  yp = -yp;
                }
              } else {
                yp = (int) (xp * Math.tan(Math.toRadians(facing)));
                if (facing > 90 && facing < 270) {
                  xp = -xp;
                  yp = -yp;
                }
              }
              cx += xp;
              cy -= yp;
            }

            arrowArea = new Area(arrow);

            tmpMatrix.translate(cx, -cy, 0);
            batch.setTransformMatrix(tmpMatrix);
            drawer.update();
            drawer.setColor(Color.YELLOW);

            areaRenderer.fillArea(arrowArea);
            drawer.setColor(Color.DARK_GRAY);
            areaRenderer.drawArea(arrowArea);
            batch.setTransformMatrix(tmpMatrix.idt());
            drawer.update();
            break;
        }
      }
      timer.stop("tokenlist-8");

      timer.start("tokenlist-9");

      // Check each of the set values
      for (String state : MapTool.getCampaign().getTokenStatesMap().keySet()) {
        Object stateValue = token.getState(state);
        AbstractTokenOverlay overlay = MapTool.getCampaign().getTokenStatesMap().get(state);
        if (stateValue instanceof AbstractTokenOverlay) {
          overlay = (AbstractTokenOverlay) stateValue;
        }
        if (overlay == null
            || overlay.isMouseover() && token != zoneRenderer.getTokenUnderMouse()
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }
        renderTokenOverlay(overlay, token, stateValue);
      }
      timer.stop("tokenlist-9");

      timer.start("tokenlist-10");

      for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
        Object barValue = token.getState(bar);
        BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
        if (overlay == null
            || overlay.isMouseover() && token != zoneRenderer.getTokenUnderMouse()
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }
        renderTokenOverlay(overlay, token, barValue);
      } // endfor
      timer.stop("tokenlist-10");

      timer.start("tokenlist-11");
      // Keep track of which tokens have been drawn so we can perform post-processing on them later
      // (such as selection borders and names/labels)
      if (!zoneRenderer.getActiveLayer().equals(token.getLayer())) continue;

      timer.stop("tokenlist-11");
      timer.start("tokenlist-12");

      boolean useIF = MapTool.getServerPolicy().isUseIndividualFOW();

      // Selection and labels

      var tokenRectangle = token.getBounds(zone);
      var gdxTokenRectangle =
          new Rectangle(
              tokenRectangle.x,
              -tokenRectangle.y - tokenRectangle.height,
              tokenRectangle.width,
              tokenRectangle.height);
      boolean isSelected = zoneRenderer.getSelectedTokenSet().contains(token.getId());
      if (isSelected) {
        ImageBorder selectedBorder =
            token.isStamp() ? AppStyle.selectedStampBorder : AppStyle.selectedBorder;
        if (zoneRenderer.getHighlightCommonMacros().contains(token)) {
          selectedBorder = AppStyle.commonMacroBorder;
        }
        if (!AppUtil.playerOwns(token)) {
          selectedBorder = AppStyle.selectedUnownedBorder;
        }
        if (useIF && !token.isStamp() && zoneRenderer.getZoneView().isUsingVision()) {
          Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
          if (tool
                  instanceof
                  RectangleExposeTool // XXX Change to use marker interface such as ExposeTool?
              || tool instanceof OvalExposeTool
              || tool instanceof FreehandExposeTool
              || tool instanceof PolygonExposeTool) {
            selectedBorder = AppConstants.FOW_TOOLS_BORDER;
          }
        }

        setProjectionMatrix(hudCam.combined);
        tmpWorldCoord.set(gdxTokenRectangle.x, gdxTokenRectangle.y, 0);
        cam.project(tmpWorldCoord);

        tmpWorldCoord.x *= Gdx.graphics.getBackBufferScale();
        tmpWorldCoord.y *= Gdx.graphics.getBackBufferScale();

        gdxTokenRectangle.set(
            tmpWorldCoord.x,
            tmpWorldCoord.y,
            gdxTokenRectangle.width / zoom,
            gdxTokenRectangle.height / zoom);

        if (token.hasFacing()
            && (token.getShape() == Token.TokenShape.TOP_DOWN || token.isStamp())) {

          var transX = gdxTokenRectangle.width / 2f - token.getAnchor().x / zoom;
          var transY = gdxTokenRectangle.height / 2f + token.getAnchor().y / zoom;

          tmpMatrix.idt();
          tmpMatrix.translate(tmpWorldCoord.x + transX, tmpWorldCoord.y + transY, 0);
          tmpMatrix.rotate(0, 0, 1, token.getFacing() + 90);
          tmpMatrix.translate(-transX, -transY, 0);
          gdxTokenRectangle.x = 0;
          gdxTokenRectangle.y = 0;
          batch.setTransformMatrix(tmpMatrix);
          renderImageBorderAround(selectedBorder, gdxTokenRectangle);
          tmpMatrix.idt();
          batch.setTransformMatrix(tmpMatrix);

        } else {
          renderImageBorderAround(selectedBorder, gdxTokenRectangle);
        }

        setProjectionMatrix(cam.combined);
      }

      // Token names and labels
      boolean showCurrentTokenLabel =
          AppState.isShowTokenNames() || token == zoneRenderer.getTokenUnderMouse();

      // if policy does not auto-reveal FoW, check if fog covers the token (slow)
      if (showCurrentTokenLabel
          && !isGMView
          && (!zoneRenderer.getZoneView().isUsingVision()
              || !MapTool.getServerPolicy().isAutoRevealOnMovement())
          && !zone.isTokenVisible(token)) {
        showCurrentTokenLabel = false;
      }
      if (showCurrentTokenLabel) {
        itemRenderList.add(new TokenLabelRenderer(token, zone, isGMView, textRenderer));
      }
      timer.stop("tokenlist-12");
    }

    timer.start("tokenlist-13");

    var tokenStackMap = zoneRenderer.getTokenStackMap();

    // Stacks
    if (!tokenList.isEmpty()
        && !tokenList.get(0).isStamp()) { // TODO: find a cleaner way to indicate token layer
      if (tokenStackMap != null) { // FIXME Needed to prevent NPE but how can it be null?
        for (Token token : tokenStackMap.keySet()) {
          var tokenRectangle = token.getBounds(zone);
          var stackImage = fetch("stack");
          batch.draw(
              stackImage,
              tokenRectangle.x + tokenRectangle.width - stackImage.getRegionWidth() + 2,
              -tokenRectangle.y - stackImage.getRegionHeight() + 2);
        }
      }
    }
    timer.stop("tokenlist-13");
  }

  private void prepareTokenSprite(Sprite image, Token token, java.awt.Rectangle footprintBounds) {
    image.setRotation(0);

    // Tokens are centered on the image center point
    float x = footprintBounds.x;
    float y = footprintBounds.y;

    timer.start("tokenlist-5");

    // handle flipping
    image.setFlip(token.isFlippedX(), token.isFlippedY());
    timer.stop("tokenlist-5");

    image.setOriginCenter();

    timer.start("tokenlist-5a");
    if (token.isFlippedIso()) {
      var assetId = token.getImageAssetId();
      if (!isoSprites.containsKey(assetId)) {
        var workImage = IsometricGrid.isoImage(ImageManager.getImage(assetId));
        try {
          var bytes = ImageUtil.imageToBytes(workImage, "png");
          var pix = new Pixmap(bytes, 0, bytes.length);
          image = new Sprite(new Texture(pix));
          pix.dispose();
        } catch (Exception e) {
        }
        isoSprites.put(assetId, image);
      } else {
        image = isoSprites.get(assetId);
      }
      token.setHeight((int) image.getHeight());
      token.setWidth((int) image.getWidth());
      footprintBounds = token.getBounds(zone);
    }
    timer.stop("tokenlist-5a");

    timer.start("tokenlist-6");
    // Position
    // For Isometric Grid we alter the height offset
    float iso_ho = 0;
    java.awt.Dimension imgSize =
        new java.awt.Dimension((int) image.getWidth(), (int) image.getHeight());
    if (token.getShape() == Token.TokenShape.FIGURE) {
      float th = token.getHeight() * (float) footprintBounds.width / token.getWidth();
      iso_ho = footprintBounds.height - th;
      footprintBounds =
          new java.awt.Rectangle(
              footprintBounds.x, footprintBounds.y - (int) iso_ho, footprintBounds.width, (int) th);
    }
    SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);

    int offsetx = 0;
    int offsety = 0;
    if (token.isSnapToScale()) {
      offsetx =
          (int)
              (imgSize.width < footprintBounds.width
                  ? (footprintBounds.width - imgSize.width) / 2
                  : 0);
      offsety =
          (int)
              (imgSize.height < footprintBounds.height
                  ? (footprintBounds.height - imgSize.height) / 2
                  : 0);
    }
    float tx = x + offsetx;
    float ty = y + offsety + iso_ho;

    // Snap
    var scaleX = 1f;
    var scaleY = 1f;
    if (token.isSnapToScale()) {
      scaleX = imgSize.width / image.getWidth();
      scaleY = imgSize.height / image.getHeight();
    } else {
      if (token.getShape() == Token.TokenShape.FIGURE) {
        scaleX = footprintBounds.width / image.getHeight();
        scaleY = footprintBounds.width / image.getWidth();
      } else {
        scaleX = footprintBounds.width / image.getWidth();
        scaleY = footprintBounds.height / image.getHeight();
      }
    }
    image.setSize(scaleX * image.getWidth(), scaleY * image.getHeight());

    image.setPosition(tx, -image.getHeight() - ty);

    image.setOriginCenter();

    // Rotated
    if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
      var originX = image.getWidth() / 2 - token.getAnchorX();
      var originY = image.getHeight() / 2 + token.getAnchorY();
      image.setOrigin(originX, originY);
      image.setRotation(token.getFacing() + 90);
    }

    timer.stop("tokenlist-6");
  }

  private Sprite getSprite(MD5Key key) {
    if (key == null) return null;

    var videoPlayer = videoPlayerMap.get(key);
    if (videoPlayer != null) {
      boolean skip = false;
      if (!videoPlayer.isPlaying()) {
        try {
          var file = AssetManager.getAssetCacheFile(key);
          if (file != null) {
            videoPlayer.play(Gdx.files.absolute(file.getAbsolutePath()));
            videoPlayer.setVolume(0);
          } else skip = true;

        } catch (FileNotFoundException ex) {
          log.warn(ex.toString());
        }
      }
      if (!skip) {
        videoPlayer.update();
        var texture = videoPlayer.getTexture();
        var sprite = new Sprite(texture);
        sprite.setSize(texture.getWidth(), texture.getHeight());
        return sprite;
      }
    }

    var animation = animationMap.get(key);
    if (animation != null) {
      var currentFrame = animation.getKeyFrame(stateTime, true);
      var sprite = new Sprite(currentFrame);
      sprite.setSize(currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
      return sprite;
    }

    var sprite = bigSprites.get(key);
    if (sprite != null) {
      sprite.setSize(sprite.getTexture().getWidth(), sprite.getTexture().getHeight());
      return sprite;
    }

    return getSprite(key.toString());
  }

  private Sprite getSprite(String name) {
    var sprite = fetchedSprites.get(name);
    if (sprite != null) {
      var region = fetchedRegions.get(name);
      sprite.setSize(region.getRegionWidth(), region.getRegionHeight());
      return sprite;
    }

    var region = fetch(name);

    if (name != "unknown" && region == null) {
      AssetManager.getAssetAsynchronously(new MD5Key(name), this);
      return getSprite("unknown");
    }

    sprite = new Sprite(region);
    sprite.setSize(region.getRegionWidth(), region.getRegionHeight());

    fetchedSprites.put(name, sprite);
    return sprite;
  }

  private TextureRegion fetch(String regionName) {
    var region = fetchedRegions.get(regionName);
    if (region != null) return region;

    region = tokenAtlas.findRegion(regionName);
    if (region == null) region = atlas.findRegion(regionName);

    fetchedRegions.put(regionName, region);
    return region;
  }

  private void renderImageBorderAround(ImageBorder border, Rectangle bounds) {
    var imagePath = border.getImagePath();
    var index = imagePath.indexOf("border/");
    var bordername = imagePath.substring(index);

    var topRight = fetch(bordername + "/tr");
    var top = fetch(bordername + "/top");
    var topLeft = fetch(bordername + "/tl");
    var left = fetch(bordername + "/left");
    var bottomLeft = fetch(bordername + "/bl");
    var bottom = fetch(bordername + "/bottom");
    var bottomRight = fetch(bordername + "/br");
    var right = fetch(bordername + "/right");

    // x,y is bottom left of the rectangle
    var leftMargin = border.getLeftMargin();
    var rightMargin = border.getRightMargin();
    var topMargin = border.getTopMargin();
    var bottomMargin = border.getBottomMargin();

    var x = bounds.x - leftMargin;
    var y = bounds.y - bottomMargin;

    var width = bounds.width + leftMargin + rightMargin;
    var height = bounds.height + topMargin + bottomMargin;

    // Draw Corners

    batch.draw(
        bottomLeft,
        x + leftMargin - bottomLeft.getRegionWidth(),
        y + topMargin - bottomLeft.getRegionHeight());
    batch.draw(bottomRight, x + width - rightMargin, y + topMargin - bottomRight.getRegionHeight());
    batch.draw(topLeft, x + leftMargin - topLeft.getRegionWidth(), y + height - bottomMargin);
    batch.draw(topRight, x + width - rightMargin, y + height - bottomMargin);

    tmpTile.setRegion(top);
    tmpTile.draw(
        batch,
        x + leftMargin,
        y + height - bottomMargin,
        width - leftMargin - rightMargin,
        top.getRegionHeight());

    tmpTile.setRegion(bottom);
    tmpTile.draw(
        batch,
        x + leftMargin,
        y + topMargin - bottom.getRegionHeight(),
        width - leftMargin - rightMargin,
        bottom.getRegionHeight());

    tmpTile.setRegion(left);
    tmpTile.draw(
        batch,
        x + leftMargin - left.getRegionWidth(),
        y + topMargin,
        left.getRegionWidth(),
        height - topMargin - bottomMargin);

    tmpTile.setRegion(right);
    tmpTile.draw(
        batch,
        x + width - rightMargin,
        y + topMargin,
        right.getRegionWidth(),
        height - topMargin - bottomMargin);
  }

  private void renderTokenOverlay(AbstractTokenOverlay overlay, Token token, Object value) {
    if (overlay instanceof BarTokenOverlay)
      renderTokenOverlay((BarTokenOverlay) overlay, token, value);
    else if (overlay instanceof BooleanTokenOverlay)
      renderTokenOverlay((BooleanTokenOverlay) overlay, token, value);
  }

  private void renderTokenOverlay(BarTokenOverlay overlay, Token token, Object value) {
    if (value == null) return;
    double val = 0;
    if (value instanceof Number) {
      val = ((Number) value).doubleValue();
    } else {
      try {
        val = Double.parseDouble(value.toString());
      } catch (NumberFormatException e) {
        return; // Bad value so don't paint.
      } // endtry
    } // endif
    if (val < 0) val = 0;
    if (val > 1) val = 1;

    if (overlay instanceof MultipleImageBarTokenOverlay)
      renderTokenOverlay((MultipleImageBarTokenOverlay) overlay, token, val);
    else if (overlay instanceof SingleImageBarTokenOverlay)
      renderTokenOverlay((SingleImageBarTokenOverlay) overlay, token, val);
    else if (overlay instanceof TwoToneBarTokenOverlay)
      renderTokenOverlay((TwoToneBarTokenOverlay) overlay, token, val);
    else if (overlay instanceof DrawnBarTokenOverlay)
      renderTokenOverlay((DrawnBarTokenOverlay) overlay, token, val);
    else if (overlay instanceof TwoImageBarTokenOverlay)
      renderTokenOverlay((TwoImageBarTokenOverlay) overlay, token, val);
  }

  private void renderTokenOverlay(
      MultipleImageBarTokenOverlay overlay, Token token, double barValue) {
    int incr = overlay.findIncrement(barValue);

    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;

    // Get the images
    var image = getSprite(overlay.getAssetIds()[incr]);

    Dimension d = bounds.getSize();
    Dimension size = new Dimension((int) image.getWidth(), (int) image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    // Find the position of the image according to the size and side where they are placed
    switch (overlay.getSide()) {
      case LEFT:
      case TOP:
        y += d.height - size.height;
        break;
      case RIGHT:
        x += d.width - size.width;
        y += d.height - size.height;
        break;
    }

    image.setPosition(x, y);
    image.setSize(size.width, size.height);
    image.draw(batch, overlay.getOpacity() / 100f);
  }

  private void renderTokenOverlay(
      SingleImageBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;

    // Get the images
    var image = getSprite(overlay.getAssetId());

    Dimension d = bounds.getSize();
    Dimension size = new Dimension((int) image.getWidth(), (int) image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    var side = overlay.getSide();
    // Find the position of the images according to the size and side where they are placed
    switch (side) {
      case LEFT:
      case TOP:
        y += d.height - size.height;
        break;
      case RIGHT:
        x += d.width - size.width;
        y += d.height - size.height;
        break;
    }

    int width =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM)
            ? overlay.calcBarSize((int) image.getWidth(), barValue)
            : (int) image.getWidth();
    int height =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT)
            ? overlay.calcBarSize((int) image.getHeight(), barValue)
            : (int) image.getHeight();

    int screenWidth =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM)
            ? overlay.calcBarSize(size.width, barValue)
            : size.width;
    int screenHeight =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT)
            ? overlay.calcBarSize(size.height, barValue)
            : size.height;

    image.setPosition(x + size.width - screenWidth, y + size.height - screenHeight);
    image.setSize(screenWidth, screenHeight);

    var u = image.getU();
    var v = image.getV();
    var u2 = image.getU2();
    var v2 = image.getV2();

    var wfactor = screenWidth * 1.0f / size.width;
    var uDiff = (u2 - u) * wfactor;
    image.setU(u2 - uDiff);

    var vfactor = screenHeight * 1.0f / size.height;
    var vDiff = (v2 - v) * vfactor;
    image.setV(v2 - vDiff);

    image.draw(batch, overlay.getOpacity() / 100f);

    image.setU(u);
    image.setV(v);
  }

  private void renderTokenOverlay(DrawnBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var side = overlay.getSide();
    var thickness = overlay.getThickness();

    int width =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) ? w : thickness;
    int height =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT) ? h : thickness;

    switch (side) {
      case LEFT:
      case TOP:
        y += h - height;
        break;
      case RIGHT:
        x += w - width;
        y += h - height;
        break;
    }

    if (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) {
      width = overlay.calcBarSize(width, barValue);
    } else {
      height = overlay.calcBarSize(height, barValue);
      y += bounds.height - height;
    }

    var barColor = overlay.getBarColor();
    tmpColor.set(
        barColor.getRed() / 255f,
        barColor.getGreen() / 255f,
        barColor.getBlue() / 255f,
        barColor.getAlpha() / 255f);
    drawer.filledRectangle(x, y, width, height, tmpColor);
  }

  private void renderTokenOverlay(TwoToneBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var side = overlay.getSide();
    var thickness = overlay.getThickness();

    int width =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) ? w : thickness;
    int height =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT) ? h : thickness;

    switch (side) {
      case LEFT:
      case TOP:
        y += h - height;
        break;
      case RIGHT:
        x += w - width;
        y += h - height;
        break;
    }

    var color = overlay.getBgColor();
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        color.getAlpha() / 255f);
    drawer.filledRectangle(x, y, width, height, tmpColor);

    // Draw the bar
    int borderSize = thickness > 5 ? 2 : 1;
    x += borderSize;
    y += borderSize;
    width -= borderSize * 2;
    height -= borderSize * 2;
    if (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) {
      width = overlay.calcBarSize(width, barValue);
    } else {
      height = overlay.calcBarSize(height, barValue);
    }

    color = overlay.getBarColor();
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        color.getAlpha() / 255f);
    drawer.filledRectangle(x, y, width, height, tmpColor);
  }

  private void renderTokenOverlay(TwoImageBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;

    // Get the images
    var topImage = getSprite(overlay.getTopAssetId());
    var bottomImage = getSprite(overlay.getBottomAssetId());

    Dimension d = bounds.getSize();
    Dimension size = new Dimension((int) topImage.getWidth(), (int) topImage.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    var side = overlay.getSide();
    // Find the position of the images according to the size and side where they are placed
    switch (side) {
      case LEFT:
      case TOP:
        y += d.height - size.height;
        break;
      case RIGHT:
        x += d.width - size.width;
        y += d.height - size.height;
        break;
    }

    var width =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM)
            ? overlay.calcBarSize((int) topImage.getWidth(), barValue)
            : topImage.getWidth();
    var height =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT)
            ? overlay.calcBarSize((int) topImage.getHeight(), barValue)
            : topImage.getHeight();

    var screenWidth =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM)
            ? overlay.calcBarSize(size.width, barValue)
            : size.width;
    var screenHeight =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT)
            ? overlay.calcBarSize(size.height, barValue)
            : size.height;

    bottomImage.setPosition(x, y);
    bottomImage.setSize(size.width, size.height);
    bottomImage.draw(batch, overlay.getOpacity() / 100f);

    var u = topImage.getU();
    var v = topImage.getV();
    var u2 = topImage.getU2();
    var v2 = topImage.getV2();

    var wFactor = screenWidth * 1.0f / size.width;
    var uDiff = (u2 - u) * wFactor;

    var vFactor = screenHeight * 1.0f / size.height;
    var vDiff = (v2 - v) * vFactor;

    topImage.setPosition(x, y);
    topImage.setSize(screenWidth, screenHeight);

    if (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT) {
      topImage.setU(u2 - uDiff);
      topImage.setV(v2 - vDiff);
    } else {

      topImage.setU2(u + uDiff);
      topImage.setV2(v + vDiff);
    }
    topImage.draw(batch, overlay.getOpacity() / 100f);

    topImage.setU(u);
    topImage.setV(v);
    topImage.setU2(u2);
    topImage.setV2(v2);
  }

  private void renderTokenOverlay(BooleanTokenOverlay overlay, Token token, Object value) {
    if (!FunctionUtil.getBooleanValue(value)) return;

    if (overlay instanceof ImageTokenOverlay)
      renderTokenOverlay((ImageTokenOverlay) overlay, token);
    else if (overlay instanceof FlowColorDotTokenOverlay)
      renderTokenOverlay((FlowColorDotTokenOverlay) overlay, token);
    else if (overlay instanceof YieldTokenOverlay)
      renderTokenOverlay((YieldTokenOverlay) overlay, token);
    else if (overlay instanceof OTokenOverlay) renderTokenOverlay((OTokenOverlay) overlay, token);
    else if (overlay instanceof ColorDotTokenOverlay)
      renderTokenOverlay((ColorDotTokenOverlay) overlay, token);
    else if (overlay instanceof DiamondTokenOverlay)
      renderTokenOverlay((DiamondTokenOverlay) overlay, token);
    else if (overlay instanceof TriangleTokenOverlay)
      renderTokenOverlay((TriangleTokenOverlay) overlay, token);
    else if (overlay instanceof CrossTokenOverlay)
      renderTokenOverlay((CrossTokenOverlay) overlay, token);
    else if (overlay instanceof XTokenOverlay) renderTokenOverlay((XTokenOverlay) overlay, token);
    else if (overlay instanceof ShadedTokenOverlay)
      renderTokenOverlay((ShadedTokenOverlay) overlay, token);
  }

  private void renderTokenOverlay(ShadedTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    tmpColor.set(1, 1, 1, overlay.getOpacity() / 100);
    // FIXME: this should change the transparency of the token. Test this when tokendrawing is moved
    // to backbuffer
    drawer.setColor(tmpColor);
    drawer.filledRectangle(x, y, w, h);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(ImageTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y;

    // Get the image
    java.awt.Rectangle iBounds = overlay.getImageBounds(bounds, token);
    Dimension d = iBounds.getSize();

    var image = getSprite(overlay.getAssetId());

    Dimension size = new Dimension((int) image.getWidth(), (int) image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    // Paint it at the right location
    int width = size.width;
    int height = size.height;

    if (overlay instanceof CornerImageTokenOverlay) {
      x += iBounds.x + (d.width - width) / 2;
      y -= iBounds.y + (d.height - height) / 2 + iBounds.height;
    } else {
      x = iBounds.x + (d.width - width) / 2;
      y = -(iBounds.y + (d.height - height) / 2) - iBounds.height;
    }

    image.setPosition(x, y);
    image.setSize(size.width, size.height);
    image.draw(batch, overlay.getOpacity() / 100f);
  }

  private void renderTokenOverlay(XTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);

    var stroke = overlay.getStroke();

    drawer.setColor(tmpColor);
    drawer.line(x, y, x + w, y + h, stroke.getLineWidth());
    drawer.line(x, y + h, x + w, y, stroke.getLineWidth());
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(FlowColorDotTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);
    drawer.setColor(tmpColor);
    Shape s = overlay.getShape(bounds, token);
    areaRenderer.fillArea(new Area(s));
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(YieldTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);

    var stroke = overlay.getStroke();
    var hc = w / 2f;
    var vc = h * (1 - 0.134f);

    var floats =
        new float[] {
          x, y + vc, x + w, y + vc, x + hc, y,
        };

    drawer.setColor(tmpColor);
    drawer.path(floats, stroke.getLineWidth(), JoinType.POINTY, false);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(OTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);

    var stroke = overlay.getStroke();
    var lineWidth = stroke.getLineWidth();

    var centerX = x + w / 2f;
    var centerY = y + h / 2f;
    var radiusX = w / 2f - lineWidth / 2f;
    var radiusY = h / 2f - lineWidth / 2f;

    drawer.setColor(tmpColor);
    drawer.ellipse(centerX, centerY, radiusX, radiusY, 0, lineWidth);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(ColorDotTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);

    var size = w * 0.1f;
    var offset = w * 0.8f;

    var posX = x + size;
    var posY = y + size;

    switch (overlay.getCorner()) {
      case SOUTH_EAST:
        posX += offset;
        break;
      case SOUTH_WEST:
        break;
      case NORTH_EAST:
        posX += offset;
        posY += offset;
        break;
      case NORTH_WEST:
        posY += offset;
        break;
    }

    drawer.setColor(tmpColor);
    drawer.filledEllipse(posX, posY, size, size);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(DiamondTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);
    var stroke = overlay.getStroke();

    var hc = w / 2f;
    var vc = h / 2f;

    var floats =
        new float[] {
          x, y + vc, x + hc, y, x + w, y + vc, x + hc, y + h,
        };

    drawer.setColor(tmpColor);
    drawer.path(floats, stroke.getLineWidth(), JoinType.POINTY, false);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(TriangleTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);
    var stroke = overlay.getStroke();

    var hc = w / 2f;
    var vc = h * (1 - 0.866f);

    var floats =
        new float[] {
          x, y + vc, x + w, y + vc, x + hc, y + h,
        };

    drawer.setColor(tmpColor);
    drawer.path(floats, stroke.getLineWidth(), JoinType.POINTY, false);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(CrossTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zone);
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        overlay.getOpacity() / 100);
    var stroke = overlay.getStroke();

    drawer.setColor(tmpColor);
    drawer.line(x, y + h / 2f, x + w, y + h / 2f, stroke.getLineWidth());
    drawer.line(x + w / 2f, y, x + w / 2f, y + h, stroke.getLineWidth());
    drawer.setColor(Color.WHITE);
  }

  // FIXME: I don't like this hardwiring
  protected java.awt.Shape getFigureFacingArrow(int angle, int size) {
    int base = (int) (size * .75);
    int width = (int) (size * .35);

    var facingArrow = new GeneralPath();
    facingArrow.moveTo(base, -width);
    facingArrow.lineTo(size, 0);
    facingArrow.lineTo(base, width);
    facingArrow.lineTo(base, -width);

    return facingArrow.createTransformedShape(
        AffineTransform.getRotateInstance(-Math.toRadians(angle)));
  }

  // FIXME: I don't like this hardwiring
  protected java.awt.Shape getCircleFacingArrow(int angle, int size) {
    int base = (int) (size * .75);
    int width = (int) (size * .35);

    var facingArrow = new GeneralPath();
    facingArrow.moveTo(base, -width);
    facingArrow.lineTo(size, 0);
    facingArrow.lineTo(base, width);
    facingArrow.lineTo(base, -width);

    return facingArrow.createTransformedShape(
        AffineTransform.getRotateInstance(-Math.toRadians(angle)));
  }

  // FIXME: I don't like this hardwiring
  protected java.awt.Shape getSquareFacingArrow(int angle, int size) {
    int base = (int) (size * .75);
    int width = (int) (size * .35);

    var facingArrow = new GeneralPath();
    facingArrow.moveTo(0, 0);
    facingArrow.lineTo(-(size - base), -width);
    facingArrow.lineTo(-(size - base), width);
    facingArrow.lineTo(0, 0);

    return facingArrow.createTransformedShape(
        AffineTransform.getRotateInstance(-Math.toRadians(angle)));
  }

  private void paintClipped(Sprite image, Area bounds, Area clip) {

    backBuffer.begin();
    ScreenUtils.clear(Color.CLEAR);

    setProjectionMatrix(cam.combined);

    image.draw(batch);

    drawer.setColor(Color.CLEAR);

    tmpArea.reset();
    tmpArea.add(bounds);
    tmpArea.subtract(clip);
    areaRenderer.fillArea(tmpArea);

    backBuffer.end();

    tmpWorldCoord.x = image.getX();
    tmpWorldCoord.y = image.getY();
    tmpWorldCoord.z = 0;
    var screenCoord = cam.project(tmpWorldCoord);

    var x = image.getX();
    var y = image.getY();
    var w = image.getWidth();
    var h = image.getHeight();
    var wsrc = image.getWidth() / zoom;
    var hsrc = image.getHeight() / zoom;

    batch.draw(
        backBuffer.getColorBufferTexture(),
        x,
        y,
        w,
        h,
        (int) screenCoord.x,
        (int) screenCoord.y,
        (int) wsrc,
        (int) hsrc,
        false,
        true);
  }

  private void renderPath(Path path, TokenFootprint footprint) {
    if (path == null) {
      return;
    }

    if (path.getCellPath().isEmpty()) {
      return;
    }
    Grid grid = zone.getGrid();

    // log.info("Rendering path..." + System.currentTimeMillis());

    java.awt.Rectangle footprintBounds = footprint.getBounds(grid);
    if (path.getCellPath().get(0) instanceof CellPoint) {
      timer.start("renderPath-1");
      CellPoint previousPoint = null;
      Point previousHalfPoint = null;

      Path<CellPoint> pathCP = (Path<CellPoint>) path;
      List<CellPoint> cellPath = pathCP.getCellPath();

      Set<CellPoint> pathSet = new HashSet<CellPoint>();
      List<ZonePoint> waypointList = new LinkedList<ZonePoint>();
      for (CellPoint p : cellPath) {
        pathSet.addAll(footprint.getOccupiedCells(p));

        if (pathCP.isWaypoint(p) && previousPoint != null) {
          ZonePoint zp = grid.convert(p);
          zp.x += footprintBounds.width / 2;
          zp.y += footprintBounds.height / 2;
          waypointList.add(zp);
        }
        previousPoint = p;
      }

      // Don't show the final path point as a waypoint, it's redundant, and ugly
      if (waypointList.size() > 0) {
        waypointList.remove(waypointList.size() - 1);
      }
      timer.stop("renderPath-1");
      // log.info("pathSet size: " + pathSet.size());

      timer.start("renderPath-2");
      Dimension cellOffset = zone.getGrid().getCellOffset();
      for (CellPoint p : pathSet) {
        ZonePoint zp = grid.convert(p);
        zp.x += grid.getCellWidth() / 2 + cellOffset.width;
        zp.y += grid.getCellHeight() / 2 + cellOffset.height;
        highlightCell(zp, getCellHighlight(), 1.0f);
      }
      if (AppState.getShowMovementMeasurements()) {
        double cellAdj = grid.isHex() ? 2.5 : 2;
        for (CellPoint p : cellPath) {
          ZonePoint zp = grid.convert(p);
          zp.x += grid.getCellWidth() / cellAdj + cellOffset.width;
          zp.y += grid.getCellHeight() / cellAdj + cellOffset.height;
          addDistanceText(
              zp,
              1.0f,
              (float) p.getDistanceTraveled(zone),
              (float) p.getDistanceTraveledWithoutTerrain());
        }
      }
      int w = 0;
      for (ZonePoint p : waypointList) {
        ZonePoint zp = new ZonePoint(p.x + cellOffset.width, p.y + cellOffset.height);
        highlightCell(zp, fetch("redDot"), .333f);
      }

      // Line path
      if (grid.getCapabilities().isPathLineSupported()) {
        ZonePoint lineOffset;
        if (grid.isHex()) {
          lineOffset = new ZonePoint(0, 0);
        } else {
          lineOffset =
              new ZonePoint(
                  footprintBounds.x + footprintBounds.width / 2 - grid.getOffsetX(),
                  footprintBounds.y + footprintBounds.height / 2 - grid.getOffsetY());
        }

        int xOffset = (int) (lineOffset.x);
        int yOffset = (int) (lineOffset.y);

        drawer.setColor(Color.BLUE);

        previousPoint = null;
        tmpFloat.clear();
        for (CellPoint p : cellPath) {
          if (previousPoint != null) {
            ZonePoint ozp = grid.convert(previousPoint);
            int ox = ozp.x;
            int oy = ozp.y;

            ZonePoint dzp = grid.convert(p);
            int dx = dzp.x;
            int dy = dzp.y;

            int halfx = ((ox + dx) / 2);
            int halfy = ((oy + dy) / 2);
            Point halfPoint = new Point(halfx, halfy);

            if (previousHalfPoint != null) {
              int x1 = previousHalfPoint.x + xOffset;
              int y1 = previousHalfPoint.y + yOffset;

              int x2 = ox + xOffset;
              int y2 = oy + yOffset;

              int xh = halfPoint.x + xOffset;
              int yh = halfPoint.y + yOffset;

              tmpVector0.set(x1, -y1);
              tmpVector1.set(x2, -y2);
              tmpVector2.set(xh, -yh);

              for (var i = 1; i <= POINTS_PER_BEZIER; i++) {
                Bezier.quadratic(
                    tmpVectorOut,
                    i / POINTS_PER_BEZIER,
                    tmpVector0,
                    tmpVector1,
                    tmpVector2,
                    tmpVector);
                tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
              }
            }
            previousHalfPoint = halfPoint;
          }
          previousPoint = p;
        }
        drawer.path(tmpFloat.toArray(), drawer.getDefaultLineWidth(), JoinType.SMOOTH, true);
      }
      drawer.setColor(Color.WHITE);
      timer.stop("renderPath-2");
    } else {
      timer.start("renderPath-3");
      // Zone point/gridless path

      // Line
      var highlight = tmpColor;
      highlight.set(1, 1, 1, 80 / 255f);
      var highlightStroke = 9f;

      ScreenPoint lastPoint = null;

      Path<ZonePoint> pathZP = (Path<ZonePoint>) path;
      List<ZonePoint> pathList = pathZP.getCellPath();
      for (ZonePoint zp : pathList) {
        if (lastPoint == null) {
          lastPoint =
              ScreenPoint.fromZonePointRnd(
                  zoneRenderer,
                  zp.x + (footprintBounds.width / 2) * footprint.getScale(),
                  zp.y + (footprintBounds.height / 2) * footprint.getScale());
          continue;
        }
        ScreenPoint nextPoint =
            ScreenPoint.fromZonePoint(
                zoneRenderer,
                zp.x + (footprintBounds.width / 2) * footprint.getScale(),
                zp.y + (footprintBounds.height / 2) * footprint.getScale());

        drawer.line(
            (float) lastPoint.x,
            -(float) lastPoint.y,
            (float) nextPoint.x,
            -(float) nextPoint.y,
            highlight,
            highlightStroke);

        drawer.line(
            (float) lastPoint.x,
            -(float) lastPoint.y,
            (float) nextPoint.x,
            -(float) nextPoint.y,
            Color.BLUE,
            drawer.getDefaultLineWidth());
        lastPoint = nextPoint;
      }

      // Waypoints
      boolean originPoint = true;
      for (ZonePoint p : pathList) {
        // Skip the first point (it's the path origin)
        if (originPoint) {
          originPoint = false;
          continue;
        }

        // Skip the final point
        if (p == pathList.get(pathList.size() - 1)) {
          continue;
        }
        p =
            new ZonePoint(
                (p.x + (footprintBounds.width / 2)), (p.y + (footprintBounds.height / 2)));
        highlightCell(p, fetch("redDot"), .333f);
      }
      timer.stop("renderPath-3");
    }
  }

  private TextureRegion getCellHighlight() {
    if (zone.getGrid() instanceof SquareGrid) return fetch("whiteBorder");
    if (zone.getGrid() instanceof HexGrid) return fetch("hexBorder");
    if (zone.getGrid() instanceof IsometricGrid) return fetch("isoBorder");

    return null;
  }

  private void addDistanceText(
      ZonePoint point, float size, float distance, float distanceWithoutTerrain) {
    if (distance == 0) return;

    Grid grid = zone.getGrid();
    float cwidth = (float) grid.getCellWidth();
    float cheight = (float) grid.getCellHeight();

    float iwidth = cwidth * size;
    float iheight = cheight * size;

    var cellX = (point.x - iwidth / 2);
    var cellY = (-point.y + iheight / 2) + distanceFont.getLineHeight();

    // Draw distance for each cell
    var textOffset = 7 * distanceFontScale; // 7 pixels at 100% zoom & grid size of 50

    String distanceText = NumberFormat.getInstance().format(distance);
    if (log.isDebugEnabled() || showAstarDebugging) {
      distanceText += " (" + NumberFormat.getInstance().format(distanceWithoutTerrain) + ")";
    }

    glyphLayout.setText(distanceFont, distanceText);

    var textWidth = glyphLayout.width;

    distanceFont.setColor(Color.BLACK);

    distanceFont.draw(
        batch,
        distanceText,
        cellX + cwidth - textWidth - textOffset,
        cellY - cheight /*- textOffset*/);
  }

  private void highlightCell(ZonePoint zp, TextureRegion image, float size) {
    Grid grid = zone.getGrid();
    float cwidth = (float) grid.getCellWidth() * size;
    float cheight = (float) grid.getCellHeight() * size;

    float rotation = 0;
    if (zone.getGrid() instanceof HexGridHorizontal) rotation = 90;

    batch.draw(
        image, zp.x - cwidth / 2, -zp.y - cheight / 2, 0, 0, cwidth, cheight, 1f, 1f, rotation);
  }

  private void disposeZoneResources() {
    if (!initialized) return;

    // cam.zoom = 1.0f;
    offsetX = 0;
    offsetY = 0;
    fogX = null;
    fogY = null;
    fetchedRegions.clear();
    fetchedSprites.clear();
    var oldPacker = packer;
    packer = createPacker();
    oldPacker.dispose();

    Gdx.app.postRunnable(
        () -> {
          disposeZoneTextures();
        });
  }

  private void disposeZoneTextures() {
    updateCam();
    var background = this.background;
    this.background = null;
    if (background != null) {
      background.dispose();
    }

    var fog = this.fog;
    this.fog = null;
    if (fog != null) {
      fog.dispose();
    }

    for (var sprite : isoSprites.values()) {
      sprite.getTexture().dispose();
    }
    isoSprites.clear();

    for (var sprite : bigSprites.values()) {
      sprite.getTexture().dispose();
    }
    bigSprites.clear();
    animationMap.clear();
  }

  private void initializeZoneResources(Zone newZone) {
    if (newZone == null || !initialized) return;

    zoneRenderer = MapTool.getFrame().getZoneRenderer(newZone);

    for (var assetId : newZone.getAllAssetIds()) {
      AssetManager.getAssetAsynchronously(assetId, this);
    }
    zone = newZone;
  }

  // shapedrawer has to learn how to draw with texturePaint first.
  private Texture paintToTexture(DrawablePaint paint) {
    if (paint instanceof DrawableTexturePaint) {
      var texturePaint = (DrawableTexturePaint) paint;
      var image = texturePaint.getAsset().getData();
      var pix = new Pixmap(image, 0, image.length);
      var texture = new Texture(pix);
      pix.dispose();
      return texture;
    }
    if (paint instanceof DrawableColorPaint) {
      var colorPaint = (DrawableColorPaint) paint;
      var colorValue = colorPaint.getColor();
      var color = new Color();
      Color.argb8888ToColor(color, colorValue);
      var pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

      pix.setColor(color);
      pix.fill();
      var texture = new Texture(pix);
      pix.dispose();
      return texture;
    }
    return null;
  }

  private Sprite paintToSprite(DrawablePaint paint) {
    if (paint instanceof DrawableTexturePaint) {
      var texturePaint = (DrawableTexturePaint) paint;
      var image = texturePaint.getAsset().getData();
      var pix = new Pixmap(image, 0, image.length);
      var sprite = new Sprite(new Texture(pix));
      sprite.setSize(pix.getWidth(), pix.getHeight());
      sprite.setPosition(0, -1 * sprite.getHeight());
      pix.dispose();
      return sprite;
    }
    if (paint instanceof DrawableColorPaint) {
      var colorPaint = (DrawableColorPaint) paint;
      var colorValue = colorPaint.getColor();
      var color = new Color();
      Color.argb8888ToColor(color, colorValue);
      var pix = new Pixmap(64, 64, Pixmap.Format.RGBA8888);

      pix.setColor(color);
      pix.fill();
      var sprite = new Sprite(new Texture(pix));
      sprite.setSize(pix.getWidth(), pix.getHeight());
      sprite.setPosition(0, -1 * sprite.getHeight());
      pix.dispose();
      return sprite;
    }
    return null;
  }

  @Subscribe
  void onZoneActivated(ZoneActivated event) {

    var oldZone = zone;
    // first disable rendering during intitialisation;
    renderZone = false;

    if (oldZone != null) {
      disposeZoneResources();
   //   oldZone.removeModelChangeListener(this);
    }

    var newZone = event.zone();
   // newZone.addModelChangeListener(this);
    initializeZoneResources(newZone);
    // just in case we are running before create was called and hence initializeZoneResources does
    // nothing
    zone = newZone;
    renderZone = true;
  }

  /*
  @Override
  public void modelChanged(ModelChangeEvent event) {

        Object evt = event.getEvent();
        System.out.println("ModelChangend: " + evt);
        if (!(evt instanceof Zone.Event)) return;
        var eventType = (Zone.Event) evt;
        switch (eventType) {
          case TOPOLOGY_CHANGED:
            flushFog();
            // flushLight();
            break;
          case FOG_CHANGED:
            flushFog = true;
            break;
          case TOKEN_CHANGED:
            {
              updateVisibleArea();
              var token = (Token) event.getArg();
              break;
            }
          case TOKEN_ADDED:
            {
              var token = (Token) event.getArg();
              System.out.println();
              break;
            }
        }
    */
    /*
    if (evt == Zone.Event.TOKEN_CHANGED
            || evt == Zone.Event.TOKEN_REMOVED
            || evt == Zone.Event.TOKEN_ADDED) {
        if (event.getArg() instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Token> list = (List<Token>) (event.getArg());
            for (Token token : list) {
                zoneRenderer.flush(token);
            }
        } else {
            zoneRenderer.flush((Token) event.getArg());
        }
    }*/
    /*
          var currentZone = zone;

          // for now quick and dirty
          disposeZoneResources();
          initializeZoneResources(currentZone);

  }
*/
  public void setScale(Scale scale) {
    if (!initialized) {
      return;
    }

    var dpiScale = Gdx.graphics.getBackBufferScale();
    offsetX = (int) (scale.getOffsetX() * dpiScale * -1);
    offsetY = (int) (scale.getOffsetY() * dpiScale);
    zoom = (float) (1f / scale.getScale() / dpiScale);
    updateCam();
  }

  public void flushFog() {
    if (!initialized) return;

    flushFog = true;
    visibleScreenArea = null;
  }

  @Override
  public void assetAvailable(MD5Key key) {
    try {
      var asset = AssetManager.getAsset(key);
      if (asset.getExtension().equals("gif")) {

        Gdx.app.postRunnable(
            () -> {
              // var ass = AssetManager.getAsset(key);
              var is = new ByteArrayInputStream(asset.getData());
              var animation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, is);
              animationMap.put(key, animation);
            });
        return;
      }
      if (asset.getExtension().equals("data")) {
        var videoPlayer = VideoPlayerCreator.createVideoPlayer();
        videoPlayerMap.put(key, videoPlayer);
        return;
      }
      var img =
          ImageUtil.createCompatibleImage(
              ImageUtil.bytesToImage(asset.getData(), asset.getName()), null);
      // var img = ImageManager.getImage(key);
      var bytes = ImageUtil.imageToBytes(img, "png");
      // without imageutil there seem to be some issues with tranparency  for some images.
      // (black background instead of tranparent)
      // var bytes = AssetManager.getAsset(key).getImage();
      var pix = new Pixmap(bytes, 0, bytes.length);

      try {
        var name = key.toString();
        synchronized (packer) {
          if (packer.getRect(name) == null) packer.pack(name, pix);

          pix.dispose();
        }
      } catch (GdxRuntimeException x) {
        // this means that the pixmap is to big for the atlas.
        Gdx.app.postRunnable(
            () -> {
              synchronized (bigSprites) {
                if (!bigSprites.containsKey(key)) bigSprites.put(key, new Sprite(new Texture(pix)));
              }
              pix.dispose();
            });
      }
    } catch (Exception e) {
    }
  }
}
