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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.MapTool;

/** Asset used in the campaign. */
public final class Asset {

  /** The type of {@code Asset}. */
  public enum Type {

    /** The {@code Asset} is an image. */
    IMAGE(false, "", Asset::createImageAsset), // extension is determined from format.
    /** The {@code Asset} is a HTML string. */
    HTML(true, "html", Asset::createHTMLAsset),
    /** The {@code Asset} is some generic data. */
    DATA(false, "data", Asset::createUnknownAssetType);

    /** Does it make sense to use this {@code Asset} as a {@link String}. */
    private final boolean stringType;

    /** Method uses to create an {@code Asset} of this type. */
    private final BiFunction<String, byte[], Asset> factory;

    /** The default extension for this type. */
    private final String defaultExtension;

    /**
     * Creates a new {@code Asset.Type}.
     *
     * @param isString is this {@code Asset} representable as a {@link String}
     * @param extension the default extension for this {@code Asset} type.
     * @param factoryFunction the method used to create {@code Asset}s of this type.
     */
    Type(boolean isString, String extension, BiFunction<String, byte[], Asset> factoryFunction) {
      stringType = isString;
      defaultExtension = extension;
      factory = factoryFunction;
    }

    /**
     * Returns if this asset is representable as a {@link String}.
     *
     * @return {@code true} if this asset is representable as a {@link String}.
     */
    public boolean isStringType() {
      return stringType;
    }

    /**
     * Returns the method that can be used to create an {@code Asset} of this type.
     *
     * @return the method that can be used to create an {@code Asset} of this type.
     */
    public BiFunction<String, byte[], Asset> getFactory() {
      return factory;
    }

    /**
     * Returns the default extension for this {@code Asset} type, if there is no default extension
     * then an empty {@code String}. An empty {@code String} can mean no extension of extensions is
     * calculated from the data on {@code Asset} creation (as is the case for images).
     *
     * @return the default extension for this {@code Asset} type.
     */
    public String getDefaultExtension() {
      return defaultExtension;
    }
  }

  /** Extension to use for generic binary data. */
  public static final String DATA_EXTENSION = "data";

  /** The name of the broken image. */
  public static final String BROKEN_IMAGE_NAME = "broken";

  /** The MD5 Sum of this {@code Asset}. */
  @XStreamAlias("id") // Maintain comparability...
  private final MD5Key md5Key;
  /** The name of the {@code Asset}. */
  private final String name;
  /** The file extension for the {@code Asset}. */
  private final String extension;
  /** The type of the {@code Asset}. */
  private final Type type;

  /** The value of the data as a {@link String}. */
  private final transient String dataAsString;

  /** The data that makes up the {@code Asset}. */
  @XStreamConverter(AssetImageConverter.class)
  private final transient byte[] data;

  /**
   * Create an {@code Asset} for an image.
   *
   * @param name The name of the {@code Asset}.
   * @param image The image that the {@code Asset} represents.
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
   * @return an {@code Asset} that represents the image.
   */
  public static Asset createImageAsset(String name, BufferedImage image) {
    return new Asset(name, image);
  }

  /**
   * Create an {@code Asset} for representing a broken image.
   *
   * @param md5Key The Md5 sum of the {@code Asset}.
   * @return an {@code Asset} that represents a broken image.
   */
  public static Asset createBrokenImageAsset(MD5Key md5Key) {
    return new Asset(md5Key, BROKEN_IMAGE_NAME, new byte[] {}, true);
  }

  /**
   * Creates an {@code Asset} of something that may or may not be an image. It will first try to
   * treat the data as an image, and fall back to a generic type if it can not.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data that the {@code Asset} represents.
   * @return an {@code Asset} that represents the data.
   */
  public static Asset createUnknownAssetType(String name, byte[] data) {
    return createImageAsset(name, data);
  }

  /**
   * Creates a HTML {@code Asset}.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data for the {@code Asset}.
   * @return the HTML {@code Asset}.
   */
  public static Asset createHTMLAsset(String name, byte[] data) {
    return new Asset(null, name, data, "html", Type.HTML);
  }

