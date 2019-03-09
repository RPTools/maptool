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
package net.rptools.maptool.model;

import java.util.List;
import java.util.ListIterator;
import net.rptools.maptool.client.MapTool;

public class TextMessage {
  // Not an enum so that it can be hessian serialized
  public interface Channel {
    public static final int ALL = 0; // General message channel
    public static final int SAY = 1; // Player/character speech
    public static final int GM = 2; // GM visible only
    public static final int ME = 3; // Targeted to the current maptool client
    public static final int GROUP = 4; // All in the group
    public static final int WHISPER = 5; // To a specific player/character
  }

  private int channel;
  private final String target;
  private final String message;
  private final String source;
  private final List<String> transform;

  ////
  // CONSTRUCTION
  public TextMessage(
      int channel, String target, String source, String message, List<String> transformHistory) {
    this.channel = channel;
    this.target = target;
    this.message = message;
    this.source = source;
    this.transform = transformHistory;
  }

  public static TextMessage say(List<String> transformHistory, String message) {
    return new TextMessage(
        Channel.SAY, null, MapTool.getPlayer().getName(), message, transformHistory);
  }

  public static TextMessage gm(List<String> transformHistory, String message) {
    return new TextMessage(
        Channel.GM, null, MapTool.getPlayer().getName(), message, transformHistory);
  }

  public static TextMessage me(List<String> transformHistory, String message) {
    return new TextMessage(
        Channel.ME, null, MapTool.getPlayer().getName(), message, transformHistory);
  }

  public static TextMessage group(List<String> transformHistory, String target, String message) {
    return new TextMessage(
        Channel.GROUP, target, MapTool.getPlayer().getName(), message, transformHistory);
  }

  public static TextMessage whisper(List<String> transformHistory, String target, String message) {
    return new TextMessage(
        Channel.WHISPER, target, MapTool.getPlayer().getName(), message, transformHistory);
  }

  @Override
  public String toString() {
    return message;
  }

  /** Attempt to cut out any redundant information */
  public void compact() {
    if (transform != null) {
      String lastTransform = null;
      for (ListIterator<String> iter = transform.listIterator(); iter.hasNext(); ) {
        String value = iter.next();
        if (value == null
            || value.length() == 0
            || value.equals(lastTransform)
            || value.equals(message)) {
          iter.remove();
          continue;
        }
        lastTransform = value;
      }
    }
  }

  ////
  // PROPERTIES
  public int getChannel() {
    return channel;
  }

  public void setChannel(int c) {
    channel = c;
  }

  public String getTarget() {
    return target;
  }

  public String getMessage() {
    return message;
  }

  public String getSource() {
    return source;
  }

  public List<String> getTransformHistory() {
    return transform;
  }

  ////
  // CONVENIENCE
  public boolean isGM() {
    return channel == Channel.GM;
  }

  public boolean isMessage() {
    return channel == Channel.ALL;
  }

  public boolean isSay() {
    return channel == Channel.SAY;
  }

  public boolean isMe() {
    return channel == Channel.ME;
  }

  public boolean isGroup() {
    return channel == Channel.GROUP;
  }

  public boolean isWhisper() {
    return channel == Channel.WHISPER;
  }
}
