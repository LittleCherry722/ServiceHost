var Mediator = function(){

    var self = this;

    // Extends IMediator
    IMediator.call(self);

    // Overwrite initListeners
    self.viewListeners = function(){
    }

    /**
     * used by afterRender in administration.html 
     */
    self.subviewListeners = {
    }

    self.afterRenderUsersRow = function(row){
    }
}

