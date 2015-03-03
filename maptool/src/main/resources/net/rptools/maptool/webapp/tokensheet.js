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

    var displayedToken = null;


    var updateSheet = function(data) {

        var source = $('#sheet-portrait').html();
        var template = Handlebars.compile(source);
        var vals = {
            tokenId: data.tokenId,
            tokenName: data.name,
            sheetProperties: []
        };

        for (var pname in data.properties) {
            var prop = data.properties[pname];
            if (prop) {
                var value = prop.value;
                if (value && prop.showOnStatSheet) {
                    vals.sheetProperties.push({name: pname, value: value});
                }
            }
        }

        var html = template(vals);

        var sheetBox =  $('#sheetBox');
        sheetBox.html(html);



        // First group together the buttons
        var groups = {};
        data.macros.forEach(function(macro) {
            var group = macro.displayGroup;
            if (!group) {
                group = 'No Group';
            }

            if (typeof(groups[group]) === 'undefined') {
                groups[group] = [];
            }

            groups[group].push(macro);
        });


        vals = {};
        for (var group in groups) {
            vals[group] = {
                group: group,
                macros: []
            }

            groups[group].forEach(function(macro) {
                vals[group].macros.push({
                    label: macro.label,
                    index: macro.index,
                    tokenId: data.tokenId
                });
            });
        }


        source = $('#macro-buttons').html();
        template = Handlebars.compile(source);
        html = template(vals);
        var macroList = $('#macroList');
        macroList.html(html);

    };

    ////////////////////////////////////////////////////////////////////////////
    //
    // Add call back to the sheet buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('#initList').delegate('.tokenSheetButton', 'click', function() {
        displayedToken = $(this).data('tokenid');

        MapTool.token.getTokenProperties(displayedToken, updateSheet);
        MapTool.r20sheet.setToken($('#r20sheetExample'), displayedToken);
    });



    ////////////////////////////////////////////////////////////////////////////
    //
    // Register call backs for macro buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('#macroList').delegate('.macroButton', 'click', function() {
        MapTool.token.callMacro($(this).data('tokenid'), $(this).data('macro-index'));
    });



    ////////////////////////////////////////////////////////////////////////////
    //
    // Register call back for token changes.
    //
    ////////////////////////////////////////////////////////////////////////////
    MapTool.token.registerTokenChangeListener(function(data) {
        if (displayedToken && data.tokensChanged && $.inArray(data.tokensChanged, displayedToken)) {
            MapTool.token.getTokenProperties(displayedToken, updateSheet);
        }
    });


});
