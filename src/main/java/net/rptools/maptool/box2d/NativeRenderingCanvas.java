package net.rptools.maptool.box2d;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Input;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;


/**
 * A native rendering canvas. The assumption is that some native renderer
 * produces an image provided as an IntBuffer or ByteBuffer. The PixelFormats
 * must be IntArgbPre or ByteBgraPre respectively. For the API see NativeRenderer.
 * <p>
 * This buffer is then used to create an Image which is bound to an ImageView.
 * This class manages the direct display of this Image in a Pane and reacts to
 * user input via mouse input or gestures on touch devices.
 * <p>
 * TODOs:
 * - Implement further user actions.
 * - Handle different render scales.
 * - Packaging of native part into jar file.
 *
 * @author Michael Paus
 */
public class NativeRenderingCanvas {
    private final ObjectProperty<WritableImage> fxImage;
    private final ImageView imageView;
    private final Pane canvasPane;
    private final NativeRenderer nativeRenderer;
    private final ChangeListener<? super Bounds> resizeListener;

    private ByteBuffer oldRawByteBuffer;
    private PixelBuffer<ByteBuffer> pixelBuffer;

    private int width;
    private int height;


    /**
     * Create and initialize a NativeRenderingCanvas instance.
     * @param dummyCanvas
     */
    public NativeRenderingCanvas(Canvas dummyCanvas) {
        nativeRenderer = new NativeRenderer(dummyCanvas, this);
        canvasPane = new Pane();

        fxImage = new SimpleObjectProperty<>();

        imageView = new ImageView();

        imageView.imageProperty().bind(fxImage);
        imageView.fitWidthProperty().bind(canvasPane.widthProperty());
        imageView.fitHeightProperty().bind(canvasPane.heightProperty());
        //imageView.setPreserveRatio(true);
        //imageView.setPickOnBounds(true);

        canvasPane.getChildren().add(imageView);


        resizeListener = (v, o, n) -> {
            width = (int) n.getWidth();
            height = (int) n.getHeight();
            if(width <= 0 || height <= 0)
                return;;

            nativeRenderer.createCanvas(width, height);

        };

        init();
    }

    public void keyPressed(KeyEvent e) {
        int newcode =  switch (e.getCode()) {
            default -> e.getCode().getCode();
            case F1 -> Input.Keys.F1;
            case F2 -> Input.Keys.F2;
            case F3 -> Input.Keys.F3;
            case F4 -> Input.Keys.F4;
            case F5 -> Input.Keys.F5;
            case F6 -> Input.Keys.F6;
            case F7 -> Input.Keys.F7;
            case F8 -> Input.Keys.F8;
            case F9 -> Input.Keys.F9;
            case F10 -> Input.Keys.F10;
            case F11 -> Input.Keys.F11;
            case F12 -> Input.Keys.F12;
        };

        if(e.getCode() == KeyCode.ESCAPE)
            nativeRenderer.pause();

        nativeRenderer.keyDown(newcode);
    }

    /**
     * Must be called before the NativeRenderingCanvas can be used again after dispose() has been called.
     */
    public void init() {
        canvasPane.boundsInLocalProperty().addListener(resizeListener);
        //nativeRenderer.init();

        imageView.setOnMouseMoved(e -> {
            if (!e.isSynthesized()) {
                nativeRenderer.mouseMoved((int)e.getX(), (int)e.getY());
                e.consume();
            }
        });

        imageView.setOnKeyPressed(e -> {
            nativeRenderer.keyDown(e.getCode().getCode());
            e.consume();
        });;

        imageView.setOnMouseReleased(e -> {
            nativeRenderer.touchUp((int)e.getX(), (int)e.getY(), 0, 0);
            e.consume();
        });

        imageView.setOnMousePressed(e -> {
            nativeRenderer.touchDown((int)e.getX(), (int)e.getY(), 0, 0);
        });

        imageView.setOnMouseDragged(e -> {
            nativeRenderer.touchDragged((int)e.getX(), (int)e.getY(), 0);
            e.consume();
        });

    }

    /**
     * Dispose all resources and disable all actions. Init() has to be called
     * before the NativeRenderingCanvas instance can be used again.
     */
    public void dispose() {

        canvasPane.boundsInLocalProperty().removeListener(resizeListener);

        imageView.setOnMouseClicked(null);
        imageView.setOnMousePressed(null);
        imageView.setOnMouseReleased(null);
        imageView.setOnMouseDragged(null);
        imageView.setOnScrollStarted(null);
        imageView.setOnScrollFinished(null);
        imageView.setOnScroll(null);
        imageView.setOnZoom(null);
        imageView.setOnRotate(null);

        fxImage.set(null);
        nativeRenderer.dispose();
    }

    /**
     * Return the root node of the NativeRenderingCanvas which can be directly
     * added to some layout-pane.
     *
     * @return the root node of the NativeRenderingCanvas.
     */
    public Node getRoot() {
        return canvasPane;
    }

    // Must be called on JavaFX application thread.
    public void renderUpdate(ByteBuffer renderBuffer, int bufferIndex, int width, int height) {
        assert Platform.isFxApplicationThread() : "Not called on JavaFX application thread.";

        if(this.width != width || this.height != height || renderBuffer.capacity() != width * height * NativeRenderer.BufferCount * NativeRenderer.BytePerInt)
            return;

        if (renderBuffer != oldRawByteBuffer) {
            oldRawByteBuffer = renderBuffer;
            pixelBuffer = new PixelBuffer<>(width, NativeRenderer.BufferCount * height, renderBuffer, PixelFormat.getByteBgraPreInstance());
            fxImage.set(new WritableImage(pixelBuffer));
        }
        pixelBuffer.updateBuffer(pb -> {
            final Rectangle2D renderedFrame = new Rectangle2D(
                    0,
                    bufferIndex * height,
                    Math.min(canvasPane.getWidth(), width),
                    Math.min(canvasPane.getHeight(), height));
            imageView.setViewport(renderedFrame);
            return renderedFrame;
        });
    }
}