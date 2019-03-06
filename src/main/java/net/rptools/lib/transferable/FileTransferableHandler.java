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
package net.rptools.lib.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileTransferableHandler extends TransferableHandler {
  private static final DataFlavor fileList = DataFlavor.javaFileListFlavor;

  @Override
  public List<URL> getTransferObject(Transferable transferable)
      throws IOException, UnsupportedFlavorException {
    if (transferable.isDataFlavorSupported(fileList)) {
      @SuppressWarnings("unchecked")
      List<File> files = (List<File>) transferable.getTransferData(fileList);
      List<URL> urls = new ArrayList<URL>(files.size());
      for (File file : files) urls.add(file.toURI().toURL());
      return urls;
    }
    throw new UnsupportedFlavorException(null);
  }
}
