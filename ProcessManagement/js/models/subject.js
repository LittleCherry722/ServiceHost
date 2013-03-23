/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
	"underscore",
	"knockout"
], function( _, ko ) {

	var SubjectConstructor = function() {

		/**
		 *	Local Subject object. Is only needed in the context of this ViewModel,
		 *	therefore no need to make it public and create its own file.
		 *
		 *	To be used as an object ( var subject = new Subject("my name"); )
		 *
		 *	@param {String} name the name of the subject
		 */
		var Subject = function( name ) {

			// Make empty string the default for the subject name
			if ( !name ) name = "";
			this.name = ko.observable( name )

			// Checks whether this subject is valid or not
			this.isValid = function() {

				// Mark as invalid is name is empty, whitespace, null, undefinde, false etc.
				if ( !this.name() ) {
					return false;
				}

				return true;
			}
		};



		Subject.all = ko.observableArray();

		Subject.allClean = function() {
			return _.chain( Subject.all() ).filter(function( subject ) {
				return subject.isValid();
			}).map(function(subject) {
				return subject.name();
			}).value();
		}

		/**
		 *	Removes a subject from the list of subjects.
		 *	@param {Subject} subject the subject to be removed.
			 */
		Subject.remove = function( subject ) {
			Subject.all.remove( subject );
		}

		/**
		 *	Adds an empty subject to the list of subjects.
		 */
		Subject.add = function() {
			Subject.all.push( new Subject() )
		}

		return Subject;
	}

		// Everything in this object will be the public API
	return new SubjectConstructor();
});

