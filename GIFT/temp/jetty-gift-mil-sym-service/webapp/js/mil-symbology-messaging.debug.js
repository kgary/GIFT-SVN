
if (!Function.prototype.bind) { // check if native implementation available
  Function.prototype.bind = function(){ 
    var fn = this, args = Array.prototype.slice.call(arguments),
        object = args.shift(); 
    return function(){ 
      return fn.apply(object, 
        args.concat(Array.prototype.slice.call(arguments))); 
    }; 
  };
}

function checkComponentReadiness() {
    if (mil.symbology.renderer.isReady() && mil.symbology.messaging.isOzoneInitialized === true) {
        mil.symbology.messaging.publish(mil.symbology.messaging.STATUS_RESPONSE_CHANNEL, { "status": "true" });
    } else {
        setTimeout(function () { checkComponentReadiness(); }, 1000);
    }
}

var mil = mil || {};
mil.symbology = mil.symbology || {};
mil.symbology.messaging = {
    mapLookup: {},
    FEATURE_REQUEST_CHANNEL: "mil.symbology.feature.request",
    FEATURE_PLOT_CHANNEL: "mil.symbology.feature.plot",
    MAP_FEATURE_PLOT_CHANNEL: "map.feature.plot",
    MAP_FEATURE_UNPLOT_CHANNEL: "map.feature.unplot",
    ICON_REQUEST_CHANNEL: "mil.symbology.icon.request",
    STATUS_REQUEST_CHANNEL: "mil.symbology.status.request",
    STATUS_RESPONSE_CHANNEL: "mil.symbology.status",
    MAP_STATUS_VIEW_CHANNEL: "map.status.view",
    ERROR_CHANNEL: "mil.symbology.error",
    SUCCESS_CHANNEL: "mil.symbology.success",
    isOzoneInitialized: false,
    widgetId: "",
    plottedFeatures: {},
    plottedPoints: {},
    
    init: function () {
        if (Ozone !== undefined && Ozone !== null) {
            widgetEventingController = new Ozone.eventing.Widget('rpc_relay.uncompressed.html');
            this.subscribeToChannels();
            this.isOzoneInitialized = true;
        } else {
            alert("Symbology Renderer Widget was unable to initialize beacuse it cannot find Ozone");
        }
    },

    subscribeToChannels: function () {
        widgetEventingController.subscribe(this.FEATURE_REQUEST_CHANNEL, this.handleFeatureRequest.bind(this));
        widgetEventingController.subscribe(this.FEATURE_PLOT_CHANNEL, this.handleFeaturePlot.bind(this));
        widgetEventingController.subscribe(this.MAP_FEATURE_UNPLOT_CHANNEL, this.handleFeatureUnplot.bind(this));
        widgetEventingController.subscribe(this.ICON_REQUEST_CHANNEL, this.handleIconRequest.bind(this));
        widgetEventingController.subscribe(this.STATUS_REQUEST_CHANNEL, this.handleStatusRequest.bind(this));
        widgetEventingController.subscribe(this.MAP_STATUS_VIEW_CHANNEL, this.handleMapViewStatus.bind(this));
    },

    handleFeatureRequest: function (sender, message) {
        var response,
            responseArray,
            len,
            i,
            item,
            kml,
            latitude,
            longitude,
            altitude = 0,
            coords,
            senderObject;

        message = this.parseMessage(message);
        
        if (this.isArray(message) === false) {
            message = [message];
        }
        len = message.length;
        responseArray = [];
        for (i = 0; i < len; i+= 1) {
            item = message[i];
            coords = item.coordinates.split(",");
            latitude = parseFloat(coords[1]);
            longitude = parseFloat(coords[0]);
            if (coords.length > 2) {
                altitude = parseFloat(coords[2]);
            }
            if (mil.symbology.renderer.symbolDefTable.isMultiPoint(item.symbolCode)) {
                kml = mil.symbology.renderer.getMultiPoint({name: item.name, 
                                                            id: item.featureId, 
                                                            description: item.description, 
                                                            coordinates: item.coordinates, 
                                                            mapScale: item.mapScale, 
                                                            mapExtents: "-180,-90,180,90", 
                                                            symbolCode: item.symbolCode, 
                                                            modifiers: item.modifiers, 
                                                            altitudeMode: item.altitudeMode});
            } else {

                kml = mil.symbology.renderer.getSinglePoint({name: item.name, 
                                                                        id: item.featureId, 
                                                                        description: item.description, 
                                                                        coordinates: item.coordinates, 
                                                                        altitudeMode: item.altitudeMode, 
                                                                        symbolCode: item.symbolCode, 
                                                                        modifiers: item.modifiers});
            }
            response = { messageId: item.messageId, type: this.FEATURE_REQUEST_CHANNEL, data: { "featureId": item.featureId, "name": item.name, "feature": kml} };
            responseArray.push(response);
        }
        this.publish(this.SUCCESS_CHANNEL, JSON.stringify(responseArray), sender);
    },

    redrawFeatures: function (targetId, mapScale, mapBounds, sender) {
        var item,
            featureKey,
            kml,
            response,
            responseArray = [];

        for (featureKey in this.plottedFeatures) {
            if (this.plottedFeatures.hasOwnProperty(featureKey)) {
                item = this.plottedFeatures[featureKey];
                if(item.altitudeMode === undefined || item.altitudeMode === null){
                    item.altitudeMode = "clampToGround";
                }
                kml = mil.symbology.renderer.getMultiPoint({name:item.name, 
                                                                    id: item.featureId, 
                                                                    description: item.description, 
                                                                    coordinates: item.coordinates, 
                                                                    mapScale: mapScale, 
                                                                    mapExtents: mapBounds, 
                                                                    symbolCode: item.symbolCode, 
                                                                    modifiers: item.modifiers, 
                                                                    altitudeMode: item.altitudeMode});
                response = { "overlayId": item.overlayId, "featureId": item.featureId, "targetId": targetId, "name": item.name, "feature": kml };
                responseArray.push(response);
            }
        }
        if (responseArray.length > 0) {
            this.publish(this.MAP_FEATURE_PLOT_CHANNEL, responseArray, sender);
        }
    },

    pushPoints: function (sender) {
        var item,
            featureKey,
            kml,
            response,
            responseArray = [],
            latitude,
            longitude,
            altitude,
            coords;

        for (featureKey in this.plottedPoints) {
            if (this.plottedPoints.hasOwnProperty(featureKey)) {
                item = this.plottedPoints[featureKey];
                coords = item.coordinates.split(",");
                latitude = parseFloat(coords[1]);
                longitude = parseFloat(coords[0]);
                if (coords.length > 2) {
                    altitude = parseFloat(coords[2]);
                }
                kml = mil.symbology.renderer.getSinglePoint({name: item.name, 
                                                                        id: item.featureId, 
                                                                        description: item.description, 
                                                                        coordinates: item.coordinates, 
                                                                        altitudeMode: item.altitudeMode, 
                                                                        symbolCode: item.symbolCode, 
                                                                        modifiers: item.modifiers});
                
                response = { "overlayId": item.overlayId, "featureId": item.featureId, "name": item.name, "feature": kml };
                responseArray.push(response);
            }
        }
        if (responseArray.length > 0) {
            this.publish(this.MAP_FEATURE_PLOT_CHANNEL, responseArray, sender);
        }
    },

    handleFeaturePlot: function (sender, message) {
        var response,
            responseArray,
            len,
            i,
            item,
            kml,
            latitude,
            longitude,
            j,
            map,
            mapInfo,
            mapScale,
            mapBounds,
            altitude =0,
            coords;

        message = this.parseMessage(message);

        if (this.isArray(message) === false) {
            message = [message];
        }
        len = message.length;
        for (map in this.mapLookup) {
            if(this.mapLookup.hasOwnProperty(map)){
                mapInfo = this.mapLookup[map];
                responseArray = [];
                for (i = 0; i < len; i+= 1) {
                    item = message[i];
                    coords = item.coordinates.split(",");
                    latitude = parseFloat(coords[1]);
                    longitude = parseFloat(coords[0]);
                    if (coords.length > 2) {
                        altitude = parseFloat(coords[2]);
                    }
                    if (mil.symbology.renderer.symbolDefTable.isMultiPoint(item.symbolCode)) {
                        this.plottedFeatures[item.featureId] = item;
                        mapScale = Math.round(mapInfo.view.range * 10.5);
                        mapBounds = mapInfo.view.bounds.southWest.lon + "," + mapInfo.view.bounds.southWest.lat + "," + mapInfo.view.bounds.northEast.lon + "," + mapInfo.view.bounds.northEast.lat;
                        if (item.description === undefined) {
                            item.description = "";
                        }
                        if (item.modifiers === undefined || item.modifiers === null || item.modifiers === "null" || item.modifiers === "undefined") {
                            item.modifiers = "";
                        } else {
                            item.modifiers = JSON.stringify(item.modifiers);
                        }
                        kml = mil.symbology.renderer.getMultiPoint({name:item.name, 
                                                                    id: item.featureId, 
                                                                    description: item.description, 
                                                                    coordinates: item.coordinates, 
                                                                    mapScale: mapScale, 
                                                                    mapExtents: mapBounds, 
                                                                    symbolCode: item.symbolCode, 
                                                                    modifiers: item.modifiers, 
                                                                    altitudeMode: item.altitudeMode});
                    } else {
                        this.plottedPoints[item.featureId] = item;
        
                        kml = mil.symbology.renderer.getSinglePoint({name: item.name, 
                                                                        id: item.featureId, 
                                                                        description: item.description, 
                                                                        coordinates: item.coordinates, 
                                                                        altitudeMode: item.altitudeMode, 
                                                                        symbolCode: item.symbolCode, 
                                                                        modifiers: item.modifiers});

                    }
                    response = { "overlayId": item.overlayId, "featureId": item.featureId, "featureName": item.name, "feature": kml };
                    responseArray.push(response);
                }
            }
            this.publish(this.MAP_FEATURE_PLOT_CHANNEL, responseArray, mapInfo.sender);
        }
    },

    handleFeatureUnplot: function (sender, message) {
        var len,
            i,
            item;

        message = this.parseMessage(message);

        if (this.isArray(message) === false) {
            message = [message];
        }
        len = message.length;

        for (i = 0; i < len; i+= 1) {
            item = message[i];
            if(this.plottedPoints[item.featureId]) {
                delete this.plottedPoints[item.featureId];
            } else if(this.plottedFeatures[item.featureId]){   
                delete this.plottedFeatures[item.featureId];
            }
        }
    },

    handleIconRequest: function (sender, message) {
        var responseArray,
            len,
            i,
            iconUrl,
            offsets,
            request,
            senderObject

        message = this.parseMessage(message);
        senderObject = this.parseMessage(sender);

        if (this.isArray(message) === false) {
            message = [message];
        }
        len = message.length;
        responseArray = [];
        for (i = 0; i < len; i+= 1) {
            request = message[i];
            iconUrl = mil.symbology.renderer.generateSymbolURL(request.symbolCode, request.modifiers, "", false);
            offsets = mil.symbology.renderer.getIconOffset(iconUrl);
            responseArray.push({ messageId: request.messageId, type: this.ICON_REQUEST_CHANNEL, data: { appletIconUrl: iconUrl.applet, serviceIconUrl: iconUrl.service, iconOffsetX: parseFloat(offsets[0]), iconOffsetY: parseFloat(offsets[1])}});
        }
        this.publish(this.SUCCESS_CHANNEL, JSON.stringify(responseArray), sender);

    },

    handleStatusRequest: function (sender, message) {
        var response;

        sender = typeof sender === 'string' ? JSON.parse(sender) : sender;
        if (sender.id !== this.widgetId) {
            if (this.isOzoneInitialized === true && mil.symbology.renderer.isReady() === true) {
                response = { "status": "true" };
            } else {
                response = { "status": "false" };
            }
            this.publish(this.STATUS_RESPONSE_CHANNEL, response);
        }
    },

    handleMapViewStatus: function (sender, message) {

        var senderObj = typeof sender === 'string' ? JSON.parse(sender) : sender,
            mapScale,
            mapBounds;

        message = this.parseMessage(message);
        /* For maps that implment the 2525 rendering solution natively
        *  they can be ignored is the "ignoreRenderer" attribute is 
        *  added to a map.status.view paylod
        */
        if (!message.hasOwnProperty('ignoreRenderer') || message.ignoreRenderer === false) {
            if (this.mapLookup[senderObj.id] === undefined || this.mapLookup[senderObj.id] === null) {
                this.pushPoints(sender);
            }
            this.mapLookup[senderObj.id] = { view: message, sender: sender };
            // Calculate approximate map scale
            mapScale = Math.round(message.range * 10.5);
            // Used to clip symbols generated for performance
            mapBounds = message.bounds.southWest.lon + "," + message.bounds.southWest.lat + "," + message.bounds.northEast.lon + "," + message.bounds.northEast.lat;
            this.redrawFeatures(senderObj.id, mapScale, mapBounds, sender);
        }
    },

    publish: function (channel, message, sender) {
        if (this.isOzoneInitialized) {
            if (sender !== undefined && sender !== null) {
                widgetEventingController.publish(channel, message, sender);
            } else {
                widgetEventingController.publish(channel, message);
            }
        }
    },

    parseMessage: function (message) {
        message = typeof message === 'string' ? JSON.parse(message) : message;
        return message;
    },

    isArray: function (obj) {
        if (Object.prototype.toString.call(obj) === '[object Array]') {
            return true;
        }
        return false;
    }
};

