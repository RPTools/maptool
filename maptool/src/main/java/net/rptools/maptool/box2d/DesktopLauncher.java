package net.rptools.maptool.box2d;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import net.rptools.maptool.client.ui.MapToolFrame;

public class DesktopLauncher extends JFrame {

	private static final long serialVersionUID = 2536172952937398744L;
	private LwjglAWTCanvas canvas;
	private LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	private MapToolFrame mapToolFrame;

	public DesktopLauncher(MapToolFrame clientFrame) {
		cfg.title = MapToolGame.TITLE;
		cfg.width = MapToolGame.SCREEN_WIDTH;
		cfg.height = MapToolGame.SCREEN_HEIGHT;
		mapToolFrame = clientFrame;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		canvas = new LwjglAWTCanvas(new MapToolGame(mapToolFrame), cfg);
		canvas.getCanvas().setSize(MapToolGame.SCREEN_WIDTH, MapToolGame.SCREEN_HEIGHT);
		add(canvas.getCanvas());

		pack();
		setVisible(true);
	}

	public static void main(String[] arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new DesktopLauncher(null);
			}
		});
	}

	@Override
	public void dispose() {
		canvas.stop();

		super.dispose();
	}
}