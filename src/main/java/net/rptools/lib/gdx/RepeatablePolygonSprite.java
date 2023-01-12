package net.rptools.lib.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Renders polygon filled with a repeating TextureRegion with specified density
 * Without causing an additional flush or render call
 *
 * @author Avetis Zakharyan (concept and first version)
 * @author Kablion (rewrite)
 */
public class RepeatablePolygonSprite implements Disposable {

    public enum WrapType {
        STRETCH,REPEAT,REPEAT_MIRRORED
    }

    private WrapType wrapTypeX = WrapType.REPEAT;
    private WrapType wrapTypeY = WrapType.REPEAT;

    private final Color color = new Color(Color.WHITE);

    private TextureRegion textureRegion;
    private TextureRegion whiteTextureRegion;

    private Vector2 textureOffset = new Vector2();
    private float textureWidth = 0;
    private float textureHeight = 0;

    private float textureDebugX = 0;
    private float textureDebugY = 0;

    private boolean dirtyGrid = true;
    private boolean dirtyAttributes = true;

    private Array<float[]> parts = new Array<float[]>();

    private Array<float[]> vertices = new Array<float[]>();
    private Array<short[]> indices = new Array<short[]>();

    private int rows;
    private Vector2 gridOffset = new Vector2();
    private float gridWidth, gridHeight;
    private Vector2 buildOffset = new Vector2();

    private Polygon polygon = new Polygon();
    private Rectangle boundingRect = new Rectangle();

