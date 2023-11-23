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
package net.rptools.maptool.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.transferable.FileTransferableHandler;
import net.rptools.lib.transferable.GroupTokenTransferData;
import net.rptools.lib.transferable.ImageTransferableHandler;
import net.rptools.lib.transferable.MapToolTokenTransferData;
import net.rptools.lib.transferable.TokenTransferData;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.mime.MediaType;

/**
 * A helper class for converting Transferable objects into their respective data types. This class
 * hides the details of drag/drop protocols as much as possible and therefore contains
 * platform-dependent checks (such as the URI_LIST_FLAVOR hack needed for Linux).
 *
 * <p><b>Note:</b> Drag-n-drop operations cannot be properly debugged by setting breakpoints at
 * random locations. For example, once a drop operation occurs the code must run to a point where
 * getTransferData() has been called as the JRE is maintaining some of the state internally and
 * hitting a breakpoint disturbs that state. (For instance the JRE only allows a single drag
 * operation at a time -- how could there be more? -- so a global structure is used to record drag
 * information and some of the fields are queried from the peer component which may be
 * time-sensitive.)
 *
 * @author tcroft
 */
public class TransferableHelper extends TransferHandler {
  private static final long serialVersionUID = 6019141249887841907L;

  private static final Logger log = LogManager.getLogger(TransferableHelper.class);

  /**
   * <b>text/uri-list; class=java.lang.String</b>
   *
   * <p>This is a JRE bug on Linux; the JRE <i>should</i> be providing DataFlavor.javaFileListFlavor
   * but doesn't. :(
   */
  private static final DataFlavor URI_LIST_FLAVOR =
      new DataFlavor("text/uri-list; class=java.lang.String", "Image"); // $NON-NLS-1$

  /**
   * <b>application/x-java-url; class=java.net.URL</b>
   *
   * <p>The best type of object to get is this one -- a URL -- since the representation of URLs is
   * universal
   */
  private static final DataFlavor URL_FLAVOR_URI =
      new DataFlavor(
          "application/x-java-url; class=java.net.URL", // $NON-NLS-1$
          "Image");

  /**
   * <b>image/x-java-image; class=java.awt.Image</b>
   *
   * <p>The next best type of object to get is this one, since the JRE has already recognized the
   * type of data
   */
  private static final DataFlavor X_JAVA_IMAGE =
      new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"); // $NON-NLS-1$

  /**
   * <b>text/plain; class=java.lang.String</b>
   *
   * <p>The last type of object to check for is text/plain. It's likely a URL -- or so we assume. :(
   */
  private static final DataFlavor URL_FLAVOR_PLAIN =
      new DataFlavor("text/plain; class=java.lang.String", "Image"); // $NON-NLS-1$

  /** Data flavors that this handler will support. */
  // @formatter:off
  public static final DataFlavor[] SUPPORTED_FLAVORS = {
    TransferableAsset.dataFlavor,
    TransferableAssetReference.dataFlavor,
    URL_FLAVOR_URI, // Prefer the real one (although this list isn't
    // necessarily scanned in order)
    X_JAVA_IMAGE,
    URL_FLAVOR_PLAIN,
    DataFlavor.javaFileListFlavor,
    URI_LIST_FLAVOR,
    TransferableToken.dataFlavor,
    MapToolTokenTransferData.MAP_TOOL_TOKEN_LIST_FLAVOR, // Is this appropriate?
    // never used herein...
    GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR,
  };

  // @formatter:on

  /**
   * Looks at a complete URL and tries to figure out which string within the URL might be the name
   * of an image.
   *
   * <p>It does this by looking for known filename extensions such as JPG, JPEG, and PNG to
   * determine where the end of the name might be, then works left from there looking for something
   * not normally part of a name. For example, in the query string of a URL it would stop looking at
   * an equal sign ("="), an ampersand ("&amp;"), a question mark ("?"), or a number sign ("#").
   *
   * @throws URISyntaxException
   */
  private static String findName(URL url) {
    String result = null;
    URI uri;
    try {
      // Try to use a URI, since the '%20' encoding will be automatically converted for us.
      uri = url.toURI();
      if (!StringUtil.isEmpty(uri.getQuery())) {
        result = findNameInThisPiece(uri.getQuery());
      } else if (!StringUtil.isEmpty(uri.getPath())) {
        result = findNameInThisPiece(uri.getPath());
      }
    } catch (URISyntaxException e) {
      // But if we can't make a URI work, fallback to just the URL.
      if (!StringUtil.isEmpty(url.getQuery())) {
        result = findNameInThisPiece(url.getQuery());
      } else if (!StringUtil.isEmpty(url.getPath())) {
        result = findNameInThisPiece(url.getPath());
      }
    }
    // If there is a query string, start there.
    return result;
  }

