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

	var MessageConstructor = function() {
		/**
		 *	To be used as an object:
		 *	var message = new Message("Subject 1", "Answer", "Subject 2");
		 *
		 *	@param {string} sender - the Sender of this message.
		 *	@param {string} message the message name / content
		 *	@param {string} receiver the receiver of this message
		 */
		var Message = function( sender, message, receiver ) {

			// Make empty string the default for every argument.
			if ( !sender ) sender = "";
			if ( !message ) message = "";
			if ( !receiver ) receiver = "";

			this.sender = ko.observable( sender );
			this.message = ko.observable( message );
			this.receiver = ko.observable( receiver );

			// Checks whether this message is valid or not
			this.isValid = function() {

				// mark as invalid if message is null, undefined, false, "0", only
				// whitespace etc
				if ( !this.message() ) {
					return false;
				}

				// Mark as invalid if sender is invalid
				if ( !this.sender() || !this.sender().isValid() ) {
					return false;
				}

				// Mark as invalid if receiver is invalid
				if ( !this.receiver() || !this.receiver().isValid() ) {
					return false
				}

				// No check has failed, so we assume everything is okay
				return true
			}.bind( this )
		};

		Message.all = ko.observableArray();

		Message.allClean = function() {
			var messages;

			messages = _( Message.all() ).filter(function( message ) {
				return message.isValid();
			});

			return _( messages ).map(function( message ) {
				return {
					message:   message.message(),
					sender:    message.sender().name().toLowerCase(),
					receiver:  message.receiver().name().toLowerCase()
				}
			});
		}

		/**
		 *	Removes a message from the list of messages.
		 *	@param {Message} message the subject to be removed.
			 */
		Message.remove = function( message ) {
			Message.all.remove( message );
		};

		/**
		 *	Adds an empty subject to the list of subjects.
		 */
		Message.add = function() {
			Message.all.push( new Message() )
		}

		return Message;
	}

	// Everything in this object will be the public API
	return new MessageConstructor();
});
