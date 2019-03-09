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

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;

public class ImageAssetSelectorPanel extends JPanel {

  private ImageAssetPanel imageAssetPanel;
  private JButton imageExplorerButton;

  public ImageAssetSelectorPanel() {
    setLayout(new BorderLayout());

    add(BorderLayout.CENTER, getImageAssetPanel());
    add(BorderLayout.SOUTH, getImageExplorerButton());
  }

  public ImageAssetPanel getImageAssetPanel() {
    if (imageAssetPanel == null) {
      imageAssetPanel = new ImageAssetPanel();
    }
    return imageAssetPanel;
  }

  public JButton getImageExplorerButton() {
    if (imageExplorerButton == null) {
      imageExplorerButton = new JButton("...");
    }

    return imageExplorerButton;
  }

  private JComponent createImageExplorerPanel() {

    final AssetPanel assetPanel =
        new AssetPanel(
            "imageAssetSelectorImageExplorer",
            MapTool.getFrame().getAssetPanel().getModel(),
            JSplitPane.HORIZONTAL_SPLIT);
    assetPanel.addImageSelectionListener(
        new SelectionListener() {
          public void selectionPerformed(List<Object> selectedList) {
            // There should be exactly one
            if (selectedList.size() != 1) {
              return;
            }

            Integer imageIndex = (Integer) selectedList.get(0);

            getImageAssetPanel().setImageId(assetPanel.getAsset(imageIndex).getId());
          }
        });

    return assetPanel;
  }
}
