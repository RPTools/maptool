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
package net.rptools.maptool.client.ui.htmlframe.content;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;
import org.jsoup.Jsoup;

/**
 * Class that represents the HTML content to be displayed in the HTML pane.
 *
 * <p>This class is can also used to preproces the HTML content.
 *
 * <p>This class is immutable and thread-safe.
 */
public class HTMLContent {

  /** The enumeration that represents the type of content. */
  private enum ContentType {
    /** The content is a string, possibly HTML. */
    STRING,
    /** HTML Content in a string */
    HTML,
    /** URL that points to the content. */
    URL,
    /** The asset that represents the content if it is a binary asset. */
    ASSET
  }

  /**
   * The content type that this object represents. This is used to determine how to handle the
   * content.
   *
   * @param str content as a string.
   * @param url URL that points to the content.
   * @param asset The asset that represents the content if it is a binary asset.
   * @param contentType The type of content.
   */
  private record Content(
      @Nullable String str, @Nullable URL url, @Nullable Asset asset, @Nonnull ContentType type) {

    /**
     * Constructor for the Content class if it is a string containing html.
     *
     * @param str content as a string.
     * @param isHtml true if the content is HTML, false otherwise or unknown.
     */
    public Content(@Nonnull String str, boolean isHtml) {
      this(str, null, null, isHtml ? ContentType.HTML : ContentType.STRING);
    }

    /**
     * Constructor for the Content class if it is a URL.
     *
     * @param url URL that points to the content.
     */
    public Content(@Nonnull URL url) {
      this(null, url, null, ContentType.URL);
    }

    /**
     * Constructor for the Content class if it is a binary asset.
     *
     * @param asset The asset that represents the content if it is a binary asset.
     */
    public Content(@Nonnull Asset asset) {
      this(null, null, asset, ContentType.ASSET);
    }
  }

  /** The content represented. */
  private Content content;

  /** Flag to indicate if the content has been preprocessed. */
  private final boolean preprocessed;

  /** The preprocessor operations to be applied to the HTML content. */
  private final Set<PreprocessorOperations> preprocessorOperations = new HashSet<>();

  /**
   * Constructor for the HTMLContent class.
   *
   * @param content The content represented.
   * @param processed flag to indicate if the coentent has been preprocessed.
   * @param preprocessorOperations the preprocessor operations to be applied to the HTML content
   */
  private HTMLContent(
      @Nullable Content content,
      boolean processed,
      Set<PreprocessorOperations> preprocessorOperations) {
    this.content = content;
    this.preprocessed = processed;
    this.preprocessorOperations.addAll(preprocessorOperations);
  }

  /**
   * Factory method to create an HTMLContent object from a string. It will try to determine if the
   * string is HTML or not, if you already know it is HTML, use {@link #htmlFromString(String)}
   * instead.
   *
   * @param str the String content to be displayed
   * @return an HTMLContent object
   */
  public static HTMLContent fromString(@Nonnull String str) {
    boolean isHtml = false;
    try {
      var mediaType = Asset.getMediaType("", str.getBytes(StandardCharsets.UTF_8));
      var assetType = Asset.Type.fromMediaType(mediaType);
      if (assetType == Asset.Type.HTML) {
        isHtml = true;
      }
    } catch (IOException e) {
      // Do nothing, treat as normal String
    }

    return new HTMLContent(new Content(str, isHtml), false, Set.of());
  }

  /**
   * Factory method to create an HTMLContent object from a string that is HTML.
   *
   * @param html the String content to be displayed
   * @return an HTMLContent object
   */
  public static HTMLContent htmlFromString(@Nonnull String html) {
    return new HTMLContent(new Content(html, true), false, Set.of());
  }

  /**
   * Factory method to create an HTMLContent object from a URL.
   *
   * @param url the URL of the HTML content
   * @return an HTMLContent object
   */
  public static HTMLContent fromURL(@Nonnull URL url) {
    return new HTMLContent(new Content(url), false, Set.of());
  }

