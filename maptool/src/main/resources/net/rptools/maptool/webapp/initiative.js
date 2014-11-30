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



var ws = new WebSocket('ws:' + document.location.host + '/ws/');

ws.onopen = function() {
    console.log("Opened");
    //ws.send("getInitiative");
};

ws.onmessage = function(event) {
    console.log("Message: " + event.data);
};

ws.onclose = function() {
    console.log("Closed");
};

ws.onerror = function(err) {
    console.log("Error: " + err);
};