  private static Pattern extensionPattern = null;

  private static String findNameInThisPiece(String text) {
    if (extensionPattern == null) {
      String extensions[] = ImageIO.getReaderFileSuffixes();
      String list = Arrays.deepToString(extensions);
      // Final result is something like: (\w+\.(jpeg|jpg|png|gif|tiff))
      String pattern =
          "([^/\\\\]+\\." + list.replace('[', '(').replace(']', ')').replace(", ", "|") + ")\\b";
      extensionPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    Matcher m = extensionPattern.matcher(text);
    if (m.find()) return m.group();
    return null;
  }

  /**
   * Takes a drop event and returns an asset from it. Returns null if an asset could not be
   * obtained.
   *
   * @param transferable the {@link Transferable} from the drop event.
   * @return A list of assets transferred or {@code null} if it could not be obtained from transfer.
   */
  @SuppressWarnings("unchecked")
  public static List<Object> getAsset(Transferable transferable) {
    List<Object> assets = new ArrayList<Object>();
    try {
      Object o = null;
      // This *really* should be done using either the Strategy or Template patterns. Sigh.

      // EXISTING ASSET
      if (transferable.isDataFlavorSupported(TransferableAsset.dataFlavor)) {
        log.info("Selected: {}", TransferableAsset.dataFlavor);
        o = handleTransferableAsset(transferable);
      }
      if (o == null && transferable.isDataFlavorSupported(TransferableAssetReference.dataFlavor)) {
        log.info("Selected: {}", TransferableAssetReference.dataFlavor);
        o = handleTransferableAssetReference(transferable);
      }

      /**
       * Check for all InputStream types first?
       *
       * <p>This would allow an application to give us a data stream instead of, for example, a URL.
       * This could be significantly better for web browsers since they have already downloaded the
       * image anyway and could give us an InputStream connected to the cached data. But being
       * passed an InputStream is a bit of a pain since the MIME type can't be known in advance for
       * all possible applications. We'd need to loop through all of them {@link
       * #whichOnesWork(Transferable)} and look for ones that return InputStream. But how to choose
       * which of those to actually use?
       */

      // LOCAL FILESYSTEM
      // Used by Linux when files are dragged from the desktop. Other systems don't use this so
      // we're safe
      // checking for it first.
      // (Except Mac OS X 10.11 does appear to use it now, but textURIListToFileList() will fail as
      // the URIs can't
      // be converted
      // to URLs. This is why we check for the empty 'list' -- if it's empty, we can't use this
      // conversion and we
      // want 'o' to be
      // null for the following checks.)
      // Note that "text/uri-list" is considered a JRE bug and it should be converting the event
      // into
      // "text/x-java-file-list", but
      // until it does...
      if (o == null && transferable.isDataFlavorSupported(URI_LIST_FLAVOR)) {
        log.info("Selected: {}", URI_LIST_FLAVOR);
        String data = (String) transferable.getTransferData(URI_LIST_FLAVOR);
        List<URL> list = textURIListToFileList(data);
        if (!list.isEmpty()) {
          List<Object> urls = handleURLList(list);
          if (!urls.isEmpty()) o = urls;
        }
      }

      // LOCAL FILESYSTEM
      // Used by OSX (and Windows?) when files are dragged from the desktop: 'text/java-file-list;
      // java.util.List<java.io.File>'
      if (o == null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        log.info("Selected: {}", DataFlavor.javaFileListFlavor);
        List<URL> list = new FileTransferableHandler().getTransferObject(transferable);
        if (!list.isEmpty()) {
          List<Object> urls = handleURLList(list);
          if (!urls.isEmpty()) o = urls;
        }
      }

      // DIRECT/BROWSER
      // Try 'image/x-java-image; java.awt.Image' to see if Java has recognized the image as such
      if (o == null && transferable.isDataFlavorSupported(X_JAVA_IMAGE)) {
        log.info("Selected: {}", X_JAVA_IMAGE);
        BufferedImage image =
            (BufferedImage) new ImageTransferableHandler().getTransferObject(transferable);
        o = Asset.createImageAsset("unnamed", ImageUtil.imageToBytes(image));
      }

      // DIRECT/BROWSER
      // Try 'application/x-java-url; java.net.URL'
      if (o == null && transferable.isDataFlavorSupported(URL_FLAVOR_URI)) {
        log.info("Selected: {}", URL_FLAVOR_URI);
        URL url = (URL) transferable.getTransferData(URL_FLAVOR_URI);
        o = handleImage(url, "URL_FLAVOR_URI", transferable);
      }

      // DIRECT/BROWSER
      // It may be that the dropped object is a URL but is 'text/plain; java.lang.String' and URLs
      // are better than
      // other file types...
      if (o == null && transferable.isDataFlavorSupported(URL_FLAVOR_PLAIN)) {
        log.info("Selected: {}", URL_FLAVOR_PLAIN);
        String text = (String) transferable.getTransferData(URL_FLAVOR_PLAIN);
        URL url = new URL(text);
        o = handleImage(url, "URL_FLAVOR_PLAIN", transferable);
      }
      if (o != null) {
        if (o instanceof List<?>) assets = (List<Object>) o;
        else assets.add(o);
      }
    } catch (Exception e) {
      MapTool.showError("TransferableHelper.error.unrecognizedAsset", e); // $NON-NLS-1$
      return null;
    }
    if (assets == null || assets.isEmpty()) {
      return null;
    }
    for (Object working : assets) {
      if (working instanceof Asset asset) {
        if (!asset.getMD5Key().equals(AssetManager.BAD_ASSET_LOCATION_KEY)) {
          if (!AssetManager.hasAsset(asset)) {
            AssetManager.putAsset(asset);
          }
          if (!MapTool.getCampaign().containsAsset(asset)) {
            MapTool.serverCommand().putAsset(asset);
          }
        }
      }
    }
    return assets;
  }

  private static List<URL> textURIListToFileList(String data) {
    List<URL> list = new ArrayList<URL>(4);
    for (StringTokenizer st = new StringTokenizer(data, "\r\n");
        st.hasMoreTokens(); ) { // $NON-NLS-1$
      String s = st.nextToken();
      if (s.startsWith("#")) { // $NON-NLS-1$
        // the line is a comment (as per RFC 2483)
        continue;
      }
      try {
        URI uri = new URI(s);
        URL url = uri.toURL();
        list.add(url);
      } catch (Exception e) {
        // There's no reason to trap the individual exceptions when a single catch suffices.
        log.info(s, e);
        // } catch (URISyntaxException e) { // Thrown by the URI constructor
        // e.printStackTrace();
        // } catch (IllegalArgumentException e) { // Thrown by URI.toURL()
        // e.printStackTrace();
        // } catch (MalformedURLException e) { // Thrown by URI.toURL()
        // e.printStackTrace();
      }
    }
    return list;
  }

  private static Asset handleImage(URL url, String type, Transferable transferable)
      throws IOException, UnsupportedFlavorException {
    BufferedImage image = null;
    Asset asset = null;
    try {
      log.debug("Reading URL:  {}", url); // $NON-NLS-1$
      image = ImageIO.read(url);
    } catch (Exception e) {
      MapTool.showError("TransferableHelper.error.urlFlavor", e); // $NON-NLS-1$
    }
    if (image == null) {
      log.debug(
          "{} didn't work; trying ImageTransferableHandler().getTransferObject()",
          type); // $NON-NLS-1$
      image = (BufferedImage) new ImageTransferableHandler().getTransferObject(transferable);
    }
    if (image != null) {
      String name = findName(url);
      asset =
          Asset.createImageAsset(name != null ? name : "unnamed", ImageUtil.imageToBytes(image));
    } else {
      throw new IllegalArgumentException("cannot convert drop object to image: " + url.toString());
    }
    return asset;
  }

  // private static Asset handleImage(Transferable transferable) throws IOException,
  // UnsupportedFlavorException {
  // String name = null;
  // BufferedImage image = null;
  // if (transferable.isDataFlavorSupported(URL_FLAVOR_PLAIN)) {
  // try {
  // String fname = (String) transferable.getTransferData(URL_FLAVOR_PLAIN);
  // if (log.isDebugEnabled())
  // log.debug("Transferable " + fname); //$NON-NLS-1$
  // name = FileUtil.getNameWithoutExtension(fname);
  //
  // File file;
  // URL url = new URL(fname);
  // try {
  // URI uri = url.toURI(); // Should replace '%20' sequences and such
  // file = new File(uri);
  // } catch (URISyntaxException e) {
  // file = new File(fname);
  // }
  // if (file.exists()) {
  // if (log.isDebugEnabled())
  // log.debug("Reading local file: " + file); //$NON-NLS-1$
  // image = ImageIO.read(file);
  // } else {
  // if (log.isDebugEnabled())
  // log.debug("Reading remote URL: " + url); //$NON-NLS-1$
  // image = ImageIO.read(url);
  // }
  // } catch (Exception e) {
  // MapTool.showError("TransferableHelper.error.urlFlavor", e); //$NON-NLS-1$
  // }
  // }
  // if (image == null) {
  // if (log.isDebugEnabled())
  // log.debug("URL_FLAVOR_PLAIN didn't work; trying
  // ImageTransferableHandler().getTransferObject()"); //$NON-NLS-1$
  // image = (BufferedImage) new ImageTransferableHandler().getTransferObject(transferable);
  // }
  // Asset asset = new Asset(name, ImageUtil.imageToBytes(image));
  // return asset;
  // }

  private static List<Object> handleURLList(List<URL> list) throws Exception {
    List<Object> assets = new ArrayList<Object>();
    for (URL url : list) {
      // A JFileChooser (at least under Linux) sends a couple empty filenames that need to be
      // ignored.
      if (!url.getPath().equals("")) { // $NON-NLS-1$
        if (Token.isTokenFile(url.getPath())) {
          // Loading the token causes the assets to be added to the AssetManager
          // so it doesn't need to be added to our List here. In fact, getAsset()
          // will strip out anything in the List that isn't an Asset anyway...
          Token token = PersistenceUtil.loadToken(url);
          assets.add(token);
        } else if (AddOnLibraryImporter.isAddOnLibrary(url.getPath())) {
          Asset temp = AssetManager.createAsset(url, Type.MTLIB);
          if (temp != null) { // `null' means no image available
            assets.add(temp);
          } else {
            log.info("Invalid MTLib for {}", url);
          }
        } else {
          // Get the MediaType so we can use it when creating the Asset later
          MediaType mediaType = Asset.getMediaType(url);

          if (Asset.Type.fromMediaType(mediaType) == Type.INVALID) {
            MapTool.showError("dragdrop.unsupportedType");
            log.info("Unsupported file type: " + mediaType.toString() + " (" + url + ")");
            assets.add(AssetManager.getAsset(AssetManager.BAD_ASSET_LOCATION_KEY));
          } else {
            Asset temp = AssetManager.createAsset(url);
            if (temp != null) { // `null' means no image available
              assets.add(temp);
            } else {
              log.info("No image available for {}", url);
            }
          }
        }
      }
    }
    return assets;
  }

  private static boolean checkValidType(MediaType mediaType) {
    String contentType = mediaType.getType();

    String subType = mediaType.getSubtype();
    return switch (contentType) {
      case "audio", "image" -> true;
      case "text" -> switch (subType) {
        case "html", "markdown", "x-web-markdown", "plain", "javascript", "css" -> true;
        default -> false;
      };
      case "application" -> switch (subType) {
        case "pdf", "json", "javascript", "xml" -> true;
        default -> false;
      };
      default -> false;
    };
  }

  private static Asset handleTransferableAssetReference(Transferable transferable)
      throws Exception {
    return AssetManager.getAsset(
        (MD5Key) transferable.getTransferData(TransferableAssetReference.dataFlavor));
  }

  private static Asset handleTransferableAsset(Transferable transferable) throws Exception {
    return (Asset) transferable.getTransferData(TransferableAsset.dataFlavor);
  }

  /**
   * Get the tokens from a token list data flavor.
   *
   * @param transferable The data that was dropped.
   * @return The tokens from the data or <code>null</code> if this isn't the proper data type.
   */
  @SuppressWarnings("unchecked")
  public static List<Token> getTokens(Transferable transferable) {
    List<Token> tokens = null;
    try {
      Object df = transferable.getTransferData(GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR);
      List<TokenTransferData> tokenMaps = (List<TokenTransferData>) df;
      tokens = new ArrayList<Token>();
      for (Object object : tokenMaps) {
        if (!(object instanceof TokenTransferData)) continue;
        TokenTransferData td = (TokenTransferData) object;
        if (td.getName() == null || td.getName().trim().length() == 0 || td.getToken() == null)
          continue;
        tokens.add(new Token(td));
      } // endfor
      if (tokens.size() != tokenMaps.size()) {
        final int missingTokens = tokenMaps.size() - tokens.size();
        final String message =
            I18N.getText(
                "TransferableHelper.warning.tokensAddedAndExcluded",
                tokens.size(), // $NON-NLS-1$
                missingTokens);
        // if (EventQueue.isDispatchThread())
        // System.out.println("Yes, we are on the EDT already.");
        SwingUtilities.invokeLater(() -> MapTool.showWarning(message));
      } // endif
    } catch (IOException e) {
      MapTool.showError("TransferableHelper.error.ioException", e); // $NON-NLS-1$
    } catch (UnsupportedFlavorException e) {
      MapTool.showError("TransferableHelper.error.unsupportedFlavorException", e); // $NON-NLS-1$
    }
    return tokens;
  }

  public static boolean isSupportedAssetFlavor(Transferable transferable) {
    return transferable.isDataFlavorSupported(TransferableAsset.dataFlavor)
        || transferable.isDataFlavorSupported(TransferableAssetReference.dataFlavor)
        || transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        || transferable.isDataFlavorSupported(URI_LIST_FLAVOR)
        || transferable.isDataFlavorSupported(URL_FLAVOR_PLAIN);
  }

  public static boolean isSupportedTokenFlavor(Transferable transferable) {
    return transferable.isDataFlavorSupported(GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR)
        || transferable.isDataFlavorSupported(TransferableToken.dataFlavor);
  }

  /**
   * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
   *     java.awt.datatransfer.DataFlavor[])
   */
  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    for (DataFlavor supportedFlavor : SUPPORTED_FLAVORS) {
      for (DataFlavor transferFlavor : transferFlavors) {
        if (supportedFlavor.equals(transferFlavor)) return true;
      }
    }
    return false;
  }

