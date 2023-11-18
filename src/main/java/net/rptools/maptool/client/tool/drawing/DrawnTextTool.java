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
package net.rptools.maptool.client.tool.drawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Style;
import net.rptools.maptool.client.swing.TwoToneTextPane;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.drawing.DrawnLabel;
import net.rptools.maptool.model.drawing.Pen;

/**
 * A text tool that uses a text component to allow text to be entered on the display and then
 * renders it as an image.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2006-03-11 02:57:18 -0600 (Sat, 11 Mar 2006) $ $Author:
 *     azhrei_fje $
 */
public class DrawnTextTool extends AbstractDrawingTool implements MouseMotionListener {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Flag used to indicate that the anchor has been set. */
  private boolean anchorSet;

  /** The anchor point originally selected */
  private Point anchor = new Point();

  /** The bounds of the display rectangle */
  private Rectangle bounds = new Rectangle();

  /** The text pane used to paint the text. */
  private TwoToneTextPane textPane;

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /** A transparent color used in the background */
  private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /** Initialize the tool icon */
  public DrawnTextTool() {}

  /*---------------------------------------------------------------------------------------------
   * Tool & AbstractDrawingTool Abstract Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.client.tool.drawing.AbstractDrawingTool#paintOverlay(ZoneRenderer,
   *     java.awt.Graphics2D)
   */
  @Override
  public void paintOverlay(ZoneRenderer aRenderer, Graphics2D aG) {
    if (!anchorSet) return;
    aG.setColor(Color.BLACK);
    aG.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  /**
   * @see Tool#getTooltip()
   */
  @Override
  public String getTooltip() {
    return "tool.text.tooltip";
  }

  /**
   * @see Tool#getInstructions()
   */
  @Override
  public String getInstructions() {
    return "tool.text.instructions";
  }

  /**
   * @see Tool#resetTool()
   */
  @Override
  protected void resetTool() {
    anchorSet = false;
    if (textPane != null) renderer.remove(textPane);
    textPane = null;
    renderer.repaint();
  }

  /*---------------------------------------------------------------------------------------------
   * MouseListener Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent event) {
    // Do nothing
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent event) {
    if (!anchorSet) {
      anchor.x = event.getX();
      anchor.y = event.getY();
      anchorSet = true;
    } else {
      setBounds(event);

      // Create a text component and place it on the renderer's component
      textPane = createTextPane(bounds, getPen(), "sanserif-BOLD-20");
      renderer.add(textPane);
      textPane.requestFocusInWindow();

      // Make the enter key addthe text
      KeyStroke k = KeyStroke.getKeyStroke("ENTER");
      textPane.getKeymap().removeKeyStrokeBinding(k);
      textPane
          .getKeymap()
          .addActionForKeyStroke(
              k,
              new AbstractAction() {
                public void actionPerformed(ActionEvent aE) {
                  completeDrawable();
                }
              });
    }
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent aE) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent aE) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent aE) {
    // TODO Auto-generated method stub

  }

  /*---------------------------------------------------------------------------------------------
   * MouseMotionListener Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  public void mouseMoved(MouseEvent event) {
    if (!anchorSet) return;
    if (textPane != null) return;
    setBounds(event);
    renderer.repaint();
  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  public void mouseDragged(MouseEvent aE) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), null);
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), null);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Set the bounds for the text area.
   *
   * @param event The mouse event used in the calculation.
   */
  private void setBounds(MouseEvent event) {
    bounds.x = Math.min(anchor.x, event.getX());
    bounds.y = Math.min(anchor.y, event.getY());
    bounds.width = Math.abs(anchor.x - event.getX());
    bounds.height = Math.abs(anchor.y - event.getY());
  }

  /** Finish drawing the text. */
  private void completeDrawable() {

    // Create a drawable from the data and clean up the component.
    DrawnLabel label =
        new DrawnLabel(
            textPane.getText(), bounds, TwoToneTextPane.getFontString(textPane.getLogicalStyle()));
    textPane.setVisible(false);
    textPane.getParent().remove(textPane);
    textPane = null;

    // Tell everybody else
    completeDrawable(renderer.getZone().getId(), getPen(), label);
    resetTool();
  }

  /**
   * Create a text pane with the passed bounds, pen, and font data
   *
   * @param bounds Bounds of the new text pane
   * @param pen Pen used for foreground and background text colors.
   * @param font Font used to pain the text
   * @return A text pane used to draw text
   */
  public static TwoToneTextPane createTextPane(Rectangle bounds, Pen pen, String font) {
    // Create a text component and place it on the renderer's component
    TwoToneTextPane textPane = new TwoToneTextPane();
    textPane.setBounds(bounds);
    textPane.setOpaque(false);
    textPane.setBackground(TRANSPARENT);

    // Create a style for the component
    Style style = textPane.addStyle("default", null);
    TwoToneTextPane.setFont(style, Font.decode(font));
    // style.addAttribute(StyleConstants.Foreground, new Color(pen.getColor()));
    // style.addAttribute(StyleConstants.Background, new Color(pen.getBackgroundColor()));
    textPane.setLogicalStyle(style);
    return textPane;
  }
}
