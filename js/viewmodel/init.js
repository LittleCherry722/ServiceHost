$(document).ready(function(){
    $.getScript("js/viewmodel/"+Utilities.getFilename(true)+".js")
    .fail(function(jqxhr, settings, exception){
        console.log("Error loading ViewModel for "+Utilities.getFilename(true)+". Error: "+exception);
    });
});