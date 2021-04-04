package net.rptools.maptool.box2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.ZoneFactory;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.util.ImageManager;

public class MapToolRenderer extends ApplicationAdapter implements AppEventListener, ModelChangeListener {

    private Texture defaultBackgroundTexture;

    private OrthographicCamera cam;
    //private Viewport viewport;
    private SpriteBatch batch;
    private NativeRenderer jfxRenderer;
    private int width;
    private int height;

    public MapToolRenderer() {
        MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
    }


    @Override
    public void create() {
        defaultBackgroundTexture = new Texture(Gdx.files.internal("net/rptools/maptool/client/image/grass.png"));
        var zone = ZoneFactory.createZone();
        var backgroundPaint = zone.getBackgroundPaint();
        if(backgroundPaint instanceof DrawableTexturePaint) {
            var texturePaint = (DrawableTexturePaint)backgroundPaint;
            var image = texturePaint.getAsset().getImage();
            defaultBackgroundTexture = new Texture(new Pixmap(image, 0, image.length));
        }
        defaultBackgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);


        var gridSize = zone.getGrid().getSize();
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        cam = new OrthographicCamera();

        //viewport = new MapToolViewport(cam);
        cam.setToOrtho(false);
        //cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        //viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());//, true);

        batch = new SpriteBatch();
    }

    @Override
    public void render() {
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();


        batch.draw(defaultBackgroundTexture, 0, 0, 0, 0, width, height);

        batch.end();

        if(jfxRenderer != null) {
            var pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
            jfxRenderer.setGdxBuffer(pixmap.getPixels());
            pixmap.dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.position.x = width /2f;
        cam.position.y = height /2f;
        cam.update();
        this.width = width;
        this.height = height;
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

    @Override
    public void handleAppEvent(AppEvent event) {
        if(event.getId() != MapTool.ZoneEvent.Activated)
            return;

    }

    @Override
    public void modelChanged(ModelChangeEvent event) {

    }
}
