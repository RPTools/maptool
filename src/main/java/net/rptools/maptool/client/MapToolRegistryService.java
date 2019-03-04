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

import java.util.List;

public interface MapToolRegistryService {

  public static final int CODE_UNKNOWN = 0;
  public static final int CODE_OK = 1;
  public static final int CODE_COULD_CONNECT_BACK = 2;
  public static final int CODE_ID_IN_USE = 3;

  public int registerInstance(String id, int port, String version);

  public void unregisterInstance(int port);

  public String findInstance(String id);

  public List<String> findAllInstances();

  public boolean testConnection(int port);

  public void heartBeat(int port);

  public String getAddress();
}
