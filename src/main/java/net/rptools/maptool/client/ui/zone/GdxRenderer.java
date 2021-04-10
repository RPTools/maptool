package net.rptools.maptool.client.ui.zone;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.BloomEffect;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.ImageLabel;
import net.rptools.maptool.box2d.NativeRenderer;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.List;
import java.util.*;

/**
 * Done:
 * - Board
 * - Grids
 * <p>
 * Bugs:
 * - y offset of VerticalHexgrid is wrong
 * - ismetric mode for token does't work
 */


public class GdxRenderer extends ApplicationAdapter implements AppEventListener, ModelChangeListener {

    private static final Logger log = LogManager.getLogger(GdxRenderer.class);

    private static GdxRenderer _instance;
    private final Map<MD5Key, Sprite> sprites = new HashMap<>();
    //renderFog
    Texture fogBufferTexture;
    private boolean flushFog = true;
    //from renderToken:
    private Area visibleScreenArea;
    private Area exposedFogArea;
    private PlayerView lastView;

    // zone specific resources
    private Zone zone;
    private ZoneRenderer zoneRenderer;
    private Sprite background;
    private Sprite fog;
    private MD5Key mapAssetId;
    private int offsetX = 0;
    private int offsetY = 0;
    private float zoom = 1.0f;

    // general resources
    private OrthographicCamera cam;
    private OrthographicCamera hudCam;
    private SpriteBatch batch;
    private SpriteBatch hudBatch;
    private NativeRenderer jfxRenderer;
    private boolean initialized = false;
    private int width;
    private int height;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private CodeTimer timer;
    private VfxManager vfxManager;
    private ChainVfxEffect vfxEffect;
    private ShapeRenderer shape;
    private VfxFrameBuffer fogBuffer;
    private Integer fogX;
    private Integer fogY;
    private EarClippingTriangulator triangulator;
    private Texture greyLabelTexture;
    private NinePatch greyLabel;

    //temorary objects. Stored here to avoid garbage collection;
    private Vector3 tmpWorldCoord;
    private Vector3 tmpScreenCoord;
    private Color tmpColor;
    private float[] floatsFromArea;
    private FloatArray tmpFloat;
    private Vector2 tmpVectorOut;
    private Vector2 tmpVector;
    private Vector2 tmpVector0;
    private Vector2 tmpVector1;
    private Vector2 tmpVector2;
    private Vector2 tmpVector3;


    public GdxRenderer() {
        MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
    }

