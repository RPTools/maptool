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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;
import org.apache.commons.io.monitor.FileEntry;

public class Directory {
  private static final FileFilter DIRECTORY_FILTER = new DirectoryFileFilter();
  private static final FileFilter HERO_LAB_FILE_FILTER = new HeroLabFileFilter();
  private static final MagicNumberFileFilter PDF_FILE_FILTER = new MagicNumberFileFilter("%PDF");

  private final List<PropertyChangeListener> listenerList =
      new CopyOnWriteArrayList<PropertyChangeListener>();
  private final File directory;

  private Directory parent;
  private List<Directory> subdirs;
  private List<File> files;
  private final FilenameFilter fileFilter;
  private FileEntry dirWatcher;

  public Directory(File directory) {
    this(directory, null);
  }

  public Directory(File directory, FilenameFilter fileFilter) {
    if (!directory.exists()) {
      throw new IllegalArgumentException(directory + " does not exist");
    }
    if (!directory.isDirectory()
        && !PDF_FILE_FILTER.accept(directory)
        && !HERO_LAB_FILE_FILTER.accept(directory)) {
      throw new IllegalArgumentException(
          directory + " is not a directory, pdf, or herolab portfolio.");
    }
    this.directory = directory;
    this.fileFilter = fileFilter;
    this.dirWatcher = new FileEntry(directory);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (!listenerList.contains(listener)) {
      listenerList.add(listener);
    }
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    listenerList.remove(listener);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Directory)) {
      return false;
    }
    return directory.equals(((Directory) o).directory);
  }

  public File getPath() {
    return directory;
  }

  public void refresh() {
    subdirs = null;
    files = null;
  }

  public List<Directory> getSubDirs() throws FileNotFoundException {
    load();
    return subdirs;
  }

  public List<File> getFiles() throws FileNotFoundException {
    load();
    return files;
  }

  public boolean hasChanged() {
    return dirWatcher.refresh(directory);
  }

  public Directory getParent() {
    return parent;
  }

  public boolean isDir() {
    return directory.isDirectory();
  }

  public boolean isPDF() {
    return PDF_FILE_FILTER.accept(directory);
  }

  public boolean isHeroLabPortfolio() {
    return HERO_LAB_FILE_FILTER.accept(directory);
  }

  private void load() throws FileNotFoundException {
    if (files == null && subdirs == null) {
      if (!directory.exists() || !directory.isDirectory()) {
        files = new ArrayList<File>();
        subdirs = new ArrayList<Directory>();
        return;
      }
      File[] listFiles = directory.listFiles(fileFilter);
      if (listFiles == null)
        throw new FileNotFoundException("Invalid directory name: '" + directory.getPath() + "'");
      files = Collections.unmodifiableList(Arrays.asList(listFiles));
      File[] subdirList = directory.listFiles(DIRECTORY_FILTER);
      subdirs = new ArrayList<Directory>();
      for (int i = 0; i < subdirList.length; i++) {
        Directory newDir = newDirectory(subdirList[i], fileFilter);
        newDir.parent = this;
        subdirs.add(newDir);
      }
      Collections.sort(
          subdirs,
          new Comparator<Directory>() {
            public int compare(Directory d1, Directory d2) {
              // Lets sort by directories first, then Hero Lab Portfolios, then finally PDF's
              String name1 = d1.getPath().getName();
              String name2 = d2.getPath().getName();

              if (d1.isDir() && d2.isDir()) {
                return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
              } else if (!d1.isDir() && !d2.isDir()) {
                if ((d1.isPDF() && d2.isPDF())
                    || (d1.isHeroLabPortfolio() && d2.isHeroLabPortfolio())) {
                  return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
                } else if (d1.isPDF()) {
                  return 1;
                } else {
                  return -1;
                }
              } else if (d1.isDir()) {
                return -1;
              } else {
                return 1;
              }
            }
          });
      subdirs = Collections.unmodifiableList(subdirs);
    }
  }

  private static class HeroLabFileFilter implements FileFilter {
    private static final MagicNumberFileFilter MAGIC_NUMBER =
        new MagicNumberFileFilter(new byte[] {(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04});

    public boolean accept(File file) {
      if (file.isDirectory()) {
        return false;
      } else {
        String path = file.getAbsolutePath().toLowerCase();

        if (path.endsWith(".por") && MAGIC_NUMBER.accept(file)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  private static class DirectoryFileFilter implements FileFilter {
    public boolean accept(File pathname) {
      return pathname.isDirectory()
          || PDF_FILE_FILTER.accept(pathname)
          || HERO_LAB_FILE_FILTER.accept(pathname);
    }
  }

  protected Directory newDirectory(File directory, FilenameFilter fileFilter) {
    return new Directory(directory, fileFilter);
  }

  protected void firePropertyChangeEvent(PropertyChangeEvent event) {
    // Me
    for (PropertyChangeListener listener : listenerList) {
      listener.propertyChange(event);
    }
    // Propagate up
    if (parent != null) {
      parent.firePropertyChangeEvent(event);
    }
  }

  public static final Comparator<Directory> COMPARATOR =
      new Comparator<Directory>() {
        public int compare(Directory o1, Directory o2) {
          String filename1 = o1.getPath().getName();
          String filename2 = o2.getPath().getName();
          return filename1.compareToIgnoreCase(filename2);
        }
      };
}
