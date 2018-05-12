(function() {
    if(window.ZhuanQuanJSBridge || window.ZhuanQuanJsBridge) {
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

    window.ZhuanQuanJSBridge = window.ZhuanQuanJsBridge = {
        android: true,
        record: function(clientId, cb) {
            if(clientId && isFunction(cb)) {
                callbackHash[clientId] = cb;
            }
        },
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
                ZhuanQuanJsBridgeNative.call('', name, JSON.stringify({ prevent: prevent }));
            }
        },
        emit: function(name, param) {
            if(name) {
                var event = document.createEvent('Events');
                event.initEvent(name, false, true);
                event.data = param;
                document.dispatchEvent(event);
            }
        },
        _invokeJs: function(clientId, resp) {
            var func = callbackHash[clientId];
            func && func(resp);
            delete callbackHash[clientId];
        }
    };

    var readyEvent = document.createEvent('Events');
    readyEvent.initEvent('ZhuanQuanJSBridgeReady', false, false);
    document.dispatchEvent(readyEvent);

    var readyEvent = document.createEvent('Events');
    readyEvent.initEvent('ZhuanQuanJsBridgeReady', false, false);
    document.dispatchEvent(readyEvent);
})();
