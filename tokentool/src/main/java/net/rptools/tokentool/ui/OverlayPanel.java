/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.ImagePanel;
import net.rptools.lib.swing.ImagePanelModel;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.lib.transferable.ImageTransferableHandler;
import net.rptools.tokentool.TokenTool;

public class OverlayPanel extends ImagePanel implements SelectionListener, DropTargetListener, PropertyChangeListener {

	public OverlayPanel() {
		setModel(new OverlayPanelModel());
		setSelectionMode(SelectionMode.SINGLE);

		addSelectionListener(this);

		// DnD
		new DropTarget(this, this);

		// Delete
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		getActionMap().put("delete", DELETE_ACTION);
	}

	@Override
	public void setModel(ImagePanelModel model) {

		if (getModel() != null) {
			getModel().removeChangeListener(this);
		}
		if (model != null) {
			((OverlayPanelModel) model).addChangeListener(this);
		}

		super.setModel(model);
	}

	@Override
	public OverlayPanelModel getModel() {
		return (OverlayPanelModel) super.getModel();
	}

	////
	// SELECTION LISTENER
	public void selectionPerformed(List<Object> selectedList) {

		// There should be exactly one
		if (selectedList.size() != 1) {
			return;
		}

		Integer imageIndex = (Integer) selectedList.get(0);
	}

	////
	// DROP LISTENER
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {

		Transferable transferable = dtde.getTransferable();
		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

		try {
			Image image = new ImageTransferableHandler().getTransferObject(transferable);
			if (!(image instanceof BufferedImage)) {
				// Convert to buffered image
				image = ImageUtil.createCompatibleImage(image);
			}

			TokenTool.addOverlayImage((BufferedImage) image);

			getModel().refresh();
			repaint();
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	////
	// ACTIONS
	public final Action DELETE_ACTION = new AbstractAction() {

		{
			putValue(Action.NAME, "Delete");
		}

		public void actionPerformed(java.awt.event.ActionEvent e) {

			if (!TokenTool.confirm("Are you sure you want to delete the selected overlay?")) {
				return;
			}

			new Thread() {
				@Override
				public void run() {
					List<Object> selectedIds = getSelectedIds();

					OverlayPanelModel model = getModel();
					for (Object id : selectedIds) {

						File file = model.getFile((Integer) id);

						try {
							file.delete();
						} catch (SecurityException se) {
							TokenTool.showError("Unable to delete overlay: " + se);
						}
					}

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							setModel(new OverlayPanelModel());
						}
					});

				}
			}.start();
		};
	};

	////
	// PROPERTY CHANGE LISTENER
	public void propertyChange(PropertyChangeEvent evt) {
		revalidate();
		repaint();
	}
}
