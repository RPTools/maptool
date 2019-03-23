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

import com.thoughtworks.xstream.annotations.XStreamConverter;
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

/** The binary representation of an image. */
public class Asset {
  public static final String DATA_EXTENSION = "data";

  private MD5Key id;
  private String name;
  private String extension;
  private String type;

  @XStreamConverter(AssetImageConverter.class)
  private byte[] image;

  protected Asset() {}

  public Asset(String name, byte[] image) {
    this.image = image;
    this.name = name;
    if (image != null) {
      this.id = new MD5Key(image);
      extension = null;
      getImageExtension();
    }
  }

  public Asset(String name, BufferedImage image) {
    try {
      this.image = ImageUtil.imageToBytes(image);
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.name = name;
    if (this.image != null) {
      this.id = new MD5Key(this.image);
      extension = null;
      getImageExtension();
    }
  }

  public Asset(MD5Key id) {
    this.id = id;
  }

  public MD5Key getId() {
    return id;
  }

  public void setId(MD5Key id) {
    this.id = id;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
    extension = null;
    getImageExtension();
  }

  public String getImageExtension() {
    if (extension == null) {
      extension = "";
      try {
        if (image != null && image.length >= 4) {
          InputStream is = new ByteArrayInputStream(image);
          ImageInputStream iis = ImageIO.createImageInputStream(is);
          Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
          if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(iis);
            extension = reader.getFormatName().toLowerCase();
          }
          // We can store more than images, eg HeroLabData in the form of a HashMap, assume this if
          // an image type can not be established
          if (extension.isEmpty()) extension = DATA_EXTENSION;
        }
      } catch (IOException e) {
        MapTool.showError("IOException?!", e); // Can this happen??
      }
    }
    return extension;
  }

  public String getName() {
    return name;
  }

  public boolean isTransfering() {
    return AssetManager.isAssetRequested(id);
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return id + "/" + name + "(" + (image != null ? image.length : "-") + ")";
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Asset)) {
      return false;
    }
    Asset asset = (Asset) obj;
    return asset.getId().equals(getId());
  }
}
