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
package net.rptools.maptool.client.ui.macrobuttons.buttongroups;

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
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.macrobuttons.panels.CampaignPanel;
import net.rptools.maptool.client.ui.macrobuttons.panels.GlobalPanel;
import net.rptools.maptool.client.ui.macrobuttons.panels.GmPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.PersistenceUtil;

@SuppressWarnings("serial")
public class ButtonGroupPopupMenu extends JPopupMenu {

  private final AreaGroup areaGroup;
  private final String macroGroup;
  private final String panelClass;
  private GUID tokenId;

  public ButtonGroupPopupMenu(
      String panelClass, AreaGroup areaGroup, String macroGroup, Token token) {
    this.areaGroup = areaGroup;
    this.macroGroup = macroGroup;
    this.panelClass = panelClass;
    if (token == null) {
      this.tokenId = null;
    } else {
      this.tokenId = token.getId();
    }
    if (panelClass.equals("SelectionPanel")) {
      if (areaGroup != null) {
        if (areaGroup
            .getGroupLabel()
            .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
          addCommonActions();
        } else {
          addActions();
        }
      }
    } else if (panelClass.equals("CampaignPanel") || panelClass.equals("GmPanel")) {
      addCampaignActions();
    } else {
      addActions();
    }
  }

  private void addActions() {
    add(new AddMacroAction());
    add(new JSeparator());
    add(new ImportMacroAction());
    add(new JSeparator());
    add(new ImportMacroSetAction());
    add(new ExportMacroSetAction());
    add(new JSeparator());
    add(new ClearGroupAction());
    if (!this.panelClass.equals("SelectionPanel")) {
      add(new JSeparator());
      add(new ClearPanelAction());
    }
  }

  private void addCommonActions() {
    add(new AddMacroAction(I18N.getText("action.macro.addNewToSelected")));
    add(new JSeparator());
    add(new ImportMacroAction(I18N.getText("action.macro.importToSelected")));
    add(new JSeparator());
    add(new ImportMacroSetAction(I18N.getText("action.macro.importSetToSelected")));
    add(new ExportMacroSetAction(I18N.getText("action.macro.exportCommonSet")));
  }

  private void addCampaignActions() {
    if (MapTool.getPlayer().isGM()) {
      add(new AddMacroAction());
      add(new JSeparator());
      add(new ImportMacroAction());
      add(new JSeparator());
      add(new ImportMacroSetAction());
      add(new ExportMacroSetAction());
      add(new JSeparator());
      add(new ClearGroupAction());
      add(new JSeparator());
      add(new ClearPanelAction());
    }
  }

  private class AddMacroAction extends AbstractAction {
    public AddMacroAction() {
      putValue(Action.NAME, I18N.getText("action.macro.new"));
    }

    public AddMacroAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {
      if (panelClass.equals("GlobalPanel")) {
        new MacroButtonProperties(panelClass, MacroButtonPrefs.getNextIndex(), macroGroup);
      } else if (panelClass.equals("CampaignPanel")) {
        new MacroButtonProperties(
            panelClass, MapTool.getCampaign().getMacroButtonNextIndex(), macroGroup);
      } else if (panelClass.equals("GmPanel")) {
        new MacroButtonProperties(
            panelClass, MapTool.getCampaign().getGmMacroButtonNextIndex(), macroGroup);
      } else if (panelClass.equals("SelectionPanel")) {
        if (areaGroup != null) {
          if (areaGroup
              .getGroupLabel()
              .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
            for (Token nextToken :
                MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
              new MacroButtonProperties(nextToken, nextToken.getMacroNextIndex(), macroGroup);
            }
          } else if (tokenId != null) {
            Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
            new MacroButtonProperties(token, token.getMacroNextIndex(), macroGroup);
          }
        }
      } else if (tokenId != null) {
        Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
        new MacroButtonProperties(token, token.getMacroNextIndex(), macroGroup);
      }
    }
  }

  private class ImportMacroAction extends AbstractAction {
    public ImportMacroAction() {
      putValue(Action.NAME, I18N.getText("action.macro.import"));
    }

