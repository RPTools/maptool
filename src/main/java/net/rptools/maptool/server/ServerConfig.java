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
package net.rptools.maptool.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.maptool.util.CipherUtil;

public class ServerConfig {
  public static final int DEFAULT_PORT = 51234;

  public static final int PORT_RANGE_START = 4000;
  public static final int PORT_RANGE_END = 20000;

  private static final String personalServerGMPassword;
  private static final SecretKeySpec personalServerGMKey;

  private static final String personalServerPlayerPassword;
  private static final SecretKeySpec personalServerPlayerKey;

  static {
    Random random = new Random();
    // Generate a random password for personal server
    personalServerGMPassword =
        random
            .ints('a', 'z' + 1)
            .limit(100)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    personalServerGMKey = CipherUtil.getInstance().createSecretKeySpec(personalServerGMPassword);
    String playerPass =
        random
            .ints('a', 'z' + 1)
            .limit(100)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    if (playerPass.equals(personalServerGMPassword)) { // super unlikely but just to play safe
      personalServerPlayerPassword = playerPass + "1";
    } else {
      personalServerPlayerPassword = playerPass;
    }
    personalServerPlayerKey =
        CipherUtil.getInstance().createSecretKeySpec(personalServerPlayerPassword);
  }

  private int port;
  private String hostPlayerId;
  private String gmPassword;
  private String playerPassword;
  private boolean personalServer;
  private String serverName;
  private SecretKeySpec playerPasswordKey;
  private SecretKeySpec gmPasswordKey;

  public static String getPersonalServerGMPassword() {
    return personalServerGMPassword;
  }

  public static SecretKeySpec getPersonalServerGMKey() {
    return personalServerGMKey;
  }

  public static String getPersonalServerPlayerPassword() {
    return personalServerPlayerPassword;
  }

  public static SecretKeySpec getPersonalServerPlayerKey() {
    return personalServerPlayerKey;
  }

  public ServerConfig() {
    playerPassword = getPersonalServerPlayerPassword();
    playerPasswordKey = getPersonalServerPlayerKey();
    gmPassword = getPersonalServerGMPassword();
    gmPasswordKey = getPersonalServerGMKey();
  }

  public ServerConfig(
      String hostPlayerId, String gmPassword, String playerPassword, int port, String serverName) {
    this.hostPlayerId = hostPlayerId;
    this.gmPassword = gmPassword;
    this.playerPassword = playerPassword;
    this.port = port;
    this.serverName = serverName;

    this.playerPasswordKey = CipherUtil.getInstance().createSecretKeySpec(this.playerPassword);
    this.gmPasswordKey = CipherUtil.getInstance().createSecretKeySpec(this.gmPassword);
  }

  public String getHostPlayerId() {
    return hostPlayerId;
  }

  public boolean isServerRegistered() {
    return serverName != null && serverName.length() > 0;
  }

  public String getServerName() {
    return serverName;
  }

  public boolean gmPasswordMatches(String password) {
    return safeCompare(gmPassword, password);
  }

  public boolean playerPasswordMatches(String password) {
    return safeCompare(playerPassword, password);
  }

  public boolean isPersonalServer() {
    return personalServer;
  }

  public int getPort() {
    return port;
  }

  public static ServerConfig createPersonalServerConfig() {
    ServerConfig config = new ServerConfig();
    config.personalServer = true;
    config.port = findOpenPort(PORT_RANGE_START, PORT_RANGE_END);
    return config;
  }

  public SecretKeySpec getPlayerPasswordKey() {
    return playerPasswordKey;
  }

  public SecretKeySpec getGMPasswordKey() {
    return gmPasswordKey;
  }

  private static Random r = new Random();

  private static int findOpenPort(int rangeLow, int rangeHigh) {
    // Presumably there will always be at least one open port between low and high
    while (true) {
      ServerSocket ss = null;
      try {
        int port = rangeLow + (int) ((rangeHigh - rangeLow) * r.nextFloat());
        ss = new ServerSocket(port);

        // This port was open before we took it, so we'll just close it and use this one
        // LATER: This isn't super exact, it's conceivable that another process will take
        // the port between our closing it and the server opening it. But that's the best
        // we can do at the moment until the server is refactored.
        return port;
      } catch (Exception e) {
        // Just keep trying
      } finally {
        if (ss != null) {
          try {
            ss.close();
          } catch (IOException ioe) {
            // No big deal
            ioe.printStackTrace();
          }
        }
      }
    }
  }

  private static boolean safeCompare(String s1, String s2) {
    if (s1 == null) {
      s1 = "";
    }
    if (s2 == null) {
      s2 = "";
    }

    return s1.equals(s2);
  }
}