    public static GdxRenderer getInstance() {
        if (_instance == null)
            _instance = new GdxRenderer();
        return _instance;
    }

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        cam = new OrthographicCamera();
        cam.setToOrtho(false);

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false);

        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
        fogBuffer = new VfxFrameBuffer(Pixmap.Format.RGBA8888);
        fogBuffer.initialize(width, height);
        triangulator = new EarClippingTriangulator();

        var pix = new Pixmap(Gdx.files.internal("net/rptools/maptool/client/image/grayLabelbox.png"));
        greyLabelTexture = new Texture(pix);
        pix.dispose();
        var topBottom = greyLabelTexture.getHeight()/2 - 1;
        greyLabel = new NinePatch(greyLabelTexture, 10, 10, 0, 0);

        vfxManager = new VfxManager(Pixmap.Format.RGBA8888);
        vfxEffect = new BloomEffect();
        //vfxManager.addEffect(vfxEffect);

        tmpWorldCoord = new Vector3();
        tmpScreenCoord = new Vector3();
        tmpColor = new Color();
        tmpFloat = new FloatArray();
        floatsFromArea = new float[6];
        tmpVector = new Vector2();
        tmpVector0 = new Vector2();
        tmpVector1 = new Vector2();
        tmpVector2 = new Vector2();
        tmpVector3 = new Vector2();
        tmpVectorOut = new Vector2();

        fogBuffer.addRenderer(new VfxFrameBuffer.BatchRendererAdapter(batch));
        fogBuffer.addRenderer(new VfxFrameBuffer.ShapeRendererAdapter(shape));

        initialized = true;
        initializeZoneResources(zone);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        font.dispose();
        vfxManager.dispose();
        vfxEffect.dispose();
        shape.dispose();
        greyLabelTexture.dispose();
        disposeZoneResources();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        vfxManager.resize(width, height);
        fogBuffer.initialize(width, height);
        // this texture is bound to the buffer
        fogBufferTexture = null;
        updateCam();

    }

    private void updateCam() {
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
        vfxManager.cleanUpBuffers();
        vfxManager.beginInputCapture();
        doRendering();
        vfxManager.endInputCapture();
        vfxManager.applyEffects();
        vfxManager.renderToScreen();
        copyFramebufferToJfx();
    }

    private void doRendering() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        if (zone == null)
            return;

        initializeTimer();
        if (zoneRenderer == null)
            return;

        setScale(zoneRenderer.getZoneScale());

        timer.start("paintComponent:createView");
        PlayerView playerView = zoneRenderer.getPlayerView();
        timer.stop("paintComponent:createView");

        batch.setProjectionMatrix(cam.combined);
        hudBatch.setProjectionMatrix(hudCam.combined);
        shape.setProjectionMatrix(cam.combined);

        batch.begin();


        renderZone(playerView);
        batch.end();

        glyphLayout.setText(font, "test");



        hudBatch.begin();
        //hudBatch.draw(greyLabelTexture, 20,20);
        greyLabel.draw(hudBatch, 20, 20 , 100, 100);
        font.draw(hudBatch, "test", 20, 20);

        if (zoneRenderer.isLoading())
            drawBoxedString(hudBatch, zoneRenderer.getLoadingProgress(), width / 2, height / 2);
        else if (MapTool.getCampaign().isBeingSerialized())
            drawBoxedString(hudBatch, "    Please Wait    ", width / 2, height / 2);

        int noteVPos = 20;
        if (!zone.isVisible() && playerView.isGMView()) {
            drawBoxedString(hudBatch,
                    "Map not visible to players", width / 2, height - noteVPos);
            noteVPos += 20;
        }
        if (AppState.isShowAsPlayer()) {
            drawBoxedString(hudBatch, "Player View", width / 2, height - noteVPos);
        }

        drawBoxedString(hudBatch, String.valueOf(Gdx.graphics.getFramesPerSecond()), 10, 10);

        hudBatch.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setProjectionMatrix(hudCam.combined);
        shape.setColor(Color.BLUE);
        shape.rect(20,20, glyphLayout.width, glyphLayout.height);
        shape.end();

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
        if (timer == null) {
            timer = new CodeTimer("ZoneRenderer.renderZone");
        }
        timer.setEnabled(AppState.isCollectProfilingData() || log.isDebugEnabled());
        timer.clear();
        timer.setThreshold(10);
    }

    private void copyFramebufferToJfx() {
        if (jfxRenderer != null) {
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
            jfxRenderer.setGdxBuffer(pixmap.getPixels());

            pixmap.dispose();
        }
    }

    public void invalidateCurrentViewCache() {
        flushFog = true;
        //   renderedLightMap = null;
        //    renderedAuraMap = null;
        visibleScreenArea = null;
        lastView = null;

        var zoneView = zoneRenderer.getZoneView();
        if (zoneView != null) {
            zoneView.flush();
        }
    }

    private void renderZone(PlayerView view) {
        if (zoneRenderer.isLoading() || MapTool.getCampaign().isBeingSerialized())
            return;

        if (lastView != null && !lastView.equals(view)) {
            invalidateCurrentViewCache();
        }
        lastView = view;

        // Calculations
        timer.start("calcs-1");
        if (visibleScreenArea == null && zoneRenderer.getZoneView().isUsingVision()) {
            timer.start("ZoneRenderer-getVisibleArea");
            visibleScreenArea = zoneRenderer.getZoneView().getVisibleArea(view);
            timer.stop("ZoneRenderer-getVisibleArea");
        }

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
        batch.end();
        timer.start("grid");
        renderGrid(view);
        timer.stop("grid");
        batch.begin();

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
            timer.start("token name/labels");
            renderRenderables();
            timer.stop("token name/labels");
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
    }

    private void renderGMVisionOverlay(PlayerView view) {
    }

    private void renderRenderables() {
    }

    private void renderFog(PlayerView view) {


        Area combined = null;

        // Optimization for panning
        if (!flushFog
                && fogX != null
                && fogY != null
                && (fogX != zoneRenderer.getViewOffsetX() || fogY != zoneRenderer.getViewOffsetY())) {
            flushFog = true;
        }
        boolean cacheNotValid =
                (fogBufferTexture == null
                        || fogBufferTexture.getWidth() != width
                        || fogBufferTexture.getHeight() != height);
        timer.start("renderFog");
      //  if (flushFog || cacheNotValid)
        {


            fogBuffer.begin();
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.setProjectionMatrix(cam.combined);

            timer.start("renderFog-allocateBufferedImage");
            timer.stop("renderFog-allocateBufferedImage");
            fogX = zoneRenderer.getViewOffsetX();
            fogY = zoneRenderer.getViewOffsetY();

            timer.start("renderFog-fill");


            // Fill
            if (fog == null)
                fog = paintToSprite(zone.getFogPaint());

            var color = fog.getColor();
            fog.setColor(color.r, color.g, color.b, view.isGMView() ? .6f : 1f);
            fillViewportWith(fog);

            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setProjectionMatrix(cam.combined);

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

            shape.setColor(Color.CLEAR);

            if (view.getTokens() != null) {
                // if there are tokens selected combine the areas, then, if individual FOW is enabled
                // we pass the combined exposed area to build the soft FOW and visible area.
                for (Token tok : view.getTokens()) {
                    ExposedAreaMetaData meta = zone.getExposedAreaMetaData(tok.getExposedAreaGUID());
                    exposedArea = meta.getExposedAreaHistory();
                    tempArea.add(new Area(exposedArea));
                }
                if (combinedView) {
                    paintArea(combined);
                    renderFogArea(combined, visibleArea);
                    renderFogOutline();
                } else {
                    // 'combined' already includes the area encompassed by 'tempArea', so just
                    // use 'combined' instead in this block of code?
                    tempArea.add(combined);

                    paintArea(tempArea);
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
                    paintArea(combined);
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
                    paintArea(myCombined);
                    renderFogArea(myCombined, visibleArea);
                    renderFogOutline();
                }
            }
            timer.stop("renderFogArea");

            shape.end();
            batch.begin();
            flushFog = false;

            fogBuffer.end();

            if (fogBufferTexture == null)
                fogBufferTexture = fogBuffer.getTexture();
        }

        batch.setProjectionMatrix(hudCam.combined);
        batch.draw(fogBufferTexture, 0, 0, width, height, 0, 0,
                fogBufferTexture.getWidth(), fogBufferTexture.getHeight(), false, true);

        batch.setProjectionMatrix(cam.combined);
        timer.stop("renderFog");
    }

    private void renderFogArea(Area softFog, Area visibleArea) {
        if (zoneRenderer.getZoneView().isUsingVision()) {
            if (visibleArea != null && !visibleArea.isEmpty()) {
                //shape.setColor(Color.BLUE);
                shape.setColor(0, 0, 0, AppPreferences.getFogOverlayOpacity() / 255.0f);

                // Fill in the exposed area
                paintArea(softFog);

                shape.setColor(Color.CLEAR);

                paintArea(visibleArea);
            } else {
                //shape.setColor(Color.BLUE);
                shape.setColor(0, 0, 0, 80 / 255.0f);
                paintArea(softFog);
            }
        } else {
            paintArea(softFog);
        }
    }

    private void renderFogOutline() {
        if (visibleScreenArea == null)
            return;

        var currentType = shape.getCurrentType();
        if (shape.getCurrentType() != ShapeRenderer.ShapeType.Line) {
            shape.end();
            shape.begin(ShapeRenderer.ShapeType.Line);
        }

        Gdx.gl.glLineWidth(1);
        shape.setColor(Color.BLACK);
        paintArea(visibleScreenArea);

        if (shape.getCurrentType() != currentType) {
            shape.end();
            shape.begin(currentType);
        }
    }

    private void renderLabels(PlayerView view) {
    }

    private void showBlockedMoves(PlayerView view, Set<ZoneRenderer.SelectionSet> unOwnedMovementSet) {
    }

    private void renderAuras(PlayerView view) {
    }

    private void renderLights(PlayerView view) {
    }

    private void renderGrid(PlayerView view) {
        var grid = zone.getGrid();
        var scale = (float) zoneRenderer.getScale();
        int gridSize = (int) (grid.getSize() * scale);

        if (!AppState.isShowGrid() || gridSize < ZoneRenderer.MIN_GRID_SIZE) {
            return;
        }

        if (grid instanceof GridlessGrid) {
            // do nothing
        } else if (grid instanceof HexGrid) {
            renderGrid((HexGrid) grid);
        } else if (grid instanceof SquareGrid) {
            renderGrid((SquareGrid) grid);
        } else if (grid instanceof IsometricGrid) {
            renderGrid((IsometricGrid) grid);
        }
    }

    private void renderGrid(HexGrid grid) {
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setProjectionMatrix(hudCam.combined);
        shape.identity();

        Color.argb8888ToColor(tmpColor, zone.getGridColor());

        shape.setColor(tmpColor);
        var path = grid.createShape(zoneRenderer.getScale());
        var vertices = pathToVertices(path);

        int offU = grid.getOffU(zoneRenderer);
        int offV = grid.getOffV(zoneRenderer);
        int count = 0;

        Gdx.gl.glLineWidth(AppState.getGridSize());

        for (double v = offV % (grid.getScaledMinorRadius() * 2) - (grid.getScaledMinorRadius() * 2);
             v < grid.getRendererSizeV(zoneRenderer);
             v += grid.getScaledMinorRadius()) {
            double offsetU = (int) ((count & 1) == 0 ? 0 : -(grid.getScaledEdgeProjection() + grid.getScaledEdgeLength()));
            count++;

            double start =
                    offU % (2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection())
                            - (2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection());
            double end = grid.getRendererSizeU(zoneRenderer) + 2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection();
            double incr = 2 * grid.getScaledEdgeLength() + 2 * grid.getScaledEdgeProjection();
            for (double u = start; u < end; u += incr) {
                float transX = 0;
                float transY = 0;
                if (grid instanceof HexGridVertical) {
                    transX = (float) (u + offsetU);
                    transY = height - (float) v;
                } else {
                    transX = (float) v;
                    transY = height - (float) (u + offsetU);
                }

                shape.translate(transX, transY, 0);
                shape.polyline(vertices.items, 0, vertices.size);
                shape.translate(-transX, -transY, 0);
            }
        }

        shape.end();
    }

    private void renderGrid(IsometricGrid grid) {
        var scale = (float) zoneRenderer.getScale();
        int gridSize = (int) (grid.getSize() * scale);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setProjectionMatrix(hudCam.combined);
        shape.identity();

        Color.argb8888ToColor(tmpColor, zone.getGridColor());

        shape.setColor(tmpColor);

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

        for (double row = startRow - (isoHeight / 2);
             row < y + h + gridSize;
             row += gridSize) {
            for (double col = startCol - (isoWidth / 2);
                 col < x + w + isoWidth;
                 col += isoWidth) {
                drawHatch(grid, (int) (col + offX), h - (int) (row + offY));
            }
        }
        shape.end();
    }

    private void drawHatch(IsometricGrid grid, float x, float y) {
        double isoWidth = grid.getSize() * zoneRenderer.getScale();
        int hatchSize = isoWidth > 10 ? (int) isoWidth / 8 : 2;

        var lineWidth = AppState.getGridSize();

        shape.rectLine(x - (hatchSize * 2), y - hatchSize, x + (hatchSize * 2), y + hatchSize, lineWidth);
        shape.rectLine(x - (hatchSize * 2), y + hatchSize, x + (hatchSize * 2), y - hatchSize, lineWidth);
    }

    private void renderGrid(SquareGrid grid) {
        var scale = (float) zoneRenderer.getScale();
        int gridSize = (int) (grid.getSize() * scale);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setProjectionMatrix(hudCam.combined);
        shape.identity();

        Color.argb8888ToColor(tmpColor, zone.getGridColor());

        shape.setColor(tmpColor);

        var x = hudCam.position.x - hudCam.viewportWidth / 2;
        var y = hudCam.position.y - hudCam.viewportHeight / 2;
        var w = hudCam.viewportWidth;
        var h = hudCam.viewportHeight;

        var offX = (int) (zoneRenderer.getViewOffsetX() % gridSize + grid.getOffsetX() * scale) + 1;
        var offY = (int) (zoneRenderer.getViewOffsetY() % gridSize + grid.getOffsetY() * scale) + 1;

        var startCol = ((int) (x / gridSize) * gridSize);
        var startRow = ((int) (y / gridSize) * gridSize);

        var lineWidth = AppState.getGridSize();

        for (float row = startRow; row < y + h + gridSize; row += gridSize)
            shape.rectLine(x, (int) (h - (row + offY)), x + w, (int) (h - (row + offY)), lineWidth);

        for (float col = startCol; col < x + w + gridSize; col += gridSize)
            shape.rectLine((int) (col + offX), y, (int) (col + offX), y + h, lineWidth);

        shape.end();
    }

    private FloatArray pathToVertices(GeneralPath path) {
        PathIterator iterator = path.getPathIterator(null);

        tmpFloat.clear();

        while (!iterator.isDone()) {
            int type = iterator.currentSegment(floatsFromArea);

            if (type != PathIterator.SEG_CLOSE) {
                tmpFloat.add(floatsFromArea[0], floatsFromArea[1]);
            }
            iterator.next();
        }

        return tmpFloat;
    }

    private void renderDrawableOverlay(PlayerView view, List<DrawnElement> drawables) {
    }

    public void drawBoxedString(SpriteBatch batch, String text, float centerX, float centerY) {
        drawBoxedString(batch, text, centerX, centerY, SwingUtilities.CENTER);
    }

    public void drawBoxedString(SpriteBatch batch, String text, float x, float y, int justification) {
        drawBoxedString(batch, text, x, y, justification, greyLabel, Color.BLACK);
    }

    private void drawBoxedString(SpriteBatch batch, String text, float x, float y, int justification, NinePatch background, Color foreground)  {
        final int BOX_PADDINGX = 10;
        final int BOX_PADDINGY = 2;

        if (text == null) text = "";

        glyphLayout.setText(font, text);
        var strWidth = glyphLayout.width;

        var width = strWidth + BOX_PADDINGX * 2;
        var height = glyphLayout.height + BOX_PADDINGY * 2;

        var boxY  = y - glyphLayout.height / 2 - BOX_PADDINGY;
        var textY = y + glyphLayout.height /2 + BOX_PADDINGY;

        y = y - glyphLayout.height / 2 - BOX_PADDINGY;
        switch (justification) {
            case SwingUtilities.CENTER:
                x = x - strWidth / 2 - BOX_PADDINGX;
                break;
            case SwingUtilities.RIGHT:
                x = x - strWidth - BOX_PADDINGX;
                break;
            case SwingUtilities.LEFT:
                break;
        }

        // Box
        background.draw(batch, x, y, width, height);

        // Renderer message
        //var oldColor = batch.getColor();
        //batch.setColor(foreground);
        float textX = x + BOX_PADDINGX;

        font.draw(batch, text, textX, textY);
        //batch.setColor(oldColor);
    }

    private void renderBoard() {
        if (!zone.drawBoard())
            return;

        if (background == null)
            background = paintToSprite(zone.getBackgroundPaint());

        fillViewportWith(background);

        if (mapAssetId != null) {
            var map = sprites.get(mapAssetId);
            if (map != null) {
                map.setPosition(zone.getBoardX(), zone.getBoardY() - map.getHeight());
                map.draw(batch);
            }
        }
    }

    private void fillViewportWith(Sprite fill) {
        var startX = (cam.position.x - cam.viewportWidth * zoom / 2);
        startX = (int) (startX / fill.getWidth()) * fill.getWidth() - fill.getWidth();
        var endX = cam.position.x + cam.viewportWidth / 2 * zoom;
        var startY = (cam.position.y - cam.viewportHeight * zoom / 2);
        startY = (int) (startY / fill.getHeight()) * fill.getHeight() - fill.getHeight();
        var endY = (cam.position.y + cam.viewportHeight / 2 * zoom);

        for (var i = startX; i < endX; i += fill.getWidth())
            for (var j = startY; j < endY; j += fill.getHeight()) {
                fill.setPosition(i, j);
                fill.draw(batch);
            }
    }

    private void renderTokens(List<Token> tokenList, PlayerView view, boolean figuresOnly) {
        boolean isGMView = view.isGMView(); // speed things up

        Set<GUID> tempVisTokens = new HashSet<GUID>();

        List<Token> tokenPostProcessing = new ArrayList<Token>(tokenList.size());
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

            timer.start("tokenlist-1b");

            // get token image sprite, using image table if present
            var imageKey = token.getTokenImageAssetId();
            Sprite image = sprites.get(imageKey);
            if (image == null)
                continue;

            image.setSize(image.getTexture().getWidth(), image.getTexture().getHeight());

            timer.stop("tokenlist-1b");

            timer.start("tokenlist-1a");
            Rectangle footprintBounds = token.getBounds(zone);
            image.setPosition(footprintBounds.x, -footprintBounds.y - footprintBounds.height);

            timer.stop("tokenlist-1a");

            timer.start("tokenlist-1e");
            try {
                var awtRect = toAwtRect(image.getBoundingRectangle());

                // Vision visibility
                if (!isGMView && token.isToken() && zoneRenderer.getZoneView().isUsingVision()) {
                    if (!GraphicsUtil.intersects(visibleScreenArea, new Area(awtRect))) {
                        continue;
                    }
                }
            } finally {
                // This ensures that the timer is always stopped
                timer.stop("tokenlist-1e");
            }

            // Add the token to our visible set.
            tempVisTokens.add(token.getId());

            // Previous path
            timer.start("renderTokens:ShowPath");
            if (zoneRenderer.getShowPathList().contains(token) && token.getLastPath() != null) {
                renderPath(token.getLastPath(), token.getFootprint(zone.getGrid()));
            }
            timer.stop("renderTokens:ShowPath");

            timer.start("tokenlist-5");

            // handle flipping
            image.setFlip(token.isFlippedX(), token.isFlippedY());
            timer.stop("tokenlist-5");

            image.setOriginCenter();

            timer.start("tokenlist-5a");
            if (token.isFlippedIso()) {
                image.setRotation(45);
                image.setSize(image.getWidth(), image.getHeight() / 2);
                token.setHeight((int) image.getHeight());
                token.setWidth((int) image.getWidth());
                footprintBounds = token.getBounds(zone);
            }
            timer.stop("tokenlist-5a");

            timer.start("tokenlist-6");
            // Position
            // For Isometric Grid we alter the height offset
            float iso_ho = 0;

            if (token.getShape() == Token.TokenShape.FIGURE) {
                float th = token.getHeight() * (float) footprintBounds.width / token.getWidth();
                iso_ho = footprintBounds.height - th;
                footprintBounds =
                        new Rectangle(
                                footprintBounds.x,
                                footprintBounds.y - (int) iso_ho,
                                footprintBounds.width,
                                (int) th);
                iso_ho = iso_ho * (float) zoneRenderer.getScale();
            }

            var factorX = footprintBounds.width / image.getWidth();
            var factorY = footprintBounds.height / image.getHeight();

            var factor = Math.min(factorX, factorY);

            image.setSize(image.getWidth() * factor, image.getHeight() * factor);
            image.setOriginCenter();

            // Rotated
            if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
                image.setRotation(token.getFacing() + 90);
            }

            if (token.isSnapToScale()) {
                var offsetx = (image.getWidth() < footprintBounds.width
                        ? (footprintBounds.width - image.getWidth()) / 2 : 0);
                var offsety = (image.getHeight() < footprintBounds.height
                        ? (footprintBounds.height - image.getHeight()) / 2 : 0);

                image.setPosition(image.getX() + offsetx, image.getY() + offsety + iso_ho);
            }


            timer.stop("tokenlist-6");
