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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

public class TokenListModel implements ListModel {

  private List<ListDataListener> listenerList = new CopyOnWriteArrayList<ListDataListener>();

  private Zone zone;
  private List<Token> tokenList;

  public TokenListModel() {
    this(null);
  }

  public TokenListModel(Zone zone) {
    this.zone = zone;
  }

  public int getSize() {
    return getTokenList().size();
  }

  public Object getElementAt(int index) {
    return getTokenList().get(index);
  }

  public void addListDataListener(ListDataListener l) {
    listenerList.add(l);
  }

  public void removeListDataListener(ListDataListener l) {
    listenerList.remove(l);
  }

  public void update() {
    tokenList = new ArrayList<Token>();

    if (zone == null) {
      return;
    }

    if (MapTool.getPlayer().isGM()) {
      tokenList.addAll(zone.getAllTokens());
    } else {
      for (Token token : zone.getAllTokens()) {
        if (zone.isTokenVisible(token)) {
          tokenList.add(token);
        }
      }
    }

    for (ListIterator<Token> iter = tokenList.listIterator(); iter.hasNext(); ) {

      if (iter.next().isObjectStamp()) {
        iter.remove();
      }
    }

    Collections.sort(
        tokenList,
        new Comparator<Token>() {
          public int compare(Token o1, Token o2) {
            String lName = o1.getName();
            String rName = o2.getName();

            return lName.compareTo(rName);
          }
        });

    fireContentsChangedEvent(
        new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, tokenList.size()));
  }

  private void fireContentsChangedEvent(ListDataEvent e) {

    for (ListDataListener listener : listenerList) {
      listener.contentsChanged(e);
    }
  }

  private List<Token> getTokenList() {
    if (tokenList == null) {
      update();
    }

    return tokenList;
  }
}
