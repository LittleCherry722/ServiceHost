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

// var polling = {
//     lastUpdate: parseInt( (new Date().getTime())/1000, 10 ),
//     pollingUrl: "/changes",

//   updateHandler: {
//     // inserted: function(data) {
//     //   if (polling.getIndexById(Process.all(), data.id) === -1) {
//     //     var newItem = new Process(data);
//     //     Process.all.push(newItem);
//     //   }
//     // },
//     // updated: function(data) {
//     //   var changedIndex = polling.getIndexById(Process.all(), data.id);
//     //   var newItem = new Process(data);
//     //   Process.all()[changedIndex] = newItem;
//     // },
//     // deleted: function(data) {
//     //   var deletedIndex = polling.getIndexById(Process.all(), data.id);
//     //   if (deletedIndex>=0) {
//     //     Process.all().splice(deletedIndex, 1);
//     //   }
//     // }
//     action: {
//       inserted: function(data, resource) {
//         if ( !resource.find( data.id ) ) {
//           var instance = new resource(data);
//           resource.all.push( instance );
//         }
//       },
//       updated: function(data, resource) {
//         instance = resource.find(data.id)
//         if ( instance ) {
//           instance.applyData( data );
//         }
//       },
//       deleted: function(data, resource) {
//         resource.all.remove(function( instance ) {
//           return instance.id() === data.id;
//         })
//       }
//     }
//     // history: {
//     //     inserted: function(data) {
//     //         if (polling.getIndexById(History.all(), data.id) === -1) {
//     //             var newItem = new History(data);
//     //             History.all().push(newItem);
//     //          }
//     //     }
//     // },
//     // processInstance: {
//     //     inserted: function(data) {
//     //          if (polling.getIndexById(ProcessInstance.all(), data.id) === -1) {
//     //             var newItem = new ProcessInstance(data);
//     //             ProcessInstance.all().push(newItem);
//     //          }
//     //     },
//     //     updated: function(data) {
//     //         var changedIndex = polling.getIndexById(ProcessInstance.all(), data.id);
//     //         var newItem = new ProcessInstance(data);
//     //         ProcessInstance.all()[changedIndex] = newItem;
//     //     },
//     //     deleted: function(data) {
//     //         var deletedIndex = polling.getIndexById(ProcessInstance.all(), data.id);
//     //         ProcessInstance.all().splice(deletedIndex, 1);
//     //     }
//     // },
//     // message: {
//     //     inserted: function(data) {

//     //         if (data.toUser === currentUser().id() && polling.getIndexById(UserMessage.all(), data.id) === -1) {
//     //             var newItem = new UserMessage(data);
//     //             UserMessage.all().push(newItem);
//     //          }
//     //     },
//     //     updated: function(data) {
//     //         var changedIndex = polling.getIndexById(UserMessage.all(), data.id);
//     //         var newItem = new UserMessage(data);
//     //         UserMessage.all()[changedIndex] = newItem;
//     //     },
//     //     deleted: function(data) {
//     //         var deletedIndex = polling.getIndexById(UserMessage.all(), data.id);
//     //         UserMessage.all().splice(deletedIndex, 1);
//     //     }
//     // }

//   },

//     waitingTime: function() {
//         var now = parseInt( (new Date().getTime())/1000, 10 );
//         var s =  now - this.lastUpdate;
//         if (s <    30) return 2;
//         if (s <  5*60) return 10;
//         if (s < 30*60) return 30;
//         return 3*60;

//     },

//     poll: function() {
//         $.getJSON(  this.pollingUrl,
//                     {since: this.lastUpdate},
//                     function(data) {
//                         polling.update(data);
//                     });
//     },

//     update: function(pollingData) {
//         var self = this;
//         var changesReceived = 0;
//         var changedResources = [];
//         var resources = {
//           "process": Process,
//           "processInstance": ProcessInstance,
//           "action",
//           "history",
//           "message"
//         };
//         var actionOrder = ["inserted", "updated", "deleted"];


//         _(resourceOrder).each(function( resourceName, resource ) {
//             if (self.updateHandler[resourceName]) {

//                 _.each(actionOrder, function( actionName) {
//                     if (self.updateHandler[resourceName][actionName] && pollingData[resourceName] && pollingData[resourceName][actionName]) {
//                         var action = pollingData[resourceName][actionName];

//                         _.each(action, function( item) {
//                             changesReceived++;
//                             if ($.inArray(resourceName, changedResources)=== -1) {
//                                 changedResources.push(resourceName);
//                             }
//                             self.updateHandler[resourceName][actionName](item);
//                         });
//                     }
//                 });
//             }
//         });

//         if (changesReceived > 0) {
//             this.lastUpdate = Math.ceil( (new Date().getTime())/1000 );
//             this.updateView(changedResources);
//         }

//         console.log(changesReceived+" changes Received. Next poll in "+this.waitingTime()+" seconds.");

//         window.setTimeout(  function() { polling.poll(); },
//                             this.waitingTime()*1000 );
//     }

// };


// // window.setTimeout(  function() { polling.poll(); },
// //                     2500 );
