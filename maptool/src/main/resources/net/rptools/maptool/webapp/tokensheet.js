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
    // Add call back to the sheet buttons.
    //
    ////////////////////////////////////////////////////////////////////////////
    $('#initList').delegate('.tokenSheetButton', 'click', function() {
        MapTool.token.getTokenProperties($(this).data('tokenid'), function(data) {
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
        });

    });

});
