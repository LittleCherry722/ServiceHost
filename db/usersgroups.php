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

if (isset($_REQUEST['userid']) && isset($_REQUEST['action'])){
	$return = array();
	
	if (isset($_REQUEST['groupid'])){
		if ($_REQUEST['action'] == 'addgroup'){
			mysql_query("INSERT INTO `users_x_groups` (`userID`,`groupID`) VALUES ('". $_REQUEST['userid'] ."','". $_REQUEST['groupid'] ."');");
			$return['id']   = mysql_insert_id();
			$return['code'] = "added";
		}elseif ($_REQUEST['action'] == 'removegroup'){
			mysql_query("DELETE FROM `users_x_groups` WHERE `userid` LIKE '". $_REQUEST['userid'] ."' AND `groupid` LIKE '". $_REQUEST['groupid'] ."'");
			$return['code'] = "removed";
		}elseif (($_REQUEST['action'] == "getrelations") && isset($_REQUEST['processid']) ){
			$result = mysql_query("SELECT * FROM `relation` WHERE `userID` LIKE '". $_REQUEST['userid'] ."' AND `groupID` LIKE '". $_REQUEST['groupid'] ."' AND `processID` LIKE '". $_REQUEST['processid'] ."'");
			$users = array();
			while ($user = mysql_fetch_array($result, MYSQL_ASSOC)){
				array_push($users, $user['responsibleID']);
			}
			$return['users'] = $users;
			$return['code']   = "ok";
		}

		if (isset($_REQUEST['responsibleid']) && isset($_REQUEST['processid']))
			if ($_REQUEST['action'] == 'addrelation'){
				mysql_query("INSERT INTO `relation` (`userID`,`groupID`,`responsibleID`,`processID`) VALUES ('". $_REQUEST['userid'] ."','". $_REQUEST['groupid'] ."','". $_REQUEST['responsibleid'] ."','". $_REQUEST['processid'] . "');");
				$return['id']   = mysql_insert_id();
				$return['code'] = "ok";
			}elseif ($_REQUEST['action'] == 'removerelation'){
				mysql_query("DELETE FROM `relation` WHERE `userid` LIKE '". $_REQUEST['userid'] ."' AND `groupid` LIKE '". $_REQUEST['groupid'] ."' AND `responsibleID` LIKE '". $_REQUEST['responsibleid'] ."' AND `processID` LIKE '". $_REQUEST['processid'] ."'");
				$return['code'] = "removed";
			}
	}elseif ($_REQUEST['action'] == "getgroups"){
		$result = mysql_query("SELECT * FROM `users_x_groups` WHERE `userID` LIKE '". $_REQUEST['userid'] ."'");
		$groups = array();
		while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($groups, $group['groupID']);
		}
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}elseif (($_REQUEST['action'] == "getresponsiblesforuser")&& isset($_REQUEST['processid'])){
		$result = mysql_query("SELECT * FROM `relation` WHERE `userID` LIKE '". $_REQUEST['userid'] ."' AND `processID` LIKE '". $_REQUEST['processid'] ."'");
		$users = array();
		$groups = array();
		while ($user = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($users, $user['responsibleID']);
			array_push($groups, $user['groupID']);
		}
		$return['users'] = $users;
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}
	
	
	if (!empty($return))
		echo json_encode($return);
}
?>