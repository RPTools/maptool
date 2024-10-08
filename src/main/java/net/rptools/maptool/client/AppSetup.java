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

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.*;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.model.AssetManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

/** Executes only the first time the application is run. */
public class AppSetup {

  private static boolean themesInstalled = false;

  private static final Logger log = LogManager.getLogger(AppSetup.class);

  public static void install() {
    try {
      // Only init once
      if (!new File(AppConstants.UNZIP_DIR, "README").exists()) {
        installDefaultTokens();
      }
    } catch (IOException ioe) {
      log.error(ioe);
    }

    installDefaultMacroEditorThemes();
    installPredefinedProperties();
  }

  public static void installDefaultTokens() throws IOException {
    installLibrary("Default", AppSetup.class.getClassLoader().getResource("default_images.zip"));
  }

  public static void installDefaultMacroEditorThemes() {
    installUsingReflection(AppConstants.DEFAULT_MACRO_THEMES, AppConstants.THEMES_DIR, "theme");
  }

  private static void installPredefinedProperties() {
    installUsingReflection(
        AppConstants.DEFAULT_CAMPAIGN_PROPERTIES,
        AppConstants.CAMPAIGN_PROPERTIES_DIR,
        "campaign property");
  }

  private static void installUsingReflection(String source, File dir, String name) {
    if (isNotEmpty(dir)) {
      return;
    }
    Set<String> resourcePathSet =
        new Reflections(
                new ConfigurationBuilder().forPackage(source).setScanners(Scanners.Resources))
            .getResources(Pattern.compile(".*")).stream()
                .filter(s -> s.startsWith(source))
                .collect(Collectors.toSet());

    for (String resourcePath : resourcePathSet) {
      URL inputUrl = AppSetup.class.getClassLoader().getResource(resourcePath);
      String resourceName = resourcePath.substring(source.length());
      File resourceFile = new File(dir, resourceName);

      try {
        log.info("Installing " + name + " in:" + resourceFile);
        FileUtils.copyURLToFile(inputUrl, resourceFile);
      } catch (IOException e) {
        log.error("ERROR copying " + inputUrl + " to " + resourceFile, e);
      }
    }
  }

  private static boolean isNotEmpty(File dir) {
    File[] files = dir.listFiles();
    return files != null && files.length > 0;
  }

  /**
   * Copies the theme files from the resource theme directory in the jar file to the directory
   * searched on MapTool start for theme files.
   */
  public static void installDefaultUIThemes() {
    if (!themesInstalled) {
      Set<String> resourcePathSet =
          new Reflections(
                  new ConfigurationBuilder()
                      .forPackage(AppConstants.DEFAULT_UI_THEMES)
                      .setScanners(Scanners.Resources))
              .getResources(Pattern.compile(".*")).stream()
                  .filter(s -> s.startsWith(AppConstants.DEFAULT_UI_THEMES))
                  .collect(Collectors.toSet())
                  .stream()
                  .filter(s -> s.endsWith(".theme"))
                  .filter(s -> s.startsWith(AppConstants.DEFAULT_UI_THEMES))
                  .collect(Collectors.toSet());

      for (String resourcePath : resourcePathSet) {
        URL inputUrl = AppSetup.class.getClassLoader().getResource(resourcePath);
        String resourceName = resourcePath.substring(AppConstants.DEFAULT_UI_THEMES.length());
        File resourceFile = new File(AppConstants.UI_THEMES_DIR, resourceName);

        try {
          log.info("Installing ui theme: " + resourceFile);
          FileUtils.copyURLToFile(inputUrl, resourceFile);
        } catch (IOException e) {
          log.error("ERROR copying " + inputUrl + " to " + resourceFile, e);
        }
      }
      themesInstalled = true;
    }
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
    try (InputStream inStream = AppSetup.class.getResourceAsStream("README");
        OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFilename))) {
      IOUtils.copy(inStream, outStream);
    }
  }

  public static void installLibrary(String libraryName, URL resourceFile) throws IOException {
    createREADME();
    File unzipDir = new File(AppConstants.UNZIP_DIR, libraryName);
    FileUtil.unzip(resourceFile, unzipDir);
    installLibrary(libraryName, unzipDir);
  }

  public static void installLibrary(final String libraryName, final File root) {
    // Add as a resource root
    AppStatePersisted.addAssetRoot(root);
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
            () -> {
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
            });
      }
    }
    new SwingWorker<Object, Object>() {
      @Override
      protected Object doInBackground() {
        AssetManager.searchForImageReferences(root, AppConstants.IMAGE_FILE_FILTER);
        return null;
      }
    }.execute();
  }
}
