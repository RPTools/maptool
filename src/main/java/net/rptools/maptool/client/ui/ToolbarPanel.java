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
package net.rptools.maptool.client.ui;

import java.awt.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MediaPlayerAdapter;
import net.rptools.maptool.client.swing.TopologyModeSelectionPanel;
import net.rptools.maptool.client.tool.AI_Tool;
import net.rptools.maptool.client.tool.AI_UseVblTool;
import net.rptools.maptool.client.tool.BoardTool;
import net.rptools.maptool.client.tool.FacingTool;
import net.rptools.maptool.client.tool.GridTool;
import net.rptools.maptool.client.tool.MeasureTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.tool.StampTool;
import net.rptools.maptool.client.tool.TextTool;
import net.rptools.maptool.client.tool.drawing.*;
import net.rptools.maptool.client.ui.theme.IconMap;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.Zone.TokenSelection;
import net.rptools.maptool.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ToolbarPanel extends JToolBar {

  private static final Logger log = LogManager.getLogger(ToolbarPanel.class);

  private final ButtonGroup buttonGroup = new ButtonGroup();
  private final ButtonGroup tokenSelectionbuttonGroup = new ButtonGroup();
  private final JToggleButton pointerGroupButton;
  private final JToggleButton drawButton;
  private final JToggleButton templateButton;
  private final JToggleButton fogButton;
  private final JToggleButton topologyButton;
  private final Component horizontalSpacer;
  private final JPanel optionPanel;
  private final Toolbox toolbox;
  private final JButton mapselect;

  public static final int ICON_W_H = 32;

  public ToolbarPanel(Toolbox tbox) {
    setRollover(true);

    toolbox = tbox;
    optionPanel = new JPanel(new CardLayout());

    final JSeparator vertSplit = new JSeparator(JSeparator.VERTICAL);
    final Component vertSpacer = Box.createHorizontalStrut(10);

    final JSeparator horizontalSplit = new JSeparator(JSeparator.HORIZONTAL);
    horizontalSplit.setVisible(false);
    horizontalSpacer = Box.createVerticalStrut(10);
    horizontalSpacer.setVisible(false);

    pointerGroupButton = createPointerGroupButton();
    add(pointerGroupButton);
    drawButton =
        createButton(
            Icons.TOOLBAR_DRAW_ON,
            Icons.TOOLBAR_DRAW_OFF,
            createDrawPanel(),
            I18N.getText("tools.drawing.tooltip"));
    add(drawButton);
    templateButton =
        createButton(
            Icons.TOOLBAR_TEMPLATE_ON,
            Icons.TOOLBAR_TEMPLATE_OFF,
            createTemplatePanel(),
            I18N.getText("tools.template.tooltip"));
    add(templateButton);
    fogButton =
        createButton(
            Icons.TOOLBAR_FOG_ON,
            Icons.TOOLBAR_FOG_OFF,
            createFogPanel(),
            I18N.getText("tools.fog.tooltip"));
    add(fogButton);
    topologyButton =
        createButton(
            Icons.TOOLBAR_TOPOLOGY_ON,
            Icons.TOOLBAR_TOPOLOGY_OFF,
            createTopologyPanel(),
            I18N.getText("tools.topo.tooltip"));
    add(topologyButton);

    add(vertSplit);
    add(horizontalSplit);
    add(vertSpacer);
    add(horizontalSpacer);

    add(optionPanel);

    add(Box.createGlue());

    // the Volume icon
    add(
        createMuteButton(
            Icons.TOOLBAR_VOLUME_OFF,
            Icons.TOOLBAR_VOLUME_ON,
            I18N.getText("tools.mute.tooltip"),
            I18N.getText("tools.unmute.tooltip")));

    // the Volume slider
    final int MAX_SLIDER = 10;
    final int INIT_VALUE = (int) Math.round(MediaPlayerAdapter.getGlobalVolume() * MAX_SLIDER);
    JSlider jslider = new JSlider(0, MAX_SLIDER, INIT_VALUE);
    jslider.addChangeListener(
        e -> MediaPlayerAdapter.setGlobalVolume((double) jslider.getValue() / MAX_SLIDER));

    // Create the label table
    Hashtable labelTable = new Hashtable();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(MAX_SLIDER, new JLabel("100"));
    jslider.setLabelTable(labelTable);
    jslider.setPaintLabels(true);

    add(jslider);
    // End slider

    add(Box.createHorizontalStrut(10));
    add(new JSeparator(JSeparator.VERTICAL));
    add(Box.createHorizontalStrut(10));

    // Jamz: Adding new Token Selection option buttons
    // Default selected button created with reference to set selection true
    final JToggleButton tokenSelectionButtonAll =
        createTokenSelectionButton(
            Icons.TOOLBAR_TOKENSELECTION_ALL_ON,
            Icons.TOOLBAR_TOKENSELECTION_ALL_OFF,
            I18N.getText("tools.token.fow.all.tooltip"),
            TokenSelection.ALL);

    add(
        createTokenSelectionButton(
            Icons.TOOLBAR_TOKENSELECTION_ME_ON,
            Icons.TOOLBAR_TOKENSELECTION_ME_OFF,
            I18N.getText("tools.token.fow.gm.tooltip"),
            TokenSelection.GM));
    add(tokenSelectionButtonAll);
    add(
        createTokenSelectionButton(
            Icons.TOOLBAR_TOKENSELECTION_PC_ON,
            Icons.TOOLBAR_TOKENSELECTION_PC_OFF,
            I18N.getText("tools.token.fow.pc.tooltip"),
            TokenSelection.PC));
    add(
        createTokenSelectionButton(
            Icons.TOOLBAR_TOKENSELECTION_NPC_ON,
            Icons.TOOLBAR_TOKENSELECTION_NPC_OFF,
            I18N.getText("tools.token.fow.npc.tooltip"),
            TokenSelection.NPC));

    add(Box.createHorizontalStrut(10));
    add(new JSeparator(JSeparator.VERTICAL));
    add(Box.createHorizontalStrut(10));

    tokenSelectionButtonAll.setSelected(true);
    // Jamz: End panel

    // the "Select Map" button
    mapselect = createZoneSelectionButton();
    add(mapselect);

    // Non visible tools
    tbox.createTool(GridTool.class);
    tbox.createTool(BoardTool.class);
    tbox.createTool(FacingTool.class);
    tbox.createTool(StampTool.class);

    addPropertyChangeListener(
        "orientation",
        evt -> {
          int orientation = (Integer) evt.getNewValue();

          horizontalSplit.setVisible(orientation == JToolBar.VERTICAL);
          horizontalSpacer.setVisible(orientation == JToolBar.VERTICAL);

          vertSplit.setVisible(orientation == JToolBar.HORIZONTAL);
          vertSpacer.setVisible(orientation == JToolBar.HORIZONTAL);
        });
  }

  public JPanel getOptionPanel() {
    return optionPanel;
  }

  public JToggleButton getPointerGroupButton() {
    return pointerGroupButton;
  }

  public JToggleButton getDrawButton() {
    return drawButton;
  }

  public JToggleButton getTemplateButton() {
    return templateButton;
  }

  public JToggleButton getFogButton() {
    return fogButton;
  }

  public JToggleButton getTopologyButton() {
    return topologyButton;
  }

  public int getOptionsPanelIndex() {
    return getComponentIndex(horizontalSpacer) + 1;
  }

  public JButton getMapselect() {
    return mapselect;
  }

  private JToggleButton createPointerGroupButton() {
    final OptionPanel pointerGroupOptionPanel = createPointerPanel();
    final JToggleButton pointerGroupButton =
        createButton(
            Icons.TOOLBAR_POINTERTOOL_ON,
            Icons.TOOLBAR_POINTERTOOL_OFF,
            pointerGroupOptionPanel,
            I18N.getText("tools.interaction.tooltip"));

    final SidePanel aiPanel = new SidePanel();

    var aiTool = aiPanel.add(AI_Tool.class);
    aiTool.setIcon(IconMap.getIcon(Icons.TOOLBAR_POINTERTOOL_AI_OFF, ICON_W_H));
    aiTool.setSelectedIcon(IconMap.getIcon(Icons.TOOLBAR_POINTERTOOL_AI_ON, ICON_W_H));

    var vblTool = aiPanel.add(AI_UseVblTool.class);
    vblTool.setIcon(IconMap.getIcon(Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_OFF, ICON_W_H));
    vblTool.setSelectedIcon(IconMap.getIcon(Icons.TOOLBAR_POINTERTOOL_VBL_ON_MOVE_ON, ICON_W_H));

    pointerGroupOptionPanel.add(Box.createHorizontalStrut(5));
    pointerGroupOptionPanel.add(aiPanel);
    pointerGroupButton.setSelected(true);
    pointerGroupOptionPanel.activate();

    return pointerGroupButton;
  }

  public JButton createZoneSelectionButton() {
    String title = I18N.getText("tools.zoneselector.tooltip");

    final JButton button = new JButton(title, IconMap.getIcon(Icons.TOOLBAR_ZONE, ICON_W_H));
    button.setToolTipText(title);

    SwingUtil.makePopupMenuButton(button, ZoneSelectionPopup::new, true);
    return button;
  }

  private OptionPanel createPointerPanel() {
    OptionPanel panel = new OptionPanel();
    panel
        .add(PointerTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_POINTERTOOL_POINTER, ICON_W_H));
    panel
        .add(MeasureTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_POINTERTOOL_MEASURE, ICON_W_H));
    return panel;
  }

  private OptionPanel createDrawPanel() {
    OptionPanel panel = new OptionPanel();
    panel
        .add(DeleteDrawingTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_DELETE, ICON_W_H));
    panel.add(FreehandTool.class).setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_FREEHAND, ICON_W_H));
    panel.add(LineTool.class).setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_LINE, ICON_W_H));
    panel.add(RectangleTool.class).setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_BOX, ICON_W_H));
    panel.add(OvalTool.class).setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_OVAL, ICON_W_H));
    panel.add(TextTool.class).setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_TEXT, ICON_W_H));
    panel.add(DiamondTool.class).setIcon(IconMap.getIcon(Icons.TOOLBAR_DRAW_DIAMOND, ICON_W_H));
    return panel;
  }

  private OptionPanel createTemplatePanel() {
    OptionPanel panel = new OptionPanel();
    panel
        .add(RadiusTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_RADIUS, ICON_W_H));
    panel
        .add(RadiusCellTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_RADIUS_CELL, ICON_W_H));
    panel
        .add(ConeTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_CONE, ICON_W_H));
    panel
        .add(LineTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_LINE, ICON_W_H));
    panel
        .add(LineCellTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_LINE_CELL, ICON_W_H));
    panel
        .add(BurstTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_BURST, ICON_W_H));
    panel
        .add(BlastTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_BLAST, ICON_W_H));
    panel
        .add(WallTemplateTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TEMPLATE_WALL, ICON_W_H));
    return panel;
  }

  private OptionPanel createFogPanel() {
    OptionPanel panel =
        new OptionPanel() {
          @Override
          protected void activate() {
            super.activate();
            Campaign c = MapTool.getCampaign();
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            // Check if there is a map. Fix #1605
            if (zr != null) {
              boolean tokensSelected = !zr.getSelectedTokenSet().isEmpty();
              if (tokensSelected && !c.hasUsedFogToolbar() && !MapTool.isHostingServer()) {
                MapTool.addLocalMessage(
                    MessageUtil.getFormattedSystemMsg(
                        I18N.getText("ToolbarPanel.manualFogActivated")));
                MapTool.showWarning("ToolbarPanel.manualFogActivated");
              }
            }
          }
        };
    panel
        .add(RectangleExposeTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_FOG_EXPOSE_BOX, ICON_W_H));
    panel
        .add(OvalExposeTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_FOG_EXPOSE_OVAL, ICON_W_H));
    panel
        .add(PolygonExposeTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_FOG_EXPOSE_POLYGON, ICON_W_H));
    panel
        .add(FreehandExposeTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_FOG_EXPOSE_FREEHAND, ICON_W_H));
    panel
        .add(DiamondExposeTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_FOG_EXPOSE_DIAMOND, ICON_W_H));
    return panel;
  }

  private OptionPanel createTopologyPanel() {
    OptionPanel panel = new OptionPanel();
    panel
        .add(RectangleTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_BOX, ICON_W_H));
    panel
        .add(HollowRectangleTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_BOX_HOLLOW, ICON_W_H));
    panel
        .add(OvalTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_OVAL, ICON_W_H));
    panel
        .add(HollowOvalTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW, ICON_W_H));
    panel
        .add(PolygonTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_POLYGON, ICON_W_H));
    panel
        .add(PolyLineTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_POLYLINE, ICON_W_H));
    panel
        .add(CrossTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_CROSS, ICON_W_H));
    panel
        .add(DiamondTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_DIAMOND, ICON_W_H));
    panel
        .add(HollowDiamondTopologyTool.class)
        .setIcon(IconMap.getIcon(Icons.TOOLBAR_TOPOLOGY_DIAMOND_HOLLOW, ICON_W_H));

    // Add with space to separate mode button group from shape button group.
    panel.add(Box.createHorizontalStrut(10));

    final var topologyModeSelectionPanel = new TopologyModeSelectionPanel();
    panel.add(topologyModeSelectionPanel);

    return panel;
  }

  private JToggleButton createButton(
          final Icons icon, final Icons offIcon, final OptionPanel panel, String tooltip) {
    final JToggleButton button = new JToggleButton();
    button.setToolTipText(tooltip);

    button.addActionListener(
        e -> {
          if (button.isSelected()) {
            panel.activate();
            ((CardLayout) optionPanel.getLayout()).show(optionPanel, tooltip);
            // This is has only an effect when the panel is used in fullscreen mode.
            optionPanel.setSize(panel.getPreferredSize());
          }
        });

    button.setIcon(IconMap.getIcon(offIcon, ICON_W_H));
    button.setSelectedIcon(IconMap.getIcon(icon, ICON_W_H));

    optionPanel.add(panel, tooltip);
    buttonGroup.add(button);

    return button;
  }

  private JToggleButton createMuteButton(
      final Icons icon, final Icons offIcon, String mutetooltip, String unmutetooltip) {
    final JToggleButton button = new JToggleButton();
    button.setToolTipText(mutetooltip);
    button.addActionListener(
        e -> {
          MediaPlayerAdapter.setGlobalMute(button.isSelected());
          if (button.isSelected()) {
            button.setToolTipText(unmutetooltip);
          } else {
            button.setToolTipText(mutetooltip);
          }
        });

    button.setIcon(IconMap.getIcon(offIcon, ICON_W_H));
    button.setSelectedIcon(IconMap.getIcon(icon, ICON_W_H));

    if (MediaPlayerAdapter.getGlobalMute()) {
      button.doClick();
    }

    return button;
  }

  private JToggleButton createTokenSelectionButton(
      final Icons icon, final Icons offIcon, String tooltip, TokenSelection tokenSelection) {
    final JToggleButton button = new JToggleButton();
    button.setToolTipText(tooltip);
    button.addActionListener(
        e -> {
          if (button.isSelected()) {
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            // Check if there is a map. Fix #1605
            if (zr != null) {
              zr.getZone().setTokenSelection(tokenSelection);
              MapTool.getFrame().refresh();
            }
          }
        });
    button.setIcon(IconMap.getIcon(offIcon, ICON_W_H));
    button.setSelectedIcon(IconMap.getIcon(icon, ICON_W_H));
    tokenSelectionbuttonGroup.add(button);
    return button;
  }

  /**
   * Return the current floating status of the ToolbarPanel.
   *
   * @return true if floating, false otherwise
   */
  private boolean isFloating() {
    return getUI() instanceof BasicToolBarUI && ((BasicToolBarUI) ui).isFloating();
  }

  /**
   * Show or hide the ToolbarPanel, even if it is floating.
   *
   * @param visible should the ToolbarPanel be visible or not
   */
  @Override
  public void setVisible(boolean visible) {
    if (isFloating()) {
      SwingUtilities.getRoot(this).setVisible(visible);
    } else {
      super.setVisible(visible);
    }
  }

  private class OptionPanel extends JToolBar {

    private Class<? extends Tool> firstTool;
    private Class<? extends Tool> currentTool;

    public OptionPanel() {
      setFloatable(false);
      setRollover(true);
      setBorder(null);
      setBorderPainted(false);

      ToolbarPanel.this.addPropertyChangeListener(
          "orientation", evt -> setOrientation((Integer) evt.getNewValue()));
    }

    public Tool add(Class<? extends Tool> toolClass) {
      if (firstTool == null) {
        firstTool = toolClass;
      }
      final Tool tool = toolbox.createTool(toolClass);
      tool.addActionListener(
          e -> {
            if (tool.isSelected()) {
              currentTool = tool.getClass();
            }
          });
      add(tool);
      return tool;
    }

    protected void activate() {
      if (currentTool == null) {
        currentTool = firstTool;
      }
      toolbox.setSelectedTool(currentTool);
    }
  }

  /*
   * Stand-alone toolbar with meant to not interact with standard toolbar
   */
  private class SidePanel extends JToolBar {

    public SidePanel() {
      setFloatable(false);
      setRollover(true);
      setBorder(null);
      setBorderPainted(false);

      ToolbarPanel.this.addPropertyChangeListener(
          "orientation", evt -> setOrientation((Integer) evt.getNewValue()));
    }

    public Tool add(Class<? extends Tool> toolClass) {
      final Tool tool = toolbox.createTool(toolClass);
      add(tool);
      return tool;
    }
  }
}
