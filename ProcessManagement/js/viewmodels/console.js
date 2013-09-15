define([
	"knockout",
	"app",
	"underscore",
	"text!api_description.json",
	"rainbow",
	"js_beautify"
], function( ko, App, _, apiDescription, Rainbow, js_beautify ) {


	var ViewModel = function() {
		var that = this;

		this.outputText = ko.observable("");

		this.selectedMethod = ko.observable();

		this.apis = JSON.parse(apiDescription);

		this.methodSelected = function(method){
			that.selectedMethod(method);
			that.outputText("");
		};

		this.executeRequest = function(method){
			var viewElement = $('#console'),
				url = method.url;

			// replace url parameters
			_.each(url.match(/(\{\w*\})/ig), function(match){
				var elementName = match.replace(/\{(\w*)\}/ig, "$1"),
					val = $('.parameters-list input[name="'+elementName+'"]', viewElement).val();

				url = url.replace(match, val)
			});

			$.ajax({
				url: url,
				type: method.method,
				dataType: 'text'
			}).always(function(data){
				var text = data;
				if(typeof  data === 'object' && 'responseText' in data){
					text = "// Response: " + data.responseText;
				}
				that.outputText('// ' + method.method + ' ' + url + '\n' + text)
			});

		};

	};


	var initialize = function() {
		var viewModel = new ViewModel();
		viewModel.outputText.subscribe(function(data){
			Rainbow.color(js_beautify(data), 'javascript', function(highlighted_code) {
				$('#console .output').html(highlighted_code);
			});
		});

		App.loadTemplate( "console", viewModel, null, function() {
			//nothing here
		});
	};


	return {
		init: initialize
	}
});
