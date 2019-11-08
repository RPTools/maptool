/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.assetpanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.SwingWorker;
import net.rptools.lib.FileUtil;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.io.PackedFile;
import net.rptools.lib.swing.ImagePanelModel;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.TransferableAsset;
import net.rptools.maptool.client.TransferableToken;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ExtractHeroLab;
import net.rptools.maptool.util.ExtractImagesFromPDF;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.PersistenceUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageFileImagePanelModel implements ImagePanelModel {

  private static final Logger log = LogManager.getLogger(ImageFileImagePanelModel.class);
  private static final Color TOKEN_BG_COLOR = new Color(215, 215, 215);
  private static Image rptokenDecorationImage;
  private static Image herolabDecorationImage;

  static {
    try {
      rptokenDecorationImage = ImageUtil.getImage("net/rptools/maptool/client/image/rptokIcon.png");
      herolabDecorationImage =
          ImageUtil.getImage("net/rptools/maptool/client/image/hero-lab-decoration.png");
    } catch (IOException ioe) {
      rptokenDecorationImage = null;
      herolabDecorationImage = null;
    }
  }

  private Directory dir;
  private static String filter;
  private boolean global;
  private static List<File> fileList = new ArrayList<File>();
  private List<Directory> subDirList;

  private static int pagesProcessed = 0;
  private static PdfExtractor extractorSwingWorker;
  private static boolean pdfExtractIsRunning = false;
  private static ScheduledExecutorService extractThreadPool;

  public ImageFileImagePanelModel(Directory dir) {
    this.dir = dir;
  }

  public void rescan(Directory dir) {
    this.dir = dir;

    if (dir.isPDF()) {
      refreshPDF(true);
    } else if (dir.isHeroLabPortfolio()) {
      refreshHeroLab();
    } else {
      refresh();
    }
  }

  public void setFilter(String filter) {
    if (dir == null) return;

    ImageFileImagePanelModel.filter = filter.toUpperCase();
    if (dir.isPDF()) {
      if (!pdfExtractIsRunning) {
        refreshPDF(false);
      }
    } else if (dir.isHeroLabPortfolio()) {
      refreshHeroLab();
    } else {
      refresh();
    }
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
    return null;
    // return Token.isTokenFile(fileList.get(index).getName()) ? TOKEN_BG_COLOR : null;
  }

  public Image[] getDecorations(int index) {
    try {
      if (Token.isTokenFile(fileList.get(index).getName())) {

        PackedFile pakFile = new PackedFile(fileList.get(index));
        Object isHeroLab = pakFile.getProperty(PersistenceUtil.HERO_LAB);
        if (isHeroLab != null) {
          if ((boolean) isHeroLab) {
            return new Image[] {herolabDecorationImage};
          }
        }

        return new Image[] {rptokenDecorationImage};
      }

    } catch (IOException | NullPointerException | IndexOutOfBoundsException e) {
      e.printStackTrace();
    }

    return null;
  }

  public Image getImage(int index) {

    Image image = null;
    try {
      if (dir == null) {
        // Nothing - let it return the transfering image.
        image = ImageManager.TRANSFERING_IMAGE;
      } else if (dir.isHeroLabPortfolio()) {
        image = ((AssetDirectory) dir).getImageFor(fileList.get(index));
      } else if (dir instanceof AssetDirectory) {
        image = ((AssetDirectory) dir).getImageFor(fileList.get(index));
      } else if (dir instanceof PdfAsDirectory) {
        image = ((PdfAsDirectory) dir).getImageFor(fileList.get(index));
      }
    } catch (IndexOutOfBoundsException e) {
      // Jamz: OK, not pretty, I know... Occasionally this will happen while a PDF is extracting the
      // files as it's multi-threaded and the panel
      // is refreshing during the extract but I don't see this as a huge issue, we'll treat is as
      // TRANSFERING_IMAGE.
      // We could write logic using the progress bar but most likely this panel will get an update
      // with JavaFX soon(tm)...
      e.printStackTrace();
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

    if (dir instanceof AssetDirectory || dir instanceof PdfAsDirectory) {
      asset = getAsset(index);

      if (asset == null) {
        return null;
      }

      // Now is a good time to tell the system about it
      AssetManager.putAsset(asset);
    }

    return asset != null ? new TransferableAsset(asset) : null;
  }

  //
  /**
   * Gets image dimensions for given file without ImageIO overhead...
   *
   * @param imgFile image file
   * @return dimensions of image
   * @throws IOException if the file is not a known image
   */
  public static Dimension getImageDimension(File imgFile) throws IOException {
    int pos = imgFile.getName().lastIndexOf(".");
    if (pos == -1) throw new IOException("No extension for file: " + imgFile.getAbsolutePath());

    String suffix = imgFile.getName().substring(pos + 1);
    Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);

    if (iter.hasNext()) {
      ImageReader reader = iter.next();
      try {
        ImageInputStream stream = new FileImageInputStream(imgFile);
        reader.setInput(stream);
        int width = reader.getWidth(reader.getMinIndex());
        int height = reader.getHeight(reader.getMinIndex());
        return new Dimension(width, height);
      } catch (IOException e) {
        log.warn("Error reading: " + imgFile.getAbsolutePath(), e);
      } finally {
        reader.dispose();
      }
    }

    throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
  }

  // Jamz: Added second method to return a caption with more image details for use
  // with the ImagePanel/Asset window
  public String getCaption(int index, boolean withDimensions) {
    if (index < 0 || index >= fileList.size()) {
      return null;
    }

    // String name = fileList.get(index).getName();
    File file = fileList.get(index);
    String name = FileUtil.getNameWithoutExtension(file.getName());
    String caption = "<html><b>" + name + "</b></html>";

    if (!file.getName().toLowerCase().endsWith(Token.FILE_EXTENSION)
        && !file.getName().toLowerCase().endsWith(".pdf")) {
      try {
        Dimension imageDim = getImageDimension(fileList.get(index));
        int width = imageDim.width;
        int height = imageDim.height;
        String fileSize = FileUtils.byteCountToDisplaySize(file.length());
        String fileType = FilenameUtils.getExtension(file.getName());

        caption =
            "<html>"
                + "<b>"
                + name
                + "</b>"
                + "<br>Dimensions: "
                + width
                + " x "
                + height
                + "<br>Type: "
                + fileType
                + "<br>Size: "
                + fileSize
                + "</html>";
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return caption;
  }

  public String getCaption(int index) {
    if (index < 0 || index >= fileList.size()) {
      return null;
    }

    try {
      String name = fileList.get(index).getName();
      return FileUtil.getNameWithoutExtension(name);
    } catch (NullPointerException e) {
      // This can occasionally happen during PDF extraction as it's multi-threaded and the fileList
      // is getting updated after each page is extracted...
      e.printStackTrace();
    }

    return "";
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
   * We need to display the contents of the PDF instead of a directory and need to cache results.
   * Since extracted files could result in a lot of images where most are not needed long term,
   * we'll cache them in the .maptool temp directory and delete on exit. If the delete fails, the
   * temp directory automatically cleans up anything older than 2 days on startup.
   *
   * <p>First we'll spawn a SwingWorker so we can get back to the GUI while all the magic happens.
   * The SwingWorker will then spawn a multi-threaded ExecutorService/CompletionService to extract
   * out the images, 1 threaded task per page. This results in VERY fast extraction and allows up to
   * update the image panel after each thread.
   *
   * @param forceRescan
   */
  private void refreshPDF(boolean forceRescan) {
    cancelPdfExtract(); // If there is a current extract going on, lets cancel it...
    fileList.clear();
    extractorSwingWorker = new PdfExtractor(forceRescan);
    extractorSwingWorker.execute();
  }

  private void cancelPdfExtract() {
    if (pdfExtractIsRunning) {
      try {
        if (extractorSwingWorker != null) {
          extractorSwingWorker.cancel(true);
        }

        if (extractThreadPool != null) {
          extractThreadPool.shutdownNow();
          extractThreadPool.awaitTermination(1, TimeUnit.MINUTES);
        }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private class PdfExtractor extends SwingWorker<Void, Boolean> {
    private ExtractImagesFromPDF extractor;
    private final int pageCount;
    private final int numThreads = 6;

    private final boolean forceRescan;

    private PdfExtractor(boolean forceRescan) {
      pdfExtractIsRunning = true;
      this.forceRescan = forceRescan;

      try {
        extractor = new ExtractImagesFromPDF(dir.getPath(), forceRescan);

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      pageCount = extractor.getPageCount();
      extractThreadPool = Executors.newScheduledThreadPool(numThreads);
      MapTool.getFrame().getAssetPanel().setImagePanelProgressMax(pageCount);
    }

    @Override
    protected Void doInBackground() throws Exception {
      try {
        // 0 page count means it's already been processed (or PDF if empty)
        if (pageCount > 0 || forceRescan) {
          MapTool.getFrame().getAssetPanel().showImagePanelProgress(true);

          for (int pageNumber = 1; pageNumber < pageCount + 1; pageNumber++) {
            ExtractImagesTask task = new ExtractImagesTask(pageNumber, pageCount, dir, forceRescan);
            // When the PDF get to the larger modules (50-100 pages) it can overload the pool...
            extractThreadPool.schedule(task, 100 * pageNumber, TimeUnit.MILLISECONDS);
          }

          extractThreadPool.shutdown();
          extractThreadPool.awaitTermination(3, TimeUnit.MINUTES);
        } else {
          dir = new PdfAsDirectory(extractor.getTempDir(), AppConstants.IMAGE_FILE_FILTER);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return null;
    }

    @Override
    public void done() {
      extractThreadPool.shutdown();
      extractor.markComplete(
          isCancelled()); // If swing worker was cancelled, tell PdfExtractor to close resources but
      // don't mark as completed
      extractor.close();
      updatePdfProgress(0, extractor.getTempDir());
      pdfExtractIsRunning = false;
    }
  }

  /**
   * @author Jamz
   *     <p>A Callable task add to the ExecutorCompletionService so we can track as Futures.
   */
  private final class ExtractImagesTask implements Callable<Void> {
    private final int pageNumber;
    private final ExtractImagesFromPDF extractor;

    public ExtractImagesTask(int pageNumber, int pageCount, Directory dir, boolean forceRescan)
        throws IOException {
      this.pageNumber = pageNumber;
      this.extractor = new ExtractImagesFromPDF(dir.getPath(), forceRescan);
    }

    @Override
    public Void call() throws Exception {
      try {
        fileList.addAll(extractor.extractPage(pageNumber));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        extractor.close();
        updatePdfProgress(pageNumber, null);
      }

      return null;
    }
  }

  private void updatePdfProgress(int progress, File tempFile) {
    if (progress == 0) {
      pagesProcessed = 0;
      fileListCleanup(new PdfAsDirectory(tempFile, AppConstants.IMAGE_FILE_FILTER));
      MapTool.getFrame().getAssetPanel().showImagePanelProgress(false);
    } else {
      fileListCleanup();
    }

    MapTool.getFrame().getAssetPanel().setImagePanelProgress(pagesProcessed++);
    MapTool.getFrame().getAssetPanel().updateImagePanel();
  }

  private void fileListCleanup(Directory dir) {
    fileList.clear();
    try {
      fileList.addAll(dir.getFiles());
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    fileListCleanup();
  }

  private void fileListCleanup() {
    Set<File> tempSet = new HashSet<File>();
    tempSet.addAll(fileList);
    fileList.clear();
    fileList.addAll(tempSet);
    Collections.sort(fileList, filenameComparator);
  }

  private void refreshHeroLab() {
    cancelPdfExtract(); // In case we interrupted the extract by changing directories before it
    // finished...

    fileList = new ArrayList<File>();
    subDirList = new ArrayList<Directory>();

    // Jamz: TODO Add progress bar!
    // MapTool.getFrame().getAssetPanel().updateImagePanel();

    boolean portfolioChanged = dir.hasChanged();
    ExtractHeroLab heroLabFile = new ExtractHeroLab(dir.getPath(), portfolioChanged);
    fileList.addAll(heroLabFile.extractAllCharacters(portfolioChanged));

    if (filter != null && filter.length() > 0) {
      for (ListIterator<File> iter = fileList.listIterator(); iter.hasNext(); ) {
        File file = iter.next();
        if (!file.getName().toUpperCase().contains(filter)) {
          iter.remove();
        }
      }
    }

    Collections.sort(fileList, filenameComparator);
    MapTool.getFrame().getAssetPanel().updateGlobalSearchLabel(fileList.size());
  }

  /**
   * Determines which images to display based on the setting of the Global vs. Local flag (<code>
   * global</code> == <b>true</b> means to search subdirectories as well as parent directory) and
   * the filter text.
   */
  private void refresh() {
    cancelPdfExtract(); // In case we interrupted the extract by changing directories before it
    // finished...

    fileList = new ArrayList<File>();
    subDirList = new ArrayList<Directory>();

    if (global == true && filter != null && filter.length() > 0) {
      // FIXME populate fileList from all filenames in the library
      // Use the AssetManager class, something akin to searchForImageReferences()
      // but I don't want to do a search; I want to use the existing cached results.
      // Looks like all files with ".lnk" (see getAssetLinkFile() in the AssetManager class).
      // assert global;

      /*
       * Jamz: In the meantime, doing raw search and only search subdirectories if some criteria is filled in. Didn't feel like hacking up AssetManager at this stage of development. For now
       * limiting global search to prevent very large arrays of 1000's of files which the panel has a hard time rendering (even without global searches, it lags on large file lists).
       */

      try {
        fileList.addAll(dir.getFiles());

        // Filter current directory of files
        for (ListIterator<File> iter = fileList.listIterator(); iter.hasNext(); ) {
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
        for (ListIterator<File> iter = fileList.listIterator(); iter.hasNext(); ) {
          File file = iter.next();
          if (!file.getName().toUpperCase().contains(filter)) {
            iter.remove();
          }
        }
      }
    }

    Collections.sort(fileList, filenameComparator);
    try {
      MapTool.getFrame().getAssetPanel().updateGlobalSearchLabel(fileList.size());
    } catch (NullPointerException e) {
      // This currently throws a NPE if the frame was not finished initializing when runs. For now,
      // lets log a message and continue.
      log.warn(
          "NullPointerException encountered while trying to update ImageFileImagePanelModel global search label",
          e);
    }
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
        // Due to multiple threads running, we may go over the limit before all threads are
        // cancelled
        // Lets truncate the results and do it synchronized so we don't invoke concurrent
        // modification errors.
        if (fileList.size() > 1000) fileList = fileList.subList(0, 1000);
      }

      // Jamz: Causes cursor to flicker due to multiple threads running. Needs a supervisior thread
      // to
      // watch over all threads. Pain to code, leave for later? Remove cursor changes?
      MapTool.getFrame()
          .getAssetPanel()
          .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /*
     * Jamz: Return all assets in subdirectories and each to fileList This will spawn SwingWorkers for each subdir and is as such "multi-threaded" although not a true "Thread". It will cancel
     * remaining workers once limit is reached. It searches through thousands of files very quickly.
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
      File[] folders =
          folderPath.listFiles(
              new FilenameFilter() {
                public boolean accept(File dir, String name) {
                  return new File(dir, name).isDirectory();
                }
              });

      for (final File fileEntry : files) {
        if (fileEntry.getName().toUpperCase().contains(filter) && !limitReached)
          fileList.add(fileEntry);
        if (limitReached()) break;
      }

      for (final File fileEntry : folders) {
        if (limitReached()) break;
        ListFilesSwingWorker workerThread = new ListFilesSwingWorker(fileEntry);
        workerThread.execute();
      }
    }

    private boolean limitReached() {
      if (fileList.size() > AppConstants.ASSET_SEARCH_LIMIT) limitReached = true;
      return limitReached;
    }
  }

  private static Comparator<File> filenameComparator =
      new Comparator<File>() {
        public int compare(File o1, File o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }
      };
}
