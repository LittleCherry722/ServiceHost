var Mediator = function() {

    var self = this;

    // Extends IMediator
    IMediator.call(self);

    // Overwrite viewListeners
    self.viewListeners = function() {

    }
    /**
     * used by afterRender in administration.html
     */
    self.subviewListeners = {
        header : function () {
          
            console.log("Mediator: Listeners for HeaderView loaded.");
     
        },
        
        menu : function () {
                   
            $("#main_menu").accordion({
                collapsible : true,
                autoHeight : false
            });
    
            $("#calendar").datepicker({
                nextText : "&raquo;",
                prevText : "&laquo;"
            });
    
            $("a#save").click(function() {
                
                if(SBPM.Service.Process.saveProcess()){
                  // reload recent processes
                  self.init();
                  
                  SBPM.Notification.Info("Information", "Process successfully created.");
                }else
                  SBPM.Notification.Info("Error", "Could not create process.");
    
            });
    
            $("#hide_menu").click(function() {
                $("#left_menu").hide();
                $("#show_menu").show();
                $("body").addClass("nobg");
                $("#content").css("marginLeft", 35);
            });
    
            $("#show_menu").click(function() {
                $("#left_menu").show();
                $(this).hide();
                $("body").removeClass("nobg");
                $("#content").css("marginLeft", 245);
            });

            console.log("Mediator: Listeners for MenuView loaded.");
        },
        internalView : function() {
            //resize canvas to fit into screen
            $("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
            $("#graph_bv_outer").css("height", window.innerHeight - 145);
            $("#show_menu").click(function() {
                $(window).trigger('resize');
            });
            $("#hide_menu").click(function() {
                $(window).trigger('resize');
            });
            $(window).resize(function() {
                if ($("#show_menu").css("display") == "none") {
                    $("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
                    $("#graph_bv_outer").css("height", window.innerHeight - 145);
                } else {
                    $("#graph_bv_outer").css("width", window.innerWidth - 195);
                    $("#graph_bv_outer").css("height", window.innerHeight - 185);
                }
            });

            $("#rightMenuTrigger").click(function() {
                if ($("#RightMenuDiv").is(":visible")) {
                    $("#RightMenuDiv").hide();
                    $("#rightMenuTrigger").html("Show")
                } else {
                    $("#RightMenuDiv").show();
                    $("#rightMenuTrigger").html("Hide")
                }
            });

            $("#internalRadioMenu :input").bind("change", function() {
                if ($("#ge_edge_type_timeout").is(":checked")) {

                    $("#timeoutdiv").show();
                } else {
                    $("#timeoutdiv").hide();
                }

            });

        },
        
        chargeView : function() {

        },
        
        processView : function() {

            $("#slctSbj").chosen();

            $("#tab2").click(function() {
                console.log("tab2 clicked");

                $(this).parent().parent().find("td input").removeClass("active");
                $(this).addClass("active");
                $(".tab_content").addClass("hide");
                $("#tab2_content").removeClass("hide");
                gv_graph.changeView('cv');
                updateListOfSubjects();
                
                SBPM.VM.processVM.activeViewIndex(0);
            });

            $("#tab3").click(function() {
                console.log("tab3 clicked");

                $(this).parent().parent().find("td input").removeClass("active");
                $(this).addClass("active");
                $(".tab_content").addClass("hide");
                $("#tab3_content").removeClass("hide");

                gv_graph.selectedNode = null;
                updateListOfSubjects();
                $("#zoominbutton").hide();
                $("#zoomoutbutton").hide();
                $("#reset-button").hide();
                
                SBPM.VM.processVM.activeViewIndex(2);
            });



            console.log("Mediator: Listeners for ProcessView loaded.");

        },
        
        subjectView : function() {

            //resize canvas to fit into screen
            $("#graph_cv_outer").css("width", window.innerWidth - 170 - 245);
            $("#graph_cv_outer").css("height", window.innerHeight - 145);
            $("#show_menu").click(function() {
                $(window).trigger('resize');
            });
            $("#hide_menu").click(function() {
                $(window).trigger('resize');
            });
            $(window).resize(function() {
                if ($("#show_menu").css("display") == "none") {
                    $("#graph_cv_outer").css("width", window.innerWidth - 170 - 245);
                    $("#graph_cv_outer").css("height", window.innerHeight - 145);
                } else {
                    $("#graph_cv_outer").css("width", window.innerWidth - 195);
                    $("#graph_cv_outer").css("height", window.innerHeight - 185);
                }
            });

            $("#ge_cv_id").bind("change", function() {
                if ($("#ge_cv_id").val() == null || $("#ge_cv_id").val() == "") {

                    $("#AssignRoleWarning").show();
                } else {
                    $("#AssignRoleWarning").hide();
                }

            });

            console.log("Mediator: Listeners for SubjectView loaded.");

        },
        
        executionView : function() {

            
            console.log("Mediator: Listeners for ExecutionView loaded.");

        },
        
        courseView : function() {

            console.log("Mediator: Listeners for CourseView loaded.");

        },
        
        instanceView : function() {
            gf_showInternalBehavior(
                SBPM.Service.Process.loadGraph(getProcessIDforInstance(SBPM.Storage.get("userid"))), 
                getGroupName(getGroupIDforResponsibleUser(SBPM.Storage.get("userid"),getProcessIDforInstance(SBPM.Storage.get("instanceid"))).groups[0]).toLowerCase(), 
                SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")].history[SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")].history.length - 1].nodeid
                );
                
            console.log("Mediator: Listeners for InstanceView loaded.");
        }
    }

}