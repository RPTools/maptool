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

import static org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.server.proto.AssetDto;
import net.rptools.maptool.server.proto.AssetDtoType;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/** Asset used in the campaign. */
public final class Asset {

  /** The type of {@code Asset}. */
  public enum Type {

    /** The {@code Asset} is an image. */
    IMAGE(false, "", Asset::createImageAsset), // extension is determined from format.
    /** The {@code Asset} is an audio file. */
    AUDIO(false, "", Asset::createAudioAsset), // extension is determined from format.
    /** The {@code Asset} is an HTML string. */
    HTML(true, "html", Asset::createHTMLAsset),
    /** The {@code Asset} is some generic data. */
    DATA(false, "data", Asset::createDataAssetType),
    /** The {@code Asset} is a Markdown file. */
    MARKDOWN(true, "md", Asset::createMarkdownAsset),
    /** The {@code Asset} is a JavaScript file. */
    JAVASCRIPT(true, "js", Asset::createJavaScriptAsset),
    /** The {@code Asset} is a css file. */
    CSS(true, "css", Asset::createCSSAsset),
    /** The {@code Asset} is a Generic Text file. */
    TEXT(true, "", Asset::createTextAsset),
    /** The {@code Asset} is a JSON file. */
    JSON(true, "json", Asset::createJsonAsset),
    /** The {@code Asset} is an XML file. */
    XML(true, "xml", Asset::createXMLAsset),
    /** The {@code Asset} is a PDF file. */
    PDF(false, "pdf", Asset::createPDFAsset),
    /** MapTool Drop In Library */
    MTLIB(false, "mtlib", Asset::createMTLibAsset),
    /** The {@code Asset} is not a supported type. */
    INVALID(false, "", Asset::createInvalidAssetType);

    /** Does it make sense to use this {@code Asset} as a {@link String}. */
    private final boolean stringType;

    /** Method uses to create an {@code Asset} of this type. */
    private final transient BiFunction<String, byte[], Asset> factory;

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

    /**
     * Gets the {@code Type} based on the {@link MediaType}.
     *
     * @param mediaType the {@link MediaType} to get the {@code Type} for.
     * @return the {@code Type}.
     */
    public static Type fromMediaType(MediaType mediaType) {
      return fromMediaType(mediaType, "");
    }

    /**
     * Gets the {@code Type} based on the {@link MediaType}.
     *
     * @param mediaType the {@link MediaType} to get the {@code Type} for.
     * @param filename the filename of the media being checked.
     * @return the {@code Type}.
     */
    public static Type fromMediaType(MediaType mediaType, String filename) {
      String contentType = mediaType.getType();

      String subType = mediaType.getSubtype();
      return switch (contentType) {
        case "audio" -> Type.AUDIO;
        case "image" -> Type.IMAGE;
        case "text" -> switch (subType) {
          case "html" -> Type.HTML;
          case "markdown", "x-web-markdown" -> Type.MARKDOWN;
          case "javascript" -> Type.JAVASCRIPT;
          case "css" -> Type.CSS;
          default -> Type.TEXT;
        };
        case "application" -> switch (subType) {
          case "pdf" -> Type.PDF;
          case "json" -> Type.JSON;
          case "javascript" -> Type.JAVASCRIPT;
          case "xml" -> Type.XML;
          case "zip" -> {
            if (filename != null && !filename.isEmpty()) {
              if (AddOnLibraryImporter.isAssetFileAddonLibrary(filename)) {
                yield Type.MTLIB;
              }
            }
            yield Type.INVALID;
          }

          default -> Type.INVALID;
        };
        default -> Type.INVALID;
      };
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

  /** Is this a broken asset or not. */
  private final transient boolean broken;

  /**
   * The value of the data as a {@link JsonElement} if it is of type JSON, otherwise {@code null}.
   */
  private final transient JsonElement json;

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
    return new Asset(
        null,
        name,
        image != null ? image : new byte[] {},
        Type.IMAGE,
        Type.IMAGE.getDefaultExtension(),
        false);
  }

