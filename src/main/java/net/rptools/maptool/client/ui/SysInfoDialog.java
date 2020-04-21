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
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.MapToolSysInfoProvider;
import net.rptools.maptool.util.SysInfoProvider;

/**
 * Retrieves certain characteristics of the execution environment for the purposes of problem
 * determination and diagnostics. This class is invoked via the Help menu, Gather Debug Info... menu
 * option.
 *
 * @author frank
 */
public class SysInfoDialog {
  private static JDialog frame;
  private JTextArea infoTextArea;
  private SysInfoProvider sysInfoProvider = new MapToolSysInfoProvider();

  private Container createContentPane() {
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setOpaque(true);

    infoTextArea = new JTextArea(5, 30);
    infoTextArea.setEditable(false);
    infoTextArea.setLineWrap(true);
    infoTextArea.setWrapStyleWord(true);
    infoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
    infoTextArea.setText(I18N.getText("action.gatherDebugInfoWait"));
    EventQueue.invokeLater(new InfoTextSwingWorker());

    JScrollPane scrollPane = new JScrollPane(infoTextArea);
    scrollPane.setHorizontalScrollBarPolicy(31);
    scrollPane.setVerticalScrollBarPolicy(22);
    scrollPane.setViewportView(infoTextArea);

    contentPane.add(scrollPane, "Center");
    return contentPane;
  }

  private class InfoTextSwingWorker extends SwingWorker<List<String>, Void> {

    @Override
    protected List<String> doInBackground() {
      return sysInfoProvider.getInfo();
    }

    @Override
    protected void done() {
      try {
        infoTextArea.setText("");
        for (String row : get()) {
          infoTextArea.append(row);
        }
      } catch (InterruptedException ignore) {
      } catch (ExecutionException e) {
        MapTool.showError("Gathering Debug Information", e);
      }
    }
  }

  public static void createAndShowGUI(String title) {
    if (frame != null) {
      frame
          .dispose(); // This is so that the memory characteristics are queried each time this frame
      // is displayed.
      frame = null;
    }
    frame = new JDialog(MapTool.getFrame(), title);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    SysInfoDialog sysInfoDialog = new SysInfoDialog();
    frame.setContentPane(sysInfoDialog.createContentPane());
    try {
      Image img = ImageUtil.getImage("net/rptools/maptool/client/image/maptool_icon.png");
      frame.setIconImage(img);
    } catch (Exception ex) {
      MapTool.showError("While retrieving MapTool logo image?!", ex);
    }
    frame.setSize(550, 640);
    frame.setLocationByPlatform(true);
    frame.setVisible(true);
  }
}
