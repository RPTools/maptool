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
package net.rptools.maptool.client.ui.javfx.vieweditpane;

import com.vladsch.flexmark.parser.ParserEmulationProfile;
import java.io.IOException;
import net.rptools.maptool.client.ui.MarkDownPane;

public class ViewEditMarkDownPane extends ViewEditPane {

  private final MarkDownPane markDownPane;

  private ViewEditMarkDownPane(MarkDownPane pane) throws IOException {
    super();
    markDownPane = pane;
    setContentPane(markDownPane);
  }

  public static ViewEditMarkDownPane createViewOnlyPane(ParserEmulationProfile profile) {
    try {
      return new ViewEditMarkDownPane(new MarkDownPane(profile));
    } catch (IOException e) {
      e.printStackTrace(); // TODO: CDW
      return null;
    }
  }

  public void setText(String markDownText) {
    markDownPane.setText(markDownText);
  }
}
