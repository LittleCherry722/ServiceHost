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

// define([
//   "knockout",
//   "model",
//   "underscore",
//   "moment"
// ], function( ko, Model, _, moment ) {

//   // Our main model that will be returned at the end of the function.
//   //
//   // Process is responsivle for everything associated with processes directly.
//   //
//   // For example: Getting a list of all processes, savin a process,
//   // validating the current process etc.
//   Graph = Model( "Graph" );

//   Graph.attrs({
//     graphString: {
//       type: "string",
//       defaults: "{}",
//       lazy: true
//     },
//     date: "string",
//     processId: "integer"
//   });

//   Graph.ids([ "id" ]);

//   Graph.belongsTo( "process" )

//   Graph.include({
//     beforeSave: function() {
//       this.date( moment().format( "YYYY-MM-DD HH:mm:ss" ) );
//     },

//     initialize: function() {
//       var self = this;

//       Graph.lazyComputed( this, 'graphObject', {
//         read: function() {
//           return $.parseJSON( self.graphString() );
//         },
//         write: function( graphObject ) {
//           var graphString = JSON.stringify( graphObject );
//           self.graphString( graphString );
//         }
//       });

//       Graph.lazyComputed( this, 'subjects', function() {
//         var subjects = {};

//         _( self.graphObject().process ).each(function( element ) {
//           subjects[ element['id'] ] = element['name'];
//         });

//         return subjects;
//       });

//       Graph.lazyComputed( this, 'subjectIds', function() {
//         var subjects = [];

//         _( self.graphObject().process ).each(function( element ) {
//           subjects.push( element['id'] );
//         });

//         return subjects;
//       });

//       Graph.lazyComputed( this, "routings", {
//         read: function() {
//           if ( self.graphObject() && self.graphObject().routings ) {
//             return self.graphObject().routings;
//           } else {
//             return [];
//           }
//         },
//         write: function( routings ) {
//           if ( !routings ) {
//             routings = [];
//           }
//           var graphObject = self.graphObject();
//           graphObject.routings = routings;
//           self.graphObject( graphObject );
//         }
//       });
//     }
//   });

//   return Graph;
// });
