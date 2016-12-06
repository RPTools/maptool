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
