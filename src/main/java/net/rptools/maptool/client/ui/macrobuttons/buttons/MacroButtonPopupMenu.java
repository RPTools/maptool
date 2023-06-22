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
package net.rptools.maptool.client.ui.macrobuttons.buttons;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.dialog.MacroEditorDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.PersistenceUtil;

@SuppressWarnings("serial")
public class MacroButtonPopupMenu extends JPopupMenu {

  private final MacroButton button;
  private final String panelClass;

  public MacroButtonPopupMenu(MacroButton parent, String panelClass, Boolean commonMacro) {
    this.button = parent;
    this.panelClass = panelClass;
    if (panelClass.equals("SelectionPanel")) {
      if (button.getProperties().getCommonMacro()) {
        addCommonActions();
      } else {
        addActions();
      }
    } else if (panelClass.equals("CampaignPanel") || panelClass.equals("GmPanel")) {
      addCampaignActions();
    } else {
      addActions();
    }
  }

  private void addActions() {
    if (MapTool.getPlayer().isGM() || button.getProperties().getAllowPlayerEdits()) {
      add(new EditButtonAction());
      add(new JSeparator());
      add(new AddNewButtonAction());
      add(new DuplicateButtonAction());
      add(new JSeparator());
      add(new ResetButtonAction());
      add(new DeleteButtonAction());
      add(new JSeparator());
      add(new ExportMacroAction());
      add(new JSeparator());
      add(new RunMacroForEachSelectedTokenAction());
    } else {
      add(new AddNewButtonAction());
      add(new JSeparator());
      add(new RunMacroForEachSelectedTokenAction());
    }
  }

  private void addCommonActions() {
    if (MapTool.getPlayer().isGM() || button.getProperties().getAllowPlayerEdits()) {
      add(new EditButtonAction());
      add(new AddNewButtonAction(I18N.getText("action.macro.addNewToSelected")));
      add(new DuplicateButtonAction(I18N.getText("action.macro.duplicateOnSelected")));
      add(new JSeparator());
      add(new DeleteButtonAction(I18N.getText("action.macro.deleteFromCommon")));
      add(new JSeparator());
      add(new ExportMacroAction(I18N.getText("action.macro.exportCommon")));
      add(new JSeparator());
      add(new RunMacroForEachSelectedTokenAction());
    } else {
      add(new AddNewButtonAction(I18N.getText("action.macro.addNewToSelected")));
      add(new JSeparator());
      add(new RunMacroForEachSelectedTokenAction());
    }
  }

  private void addCampaignActions() {
    if (MapTool.getPlayer().isGM()) {
      add(new EditButtonAction());
      add(new JSeparator());
      add(new AddNewButtonAction());
      add(new DuplicateButtonAction());
      add(new JSeparator());
      add(new ResetButtonAction());
      add(new DeleteButtonAction());
      add(new JSeparator());
      add(new ExportMacroAction());
      add(new JSeparator());
      add(new RunMacroForEachSelectedTokenAction());
    } else {
      add(new RunMacroForEachSelectedTokenAction());
    }
  }

  private class AddNewButtonAction extends AbstractAction {
    public AddNewButtonAction() {
      putValue(Action.NAME, I18N.getText("action.macro.new"));
    }

    public AddNewButtonAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {
      // TODO: refactor to put tab index from Tab enum
      if (panelClass.equals("GlobalPanel")) {
        new MacroButtonProperties(
            panelClass, MacroButtonPrefs.getNextIndex(), button.getProperties().getGroup());
      } else if (panelClass.equals("CampaignPanel")) {
        new MacroButtonProperties(
            panelClass,
            MapTool.getCampaign().getMacroButtonNextIndex(),
            button.getProperties().getGroup());
      } else if (panelClass.equals("GmPanel")) {
        new MacroButtonProperties(
            panelClass,
            MapTool.getCampaign().getGmMacroButtonNextIndex(),
            button.getProperties().getGroup());
      } else if (panelClass.equals("SelectionPanel")) {
        if (MapTool.getFrame()
            .getSelectionPanel()
            .getCommonMacros()
            .contains(button.getProperties())) {
          for (Token nextToken :
              MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
            new MacroButtonProperties(
                nextToken, nextToken.getMacroNextIndex(), button.getProperties().getGroup());
          }
        } else {
          new MacroButtonProperties(
              button.getToken(),
              button.getToken().getMacroNextIndex(),
              button.getProperties().getGroup());
        }
      } else if (button.getToken() != null) {
        new MacroButtonProperties(
            button.getToken(),
            button.getToken().getMacroNextIndex(),
            button.getProperties().getGroup());
      }
    }
  }

