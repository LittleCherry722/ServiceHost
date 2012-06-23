var Notification = function() {
    
    var freeOwId = "#freeow",
        infoClass = "ok",
        warnClass = "notice",
        errClass  = "error";

    this.Info = function(title, text) {
        return $(freeOwId).freeow(title, text, {
            classes: [,infoClass],
            autohide: true
        });
    }

    this.Error = function(title, text) {
        return $(freeOwId).freeow(title, text, {
            classes: [,errClass],
            autohide: true
        });
    }
    
    this.Warning = function(title, text) {
        return $(freeOwId).freeow(title, text, {
            classes: [,warnClass],
            autohide: true
        });
    }
}

SBPM.Notification = new Notification();