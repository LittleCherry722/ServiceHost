var Notification = function() {
    
    var freeOwId = "#freeow",
        infoClass = "ok",
        warnClass = "notice",
        errClass  = "error",
        context;
        
    if(parent)
        context = parent;
    else
        context = window;

    this.Info = function(title, text) {
        return context.$(freeOwId).freeow(title, text, {
            classes: [,infoClass],
            autohide: true
        });
    }

    this.Error = function(title, text) {
        return context.$(freeOwId).freeow(title, text, {
            classes: [,errClass],
            autohide: true
        });
    }
    
    this.Warning = function(title, text) {
        return context.$(freeOwId).freeow(title, text, {
            classes: [,warnClass],
            autohide: true
        });
    }
}

SBPM.Notification = new Notification();