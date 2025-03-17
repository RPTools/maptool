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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.rptools.maptool.client.MapToolRegistry;

public class NetUtil {
  private static final NetUtil instance = new NetUtil();

  @Nonnull
  public static String formatAddress(@Nonnull InetAddress addr) {
    return switch (addr) {
      case Inet4Address a -> a.getHostAddress();
      case Inet6Address a -> {
        var s = a.getHostAddress();
        int scopeCharIdx = s.indexOf("%");
        if (scopeCharIdx != -1) {
          s = s.substring(0, scopeCharIdx);
        }
        yield "[" + s + "]";
      }
      default ->
          throw new AssertionError(
              "Unable to predict how to format future Internet Protocol Addresses");
    };
  }

  @Nonnull
  public static NetUtil getInstance() {
    return instance;
  }

  @Nonnull private CompletableFuture<InetAddress> externalAddressFuture;

  public NetUtil() {
    externalAddressFuture = MapToolRegistry.getInstance().getAddressAsync();
  }

  /**
   * Get a cached result of requesting the external address from the registry.
   *
   * @return A future that resolves to the external address or null if indeterminate.
   */
  @Nonnull
  public CompletableFuture<InetAddress> getExternalAddress() {
    // Reuse the future if the last one didn't fail
    switch (externalAddressFuture.state()) {
      case Future.State.CANCELLED, Future.State.FAILED -> {
        externalAddressFuture = MapToolRegistry.getInstance().getAddressAsync();
      }
      default -> {}
    }

    return externalAddressFuture;
  }

  public record LocalAddresses(
      @Nonnull List<Inet4Address> ipv4, @Nonnull List<Inet6Address> ipv6) {}

  /**
   * Asynchronously get all local addresses in order of most reachable first.
   *
   * <p>This checks routability to www.rptools.net using the SSL port 443 from each address in
   * parallel and uses this and if it's link-local to sort the addresses in order of most reachable
   *
   * <p>This checks routability using the SSL port rather than IGMP (ping) or TCP port 7 so strict
   * firewalls won't cause false negatives.
   *
   * @return A future that resolves to record of possibly empty IPv4 addresses and IPv6 addresses.
   */
  @Nonnull
  public CompletableFuture<LocalAddresses> getLocalAddresses() {

    record AddressInfo<T extends InetAddress>(
        @Nonnull T address, boolean isRoutable, boolean isLinkLocal)
        implements Comparable<AddressInfo<T>> {

      /**
       * Get properties of the address necessary to sort in reachability order.
       *
       * <p>This adds to the list directly if this requires no network requests or adds a task to do
       * so to the tasks list.
       *
       * @param address The address to get info for
       * @param rptools The rptools address to check routability to
       * @param infos Output list to add info to
       * @param tasks Output list of tasks that should be awaited for results
       */
      static <T extends InetAddress> void getInfo(
          @Nonnull T address,
          @Nonnull T rptools,
          @Nonnull List<AddressInfo<T>> infos,
          @Nonnull List<Callable<Void>> tasks) {
        if (rptools == null) {
          infos.add(new AddressInfo<T>(address, true /*isRoutable*/, address.isLinkLocalAddress()));
          return;
        }

        tasks.add(
            () -> {
              boolean isRoutable = true;
              try (var s = new Socket(rptools, 443, address, 0)) {
              } catch (IOException | SecurityException | IllegalArgumentException e) {
                isRoutable = false;
              }
              infos.add(new AddressInfo<T>(address, isRoutable, address.isLinkLocalAddress()));
              return null;
            });
      }

      /** Order AddressInfos by reachability, more reachable before less */
      public int compareTo(AddressInfo<T> other) {
        if (isRoutable() != other.isRoutable()) {
          return isRoutable() ? -1 : 1;
        }
        return (isLinkLocal() ? 1 : 0) - (other.isLinkLocal() ? 1 : 0);
      }
    }

    return CompletableFuture.supplyAsync(
        () -> {
          // Get the appropriate IPv4/6 address for future reachability checks
          Inet4Address rptools4 = null;
          Inet6Address rptools6 = null;
          try {
            for (InetAddress addr : InetAddress.getAllByName("www.rptools.net")) {
              switch (addr) {
                case Inet4Address a -> {
                  if (rptools4 != null) {
                    continue;
                  }
                  rptools4 = a;
                }
                case Inet6Address a -> {
                  if (rptools6 != null) {
                    continue;
                  }
                  rptools6 = a;
                }
                default -> {
                  continue;
                }
              }

              if (rptools4 != null && rptools6 != null) {
                break;
              }
            }
          } catch (UnknownHostException ignore) {
          }
          // We might find we didn't resolve an address for our address family
          // but we can fall back to skipping the reachability check
          // since rptools.net not having an address doesn't imply we're unreachable.

          // Get all addresses, concurrently checking routability to rptools
          // and partition them into v4 and v6.
          var v4Infos = new ArrayList<AddressInfo<Inet4Address>>();
          var v6Infos = new ArrayList<AddressInfo<Inet6Address>>();
          var tasks = new ArrayList<Callable<Void>>();
          List<NetworkInterface> netInts;
          try {
            netInts = Collections.list(NetworkInterface.getNetworkInterfaces());
          } catch (SocketException e) {
            netInts = List.of();
          }
          for (NetworkInterface netInt : netInts) {
            try {
              if (!netInt.isUp() || netInt.isLoopback()) {
                continue;
              }
            } catch (SocketException e) {
              continue;
            }

            for (InetAddress inetAddress : Collections.list(netInt.getInetAddresses())) {
              if (inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()) {
                continue;
              }

              switch (inetAddress) {
                case Inet4Address a -> AddressInfo.getInfo(a, rptools4, v4Infos, tasks);
                case Inet6Address a -> AddressInfo.getInfo(a, rptools6, v6Infos, tasks);
                default -> {}
              }
            }
          }
          ForkJoinPool.commonPool().invokeAll(tasks);

          // Return addresses most reachable first
          v4Infos.sort(null);
          v6Infos.sort(null);
          return new LocalAddresses(
              v4Infos.stream().map(x -> x.address()).collect(Collectors.toList()),
              v6Infos.stream().map(x -> x.address()).collect(Collectors.toList()));
        });
  }
}
