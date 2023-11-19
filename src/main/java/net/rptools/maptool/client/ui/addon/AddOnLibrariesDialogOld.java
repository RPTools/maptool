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
package net.rptools.maptool.client.ui.addon;

import net.rptools.maptool.client.ui.javfx.SimpleSwingJavaFXDialog;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialog;

public class AddOnLibrariesDialogOld {
  /** The path of the FXML file for the dialog. */
  private static final String FXML_PATH =
      "/net/rptools/maptool/client/ui/fxml/AddOnLibrariesDialog.fxml";

  /** The {@link SwingJavaFXDialog} used to display the dialog. */
  private final SimpleSwingJavaFXDialog simpleSwingJavaFXDialog;

  public AddOnLibrariesDialogOld() {
    simpleSwingJavaFXDialog =
        new SimpleSwingJavaFXDialog<AddOnLibrariesDialogController>(
            FXML_PATH, "library.dialog.title");
  }

  /** Shows the dialog and its contents. This method must be called on the Swing Event thread. */
  public void show() {
    simpleSwingJavaFXDialog.show();
  }
}
