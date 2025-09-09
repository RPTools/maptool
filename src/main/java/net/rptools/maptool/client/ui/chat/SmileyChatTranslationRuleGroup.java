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
package net.rptools.maptool.client.ui.chat;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SmileyChatTranslationRuleGroup extends ChatTranslationRuleGroup {
  static final Logger log = LogManager.getLogger(SmileyChatTranslationRuleGroup.class);
  private JPopupMenu emotePopup;

  public SmileyChatTranslationRuleGroup() {
    super("Smilies");
    initSmilies();
  }

  public JPopupMenu getEmotePopup() {
    return emotePopup;
  }

  @Override
  public boolean isEnabled() {
    return AppPreferences.showSmilies.get();
  }

  private void initSmilies() {
    // Load the smilies
    Properties smileyProps = new Properties();
    try {
      smileyProps.loadFromXML(
          ChatProcessor.class
              .getClassLoader()
              .getResourceAsStream("net/rptools/maptool/client/ui/chat/smileyMap.xml"));
    } catch (IOException ioe) {
      log.error("Could not load smiley map", ioe);
    }
    // Wrap values with img tag
    emotePopup = new JPopupMenu();
    for (Enumeration<?> e = smileyProps.propertyNames(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();

      // This is an incredibly bad hack to avoid writing an xml parser for the smiley map. I'm
      // feeling lazy today.
      StringTokenizer strtok = new StringTokenizer(smileyProps.getProperty(key), "|");
      String value = strtok.nextToken();
      String example = strtok.nextToken();

      String imgValue = "<img src='cp://" + value + "'>";
      smileyProps.setProperty(key, imgValue);

      JMenuItem item =
          new JMenuItem(new InsertEmoteAction(value, example)) {
            {
              setPreferredSize(new Dimension(25, 16));
            }
          };
      emotePopup.add(item);
    }

    // Install the translation rules
    for (Enumeration<?> e = smileyProps.propertyNames(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      String value = smileyProps.getProperty(key);
      /*
       * Make sure we're not in roll output. Wouldn't let me do this usinglookbehind :-/
       * Prevent all characters surrounding smiley except space, tab, and newline
       */
      key = "^((?:[^\036]|\036[^\036]*\036)*)(?<![^\\s\t\n\r])" + key + "(?![^\\s\t\n\r])";
      value = "$1" + value;
      addRule(new RegularExpressionTranslationRule(key, value));
    }
  }

  ////
  // EMOTE
  private class InsertEmoteAction extends AbstractAction {
    private final String insert;

    public InsertEmoteAction(String emoteImageSrc, String insert) {
      // This will force the image to be loaded into memory for use in the message panel
      try {
        putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil.getImage(emoteImageSrc)));
      } catch (IOException ioe) {
        SmileyChatTranslationRuleGroup.log.error("Cannot load smileyEmote", ioe);
      }
      this.insert = insert;
    }

    public void actionPerformed(ActionEvent e) {
      MapTool.getFrame()
          .getCommandPanel()
          .getCommandTextArea()
          .setText(MapTool.getFrame().getCommandPanel().getCommandTextArea().getText() + insert);
      MapTool.getFrame().getCommandPanel().getCommandTextArea().requestFocusInWindow();
    }
  }
}
