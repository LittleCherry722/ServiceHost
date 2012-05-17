<?php
session_start();

include("include/dbconnect.php");

$return = array();
if (isset($_REQUEST['username'])){
	$result = mysql_query("SELECT * FROM `users` WHERE `name` LIKE '". $_REQUEST['username'] ."'");
	if (mysql_num_rows($result) == 1){
		$line = mysql_fetch_array($result, MYSQL_ASSOC);
		$_SESSION['user']   = $line['name'];
		$_SESSION['userid'] = $line['ID'];
		$return['code'] = 'ok';
		$return['user']   = $_SESSION['user'];
		$return['userid'] = $_SESSION['userid'];
		
		$result2 = mysql_query("SELECT * FROM `users_x_groups` WHERE `userID` LIKE '". $_SESSION['userid'] ."'");
		$groups = array();
		while ($group = mysql_fetch_array($result2, MYSQL_ASSOC)){
			array_push($groups, $group['groupID']);
		}
		$return['groups'] = $groups;
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