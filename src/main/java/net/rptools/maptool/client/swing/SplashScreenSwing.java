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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SplashScreenSwing extends JFrame implements ISplashScreen {
  private Image splashImage;

  private int imgWidth, imgHeight;

  public SplashScreenSwing(String imgName, final String versionText) {
    this(imgName, versionText, 195, 60, new Color(27, 85, 139));
  }

  public SplashScreenSwing(
      String imgName, final String versionText, int versionTextX, int versionTextY) {
    this(imgName, versionText, versionTextX, versionTextY, new Color(27, 85, 139));
  }

  public SplashScreenSwing(
      String imgName,
      final String versionText,
      final int versionTextX,
      final int versionTextY,
      final Color versionColor) {
    setUndecorated(true);
    loadSplashImage(imgName);

    setContentPane(
        new JPanel() {
          @Override
          public void paintComponent(Graphics g) {
            g.drawImage(splashImage, 0, 0, this);
            g.setColor(versionColor);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString(versionText, versionTextX, versionTextY);
          }
        });
  }

  public void loadSplashImage(String imgName) {
    MediaTracker tracker = new MediaTracker(this);
    splashImage =
        Toolkit.getDefaultToolkit()
            .createImage(SplashScreenSwing.class.getClassLoader().getResource(imgName));
    tracker.addImage(splashImage, 0);
    try {
      tracker.waitForAll();
    } catch (Exception e) {
      e.printStackTrace();
    }
    imgWidth = splashImage.getWidth(this);
    imgHeight = splashImage.getHeight(this);
  }

  public void showSplashScreen() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = imgWidth;
    int h = imgHeight;
    int x = (screenSize.width - w) / 2;
    int y = (screenSize.height - h) / 2;
    setBounds(x, y, w, h);
    try {
      // Jamz: Remove border, change to transparent background
      // Done in a try/catch because this can bomb on linux...
      setBackground(new Color(0, 0, 0, 0));
    } catch (Exception e) {
      setBackground(new Color(0, 0, 0));
    }
    setVisible(true);
  }

  /* (non-Javadoc)
   * @see net.rptools.maptool.client.swing.ISplashScreen#hideSplashScreen()
   */
  @Override
  public void hideSplashScreen() {
    setVisible(false);
    dispose();
  }
}