    public ImportMacroAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {

      JFileChooser chooser = MapTool.getFrame().getLoadMacroFileChooser();

      if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      final File selectedFile = chooser.getSelectedFile();
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              try {
                MacroButtonProperties newButtonProps = PersistenceUtil.loadMacro(selectedFile);
                Boolean alreadyExists = false;
                if (panelClass.equals("GlobalPanel")) {
                  for (MacroButtonProperties nextMacro : MacroButtonPrefs.getButtonProperties()) {
                    if (newButtonProps.hashCodeForComparison()
                        == nextMacro.hashCodeForComparison()) {
                      alreadyExists = true;
                    }
                  }
                  if (alreadyExists) {
                    alreadyExists =
                        confirmImport(
                            newButtonProps,
                            I18N.getText(
                                "confirm.macro.panelLocation", I18N.getText("panel.Global")));
                  }
                  if (!alreadyExists) {
                    new MacroButtonProperties(
                        panelClass, MacroButtonPrefs.getNextIndex(), newButtonProps);
                  }
                } else if (panelClass.equals("CampaignPanel")) {
                  for (MacroButtonProperties nextMacro :
                      MapTool.getCampaign().getMacroButtonPropertiesArray()) {
                    if (newButtonProps.hashCodeForComparison()
                        == nextMacro.hashCodeForComparison()) {
                      alreadyExists = true;
                    }
                  }
                  if (alreadyExists) {
                    alreadyExists =
                        confirmImport(
                            newButtonProps,
                            I18N.getText(
                                "confirm.macro.panelLocation", I18N.getText("panel.Campaign")));
                  }
                  if (!alreadyExists) {
                    new MacroButtonProperties(
                        panelClass,
                        MapTool.getCampaign().getMacroButtonNextIndex(),
                        newButtonProps);
                  }
                } else if (panelClass.equals("GmPanel")) {
                  for (MacroButtonProperties nextMacro :
                      MapTool.getCampaign().getGmMacroButtonPropertiesArray()) {
                    if (newButtonProps.hashCodeForComparison()
                        == nextMacro.hashCodeForComparison()) {
                      alreadyExists = true;
                    }
                  }
                  if (alreadyExists) {
                    alreadyExists =
                        confirmImport(
                            newButtonProps,
                            I18N.getText("confirm.macro.panelLocation", I18N.getText("panel.Gm")));
                  }
                  if (!alreadyExists) {
                    new MacroButtonProperties(
                        panelClass,
                        MapTool.getCampaign().getGmMacroButtonNextIndex(),
                        newButtonProps);
                  }
                } else if (panelClass.equals("SelectionPanel")) {
                  if (areaGroup != null) {
                    if (areaGroup
                        .getGroupLabel()
                        .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
                      for (Token nextToken :
                          MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
                        alreadyExists = false;
                        for (MacroButtonProperties nextMacro : nextToken.getMacroList(true)) {
                          if (newButtonProps.hashCodeForComparison()
                              == nextMacro.hashCodeForComparison()) {
                            alreadyExists = true;
                          }
                        }
                        if (alreadyExists) {
                          alreadyExists =
                              confirmImport(
                                  newButtonProps,
                                  I18N.getText("confirm.macro.commonSelectionLocation"));
                        }
                        if (!alreadyExists) {
                          new MacroButtonProperties(
                              nextToken, nextToken.getMacroNextIndex(), newButtonProps);
                        }
                      }
                    } else if (tokenId != null) {
                      Token token =
                          MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
                      for (MacroButtonProperties nextMacro : token.getMacroList(true)) {
                        if (newButtonProps.hashCodeForComparison()
                            == nextMacro.hashCodeForComparison()) {
                          alreadyExists = true;
                        }
                      }
                      if (alreadyExists) {
                        String tokenName = token.getName();
                        if (MapTool.getPlayer().isGM()) {
                          if (token.getGMName() != null) {
                            if (!token.getGMName().equals("")) {
                              tokenName = tokenName + "(" + token.getGMName() + ")";
                            }
                          }
                        }
                        alreadyExists =
                            confirmImport(
                                newButtonProps,
                                I18N.getText("confirm.macro.tokenLocation", tokenName));
                      }
                      if (!alreadyExists) {
                        new MacroButtonProperties(token, token.getMacroNextIndex(), newButtonProps);
                      }
                    }
                  }
                } else if (tokenId != null) {
                  Token token =
                      MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
                  for (MacroButtonProperties nextMacro : token.getMacroList(true)) {
                    if (newButtonProps.hashCodeForComparison()
                        == nextMacro.hashCodeForComparison()) {
                      alreadyExists = true;
                    }
                  }
                  if (alreadyExists) {
                    String tokenName = token.getName();
                    if (MapTool.getPlayer().isGM()) {
                      if (token.getGMName() != null) {
                        if (!token.getGMName().equals("")) {
                          tokenName = tokenName + "(" + token.getGMName() + ")";
                        }
                      }
                    }
                    alreadyExists =
                        confirmImport(
                            newButtonProps, I18N.getText("confirm.macro.tokenLocation", tokenName));
                  }
                  if (!alreadyExists) {
                    new MacroButtonProperties(token, token.getMacroNextIndex(), newButtonProps);
                  }
                }
              } catch (IOException ioe) {
                ioe.printStackTrace();
                MapTool.showError(I18N.getText("msg.error.macro.exportSetFail", ioe));
              }
            }
          });
    }
  }

  private class ImportMacroSetAction extends AbstractAction {
    public ImportMacroSetAction() {
      putValue(Action.NAME, I18N.getText("action.macro.importSet"));
    }

    public ImportMacroSetAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {

      JFileChooser chooser = MapTool.getFrame().getLoadMacroSetFileChooser();

      if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      final File selectedFile = chooser.getSelectedFile();
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              try {
                List<MacroButtonProperties> newButtonProps =
                    PersistenceUtil.loadMacroSet(selectedFile);
                Boolean alreadyExists = false;
                for (MacroButtonProperties nextProps : newButtonProps) {
                  alreadyExists = false;
                  if (panelClass.equals("GlobalPanel")) {
                    for (MacroButtonProperties nextMacro : MacroButtonPrefs.getButtonProperties()) {
                      if (nextProps.hashCodeForComparison() == nextMacro.hashCodeForComparison()) {
                        alreadyExists = true;
                      }
                    }
                    if (alreadyExists) {
                      alreadyExists =
                          confirmImport(
                              nextProps,
                              I18N.getText(
                                  "confirm.macro.panelLocation", I18N.getText("panel.Global")));
                    }
                    if (!alreadyExists) {
                      new MacroButtonProperties(
                          panelClass, MacroButtonPrefs.getNextIndex(), nextProps);
                    }
                  } else if (panelClass.equals("CampaignPanel")) {
                    for (MacroButtonProperties nextMacro :
                        MapTool.getCampaign().getMacroButtonPropertiesArray()) {
                      if (nextProps.hashCodeForComparison() == nextMacro.hashCodeForComparison()) {
                        alreadyExists = true;
                      }
                    }
                    if (alreadyExists) {
                      alreadyExists =
                          confirmImport(
                              nextProps,
                              I18N.getText(
                                  "confirm.macro.panelLocation", I18N.getText("panel.Campaign")));
                    }
                    if (!alreadyExists) {
                      new MacroButtonProperties(
                          panelClass, MapTool.getCampaign().getMacroButtonNextIndex(), nextProps);
                    }
                  } else if (panelClass.equals("GmPanel")) {
                    for (MacroButtonProperties nextMacro :
                        MapTool.getCampaign().getGmMacroButtonPropertiesArray()) {
                      if (nextProps.hashCodeForComparison() == nextMacro.hashCodeForComparison()) {
                        alreadyExists = true;
                      }
                    }
                    if (alreadyExists) {
                      alreadyExists =
                          confirmImport(
                              nextProps,
                              I18N.getText(
                                  "confirm.macro.panelLocation", I18N.getText("panel.Gm")));
                    }
                    if (!alreadyExists) {
                      new MacroButtonProperties(
                          panelClass, MapTool.getCampaign().getGmMacroButtonNextIndex(), nextProps);
                    }
                  } else if (panelClass.equals("SelectionPanel")) {
                    if (areaGroup != null) {
                      if (areaGroup
                          .getGroupLabel()
                          .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
                        for (Token nextToken :
                            MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
                          alreadyExists = false;
                          for (MacroButtonProperties nextMacro : nextToken.getMacroList(true)) {
                            if (nextProps.hashCodeForComparison()
                                == nextMacro.hashCodeForComparison()) {
                              alreadyExists = true;
                            }
                          }
                          if (alreadyExists) {
                            alreadyExists =
                                confirmImport(
                                    nextProps,
                                    I18N.getText("confirm.macro.commonSelectionLocation"));
                          }
                          if (!alreadyExists) {
                            new MacroButtonProperties(
                                nextToken, nextToken.getMacroNextIndex(), nextProps);
                          }
                        }
                      } else if (tokenId != null) {
                        Token token =
                            MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
                        for (MacroButtonProperties nextMacro : token.getMacroList(true)) {
                          if (nextProps.hashCodeForComparison()
                              == nextMacro.hashCodeForComparison()) {
                            alreadyExists = true;
                          }
                        }
                        if (alreadyExists) {
                          String tokenName = token.getName();
                          if (MapTool.getPlayer().isGM()) {
                            if (token.getGMName() != null) {
                              if (!token.getGMName().equals("")) {
                                tokenName = tokenName + "(" + token.getGMName() + ")";
                              }
                            }
                          }
                          alreadyExists =
                              confirmImport(
                                  nextProps,
                                  I18N.getText("confirm.macro.tokenLocation", tokenName));
                        }
                        if (!alreadyExists) {
                          new MacroButtonProperties(token, token.getMacroNextIndex(), nextProps);
                        }
                      }
                    }
                  } else if (tokenId != null) {
                    Token token =
                        MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
                    for (MacroButtonProperties nextMacro : token.getMacroList(true)) {
                      if (nextProps.hashCodeForComparison() == nextMacro.hashCodeForComparison()) {
                        alreadyExists = true;
                      }
                    }
                    if (alreadyExists) {
                      String tokenName = token.getName();
                      if (MapTool.getPlayer().isGM()) {
                        if (token.getGMName() != null) {
                          if (!token.getGMName().equals("")) {
                            tokenName = tokenName + "(" + token.getGMName() + ")";
                          }
                        }
                      }
                      alreadyExists =
                          confirmImport(
                              nextProps, I18N.getText("confirm.macro.tokenLocation", tokenName));
                    }
                    if (!alreadyExists) {
                      new MacroButtonProperties(token, token.getMacroNextIndex(), nextProps);
                    }
                  }
                }
              } catch (IOException ioe) {
                MapTool.showError("msg.error.macro.importSetFail", ioe);
              }
            }
          });
    }
  }

  private class ExportMacroSetAction extends AbstractAction {
    public ExportMacroSetAction() {
      putValue(Action.NAME, I18N.getText("action.macro.exportSet"));
    }

    public ExportMacroSetAction(String name) {
      putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event) {

      JFileChooser chooser = MapTool.getFrame().getSaveMacroSetFileChooser();

      if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      final File selectedFile = chooser.getSelectedFile();

      if (selectedFile.exists()) {
        if (selectedFile.getName().endsWith(".mtmacset")) {
          if (!MapTool.confirm(I18N.getText("confirm.macro.exportSetInto"))) {
            return;
          }
        } else if (!MapTool.confirm(I18N.getText("confirm.macro.exportSetOverwrite"))) {
          return;
        }
      }

      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              try {
                if (panelClass.equals("GlobalPanel")) {
                  PersistenceUtil.saveMacroSet(
                      MacroButtonPrefs.getButtonProperties(), selectedFile);
                } else if (panelClass.equals("CampaignPanel")) {
                  PersistenceUtil.saveMacroSet(
                      MapTool.getCampaign().getMacroButtonPropertiesArray(), selectedFile);
                } else if (panelClass.equals("GmPanel")) {
                  PersistenceUtil.saveMacroSet(
                      MapTool.getCampaign().getGmMacroButtonPropertiesArray(), selectedFile);
                } else if (panelClass.equals("SelectionPanel")) {
                  if (areaGroup != null) {
                    if (areaGroup
                        .getGroupLabel()
                        .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
                      Boolean checkComparisons = MapTool.confirm("confirm.macro.checkComparisons");
                      List<MacroButtonProperties> commonMacros =
                          MapTool.getFrame().getSelectionPanel().getCommonMacros();
                      List<MacroButtonProperties> exportList =
                          new ArrayList<MacroButtonProperties>();
                      Boolean trusted = true;
                      Boolean allowExport = true;
                      for (MacroButtonProperties nextMacro : commonMacros) {
                        trusted = true;
                        allowExport = true;
                        for (Token nextToken :
                            MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
                          if (!AppUtil.playerOwns(nextToken)) {
                            trusted = false;
                          }
                          if (nextToken.getMacroList(trusted).size() > 0) {
                            for (MacroButtonProperties nextCompMacro :
                                nextToken.getMacroList(trusted)) {
                              if (nextCompMacro.hashCodeForComparison()
                                      == nextMacro.hashCodeForComparison()
                                  && (!MapTool.getPlayer().isGM()
                                      || (!MapTool.getPlayer().isGM()
                                          && !nextCompMacro.getAllowPlayerEdits()))) {
                                allowExport = false;
                              }
                            }
                          } else {
                            allowExport = false;
                          }
                        }
                        if (checkComparisons) {
                          if (confirmCommonExport(nextMacro)) {
                            if (trusted && allowExport) {
                              exportList.add(nextMacro);
                            } else {
                              MapTool.showWarning(
                                  I18N.getText(
                                      "msg.warning.macro.willNotExport", nextMacro.getLabel()));
                            }
                          } else {
                            return;
                          }
                        } else {
                          if (trusted && allowExport) {
                            exportList.add(nextMacro);
                          } else {
                            MapTool.showWarning(
                                I18N.getText(
                                    "msg.warning.macro.willNotExport", nextMacro.getLabel()));
                          }
                        }
                      }
                      PersistenceUtil.saveMacroSet(exportList, selectedFile);
                    } else if (tokenId != null) {
                      Token token =
                          MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
                      Boolean trusted = AppUtil.playerOwns(token);
                      List<MacroButtonProperties> exportList =
                          new ArrayList<MacroButtonProperties>();
                      for (MacroButtonProperties nextMacro : token.getMacroList(trusted)) {
                        if (MapTool.getPlayer().isGM()
                            || (!MapTool.getPlayer().isGM() && nextMacro.getAllowPlayerEdits())) {
                          exportList.add(nextMacro);
                        } else {
                          MapTool.showWarning(
                              I18N.getText(
                                  "msg.warning.macro.willNotExport", nextMacro.getLabel()));
                        }
                      }
                      PersistenceUtil.saveMacroSet(exportList, selectedFile);
                    }
                  }
                } else if (tokenId != null) {
                  Token token =
                      MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
                  PersistenceUtil.saveMacroSet(token.getMacroList(true), selectedFile);
                }
              } catch (IOException ioe) {
                ioe.printStackTrace();
                MapTool.showError(I18N.getText("msg.error.macro.exportSetFail", ioe));
              }
            }
          });
    }
  }

  private class ClearGroupAction extends AbstractAction {
    public ClearGroupAction() {
      putValue(Action.NAME, I18N.getText("action.macro.clearGroup"));
    }

    public void actionPerformed(ActionEvent event) {
      if (MapTool.confirm(I18N.getText("confirm.macro.clearGroup", macroGroup))) {
        if (panelClass.equals("GlobalPanel")) {
          GlobalPanel.deleteButtonGroup(macroGroup);
        } else if (panelClass.equals("CampaignPanel")) {
          CampaignPanel.deleteButtonGroup(macroGroup);
        } else if (panelClass.equals("GmPanel")) {
          GmPanel.deleteButtonGroup(macroGroup);
        } else if (tokenId != null) {
          MapTool.getFrame()
              .getCurrentZoneRenderer()
              .getZone()
              .getToken(tokenId)
              .deleteMacroGroup(macroGroup, true);
        }
      }
    }
  }

  private class ClearPanelAction extends AbstractAction {
    public ClearPanelAction() {
      putValue(Action.NAME, I18N.getText("action.macro.clearPanel"));
    }

    public void actionPerformed(ActionEvent event) {
      if (panelClass.equals("GlobalPanel")) {
        if (MapTool.confirm(
            I18N.getText("confirm.macro.clearPanel", I18N.getText("panel.Global")))) {
          GlobalPanel.clearPanel();
        }
      } else if (panelClass.equals("CampaignPanel")) {
        if (MapTool.confirm(
            I18N.getText("confirm.macro.clearPanel", I18N.getText("panel.Campaign")))) {
          CampaignPanel.clearPanel();
        }
      } else if (panelClass.equals("GmPanel")) {
        if (MapTool.confirm(I18N.getText("confirm.macro.clearPanel", I18N.getText("panel.Gm")))) {
          GmPanel.clearPanel();
        }
      } else if (tokenId != null) {
        if (panelClass.equals("ImpersonatePanel")) {
          if (MapTool.confirm(
              I18N.getText("confirm.macro.clearPanel", I18N.getText("panel.Impersonate")))) {
            MapTool.getFrame()
                .getCurrentZoneRenderer()
                .getZone()
                .getToken(tokenId)
                .deleteAllMacros(true);
          }
        }
      }
    }
  }

  private Boolean confirmCommonExport(MacroButtonProperties buttonMacro) {
    Boolean failComparison = false;
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
                  "confirm.macro.failComparison", buttonMacro.getLabel(), comparisonResults));
    }
    return failComparison;
  }

  private Boolean confirmImport(MacroButtonProperties importMacro, String location) {
    return !MapTool.confirm(I18N.getText("confirm.macro.import", importMacro.getLabel(), location));
  }
}
