(function() {
    if(window.ZhuanQuanJSBridge) {
        return;
    }

    var toString = {}.toString;
    function isType(type) {
      return function(obj) {
        return toString.call(obj) == '[object ' + type + ']';
      }
    }
    var isString = isType('String');
    var isNumber = isType('Number');
    var isFunction = isType('Function');
    var isBoolean = isType('Boolean');

    var console = window.console;
    var log = console.log;
    var postMessage = function(msg) {
        log.call(console, 'h5container.message: ' + msg);
    };

    var callbackHash = {};

    window.ZhuanQuanJSBridge = {
        android: true,
        call: function(fn, param, cb) {
            if(!isString(fn)) {
                return;
            }
            if(isFunction(param)) {
                cb = param;
                param = null;
            }
            var clientId = new Date().getTime() + '' + Math.random();
            cb = cb || function() {};
            if(isFunction(cb)) {
                callbackHash[clientId] = cb;
            }
            var invokeMsg = JSON.stringify({
                fn: fn,
                param: param,
                clientId: clientId,
                cb: cb
            });
            postMessage(invokeMsg);
        },
        trigger: function(name) {
            if(name) {
                var event = document.createEvent('Events');
                event.initEvent(name, false, true);
                var prevent = !document.dispatchEvent(event);
                ZhuanQuanJSBridge.call(name, { prevent: prevent });
            }
        },
        emit: function(name, param) {
            if(name) {
                var event = document.createEvent('Events');
                event.initEvent(name, false, true);
                if(param !== undefined && param !== null) {
                    var data = {};
                    if(isNumber(param) || isBoolean(param)) {
                        data = param;
                    }
                    else if(isString(param)) {
                        if(param.charAt(0) == '{' && param.charAt(param.length - 1) == '}'
                            || param.charAt(0) == '[' && param.charAt(param.length - 1) == ']') {
                            data = JSON.parse(param);
                        }
                        else {
                            data = param;
                        }
                    }
                    else {
                        data = param;
                    }
                    event.data = data;
                }
                document.dispatchEvent(event);
            }
        },
        _invokeJS: function(clientId, resp) {
            console.log("_invokeJS: " + clientId + ", " + typeof resp + ", " + resp);
            if(resp !== null && resp !== undefined && isString(resp)
                && (resp.charAt(0) == '{' && resp.charAt(resp.length - 1) == '}'
                   || resp.charAt(0) == '[' && resp.charAt(resp.length - 1) == ']')) {
                resp = JSON.parse(resp);
            }
            var func = callbackHash[clientId];
            setTimeout(function() {
                func(resp);
                delete callbackHash[clientId];
            }, 1);
        }
    };

    var readyEvent = document.createEvent('Events');
    readyEvent.initEvent('ZhuanQuanJSBridgeReady', false, false);
    document.dispatchEvent(readyEvent);
})();