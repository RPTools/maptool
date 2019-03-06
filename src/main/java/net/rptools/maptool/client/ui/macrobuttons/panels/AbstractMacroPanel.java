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
package net.rptools.maptool.client.ui.macrobuttons.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.AreaGroup;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.ButtonGroup;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.ButtonGroupPopupMenu;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.Zone.Event;

@SuppressWarnings("serial")
public abstract class AbstractMacroPanel extends JPanel
    implements Scrollable, MouseListener, ModelChangeListener, AppEventListener {
  private String panelClass = "";
  private GUID tokenId = null;

  public void addArea(List<MacroButtonProperties> propertiesList, String label) {
    add(new AreaGroup(propertiesList, label, this));
    setLayout(new FlowLayout(FlowLayout.LEFT));
    revalidate();
    repaint();
  }

  public void addArea(GUID tokenId) {
    add(new AreaGroup(tokenId, this));
    setLayout(new FlowLayout(FlowLayout.LEFT));
    revalidate();
    repaint();
  }

  @Override
  public Insets getInsets() {
    return new Insets(0, 0, 0, 0);
  }

  public int getAvailableWidth() {
    Dimension size = getParent().getSize();
    Insets insets = getInsets();
    return size.width - insets.left - insets.right;
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension size = getParent().getSize();
    FlowLayout layout = (FlowLayout) getLayout();
    Insets insets = getInsets();
    // This isn't exact, but hopefully it's close enough
    int panelWidth = size.width - insets.left - insets.right;
    int panelHeight = size.height - insets.top - insets.bottom;
    int height = insets.top + insets.bottom + layout.getVgap();
    for (Component c : getComponents()) {
      Dimension cSize = c.getPreferredSize();
      height += cSize.height + layout.getVgap();
    }
    height = Math.max(height, panelHeight); // fill the panel if it wouldn't already
    Dimension prefSize = new Dimension(panelWidth, height);
    return prefSize;
  }

  public String getPanelClass() {
    return panelClass;
  }

  public void setPanelClass(String panelClass) {
    this.panelClass = panelClass;
  }

  public Token getToken() {
    if (this.tokenId == null) {
      return null;
    } else {
      return MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(this.tokenId);
    }
  }

  public GUID getTokenId() {
    return this.tokenId;
  }

  public void setTokenId(Token token) {
    if (token == null) {
      this.tokenId = null;
    } else {
      this.tokenId = token.getId();
    }
  }

  public void setTokenId(GUID tokenId) {
    this.tokenId = tokenId;
  }

  protected void clear() {
    removeAll();
    revalidate();
    repaint();
  }

  public abstract void reset();

  // SCROLLABLE
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 75;
  }

  public boolean getScrollableTracksViewportHeight() {
    return getPreferredSize().height < getParent().getSize().height;
  }

  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 25;
  }

  // Override these mouse events in subclasses to specify component specific behavior.
  public void mouseClicked(MouseEvent event) {}

  public void mousePressed(MouseEvent event) {}

  public void mouseReleased(MouseEvent event) {
    if (SwingUtilities.isRightMouseButton(event)) {
      if ("CampaignPanel".equals(getPanelClass()) && !MapTool.getPlayer().isGM()) {
        return;
      }
      // open button group menu
      new ButtonGroupPopupMenu(getPanelClass(), null, "", getToken())
          .show(this, event.getX(), event.getY());
    }
  }

  public void mouseEntered(MouseEvent event) {}

  public void mouseExited(MouseEvent event) {}

  // currently only used for Impersonate/Selection panels to refresh when the token is removed or a
  // macro changes
  public void modelChanged(ModelChangeEvent event) {
    if (event.eventType == Token.ChangeEvent.MACRO_CHANGED
        || event.eventType == Event.TOKEN_REMOVED) {
      reset();
    }
  }

  public void handleAppEvent(AppEvent event) {
    Zone oldZone = (Zone) event.getOldValue();
    Zone newZone = (Zone) event.getNewValue();

    if (oldZone != null) {
      oldZone.removeModelChangeListener(this);
    }
    newZone.addModelChangeListener(this);
    reset();
  }

  public static void clearHotkeys(AbstractMacroPanel panel) {
    for (int areaGroupCount = 0; areaGroupCount < panel.getComponentCount(); areaGroupCount++) {
      AreaGroup area = (AreaGroup) panel.getComponent(areaGroupCount);
      for (ButtonGroup group : area.getButtonGroups()) {
        for (MacroButton nextButton : group.getButtons()) {
          nextButton.clearHotkey();
        }
      }
    }
  }
}