  /**
   * Create an {@code Asset} for an audio file.
   *
   * @param name The name of the {@code Asset}.
   * @param audio The audio.
   * @return the {@code Asset} that represents the audio.
   */
  public static Asset createAudioAsset(String name, byte[] audio) {
    return new Asset(null, name, audio, Type.AUDIO, Type.AUDIO.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for an image.
   *
   * @param name The name of the {@code Asset}.
   * @param image The image that the {@code Asset} represents.
   * @return an {@code Asset} that represents the image.
   */
  public static Asset createImageAsset(String name, BufferedImage image) {
    return new Asset(name, image, false);
  }

  /**
   * Create an {@code Asset} for a PDF.
   *
   * @param name The name of the {@code Asset}.
   * @param pdf The pdf that the {@code Asset} represents.
   * @return an {@code Asset} that represents the pdf.
   */
  public static Asset createPDFAsset(String name, byte[] pdf) {
    return new Asset(null, name, pdf, Type.PDF, Type.PDF.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for markdown.
   *
   * @param name The name of the {@code Asset}.
   * @param markdown The markdown that the {@code Asset} represents.
   * @return an {@code Asset} that represents the markdown.
   */
  public static Asset createMarkdownAsset(String name, byte[] markdown) {
    return new Asset(
        null, name, markdown, Type.MARKDOWN, Type.MARKDOWN.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for JavaScript.
   *
   * @param name The name of the {@code Asset}.
   * @param javascript The JavaScript that the {@code Asset} represents.
   * @return an {@code Asset} that represents the JavaScript.
   */
  public static Asset createJavaScriptAsset(String name, byte[] javascript) {
    return new Asset(
        null, name, javascript, Type.JAVASCRIPT, Type.JAVASCRIPT.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for CSS.
   *
   * @param name The name of the {@code Asset}.
   * @param css The css that the {@code Asset} represents.
   * @return an {@code Asset} that represents the css.
   */
  public static Asset createCSSAsset(String name, byte[] css) {
    return new Asset(null, name, css, Type.CSS, Type.CSS.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for general text.
   *
   * @param name The name of the {@code Asset}.
   * @param text The text that the {@code Asset} represents.
   * @return an {@code Asset} that represents the text.
   */
  public static Asset createTextAsset(String name, byte[] text) {
    return new Asset(null, name, text, Type.TEXT, Type.TEXT.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for JSON.
   *
   * @param name The name of the {@code Asset}.
   * @param json The json that the {@code Asset} represents.
   * @return an {@code Asset} that represents the json.
   */
  public static Asset createJsonAsset(String name, byte[] json) {
    return new Asset(null, name, json, Type.JSON, Type.JSON.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for XML.
   *
   * @param name The name of the {@code Asset}.
   * @param xml The xml that the {@code Asset} represents.
   * @return an {@code Asset} that represents the xml.
   */
  public static Asset createXMLAsset(String name, byte[] xml) {
    return new Asset(null, name, xml, Type.XML, Type.XML.getDefaultExtension(), false);
  }

  /**
   * Create an {@code Asset} for an invalid type. The asset wont actually be created and saved only
   * a "broken image" for it.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data that the {@code Asset} represents.
   * @return an {@code Asset} that represents the data.
   */
  public static Asset createInvalidAssetType(String name, byte[] data) {
    return createBrokenImageAsset(name, new MD5Key(data));
  }

  /**
   * Create an {@code Asset} for representing a broken image.
   *
   * @param md5Key The Md5 sum of the {@code Asset}.
   * @return an {@code Asset} that represents a broken image.
   */
  public static Asset createBrokenImageAsset(MD5Key md5Key) {
    return new Asset(md5Key, BROKEN_IMAGE_NAME, new byte[] {}, Type.IMAGE, ".png", true);
  }

  /**
   * Create an {@code Asset} for representing a broken image.
   *
   * @param md5Key The Md5 sum of the {@code Asset}.
   * @return an {@code Asset} that represents a broken image.
   */
  public static Asset createBrokenImageAsset(String name, MD5Key md5Key) {
    return new Asset(md5Key, name, new byte[] {}, Type.IMAGE, ".png", true);
  }

  /**
   * Creates an {@code Asset} of some unknown data.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data that the {@code Asset} represents.
   * @return an {@code Asset} that represents the data.
   */
  public static Asset createDataAssetType(String name, byte[] data) {
    return new Asset(null, name, data, Type.DATA, Type.DATA.getDefaultExtension(), false);
  }

  /**
   * Creates an Asset detecting the type.
   *
   * @param name the name of the asset.
   * @param data the data for the asset.
   * @return the newly created asset.
   * @throws IOException if there is an error.
   */
  public static Asset createAssetDetectType(String name, byte[] data) throws IOException {
    MediaType mediaType = getMediaType(name, data);
    var factory = Type.fromMediaType(mediaType).getFactory();
    return factory.apply(name, data);
  }

  /**
   * Creates an Asset detecting the type.
   *
   * @param name the name of the asset.
   * @param data the data for the asset.
   * @param file the file for the asset (or null if no file).
   * @return the newly created asset.
   * @throws IOException if there is an error.
   */
  public static Asset createAssetDetectType(String name, byte[] data, File file)
      throws IOException {
    MediaType mediaType = getMediaType(name, data);
    var factory = Type.fromMediaType(mediaType, file.getPath()).getFactory();
    return factory.apply(name, data);
  }

  /**
   * Creates a HTML {@code Asset}.
   *
   * @param name The name of the {@code Asset}.
   * @param data The data for the {@code Asset}.
   * @return the HTML {@code Asset}.
   */
  public static Asset createHTMLAsset(String name, byte[] data) {
    return new Asset(null, name, data, Type.HTML, Type.HTML.getDefaultExtension(), false);
  }

  /**
   * Creates a MapTool Drop In Library {@code Asset}.
   *
   * @param namespace The namespace of the {@code Asset}.
   * @param data the data for the {@code Asset}.
   * @return the MapTool Drop In Library {@code Asset}.
   */
  static Asset createMTLibAsset(String namespace, byte[] data) {
    return new Asset(null, namespace, data, Type.MTLIB, Type.MTLIB.getDefaultExtension(), false);
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
   * Creates a new {@code Asset}.
   *
   * @param key the MD5 sum for the {@code Asset}, if {@code null} a new one will be calculated.
   * @param name The name of the {@code Asset}.
   * @param data The data for the {@code Asset}.
   * @param type The type of the {@code Asset}.
   * @param extension the extension for the {@code Asset}.
   * @param broken is the {@code Asset} broken.
   */
  private Asset(MD5Key key, String name, byte[] data, Type type, String extension, boolean broken) {
    assert data != null;
    this.data = Arrays.copyOf(data, data.length);
    this.name = name;
    this.type = type;
    this.broken = broken;

    md5Key = Objects.requireNonNullElseGet(key, () -> new MD5Key(this.data));

    if (type == Type.DATA) {
      this.extension = DATA_EXTENSION;
    } else if (extension == null || extension.isEmpty()) {
      this.extension = determineImageExtension();
    } else {
      this.extension = extension;
    }

    if (type.isStringType()) {
      dataAsString = new String(data);
    } else {
      dataAsString = null;
    }

    if (type == Type.JSON && dataAsString != null) {
      json = JsonParser.parseString(dataAsString);
    } else {
      json = null;
    }
  }

  /**
   * Creates a new {@code Asset} for an image.
   *
   * @param name The name of the {@code Asset}.
   * @param image The data for the {@code Asset}.
   * @param broken is the {@code Asset} broken.
   */
  private Asset(String name, BufferedImage image, boolean broken) {
    this.name = name;
    byte[] imageData = null;
    try {
      imageData = ImageUtil.imageToBytes(image);
    } catch (IOException e) {
      throw new AssertionError(e); // Shouldn't happen
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

    json = null;
    this.broken = broken;
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
  private Asset(MD5Key key, String name, byte[] data, String extension, Type type, boolean broken) {
    this.name = name;
    this.data = Arrays.copyOf(data, data.length);
    this.extension = extension;
    this.type = type;
    this.md5Key = key != null ? key : new MD5Key(this.data);
    this.broken = broken;

    if (type.isStringType()) {
      dataAsString = new String(data);
    } else {
      dataAsString = null;
    }

    if (type == Type.JSON && dataAsString != null) {
      json = JsonParser.parseString(dataAsString);
    } else {
      json = null;
    }
  }

  // for serialisation
  private Asset(MD5Key key, String name, String extension, Type type, boolean broken) {
    this.md5Key = key;
    this.name = name;
    this.extension = extension;
    this.type = type;
    data = new byte[0];
    dataAsString = null;
    json = null;
    this.broken = broken;
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
    return new Asset(recalcMd5 ? null : this.md5Key, this.name, data, type, extension, broken);
  }

  /**
   * Attempts to determine the extension for the data passed in.
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
   * Returns the data for the {@code Asset} as a {@link JsonElement}. If the {@code Asset} can't be
   * represented as a {@link JsonElement} then {@link IllegalStateException} will be thrown.
   *
   * @return the date as a {@link JsonElement}.
   * @throws IllegalStateException if this {@code Asset} cannot be represented as a {@link
   *     JsonElement}.
   */
  public JsonElement getDataAsJson() {
    if (json == null) {
      throw new IllegalStateException("Asset can't be represented as a JsonElement.");
    } else {
      return json;
    }
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
    try {
      var asset = Asset.createAssetDetectType(this.name, dataVal);
      if (asset.isBroken()) {
        return new Asset(this.md5Key, this.name, dataVal, this.extension, this.type, true);
      } else {
        return asset;
      }
    } catch (IOException e) {
      return new Asset(this.md5Key, this.name, dataVal, this.extension, this.type, false);
    }
  }

  private static MediaType getMediaType(String filename, TikaInputStream tis) throws IOException {
    Metadata metadata = new Metadata();
    metadata.set(RESOURCE_NAME_KEY, filename);
    try {
      TikaConfig tika = new TikaConfig();
      MediaType mediaType = tika.getDetector().detect(tis, metadata);

      /* Workaround for Tika seeing Javascript files as Matlab scripts */
      if ("text/x-matlab".equals(mediaType.toString())) {
        String ext = FilenameUtils.getExtension(filename);
        if ("js".equals(ext) || "javascript".equals(ext))
          mediaType = new MediaType("text", "javascript");
      }
      return mediaType;

    } catch (TikaException e) {
      throw new IOException(e);
    }
  }

  /**
   * Detects and returns the {@link MediaType} for a byte array.
   *
   * @param filename the name of the file that the bytes were read from.
   * @param bytes the bytes to determine the {@link MediaType} of.
   * @return the detected {@link MediaType}.
   * @throws IOException when an error occurs.
   */
  public static MediaType getMediaType(String filename, byte[] bytes) throws IOException {
    return getMediaType(filename, TikaInputStream.get(bytes));
  }

  /**
   * Detects and returns the {@link MediaType} for an {@link InputStream}.
   *
   * @param filename the name of the file that corresponds to the {@link InputStream}.
   * @param is the {@link InputStream} to determine the {@link MediaType} of.
   * @return the detected {@link MediaType}.
   * @throws IOException when an error occurs.
   */
  public static MediaType getMediaType(String filename, InputStream is) throws IOException {
    return getMediaType(filename, TikaInputStream.get(is));
  }

  /**
   * Detects and returns the {@link MediaType} for a {@link URL}.
   *
   * @param url the {@link URL} to determine the {@link MediaType} of.
   * @return the detected {@link MediaType}.
   * @throws IOException when an error occurs.
   */
  public static MediaType getMediaType(URL url) throws IOException {
    return getMediaType(url.getFile(), TikaInputStream.get(url));
  }

  /**
   * Returns if the {@code Asset} is broken or not.
   *
   * @return {@code true} if the {@code Asset} is broken.
   */
  private boolean isBroken() {
    return broken;
  }

  public static Asset fromDto(AssetDto dto) {
    var dtoData = dto.getData().toByteArray();
    var asset =
        new Asset(
            new MD5Key(dto.getMd5Key()),
            dto.getName(),
            dtoData,
            dto.getExtension(),
            Asset.Type.valueOf(dto.getType().name()),
            dtoData.length == 0);
    return asset;
  }

  public AssetDto toDto() {
    var builder =
        AssetDto.newBuilder()
            .setMd5Key(getMD5Key().toString())
            .setName(getName())
            .setExtension(getExtension())
            .setType(AssetDtoType.valueOf(getType().name()));

    if (getData() != null) {
      builder.setData(ByteString.copyFrom(data));
    }
    return builder.build();
  }
}
