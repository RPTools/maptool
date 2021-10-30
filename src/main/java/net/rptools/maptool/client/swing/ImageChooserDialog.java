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
package net.rptools.maptool.client.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;

public class ImageChooserDialog extends JDialog {

  private MD5Key imageId = null;
  private AssetPanel imageChooser =
      new AssetPanel("imageAssetPanel", MapTool.getFrame().getAssetPanel().getModel());

  public ImageChooserDialog(JFrame owner) {
    super(owner, I18N.getText("Label.image.choose"), true);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            imageId = null;
            setVisible(false);
          }
        });

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.CENTER, imageChooser);
    panel.add(BorderLayout.SOUTH, createButtonPanel());

    setContentPane(panel);
    setSize(400, 500);
    SwingUtil.centerOver(this, getOwner());

    imageChooser.addImageSelectionListener(
        selected -> {
          if (selected.isEmpty() || (Integer) selected.get(0) < 0) {
            return;
          }

          // Sometimes asset is coming back null causing an NPE. Could not reproduce but am
          // putting in a check for it.  On Sentry:  MAPTOOL-11H
          Asset asset = imageChooser.getAsset((Integer) selected.get(0));
          if (asset != null) {
            imageId = asset.getMD5Key();

            // Put the asset into the asset manager since we have the asset handy here
            AssetManager.putAsset(asset);
          } else {
            MapTool.showError("msg.asset.error.invalidAsset");
          }
        });
  }

  /**
   * Returns the asset ID of the last selected image.
   *
   * @return Asset ID
   */
  public MD5Key getImageId() {
    return imageId;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    panel.add(createOKButton());
    panel.add(createCancelButton());

    return panel;
  }

  private JButton createOKButton() {
    JButton button = new JButton(I18N.getText("Button.ok"));
    button.addActionListener(e -> setVisible(false));

    return button;
  }

  private JButton createCancelButton() {
    JButton button = new JButton(I18N.getText("Button.cancel"));
    button.addActionListener(
        e -> {
          imageId = null;
          setVisible(false);
        });

    return button;
  }
}
