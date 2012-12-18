<?php

/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Thorsten Jacobi, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

include ("include/dbconnect.php");

$userid = (isset($_REQUEST['userid'])) ? $_REQUEST['userid'] : -1;
$subjects = (isset($_REQUEST['subjects'])) ? $_REQUEST['subjects'] : -1;
$limit = (isset($_REQUEST['limit'])) ? "LIMIT 0," . $_REQUEST['limit'] : "";

if (isset($_REQUEST['action'])) {
	$return = array();
	$action = $_REQUEST['action'];


	// Fetch all processes
	if ($action == 'all') {
		$result = mysql_query("SELECT * FROM `process` ORDER BY `ID`");
		$processes = array();
		while ($process = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push( $processes, array(
				"id" => $process['ID'],
				"name" => $process['name'],
				"isCase" => ( $process['isProcess'] == '0' ),
				"graphID" => $process['graphID']
			));
		}
		$return = $processes;

	// Create a new process
	} elseif ($action == 'create') {
		$isProcess = (isset($_REQUEST['isCase']) && ($_REQUEST['isCase'] === "true" || $_REQUEST['isCase'] === true))? 0 : 1;
		$name = mysql_real_escape_string($_REQUEST['name']);
		mysql_query("INSERT INTO `process` (`name`, `isProcess`) VALUES ('" . $name . "', '" . $isProcess . "');");
		$return['id'] = mysql_insert_id();
		$return['isCase'] = $_REQUEST['isCase'];
		$return['name'] = $_REQUEST['name'];
		$return['graphID'] = $_REQUEST['graphID'];

  // destroy an existing process
	} elseif ($action == 'destroy') {
		$processes = mysql_query("SELECT * FROM `process` WHERE `ID` LIKE '" . $_REQUEST['id'] . "'");
		if (mysql_num_rows($processes) > 0) {
			mysql_query("DELETE FROM `process` WHERE `ID` LIKE '" . $_REQUEST['id'] . "'");
			$return['code'] = "removed";
		} else {
			$return['code'] = "error";
		}

	// save existing process
	} elseif ($action == 'save') {
		$isProcess = (isset($_REQUEST['isCase']) && ($_REQUEST['isCase'] === "true" || $_REQUEST['isCase'] === true))? 0 : 1;
		$id = mysql_real_escape_string($_REQUEST['id']);
		$name = mysql_real_escape_string($_REQUEST['name']);
		$graphID = mysql_real_escape_string($_REQUEST['graphID']);
    mysql_query("UPDATE `process` SET `graphID` = '" . $graphID . "', `name` = '" . $name . "' WHERE `ID` = " . $id);
		$return['id'] = $_REQUEST['id'];
		$return['isCase'] = $_REQUEST['isCase'];
		$return['name'] = $_REQUEST['name'];
		$return['graphID'] = $_REQUEST['graphID'];

	} elseif ($action == "getid") {
		$name = mysql_real_escape_string($_REQUEST['name']);
    $procs = mysql_query("SELECT * FROM `process` WHERE `name` LIKE '" . $name . "'");
		if (mysql_num_rows($procs) == 1) {
			$line = mysql_fetch_array($procs, MYSQL_ASSOC);
			$return['id'] = $line['ID'];
			$return['code'] = "ok";
		} else {
			$return['code'] = "error";
		}
	}







	// if (isset($_REQUEST['processname'])) {
	//   $procs = mysql_query("SELECT * FROM `process` WHERE `name` LIKE '" . $_REQUEST['processname'] . "'");
	//   if ($_REQUEST['action'] == 'new') {
	//     if (mysql_num_rows($procs) == 0) {
  //       $isProcess = (isset($_REQUEST['isProcess']) && $_REQUEST['isProcess'] == "true")? 1 : 0;
	//       mysql_query("INSERT INTO `process` (`name`, `isProcess`) VALUES ('" . $_REQUEST['processname'] . "', '" . $isProcess . "');");
	//       $return['id'] = mysql_insert_id();
	//       $return['code'] = "added";
	//     } else {
	//       $return['code'] = "error";
	//     }
	//   } elseif ($_REQUEST['action'] == 'remove') {
	//     if (mysql_num_rows($procs) > 0) {
	//       mysql_query("DELETE FROM `process` WHERE `name` LIKE '" . $_REQUEST['processname'] . "'");
	//       $return['code'] = "removed";
	//     } else {
	//       $return['code'] = "error";
	//     }
	//   } elseif ($_REQUEST['action'] == "getIsProcess") {
	//     if (mysql_num_rows($procs) == 1) {
	//       $line = mysql_fetch_array($procs, MYSQL_ASSOC);
	//       $return['isProcess'] = "" . $line['isProcess'];
	//       $return['code'] = "ok";
	//     } else {
	//       $return['code'] = "error";
	//     }
	//   }






  // // NEW AND BETTER

	// // Fetch all processes
	// } elseif ($_REQUEST['action'] == 'all') {
	//   $result = mysql_query("SELECT * FROM `process` ORDER BY `ID`");
	//   $processes = array();
	//   while ($process = mysql_fetch_array($result, MYSQL_ASSOC)) {
	//     array_push( $processes, array( "id" => $process['ID'], "name" =>
	//       $process['name'], "isCase" => ( $process['isProcess'] == '0' ),
	//       "graphID" => $process['graphID'] ) );
	//   }
	//   $return = $processes;

	// // Create a new process
	// } elseif ($_REQUEST['action'] == 'create') {
	//   $isProcess = (isset($_REQUEST['isCase']) && ($_REQUEST['isCase'] === "true" || $_REQUEST['isCase'] === true))? 0 : 1;
	//   $name = mysql_real_escape_string($_REQUEST['name']);
	//   $graphID = mysql_real_escape_string($_REQUEST['graphID']);
	//   $id = mysql_real_escape_string($_REQUEST['id']);
  //   mysql_query("UPDATE `process` SET `graphID` = '" . $graphID . "', `name` = '" . $name . "' WHERE `ID` = " . $id);
	//   $return['id'] = mysql_insert_id();
	//   $return['isCase'] = $_REQUEST['isCase'];
	//   $return['name'] = $_REQUEST['name'];
	//   $return['graphID'] = -1;

  // // destroy an existing process
	// } elseif ($_REQUEST['action'] == 'destroy') {
	//   $processes = mysql_query("SELECT * FROM `process` WHERE `ID` LIKE '" . $_REQUEST['id'] . "'");
  //   if (mysql_num_rows($processes) > 0) {
  //     mysql_query("DELETE FROM `process` WHERE `ID` LIKE '" . $_REQUEST['id'] . "'");
  //     $return['code'] = "removed";
  //   } else {
  //     $return['code'] = "error";
  //   }

  // // save existing process
	// } elseif ($_REQUEST['action'] == 'save') {
	//   $isProcess = (isset($_REQUEST['isCase']) && ($_REQUEST['isCase'] === "true" || $_REQUEST['isCase'] === true))? 0 : 1;
	//   $name = mysql_real_escape_string($_REQUEST['name']);
	//   mysql_query("INSERT INTO `process` (`name`, `isProcess`) VALUES ('" . $name . "', '" . $isProcess . "');");
	//   $return['id'] = mysql_insert_id();
	//   $return['isCase'] = $_REQUEST['isCase'];
	//   $return['name'] = $_REQUEST['name'];
	//   $return['graphID'] = $_REQUEST['name'];


	// } elseif (isset($_REQUEST['processid'])) {
	//   $procs = mysql_query("SELECT * FROM `process` WHERE `ID` LIKE '" . $_REQUEST['processid'] . "'");
	//   if ($_REQUEST['action'] == "getname") {
	//     if (mysql_num_rows($procs) == 1) {
	//       $line = mysql_fetch_array($procs, MYSQL_ASSOC);
	//       $return['name'] = $line['name'];
	//       $return['code'] = "ok";
	//     } else {
	//       $return['code'] = "error";
	//     }
	//   } elseif ($_REQUEST['action'] == 'remove') {
	//     if (mysql_num_rows($procs) > 0) {
	//       mysql_query("DELETE FROM `process` WHERE `ID` LIKE '" . $_REQUEST['processid'] . "'");
	//       $return['code'] = "removed";
	//     } else {
	//       $return['code'] = "error";
	//     }
	//   } elseif (($_REQUEST['action'] == 'save') && isset($_REQUEST['graph']) && ($subjects != "-1")) {
	//     $graph = $_REQUEST['graph'];
	//     $lowestTS = mysql_query("SELECT `date` FROM `process_graphs` WHERE `processID` = " . $_REQUEST['processid'] . " ORDER BY `date` DESC");
	//     if (mysql_num_rows($lowestTS) > 14) {
	//       $counter = 0;
	//       while ($row = mysql_fetch_array($lowestTS)) {
	//         $counter = $counter + 1;
	//         if($counter == 13) {
	//         mysql_query("DELETE FROM `process_graphs` WHERE `processID` = " . $_REQUEST['processid'] . " AND `date` <= '" . $row[0] . "'");
	//         }
	//       }


	//     }
	//     mysql_query("INSERT INTO `process_graphs` (`graph`) VALUES ('" . $graph . "')");
	//     $saveID = mysql_insert_id();
	//     mysql_query("UPDATE `process` SET `graphID` = '" . mysql_insert_id() . "', `startSubjects` = '" . $subjects . "' WHERE `ID` = " . $_REQUEST['processid']);
	//     mysql_query("UPDATE `process_graphs` SET `processID` = '" . $_REQUEST['processid'] . "' WHERE `ID` = " . $saveID);
	//     $return['code'] = "ok";

	//   } elseif (($_REQUEST['action'] == 'showStamps')) {
	//     $procs = mysql_query("SELECT * FROM `process_graphs` WHERE `processID` LIKE '" . $_REQUEST['processid'] . "'");
	//     $processes = array();
	//     while ($process = mysql_fetch_array($procs, MYSQL_ASSOC)) {
	//       $processes[] = $process['date'];
	//     }
	//     $return['stamps'] = $processes;
	//     $return['code'] = "ok";

	//   } elseif ($_REQUEST['action'] == 'load') {
	//     if (mysql_num_rows($procs) == 1) {
	//       $proc = mysql_fetch_array($procs, MYSQL_ASSOC);
	//       $return['id'] = $proc['ID'];
	//       $return['name'] = $proc['name'];
	//       $return['graphid'] = $proc['graphID'];
	//       $result = mysql_query("SELECT * from `process_graphs` WHERE `ID` LIKE '" . $return['graphid'] . "'");
	//       if (mysql_num_rows($result) == 1) {
	//         $graph = mysql_fetch_array($result, MYSQL_ASSOC);
	//         $return['graph'] = $graph['graph'];
	//       }
	//       $return['code'] = "ok";
	//     } else {
	//       $return['code'] = "error";
	//     }
	//   } elseif ($_REQUEST['action'] == 'loadHistory') {
	//     $result = mysql_query("SELECT * from `process_graphs` WHERE `date` LIKE '" . $_REQUEST['stamp'] . "' AND `processID` LIKE '" . $_REQUEST['processid'] . "'");
	//     if (mysql_num_rows($result) == 1) {
	//       $graph = mysql_fetch_array($result, MYSQL_ASSOC);
	//       $return['graph'] = $graph['graph'];
	//     }
	//     $return['code'] = "ok";

	//   }
	// } elseif ($_REQUEST['action'] == "getallprocesses") {
	//   $result = mysql_query("SELECT * FROM `process` ORDER BY graphID " . mysql_real_escape_string($limit) . "");
	//   $processes = array();
	//   while ($process = mysql_fetch_array($result, MYSQL_ASSOC)) {
  //     array_push($processes, array("name" => $process['name'], "isProcess" => 
  //       $process['isProcess'], "displayName" => (($process['isProcess'] == "0") 
  //       ? "[C] " : "[P] ") . $process['name']));
	//   }
	//   $return['processes'] = $processes;
	//   $return['code'] = "ok";
	// } elseif ($_REQUEST['action'] == "getallprocessesids") {
	//   $result = mysql_query("SELECT * FROM `process` ORDER BY graphID " . mysql_real_escape_string($limit) . "");
	//   $processes = array();
	//   while ($process = mysql_fetch_array($result, MYSQL_ASSOC)) {
	//     array_push($processes, $process['ID']);
	//   }
	//   $return['ids'] = $processes;
	//   $return['code'] = "ok";
	// } elseif (($_REQUEST['action'] == "getallstartable") && ($userid >= 0)) {
	//   $resultgroups = mysql_query("SELECT * FROM `users_x_groups` WHERE `userID` LIKE '" . $userid . "'");
	//   $query = "";
	//   while ($group = mysql_fetch_array($resultgroups, MYSQL_ASSOC)) {
	//     $query .= (($query == "") ? "" : " OR ") . "`startSubjects` LIKE '%" . $group['groupID'] . "%'";
	//   }
	//   if ($query != "") {
	//     $result = mysql_query("SELECT * FROM `process` WHERE " . $query);
	//     $processes = array();
	//     while ($process = mysql_fetch_array($result, MYSQL_ASSOC)) {
	//       array_push($processes, $process['ID']);
	//     }
	//     $return['ids'] = $processes;
	//   }
	//   $return['code'] = "ok";
	// }

	if (!empty($return)) {
		if ( sizeof($return) == 0 ) {
			echo "{}";
		} else {
			echo json_encode($return);
		}
	} else {
		 echo "{}";
	}
}
?>
