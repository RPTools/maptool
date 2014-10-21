/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui.assetpanel;

import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import net.rptools.lib.FileUtil;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImagePanelModel;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.TransferableAsset;
import net.rptools.maptool.client.TransferableToken;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.PersistenceUtil;

public class ImageFileImagePanelModel implements ImagePanelModel {

	private static final Color TOKEN_BG_COLOR = new Color(255, 250, 205);
	private static Image rptokenDecorationImage;

	static {
		try {
			rptokenDecorationImage = ImageUtil.getImage("net/rptools/maptool/client/image/rptokIcon.png");
		} catch (IOException ioe) {
			rptokenDecorationImage = null;
		}
	}

	private final Directory dir;
	private String filter;
	private boolean global;
	private List<File> fileList;

	public ImageFileImagePanelModel(Directory dir) {
		this.dir = dir;
		refresh();
	}

	public void setFilter(String filter) {
		this.filter = filter.toUpperCase();
		refresh();
	}

	public void setGlobalSearch(boolean yes) {
		this.global = yes;
		// Should be calling refresh() but the only implementation calls this method
		// followed by setFilter() [above] so that method will call refresh().
	}

	public int getImageCount() {
		return fileList.size();
	}

	public Paint getBackground(int index) {
		return Token.isTokenFile(fileList.get(index).getName()) ? TOKEN_BG_COLOR : null;
	}

	public Image[] getDecorations(int index) {
		return Token.isTokenFile(fileList.get(index).getName()) ? new Image[] { rptokenDecorationImage } : null;
	}

	public Image getImage(int index) {

		Image image = null;
		if (dir instanceof AssetDirectory) {

			image = ((AssetDirectory) dir).getImageFor(fileList.get(index));
		}

		return image != null ? image : ImageManager.TRANSFERING_IMAGE;
	}

	public Transferable getTransferable(int index) {
		Asset asset = null;

		File file = fileList.get(index);
		if (file.getName().toLowerCase().endsWith(Token.FILE_EXTENSION)) {

			try {
				Token token = PersistenceUtil.loadToken(file);

				return new TransferableToken(token);
			} catch (IOException ioe) {
				MapTool.showError("Could not load that token: ", ioe);
				return null;
			}
		}

		if (dir instanceof AssetDirectory) {
			asset = getAsset(index);

			if (asset == null) {
				return null;
			}

			// Now is a good time to tell the system about it
			AssetManager.putAsset(asset);
		}

		return asset != null ? new TransferableAsset(asset) : null;
	}

	public String getCaption(int index) {
		if (index < 0 || index >= fileList.size()) {
			return null;
		}

		String name = fileList.get(index).getName();
		return FileUtil.getNameWithoutExtension(name);
	}

	public Object getID(int index) {
		return new Integer(index);
	}

	public Image getImage(Object ID) {
		return getImage(((Integer) ID).intValue());
	}

	public Asset getAsset(int index) {
		if (index < 0) {
			return null;
		}

		try {
			Asset asset = AssetManager.createAsset(fileList.get(index));

			// I don't like having to do this, but the ImageManager api only allows assets that
			// the assetmanager knows about (by design). So there isn't an "immediate" mode
			// for assets anymore.
			AssetManager.putAsset(asset);

			return asset;
		} catch (IOException ioe) {
			return null;
		}
	}

	/**
	 * Determines which images to display based on the setting of the Global vs. Local flag (<code>global</code> ==
	 * <b>true</b> means to search all files in the library) and the filter text.
	 */
	private void refresh() {
		fileList = new ArrayList<File>();
		if (global == true) {
			// FIXME populate fileList from all filenames in the library
			// Use the AssetManager class, something akin to searchForImageReferences()
			// but I don't want to do a search; I want to use the existing cached results.
			// Looks like all files with ".lnk" (see getAssetLinkFile() in the AssetManager class).
			assert global;
		} else {
			try {
				fileList.addAll(dir.getFiles());
			} catch (FileNotFoundException fnf) {
				MapTool.showError(fnf.getLocalizedMessage(), fnf);
			}
		}
		if (filter != null && filter.length() > 0) {
			for (ListIterator<File> iter = fileList.listIterator(); iter.hasNext();) {
				File file = iter.next();
				if (!file.getName().toUpperCase().contains(filter)) {
					iter.remove();
				}
			}
		}
		Collections.sort(fileList, filenameComparator);
	}

	private static Comparator<File> filenameComparator = new Comparator<File>() {
		public int compare(File o1, File o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};
}
