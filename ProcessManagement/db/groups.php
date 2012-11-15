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

if (isset($_REQUEST['action'])){
	$return = array();

	error_log(isset($_REQUEST['groupid']));

	if (isset($_REQUEST['groupname'])){
		
		if ($_REQUEST['action'] == 'getbyname'){
			$result = mysql_query("SELECT * FROM `groups` WHERE `name` = '". $_REQUEST['groupname'] ."'");
			
			if (mysql_num_rows($result) == 1){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['group']   = $line;
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}else{
		
			$groupsq = mysql_query("SELECT * FROM `groups` WHERE `name` LIKE '". $_REQUEST['groupname'] ."'");
			if ($_REQUEST['action'] == 'add'){
				if (mysql_num_rows($groupsq) == 0){
					mysql_query("INSERT INTO `groups` (`name`) VALUES ('". $_REQUEST['groupname'] ."');");
					$return['id']   = mysql_insert_id();
					$return['code'] = "added";
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
		
		}
	}elseif (isset($_REQUEST['groupid'])){
		if ($_REQUEST['action'] == 'getbyid'){
			$result = mysql_query("SELECT * FROM `groups` WHERE `id` = '". $_REQUEST['groupid'] ."'");
			
			if (mysql_num_rows($result) == 1){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['group']   = $line;
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getname"){
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
		}elseif ($_REQUEST['action'] == 'remove'){
	
				mysql_query("DELETE FROM `group_x_roles` WHERE `groupID` = '". $_REQUEST['groupid'] ."'");
				
				mysql_query("DELETE FROM `groups` WHERE `ID` = '". $_REQUEST['groupid'] ."'");
				
				if(mysql_affected_rows() > 0){
					$return['code'] = "removed";
				}else{
					$return['code'] = "error";
				}
				
		}
	}elseif (isset($_REQUEST['groups'])){
		
		/*
		 * 	users = [
		 * 		{id, name, active, roles : [roleId1, ...]},
		 * 		...
		 * 	]
		 */		 
		$groups = $_REQUEST['groups'];
		
		if ($_REQUEST['action'] == 'save') {
			
			foreach ($groups as $group){
																	
			
			
				// insert/update group
				mysql_query("INSERT INTO `groups` (`name`) VALUES (" . $group['name'].")");

			}

			$result = mysql_query("SELECT * FROM `groups`");
			$groups = array();
			while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
				array_push($groups, $group);
			}
			$return['groups'] = $groups;
			$return['code']   = "ok";	
		}

	}elseif ($_REQUEST['action'] == "getallgroups"){ // deprecated
		$result = mysql_query("SELECT * FROM `groups`");
		$groups = array();
		while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($groups, $group['name']);
		}
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getall"){
		$result = mysql_query("SELECT * FROM `groups`");
		$groups = array();
		while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($groups, $group);
		}
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getallrolesandusers"){
		$result = mysql_query("	SELECT groups.name as groupName, users.name as userName FROM groups, users_x_groups, users WHERE groups.id = users_x_groups.groupid AND users.id = users_x_groups.userid ORDER BY groupName, userName");
		$groups = array();
		while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
			if(array_key_exists($group['groupName'], $groups))
				array_push($groups[$group['groupName']], $group['userName']);
			else
				$groups[$group['groupName']] = array($group['userName']);
		}
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getallgroupsandroles"){
		$result = mysql_query("	SELECT groups.name as groupName, groups.ID as groupID, roles.name as roleName, roles.ID as roleID FROM (groups INNER JOIN group_x_roles ON groups.ID = group_x_roles.groupID) INNER JOIN roles ON group_x_roles.roleID = roles.ID");
		$groups = array();
		while ($group = mysql_fetch_array($result, MYSQL_ASSOC)){
			if(array_key_exists($group['groupID'], $groups)){
				array_push($groups[$group['groupID']]['roleName'], $group['roleName']);
				array_push($groups[$group['groupID']]['roleID'], $group['roleID']);
				}
			else{
				$groups[$group['groupID']] = $group;
				$groups[$group['groupID']]['roleName'] = array($group['roleName']);
				$groups[$group['groupID']]['roleID'] = array($group['roleID']);
		}
		}
		$return['groups'] = $groups;
		$return['code']   = "ok";
	}
	
	


	if (!empty($return))
			echo json_encode($return);
}
?>