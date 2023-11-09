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
package net.rptools.maptool.client.ui;

import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

public class StampPopupMenu extends AbstractTokenPopupMenu {
  private static final long serialVersionUID = -1355308966041002520L;

  public StampPopupMenu(
      Set<GUID> selectedTokenSet, int x, int y, ZoneRenderer renderer, Token tokenUnderMouse) {
    super(selectedTokenSet, x, y, renderer, tokenUnderMouse);

    add(new SetFacingAction());
    add(new ClearFacingAction());
    add(new StartMoveAction());
    add(createFlipMenu());
    add(createSizeMenu());
    add(createArrangeMenu());
    add(
        createChangeToMenu(
            Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND));
    if (getTokenUnderMouse().getCharsheetImage() != null
        && AppUtil.playerOwns(getTokenUnderMouse())) {
      add(new ShowHandoutAction());
    }
    add(new JSeparator());
    addOwnedItem(createLightSourceMenu());
    add(new JSeparator());

    addToggledItem(
        new SnapToGridAction(tokenUnderMouse.isSnapToGrid(), renderer),
        tokenUnderMouse.isSnapToGrid());
    addToggledGMItem(new VisibilityAction(), tokenUnderMouse.isVisible());
    addToggledGMItem(
        new AlwaysVisibleAction(tokenUnderMouse.isAlwaysVisible(), renderer),
        tokenUnderMouse.isAlwaysVisible());

    add(new JSeparator());

    add(new JMenuItem(new AutoResizeAction()));
    add(new JSeparator());

    add(new JMenuItem(new CutAction()));
    add(new JMenuItem(new CopyAction()));
    add(new JMenuItem(new DeleteAction()));

    add(new JSeparator());

    add(new ShowPropertiesDialogAction().asJMenuItem());
    add(new SaveAction());
  }
}
