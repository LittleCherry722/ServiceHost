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
