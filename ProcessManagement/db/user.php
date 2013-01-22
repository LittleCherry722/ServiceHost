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

include("include/dbconnect.php");

if (isset($_REQUEST['action'])) {
	$return = array();
	$action = $_REQUEST['action'];

	if ($action == 'all') {
		$query = mysql_query("SELECT * FROM `users` ORDER BY `ID`");
		$results = array();
		while ($result = mysql_fetch_array($query, MYSQL_ASSOC)) {
			array_push( $results, array(
				"id" => $result['ID'],
				"name" => $result['name'],
				"isActive" => ($result['active'] == "1"),
				"inputPoolSize" => $result['inputpoolsize']
			));
		}
		$return = $results;

		// Create a new graph
	} elseif ($action == 'create') {
		$attr_name = mysql_real_escape_string($_REQUEST['name']);
		$attr_active = ($_REQUEST['isActive'] == "true" )? 1 : 0;
		$attr_input_pool_size = mysql_real_escape_string($_REQUEST['inputPoolSize']);
		mysql_query("INSERT INTO `users` ( `name`, `active`, `inputpoolsize` ) VALUES ( '" . $attr_name . "', '" . $attr_active . "', '" . $attr_input_pool_size . "' );");
		$return['id'] = mysql_insert_id();
		$return['name'] = $_REQUEST['name'];
		$return['isActive'] = $_REQUEST['isActive'];
		$return['inputPoolSize'] = $_REQUEST['inputPoolSize'];

	// destroy an existing graph
	} elseif ($action == 'destroy') {
		$attr_id = mysql_real_escape_string($_REQUEST['id']);
		$results = mysql_query("SELECT * FROM `users` WHERE `ID` LIKE '" . $attr_id . "'");
		if (mysql_num_rows($results) > 0) {
			mysql_query("DELETE FROM `users` WHERE `ID` LIKE '" . $attr_id . "'");
			$return['code'] = "removed";
		} else {
			$return['code'] = "error";
		}

	// save existing user
	} elseif ($action == 'save') {
		$attr_id = mysql_real_escape_string($_REQUEST['id']);
		$attr_name = mysql_real_escape_string($_REQUEST['name']);
		$attr_active = ($_REQUEST['isActive'] == "true" )? 1 : 0;
		$attr_input_pool_size = mysql_real_escape_string($_REQUEST['inputPoolSize']);
    mysql_query("UPDATE `users` SET `name` = '" . $attr_name . "', `active` = " . $attr_active . ", `inputpoolsize` = '" . $attr_input_pool_size . "' WHERE `ID` = " . $attr_id);
		$return['id'] = $_REQUEST['id'];
		$return['name'] = $_REQUEST['name'];
		$return['isActive'] = $_REQUEST['isActive'];
		$return['inputPoolSize'] = $_REQUEST['inputPoolSize'];
	}

	if (empty($return) || sizeof($return) == 0 ) {
		echo "{}";
	} else {
		echo json_encode($return);
	}
}

?>
