require([
	"knockout",
], function( ko ) {

// extended template binding with a listeners attributed
// ko.bindingHandlers.templateWithListeners = {
//   init : function(element, valueAccessor, allBindingAccessor, viewModel, context) {

//     return ko.bindingHandlers.template.init(element, valueAccessor, allBindingAccessor, viewModel, context);
//   },
//   update : function(element, valueAccessor, allBindingAccessor, viewModel, context) {
//     ko.bindingHandlers.template.update(element, valueAccessor, allBindingAccessor, viewModel, context);

//     var options = valueAccessor();

//     if (options.listeners) {
//       options.listeners.call(context);
//     }
//   }
// };

// adding a "tooltip label"
// ko.bindingHandlers.tooltip = {
//   init : function(element, valueAccessor, allBindingAccessor, viewModel, context) {

//     var tooltip = valueAccessor();

//     if(tooltip){

//       var qtipStyle = "ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow";
//       var qtipPositionAt = 'right top';
//       var qtipPositionMy = 'left bottom';

//       $(element).qtip({
//         content : {
//           text : tooltip
//         },
//         position : {
//           at : qtipPositionAt,
//           my : qtipPositionMy,
//           viewport : $(window),
//           adjust : {
//             method : 'mouse',
//             x : 0,
//             y : 0
//           }
//         },
//         style : {
//           classes : qtipStyle
//         }
//       });

//     }
//   }
// };


ko.bindingHandlers.fancybox = {
	init : function(element, valueAccessor, allBindingAccessor, viewModel, context) {

		var options = valueAccessor();

		// define a default fancybox
		var defaultValues = {
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function(){

			}
		};

		// replace default values by defined ones
		$(element).fancybox($.extend(defaultValues, options));
	}
};

ko.bindingHandlers.fancybox2 = {
	init : function(element, valueAccessor, allBindingAccessor, viewModel, context) {

		var options = valueAccessor();

		// define a default fancybox
		var defaultValues = {
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			// 'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function(){

			}
		};

		// replace default values by defined ones
		$(element).fancybox($.extend(defaultValues, options));
	}
};

// ko.bindingHandlers.chosen = {
//   init: function(element, valueAccessor, allBindingsAccessor, viewModel, context) {
//     var allBindings = allBindingsAccessor();

//     var options = {'default': 'Select one...'};
//     $.extend(options, allBindings.chosen)

//     $(element).attr('data-placeholder', options['default']).addClass('chzn-select').chosen();
//   },
//   update: function(element, valueAccessor, allBindingsAccessor, viewModel, context) {
//     $(element).trigger("liszt:updated");
//   }
// };


});
