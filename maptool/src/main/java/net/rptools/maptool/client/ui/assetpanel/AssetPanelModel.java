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

import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssetPanelModel implements PropertyChangeListener {

	private final ImageFileTreeModel imageFileTreeModel;

	private final List<ImageObserver> observerList = new CopyOnWriteArrayList<ImageObserver>();

	public AssetPanelModel() {
		imageFileTreeModel = new ImageFileTreeModel();
	}

	public ImageFileTreeModel getImageFileTreeModel() {
		return imageFileTreeModel;
	}

	public void removeRootGroup(Directory dir) {
		imageFileTreeModel.removeRootGroup(dir);
		dir.removePropertyChangeListener(this);
	}

	public void addRootGroup(Directory dir) {
		if (imageFileTreeModel.containsRootGroup(dir)) {
			return;
		}
		dir.addPropertyChangeListener(this);
		imageFileTreeModel.addRootGroup(dir);
	}

	public void addImageUpdateObserver(ImageObserver observer) {
		if (!observerList.contains(observer)) {
			observerList.add(observer);
		}
	}

	public void removeImageUpdateObserver(ImageObserver observer) {
		observerList.remove(observer);
	}

	// PROPERTY CHANGE LISTENER
	public void propertyChange(PropertyChangeEvent evt) {
		for (ImageObserver observer : observerList) {
			observer.imageUpdate(null, ImageObserver.ALLBITS, 0, 0, 0, 0);
		}
	}
}
