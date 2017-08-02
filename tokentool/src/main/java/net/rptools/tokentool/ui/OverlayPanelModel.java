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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Transparency;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImagePanelModel;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.TokenCompositor;

public class OverlayPanelModel implements ImagePanelModel {

	private ExecutorService imageLoaderService = Executors.newFixedThreadPool(3);
	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	private File[] imageFiles;
	private Map<File, BufferedImage> imageMap;

	private static BufferedImage tempImage;

	static {
		tempImage = new BufferedImage(64, 64, Transparency.OPAQUE);
		Graphics2D g = tempImage.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 64, 64);

		g.setPaint(new GradientPaint(0, 0, Color.white, 63, 63, Color.lightGray));
		g.fillRect(0, 0, 63, 63);

		g.drawRect(0, 0, 63, 63);

		g.dispose();
	}

	public OverlayPanelModel() {
		refresh();
	}

	public void refresh() {
		imageFiles = AppConstants.OVERLAY_DIR.listFiles(ImageUtil.SUPPORTED_IMAGE_FILE_FILTER);
		imageMap = new HashMap<File, BufferedImage>();
	}

	public int getImageCount() {
		return imageFiles.length;
	}

	public Transferable getTransferable(int index) {
		return null;
	}

	public Object getID(int index) {
		return new Integer(index);
	}

	public Image getImage(Object ID) {

		return null;
	}

	public Image getImage(final int index) {
		if (index >= imageFiles.length || index < 0) {
			// index is out of bounds
			// how should i handle that?
			// why does it happens??
			throw new IndexOutOfBoundsException();
		}
		BufferedImage image = imageMap.get(imageFiles[index]);

		if (image == null) {
			image = tempImage;
			imageMap.put(imageFiles[index], image);

			imageLoaderService.submit(new Runnable() {
				public void run() {
					try {
						BufferedImage image = TokenCompositor.translateOverlay(ImageUtil.createCompatibleImage(ImageUtil.getImage(imageFiles[index])), 1);
						imageMap.put(imageFiles[index], image);

						changeSupport.fireIndexedPropertyChange("image", index, null, image);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			});
		}

		return image;
	}

	public String getCaption(int index) {
		if (index >= 0 && index < imageFiles.length) {
			Image image = getImage(index);
			return image.getWidth(null) + "x" + image.getHeight(null);
		} else {
			return "n/a";
		}
	}

	public File getFile(int index) {
		return imageFiles[index];
	}

	public void addChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removeChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * @see net.rptools.lib.swing.ImagePanelModel#getBackground(int)
	 */
	public Paint getBackground(int index) {
		// throw new UnsupportedOperationException("Added by newer version of RPLIB, but not used here.");
		return null;
	}

	/**
	 * @see net.rptools.lib.swing.ImagePanelModel#getDecorations(int)
	 */
	public Image[] getDecorations(int index) {
		// throw new UnsupportedOperationException("Added by newer version of RPLIB, but not used here.");
		return null;
	}

	@Override
	public String getCaption(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
