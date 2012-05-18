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

	if (isset($_REQUEST['groupname'])){
		
		$groupsq = mysql_query("SELECT * FROM `groups` WHERE `name` LIKE '". $_REQUEST['groupname'] ."'");
		if ($_REQUEST['action'] == 'add'){
			if (mysql_num_rows($groupsq) == 0){
				mysql_query("INSERT INTO `groups` (`name`) VALUES ('". $_REQUEST['groupname'] ."');");
				$return['id']   = mysql_insert_id();
				$return['code'] = "added";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == 'remove'){
			if (mysql_num_rows($groupsq) > 0){
				while ($group = mysql_fetch_array($groupsq, MYSQL_ASSOC))
					mysql_query("DELETE FROM `users_x_groups` WHERE `groupID` LIKE '". $group['ID'] ."'");
				mysql_query("DELETE FROM `groups` WHERE `name` LIKE '". $_REQUEST['groupname'] ."'");
				$return['code'] = "removed";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getid"){
			if (mysql_num_rows($groupsq) == 1){
				$line = mysql_fetch_array($groupsq, MYSQL_ASSOC);
				$return['id']   = $line['ID'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}
	}elseif (isset($_REQUEST['groupid'])){
		if ($_REQUEST['action'] == "getname"){
			$result = mysql_query("SELECT * FROM `groups` WHERE `ID` LIKE '". $_REQUEST['groupid'] ."'");
			if (mysql_num_rows($result) > 0){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['name'] =  $line['name'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getallusers"){
			$result = mysql_query("SELECT * FROM `users_x_groups` WHERE `groupID` LIKE '". $_REQUEST['groupid'] ."'");
			$users = array();
			while ($user = mysql_fetch_array($result, MYSQL_ASSOC)){
				array_push($users, $user['userID']);
			}
			$return['users'] = $users;
			$return['code']   = "ok";
		}
	}elseif ($_REQUEST['action'] == "getallgroups"){
		$result = mysql_query("SELECT * FROM `groups`");
		$groups = array();
		while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($groups, $group['name']);
		}
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}

	if (!empty($return))
			echo json_encode($return);
}
?>