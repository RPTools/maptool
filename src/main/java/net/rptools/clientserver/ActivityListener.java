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
package net.rptools.clientserver;

/**
 * @author drice
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public interface ActivityListener {
  public static enum Direction {
    Inbound,
    Outbound
  };

  public static enum State {
    Start,
    Progress,
    Complete
  };

  public static final int CHUNK_SIZE = 4 * 1024;

  public void notify(
      Direction direction, State state, int totalTransferSize, int currentTransferSize);
}
