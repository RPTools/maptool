package net.rptools.maptool.box2d;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.ArrayUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.EarClippingTriangulator;
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
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.ImageManager;

public class Box2dRenderer implements Screen {
	private static final Texture DEFAULT_BACKGROUND_TEXURE = new Texture(Gdx.files.internal("net/rptools/maptool/client/image/grass.png"));
	private static final Sprite SOCCER_BALL = new Sprite(new Texture("net/rptools/maptool/client/image/soccer_ball.png"));
	private static final Sprite LIGHT_SOURCE = new Sprite(new Texture("net/rptools/maptool/client/image/light_ball.png"));

	private static final boolean DEBUG_BOX2D = false;
	private static final boolean CREATE_RANDOM_EFFECTS = true;
	private static boolean GAME_PAUSED = false;

	private static Texture backgroundTexture;
	private static TextureRegion textureRegion;
	private static byte[] backgroundImage;
	private static Area vblArea = new Area();

	private static Set<Body> vblBodySet = new HashSet<>();
	private static Set<PolygonSprite> polySpriteSet = new HashSet<>();
	private static Set<DebugPointLight> lightSet = new HashSet<>();

	public static float frameRate = 60;

	private final MapToolGame app;
	private MapToolFrame mapToolFrame;
	private Stage stage;
	private Box2DDebugRenderer renderer;
	private RayHandler rayHandler;
	private ShapeRenderer shapeRenderer;