  /**
   * Checks if the HTML content is a URL.
   *
   * @return true if the HTML content is a URL, false otherwise
   */
  public boolean isUrl() {
    return content.type == ContentType.URL;
  }

  /**
   * Checks if the HTML content is a string.
   *
   * @return true if the HTML content is a string, false otherwise
   */
  public boolean isHTMLString() {
    return content.type == ContentType.HTML;
  }

  /**
   * Returns the HTML content as a string. If the content is a URL it will return null.
   *
   * @return the HTML content as a string
   */
  public String getHtmlString() {
    return content.str;
  }

  /**
   * Returns the URL of the HTML content. If the content is a string it will return null.
   *
   * @return the URL of the HTML content
   */
  public URL getUrl() {
    return content.url;
  }

  /**
   * Checks if the HTML content is a binary asset.
   *
   * @return true if the HTML content is a binary asset, false otherwise
   */
  public boolean isBinaryAsset() {
    return content.type == ContentType.ASSET;
  }

  /**
   * Returns the Asset of the HTML content. If the content is not a binary asset, it will return
   * null.
   *
   * @return the Asset of the HTML content
   */
  public Asset getAsset() {
    return content.asset;
  }

  /**
   * Returns the HTML content as a data URL in base64 format.
   *
   * @return the HTML content as a data URL in base64 format.
   */
  public String getHtmlStringAsDataUrl() {
    if (isHTMLString()) {
      String encodedHtml =
          Base64.getEncoder().encodeToString(content.str.getBytes(StandardCharsets.UTF_8));
      return "data:text/html;base64," + encodedHtml;
    } else {
      throw new IllegalStateException("HTMLContent is not a string");
    }
  }

  /**
   * Adds a preprocessor operation to the HTML content. The operations will be applied when {@link
   * preprocess} is called.
   *
   * <p>This method can not be called after the content has been preprocessed.
   *
   * @return the HTMLContent object with the base URL preprocessor operation added.
   * @throws IllegalStateException if the HTML content has already been preprocessed.
   */
  public HTMLContent injectURLBase(@Nonnull URL baseUrl) {
    if (preprocessed) {
      throw new IllegalStateException("HTMLContent has already been preprocessed");
    }

    return addPreprocessorOperation(new InjectUrlBase(baseUrl.toExternalForm()));
  }

  /**
   * Appplies the preprocessor operations to the HTML content. If the content is a URL then it will
   * be fetched using the {@link #fetchContent()} method. If the content is a string but not html it
   * will return the same object without any preprocessing being applied.
   *
   * @param options The preprocessing options to apply.
   * @return the HTMLContent object with the Java Bridge ad base URL injected.
   * @throws IOException if an error occurs while fetching the HTML content from the URL.
   */
  public HTMLContent preprocess() throws IOException {
    if (preprocessed) {
      return this; // already preprocessed so return the same object
    }

    var htmlContent = this;

    if (isUrl()) {
      htmlContent = fetchContent();
    }

    if (!htmlContent.isHTMLString()) {
      return this; // if the content is not HTML, return the same object
    }

    var document = Jsoup.parse(htmlContent.getHtmlString());
    htmlContent.preprocessorOperations.stream()
        .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
        .forEach(op -> op.apply(document));

    var newHtml = document.html();
    return new HTMLContent(new Content(newHtml, true), true, preprocessorOperations);
  }

  /**
   * Checks if the HTML content has been preprocessed.
   *
   * @return true if the HTML content has been preprocessed, false otherwise
   */
  public boolean hasBeenPreprocessed() {
    return preprocessed;
  }

  /**
   * Adds the Preprocessor that will inject the Java Bridge and JavaScript API into the HTML
   * content.
   *
   * <p>This method can not be called after the content has been preprocessed.
   *
   * @return a HTMLContent object with the base Java Bridge and API preprocessor operation added.
   * @throws IllegalStateException if the HTML content has already been preprocessed.
   */
  public HTMLContent injectJSAPI() {
    if (preprocessed) {
      throw new IllegalStateException("HTMLContent has already been preprocessed");
    }

    return addPreprocessorOperation(new InjectJSAPi());
  }

