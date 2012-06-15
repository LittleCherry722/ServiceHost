$(document).ready(function(){
    var scriptTags = $('script[type="text/html"]');
    var i = 1;
   
    scriptTags.each(function(idx){
        $(this).load("include/"+Utilities.getFilename(true)+"/"+$(this).attr('id')+".html", function(data){
           
        console.log("template include/"+Utilities.getFilename(true)+"/"+$(this).attr('id')+".html loaded");
            
            if(scriptTags.length == i++){
                
                
                    $.getScript("js/viewmodel/"+Utilities.getFilename(true)+".js")
                    .fail(function(jqxhr, settings, exception){
                        console.log("Error loading ViewModel for "+Utilities.getFilename(true)+". Error: "+exception);
                    });
                    
            }
            
        });
    })

});