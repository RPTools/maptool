package net.rptools.maptool.box2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;

public class MapTool_InputProcessor implements InputProcessor {
	Box2dRenderer renderer;

	public MapTool_InputProcessor(Box2dRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.SPACE:
			renderer.pause();
			break;

		case Keys.UP:
			Box2dRenderer.frameRate += 1;
			break;

		case Keys.DOWN:
			Box2dRenderer.frameRate -= 1;
			break;

		default:
			break;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//		System.out.println("pointer, button: " + pointer + ", " + button);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == Buttons.LEFT) {
			Vector3 input = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
			renderer.getCamera().unproject(input);

			//Now you can use input.x and input.y, as opposed to Gdx.input.getX() and Gdx.input.getY(), to draw the circle
			System.out.println("x, y: " + input.x + ", " + input.y);
			renderer.createBody(true, input.x, input.y);
		} else if (button == Buttons.RIGHT) {
			renderer.clearLights();
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		//		System.out.println("mouse moved: " + screenX + ", " + screenY);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		System.out.println("Scrolled " + amount);
		return false;
	}
}
