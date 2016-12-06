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
package net.rptools.lib.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.SwingUtil;

/**
 * Mechanism to view very, very large images in memory without taking up the memory.
 * Works by precomputing chunks of the image at various zoom levels.  These are then stored
 * in a cache directory, specified during creation.
 * 
 * Then entire image has to be in memory at some point in order to chunk it, but once it's
 * been processed it can identify the cache via the image bytes (perhaps loaded from disk)
 * 
 * @author trevor
 */
public class LargeImage {

	private static final int CHUNK_SIZE = 250;

	// Can scale in such that there are this many extra chunks in view
	private static final int SCALE_CHUNK_INTERVAL = 4;

	private double scale;
	private Rectangle currentView;
	private BufferedImage currentViewBuffer;

	private ImageInfo info;

	private File cacheDir;

	private double[] scaleArray;

	private final Map<Integer, BufferedImage> loadedChunkMap = new HashMap<Integer, BufferedImage>();
	List<Integer> usedChunkList = new ArrayList<Integer>();

	public LargeImage(byte[] image, File cacheDir) throws IOException {
	}

	public LargeImage(BufferedImage image, File cacheDir) throws IOException {
		info = loadInfo();
		if (info == null) {
			info = new ImageInfo(image.getWidth(), image.getHeight());
		}
		this.cacheDir = new File(cacheDir.getAbsolutePath(), getId(image));
		this.cacheDir.mkdirs();
		chunkize(image);
	}

	private ImageInfo loadInfo() {
		return null;
	}

	private BufferedImage getImageView(Rectangle view, double scale) {
		if (currentView != null && currentView.equals(view) && currentViewBuffer != null && this.scale == scale) {
			return currentViewBuffer;
		}
		if (currentViewBuffer == null || currentViewBuffer.getWidth() != view.width || currentViewBuffer.getHeight() != view.height) {
			currentViewBuffer = new BufferedImage(view.width, view.height, Transparency.OPAQUE);
		}
		Graphics2D g = currentViewBuffer.createGraphics();

		// Background
		g.setColor(Color.black);
		g.fillRect(0, 0, view.width, view.height);

		double xOffset = (view.x % CHUNK_SIZE) * scale;
		double yOffset = (view.y % CHUNK_SIZE) * scale;

		int visibleXChunks = (int) Math.ceil((view.width + xOffset) / (CHUNK_SIZE * scale));
		int visibleYChunks = (int) Math.ceil((view.height + yOffset) / (CHUNK_SIZE * scale));

		for (int row = 0; row < visibleYChunks; row++) {
			for (int col = 0; col < visibleXChunks; col++) {
				int chunkX = view.x / CHUNK_SIZE + col;
				int chunkY = view.y / CHUNK_SIZE + row;

				BufferedImage chunk = getChunk(chunkX, chunkY, scale);
				if (chunk == null) {
					continue;
				}
				int x = (int) (col * CHUNK_SIZE * scale - xOffset);
				int y = (int) (row * CHUNK_SIZE * scale - yOffset);
				g.drawImage(chunk, x, y, (int) Math.ceil(CHUNK_SIZE * scale), (int) Math.ceil(CHUNK_SIZE * scale), null);
			}
		}
		flushChunkCache();
		g.dispose();
		return currentViewBuffer;
	}