  /**
   * Creates a new {@code Asset} of the specified type.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data for the {@code Asset}.
   * @param type The {@link Type} of the {@code Asset}.
   * @return the new {@code Asset}.
   */
  public static Asset createAsset(String name, byte[] data, Type type) {
    Type assetType = type != null ? type : Type.DATA;
    return assetType.getFactory().apply(name, data);
  }

  /**
   * Creates a generic {@code Asset}.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data that the {@code Asset} represents.
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
    this.data = Arrays.copyOf(data, data.length);
    this.name = name;
    if (key != null) {
      md5Key = key;
    } else {
      this.md5Key = new MD5Key(this.data);
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

    if (type.isStringType()) {
      dataAsString = new String(data);
    } else {
      dataAsString = null;
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
    byte[] imageData = null;
    try {
      imageData = ImageUtil.imageToBytes(image);
    } catch (IOException e) {
      // TODO CDW: throw new AssertionError(e); // Shouldn't happen
    }

    if (imageData != null) {
      this.data = imageData;
      extension = determineImageExtension();
    } else {
      this.data = new byte[0];
      extension = DATA_EXTENSION;
    }

    this.md5Key = new MD5Key(this.data);

    if (extension.equals(DATA_EXTENSION)) {
      type = Type.DATA;
    } else {
      type = Type.IMAGE;
    }

    if (type.isStringType()) {
      dataAsString = new String(data);
    } else {
      dataAsString = null;
    }
  }

  /**
   * Creates a new {@code Asset}.
   *
   * @param key The md5 sum of the {@code Asset} if {@code null} this will be calculated.
   * @param name The name of the {@code Asset}.
   * @param data The data for the {@code Asset}.
   * @param extension The extension for the {@code Asset}.
   * @param type The {@link Type} of the {@code Asset}.
   */
  private Asset(MD5Key key, String name, byte[] data, String extension, Type type) {
    this.name = name;
    this.data = Arrays.copyOf(data, data.length);
    this.extension = extension;
    this.type = type;
    this.md5Key = key != null ? key : new MD5Key(this.data);

    if (type.isStringType()) {
      dataAsString = new String(data);
    } else {
      dataAsString = null;
    }
  }

  /**
   * Returns the MD5 Sum of the {@code Asset}.
   *
   * @return the MD5 Sum of the {@code Asset}.
   */
  public MD5Key getMD5Key() {
    return md5Key;
  }

  /**
   * Returns the data for this {@code Asset}.
   *
   * @return the data for this {@code Asset}.
   */
  public byte[] getData() {
    return data; // This should be a defensive copy but that really blows out memory on load/save
    // campaign
  }

  /**
   * Returns a new {@code Asset} with the passed in data and other details from this {@code Asset}.
   *
   * @param data The new data for the new {@code Asset}.
   * @param recalcMd5 if {@code true} then a new Md5 sum will be calculated otherwise the existing
   *     will be used.
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
   *
   * @return the extension for the {@code Asset}.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Returns the name of the {@code Asset}.
   *
   * @return the name of the {@code Asset}.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of the {@code Asset}.
   *
   * @return the type of the {@code Asset}.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the data for the {@code Asset} as a {@link String}. If the {@code Asset} can't be
   * represented as a {@link String} then {@link IllegalStateException} will be thrown.
   *
   * @return the date for the {@code Asset} as {@link String}.
   * @throws IllegalStateException if this {@code Asset} cannot be represented as a {@link String}.
   */
  public String getDataAsString() {
    if (dataAsString == null) {
      throw new IllegalStateException("Asset can't be represented as a String.");
    }

    return dataAsString;
  }

  /**
   * Returns if the {@code Asset} be represented as a {@link String}.
   *
   * @return {@code true} if the {@code Asset} can be represented as a {@link String}.
   */
  public boolean isStringAsset() {
    return type.isStringType();
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

  /**
   * Used to ensure that transient fields are correctly initialized on read.
   *
   * @return replacement object on read.
   */
  Object readResolve() {
    byte[] dataVal;
    dataVal = Objects.requireNonNullElseGet(this.data, () -> new byte[0]);
    return new Asset(this.md5Key, this.name, dataVal, this.extension, this.type);
  }
}
