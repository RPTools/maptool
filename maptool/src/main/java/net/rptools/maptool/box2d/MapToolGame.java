package net.rptools.maptool.box2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

import net.rptools.maptool.client.ui.MapToolFrame;

public class MapToolGame extends Game {
	public static final String TITLE = "MapTool Raycasting Test";
	public static int SCREEN_WIDTH = 1900;
	public static int SCREEN_HEIGHT = 1100;
	public static int VIEWPORT_WIDTH = 25;
	public static int VIEWPORT_HEIGHT = 14;
	public static final float PPM = 76.2f; // Pixels Per Meter, gives us 50 pixels per 5 feet
	public static final float F2M = .3048f; // Feet to Meters

	public OrthographicCamera camera;
	public SpriteBatch sb;
	public PolygonSpriteBatch polyBatch;
	public World world;
	public BitmapFont font;

	private MapToolFrame mapToolFrame;

	public MapToolGame(MapToolFrame parentFrame) {
		mapToolFrame = parentFrame;
	}

	@Override
	public void create() {
		camera = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		camera.position.set(VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2, 0);
		camera.setToOrtho(false);
		camera.update();

		sb = new SpriteBatch();
		polyBatch = new PolygonSpriteBatch();
		polyBatch.setProjectionMatrix(camera.combined);
		font = new BitmapFont();

		this.setScreen(new Box2dRenderer(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		sb.dispose();
	}

	public MapToolFrame getMapToolFrame() {
		return this.mapToolFrame;
	}
}