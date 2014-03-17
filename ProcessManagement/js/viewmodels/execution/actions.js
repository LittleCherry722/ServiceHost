define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"notify"
], function( ko, App, _, ProcessInstance, Notify ) {

	var ViewModel = function() {
		var self = this;
		this.processInstance = processInstance;
		this.availableActions = availableActions;
		this.currentSubject = currentSubject;
		this.actionOfCurrentSubject = actionOfCurrentSubject;
		this.actionData = actionData;
		this.messageText = messageText;
		this.action = action;
		this.send = send;
		this.stateName = stateName;
		this.stateText = stateText;
		this.isTypeOf = isTypeOf;
		this.serverDone = serverDone;
		this.currentSelectedFile = currentSelectedFile;
		this.selectFile = selectFile;
		this.executable = ko.computed(function() {

		});
		this.googleDriveData = ko.observable();
		this.selectUser = selectUser;
		this.selectedUsers = selectedUsers;
		this.selectUsersMin = selectUsersMin;
		this.selectUsersMax = selectUsersMax;
		this.selectUsersText = selectUsersText;
		this.refreshGoogleDriveData = function() {
			$.ajax({
				cache: false,
				dataType: "json",
				type: "GET",
				url: "../googledrive/get_files?id=" + App.currentUser().id(),
				success: function( data, textStatus, jqXHR ) {
					self.googleDriveData( data.items );
				},
				error: function( jqXHR, textStatus, error ) {
					Notify.error("Error", "There has been an Error retrieving the file list." +
											"Please make sure you have the appropriate permissions.");
				}
			});
		};
	};

	var selectFile = function() {
		$('#googleDriveModal').modal('hide');
		currentSelectedFile( this );

		// TODO set file ID for actually sending the file.
	};

	var currentSelectedFile = ko.observable({});

	var processInstance = ko.observable(),
		messageText     = ko.observable(),
		currentSubject  = ko.observable(),
		serverDone      = ko.observable(true),
		selectedUsers   = ko.observableArray(),
		actionOfCurrentSubject,
		availableActions,
		actionData,
		stateName,
		stateText,
		isTypeOf,
		selectUser,
		selectUsersMin,
		selectUsersMax,
		selectUsersText;

	var action = function(action) {
		serverDone(false);

		data = actionOfCurrentSubject();
		id = data.processInstanceID;
		data.actionData = action;

		data = JSON.stringify(data);
		$.ajax({
			url: '/processinstance/' + id,
			type: "PUT",
			data: data,
			async: true,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			success: function(data, textStatus, jqXHR) {
				serverDone( true );
				currentSelectedFile({});
				processInstance().refresh();
			},
			error : function(jqXHR, textStatus, error) {
				// TODO: IMPROVE ERROR HANDLING!
				Notify.error( "Error", "Unable to send action. Please try again." );
			}
		});

	};

	var refresh = function(data){
	};

	var send = function() {
		var deArray;

		serverDone( false );
		data = actionOfCurrentSubject();

		deArray = data.actionData[ 0 ];
		if( messageText() ) {
			deArray.messageContent = messageText();
		} else {
			deArray.messageContent = "[empty message]";
		}

		deArray.selectedUser = undefined;
		if(selectedUsers().length > selectUsersMax() || selectedUsers().length < selectUsersMin()){
			alert( 'Please select at least ' + selectUsersMin() + ' and at most ' + selectUsersMax() + ' Users!');
			return;
		}

		if ( deArray.hasOwnProperty('targetUsersData') ) {
			deArray.targetUsersData.targetUsers = selectedUsers();
		}

		data.actionData = deArray;
		data.actionData.fileId = currentSelectedFile().id;

		id = data.processInstanceID;

		data = JSON.stringify( data );

		$.ajax({
			url : '/processinstance/' + id,
			type : "PUT",
			data: data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				processInstance().refresh();
			},
			error : function(jqXHR, textStatus, error) {
				// TODO: IMPROVE ERROR HANDLING!
				Notify.error( "Error", "Unable to send action. Please try again." );
			},
			complete: function(jqXHR, textStatus) {
				serverDone( true );
				currentSelectedFile({});
			}
		});
	};

	var initialize = function( instance, subjectId ) {

		var viewModel;
		processInstance( instance );
		processInstance().refresh();
		availableActions = instance.actions;
		currentSubject = subjectId;

		//Only one currentSubject possible.
		actionOfCurrentSubject = ko.computed(function() {
            if(!$.isArray(availableActions())) {
                return undefined;
            }
			return availableActions().filter(function(action) {
				return action.subjectID === currentSubject();
			})[0];
		});

		isTypeOf = ko.computed(function() {
			if (actionOfCurrentSubject() !== undefined && actionOfCurrentSubject().stateType !== undefined) {
				return  actionOfCurrentSubject().stateType;
			} else {
				return undefined;
			}
		});

		stateName = ko.computed(function() {
			if (actionOfCurrentSubject() !== undefined) {
				return actionOfCurrentSubject().stateName;
			} else {
				return "";
			}
		});

		stateText = ko.computed(function() {
			if (actionOfCurrentSubject() !== undefined) {
				return actionOfCurrentSubject().stateText;
			} else {
				return "";
			}
		});

		actionData = ko.computed({
			//deferEvaluation : false,
			read : function() {
				if (actionOfCurrentSubject() !== undefined && actionOfCurrentSubject().actionData !== undefined) {
					return actionOfCurrentSubject().actionData;
				} else {
					return [];
				}
			}
		});

		selectUser = ko.computed(function () {
			if (actionOfCurrentSubject() !== undefined && actionOfCurrentSubject().actionData !== undefined && actionOfCurrentSubject().actionData.length > 0 && actionOfCurrentSubject().actionData[0].targetUsersData !== undefined) {
				var targetUsers = actionOfCurrentSubject().actionData[0].targetUsersData.targetUsers;
				return targetUsers.map(function (u) {
					for (var i in User.all()) {
						if (u === User.all()[i].id()) return User.all()[i];
					}
				});
			}
		});

		selectUsersMin = ko.computed( function() {
			if (actionOfCurrentSubject() && actionOfCurrentSubject().hasOwnProperty('actionData')){
				var actionData = actionOfCurrentSubject().actionData[0];
				if(actionData && actionData.targetUsersData && actionData.targetUsersData.hasOwnProperty('min')){
					return actionData.targetUsersData.min;
				}
			}
			return 1;
		});

		selectUsersMax = ko.computed( function() {
			if (actionOfCurrentSubject() && actionOfCurrentSubject().hasOwnProperty('actionData')){
				var actionData = actionOfCurrentSubject().actionData[0];
				if(actionData && actionData.targetUsersData && actionData.targetUsersData.hasOwnProperty('max')){
					return actionData.targetUsersData.max;
				}
			}
			return 1;
		});

		selectUsersText = ko.computed( function() {
			return selectUsersMax() == 1 ? 'User:' : 'Users (min=' + selectUsersMin() + ', max=' + selectUsersMax() + ')';
		});

		viewModel = new ViewModel();
		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
			/**
			 * Used to enable 'chosen' selects (if needed)
			 */
			var subscriptionFn = function () {
				if( selectUsersMax() > 1 ) {
					if( selectedUsers.length == 0 ) {
						$( '#actions .chzn-select' ).val( null )
					}
					//let the dom refresh first
					setTimeout( function () {
						$ ('#actions .chzn-select').chosen ()
					}, 0 );
				}
			};

			// chosen selects have to be enabled after knockout processed the html (otherwise the changes are
			// conflicting and overwritten)
			actionData.subscribe(subscriptionFn);
			isTypeOf.subscribe(subscriptionFn);
		});
	};

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});
