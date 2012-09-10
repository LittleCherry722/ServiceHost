<?php

/*
 * S-BPM Groupware v0.8
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

$query = "";
$return = array();

$action = $_REQUEST['action'];

switch ($action){
	case "read":		
	
		$result = mysql_query("SELECT * FROM `configuration`");
		
		$configuration = array();
		
		while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push($configuration, $row);
		}
		
		$return['configuration'] = $configuration;
		
		break;
	case "write":
		
		if(!isset($_REQUEST['configuration']))
			break;

		$configuration = $_REQUEST['configuration'];
		
		if(is_array($configuration))
			foreach ($configuration as $row) 
				mysql_query("INSERT INTO `configuration` (`key`, `label`, `value`, `type`) 
							 VALUES (" . $row['key'] . ", '" . $row['label'] . "', '" . $row['value'] . "', '" . $row['type'] . "') 
							 ON DUPLICATE KEY UPDATE label = '" . $row['label'] . "', value = " . $row['value'] .", type = " . $row['type']);

		break;
}

$rows = mysql_affected_rows($link);

$return['error'] = mysql_error($link);

$return['code'] = ($rows < 1 && !empty($return['error'])) ? 'error' : 'ok' ;
		
echo json_encode($return);

?>