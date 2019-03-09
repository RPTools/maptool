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
package net.rptools.maptool.util;

import static org.apache.commons.io.FileUtils.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Custom Implementation of FileUtils.byteCountToDisplaySize to fix rounding bug
 * https://issues.apache.org/jira/browse/IO-373
 */
public class FileUtil {

  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  enum FileSize {
    EXABYTE("EB", ONE_EB_BI),
    PETABYTE("PB", ONE_PB_BI),
    TERABYTE("TB", ONE_TB_BI),
    GIGABYTE("GB", ONE_GB_BI),
    MEGABYTE("MB", ONE_MB_BI),
    KILOBYTE("KB", ONE_KB_BI),
    BYTE("bytes", BigInteger.ONE);

    private final String unit;
    private final BigInteger byteCount;

    FileSize(String unit, BigInteger byteCount) {
      this.unit = unit;
      this.byteCount = byteCount;
    }

    private String unit() {
      return unit;
    }

    private BigInteger byteCount() {
      return byteCount;
    }
  }

  /**
   * Formats a file's size into a human readable format
   *
   * @param fileSize the file's size as BigInteger
   * @return the size as human readable string
   */
  public static String byteCountToDisplaySize(final BigInteger fileSize) {

    String unit = FileSize.BYTE.unit;
    BigDecimal fileSizeInUnit = BigDecimal.ZERO;
    String val;

    for (FileSize fs : FileSize.values()) {
      BigDecimal size_bd = new BigDecimal(fileSize);
      fileSizeInUnit = size_bd.divide(new BigDecimal(fs.byteCount), 5, ROUNDING_MODE);
      if (fileSizeInUnit.compareTo(BigDecimal.ONE) >= 0) {
        unit = fs.unit;
        break;
      }
    }

    // always round so that at least 3 numerics are displayed (###, ##.#, #.##)
    if (fileSizeInUnit
            .divide(BigDecimal.valueOf(100.0), BigDecimal.ROUND_DOWN)
            .compareTo(BigDecimal.ONE)
        >= 0) {
      val = fileSizeInUnit.setScale(0, ROUNDING_MODE).toString();
    } else if (fileSizeInUnit
            .divide(BigDecimal.valueOf(10.0), BigDecimal.ROUND_DOWN)
            .compareTo(BigDecimal.ONE)
        >= 0) {
      val = fileSizeInUnit.setScale(1, ROUNDING_MODE).toString();
    } else {
      val = fileSizeInUnit.setScale(2, ROUNDING_MODE).toString();
    }

    // trim zeros at the end
    if (val.endsWith(".00")) {
      val = val.substring(0, val.length() - 3);
    } else if (val.endsWith(".0")) {
      val = val.substring(0, val.length() - 2);
    }

    return String.format("%s %s", val, unit);
  }

  /**
   * Formats a file's size into a human readable format
   *
   * @param fileSize the file's size as long
   * @return the size as human readable string
   */
  public static String byteCountToDisplaySize(final long fileSize) {
    return byteCountToDisplaySize(BigInteger.valueOf(fileSize));
  }

  /**
   * Formats a file's name into a proper canonical filename and strips invalid characters. Also
   * checks for duplicate file names, appending a _# where # increases until a unique name is found.
   *
   * @param filePath system path
   * @param fileName file's base without path or extension name
   * @param extension file extension
   * @return the File object with new name
   */
  public static File getCleanFileName(String filePath, String fileName, String extension) {
    File newFileName = new File(filePath + "/" + fileName + extension);

    try {
      newFileName = newFileName.getCanonicalFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (!extension.isEmpty()) {
      int count = 2;
      while (newFileName.exists()) {
        newFileName = new File(filePath + "/" + fileName + "_" + count++ + extension);
      }
    }

    try {
      if (newFileName.createNewFile()) {
        newFileName.delete();
      }
    } catch (IOException e) {
      System.out.println("Bad file name, replacing bad characters in: " + fileName + extension);
      fileName =
          fileName.replaceAll(
              "[^a-zA-Z0-9\\._ \\/`~!@#$%\\^&\\(\\)\\-=\\+\\[\\]\\{\\}',\\\\:]+", "_");
      newFileName = new File(filePath + "/" + fileName + extension);
    }

    return newFileName;
  }

  /**
   * Formats a file's name into a proper canonical filename and strips invalid characters. Also
   * checks for duplicate file names, appending a _# where # increases until a unique name is found.
   *
   * @param filePath system path
   * @param fileName file's base without path or extension name
   * @param extension file extension
   * @return the File object with new name
   */
  public static File cleanFileName(String path, String fileName, String extension) {
    File newFileName = new File(path, fileName + extension);

    try {
      newFileName = newFileName.getCanonicalFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      if (newFileName.createNewFile()) {
        newFileName.delete();
      }
    } catch (IOException e) {
      System.out.println("Bad file name, replacing bad characters in: " + fileName + extension);
      fileName =
          fileName.replaceAll(
              "[^a-zA-Z0-9\\._ \\/`~!@#$%\\^&\\(\\)\\-=\\+\\[\\]\\{\\}',\\\\:]+", "_");
      newFileName = new File(fileName + extension);
    }

    return newFileName;
  }

  public static File cleanFileName(String fileName, String extension) {
    return cleanFileName(null, fileName, extension);
  }
}
