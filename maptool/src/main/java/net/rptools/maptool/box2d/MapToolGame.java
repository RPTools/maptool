package net.rptools.maptool.box2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;

public class MapToolGame extends Game {
	public static final String TITLE = "MapTool Raycasting Test";
	public static final int V_WIDTH = 1900;
	public static final int V_HEIGHT = 1100;
	public static final int PPM = 64; // Pixels Per Meter

	public OrthographicCamera camera;
	public SpriteBatch sb;
	public World world;
	public BitmapFont font;

	@Override
	public void create() {
		camera = new OrthographicCamera(V_WIDTH / PPM, V_HEIGHT / PPM);
		camera.setToOrtho(false);
		sb = new SpriteBatch();
		font = new BitmapFont();

		this.setScreen(new MapToolScreen(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		sb.dispose();
	}

}