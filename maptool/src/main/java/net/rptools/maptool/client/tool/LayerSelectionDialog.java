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

package net.rptools.maptool.client.tool;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;

import com.jeta.forms.components.panel.FormPanel;

public class LayerSelectionDialog extends JPanel {

	private final FormPanel panel;
	private JList list;
	private final LayerSelectionListener listener;
	private final Zone.Layer[] layerList;

	public LayerSelectionDialog(Zone.Layer[] layerList, LayerSelectionListener listener) {
		panel = new FormPanel("net/rptools/maptool/client/ui/forms/layerSelectionDialog.xml");
		this.listener = listener;
		this.layerList = layerList;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		getLayerList();

		setLayout(new GridLayout(1, 1));
		add(panel);
	}

	public void fireViewSelectionChange() {

		int index = list.getSelectedIndex();
		if (index >= 0 && listener != null) {
			listener.layerSelected((Zone.Layer) list.getModel().getElementAt(index));
		}
	}

	public void updateViewList() {
		getLayerList().setSelectedValue(MapTool.getFrame().getCurrentZoneRenderer().getActiveLayer(), true);
	}

	private JList getLayerList() {

		if (list == null) {
			list = panel.getList("layerList");

			DefaultListModel model = new DefaultListModel();
			for (Zone.Layer layer : layerList) {
				model.addElement(layer);
			}

			list.setModel(model);
			list.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) {
						return;
					}

					fireViewSelectionChange();
				}
			});
			list.setSelectedIndex(0);
		}

		return list;
	}

	public void setSelectedLayer(Zone.Layer layer) {
		list.setSelectedValue(layer, true);
	}

	public static interface LayerSelectionListener {
		public void layerSelected(Zone.Layer layer);
	}
}
