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

package net.rptools.maptool.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaseModel {

	// Transient so that it isn't transfered over the wire
	private transient List<ModelChangeListener> listenerList = new CopyOnWriteArrayList<ModelChangeListener>();

	public void addModelChangeListener(ModelChangeListener listener) {
		listenerList.add(listener);
	}

	public void removeModelChangeListener(ModelChangeListener listener) {
		listenerList.remove(listener);
	}

	protected void fireModelChangeEvent(ModelChangeEvent event) {

		for (ModelChangeListener listener : listenerList) {
			listener.modelChanged(event);
		}
	}

	protected Object readResolve() {
		listenerList = new CopyOnWriteArrayList<ModelChangeListener>();
		return this;
	}
}
