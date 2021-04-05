package net.rptools.maptool.box2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;

public class MapToolRenderer extends ApplicationAdapter implements AppEventListener, ModelChangeListener {

    private static MapToolRenderer _instance;
    public static MapToolRenderer getInstance() {
        if(_instance == null)
            _instance = new MapToolRenderer();
        return _instance;
    }

    private boolean useFbo = false;


    // zone specific resources
    private Zone zone;
    private Sprite background;
    private Sprite map;


    // general resources
    private OrthographicCamera cam;
    private SpriteBatch batch;
    private NativeRenderer jfxRenderer;
    private boolean initialized = false;
    private int width;
    private int height;
    private int offsetX = 0;
    private int offsetY = 0;
    private float zoom = 1.0f;

    public MapToolRenderer() {
        MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
    }


    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        cam = new OrthographicCamera();
        cam.setToOrtho(false);

        batch = new SpriteBatch();
        initialized = true;
        initializeZoneResources();
    }

    @Override
    public void render() {
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        if (background != null) {
            var startX = (cam.position.x - cam.viewportWidth*zoom/2);
            startX = (int)(startX/background.getWidth()) * background.getWidth() - background.getWidth();
            var endX = cam.position.x + cam.viewportWidth/2*zoom;
            var startY = (cam.position.y - cam.viewportHeight*zoom/2);
            startY= (int)(startY/background.getHeight()) * background.getHeight() - background.getHeight();
            var endY = (cam.position.y + cam.viewportHeight/2*zoom);

            for(var i = startX; i < endX; i+= background.getWidth())
                for(var j = startY; j < endY; j += background.getHeight()) {
                    background.setPosition(i, j);
                    background.draw(batch);
                }
        }

        if (map != null)
            map.draw(batch);

        batch.end();

        if (jfxRenderer != null) {
            Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
            jfxRenderer.setGdxBuffer(pixmap.getPixels());
            pixmap.dispose();
        }
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
        cam.position.x = zoom *(width / 2f + offsetX);
        cam.position.y = zoom *(height / 2f * - 1 + offsetY);
        cam.zoom = zoom;
        cam.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    public void setJfxRenderer(NativeRenderer renderer) {
        jfxRenderer = renderer;
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "MapTool libgdx Test!";
        cfg.width = 800;
        cfg.height = 600;

        new LwjglApplication(new MapToolRenderer(), cfg);
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

            var map = this.map;
            this.map = null;
            if (map != null) {
                map.getTexture().dispose();
            }
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
        var mapAssetId = zone.getMapAssetId();
        if (mapAssetId != null) {
            var mapBytes = AssetManager.getAsset(mapAssetId).getImage();
            Gdx.app.postRunnable(() -> {
                var pix = new Pixmap(mapBytes, 0, mapBytes.length);
                map = new Sprite(new Texture(pix));
                map.setPosition(zone.getBoardX(), zone.getBoardY() - pix.getHeight());
                map.setSize(pix.getWidth(), pix.getHeight());
                pix.dispose();
            });
        }

    }


    @Override
    public void handleAppEvent(AppEvent event) {
        if (event.getId() != MapTool.ZoneEvent.Activated)
            return;

        if (zone != null)
            disposeZoneResources();

        this.zone = (Zone) event.getNewValue();
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
        zoom = (float)(1f/scale.getScale());
        updateCam();
    }
}
