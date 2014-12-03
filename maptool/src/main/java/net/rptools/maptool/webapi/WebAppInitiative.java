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
import net.rptools.maptool.client.ui.tokenpanel.InitiativePanel;
import net.rptools.maptool.model.*;
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
            if (InitiativeListModel.isTokenVisible(token.getToken(), initiativeList.isHideNPC())) {
                JSONObject tokJSon = new JSONObject();
                tokJSon.put("id", token.getToken().getId().toString());
                tokJSon.put("name", token.getToken().getName());
                tokJSon.put("holding", token.isHolding() ? "true" : "false");
                tokJSon.put("initiative", token.getState());
                tokArray.add(tokJSon);
            }
        }

        json.put("initiative", tokArray);
        json.put("current", initiativeList.getCurrent());
        json.put("round", initiativeList.getRound());
        json.put("canAdvance", canAdvanceInitiative());

        return json;
    }

    void sendInitiative(MTWebSocket mtws) {
        mtws.sendMessage("initiative", getInitiativeDetails());
    }

    void sendInitiative() {
        JSONObject init = getInitiativeDetails();
        MTWebClientManager.getInstance().sendToAllSessions("initiative", init);
    }

    void processInitiativeMessage(JSONObject json) {
        InitiativeList ilist = initiativeListener.initiativeList;
        String currentInit = Integer.toString(ilist.getCurrent());
        String currentRound = Integer.toString(ilist.getRound());

        // If there is a mismatch between client and us don't perform the command. This can happen because multiple
        // people can be updating the initiative at the same time and its possible that two people might update the
        // initiative at the same time.
        if (!currentInit.equals(json.getString("currentInitiative")) ||
                !currentRound.equals(json.getString("currentRound"))) {
            return; // FIXME: This needs to send a "can not do" message back to client.

        }

        String command = json.getString("command");
        if ("nextInitiative".equals(command)) {
            System.out.println("DEBUG: got next initiative call" + json.toString());

            if (canAdvanceInitiative()) { // Trust a web client? You gotta be joking :)
                ilist.nextInitiative();
            }
        } else if ("previousInitiative".equals(command)) {
            if (canAdvanceInitiative()) { // Trust a web client? You gotta be joking :)
                ilist.prevInitiative();
            }
        } else if ("sortInitiative".equals(command)) {
            ilist.sort();
        } else if ("toggleHoldInitiative".equals(command)) {
            if (canAdvanceInitiative()) { // Trust a web client? You gotta be joking :)
                InitiativeList.TokenInitiative ti = ilist.getTokenInitiative(ilist.getCurrent());
                ti.setHolding(!ti.isHolding());
            }
        }
    }

    // TODO: When the whole of initiative functionality is fixed up then this needs to be rolled into that.
    private boolean canAdvanceInitiative() {
        InitiativePanel ipanel = MapTool.getFrame().getInitiativePanel();
        if (ipanel.hasGMPermission()) {
            return true;
        }

        InitiativeList ilist = initiativeListener.initiativeList;

        if (ipanel.hasOwnerPermission(ilist.getTokenInitiative(ilist.getCurrent()).getToken())) {
            return true;
        }

        return false;
    }

}
