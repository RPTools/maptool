package net.rptools.maptool.box2d.unused;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Box2DSpriteAdapter {
	public final String TAG = this.getClass().getSimpleName();

	private Body body;
	private Vector2 offset = new Vector2();

	public Box2DSpriteAdapter(Body body, float width, float height) {
		this.body = body;
		offset.set(width, height);
	}

	public Box2DSpriteAdapter(Body body) {
		this.body = body;
	}

	public Vector2 position() {
		return body.getPosition();
	}

	public float angleRad() {
		return body.getAngle();
	}

	public float angleDeg() {
		return 180 * body.getAngle() / ((float) Math.PI);
	}

	public Vector2 centreOfRotation() {
		return body.getLocalCenter();
	}

	public Vector2 offset() {
		return offset;
	}

	public void setOffset(float x, float y) {
		offset.set(x, y);
	}
}