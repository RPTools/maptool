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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
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
import org.apache.commons.lang.StringUtils;

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

  private Token getToken() {
    return MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
  }

  private void addActions() {
    addBasicActions();
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
      addBasicActions();
      add(new JSeparator());
      add(new ClearPanelAction());
    }
  }

  private void addBasicActions() {
    add(new AddMacroAction());
    add(new JSeparator());
    add(new ImportMacroAction());
    add(new JSeparator());
    add(new ImportMacroSetAction());
    add(new ExportMacroSetAction());
    add(new JSeparator());
    if (areaGroup != null) {
      add(new ExportMacroGroupAction());
      add(new RenameGroupAction());
    }
    add(new ClearGroupAction());
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
            Token token = getToken();
            new MacroButtonProperties(token, token.getMacroNextIndex(), macroGroup);
          }
        }
      } else if (tokenId != null) {
        Token token = getToken();
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
          () -> {
            try {
              MacroButtonProperties newButtonProps = PersistenceUtil.loadMacro(selectedFile);
              if (newButtonProps != null) {
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
                      Token token = getToken();
                      for (MacroButtonProperties nextMacro : token.getMacroList(true)) {
                        if (newButtonProps.hashCodeForComparison()
                            == nextMacro.hashCodeForComparison()) {
                          alreadyExists = true;
                        }
                      }
                      if (alreadyExists) {
                        String tokenName = token.getName();
                        if (MapTool.getPlayer().isGM() && !StringUtils.isEmpty(token.getGMName())) {
                          tokenName = tokenName + "(" + token.getGMName() + ")";
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
                  Token token = getToken();
                  for (MacroButtonProperties nextMacro : token.getMacroList(true)) {
                    if (newButtonProps.hashCodeForComparison()
                        == nextMacro.hashCodeForComparison()) {
                      alreadyExists = true;
                    }
                  }
                  if (alreadyExists) {
                    String tokenName = token.getName();
                    if (MapTool.getPlayer().isGM() && !StringUtils.isEmpty(token.getGMName())) {
                      tokenName += "(" + token.getGMName() + ")";
                    }
                    alreadyExists =
                        confirmImport(
                            newButtonProps, I18N.getText("confirm.macro.tokenLocation", tokenName));
                  }
                  if (!alreadyExists) {
                    new MacroButtonProperties(token, token.getMacroNextIndex(), newButtonProps);
                  }
                }
              }
            } catch (IOException ioe) {
              ioe.printStackTrace();
              MapTool.showError(I18N.getText("msg.error.macro.exportSetFail", ioe));
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
            /**
             * Handles imports to a token-based panel (Selected or Impersonated). De-conflicts
             * within the context of the given token.
             *
             * @param newButtonProps the loaded macro buttons to import
             * @param token the Token receiving the new macros
             */
            public void addMacrosToToken(List<MacroButtonProperties> newButtonProps, Token token) {
              List<MacroButtonProperties> existingMacros = token.getMacroList(true);
              Map<Integer, MacroButtonProperties> existingForComparison =
                  existingMacros.stream()
                      .collect(
                          Collectors.toMap(
                              MacroButtonProperties::hashCodeForComparison, Function.identity()));
              List<MacroButtonProperties> toAdd = new ArrayList<>(newButtonProps.size());
              int nextIndex = token.getMacroNextIndex();
              String tokenName = token.getName();
              if (MapTool.getPlayer().isGM() && !StringUtils.isEmpty(token.getGMName())) {
                tokenName = tokenName + "(" + token.getGMName() + ")";
              }
              for (MacroButtonProperties nextProps : newButtonProps) {
                Integer code = nextProps.hashCodeForComparison();
                boolean shouldAdd = true;
                if (existingForComparison.containsKey(code)) {
                  // confirmImport is inverted?
                  shouldAdd =
                      !confirmImport(
                          nextProps, I18N.getText("confirm.macro.tokenLocation", tokenName));
                }
                if (shouldAdd) {
                  MacroButtonProperties theMacro =
                      new MacroButtonProperties(token, nextIndex++, nextProps, false);
                  toAdd.add(theMacro);
                  existingForComparison.put(theMacro.hashCodeForComparison(), theMacro);
                }
              }
              if (!toAdd.isEmpty()) {
                MapTool.serverCommand()
                    .updateTokenProperty(token, Token.Update.saveMacroList, toAdd, false);
              }
            }

            /**
             * Handles imports to the Global, Campaign, or GM panels.
             *
             * @param newButtonProps the loaded macro buttons to import
             */
            public void addMacrosToGeneralPanel(List<MacroButtonProperties> newButtonProps) {
              List<MacroButtonProperties> existingMacros;
              String locationText;
              if ("GlobalPanel".equals(panelClass)) {
                existingMacros = MacroButtonPrefs.getButtonProperties();
                locationText =
                    I18N.getText("confirm.macro.panelLocation", I18N.getText("panel.Global"));
              } else if ("CampaignPanel".equals(panelClass)) {
                existingMacros = MapTool.getCampaign().getMacroButtonPropertiesArray();
                locationText =
                    I18N.getText("confirm.macro.panelLocation", I18N.getText("panel.Campaign"));
              } else if ("GmPanel".equals(panelClass)) {
                existingMacros = MapTool.getCampaign().getGmMacroButtonPropertiesArray();
                locationText =
                    I18N.getText("confirm.macro.panelLocation", I18N.getText("panel.Gm"));
              } else {
                throw new IllegalStateException("Error importing macros - panelClass changed?");
              }
              Map<Integer, MacroButtonProperties> existingForComparison =
                  existingMacros.stream()
                      .collect(
                          Collectors.toMap(
                              MacroButtonProperties::hashCodeForComparison, Function.identity()));
              List<MacroButtonProperties> toAdd = new ArrayList<>(newButtonProps.size());
              for (MacroButtonProperties nextProps : newButtonProps) {
                Integer code = nextProps.hashCodeForComparison();
                boolean shouldAdd = true;
                if (existingForComparison.containsKey(code)) {
                  // confirmImport is inverted?
                  shouldAdd = !confirmImport(nextProps, locationText);
                }
                if (shouldAdd) {
                  MacroButtonProperties theMacro =
                      new MacroButtonProperties(panelClass, 0, nextProps, false);
                  toAdd.add(theMacro);
                  existingForComparison.put(theMacro.hashCodeForComparison(), theMacro);
                }
              }
              if ("GlobalPanel".equals(panelClass)) {
                MacroButtonPrefs.saveMacroButtonsAtNextIndex(toAdd);
              } else if ("CampaignPanel".equals(panelClass)) {
                MapTool.getCampaign().addMacroButtonPropertiesAtNextIndex(toAdd, false);
              } else if ("GmPanel".equals(panelClass)) {
                MapTool.getCampaign().addMacroButtonPropertiesAtNextIndex(toAdd, true);
              }
            }

            public void run() {
              try {
                List<MacroButtonProperties> newButtonProps =
                    PersistenceUtil.loadMacroSet(selectedFile);
                Boolean alreadyExists;
                if (newButtonProps != null) {
                  if ("GlobalPanel".equals(panelClass)
                      || "CampaignPanel".equals(panelClass)
                      || "GmPanel".equals(panelClass)) {
                    addMacrosToGeneralPanel(newButtonProps);
                  } else {
                    if (areaGroup != null
                        && areaGroup
                            .getGroupLabel()
                            .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
                      for (Token nextToken :
                          MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
                        addMacrosToToken(newButtonProps, nextToken);
                      }
                    } else if (tokenId != null) {
                      addMacrosToToken(newButtonProps, getToken());
                    } else {
                      throw new IllegalStateException("Macros import - invalid panel.");
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

  /** Export macros from a single group. */
  private class ExportMacroGroupAction extends AbstractAction {
    public ExportMacroGroupAction() {
      putValue(Action.NAME, I18N.getText("action.macro.exportGroup"));
    }

    public ExportMacroGroupAction(String name) {
      putValue(Action.NAME, name);
    }

    /**
     * Filters a list of Macro Buttons to return only those in the specified macroGroup.
     *
     * @param buttons the list to filter
     * @param macroGroup the macroGroup to match
     * @return a list of matching macros
     */
    private List<MacroButtonProperties> buttonsInGroup(
        List<MacroButtonProperties> buttons, String macroGroup) {
      return buttons.stream()
          .filter(b -> macroGroup.equals(b.getGroup()))
          .collect(Collectors.toList());
    }

    public void actionPerformed(ActionEvent event) {

      JFileChooser chooser = MapTool.getFrame().getSaveMacroSetFileChooser();

      boolean tryAgain = true;
      while (tryAgain) {
        if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
          return;
        }
        var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
        var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
        if (saveDir.startsWith(installDir)) {
          MapTool.showWarning("msg.warning.saveMacrosToInstallDir");
        } else {
          tryAgain = false;
        }
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
          () -> {
            try {
              if (panelClass.equals("GlobalPanel")) {
                PersistenceUtil.saveMacroSet(
                    buttonsInGroup(MacroButtonPrefs.getButtonProperties(), macroGroup),
                    selectedFile);
              } else if (panelClass.equals("CampaignPanel")) {
                PersistenceUtil.saveMacroSet(
                    buttonsInGroup(
                        MapTool.getCampaign().getMacroButtonPropertiesArray(), macroGroup),
                    selectedFile);
              } else if (panelClass.equals("GmPanel")) {
                PersistenceUtil.saveMacroSet(
                    buttonsInGroup(
                        MapTool.getCampaign().getGmMacroButtonPropertiesArray(), macroGroup),
                    selectedFile);
              } else if (panelClass.equals("SelectionPanel")) {
                if (areaGroup != null) {
                  if (areaGroup
                      .getGroupLabel()
                      .equals(I18N.getText("component.areaGroup.macro.commonMacros"))) {
                    Boolean checkComparisons = MapTool.confirm("confirm.macro.checkComparisons");
                    List<MacroButtonProperties> commonMacros =
                        MapTool.getFrame().getSelectionPanel().getCommonMacros();
                    commonMacros = buttonsInGroup(commonMacros, macroGroup); // filter early
                    List<MacroButtonProperties> exportList = new ArrayList<MacroButtonProperties>();
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
                    Token token = getToken();
                    Boolean trusted = AppUtil.playerOwns(token);
                    List<MacroButtonProperties> exportList = new ArrayList<MacroButtonProperties>();
                    List<MacroButtonProperties> candidateList =
                        buttonsInGroup(token.getMacroList(trusted), macroGroup);
                    for (MacroButtonProperties nextMacro : candidateList) {
                      if (MapTool.getPlayer().isGM()
                          || (!MapTool.getPlayer().isGM() && nextMacro.getAllowPlayerEdits())) {
                        exportList.add(nextMacro);
                      } else {
                        MapTool.showWarning(
                            I18N.getText("msg.warning.macro.willNotExport", nextMacro.getLabel()));
                      }
                    }
                    PersistenceUtil.saveMacroSet(exportList, selectedFile);
                  }
                }
              } else if (tokenId != null) {
                Token token = getToken();
                PersistenceUtil.saveMacroSet(
                    buttonsInGroup(token.getMacroList(true), macroGroup), selectedFile);
              }
            } catch (IOException ioe) {
              ioe.printStackTrace();
              MapTool.showError(I18N.getText("msg.error.macro.exportSetFail", ioe));
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

      boolean tryAgain = true;
      while (tryAgain) {
        if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
          return;
        }
        var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
        var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
        if (saveDir.startsWith(installDir)) {
          MapTool.showWarning("msg.warning.saveMacrosToInstallDir");
        } else {
          tryAgain = false;
        }
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
          () -> {
            try {
              if (panelClass.equals("GlobalPanel")) {
                PersistenceUtil.saveMacroSet(MacroButtonPrefs.getButtonProperties(), selectedFile);
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
                    List<MacroButtonProperties> exportList = new ArrayList<MacroButtonProperties>();
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
                    Token token = getToken();
                    Boolean trusted = AppUtil.playerOwns(token);
                    List<MacroButtonProperties> exportList = new ArrayList<MacroButtonProperties>();
                    for (MacroButtonProperties nextMacro : token.getMacroList(trusted)) {
                      if (MapTool.getPlayer().isGM()
                          || (!MapTool.getPlayer().isGM() && nextMacro.getAllowPlayerEdits())) {
                        exportList.add(nextMacro);
                      } else {
                        MapTool.showWarning(
                            I18N.getText("msg.warning.macro.willNotExport", nextMacro.getLabel()));
                      }
                    }
                    PersistenceUtil.saveMacroSet(exportList, selectedFile);
                  }
                }
              } else if (tokenId != null) {
                Token token = getToken();
                PersistenceUtil.saveMacroSet(token.getMacroList(true), selectedFile);
              }
            } catch (IOException ioe) {
              ioe.printStackTrace();
              MapTool.showError(I18N.getText("msg.error.macro.exportSetFail", ioe));
            }
          });
    }
  }

  private class RenameGroupAction extends AbstractAction {

    public RenameGroupAction() {
      putValue(Action.NAME, I18N.getText("action.macro.renameGroup"));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      String newMacroGroupName =
          JOptionPane.showInputDialog(I18N.getText("panel.NewGroupName"), macroGroup);
      if (newMacroGroupName != null && !newMacroGroupName.equals(macroGroup)) {
        if (panelClass.equals("CampaignPanel")
            || panelClass.equals("GlobalPanel")
            || panelClass.equals("GmPanel")) {
          areaGroup.getPanel().renameMacroGroup(macroGroup, newMacroGroupName);
        } else if (tokenId != null) {
          getToken().renameMacroGroup(macroGroup, newMacroGroupName);
        }
      }
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
      } else if (tokenId != null && "ImpersonatePanel".equals(panelClass)) {
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
                  "confirm.macro.failComparison", buttonMacro.getLabel(), comparisonResults));
    }
    return failComparison;
  }

  private Boolean confirmImport(MacroButtonProperties importMacro, String location) {
    return !MapTool.confirm(I18N.getText("confirm.macro.import", importMacro.getLabel(), location));
  }
}
