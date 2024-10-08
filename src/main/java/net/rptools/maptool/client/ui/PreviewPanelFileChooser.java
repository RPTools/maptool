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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import net.rptools.lib.image.ThumbnailManager;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.language.I18N;

/*
 * A File chooser with an image preview panel
 */
public class PreviewPanelFileChooser extends JFileChooser {

  private JPanel previewWrapperPanel;
  private ImagePreviewPanel browsePreviewPanel;
  private ThumbnailManager thumbnailManager =
      new ThumbnailManager(AppUtil.getAppHome("previewPanelThumbs"), new Dimension(150, 150));

  public PreviewPanelFileChooser() {
    this.setCurrentDirectory(AppPreferences.loadDirectory.get());
    this.setAccessory(getPreviewWrapperPanel());
    this.addPropertyChangeListener(
        PreviewPanelFileChooser.SELECTED_FILE_CHANGED_PROPERTY, new FileSystemSelectionHandler());
  }

  private class FileSystemSelectionHandler implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      File previewFile = getImageFileOfSelectedFile();

      if (previewFile != null && !previewFile.isDirectory()) {
        try {
          Image img = thumbnailManager.getThumbnail(previewFile);
          getPreviewPanel().setImage(img);
        } catch (IOException ioe) {
          getPreviewPanel().setImage(null);
        }
      } else {
        getPreviewPanel().setImage(null);
      }
    }
  }

  // Override if selected file is not also the image
  protected File getImageFileOfSelectedFile() {
    return getSelectedFile();
  }

  public Image getSelectedThumbnailImage() {
    return browsePreviewPanel.getImage();
  }

  private JPanel getPreviewWrapperPanel() {
    if (previewWrapperPanel == null) {
      GridLayout gridLayout = new GridLayout();
      gridLayout.setRows(1);
      gridLayout.setColumns(1);
      previewWrapperPanel = new JPanel();
      previewWrapperPanel.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(0, 5, 0, 0),
              BorderFactory.createTitledBorder(
                  null,
                  I18N.getText("Label.preview"),
                  TitledBorder.CENTER,
                  TitledBorder.BELOW_BOTTOM,
                  null,
                  null)));
      previewWrapperPanel.setLayout(gridLayout);
      previewWrapperPanel.add(getPreviewPanel(), null);
    }
    return previewWrapperPanel;
  }

  private ImagePreviewPanel getPreviewPanel() {
    if (browsePreviewPanel == null) {

      browsePreviewPanel = new ImagePreviewPanel();
    }

    return browsePreviewPanel;
  }
}
