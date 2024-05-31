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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JFrame;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;

public class SplashScreen extends JFrame {
  private static final String FONT_RESOURCE = "/net/rptools/maptool/client/fonts/Horta.ttf";

  private static int imgWidth = 490;
  private static int imgHeight = 290;
  private static final int versionTextX = 48;
  private static final int versionTextY = 37;

  public SplashScreen(String versionText) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final JFXPanel fxPanel = new JFXPanel();

    setUndecorated(true);
    setType(Type.UTILITY);

    add(fxPanel);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    try {
      // Jamz: Remove border, change to transparent background
      // Done in a try/catch because this can bomb on linux...
      setBackground(new java.awt.Color(0, 0, 0, 0));
    } catch (Exception e) {
      setBackground(new java.awt.Color(0, 0, 0));
    }

    Platform.runLater(
        () -> {
          initFX(
              fxPanel, Character.isDigit(versionText.charAt(0)) ? "v" + versionText : versionText);
          int w = imgWidth;
          int h = imgHeight;
          int x = (screenSize.width - w) / 2;
          int y = (screenSize.height - h) / 2;
          setBounds(x, y, imgWidth, imgHeight);
          setVisible(true);
        });
  }

  public void hideSplashScreen() {
    setVisible(false);
    dispose();
  }

  private static void initFX(JFXPanel fxPanel, String versionText) {
    // This method is invoked on the JavaFX thread
    Group root = new Group();
    Scene scene = new Scene(root, Color.TRANSPARENT);

    if (Character.isDigit(versionText.charAt(0))) {
      versionText = "v" + versionText;
    }

    javafx.scene.image.Image splashImage =
        SwingFXUtils.toFXImage(createLaunchSplash("Launching... " + versionText), null);
    ImageView splashView = new ImageView(splashImage);
    imgWidth = (int) splashImage.getWidth();
    imgHeight = (int) splashImage.getHeight();
    root.getChildren().add(splashView);

    fxPanel.setScene(scene);
  }

  private static BufferedImage createLaunchSplash(String versionText) {
    var splashIcon = RessourceManager.getImage(Images.MAPTOOL_SPLASH);
    final Color versionColor = Color.rgb(3, 78, 149, 1); // Color.rgb(27, 85, 139, 1)

    InputStream is = SplashScreen.class.getResourceAsStream(FONT_RESOURCE);
    var versionFont = Font.loadFont(is, 28);

    BufferedImage buffImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = buffImage.createGraphics();
    RenderingHints rh =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    g2d.setRenderingHints(rh);
    g2d.drawImage(splashIcon, 0, 0, null);

    // Adding glow twice to make it more pronounced...
    g2d.drawImage(
        textToImage(versionText, Color.WHITESMOKE, versionFont, true),
        versionTextX,
        versionTextY,
        null);
    g2d.drawImage(
        textToImage(versionText, Color.WHITESMOKE, versionFont, true),
        versionTextX,
        versionTextY,
        null);
    g2d.drawImage(
        textToImage(versionText, versionColor, versionFont, false),
        versionTextX,
        versionTextY,
        null);
    g2d.dispose();

    return buffImage;
  }

  private static BufferedImage textToImage(
      String text, Color fontColor, Font versionFont, boolean addGlow) {
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
}