/*
            // Render Halo
            if (token.hasHalo()) {
                tokenG.setStroke(new BasicStroke(AppPreferences.getHaloLineWidth()));
                tokenG.setColor(token.getHaloColor());
                tokenG.draw(zone.getGrid().getTokenCellArea(tokenBounds));
            }
            */

            // Calculate alpha Transparency from token and use opacity for indicating that token is moving
            float opacity = token.getTokenOpacity();
            if (zoneRenderer.isTokenMoving(token)) opacity = opacity / 2.0f;

            // Finally render the token image
            timer.start("tokenlist-7");
            if (!isGMView && zoneRenderer.getZoneView().isUsingVision() && (token.getShape() == Token.TokenShape.FIGURE)) {
                Area cb = zone.getGrid().getTokenCellArea(toAwtRect(image.getBoundingRectangle()));
                if (GraphicsUtil.intersects(visibleScreenArea, cb)) {
                    // the cell intersects visible area so
                    var oldAlpha = image.getColor().a;
                    image.setAlpha(opacity);
                    image.draw(batch);
                    image.setAlpha(oldAlpha);
                }
            } else if (!isGMView && zoneRenderer.getZoneView().isUsingVision() && token.isAlwaysVisible()) {
                // Jamz: Always Visible tokens will get rendered again here to place on top of FoW
                Area cb = zone.getGrid().getTokenCellArea(toAwtRect(image.getBoundingRectangle()));
                if (GraphicsUtil.intersects(visibleScreenArea, cb)) {
                    // if we can see a portion of the stamp/token, draw the whole thing, defaults to 2/9ths
                    // the cell intersects visible area so
                    var oldAlpha = image.getColor().a;
                    image.setAlpha(opacity);
                    image.draw(batch);
                    image.setAlpha(oldAlpha);
                }
            } else {
                // fallthrough normal token rendered against visible area
                image.draw(batch);
            }
            timer.stop("tokenlist-7");
