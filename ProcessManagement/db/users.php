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

if (isset($_REQUEST['action'])) {
	$return = array();

	if (isset($_REQUEST['username']) && ($_REQUEST['username'] != "")) {
		$usersq = mysql_query("SELECT * FROM `users` WHERE `name` LIKE '" . $_REQUEST['username'] . "'");
		if ($_REQUEST['action'] == 'add') {
			if (mysql_num_rows($usersq) == 0) {
				mysql_query("INSERT INTO `users` (`name`) VALUES ('" . $_REQUEST['username'] . "');");
				$return['id'] = mysql_insert_id();
				$return['code'] = "added";
			} else {
				$return['code'] = "error";
			}
		} elseif ($_REQUEST['action'] == "getid") {
			if (mysql_num_rows($usersq) == 1) {
				$line = mysql_fetch_array($usersq, MYSQL_ASSOC);
				$return['id'] = $line['ID'];
				$return['code'] = "ok";
			} else {
				$return['code'] = "error";
			}
		}
	} elseif (isset($_REQUEST['users'])) {

		/*
		 * 	users = [
		 * 		{id, name, active, roles : [roleId1, ...]},
		 * 		...
		 * 	]
		 */
		$users = $_REQUEST['users'];

		if ($_REQUEST['action'] == 'save') {

			foreach ($users as $user) {

				error_log(var_export($user, true));

				// insert/update user
				if (array_key_exists('groupID', $user)){			
				mysql_query("INSERT INTO `users` (`name`, `groupID`,`inputpoolsize`) VALUES (" . $user['userName'] . ", " . $user['groupID'] . ", " . $user['inputpoolsize'] . ")");
				}else{
				mysql_query("INSERT INTO `users` (`name`, `inputpoolsize`) VALUES (" . $user['userName'] . ", " . $user['inputpoolsize'] . ")");
				}
			}

		$result = mysql_query("SELECT * FROM `users`");
		$users = array();
		while ($user = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push($users, $user);
		}

		$return['users'] = $users;
		$return['code'] = "ok";

		}

	} elseif (isset($_REQUEST['userid'])) {
		if ($_REQUEST['action'] == "getname") {
			$result = mysql_query("SELECT * FROM `users` WHERE `ID` LIKE '" . $_REQUEST['userid'] . "'");
			if (mysql_num_rows($result) > 0) {
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['name'] = $line['name'];
				$return['code'] = "ok";
			} else {
				$return['code'] = "error";
			}
		} elseif ($_REQUEST['action'] == 'remove') {

			mysql_query("DELETE FROM `users_x_groups` WHERE `userID` = " . $_REQUEST['userid']);

			mysql_query("DELETE FROM `users` WHERE `id` = " . $_REQUEST['userid']);

			$affectedRows = mysql_affected_rows($link);

			mysql_query("DELETE FROM `relation` WHERE `responsibleID` = " . $_REQUEST['userid']);

			if ($affectedRows > 0) {
				$return['code'] = "removed";
			} else {
				$return['code'] = "error";
			}
		} elseif ($_REQUEST['action'] == 'getallgroupsbyuserid') {

			$result = mysql_query("SELECT groups.id, groups.name, groups.active
								FROM users_x_groups 
								RIGHT JOIN users ON users.id = users_x_groups.userID 
								LEFT JOIN groups ON groups.id = users_x_groups.groupID
								WHERE users.id = " . $_REQUEST['userid']);

			$groups = array();

			while ($group = mysql_fetch_array($result, MYSQL_ASSOC)) {
				array_push($groups, $group);
			}

			$return['groups'] = $groups;
			$return['code'] = "ok";

		}
	} elseif ($_REQUEST['action'] == "getallusers") {// deprecated
		$result = mysql_query("SELECT * FROM `users`");
		$users = array();
		while ($user = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push($users, $user['name']);
		}
		$return['users'] = $users;
		$return['code'] = "ok";
	} elseif ($_REQUEST['action'] == "getall") {
		$result = mysql_query("SELECT * FROM `users`");
		$users = array();
		while ($user = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push($users, $user);
		}

		$return['users'] = $users;
		$return['code'] = "ok";
	}elseif ($_REQUEST['action'] == "getAllUsersAndGroups"){
		$result = mysql_query("	SELECT groups.name as groupName, groups.ID as groupID, users.name as userName, users.ID as userID FROM (groups INNER JOIN group_x_users ON groups.ID = group_x_users.groupID) INNER JOIN users ON group_x_users.userID = users.ID");
		$users = array();
		while ($user = mysql_fetch_array($result, MYSQL_ASSOC)){
			if(array_key_exists($user['userID'], $users)){
				array_push($users[$user['userID']]['groupName'], $user['groupName']);
				array_push($users[$user['userID']]['groupID'], $user['groupID']);
				}
			else{
				$users[$user['userID']] = $user;
				$users[$user['userID']]['groupName'] = array($user['groupName']);
				$users[$user['userID']]['groupID'] = array($user['groupID']);
		}
		}
		$return['users'] = $users;
		$return['code']   = "ok";
	}

	if (!empty($return))
		echo json_encode($return);
}
?>