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
package net.rptools.lib.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SoundPlayer {

  private static ExecutorService playerThreadPool = Executors.newCachedThreadPool();
  private static AtomicInteger playerCount = new AtomicInteger();
  public static final String FILE_EXTENSION = "mp3";

  public static void play(File file) throws IOException {
    try {
      Player player = new Player(new FileInputStream(file));
      play(player);
    } catch (JavaLayerException jle) {
      throw new IOException(jle.toString());
    }
  }

  public static void play(URL url) throws IOException {
    try {
      Player player = new Player(url.openStream());
      play(player);
    } catch (JavaLayerException jle) {
      throw new IOException(jle.toString());
    }
  }

  public static void play(String sound) throws IOException {
    try {
      Player player = new Player(SoundPlayer.class.getClassLoader().getResourceAsStream(sound));
      play(player);
      player.close();
    } catch (JavaLayerException jle) {
      throw new IOException(jle.toString());
    } catch (NullPointerException npe) {
      throw new IOException("Could not find sound: " + sound);
    }
  }

  public static int stopAll() {
    int currentThreads = playerCount.get();
    playerThreadPool.shutdownNow();
    System.out.println("Shutdown? " + playerThreadPool.isShutdown());
    playerThreadPool.shutdown();
    System.out.println("Terminated? " + playerThreadPool.isTerminated());

    return currentThreads;
  }

  /** Wait for all sounds to stop playing (Mostly for testing purposes) */
  public static void waitFor() {

    while (playerCount.get() > 0) {
      try {
        synchronized (playerCount) {
          playerCount.wait();
        }
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }

  private static void play(final Player player) {
    playerCount.incrementAndGet();

    // Not sure how to use Future here?
    Future<?> f =
        playerThreadPool.submit(
            new Runnable() {
              public void run() {
                try {
                  player.play();
                  playerCount.decrementAndGet();
                  synchronized (playerCount) {
                    playerCount.notify();
                  }
                } catch (JavaLayerException jle) {
                  jle.printStackTrace();
                }
              }
            });
  }
}