/*
            timer.start("tokenlist-8");

            // Facing ?
            // TODO: Optimize this by doing it once per token per facing
            if (token.hasFacing()) {
                Token.TokenShape tokenType = token.getShape();
                switch (tokenType) {
                    case FIGURE:
                        if (token.getHasImageTable()
                                && token.hasFacing()
                                && AppPreferences.getForceFacingArrow() == false) {
                            break;
                        }
                        Shape arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);

                        if (!zone.getGrid().isIsometric()) {
                            arrow = getCircleFacingArrow(token.getFacing(), footprintBounds.width / 2);
                        }

                        double fx = location.x + location.scaledWidth / 2;
                        double fy = location.y + location.scaledHeight / 2;

                        tokenG.translate(fx, fy);
                        if (token.getFacing() < 0) {
                            tokenG.setColor(java.awt.Color.yellow);
                        } else {
                            tokenG.setColor(TRANSLUCENT_YELLOW);
                        }
                        tokenG.fill(arrow);
                        tokenG.setColor(java.awt.Color.darkGray);
                        tokenG.draw(arrow);
                        tokenG.translate(-fx, -fy);
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

                        double cx = location.x + location.scaledWidth / 2;
                        double cy = location.y + location.scaledHeight / 2;

                        tokenG.translate(cx, cy);
                        tokenG.setColor(java.awt.Color.yellow);
                        tokenG.fill(arrow);
                        tokenG.setColor(java.awt.Color.darkGray);
                        tokenG.draw(arrow);
                        tokenG.translate(-cx, -cy);
                        break;
                    case SQUARE:
                        if (zone.getGrid().isIsometric()) {
                            arrow = getFigureFacingArrow(token.getFacing(), footprintBounds.width / 2);
                            cx = location.x + location.scaledWidth / 2;
                            cy = location.y + location.scaledHeight / 2;
                        } else {
                            int facing = token.getFacing();
                            while (facing < 0) {
                                facing += 360;
                            } // TODO: this should really be done in Token.setFacing() but I didn't want to take
                            // the chance
                            // of breaking something, so change this when it's safe to break stuff
                            facing %= 360;
                            arrow = getSquareFacingArrow(facing, footprintBounds.width / 2);

                            cx = location.x + location.scaledWidth / 2;
                            cy = location.y + location.scaledHeight / 2;

                            // Find the edge of the image
                            // TODO: Man, this is horrible, there's gotta be a better way to do this
                            double xp = location.scaledWidth / 2;
                            double yp = location.scaledHeight / 2;
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

                        tokenG.translate(cx, cy);
                        tokenG.setColor(java.awt.Color.yellow);
                        tokenG.fill(arrow);
                        tokenG.setColor(java.awt.Color.darkGray);
                        tokenG.draw(arrow);
                        tokenG.translate(-cx, -cy);
                        break;
                }
            }
            timer.stop("tokenlist-8");

            timer.start("tokenlist-9");
            // Set up the graphics so that the overlay can just be painted.
            Graphics2D locg =
                    (Graphics2D)
                            tokenG.create(
                                    (int) tokenBounds.getBounds().getX(),
                                    (int) tokenBounds.getBounds().getY(),
                                    (int) tokenBounds.getBounds().getWidth(),
                                    (int) tokenBounds.getBounds().getHeight());
            Rectangle bounds =
                    new Rectangle(
                            0,
                            0,
                            (int) tokenBounds.getBounds().getWidth(),
                            (int) tokenBounds.getBounds().getHeight());

            // Check each of the set values
            for (String state : MapTool.getCampaign().getTokenStatesMap().keySet()) {
                Object stateValue = token.getState(state);
                AbstractTokenOverlay overlay = MapTool.getCampaign().getTokenStatesMap().get(state);
                if (stateValue instanceof AbstractTokenOverlay) {
                    overlay = (AbstractTokenOverlay) stateValue;
                }
                if (overlay == null
                        || overlay.isMouseover() && token != tokenUnderMouse
                        || !overlay.showPlayer(token, MapTool.getPlayer())) {
                    continue;
                }
                overlay.paintOverlay(locg, token, bounds, stateValue);
            }
            timer.stop("tokenlist-9");

            timer.start("tokenlist-10");

            for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
                Object barValue = token.getState(bar);
                BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
                if (overlay == null
                        || overlay.isMouseover() && token != tokenUnderMouse
                        || !overlay.showPlayer(token, MapTool.getPlayer())) {
                    continue;
                }

                overlay.paintOverlay(locg, token, bounds, barValue);
            } // endfor
            locg.dispose();
            timer.stop("tokenlist-10");

            timer.start("tokenlist-11");
            // Keep track of which tokens have been drawn so we can perform post-processing on them later
            // (such as selection borders and names/labels)
            if (zoneRenderer.getActiveLayer().equals(token.getLayer())) {
                tokenPostProcessing.add(token);
            }
            timer.stop("tokenlist-11");
*/
        }
 /*
        timer.start("tokenlist-12");
        boolean useIF = MapTool.getServerPolicy().isUseIndividualFOW();
        // Selection and labels
        for (Token token : tokenPostProcessing) {
            ZoneRenderer.TokenLocation location = tokenLocationCache.get(token);
            if (location == null) {
                continue;
            }
            Area bounds = location.bounds;

            // TODO: This isn't entirely accurate as it doesn't account for the actual text
            // to be in the clipping bounds, but I'll fix that later
            if (!bounds.getBounds().intersects(clipBounds)) {
                continue;
            }
            Rectangle footprintBounds = token.getBounds(zone);

            boolean isSelected = selectedTokenSet.contains(token.getId());
            if (isSelected) {
                ScreenPoint sp = ScreenPoint.fromZonePoint(this, footprintBounds.x, footprintBounds.y);
                double width = footprintBounds.width * getScale();
                double height = footprintBounds.height * getScale();

                ImageBorder selectedBorder =
                        token.isStamp() ? AppStyle.selectedStampBorder : AppStyle.selectedBorder;
                if (highlightCommonMacros.contains(token)) {
                    selectedBorder = AppStyle.commonMacroBorder;
                }
                if (!AppUtil.playerOwns(token)) {
                    selectedBorder = AppStyle.selectedUnownedBorder;
                }
                if (useIF && !token.isStamp() && zoneView.isUsingVision()) {
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
                if (token.hasFacing()
                        && (token.getShape() == Token.TokenShape.TOP_DOWN || token.isStamp())) {
                    AffineTransform oldTransform = clippedG.getTransform();

                    // Rotated
                    clippedG.translate(sp.x, sp.y);
                    clippedG.rotate(
                            Math.toRadians(-token.getFacing() - 90),
                            width / 2 - (token.getAnchor().x * scale),
                            height / 2 - (token.getAnchor().y * scale)); // facing defaults to down, or -90
                    // degrees
                    selectedBorder.paintAround(clippedG, 0, 0, (int) width, (int) height);

                    clippedG.setTransform(oldTransform);
                } else {
                    selectedBorder.paintAround(clippedG, (int) sp.x, (int) sp.y, (int) width, (int) height);
                }
                // Remove labels from the cache if the corresponding tokens are deselected
            } else if (!AppState.isShowTokenNames()) {
                labelRenderingCache.remove(token.getId());
            }

            // Token names and labels
            boolean showCurrentTokenLabel = AppState.isShowTokenNames() || token == tokenUnderMouse;

            // if policy does not auto-reveal FoW, check if fog covers the token (slow)
            if (showCurrentTokenLabel
                    && !isGMView
                    && (!zoneView.isUsingVision() || !MapTool.getServerPolicy().isAutoRevealOnMovement())
                    && !zone.isTokenVisible(token)) {
                showCurrentTokenLabel = false;
            }
            if (showCurrentTokenLabel) {
                GUID tokId = token.getId();
                int offset = 3; // Keep it from tramping on the token border.
                ImageLabel background;
                java.awt.Color foreground;

                if (token.isVisible()) {
                    if (token.getType() == Token.Type.NPC) {
                        background = GraphicsUtil.BLUE_LABEL;
                        foreground = java.awt.Color.WHITE;
                    } else {
                        background = GraphicsUtil.GREY_LABEL;
                        foreground = java.awt.Color.BLACK;
                    }
                } else {
                    background = GraphicsUtil.DARK_GREY_LABEL;
                    foreground = java.awt.Color.WHITE;
                }
                String name = token.getName();
                if (isGMView && token.getGMName() != null && !StringUtil.isEmpty(token.getGMName())) {
                    name += " (" + token.getGMName() + ")";
                }
                if (!view.equals(lastView) || !labelRenderingCache.containsKey(tokId)) {
                    // if ((lastView != null && !lastView.equals(view)) ||
                    // !labelRenderingCache.containsKey(tokId)) {
                    boolean hasLabel = false;

                    // Calculate image dimensions
                    FontMetrics fm = g.getFontMetrics();
                    Font f = g.getFont();
                    int strWidth = SwingUtilities.computeStringWidth(fm, name);

                    int width = strWidth + GraphicsUtil.BOX_PADDINGX * 2;
                    int height = fm.getHeight() + GraphicsUtil.BOX_PADDINGY * 2;
                    int labelHeight = height;

                    // If token has a label (in addition to name).
                    if (token.getLabel() != null && token.getLabel().trim().length() > 0) {
                        hasLabel = true;
                        height = height * 2; // Double the image height for two boxed strings.
                        int labelWidth =
                                SwingUtilities.computeStringWidth(fm, token.getLabel())
                                        + GraphicsUtil.BOX_PADDINGX * 2;
                        width = Math.max(width, labelWidth);
                    }

                    // Set up the image
                    BufferedImage labelRender = new BufferedImage(width, height, Transparency.TRANSLUCENT);
                    Graphics2D gLabelRender = labelRender.createGraphics();
                    gLabelRender.setFont(f); // Match font used in the main graphics context.
                    gLabelRender.setRenderingHints(g.getRenderingHints()); // Match rendering style.

                    // Draw name and label to image
                    if (hasLabel) {
                        GraphicsUtil.drawBoxedString(
                                gLabelRender,
                                token.getLabel(),
                                width / 2,
                                height - (labelHeight / 2),
                                SwingUtilities.CENTER,
                                background,
                                foreground);
                    }
                    GraphicsUtil.drawBoxedString(
                            gLabelRender,
                            name,
                            width / 2,
                            labelHeight / 2,
                            SwingUtilities.CENTER,
                            background,
                            foreground);

                    // Add image to cache
                    labelRenderingCache.put(tokId, labelRender);
                }
                // Create LabelRenderer using cached label.
                Rectangle r = bounds.getBounds();
                delayRendering(
                        new ZoneRenderer.LabelRenderer(
                                name,
                                r.x + r.width / 2,
                                r.y + r.height + offset,
                                SwingUtilities.CENTER,
                                background,
                                foreground,
                                tokId));
            }
        }
        timer.stop("tokenlist-12");

        timer.start("tokenlist-13");
        // Stacks
        if (!tokenList.isEmpty()
                && !tokenList.get(0).isStamp()) { // TODO: find a cleaner way to indicate token layer
            if (tokenStackMap != null) { // FIXME Needed to prevent NPE but how can it be null?
                for (Token token : tokenStackMap.keySet()) {
                    Area bounds = getTokenBounds(token);
                    if (bounds == null) {
                        // token is offscreen
                        continue;
                    }
                    BufferedImage stackImage = AppStyle.stackImage;
                    clippedG.drawImage(
                            stackImage,
                            bounds.getBounds().x + bounds.getBounds().width - stackImage.getWidth() + 2,
                            bounds.getBounds().y - 2,
                            null);
                }
            }
        }

        // Markers
        // for (TokenLocation location : getMarkerLocations()) {
        // BufferedImage stackImage = AppStyle.markerImage;
        // g.drawImage(stackImage, location.bounds.getBounds().x, location.bounds.getBounds().y, null);
        // }

        timer.stop("tokenlist-13");

        if (figuresOnly) {
            tempVisTokens.addAll(visibleTokenSet);
        }

        visibleTokenSet = Collections.unmodifiableSet(tempVisTokens);*/
    }

    private void renderPath(Path lastPath, TokenFootprint footprint) {
    }

    public void setJfxRenderer(NativeRenderer renderer) {
        jfxRenderer = renderer;
    }

    private void disposeZoneResources() {
        if (!initialized)
            return;

        cam.zoom = 1.0f;
        offsetX = 0;
        offsetY = 0;
        fogX = null;
        fogY = null;

        Gdx.app.postRunnable(() -> {
            updateCam();
            var background = this.background;
            this.background = null;
            if (background != null) {
                background.getTexture().dispose();
            }

            var fog = this.fog;
            this.fog = null;
            if (fog != null) {
                fog.getTexture().dispose();
            }

            for (var sprite : sprites.values()) {
                sprite.getTexture().dispose();
            }
            sprites.clear();

            fogBufferTexture = null;
        });
    }

    private void initializeZoneResources(Zone newZone) {
        if (newZone == null || !initialized)
            return;

        zoneRenderer = MapTool.getFrame().getZoneRenderer(newZone);
        mapAssetId = newZone.getMapAssetId();

        // FIXME: zonechanges during wait for resources
        new Thread(() -> {
            try {
                while (zoneRenderer.isLoading()) {
                    Thread.sleep(100);
                }

                // create sprites for all assets
                //TODO: create textureAtlas ?
                Gdx.app.postRunnable(() -> {
                    for (var assetId : newZone.getAllAssetIds()) {
                        var bytes = AssetManager.getAsset(assetId).getImage();

                        var pix = new Pixmap(bytes, 0, bytes.length);
                        var sprite = new Sprite(new Texture(pix));
                        sprite.setSize(pix.getWidth(), pix.getHeight());
                        sprites.put(assetId, sprite);
                        pix.dispose();
                    }
                });
            } catch (InterruptedException e) {
            }
        }).start();

        zone = newZone;
    }

    private Sprite paintToSprite(DrawablePaint paint) {
        if (paint instanceof DrawableTexturePaint) {
            var texturePaint = (DrawableTexturePaint) paint;
            var image = texturePaint.getAsset().getImage();
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

    private void paintArea(Area area) {
        if (area.isEmpty())
            return;

        var pointsPerBezier = 10.0f;

        tmpFloat.clear();

        for (var it = area.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(floatsFromArea);

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    System.out.println("Move to: ( " + floatsFromArea[0] + ", " + floatsFromArea[1] + ")");
                    tmpFloat.add(floatsFromArea[0], -floatsFromArea[1]);

                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.println("Close");
                    if (shape.getCurrentType() == ShapeRenderer.ShapeType.Filled) {
                        var indicies = triangulator.computeTriangles(tmpFloat.items, 0, tmpFloat.size);
                        for (int i = 0; i < indicies.size - 2; i = i + 3) {
                            float x1 = tmpFloat.get(indicies.get(i) * 2);
                            float y1 = tmpFloat.get((indicies.get(i) * 2) + 1);

                            float x2 = tmpFloat.get((indicies.get(i + 1)) * 2);
                            float y2 = tmpFloat.get((indicies.get(i + 1) * 2) + 1);

                            float x3 = tmpFloat.get(indicies.get(i + 2) * 2);
                            float y3 = tmpFloat.get((indicies.get(i + 2) * 2) + 1);

                            shape.triangle(x1, y1, x2, y2, x3, y3);
                        }
                    } else {
                        shape.polyline(tmpFloat.items, 0, tmpFloat.size);
                    }
                    tmpFloat.clear();
                    break;
                case PathIterator.SEG_LINETO:
                    System.out.println("Line to: ( " + floatsFromArea[0] + ", " + floatsFromArea[1] + ")");
                    tmpFloat.add(floatsFromArea[0], -floatsFromArea[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    System.out.println("quadratic bezier with: ( " + floatsFromArea[0] + ", " + floatsFromArea[1] +
                            "), (" + floatsFromArea[2] + ", " + floatsFromArea[3] + ")");

                    tmpVector0.set(tmpFloat.get(tmpFloat.size - 2), tmpFloat.get(tmpFloat.size - 1));
                    tmpVector1.set(floatsFromArea[0], -floatsFromArea[1]);
                    tmpVector2.set(floatsFromArea[2], -floatsFromArea[3]);
                    for (var i = 1; i <= pointsPerBezier; i++) {
                        Bezier.quadratic(tmpVectorOut, i / pointsPerBezier, tmpVector0, tmpVector1, tmpVector2, tmpVector);
                        tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
                    }
                    break;
                case PathIterator.SEG_CUBICTO:
                    System.out.println("cubic bezier with: ( " + floatsFromArea[0] + ", " + floatsFromArea[1] +
                            "), (" + floatsFromArea[2] + ", " + floatsFromArea[3] +
                            "), (" + floatsFromArea[4] + ", " + floatsFromArea[5] + ")");

                    tmpVector0.set(tmpFloat.get(tmpFloat.size - 2), tmpFloat.get(tmpFloat.size - 1));
                    tmpVector1.set(floatsFromArea[0], -floatsFromArea[1]);
                    tmpVector2.set(floatsFromArea[2], -floatsFromArea[3]);
                    tmpVector3.set(floatsFromArea[4], -floatsFromArea[5]);
                    for (var i = 1; i <= pointsPerBezier; i++) {
                        Bezier.cubic(tmpVectorOut, i / pointsPerBezier, tmpVector0, tmpVector1, tmpVector2, tmpVector3, tmpVector);
                        tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
                    }
                    break;
                default:
                    System.out.println("Type: " + type);
            }
        }
    }

    public java.awt.Rectangle toAwtRect(com.badlogic.gdx.math.Rectangle rectangle) {
        var awtRect = new java.awt.Rectangle();
        awtRect.x = (int) rectangle.x;
        awtRect.y = (int) rectangle.y;
        awtRect.width = (int) rectangle.width;
        awtRect.height = (int) rectangle.height;
        return awtRect;
    }

    @Override
    public void handleAppEvent(AppEvent event) {
        System.out.println("AppEvent:" + event.getId());

        if (event.getId() != MapTool.ZoneEvent.Activated)
            return;

        var oldZone = zone;
        // first disable rendering during intitialisation;
        zone = null;

        if (oldZone != null) {
            disposeZoneResources();
            oldZone.removeModelChangeListener(this);
        }

        var newZone = (Zone) event.getNewValue();
        newZone.addModelChangeListener(this);
        initializeZoneResources(newZone);
        // just in case we are running before create was called and hence initializeZoneResources does nothing
        zone = newZone;
    }

    @Override
    public void modelChanged(ModelChangeEvent event) {
        Object evt = event.getEvent();
        System.out.println("ModelChangend: " + evt);
        if (evt == Zone.Event.TOPOLOGY_CHANGED) {
            flushFog();
            //flushLight();
            return;
        }
        if (evt == Zone.Event.FOG_CHANGED) {
            flushFog = true;
            return;
        }
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

        var currentZone = zone;

        // for now quick and dirty
        disposeZoneResources();
        initializeZoneResources(currentZone);
    }

    public void setScale(Scale scale) {
        offsetX = scale.getOffsetX() * -1;
        offsetY = scale.getOffsetY();
        zoom = (float) (1f / scale.getScale());
        updateCam();
    }

    public void flushFog() {
        flushFog = true;
        visibleScreenArea = null;
    }

}