  /** The tokens to be loaded onto the renderer when we get a point */
  List<Token> tokens;

  /** Whether or not each token needs additional configuration (set footprint, guess shape). */
  List<Boolean> configureTokens;

  /**
   * Retrieves a list of DataFlavors from the passed in Transferable, then tries to actually
   * retrieve an object from the drop event using each one.
   *
   * <p>Theoretically at least one should always work. But this can backfire as some data sources
   * may not support retreiving the object more than once. (Think of a data source such as
   * unidirectional pipe.)
   *
   * @param t Transferable to check
   * @return a list of all DataFlavor objects that succeeded
   */
  // TODO The result is always ignored, this method is just used for informational logging now.
  private static List<DataFlavor> whichOnesWork(Transferable t) {
    List<DataFlavor> worked = new ArrayList<DataFlavor>();

    // On OSX Java6, any data flavor that uses java.nio.ByteBuffer or an array of bytes
    // appears to output the object to the console (via System.out?). Geez, can't
    // Apple even run a frakkin' grep against their code before releasing it?!
    // PrintStream old = null;
    // if (MapTool.MAC_OS_X) {
    // old = System.out;
    // setOnOff(null);
    // }
    for (DataFlavor flavor : t.getTransferDataFlavors()) {
      Object result = null;
      try {
        result = t.getTransferData(flavor);
      } catch (UnsupportedFlavorException ufe) {
        log.debug("Failed (UFE):  {}", flavor.toString()); // $NON-NLS-1$
      } catch (IOException ioe) {
        log.debug("Failed (IOE):  {}", flavor.toString()); // $NON-NLS-1$
      } catch (Exception e) {
        // System.err.println(e);
      }
      if (result != null) {
        for (Class<?> type : validTypes) {
          if (type.equals(result.getClass())) {
            worked.add(flavor);
            log.info("Possible: {} ({})", flavor, result); // $NON-NLS-1$
            break;
          }
        }
      }
    }
    // if (MapTool.MAC_OS_X)
    // setOnOff(old);
    return worked;
  }

