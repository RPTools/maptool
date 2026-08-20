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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ImagePanel;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.util.PersistenceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookupTablePanel extends AbeillePanel<LookupTableImagePanelModel> {
  private static final long serialVersionUID = -4404834393567699280L;
  private static final Logger log = LogManager.getLogger(LookupTablePanel.class);

  /** the panel which contains everything */
  private JPanel viewContainer;

  /** an icon and name label view for {@link LookupTable}s */
  private ImagePanel imagePanel;

  /** a tabular view of {@link LookupTable}s' details */
  private LookupTableDetailsTablePanel detailsTablePanel;

  /** manages the different views and so only one is visible as a time */
  private CardLayout viewLayout;

  public LookupTablePanel() {
    super(new LookupTablePaneView().getRootComponent());
    panelInit();
  }

  /** the view options */
  private enum ViewMode {
    /** a view of {@code LookupTable} images and their names as labels. */
    ICONS,
    /** a tabular view of {@code LookupTable} details. */
    DETAILS
  }

  /** the default view */
  private ViewMode currentView = ViewMode.ICONS;

  /** update the view */
  public void updateView() {
    getNewButton().setVisible(MapTool.getPlayer().isGM());
    getEditButton().setVisible(MapTool.getPlayer().isGM());
    getExportButton().setVisible(MapTool.getPlayer().isGM());
    getImportButton().setVisible(MapTool.getPlayer().isGM());
    getDuplicateButton().setVisible(MapTool.getPlayer().isGM());
    getDeleteButton().setVisible(MapTool.getPlayer().isGM());
    revalidate();

    detailsTablePanel.refreshStructure(); // columns for GM/player differ
    refreshData();
  }

  public void initViewCardContainerPanel() {

    // create the image panel
    imagePanel = new ImagePanel();
    imagePanel.setModel(new LookupTableImagePanelModel(this));
    imagePanel.setSelectionMode(ImagePanel.SelectionMode.SINGLE);
    imagePanel.addMouseListener(
        new MouseAdapter() {
          /** double-clicking the mouse should roll on the appropriate LookupTable */
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              LookupTable lookupTable = getSelectedLookupTable();
              if (lookupTable == null) {
                return;
              }
              lookupTableRoll(lookupTable);
            }
          }
        });

    // add scrolling to the image panel
    JScrollPane imageScroll =
        new JScrollPane(
            imagePanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // create the table panel (which already caters for scrolling)
    detailsTablePanel = new LookupTableDetailsTablePanel();
    JTable detailsTable = detailsTablePanel.getDetailsTable();
    detailsTable.addMouseListener(
        new MouseAdapter() {
          /**
           * double-clicking the mouse on the table should:
           *
           * <ul>
           *   <li>if a Player -> roll on the appropriate LookupTable
           *   <li>if a GM -> edit the table, or roll on the table if the image column was the
           *       target
           */
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() != 2) {
              return;
            }
            LookupTable lookupTable = getSelectedLookupTable();
            if (lookupTable == null) {
              return;
            }
            if (MapTool.getPlayer().isGM()) {
              if (detailsTable.columnAtPoint(e.getPoint()) == 0) {
                lookupTableRoll(lookupTable);
              } else {
                new EditLookupTablePanel().showDialog(lookupTable, false);
              }
            } else {
              lookupTableRoll(lookupTable);
            }
          }
        });

    viewLayout = new CardLayout();
    viewContainer = new JPanel(viewLayout);

    viewContainer.add(imageScroll, ViewMode.ICONS.name());
    viewContainer.add(detailsTablePanel, ViewMode.DETAILS.name());

    replaceComponent("mainForm", "viewCardContainerPanel", viewContainer);
  }

  /** Switch to the image panel icon view */
  public void showIconsView() {
    currentView = ViewMode.ICONS;
    viewLayout.show(viewContainer, currentView.name());
  }

  /** Switch to the table panel details view */
  public void showDetailsView() {
    currentView = ViewMode.DETAILS;
    viewLayout.show(viewContainer, currentView.name());
  }

  public ImagePanel getImagePanel() {
    return imagePanel;
  }

  public LookupTableDetailsTablePanel getDetailsTablePanel() {
    return detailsTablePanel;
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

  public JButton getImportButton() {
    return (JButton) getComponent("importButton");
  }

  public JButton getExportButton() {
    return (JButton) getComponent("exportButton");
  }

  public JToggleButton getViewIconsToggleButton() {
    return (JToggleButton) getComponent("viewIconsToggleButton");
  }

  public JToggleButton getViewDetailsToggleButton() {
    return (JToggleButton) getComponent("viewDetailsToggleButton");
  }

  public void initDuplicateButton() {
    getDuplicateButton().setMargin(new Insets(0, 0, 0, 0));
    getDuplicateButton().setIcon(RessourceManager.getSmallIcon(Icons.ACTION_COPY));
    getDuplicateButton()
        .addActionListener(
            e -> {
              LookupTable selected = getSelectedLookupTable();
              if (selected == null) {
                return;
              }
              LookupTable lookupTable = new LookupTable(selected);
              lookupTable.setName("Copy of " + lookupTable.getName());
              new EditLookupTablePanel().showDialog(lookupTable, true);

              imagePanel.clearSelection();
              refreshStructure();
            });
  }

  public void initEditTableButton() {
    getEditButton().setMargin(new Insets(0, 0, 0, 0));
    getEditButton().setIcon(RessourceManager.getSmallIcon(Icons.ACTION_EDIT));
    getEditButton()
        .addActionListener(
            e -> {
              LookupTable lookupTable = getSelectedLookupTable();
              if (lookupTable == null) {
                return;
              }
              new EditLookupTablePanel().showDialog(lookupTable, false);
              refreshData();
            });
  }

  public void initNewTableButton() {
    getNewButton().setMargin(new Insets(0, 0, 0, 0));
    getNewButton().setIcon(RessourceManager.getSmallIcon(Icons.ACTION_NEW));
    getNewButton()
        .addActionListener(
            e -> {
              new EditLookupTablePanel().showDialog(new LookupTable(), true);
              imagePanel.clearSelection();
              refreshStructure();
            });
  }

  public void initDeleteTableButton() {
    getDeleteButton().setMargin(new Insets(0, 0, 0, 0));
    getDeleteButton().setIcon(RessourceManager.getSmallIcon(Icons.ACTION_DELETE));
    getDeleteButton()
        .addActionListener(
            e -> {
              LookupTable lookupTable = getSelectedLookupTable();
              if (lookupTable == null) {
                return;
              }
              if (MapTool.confirm("LookupTablePanel.confirm.delete", lookupTable.getName())) {
                MapTool.serverCommand().deleteLookupTable(lookupTable.getName());
                imagePanel.clearSelection();
                refreshStructure();
              }
            });
  }

  public void initImportButton() {
    getImportButton().setMargin(new Insets(0, 0, 0, 0));
    var icon = RessourceManager.getSmallIcon(Icons.ACTION_IMPORT);
    if (icon != null) {
      getImportButton().setIcon(icon);
      getImportButton().setToolTipText(getImportButton().getText());
      getImportButton().setText("");
    }
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
                    boolean alreadyExists = lookupTables.keySet().contains(newTable.getName());
                    if (alreadyExists
                        && !MapTool.confirm(
                            "LookupTablePanel.confirm.import", newTable.getName())) {
                      return;
                    }
                    MapTool.serverCommand().putLookupTable(newTable);
                    imagePanel.clearSelection();
                    refreshStructure();
                  });
            });
  }

  public void initExportButton() {
    getExportButton().setMargin(new Insets(0, 0, 0, 0));
    var icon = RessourceManager.getSmallIcon(Icons.ACTION_EXPORT);
    if (icon != null) {
      getExportButton().setIcon(icon);
      getExportButton().setToolTipText(getExportButton().getText());
      getExportButton().setText("");
    }
    getExportButton()
        .addActionListener(
            e -> {
              JFileChooser chooser = MapTool.getFrame().getSaveTableFileChooser();
              boolean tryAgain = true;
              while (tryAgain) {
                if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                  return;
                }
                var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
                var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
                if (saveDir.startsWith(installDir)) {
                  MapTool.showWarning("msg.warning.saveTableToInstallDir");
                } else {
                  tryAgain = false;
                }
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
                      LookupTable lookupTable = getSelectedLookupTable();
                      if (lookupTable == null) {
                        return;
                      }
                      PersistenceUtil.saveTable(lookupTable, selectedFile);
                      MapTool.showInformation(
                          I18N.getText("LookupTablePanel.info.saved", selectedFile.getName()));
                    } catch (IOException ioe) {
                      log.error("Error while saving table", ioe);
                      MapTool.showError("LookupTablePanel.error.saveFailed", ioe);
                    }
                  });
            });
  }

  public void initToggleViewButtonGroup() {
    JToggleButton viewIconsToggleButton = getViewIconsToggleButton();
    JToggleButton viewDetailsToggleButton = getViewDetailsToggleButton();

    viewIconsToggleButton.setMargin(new Insets(0, 0, 0, 0));
    viewDetailsToggleButton.setMargin(new Insets(0, 0, 0, 0));

    var iconViewIcon = RessourceManager.getSmallIcon(Icons.TABLEPANEL_VIEW_ICONS);
    if (iconViewIcon != null) {
      viewIconsToggleButton.setIcon(iconViewIcon);
      viewIconsToggleButton.setText("");
    } else {
      viewIconsToggleButton.setText(I18N.getText("LookupTablePanel.viewIcons"));
    }
    var iconViewDetails = RessourceManager.getSmallIcon(Icons.TABLEPANEL_VIEW_DETAILS);
    if (iconViewDetails != null) {
      viewDetailsToggleButton.setIcon(iconViewDetails);
      viewDetailsToggleButton.setText("");
    } else {
      viewDetailsToggleButton.setText(I18N.getText("LookupTablePanel.viewDetails"));
    }

    viewIconsToggleButton.setToolTipText(I18N.getText("LookupTablePanel.viewIcons.tooltip"));
    viewDetailsToggleButton.setToolTipText(I18N.getText("LookupTablePanel.viewDetails.tooltip"));

    ButtonGroup group = new ButtonGroup();
    group.add(viewIconsToggleButton);
    group.add(viewDetailsToggleButton);

    viewIconsToggleButton.setSelected(true);

    viewIconsToggleButton.addActionListener(e -> showIconsView());
    viewDetailsToggleButton.addActionListener(e -> showDetailsView());
  }

  /**
   * Retrieve the selected {@code LookupTable} from the appropriate panel
   *
   * @return the selected {@code LookupTable}
   */
  private LookupTable getSelectedLookupTable() {
    if (currentView == ViewMode.DETAILS) {
      return detailsTablePanel.getSelectedLookupTable();
    }
    return getSelectedLookupTableFromIcons();
  }

  /**
   * Retrieve the selected {@link LookupTable} from the icons image panel
   *
   * @return the selected LookupTable
   */
  private LookupTable getSelectedLookupTableFromIcons() {
    List<Object> ids = imagePanel.getSelectedIds();
    if (ids == null || ids.isEmpty()) {
      return null;
    }
    return MapTool.getCampaign().getLookupTableMap().get(ids.get(0));
  }

  /** Refresh the panel cards' inner panels which list the {@link LookupTable}s */
  public void refreshData() {
    detailsTablePanel.refreshData();
    imagePanel.repaint();
  }

  /** Refresh the panel cards' inner panels which list the {@link LookupTable}s */
  public void refreshStructure() {
    detailsTablePanel.refreshStructure();
    imagePanel.repaint();
  }

  /** Reset the panel */
  public void reset() {
    detailsTablePanel.reset();
    imagePanel.repaint();
  }

  /** Perform a roll on the given {@link LookupTable} */
  public void lookupTableRoll(LookupTable lookupTable) {
    MapTool.getFrame().getCommandPanel().commitCommand("/tbl \"" + lookupTable.getName() + "\"");
  }
}
