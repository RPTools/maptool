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

package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.TokenPopupMenu;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

public class TokenPanel extends JPanel implements ModelChangeListener {

	private ZoneRenderer currentZoneRenderer;
	private JList tokenList;

	public TokenPanel() {
		setLayout(new BorderLayout());
		tokenList = new JList();
		tokenList.setCellRenderer(new TokenListCellRenderer());
		tokenList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// TODO: make this not an aic
				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

					Token token = (Token) tokenList.getSelectedValue();
					currentZoneRenderer.centerOn(new ZonePoint(token.getX(), token.getY()));
					currentZoneRenderer.clearSelectedTokens();
					currentZoneRenderer.selectToken(token.getId());
				}
				if (SwingUtilities.isRightMouseButton(e)) {

					int itemUnderMouse = tokenList.locationToIndex(new Point(e.getX(), e.getY()));
					if (!tokenList.isSelectedIndex(itemUnderMouse)) {
						if (!SwingUtil.isShiftDown(e)) {
							tokenList.clearSelection();
						}
						tokenList.addSelectionInterval(itemUnderMouse, itemUnderMouse);
					}

					final int x = e.getX();
					final int y = e.getY();
					EventQueue.invokeLater(new Runnable() {
						public void run() {

							Token firstToken = null;
							Set<GUID> selectedTokenSet = new HashSet<GUID>();
							for (int index : tokenList.getSelectedIndices()) {

								Token token = (Token) tokenList.getModel().getElementAt(index);
								if (firstToken == null) {
									firstToken = token;
								}

								if (AppUtil.playerOwns(token)) {
									selectedTokenSet.add(token.getId());
								}
							}
							if (selectedTokenSet.size() > 0) {

								new TokenPopupMenu(selectedTokenSet, x, y, currentZoneRenderer, firstToken).showPopup(tokenList);
							}
						}
					});
				}
			}
		});
		new TokenPanelTransferHandler(tokenList);
		add(BorderLayout.CENTER, new JScrollPane(tokenList));
	}

	public void setZoneRenderer(ZoneRenderer renderer) {
		if (currentZoneRenderer != null) {
			currentZoneRenderer.getZone().removeModelChangeListener(this);
		}

		currentZoneRenderer = renderer;

		if (currentZoneRenderer != null) {
			currentZoneRenderer.getZone().addModelChangeListener(this);

			repaint();
		}

		// TODO: make this not a aic
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				Zone zone = currentZoneRenderer != null ? currentZoneRenderer.getZone() : null;
				tokenList.setModel(new TokenListModel(zone));
			}
		});
	}

	////
	// ModelChangeListener
	public void modelChanged(ModelChangeEvent event) {

		// Tokens are added and removed, just repaint ourself
		((TokenListModel) tokenList.getModel()).update();
		repaint();
	}
}
