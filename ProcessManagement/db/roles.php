<?php

/*
 * S-BPM roleware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Thorsten Jacobi, Telecooperation role @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

include("include/dbconnect.php");

if (isset($_REQUEST['action'])){
	$return = array();

	error_log(isset($_REQUEST['roleid']));

	if (isset($_REQUEST['rolename'])){
		
		if ($_REQUEST['action'] == 'getbyname'){
			$result = mysql_query("SELECT * FROM `roles` WHERE `name` = '". $_REQUEST['rolename'] ."'");
			
			if (mysql_num_rows($result) == 1){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['role']   = $line;
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}else{
		
			$rolesq = mysql_query("SELECT * FROM `roles` WHERE `name` LIKE '". $_REQUEST['rolename'] ."'");
			if ($_REQUEST['action'] == 'add'){
				if (mysql_num_rows($rolesq) == 0){
					mysql_query("INSERT INTO `roles` (`name`) VALUES ('". $_REQUEST['rolename'] ."');");
					$return['id']   = mysql_insert_id();
					$return['code'] = "added";
				}else{
					$return['code'] = "error";
				}
			}elseif ($_REQUEST['action'] == "getid"){
				if (mysql_num_rows($rolesq) == 1){
					$line = mysql_fetch_array($rolesq, MYSQL_ASSOC);
					$return['id']   = $line['ID'];
					$return['code'] = "ok";
				}else{
					$return['code'] = "error";
				}
			}
		
		}
	}elseif (isset($_REQUEST['roleid'])){
		if ($_REQUEST['action'] == 'getbyid'){
			$result = mysql_query("SELECT * FROM `roles` WHERE `id` = '". $_REQUEST['roleid'] ."'");
			
			if (mysql_num_rows($result) == 1){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['role']   = $line;
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getname"){
			$result = mysql_query("SELECT * FROM `roles` WHERE `ID` LIKE '". $_REQUEST['roleid'] ."'");
			if (mysql_num_rows($result) > 0){
				$line = mysql_fetch_array($result, MYSQL_ASSOC);
				$return['name'] =  $line['name'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getallusers"){
			$result = mysql_query("SELECT * FROM `users_x_roles` WHERE `roleID` LIKE '". $_REQUEST['roleid'] ."'");
			$users = array();
			while ($user = mysql_fetch_array($result, MYSQL_ASSOC)){
				array_push($users, $user['userID']);
			}
			$return['users'] = $users;
			$return['code']   = "ok";
		}elseif ($_REQUEST['action'] == 'remove'){
	
				mysql_query("DELETE FROM `group_x_roles` WHERE `roleID` = '". $_REQUEST['roleid'] ."'");
				
				mysql_query("DELETE FROM `roles` WHERE `ID` = '". $_REQUEST['roleid'] ."'");
				
				if(mysql_affected_rows() > 0){
					$return['code'] = "removed";
				}else{
					$return['code'] = "error";
				}
				
		}
	}elseif (isset($_REQUEST['roles'])){
		
		/*
		 * 	users = [
		 * 		{id, name, active, roles : [roleId1, ...]},
		 * 		...
		 * 	]
		 */		 
		$roles = $_REQUEST['roles'];
		
		if ($_REQUEST['action'] == 'save') {
			
		mysql_query("TRUNCATE TABLE group_x_roles;");

			foreach ($roles as $role) {

				error_log(var_export($role, true));

				// update role
				if (is_numeric($role['roleID'])){
					
				mysql_query("UPDATE `roles` Set `roles.name` = '" . $role['roleName'] . "' WHERE `roles.ID` = '" . $role['roleID'] .  "' )");
					$groups = $role['groupID'];
					foreach ($groups as $group) {					
						if(! (is_null($group))){
						mysql_query("INSERT INTO `group_x_roles` (`roleID` , `groupID`) VALUES ( '" . $role['roleID'] . "' , '" . $group . "' )");
						}
					}
					
				}else{
				// insert role	
				mysql_query("INSERT INTO `roles` (`name`) VALUES ( '" . $role['roleName'] . "' )");
				if(key_exists('groupID', $role)){
					$groups = $role['groupID'];
					foreach ($groups as $group) {					
					if(! (is_null($group))){
					mysql_query("INSERT INTO `group_x_roles` (`roleID` , `groupID`) VALUES ( '" . mysql_insert_id() . "' , '" . $group . "' )");
						}
					}
				}
				}
									
			}	

			$result = mysql_query("SELECT * FROM `roles`");
			$roles = array();
			while ($role = mysql_fetch_array($result, MYSQL_ASSOC)){
				array_push($roles, $role);
			}
			$return['roles'] = $roles;
			$return['code']   = "ok";	
		}

	}elseif ($_REQUEST['action'] == "getallroles"){ // deprecated
		$result = mysql_query("SELECT * FROM `roles`");
		$roles = array();
		while ($role = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($roles, $role['name']);
		}
		$return['roles'] = $roles;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getall"){
		$result = mysql_query("SELECT * FROM `roles`");
		$roles = array();
		while ($role = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($roles, $role);
		}
		$return['roles'] = $roles;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getallrolesandusers"){
		$result = mysql_query("	SELECT roles.name as roleName, users.name as userName FROM roles, users_x_roles, users WHERE roles.id = users_x_roles.roleid AND users.id = users_x_roles.userid ORDER BY roleName, userName");
		$roles = array();
		while ($role = mysql_fetch_array($result, MYSQL_ASSOC)){
			if(array_key_exists($role['roleName'], $roles))
				array_push($roles[$role['roleName']], $role['userName']);
			else
				$roles[$role['roleName']] = array($role['userName']);
		}
		$return['roles'] = $roles;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getallrolesandgroups"){
		$result = mysql_query("	SELECT groups.name as groupName, groups.ID as groupID, roles.name as roleName, roles.ID as roleID FROM (groups right JOIN group_x_roles ON groups.ID = group_x_roles.groupID) right JOIN roles ON group_x_roles.roleID = roles.ID");
		$roles = array();
		while ($role = mysql_fetch_array($result, MYSQL_ASSOC)){
			if(array_key_exists($role['roleID'], $roles)){
				array_push($roles[$role['roleID']]['groupName'], $role['groupName']);
				array_push($roles[$role['roleID']]['groupID'], $role['groupID']);
				}
			else{
				$roles[$role['roleID']] = $role;
				$roles[$role['roleID']]['groupName'] = array($role['groupName']);
				$roles[$role['roleID']]['groupID'] = array($role['groupID']);
		}
		}
		$return['roles'] = $roles;
		$return['code']   = "ok";
	}
	
	


	if (!empty($return))
			echo json_encode($return);
}
?>