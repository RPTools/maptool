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

package net.rptools.maptool.client.ui.zone;

import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.Animatable;
import net.rptools.maptool.client.swing.AnimationManager;
import net.rptools.maptool.util.GraphicsUtil;

public class NotificationOverlay implements ZoneOverlay, Animatable {

	private List<EventDetail> eventList = new CopyOnWriteArrayList<EventDetail>();

	// TODO: make this configurable
	private static final int MESSAGE_DELAY = 2500;

	public NotificationOverlay() {
		AnimationManager.addAnimatable(this);
	}

	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {

		int y = 15;
		for (EventDetail detail : eventList) {

			GraphicsUtil.drawBoxedString(g, detail.message, 10, y, SwingUtilities.LEFT);

			y += 20;
		}
	}

	public void addEvent(String message) {
		if (message == null) {
			return;
		}

		eventList.add(new EventDetail(message));
	}

	private static class EventDetail {

		public long timestamp;
		public String message;

		public EventDetail(String message) {
			this.message = message;
			timestamp = System.currentTimeMillis();
		}
	}

	////
	// ANIMATABLE
	public void animate() {

		boolean requiresRepaint = false;
		while (eventList.size() > 0) {

			EventDetail detail = eventList.get(0);
			if (System.currentTimeMillis() - detail.timestamp > MESSAGE_DELAY) {

				eventList.remove(0);
				requiresRepaint = true;
			} else {
				break;
			}
		}
		if (requiresRepaint) {
			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
			if (renderer != null) {
				renderer.repaint();
			}
		}
	}

}
