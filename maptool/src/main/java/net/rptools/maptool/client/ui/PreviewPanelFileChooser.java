/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.rptools.lib.image.ThumbnailManager;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;

/*
 * A File chooser with an image preview panel
 */
public class PreviewPanelFileChooser extends JFileChooser {

	private JPanel previewWrapperPanel;
	private ImagePreviewPanel browsePreviewPanel;
	private ThumbnailManager thumbnailManager = new ThumbnailManager(AppUtil.getAppHome("previewPanelThumbs"), new Dimension(150, 150));

	public PreviewPanelFileChooser() {
		this.setCurrentDirectory(AppPreferences.getLoadDir());
		this.setAccessory(getPreviewWrapperPanel());
		this.addPropertyChangeListener(PreviewPanelFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
				new FileSystemSelectionHandler());
	}

	private class FileSystemSelectionHandler implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			File previewFile = getImageFileOfSelectedFile();

			if (previewFile != null && !previewFile.isDirectory()) {
				try {
					Image img = thumbnailManager.getThumbnail(previewFile);
					getPreviewPanel().setImage(img);
				} catch (IOException ioe) {
					getPreviewPanel().setImage(null);
				}
			} else {
				getPreviewPanel().setImage(null);
			}
		}
	}

	//Override if selected file is not also the image
	protected File getImageFileOfSelectedFile() {
		return getSelectedFile();
	}

	public Image getSelectedThumbnailImage() {
		return browsePreviewPanel.getImage();
	}

	private JPanel getPreviewWrapperPanel() {
		if (previewWrapperPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setColumns(1);
			previewWrapperPanel = new JPanel();
			previewWrapperPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(0, 5, 0, 0), BorderFactory
							.createTitledBorder(null, "Preview",
									TitledBorder.CENTER,
									TitledBorder.BELOW_BOTTOM, null, null)));
			previewWrapperPanel.setLayout(gridLayout);
			previewWrapperPanel.add(getPreviewPanel(), null);
		}
		return previewWrapperPanel;
	}

	private ImagePreviewPanel getPreviewPanel() {
		if (browsePreviewPanel == null) {

			browsePreviewPanel = new ImagePreviewPanel();
		}

		return browsePreviewPanel;
	}

}