  private class EditButtonAction extends AbstractAction {
    public EditButtonAction() {
      putValue(Action.NAME, I18N.getText("action.macro.edit"));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      String macroUUID = button.getProperties().getMacroUUID();
      // Don't create new dialog is it is already opened. Fixes #1426 and #1495.
      if (!MacroEditorDialog.isMacroDialogOpen(macroUUID)) {
        MacroEditorDialog.createMacroButtonDialog().show(button);
      }
    }
  }

  private class DeleteButtonAction extends AbstractAction {
    /** Adds the "Delete..." button. */
    public DeleteButtonAction() {
      putValue(Action.NAME, I18N.getText("action.macro.delete"));
    }

    public DeleteButtonAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {
      if (MapTool.confirm(
          I18N.getText("confirm.macro.delete", button.getProperties().getLabel()))) {
        // remove the hot key or the hot key will remain and you'll get an exception later
        // when you want to assign that hotkey to another button.
        button.clearHotkey();

        if (panelClass.equals("GlobalPanel")) {
          MacroButtonPrefs.delete(button.getProperties());
        } else if (panelClass.equals("CampaignPanel")) {
          MapTool.getCampaign().deleteMacroButton(button.getProperties());
        } else if (panelClass.equals("GmPanel")) {
          MapTool.getCampaign().deleteGmMacroButton(button.getProperties());
        } else if (panelClass.equals("SelectionPanel")) {
          if (MapTool.getFrame()
              .getSelectionPanel()
              .getCommonMacros()
              .contains(button.getProperties())) {
            for (Token nextToken :
                MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
              if (AppUtil.playerOwns(nextToken)) {
                List<MacroButtonProperties> workingMacros = new ArrayList<MacroButtonProperties>();
                boolean hashCodesMatch = false;
                boolean allowDelete = false;
                for (MacroButtonProperties nextMacro : nextToken.getMacroList(true)) {
                  hashCodesMatch =
                      nextMacro.hashCodeForComparison()
                          == button.getProperties().hashCodeForComparison();
                  allowDelete =
                      MapTool.getPlayer().isGM()
                          || (!MapTool.getPlayer().isGM() && nextMacro.getAllowPlayerEdits());
                  if (!hashCodesMatch || !allowDelete) {
                    // Keeps macros that can't be deleted or don't match the button
                    workingMacros.add(nextMacro);
                  }
                }
                // Updates the client macros. Fixes #1657.
                MapTool.serverCommand()
                    .updateTokenProperty(
                        nextToken, Token.Update.saveMacroList, workingMacros, true);
              }
            }
          } else {
            int index = button.getProperties().getIndex();
            Token token = button.getToken();
            MapTool.serverCommand().updateTokenProperty(token, Token.Update.deleteMacro, index);
          }
        } else if (button.getToken() != null) {
          if (AppUtil.playerOwns(button.getToken())) {
            int index = button.getProperties().getIndex();
            Token token = button.getToken();
            MapTool.serverCommand().updateTokenProperty(token, Token.Update.deleteMacro, index);
          }
        }
      }
    }
  }

  private class DuplicateButtonAction extends AbstractAction {
    public DuplicateButtonAction() {
      putValue(Action.NAME, I18N.getText("action.macro.duplicate"));
    }

    public DuplicateButtonAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {
      if (panelClass.equals("GlobalPanel")) {
        new MacroButtonProperties(
            panelClass, MacroButtonPrefs.getNextIndex(), button.getProperties());
      } else if (panelClass.equals("CampaignPanel")) {
        new MacroButtonProperties(
            panelClass, MapTool.getCampaign().getMacroButtonNextIndex(), button.getProperties());
      } else if (panelClass.equals("GmPanel")) {
        new MacroButtonProperties(
            panelClass, MapTool.getCampaign().getGmMacroButtonNextIndex(), button.getProperties());
      } else if (panelClass.equals("SelectionPanel")) {
        if (MapTool.getFrame()
            .getSelectionPanel()
            .getCommonMacros()
            .contains(button.getProperties())) {
          for (Token nextToken :
              MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
            new MacroButtonProperties(
                nextToken, nextToken.getMacroNextIndex(), button.getProperties());
          }
        } else {
          new MacroButtonProperties(
              button.getToken(), button.getToken().getMacroNextIndex(), button.getProperties());
        }
      } else if (button.getToken() != null) {
        new MacroButtonProperties(
            button.getToken(), button.getToken().getMacroNextIndex(), button.getProperties());
      }
    }
  }

