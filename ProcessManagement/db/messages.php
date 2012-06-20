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


$action     = (isset($_REQUEST['action'])) ? $_REQUEST['action'] : "" ; 
if ($action == "") return;// no action = abort
$msgid      = (isset($_REQUEST['msgid']))      ? $_REQUEST['msgid']      : -1;
$instanceid = (isset($_REQUEST['instanceid'])) ? $_REQUEST['instanceid'] : -1;
$from       = (isset($_REQUEST['from']))       ? $_REQUEST['from']       : -1;
$to         = (isset($_REQUEST['to']))         ? $_REQUEST['to']         : -1;
$data       = (isset($_REQUEST['data']))       ? $_REQUEST['data']       : "{}"; 
$read       = (isset($_REQUEST['read']))       ? $_REQUEST['read']       : -1; 
$userid		= (isset($_REQUEST['userid']))     ? $_REQUEST['userid']     : -1; 


$return = array();

$error = true;

switch ($action){
	case "send":
		if (($instanceid < 0)||($from < 0)||($to < 0)) break;
		
		mysql_query("INSERT INTO `messages` (`from`, `to`, `instanceID`, `data`) VALUES ('". $from ."','". $to ."','". $instanceid ."','". $data ."')"); 
		$return['id'] = mysql_insert_id();
		$error = ($return['id'] <= 0);
		break;
	case "get":
		$query = "";
		if ($instanceid >= 0) $query .= ($query ==""? "" : " AND ") . "`instanceID` LIKE '". $instanceid ."'";
		if ($from       >= 0) $query .= ($query ==""? "" : " AND ") . "`from` LIKE '". $from ."'";
		if ($to         >= 0) $query .= ($query ==""? "" : " AND ") . "`to` LIKE '". $to ."'";
		if ($read       >= 0) $query .= ($query ==""? "" : " AND ") . "`read` LIKE '". $read ."'";
		if ($msgid      >= 0) $query .= ($query ==""? "" : " AND ") . "`ID` LIKE '". $msgid ."'";
		
		$query = "SELECT * FROM `messages`". ($query == "" ? "" : " WHERE ". $query );

		$result = mysql_query($query);
		$msgs = array();
		while ($msg = mysql_fetch_array($result, MYSQL_ASSOC)){
			$msgdata = array();
			$msgdata['id']         = $msg['ID'];
			$msgdata['from']       = $msg['from'];
			$msgdata['to']         = $msg['to'];
			$msgdata['instanceid'] = $msg['instanceID'];
			$msgdata['read']       = $msg['read'];
			$msgdata['data']       = $msg['data'];
			array_push($msgs, $msgdata);
		}
		$return['msgs'] = $msgs;
		$error = false;
		break;
	case "setread":
		if ($msgid < 0) break;
		
		mysql_query("UPDATE `messages` SET `read` = 1 WHERE `ID` = ". $msgid);
		$error = false;
		break;
	case "count":
		if($userid < 0) 
			break;
		
		$result = mysql_query("SELECT count(*) AS count FROM messages WHERE messages.read = 0 AND messages.to = ".$userid);
		
		$count = mysql_fetch_array($result, MYSQL_ASSOC);
		
		error_log(var_export($count, true));
		
		$return['count'] = $count['count']['count'];
		
		$error = false;
		break;
}

$return['code'] = ($error) ? 'error' : 'ok' ;

if (!empty($return))
	echo json_encode($return);
?>