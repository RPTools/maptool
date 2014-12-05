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



$(document).ready(function() {

    ////////////////////////////////////////////////////////////////////////////
    //
    // Add call back to the initiative buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('.initButton').on('click', function() {
        var initCommand = $(this).data('initcommand');

        switch (initCommand) {
            case 'nextInitiative':
                MapToolAPI.initative.nextInitiative();
                break;
            case 'previousInitiative':
                MapToolAPI.initative.previousInitiative();
                break;
            case 'sortInitiative':
                MapToolAPI.initative.sortInitiative();
                break;
        }
    });

    ////////////////////////////////////////////////////////////////////////////
    //
    // Add call back to the initiative hold buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('#initList').delegate('.tokenInitButton', 'click', function() {
        MapToolAPI.initative.toggleHold($(this).data('tokenid'), $(this).data('tokenindex'));
    });


});