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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.ImageLabel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Borders;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.GraphicsUtil;
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

  /** The label used to display the current item indicator. */
  private final JLabel currentIndicator;

  /** The label used to display the item's name and icon. */
  private final JLabel name;

  /** This is the panel showing initiative. It contains the state for display. */
  private final InitiativePanel panel;

  /** Used to draw the background of the item. */
  private ImageLabel backgroundImageLabel;

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

  /** Border used for name plate */
  public static final Border NAME_BORDER = BorderFactory.createEmptyBorder(2, 4, 3, 4);

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
    panel = aPanel;
    setLayout(new MigLayout("", "[][grow]"));
    //    setBorder(SELECTED_BORDER);
    //    setBackground(Color.WHITE);

    // The current indicator
    currentIndicator = new JLabel();
    currentIndicator.setPreferredSize(INDICATOR_SIZE);
    currentIndicator.setHorizontalAlignment(SwingConstants.CENTER);
    currentIndicator.setVerticalAlignment(SwingConstants.CENTER);
    add(currentIndicator);

    // And the name
    name = new NameLabel();
    name.setText("Ty");
    name.setBorder(NAME_BORDER);
    name.setFont(getFont().deriveFont(Font.BOLD));
    textHeight = getFontMetrics(getFont()).getHeight();
    add(name);
    validate();
  }

  /*---------------------------------------------------------------------------------------------
   * ListCellRenderer Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
   *     java.lang.Object, int, boolean, boolean)
   */
  @Override
  public Component getListCellRendererComponent(
      JList list, TokenInitiative ti, int index, boolean isSelected, boolean cellHasFocus) {

    setOpaque(false);

    // Set the background by type
    Token token = null;
    if (ti != null) token = ti.getToken();
    if (token == null) { // Can happen when deleting a token before all events have propagated
      currentIndicator.setIcon(null);
      name.setText(null);
      name.setIcon(null);
      setBorder(UNSELECTED_BORDER);
      return this;
    } // endif
    backgroundImageLabel =
        token.isVisible()
            ? token.getType() == Token.Type.NPC ? GraphicsUtil.BLUE_LABEL : GraphicsUtil.GREY_LABEL
            : GraphicsUtil.DARK_GREY_LABEL;
    name.setForeground(Color.BLACK);
    name.setFont(name.getFont().deriveFont(token.isVisible() ? Font.PLAIN : Font.ITALIC));

    // Show the indicator?
    int currentIndex = panel.getList().getCurrent();
    if (currentIndex >= 0 && ti == panel.getList().getTokenInitiative(currentIndex)) {
      currentIndicator.setIcon(CURRENT_INDICATOR_ICON);
    } else {
      currentIndicator.setIcon(null);
    } // endif

    // Get the name string, add the state if displayed, then get the icon if needed
    boolean initStateSecondLine = panel.isInitStateSecondLine() && panel.isShowInitState();
    String sName = (initStateSecondLine ? "<html>" : "") + ti.getToken().getName();
    if (MapTool.getFrame().getInitiativePanel().hasGMPermission()
        && token.getGMName() != null
        && token.getGMName().trim().length() != 0) sName += " (" + token.getGMName().trim() + ")";
    if (panel.isShowInitState() && ti.getState() != null)
      sName += (initStateSecondLine ? "<br>" : " = ") + ti.getState();
    if (initStateSecondLine) sName += "</html>";
    Icon icon = null;
    if (panel.isShowTokens()) {
      icon = ti.getDisplayIcon();
      if (icon == null || ti.wasTokenVisibleWhenIconUpdated() != token.isVisible()) {
        icon = new InitiativeListIcon(ti);
        ti.setDisplayIcon(icon);
        ti.setTokenVisibleWhenIconUpdated(token.isVisible());
      } // endif
    } // endif
    name.setText(sName);
    name.setIcon(icon);

    // Align it properly
    var alignment = ti.isHolding() ? SwingConstants.LEFT : SwingConstants.RIGHT;
    name.setHorizontalTextPosition(alignment);
    MigLayout layout = (MigLayout) getLayout();
    //
    if (alignment == SwingConstants.RIGHT) {
      layout.setComponentConstraints(name, "align left");
    } else {
      layout.setComponentConstraints(name, "align right");
    } // endif

    // Selected?
    if (isSelected) {
      setBorder(SELECTED_BORDER);
    } else {
      setBorder(UNSELECTED_BORDER);
    } // endif
    return this;
  }

  /*---------------------------------------------------------------------------------------------
   * NameLabel Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * This label contains the sized background image for the name component.
   *
   * @author Jay
   */
  public class NameLabel extends JLabel {

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
      boolean initStateSecondLine = panel.isInitStateSecondLine() && panel.isShowInitState();
      Dimension s = name.getSize();
      int th = (textHeight + 2) * (initStateSecondLine ? 2 : 1);
      backgroundImageLabel.renderLabel((Graphics2D) g, 0, (s.height - th) / 2, s.width, th);
      super.paintComponent(g);
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
      boolean initStateSecondLine = panel.isInitStateSecondLine() && panel.isShowInitState();
      Dimension s = super.getPreferredSize();
      int th = textHeight * (initStateSecondLine ? 2 : 1);
      Insets insets = getInsets();
      if (getIcon() != null) th = Math.max(th, getIcon().getIconHeight());
      s.height = th + insets.top + insets.bottom - 4;
      return s;
    }
  }

  /*---------------------------------------------------------------------------------------------
   * InitiativeListIcon Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * An icon that will show a token image and all of the states as needed.
   *
   * @author Jay
   */
  public class InitiativeListIcon extends ImageIcon {

    /** Bounds sent to the token state */
    private final Rectangle bounds = new Rectangle(0, 0, ICON_SIZE, ICON_SIZE);

    /** The token painted by this icon */
    private final TokenInitiative tokenInitiative;

    /** The image that is displayed when states are not being shown. */
    private Image textTokenImage;

    /** The image that is displayed when states are being shown. */
    private Image stateTokenImage;

    /** Size of the text only token */
    private final int textTokenSize = Math.max(textHeight + 4, 16);

    /**
     * Create the image from the token and then build an icon suitable for displaying state.
     *
     * @param aTokenInitiative The initiative item being rendered
     */
    public InitiativeListIcon(TokenInitiative aTokenInitiative) {
      tokenInitiative = aTokenInitiative;
      if (panel.isShowTokenStates()) {
        stateTokenImage = scaleImage();
      } else {
        textTokenImage = scaleImage();
      } // endif
    }

    /**
     * Scale the token's image to fit in the allotted space for text or state painting.
     *
     * @return The properly scaled image.
     */
    public Image scaleImage() {
      Image image = ImageManager.getImageAndWait(tokenInitiative.getToken().getImageAssetId());
      BufferedImage bi =
          ImageUtil.createCompatibleImage(
              getIconWidth(), getIconHeight(), Transparency.TRANSLUCENT);
      Dimension d = new Dimension(image.getWidth(null), image.getHeight(null));
      SwingUtil.constrainTo(d, getIconWidth(), getIconHeight());
      Graphics2D g = bi.createGraphics();
      g.setComposite(
          AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER, tokenInitiative.getToken().isVisible() ? 1.0F : 0.5F));
      g.drawImage(
          image,
          (getIconWidth() - d.width) / 2,
          (getIconHeight() - d.height) / 2,
          d.width,
          d.height,
          null);
      setImage(bi);
      return bi;
    }

    /**
     * @see javax.swing.ImageIcon#getIconHeight()
     */
    @Override
    public int getIconHeight() {
      return panel.isShowTokenStates() ? ICON_SIZE : textTokenSize;
    }

    /**
     * @see javax.swing.ImageIcon#getIconWidth()
     */
    @Override
    public int getIconWidth() {
      return panel.isShowTokenStates() ? ICON_SIZE : textTokenSize;
    }

    /**
     * Paint the icon and then the image.
     *
     * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

      // Paint the halo if needed
      Token token = tokenInitiative.getToken();
      if (panel.isShowTokenStates() && token.hasHalo()) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g.getColor();
        g2d.setStroke(new BasicStroke(AppPreferences.haloLineWidth.get()));
        g.setColor(token.getHaloColor());
        g2d.draw(new Rectangle2D.Double(x, y, ICON_SIZE, ICON_SIZE));
        g2d.setStroke(oldStroke);
        g.setColor(oldColor);
      } // endif

      // Paint the icon, is that all that's needed?
      if (panel.isShowTokenStates() && getImage() != stateTokenImage) {
        if (stateTokenImage == null) stateTokenImage = scaleImage();
        setImage(stateTokenImage);
      } else if (!panel.isShowTokenStates() && getImage() != textTokenImage) {
        if (textTokenImage == null) textTokenImage = scaleImage();
        setImage(textTokenImage);
      } // endif
      super.paintIcon(c, g, x, y);
      if (!panel.isShowTokenStates()) return;

      // Paint all the states
      g.translate(x, y);
      Shape old = g.getClip();
      g.setClip(bounds.intersection(old.getBounds()));
      for (String state : MapTool.getCampaign().getTokenStatesMap().keySet()) {
        Object stateSet = token.getState(state);
        AbstractTokenOverlay overlay = MapTool.getCampaign().getTokenStatesMap().get(state);
        if (stateSet instanceof AbstractTokenOverlay
            || overlay == null
            || !overlay.showPlayer(token, MapTool.getPlayer())
            || overlay.isMouseover()) continue;
        overlay.paintOverlay((Graphics2D) g, token, bounds, stateSet);
      } // endfor
      for (String bar : MapTool.getCampaign().getTokenBarsMap().keySet()) {
        Object barSet = token.getState(bar);
        BarTokenOverlay overlay = MapTool.getCampaign().getTokenBarsMap().get(bar);
        if (overlay == null || !overlay.showPlayer(token, MapTool.getPlayer())) continue;
        overlay.paintOverlay((Graphics2D) g, token, bounds, barSet);
      } // endfor
      g.setClip(old);
      g.translate(-x, -y);
    }
  }
}