	public Box2dRenderer(final MapToolGame app) {
		this.app = app;
		this.mapToolFrame = app.getMapToolFrame();

		MapTool_InputProcessor inputProcessor = new MapTool_InputProcessor(this);
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(inputProcessor); // was stage

		app.world = new World(new Vector2(0, 0), true); // Earth gravity would be -9.7

		if (DEBUG_BOX2D)
			renderer = new Box2DDebugRenderer();

		rayHandler = new RayHandler(app.world);
		//rayHandler.setAmbientLight(new Color(.1f, .1f, .1f, .1f));
		RayHandler.useDiffuseLight(true);
		rayHandler.setCombinedMatrix(app.camera);
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		// For Testing purposes
		if (CREATE_RANDOM_EFFECTS) {
			createTop();
			createBottom();
			createLeftSide();
			createRightSide();

			for (int i = 0; i < 20; i++) {
				createRandomBody(false);
			}

			// Create a couple of lights on bodies?
			for (int i = 0; i < 5; i++) {
				createRandomBody(true);
				//				createRandomLight();
			}
		}

		// For VBL texture
		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, 196); // 128 default alpha
		pix.fill();
		Texture textureSolid = new Texture(pix);
		textureRegion = new TextureRegion(textureSolid);
	}

	public Camera getCamera() {
		return app.camera;
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

		drawBackgroundTile();

		if (DEBUG_BOX2D)
			renderer.render(app.world, app.camera.combined);

		renderGrid();
		renderLightSources(bodies);
		renderVBL();
		renderVblSprites(bodies);

		renderSoccerBalls(bodies, true);
		rayHandler.updateAndRender();

		debugLights();

		// Drawn after rayHandler, spites always shown regardless of light then, eg always visible, can break up vbl/tokens later
		//		renderSoccerBalls(bodies, false);
		renderFPS();

		if (!GAME_PAUSED)
			app.world.step(1 / frameRate, 6, 2);

		Gdx.graphics.setTitle(MapToolGame.TITLE + " - " + Gdx.graphics.getFramesPerSecond() + " fps");
	}

	public void update(float delta) {
		stage.act(delta);
	}

	@Override
	public void resize(int width, int height) {
		app.camera.viewportHeight = (MapToolGame.VIEWPORT_WIDTH / width) * height;

		//		stage.getViewport().update(width, height, true);
		//		MapToolGame.SCREEN_WIDTH = width;
		//		MapToolGame.SCREEN_HEIGHT = height;
	}

	@Override
	public void pause() {
		GAME_PAUSED = !GAME_PAUSED;
	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		shapeRenderer.dispose();
		stage.dispose();
	}

	private void renderFPS() {
		app.sb.begin();
		app.font.draw(app.sb, MapToolGame.TITLE, 20, MapToolGame.SCREEN_HEIGHT - 10);
		app.font.draw(app.sb, "FPS: " + Gdx.graphics.getFramesPerSecond() + " [" + frameRate + "]", 20, MapToolGame.SCREEN_HEIGHT - 35);
		app.font.draw(app.sb, "Lights: " + lightSet.size(), 20, MapToolGame.SCREEN_HEIGHT - 60);
		app.sb.end();
	}

	private void renderLightSources(Array<Body> bodies) {
		app.sb.begin();
		for (Body body : bodies) {
			if (body.getUserData() != null) {
				if (body.getUserData() instanceof Sprite) {
					Sprite sprite = (Sprite) body.getUserData();
					sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
					sprite.setRotation((float) (body.getAngle() * 180 / Math.PI % 360));

					if (sprite == LIGHT_SOURCE) {
						sprite.draw(app.sb);
					}
				}

			}
		}
		app.sb.end();

	}

	private void renderSoccerBalls(Array<Body> bodies, boolean beforeLights) {
		app.sb.begin();
		for (Body body : bodies) {
			if (body.getUserData() != null) {
				if (body.getUserData() instanceof Sprite) {
					Sprite sprite = (Sprite) body.getUserData();
					sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
					sprite.setRotation((float) (body.getAngle() * 180 / Math.PI % 360));

					if (sprite == SOCCER_BALL) {
						if (beforeLights) {
							sprite.draw(app.sb);
						} else {
							for (PointLight light : lightSet) {
								if (light.contains(sprite.getX(), sprite.getY())) {
									sprite.draw(app.sb);
									break;
								}
							}
						}
					}
				}

			}
		}
		app.sb.end();

	}

	private void renderVblSprites(Array<Body> bodies) {
		app.polyBatch.begin();
		for (Body body : bodies) {
			if (body.getUserData() != null) {
				if (body.getUserData() instanceof PolygonSprite) {
					PolygonSprite sprite = (PolygonSprite) body.getUserData();
					sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
					sprite.setRotation((float) (body.getAngle() * 180 / Math.PI % 360));
					sprite.draw(app.polyBatch);
				}
			}
		}
		app.polyBatch.end();
	}

	private void debugLights() {
		if (lightSet.isEmpty() || !DEBUG_BOX2D)
			return;

		shapeRenderer.begin();
		for (DebugPointLight light : lightSet) {
			//light.drawRays(shapeRenderer);
			light.drawEdge(shapeRenderer);
		}
		shapeRenderer.end();
	}

	/*
	 * For POC only TODO: Check for color vs image and change to a listener vs getting images every frame
	 */
	private void drawBackgroundTile() {
		Zone zone = mapToolFrame.getCurrentZoneRenderer().getZone();
		byte[] image = ImageManager.getTexture(zone.getBackgroundAsset(), mapToolFrame.getCurrentZoneRenderer());

		if (image != null && image != backgroundImage) {
			backgroundTexture = new Texture(new Pixmap(image, 0, image.length));
			backgroundImage = image; // Remember last image
			System.out.println("Background image changed!");
		}

		if (backgroundTexture == null)
			backgroundTexture = DEFAULT_BACKGROUND_TEXURE;

		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		app.sb.begin();
		app.sb.draw(backgroundTexture, 0, 0, 0, 0, MapToolGame.SCREEN_WIDTH, MapToolGame.SCREEN_HEIGHT);
		app.sb.end();
	}

	/*
	 * TODO: Store grid shape for future frames and fix Hex/Iso offset!
	 */
	private void renderGrid() {
		Zone zone = mapToolFrame.getCurrentZoneRenderer().getZone();
		int gridSize = (int) (zone.getGrid().getSize() * getScale());

		if (!AppState.isShowGrid() || gridSize < ZoneRenderer.MIN_GRID_SIZE) {
			return;
		}

		float gridWidth = (float) zone.getGrid().getCellWidth();
		float gridHeight = (float) zone.getGrid().getCellHeight();

		java.awt.Color gridColor = new java.awt.Color(zone.getGridColor());
		ShapeRenderer shape = new ShapeRenderer();

		shape.begin(ShapeType.Line);
		shape.setColor(gridColor.getRed(), gridColor.getGreen(), gridColor.getBlue(), 4f);
		float[] gridVertices = areaToVertices(zone.getGrid().getCellShape());

		shape.translate(1, 1, 0);
		float transX = ((Gdx.graphics.getWidth() / gridWidth) + 1) * -gridWidth;

		for (int h = 0; h <= Gdx.graphics.getHeight() / gridHeight; h++) {
			for (int w = 0; w <= Gdx.graphics.getWidth() / gridWidth; w++) {
				shape.polygon(gridVertices);
				shape.translate(gridWidth, 0, 0);
			}

			shape.translate(transX, gridHeight, 0);
		}

		shape.end();
	}

	private void renderVBL() {
		Zone zone = mapToolFrame.getCurrentZoneRenderer().getZone();

		if (vblArea.equals(zone.getTopology()))
			return;

		System.out.println("VBL Changed!");
		vblArea = new Area(zone.getTopology());

		for (Body body : vblBodySet) {
			app.world.destroyBody(body);
		}

		vblBodySet.clear();
		polySpriteSet.clear();

		ArrayList<float[]> vblAreas = parseVblAreas(vblArea);
		for (float[] vertices : vblAreas) {
			// Creating a Sprite to show VBL for each VBL "island"
			EarClippingTriangulator triangulator = new EarClippingTriangulator();
			ShortArray triangleIndices = triangulator.computeTriangles(vertices);
			PolygonRegion polyReg = new PolygonRegion(textureRegion, vertices, triangleIndices.toArray());

			Body vblBody = createVblBody(vertices);
			if (vblBody != null) {
				vblBody.setUserData(new PolygonSprite(polyReg));
				vblBodySet.add(vblBody);
			}
		}
	}

	private ArrayList<float[]> parseVblAreas(Area area) {
		ArrayList<float[]> vblAreas = new ArrayList<float[]>();
		ArrayList<Float> vertices = new ArrayList<Float>();
		float[] coords = new float[6];
		float lastX = 0, lastY = 0;

		// Flip the y axis because box2d y is down
		AffineTransform at = new AffineTransform();
		at.translate(0, Gdx.graphics.getHeight());
		at.scale(1, -1);

		for (PathIterator iter = area.createTransformedArea(at).getPathIterator(null); !iter.isDone(); iter.next()) {
			int type = iter.currentSegment(coords);

			switch (type) {
			case PathIterator.SEG_CLOSE:
				System.out.println("SEG_CLOSE");
				vblAreas.add(ArrayUtils.toPrimitive(vertices.toArray(new Float[vertices.size()])));
				break;
			case PathIterator.SEG_LINETO:
				System.out.println("SEG_LINETO");
				// Jamz: depending on the topology tool used, the last point == first point which is not expected for box2d polygon ear clipping
				if (coords[0] == vertices.get(0) && coords[1] == vertices.get(1))
					break;
				if (lastX == coords[0] && lastY == coords[1]) {
					System.out.println("coords: " + Arrays.toString(coords));
					System.out.println("Skip!");
					break;
				}
				vertices.add(coords[0]);
				vertices.add(coords[1]);
				lastX = coords[0];
				lastY = coords[1];
				break;
			case PathIterator.SEG_MOVETO:
				System.out.println("SEG_MOVETO");
				vertices = new ArrayList<Float>();
				vertices.add(coords[0]);
				vertices.add(coords[1]);
				lastX = coords[0];
				lastY = coords[1];
				break;
			}
		}

		return vblAreas;
	}

	//	private void drawFilledPolygon(Polygon polygon, Color color) {
	//		ShapeRenderer shapeRenderer = null;
	//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
	//		shapeRenderer.setColor(color);
	//
	//		float[] vertices = polygon.getTransformedVertices();
	//
	//		// NOTE: you probably don't want to create a new EarClippingTriangulator each frame
	//		ShortArray triangleIndices = new EarClippingTriangulator().computeTriangles(vertices);
	//		for (int i = 0; i < triangleIndices.size; i += 3) {
	//			shapeRenderer.triangle(
	//					vertices[triangleIndices.get(i) * 2], vertices[triangleIndices.get(i) * 2 + 1],
	//					vertices[triangleIndices.get(i + 1) * 2], vertices[triangleIndices.get(i + 1) * 2 + 1],
	//					vertices[triangleIndices.get(i + 2) * 2], vertices[triangleIndices.get(i + 2) * 2 + 1]);
	//		}
	//
	//		shapeRenderer.end();
	//	}

	private float[] areaToVertices(Area area) {
		if (area.isEmpty())
			return new float[0];

		PathIterator iterator = area.getPathIterator(null);
		float[] floats = new float[60];
		float[] vertices = new float[1000];
		int count1 = 0;

		while (!iterator.isDone()) {
			int type = iterator.currentSegment(floats);

			if (type != PathIterator.SEG_CLOSE) {
				vertices[count1++] = floats[0];
				vertices[count1++] = floats[1];
			}
			iterator.next();
		}

		float[] finalVertices = new float[count1];
		System.arraycopy(vertices, 0, finalVertices, 0, count1);

		return finalVertices;
	}

	//	private Body createVblBody(ShortArray triangles) {
	//		if (triangles.size == 0)
	//			return null;
	//
	//		Body vblBody;
	//
	//		BodyDef vblDef = new BodyDef();
	//		vblDef.type = BodyType.StaticBody;
	//		vblBody = app.world.createBody(vblDef);
	//
	//		System.out.println("Looks like we have an array size of: " + triangles.size);
	//		System.out.println("triangle: " + triangles.toString());
	//
	//		for (int i = 0; i < triangles.size; i = i + 6) {
	//			PolygonShape vblShape = new PolygonShape();
	//
	//			Vector2[] triangle = { new Vector2(triangles.get(i + 0), triangles.get(i + 1)),
	//					new Vector2(triangles.get(i + 2), triangles.get(i + 3)),
	//					new Vector2(triangles.get(i + 4), triangles.get(i + 5)) };
	//
	//			vblShape.set(triangle);
	//			vblBody.createFixture(vblShape, 0f);
	//			vblShape.dispose();
	//		}
	//
	//		return vblBody;
	//	}

	private Body createVblBody(float[] areaPath) {
		if (areaPath.length == 0)
			return null;

		Body vblBody;

		BodyDef vblDef = new BodyDef();
		vblDef.type = BodyType.StaticBody;
		vblBody = app.world.createBody(vblDef);

		System.out.println("Looks like we have an array size of: " + areaPath.length);
		System.out.println("areaPath : " + Arrays.toString(areaPath));

		ShortArray triangles = new EarClippingTriangulator().computeTriangles(areaPath);
		FloatArray polygon = new FloatArray(areaPath);
		//		FloatArray triangleOutlines = new FloatArray(triangles.size * 2);

		System.out.println("triangle size : " + triangles.size);

		for (int i = 0; i < triangles.size; i += 3) {
			PolygonShape vblShape = new PolygonShape();
			FloatArray triangleOutlines = new FloatArray();

			float ax = polygon.get(triangles.get(i) * 2);
			float ay = polygon.get(triangles.get(i) * 2 + 1);
			float bx = polygon.get(triangles.get(i + 1) * 2);
			float by = polygon.get(triangles.get(i + 1) * 2 + 1);
			float cx = polygon.get(triangles.get(i + 2) * 2);
			float cy = polygon.get(triangles.get(i + 2) * 2 + 1);

			triangleOutlines.add(ax);
			triangleOutlines.add(ay);
			triangleOutlines.add(bx);
			triangleOutlines.add(by);
			triangleOutlines.add(cx);
			triangleOutlines.add(cy);

			//			System.out.println("triangleOutlines : " + Arrays.toString(triangleOutlines.toArray()));
			vblShape.set(triangleOutlines.toArray());
			//			System.out.println("Body shape created ok.");
			vblBody.createFixture(vblShape, 0f);

			vblShape.dispose();
		}

		return vblBody;
	}

	//	private Body createVblBodyOLD(float[] vertices) {
	//		if (vertices.length == 0)
	//			return null;
	//
	//		Body vblBody;
	//
	//		BodyDef vblDef = new BodyDef();
	//		vblDef.type = BodyType.StaticBody;
	//		vblBody = app.world.createBody(vblDef);
	//
	//		PolygonShape vblShape = new PolygonShape();
	//		vblShape.set(vertices);
	//		vblBody.createFixture(vblShape, 0f);
	//
	//		vblShape.dispose();
	//
	//		return vblBody;
	//	}

	public double getScale() {
		return mapToolFrame.getCurrentZoneRenderer().getScale();
	}

	/*
	 * For Testing only
	 */
	private void createTop() {
		// Create the ground...
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(app.camera.viewportWidth * .25f, app.camera.viewportHeight * 1.25f);
		Body groundBody = app.world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(app.camera.viewportWidth * 1.5f, 3.0f);
		groundBody.createFixture(groundBox, 0.0f);
	}

	private void createBottom() {
		// Create the ground...
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(app.camera.viewportWidth * .25f, -(app.camera.viewportHeight * .25f));
		Body groundBody = app.world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(app.camera.viewportWidth * 1.5f, 3.0f);
		groundBody.createFixture(groundBox, 0.0f);
	}

	private void createLeftSide() {
		// Create the ground...
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(1, 0);
		Body groundBody = app.world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(3.0f, app.camera.viewportHeight * 2);
		groundBody.createFixture(groundBox, 0.0f);
	}

	private void createRightSide() {
		// Create the ground...
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set((app.camera.viewportWidth) - 1, 0);
		Body groundBody = app.world.createBody(groundBodyDef);

		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(3.0f, app.camera.viewportHeight * 2);
		groundBody.createFixture(groundBox, 0.0f);
	}

	private DebugPointLight createLight(Color color, float distance, float x, float y) {
		return new DebugPointLight(rayHandler, 500, color, distance, x, y);
		// new ConeLight(rayHandler, 2000, Color.GOLDENROD, 600, (MapToolGame.V_WIDTH / 2) + 30, (MapToolGame.V_HEIGHT / 2) + 15, 250, 35);
		// new PointLight(rayHandler, 2000, Color.DARK_GRAY, 1500, (MapToolGame.V_WIDTH / 3), (MapToolGame.V_HEIGHT / 2) + 50);
	}

	private DebugPointLight createRandomLight() {
		Random rand = new Random();

		float x = rand.nextFloat() * MapToolGame.SCREEN_WIDTH;
		float y = rand.nextFloat() * MapToolGame.SCREEN_HEIGHT * 1.25f;

		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();

		//		float distance = (rand.nextFloat() * 500) + 400;
		float distance = 20 * MapToolGame.F2M * MapToolGame.PPM; // 20 feet?

		//		Color randomColor = new Color(0f, 0f, 0f, 255f);
		Color randomColor = new Color(r, g, b, 255);

		DebugPointLight light = createLight(randomColor, distance, x, y);
		light.setSoftnessLength(128f); // how far light travels through body...

		return light;
	}

	public void clearLights() {
		System.out.println("Removing " + lightSet.size() + " lights.");

		for (DebugPointLight light : lightSet) {
			light.remove(true);
			app.world.destroyBody(light.getBody());
		}

		lightSet.clear();
	}

	public void createBody(boolean attachLight, float x, float y) {
		Body circleBody;

		BodyDef circleDef = new BodyDef();
		circleDef.type = BodyType.DynamicBody;
		circleDef.position.set(x, y);

		circleBody = app.world.createBody(circleDef);

		CircleShape circleShape = new CircleShape();
		if (attachLight)
			circleShape.setRadius(24f);
		else
			circleShape.setRadius(32f);

		FixtureDef circleFixture = new FixtureDef();
		circleFixture.shape = circleShape;
		circleFixture.density = random(1, 100f);
		circleFixture.friction = random(.25f, 1f);
		circleFixture.restitution = random(.25f, 1f);

		circleBody.createFixture(circleFixture);
		//circleBody.setAngularVelocity(random(-5f, 5f));
		circleBody.setLinearVelocity(new Vector2(random(-250, 250), random(-250, 250)));

		// Attach the sprite
		Sprite bodySprite;
		if (attachLight)
			bodySprite = LIGHT_SOURCE;
		else
			bodySprite = SOCCER_BALL;

		bodySprite.setOriginCenter();
		bodySprite.setPosition(x, y);
		//sprite.scale(r);
		circleBody.setUserData(bodySprite);

		if (attachLight) {
			DebugPointLight pointLight = createRandomLight();
			lightSet.add(pointLight);
			pointLight.attachToBody(circleBody);
		}

		// Dispose of stuff
		circleShape.dispose();
	}

	private void createRandomBody(boolean attachLight) {
		float x = random(64, MapToolGame.SCREEN_WIDTH);
		float y = random(MapToolGame.SCREEN_HEIGHT / 2, MapToolGame.SCREEN_HEIGHT);

		createBody(attachLight, x, y);
	}

	private float random(float min, float max) {
		return (float) ThreadLocalRandom.current().nextDouble(min, max + 1);
	}
}