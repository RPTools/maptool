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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.zip.Deflater;
import javax.swing.*;
import net.rptools.lib.AwtUtil;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.events.ZoneActivated;
import net.rptools.maptool.client.swing.ImageBorder;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.tool.WallTopologyTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.theme.Borders;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.zone.DrawableLight;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.gdx.drawing.DrawnElementRenderer;
import net.rptools.maptool.client.ui.zone.gdx.label.ItemRenderer;
import net.rptools.maptool.client.ui.zone.gdx.label.LabelRenderer;
import net.rptools.maptool.client.ui.zone.gdx.label.TextRenderer;
import net.rptools.maptool.client.ui.zone.gdx.label.TokenLabelRenderer;
import net.rptools.maptool.client.ui.zone.renderer.SelectionSet;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The coordinates in the model are y-down, x-left. The world coordinates are y-up, x-left. I moved
 * the world to the 4th quadrant of the coordinate system. So if you would draw a token t awt at
 * (x,y) you have to draw it at (x, -y - t.width)
 *
 * <p>
 */
public class GdxRenderer extends ApplicationAdapter {

  private static final Logger log = LogManager.getLogger(GdxRenderer.class);
  private static final int BLENDING_TEXTURE_INDEX = 1;

  public static final float POINTS_PER_BEZIER = 10f;
  private static GdxRenderer _instance;

  // renderFog
  private final String ATLAS = "net/rptools/maptool/client/maptool.atlas";
  private final String FONT_NORMAL = "normalFont.ttf";
  private final String FONT_BOLD = "boldFont.ttf";

  private final String font = "NotoSansSymbols";

  private ZoneViewModel viewModel;

  // from renderToken:
  private Area visibleScreenArea;
  private Area exposedFogArea;
  private PlayerView lastView;
  private final List<ItemRenderer> itemRenderList = new LinkedList<>();

  // zone specific resources
  private ZoneCache zoneCache;
  private int offsetX = 0;
  private int offsetY = 0;
  private float zoom = 1.0f;
  private float stateTime = 0f;
  private boolean renderZone = false;
  private boolean showAstarDebugging = false;

  private ShaderProgram environmentalLightingShader;

  // general resources
  private OrthographicCamera cam;
  private PerspectiveCamera cam3d;
  private OrthographicCamera hudCam;
  private PolygonSpriteBatch batch;
  private boolean initialized = false;
  private int width;
  private int height;
  private BitmapFont normalFont;
  private BitmapFont boldFont;
  private float boldFontScale = 0;
  private final CodeTimer timer = new CodeTimer("GdxRenderer.renderZone");

  /** Used by render layers to compose the layer prior to blending. */
  private FrameBuffer backBuffer;

  /**
   * Holds the results of all layers rendered so far.
   *
   * <p>If any rendering layer binds a different buffer, it must rebind this buffer before
   * completing.
   */
  private FrameBuffer resultsBuffer;

  /**
   * Used when a layer needs to blend {@link #backBuffer} with {@link #resultsBuffer} using a
   * shader.
   *
   * <p>After such a render, this buffer is swapped with {@link #resultsBuffer}.
   */
  private FrameBuffer spareBuffer;

