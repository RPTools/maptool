package net.rptools.maptool.box2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.*;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.awt.*;
import java.awt.geom.Area;
import java.util.List;
import java.util.*;

public class GdxRenderer extends ApplicationAdapter implements AppEventListener, ModelChangeListener {

    private static final Logger log = LogManager.getLogger(GdxRenderer.class);

    private static GdxRenderer _instance;
    private final Map<MD5Key, Sprite> sprites = new HashMap<>();
    //from renderToken:
    private Area visibleScreenArea;
    private Area exposedFogArea;
    // zone specific resources
    private Zone zone;
    private ZoneRenderer zoneRenderer;
    private ZoneView zoneView;
    private Sprite background;
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

    public GdxRenderer() {
        MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
    }

    public static GdxRenderer getInstance() {
        if (_instance == null)
            _instance = new GdxRenderer();
        return _instance;
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "MapTool libgdx Test!";
        cfg.width = 800;
        cfg.height = 600;

        new LwjglApplication(new GdxRenderer(), cfg);
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
        vfxManager = new VfxManager(Pixmap.Format.RGBA8888);
        vfxEffect = new BloomEffect();
        vfxManager.addEffect(vfxEffect);

        initialized = true;
        initializeZoneResources();
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        font.dispose();
        vfxManager.dispose();
        vfxEffect.dispose();
        disposeZoneResources();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        updateCam();
        vfxManager.resize(width, height);
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

        // Begin render to an off-screen buffer.
        vfxManager.beginInputCapture();
        doRendering();
        vfxManager.endInputCapture();

        // Apply the effects chain to the captured frame.
        // In our case, only one effect (gaussian blur) will be applied.
        vfxManager.applyEffects();

        copyFramebufferToJfx();
        // Render result to the screen.
        vfxManager.renderToScreen();

    }

    private void doRendering() {
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

        // Calculations
        timer.start("calcs-1");
        if (visibleScreenArea == null && zoneView.isUsingVision()) {
            timer.start("ZoneRenderer-getVisibleArea");
            visibleScreenArea = zoneView.getVisibleArea(view);
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
        font.draw(hudBatch, text, centerX - glyphLayout.width / 2, centerY + glyphLayout.height / 2);
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
            var image = sprites.get(imageKey);
            if (image == null)
                continue;

            timer.stop("tokenlist-1b");

            timer.start("tokenlist-1a");
            Rectangle footprintBounds = token.getBounds(zone);
            image.setPosition(footprintBounds.x, -footprintBounds.y - footprintBounds.height);
            image.setSize(footprintBounds.width, footprintBounds.height);

            // Rotated
            if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN)
                image.setRotation(-token.getFacing() - 90);

            timer.stop("tokenlist-1a");

            timer.start("tokenlist-1e");
            try {
                var awtRect = toAwtRect(image.getBoundingRectangle());

                // Vision visibility
                if (!isGMView && token.isToken() && zoneView.isUsingVision()) {
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

            timer.start("tokenlist-5a");
            if (token.isFlippedIso()) {
                //FIXME: later
                /*
                if (flipIsoImageMap.get(token) == null) {
                    workImage = IsometricGrid.isoImage(workImage);
                    flipIsoImageMap.put(token, workImage);
                } else {
                    workImage = flipIsoImageMap.get(token);
                }
                token.setHeight(workImage.getHeight());
                token.setWidth(workImage.getWidth());
                footprintBounds = token.getBounds(zone);*/
            }
            timer.stop("tokenlist-5a");
/*
            timer.start("tokenlist-6");
            // Position
            // For Isometric Grid we alter the height offset
            double iso_ho = 0;
            Dimension imgSize = new Dimension(workImage.getWidth(), workImage.getHeight());
            if (token.getShape() == Token.TokenShape.FIGURE) {
                double th = token.getHeight() * (double) footprintBounds.width / token.getWidth();
                iso_ho = footprintBounds.height - th;
                footprintBounds =
                        new Rectangle(
                                footprintBounds.x,
                                footprintBounds.y - (int) iso_ho,
                                footprintBounds.width,
                                (int) th);
                iso_ho = iso_ho * getScale();
            }
            SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);
*/

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
            if (!isGMView && zoneView.isUsingVision() && (token.getShape() == Token.TokenShape.FIGURE)) {
                Area cb = zone.getGrid().getTokenCellArea(toAwtRect(image.getBoundingRectangle()));
                if (GraphicsUtil.intersects(visibleScreenArea, cb)) {
                    // the cell intersects visible area so
                    var oldAlpha = image.getColor().a;
                    image.setAlpha(opacity);
                    image.draw(batch);
                    image.setAlpha(oldAlpha);
                }
            } else if (!isGMView && zoneView.isUsingVision() && token.isAlwaysVisible()) {
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
            } catch (InterruptedException e) {
            }
        }).start();


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
        if (event.getId() != MapTool.ZoneEvent.Activated)
            return;

        if (zone != null) {
            disposeZoneResources();
            zone.removeModelChangeListener(this);
        }

        var newZone = (Zone) event.getNewValue();
        newZone.addModelChangeListener(this);

        zone = newZone;
        zoneView = new ZoneView(zone);
        zoneRenderer = MapTool.getFrame().getZoneRenderer(zone);
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
