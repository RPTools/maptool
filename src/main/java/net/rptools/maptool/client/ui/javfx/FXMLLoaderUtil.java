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
package net.rptools.maptool.client.ui.javfx;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;

public class FXMLLoaderUtil {

  private static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle("net.rptools.maptool.language.i18n");

  public void parentFromFXML(
      String fxmlPath, BiConsumer<Parent, SwingJavaFXDialogController> callback) {
    Platform.runLater(
        () -> {
          JFXPanel panel = new JFXPanel();
          javafx.fxml.FXMLLoader loader =
              new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath), RESOURCE_BUNDLE);
          Parent parentControl = null;
          try {
            parentControl = loader.load();
            SwingJavaFXDialogController controller = loader.getController();
            callback.accept(parentControl, controller);
          } catch (IOException e) {
            MapTool.showError(I18N.getText("javafx.error.errorLoadingFXML", fxmlPath), e);
          }
        });
  }

  public void jfxPanelFromFXML(
      String fxmlPath, BiConsumer<JFXPanel, SwingJavaFXDialogController> callback) {
    Platform.runLater(
        () -> {
          JFXPanel panel = new JFXPanel();
          parentFromFXML(
              fxmlPath,
              (parent, controller) -> {
                Scene scene = new Scene(parent);
                panel.setScene(scene);
                SwingUtilities.invokeLater(() -> callback.accept(panel, controller));
              });
        });
  }
}
