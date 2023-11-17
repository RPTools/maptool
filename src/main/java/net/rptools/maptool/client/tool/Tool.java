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
package net.rptools.maptool.client.tool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Grid;

public abstract class Tool extends JToggleButton implements ActionListener, KeyListener {

  private static final long serialVersionUID = -6365594431759083634L;
  private final Set<Character> keyDownSet = new HashSet<Character>();
  protected Map<KeyStroke, Action> keyActionMap = new HashMap<KeyStroke, Action>();
  private Toolbox toolbox;

  public Tool() {
    // Map the escape key to reset this tool.
    installKeystrokes(keyActionMap);

    addActionListener(this);

    setToolTipText(I18N.getText(getTooltip()));
    setFocusable(false);
    setFocusPainted(false);
  }

  protected boolean isKeyDown(char key) {
    return keyDownSet.contains(key);
  }

  public boolean isAvailable() {
    return true;
  }

  void setToolbox(Toolbox toolbox) {
    this.toolbox = toolbox;
  }

  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new EscapeAction());
  }

  public abstract String getTooltip();

  public abstract String getInstructions();

  protected void addListeners(JComponent comp) {
    if (comp == null) {
      return;
    }
    if (this instanceof MouseListener) {
      comp.addMouseListener((MouseListener) this);
    }
    if (this instanceof MouseMotionListener) {
      comp.addMouseMotionListener((MouseMotionListener) this);
    }
    if (this instanceof MouseWheelListener) {
      comp.addMouseWheelListener((MouseWheelListener) this);
    }
    // Keystrokes
    comp.addKeyListener(this);
    comp.setActionMap(createActionMap(keyActionMap));
    comp.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, createInputMap(keyActionMap));
  }

  void removeListeners(JComponent comp) {
    if (comp == null) {
      return;
    }
    comp.removeKeyListener(this);

    if (this instanceof MouseListener) {
      comp.removeMouseListener((MouseListener) this);
    }
    if (this instanceof MouseMotionListener) {
      comp.removeMouseMotionListener((MouseMotionListener) this);
    }
    if (this instanceof MouseWheelListener) {
      comp.removeMouseWheelListener((MouseWheelListener) this);
    }
  }

  protected void attachTo(ZoneRenderer renderer) {
    // No op
  }

  protected void detachFrom(ZoneRenderer renderer) {
    // No op
  }

  protected void addGridBasedKeys(Grid grid, boolean enable) {
    // do nothing; only overridden by PointerTool currently
  }

  private InputMap createInputMap(Map<KeyStroke, Action> keyActionMap) {
    ComponentInputMap inputMap =
        new ComponentInputMap((JPanel) MapTool.getFrame().getContentPane());
    for (KeyStroke keyStroke : keyActionMap.keySet()) {
      inputMap.put(keyStroke, keyStroke.toString());
    }
    return inputMap;
  }

  private ActionMap createActionMap(Map<KeyStroke, Action> keyActionMap) {
    ActionMap actionMap = new ActionMap();
    for (var keyAction : keyActionMap.entrySet()) {
      actionMap.put(keyAction.getKey().toString(), keyAction.getValue());
      // System.out.println(keyAction.getKey().toString() + ": " + keyAction.getValue().toString());
    }
    return actionMap;
  }

  /**
   * Implement this method to clear internal data to a start drawing state. This method must repaint
   * whatever it is being displayed upon.
   */
  protected abstract void resetTool();

  /*
   * Defines if the Tool belongs to a button group or not
   */
  protected boolean hasGroup() {
    return true;
  }

  ////
  // ACTION LISTENER
  public void actionPerformed(ActionEvent e) {
    if (isSelected()) {
      toolbox.setSelectedTool(Tool.this);
    }
  }

  ////
  // KEY LISTENER
  public void keyPressed(KeyEvent e) {
    keyDownSet.add(e.getKeyChar());
  }

  public void keyReleased(KeyEvent e) {
    keyDownSet.remove(e.getKeyChar());
  }

  public void keyTyped(KeyEvent e) {}

  public void updateButtonState() {}

  /**
   * Perform the escape action on a tool.
   *
   * @author jgorrell
   */
  private class EscapeAction extends AbstractAction {

    private static final long serialVersionUID = -514197544905143826L;

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // This should probably go somewhere else like MapToolFrame...
      // but it was just too easy to plop down here to resist =)
      // although having it here will be a quick reminder about competing
      // Escape actions.
      if (MapTool.getFrame().isFullScreen()) {
        MapTool.getFrame().showWindowed();
      }
      resetTool();
    }
  }
}
