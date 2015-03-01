require([
		"knockout",
		"jquery",
		"jquery.chosen",
], function( ko, $ ) {

	// idea from https://stackoverflow.com/questions/22022261/jquery-chosen-doesnt-update-select-options-while-working-with-knockout-js
	ko.bindingHandlers.chosen =
	{
		init: function (element, valueAccessor, allBindings) {
			var opts = valueAccessor();
			if (opts.disable_search_threshold === undefined) opts.disable_search_threshold = 10;
			if (opts.width === undefined) opts.width = '100%';
			$(element).chosen(opts);

			// trigger chosen:updated event when the bound value or options changes
			[ 'value', 'selectedOptions', 'options' ].forEach(function (e) {
				var bv = allBindings.get(e);
				if (ko.isObservable(bv)) {
					bv.subscribe(function () { $(element).trigger('chosen:updated'); });

					// FIXME: for some reason I need an additional explicit listener
					if (e === 'value')
						$(element).on("change", function(e) { bv($(element).val()); });
				}
			});
		},
		update: function (element) {
			$(element).trigger('chosen:updated');
		}
	};

});
