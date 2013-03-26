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

// new Objects
gv_graph.addSubject("Employee", "Employee");
gv_graph.addSubject("Manager", "Manager");
gv_graph.addSubject("HR", "HumanResources");
var gt_behav = null;

// employee
gt_behav = gv_graph.getBehavior("Employee");

gt_behav.addNode("start", "fill out vacation request", "start");
gt_behav.addNode("s1", "S");
gt_behav.addNode("r1", "R");
gt_behav.addNode("act1", "take\nvacation");
gt_behav.addNode("act2", "check\ndenial\nreason");
gt_behav.addNode("act3", "adapt\nvacation\nrequest");
gt_behav.addNode("end", "end");

gt_behav.addEdge("start", "s1", "");
gt_behav.addEdge("s1", "r1", "vacation\nrequest", "Manager");
gt_behav.addEdge("r1", "act2", "denial", "Manager");
gt_behav.addEdge("r1", "act1", "approval", "Manager");
gt_behav.addEdge("act1", "end", "");
gt_behav.addEdge("act2", "act3", "");
gt_behav.addEdge("act2", "end", "adaption\nnot possible");
gt_behav.addEdge("act3", "start", "");


// manager
gt_behav = gv_graph.getBehavior("Manager");

gt_behav.addNode("rcv1", "R", "start");
gt_behav.addNode("act1", "review\nvacation\nrequest");
gt_behav.addNode("s1", "S");
gt_behav.addNode("s2", "S");
gt_behav.addNode("s3", "S");
gt_behav.addNode("end");

gt_behav.addEdge("rcv1", "act1", "vacation\nrequest", "Employee");
gt_behav.addEdge("act1", "s1", "denial");
gt_behav.addEdge("act1", "s2", "approval")
gt_behav.addEdge("s1", "rcv1", "denial", "Employee");
gt_behav.addEdge("s2", "s3", "approval", "Employee");
gt_behav.addEdge("s3", "end", "approved\nvacation\nrequest", "hr");


// hr
gt_behav = gv_graph.getBehavior("HR");

gt_behav.addNode("rcv1", "R", "start");
gt_behav.addNode("act1", "archive\nvacation request");
gt_behav.addNode("end");

gt_behav.addEdge("rcv1", "act1", "");
gt_behav.addEdge("act1", "end", "vacation\nrequest\narchived");