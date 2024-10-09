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

/**
 * The FlatImageLabelFactory class is responsible for creating instances of FlatImageLabel objects.
 * It provides methods to customize the labels based on different parameters.
 */
public class FlatImageLabelFactory {

  /** The singleton instance of the FlatImageLabelFactory class for NPC labels */
  private final FlatImageLabel npcImageLabel;

  /** The singleton instance of the FlatImageLabelFactory class for PC labels */
  private final FlatImageLabel pcImageLabel;

  /** The singleton instance of the FlatImageLabelFactory class for non-visible token labels */
  private final FlatImageLabel nonVisibleImageLabel;

  /** Creates a new instance of the FlatImageLabelFactory class. */
  public FlatImageLabelFactory() {
    var npcBackground = AppPreferences.npcMapLabelBackground.get();
    var npcForeground = AppPreferences.npcMapLabelForeground.get();
    var npcBorder = AppPreferences.npcMapLabelBorder.get();
    var pcBackground = AppPreferences.pcMapLabelBackground.get();
    var pcForeground = AppPreferences.pcMapLabelForeground.get();
    var pcBorder = AppPreferences.pcMapLabelBorder.get();
    var nonVisBackground = AppPreferences.nonVisibleTokenMapLabelBackground.get();
    var nonVisForeground = AppPreferences.nonVisibleTokenMapLabelForeground.get();
    var nonVisBorder = AppPreferences.nonVisibleTokenMapLabelBorder.get();
    int fontSize = AppPreferences.mapLabelFontSize.get();
    var font = AppStyle.labelFont.deriveFont(AppStyle.labelFont.getStyle(), fontSize);
    boolean showBorder = AppPreferences.mapLabelShowBorder.get();
    int borderWidth = showBorder ? AppPreferences.mapLabelBorderWidth.get() : 0;
    int borderArc = AppPreferences.mapLabelBorderArc.get();

    npcImageLabel =
        new FlatImageLabel(
            4,
            4,
            npcForeground,
            npcBackground,
            npcBorder,
            font,
            Justification.Center,
            borderWidth,
            borderArc);
    pcImageLabel =
        new FlatImageLabel(
            4,
            4,
            pcForeground,
            pcBackground,
            pcBorder,
            font,
            Justification.Center,
            borderWidth,
            borderArc);
    nonVisibleImageLabel =
        new FlatImageLabel(
            4,
            4,
            nonVisForeground,
            nonVisBackground,
            nonVisBorder,
            font,
            Justification.Center,
            borderWidth,
            borderArc);
  }

  /**
   * Retrieves the appropriate map image label based on the provided token.
   *
   * @param token The token representing the entity on the map.
   * @return The map image label corresponding to the token type, and/or visibility.
   */
  public FlatImageLabel getMapImageLabel(Token token) {
    if (!token.isVisible()) {
      return nonVisibleImageLabel;
    } else if (token.getType() == Type.NPC) {
      return npcImageLabel;
    } else {
      return pcImageLabel;
    }
  }

  /**
   * Retrieves the map image label based on the provided label.
   *
   * @param label The label containing the properties for the map image label.
   * @return The map image label with the specified properties.
   */
  public FlatImageLabel getMapImageLabel(Label label) {
    var font = AppStyle.labelFont.deriveFont(AppStyle.labelFont.getStyle(), label.getFontSize());
    var bg = label.isShowBackground() ? label.getBackgroundColor() : new Color(0, 0, 0, 0);
    int borderSize = label.isShowBorder() ? label.getBorderWidth() : 0;
    return new FlatImageLabel(
        4,
        4,
        label.getForegroundColor(),
        bg,
        label.getBorderColor(),
        font,
        Justification.Center,
        borderSize,
        label.getBorderArc());
  }
}
