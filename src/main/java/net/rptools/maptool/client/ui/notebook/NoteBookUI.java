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
package net.rptools.maptool.client.ui.notebook;

import java.io.IOException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapTool.CampaignEvent;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookTableTreeModel;

public class NoteBookUI {

  private SwingJavaFXDialog noteBookDialog;
  private JFXPanel jfxPanel;
  private NoteBookController controller;

  /**
   * The {@link NoteBookTableTreeModel} with all the {@link
   * NoteBookEntry}s for the campaign.
   */
  private NoteBookTableTreeModel noteBookTableTreeModel;

  public void init(MapToolFrame parentFrame) {
    Platform.runLater(
        () -> {
          try {
            initFX(parentFrame);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  private void initFX(MapToolFrame parentFrame) throws IOException {
    assert Platform.isFxApplicationThread() : "init() must be run on the FX Application Thread.";
    ResourceBundle resourceBundle = ResourceBundle.getBundle("net.rptools.maptool.language.i18n");
    var loader =
        new FXMLLoader(
            getClass().getResource("/net/rptools/maptool/client/ui/fxml/NoteBook.fxml"),
            resourceBundle);

    Parent parent = loader.load();
    controller = loader.getController();

    Scene scene = new Scene(loader.getRoot());
    jfxPanel = new JFXPanel();
    jfxPanel.setScene(scene);

    campaignChanged(null, MapTool.getCampaign());

    SwingUtilities.invokeLater(
        () -> {
          noteBookDialog =
              new SwingJavaFXDialog(I18N.getText("noteBook.title"), parentFrame, jfxPanel, false);

          MapTool.getEventDispatcher()
              .addListener(
                  e -> campaignChanged((Campaign) e.getOldValue(), (Campaign) e.getNewValue()),
                  CampaignEvent.Changed);
        });
  }

  public void show() {
    SwingUtilities.invokeLater(() -> noteBookDialog.showDialog());
  }

  /**
   * Method called when the {@link Campaign} is changed.
   *
   * @param oldCampaign The previous {@link Campaign}.
   * @param newCampaign The new {@link Campaign}.
   * @note This method can safely be called from any thread.
   */
  private void campaignChanged(Campaign oldCampaign, Campaign newCampaign) {

    if (newCampaign != null) {
      Platform.runLater(
          () -> {
            NoteBookTableTreeModel oldNoteBookTableTreeModel = noteBookTableTreeModel;

            noteBookTableTreeModel =
                NoteBookTableTreeModel.getTreeModelFor(newCampaign.getNotebook());
            controller.setTreeRoot(noteBookTableTreeModel.getRoot());
          });
    }
  }
}
