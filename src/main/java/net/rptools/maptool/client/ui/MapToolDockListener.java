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

import com.jidesoft.docking.event.DockableFrameEvent;
import com.jidesoft.docking.event.DockableFrameListener;
import net.rptools.maptool.client.MapTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class acts as a listener to the various dockable frames that MapTool uses.
 *
 * <p>Because rendering of the Selection and Impersonate panels is now suppressed when they are not
 * visible (to improve performance) this class resets those panels when they become visible (so that
 * the user sees a seamless transition and does not have to select the token again to get the
 * selection / impersonate panels to populate).
 *
 * @author Rumble
 */
public class MapToolDockListener implements DockableFrameListener {
  private static final Logger log = LogManager.getLogger(MapToolDockListener.class);

  public void dockableFrameActivated(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameAdded(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameAutohidden(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
    updatePanels(dfe.getDockableFrame().getName());
  }

  public void dockableFrameAutohideShowing(DockableFrameEvent dfe) {
    updatePanels(dfe.getDockableFrame().getName());
    showEvent(dfe.toString());
  }

  public void dockableFrameDeactivated(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameDocked(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
    updatePanels(dfe.getDockableFrame().getName());
  }

  public void dockableFrameFloating(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameHidden(DockableFrameEvent dfe) {}

  public void dockableFrameMaximized(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameRemoved(DockableFrameEvent dfe) {}

  public void dockableFrameRestored(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameShown(DockableFrameEvent dfe) {
    updatePanels(dfe.getDockableFrame().getName());
    showEvent(dfe.toString());
  }

  public void dockableFrameTabHidden(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameTabShown(DockableFrameEvent dfe) {
    updatePanels(dfe.getDockableFrame().getName());
    showEvent(dfe.toString());
  }

  public void dockableFrameMoved(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  public void dockableFrameTransferred(DockableFrameEvent dfe) {
    showEvent(dfe.toString());
  }

  /**
   * Updates the Selected or Impersonated panel when it becomes visible to improve performance for
   * moving and selecting tokens.
   *
   * @param panel the panel to be updated
   */
  private void updatePanels(String panel) {
    if (MapTool.getFrame() != null) {
      if (panel == "SELECTION") {
        MapTool.getFrame().getSelectionPanel().reset();
      }
      if (panel == "IMPERSONATED") {
        MapTool.getFrame().getImpersonatePanel().reset();
      }
    }
  }

  /**
   * Logging convenience function to show which events are fired
   *
   * @param dfeId the DockableFrameEvent to record
   */
  private void showEvent(String dfeId) {
    if (log.isTraceEnabled()) log.trace(dfeId);
  }
}
