/*
 * The MIT License
 * 
 * Copyright (c) 2005 David Rice, Trevor Croft
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.rptools.tokentool.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.transferable.FileListTransferable;
import net.rptools.lib.transferable.ImageTransferable;
import net.rptools.tokentool.AppActions;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.TokenTool;

public class TokenPreviewPanel extends JComponent implements Observer, DragGestureListener, DragSourceListener {

	private static final Dimension PREFERRED_SIZE = new Dimension(150, 150);
	private static final int GRID_SIZE = 10;

	private FutureTask composeTask;
	private static ExecutorService composeThreadPool = Executors.newFixedThreadPool(1);
	boolean repaintRequested;

	private BufferedImage iconImage;

	public TokenPreviewPanel() {
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
		setPreferredSize(PREFERRED_SIZE);
		setMinimumSize(PREFERRED_SIZE);

	}

	@Override
	protected void paintComponent(Graphics g) {

		Dimension size = getSize();

		// background grid
		g.setColor(Color.white);
		g.fillRect(0, 0, size.width - 1, size.height - 1);
		g.setColor(Color.lightGray);
		for (int y = 0; y < size.height; y += GRID_SIZE) {
			for (int x = y % (GRID_SIZE * 2) == 0 ? 0 : GRID_SIZE; x < size.width; x += GRID_SIZE * 2) {

				g.fillRect(x, y, GRID_SIZE, GRID_SIZE);
			}
		}

		BufferedImage tokenImage = getIconImage();
		if (tokenImage == null) {
			return;
		}

		Dimension imgSize = new Dimension(tokenImage.getWidth(), tokenImage.getHeight());
		SwingUtil.constrainTo(imgSize, size.width, size.height);

		if (tokenImage != null) {
			Graphics2D g2d = (Graphics2D) g;
			Object preRenderHint = g2d.getRenderingHint(RenderingHints.KEY_RENDERING);

			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(tokenImage, (size.width - imgSize.width) / 2, (size.height - imgSize.height) / 2, imgSize.width, imgSize.height, this);

			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, preRenderHint);
		}
	}

	private BufferedImage getIconImage() {
		if (iconImage == null) {
			update(null, null);
		}

		return iconImage;
	}

	// //
	// OBSERVER
	public synchronized void update(Observable o, Object arg) {

		repaintRequested = true;
		if (composeTask != null && !composeTask.isDone()) {
			return;
		}

		composeTask = new FutureTask<BufferedImage>(new Runnable() {
			public void run() {

				while (repaintRequested) {
					repaintRequested = false;

					// Force a redraw of the icon
					iconImage = TokenTool.getFrame().getComposedToken();

					repaint();
				}
			}
		}, null);
		composeThreadPool.submit(composeTask);
	}

	/**
	 * Get a File object using a filename according to user selection of (useIncrementalNumbering), (FilePrefix) and (FileNumber)
	 * 
	 * @author cif
	 * @return File object
	 */
	private File lastFile = null;

	private File getTempFileAsToken(boolean asToken) throws IOException {
		final String _extension;
		String tempFileName;

		if (asToken) {
			_extension = AppConstants.DEFAULT_TOKEN_EXTENSION;
		} else {
			_extension = AppConstants.DEFAULT_IMAGE_EXTENSION;
		}

		if (TokenTool.getFrame().getControlPanel().useFileNumbering()) {
			int dragCounter = TokenTool.getFrame().getControlPanel().getFileNumber();
			String namePrefix = TokenTool.getFrame().getControlPanel().getNamePrefix();
			if (namePrefix == null)
				namePrefix = "token";
			tempFileName = String.format("%s_%04d" + _extension, namePrefix, dragCounter);

		} else {
			//tempTileName = AppConstants.DEFAULT_TOKEN_DRAG_NAME + _extension;

			tempFileName = TokenTool.getFrame().getControlPanel().getNamePrefix();
			if (tempFileName == null || tempFileName.isEmpty())
				tempFileName = AppConstants.DEFAULT_TOKEN_NAME + _extension;
			tempFileName += _extension;
		}

		tempFileName = System.getProperty("java.io.tmpdir") + tempFileName;

		return new File(tempFileName);
	}

	// DRAG GESTURE LISTENER
	public void dragGestureRecognized(DragGestureEvent dge) {
		boolean saveAsToken = TokenTool.getFrame().getControlPanel().dragAsToken();

		BufferedImage tokenImage = TokenTool.getFrame().getComposedToken();
		if (tokenImage == null)
			return;

		Transferable transferable = null;
		try {
			File tempTokenFile = getTempFileAsToken(saveAsToken);

			// remember the temp file to delete it later on ...
			lastFile = new File(tempTokenFile.getAbsolutePath());

			if (saveAsToken) {
				AppActions.saveToken(tempTokenFile, true);
			} else {
				ImageIO.write(tokenImage, "png", tempTokenFile);
			}

			transferable = new FileListTransferable(tempTokenFile);
		} catch (Exception e) {
			transferable = new ImageTransferable(tokenImage);
		}

		dge.startDrag(Toolkit.getDefaultToolkit().createCustomCursor(tokenImage, new Point(0, 0), "Thumbnail"), transferable, this);
	}

	// DRAG SOURCE LISTENER
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// CIF: if drop successful and file numbering is used increase counter
		if (dsde.getDropSuccess())
			if (TokenTool.getFrame().getControlPanel().useFileNumbering()) {
				TokenTool.getFrame().getControlPanel().setFileNumber(TokenTool.getFrame().getControlPanel().getFileNumber() + 1);
			}

		// CIF: since use different filenames i have to delete the temp files
		try {
			lastFile.delete();
		} catch (SecurityException ex) {
			// cannot
			System.out.println("Temp file not accessable: " + ex.getMessage());
		}
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

}
