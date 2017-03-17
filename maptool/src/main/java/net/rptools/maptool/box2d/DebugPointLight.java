package net.rptools.maptool.box2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import box2dLight.PointLight;
import box2dLight.RayHandler;

public class DebugPointLight extends PointLight {
	public DebugPointLight(RayHandler rayHandler, int rays, Color color, float distance, float x, float y) {
		super(rayHandler, rays, color, distance, x, y);
	}

	public void drawEdge(ShapeRenderer renderer) {
		if (isSoft()) {
			int numVertices = softShadowMesh.getNumVertices();
			// default mesh edge
			renderer.setColor(Color.CYAN);
			for (int i = 0; i < numVertices * 4 - 8; i += 8) {
				renderer.line(segments[i], segments[i + 1], segments[i + 8], segments[i + 9]);
			}
			renderer.setColor(Color.RED);
			// soft mesh edge
			for (int i = 0; i < numVertices * 4 - 8; i += 8) {
				renderer.line(segments[i + 4], segments[i + 5], segments[i + 12], segments[i + 13]);
			}
		} else {
			int numVertices = lightMesh.getNumVertices();
			renderer.setColor(Color.CYAN);
			for (int i = 4; i < numVertices * 4 - 4; i += 4) {
				renderer.line(segments[i], segments[i + 1], segments[i + 4], segments[i + 5]);
			}
		}
	}

	public void drawRays(ShapeRenderer renderer) {
		float sx = getX();
		float sy = getY();
		if (isSoft()) {
			int numVertices = softShadowMesh.getNumVertices();
			renderer.setColor(Color.PURPLE);
			for (int i = 0; i < numVertices * 4 - 8; i += 8) {
				renderer.line(sx, sy, segments[i + 4], segments[i + 5]);
			}
		} else {
			// rays
			renderer.setColor(Color.YELLOW);
			int numVertices = lightMesh.getNumVertices();
			for (int i = 4; i < numVertices * 4; i += 4) {
				renderer.line(sx, sy, segments[i], segments[i + 1]);
			}
		}
	}
}