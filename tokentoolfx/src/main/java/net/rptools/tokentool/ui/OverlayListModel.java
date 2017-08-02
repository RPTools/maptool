/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import javafx.application.Preloader;
import net.rptools.lib.image.ImageUtil;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.TokenCompositor;
import net.rptools.tokentool.fx.view.TokenToolFX;

public class OverlayListModel extends AbstractListModel implements ComboBoxModel {

	private static final int THUMB_SIZE = 50;

	private List<BufferedImage> overlayList;
	private List<BufferedImage> thumbList;
	private int selectedIndex = -1;

	public OverlayListModel() {

		refresh();
	}

	public Object getSelectedItem() {
		return (selectedIndex >= 0 && selectedIndex < thumbList.size()) ? thumbList.get(selectedIndex) : null;
	}

	public void setSelectedItem(Object anItem) {
		selectedIndex = thumbList.indexOf(anItem);
	}

	public BufferedImage getOverlayAt(int index) {
		return overlayList.get(index);
	}

	public Object getElementAt(int index) {
		return thumbList.get(index);
	}

	public int getSize() {
		return thumbList.size();
	}

	public BufferedImage getSelectedOverlay() {
		return (selectedIndex >= 0 && selectedIndex < overlayList.size()) ? overlayList.get(selectedIndex) : null;
	}

	public void refresh() {

		overlayList = new ArrayList<BufferedImage>();
		thumbList = new ArrayList<BufferedImage>();

		File[] files = AppConstants.OVERLAY_DIR.listFiles(ImageUtil.SUPPORTED_IMAGE_FILE_FILTER);

		// Put them in last modified order so that new overlays show up at the top
		List<File> fileList = Arrays.asList(files);
		Collections.sort(fileList, new Comparator<File>() {
			public int compare(File o1, File o2) {

				return o1.lastModified() < o2.lastModified() ? 1 : o1.lastModified() > o2.lastModified() ? -1 : 0;
			}
		});

		for (File file : fileList) {
			try {
				BufferedImage image = ImageUtil.createCompatibleImage(ImageUtil.getImage(file));
				overlayList.add(image);
				BufferedImage overlay = TokenCompositor.translateOverlay(image, 1);

				BufferedImage thumb = ImageUtil.createCompatibleImage(overlay, THUMB_SIZE, THUMB_SIZE, null);
				thumbList.add(thumb);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

		}

		fireContentsChanged(this, 0, fileList.size());
	}
}
