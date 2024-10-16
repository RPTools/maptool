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
package net.rptools.maptool.client.ui.addon.creator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.rptools.maptool.client.MapTool;

/**
 * This class is responsible for creating a .mtlib file from the contents of a directory.
 *
 * <p>The .mtlib file is a zip file that contains all the files in the directory.
 *
 * <p>The .mtlib file is used to install the add-on into MapTool.
 */
public class MTLibCreator {

  /** The path to the directory to create the .mtlib file from. */
  private final Path addOnPath;

  /** The output path for the .mtlib file. */
  private final Path outputPath;

  /** The name of the .mtlib file to create. */
  private final String fileName;

  /**
   * Create a new instance of the MTLibCreator.
   *
   * @param addOnPath The path to the directory to create the .mtlib file from.
   * @param outputPath The output path for the .mtlib file.
   * @param fileName The name of the .mtlib file to create.
   */
  public MTLibCreator(Path addOnPath, Path outputPath, String fileName) {
    this.addOnPath = addOnPath;
    this.outputPath = outputPath;
    this.fileName = fileName;
  }

  /** Create the .mtlib file. */
  public void create() {
    try (var zipOut = new ZipOutputStream(Files.newOutputStream(outputPath.resolve(fileName)))) {
      zipOut.setLevel(Deflater.BEST_COMPRESSION);
      var fileList = new HashSet<Path>();
      try (var pathStream = Files.find(addOnPath, Integer.MAX_VALUE, this::includeFile)) {
        pathStream.forEach(fileList::add);
      }
      for (Path path : fileList) {
        zipOut.putNextEntry(new ZipEntry(addOnPath.relativize(path).toString()));
        Files.copy(path, zipOut);
      }
    } catch (Exception e) {
      MapTool.showError("library.dialog.addon.errorCreatingMTLib", e);
    }
  }

  /**
   * Determine if a file should be included in the .mtlib file.
   *
   * @param path The path to the file.
   * @param att The attributes of the file.
   * @return True if the file should be included, false otherwise.
   */
  private boolean includeFile(Path path, BasicFileAttributes att) {
    var subPath = addOnPath.relativize(path);

    if (att.isDirectory()) {
      return false;
    }

    if (subPath.getNameCount() == 0) {
      return false;
    }

    if (subPath.getName(0).toString().startsWith(".")) {
      return false;
    }

    if (path.getFileName().toString().toLowerCase().endsWith(".mtlib")) {
      return false;
    }

    return true;
  }
}
