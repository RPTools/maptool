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
package net.rptools.maptool.client.ui.io;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/** @author crash */
public class ResolveLocalHostname {
  /**
   * Currently the parameter is unused. This routine there returns the ANY local address if it can,
   * or the local host address if it can't. It presumes that ANY is actually "0.0.0.0" but if the
   * underlying platform says it is, that's when it fallsback to using localhost.
   *
   * @param intendedDestination used to determine which NIC MapTool should bind to
   * @return
   * @throws UnknownHostException
   * @throws SocketException
   */
  public static InetAddress getLocalHost(InetAddress intendedDestination)
      throws UnknownHostException, SocketException {
    InetAddress inet = InetAddress.getByAddress(new byte[] {0, 0, 0, 0});
    if (inet.isAnyLocalAddress()) return inet;
    inet = InetAddress.getLocalHost();
    return inet;
  }
}
