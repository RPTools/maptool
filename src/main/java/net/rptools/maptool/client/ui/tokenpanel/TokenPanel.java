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
package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.ui.TokenPopupMenu;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

public class TokenPanel extends JPanel implements ModelChangeListener {

  private ZoneRenderer currentZoneRenderer;
  private JList tokenList;

  public TokenPanel() {
    setLayout(new BorderLayout());
    tokenList = new JList();
    tokenList.setCellRenderer(new TokenListCellRenderer());
    tokenList.addMouseListener(
        new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            // TODO: make this not an aic
            if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

              Token token = (Token) tokenList.getSelectedValue();
              currentZoneRenderer.centerOn(new ZonePoint(token.getX(), token.getY()));
              currentZoneRenderer.clearSelectedTokens();
              currentZoneRenderer.selectToken(token.getId());
            }
            if (SwingUtilities.isRightMouseButton(e)) {

              int itemUnderMouse = tokenList.locationToIndex(new Point(e.getX(), e.getY()));
              if (!tokenList.isSelectedIndex(itemUnderMouse)) {
                if (!SwingUtil.isShiftDown(e)) {
                  tokenList.clearSelection();
                }
                tokenList.addSelectionInterval(itemUnderMouse, itemUnderMouse);
              }

              final int x = e.getX();
              final int y = e.getY();
              EventQueue.invokeLater(
                  new Runnable() {
                    public void run() {

                      Token firstToken = null;
                      Set<GUID> selectedTokenSet = new HashSet<GUID>();
                      for (int index : tokenList.getSelectedIndices()) {

                        Token token = (Token) tokenList.getModel().getElementAt(index);
                        if (firstToken == null) {
                          firstToken = token;
                        }

                        if (AppUtil.playerOwns(token)) {
                          selectedTokenSet.add(token.getId());
                        }
                      }
                      if (selectedTokenSet.size() > 0) {

                        new TokenPopupMenu(selectedTokenSet, x, y, currentZoneRenderer, firstToken)
                            .showPopup(tokenList);
                      }
                    }
                  });
            }
          }
        });
    new TokenPanelTransferHandler(tokenList);
    add(BorderLayout.CENTER, new JScrollPane(tokenList));
  }

  public void setZoneRenderer(ZoneRenderer renderer) {
    if (currentZoneRenderer != null) {
      currentZoneRenderer.getZone().removeModelChangeListener(this);
    }

    currentZoneRenderer = renderer;

    if (currentZoneRenderer != null) {
      currentZoneRenderer.getZone().addModelChangeListener(this);

      repaint();
    }

    // TODO: make this not a aic
    EventQueue.invokeLater(
        new Runnable() {

          public void run() {
            Zone zone = currentZoneRenderer != null ? currentZoneRenderer.getZone() : null;
            tokenList.setModel(new TokenListModel(zone));
          }
        });
  }

  ////
  // ModelChangeListener
  public void modelChanged(ModelChangeEvent event) {

    // Tokens are added and removed, just repaint ourself
    ((TokenListModel) tokenList.getModel()).update();
    repaint();
  }
}
