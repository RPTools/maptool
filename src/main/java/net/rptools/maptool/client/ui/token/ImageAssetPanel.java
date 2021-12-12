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
package net.rptools.maptool.client.ui.token;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.TransferableHelper;
import net.rptools.maptool.client.swing.ImageChooserDialog;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageAssetPanel extends JPanel implements DropTargetListener {
  private static final Logger log = LogManager.getLogger(ImageAssetPanel.class);

  private MD5Key imageId;

  private JButton cancelButton;
  private JButton addButton;

  private ImageObserver[] observers;

  private boolean allowEmpty = true;
  private float opacity = 1.0f;

  public ImageAssetPanel() {
    new DropTarget(this, this);
    init();
  }

  private void init() {
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, createNorthPanel());
    setImageId(null);
  }

  private JPanel createNorthPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panel.setOpaque(false);

    panel.add(getAddButton());
    panel.add(getCancelButton());

    return panel;
  }

  public JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton(new ImageIcon(AppStyle.cancelButton));
      cancelButton.setContentAreaFilled(false);
      cancelButton.setBorderPainted(false);
      cancelButton.setFocusable(false);
      cancelButton.setMargin(new Insets(0, 0, 0, 0));

      cancelButton.addActionListener(e -> setImageId(null));
    }
    return cancelButton;
  }

  public JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(new ImageIcon(AppStyle.addButton));
      addButton.setContentAreaFilled(false);
      addButton.setBorderPainted(false);
      addButton.setFocusable(false);
      addButton.setMargin(new Insets(0, 0, 0, 0));

      addButton.addActionListener(
          e -> {
            ImageChooserDialog chooserDialog = MapTool.getFrame().getImageChooserDialog();
            chooserDialog.setVisible(true);

            MD5Key imageId = chooserDialog.getImageId();
            if (imageId == null) {
              return;
            }
            setImageId(imageId);
          });
    }
    return addButton;
  }

  public MD5Key getImageId() {
    return imageId;
  }

  public void setAllowEmptyImage(boolean allow) {
    allowEmpty = allow;
  }

  public void setImageId(MD5Key sheetAssetId, ImageObserver... observers) {
    this.imageId = sheetAssetId;
    this.observers =
        observers != null && observers.length > 0 ? observers : new ImageObserver[] {this};

    getCancelButton().setVisible(allowEmpty && sheetAssetId != null);

    revalidate();
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Dimension size = getSize();
    ((Graphics2D) g)
        .setPaint(
            new TexturePaint(
                AppStyle.panelTexture,
                new Rectangle(
                    0, 0, AppStyle.panelTexture.getWidth(), AppStyle.panelTexture.getHeight())));
    g.fillRect(0, 0, size.width, size.height);

    if (imageId == null) {
      return;
    }
    BufferedImage image = ImageManager.getImage(imageId, observers);

    Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
    SwingUtil.constrainTo(imgSize, size.width - 8, size.height - 8);

    // support opacity of the image
    // setting JPanel background via alpha level to have opacity
    // will not work for the image, therefore this is used explicitly
    Composite originalComposite = null;
    if (opacity != 1.0f) {
      AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
      originalComposite = ((Graphics2D) g).getComposite();
      ((Graphics2D) g).setComposite(alphaComposite);
    }

    g.drawImage(
        image,
        (size.width - imgSize.width) / 2,
        (size.height - imgSize.height) / 2,
        imgSize.width,
        imgSize.height,
        this);

    // restore original composite to make sure button etc are not opaque
    if (originalComposite != null) {
      ((Graphics2D) g).setComposite(originalComposite);
    }
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }

  ////
  // DROP TARGET LISTENER
  public void dragEnter(DropTargetDragEvent dtde) {}

  public void dragExit(DropTargetEvent dte) {}

  public void dragOver(DropTargetDragEvent dtde) {}

  public void drop(DropTargetDropEvent dtde) {
    Transferable t = dtde.getTransferable();
    if (!(TransferableHelper.isSupportedAssetFlavor(t)
            || TransferableHelper.isSupportedTokenFlavor(t))
        || (dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
      dtde.rejectDrop(); // Not a supported flavor or not a copy/move
      log.warn("Couldn't figure out the drop type");
      return;
    }
    dtde.acceptDrop(dtde.getDropAction());

    List<Object> assets = TransferableHelper.getAsset(dtde.getTransferable());
    if (assets == null || assets.isEmpty() || !(assets.get(0) instanceof Asset)) {
      return;
    }
    setImageId(((Asset) assets.get(0)).getMD5Key());
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {}
}
