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

import com.jidesoft.docking.DialogFloatingContainer;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.docking.DockingUtils;
import com.jidesoft.plaf.UIDefaultsLookup;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import javax.swing.JRootPane;

/**
 * Container implementation that only decorates the root pane and not the dialog itself.
 *
 * <p>The default implementation ({@link com.jidesoft.docking.DialogFloatingContainer}) always
 * decorates both the dialog and the root pane when decorations are requested, resulting in
 * redundant title bars for docked floating frames.
 */
public class SingleTitleBarDialogFloatingContainer extends DialogFloatingContainer {
  public SingleTitleBarDialogFloatingContainer(
      DockingManager dockingManager, FloatingContainerManager floatingContainerManager, Frame frame)
      throws HeadlessException {
    super(dockingManager, floatingContainerManager, frame);
  }

  public SingleTitleBarDialogFloatingContainer(
      DockingManager dockingManager,
      FloatingContainerManager floatingContainerManager,
      Dialog dialog)
      throws HeadlessException {
    super(dockingManager, floatingContainerManager, dialog);
  }

  @Override
  public void updateUndecorated() {
    this.setVisible(false); // So we can change the decorations.

    this.setUndecorated(true);

    final var dockingManager = this.getDockingManager();
    final var contentPane = this.getContentPane();
    if (DockingUtils.shouldUseDecoratedFloatingContainer(dockingManager, contentPane)) {
      this.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    } else {
      this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    }
    this.setBorder(UIDefaultsLookup.getBorder("Resizable.resizeBorder"));

    this.updateBorders();

    if (!this.getDockingManager().getMainContainer().isShowing()) {
      return;
    }

    this.setVisible(true);
  }
}
