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
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.swing.label.FlatImageLabel.Justification;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;

public class FlatImageLabelFactory {

  private final FlatImageLabel npcImageLabel;
  private final FlatImageLabel pcImageLabel;
  private final FlatImageLabel nonVisibleImageLabel;

  public FlatImageLabelFactory() {
    var npcBackground = AppPreferences.getNPCMapLabelBG();
    var npcForeground = AppPreferences.getNPCMapLabelFG();
    var pcBackground = AppPreferences.getPCMapLabelBG();
    var pcForeground = AppPreferences.getPCMapLabelFG();
    var nonVisBackground = AppPreferences.getNonVisMapLabelBG();
    var nonVisForeground = AppPreferences.getNonVisMapLabelFG();
    int fontSize = AppPreferences.getMapLabelFontSize();
    var font = AppStyle.labelFont.deriveFont(AppStyle.labelFont.getStyle(), fontSize);

    npcImageLabel =
        new FlatImageLabel(4, 4, npcForeground, npcBackground, font, Justification.Center);
    pcImageLabel = new FlatImageLabel(4, 4, pcForeground, pcBackground, font, Justification.Center);
    nonVisibleImageLabel =
        new FlatImageLabel(4, 4, nonVisForeground, nonVisBackground, font, Justification.Center);
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

  public FlatImageLabel getMapImageLabel(Label label) {
    if (label.isShowBackground()) {
      // TODO: CDW
      return new FlatImageLabel(
          4, 4, label.getForegroundColor(), Color.GRAY, AppStyle.labelFont, Justification.Center);
    } else {
      return new FlatImageLabel(
          4,
          4,
          label.getForegroundColor(),
          new Color(0, 0, 0, 0),
          AppStyle.labelFont,
          Justification.Center);
    }
  }
}
