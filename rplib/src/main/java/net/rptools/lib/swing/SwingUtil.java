/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import net.rptools.lib.image.ImageUtil;

/**
 */
public class SwingUtil {
	public static Cursor emptyCursor;

	static {
		try {
			emptyCursor = Toolkit.getDefaultToolkit().createCustomCursor(ImageUtil.getImage("net/rptools/lib/swing/image/empty.png"), new Point(0, 0), "");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void useAntiAliasing(JComponent component) {
		component.putClientProperty("AATextPropertyKey", true);
	}

	/**
	 * Tell the Graphics2D to use anti-aliased drawing and text.
	 * 
	 * @return old AA
	 */
	public static Object useAntiAliasing(Graphics2D g) {
		Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return oldAA;
	}

	/**
	 * Used after useAntiAliasing
	 * 
	 * @param oldAA
	 *            the value returned from useAntiAliasing
	 */
	public static void restoreAntiAliasing(Graphics2D g, Object oldAA) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
	}

	public static void centerOnScreen(Window window) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = window.getSize();

		int x = (screenSize.width - windowSize.width) / 2;
		int y = (screenSize.height - windowSize.height) / 2;

		window.setLocation(x, y);
	}

	public static boolean isControlDown(InputEvent e) {
		return isControlDown(e.getModifiersEx());
	}

	/**
	 * Passed the event's extended modifiers this method returns <code>true</code> if the Control key is down.
	 * 
	 * @param modifiers
	 *            as returned by {@link InputEvent#getModifiersEx()}
	 * @return <code>true</code> if Control key is down
	 */
	public static boolean isControlDown(int modifiers) {
		return (modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK;
	}

	public static boolean isShiftDown(InputEvent e) {
		return isShiftDown(e.getModifiersEx());
	}

	/**
	 * Passed the event's extended modifiers this method returns <code>true</code> if the Shift key is down.
	 * 
	 * @param modifiers
	 *            as returned by {@link InputEvent#getModifiersEx()}
	 * @return <code>true</code> if Shift key is down
	 */
	public static boolean isShiftDown(int modifiers) {
		return (modifiers & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK;
	}

	/**
	 * Centers the innerWindow over the outerWindow. Basically, this method finds the centerpoint of the
	 * <code>outerWindow</code> and sets the location of <code>innerWindow</code> so that it's center is coincident with
	 * <code>outerWindow</code>'s center.
	 * 
	 * @param innerWindow
	 *            window to move
	 * @param outerWindow
	 *            window to be centered over
	 */
	public static void centerOver(Window innerWindow, Window outerWindow) {
		Dimension innerSize = innerWindow.getSize();
		Dimension outerSize = outerWindow.getSize();

		int x = outerWindow.getLocation().x + (outerSize.width - innerSize.width) / 2;
		int y = outerWindow.getLocation().y + (outerSize.height - innerSize.height) / 2;

		// Jamz: For multiple monitor's, x & y can be negative values...
		// innerWindow.setLocation(x < 0 ? 0 : x, y < 0 ? 0 : y);
		innerWindow.setLocation(x, y);
	}

	public static void constrainTo(Dimension dim, int size) {
		boolean widthBigger = dim.width > dim.height;

		if (widthBigger) {
			dim.height = (int) ((dim.height / (double) dim.width) * size);
			dim.width = size;
		} else {
			dim.width = (int) ((dim.width / (double) dim.height) * size);
			dim.height = size;
		}
	}

	public static void constrainTo(Dimension dim, int width, int height) {
		boolean widthBigger = dim.width > dim.height;

		constrainTo(dim, widthBigger ? width : height);

		if ((widthBigger && dim.height > height) || (!widthBigger && dim.width > width)) {
			int size = (int) Math.round(widthBigger ? (height / (double) dim.height) * width : (width / (double) dim.width) * dim.height);
			constrainTo(dim, size);
		}
	}

	/**
	 * Don't show the mouse pointer for this component
	 * 
	 * @param component
	 */
	public static void hidePointer(Component component) {
		component.setCursor(emptyCursor);
	}

	/**
	 * Set the mouse pointer for this component to the default system cursor
	 * 
	 * @param component
	 */
	public static void showPointer(Component component) {
		component.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * This method adds the given MouseListener to all components within the given container and all child containers.
	 * 
	 * @param c
	 *            top component to start with
	 * @param listener
	 *            MouseListener to add
	 */
	public static void addMouseListenerToHierarchy(JComponent c, MouseListener listener) {
		for (Component comp : c.getComponents()) {
			comp.addMouseListener(listener);

			if (comp instanceof JComponent) {
				addMouseListenerToHierarchy((JComponent) comp, listener);
			}
		}
	}

	/**
	 * Removes the given MouseListener from all components in a hierarchy.
	 * 
	 * @param c
	 *            top component to start with
	 * @param listener
	 *            MouseListener to remove
	 */
	public static void removeMouseListenerToHierarchy(JComponent c, MouseListener listener) {
		for (Component comp : c.getComponents()) {
			comp.removeMouseListener(listener);

			if (comp instanceof JComponent) {
				removeMouseListenerToHierarchy((JComponent) comp, listener);
			}
		}
	}

	public static BufferedImage takeScreenShot(Component component, String... watermarks) {
		Dimension size = component.getSize();

		BufferedImage screenshot = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
		Graphics2D g = screenshot.createGraphics();
		g.setClip(0, 0, size.width - 1, size.height - 1);

		component.update(g);

		FontMetrics fm = g.getFontMetrics();
		int y = fm.getDescent();
		for (String watermark : watermarks) {
			if (watermark != null) {
				int x = size.width - SwingUtilities.computeStringWidth(fm, watermark);

				g.setColor(Color.black);
				g.drawString(watermark, x, y);

				g.setColor(Color.white);
				g.drawString(watermark, x - 1, y - 1);

				y -= fm.getHeight();
			}
		}
		g.dispose();
		return screenshot;
	}

	public static BufferedImage replaceColor(BufferedImage src, int sourceRGB, int replaceRGB) {
		for (int y = 0; y < src.getHeight(); y++) {
			for (int x = 0; x < src.getWidth(); x++) {
				int rawRGB = src.getRGB(x, y);
				int rgb = rawRGB & 0xffffff;
				int alpha = rawRGB & 0xff000000;

				if (rgb == sourceRGB) {
					src.setRGB(x, y, alpha | replaceRGB);
				}
			}
		}
		return src;
	}

	public static Rectangle flip(Dimension view, Rectangle rect, int direction) {
		boolean flipHorizontal = (direction & 1) == 1;
		boolean flipVertical = (direction & 2) == 2;

		int x = flipHorizontal ? view.width - (rect.x + rect.width) : rect.x;
		int y = flipVertical ? view.height - (rect.y + rect.height) : rect.y;

		//		System.out.println(rect + " - " + new Rectangle(x, y, rect.width, rect.height));
		return new Rectangle(x, y, rect.width, rect.height);
	}

	public static JComponent getComponent(JComponent container, String name) {
		assert name != null;

		List<Component> componentQueue = new LinkedList<Component>();
		componentQueue.add(container);
		while (!componentQueue.isEmpty()) {
			Component c = componentQueue.remove(0);
			if (name.equals(c.getName())) {
				return (JComponent) c;
			}
			if (c instanceof Container) {
				for (Component child : ((Container) c).getComponents()) {
					componentQueue.add(child);
				}
			}
		}
		return null;
	}

	public static boolean hasComponent(JComponent container, String name) {
		return getComponent(container, name) != null;
	}
}
