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
package net.rptools.maptool.client.ui.lookuptable;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.rptools.lib.swing.ImagePanel;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.util.PersistenceUtil;

public class LookupTablePanel extends AbeillePanel<LookupTableImagePanelModel> {
  private static final long serialVersionUID = -4404834393567699280L;

  private ImagePanel imagePanel;
  private JDialog editorDialog;
  private EditLookupTablePanel editorPanel;

  public LookupTablePanel() {
    super("net/rptools/maptool/client/ui/forms/lookupTablePanel.xml");
    panelInit();
  }

  public void updateView() {
    getButtonPanel().setVisible(MapTool.getPlayer().isGM());
    revalidate();
    repaint();
  }

  public JDialog getEditorDialog() {
    if (editorDialog == null) {
      editorDialog = new JDialog(MapTool.getFrame(), true);
      editorDialog.setSize(500, 400);
      editorDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
      editorDialog.add(editorPanel);
      SwingUtil.centerOver(editorDialog, MapTool.getFrame());
    }
    return editorDialog;
  }

  public void initImagePanel() {
    imagePanel = new ImagePanel();
    imagePanel.setBackground(Color.white);
    imagePanel.setModel(new LookupTableImagePanelModel(this));
    imagePanel.setSelectionMode(ImagePanel.SelectionMode.SINGLE);
    imagePanel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              List<Object> ids = getImagePanel().getSelectedIds();
              if (ids == null || ids.size() == 0) {
                return;
              }
              LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(ids.get(0));
              if (lookupTable == null) {
                return;
              }
              MapTool.getFrame()
                  .getCommandPanel()
                  .commitCommand("/tbl \"" + lookupTable.getName() + "\"");
            }
          }
        });
    replaceComponent(
        "mainForm",
        "imagePanel",
        new JScrollPane(
            imagePanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
  }

  public JPanel getButtonPanel() {
    return (JPanel) getComponent("buttonPanel");
  }

  public void initEditorPanel() {
    editorPanel = new EditLookupTablePanel();
  }

  public JButton getNewButton() {
    return (JButton) getComponent("newButton");
  }

  public JButton getEditButton() {
    return (JButton) getComponent("editButton");
  }

  public JButton getDeleteButton() {
    return (JButton) getComponent("deleteButton");
  }

  public JButton getDuplicateButton() {
    return (JButton) getComponent("duplicateButton");
  }

  public JButton getRunButton() {
    return (JButton) getComponent("runButton");
  }

  public ImagePanel getImagePanel() {
    return imagePanel;
  }

  public JButton getImportButton() {
    return (JButton) getComponent("importButton");
  }

  public JButton getExportButton() {
    return (JButton) getComponent("exportButton");
  }

  public void initDuplicateButton() {
    getDuplicateButton().setMargin(new Insets(0, 0, 0, 0));
    getDuplicateButton()
        .addActionListener(
            e -> {
              List<Object> ids = getImagePanel().getSelectedIds();
              if (ids == null || ids.size() == 0) {
                return;
              }
              LookupTable lookupTable =
                  new LookupTable(MapTool.getCampaign().getLookupTableMap().get(ids.get(0)));
              lookupTable.setName("Copy of " + lookupTable.getName());

              editorPanel.attach(lookupTable);

              getEditorDialog().setTitle(I18N.getString("LookupTablePanel.msg.titleNew"));
              getEditorDialog().setVisible(true);

              imagePanel.clearSelection();
              repaint();
            });
  }

  public void initEditTableButton() {
    getEditButton().setMargin(new Insets(0, 0, 0, 0));
    getEditButton()
        .addActionListener(
            e -> {
              List<Object> ids = getImagePanel().getSelectedIds();
              if (ids == null || ids.size() == 0) {
                return;
              }
              LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(ids.get(0));

              editorPanel.attach(lookupTable);

              getEditorDialog().setTitle(I18N.getString("LookupTablePanel.msg.titleEdit"));
              getEditorDialog().setVisible(true);
            });
  }

  public void initNewTableButton() {
    getNewButton().setMargin(new Insets(0, 0, 0, 0));
    getNewButton()
        .addActionListener(
            e -> {
              editorPanel.attach(null);

              getEditorDialog().setTitle(I18N.getString("LookupTablePanel.msg.titleNew"));
              getEditorDialog().setVisible(true);

              imagePanel.clearSelection();
              repaint();
            });
  }

  public void initDeleteTableButton() {
    getDeleteButton().setMargin(new Insets(0, 0, 0, 0));
    getDeleteButton()
        .addActionListener(
            e -> {
              List<Object> ids = getImagePanel().getSelectedIds();
              if (ids == null || ids.size() == 0) {
                return;
              }
              LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(ids.get(0));

              if (MapTool.confirm("LookupTablePanel.confirm.delete", lookupTable.getName())) {
                MapTool.getCampaign().getLookupTableMap().remove(lookupTable.getName());
                MapTool.serverCommand()
                    .updateCampaign(MapTool.getCampaign().getCampaignProperties());

                imagePanel.clearSelection();
                repaint();
              }
            });
  }

  public void initImportButton() {
    getImportButton().setMargin(new Insets(0, 0, 0, 0));
    getImportButton()
        .addActionListener(
            e -> {
              JFileChooser chooser = MapTool.getFrame().getLoadTableFileChooser();

              if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                return;
              }
              final File selectedFile = chooser.getSelectedFile();
              EventQueue.invokeLater(
                  () -> {
                    Map<String, LookupTable> lookupTables =
                        MapTool.getCampaign().getLookupTableMap();
                    LookupTable newTable = PersistenceUtil.loadTable(selectedFile);
                    Boolean alreadyExists = lookupTables.keySet().contains(newTable.getName());
                    if (alreadyExists) {
                      if (MapTool.confirm("LookupTablePanel.confirm.import", newTable.getName())) {
                        lookupTables.remove(newTable.getName());
                      } else {
                        return;
                      }
                      lookupTables.put(newTable.getName(), newTable);
                      imagePanel.clearSelection();
                      imagePanel.repaint();
                      MapTool.serverCommand()
                          .updateCampaign(MapTool.getCampaign().getCampaignProperties());
                    }
                    lookupTables.put(newTable.getName(), newTable);
                    imagePanel.clearSelection();
                    imagePanel.repaint();
                    MapTool.serverCommand()
                        .updateCampaign(MapTool.getCampaign().getCampaignProperties());
                  });
            });
  }

  public void initExportButton() {
    getExportButton().setMargin(new Insets(0, 0, 0, 0));
    getExportButton()
        .addActionListener(
            e -> {
              JFileChooser chooser = MapTool.getFrame().getSaveTableFileChooser();

              if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                return;
              }
              final File selectedFile = chooser.getSelectedFile();
              EventQueue.invokeLater(
                  () -> {
                    if (selectedFile.exists()) {
                      if (selectedFile.getName().endsWith(".mttable")) {
                        if (!MapTool.confirm(
                            "LookupTablePanel.confirm.export", selectedFile.getName())) {
                          return;
                        }
                      } else if (!MapTool.confirm(
                          "LookupTablePanel.confirm.overwrite", selectedFile.getName())) {
                        return;
                      }
                    }
                    try {
                      List<Object> ids = getImagePanel().getSelectedIds();
                      if (ids == null || ids.size() == 0) {
                        return;
                      }
                      LookupTable lookupTable =
                          MapTool.getCampaign().getLookupTableMap().get(ids.get(0));
                      PersistenceUtil.saveTable(lookupTable, selectedFile);
                      MapTool.showInformation(
                          I18N.getText("LookupTablePanel.info.saved", selectedFile.getName()));
                    } catch (IOException ioe) {
                      ioe.printStackTrace();
                      MapTool.showError("LookupTablePanel.error.saveFailed", ioe);
                    }
                  });
            });
  }
}
