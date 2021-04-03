package net.rptools.maptool.box2d;

import java.util.Objects;

/**
 * Immutable viewport class.
 *
 * @author Michael Paus
 */
public class Viewport {

    private final int minX;
    private final int minY;
    private final int width;
    private final int height;

    public Viewport() {
        this(0, 0, -1, -1);
    }

    public Viewport(int minX, int minY, int width, int height) {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
    }

    public Viewport withLocation(int minX, int minY) {
        return new Viewport(minX, minY, width, height);
    }

    public Viewport withDeltaLocation(int deltaX, int deltaY) {
        return new Viewport(minX + deltaX, minY + deltaY, width, height);
    }

    public Viewport withSize(int width, int height) {
        return new Viewport(minX, minY, width, height);
    }

    // Increment or decrement the view size in steps of view_incr.
    public Viewport withSizeIncrement(int width, int height, int sizeIncrement) {
        if (width > 0 && height > 0 && sizeIncrement > 0) {
            int newNrViewWidth = (width % sizeIncrement > 0) ? (width / sizeIncrement + 1) * sizeIncrement : (width / sizeIncrement) * sizeIncrement;
            int newNrViewHeight = (height % sizeIncrement > 0) ? (height / sizeIncrement + 1) * sizeIncrement : (height / sizeIncrement) * sizeIncrement;

            if (newNrViewWidth != this.width || newNrViewHeight != this.height) {
                return this.withSize(newNrViewWidth, newNrViewHeight);
            }
        }
        return this;
    }

    public Viewport withDeltaSize(int deltaWidth, int deltaHeight) {
        return new Viewport(minX, minY, width + deltaWidth, height + deltaHeight);
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isEmpty() {
        return width < 0 || height < 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, minX, minY, width);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Viewport other = (Viewport) obj;
        return height == other.height && minX == other.minX && minY == other.minY && width == other.width;
    }

    @Override
    public String toString() {
        return "Viewport [minX=" + minX + ", minY=" + minY + ", width=" + width + ", height=" + height + "]";
    }

}