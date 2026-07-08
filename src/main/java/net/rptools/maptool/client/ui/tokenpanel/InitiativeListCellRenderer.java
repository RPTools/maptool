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
package net.rptools.maptool.client.ui.tokenpanel;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import net.rptools.lib.AwtUtil;
import net.rptools.lib.StringUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.label.FlatImageLabel;
import net.rptools.maptool.client.swing.label.FlatImageLabelFactory;
import net.rptools.maptool.client.ui.theme.Borders;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

/**
 * This is the renderer that shows a token in the initiative panel.
 *
 * @author Jay
 */
public class InitiativeListCellRenderer extends JPanel
    implements ListCellRenderer<TokenInitiative> {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** This is the panel showing initiative. It contains the state for display. */
  private final InitiativePanel initiativePanel;

  /** The label used to display the current item indicator. */
  private final JLabel currentIndicatorLabel;

  // region Column 2: the token label

  /** Contains the token image, name, and initiative, styled like a token label. */
  private final JPanel labelPanel;

  /**
   * Shows the token image, optionally with states. Shown on the right of {@link #textPanelTwoLines}
   * when the token is holding, otherwise on the left.
   */
  private final TokenImagePanel tokenImagePanel;

  /** Shows the textual parts of the label (name, GM name, initiative). */
  private final TextPanel textPanelTwoLines;

  /** Same as {@link #textPanelTwoLines} but shows information on one line. */
  private final TextPanel textPanelOneLine;

  /** Used to draw the background of the item. */
  private FlatImageLabel backgroundFlatImageLabel;

  // endregion

  /**
   * The text height for the background image label. Only the text is painted inside, the token
   * remains on the outside,
   */
  private final int textHeight;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** The size of an indicator. */
  public static final Dimension INDICATOR_SIZE = new Dimension(18, 16);

  /** The icon for the current indicator. */
  public static final Icon CURRENT_INDICATOR_ICON =
      RessourceManager.getSmallIcon(Icons.INITIATIVE_CURRENT_INDICATOR);

  /** Border used to show that an item is selected */
  public static final Border SELECTED_BORDER = RessourceManager.getBorder(Borders.RED);

  /** Border used to show that an item is not selected */
  public static final Border UNSELECTED_BORDER =
      BorderFactory.createEmptyBorder(
          RessourceManager.getBorder(Borders.RED).getTopMargin(),
          RessourceManager.getBorder(Borders.RED).getLeftMargin(),
          RessourceManager.getBorder(Borders.RED).getBottomMargin(),
          RessourceManager.getBorder(Borders.RED).getRightMargin());

  /** The size of the ICON shown in the list renderer */
  public static final int ICON_SIZE = 50;

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create a renderer for the initiative panel.
   *
   * @param aPanel The initiative panel containing view state.
   */
  public InitiativeListCellRenderer(InitiativePanel aPanel) {

    // Set up the panel
    initiativePanel = aPanel;

    /*
     * The main layout is two columns: one to hold the current initiative indicator, the other to
     * hold the "label" (which includes the token image, name, initiative, etc., all on a label
     * background).
     *
     * The label is itself made of _three_ columns, though at any point in time only two are used.
     * The first column shows the token image in a non-holding state. The second holds the textual
     * bits (name, GM name, initiative), possibly as two lines. The third holds the token image in a
     * holding state.
     */

    setLayout(new GridLayoutManager(1, 2, new Insets(6, 6, 9, 6), 7, 0, false, false));

    // The current indicator
    currentIndicatorLabel = new JLabel();
    currentIndicatorLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    currentIndicatorLabel.setPreferredSize(INDICATOR_SIZE);
    currentIndicatorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    currentIndicatorLabel.setVerticalAlignment(SwingConstants.CENTER);
    add(
        currentIndicatorLabel,
        new GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            INDICATOR_SIZE,
            INDICATOR_SIZE,
            INDICATOR_SIZE,
            0,
            false));

    labelPanel = new JPanel();
    labelPanel.setOpaque(false);
    labelPanel.setLayout(new GridLayoutManager(3, 3, new Insets(2, 3, 0, 4), 4, 0, false, false));
    add(
        labelPanel,
        new GridConstraints(
            0,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_VERTICAL,
            GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
            GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
            null,
            null,
            null,
            0,
            false));

    textPanelTwoLines = new TextPanel(true);
    labelPanel.add(
        textPanelTwoLines,
        new GridConstraints(
            1,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_VERTICAL,
            GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
            GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
            null,
            null,
            null,
            0,
            false));
    textPanelOneLine = new TextPanel(false);
    labelPanel.add(
        textPanelOneLine,
        new GridConstraints(
            1,
            1,
            1,
            1,
            GridConstraints.ANCHOR_WEST,
            GridConstraints.FILL_VERTICAL,
            GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
            GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
            null,
            null,
            null,
            0,
            false));

    textHeight = getFontMetrics(getFont()).getHeight();

    tokenImagePanel = new TokenImagePanel();
    tokenImagePanel.putClientProperty("html.disable", true);
    var iconSize = new Dimension(ICON_SIZE, ICON_SIZE);
    labelPanel.add(
        tokenImagePanel,
        new GridConstraints(
            0,
            0,
            3,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            iconSize,
            iconSize,
            iconSize,
            0,
            false));

    labelPanel.add(
        new Spacer(),
        new GridConstraints(
            0,
            1,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_VERTICAL,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            null,
            null,
            null,
            0,
            false));
    labelPanel.add(
        new Spacer(),
        new GridConstraints(
            2,
            1,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_VERTICAL,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            null,
            null,
            null,
            0,
            false));

    validate();
  }

  /*---------------------------------------------------------------------------------------------
   * ListCellRenderer Interface Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Component getListCellRendererComponent(
      JList<? extends TokenInitiative> list,
      TokenInitiative ti,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {
    setOpaque(false);

    TextPanel textPanel =
        initiativePanel.isInitStateSecondLine() && initiativePanel.isShowInitState()
            ? textPanelTwoLines
            : textPanelOneLine;
    textPanelTwoLines.setVisible(textPanel == textPanelTwoLines);
    textPanelOneLine.setVisible(textPanel == textPanelOneLine);

    Token token;
    if (ti == null || (token = ti.getToken()) == null) {
      // Can happen when deleting a token before all events have propagated
      currentIndicatorLabel.setIcon(null);
      textPanel.setName(null);
      textPanel.setInitiative(null);
      setBorder(UNSELECTED_BORDER);
      return this;
    }

    var labelRenderFactory = new FlatImageLabelFactory();
    backgroundFlatImageLabel = labelRenderFactory.getMapImageLabel(token);

    textPanel.setTokenIsVisible(token.isVisible());
    textPanel.setTokenIsNpc(token.getType() == Token.Type.NPC);

    // Show the indicator?
    var isCurrent = ti == initiativePanel.getList().getCurrentTokenInitiative();
    currentIndicatorLabel.setIcon(isCurrent ? CURRENT_INDICATOR_ICON : null);

    var nameText = new StringBuilder(token.getName());
    if (MapTool.getFrame().getInitiativePanel().hasGMPermission()) {
      var gmName = token.getGMName();
      if (!StringUtil.isEmpty(gmName)) {
        nameText.append(" (").append(gmName.trim()).append(")");
      }
    }
    textPanel.setName(nameText.toString());

    textPanel.setInitiative(initiativePanel.isShowInitState() ? ti.getState() : null);

    var layout = (GridLayoutManager) getLayout();
    layout
        .getConstraintsForComponent(labelPanel)
        .setAnchor(ti.isHolding() ? GridConstraints.ANCHOR_EAST : GridConstraints.ANCHOR_WEST);

    // Arrange the panel depending on whether the token is holding.
    var labelPanelLayout = (GridLayoutManager) labelPanel.getLayout();

    if (initiativePanel.isShowTokens()) {
      tokenImagePanel.setVisible(true);
      tokenImagePanel.setModel(token, initiativePanel.isShowTokenStates());

      var iconConstraints = labelPanelLayout.getConstraintsForComponent(tokenImagePanel);
      iconConstraints.setColumn(ti.isHolding() ? 2 : 0);
      int iconSize = initiativePanel.isShowTokenStates() ? ICON_SIZE : Math.max(textHeight + 4, 16);
      iconConstraints.myMinimumSize.setSize(iconSize, iconSize);
      iconConstraints.myMaximumSize.setSize(iconSize, iconSize);
      iconConstraints.myPreferredSize.setSize(iconSize, iconSize);
    } else {
      tokenImagePanel.setVisible(false);
    }

    // Selected?
    if (isSelected) {
      setBorder(SELECTED_BORDER);
    } else {
      setBorder(UNSELECTED_BORDER);
    }

    return this;
  }

  @Override
  protected void paintComponent(Graphics g) {
    var labelPanelBounds = labelPanel.getBounds();
    var textBounds =
        textPanelTwoLines.isVisible()
            ? textPanelTwoLines.getBounds()
            : textPanelOneLine.getBounds();

    // Horizontally, we want to paint around everything (image and text).
    // Vertically, we want to fit tightly to just the text, even if the image is larger.
    var bounds =
        new Rectangle(
            labelPanelBounds.x,
            labelPanelBounds.y + textBounds.y,
            labelPanelBounds.width,
            textBounds.height);

    // Render an image label with set dimensions and without text.
    backgroundFlatImageLabel.render(
        (Graphics2D) g, bounds.x, bounds.y, bounds.width, bounds.height, "");

    super.paintComponent(g);
  }

  private static final class TextPanel extends JPanel {
    private final JLabel nameLabel;
    private final JLabel initLabel;
    private final Function<String, String> initiativeFormatter;
    private boolean tokenIsVisible;
    private boolean tokenIsNpc;

    public TextPanel(boolean initiativeOnSecondLine) {
      super(
          new GridLayoutManager(
              initiativeOnSecondLine ? 2 : 1,
              initiativeOnSecondLine ? 1 : 2,
              new Insets(3, 0, 1, 0),
              initiativeOnSecondLine ? 0 : 10,
              0,
              false,
              true));
      setOpaque(false);

      nameLabel = new JLabel();
      nameLabel.setHorizontalTextPosition(SwingConstants.LEADING);
      nameLabel.putClientProperty("html.disable", true);
      nameLabel.setText("Ty");
      nameLabel.setBorder(BorderFactory.createEmptyBorder());
      nameLabel.setFont(getFont());
      nameLabel.setBackground(Color.blue);
      add(
          nameLabel,
          new GridConstraints(
              0,
              0,
              1,
              1,
              GridConstraints.ANCHOR_CENTER,
              GridConstraints.FILL_BOTH,
              GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
              GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
              new Dimension(0, 0),
              null,
              null,
              0,
              false));

      initLabel = new JLabel();
      initLabel.setHorizontalTextPosition(SwingConstants.LEADING);
      initLabel.putClientProperty("html.disable", true);
      initLabel.setText("Ty");
      initLabel.setBorder(BorderFactory.createEmptyBorder());
      initLabel.setFont(getFont());
      initLabel.setBackground(Color.red);
      add(
          initLabel,
          new GridConstraints(
              initiativeOnSecondLine ? 1 : 0,
              initiativeOnSecondLine ? 0 : 1,
              1,
              1,
              GridConstraints.ANCHOR_CENTER,
              GridConstraints.FILL_BOTH,
              GridConstraints.SIZEPOLICY_FIXED,
              GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
              null,
              null,
              null,
              0,
              false));

      initiativeFormatter =
          initiativeOnSecondLine
              ? Function.identity()
              : init -> init.isEmpty() ? init : "= " + init;
    }

    public void setName(@Nullable String name) {
      name = Objects.requireNonNullElse(name, "");

      nameLabel.setText(name);
    }

    public void setInitiative(@Nullable String init) {
      init = initiativeFormatter.apply(Objects.requireNonNullElse(init, ""));

      initLabel.setText(init);
      // Don't allocate spacing for the label if there is no initiative state.
      initLabel.setVisible(!init.isEmpty());
    }

    public void setTokenIsVisible(boolean tokenIsVisible) {
      this.tokenIsVisible = tokenIsVisible;
      updateStyle();
    }

    public void setTokenIsNpc(boolean tokenIsNpc) {
      this.tokenIsNpc = tokenIsNpc;
      updateStyle();
    }

    private void updateStyle() {
      // We still use the UI text so use the map label color preferences
      if (!tokenIsVisible) {
        nameLabel.setForeground(AppPreferences.nonVisibleTokenMapLabelForeground.get());
      } else if (tokenIsNpc) {
        nameLabel.setForeground(AppPreferences.npcMapLabelForeground.get());
      } else {
        nameLabel.setForeground(AppPreferences.pcMapLabelForeground.get());
      }
      nameLabel.setFont(getFont().deriveFont(tokenIsVisible ? Font.PLAIN : Font.ITALIC));
    }
  }

  /** An icon that will show a token image and all of the states as needed. */
  private static final class TokenImagePanel extends JPanel {

    /** The token painted by this icon */
    private Token token;

    private boolean showTokenStates;

    /** Create the image from the token and then build an icon suitable for displaying state. */
    public TokenImagePanel() {
      setOpaque(false);
    }

    /**
     * @param token The token being rendererd.
     * @param showTokenStates Whether to render the token's states as part of the image.
     */
    public void setModel(Token token, boolean showTokenStates) {
      this.token = token;
      this.showTokenStates = showTokenStates;
      invalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
      if (this.token == null) {
        return;
      }

      var bounds = new Rectangle(0, 0, getWidth(), getHeight());

      // Paint the halo if needed
      Color haloColor = token.getHaloColor();
      if (showTokenStates && haloColor != null) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g.getColor();
        g2d.setStroke(new BasicStroke(AppPreferences.haloLineWidth.get()));
        g.setColor(haloColor);
        g2d.draw(bounds);
        g2d.setStroke(oldStroke);
        g.setColor(oldColor);
      }

      {
        // Paint the icon, is that all that's needed?
        BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());
        Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
        AwtUtil.constrainTo(imageSize, bounds.width, bounds.height);
        g.drawImage(
            image,
            (bounds.width - imageSize.width) / 2,
            (bounds.height - imageSize.height) / 2,
            imageSize.width,
            imageSize.height,
            this);
      }

      if (showTokenStates) {
        // Paint all the states
        Shape old = g.getClip();
        g.setClip(bounds.intersection(old.getBounds()));
        for (String state : MapTool.getCampaign().getTokenStatesMap().keySet()) {
          Object stateSet = token.getState(state);
          AbstractTokenOverlay overlay = MapTool.getCampaign().getTokenStatesMap().get(state);
          if (stateSet instanceof AbstractTokenOverlay
              || overlay == null
              || !overlay.showPlayer(token, MapTool.getPlayer())
              || overlay.isMouseover()) {
            continue;
          }
          overlay.paintOverlay((Graphics2D) g, token, bounds, stateSet);
        }
        for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
          Object barSet = token.getState(bar);
          BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
          if (overlay == null || !overlay.showPlayer(token, MapTool.getPlayer())) {
            continue;
          }
          overlay.paintOverlay((Graphics2D) g, token, bounds, barSet);
        }
        g.setClip(old);
      }
    }
  }
}
