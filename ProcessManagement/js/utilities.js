var Utilities = {
    getFilename : function(noExt){
        var url = window.location.pathname;
        var filename = url.substring(url.lastIndexOf('/')+1);

        if(noExt)
            filename = filename.replace(/\.(html|htm)/, "");

        return filename;
    },
    unimplError : function(callee){
        console.log("Function '"+callee+"' was called which is not implemented yet.");
    }
}