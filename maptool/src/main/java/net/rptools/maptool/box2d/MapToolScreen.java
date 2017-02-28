package net.rptools.maptool.box2d;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import box2dLight.PointLight;
import box2dLight.RayHandler;

public class MapToolScreen implements Screen {
	private final MapToolGame app;
	private Stage stage;

	private Texture backgroundTexture;

	private Box2DDebugRenderer renderer;
	private RayHandler rayHandler;

	private static final boolean DEBUG_BOX2D = false;
	private static final boolean CREATE_RANDOM_EFFECTS = true;

	public MapToolScreen(final MapToolGame app) {
		this.app = app;
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		backgroundTexture = new Texture(Gdx.files.internal("net/rptools/maptool/client/image/grass.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

		app.world = new World(new Vector2(0, -9.8f), false);

		if (DEBUG_BOX2D)
			renderer = new Box2DDebugRenderer();

		rayHandler = new RayHandler(app.world);
		rayHandler.setAmbientLight(new Color(.1f, .1f, .1f, .1f));
		RayHandler.useDiffuseLight(true);
		rayHandler.setCombinedMatrix(app.camera);

		// For Testing purposes
		if (CREATE_RANDOM_EFFECTS) {
			createGround();

			for (int i = 0; i < 25; i++) {
				createRandomBody();
			}

			// Create a couple of lights
			for (int i = 0; i < 10; i++) {
				createRandomLight();
			}
		}
	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		update(delta);
		stage.draw();

		Array<Body> bodies = new Array<Body>();
		app.world.getBodies(bodies);

		app.sb.begin();
		app.sb.draw(backgroundTexture, 0, 0, 0, 0, MapToolGame.V_WIDTH, MapToolGame.V_HEIGHT);
		app.font.draw(app.sb, MapToolGame.TITLE, 10, MapToolGame.V_HEIGHT - 10);
		app.font.draw(app.sb, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, MapToolGame.V_HEIGHT - 35);
		app.sb.end();

		drawGrid(50);

		if (DEBUG_BOX2D)
			renderer.render(app.world, app.camera.combined);

		rayHandler.updateAndRender();

		// Drawn after rag=yHandler, sprites always shown regardless of light then, eg always visible
		app.sb.begin();
		for (Body body : bodies) {
			Sprite sprite = (Sprite) body.getUserData();
			if (sprite != null) {
				sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
				sprite.draw(app.sb);
			}
		}
		app.sb.end();

		app.world.step(1 / 60f, 6, 2);
		Gdx.graphics.setTitle(MapToolGame.TITLE + " - " + Gdx.graphics.getFramesPerSecond() + " fps");
	}

	public void update(float delta) {
		stage.act(delta);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	private void drawGrid(int size) {
		ShapeRenderer shape = new ShapeRenderer();

		shape.begin(ShapeType.Line);
		shape.setColor(Color.BLACK);
		for (int i = 0; i < Gdx.graphics.getHeight() / size; i++) {
			shape.line(0, i * size, Gdx.graphics.getWidth(), i * size);
		}
		for (int i = 0; i < Gdx.graphics.getWidth() / size; i++) {
			shape.line(i * size, 0, i * size, Gdx.graphics.getHeight());
		}
		shape.end();
	}

	private void createLight(Color color, float distance, float x, float y) {
		PointLight light = new PointLight(rayHandler, 1000, color, distance, x, y);
		// new ConeLight(rayHandler, 2000, Color.GOLDENROD, 600, (MapToolGame.V_WIDTH / 2) + 30, (MapToolGame.V_HEIGHT / 2) + 15, 250, 35);
		// new PointLight(rayHandler, 2000, Color.DARK_GRAY, 1500, (MapToolGame.V_WIDTH / 3), (MapToolGame.V_HEIGHT / 2) + 50);
	}

	/*
	 * For Testing only
	 */
	private void createGround() {
		// Create the ground...
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0, 3);
		Body groundBody = app.world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(app.camera.viewportWidth * 2, 3.0f);
		groundBody.createFixture(groundBox, 0.0f);
	}

	private void createRandomLight() {
		Random rand = new Random();

		float x = rand.nextFloat() * MapToolGame.V_WIDTH;
		float y = rand.nextFloat() * MapToolGame.V_HEIGHT * 1.25f;

		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();

		float distance = (rand.nextFloat() * 500) + 400;

		Color randomColor = new Color(r, g, b, 1);

		createLight(randomColor, distance, x, y);
	}

	private void createRandomBody() {
		Body circleBody;
		Sprite sprite = new Sprite(new Texture("net/rptools/maptool/client/image/redDot.png"));
		Random rand = new Random();

		float x = rand.nextFloat() * MapToolGame.V_WIDTH;
		float y = rand.nextFloat() * MapToolGame.V_HEIGHT * 1.25f;
		float r = rand.nextFloat();

		BodyDef circleDef = new BodyDef();
		circleDef.type = BodyType.DynamicBody;
		circleDef.position.set(x, y);

		circleBody = app.world.createBody(circleDef);

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(32f);

		FixtureDef circleFixture = new FixtureDef();
		circleFixture.shape = circleShape;
		circleFixture.density = rand.nextFloat();
		circleFixture.friction = rand.nextFloat();
		circleFixture.restitution = rand.nextFloat();

		circleBody.createFixture(circleFixture);

		// Attach the sprite
		sprite.setOriginCenter();
		sprite.setPosition(x, y);
		//		sprite.scale(r);
		circleBody.setUserData(sprite);

		// Dispose of stuff
		circleShape.dispose();
	}
}