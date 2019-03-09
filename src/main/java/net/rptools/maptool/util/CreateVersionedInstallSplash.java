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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.rptools.maptool.client.swing.SplashScreen;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CreateVersionedInstallSplash extends Application {
  private static String resourceImage =
      "net/rptools/maptool/client/image/maptool_splash_template.png";
  private static String webOutputPath;
  private static String versionText = "Dev-Build";
  private static final String FONT_RESOURCE = "/net/rptools/maptool/client/fonts/Horta.ttf";
  private static Font versionFont;

  public static void main(String[] args) {
    Options cmdOptions = new Options();
    cmdOptions.addOption("s", "source", true, "Source image to add version string to.");
    cmdOptions.addOption("o", "output", true, "Output /path/image to write to.");
    cmdOptions.addOption("v", "version", true, "Version text to add to image.");
    cmdOptions.addOption("w", "web_output", true, "Output path for upload to web server");

    // Parameters that can be overridden via command line options...
    resourceImage = getCommandLineOption(cmdOptions, "source", resourceImage, args);
    versionText = getCommandLineOption(cmdOptions, "version", versionText, args);
    webOutputPath = getCommandLineOption(cmdOptions, "web_output", null, args);

    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    BufferedImage webImage = createLaunchSplash("v" + versionText);

    try {
      System.out.println("Version: " + versionText);
      System.out.println("Source: " + resourceImage);

      if (webOutputPath != null) {
        System.out.println("Web Output: " + webOutputPath);
        updateWebVersion(versionText);
        ImageIO.write(webImage, "png", new File(webOutputPath + "/MapTool-splash.png"));
      }

    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }

    System.exit(0);
  }

  private static void updateWebVersion(String versionText) throws IOException {
    File releaseDir = new File(webOutputPath);
    if (!releaseDir.mkdirs())
      System.out.println("Error: Unable to create directory path [" + releaseDir + "]");

    FileWriter fstream = new FileWriter(webOutputPath + "/MapTool-version.js");
    BufferedWriter out = new BufferedWriter(fstream);
    out.write("var mtVersion = \"" + versionText + "\";");
    out.close();
  }

  public static BufferedImage createLaunchSplash(String versionText) {
    final ImageIcon splashIcon =
        new ImageIcon(SplashScreen.class.getClassLoader().getResource(resourceImage));
    final Color versionColor = Color.rgb(3, 78, 149, 1); // Color.rgb(27, 85, 139, 1)

    final int imgWidth = 490;
    final int imgHeight = 290;
    final int versionTextX = 48;
    final int versionTextY = 37;

    InputStream is = SplashScreen.class.getResourceAsStream(FONT_RESOURCE);
    versionFont = Font.loadFont(is, 28);

    BufferedImage buffImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = buffImage.createGraphics();
    RenderingHints rh =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    g2d.setRenderingHints(rh);
    g2d.drawImage(splashIcon.getImage(), 0, 0, null);

    // Adding glow twice to make it more pronounced...
    g2d.drawImage(
        textToImage(versionText, Color.WHITESMOKE, 28, true), versionTextX, versionTextY, null);
    g2d.drawImage(
        textToImage(versionText, Color.WHITESMOKE, 28, true), versionTextX, versionTextY, null);
    g2d.drawImage(
        textToImage(versionText, versionColor, 28, false), versionTextX, versionTextY, null);
    g2d.dispose();

    return buffImage;
  }

  private static BufferedImage textToImage(
      String text, Color fontColor, int fontSize, boolean addGlow) {
    Text versionText = new Text(0, 0, text);
    versionText.setFill(fontColor);
    versionText.setFont(versionFont);

    if (addGlow) {
      Effect glow = new Glow(1.0);
      versionText.setEffect(glow);
    }

    Stage stage = new Stage(StageStyle.TRANSPARENT);
    Group root = new Group();
    Scene scene = new Scene(root);
    SnapshotParameters sp = new SnapshotParameters();

    sp.setFill(Color.TRANSPARENT);
    stage.setScene(scene);
    root.getChildren().add(versionText);

    WritableImage img = root.snapshot(sp, null);

    return SwingFXUtils.fromFXImage(img, null);
  }

  private static String getCommandLineOption(
      Options options, String searchValue, String defaultValue, String[] args) {
    CommandLineParser parser = new DefaultParser();

    try {
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption(searchValue)) {
        return cmd.getOptionValue(searchValue);
      }
    } catch (ParseException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return defaultValue;
  }
}
