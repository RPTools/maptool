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
package net.rptools.maptool.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.MapTool;

/** Asset used in the campaign. */
public final class Asset {

  /**
   * The type of {@code Asset}.
   */
  public enum Type {
    IMAGE,
    DATA // Generic data
  };


  /** Extension to use for generic binary data. */
  public static final String DATA_EXTENSION = "data";

  /** The name of the broken image. */
  public static final String BROKEN_IMAGE_NAME = "broken";

  @XStreamAlias("id")   // Maintain comparability...
  /** The MD5 Sum of this {@code Asset}. */
  private final MD5Key md5Key;
  /** The name of the {@code Asset}. */
  private final String name;
  /** The file extension for the {@code Asset}. */
  private final String extension;
  /** The type of the {@code Asset}. */
  private final Type type;

  /** The data that makes up the {@code Asset}. */
  transient private final byte[] data;


  /**
   * Create an {@code Asset} for an image.
   *
   * @param name The name of the {@code Asset}.
   * @param image The image that the {@code Asset} represents.
   *
   * @return an {@code Asset} that represents the image.
   */
  public static Asset createImageAsset(String name, byte[] image) {
    return new Asset(null, name, image != null ? image : new byte[] {}, true);
  }

  /**
   * Create an {@code Asset} for an image.
   *
   * @param name The name of the {@code Asset}.
   * @param image The image that the {@code Asset} represents.
   *
   * @return an {@code Asset} that represents the image.
   */
  public static Asset createImageAsset(String name, BufferedImage image) {
    return new Asset(name, image);
  }

  /**
   * Create an {@code Asset} for representing a broken image.
   *
   * @param md5Key The Md5 sum of the {@code Asset}.
   *
   * @return an {@code Asset} that represents a broken image.
   */
  public static Asset createBrokenImageAsset(MD5Key md5Key) {
    return new Asset(md5Key, BROKEN_IMAGE_NAME, new byte[] {}, true);
  }

  /**
   * Creates an {@code Asset} of something that may or may not be an image.
   * It will first try to treat the data as an image, and fall back to a generic type if it can not.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data that the {@code Asset} represents.
   *
   * @return an {@code Asset} that represents the data.
   */
  public static Asset createUnknownAssetType(String name, byte[] data) {
    return createImageAsset(name, data);
  }


  /**
   * Creates a generic {@code Asset}.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data that the {@code Asset} represents.
   *
   * @return an {@code Asset} that represents the data.
   */
  public static Asset createAsset(String name, byte[] data) {
    return new Asset(null, name, data != null ? data : new byte[] {}, false);
  }

  /**
   * Creates a new {@code Asset}.
   *
   * @param key the MD5 sum for the {@code Asset}, if {@code null} a new one will be calculated.
   * @param name The name of the {@code Asset}.
   * @param data The data for the {@code Asset}.
   * @param isImage {@code true} if the data for this {@code Asset} is an image.
   */
  private Asset(MD5Key key, String name, byte[] data, boolean isImage) {
    assert data != null;
    this.data = data;
    this.name = name;
    if (key != null) {
      md5Key = key;
    } else {
      this.md5Key = new MD5Key(data);
    }

    if (isImage) {
      extension = determineImageExtension();
    } else {
      extension = DATA_EXTENSION;
    }

    if (extension.equals(DATA_EXTENSION)) {
      type = Type.DATA;
    } else {
      type = Type.IMAGE;
    }
  }

  /**
   * Creates a new {@code Asset} for an image.
   *
   * @param name The name of the {@code Asset}.
   * @param image The data for the {@code Asset}.
   */
  private Asset(String name, BufferedImage image) {
    this.name = name;
    try {
      this.data = ImageUtil.imageToBytes(image);
    } catch (IOException e) {
      throw new AssertionError(e); // Shouldn't happen
    }
    this.md5Key = new MD5Key(this.data);
    extension = determineImageExtension();

    if (extension.equals(DATA_EXTENSION)) {
      type = Type.DATA;
    } else {
      type = Type.IMAGE;
    }
  }

  /**
   * Returns the MD5 Sum of the {@code Asset}.
   * @return the MD5 Sum of the {@code Asset}.
   */
  public MD5Key getMD5Key() {
    return md5Key;
  }

  /**
   * Returns the data for this {@code Asset}.
   * @return the data for this {@code Asset}.
   */
  public byte[] getData() {
    return data; // This should be a defensive copy but that really blows out memory on load/save campaign
  }

  /**
   * Returns a new {@code Asset} with the passed in data and other details from this {@code Asset}.
   * @param data The new data for the new {@code Asset}.
   * @param recalcMd5 if {@code true} then a new Md5 sum will be calculated otherwise the existing will be used.
   * @return the new {@code Asset}.
   */
  public Asset setData(byte[] data, boolean recalcMd5) {
    return new Asset(recalcMd5 ? null : this.md5Key, this.name, data, true);
  }


  /**
   * Attempts to determine the extension for the image represented by the {@code Asset}. If it is
   * unable to be determined then the extension type for generic data {@link #DATA_EXTENSION} is
   * returned.
   *
   * @return the extension type for the image.
   */
  private String determineImageExtension() {
    String ext = "";
    try {
      if (data != null && data.length >= 4) {
        InputStream is = new ByteArrayInputStream(data);
        ImageInputStream iis = ImageIO.createImageInputStream(is);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (readers.hasNext()) {
          ImageReader reader = readers.next();
          reader.setInput(iis);
          ext = reader.getFormatName().toLowerCase();
        }
        // We can store more than images, eg HeroLabData in the form of a HashMap, assume this if
        // an image type can not be established
        if (ext.isEmpty()) {
          ext = DATA_EXTENSION;
        }
      }
    } catch (IOException e) {
      MapTool.showError("IOException?!", e); // Can this happen??
    }
    return ext;
  }

  /**
   * Returns the extension for the {@code Asset}.
   * @return the extension for the {@code Asset}.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Returns the name of the {@code Asset}.
   * @return the name of the {@code Asset}.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of the {@code Asset}.
   * @return the type of the {@code Asset}.
   */
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return md5Key + "/" + name + "(" + (data != null ? data.length : "-") + ")";
  }

  @Override
  public int hashCode() {
    return getMD5Key().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Asset)) {
      return false;
    }
    Asset asset = (Asset) obj;
    return asset.getMD5Key().equals(getMD5Key());
  }
}
