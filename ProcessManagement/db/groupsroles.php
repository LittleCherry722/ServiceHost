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
		$query = mysql_query("SELECT * FROM `group_x_roles`");

		$results = array();
		while ($result = mysql_fetch_array($query, MYSQL_ASSOC)) {
			array_push( $results, array(
				"roleID" => $result['roleID'],
				"groupID" => $result['groupID'],
				"isActive" => $result['active']
			));
		}
		$return = $results;

		// Create a new graph
	} elseif ($action == 'create') {
		$attr_active = ($_REQUEST['isActive'] == "true" );
		$attr_roleID = mysql_real_escape_string($_REQUEST['roleID']);
		$attr_groupID = mysql_real_escape_string($_REQUEST['groupID']);
		mysql_query("INSERT INTO `group_x_roles` ( `roleID`, `groupID`, `active` ) VALUES ( '" . $attr_roleID . "', '" . $attr_groupID . "', '" . $attr_active . "' );");
		$return['roleID'] = $_REQUEST['roleID'];
		$return['groupID'] = $_REQUEST['groupID'];
		$return['isActive'] = $_REQUEST['isActive'];

	// destroy an existing graph
	} elseif ($action == 'destroy') {
		$attr_roleID = mysql_real_escape_string($_REQUEST['roleID']);
		$attr_groupID = mysql_real_escape_string($_REQUEST['groupID']);
		$results = mysql_query("SELECT * FROM `group_x_roles` WHERE `roleID` LIKE '" . $attr_roleID . "' AND `groupID` LIKE '" . $attr_groupID . "'");
		if (mysql_num_rows($results) > 0) {
			mysql_query("DELETE FROM `group_x_roles` WHERE `roleID` LIKE '" . $attr_roleID . "' AND `groupID` LIKE '" . $attr_groupID . "'");
			$return['code'] = "removed";
		} else {
			$return['code'] = "error";
		}

	// save existing user
	} elseif ($action == 'save') {
		$attr_active = ($_REQUEST['isActive'] == "true" );
		$attr_roleID = mysql_real_escape_string($_REQUEST['roleID']);
		$attr_groupID = mysql_real_escape_string($_REQUEST['groupID']);
    mysql_query("UPDATE `group_x_roles` SET `roleID` = '" . $attr_roleID . "', `groupID` = '" . $attr_groupID . "', `active` = '" . $attr_active . "' WHERE `ID` = " . $attr_id);
		$return['roleID'] = $_REQUEST['roleID'];
		$return['groupID'] = $_REQUEST['groupID'];
		$return['isActive'] = $_REQUEST['isActive'];
	}

	if (empty($return) || sizeof($return) == 0 ) {
		echo "{}";
	} else {
		echo json_encode($return);
	}
}

?>
