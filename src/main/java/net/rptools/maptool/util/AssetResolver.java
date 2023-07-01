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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;

/**
 * Utility Class to aid in resolving asset keys for images. It will resolve the asset key for the
 * following: lib:// URI asset:// URI image:token MD5 hash (as String)
 */
public class AssetResolver {

  /**
   * Returns the asset key for the specified URL.
   *
   * @param url the URL to get the asset key for.
   * @return the MD5key of the asset at the specified URL.
   */
  public Optional<MD5Key> getAssetKey(URL url) {
    Optional<Library> lib = null;
    try {
      lib = new LibraryManager().getLibrary(url).get();
      if (lib.isPresent()) {
        var asset = lib.get().getAssetKey(url).get();
        if (asset.isPresent()) {
          String key = asset.get().toString();
          return Optional.of(new MD5Key(key));
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * Returns the asset key for the specified location. Locations can be: lib:// URI asset:// URI
   * image:token MD5 hash (as String)
   *
   * @param location the location to get the asset key for.
   * @return the MD5key of the asset at the specified location.
   */
  public Optional<MD5Key> getAssetKey(String location) {
    if (location.startsWith("asset://")) {
      return getAssetKey(location.substring(8));
    } else if (location.toLowerCase().startsWith("lib:")) {
      try {
        return getAssetKey(new URI(location));
      } catch (URISyntaxException e) {
        return Optional.empty();
      }
    } else {
      return Optional.of(new MD5Key(location));
    }
  }

  /**
   * Returns the asset key for the specified URI.
   *
   * @param uri the URI to get the asset key for.
   * @return the MD5key of the asset at the specified URI.
   */
  public Optional<MD5Key> getAssetKey(URI uri) {
    try {
      return getAssetKey(uri.toURL());
    } catch (MalformedURLException e) {
      return Optional.empty();
    }
  }
}
