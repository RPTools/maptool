package net.rptools.maptool.box2d;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import javafx.application.Platform;
import net.rptools.maptool.client.ui.zone.GdxRenderer;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;

public class NativeRenderer implements InputProcessor {
    public static final int BufferCount = 2;
    public static final int BytePerInt = 4;
    private ByteBuffer gdxBuffer;
    private RendererConfig oldConfig;
    private RendererConfig newConfig;
    private InputProcessor gdxInput;
    private Canvas canvas;
    private LwjglApplication app;
    private NativeRenderingCanvas nativeCanvas;
    public NativeRenderer(Canvas dummyCanvas, NativeRenderingCanvas nativeRenderingCanvas) {
        canvas = dummyCanvas;
        nativeCanvas = nativeRenderingCanvas;
    }

    public void setGdxBuffer(ByteBuffer buffer) {
        gdxBuffer = buffer;
        fixColors();
        flip();

        var bufferIndex = render();
        Platform.runLater(() -> nativeCanvas.renderUpdate(oldConfig.renderBuffer, bufferIndex, oldConfig.width, oldConfig.height));
        checkSizeChange();
    }

    private void checkSizeChange() {
        if (newConfig == oldConfig)
            return;

        oldConfig = newConfig;

        SwingUtilities.invokeLater(() -> canvas.setSize(oldConfig.width, oldConfig.height));
    }

    // on mac this must be called when the canvas is isDisplayable
    public void init() {
        if (app != null)
            return;

        checkSizeChange();

        var config = new LwjglApplicationConfiguration();
        config.allowSoftwareMode = true;
        config.samples = 8;
        config.forceExit = false;
        LwjglApplicationConfiguration.disableAudio = true;

        var appListender = GdxRenderer.getInstance();
        appListender.setJfxRenderer(this);
        //gdxInput = box;

        app = new LwjglApplication(appListender, config, canvas);
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }

    public void dispose() {
        app.stop();
    }

    public void createCanvas(int width, int height) {
        var cfg = new RendererConfig();

        cfg.width = width;
        cfg.height = height;
        cfg.currentBufferIndex = 0;

        cfg.singleBufferSize = width * height * BytePerInt;

        cfg.renderBuffer = ByteBuffer.allocate(cfg.singleBufferSize * BufferCount);
        cfg.flippedBuffer = new byte[cfg.singleBufferSize];
        newConfig = cfg;

        init();
    }

    // Canvas creation and rendering:

    private void fixColors() {
        //fix colors RGBA -> BGRA
        if (gdxBuffer != null) {
            for (int i = 0; i < gdxBuffer.capacity(); i += BytePerInt) {
                var red = gdxBuffer.get(i);
                gdxBuffer.put(i, gdxBuffer.get(i + 2));
                gdxBuffer.put(i + 2, red);
                // set alpha to 1
                gdxBuffer.put(i+3, (byte)0xff);
            }
        }
    }

    private void flip() {
        if (oldConfig.flippedBuffer.length != gdxBuffer.capacity())
            return;

        int numBytesPerLine = oldConfig.width * BytePerInt;
        for (int i = 0; i < oldConfig.height; i++) {
            gdxBuffer.position((oldConfig.height - i - 1) * numBytesPerLine);
            gdxBuffer.get(oldConfig.flippedBuffer, i * numBytesPerLine, numBytesPerLine);
        }
    }

    public int render() {
        oldConfig.currentBufferIndex += 1;
        if (oldConfig.currentBufferIndex >= BufferCount) oldConfig.currentBufferIndex = 0;

        if (oldConfig.flippedBuffer != null && oldConfig.renderBuffer.capacity() == oldConfig.flippedBuffer.length * BufferCount
                && oldConfig.flippedBuffer.length == oldConfig.singleBufferSize) {

            oldConfig.renderBuffer.position(oldConfig.currentBufferIndex * oldConfig.singleBufferSize);
            oldConfig.renderBuffer.put(oldConfig.flippedBuffer);
            oldConfig.renderBuffer.rewind();
        }


        return oldConfig.currentBufferIndex;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (gdxInput == null)
            return false;
        return gdxInput.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (gdxInput == null)
            return false;
        return gdxInput.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        if (gdxInput == null)
            return false;
        return gdxInput.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (gdxInput == null)
            return false;
        return gdxInput.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (gdxInput == null)
            return false;
        return gdxInput.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (gdxInput == null)
            return false;
        return gdxInput.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (gdxInput == null)
            return false;
        return gdxInput.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (gdxInput == null)
            return false;
        return gdxInput.scrolled(amountX, amountY);
    }

    public void pause() {
        app.stop();
    }

    private class RendererConfig {
        public int width;
        public int height;
        public int currentBufferIndex;
        public int singleBufferSize;
        public ByteBuffer renderBuffer;
        byte[] flippedBuffer;
    }
}