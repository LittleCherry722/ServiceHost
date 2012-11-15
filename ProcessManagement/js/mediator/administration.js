var Mediator = function(){
    
    var self = this;
    
    // Extends IMediator
    IMediator.call(self);

    // Overwrite initListeners
    self.viewListeners = function(){

        key('c', function() {
            // if the subsite supports the create function
            if(typeof SBPM.VM.subsite().create == "function")
                SBPM.VM.subsite().create();
        });
        
    }

    /**
     * used by afterRender in administration.html 
     */
    self.subviewListeners = {
        General : function(){
            
            console.log("Mediator: Listeners of tab 'General' has been initialized.");
        },
        Users : function(){
            
            console.log("Mediator: Listeners of tab 'Users' has been initialized.");
        },
        Roles : function(elements){
            
            console.log("Mediator: Listeners of tab 'Roles' has been initialized.");
        },
        Groups : function(){
            
            console.log("Mediator: Listeners of tab 'Groups' has been initialized.");
        },
        Debug : function(){
            
            console.log("Mediator: Listeners of tab 'Debug' has been initialized.");
        }        
    }

    /**
     * called by include\administration\users.html
     * 
     * @param {Object} elements
     */
    self.afterRenderUsersRow = function(row){
        var select = $(row).find('.chzn-select');
        
        select.chosen();
        select.trigger("liszt:updated");

        $(row).find('.slider').slider({
            min : 1,
            max : 256,
            value : 8,
            slide : function(event, ui) {
                var input = $(this).parent().prev();

                // change value
                input.val(ui.value);
                
                // populate changed value to knockout
                input.change();
            },
            create : function(event, ui) {
                $(this).slider( "option", "value", $(event.target).parent().prev().val() );
            }
        });
        
    }
    self.afterRenderGroupRow = function(row){
        var select = $(row).find('.chzn-select');
        
        select.chosen();
        select.trigger("liszt:updated");

        $(row).find('.slider').slider({
            min : 1,
            max : 256,
            value : 8,
            slide : function(event, ui) {
                var input = $(this).parent().prev();

                // change value
                input.val(ui.value);
                
                // populate changed value to knockout
                input.change();
            },
            create : function(event, ui) {
                $(this).slider( "option", "value", $(event.target).parent().prev().val() );
            }
        });
        
    }
    self.afterRenderRoleRow = function(row){
        var select = $(row).find('.chzn-select');
       
        select.chosen();
        select.trigger("liszt:updated");

        $(row).find('.slider').slider({
            min : 1,
            max : 256,
            value : 8,
            slide : function(event, ui) {
                var input = $(this).parent().prev();

                // change value
                input.val(ui.value);
                
                // populate changed value to knockout
                input.change();
            },
            create : function(event, ui) {
                $(this).slider( "option", "value", $(event.target).parent().prev().val() );
            }
        });
        
    }

}

