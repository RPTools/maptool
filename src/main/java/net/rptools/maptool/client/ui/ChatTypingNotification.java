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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;
import javax.swing.JPanel;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import org.apache.commons.collections4.map.LinkedMap;

@SuppressWarnings("serial")
public class ChatTypingNotification extends JPanel {
  private BufferedImage chatImage =
      ImageUtil.createCompatibleImage(
          RessourceManager.getSmallIcon(Icons.CHAT_NOTIFICATION).getImage());

  /**
   * This component is only made visible when there are notifications to be displayed. That means
   * the first couple of IF statements in this method are redundant since paintComponent() will not
   * be called unless the component is visible, and it will only be visible when there are
   * notifications...
   */
  @Override
  protected void paintComponent(Graphics g) {
    // System.out.println("Chat panel is painting itself...");
    if (AppPreferences.typingNotificationDurationInSeconds.get() == 0) {
      return;
    }
    LinkedMap chatTypers = MapTool.getFrame().getChatNotificationTimers().getChatTypers();
    if (chatTypers == null || chatTypers.isEmpty()) {
      return;
    }
    boolean showBackground = AppPreferences.chatNotificationBackground.get();

    Graphics2D statsG = (Graphics2D) g.create();

    Font boldFont = AppStyle.labelFont.deriveFont(Font.BOLD);
    Font font = AppStyle.labelFont;
    FontMetrics valueFM = g.getFontMetrics(font);
    FontMetrics keyFM = g.getFontMetrics(boldFont);

    int PADDING7 = 7;
    int PADDING3 = 3;
    int PADDING2 = 2;

    BufferedImage img = RessourceManager.getImage(Images.TEXTURE_PANEL);
    int rowHeight = Math.max(valueFM.getHeight(), keyFM.getHeight());

    setBorder(null);
    int width = AppStyle.miniMapBorder.getRightMargin() + AppStyle.miniMapBorder.getLeftMargin();
    int height =
        getHeight()
            - PADDING2
            + AppStyle.miniMapBorder.getTopMargin()
            + AppStyle.miniMapBorder.getBottomMargin();

    statsG.setFont(font);
    SwingUtil.useAntiAliasing(statsG);
    Rectangle bounds =
        new Rectangle(
            AppStyle.miniMapBorder.getLeftMargin(),
            height - getHeight() - AppStyle.miniMapBorder.getTopMargin(),
            getWidth() - width,
            getHeight()
                - AppStyle.miniMapBorder.getBottomMargin()
                - AppStyle.miniMapBorder.getTopMargin()
                + PADDING2);

    int y = bounds.y + rowHeight;
    rowHeight = Math.max(rowHeight, chatImage.getHeight());

    setSize(
        getWidth(),
        ((chatTypers.size() * (PADDING3 + rowHeight))
            + AppStyle.miniMapBorder.getTopMargin()
            + AppStyle.miniMapBorder.getBottomMargin()));

    if (showBackground) {
      g.drawImage(img, 0, 0, getWidth(), getHeight() + PADDING7, this);
      AppStyle.miniMapBorder.paintAround(statsG, bounds);
    }
    Rectangle rightRow =
        new Rectangle(
            AppStyle.miniMapBorder.getLeftMargin() + PADDING7,
            AppStyle.miniMapBorder.getTopMargin() + PADDING7,
            chatImage.getWidth(),
            chatImage.getHeight());

    Set<String> keySet = chatTypers.keySet();
    @SuppressWarnings("unchecked")
    Set<String> playerTimers = keySet;
    for (String playerNamer : playerTimers) {
      if (showBackground) {
        statsG.setColor(new Color(249, 241, 230, 140));
        statsG.fillRect(
            bounds.x + PADDING3,
            y - keyFM.getAscent(),
            (bounds.width - PADDING7 / 2) - PADDING3,
            rowHeight);
        statsG.setColor(new Color(175, 163, 149));
        statsG.drawRect(
            bounds.x + PADDING3,
            y - keyFM.getAscent(),
            (bounds.width - PADDING7 / 2) - PADDING3,
            rowHeight);
      }
      g.drawImage(
          chatImage,
          bounds.x + 5,
          y - keyFM.getAscent(),
          (int) rightRow.getWidth(),
          (int) rightRow.getHeight(),
          this);

      // Values
      statsG.setColor(MapTool.getFrame().getChatTypingLabelColor());
      statsG.setFont(boldFont);
      statsG.drawString(
          I18N.getText("msg.commandPanel.liveTyping", playerNamer),
          bounds.x + chatImage.getWidth() + PADDING7 * 2,
          y + 5);

      y += PADDING2 + rowHeight;
    }
    if (showBackground) {
      AppStyle.shadowBorder.paintWithin(statsG, bounds);
    } else {
      setOpaque(false);
    }
  }
}
