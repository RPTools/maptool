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
package net.rptools.maptool.client.swing;

import java.util.ArrayList;
import java.util.List;

/** @author trevor */
public class AnimationManager {

  private static List<Animatable> animatableList = new ArrayList<Animatable>();

  private static List<Animatable> removeList = new ArrayList<Animatable>();
  private static List<Animatable> addList = new ArrayList<Animatable>();

  private static int delay = 200;

  static {
    new AnimThread().start();
  }

  public static void addAnimatable(Animatable animatable) {

    synchronized (animatableList) {
      if (!animatableList.contains(animatable)) {
        addList.add(animatable);
      }
    }
  }

  public static void removeAnimatable(Animatable animatable) {

    synchronized (animatableList) {
      removeList.remove(animatable);
    }
  }

  private static class AnimThread extends Thread {

    public void run() {

      while (true) {

        if (animatableList.size() > 0) {}

        synchronized (animatableList) {
          animatableList.addAll(addList);
          addList.clear();

          for (Animatable animatable : animatableList) {
            animatable.animate();
          }

          animatableList.removeAll(removeList);
          removeList.clear();
        }

        try {
          Thread.sleep(delay);
        } catch (InterruptedException ie) {
          ie.printStackTrace();
        }
      }
    }
  }
}
