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

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import net.rptools.lib.swing.AbstractPaintChooserPanel;
import net.rptools.lib.swing.ImagePanel;
import net.rptools.lib.swing.PaintChooser;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
import net.rptools.maptool.client.ui.assetpanel.AssetPanelModel;
import net.rptools.maptool.model.Asset;

public class TextureChooserPanel extends AbstractPaintChooserPanel {

	private PaintChooser paintChooser;
	private ImagePanel imagePanel;

	public TextureChooserPanel(PaintChooser paintChooser, AssetPanelModel model) {
		this(paintChooser, model, "textureChooser");
	}

	public TextureChooserPanel(PaintChooser paintChooser, AssetPanelModel model, String controlName) {
		setLayout(new GridLayout());

		this.paintChooser = paintChooser;

		add(createImageExplorerPanel(model, controlName));
	}

	private JComponent createImageExplorerPanel(AssetPanelModel model, String controlName) {

		final AssetPanel assetPanel = new AssetPanel(controlName, model, JSplitPane.HORIZONTAL_SPLIT);
		assetPanel.addImageSelectionListener(new SelectionListener() {
			public void selectionPerformed(List<Object> selectedList) {
				// There should be exactly one
				if (selectedList.size() != 1) {
					return;
				}

				Integer imageIndex = (Integer) selectedList.get(0);
				Asset asset = assetPanel.getAsset(imageIndex);
				if (asset == null) {
					return;
				}

				paintChooser.setPaint(new AssetPaint(asset));
			}
		});
		assetPanel.setThumbSize(100);

		return assetPanel;
	}

	@Override
	public String getDisplayName() {
		return "Texture";
	}
}
