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


session_start();

include("include/dbconnect.php");

$return = array();
if (isset($_REQUEST['username'])){

	$result = mysql_query("SELECT users.id, users.name, GROUP_CONCAT( groups.name SEPARATOR  ',' ) AS roles, users.active
									FROM users_x_groups 
									RIGHT JOIN users ON users.id = users_x_groups.userID 
									LEFT JOIN groups ON groups.id = users_x_groups.groupID 
									WHERE users.name = '".mysql_escape_string($_REQUEST['username'])."'
									GROUP BY users.id");
	if (mysql_num_rows($result) == 1){
		$user = mysql_fetch_array($result, MYSQL_ASSOC);
		$_SESSION['user']   = $user['name'];
		$_SESSION['userid'] = $user['id'];
		$return['code'] = 'ok';
		$user['roles'] = explode(",", $user['roles']);
		$return['user'] = $user;
	}else{
		$_SESSION['user'] = '';
		$return['code'] = 'not found';
	}
}elseif (!isset($_SESSION['userid'])){
	$return['code'] = 'no login';
}

if (!empty($return)){
	echo json_encode($return);
	exit();
}
?>