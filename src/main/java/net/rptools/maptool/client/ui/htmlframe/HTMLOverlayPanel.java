/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.DefaultTool;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.ui.AppMenuBar;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Represents the JFXPanel that will contains the map overlays. */
public class HTMLOverlayPanel extends JFXPanel {
  enum mousePassResult {
    BLOCK,
    PASS,
    CHECK_OPACITY
  }

  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLOverlayManager.class);

  /** The ordered set of the overlays. Ordered by Z order and then by name. */
  private final ConcurrentSkipListSet<HTMLOverlayManager> overlays =
      new ConcurrentSkipListSet<>(
          Comparator.comparingInt(HTMLOverlayManager::getZOrder)
              .thenComparing(HTMLOverlayManager::getName));

  /** The StackPane holding all the overlays. */
  private StackPane root;

  /** The Region used to catch the clicks on the overlays. */
  private Region front;

  /** Creates a new HTMLJFXPanel. */
  public HTMLOverlayPanel() {
    super();
    addMouseListeners(); // mouse listeners to transmit to the ZR
    setBackground(new Color(0, 0, 0, 0)); // transparent overlay

    Platform.runLater(this::setupScene);
    setVisible(false); // disabled by default
  }

  /** Setups the scene of the JFXPanel. */
  void setupScene() {
    front = new Region();
    front.setBackground(Background.EMPTY);
    front.setPickOnBounds(true); // catches the clicks
    front.addEventFilter(
        javafx.scene.input.MouseEvent.ANY,
        event -> {
          // Passes the mouse event to all overlays
          for (HTMLOverlayManager overlay : overlays) {
            if (overlay.isVisible()) {
              overlay.getWebView().fireEvent(event);
            }
          }
        });

    // In JavaFX mousewheel events are not included in MouseEvent.ANY but in ScrollEvent.ANY, add a
    // separate event filter for those to make sure these events reach the Webview
    front.addEventFilter(
        ScrollEvent.ANY,
        event -> {
          // Passes the mouse event to all overlays
          for (HTMLOverlayManager overlay : overlays) {
            if (overlay.isVisible()) {
              overlay.getWebView().fireEvent(event);
            }
          }
        });

    root = new StackPane(front);
    root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);"); // set stackpane transparent

    Scene scene = new Scene(root);
    scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // set scene transparent
    this.setScene(scene);
  }

  /**
   * @return a cloned set of the overlays.
   */
  public ConcurrentSkipListSet<HTMLOverlayManager> getOverlays() {
    return overlays.clone();
  }

  /**
   * Returns the overlay associated with the name.
   *
   * @param name the name of the overlay
   * @return the overlay, or null if not existing
   */
  public HTMLOverlayManager getOverlay(String name) {
    for (HTMLOverlayManager overlay : overlays) {
      if (overlay.getName().equals(name)) {
        return overlay;
      }
    }
    return null;
  }

  /**
   * Returns whether the overlay exists.
   *
   * @param name the name of the overlay
   * @return true if it exists, false otherwise
   */
  public boolean isRegistered(String name) {
    return getOverlay(name) != null;
  }

  /**
   * Sets the overlay cursor to a JavaFX cursor.
   *
   * @param cursor the cursor to set
   */
  public void setOverlayCursor(Cursor cursor) {
    front.setCursor(cursor);
  }

  /**
   * Sets the overlay cursor to a Swing cursor.
   *
   * @param cursor the cursor to set
   */
  public void setOverlayCursor(java.awt.Cursor cursor) {
    front.setCursor(SwingUtil.swingCursorToFX(cursor));
  }

  /**
   * Runs the javascript on an overlay.
   *
   * @param name the name of the overlay
   * @param script the script to run
   * @return true if the overlay is found, and false otherwise
   */
  public boolean runScript(String name, String script) {
    HTMLOverlayManager manager = getOverlay(name);
    if (manager == null) {
      return false;
    }
    Platform.runLater(() -> manager.getWebEngine().executeScript(script));
    return true;
  }

  /**
   * @return whether all overlay WebViews have the default cursor.
   */
  public boolean areWebViewCursorsDefault() {
    for (HTMLOverlayManager overlay : overlays) {
      if (overlay.isVisible()) {
        Cursor cursor = overlay.getWebView().getCursor();
        if (cursor == null || !"DEFAULT".equals(cursor.toString())) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Removes one overlay. Executed on the JavaFX Application thread.
   *
   * @param name The name of the overlay.
   */
  public void removeOverlay(String name) {
    Platform.runLater(() -> removeOverlay(getOverlay(name)));
  }

  /**
   * Removes an overlay.
   *
   * @param overlay the overlay to remove
   */
  private void removeOverlay(HTMLOverlayManager overlay) {
    if (overlay != null) {
      root.getChildren().remove(overlay.getWebView());
      overlays.remove(overlay);
      AppMenuBar.removeFromOverlayMenu(overlay.getName());
      overlay.flush();
      if (overlays.isEmpty()) {
        setVisible(false); // hide overlay panel if all are gone
      }
    }
  }

  /** Removes all overlays. */
  public void removeAllOverlays() {
    this.setVisible(false);
    Platform.runLater(
        () -> {
          ObservableList<Node> listChildren = root.getChildren();
          for (HTMLOverlayManager overlay : overlays) {
            listChildren.remove(overlay.getWebView());
            AppMenuBar.removeFromOverlayMenu(overlay.getName());
            overlay.flush();
          }
          overlays.clear();
          setVisible(false);
        });
  }

  /**
   * Shows an overlay.
   *
   * @param name the name of the overlay
   * @param zOrder the zOrder of the overlay
   * @param html the HTML of the overlay
   */
  public void showOverlay(String name, int zOrder, String html, Object frameValue) {
    getDropTarget().setActive(false); // disables drop on overlay, drop goes to map
    setVisible(true);
    Platform.runLater(
        () -> {
          boolean needsSorting = false;
          HTMLOverlayManager overlayManager = getOverlay(name);
          if (overlayManager != null) {
            if ("".equals(html)) {
              // Blank removes the overlay
              removeOverlay(overlayManager);
              return;
            } else if (zOrder != overlayManager.getZOrder()) {
              // Resorts by removing and adding back the overlay
              overlays.remove(overlayManager);
              overlayManager.setZOrder(zOrder);
              overlays.add(overlayManager);
              needsSorting = true;
            }
          } else {
            overlayManager = new HTMLOverlayManager(name, zOrder);
            overlayManager.setupWebView(new WebView());
            overlays.add(overlayManager);
            root.getChildren().add(overlayManager.getWebView());
            if (!HTMLFrameFactory.isInternalOnly(overlayManager.getName())) {
              AppMenuBar.addToOverlayMenu(overlayManager);
              needsSorting = true;
            }
          }
          if (needsSorting) {
            sortOverlays();
          }
          overlayManager.updateContents(html, true);
          if (frameValue != null) {
            overlayManager.setValue(frameValue);
          }
        });
  }

  /** Display the overlays according to their zOrder. */
  private void sortOverlays() {
    overlays.forEach(overlay -> overlay.getWebView().toFront());
    front.toFront();
  }

  /**
   * Determines if the mouse event should be forwarded, and if so dispatch it to the ZoneRenderer.
   *
   * @param e the mouse event
   */
  private void mayPassClick(MouseEvent e) {
    Platform.runLater(
        () -> {
          // get the result based on the most restrictive CSS of all overlays
          mousePassResult result = getMousePassResult(e);
          if (result != mousePassResult.BLOCK) {
            SwingUtilities.invokeLater(
                () -> {
                  if (result == mousePassResult.PASS || !isOpaque(e.getX(), e.getY())) {
                    passMouseEvent(e);
                  }
                });
          }
        });
  }

  /**
   * Returns true if one overlay blocks the mouse event, false otherwise.
   *
   * @param e the mouse event
   * @return whether the mouse event should be blocked
   */
  private mousePassResult getMousePassResult(MouseEvent e) {
    mousePassResult globalResult = mousePassResult.PASS;
    for (HTMLOverlayManager overlay : overlays) {
      mousePassResult result = overlay.getMousePassResult(e.getX(), e.getY());
      if (result == mousePassResult.BLOCK) {
        globalResult = mousePassResult.BLOCK;
        break;
      } else if (result == mousePassResult.CHECK_OPACITY) {
        globalResult = mousePassResult.CHECK_OPACITY;
      }
    }
    return globalResult;
  }

  /**
   * Returns whether the overlay is opaque (alpha > 0) at the x,y pixel. Method provided by gthanop
   * at https://stackoverflow.com/questions/60906929
   *
   * @param x the x coordinate of the pixel
   * @param y the y coordinate of the pixel
   * @return true if alpha isn't 0, false if it is
   */
  private boolean isOpaque(int x, int y) {
    if (!getBounds().contains(x, y)) return false; // no overlay outside the bounds
    final BufferedImage bimg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2d = bimg.createGraphics();
    g2d.translate(-x, -y); // pixel of interest is now at 0,0
    printAll(g2d); // draw a 1,1 pixel at 0,0
    g2d.dispose();
    Color c = new Color(bimg.getRGB(0, 0), true);
    bimg.flush();
    return c.getAlpha() != 0;
  }

  /**
   * Add the mouse listeners to forward the mouse events to the current ZoneRenderer. Clicks and
   * mouse press get validated first to see if they need forwarding.
   */
  private void addMouseListeners() {
    addMouseWheelListener(this::passMouseEvent);
    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseDragged(MouseEvent e) {
            passMouseEvent(e);
          }
        });
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            mayPassClick(e);
          }

          @Override
          public void mousePressed(MouseEvent e) {
            maySetMapDragStart(e); // may set map dragstart x and y, even if on overlay
            mayPassClick(e);
            e.consume(); // workaround for java bug JDK-8200224
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            passMouseEvent(e);
          }
        });
  }

  /**
   * Passes a mouse event to the ZoneRenderer.
   *
   * @param e the mouse event to forward
   */
  void passMouseEvent(MouseEvent e) {
    Component c = MapTool.getFrame().getCurrentZoneRenderer();
    if (c != null) {
      c.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, c));
    }
  }

  /**
   * Sets up the initial drag start. If the mouse press is a right click and the tool could be
   * dragging the map, sets up the initial drag start. This is required or the map will "jump" if
   * performing a right click on the overlay followed by a drag.
   *
   * @param e the mouse press event
   */
  private void maySetMapDragStart(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
      if (tool instanceof DefaultTool) {
        ((DefaultTool) tool).setDragStart(e.getX(), e.getY());
      }
    }
  }

  /**
   * Run all callback macros for "onTokenChanged".
   *
   * @param token the token that have changed
   */
  public void doTokenChanged(Token token) {
    for (HTMLOverlayManager overlay : overlays) {
      if (overlay.getWebView().isVisible()) {
        HTMLPanelContainer.tokenChanged(token, overlay.macroCallbacks());
      }
    }
  }

  /** Run all callback macros for "onChangeImpersonated". */
  public void doImpersonatedChanged() {
    for (HTMLOverlayManager overlay : overlays) {
      if (overlay.getWebView().isVisible()) {
        HTMLPanelContainer.impersonatedChanged(overlay.macroCallbacks());
      }
    }
  }

  /** Run all callback macros for "onChangeSelection". */
  public void doSelectedChanged() {
    for (HTMLOverlayManager overlay : overlays) {
      if (overlay.getWebView().isVisible()) {
        HTMLPanelContainer.selectedChanged(overlay.macroCallbacks());
      }
    }
  }
}
