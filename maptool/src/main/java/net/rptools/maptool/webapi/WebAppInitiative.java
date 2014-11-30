/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source Code.  If not, see <http://www.gnu.org/licenses/>
 */

package net.rptools.maptool.webapi;

import net.rptools.lib.AppEvent;
import net.rptools.lib.AppEventListener;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Zone;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class WebAppInitiative {


    private static final WebAppInitiative instance = new WebAppInitiative();


    private class InitiativeListener implements PropertyChangeListener, ModelChangeListener, AppEventListener {

        private InitiativeList initiativeList;
        private Zone zone;

        private void setList(InitiativeList ilist) {
            if (initiativeList != null) {
                initiativeList.removePropertyChangeListener(this);
            }
            initiativeList = ilist;
            initiativeList.addPropertyChangeListener(this);
        }

        private void setZone(Zone z) {
            setList(z.getInitiativeList());
            if (zone != null) {
                zone.removeModelChangeListener(this);
            }
            zone = z;
            zone.addModelChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // This should really be run in a separate thread, but to do that I need to work out concurrency issues.
            sendInitiative();
            System.out.println("Here!");
        }

        public void modelChanged(ModelChangeEvent event) {
            if (event.getEvent().equals(Zone.Event.INITIATIVE_LIST_CHANGED)) {
                setList(((Zone) event.getModel()).getInitiativeList());
                sendInitiative();
            }

        }

        @Override
        public void handleAppEvent(AppEvent appEvent) {
            // This is overkill but unfortunately have to do it this way because the initiative panel does and
            // creates a new initiative list when it does. FIXME: This needs to be fixed in both places.
            System.out.println("Here in handleAppEvent");
            setZone((Zone) appEvent.getNewValue());
            sendInitiative();
        }


        public void updateListeners() {
            setZone(MapTool.getFrame().getCurrentZoneRenderer().getZone());
        }


    }


    private InitiativeListener initiativeListener;

    public static WebAppInitiative getInstance() {
        return instance;
    }

    private WebAppInitiative() {
        initiativeListener = new InitiativeListener();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MapTool.getEventDispatcher().addListener(initiativeListener, MapTool.ZoneEvent.Activated);
                initiativeListener.updateListeners();
                //MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList().addPropertyChangeListener(initiativeListener);
                //MapTool.getFrame().getCurrentZoneRenderer().getZone().addModelChangeListener(initiativeListener);
                System.out.println("Here...");
            }
        });
    }


    private JSONObject getInitiativeDetails() {
        JSONObject json = new JSONObject();
        InitiativeList initiativeList = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();

        List<net.rptools.maptool.model.InitiativeList.TokenInitiative> tokenInitList = initiativeList.getTokens();

        JSONArray tokArray = new JSONArray();
        for (InitiativeList.TokenInitiative token : tokenInitList) {
            JSONObject tokJSon = new JSONObject();
            tokJSon.put("id", token.getToken().getId().toString());
            tokJSon.put("name", token.getToken().getName());
            tokJSon.put("holding", token.isHolding() ? "true" : "false");
            tokJSon.put("initiative", token.getState());
            tokArray.add(tokJSon);
        }

        json.put("initiative", tokArray);
        json.put("current", initiativeList.getCurrent());
        json.put("round", initiativeList.getRound());

        return json;
    }

    void sendInitiative(MTWebSocket mtws) {
        mtws.sendMessage("initiative", getInitiativeDetails());
    }

    void sendInitiative() {
        JSONObject init = getInitiativeDetails();
        MTWebClientManager.getInstance().sendToAllSessions("initiative", init);
    }


}
