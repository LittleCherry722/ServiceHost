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

if (isset($_REQUEST['action'])){
	$return = array();
	
	if (isset($_REQUEST['username']) && ($_REQUEST['username'] != "")){
		$usersq = mysql_query("SELECT * FROM `users` WHERE `name` LIKE '". $_REQUEST['username'] ."'");
		if ($_REQUEST['action'] == 'add'){
			if (mysql_num_rows($usersq) == 0){
				mysql_query("INSERT INTO `users` (`name`) VALUES ('". $_REQUEST['username'] ."');");
				$return['id']   = mysql_insert_id();
				$return['code'] = "added";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == 'remove'){
			if (mysql_num_rows($usersq) > 0){
				while ($user = mysql_fetch_array($usersq, MYSQL_ASSOC))
					mysql_query("DELETE FROM `users_x_groups` WHERE `userID` LIKE '". $user['ID'] ."'");
				mysql_query("DELETE FROM `users` WHERE `name` LIKE '". $_REQUEST['username'] ."'");
				$return['code'] = "removed";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getid"){
			if (mysql_num_rows($usersq) == 1){
				$line = mysql_fetch_array($usersq, MYSQL_ASSOC);
				$return['id']   = $line['ID'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}
	}elseif (isset($_REQUEST['userid'])){
		if ($_REQUEST['action'] == "getname"){
			$result = mysql_query("SELECT * FROM `users` WHERE `ID` LIKE '". $_REQUEST['userid'] ."'");
			if (mysql_num_rows($result) > 0){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['name'] =  $line['name'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}
	}elseif ($_REQUEST['action'] == "getallusers"){
		$result = mysql_query("SELECT * FROM `users`");
		$users = array();
		while ($user = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($users, $user['name']);
		}
		$return['users'] = $users;
		$return['code']   = "ok";
	}

	if (!empty($return))
		echo json_encode($return);
}
?>