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
package net.rptools.maptool.client.swing.label;

import java.awt.Color;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.swing.label.FlatImageLabel.Justification;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;

public class FlatImageLabelFactory {

  private static FlatImageLabel npcImageLabel;
  private static FlatImageLabel pcImageLabel;
  private static FlatImageLabel nonVisibleImageLabel;

  static {
    npcImageLabel =
        new FlatImageLabel(4, 4, Color.blue, Color.white, AppStyle.labelFont, Justification.Center);
    pcImageLabel =
        new FlatImageLabel(
            4, 4, Color.lightGray, Color.lightGray, AppStyle.labelFont, Justification.Center);
    nonVisibleImageLabel =
        new FlatImageLabel(
            4, 4, Color.white, Color.darkGray, AppStyle.labelFont, Justification.Center);
  }

  public FlatImageLabel getMapImageLabel(Token token) {
    if (!token.isVisible()) {
      return nonVisibleImageLabel;
    } else if (token.getType() == Type.NPC) {
      return npcImageLabel;
    } else {
      return pcImageLabel;
    }
  }
}
