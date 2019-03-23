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
package net.rptools.maptool.protocol.syrinscape;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Support "syrinscape-fantasy" URI in Swing components
 *
 * @author Jamz
 */
public class SyrinscapeConnection extends URLConnection {
  public SyrinscapeConnection(URL url) {
    super(url);
  }

  @Override
  public void connect() throws IOException {
    // TODO Auto-generated method stub
  }
}
