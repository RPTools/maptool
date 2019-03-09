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

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import net.rptools.lib.swing.AbstractPaintChooserPanel;
import net.rptools.lib.swing.ImagePanel;
import net.rptools.lib.swing.PaintChooser;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
import net.rptools.maptool.client.ui.assetpanel.AssetPanelModel;
import net.rptools.maptool.model.Asset;

public class TextureChooserPanel extends AbstractPaintChooserPanel {

  private PaintChooser paintChooser;
  private ImagePanel imagePanel;

  public TextureChooserPanel(PaintChooser paintChooser, AssetPanelModel model) {
    this(paintChooser, model, "textureChooser");
  }

  public TextureChooserPanel(PaintChooser paintChooser, AssetPanelModel model, String controlName) {
    setLayout(new GridLayout());

    this.paintChooser = paintChooser;

    add(createImageExplorerPanel(model, controlName));
  }

  private JComponent createImageExplorerPanel(AssetPanelModel model, String controlName) {

    final AssetPanel assetPanel = new AssetPanel(controlName, model, JSplitPane.HORIZONTAL_SPLIT);
    assetPanel.addImageSelectionListener(
        new SelectionListener() {
          public void selectionPerformed(List<Object> selectedList) {
            // There should be exactly one
            if (selectedList.size() != 1) {
              return;
            }

            Integer imageIndex = (Integer) selectedList.get(0);
            Asset asset = assetPanel.getAsset(imageIndex);
            if (asset == null) {
              return;
            }

            paintChooser.setPaint(new AssetPaint(asset));
          }
        });
    assetPanel.setThumbSize(100);

    return assetPanel;
  }

  @Override
  public String getDisplayName() {
    return "Texture";
  }
}
