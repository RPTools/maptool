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
package net.rptools.maptool.client.ui.assetpanel;

import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssetPanelModel implements PropertyChangeListener {

  private final ImageFileTreeModel imageFileTreeModel;

  private final List<ImageObserver> observerList = new CopyOnWriteArrayList<ImageObserver>();

  public AssetPanelModel() {
    imageFileTreeModel = new ImageFileTreeModel();
  }

  public ImageFileTreeModel getImageFileTreeModel() {
    return imageFileTreeModel;
  }

  public void removeRootGroup(Directory dir) {
    imageFileTreeModel.removeRootGroup(dir);
    dir.removePropertyChangeListener(this);
  }

  public void addRootGroup(Directory dir) {
    if (imageFileTreeModel.containsRootGroup(dir)) {
      return;
    }
    dir.addPropertyChangeListener(this);
    imageFileTreeModel.addRootGroup(dir);
  }

  public void addImageUpdateObserver(ImageObserver observer) {
    if (!observerList.contains(observer)) {
      observerList.add(observer);
    }
  }

  public void removeImageUpdateObserver(ImageObserver observer) {
    observerList.remove(observer);
  }

  // PROPERTY CHANGE LISTENER
  public void propertyChange(PropertyChangeEvent evt) {
    for (ImageObserver observer : observerList) {
      observer.imageUpdate(null, ImageObserver.ALLBITS, 0, 0, 0, 0);
    }
  }
}
