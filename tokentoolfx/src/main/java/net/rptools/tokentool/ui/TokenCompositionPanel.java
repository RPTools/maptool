/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;

import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.PositionalLayout;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.transferable.FileTransferableHandler;
import net.rptools.lib.transferable.ImageTransferableHandler;
import net.rptools.tokentool.AppState;
import net.rptools.tokentool.TokenCompositor;
import net.rptools.tokentool.TokenTool;

public class TokenCompositionPanel extends JPanel implements
		DropTargetListener, MouseListener, MouseMotionListener,
		MouseWheelListener, KeyListener {

	private BufferedImage overlayImage;
	private BufferedImage tokenImage;

	private int tokenOffsetX;

	private int tokenOffsetY;

	private int dragStartX;

	private int dragStartY;

	private Rectangle overlayBounds;

	private double tokenScale;

	private BufferedImage composedOverlayImage;

	private ChangeObservable changeObservers;

	// AppActions.SAVE_TOKEN needs to know these values
	// so it can set the token metadata correctly.

	public int getTokenOffsetX() {
		return tokenOffsetX;
	}

	public int getTokenOffsetY() {
		return tokenOffsetY;
	}

	public int getOverlayWidth() {
		return (int) overlayBounds.getWidth();
	}

	public int getOverlayHeight() {
		return (int) overlayBounds.getHeight();
	}

	public TokenCompositionPanel() {
		setLayout(new PositionalLayout());
		setFocusable(true);

		// DnD
		new DropTarget(this, this);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);

		changeObservers = new ChangeObservable();
	}

	public void addChangeObserver(Observer observer) {
		changeObservers.addObserver(observer);
	}

	public void removeChangeObserver(Observer observer) {
		changeObservers.deleteObserver(observer);
	}

	private void paintOverlay(Graphics g, Dimension size) {
		if (composedOverlayImage == null) {
			composedOverlayImage = TokenCompositor.translateOverlay(
					overlayImage, 1);
			fireCompositionChanged();
		}
		if (composedOverlayImage != null) {
			int width = (Integer) TokenTool.getFrame().getControlPanel()
					.getWidthSpinner().getValue();
			int height = (Integer) TokenTool.getFrame().getControlPanel()
					.getHeightSpinner().getValue();
			int x = (size.width - width) / 2;
			int y = (size.height - height) / 2;

			if (overlayBounds != null) {
				if (overlayBounds.width != width
						|| overlayBounds.height != height) {
					fireCompositionChanged();
				}
			}

			g.drawImage(composedOverlayImage, x, y, width, height, this);

			overlayBounds = new Rectangle(x, y, width, height);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {

		Dimension size = getSize();
		g.setColor(Color.black);
		g.fillRect(0, 0, size.width, size.height);

		int messageY = 15;
		int messageX = 5;

		// BASE
		if (AppState.compositionProperties.isBase()) {
			paintOverlay(g, size);
		}

		// TOKEN
		if (tokenImage != null) {
			int width = (int) (tokenImage.getWidth() * tokenScale);
			int height = (int) (tokenImage.getHeight() * tokenScale);
			g.drawImage(tokenImage, tokenOffsetX, tokenOffsetY, width, height,
					this);
		} else {
			g.setColor(Color.white);
			g.drawString("Drag an image onto this pane", messageX, messageY);
			messageY += 15;
		}

		if (!AppState.compositionProperties.isBase()) {
			paintOverlay(g, size);
		}
	}

	public void setToken(BufferedImage tokenImage) {
		this.tokenImage = tokenImage;
		tokenOffsetX = 0;
		tokenOffsetY = 0;

		tokenScale = 1;

		repaint();
		fireCompositionChanged();
	}

	public BufferedImage getBaseImage() {
		return tokenImage;
	}

	public void setOverlay(BufferedImage overlayImage) {
		this.overlayImage = overlayImage;
		composedOverlayImage = null;
		repaint();
		fireCompositionChanged();
	}

	public void zoomIn() {
		setScale(tokenScale + .01);
	}

	public void zoomInFast() {
		setScale(tokenScale + .1);
	}

	public void zoomOut() {
		setScale(tokenScale - .01);
	}

	public void zoomOutFast() {
		setScale(tokenScale - .1);
	}

	public void setScale(double scale) {

		if (scale * tokenImage.getWidth() < 10) {
			return;
		}

		tokenScale = scale;

		repaint();
		fireCompositionChanged();
	}

	public void fireCompositionChanged() {
		changeObservers.fireChangeEvent();
	}

	// //
	// KEY LISTENER
	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case java.awt.event.KeyEvent.VK_UP:
			tokenOffsetY += -1;
			repaint();
			fireCompositionChanged();
			break;
		case java.awt.event.KeyEvent.VK_DOWN:
			tokenOffsetY += 1;
			repaint();
			fireCompositionChanged();
			break;
		case java.awt.event.KeyEvent.VK_LEFT:
			tokenOffsetX += -1;
			repaint();
			fireCompositionChanged();
			break;
		case java.awt.event.KeyEvent.VK_RIGHT:
			tokenOffsetX += 1;
			repaint();
			fireCompositionChanged();
		}

	}

	// //
	// DROP TARGET LISTNER

	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {

		Transferable transferable = dtde.getTransferable();
		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

		try {
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<URL> urls = new FileTransferableHandler().getTransferObject(transferable);

				for (URL url : urls) {
					String baseName = java.net.URLDecoder.decode(FilenameUtils.getBaseName(url.getFile()), "UTF-8");
					TokenTool.getFrame().getControlPanel().setNamePrefixField(baseName);
				}
			}
		} catch (UnsupportedFlavorException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			Image image = new ImageTransferableHandler()
					.getTransferObject(transferable);
			if (!(image instanceof BufferedImage)) {
				// Convert to buffered image
				image = ImageUtil.createCompatibleImage(image);
			}

			setToken((BufferedImage) image);
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		fireCompositionChanged();
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	// //
	// MOUSE LISTENER
	public void mouseClicked(MouseEvent e) {
		requestFocus();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

		dragStartX = e.getX();
		dragStartY = e.getY();

	}

	public void mouseReleased(MouseEvent e) {
	}

	// //
	// MOUSE MOTION LISTENEr
	public void mouseDragged(MouseEvent e) {

		int dx = e.getX() - dragStartX;
		int dy = e.getY() - dragStartY;

		dragStartX = e.getX();
		dragStartY = e.getY();

		tokenOffsetX += dx;
		tokenOffsetY += dy;

		repaint();
		fireCompositionChanged();
	}

	public void mouseMoved(MouseEvent e) {
	}

	// //
	// Mouse Wheel
	public void mouseWheelMoved(MouseWheelEvent e) {

		if (tokenImage == null) {
			return;
		}

		double delta = SwingUtil.isControlDown(e) ? .1 : .01;

		double newScale = 0;
		if (e.getWheelRotation() > 0) {
			newScale = tokenScale - delta;
		} else {
			newScale = tokenScale + delta;
		}

		setScale(newScale);
	}

	public BufferedImage getComposedToken() {
		if (overlayBounds == null) {
			return null;
		}
		return TokenCompositor.composeToken(
				overlayImage,
				tokenImage,
				overlayBounds.x - tokenOffsetX,
				overlayBounds.y - tokenOffsetY,
				overlayBounds.width,
				overlayBounds.height,
				tokenScale,
				AppState.compositionProperties);

	}

	private static class ChangeObservable extends Observable {

		public void fireChangeEvent() {
			setChanged();
			notifyObservers();
		}
	}
}