  private class ResetButtonAction extends AbstractAction {
    public ResetButtonAction() {
      putValue(Action.NAME, I18N.getText("action.macro.reset"));
    }

    public void actionPerformed(ActionEvent event) {
      if (MapTool.confirm(I18N.getText("confirm.macro.reset", button.getProperties().getLabel()))) {
        button.getProperties().reset();
        button.getProperties().save();
      }
    }
  }

  private class RunMacroForEachSelectedTokenAction extends AbstractAction {
    public RunMacroForEachSelectedTokenAction() {
      putValue(Action.NAME, I18N.getText("action.macro.runForEachSelected"));
    }

    public void actionPerformed(ActionEvent event) {
      if (MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList().size() > 0) {
        button
            .getProperties()
            .executeMacro(MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList());
      }
    }
  }

  private class ExportMacroAction extends AbstractAction {
    private ExportMacroAction() {
      putValue(Action.NAME, I18N.getText("action.macro.export"));
    }

    private ExportMacroAction(String name) {
      putValue(Action.NAME, name);
    }

    /**
     * Shows a prompt to save the macro.
     *
     * @param event the event
     */
    public void actionPerformed(ActionEvent event) {
      JFileChooser chooser = MapTool.getFrame().getSaveMacroFileChooser();

      if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      final File selectedFile =
          FileUtil.getFileWithExtension(
              chooser.getSelectedFile(), AppConstants.MACRO_FILE_EXTENSION);
      EventQueue.invokeLater(
          () -> {
            if (!selectedFile.exists()) {
              if (!MapTool.confirm(
                  I18N.getText("confirm.macro.exportInto", button.getProperties().getLabel()))) {
                return;
              }
            } else if (!MapTool.confirm(I18N.getText("confirm.macro.exportOverwrite"))) {
              return;
            }

            try {
              if (panelClass.equals("SelectionPanel")) {
                if (MapTool.getFrame()
                    .getSelectionPanel()
                    .getCommonMacros()
                    .contains(button.getProperties())) {
                  if (confirmCommonExport(button.getProperties())) {
                    PersistenceUtil.saveMacro(button.getProperties(), selectedFile);
                  } else {
                    MapTool.showInformation(I18N.getText("msg.info.macro.exportCancel"));
                    return;
                  }
                } else {
                  PersistenceUtil.saveMacro(button.getProperties(), selectedFile);
                }
              }
              PersistenceUtil.saveMacro(button.getProperties(), selectedFile);
              MapTool.showInformation(I18N.getText("msg.info.macro.exportSuccess"));
            } catch (IOException ioe) {
              ioe.printStackTrace();
              MapTool.showError(I18N.getText("msg.error.macro.exportFail", ioe));
            }
          });
    }
  }

  private Boolean confirmCommonExport(MacroButtonProperties buttonMacro) {
    boolean failComparison = false;
    String comparisonResults = "";
    if (!buttonMacro.getCompareGroup()) {
      failComparison = true;
      comparisonResults =
          comparisonResults + "<li>" + I18N.getText("component.label.macro.group") + "</li>";
    }
    if (!buttonMacro.getCompareSortPrefix()) {
      failComparison = true;
      comparisonResults =
          comparisonResults + "<li>" + I18N.getText("component.label.macro.sortPrefix") + "</li>";
    }
    if (!buttonMacro.getCompareCommand()) {
      failComparison = true;
      comparisonResults =
          comparisonResults + "<li>" + I18N.getText("component.label.macro.command") + "</li>";
    }
    if (!buttonMacro.getCompareIncludeLabel()) {
      failComparison = true;
      comparisonResults =
          comparisonResults + "<li>" + I18N.getText("component.label.macro.includeLabel") + "</li>";
    }
    if (!buttonMacro.getCompareAutoExecute()) {
      failComparison = true;
      comparisonResults =
          comparisonResults + "<li>" + I18N.getText("component.label.macro.autoExecute") + "</li>";
    }
    if (!buttonMacro.getApplyToTokens()) {
      failComparison = true;
      comparisonResults =
          comparisonResults
              + "<li>"
              + I18N.getText("component.label.macro.applyToSelected")
              + "</li>";
    }
    if (failComparison) {
      failComparison =
          MapTool.confirm(
              I18N.getText(
                  "msg.error.macro.exportFail", buttonMacro.getLabel(), comparisonResults));
    }
    return failComparison;
  }
}
