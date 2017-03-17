package net.rptools.maptool.box2d.unused;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "MapTool libgdx Test!";
		cfg.width = 1280;
		cfg.height = 720;

		new LwjglApplication(new Box2dLightTest(), cfg);
	}
}