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
package net.rptools.maptool.client.ui.players;

import java.util.function.Consumer;
import net.rptools.maptool.client.ui.javfx.SimpleSwingJavaFXDialog;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialog;

public class PlayerDatabaseEditDialog {
  /** The path of the FXML file for the dialog. */
  private static final String FXML_PATH =
      "/net/rptools/maptool/client/ui/fxml/PlayerDatabaseEdit.fxml";

  private static final String EDIT_TITLE = "playerDB.dialog.title.edit";
  private static final String NEW_TITLE = "playerDB.dialog.title.new";

  /** The {@link SwingJavaFXDialog} used to display the dialog. */
  private final SimpleSwingJavaFXDialog<PlayerDatabaseEditController> simpleSwingJavaFXDialog;

  private PlayerDatabaseEditDialog(String title, Consumer<PlayerDatabaseEditController> callback) {
    simpleSwingJavaFXDialog = new SimpleSwingJavaFXDialog<>(FXML_PATH, title, callback);
  }

  public static PlayerDatabaseEditDialog getEdtPlayerDialog(
      Consumer<PlayerDatabaseEditController> callback) {
    return new PlayerDatabaseEditDialog(
        EDIT_TITLE,
        c -> {
          c.setNewPlayerMode(false);
          callback.accept(c);
        });
  }

  public static PlayerDatabaseEditDialog getNewPlayerDialog(
      Consumer<PlayerDatabaseEditController> callback) {
    return new PlayerDatabaseEditDialog(
        NEW_TITLE,
        c -> {
          c.setNewPlayerMode(true);
          callback.accept(c);
        });
  }

  /** Shows the dialog and its contents. This method must be called on the Swing Event thread. */
  public void show() {
    simpleSwingJavaFXDialog.show();
  }
}
