(function() {
    if(window.ZhuanQuanJSBridge) {
        return;
    }

    var console = window.console;
    var log = console.log;
    var postMessage = function(msg) {
        log.call(console, 'h5container.message: ' + msg);
    };

    window.ZhuanQuanJSBridge = {
        call: function(fn, param, cb) {
            if(typeof fn !== 'string') {
                return;
            }
            if(typeof param === 'function') {
                cb = param;
                param = null;
            }
            var invokeMsg = JSON.stringify({
                fn: fn,
                param: param,
                cb: cb
            });
            postMessage(invokeMsg);
        },
        trigger: function(name, param) {
            if(name) {
                var event = document.createEvent('Events');
                event.initEvent(name, false, true);
                if (typeof param === 'object') {
                    for(var k in param) {
                        event[k] = param[k];
                    }
                }
                var prevent = !document.dispatchEvent(event);
                ZhuanQuanJSBridge.call(name, { prevent: prevent });
            }
        }
    };

    var readyEvent = document.createEvent('Events');
    readyEvent.initEvent('ZhuanQuanJSBridgeReady', false, false);
    document.dispatchEvent(readyEvent);
})();