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

import com.caucho.hessian.client.HessianProxyFactory;
import java.net.MalformedURLException;
import java.util.List;

public class MapToolRegistry {
  private static final String SERVICE_URL = "http://services.rptools.net/maptool_registry-1_3.php";
  private static MapToolRegistryService service;

  static {
    HessianProxyFactory factory = new HessianProxyFactory();
    factory.setChunkedPost(false);
    try {
      service = (MapToolRegistryService) factory.create(MapToolRegistryService.class, SERVICE_URL);
    } catch (MalformedURLException mue) {
      mue.printStackTrace();
    }
  }

  public static String findInstance(String id) {
    checkService();
    return service.findInstance(id);
  }

  public static List<String> findAllInstances() {
    checkService();
    return service.findAllInstances();
  }

  public static String getAddress() {
    checkService();
    return service.getAddress();
  }

  public static int registerInstance(String id, int port) {
    checkService();
    return service.registerInstance(id, port, MapTool.getVersion());
  }

  public static void unregisterInstance(int port) {
    checkService();
    service.unregisterInstance(port);
  }

  public static boolean testConnection(int port) {
    checkService();
    return service.testConnection(port);
  }

  public static void heartBeat(int port) {
    checkService();
    service.heartBeat(port);
  }

  private static void checkService() {
    if (service == null) {
      throw new RuntimeException("Service is not available");
    }
  }

  public static void main(String[] args) throws Exception {
    // long delay = 0;
    //
    // Thread.sleep(delay);
    // System.out.println("Register");
    // registerInstance("my test", 4444);
    //
    // Thread.sleep(delay);
    // System.out.println("Heartbeat");
    //
    // heartBeat(4444);
    //
    // Thread.sleep(delay);
    // System.out.println("Find: " + findInstance("my test"));
    //
    // Thread.sleep(delay);
    // System.out.println("RERegister");
    // registerInstance("my test", 4444);
    //
    // Thread.sleep(delay);
    // System.out.println("Find: " + findInstance("my test"));
    //
    // Thread.sleep(delay);
    // System.out.println("Find: " + findInstance("my test"));
    //
    // Thread.sleep(delay);
    // System.out.println("UnRegister");
    // unregisterInstance(4444);

    System.out.println("Test: " + testConnection(51234));
    System.out.println("All instances: " + findAllInstances());
  }
}
