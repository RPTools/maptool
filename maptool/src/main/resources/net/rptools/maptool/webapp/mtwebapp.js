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


////////////////////////////////////////////////////////////////////////////////
//
// "private" __MapTool object.
//
// There are no guarantees that anything in this will stay the same so you
// should not call them directly.
//
// This will be moved/renamed/or something so do not use it (you have been
// warned!).
//
////////////////////////////////////////////////////////////////////////////////
var __MapTool = new (function __MapToolClient() {

    var messageIdSequence = 0;
    var websocket = null;

    var callbacks = {};

    var listeners = {};

    ////////////////////////////////////////////////////////////////////////////
    //
    // Checks to see if there is a call back registered for the specific message.
    //
    ////////////////////////////////////////////////////////////////////////////
    var hasCallback = function(messageType, messageId) {
        if (callbacks[messageType] && callbacks[messageType][messageId]) {
            return true;
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Gets the listener for a message type. A listener is called for a
    // message type when ever there is no callback waiting for the specific
    // message id.
    //
    ////////////////////////////////////////////////////////////////////////////
    var getListener = function(messageType) {
        return listeners[messageType];
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Handle information coming back from the the web socket and despatch it
    // to the correct call back.
    //
    ////////////////////////////////////////////////////////////////////////////
    var wsDespatchCallback = function(event) {
        console.log(event);
        var msg = jQuery.parseJSON(event.data);

        if (msg.messageType === 'keepalive') {
            return;
        }

        var callback;

        if (hasCallback(msg.messageType, msg.messageId)) {
            callback = getAndRemoveCallback(msg.messageType, msg.messageId);
        } else {
            callback = getListener(msg.messageType, msg.messageId);
        }

        if (typeof(callback) === 'function') {
            callback(msg.data);
        } else {
            console.log('Attempted a callback on something that is not a function, messageType = ' + msg.messageType);
            console.log(data);
            // FIXME: something (but what) should happen here
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Sets the web socket used by the object.
    //
    ////////////////////////////////////////////////////////////////////////////
    var setWebSocket = function(ws) {
        if (websocket) {
            console.log('Attempted to update the web socket.');
            // Do nothing for now, FIXME: something (but what) should happen here
        }
        websocket = ws;
        websocket.onmessage = wsDespatchCallback;

        websocket.onclose = function() {
            console.log('Closed');
            // FIXME: something (but what) should happen here
        };

        websocket.onerror = function(err) {
            console.log('Error: ' + err);
            // FIXME: something (but what) should happen here
        };

        websocket.onopen = function() {
            console.log('Opened');
            // FIXME: something (but what) should happen here
        };


        // Make sure that we explicitly close the web socket before before browser disposed of page incase the
        // browser doesn't actually do it.
        window.onbeforeunload = function() {
            websocket.onclose = function () {}; // disable onclose handler first
            websocket.close()
        };

    }


    ////////////////////////////////////////////////////////////////////////////
    //
    // Register a callback for a message type and message id.
    //
    ////////////////////////////////////////////////////////////////////////////
    var registerCallback = function(messageType, messageId, callback) {
        if (!callbacks[messageType]) {
            callbacks[messageType] = {};
        }

        callbacks[messageType][messageId] = callback;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Remove a callback for the specific message type/id from the list of call
    // backs awaiting messages and return it. If there is no specific call back
    // for the messageId the default (if it exists) call back for the
    // messageType, in this case the call back will not be removed.
    //
    ////////////////////////////////////////////////////////////////////////////
    var getAndRemoveCallback = function(messageType, messageId) {
        if (callbacks[messageType]) {
            var callback = callbacks[messageType][messageId];
            return callback;
        } else {
            return undefined;
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    //
    // Registers the listener for a message type. A listener is called for a
    // message type when ever there is no callback waiting for the specific
    // message id
    //
    ////////////////////////////////////////////////////////////////////////////
    this.regisetListener = function(messageType, callback) {
        listeners[messageType] = callback;
    }


    ////////////////////////////////////////////////////////////////////////////
    //
    // Returns the next message id.
    //
    ////////////////////////////////////////////////////////////////////////////
    var nextMessageId = function() {
        return messageIdSequence++;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Returns the current message id.
    //
    ////////////////////////////////////////////////////////////////////////////
    var currentMessageId = function() {
        return messageIdSequence;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Returns the websocket used for communicating with maptool.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.getWebSocket = function() {
        return websocket;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Sets the address for the websocket.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.setWebSocketAddress = function(addr) {
        setWebSocket(new WebSocket(addr));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Sends a message to MapTool.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.sendMessage = function(messageType, data,  callback) {
        var messageId = 'messsage::' + nextMessageId();

        var msg = {
            messageType: messageType,
            messageId: messageId,
            data: data
        };

        //
        // Only register a callback if we have one, otherwise just send
        // the message.
        //
        if (callback) {
            registerCallback(messageType, messageId, callback);
        }

        websocket.send(JSON.stringify(msg));
    }


    // Create the web socket for the information exchange.
    $(document).ready(function() {
        __MapTool.setWebSocketAddress('ws:' + document.location.host + '/ws/');
    });

})();





////////////////////////////////////////////////////////////////////////////////
//
// Constructor for MapToolAPI object.
//
////////////////////////////////////////////////////////////////////////////////
var MapToolAPI = new (function() {

    ////////////////////////////////////////////////////////////////////////////
    //
    // Gets the url for retrieving the image of the token.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.tokenImageURL = function(tokenId) {
        return '/token/image/' + tokenId;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Gets the url for retrieving the portrait of the token.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.tokenPortraitURL = function(tokenId) {
        return '/token/portrait/' + tokenId;
    }


    ////////////////////////////////////////////////////////////////////////////
    //
    // Gets the url for retrieving the portrait or the image if the portrait is
    // not defined of the token.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.tokenPortraitOrImageURL = function(tokenId) {
        return '/token/portraitOrImage/' + tokenId;
    }


    this.sendMessage = function(messageType, data, callback) {
        __MapTool.sendMessage(messageType, data, callback);
    }

    this.initative = new (function() {

        // Values used when we send initiative commands back.
        var currentInitiative;
        var currentRound;

        ////////////////////////////////////////////////////////////////////////////
        //
        // Function used to update the initiative values on the web page.
        //
        ////////////////////////////////////////////////////////////////////////////
        var updateInitiative = function(data) {
            console.log('Received Initiative');
            var initList =  $('#initList');
            initList.empty();


            currentInitiative = data.current;
            currentRound = data.round;


            var source = $('#init-element').html();
            var template = Handlebars.compile(source);

            var entries = data.initiative;
            var toggle = 0;
            for (var i = 0; i < entries.length; i++) {
                console.log('name = ' + entries[i].name + ' => ' + entries[i].initiative);
                var initDivClass;
                if (entries[i].holding === 'true') {
                    initDivClass = 'initHolding';
                } else if (currentInitiative == i) {
                    initDivClass = 'initCurrent';
                } else if (i < data.current) {
                    initDivClass = 'initDone';
                } else {
                    initDivClass = 'initPending';
                }

                var ownerClass;
                if (entries[i].playerOwns === 'true') {
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
        // Register a listener for initiative changes.
        //
        ////////////////////////////////////////////////////////////////////////////
        this.registerInitativeListener = function(listener) {
            __MapTool.regisetListener('initiative', listener);
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // Advance the initiative to the next token.
        //
        ////////////////////////////////////////////////////////////////////////////
        this.nextInitiative = function() {
            var data = {
                command: 'nextInitiative',
                currentInitiative: currentInitiative,
                currentRound: currentRound
            };

            MapToolAPI.sendMessage('initiative', data);
        }


        ////////////////////////////////////////////////////////////////////////////
        //
        // Move the initiative to the previous token.
        //
        ////////////////////////////////////////////////////////////////////////////
        this.previousInitiative = function() {
            var data = {
                command: 'previousInitiative',
                currentInitiative: currentInitiative,
                currentRound: currentRound
            };

            MapToolAPI.sendMessage('initiative', data);
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // Sort the tokens by initiative value.
        //
        ////////////////////////////////////////////////////////////////////////////
        this.sortInitiative = function() {
            var data = {
                command: 'sortInitiative',
                currentInitiative: currentInitiative,
                currentRound: currentRound
            };

            MapToolAPI.sendMessage('initiative', data);
        }


        ////////////////////////////////////////////////////////////////////////////
        //
        // Told the held property for the specified token.
        //
        ////////////////////////////////////////////////////////////////////////////
        this.toggleHold = function(tokenId, tokenIndex) {
            var data = {
                command: 'toggleHoldInitiative',
                token: tokenId,
                currentInitiative: currentInitiative,
                currentRound: currentRound,
                tokenIndex: tokenIndex
            };

            MapToolAPI.sendMessage('initiative', data);
        }


        // Register the default initiative listener.
        this.registerInitativeListener(updateInitiative);

    })();
})();



/*$(document).ready(function() {

    ////////////////////////////////////////////////////////////////////////////
    //
    // Function used to update the property types for tokens.
    //
    ////////////////////////////////////////////////////////////////////////////
    function updatePropertyTypes(data) {
        data.propertyTypes.forEach(function(ptype) {
            var templateName = ptype.name.replace(' ', '-');
            var template;
            template = '<template id = "' + + '">';
            template += '</template>'
        });
    }

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

    $('#initList').delegate('.tokenInitButton', 'click', function() {
        MapToolAPI.initative.toggleHold($(this).data('tokenid'), $(this).data('tokenindex'));
    });


});
*/
