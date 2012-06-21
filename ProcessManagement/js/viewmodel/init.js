
/**
 * Current ViewModel 
 */
SBPM.VM = {}

// on dom ready load templates
$(document).ready(function(){
    var scriptTags = $('script[type="text/html"]');
    var i = 1;
   
   // iterate through templates
    scriptTags.each(function(idx){
        // and load each
        $(this).load("include/"+Utilities.getFilename(true)+"/"+$(this).attr('id')+".html", function(data){
              
                                	console.log(data);
                    	
              
            // if the last template is loaded init and bind ViewModel to dom
            if(scriptTags.length == i++){
                
                    $.getScript("js/viewmodel/"+Utilities.getFilename(true)+".js")
                    .fail(function(jqxhr, settings, exception){
                        console.log("Error loading ViewModel for "+Utilities.getFilename(true)+". Error: "+exception);
                    })
                    .success(function(){
                        // the current ViewModel can be found here
                        SBPM.VM = new ViewModel();   
                        ko.applyBindings(SBPM.VM);
                    });
                    
            }
            
        });
    })

});