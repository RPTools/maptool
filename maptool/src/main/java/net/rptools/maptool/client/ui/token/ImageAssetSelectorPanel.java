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

package net.rptools.maptool.client.ui.token;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.rptools.lib.swing.SelectionListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;

public class ImageAssetSelectorPanel extends JPanel {

	private ImageAssetPanel imageAssetPanel;
	private JButton imageExplorerButton;

	public ImageAssetSelectorPanel() {
		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, getImageAssetPanel());
		add(BorderLayout.SOUTH, getImageExplorerButton());
	}

	public ImageAssetPanel getImageAssetPanel() {
		if (imageAssetPanel == null) {
			imageAssetPanel = new ImageAssetPanel();
		}
		return imageAssetPanel;
	}

	public JButton getImageExplorerButton() {
		if (imageExplorerButton == null) {
			imageExplorerButton = new JButton("...");
		}

		return imageExplorerButton;
	}

	private JComponent createImageExplorerPanel() {

		final AssetPanel assetPanel = new AssetPanel("imageAssetSelectorImageExplorer", MapTool.getFrame().getAssetPanel().getModel(), JSplitPane.HORIZONTAL_SPLIT);
		assetPanel.addImageSelectionListener(new SelectionListener() {
			public void selectionPerformed(List<Object> selectedList) {
				// There should be exactly one
				if (selectedList.size() != 1) {
					return;
				}

				Integer imageIndex = (Integer) selectedList.get(0);

				getImageAssetPanel().setImageId(assetPanel.getAsset(imageIndex).getId());
			}
		});

		return assetPanel;
	}
}
