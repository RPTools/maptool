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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.server.ServerConfig;

public sealed interface ServerAddress {
  static int TIMEOUT_SECONDS = 3;

  /**
   * Resolves server names into a connectable server.
   *
   * @return null if the named server does not exist, the RemoteServerConfig otherwise.
   */
  @Nullable
  RemoteServerConfig findServer();

  @Nonnull
  URI toUri();

  @Nonnull
  default URI toHttpUrl() {
    return MapToolRegistry.getInstance().getRedirectURL(toUri());
  }

  record Registry(@Nonnull String serverName) implements ServerAddress {
    @Override
    @Nullable
    public RemoteServerConfig findServer() {
      return MapToolRegistry.getInstance().findInstance(serverName());
    }

    @Override
    @Nonnull
    public URI toUri() {
      try {
        return new URI("rptools-maptool+registry", null, null, -1, "/" + serverName(), null, null);
      } catch (URISyntaxException e) {
        throw new AssertionError(
            "Scheme and path are given and the path is absolute and there is no authority so this should be infallible",
            e);
      }
    }
  }

  record Lan(@Nonnull String serviceIdentifier) implements ServerAddress {
    @Override
    @Nullable
    public RemoteServerConfig findServer() {
      var finder = MapToolServiceFinder.getInstance();
      var future = new CompletableFuture<RemoteServerConfig>();
      MapToolServiceFinder.MapToolAnnouncementListener listener =
          (var id, var serverConfig) -> {
            if (id.equals(serviceIdentifier()) && !future.isDone()) {
              future.complete(serverConfig);
            }
          };
      finder.addAnnouncementListener(listener);
      finder.find();
      try {
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      } catch (TimeoutException | InterruptedException e) {
        return null;
      } catch (ExecutionException e) {
        throw new Error("Failed to find LAN service " + serviceIdentifier(), e);
      } finally {
        finder.removeAnnouncementListener(listener);
      }
    }

    @Override
    @Nonnull
    public URI toUri() {
      try {
        return new URI(
            "rptools-maptool+lan", null, null, -1, "/" + serviceIdentifier(), null, null);
      } catch (URISyntaxException e) {
        throw new AssertionError(
            "Scheme and path are given and the path is absolute and there is no authority so this should be infallible",
            e);
      }
    }
  }

  record Tcp(@Nonnull String address, int port) implements ServerAddress {
    @Override
    @Nonnull
    public RemoteServerConfig findServer() {
      return new RemoteServerConfig.Socket(
          address(), port() == -1 ? ServerConfig.DEFAULT_PORT : port());
    }

    @Override
    @Nonnull
    public URI toUri() {
      try {
        return new URI("rptools-maptool+tcp", null, address(), port(), "/", null, null);
      } catch (URISyntaxException e) {
        throw new AssertionError(
            "Scheme and path are given and the path is absolute and IP address authorities are all valid so this should be infallible",
            e);
      }
    }
  }

  /**
   * Parse
   *
   * @param s String that may be a maptool URI.
   * @return A Server Address
   * @throws IllegalArgumentException if the URI wasn't a MapTool URI or had invalid parameters
   * @throws URISyntaxException if the string isn't a URI.
   */
  @Nonnull
  static ServerAddress parse(@Nonnull String s)
      throws IllegalArgumentException, URISyntaxException {
    URI uri = new URI(s); // may throw URISyntaxException

    var scheme = uri.getScheme();
    var authority = uri.getAuthority();
    var path = uri.getPath();
    var host = uri.getHost();
    var port = uri.getPort();
    switch (scheme) {
      case "rptools-maptool+registry":
        if (authority != null) {
          throw new IllegalArgumentException(
              "rptools-maptool+registry URIs must not have an authority");
        }
        if (path == null
            || path.length() <= 1
            || !path.startsWith("/")
            || path.indexOf("/", 1) != -1) {
          throw new IllegalArgumentException(
              "rptools-maptool+registry URIs must have path of the form /serverName");
        }
        var serverName = path.substring(1);
        return new ServerAddress.Registry(serverName);

      case "rptools-maptool+lan":
        if (authority != null) {
          throw new IllegalArgumentException("rptools-maptool+lan URIs must not have an authority");
        }
        if (path == null
            || path.length() <= 1
            || !path.startsWith("/")
            || path.indexOf("/", 1) != -1) {
          throw new IllegalArgumentException(
              "rptools-maptool+lan URIs must have path of the form /lanID");
        }
        var serviceIdentifier = path.substring(1);
        return new ServerAddress.Lan(serviceIdentifier);

      case "rptools-maptool+tcp":
        if (host == null) {
          throw new IllegalArgumentException("rptools-maptool+tcp URIs must have a host");
        }
        if (path != null && !path.isEmpty() && !path.equals("/")) {
          throw new IllegalArgumentException(
              "rptools-maptool+tcp URIs must have no path or just /");
        }
        return new ServerAddress.Tcp(host, port);

      case null:
      default:
        throw new IllegalArgumentException(
            Objects.toString(scheme, "\"\"") + " is not a valid maptool URI scheme");
    }
  }
}