  private com.badlogic.gdx.assets.AssetManager manager;
  private TextureAtlas atlas;
  private Texture onePixel;
  private ShapeDrawer drawer;
  private final GlyphLayout glyphLayout = new GlyphLayout();
  private TextRenderer textRenderer;
  private TextRenderer hudTextRenderer;
  private AreaRenderer areaRenderer;
  private DrawnElementRenderer drawnElementRenderer;
  private TokenOverlayRenderer tokenOverlayRenderer;
  private GridRenderer gridRenderer;

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
      boldFont = null;
    }

    environmentalLightingShader =
        new ShaderProgram(
            Gdx.files.classpath("net/rptools/maptool/client/ui/zone/gdx/environmentalLighting.vsh"),
            Gdx.files.classpath(
                "net/rptools/maptool/client/ui/zone/gdx/environmentalLighting.fsh"));

    manager = new com.badlogic.gdx.assets.AssetManager();
    loadAssets();

    var resolver = new InternalFileHandleResolver();
    manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
    manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

    width = Gdx.graphics.getWidth();
    height = Gdx.graphics.getHeight();

    // Cam for 3D-Models
    cam3d = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam3d.lookAt(0, 0, 0);

    cam = new OrthographicCamera();
    cam.setToOrtho(false);

    hudCam = new OrthographicCamera();
    hudCam.setToOrtho(false);

    updateCam();

    batch = new PolygonSpriteBatch();
    batch.enableBlending();

    backBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    resultsBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    spareBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

    // TODO: Add it to the texture atlas
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.drawPixel(0, 0);
    onePixel = new Texture(pixmap);
    pixmap.dispose();
    TextureRegion region = new TextureRegion(onePixel, 0, 0, 1, 1);
    drawer = new ShapeDrawer(batch, region);

    areaRenderer = new AreaRenderer(drawer);
    drawnElementRenderer = new DrawnElementRenderer(areaRenderer);
    tokenOverlayRenderer = new TokenOverlayRenderer(areaRenderer);
    gridRenderer = new GridRenderer(areaRenderer, hudCam);

    initialized = true;
  }

  @Override
  public void dispose() {
    environmentalLightingShader.dispose();
    manager.dispose();
    batch.dispose();
    if (zoneCache != null) {
      zoneCache.dispose();
    }
    onePixel.dispose();
  }

  @Override
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;

    backBuffer.dispose();
    backBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

    resultsBuffer.dispose();
    resultsBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

    spareBuffer.dispose();
    spareBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

    updateCam();
  }

  private void drawBackBuffer(BlendFunction blendDown) {
    setProjectionMatrix(hudCam.combined);
    resultsBuffer.begin();
    blendDown.applyToBatch(batch);
    batch.draw(backBuffer.getColorBufferTexture(), 0, 0, width, height, 0, 0, 1, 1);
    setProjectionMatrix(cam.combined);
    // Leave results buffer current for the next folks.
  }

  private void drawBackBuffer(ShaderProgram shader) {
    var oldShader = batch.getShader();

    setProjectionMatrix(hudCam.combined);
    spareBuffer.begin();
    batch.setShader(shader);
    ScreenUtils.clear(Color.CLEAR);
    BlendFunction.SRC_ONLY.applyToBatch(batch);
    try {
      shader.setUniformi("u_dst", BLENDING_TEXTURE_INDEX);
      try {
        resultsBuffer.getColorBufferTexture().bind(BLENDING_TEXTURE_INDEX);
        // Avoid affecting resultsResults.getColorBufferTexture() any more (OpenGL state machine)
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        batch.draw(backBuffer.getColorBufferTexture(), 0, 0, width, height, 0, 0, 1, 1);
      } finally {
        batch.flush();

        // Swap buffers
        var tmp = resultsBuffer;
        resultsBuffer = spareBuffer;
        spareBuffer = tmp;

        // Leave results buffer current for the next folks.
      }
    } finally {
      batch.setShader(oldShader);
    }

    setProjectionMatrix(cam.combined);
  }

  private void updateCam() {
    if (cam == null) return;

    cam3d.viewportWidth = width;
    cam3d.viewportHeight = height;

    cam3d.position.x = zoom * (width / 2f + offsetX);
    cam3d.position.y = zoom * (height / 2f * -1 + offsetY);
    cam3d.position.z =
        (zoom * height) / (2f * (float) Math.tan(Math.toRadians(cam3d.fieldOfView / 2f)));
    cam3d.far = 1.1f * cam3d.position.z;
    cam3d.near = 0.1f * cam3d.position.z;
    cam3d.update();

    cam.viewportWidth = width;
    cam.viewportHeight = height;
    cam.position.x = zoom * (width / 2f + offsetX);
    cam.position.y = zoom * (height / 2f * -1 + offsetY);
    cam.zoom = zoom;
    cam.update();

    hudCam.viewportWidth = width;
    hudCam.viewportHeight = height;
    hudCam.position.x = width / 2f;
    hudCam.position.y = height / 2f;
    hudCam.update();
  }

  @Override
  public void render() {
    viewModel.update();

    // System.out.println("FPS:   " + Gdx.graphics.getFramesPerSecond());
    var delta = Gdx.graphics.getDeltaTime();
    stateTime += delta;
    manager.finishLoading();

    if (atlas == null) {
      atlas = manager.get(ATLAS, TextureAtlas.class);
      zoneCache.setSharedAtlas(atlas);
    }

    if (normalFont == null) {
      normalFont = manager.get(FONT_NORMAL, BitmapFont.class);
      textRenderer = new TextRenderer(atlas, batch, normalFont);
      hudTextRenderer = new TextRenderer(atlas, batch, normalFont, false);
    }

    ensureTtfFont();
    ScreenUtils.clear(Color.BLACK);
    try {
      doRendering();
    } catch (Exception ex) {
      log.warn("Error while rendering", ex);
    }
  }

  private void ensureTtfFont() {
    if (zoneCache == null) return;

    var fontScale =
        (float) zoneCache.getZone().getGrid().getSize()
            / 50; // Font size of 12 at grid size 50 is default

    if (fontScale == this.boldFontScale && boldFont != null) return;

    var fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    //    fontParams.fontFileName = "net/rptools/maptool/client/fonts/OpenSans-Bold.ttf";
    fontParams.fontFileName =
        String.format("net/rptools/maptool/client/fonts/%s/%s-Bold.ttf", font, font);
    fontParams.fontParameters.size = (int) (12 * fontScale);
    fontParams.loadedCallback = GdxRenderer::premultiplyFontOnLoad;
    manager.load(FONT_BOLD, BitmapFont.class, fontParams);
    manager.finishLoading();
    boldFont = manager.get(FONT_BOLD, BitmapFont.class);
    boldFontScale = fontScale;
  }

  private void loadAssets() {
    manager.load(ATLAS, TextureAtlas.class);
    var fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    fontParams.fontFileName =
        String.format("net/rptools/maptool/client/fonts/%s/%s-Regular.ttf", font, font);
    fontParams.fontParameters.size = 12;
    fontParams.loadedCallback = GdxRenderer::premultiplyFontOnLoad;

    manager.load(FONT_NORMAL, BitmapFont.class, fontParams);
  }

  private void doRendering() {
    batch.enableBlending();
    // Framebuffer is premultiplied. Assume source textures are as well (can be changed for
    // operations that require something else).
    BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER.applyToBatch(batch);

    // this happens sometimes when starting with ide (non-debug)
    if (batch.isDrawing()) batch.end();
    batch.begin();

    if (zoneCache == null || !renderZone) return;

    initializeTimer();
    if (zoneCache.getZoneRenderer() == null) return;

    setScale(viewModel.getZoneScale());

    timer.start("paintComponent:createView");
    PlayerView playerView = viewModel.getPlayerView();
    timer.stop("paintComponent:createView");

    setProjectionMatrix(cam.combined);

    renderZone(playerView);

    setProjectionMatrix(hudCam.combined);

    var loadingProgress = viewModel.getLoadingStatus();
    if (loadingProgress.isPresent()) {
      hudTextRenderer.drawBoxedString(loadingProgress.get(), width / 2f, height / 2f);
    } else if (MapTool.getCampaign().isBeingSerialized()) {
      hudTextRenderer.drawBoxedString("    Please Wait    ", width / 2f, height / 2f);
    }

    float noteVPos = 20;
    if (!zoneCache.getZone().isVisible() && playerView.isGMView()) {
      hudTextRenderer.drawBoxedString(
          I18N.getText("zone.map_not_visible"), width / 2f, height - noteVPos);
      noteVPos += 20;
    }
    if (AppState.isShowAsPlayer()) {
      hudTextRenderer.drawBoxedString(
          I18N.getText("zone.player_view"), width / 2f, height - noteVPos);
    }

    hudTextRenderer.drawString("FPS:   " + Gdx.graphics.getFramesPerSecond(), width - 30, 30);
    hudTextRenderer.drawString("Draws: " + batch.renderCalls, width - 30, 16);

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
    visibleScreenArea = null;
    lastView = null;
  }

  private void renderZone(PlayerView view) {
    if (!prerender(view)) {
      return;
    }

    resultsBuffer.begin();
    BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER.applyToBatch(batch);
    ScreenUtils.clear(Color.CLEAR);

    renderBoard();

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.BACKGROUND, view)) {
      renderDrawables(zoneCache.getZone().getDrawnElements(Zone.Layer.BACKGROUND));
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.BACKGROUND, view)) {
      timer.start("tokensBackground");
      renderTokens(zoneCache.getZone().getTokensOnLayer(Zone.Layer.BACKGROUND, false), view, false);
      timer.stop("tokensBackground");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.OBJECT, view)) {
      // Drawables on the object layer are always below the grid, and...
      timer.start("drawableObjects");
      drawnElementRenderer.render(
          batch, zoneCache.getZone(), zoneCache.getZone().getDrawnElements(Zone.Layer.OBJECT));
      timer.stop("drawableObjects");
    }

    timer.start("grid");
    setProjectionMatrix(hudCam.combined);
    gridRenderer.render();
    setProjectionMatrix(cam.combined);
    timer.stop("grid");

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.OBJECT, view)) {
      // ... Images on the object layer are always ABOVE the grid.
      timer.start("tokensStamp");
      renderTokens(zoneCache.getZone().getTokensOnLayer(Zone.Layer.OBJECT, false), view, false);
      timer.stop("tokensStamp");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("lights");
      renderLights(view);
      timer.stop("lights");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("lumens");
      renderLumens(view);
      timer.stop("lumens");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("auras");
      renderAuras(view);
      timer.stop("auras");
    }

    renderPlayerDarkness(view);

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
    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("drawableTokens");
      drawnElementRenderer.render(
          batch, zoneCache.getZone(), zoneCache.getZone().getDrawnElements(Zone.Layer.TOKEN));
      timer.stop("drawableTokens");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.GM, view)) {
        timer.start("drawableGM");
        drawnElementRenderer.render(
            batch, zoneCache.getZone(), zoneCache.getZone().getDrawnElements(Zone.Layer.GM));
        timer.stop("drawableGM");
      }
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.GM, view)) {
        timer.start("tokensGM");
        renderTokens(zoneCache.getZone().getTokensOnLayer(Zone.Layer.GM, false), view, false);
        timer.stop("tokensGM");
      }
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("tokens");
      renderTokens(zoneCache.getZone().getTokensOnLayer(Zone.Layer.TOKEN, false), view, false);
      timer.stop("tokens");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("unowned movement");
      showBlockedMoves(view, zoneCache.getZoneRenderer().getUnOwnedMovementSet(view));
      timer.stop("unowned movement");
    }

    if (AppState.getShowTextLabels()) {
      renderLabels(view);
    }

    if (zoneCache.getZone().hasFog()) {
      batch.flush();
      backBuffer.begin();
      renderFog(view);
      batch.flush();
      backBuffer.end();

      drawBackBuffer(BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER);
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      // Jamz: If there is fog or vision we may need to re-render vision-blocking type tokens
      // For example. this allows a "door" stamp to block vision but still allow you to see the
      // door.
      timer.start("tokens - always visible");
      renderTokens(zoneCache.getZone().getTokensAlwaysVisible(), view, true);
      timer.stop("tokens - always visible");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      // if there is fog or vision we may need to re-render figure type tokens
      // and figure tokens need sorting via alternative logic.
      List<Token> tokens = zoneCache.getZone().getFigureTokens();
      List<Token> sortedTokens = new ArrayList<>(tokens);
      sortedTokens.sort(zoneCache.getZone().getFigureZOrderComparator());
      timer.start("tokens - figures");
      renderTokens(sortedTokens, view, true);
      timer.stop("tokens - figures");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("owned movement");
      showBlockedMoves(view, zoneCache.getZoneRenderer().getOwnedMovementSet(view));
      timer.stop("owned movement");
    }

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      // Text associated with tokens being moved is added to a list to be drawn after, i.e. on top
      // of, the tokens themselves.
      // So if one moving token is on top of another moving token, at least the textual identifiers
      // will be visible.
      setProjectionMatrix(hudCam.combined);
      timer.start("token name/labels");
      renderRenderables();
      timer.stop("token name/labels");
      setProjectionMatrix(cam.combined);
    }

    timer.start("visionOverlay");
    renderVisionOverlay(view);
    timer.stop("visionOverlay");

    timer.start("renderCoordinates");
    renderCoordinates(view);
    timer.stop("renderCoordinates");

    if (zoneCache.getZoneRenderer().shouldRenderLayer(Zone.Layer.TOKEN, view)) {
      timer.start("lightSourceIconOverlay.paintOverlay");
      paintLightSourceIconOverlay(view);
      timer.stop("lightSourceIconOverlay.paintOverlay");
    }

    batch.flush();
    resultsBuffer.end();

    setProjectionMatrix(hudCam.combined);
    BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER.applyToBatch(batch);
    batch.draw(resultsBuffer.getColorBufferTexture(), 0, 0, width, height, 0, 0, 1, 1);
    setProjectionMatrix(cam.combined);
  }

  /**
   * Updates renderer state prior to rendering the zone.
   *
   * @return {@code true} if rendering should proceed.
   */
  private boolean prerender(PlayerView view) {
    if (viewModel.getLoadingStatus().isPresent() || MapTool.getCampaign().isBeingSerialized()) {
      return false;
    }

    if (lastView != null && !lastView.equals(view)) {
      invalidateCurrentViewCache();
    }
    lastView = view;
    itemRenderList.clear();

    // Calculations
    timer.start("calcs-1");
    timer.start("ZoneRenderer-getVisibleArea");
    if (visibleScreenArea == null) {
      visibleScreenArea = zoneCache.getZoneView().getVisibleArea(viewModel.getPlayerView());
    }
    timer.stop("ZoneRenderer-getVisibleArea");

    timer.stop("calcs-1");
    timer.start("calcs-2");
    exposedFogArea = new Area(zoneCache.getZone().getExposedArea());
    timer.stop("calcs-2");

    return true;
  }

  private void renderCoordinates(PlayerView view) {
    if (!AppState.isShowCoordinates()
        || !(zoneCache.getZone().getGrid() instanceof SquareGrid grid)) return;

    batch.setProjectionMatrix(hudCam.combined);
    var font = boldFont;

    float cellSize = (float) zoneCache.getZoneRenderer().getScaledGridSize();
    CellPoint topLeft =
        grid.convert(new ScreenPoint(0, 0).convertToZone(zoneCache.getZoneRenderer()));
    ScreenPoint sp = ScreenPoint.fromZonePoint(zoneCache.getZoneRenderer(), grid.convert(topLeft));

    Dimension size = zoneCache.getZoneRenderer().getSize();
    glyphLayout.setText(font, "MMM");
    float startX = glyphLayout.width + 10;

    float x = (float) (sp.x + cellSize / 2); // Start at middle of the cell that's on screen
    float nextAvailableSpace = -1;
    while (x < size.width) {
      String coord = Integer.toString(topLeft.x);
      glyphLayout.setText(font, coord);
      float strWidth = glyphLayout.width;
      float strX = (int) x - strWidth / 2;

      if (x > startX && strX > nextAvailableSpace) {
        font.setColor(Color.BLACK);
        font.draw(batch, coord, strX, height - glyphLayout.height / 2 - 1);
        font.setColor(Color.ORANGE);
        font.draw(batch, coord, strX - 1, height - glyphLayout.height / 2);

        nextAvailableSpace = strX + strWidth + 10;
      }
      x += cellSize;
      topLeft.x++;
    }
    float y = (float) sp.y + cellSize / 2f; // Start at middle of the cell that's on screen
    nextAvailableSpace = -1;
    while (y < size.height) {
      String coord = grid.decimalToAlphaCoord(topLeft.y);

      float strY = y + font.getAscent() / 2;

      if (y > glyphLayout.height && strY > nextAvailableSpace) {
        font.setColor(Color.BLACK);
        font.draw(batch, coord, 10, height - strY + glyphLayout.height / 2 - 1);
        font.setColor(Color.YELLOW);
        font.draw(batch, coord, 10 - 1, height - strY + glyphLayout.height / 2);

        nextAvailableSpace = strY + font.getAscent() / 2 + 10;
      }
      y += cellSize;
      topLeft.y++;
    }
    batch.setProjectionMatrix(cam.combined);
  }

  private void paintLightSourceIconOverlay(PlayerView view) {
    if (!AppState.isShowLightSources() || !view.isGMView()) {
      return;
    }

    TextureRegion lightbulb = zoneCache.fetch("lightbulb");
    for (var point : viewModel.getLightPositions()) {
      var x = point.getX() - lightbulb.getRegionWidth() / 2.;
      var y = -point.getY() - lightbulb.getRegionHeight() / 2.;
      batch.draw(lightbulb, (float) x, (float) y);
    }
  }

  private void renderPlayerDarkness(PlayerView view) {
    if (view.isGMView()) {
      // GMs see the darkness rendered as lights, not as blackness.
      return;
    }

    final var darkness = zoneCache.getZoneView().getIllumination(view).getDarkenedArea();
    if (darkness.isEmpty()) {
      // Skip the rendering work if it isn't necessary.
      return;
    }
    areaRenderer.setColor(Color.BLACK);
    areaRenderer.fillArea(batch, darkness);
  }

  private void renderVisionOverlay(PlayerView view) {
    var tokenUnderMouse = zoneCache.getZoneRenderer().getTokenUnderMouse();
    if (tokenUnderMouse == null) return;

    Area currentTokenVisionArea = zoneCache.getZoneView().getVisibleArea(tokenUnderMouse, view);
    if (currentTokenVisionArea == null) {
      return;
    }
    Area combined = new Area(currentTokenVisionArea);
    ExposedAreaMetaData meta =
        zoneCache.getZone().getExposedAreaMetaData(tokenUnderMouse.getExposedAreaGUID());

    Area tmpArea = new Area(meta.getExposedAreaHistory());
    tmpArea.add(zoneCache.getZone().getExposedArea());
    if (zoneCache.getZone().hasFog()) {
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
      areaRenderer.setColor(Color.WHITE);
      areaRenderer.drawArea(
          batch, combined, false, (float) (1 / viewModel.getZoneScale().getScale()));
      renderHaloArea(combined);
    }
  }

  private void renderHaloArea(Area visible) {
    var tokenUnderMouse = zoneCache.getZoneRenderer().getTokenUnderMouse();
    if (tokenUnderMouse == null) return;

    boolean useHaloColor =
        tokenUnderMouse.getHaloColor() != null && AppPreferences.useHaloColorOnVisionOverlay.get();
    if (tokenUnderMouse.getVisionOverlayColor() != null || useHaloColor) {
      java.awt.Color visionColor =
          useHaloColor ? tokenUnderMouse.getHaloColor() : tokenUnderMouse.getVisionOverlayColor();

      tmpColor
          .set(
              visionColor.getRed() / 255f,
              visionColor.getGreen() / 255f,
              visionColor.getBlue() / 255f,
              AppPreferences.haloOverlayOpacity.get() / 255f)
          .premultiplyAlpha();
      areaRenderer.setColor(tmpColor);
      areaRenderer.fillArea(batch, visible);
    }
  }

  private void renderRenderables() {
    for (ItemRenderer renderer : itemRenderList) {
      renderer.render(cam, zoom);
    }
  }

  private void renderFog(PlayerView view) {
    var zoneView = zoneCache.getZoneView();

    timer.start("renderFog-visibleArea");
    Area visibleArea = zoneView.getVisibleArea(view);
    timer.stop("renderFog-visibleArea");

    String msg = null;
    if (timer.isEnabled()) {
      msg = "renderFog-combined(" + (view.isUsingTokenView() ? view.getTokens().size() : 0) + ")";
    }
    timer.start(msg);
    Area exposedArea = zoneView.getExposedArea(view);
    timer.stop(msg);

    Area softFogArea;
    Area clearArea;
    if (zoneView.isUsingVision()) {
      softFogArea = exposedArea;
      clearArea = new Area(visibleArea);
      clearArea.intersect(softFogArea);
    } else {
      softFogArea = new Area();
      clearArea = exposedArea;
    }

    timer.start("renderFog");
    ScreenUtils.clear(Color.CLEAR);

    BlendFunction.SRC_ONLY.applyToBatch(batch);
    setProjectionMatrix(cam.combined);

    timer.start("renderFog-hardFow");
    // Fill
    batch.setColor(Color.WHITE);
    var paint = zoneCache.getZone().getFogPaint();
    var fogPaint = zoneCache.getPaint(paint);
    var fogColor = fogPaint.color();
    fogPaint
        .color()
        .set(fogColor.r, fogColor.g, fogColor.b, view.isGMView() ? .6f : 1f)
        .premultiplyAlpha();
    fillViewportWith(fogPaint);
    timer.stop("renderFog-hardFow");

    timer.start("renderFog-softFow");
    if (!softFogArea.isEmpty()) {
      areaRenderer.setColor(tmpColor.set(0, 0, 0, AppPreferences.fogOverlayOpacity.get() / 255.0f));
      // Fill in the exposed area
      areaRenderer.fillArea(batch, softFogArea);
    }
    timer.stop("renderFog-softFow");

    timer.start("renderFog-exposedArea");
    if (!clearArea.isEmpty()) {
      areaRenderer.setColor(tmpColor.set(Color.CLEAR));
      // Fill in the exposed area
      areaRenderer.fillArea(batch, clearArea);
    }
    timer.stop("renderFog-exposedArea");

    timer.start("renderFog-outline");
    // If there is no boundary between soft fog and visible area, there is no need for an outline.
    if (!softFogArea.isEmpty() && !clearArea.isEmpty()) {
      areaRenderer.setColor(Color.BLACK);
      areaRenderer.drawArea(
          batch, visibleScreenArea, false, (float) (1 / viewModel.getZoneScale().getScale()));
    }
    timer.stop("renderFog-outline");

    timer.stop("renderFog");
  }

  private void setProjectionMatrix(Matrix4 matrix) {
    batch.setProjectionMatrix(matrix);
    drawer.update();
  }

  private void renderLabels(PlayerView view) {
    timer.start("labels-1");

    for (Label label : zoneCache.getZone().getLabels()) {
      timer.start("labels-1.1");
      Color.argb8888ToColor(tmpColor, label.getForegroundColor().getRGB());
      tmpColor.premultiplyAlpha();
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

  private void showBlockedMoves(PlayerView view, Set<SelectionSet> movementSet) {
    var selectionSetMap = zoneCache.getZoneRenderer().getSelectionSetMap();
    if (selectionSetMap.isEmpty()) {
      return;
    }

    boolean clipInstalled = false;
    for (SelectionSet set : movementSet) {
      Token keyToken = zoneCache.getZone().getToken(set.getKeyToken());
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
        Token token = zoneCache.getZone().getToken(tokenGUID);

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
            && zoneCache.getZone().hasFog()
            && zoneCache.getZoneView().isUsingVision()) {
          continue;
        }

        // ... or if it doesn't have an image to display. (Hm, should still show *something*?)
        Asset asset = AssetManager.getAsset(token.getImageAssetId());
        if (asset == null) {
          continue;
        }

        // OPTIMIZE: combine this with the code in renderTokens()
        java.awt.Rectangle footprintBounds = token.getFootprintBounds(zoneCache.getZone());

        // get token image, using image table if present
        Sprite image = zoneCache.getSprite(token.getImageAssetId(), stateTime);
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
        if (token == keyToken && token.getLayer().supportsWalker()) {
          renderPath(
              walker != null ? walker.getPath() : set.getGridlessPath(),
              token.getFootprint(zoneCache.getZone().getGrid()));
        }

        // Show current Blocked Movement directions for A*
        if (walker != null && (log.isDebugEnabled() || showAstarDebugging)) {
          Map<CellPoint, Set<CellPoint>> blockedMovesByTarget = walker.getBlockedMoves();
          // Color currentColor = g.getColor();
          for (var entry : blockedMovesByTarget.entrySet()) {
            var position = entry.getKey();
            var blockedMoves = entry.getValue();

            for (CellPoint point : blockedMoves) {
              ZonePoint zp =
                  zoneCache.getZoneRenderer().getZone().getGrid().midZonePoint(point, position);
              double r = (zp.x - 1) * 45;
              showBlockedMoves(zp, r, zoneCache.getSprite("block_move"), 1.0f);
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

          Grid grid = zoneCache.getZone().getGrid();
          boolean checkForFog =
              MapTool.getServerPolicy().isUseIndividualFOW()
                  && zoneCache.getZoneView().isUsingVision();
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
                java.awt.Rectangle tokBounds = token.getFootprintBounds(zoneCache.getZone());
                tokenRectangle = new java.awt.Rectangle();
                tokenRectangle.setBounds(
                    lastPoint.x,
                    lastPoint.y,
                    (int) tokBounds.getWidth(),
                    (int) tokBounds.getHeight());
              }
              showLabels =
                  showLabels
                      || zoneCache
                          .getZoneRenderer()
                          .getZoneView()
                          .getVisibleArea(view)
                          .intersects(tokenRectangle);
            }
          } else {
            boolean hasFog = zoneCache.getZone().hasFog();
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

            if (token.getLayer().supportsWalker() && AppState.getShowMovementMeasurements()) {
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
                c /= zoneCache.getZone().getGrid().getSize(); // Number of "cells"
                c *= zoneCache.getZone().getUnitsPerCell(); // "actual" distance traveled
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
    var resizeWidth =
        (float) zoneCache.getZone().getGrid().getCellWidth() / image.getWidth() * .25f;
    var resizeHeight =
        (float) zoneCache.getZone().getGrid().getCellHeight() / image.getHeight() * .25f;

    var w = image.getWidth() * resizeWidth * size;
    var h = image.getHeight() * resizeHeight * size;

    image.setSize(w, h);
    image.setPosition(zp.x - w / 2f, -(zp.y - h / 2f));
    image.draw(batch);
  }

  private void renderLumensOverlay(PlayerView view, float overlayAlpha) {
    final var disjointLumensLevels = zoneCache.getZoneView().getDisjointObscuredLumensLevels(view);

    BlendFunction.SRC_ONLY.applyToBatch(batch);
    // At night, show any uncovered areas as dark. In daylight, show them as light (clear).
    if (zoneCache.getZone().getVisionType() == Zone.VisionType.NIGHT) {
      ScreenUtils.clear(0, 0, 0, overlayAlpha);
    } else {
      ScreenUtils.clear(Color.CLEAR);
    }

    timer.start("renderLumensOverlay:drawLumens");
    for (final var lumensLevel : disjointLumensLevels) {
      final var lumensStrength = lumensLevel.lumensStrength();

      // Light is weaker than darkness, so do it first.
      float lightOpacity;
      float lightShade;
      if (lumensStrength == 0) {
        // This area represents daylight, so draw it as clear despite the low value.
        lightShade = 1.f;
        lightOpacity = 0;
      } else if (lumensStrength >= 100) {
        // Bright light, render mostly clear.
        lightShade = 1.f;
        lightOpacity = 1.f / 10.f;
      } else {
        lightShade = Math.max(0.f, Math.min(lumensStrength / 100.f, 1.f));
        lightShade *= lightShade;
        lightOpacity = 1.f;
      }

      timer.start("renderLumensOverlay:drawLights:fillArea");

      areaRenderer.setColor(
          tmpColor
              .set(lightShade, lightShade, lightShade, lightOpacity * overlayAlpha)
              .premultiplyAlpha());
      areaRenderer.fillArea(batch, lumensLevel.lightArea());

      areaRenderer.setColor(tmpColor.set(0.f, 0.f, 0.f, overlayAlpha));
      areaRenderer.fillArea(batch, lumensLevel.darknessArea());
      timer.stop("renderLumensOverlay:drawLights:fillArea");
    }

    timer.stop("renderLumensOverlay:drawLumens");

    BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER.applyToBatch(batch);
    // Now draw borders around each region if configured.
    batch.setColor(Color.WHITE);
    final var borderThickness = AppPreferences.lumensOverlayBorderThickness.get();
    if (borderThickness > 0) {
      tmpColor.set(0.f, 0.f, 0.f, 1.f);
      for (final var lumensLevel : disjointLumensLevels) {
        timer.start("renderLumensOverlay:drawLights:drawArea");
        areaRenderer.setColor(tmpColor);
        areaRenderer.drawArea(batch, lumensLevel.lightArea(), true, borderThickness);
        areaRenderer.setColor(tmpColor);
        areaRenderer.drawArea(batch, lumensLevel.darknessArea(), true, borderThickness);
        timer.stop("renderLumensOverlay:drawLights:drawArea");
      }
    }
  }

  private void renderLights(PlayerView view) {
    if (AppState.isShowLights()) {
      timer.start("renderLights:getLights");
      final var drawableLights = zoneCache.getZoneView().getDrawableLights(view);
      timer.stop("renderLights:getLights");

      timer.start("renderLights:renderLightOverlay");
      if (!drawableLights.isEmpty()) {
        batch.flush();
        backBuffer.begin();
        renderLightOverlay(
            drawableLights,
            AppPreferences.lightOverlayOpacity.get() / 255.f,
            BlendFunction.SCREEN,
            false);
        batch.flush();
        backBuffer.end();

        if (zoneCache.getZone().getLightingStyle() == Zone.LightingStyle.ENVIRONMENTAL) {
          drawBackBuffer(environmentalLightingShader);
        } else {
          drawBackBuffer(BlendFunction.ALPHA_SRC_OVER);
        }
      }
      timer.stop("renderLights:renderLightOverlay");
    }
  }

  private void renderLumens(PlayerView view) {
    if (AppState.isShowLumensOverlay()) {
      batch.flush();
      backBuffer.begin();
      renderLumensOverlay(view, AppPreferences.lumensOverlayOpacity.get() / 255.0f);
      batch.flush();
      backBuffer.end();
      drawBackBuffer(BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER);
    }
  }

  private void renderAuras(PlayerView view) {
    timer.start("renderAuras:getAuras");
    final var drawableAuras = zoneCache.getZoneView().getDrawableAuras(view);
    timer.stop("renderAuras:getAuras");

    batch.flush();
    backBuffer.begin();
    renderLightOverlay(
        drawableAuras,
        AppPreferences.auraOverlayOpacity.get() / 255.0f,
        BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER,
        true);
    batch.flush();
    backBuffer.end();
    drawBackBuffer(BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER);
  }

  private void renderLightOverlay(
      Collection<DrawableLight> lights,
      float alpha,
      BlendFunction lightBlending,
      boolean premultipy) {
    // Set up a buffer image for lights to be drawn onto before the map
    timer.start("renderLightOverlay:allocateBuffer");
    ScreenUtils.clear(Color.CLEAR);
    setProjectionMatrix(cam.combined);
    lightBlending.applyToBatch(batch);
    timer.stop("renderLightOverlay:allocateBuffer");

    // Draw lights onto the buffer image so the map doesn't affect how they blend
    timer.start("renderLightOverlay:drawLights");
    for (var light : lights) {
      var paint = light.getPaint().getPaint();

      if (paint instanceof java.awt.Color color) {
        Color.argb8888ToColor(tmpColor, color.getRGB());
      } else {
        log.warn("Unexpected color type: {}", paint.getClass());
        continue;
      }
      tmpColor.set(tmpColor.r, tmpColor.g, tmpColor.b, alpha);
      if (premultipy) {
        tmpColor.premultiplyAlpha();
      }
      areaRenderer.setColor(tmpColor);

      var triangulation = areaRenderer.triangulate(light.getAreaAsPolygons());
      areaRenderer.fill(batch, triangulation);
    }
    timer.stop("renderLightOverlay:drawLights");
  }

  private void createScreenshot(String name) {
    var file = Gdx.files.absolute("C:\\Users\\tkunze\\OneDrive\\Desktop\\" + name + ".png");
    if (!file.exists()) {
      Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
      PixmapIO.writePNG(file, pixmap, Deflater.DEFAULT_COMPRESSION, true);
      pixmap.dispose();
    }
  }

  private void renderBoard() {
    if (!zoneCache.getZone().drawBoard()) return;

    var paint = zoneCache.getZone().getBackgroundPaint();
    fillViewportWith(zoneCache.getPaint(paint));

    var map = zoneCache.getSprite(zoneCache.getZone().getMapAssetId(), stateTime);
    if (map != null) {
      map.setPosition(
          zoneCache.getZone().getBoardX(), zoneCache.getZone().getBoardY() - map.getHeight());
      map.draw(batch);
    }
  }

  private void renderDrawables(List<DrawnElement> drawables) {
    timer.start("drawableBackground");
    drawnElementRenderer.render(batch, zoneCache.getZone(), drawables);
    timer.stop("drawableBackground");
  }

  private void fillViewportWith(ZoneCache.GdxPaint paint) {
    var w = cam.viewportWidth * zoom;
    var h = cam.viewportHeight * zoom;
    var startX = (cam.position.x - cam.viewportWidth * zoom / 2);

    var startY = (cam.position.y - cam.viewportHeight * zoom / 2);
    var vertices =
        new float[] {
          startX, startY, startX, startY + h, startX + w, startY + h, startX + w, startY
        };

    var indices = new short[] {1, 0, 3, 3, 2, 1};

    var polySprite = new PolygonSprite(new PolygonRegion(paint.textureRegion(), vertices, indices));
    polySprite.setColor(paint.color());
    polySprite.draw(batch);
  }

  private void renderTokens(List<Token> tokenList, PlayerView view, boolean figuresOnly) {
    if (tokenList.isEmpty() || visibleScreenArea == null) {
      return;
    }

    boolean isGMView = view.isGMView(); // speed things up

    for (Token token : tokenList) {
      if (token.getShape() != Token.TokenShape.FIGURE && figuresOnly && !token.isAlwaysVisible()) {
        continue;
      }

      timer.start("tokenlist-1");
      try {
        if (token.getLayer().isStampLayer() && viewModel.isTokenMoving(token.getId())) {
          continue;
        }
        // Don't bother if it's not visible
        // NOTE: Not going to use zoneCache.getZone().isTokenVisible as it is very slow. In fact,
        // it's faster
        // to just draw the tokens and let them be clipped
        if ((!token.isVisible() || !token.getLayer().isVisibleToPlayers()) && !isGMView) {
          continue;
        }
        if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
          continue;
        }
      } finally {
        // This ensures that the timer is always stopped
        timer.stop("tokenlist-1");
      }

      java.awt.Rectangle footprintBounds = token.getFootprintBounds(zoneCache.getZone());
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
        if (!isGMView
            && token.getLayer().supportsVision()
            && zoneCache.getZoneView().isUsingVision()) {
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
      if (zoneCache.getZoneRenderer().getShowPathList().contains(token)
          && token.getLastPath() != null) {
        renderPath(token.getLastPath(), token.getFootprint(zoneCache.getZone().getGrid()));
      }
      timer.stop("renderTokens:ShowPath");

      // get token image sprite, using image table if present
      var imageKey = token.getTokenImageAssetId();
      Sprite image = zoneCache.getSprite(imageKey, stateTime);

      prepareTokenSprite(image, token, footprintBounds);

      // Render Halo
      var haloColor = token.getHaloColor();
      if (haloColor != null) {
        Color.argb8888ToColor(tmpColor, haloColor.getRGB());
        tmpColor.premultiplyAlpha();
        areaRenderer.setColor(tmpColor);
        areaRenderer.drawArea(
            batch,
            zoneCache.getZone().getGrid().getTokenCellArea(tokenBounds),
            false,
            (float) (AppPreferences.haloLineWidth.get() / viewModel.getZoneScale().getScale()));
      }

      // Calculate alpha Transparency from token and use opacity for indicating that token is moving
      float opacity = token.getTokenOpacity();
      if (viewModel.isTokenMoving(token.getId())) {
        opacity = opacity / 2.0f;
      }

      Area tokenCellArea = zoneCache.getZone().getGrid().getTokenCellArea(tokenBounds);
      Area cellArea = new Area(visibleScreenArea);
      cellArea.intersect(tokenCellArea);

      // Finally render the token image
      timer.start("tokenlist-7");
      image.setColor(1, 1, 1, opacity);
      if (!isGMView
          && zoneCache.getZoneView().isUsingVision()
          && (token.getShape() == Token.TokenShape.FIGURE)) {
        if (zoneCache
            .getZone()
            .getGrid()
            .checkCenterRegion(tokenCellArea.getBounds(), visibleScreenArea)) {
          // if we can see the centre, draw the whole token
          image.draw(batch);
        } else {
          // else draw the clipped token
          paintClipped(resultsBuffer, image, tokenCellArea, cellArea);
        }
      } else if (!isGMView && zoneCache.getZoneView().isUsingVision() && token.isAlwaysVisible()) {
        // Jamz: Always Visible tokens will get rendered again here to place on top of FoW
        if (GraphicsUtil.intersects(visibleScreenArea, tokenCellArea)) {
          // if we can see a portion of the stamp/token, draw the whole thing, defaults to 2/9ths
          if (zoneCache
              .getZone()
              .getGrid()
              .checkRegion(
                  tokenCellArea.getBounds(),
                  visibleScreenArea,
                  token.getAlwaysVisibleTolerance())) {

            image.draw(batch);

          } else {
            // else draw the clipped stamp/token
            // This will only show the part of the token that does not have VBL on it
            // as any VBL on the token will block LOS, affecting the clipping.
            paintClipped(resultsBuffer, image, tokenCellArea, cellArea);
          }
        }
      } else {
        // fallthrough normal token rendered against visible area
        if (zoneCache
            .getZoneRenderer()
            .isTokenInNeedOfClipping(
                token, viewModel.getZoneScale().toWorldSpace(tokenCellArea), isGMView)) {
          paintClipped(resultsBuffer, image, tokenCellArea, cellArea);
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
                && AppPreferences.forceFacingArrow.get() == false) {
              break;
            }
            java.awt.Shape arrow =
                getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);

            if (!zoneCache.getZone().getGrid().isIsometric()) {
              arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
            }

            float fx = origBounds.x + origBounds.width / zoom / 2f;
            float fy = origBounds.y + origBounds.height / zoom / 2f;

            tmpMatrix.idt();
            tmpMatrix.translate(fx, -fy, 0);
            batch.setTransformMatrix(tmpMatrix);
            drawer.update();

            if (token.getFacing() < 0) {
              tmpColor.set(Color.YELLOW);
            } else {
              tmpColor.set(1, 1, 0, 0.5f);
            }

            var arrowArea = new Area(arrow);
            areaRenderer.setColor(tmpColor);
            areaRenderer.fillArea(batch, arrowArea);

            areaRenderer.setColor(Color.DARK_GRAY);
            areaRenderer.drawArea(batch, arrowArea, false, 1);

            break;
          case TOP_DOWN:
            if (AppPreferences.forceFacingArrow.get() == false) {
              break;
            }
          case CIRCLE:
            arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
            if (zoneCache.getZone().getGrid().isIsometric()) {
              arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);
            }
            arrowArea = new Area(arrow);

            float cx = origBounds.x + origBounds.width / 2f;
            float cy = origBounds.y + origBounds.height / 2f;

            tmpMatrix.idt();
            tmpMatrix.translate(cx, -cy, 0);
            batch.setTransformMatrix(tmpMatrix);

            areaRenderer.setColor(Color.YELLOW);
            areaRenderer.fillArea(batch, arrowArea);
            areaRenderer.setColor(Color.DARK_GRAY);
            areaRenderer.drawArea(batch, arrowArea, false, 1);
            tmpMatrix.idt();
            batch.setTransformMatrix(tmpMatrix);
            break;
          case SQUARE:
            if (zoneCache.getZone().getGrid().isIsometric()) {
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
            areaRenderer.setColor(Color.YELLOW);

            areaRenderer.fillArea(batch, arrowArea);
            areaRenderer.setColor(Color.DARK_GRAY);
            areaRenderer.drawArea(batch, arrowArea, false, 1);
            batch.setTransformMatrix(tmpMatrix.idt());
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
            || overlay.isMouseover() && token != zoneCache.getZoneRenderer().getTokenUnderMouse()
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }
        tokenOverlayRenderer.render(stateTime, overlay, token, stateValue);
      }
      timer.stop("tokenlist-9");

      timer.start("tokenlist-10");

      for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
        Object barValue = token.getState(bar);
        BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
        if (overlay == null
            || overlay.isMouseover() && token != zoneCache.getZoneRenderer().getTokenUnderMouse()
            || !overlay.showPlayer(token, MapTool.getPlayer())) {
          continue;
        }
        tokenOverlayRenderer.render(stateTime, overlay, token, barValue);
      } // endfor
      timer.stop("tokenlist-10");

      timer.start("tokenlist-11");
      // Keep track of which tokens have been drawn so we can perform post-processing on them later
      // (such as selection borders and names/labels)
      if (!zoneCache.getZoneRenderer().getActiveLayer().equals(token.getLayer())) continue;

      timer.stop("tokenlist-11");
      timer.start("tokenlist-12");

      boolean useIF = MapTool.getServerPolicy().isUseIndividualFOW();

      // Selection and labels

      var tokenRectangle = token.getFootprintBounds(zoneCache.getZone());
      var gdxTokenRectangle =
          new Rectangle(
              tokenRectangle.x,
              -tokenRectangle.y - tokenRectangle.height,
              tokenRectangle.width,
              tokenRectangle.height);
      boolean isSelected =
          zoneCache.getZoneRenderer().getSelectedTokenSet().contains(token.getId());
      if (isSelected) {
        ImageBorder selectedBorder =
            token.getLayer().isStampLayer()
                ? AppStyle.selectedStampBorder
                : AppStyle.selectedBorder;
        if (viewModel.getHighlightCommonMacros().contains(token.getId())) {
          selectedBorder = AppStyle.commonMacroBorder;
        }
        if (!AppUtil.playerOwns(token)) {
          selectedBorder = AppStyle.selectedUnownedBorder;
        }
        if (useIF && token.getLayer().supportsVision() && zoneCache.getZoneView().isUsingVision()) {
          Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
          if (tool instanceof WallTopologyTool) {
            selectedBorder = RessourceManager.getBorder(Borders.FOW_TOOLS);
          }
        }

        setProjectionMatrix(hudCam.combined);
        tmpWorldCoord.set(gdxTokenRectangle.x, gdxTokenRectangle.y, 0);
        cam.project(tmpWorldCoord);

        gdxTokenRectangle.set(
            tmpWorldCoord.x,
            tmpWorldCoord.y,
            gdxTokenRectangle.width / zoom,
            gdxTokenRectangle.height / zoom);

        if (token.hasFacing()
            && (token.getShape() == Token.TokenShape.TOP_DOWN || token.getLayer().isStampLayer())) {

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
          AppState.isShowTokenNames() || token == zoneCache.getZoneRenderer().getTokenUnderMouse();

      // if policy does not auto-reveal FoW, check if fog covers the token (slow)
      if (showCurrentTokenLabel
          && !isGMView
          && (!zoneCache.getZoneView().isUsingVision()
              || !MapTool.getServerPolicy().isAutoRevealOnMovement())
          && !zoneCache.getZone().isTokenVisible(token)) {
        showCurrentTokenLabel = false;
      }
      if (showCurrentTokenLabel) {
        itemRenderList.add(
            new TokenLabelRenderer(token, zoneCache.getZone(), isGMView, textRenderer));
      }
      timer.stop("tokenlist-12");
    }

    timer.start("tokenlist-13");

    var tokenStackMap = viewModel.getTokenStackMap();

    // Stacks
    // TODO: find a cleaner way to indicate token layer
    if (!tokenList.isEmpty() && tokenList.get(0).getLayer().isTokenLayer()) {
      boolean hideTSI = AppPreferences.hideTokenStackIndicator.get();
      if (tokenStackMap != null
          && !hideTSI) { // FIXME Needed to prevent NPE but how can it be null?
        for (Token token : tokenStackMap.keySet()) {
          var tokenRectangle = token.getFootprintBounds(zoneCache.getZone());
          var stackImage = zoneCache.fetch("stack");
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
    if (token.getIsFlippedIso()) {
      image = zoneCache.getIsoSprite(token.getImageAssetId());
      token.setHeight((int) image.getHeight());
      token.setWidth((int) image.getWidth());
      footprintBounds = token.getFootprintBounds(zoneCache.getZone());
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
    AwtUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);

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

  private void renderImageBorderAround(ImageBorder border, Rectangle bounds) {
    var imagePath = border.getImagePath();
    var index = imagePath.indexOf("border/");
    var bordername = imagePath.substring(index);

    var topRight = zoneCache.fetch(bordername + "/tr");
    var top = zoneCache.fetch(bordername + "/top");
    var topLeft = zoneCache.fetch(bordername + "/tl");
    var left = zoneCache.fetch(bordername + "/left");
    var bottomLeft = zoneCache.fetch(bordername + "/bl");
    var bottom = zoneCache.fetch(bordername + "/bottom");
    var bottomRight = zoneCache.fetch(bordername + "/br");
    var right = zoneCache.fetch(bordername + "/right");

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

  private void paintClipped(FrameBuffer buffer, Sprite image, Area bounds, Area clip) {
    batch.flush();
    buffer.end();

    spareBuffer.begin();
    ScreenUtils.clear(Color.CLEAR);

    setProjectionMatrix(cam.combined);

    image.draw(batch);

    areaRenderer.setColor(Color.CLEAR);
    tmpArea.reset();
    tmpArea.add(bounds);
    tmpArea.subtract(clip);
    areaRenderer.fillArea(batch, tmpArea);

    batch.flush();
    spareBuffer.end();

    buffer.begin();
    BlendFunction.PREMULTIPLIED_ALPHA_SRC_OVER.applyToBatch(batch);

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
        spareBuffer.getColorBufferTexture(),
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
    Grid grid = zoneCache.getZone().getGrid();

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
      Dimension cellOffset = zoneCache.getZone().getGrid().getCellOffset();
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
              (float) p.getDistanceTraveled(zoneCache.getZone()),
              (float) p.getDistanceTraveledWithoutTerrain());
        }
      }
      int w = 0;
      for (ZonePoint p : waypointList) {
        ZonePoint zp = new ZonePoint(p.x + cellOffset.width, p.y + cellOffset.height);
        highlightCell(zp, zoneCache.fetch("redDot"), .333f);
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
        drawer.path(tmpFloat.toArray(), drawer.getDefaultLineWidth(), JoinType.NONE, true);
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
                  zoneCache.getZoneRenderer(),
                  zp.x + (footprintBounds.width / 2) * footprint.getScale(),
                  zp.y + (footprintBounds.height / 2) * footprint.getScale());
          continue;
        }
        ScreenPoint nextPoint =
            ScreenPoint.fromZonePoint(
                zoneCache.getZoneRenderer(),
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
        highlightCell(p, zoneCache.fetch("redDot"), .333f);
      }
      timer.stop("renderPath-3");
    }
  }

  private TextureRegion getCellHighlight() {
    if (zoneCache.getZone().getGrid() instanceof SquareGrid) return zoneCache.fetch("whiteBorder");
    if (zoneCache.getZone().getGrid() instanceof HexGrid) return zoneCache.fetch("hexBorder");
    if (zoneCache.getZone().getGrid() instanceof IsometricGrid) return zoneCache.fetch("isoBorder");

    return null;
  }

  private void addDistanceText(
      ZonePoint point, float size, float distance, float distanceWithoutTerrain) {
    if (distance == 0) return;

    Grid grid = zoneCache.getZone().getGrid();
    float cwidth = (float) grid.getCellWidth();
    float cheight = (float) grid.getCellHeight();

    float iwidth = cwidth * size;
    float iheight = cheight * size;

    var cellX = (point.x - iwidth / 2);
    var cellY = (-point.y + iheight / 2) + boldFont.getLineHeight();

    // Draw distance for each cell
    var textOffset = 7 * boldFontScale; // 7 pixels at 100% zoom & grid size of 50

    String distanceText = NumberFormat.getInstance().format(distance);
    if (log.isDebugEnabled() || showAstarDebugging) {
      distanceText += " (" + NumberFormat.getInstance().format(distanceWithoutTerrain) + ")";
    }

    glyphLayout.setText(boldFont, distanceText);

    var textWidth = glyphLayout.width;

    boldFont.setColor(Color.BLACK);

    boldFont.draw(
        batch,
        distanceText,
        cellX + cwidth - textWidth - textOffset,
        cellY - cheight /*- textOffset*/);
  }

  private void highlightCell(ZonePoint zp, TextureRegion image, float size) {
    Grid grid = zoneCache.getZone().getGrid();
    float cwidth = (float) grid.getCellWidth() * size;
    float cheight = (float) grid.getCellHeight() * size;

    float rotation = 0;
    if (zoneCache.getZone().getGrid() instanceof HexGridHorizontal) rotation = 90;

    batch.draw(
        image, zp.x - cwidth / 2, -zp.y - cheight / 2, 0, 0, cwidth, cheight, 1f, 1f, rotation);
  }

  @Subscribe
  void onZoneActivated(ZoneActivated event) {
    Gdx.app.postRunnable(
        () -> {
          renderZone = false;

          var newZone = event.zone();
          zoneCache = new ZoneCache(newZone, atlas);
          viewModel = zoneCache.getZoneRenderer().getViewModel();
          drawnElementRenderer.setZoneCache(zoneCache);
          tokenOverlayRenderer.setZoneCache(zoneCache);
          gridRenderer.setZoneCache(zoneCache);
          renderZone = true;
        });
  }

  public void setScale(Scale scale) {
    if (!initialized) {
      return;
    }

    offsetX = (int) (scale.getOffsetX() * -1);
    offsetY = (int) (scale.getOffsetY());
    zoom = (float) (1f / scale.getScale());
    updateCam();
  }

  public void flushFog() {
    visibleScreenArea = null;
  }

  /**
   * Premultiplies the texture for a font upon loading.
   *
   * <p>This method assumes the font is backed by a single texture.
   *
   * <p>It would be nicer if LibGDX supported premultiplied fonts, but the feature never get added
   * (see <a href="https://github.com/libgdx/libgdx/issues/3642">this issue</a>).
   *
   * @param assetManager
   * @param fileName
   * @param type
   */
  private static void premultiplyFontOnLoad(
      com.badlogic.gdx.assets.AssetManager assetManager, String fileName, Class<BitmapFont> type) {
    var font = assetManager.get(fileName, type);
    var texture = font.getRegion().getTexture();
    try {
      Pixmap pixmap = texture.getTextureData().consumePixmap();
      ByteBuffer pixels = pixmap.getPixels();
      Color color = new Color();
      while (pixels.hasRemaining()) {
        var position = pixels.position();
        color.set(pixels.getInt());
        color.premultiplyAlpha();
        pixels.putInt(position, color.toIntBits());
      }
      pixels.rewind();
      pixmap.setPixels(pixels);
      font.getRegion().setTexture(new Texture(pixmap));
    } catch (Throwable t) {
      log.error("Unexpected error while loading font", t);
    } finally {
      texture.dispose();
    }
  }
}
