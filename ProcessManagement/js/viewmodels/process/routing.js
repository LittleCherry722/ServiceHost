define([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/group"
], function( ko, App, _, User, Group ) {



	var ViewModel = function() {

		this.currentProcess = currentProcess;

window.test = currentProcess;


		var self = this;

		self.name = "routingView";
		self.label = "Routing";

		self.routings = ko.observableArray([]);

		//Content dropdown
		self.subject = ko.observableArray(gf_getSubjectIDs());

		// TODO Subscribe to "/tk_graph/subjects" and trigger this function
		self.populateSubject = function() {
			self.subject(gf_getSubjectIDs())
		};

		self.is = ko.observableArray(["is", "is not"]);
		self.groupUser = ko.observableArray(["in group", "user"]);

		self.init = function() {
			self.populateSubject();
			console.log("RoutingViewModel: initialized.");
		}

		self.load = function(graph) {
			self.populateSubject();
			self.routings([]);
			//if routings exist
			if (graph.routings) {
				//add new Routings to routings
				for (var i in graph.routings) {
					z = graph.routings[i];
					y = new Routing(z.subject1Value, z.is1Value, z.groupUser1Value, z.groupUser1ListValue, z.subject2Value, z.is2Value, z.groupUser2Value, z.groupUser2ListValue);
					self.routings.push(y);
				}
			}
		}

		self.save = function() {
			var preSave = {
				routings : ko.mapping.toJS(self.routings())
			};
			return preSave;

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
			self.populateSubject();
			self.routings.push(new Routing());
		}

		self.removeRouting = function(element) {
			self.routings.remove(element);
		}

		self.showView = function() {
			//	SBPM.VM.contentVM().activeViewIndex(2);
		}
	};






	var currentProcess = ko.observable();

window.test = currentProcess;

	currentProcess.subscribe(function( process ) {
		console.log( "a new process has been loaded: " + process.name() );
	});

	var initialize = function( processID ) {
		var viewModel, process;

		process = Process.find( processID )

		if ( process === currentProcess() ) {
			currentProcess.valueHasMutated();
		} else {
			currentProcess( process )
		}

		viewModel = new ViewModel();

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


