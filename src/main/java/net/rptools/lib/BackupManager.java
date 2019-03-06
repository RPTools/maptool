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
package net.rptools.lib;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BackupManager {

  private static final long DEFAULT_MAX_BACKUP_SIZE = 128 * 1024 * 1024; // megs

  private File backupDir;
  private long maxBackupSize;

  public BackupManager(File backupDir) throws IOException {
    this(backupDir, DEFAULT_MAX_BACKUP_SIZE);
  }

  public BackupManager(File backupDir, long maxBackupSize) throws IOException {
    this.backupDir = backupDir;
    this.maxBackupSize = maxBackupSize;

    backupDir.mkdirs();
  }

  /** The maximum number of bytes that the backup directory should use for backups */
  public void setMaxBackupSize(long size) {
    maxBackupSize = size;
  }

  public void backup(File file) throws IOException {

    // Active ?
    if (maxBackupSize < 1) {
      return;
    }

    // Enough room ?
    List<File> fileList = getFiles();
    long availableSpace = maxBackupSize - getUsedSpace();
    while (fileList.size() > 0 && file.length() > availableSpace) {
      File oldFile = fileList.remove(0);
      availableSpace += oldFile.length();
      oldFile.delete();
    }

    // Filename
    File newFile = new File(backupDir.getAbsolutePath() + "/" + file.getName());
    for (int count = 1; newFile.exists(); count++) {
      newFile = new File(backupDir.getAbsolutePath() + "/" + count + "_" + file.getName());
    }

    // Save
    FileUtil.copyFile(file, newFile);
  }

  /** List of existing backup files, with the oldest at the front */
  private List<File> getFiles() {

    List<File> fileList = new LinkedList<File>(Arrays.asList(backupDir.listFiles()));
    Collections.sort(
        fileList,
        new Comparator<File>() {
          public int compare(File o1, File o2) {

            return o1.lastModified() < o2.lastModified() ? -1 : 1;
          }
        });

    return fileList;
  }

  private long getUsedSpace() {
    long count = 0;
    for (File file : backupDir.listFiles()) {
      count += file.length();
    }
    return count;
  }

  // public static void main(String[] args) throws IOException {
  //
  // BackupManager mgr = new BackupManager(new File("/home/trevor/tmp/backup"));
  // mgr.setMaxBackupSize(35000);
  //
  // mgr.backup(new File("/home/trevor/tmp/applet.html"));
  //
  // System.out.println("Done");
  // }
}
