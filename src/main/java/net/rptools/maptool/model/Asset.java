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

import com.google.gson.JsonObject;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.util.ImageManager;

/** The binary representation of an image. */
public final class Asset {
  public static final String DATA_EXTENSION = "data";

  public static final String BROKEN_IMAGE_NAME = "broken";

  private final MD5Key md5Key;
  private final String name;
  private final String extension;
  private final String type = "image";

  private final byte[] data;


  public static Asset createImageAsset(String name, byte[] image) {
    return new Asset(null, name, image != null ? image : new byte[] {}, true);
  }

  public static Asset createImageAsset(String name, BufferedImage image) {
    return new Asset(name, image);
  }

  public static Asset createBrokenImageAsset(MD5Key md5Key) {
    return new Asset(md5Key, BROKEN_IMAGE_NAME, new byte[] {}, true);
  }

  public static Asset createUnknownAssetType(String name, byte[] data) {
    return createImageAsset(name, data);
  }


  public static Asset createAsset(String name, byte[] data) {
    return new Asset(null, name, data != null ? data : new byte[] {}, false);
  }

  private Asset(MD5Key key, String name, byte[] image, boolean isImage) {
    assert image != null;
    this.data = image;
    this.name = name;
    if (key != null) {
      md5Key = key;
    } else {
      this.md5Key = new MD5Key(image);
    }

    if (isImage) {
      extension = determineImageExtension();
    } else {
      extension = DATA_EXTENSION;
    }
  }

  private Asset(String name, BufferedImage image) {
    this.name = name;
    try {
      this.data = ImageUtil.imageToBytes(image);
    } catch (IOException e) {
      throw new AssertionError(e); // Shouldn't happen
    }
    this.md5Key = new MD5Key(this.data);
    extension = determineImageExtension();
  }




  public MD5Key getMD5Key() {
    return md5Key;
  }

  public byte[] getData() {
    return data;
    //return Arrays.copyOf(data, data.length);
  }

  public Asset setData(byte[] data) {
    return new Asset(this.md5Key, this.name, data, true);
  }


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

  public String getExtension() {
    return extension;
  }

  public String getName() {
    return name;
  }

  /**
   * Get the properties of the asset and put them in a JsonObject.
   *
   * @return the JsonObject with the properties.
   */
  public JsonObject getProperties() {
    JsonObject properties = new JsonObject();
    properties.addProperty("type", type);
    properties.addProperty("subtype", extension);
    properties.addProperty("id", md5Key.toString());
    properties.addProperty("name", name);

    Image img = ImageManager.getImageAndWait(md5Key); // wait until loaded, so width/height are correct
    String status = "loaded";
    if (img == ImageManager.BROKEN_IMAGE) {
      status = "broken";
    } else if (img == ImageManager.TRANSFERING_IMAGE) {
      status = "transferring";
    }
    properties.addProperty("status", status);
    properties.addProperty("width", img.getWidth(null));
    properties.addProperty("height", img.getHeight(null));
    return properties;
  }

  public boolean isTransfering() {
    return AssetManager.isAssetRequested(md5Key);
  }

  public String getType() {
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