  /**
   * Adds the preprocessor that will apply the pre processor that applies a "fix" for a vue app.
   *
   * @return the HTMLContentobject with the vue hack preprocessor operation added.
   */
  public HTMLContent injectVueHack() {
    if (preprocessed) {
      throw new IllegalStateException("HTMLContent has already been preprocessed");
    }

    return addPreprocessorOperation(new VueHack());
  }

  /**
   * Adds the Content security policy Preprocessor operation to the list of prepocessors to be run
   * when the {@link #preprocess()} is called.
   *
   * @return a HTMLContent object with the content security policy preprocessor added.
   * @throws IllegalStateException if the content has alread been preprocessed.
   */
  public HTMLContent InjectContentSecurityPolicy() {
    if (preprocessed) {
      throw new IllegalStateException("HTMLContent has already been preprocessed");
    }

    return addPreprocessorOperation(new InjectContentSecurityPolicy());
  }

  /**
   * This method creates a new {code HTMLContent} with the added PreprocessorOperation. If the {code
   * HTMLContent} alredy contains the PreprocessorOperation being added then it will return {code
   * this};
   *
   * @param op The PreprocessorOperation
   */
  private HTMLContent addPreprocessorOperation(@Nonnull PreprocessorOperations op) {
    if (preprocessorOperations.contains(op)) {
      return this; // already injected so return the same object
    }
    var newOps = new HashSet<>(preprocessorOperations);
    newOps.add(op);
    return new HTMLContent(content, preprocessed, newOps);
  }

  /**
   * Fetches the content from the URL.
   *
   * @return an HTMLContent object containing the HTML content.
   * @throws IOException if an error occurs while fetching the HTML content from the URL.
   * @throws IllegalStateException if the content is not a URL.
   */
  public HTMLContent fetchContent() throws IOException {
    if (!isUrl()) {
      throw new IllegalStateException("HTMLContent is not a URL");
    }
    try {
      Optional<Library> libraryOpt = new LibraryManager().getLibrary(content.url).get();
      if (libraryOpt.isEmpty()) {
        throw new IOException(
            I18N.getText("msg.error.html.loadingURL", content.url.toExternalForm()));
      }

      var library = libraryOpt.get();
      var assetKey = library.getAssetKey(content.url).get().orElse(null);
      // Check if the asset key is null, if so try reading the resource as a string from the
      // library
      if (assetKey == null) {
        String html = library.readAsString(content.url).get();
        if (html != null) {
          var mediaType = Asset.getMediaType("", html.getBytes(StandardCharsets.UTF_8));
          var assetType = Asset.Type.fromMediaType(mediaType);
          return new HTMLContent(
              new Content(html, assetType == Asset.Type.HTML),
              preprocessed,
              preprocessorOperations);
        }
      }

      var asset = AssetManager.getAsset(assetKey);
      if (asset != null) {
        if (asset.isStringAsset()) {
          return new HTMLContent(
              new Content(asset.getDataAsString(), asset.getType() == Asset.Type.HTML),
              preprocessed,
              preprocessorOperations);
        } else {
          return new HTMLContent(new Content(asset), preprocessed, preprocessorOperations);
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new IOException(e);
    }
    throw new IOException(I18N.getText("msg.error.html.loadingURL", content.url.toExternalForm()));
  }

  /**
   * Fetches the string content from the URL or returns the HTML string if the content is a string.
   *
   * @return a string containing the HTML content.
   * @throws IOException if an error occurs while fetching the HTML string from the URL.
   * @throws IllegalStateException if the URL points to a binary asset.
   */
  public String fetchString() throws IOException {
    if (isHTMLString()) {
      return content.str;
    } else {
      return fetchContent().getHtmlString();
    }
  }
}
