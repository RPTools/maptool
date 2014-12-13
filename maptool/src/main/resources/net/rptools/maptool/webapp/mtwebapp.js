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
        var msg = jQuery.parseJSON(event.data);

        if (msg.messageType === 'keepalive') {
            return;
        }

        var callback;

        if (msg.inResponseTo && hasCallback(msg.messageType, msg.inResponseTo)) {
            callback = getAndRemoveCallback(msg.messageType, msg.inResponseTo);
        } else {
            callback = getListener(msg.messageType);
        }

        if (typeof(callback) === 'function') {
            callback(msg.data);
        } else {
            console.log('Attempted a callback on something that is not a function, messageType = ' + msg.messageType);
            console.log(callback);
            console.log(msg.data);
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
// MapTool API object.
//
////////////////////////////////////////////////////////////////////////////////
var MapTool = new (function() {



    var ListenerSupport = function() {
        var nextListenerSeq = 0;
        var initiativeListeners  = {};

        ////////////////////////////////////////////////////////////////////////
        //
        // Gets the next sequence number for the listener handles.
        //
        ////////////////////////////////////////////////////////////////////////
        var getNextListenerSeq = function() {
            return nextListenerSeq++;
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Register a listener for initiative changes.
        //
        ////////////////////////////////////////////////////////////////////////
        this.registerListener = function(listener) {
            var handle = 'listener:' + getNextListenerSeq();

            initiativeListeners[handle] = listener;

            return handle;
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Removes a initiative change listener.
        //
        ////////////////////////////////////////////////////////////////////////
        this.removeListener = function(handle) {
            delete initiativeListeners[handle];
        }

        this.updateListeners = function(data) {
            for (var handle in initiativeListeners) {
                var listener = initiativeListeners[handle];
                if (typeof(listener) === 'function') {
                    listener(data);
                }
            }
        }

    }

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

    ////////////////////////////////////////////////////////////////////////////
    //
    // Initiative functions.
    //
    ////////////////////////////////////////////////////////////////////////////
    this.initative = new (function() {

        // Values used when we send initiative commands back.
        var currentInitiative;
        var currentRound;

        var listenerSupport = new ListenerSupport();

        ////////////////////////////////////////////////////////////////////////
        //
        // Function that gets called when there is a change to the initiative
        //
        ////////////////////////////////////////////////////////////////////////
        var updateInitiative = function(data) {
            currentInitiative = data.current;
            currentRound = data.round;
            listenerSupport.updateListeners(data);
        }

        ////////////////////////////////////////////////////////////////////////
        //
        // Register a listener for initiative changes.
        //
        ////////////////////////////////////////////////////////////////////////
        this.registerInitativeListener = function(listener) {
            return listenerSupport.registerListener(listener);
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Removes a initiative change listener.
        //
        ////////////////////////////////////////////////////////////////////////
        this.removeInitiativeListener = function(handle) {
            listenerSupport.removeListener(handle);
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Gets the current initiative round.
        //
        ////////////////////////////////////////////////////////////////////////
        this.getCurrentRound = function() {
            return currentRound;
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Advance the initiative to the next token.
        //
        ////////////////////////////////////////////////////////////////////////
        this.nextInitiative = function() {
            var data = {
                command: 'nextInitiative',
                currentInitiative: currentInitiative,
                currentRound: currentRound
            };

            MapTool.sendMessage('initiative', data);
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Move the initiative to the previous token.
        //
        ////////////////////////////////////////////////////////////////////////
        this.previousInitiative = function() {
            var data = {
                command: 'previousInitiative',
                currentInitiative: currentInitiative,
                currentRound: currentRound
            };

            MapTool.sendMessage('initiative', data);
        }

        ////////////////////////////////////////////////////////////////////////
        //
        // Sort the tokens by initiative value.
        //
        ////////////////////////////////////////////////////////////////////////
        this.sortInitiative = function() {
            var data = {
                command: 'sortInitiative',
                currentInitiative: currentInitiative,
                currentRound: currentRound
            };

            MapTool.sendMessage('initiative', data);
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Told the held property for the specified token.
        //
        ////////////////////////////////////////////////////////////////////////
        this.toggleHold = function(tokenId, tokenIndex) {
            var data = {
                command: 'toggleHoldInitiative',
                token: tokenId,
                currentInitiative: currentInitiative,
                currentRound: currentRound,
                tokenIndex: tokenIndex
            };

            MapTool.sendMessage('initiative', data);
        }


        // Register the initiative listener.
        __MapTool.regisetListener("initiative", updateInitiative);

    })();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Token related functions
    //
    ////////////////////////////////////////////////////////////////////////////
    this.token = new (function() {

        var listenerSupport = new ListenerSupport();

        ////////////////////////////////////////////////////////////////////////
        //
        // Function that gets called when there is a change to a token
        //
        ////////////////////////////////////////////////////////////////////////
        var tokenChange = function(data) {
            listenerSupport.updateListeners(data);
        }

        ////////////////////////////////////////////////////////////////////////
        //
        // Register a listener for initiative changes.
        //
        ////////////////////////////////////////////////////////////////////////
        this.registerTokenChangeListener = function(listener) {
            return listenerSupport.registerListener(listener);
        }


        ////////////////////////////////////////////////////////////////////////
        //
        // Removes a initiative change listener.
        //
        ////////////////////////////////////////////////////////////////////////
        this.removeTokenChangeListener = function(handle) {
            listenerSupport.removeListener(handle);
        }



        ////////////////////////////////////////////////////////////////////////
        //
        // Get token properties.
        //
        ////////////////////////////////////////////////////////////////////////
        this.getTokenProperties = function(tokenId, callback) {
            var data = {
                command: 'tokenProperty',
                tokenId: tokenId
            };

            MapTool.sendMessage('tokenInfo', data, callback);
        }

        ////////////////////////////////////////////////////////////////////////
        //
        // Call a macro
        //
        ////////////////////////////////////////////////////////////////////////
        this.callMacro = function(tokenId, macroIndex) {
            var data = {
                command: 'callMacro',
                tokenId: tokenId,
                macroIndex: macroIndex
            };

            MapTool.sendMessage('macro', data);
        }

        ////////////////////////////////////////////////////////////////////////
        //
        // Get a token property
        //
        ////////////////////////////////////////////////////////////////////////
        this.getProperties = function(tokenId, propertyNames, callback) {
            var data = {
                command: 'tokenProperty',
                tokenId: tokenId
            };

            if ($.isArray(propertyNames)) {
                data.propertyNames = propertyNames;
            } else {
                console.log(typeof(propertyNames))
                data.propertyNames = [propertyNames];
            }

            MapTool.sendMessage('tokenProperties', data, callback);
        }

        ////////////////////////////////////////////////////////////////////////
        //
        // Set a token property
        //
        ////////////////////////////////////////////////////////////////////////
        this.setProperties = function(tokenId, properties) {
            var data = {
                command: 'setProperty',
                tokenId: tokenId,
                properties: properties
            }

            MapTool.sendMessage('setProperties', data);
        }


        // Register the initiative listener.
        __MapTool.regisetListener("token-update", tokenChange);

    })();

    ////////////////////////////////////////////////////////////////////////////
    this.misc = new (function() {
        this.includeCss = function(href) {
            var cssLink = $("<link rel='stylesheet' type='text/css' href='"+href+"'>");
            $("head").append(cssLink);
        }

    })();

    ////////////////////////////////////////////////////////////////////////////
    this.r20sheet = new (function() {

        ////////////////////////////////////////////////////////////////////////
        var updateSheet = function(sheet) {
            var propertyNames = [];
            $(sheet).find("input[name^='attr_']").each(function() {
                var propName = this.name.replace('attr_','');
                // Translate character_name into request for token name
                if (propName === 'character_name') {
                    propName = ':name';
                }
                propertyNames.push(propName);

                console.log(propName);
            });

            var tokenId = $(sheet).find('.__mt_r20sheet_tokenId').val();

            console.log(propertyNames);

            // FIXME: need to handle errors.
            MapTool.token.getProperties(tokenId, propertyNames, function(data) {
                for (var propName in data)
                $(sheet).find("input[name^='attr_']").each(function() {
                    var propName = this.name.replace('attr_', '');
                    // Translate character_name into request for token name
                    if (propName === 'character_name') {
                        propName = ':name';
                    }
                    if (data.propertiesMap[propName]) {
                        $(this).val(data.propertiesMap[propName]);
                    }
                });

            });
        }


        ////////////////////////////////////////////////////////////////////////
        var updateR20Sheet = function(sheet, dirname, sheetJson) {
            console.log(sheet);
            console.log(sheetJson);

            $(sheet).html(JSON.stringify(sheetJson));
            MapTool.misc.includeCss(dirname + sheetJson.css);
            $(sheet).load(dirname + sheetJson.html, function(response, status, xhr) {
                if (status != 'error') {
                    $(sheet).append('<input type="hidden" class="__mt_r20sheet_tokenId" value="">');
                }
            });
        }

        ////////////////////////////////////////////////////////////////////////
        this.setToken = function(sheet, tokenId) {
            $(sheet).find('.__mt_r20sheet_tokenId').val(tokenId);
            updateSheet(sheet);
        }


        ////////////////////////////////////////////////////////////////////////
        this.includeR20Sheets = function() {
            $('.r20Sheet').each(function(index) {
                var sheet = $(this);
                var sheetJsonUrl = $(sheet).data('sheetjson');
                var dirname = sheetJsonUrl.match(/.*\//);
                $.get(sheetJsonUrl, function(data) {
                    updateR20Sheet(sheet, dirname, data);
                });
            });
        }


        $(document).ready(function() {
            MapTool.r20sheet.includeR20Sheets();
        });
    })();



})();

