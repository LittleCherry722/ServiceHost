define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"notify"
], function( ko, App, _, ProcessInstance, Notify ) {

	var ViewModel = function() {
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

		this.googleDriveData = googleDriveData;
		
	}

	var processInstance = ko.observable(),
			messageText     = ko.observable(),
			currentSubject  = ko.observable(),
			serverDone      = ko.observable(true),
			actionOfCurrentSubject,
			availableActions,
			actionData,
			stateName,
			stateText,
			isTypeOf;
	
	var action = function(action) {
		serverDone(false);
		
		data = actionOfCurrentSubject()
		id = data.processInstanceID;
		data.actionData = action;
		data = JSON.stringify(data);
		$.ajax({
			url : '/processinstance/' + id,
			type : "PUT",
			data : data,
			async : true,
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				processInstance().refresh();
				serverDone( true );
			},
			error : function(jqXHR, textStatus, error) {
				// TODO: IMPROVE ERROR HANDLING!
				Notify.error( "Error", "Unable to send action. Please try again." );
			}
		});

	};


	var refresh = function(data){
		
	}

	var send = function() {
		var deArray;

		serverDone( false );
		data = actionOfCurrentSubject()

		deArray = data.actionData[ 0 ];
		deArray[ "messageContent" ] = messageText();
		data.actionData = deArray;

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
			
			},
			complete : function(jqXHR, textStatus) {
			
				serverDone(true);
			}
		});
	};


	var googleDriveData = [{
		"alternateLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/edit",
		"createdDate": "2012-12-13T13:12:21.854Z",
		"editable": true,
		"embedLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/preview",
		"etag": "\"8yVRNuccmqeFK9PVUh1X3uV516c/MTM2MTkwMTUyODc3Ng\"",
		"exportLinks": {
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document": "https://docs.google.com/feeds/download/documents/export/Export?id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&exportFormat=docx",
			"application/vnd.oasis.opendocument.text": "https://docs.google.com/feeds/download/documents/export/Export?id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&exportFormat=odt",
			"text/html": "https://docs.google.com/feeds/download/documents/export/Export?id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&exportFormat=html",
			"application/rtf": "https://docs.google.com/feeds/download/documents/export/Export?id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&exportFormat=rtf",
			"text/plain": "https://docs.google.com/feeds/download/documents/export/Export?id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&exportFormat=txt",
			"application/pdf": "https://docs.google.com/feeds/download/documents/export/Export?id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&exportFormat=pdf"
		},
		"iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_document_list.png",
		"id": "1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc",
		"kind": "drive#file",
		"labels": {
			"hidden": false,
			"restricted": false,
			"starred": false,
			"trashed": false,
			"viewed": true
		},
		"lastModifyingUserName": "dp.dornseifer",
		"lastViewedByMeDate": "2013-02-26T17:53:17.187Z",
		"mimeType": "application/vnd.google-apps.document",
		"modifiedByMeDate": "2013-02-26T17:58:48.776Z",
		"modifiedDate": "2013-02-26T17:58:48.776Z",
		"ownerNames": ["dp.dornseifer"],
		"parents": [{
			"id": "0ANHCcqVVsnmfUk9PVA",
			"isRoot": true,
			"kind": "drive#parentReference",
			"parentLink": "https://www.googleapis.com/drive/v2/files/0ANHCcqVVsnmfUk9PVA",
			"selfLink": "https://www.googleapis.com/drive/v2/files/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/parents/0ANHCcqVVsnmfUk9PVA"
		}],
		"quotaBytesUsed": "0",
		"selfLink": "https://www.googleapis.com/drive/v2/files/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc",
		"shared": true,
		"thumbnailLink": "https://docs.google.com/feeds/vt?gd=true&id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&v=793&s=AMedNnoAAAAAUURbZSQ7c-9rXNiaTxNH3LsNQ0VjpAex&sz=s220",
		"title": "@llmydevices",
		"userPermission": {
			"etag": "\"8yVRNuccmqeFK9PVUh1X3uV516c/jdTZkbPb5zeRgHV3jNHz6RDePNo\"",
			"id": "me",
			"kind": "drive#permission",
			"role": "owner",
			"selfLink": "https://www.googleapis.com/drive/v2/files/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/permissions/me",
			"type": "user"
		},
		"writersCanShare": true,
		"owners": [{
			"kind": "drive#user",
			"displayName": "dp.dornseifer",
			"isAuthenticatedUser": true,
			"permissionId": "10836910001397265166"
		}],
		"lastModifyingUser": {
			"kind": "drive#user",
			"displayName": "dp.dornseifer",
			"isAuthenticatedUser": true,
			"permissionId": "10836910001397265166"
		}
	}, {
		"alternateLink": "https://docs.google.com/folder/d/0B9HCcqVVsnmfODBmNGM4YjQtOWZkZS00MTQ4LTkyMTctNzM3OTNjMzhhYjM5/edit",
		"createdDate": "2010-12-28T16:23:45.365Z",
		"editable": true,
		"etag": "\"8yVRNuccmqeFK9PVUh1X3uV516c/MTI5MzU1MzQyNTM2NQ\"",
		"iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_shared_collection_list.png",
		"id": "0B9HCcqVVsnmfODBmNGM4YjQtOWZkZS00MTQ4LTkyMTctNzM3OTNjMzhhYjM5",
		"kind": "drive#file",
		"labels": {
			"hidden": false,
			"restricted": false,
			"starred": false,
			"trashed": false,
			"viewed": false
		},
		"lastModifyingUserName": "dp.dornseifer",
		"mimeType": "application/vnd.google-apps.folder",
		"modifiedDate": "2010-12-28T16:23:45.365Z",
		"ownerNames": ["dp.dornseifer"],
		"parents": [{
			"id": "17JFzAP1Spy5JWmwZAWSmUUcyGndoJSttsJEGUWF4TL0",
			"isRoot": false,
			"kind": "drive#parentReference",
			"parentLink": "https://www.googleapis.com/drive/v2/files/17JFzAP1Spy5JWmwZAWSmUUcyGndoJSttsJEGUWF4TL0",
			"selfLink": "https://www.googleapis.com/drive/v2/files/0B9HCcqVVsnmfODBmNGM4YjQtOWZkZS00…yMTctNzM3OTNjMzhhYjM5/parents/17JFzAP1Spy5JWmwZAWSmUUcyGndoJSttsJEGUWF4TL0"
		}],
		"quotaBytesUsed": "0",
		"selfLink": "https://www.googleapis.com/drive/v2/files/0B9HCcqVVsnmfODBmNGM4YjQtOWZkZS00MTQ4LTkyMTctNzM3OTNjMzhhYjM5",
		"shared": true,
		"title": "New Folder",
		"userPermission": {
			"etag": "\"8yVRNuccmqeFK9PVUh1X3uV516c/HEKwisuPoG24xcOBvZaMc82GVYU\"",
			"id": "me",
			"kind": "drive#permission",
			"role": "owner",
			"selfLink": "https://www.googleapis.com/drive/v2/files/0B9HCcqVVsnmfODBmNGM4YjQtOWZkZS00MTQ4LTkyMTctNzM3OTNjMzhhYjM5/permissions/me",
			"type": "user"
		},
		"writersCanShare": true,
		"owners": [{
			"kind": "drive#user",
			"displayName": "dp.dornseifer",
			"isAuthenticatedUser": true,
			"permissionId": "10836910001397265166"
		}],
		"lastModifyingUser": {
			"kind": "drive#user",
			"displayName": "dp.dornseifer",
			"isAuthenticatedUser": true,
			"permissionId": "10836910001397265166"
		}
	}, {
		"alternateLink": "https://docs.google.com/folder/d/0B9HCcqVVsnmfN2ZlMWQwYmEtMDAxNy00OGVmLTllZGMtN2M1MzhmMWRlM2Zj/edit",
		"createdDate": "2010-12-28T16:21:58.318Z",
		"editable": true,
		"etag": "\"8yVRNuccmqeFK9PVUh1X3uV516c/MTI5MzU1MzMxODMxOA\"",
		"iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_shared_collection_list.png",
		"id": "0B9HCcqVVsnmfN2ZlMWQwYmEtMDAxNy00OGVmLTllZGMtN2M1MzhmMWRlM2Zj",
		"kind": "drive#file",
		"labels": {
			"hidden": false,
			"restricted": false,
			"starred": false,
			"trashed": false,
			"viewed": false
		},
		"lastModifyingUserName": "dp.dornseifer",
		"mimeType": "application/vnd.google-apps.folder",
		"modifiedDate": "2010-12-28T16:21:58.318Z",
		"ownerNames": ["dp.dornseifer"],
		"parents": [],
		"quotaBytesUsed": "0",
		"selfLink": "https://www.googleapis.com/drive/v2/files/0B9HCcqVVsnmfN2ZlMWQwYmEtMDAxNy00OGVmLTllZGMtN2M1MzhmMWRlM2Zj",
		"shared": true,
		"title": "DEV",
		"userPermission": {
			"etag": "\"8yVRNuccmqeFK9PVUh1X3uV516c/p4GNpU5lvWZq_amec9GqY7q5QNE\"",
			"id": "me",
			"kind": "drive#permission",
			"role": "owner",
			"selfLink": "https://www.googleapis.com/drive/v2/files/0B9HCcqVVsnmfN2ZlMWQwYmEtMDAxNy00OGVmLTllZGMtN2M1MzhmMWRlM2Zj/permissions/me",
			"type": "user"
		},
		"writersCanShare": true,
		"owners": [{
			"kind": "drive#user",
			"displayName": "dp.dornseifer",
			"isAuthenticatedUser": true,
			"permissionId": "10836910001397265166"
		}],
		"lastModifyingUser": {
			"kind": "drive#user",
			"displayName": "dp.dornseifer",
			"isAuthenticatedUser": true,
			"permissionId": "10836910001397265166"
		}
	}]

	var initialize = function( instance, subjectId ) {
		
		var viewModel;
		
		processInstance( instance );
		processInstance().refresh();
		availableActions = instance.actions;
		currentSubject = subjectId;

			//Only one currentSubject possible.
		actionOfCurrentSubject = ko.computed(function() {
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


		viewModel = new ViewModel();


		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


