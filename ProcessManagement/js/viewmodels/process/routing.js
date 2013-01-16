define([
	"knockout",
	"knockout.mapping",
	"app",
	"underscore",
	"models/user",
	"models/group"
], function( ko,komapping, App, _, User, Group ) {
	var viewModel;
	var ViewModel = function() {
		this.currentProcessID = currentProcessID;
		this.currentProcess = currentProcess;
		var self = this;

		var _super = self;

		self.name = "routingView";
		self.label = "Routing";

		self.routings = ko.observableArray( [] );

		//Save
		// These values do not need to be saved, for they will be loaded from the model.
		var mapping = {
			'ignore' : ["groupUser1", "groupUser2"]
		}
		self.routings.subscribe(function( newValue ) {
			currentProcess().graph().routings(komapping.toJS(self.routings(), mapping));
			console.log("has changed!")
			return komapping.toJS(self.routings(), mapping);
		});

		//Content dropdown
		self.subject = currentProcess().graph().subjectIDs;


		self.is = ko.observableArray( ["is", "is not"] );
		self.groupUser = ko.observableArray( ["in group", "user"] );

		self.init = function() {
			self.load(currentProcess().graph().routings());
			console.log("RoutingViewModel: initialized.");
		}


		self.load = function(arrayOfRoutings) {
			var newRoutings = ko.observableArray([]);
			self.routings([]);

			for ( var i in arrayOfRoutings ) {
				z = arrayOfRoutings[i];
				y = new Routing( z.subject1Value, z.is1Value, z.groupUser1Value, z.groupUser1ListValue, z.subject2Value, z.is2Value, z.groupUser2Value, z.groupUser2ListValue );
				newRoutings().push( y );
			}
			self.routings( newRoutings() );

		}

		var Routing = function(subject1Value, is1Value, groupUser1Value, groupUser1ListValue, subject2Value, is2Value, groupUser2Value, groupUser2ListValue) {
			var self = this;

			//Values of dropdown

			self.subject1Value = ko.observable(subject1Value);
			self.is1Value = ko.observable(is1Value);
			self.groupUser1Value = ko.observable(groupUser1Value);
			self.groupUser1ListValue = ko.observable(groupUser1ListValue);
			self.subject2Value = ko.observable(subject2Value);
			self.is2Value = ko.observable(is2Value);
			self.groupUser2Value = ko.observable(groupUser2Value);
			self.groupUser2ListValue = ko.observable(groupUser2ListValue);

			self.hasChanged = ko.computed(function() {
				komapping.toJS(self);
			});

			self.hasChanged.subscribe(function() {
				_super.routings.valueHasMutated();
			});


			//Dynamic content of dropdown
			self.groupUser1 = ko.computed(function() {
				if (self.groupUser1Value() === "user") {
					return User.all();
				} else {
					return Group.all();
				}
			});

			self.groupUser2 = ko.computed(function() {
				if (self.groupUser2Value() === "user") {
					return User.all();
				} else {
					return Group.all();
				}
			});

			//Updates dynamic value of dropdown
			self.groupUser1Value.subscribe(function() {
				self.groupUser1ListValue(undefined);

			});

			self.groupUser2Value.subscribe(function() {
				self.groupUser2ListValue(undefined);

			});

		}

		self.addRouting = function() {
			self.routings.push(new Routing());
		}

		self.removeRouting = function(element) {
			self.routings.remove(element);
		}
	};







	var currentProcess = ko.observable();

window.test = currentProcess;

	currentProcess.subscribe(function( process ) {
		console.log( "a new process has been loaded: " + process.name() );
	
	});

	var initialize = function( processID ) {
		var process;
		currentProcessID = processID;
		process = Process.find( processID )

		if ( process === currentProcess() ) {
			currentProcess.valueHasMutated();
		} else {
			currentProcess( process )
		}

		if ( !viewModel || viewModel.currentProcessID != currentProcessID ) {
			viewModel = new ViewModel();
		}

		console.log(process.graph().routings());

		viewModel.init();

		window.viewModel = viewModel;

		App.loadTemplate( "process/routing", viewModel, null, function() {
			console.log("template loaded")
		});
		
	
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


