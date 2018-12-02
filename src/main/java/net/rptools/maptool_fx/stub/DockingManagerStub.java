package net.rptools.maptool_fx.stub;

import com.jidesoft.docking.DockableFrame;
import javafx.scene.control.Alert;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;

import java.awt.*;

public class DockingManagerStub {
    public void hideFrame(String name) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Docking Manager Stub");
        dialog.setHeaderText("DockingManagerStub.hideFrame()");
        dialog.setContentText("Frame Name = " + name);
        dialog.showAndWait();
    }

    public void showFrame(String name) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Docking Manager Stub");
        dialog.setHeaderText("DockingManagerStub.showFrame()");
        dialog.setContentText("Frame Name = " + name);
        dialog.showAndWait();
    }

    public void resetToDefault() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Docking Manager Stub");
        dialog.setHeaderText("DockingManagerStub.resetToDefault()");
        dialog.showAndWait();
    }

    public void addFrame(HTMLFrame htmlFrame) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Docking Manager Stub");
        dialog.setHeaderText("DockingManagerStub.addFrame()");
        dialog.setContentText("HTML Frame Name = " + htmlFrame.getName());
        dialog.showAndWait();
    }

    public void floatFrame(String key, Rectangle rect, boolean b) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Docking Manager Stub");
        dialog.setHeaderText("DockingManagerStub.floatFrame()");
        dialog.setContentText("key = " + key);
        dialog.showAndWait();
    }

    public DockableFrame getFrame(String name) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Docking Manager Stub");
        dialog.setHeaderText("DockingManagerStub.getFrame()");
        dialog.setContentText("Name = " + name);
        dialog.showAndWait();

        return null;
    }
}
