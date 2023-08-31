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
package net.rptools.maptool.client.ui.docking;

import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DialogFloatingContainer;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.RootPaneContainer;

/**
 * Custom docking manager that creates custom floating containers.
 *
 * <p>This is how to use {@link SingleTitleBarDialogFloatingContainer} instead of the default
 * implementation.
 */
public class MapToolDockingManager extends DefaultDockingManager {
  public MapToolDockingManager(RootPaneContainer rootPaneContainer, Container container) {
    super(rootPaneContainer, container);
  }

  @Override
  protected DialogFloatingContainer createDialogFloatingContainer(
      InternalEventManager internalEventManager, Window window) {
    if (window instanceof Dialog dialog) {
      return new SingleTitleBarDialogFloatingContainer(this, internalEventManager, dialog);
    }

    if (window instanceof Frame frame) {
      return new SingleTitleBarDialogFloatingContainer(this, internalEventManager, frame);
    }

    throw new UnsupportedOperationException(
        "JIDE Docking Framework doesn't support floating frames if rootPaneContainer is "
            + (window == null ? "null" : window.getClass().getName()));
  }
}
