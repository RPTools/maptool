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
package net.rptools.lib.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

  private final int port;
  private boolean stop;
  private ServerSocket server;

  public EchoServer(int port) {
    this.port = port;
  }

  public synchronized void start() throws IOException {
    if (server != null) {
      return;
    }
    server = new ServerSocket(port);
    new ReceiveThread().start();
  }

  public synchronized void stop() {
    if (server == null) {
      return;
    }
    try {
      stop = true;
      server.close();
      server = null;
    } catch (IOException ioe) {
      // Since we're trying to kill it anyway
      ioe.printStackTrace();
    }
  }

  private class ReceiveThread extends Thread {
    @Override
    public void run() {
      try {
        while (!stop) {
          Socket clientSocket = server.accept();
          BufferedReader reader =
              new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
          PrintWriter writer =
              new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));

          String line = reader.readLine();
          while (line != null) {
            writer.println(line);
            writer.flush();
            line = reader.readLine();
          }
        }
      } catch (IOException e) {
        // Expected when the accept is killed
      } finally {
        server = null;
      }
    }
  }
}
