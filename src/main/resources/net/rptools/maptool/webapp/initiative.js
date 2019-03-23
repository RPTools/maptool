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
    // Function used to update the initiative values on the web page.
    //
    ////////////////////////////////////////////////////////////////////////////
    var updateInitiative = function(data) {
        var initList =  $('#initList');
        initList.empty();


        currentInitiative = data.current;
        currentRound = data.round;


        var source = $('#init-element').html();
        var template = Handlebars.compile(source);

        var entries = data.initiative;
        var toggle = 0;
        for (var i = 0; i < entries.length; i++) {
            var initDivClass;
            if (entries[i].holding) {
                initDivClass = 'initHolding';
            } else if (currentInitiative == i) {
                initDivClass = 'initCurrent';
            } else if (i < data.current) {
                initDivClass = 'initDone';
            } else {
                initDivClass = 'initPending';
            }

            var ownerClass;
            if (entries[i].playerOwns) {
                ownerClass = 'playerIsOwner';
            } else {
                ownerClass = 'playerIsNotOwner';
            }

            /* FIXME: Remove this its just for testing.
             if (toggle == 0) {
             toggle = 1;
             ownerClass = 'playerIsOwner';
             } else {
             toggle = 0;
             ownerClass = 'playerIsNotOwner';
             }*/


            var vals = {
                'tokenName': entries[i].name,
                'initiative': entries[i].initiative,
                'initDivClass': initDivClass,
                'tokenIndex': entries[i].tokenIndex,
                'tokenId': entries[i].id,
                'tokenOwnerClass': ownerClass
            };

            var html = template(vals);
            initList.append(html);
        }
        $('#initRound').html(currentRound);
    };


    ////////////////////////////////////////////////////////////////////////////
    //
    // Add call back to the initiative buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('.initButton').on('click', function() {
        var initCommand = $(this).data('initcommand');

        switch (initCommand) {
            case 'nextInitiative':
                MapTool.initative.nextInitiative();
                break;
            case 'previousInitiative':
                MapTool.initative.previousInitiative();
                break;
            case 'sortInitiative':
                MapTool.initative.sortInitiative();
                break;
        }
    });

    ////////////////////////////////////////////////////////////////////////////
    //
    // Add call back to the initiative hold buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('#initList').delegate('.tokenInitButton', 'click', function() {
        MapTool.initative.toggleHold($(this).data('tokenid'), $(this).data('tokenindex'));
    });


    var listenerHandle = MapTool.initative.registerInitativeListener(updateInitiative);


});