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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
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
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import org.javatuples.Pair;

public class SplashScreen extends JFrame {
  private static final String FONT_RESOURCE = "/net/rptools/maptool/client/fonts/Horta.ttf";

  private static final int imgWidth = 490;
  private static final int imgHeight = 290;
  private static final int versionTextX = 48;
  private static final int versionTextY = 37;

  public SplashScreen(String versionText) {
    versionText = Character.isDigit(versionText.charAt(0)) ? "v" + versionText : versionText;

    setUndecorated(true);
    setType(Type.UTILITY);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = imgWidth;
    int h = imgHeight;
    int x = (screenSize.width - w) / 2;
    int y = (screenSize.height - h) / 2;
    setBounds(x, y, imgWidth, imgHeight);

    setLocationRelativeTo(null);
    setLayout(new GridBagLayout());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    try {
      // Jamz: Remove border, change to transparent background
      // Done in a try/catch because this can bomb on linux...
      setBackground(new java.awt.Color(0, 0, 0, 0));
    } catch (Exception e) {
      setBackground(new java.awt.Color(0, 0, 0));
    }

    var image = createLaunchSplash("Launching... " + versionText);

    setContentPane(
        new JPanel() {
          @Override
          protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, imgWidth, imgHeight, null);
          }
        });
  }

  private static BufferedImage createLaunchSplash(String versionText) {
    Image splashIcon = RessourceManager.getImage(Images.MAPTOOL_SPLASH);
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

    var future =
        CompletableFuture.supplyAsync(
            () -> {
              // Adding glow twice to make it more pronounced...
              var glowingText = textToImage(versionText, Color.WHITESMOKE, versionFont, true);
              var regularText = textToImage(versionText, versionColor, versionFont, false);
              return new Pair<>(glowingText, regularText);
            },
            Platform::runLater);

    try {
      var result = future.get();
      // Adding glow twice to make it more pronounced...
      g2d.drawImage(result.getValue0(), versionTextX, versionTextY, null);
      g2d.drawImage(result.getValue0(), versionTextX, versionTextY, null);
      g2d.drawImage(result.getValue1(), versionTextX, versionTextY, null);

    } catch (InterruptedException | ExecutionException e) {
      // Oh no... we can't show the version. Oh, well.
    }

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
