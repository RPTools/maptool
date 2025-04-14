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

import java.net.InetAddress;
import javax.annotation.Nonnull;
import net.tsc.servicediscovery.AnnouncementListener;
import net.tsc.servicediscovery.ServiceFinder;

/**
 * Abstraction over net.tsc.servicediscovery.ServiceFinder to hide MapTool-specific implementation
 * details.
 */
public class MapToolServiceFinder {
  public interface MapToolAnnouncementListener extends AnnouncementListener {
    public void serviceAnnouncement(@Nonnull String id, @Nonnull RemoteServerConfig.Socket config);

    default void serviceAnnouncement(
        @Nonnull String type, @Nonnull InetAddress address, int port, @Nonnull byte[] data) {
      serviceAnnouncement(
          new String(data), new RemoteServerConfig.Socket(address.getHostAddress(), port));
    }
  }

  @Nonnull private static MapToolServiceFinder instance = new MapToolServiceFinder();

  @Nonnull
  public static MapToolServiceFinder getInstance() {
    return instance;
  }

  @Nonnull private ServiceFinder finder;

  public MapToolServiceFinder() {
    finder = new ServiceFinder(AppConstants.SERVICE_GROUP);
  }

  public void addAnnouncementListener(@Nonnull MapToolAnnouncementListener listener) {
    finder.addAnnouncementListener(listener);
  }

  public void removeAnnouncementListener(@Nonnull MapToolAnnouncementListener listener) {
    finder.removeAnnouncementListener(listener);
  }

  public void find() {
    finder.find();
  }
}
