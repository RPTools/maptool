package net.rptools.tokentool;

import java.awt.Color;

public class CompositionProperties {

	private double translucency = 1;
	private int fudgeFactor = 20;
	private boolean solidBackground = true;
	private boolean base = false;
	private Color backgroundColor = Color.white;

	public double getTranslucency() {
		return translucency;
	}

	public void setTranslucency(double alpha) {
		this.translucency = alpha;
	}

	public int getFudgeFactor() {
		return fudgeFactor;
	}

	public void setFudgeFactor(int fudgeFactor) {
		this.fudgeFactor = fudgeFactor;
	}

	public boolean isSolidBackground() {
		return solidBackground;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color newColor) {
		this.backgroundColor = newColor;
	}

	public boolean isBase() {
		return base;
	}

	public void setBase(boolean base) {
		this.base = base;
	}

	public void setSolidBackground(boolean solidBackground) {
		this.solidBackground = solidBackground;
	}

}