  // private static void setOnOff(PrintStream old) {
  // System.setOut(old);
  // }

  private static final Class<?> validTypes[] = {
    java.lang.String.class, java.net.URL.class, java.util.List.class, java.awt.Image.class,
  };

  /**
   * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
   *     java.awt.datatransfer.Transferable)
   */
  @Override
  public boolean importData(JComponent comp, Transferable t) {
    if (tokens != null) {
      // tokens.clear(); // will not help with memory cleanup and we may see unmodifiable lists here
      tokens = null;
    }
    if (configureTokens != null) {
      // configureTokens.clear(); // will not help with memory cleanup and we may see unmodifiable
      // lists here
      configureTokens = null;
    }
    if (log.isInfoEnabled()) whichOnesWork(t);

    List<Object> assets = getAsset(t);
    if (assets != null) {
      tokens = new ArrayList<Token>(assets.size());
      configureTokens = new ArrayList<Boolean>(assets.size());
      // Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      for (Object working : assets) {
        if (working instanceof Asset asset) {
          if (asset.getType() == Type.MTLIB) {
            if (MapTool.getPlayer().isGM()) {
              try {
                var addOnLibrary = new AddOnLibraryImporter().importFromAsset(asset);
                new LibraryManager().reregisterAddOnLibrary(addOnLibrary);
                SwingUtilities.invokeLater(
                    () -> {
                      MapTool.showInformation(
                          I18N.getText("library.imported", addOnLibrary.getNamespace().join()));
                    });
              } catch (IOException e) {
                SwingUtilities.invokeLater(
                    () -> {
                      MapTool.showError(I18N.getText("library.import.error", asset.getName()), e);
                    });
              }
            } else {
              MapTool.showError(I18N.getText("library.import.error.notGM"));
            }
          } else {
            Token token = new Token(asset.getName(), asset.getMD5Key());
            // token.setName(MapToolUtil.nextTokenId(zone, token));
            tokens.add(token);
            // A token from an image asset needs additional configuration.
            configureTokens.add(true);
          }
        } else if (working instanceof Token) {
          Token token = new Token((Token) working);
          // token.setName(MapToolUtil.nextTokenId(zone, token));
          tokens.add(token);
          // A token from an .rptok file is already fully configured.
          configureTokens.add(false);
        }
      }
    } else {
      if (t.isDataFlavorSupported(TransferableToken.dataFlavor)) {
        try {
          // Make a copy so that it gets a new unique GUID
          tokens =
              Collections.singletonList(
                  new Token((Token) t.getTransferData(TransferableToken.dataFlavor)));
          // A token from the Resource Library is already fully configured.
          configureTokens = Collections.singletonList(Boolean.FALSE);
        } catch (Exception e) {
          log.error("while using TransferableToken.dataFlavor", e); // $NON-NLS-1$
        }
      } else if (t.isDataFlavorSupported(GroupTokenTransferData.GROUP_TOKEN_LIST_FLAVOR)) {
        tokens = getTokens(t);
        // Tokens from Init Tool all need to be configured.
        configureTokens = new ArrayList<Boolean>(tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
          configureTokens.add(true);
        }
      } else {
        MapTool.showWarning("TransferableHelper.warning.badObject"); // $NON-NLS-1$
      }
    }
    return tokens != null;
  }

  /**
   * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
   */
  @Override
  public int getSourceActions(JComponent c) {
    return NONE;
  }

  /**
   * @return Getter for tokens
   */
  public List<Token> getTokens() {
    return tokens;
  }

  /**
   * @param tokens Setter for tokens
   */
  public void setTokens(List<Token> tokens) {
    // This doesn't appear to be called from anywhere; this class simply makes assignments
    // to the instance member variable. Remove this method?
    this.tokens = tokens;
  }

  /**
   * @return Getter for configureTokens
   */
  public List<Boolean> getConfigureTokens() {
    return configureTokens;
  }

  /**
   * @param configureTokens Setter for configureTokens
   */
  public void setConfigureTokens(List<Boolean> configureTokens) {
    this.configureTokens = configureTokens;
  }
}
