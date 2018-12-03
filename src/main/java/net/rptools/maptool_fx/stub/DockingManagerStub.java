/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx.stub;

import com.jidesoft.docking.DockableFrame;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DockingManagerStub {

    // Only want to show get frame once for some names as its very noisy otherwise
    private static Set<String> frameShown = ConcurrentHashMap.newKeySet();

    private static List<String> showOnlyOnce = Arrays.asList("IMPERSONATED", "SELECTION");

    private void showDialog(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            if (content != null) {
                dialog.setContentText(content);
            }
            dialog.showAndWait();
        });
    }

    public void hideFrame(String name) {
        showDialog(
                "Docking Manager Stub",
                "DockingManagerStub.hideFrame()",
                "Frame Name = " + name);
    }

    public void showFrame(String name) {
        showDialog(
                "Docking Manager Stub",
                "DockingManagerStub.showFrame()",
                "Frame Name = " + name);
    }

    public void resetToDefault() {
        showDialog(
                "Docking Manager Stub",
                "DockingManagerStub.resetToDefault()",
                null);
    }

    public void addFrame(HTMLFrame htmlFrame) {
        showDialog(
                "Docking Manager Stub",
                "DockingManagerStub.addFrame()",
                "HTML Frame Name = " + htmlFrame.getName());
    }

    public void floatFrame(String key, Rectangle rect, boolean b) {
        showDialog(
                "Docking Manager Stub",
                "DockingManagerStub.floatFrame()",
                "key = " + key);
    }

    public DockableFrame getFrame(String name) {
        if (showOnlyOnce.contains(name)) {
            if (!frameShown.contains(name)) {
                frameShown.add(name);
                showDialog(
                        "Docking Manager Stub",
                        "DockingManagerStub.getFrame()",
                        "Name = " + name + " (" + name + " will only be shown once)");
            }
        } else {
            showDialog(
                    "Docking Manager Stub",
                    "DockingManagerStub.getFrame()",
                    "Name = " + name);
        }
        return null;
    }
}