	private void flushChunkCache() {
		Set<Integer> keySet = new HashSet<Integer>();
		keySet.addAll(loadedChunkMap.keySet());
		for (Integer num : keySet) {
			if (!usedChunkList.contains(num)) {
				loadedChunkMap.remove(num);
			}
		}
		usedChunkList.clear();
		//		System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}

	private BufferedImage getChunk(int col, int row, double scale) {
		int scaleIndex = getScaleIndex(scale);
		int chunkId = getChunkId(col, row, scaleIndex);

		if (col >= getChunkCountX(scaleIndex) || row >= getChunkCountY(scaleIndex) ||
				col < 0 || row < 0) {
			return null;
		}
		usedChunkList.add(chunkId);

		BufferedImage chunk = loadedChunkMap.get(chunkId);
		if (chunk != null) {
			return chunk;
		}
		try {
			chunk = ImageIO.read(getChunkFilename(col, row, scaleIndex));
			loadedChunkMap.put(chunkId, chunk);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return chunk;
	}

	private int getScaleIndex(double scale) {
		// Pick the highest (Biggest) scale that is closest to the target scale
		for (int i = 0; i < getScaleArray().length - 1; i++) {
			if (scale > scaleArray[i + 1]) {
				return i;
			}
		}
		return getScaleArray().length - 1;
	}

	private double[] getScaleArray() {
		if (scaleArray == null) {
			scaleArray = new double[getScaleCount()];

			scaleArray[0] = 1; // 0 is always 1:1
			for (int i = 1; i < scaleArray.length; i++) {
				scaleArray[i] = (double) (info.width - i * CHUNK_SIZE * SCALE_CHUNK_INTERVAL) / info.width;
			}
		}
		return scaleArray;
	}

	private int getChunkId(int col, int row, int scaleIndex) {
		return (scaleIndex * getChunkCountX(0) * getChunkCountY(0)) + row * getChunkCountX(scaleIndex) + col;
	}

	private int getChunkCountX(int scaleIndex) {
		return (int) Math.ceil((info.width * getScaleArray()[scaleIndex]) / CHUNK_SIZE);
	}

	private int getChunkCountY(int scaleIndex) {
		return (int) Math.ceil((info.width * getScaleArray()[scaleIndex]) / CHUNK_SIZE);
	}

	private int getScaleCount() {
		return (int) Math.max(Math.ceil(info.width / (CHUNK_SIZE * SCALE_CHUNK_INTERVAL)), Math.ceil(info.height / (CHUNK_SIZE * SCALE_CHUNK_INTERVAL)));
	}

	private void chunkize(BufferedImage image) throws IOException {
		for (int scaleIndex = 0; scaleIndex < getScaleCount(); scaleIndex++) {
			int chunksX = getChunkCountX(scaleIndex);
			int chunksY = getChunkCountY(scaleIndex);

			double scaledChunkSize = CHUNK_SIZE / getScaleArray()[scaleIndex];

			BufferedImage tmpImage = new BufferedImage(CHUNK_SIZE, CHUNK_SIZE, Transparency.OPAQUE);
			for (int row = 0; row < chunksY; row++) {
				for (int col = 0; col < chunksX; col++) {
					// Don't bother if it's already been created
					File filename = getChunkFilename(col, row, scaleIndex);
					if (filename.exists()) {
						continue;
					}
					int width = (int) Math.min(scaledChunkSize, image.getWidth() - col * scaledChunkSize);
					int height = (int) Math.min(scaledChunkSize, image.getHeight() - row * scaledChunkSize);

					BufferedImage chunkImage = image.getSubimage((int) (col * scaledChunkSize), (int) (row * scaledChunkSize), width, height);

					Graphics2D g = tmpImage.createGraphics();
					g.drawImage(chunkImage, 0, 0, CHUNK_SIZE, CHUNK_SIZE, null);
					g.dispose();

					ImageIO.write(tmpImage, "jpg", filename);
				}
			}
		}
	}

	private File getChunkFilename(int col, int row, int scaleIndex) {
		return new File(cacheDir.getAbsolutePath() + File.separator + scaleIndex + "-" + col + "-" + row + ".jpg");
	}

	private String getId(BufferedImage image) throws IOException {
		return getId(ImageUtil.imageToBytes(image));
	}

	private String getId(byte[] image) throws IOException {
		return new MD5Key(image).toString();
	}

	@SuppressWarnings("serial")
	public static class LoaderFrame extends JFrame {
		public LoaderFrame(LargeImage image) {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setLayout(new BorderLayout());

			setBounds(200, 200, 200, 200);
			add(BorderLayout.CENTER, new LoaderViewPanel(image));
		}
	}

	@SuppressWarnings("serial")
	public static class LoaderViewPanel extends JPanel {
		LargeImage image;
		Rectangle view = new Rectangle();
		double scale = 1;

		int dragX, dragY;

		public LoaderViewPanel(LargeImage image) {
			this.image = image;

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					dragX = e.getX();
					dragY = e.getY();

					if (!SwingUtilities.isLeftMouseButton(e)) {
						scale += SwingUtil.isShiftDown(e) ? .01 : -.01;
						repaint();
					}
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						view.x -= e.getX() - dragX;
						view.y -= e.getY() - dragY;

						dragX = e.getX();
						dragY = e.getY();

						repaint();
					}
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			Dimension size = getSize();
			view.setBounds(view.x, view.y, size.width, size.height);

			BufferedImage imageView = image.getImageView(view, scale);

			g.drawImage(imageView, 0, 0, this);
		}
	}

	private static class ImageInfo {
		int width;
		int height;

		public ImageInfo(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}

	public static void main(String[] args) throws Exception {
		ImageIO.setUseCache(false);

		LargeImage image = new LargeImage(ImageIO.read(new File("map.jpg")), new File("images"));
		System.out.println("START: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

		new LoaderFrame(image).setVisible(true);
	}
}
