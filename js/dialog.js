var PM = {
 Dialog : function(title, text, buttons){
	
	function buildButtonString(buttons){
	    var buttonStr = "";
    	
    	for(var i = 0; i < buttons.length; i++)
    		buttonStr += '<input id="'+buttons[i].id+'" type="button" value="'+buttons[i].name+'" />';
    	
    	return buttonStr;
	}
	
	$.fancybox({
        modal   : true,
        width   : '400px',
        content :     '<div id="dialog-header" style="background-image:url(images/bg_header.png); background-repeat:repeat-x;">'
					+ '	<div id="dialog-logo">'
					+ '	<h3>'
					+ title
					+ '</h3>'
					+ '	</div>'
					+ '</div>'
					+ '<div style="margin:10px; width:270px">'
					+ '	<br class="clear"/>'
					+ text
					+ '	<br class="clear"/>'
					+ '	<br class="clear"/>'
					+ '	<br class="clear"/>'
					+ '	<div id="dialog-buttons" align="center" >'
					+ buildButtonString(buttons)
					+ '	</div>'
					+ '</div>',
		onComplete : function(){
			
			// assign actions
			for(var i = 0; i < buttons.length; i++)
			   jQuery("#"+buttons[i].id).click(buttons[i].action);
			   
		}
    });
 },
 
 NoticeDialog : function(title, text, okAction){
	PM.Dialog(title, text, [
		{
			id : "okBtn",
			name : "Ok",
			action : okAction || function(){
				parent.$.fancybox.close();
			}
		}		
	]);
 },
 
 YesNoDialog : function(title, text, yesAction, noAction){
	PM.Dialog(title, text, [
		{
			id : "yesBtn",
			name : "Yes",
			action : yesAction || function(){
				parent.$.fancybox.close();
			}
		},
		{
			id : "noBtn",
			name : "No",
			action : noAction || function(){
				parent.$.fancybox.close();
			}
		}
		
	]);
 }
}
