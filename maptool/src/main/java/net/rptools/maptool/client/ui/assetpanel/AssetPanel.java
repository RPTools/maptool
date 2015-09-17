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

package net.rptools.maptool.client.ui.assetpanel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.rptools.lib.swing.ImagePanel;
import net.rptools.lib.swing.ImagePanel.SelectionMode;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.lib.swing.preference.SplitPanePreferences;
import net.rptools.lib.swing.preference.TreePreferences;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;

public class AssetPanel extends JComponent {
	private static final ImageIcon FILTER_IMAGE = new ImageIcon(AssetPanel.class.getClassLoader().getResource("net/rptools/maptool/client/image/zoom.png"));

	private final AssetTree assetTree;
	private ImagePanel imagePanel;
	private JTextField filterTextField;
	private JCheckBox globalSearchField;
	private final AssetPanelModel assetPanelModel;
	private Timer updateFilterTimer;

	public AssetPanel(String controlName) {
		this(controlName, new AssetPanelModel());
	}

	public AssetPanel(String controlName, AssetPanelModel model) {
		this(controlName, model, JSplitPane.VERTICAL_SPLIT);
	}

	public AssetPanel(String controlName, AssetPanelModel model, int splitPaneDirection) {
		assetPanelModel = model;
		model.addImageUpdateObserver(this);

		assetTree = new AssetTree(this);
		createImagePanel();

		JSplitPane splitPane = new JSplitPane(splitPaneDirection);
		splitPane.setContinuousLayout(true);

		splitPane.setTopComponent(new JScrollPane(assetTree));
		splitPane.setBottomComponent(createSouthPanel());
		splitPane.setDividerLocation(100);

		new SplitPanePreferences(AppConstants.APP_NAME, controlName, splitPane);
		new TreePreferences(AppConstants.APP_NAME, controlName, assetTree);

		setLayout(new GridLayout());
		add(splitPane);
	}

	private void createImagePanel() {
		imagePanel = new ImagePanel();
		/*
		 * {
		 * 
		 * @Override public void dragGestureRecognized(DragGestureEvent dge) { super.dragGestureRecognized(dge);
		 * 
		 * MapTool.getFrame().getDragImageGlassPane().setImage(ImageManager.getImageAndWait( assetBeingTransferred)); }
		 * 
		 * @Override public void dragMouseMoved(DragSourceDragEvent dsde) { super.dragMouseMoved(dsde);
		 * 
		 * Point p = new Point(dsde.getLocation()); SwingUtilities.convertPointFromScreen(p,
		 * MapTool.getFrame().getDragImageGlassPane());
		 * 
		 * MapTool.getFrame().getDragImageGlassPane().setImagePosition(p); }
		 * 
		 * @Override public void dragDropEnd(DragSourceDropEvent dsde) { super.dragDropEnd(dsde);
		 * 
		 * MapTool.getFrame().getDragImageGlassPane().setImage(null); }
		 * 
		 * @Override protected Cursor getDragCursor() { return Toolkit.getDefaultToolkit().createCustomCursor(new
		 * BufferedImage(1, 1, Transparency.BITMASK), new Point (0,0), ""); } };
		 */
		imagePanel.setShowCaptions(true);
		imagePanel.setSelectionMode(SelectionMode.SINGLE);
		imagePanel.setFont(new Font("Helvetica", 0, 10)); // XXX Overrides TinyLAF?
	}

	public void setThumbSize(int size) {
		imagePanel.setGridSize(size);
	}

	private JPanel createSouthPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(BorderLayout.NORTH, createFilterPanel());
		panel.add(BorderLayout.CENTER, new JScrollPane(imagePanel));

