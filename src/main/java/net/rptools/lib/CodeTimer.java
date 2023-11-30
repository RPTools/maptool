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
package net.rptools.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeTimer {
  private final Map<String, Timer> timeMap = new HashMap<>();
  private final Map<String, Integer> orderMap = new HashMap<>();
  private final String name;
  private boolean enabled;
  private int threshold = 1;

  public CodeTimer(String n) {
    name = n;
    enabled = true;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void start(String id) {
    if (!enabled) {
      return;
    }
    int count = orderMap.size();
    orderMap.putIfAbsent(id, count);
    Timer timer = timeMap.get(id);
    if (timer == null) {
      timer = new Timer();
      timeMap.put(id, timer);
    }
    timer.start();
  }

  public void stop(String id) {
    if (!enabled) {
      return;
    }
    if (!orderMap.containsKey(id)) {
      throw new IllegalArgumentException("Could not find orderMap id: " + id);
    }
    if (!timeMap.containsKey(id)) {
      throw new IllegalArgumentException("Could not find timer id: " + id);
    }
    timeMap.get(id).stop();
  }

  public void clear() {
    orderMap.clear();
    timeMap.clear();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(100);

    builder
        .append("Timer ")
        .append(name)
        .append(" (")
        .append(orderMap.size())
        .append(" elements)\n");

    List<String> idSet = new ArrayList<String>(timeMap.keySet());
    idSet.sort((arg0, arg1) -> orderMap.get(arg0) - orderMap.get(arg1));
    for (String id : idSet) {
      long elapsed = timeMap.get(id).getElapsed();
      if (elapsed < threshold) {
        continue;
      }
      builder.append(String.format("  %3d.  %6d ms  %s\n", orderMap.get(id), elapsed, id));
    }
    return builder.toString();
  }

  private static class Timer {
    long elapsed;
    long start = -1;

    public void start() {
      start = System.currentTimeMillis();
    }

    public void stop() {
      elapsed += (System.currentTimeMillis() - start);
      start = -1;
    }

    public long getElapsed() {
      long time = elapsed;
      if (start > 0) {
        time += (System.currentTimeMillis() - start);
      }
      return time;
    }
  }
}
