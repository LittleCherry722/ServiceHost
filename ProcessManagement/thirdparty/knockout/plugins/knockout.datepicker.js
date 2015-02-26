require([
		"knockout",
		"jquery",
		"bootstrap.datepicker",
], function( ko, $ ) {

	ko.bindingHandlers.datepicker =
	{
		format: 'DD.MM.YYYY',
		init: function (element, valueAccessor, allBindings) {
			var opts = valueAccessor();
			if (opts.todayHighlight === undefined) opts.todayHighlight = true;
			if (opts.weekStart === undefined) opts.weekStart = 1;
			opts.format = ko.bindingHandlers.datepicker.format.toLowerCase();
			$(element).datepicker(opts);

			// trigger chosen:updated event when the bound value or options changes
			[ 'value' ].forEach(function (e) {
				var bv = allBindings.get(e);
				if (ko.isObservable(bv)) {
					bv.subscribe(function () { $(element).trigger('datepicker:updated'); });

					// FIXME: for some reason I need an additional explicit listener
					if (e === 'value')
						$(element).on("change", function(e) { bv($(element).val()); });
				}
			});
		},
		update: function (element) {
			$(element).trigger('datepicker:updated');
		}
	};

});
