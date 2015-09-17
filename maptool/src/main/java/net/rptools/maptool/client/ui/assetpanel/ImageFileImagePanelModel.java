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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Paint;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SwingWorker;

import net.rptools.lib.FileUtil;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImagePanelModel;
import net.rptools.maptool.client.AppConstants;
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
	private static String filter;
	private boolean global;
	private static List<File> fileList;
	private List<Directory> subDirList;

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
	 * <b>true</b> means to search subdirectories as well as parent directory) and the filter text.
	 */
	private void refresh() {
		fileList = new ArrayList<File>();
		subDirList = new ArrayList<Directory>();

		if (global == true && filter != null && filter.length() > 0) {
			// FIXME populate fileList from all filenames in the library
			// Use the AssetManager class, something akin to searchForImageReferences()
			// but I don't want to do a search; I want to use the existing cached results.
			// Looks like all files with ".lnk" (see getAssetLinkFile() in the AssetManager class).
			// assert global;

			/*
			 * Jamz: In the meantime, doing raw search and only search subdirectories if some criteria is filled in.
			 * Didn't feel like hacking up AssetManager at this stage of development.
			 * For now limiting global search to prevent very large arrays of 1000's of files which the panel
			 * has a hard time rendering (even without global searches, it lags on large file lists).
			 */

			try {
				fileList.addAll(dir.getFiles());

				// Filter current directory of files
				for (ListIterator<File> iter = fileList.listIterator(); iter.hasNext();) {
					File file = iter.next();
					if (!file.getName().toUpperCase().contains(filter)) {
						iter.remove();
					}
				}

				// Now search remaining subdirectories and filter as it goes.
				// Stop at any time if it reaches SEARCH_LIMIT
				subDirList.addAll(dir.getSubDirs());
				ListFilesSwingWorker.reset();

				for (Directory folder : subDirList) {
					ListFilesSwingWorker workerThread = new ListFilesSwingWorker(folder.getPath());
					workerThread.execute();
				}

			} catch (FileNotFoundException fnf) {
				MapTool.showError(fnf.getLocalizedMessage(), fnf);
			}
		} else {
			try {
				fileList.addAll(dir.getFiles());
			} catch (FileNotFoundException fnf) {
				MapTool.showError(fnf.getLocalizedMessage(), fnf);
			}

			if (filter != null && filter.length() > 0) {
				for (ListIterator<File> iter = fileList.listIterator(); iter.hasNext();) {
					File file = iter.next();
					if (!file.getName().toUpperCase().contains(filter)) {
						iter.remove();
					}
				}
			}
		}

		Collections.sort(fileList, filenameComparator);
		MapTool.getFrame().getAssetPanel().updateGlobalSearchLabel(fileList.size());
	}

	private static class ListFilesSwingWorker extends SwingWorker<Void, Integer> {
		private final File folderPath;
		private static boolean limitReached = false;

		private ListFilesSwingWorker(File path) {
			folderPath = path;
		}

		private static void reset() {
			limitReached = false;
		}

		@Override
		protected Void doInBackground() throws Exception {
			MapTool.getFrame().getAssetPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			listFilesInSubDirectories();
			publish(fileList.size());
			return null;
		}

		@Override
		protected void process(List<Integer> integers) {
			MapTool.getFrame().getAssetPanel().updateGlobalSearchLabel(fileList.size());
		}

		@Override
		protected void done() {
			synchronized (this) {
				// Due to multiple threads running, we may go over the limit before all threads are cancelled
				// Lets truncate the results and do it synchronized so we don't invoke concurrent modification errors.
				if (fileList.size() > 1000)
					fileList = fileList.subList(0, 1000);
			}

			// Jamz: Causes cursor to flicker due to multiple threads running. Needs a supervisior thread to
			//       watch over all threads. Pain to code, leave for later? Remove cursor changes?
			MapTool.getFrame().getAssetPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		/* 
		 * Jamz: Return all assets in subdirectories and each to fileList
		 *       This will spawn SwingWorkers for each subdir and is as such "multi-threaded"
		 *       although not a true "Thread". It will cancel remaining workers once limit
		 *       is reached. It searches through thousands of files very quickly.
		 */
		private void listFilesInSubDirectories() {
			publish(fileList.size());

			if (limitReached()) {
				cancel(true);
				return;
			}

			// This will filter out any non maptool files, ie show only image file types
			// But it also filters out directories, so we'll just handle them as separate loops.
			File[] files = folderPath.listFiles(AppConstants.IMAGE_FILE_FILTER);
			File[] folders = folderPath.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			});

			for (final File fileEntry : files) {
				if (fileEntry.getName().toUpperCase().contains(filter) && !limitReached)
					fileList.add(fileEntry);
				if (limitReached())
					break;
			}

			for (final File fileEntry : folders) {
				if (limitReached())
					break;
				ListFilesSwingWorker workerThread = new ListFilesSwingWorker(fileEntry);
				workerThread.execute();
			}
		}

		private boolean limitReached() {
			if (fileList.size() > AppConstants.ASSET_SEARCH_LIMIT)
				limitReached = true;
			return limitReached;
		}
	}

	private static Comparator<File> filenameComparator = new Comparator<File>() {
		public int compare(File o1, File o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};
}