    public RepeatablePolygonSprite() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whiteTextureRegion = new TextureRegion(new Texture(pixmap));
        pixmap.dispose();
    }

    /**
     * calculates the grid and the parts in relation to the texture Origin
     */
    private void prepareVertices() {
        parts.clear();
        if (polygon.getVertices().length == 0) return;
        float[] vertices = polygon.getVertices().clone();

        Polygon polygon = new Polygon(vertices);
        Polygon tmpPoly = new Polygon();
        Polygon intersectionPoly = new Polygon();
        EarClippingTriangulator triangulator = new EarClippingTriangulator();

        int idx;

        Rectangle bounds = polygon.getBoundingRectangle();


        if (wrapTypeX == WrapType.STRETCH || textureRegion == null) {
            gridWidth = bounds.getWidth();
        } else {
            gridWidth = textureWidth;
        }
        if (wrapTypeY == WrapType.STRETCH || textureRegion == null) {
            gridHeight = bounds.getHeight();
        } else {
            gridHeight = textureHeight;
        }

        polygon.setVertices(offset(vertices));

        bounds = polygon.getBoundingRectangle();

        int cols = (int) (Math.ceil(bounds.getWidth() / gridWidth));
        if (bounds.getX() + bounds.getWidth() > (cols + gridOffset.x) * gridWidth) cols++;
        rows = (int) Math.ceil(bounds.getHeight() / gridHeight);
        if (bounds.getY() + bounds.getHeight() > (rows + gridOffset.y) * gridHeight) rows++;

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                float[] verts = new float[8];
                idx = 0;
                int offsettedCol = col + (int) gridOffset.x;
                int offsettedRow = row + (int) gridOffset.y;

                verts[idx++] = offsettedCol * gridWidth;
                verts[idx++] = offsettedRow * gridHeight;

                verts[idx++] = (offsettedCol) * gridWidth;
                verts[idx++] = (offsettedRow + 1) * gridHeight;

                verts[idx++] = (offsettedCol + 1) * gridWidth;
                verts[idx++] = (offsettedRow + 1) * gridHeight;

                verts[idx++] = (offsettedCol + 1) * gridWidth;
                verts[idx] = (offsettedRow) * gridHeight;

                tmpPoly.setVertices(verts);

                Intersector.intersectPolygons(polygon, tmpPoly, intersectionPoly);
                verts = intersectionPoly.getVertices();
                if (verts.length > 0) {
                    parts.add(snapToGrid(verts, offsettedRow, offsettedCol));
                    ShortArray arr = triangulator.computeTriangles(verts);
                    indices.add(arr.toArray());
                } else {
                    // adding null for key consistancy, needed to get col/row from key
                    // the other alternative is to make parts - IntMap<FloatArray>
                    parts.add(null);
                }
            }
        }
        dirtyGrid = false;
        dirtyAttributes = true;
    }

    /**
     * Builds final vertices with vertex attributes like coordinates, color and region u/v
     */
    private void buildVertices() {
        vertices.clear();
        if (polygon.getVertices().length == 0) return;
        for (int i = 0; i < parts.size; i++) {
            float verts[] = parts.get(i);
            if (verts == null) continue;

            float[] fullVerts = new float[5 * verts.length / 2];
            int idx = 0;

            int col = i / rows;
            int row = i % rows;
            int offsettedCol = col + (int) gridOffset.x;
            int offsettedRow = row + (int) gridOffset.y;

            for (int j = 0; j < verts.length; j += 2) {
                float x = (verts[j] + buildOffset.x + textureOffset.x) - getOriginX();
                x *= getScaleX();
                float y = (verts[j + 1] + buildOffset.y + textureOffset.y) - getOriginY();
                y *= getScaleY();
                if (getRotation() != 0) {
                    float tempX = x;
                    float tempY = y;
                    float rotationInRads = getRotation() * MathUtils.degreesToRadians;
                    x = tempX * (float) Math.cos(rotationInRads) - tempY * (float) Math.sin(rotationInRads);
                    y = tempY * (float) Math.cos(rotationInRads) + tempX * (float) Math.sin(rotationInRads);
                }
                x += getX() + getOriginX();
                y += getY() + getOriginY();
                fullVerts[idx++] = x;
                fullVerts[idx++] = y;

                fullVerts[idx++] = color.toFloatBits();

                float inGridX = verts[j] - offsettedCol * gridWidth;
                float inGridY = verts[j + 1] - offsettedRow * gridHeight;
                float u = inGridX / gridWidth;
                float v = inGridY / gridHeight;
                if (u > 1.0f) u = 1.0f;
                if (v > 1.0f) v = 1.0f;
                if (u < 0.0f) u = 0.0f;
                if (v < 0.0f) v = 0.0f;
                // (col & 1 == 0) == true : col is even
                if (wrapTypeX == WrapType.REPEAT_MIRRORED & (col & 1) != 0) u = 1 - u;
                if (wrapTypeY == WrapType.REPEAT_MIRRORED & (row & 1) == 0) v = 1 - v;

                if (textureRegion != null) {
                    u = textureRegion.getU() + (textureRegion.getU2() - textureRegion.getU()) * u;
                    v = textureRegion.getV() + (textureRegion.getV2() - textureRegion.getV()) * v;
                } else {
                    u = whiteTextureRegion.getU() + (whiteTextureRegion.getU2() - whiteTextureRegion.getU()) * u;
                    v = whiteTextureRegion.getV() + (whiteTextureRegion.getV2() - whiteTextureRegion.getV()) * v;
                }
                fullVerts[idx++] = u;
                fullVerts[idx++] = v;
            }
            vertices.add(fullVerts);
        }

        this.textureDebugX = (buildOffset.x + textureOffset.x) * getScaleX();
        this.textureDebugY = (buildOffset.y + textureOffset.y) * getScaleY();
        if (getRotation() != 0) {
            float tempX = this.textureDebugX;
            float tempY = this.textureDebugY;
            float rotationInRads = getRotation() * MathUtils.degreesToRadians;
            this.textureDebugX = tempX * (float) Math.cos(rotationInRads) - tempY * (float) Math.sin(rotationInRads);
            this.textureDebugY = tempY * (float) Math.cos(rotationInRads) + tempX * (float) Math.sin(rotationInRads);
        }
        this.textureDebugX += getX();
        this.textureDebugY += getY();

        this.boundingRect = this.polygon.getBoundingRectangle();

        dirtyAttributes = false;
    }

    /**
     * This is a garbage, due to Intersector returning values slightly different then the grid values
     * Snapping exactly to grid is important, so that during bulidVertices method, it can be figured out
     * if points are on the wall of it's own grid box or not, to set u/v properly.
     * Any other implementations are welcome
     */
    private float[] snapToGrid(float[] vertices, int row, int col) {
        float[] resultVerts = vertices.clone();
        for (int i = 0; i < resultVerts.length; i += 2) {
            float inGridX = resultVerts[i] - col * gridWidth;
            float inGridY = resultVerts[i + 1] - row * gridHeight;
            float inGridXFraction = inGridX / gridWidth;
            float inGridYFraction = inGridY / gridHeight;
            if (inGridXFraction != 1 & inGridXFraction > 0.9999f) {
                resultVerts[i] = (col + 1) * gridWidth;
            } else if (inGridXFraction != 0 & inGridXFraction < 0.0001f) {
                resultVerts[i] = col * gridWidth;
            }
            if (inGridYFraction != 1 & inGridYFraction > 0.9999f) {
                resultVerts[i + 1] = (row + 1) * gridHeight;
            } else if (inGridYFraction != 0 & inGridYFraction < 0.0001f) {
                resultVerts[i + 1] = row * gridHeight;
            }
        }

        return resultVerts;
    }

    /**
     * Offsets polygon to 0 - textureOffset coordinate for ease of calculations, later this is put back on final render
     *
     * @param vertices vertices to offset
     * @return offsetted vertices
     */
    private float[] offset(float[] vertices) {
        float[] result = vertices.clone();

        Polygon polygon = new Polygon(result);
        Rectangle bounds = polygon.getBoundingRectangle();

        buildOffset.x = bounds.x;
        buildOffset.y = bounds.y;

        for (int i = 0; i < result.length; i += 2) {
            result[i] -= (buildOffset.x + textureOffset.x);
            result[i + 1] -= (buildOffset.y + textureOffset.y);
        }

        gridOffset.x = (int) Math.floor(-(textureOffset.x / gridWidth));
        gridOffset.y = (int) Math.floor(-(textureOffset.y / gridHeight));

        return result;
    }

    public void draw(PolygonSpriteBatch batch) {
        if (dirtyGrid || parts.size == 0) {
            prepareVertices();
        }
        if (dirtyAttributes || vertices.size == 0) {
            buildVertices();
        }

        Texture textureToDraw;
        if (textureRegion != null) {
            textureToDraw = textureRegion.getTexture();
        } else {
            textureToDraw = whiteTextureRegion.getTexture();
        }

        for (int i = 0; i < vertices.size; i++) {
            batch.draw(textureToDraw, vertices.get(i), 0, vertices.get(i).length, indices.get(i), 0, indices.get(i).length);
        }
    }

    public void drawDebug(ShapeRenderer shapes, Color color) {
        if (dirtyGrid) {
            prepareVertices();
        }
        if (dirtyAttributes) {
            buildVertices();
        }

        // draw grid
        for (int i = 0; i < vertices.size; i++) {

            // draw vertices in grid
            shapes.setColor(color);
            float[] curVerts = vertices.get(i);
            short[] curIndices = this.indices.get(i);
            for (int j = 0; j < curIndices.length; j += 3) {
                float x1 = curVerts[curIndices[j] * 5];
                float y1 = curVerts[curIndices[j] * 5 + 1];
                float x2 = curVerts[curIndices[j + 1] * 5];
                float y2 = curVerts[curIndices[j + 1] * 5 + 1];
                float x3 = curVerts[curIndices[j + 2] * 5];
                float y3 = curVerts[curIndices[j + 2] * 5 + 1];
                shapes.triangle(x1, y1, x2, y2, x3, y3);
            }
        }

        //draw cross on grid 0/0
        shapes.setColor(Color.RED);
        shapes.line(textureDebugX - 1, textureDebugY - 1,
                textureDebugX + 1, textureDebugY + 1);
        shapes.line(textureDebugX - 1, textureDebugY + 1,
                textureDebugX + 1, textureDebugY - 1);
    }

    @Override
    public void dispose() {
        whiteTextureRegion.getTexture().dispose();
        whiteTextureRegion = null;
        if (textureRegion != null) {
            textureRegion.getTexture().dispose();
            textureRegion = null;
        }
        polygon = null;
        boundingRect = null;
        parts.clear();
        parts = null;
        vertices.clear();
        vertices = null;
        indices.clear();
        indices = null;
        buildOffset = null;
        gridOffset = null;
        textureOffset = null;
    }

    /**
     * Sets the outline vertices of the polygon
     *
     * @param vertices - vertices of polygon relative to the origin
     */
    public void setVertices(float[] vertices) {
        if (vertices == null) {
            Polygon tempPolygon = new Polygon();
            tempPolygon.setPosition(getX(), getY());
            tempPolygon.setOrigin(getOriginX(), getOriginY());
            tempPolygon.setRotation(getRotation());
            tempPolygon.setScale(getScaleX(), getScaleY());
            polygon = tempPolygon;
        } else {
            polygon.setVertices(vertices);
        }
        dirtyGrid = true;
    }

    /**
     * Sets the texture region, the size of repeating grid is equal to region size
     *
     * @param textureRegion - texture region mapped on the polygon
     * @param wrapTypeX     - WrapType how the texture region is drawn along the X-Axis
     * @param wrapTypeY     - WrapType how the texture region is drawn along the Y-Axis
     */
    public void setTextureRegion(TextureRegion textureRegion, WrapType wrapTypeX, WrapType wrapTypeY) {
        setTextureRegion(textureRegion);
        setWrapTypeX(wrapTypeX);
        setWrapTypeY(wrapTypeY);
    }

    /**
     * Sets the texture region, the size of repeating grid is equal to region size
     *
     * @param textureRegion - texture region mapped on the polygon
     * @param textureWidth  - width of the repeating region
     * @param textureHeight - height of the repeating region
     */
    public void setTextureRegion(TextureRegion textureRegion, float textureWidth, float textureHeight) {
        setTextureRegion(textureRegion);
        setTextureSize(textureWidth, textureHeight);
    }

    /**
     * Sets the texture region, the size of repeating grid is equal to region size
     *
     * @param textureRegion - texture region mapped on the polygon
     * @param textureWidth  - width of the repeating region
     * @param textureHeight - height of the repeating region
     * @param wrapTypeX     - WrapType how the texture region is drawn along the X-Axis
     * @param wrapTypeY     - WrapType how the texture region is drawn along the Y-Axis
     */
    public void setTextureRegion(TextureRegion textureRegion, float textureWidth, float textureHeight, WrapType wrapTypeX, WrapType wrapTypeY) {
        setTextureRegion(textureRegion);
        setTextureSize(textureWidth, textureHeight);
        setWrapTypeX(wrapTypeX);
        setWrapTypeY(wrapTypeY);
    }

    /**
     * Sets the texture region, the size of repeating grid is equal to region size if not set already
     *
     * @param textureRegion - texture region mapped on the polygon
     */
    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
        if (this.textureWidth == 0) this.textureWidth = textureRegion.getRegionWidth();
        if (this.textureHeight == 0) this.textureHeight = textureRegion.getRegionHeight();
        dirtyGrid = true;
    }

    /**
     * Sets the position of the texture where 0 is the bottom left corner of the bounding rectangle
     */
    public void setTextureOffset(float x, float y) {
        this.textureOffset.set(x, y);
        dirtyGrid = true;
    }

    /**
     * Sets the to be drawn width and height of the texture
     */
    public void setTextureSize(float width, float height) {
        this.textureWidth = width;
        this.textureHeight = height;
        dirtyGrid = true;
    }

    /**
     * Sets the sprite's position in the world
     */
    public void setPosition(float x, float y) {
        polygon.setPosition(x, y);
        dirtyAttributes = true;
    }

    /**
     * Sets the sprite's x position in the world
     */
    public void setX(float x) {
        polygon.setPosition(x, getY());
        dirtyAttributes = true;
    }

    /**
     * Sets the sprite's y position in the world
     */
    public void setY(float y) {
        polygon.setPosition(getX(), y);
        dirtyAttributes = true;
    }

    /**
     * Sets the sprite's position in the world
     */
    public void translate(float x, float y) {
        polygon.setPosition(getX() + x, getY() + y);
        dirtyAttributes = true;
    }

    /**
     * Sets the origin in relation to the sprite's position for scaling and rotation.
     */
    public void setOrigin(float x, float y) {
        polygon.setOrigin(x, y);
        dirtyAttributes = true;
    }

    /**
     * Sets the origin x in relation to the sprite's position for scaling and rotation.
     */
    public void setOriginX(float x) {
        setOrigin(x, getOriginY());
    }

    /**
     * Sets the origin y in relation to the sprite's position for scaling and rotation.
     */
    public void setOriginY(float y) {
        setOrigin(getOriginX(), y);
    }

    /**
     * Sets the scale along both axises where 1 = normal Size
     */
    public void setScale(float scaleX, float scaleY) {
        polygon.setScale(scaleX, scaleY);
        dirtyAttributes = true;
    }

    /**
     * Sets the scale along the x axis where 1 = normal Size
     */
    public void setScaleX(float scaleX) {
        polygon.setScale(scaleX, getScaleY());
        dirtyAttributes = true;
    }

    /**
     * Sets the scale along the y axis where 1 = normal Size
     */
    public void setScaleY(float scaleY) {
        polygon.setScale(getScaleX(), scaleY);
        dirtyAttributes = true;
    }

    /**
     * Adds the specified scale to the current scale.
     */
    public void scaleBy(float scaleXY) {
        polygon.scale(scaleXY);
        dirtyAttributes = true;
    }

    /**
     * Adds the specified scale to the current scale.
     */
    public void scaleBy(float scaleX, float scaleY) {
        polygon.setScale(getScaleX() + scaleX, getScaleY() + scaleY);
        dirtyAttributes = true;
    }

    public void setRotation(float degrees) {
        polygon.setRotation(degrees);
        dirtyAttributes = true;
    }

    /**
     * Adds the specified rotation to the current rotation.
     */
    public void rotateBy(float amountInDegrees) {
        polygon.rotate(amountInDegrees);
        dirtyAttributes = true;
    }

    /**
     * Sets the type how the texture region is drawn along the X-Axis
     *
     * @param wrapType - a type of WrapType
     */
    public void setWrapTypeX(WrapType wrapType) {
        this.wrapTypeX = wrapType;
        dirtyGrid = true;
    }

    /**
     * Sets the type how the texture region is drawn along the Y-Axis
     *
     * @param wrapType - a type of WrapType
     */
    public void setWrapTypeY(WrapType wrapType) {
        this.wrapTypeY = wrapType;
        dirtyGrid = true;
    }

    /**
     * @param color - Tint color to be applied to entire polygon
     */
    public void setColor(Color color) {
        this.color.set(color);
        dirtyAttributes = true;
    }

    public WrapType getWrapTypeX() {
        return wrapTypeX;
    }

    public WrapType getWrapTypeY() {
        return wrapTypeY;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public float getTextureOffsetX() {
        return textureOffset.x;
    }

    public float getTextureOffsetY() {
        return textureOffset.y;
    }

    public float getTextureWidth() {
        return textureWidth;
    }

    public float getTextureHeight() {
        return textureHeight;
    }

    public float getX() {
        return polygon.getX();
    }

    public float getY() {
        return polygon.getY();
    }

    public float getOriginX() {
        return polygon.getOriginX();
    }

    public float getOriginY() {
        return polygon.getOriginY();
    }

    public float getScaleX() {
        return polygon.getScaleX();
    }

    public float getScaleY() {
        return polygon.getScaleY();
    }

    public float getRotation() {
        return polygon.getRotation();
    }

    public Color getColor() {
        return color;
    }

    /**
     * Returns the packed vertices, colors, and texture coordinates for this sprite.
     */
    public Array<float[]> getVertices() {
        return vertices;
    }

    public float[] getOriginalVertices() {
        return polygon.getVertices().clone();
    }

    public float[] getTransformedVertices() {
        return polygon.getTransformedVertices().clone();
    }

    public Polygon getPolygon() {
        Polygon tempPolygon = new Polygon();
        if (getOriginalVertices().length != 0) tempPolygon.setVertices(getOriginalVertices());
        tempPolygon.setPosition(getX(), getY());
        tempPolygon.setOrigin(getOriginX(), getOriginY());
        tempPolygon.setRotation(getRotation());
        tempPolygon.setScale(getScaleX(), getScaleY());
        return tempPolygon;
    }

    public Rectangle getBoundingRectangle() {
        return this.boundingRect;
    }
}

