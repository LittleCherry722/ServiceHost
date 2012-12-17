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
$limit = (isset($_REQUEST['limit'])) ? "LIMIT 0," . $_REQUEST['limit'] : "";

if (isset($_REQUEST['action'])) {
	$return = array();
	$action = $_REQUEST['action'];

	if ($action == 'all') {
		$result = mysql_query("SELECT * FROM `process_graphs` ORDER BY `ID`");
		$graphs = array();
		while ($graph = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push( $graphs, array( "id" => $graph['ID'], "graphString" =>
				$graph['graph'], "date" => $graph['date'],
				"processID" => $graph['processID'] ) );
		}
		$return = $graphs;

		// Create a new graph
	} elseif ($action == 'create') {
		$graph = mysql_real_escape_string($_REQUEST['graphString']);
		$date = mysql_real_escape_string($_REQUEST['date']);
		$processID = mysql_real_escape_string($_REQUEST['processID']);
		mysql_query("INSERT INTO `process_graphs` (`graph`, `date`, `processID`) VALUES ('" . $graph . "', '" . $date . "', '" . $processID . "');");
		$return['id'] = mysql_insert_id();
		$return['graphString'] = $_REQUEST['graphString'];
		$return['date'] = $_REQUEST['date'];
		$return['processID'] = $_REQUEST['processID'];

	// destroy an existing graph
	} elseif ($action == 'destroy') {
		$graphs = mysql_query("SELECT * FROM `process_graphs` WHERE `ID` LIKE '" . $_REQUEST['id'] . "'");
		if (mysql_num_rows($graphs) > 0) {
			mysql_query("DELETE FROM `process_graphs` WHERE `ID` LIKE '" . $_REQUEST['id'] . "'");
			$return['code'] = "removed";
		} else {
			$return['code'] = "error";
		}

	// save existing graph
	} elseif ($action == 'save') {
		$id = mysql_real_escape_string($_REQUEST['id']);
		$graph = mysql_real_escape_string($_REQUEST['graphString']);
		$date = mysql_real_escape_string($_REQUEST['date']);
		$processID = mysql_real_escape_string($_REQUEST['processID']);
    mysql_query("UPDATE `process_graphs` SET `graph` = '" . $graph . "', `date` = '" . $date . "', `processID = '" . $processID . "'` WHERE `ID` = " . $id);
		$return['id'] = $_REQUEST['id'];
		$return['graphString'] = $_REQUEST['graphString'];
		$return['date'] = $_REQUEST['date'];
		$return['processID'] = $_REQUEST['processID'];
	}

	if (!empty($return)){
		echo json_encode($return);
	}
}
?>
