package net.rptools.maptool.box2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawnElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;

public class GdxRenderer extends ApplicationAdapter implements AppEventListener, ModelChangeListener {

    private static final Logger log = LogManager.getLogger(GdxRenderer.class);

    private static GdxRenderer _instance;

    public static GdxRenderer getInstance() {
        if (_instance == null)
            _instance = new GdxRenderer();
        return _instance;
    }

    // zone specific resources
    private Zone zone;
    private ZoneRenderer zoneRenderer;
    private Sprite background;
    private MD5Key mapAssetId;
    private final Map<MD5Key, Sprite> sprites = new HashMap<>();
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

    public GdxRenderer() {
        MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
    }

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        cam = new OrthographicCamera();
        cam.setToOrtho(false);

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false);

        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
        initialized = true;
        initializeZoneResources();
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        font.dispose();
        disposeZoneResources();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
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
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (zone == null)
            return;

        initializeTimer();

        timer.start("paintComponent:createView");
        PlayerView playerView = zoneRenderer.getPlayerView();
        timer.stop("paintComponent:createView");

        batch.setProjectionMatrix(cam.combined);
        hudBatch.setProjectionMatrix(hudCam.combined);

        batch.begin();


        renderZone(playerView);
        batch.end();

        hudBatch.begin();
        if (zoneRenderer.isLoading())
            drawBoxedString(zoneRenderer.getLoadingProgress(), width / 2, height / 2);
        else if (MapTool.getCampaign().isBeingSerialized())
            drawBoxedString("    Please Wait    ", width / 2, height / 2);

        int noteVPos = 20;
        if (!zone.isVisible() && playerView.isGMView()) {
            drawBoxedString(
                    "Map not visible to players", width / 2, height - noteVPos);
            noteVPos += 20;
        }
        if (AppState.isShowAsPlayer()) {
            drawBoxedString("Player View", width / 2, height - noteVPos);
        }

        drawBoxedString(String.valueOf(Gdx.graphics.getFramesPerSecond()), 10, 10);

        hudBatch.end();

        collectTimerResults();

        copyFramebufferToJfx();
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

    private void renderZone(PlayerView view) {
        if (zoneRenderer.isLoading() || MapTool.getCampaign().isBeingSerialized())
            return;

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
    }

    private void renderDrawableOverlay(PlayerView view, List<DrawnElement> drawables) {
    }

    private void drawBoxedString(String text, int centerX, int centerY) {
        glyphLayout.setText(font, text);
        font.draw(hudBatch, text, centerX-glyphLayout.width/2, centerY+glyphLayout.height/2);
    }

    private void renderBoard() {
        if (!zone.drawBoard())
            return;

        if (background != null) {
            var startX = (cam.position.x - cam.viewportWidth * zoom / 2);
            startX = (int) (startX / background.getWidth()) * background.getWidth() - background.getWidth();
            var endX = cam.position.x + cam.viewportWidth / 2 * zoom;
            var startY = (cam.position.y - cam.viewportHeight * zoom / 2);
            startY = (int) (startY / background.getHeight()) * background.getHeight() - background.getHeight();
            var endY = (cam.position.y + cam.viewportHeight / 2 * zoom);

            for (var i = startX; i < endX; i += background.getWidth())
                for (var j = startY; j < endY; j += background.getHeight()) {
                    background.setPosition(i, j);
                    background.draw(batch);
                }
        }

        if (mapAssetId != null) {
            var map = sprites.get(mapAssetId);
            if (map != null) {
                map.setPosition(zone.getBoardX(), zone.getBoardY() - map.getHeight());
                map.draw(batch);
            }
        }
    }

    private void renderTokens(List<Token> background, PlayerView view, boolean figuresOnly) {
        for (var token : background) {
            var assetId = token.getImageAssetId();
            var sprite = sprites.get(assetId);
            if (sprite == null)
                continue;

            sprite.setPosition(token.getX(), -1 * token.getY() - token.getHeight());
            sprite.draw(batch);
        }
    }

    public void setJfxRenderer(NativeRenderer renderer) {
        jfxRenderer = renderer;
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "MapTool libgdx Test!";
        cfg.width = 800;
        cfg.height = 600;

        new LwjglApplication(new GdxRenderer(), cfg);
    }

    private void disposeZoneResources() {
        if (!initialized)
            return;

        cam.zoom = 1.0f;
        offsetX = 0;
        offsetY = 0;

        Gdx.app.postRunnable(() -> {
            updateCam();
            var background = this.background;
            this.background = null;
            if (background != null) {
                background.getTexture().dispose();
            }

            for (var sprite : sprites.values()) {
                sprite.getTexture().dispose();
            }
            sprites.clear();
        });
    }

    private void initializeZoneResources() {
        if (zone == null || !initialized)
            return;

        var backgroundPaint = zone.getBackgroundPaint();
        if (backgroundPaint instanceof DrawableTexturePaint) {
            var texturePaint = (DrawableTexturePaint) backgroundPaint;
            var image = texturePaint.getAsset().getImage();
            Gdx.app.postRunnable(() -> {
                var pix = new Pixmap(image, 0, image.length);
                background = new Sprite(new Texture(pix));
                background.setSize(pix.getWidth(), pix.getHeight());
                background.setPosition(0, -1 * background.getHeight());
                pix.dispose();
            });
        }
        if (backgroundPaint instanceof DrawableColorPaint) {
            var colorPaint = (DrawableColorPaint) backgroundPaint;
            var colorValue = colorPaint.getColor();
            var color = new Color();
            Color.argb8888ToColor(color, colorValue);
            Gdx.app.postRunnable(() -> {
                var pix = new Pixmap(64, 64, Pixmap.Format.RGBA8888);

                pix.setColor(color);
                pix.fill();
                background = new Sprite(new Texture(pix));
                background.setSize(pix.getWidth(), pix.getHeight());
                background.setPosition(0, -1 * background.getHeight());
                pix.dispose();
            });
        }
        mapAssetId = zone.getMapAssetId();

        // FIXME: zonechanges during wait for resources
        new Thread(() -> {
            try {
                while (zoneRenderer.isLoading()) {
                    Thread.sleep(100);
                }

                // create sprites for all assets
                //TODO: create textureAtlas ?
                Gdx.app.postRunnable(() -> {
                    for (var assetId : zone.getAllAssetIds()) {
                        var bytes = AssetManager.getAsset(assetId).getImage();

                        var pix = new Pixmap(bytes, 0, bytes.length);
                        var sprite = new Sprite(new Texture(pix));
                        sprite.setSize(pix.getWidth(), pix.getHeight());
                        sprites.put(assetId, sprite);
                        pix.dispose();
                    }
                });
            } catch (InterruptedException e) {}
        }).start();


    }


    @Override
    public void handleAppEvent(AppEvent event) {
        if (event.getId() != MapTool.ZoneEvent.Activated)
            return;

        if (zone != null)
            disposeZoneResources();

        this.zone = (Zone) event.getNewValue();
        this.zoneRenderer = MapTool.getFrame().getZoneRenderer(this.zone);
        initializeZoneResources();
    }

    @Override
    public void modelChanged(ModelChangeEvent event) {
        // for now quick and dirty
        disposeZoneResources();
        initializeZoneResources();
    }

    public void setScale(Scale scale) {
        offsetX = scale.getOffsetX() * -1;
        offsetY = scale.getOffsetY();
        zoom = (float) (1f / scale.getScale());
        updateCam();
    }
}
