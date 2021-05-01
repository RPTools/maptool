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
import java.io.IOException;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MediaPlayerAdapter;
import net.rptools.maptool.client.tool.AI_Tool;
import net.rptools.maptool.client.tool.AI_UseVblTool;
import net.rptools.maptool.client.tool.BoardTool;
import net.rptools.maptool.client.tool.DrawTopologySelectionTool;
import net.rptools.maptool.client.tool.FacingTool;
import net.rptools.maptool.client.tool.GridTool;
import net.rptools.maptool.client.tool.MeasureTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.tool.StampTool;
import net.rptools.maptool.client.tool.TextTool;
import net.rptools.maptool.client.tool.drawing.*;
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

  public ToolbarPanel(Toolbox tbox) {
    setRollover(true);

    toolbox = tbox;
    optionPanel = new JPanel(new CardLayout());
    optionPanel.setOpaque(false);

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
            "net/rptools/maptool/client/image/tool/draw-blue.png",
            "net/rptools/maptool/client/image/tool/draw-blue-off.png",
            createDrawPanel(),
            I18N.getText("tools.drawing.tooltip"));
    add(drawButton);
    templateButton =
        createButton(
            "net/rptools/maptool/client/image/tool/temp-blue.png",
            "net/rptools/maptool/client/image/tool/temp-blue-off.png",
            createTemplatePanel(),
            I18N.getText("tools.template.tooltip"));
    add(templateButton);
    fogButton =
        createButton(
            "net/rptools/maptool/client/image/tool/fog-blue.png",
            "net/rptools/maptool/client/image/tool/fog-blue-off.png",
            createFogPanel(),
            I18N.getText("tools.fog.tooltip"));
    add(fogButton);
    topologyButton =
        createButton(
            "net/rptools/maptool/client/image/tool/eye-blue.png",
            "net/rptools/maptool/client/image/tool/eye-blue-off.png",
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
            "net/rptools/maptool/client/image/audio/mute.png",
            "net/rptools/maptool/client/image/audio/volume.png",
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
            "net/rptools/maptool/client/image/tool/select-all-blue.png",
            "net/rptools/maptool/client/image/tool/select-all-blue-off.png",
            I18N.getText("tools.token.fow.all.tooltip"),
            TokenSelection.ALL);

    add(
        createTokenSelectionButton(
            "net/rptools/maptool/client/image/tool/select-me-blue.png",
            "net/rptools/maptool/client/image/tool/select-me-blue-off.png",
            I18N.getText("tools.token.fow.gm.tooltip"),
            TokenSelection.GM));
    add(tokenSelectionButtonAll);
    add(
        createTokenSelectionButton(
            "net/rptools/maptool/client/image/tool/select-pc-blue.png",
            "net/rptools/maptool/client/image/tool/select-pc-blue-off.png",
            I18N.getText("tools.token.fow.pc.tooltip"),
            TokenSelection.PC));
    add(
        createTokenSelectionButton(
            "net/rptools/maptool/client/image/tool/select-npc-blue.png",
            "net/rptools/maptool/client/image/tool/select-npc-blue-off.png",
            I18N.getText("tools.token.fow.npc.tooltip"),
            TokenSelection.NPC));

    add(Box.createHorizontalStrut(10));
    add(new JSeparator(JSeparator.VERTICAL));
    add(Box.createHorizontalStrut(10));

    tokenSelectionButtonAll.setSelected(true);
    // Jamz: End panel

    // the "Select Map" button
    add(createZoneSelectionButton());

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

    setBorderSizes(optionPanel, pointerGroupButton);
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

  private JToggleButton createPointerGroupButton() {
    final OptionPanel pointerGroupOptionPanel = createPointerPanel();
    final JToggleButton pointerGroupButton =
        createButton(
            "net/rptools/maptool/client/image/tool/pointer-blue.png",
            "net/rptools/maptool/client/image/tool/pointer-blue-off.png",
            pointerGroupOptionPanel,
            I18N.getText("tools.interaction.tooltip"));

    final SidePanel aiPanel = new SidePanel();
    aiPanel.add(AI_Tool.class);
    aiPanel.add(AI_UseVblTool.class);

    pointerGroupOptionPanel.add(Box.createHorizontalStrut(5));
    pointerGroupOptionPanel.add(aiPanel);
    pointerGroupButton.setSelected(true);
    pointerGroupOptionPanel.activate();

    return pointerGroupButton;
  }

  public JButton createZoneSelectionButton() {
    String title = I18N.getText("tools.zoneselector.tooltip");

    final JButton button =
        new JButton(
            title,
            new ImageIcon(
                getClass()
                    .getClassLoader()
                    .getResource("net/rptools/maptool/client/image/tool/btn-world.png")));
    button.setToolTipText(title);
    button.setContentAreaFilled(false);

    SwingUtil.makePopupMenuButton(button, ZoneSelectionPopup::new, true);
    return button;
  }

  private OptionPanel createPointerPanel() {
    OptionPanel panel = new OptionPanel();
    panel.add(PointerTool.class);
    panel.add(MeasureTool.class);
    return panel;
  }

  private OptionPanel createDrawPanel() {
    OptionPanel panel = new OptionPanel();
    panel.add(DeleteDrawingTool.class);
    panel.add(FreehandTool.class);
    panel.add(LineTool.class);
    panel.add(RectangleTool.class);
    panel.add(OvalTool.class);
    panel.add(TextTool.class);
    panel.add(DiamondTool.class);
    return panel;
  }

  private OptionPanel createTemplatePanel() {
    OptionPanel panel = new OptionPanel();
    panel.add(RadiusTemplateTool.class);
    panel.add(RadiusCellTemplateTool.class);
    panel.add(ConeTemplateTool.class);
    panel.add(LineTemplateTool.class);
    panel.add(LineCellTemplateTool.class);
    panel.add(BurstTemplateTool.class);
    panel.add(BlastTemplateTool.class);
    panel.add(WallTemplateTool.class);
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
    panel.add(RectangleExposeTool.class);
    panel.add(OvalExposeTool.class);
    panel.add(PolygonExposeTool.class);
    panel.add(FreehandExposeTool.class);
    panel.add(DiamondExposeTool.class);
    return panel;
  }

  private OptionPanel createTopologyPanel() {
    OptionPanel panel = new OptionPanel();
    panel.add(RectangleTopologyTool.class);
    panel.add(HollowRectangleTopologyTool.class);
    panel.add(OvalTopologyTool.class);
    panel.add(HollowOvalTopologyTool.class);
    panel.add(PolygonTopologyTool.class);
    panel.add(PolyLineTopologyTool.class);
    panel.add(CrossTopologyTool.class);
    panel.add(DiamondTopologyTool.class);
    panel.add(HollowDiamondTopologyTool.class);

    // Add with space to denote button is not part of the Topology Panel button group
    final SidePanel topologySelectionPanel = new SidePanel();
    topologySelectionPanel.add(DrawTopologySelectionTool.class);

    panel.add(Box.createHorizontalStrut(5));
    panel.add(topologySelectionPanel);

    return panel;
  }

  private void setBorderSizes(JPanel container, JToggleButton source) {
    for (var component : container.getComponents()) {
      if (component instanceof JPanel) setBorderSizes((JPanel) component, source);
      if (component instanceof JToggleButton) {
        ((JToggleButton) component).setBorder(source.getBorder());
      }
      if (component instanceof AbstractButton) {
        component.setSize(source.getSize());
      }
    }
  }

  private JToggleButton createButton(
      final String icon, final String offIcon, final OptionPanel panel, String tooltip) {
    final JToggleButton button = new JToggleButton();
    button.setToolTipText(tooltip);
    button.setContentAreaFilled(false);

    button.addActionListener(
        e -> {
          if (button.isSelected()) {
            panel.activate();
            ((CardLayout) optionPanel.getLayout()).show(optionPanel, icon);
            // This is has only an effect when the panel is used in fullscreen mode.
            optionPanel.setSize(panel.getPreferredSize());
          }
        });
    try {
      button.setIcon(new ImageIcon(ImageUtil.getImage(offIcon)));
      button.setSelectedIcon(new ImageIcon(ImageUtil.getImage(icon)));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    optionPanel.add(panel, icon);
    buttonGroup.add(button);

    return button;
  }

  private JToggleButton createMuteButton(
      final String icon, final String offIcon, String mutetooltip, String unmutetooltip) {
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

    try {
      button.setIcon(createIcon(offIcon, 25, 25));
      button.setSelectedIcon(createIcon(icon, 25, 25));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    if (MediaPlayerAdapter.getGlobalMute()) {
      button.doClick();
    }

    return button;
  }

  private ImageIcon createIcon(String strResource, int w, int h) throws IOException {
    return ImageUtil.resizeImage(
        new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(strResource))),
        w,
        h);
  }

  private JToggleButton createTokenSelectionButton(
      final String icon, final String offIcon, String tooltip, TokenSelection tokenSelection) {
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
    try {
      button.setIcon(new ImageIcon(ImageUtil.getImage(offIcon)));
      button.setSelectedIcon(new ImageIcon(ImageUtil.getImage(icon)));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
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

  private class OptionPanel extends JPanel {

    private Class<? extends Tool> firstTool;
    private Class<? extends Tool> currentTool;

    public OptionPanel() {
      setFloatable(false);
      setRollover(true);
      setBorder(null);
      setBorderPainted(false);
      setOpaque(false);
      setLayout(ToolbarPanel.this.getOrientation());

      ToolbarPanel.this.addPropertyChangeListener(
          "orientation", evt -> setLayout((Integer) evt.getNewValue()));
    }

    private void setLayout(int orientation) {
      if (orientation == JToolBar.HORIZONTAL) setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      else setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public void add(Class<? extends Tool> toolClass) {
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
      tool.setOpaque(false);
      add(tool);
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
  private class SidePanel extends JPanel {

    public SidePanel() {
      setFloatable(false);
      setRollover(true);
      setBorder(null);
      setBorderPainted(false);
      setOpaque(false);
      setLayout(ToolbarPanel.this.getOrientation());

      ToolbarPanel.this.addPropertyChangeListener(
          "orientation", evt -> setLayout((Integer) evt.getNewValue()));
    }

    private void setLayout(int orientation) {
      if (orientation == JToolBar.HORIZONTAL) setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      else setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public void add(Class<? extends Tool> toolClass) {
      final Tool tool = toolbox.createTool(toolClass);
      tool.setOpaque(false);
      add(tool);
    }
  }
}
