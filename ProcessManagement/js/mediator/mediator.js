var IMediator = function(){

    var self = this;

    // every mediator should know its filepath
    self.filename = Utilities.getFilename(true);

    // init those empty
    self.viewListeners = function(){};
    self.subviewListeners = {};

    // epxose only the init method to inheritors
    self.init = function(viewListeners){

        // check if an unsupported browser is in use
        if ( $.browser.msie && $.browser.version <= 10)
            return $.fancybox(
                "<div style=\"background-image:url(images/bg_header.png); background-repeat:repeat-x; padding: 5px\"><h2>The browser you are using is not supported by S-BPM Groupware.</h2></div><br><br>"+
                "Since this application uses cutting edge technologies you should upgrade to a modern browser like <a href=\"https://www.google.com/intl/en/chrome/browser/\" target=\"_blank\">Google Chrome</a> and <a href=\"http://www.mozilla.org/en-US/firefox/new/\" target=\"_blank\">Mozilla Firefox</a>.<br><br><br>In the case you have no other choice then using Microsoft Internet Explorer please think about installing <a href=\"http://www.google.com/chromeframe/eula.html?user=true\" target=\"_blank\">Chrome Frame</a>."
                ,
                {
                    'autoDimensions'    : false,
                    'width'                 : 450,
                    'height'                : 200,
                    'transitionIn'      : 'none',
                    'transitionOut'     : 'none',
                    'showCloseButton' : false,
                    'enableEscapeButton' : false,
                    'hideOnOverlayClick':false,
                    'hideOnContentClick':false
                }
            );

        _private.loadTemplates(function(){
            (viewListeners || _private.viewListeners)();
            _private.loadViewModel();
        });

        console.log("IMediator: Initialized.");
    }

    // these vars are not accessable
    var _private = {
        loadTemplates : function(callback){
            var scriptTags = $('script[type="text/html"]');
            var i = 1;

            if(scriptTags.size() < 1) {
                callback();
            } else {
                // iterate through templates
                scriptTags.each(function(idx){
                    // and load each
                    $(this).load("include/"+self.filename+"/"+$(this).attr('id')+".html", function(data){

                        console.log("IMediator: Loading template 'include/"+self.filename+"/"+$(this).attr('id')+".html'");

                        // if the last template is loaded init and bind
                        // ViewModel to dom
                        if(scriptTags.length == i++){

                            callback();
                        }
                    });
                })
            }
        },
        viewListeners : function(){
            // this one needs to be defined in the specific mediator and is
            // used here only to avoid error-proneness
            console.log("IMediator: No listeners loaded for "+self.filename+".");
        },
        loadViewModel : function(){

            // load page's viewmodel
            $.getScript("js/viewmodel/"+self.filename+".js")
                .fail(function(jqxhr, settings, exception){
                    console.log("Error loading ViewModel for "+self.filename+". Error: "+exception);
                })
                .success(function(){
                    // the current ViewModel can be found here
                    SBPM.VM = new ViewModel();
                    SBPM.VM.init(function(){
                        ko.applyBindings(SBPM.VM);

                        console.log("IMediator: ViewModel bound to dom.");
                    });
                });
        }
    }
}