		return panel;
	}

	/**
	 * Creates the GUI for the bottom half of the splitpane that allows for finding assets within any of the repository
	 * locations (such as local directories).
	 * 
	 * @return
	 */
	private JPanel createFilterPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		JPanel top = new JPanel(new BorderLayout());
		top.add(BorderLayout.WEST, new JLabel("", FILTER_IMAGE, JLabel.LEFT));
		top.add(BorderLayout.CENTER, getFilterTextField());

		panel.add(BorderLayout.NORTH, top);
		panel.add(BorderLayout.SOUTH, getGlobalSearchField());

		return panel;
	}

	public void addImageSelectionListener(SelectionListener listener) {
		imagePanel.addSelectionListener(listener);
	}

	public void removeImageSelectionListener(SelectionListener listener) {
		imagePanel.removeSelectionListener(listener);
	}

	public List<Object> getSelectedIds() {
		return imagePanel.getSelectedIds();
	}

	public void showImagePanelPopup(JPopupMenu menu, int x, int y) {
		menu.show(imagePanel, x, y);
	}

	public JTextField getFilterTextField() {
		if (filterTextField == null) {
			filterTextField = new JTextField();
			filterTextField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					// no op
				}

				public void insertUpdate(DocumentEvent e) {
					updateFilter();
				}

				public void removeUpdate(DocumentEvent e) {
					updateFilter();
				}
			});
		}
		return filterTextField;
	}

	/**
	 * Returns a checkbox that indicates whether the filter field applies to <i>all</i> images in all libraries or just the currently selected image directory.
	 * Currently not implemented.
	 * 
	 * @return the checkbox component
	 */
	private JCheckBox getGlobalSearchField() {
		if (globalSearchField == null) {
			globalSearchField = new JCheckBox(I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir1"), false);
			globalSearchField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					updateFilter();
				}
			});
		}
		return globalSearchField;
	}

	public void updateGlobalSearchLabel(int listSize) {
		if (getGlobalSearchField().isSelected()) {
			globalSearchField.setText(
					I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir1")
							+ " (" + listSize + "/" + AppConstants.ASSET_SEARCH_LIMIT + " "
							+ I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir2") + ")");
		} else {
			globalSearchField.setText(I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir1"));
		}

		imagePanel.revalidate();
		imagePanel.repaint();
	}

	private synchronized void updateFilter() {
		if (updateFilterTimer == null) {
			updateFilterTimer = new Timer(500, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ImageFileImagePanelModel model = (ImageFileImagePanelModel) imagePanel.getModel();
					if (model == null) {
						return;
					}
					model.setGlobalSearch(getGlobalSearchField().isSelected());
					model.setFilter(getFilterTextField().getText());
					// TODO: This should be event based
					imagePanel.revalidate();
					imagePanel.repaint();

					updateFilterTimer.stop();
					updateFilterTimer = null;
				}
			});
			updateFilterTimer.start();
		} else {
			updateFilterTimer.restart();
		}
	}

	// TODO: Find a way around this, it's ugly
	public Asset getAsset(int index) {
		return ((ImageFileImagePanelModel) imagePanel.getModel()).getAsset(index);
	}

	public void addImagePanelMouseListener(MouseListener listener) {
		imagePanel.addMouseListener(listener);
	}

	public void removeImagePanelMouseListener(MouseListener listener) {
		imagePanel.removeMouseListener(listener);
	}

	public AssetPanelModel getModel() {
		return assetPanelModel;
	}

	public boolean isAssetRoot(Directory dir) {
		return ((ImageFileTreeModel) assetTree.getModel()).isRootGroup(dir);
	}

	public void removeAssetRoot(Directory dir) {
		assetPanelModel.removeRootGroup(dir);
	}

	public Directory getSelectedAssetRoot() {
		return assetTree.getSelectedAssetGroup();
	}

	public void addAssetRoot(Directory dir) {
		assetPanelModel.addRootGroup(dir);
	}

	public void setDirectory(Directory dir) {
		imagePanel.setModel(new ImageFileImagePanelModel(dir) {
			@Override
			public Transferable getTransferable(int index) {
				// TransferableAsset t = (TransferableAsset) super.getTransferable(index);
				// assetBeingTransferred = t.getAsset();
				// return t;
				return super.getTransferable(index);
			}
		});
		updateFilter();
	}

	public AssetTree getAssetTree() {
		return assetTree;
	}
}