mil.symbology.messaging.init();
checkComponentReadiness();

// Create a JSON object only if one does not already exist. We create the
// methods in a closure to avoid creating global variables.

var JSON;
if (!JSON) {
    JSON = {};
}

(function () {
    "use strict";

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    if (typeof Date.prototype.toJSON !== 'function') {

        Date.prototype.toJSON = function (key) {

            return isFinite(this.valueOf()) ?
                    this.getUTCFullYear() + '-' +
                    f(this.getUTCMonth() + 1) + '-' +
                    f(this.getUTCDate()) + 'T' +
                    f(this.getUTCHours()) + ':' +
                    f(this.getUTCMinutes()) + ':' +
                    f(this.getUTCSeconds()) + 'Z' : null;
        };

        String.prototype.toJSON =
            Number.prototype.toJSON =
            Boolean.prototype.toJSON = function (key) {
                return this.valueOf();
            };
    }

    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap,
        indent,
        meta = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"': '\\"',
            '\\': '\\\\'
        },
        rep;


    function quote(string) {

        // If the string contains no control characters, no quote characters, and no
        // backslash characters, then we can safely slap some quotes around it.
        // Otherwise we must also replace the offending characters with safe escape
        // sequences.

        escapable.lastIndex = 0;
        return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
            var c = meta[a];
            return typeof c === 'string' ? c :
                    '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' : '"' + string + '"';
    }


    function str(key, holder) {

        // Produce a string from holder[key].

        var i,          // The loop counter.
            k,          // The member key.
            v,          // The member value.
            length,
            mind = gap,
            partial,
            value = holder[key];

        // If the value has a toJSON method, call it to obtain a replacement value.

        if (value && typeof value === 'object' &&
                typeof value.toJSON === 'function') {
            value = value.toJSON(key);
        }

        // If we were called with a replacer function, then call the replacer to
        // obtain a replacement value.

        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

        // What happens next depends on the value's type.

        switch (typeof value) {
            case 'string':
                return quote(value);

            case 'number':

                // JSON numbers must be finite. Encode non-finite numbers as null.

                return isFinite(value) ? String(value) : 'null';

            case 'boolean':
            case 'null':

                // If the value is a boolean or null, convert it to a string. Note:
                // typeof null does not produce 'null'. The case is included here in
                // the remote chance that this gets fixed someday.

                return String(value);

                // If the type is 'object', we might be dealing with an object or an array or
                // null.

            case 'object':

                // Due to a specification blunder in ECMAScript, typeof null is 'object',
                // so watch out for that case.

                if (!value) {
                    return 'null';
                }

                // Make an array to hold the partial results of stringifying this object value.

                gap += indent;
                partial = [];

                // Is the value an array?

                if (Object.prototype.toString.apply(value) === '[object Array]') {

                    // The value is an array. Stringify every element. Use null as a placeholder
                    // for non-JSON values.

                    length = value.length;
                    for (i = 0; i < length; i += 1) {
                        partial[i] = str(i, value) || 'null';
                    }

                    // Join all of the elements together, separated with commas, and wrap them in
                    // brackets.

                    v = partial.length === 0 ? '[]' : gap ?
                        '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']' :
                        '[' + partial.join(',') + ']';
                    gap = mind;
                    return v;
                }

                // If the replacer is an array, use it to select the members to be stringified.

                if (rep && typeof rep === 'object') {
                    length = rep.length;
                    for (i = 0; i < length; i += 1) {
                        if (typeof rep[i] === 'string') {
                            k = rep[i];
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                } else {

                    // Otherwise, iterate through all of the keys in the object.

                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                }

                // Join all of the member texts together, separated with commas,
                // and wrap them in braces.

                v = partial.length === 0 ? '{}' : gap ?
                    '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}' :
                    '{' + partial.join(',') + '}';
                gap = mind;
                return v;
        }
    }

    // If the JSON object does not yet have a stringify method, give it one.

    if (typeof JSON.stringify !== 'function') {
        JSON.stringify = function (value, replacer, space) {

            // The stringify method takes a value and an optional replacer, and an optional
            // space parameter, and returns a JSON text. The replacer can be a function
            // that can replace values, or an array of strings that will select the keys.
            // A default replacer method can be provided. Use of the space parameter can
            // produce text that is more easily readable.

            var i;
            gap = '';
            indent = '';

            // If the space parameter is a number, make an indent string containing that
            // many spaces.

            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }

                // If the space parameter is a string, it will be used as the indent string.

            } else if (typeof space === 'string') {
                indent = space;
            }

            // If there is a replacer, it must be a function or an array.
            // Otherwise, throw an error.

            rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                    (typeof replacer !== 'object' ||
                    typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }

            // Make a fake root object containing our value under the key of ''.
            // Return the result of stringifying the value.

            return str('', { '': value });
        };
    }


    // If the JSON object does not yet have a parse method, give it one.

    if (typeof JSON.parse !== 'function') {
        JSON.parse = function (text, reviver) {

            // The parse method takes a text and an optional reviver function, and returns
            // a JavaScript value if the text is a valid JSON text.

            var j;

            function walk(holder, key) {

                // The walk method is used to recursively walk the resulting structure so
                // that modifications can be made.

                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }


            // Parsing happens in four stages. In the first stage, we replace certain
            // Unicode characters with escape sequences. JavaScript handles many characters
            // incorrectly, either silently deleting them, or treating them as line endings.

            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }

            // In the second stage, we run the text against regular expressions that look
            // for non-JSON patterns. We are especially concerned with '()' and 'new'
            // because they can cause invocation, and '=' because it can cause mutation.
            // But just to be safe, we want to reject all unexpected forms.

            // We split the second stage into 4 regexp operations in order to work around
            // crippling inefficiencies in IE's and Safari's regexp engines. First we
            // replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
            // replace all simple value tokens with ']' characters. Third, we delete all
            // open brackets that follow a colon or comma or that begin the text. Finally,
            // we look to see that the remaining characters are only whitespace or ']' or
            // ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

            if (/^[\],:{}\s]*$/
                    .test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
                        .replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
                        .replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

                // In the third stage we use the eval function to compile the text into a
                // JavaScript structure. The '{' operator is subject to a syntactic ambiguity
                // in JavaScript: it can begin a block or an object literal. We wrap the text
                // in parens to eliminate the ambiguity.

                j = eval('(' + text + ')');

                // In the optional fourth stage, we recursively walk the new structure, passing
                // each name/value pair to a reviver function for possible transformation.

                return typeof reviver === 'function' ?
                        walk({ '': j }, '') : j;
            }

            // If the text is not JSON parseable, then a SyntaxError is thrown.

            throw new SyntaxError('JSON.parse');
        };
    }
} ());



