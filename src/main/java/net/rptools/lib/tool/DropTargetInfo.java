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
package net.rptools.lib.tool;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import net.rptools.lib.transferable.ImageTransferableHandler;

/**
 * This class will show a frame that accepts system drag-and-drop events it is a discovery tool
 * useful to determine which flavors a drop from a specific application supports (such as a browser)
 */
@SuppressWarnings("serial")
public class DropTargetInfo extends JFrame implements DropTargetListener {
  JLabel label = new JLabel("Drop here");

  public DropTargetInfo() {
    super("Drag and drop into this window");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocation(300, 200);
    setSize(200, 200);

    new DropTarget(this, this);

    add(label);
  }

  public static void main(String[] args) {
    DropTargetInfo dti = new DropTargetInfo();
    dti.setVisible(true);
  }

  ////
  // DROP TARGET LISTENER
  public void dragEnter(DropTargetDragEvent dtde) {}

  public void dragExit(DropTargetEvent dte) {}

  public void dragOver(DropTargetDragEvent dtde) {}

  public void drop(DropTargetDropEvent dtde) {
    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    @SuppressWarnings("unused")
    Object handlerObj = null;
    try {
      handlerObj = new ImageTransferableHandler().getTransferObject(dtde.getTransferable());

      System.out.println("DropAction:" + dtde.getDropAction());
      System.out.println("Source:" + dtde.getSource());
      System.out.println("DropTargetContext:" + dtde.getDropTargetContext());
      System.out.println("Data Flavors:");
      for (DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
        try {
          System.out.println("\t" + flavor.getMimeType());
        } catch (Exception e) {
          System.out.println("\t\tfailed");
        }
      }
      System.out.println("--------------------");
      label.setIcon(
          new ImageIcon(new ImageTransferableHandler().getTransferObject(dtde.getTransferable())));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {}
}
