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
package net.rptools.maptool.util;

import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import net.rptools.lib.FileUtil;
import net.rptools.lib.io.PackedFile;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Asset;
import org.apache.commons.io.IOUtils;

/**
 * Appears to be unused within MapTool. What was its original purpose? It appears to be some way to
 * extract individual images from a campaign, but it has multiple problems: always names output
 * images with <b>.jpg</b> extensions, doesn't allow a choice of which images are extracted, doesn't
 * turn on annotation processing for {@link Asset} objects (needed for XStream processing), and
 * doesn't used buffered I/O classes.
 *
 * @author ??
 */
public class AssetExtractor {
  public static void extract() throws Exception {
    new Thread() {
      @Override
      public void run() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
          return;
        }
        File file = chooser.getSelectedFile();
        File newDir =
            new File(
                file.getParentFile(),
                file.getName().substring(0, file.getName().lastIndexOf('.')) + "_images");

        JLabel label = new JLabel("", JLabel.CENTER);
        JFrame frame = new JFrame();
        frame.setTitle("Campaign Image Extractor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 75);
        frame.add(label);
        SwingUtil.centerOnScreen(frame);
        frame.setVisible(true);
        Reader r = null;
        OutputStream out = null;
        PackedFile pakfile = null;
        try {
          newDir.mkdirs();

          label.setText("Loading campaign ...");
          pakfile = new PackedFile(file);

          Set<String> files = pakfile.getPaths();
          XStream xstream = FileUtil.getConfiguredXStream();
          int count = 0;
          for (String filename : files) {
            count++;
            if (filename.indexOf("assets") < 0) {
              continue;
            }
            r = pakfile.getFileAsReader(filename);
            Asset asset = (Asset) xstream.fromXML(r);
            IOUtils.closeQuietly(r);

            File newFile = new File(newDir, asset.getName() + ".jpg");
            label.setText("Extracting image " + count + " of " + files.size() + ": " + newFile);
            if (newFile.exists()) {
              newFile.delete();
            }
            newFile.createNewFile();
            out = new FileOutputStream(newFile);
            FileUtil.copyWithClose(new ByteArrayInputStream(asset.getImage()), out);
          }
          label.setText("Done.");
        } catch (Exception ioe) {
          MapTool.showInformation("AssetExtractor failure", ioe);
        } finally {
          if (pakfile != null) pakfile.close();
          IOUtils.closeQuietly(r);
          IOUtils.closeQuietly(out);
        }
      }
    }.start();
  }
}
