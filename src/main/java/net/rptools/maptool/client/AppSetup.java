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
package net.rptools.maptool.client;

import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.model.AssetManager;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

/** Executes only the first time the application is run. */
public class AppSetup {
  private static final Logger log = LogManager.getLogger(AppSetup.class);

  public static void install() {
    File appDir = AppUtil.getAppHome();

    // Only init once
    if (appDir.listFiles().length > 0) {
      return;
    }
    try {
      installDefaultTokens();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static void installDefaultTokens() throws IOException {
    installLibrary("Default", AppSetup.class.getClassLoader().getResource("default_images.zip"));
  }

  /**
   * Overwrites any existing README file in the ~/.maptool/resource directory with the one from the
   * current MapTool JAR file. This way any updates to the README will eventually be seen by the
   * user, although only when a new directory is added to the resource library...
   *
   * @throws IOException
   */
  private static void createREADME() throws IOException {
    File outFilename = new File(AppConstants.UNZIP_DIR, "README");
    InputStream inStream = null;
    OutputStream outStream = null;
    try {
      inStream = AppSetup.class.getResourceAsStream("README");
      outStream = new BufferedOutputStream(new FileOutputStream(outFilename));
      IOUtils.copy(inStream, outStream);
    } finally {
      IOUtils.closeQuietly(inStream);
      IOUtils.closeQuietly(outStream);
    }
  }

  public static void installLibrary(String libraryName, URL resourceFile) throws IOException {
    createREADME();
    File unzipDir = new File(AppConstants.UNZIP_DIR, libraryName);
    FileUtil.unzip(resourceFile, unzipDir);
    installLibrary(libraryName, unzipDir);
  }

  public static void installLibrary(final String libraryName, final File root) throws IOException {
    // Add as a resource root
    AppPreferences.addAssetRoot(root);
    if (MapTool.getFrame() != null) {
      MapTool.getFrame().addAssetRoot(root);

      // License
      File licenseFile = new File(root, "License.txt");
      if (!licenseFile.exists()) {
        licenseFile = new File(root, "license.txt");
      }
      if (licenseFile.exists()) {
        final File licenseFileFinal = licenseFile;
        EventQueue.invokeLater(
            new Runnable() {
              public void run() {
                try {
                  JTextPane pane = new JTextPane();
                  pane.setPage(licenseFileFinal.toURI().toURL());
                  JOptionPane.showMessageDialog(
                      MapTool.getFrame(),
                      pane,
                      "License for " + libraryName,
                      JOptionPane.INFORMATION_MESSAGE);
                } catch (MalformedURLException e) {
                  log.error("Could not load license file: " + licenseFileFinal, e);
                } catch (IOException e) {
                  log.error("Could not load license file: " + licenseFileFinal, e);
                }
              }
            });
      }
    }
    new SwingWorker<Object, Object>() {
      @Override
      protected Object doInBackground() throws Exception {
        AssetManager.searchForImageReferences(root, AppConstants.IMAGE_FILE_FILTER);
        return null;
      }
    }.execute();
  }
}
