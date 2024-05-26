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
package net.rptools.clientserver.simple.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import net.rptools.clientserver.simple.connection.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RouterTest {
  private final List<Connection> mockConnections = new ArrayList<>();
  private Router router;

  @BeforeEach
  public void setUp() {
    router = new Router();

    for (int i = 0; i < 3; ++i) {
      var connection = mock(Connection.class);
      mockConnections.add(connection);
      when(connection.getId()).thenReturn("connection-" + i);
      when(connection.isAlive()).thenReturn(true);

      router.addConnection(connection);
    }
  }

  @Test
  public void testGetConnection() {
    var connection = router.getConnection("connection-2");

    assertSame(mockConnections.get(2), connection);
  }

  @Test
  public void testSendMessage() {
    var message = new byte[] {0, 1, 2, 3, 4};

    router.sendMessage("connection-1", message);

    verify(mockConnections.get(0), never()).sendMessage(any(), any());
    verify(mockConnections.get(1)).sendMessage(null, message);
    verify(mockConnections.get(2), never()).sendMessage(any(), any());
  }

  @Test
  public void testSendMessageWithChannel() {
    var channel = "channel";
    var message = new byte[] {0, 1, 2, 3, 4};

    router.sendMessage("connection-1", channel, message);

    verify(mockConnections.get(0), never()).sendMessage(any(), any());
    verify(mockConnections.get(1)).sendMessage(channel, message);
    verify(mockConnections.get(2), never()).sendMessage(any(), any());
  }

  @Test
  public void testSendToUnknown() {
    var message = new byte[] {0, 1, 2, 3, 4};

    router.sendMessage("unknown", message);

    verify(mockConnections.get(0), never()).sendMessage(any());
    verify(mockConnections.get(1), never()).sendMessage(any());
    verify(mockConnections.get(2), never()).sendMessage(any());
  }

  @Test
  public void testSimpleBroadcast() {
    var message = new byte[] {0, 1, 2, 3, 4};

    router.broadcastMessage(message);

    verify(mockConnections.get(0)).sendMessage(message);
    verify(mockConnections.get(1)).sendMessage(message);
    verify(mockConnections.get(2)).sendMessage(message);
  }

  @Test
  public void testBroadcastWithExclude() {
    var message = new byte[] {0, 1, 2, 3, 4};

    router.broadcastMessage(new String[] {mockConnections.get(1).getId()}, message);

    verify(mockConnections.get(0)).sendMessage(message);
    verify(mockConnections.get(1), never()).sendMessage(any());
    verify(mockConnections.get(2)).sendMessage(message);
  }

  @Test
  public void testRedundantAddConnection() {
    var newConnection = mock(Connection.class);
    when(newConnection.getId()).thenReturn("connection-1");
    when(newConnection.isAlive()).thenReturn(true);

    router.addConnection(newConnection);

    var message = new byte[] {0, 1, 2, 3, 4};
    router.broadcastMessage(message);
    verify(mockConnections.get(0)).sendMessage(message);
    verify(mockConnections.get(1)).sendMessage(message);
    verify(mockConnections.get(2)).sendMessage(message);
    verify(newConnection, never()).sendMessage(any());
  }

  @Test
  public void testRemoveConnection() {
    router.removeConnection(mockConnections.get(1));

    var message = new byte[] {0, 1, 2, 3, 4};
    router.broadcastMessage(message);
    verify(mockConnections.get(0)).sendMessage(message);
    verify(mockConnections.get(1), never()).sendMessage(any());
    verify(mockConnections.get(2)).sendMessage(message);
  }

  @Test
  public void testRedundantRemoveConnection() {
    router.removeConnection(mockConnections.get(1));
    router.removeConnection(mockConnections.get(1));

    var message = new byte[] {0, 1, 2, 3, 4};
    router.broadcastMessage(message);
    verify(mockConnections.get(0)).sendMessage(message);
    verify(mockConnections.get(1), never()).sendMessage(any());
    verify(mockConnections.get(2)).sendMessage(message);
  }

  @Test
  public void testReapClients() {
    when(mockConnections.get(0).isAlive()).thenReturn(false);
    when(mockConnections.get(2).isAlive()).thenReturn(false);
    var message = new byte[] {0, 1, 2, 3, 4};

    var reaped = router.reapClients();
    assert reaped.contains(mockConnections.get(0));
    assert !reaped.contains(mockConnections.get(1));
    assert reaped.contains(mockConnections.get(2));

    router.broadcastMessage(message);
    verify(mockConnections.get(0), never()).sendMessage(any());
    verify(mockConnections.get(1)).sendMessage(message);
    verify(mockConnections.get(2), never()).sendMessage(any());
  }

  @Test
  public void testRemoveAll() {
    var message = new byte[] {0, 1, 2, 3, 4};

    var removed = router.removeAll();
    assert removed.contains(mockConnections.get(0));
    assert removed.contains(mockConnections.get(1));
    assert removed.contains(mockConnections.get(2));

    router.broadcastMessage(message);
    verify(mockConnections.get(0), never()).sendMessage(message);
    verify(mockConnections.get(1), never()).sendMessage(message);
    verify(mockConnections.get(2), never()).sendMessage(message);
  }
}
