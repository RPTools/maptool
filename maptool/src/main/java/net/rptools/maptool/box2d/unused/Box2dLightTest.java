package net.rptools.maptool.box2d.unused;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;

public class Box2dLightTest extends ApplicationAdapter {
	OrthographicCamera camera;

	float width, height;

	FPSLogger logger;

	World world;
	Box2DDebugRenderer renderer;

	Body circleBody;

	RayHandler rayHandler;

	@Override
	public void create() {
		width = Gdx.graphics.getWidth() / 5;
		height = Gdx.graphics.getHeight() / 5;

		camera = new OrthographicCamera(width, height);
		camera.position.set(width * 0.5f, height * 0.5f, 0);
		camera.update();

		world = new World(new Vector2(0, -9.8f), false);

		renderer = new Box2DDebugRenderer();

		logger = new FPSLogger();

		BodyDef circleDef = new BodyDef();
		circleDef.type = BodyType.DynamicBody;
		circleDef.position.set(width / 2f, height / 1.1f);

		circleBody = world.createBody(circleDef);

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(3f);

		FixtureDef circleFixture = new FixtureDef();
		circleFixture.shape = circleShape;
		circleFixture.density = 0.4f;
		circleFixture.friction = 0.1f;
		circleFixture.restitution = 0.75f;

		circleBody.createFixture(circleFixture);

		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0, 3);

		Body groundBody = world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(camera.viewportWidth * 2, 3.0f);

		groundBody.createFixture(groundBox, 0.0f);

		rayHandler = new RayHandler(world);
		rayHandler.setCombinedMatrix(camera);

		new PointLight(rayHandler, 5000, Color.DARK_GRAY, 400, (width / 2) - 75, (height / 2) + 50);
		new ConeLight(rayHandler, 5000, Color.PINK, 400, (width / 2) + 30, (height / 2) + 15, 250, 35);
	}

	@Override
	public void dispose() {
		world.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		renderer.render(world, camera.combined);
		rayHandler.updateAndRender();

		world.step(1 / 60f, 6, 2);

		logger.log();
	}
}