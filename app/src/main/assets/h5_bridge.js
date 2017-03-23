(function() {
    if(window.JSBridge) {
        return;
    }

    var console = window.console;
    var log = console.log;
    var postMessage = function(msg) {
        log.call(console, "h5container.message: " + msg);
    };

    window.JSBridge = {
        call: function(fn, param, cb) {
            if(typeof fn !== 'string') {
                return;
            }
            if(typeof param === 'function') {
                cb = param;
                param = null;
            }
            var clientId = new Date().getTime() + '' + Math.random();
            var invokeMsg = JSON.stringify({
                fn: fn,
                param: param,
                clientId: clientId,
            });
            postMessage(invokeMsg);
        }
    };

    var readyEvent = document.createEvent('Events');
    readyEvent.initEvent('JSBridgeReady', false, false);
    document.dispatchEvent(readyEvent);
})();