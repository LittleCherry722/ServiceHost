<?php

include("include/dbconnect.php");


$action     = (isset($_REQUEST['action'])) ? $_REQUEST['action'] : "" ; 
if ($action == "") return;// no action = abort
$msgid      = (isset($_REQUEST['msgid']))      ? $_REQUEST['msgid']      : -1;
$instanceid = (isset($_REQUEST['instanceid'])) ? $_REQUEST['instanceid'] : -1;
$from       = (isset($_REQUEST['from']))       ? $_REQUEST['from']       : -1;
$to         = (isset($_REQUEST['to']))         ? $_REQUEST['to']         : -1;
$data       = (isset($_REQUEST['data']))       ? $_REQUEST['data']       : "{}"; 
$read       = (isset($_REQUEST['read']))       ? $_REQUEST['read']       : -1; 

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
		$query = "SELECT * FROM `messages`". ($query == "" ? "" : " WHERE ". $query);

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
		
		mysql_query("UPDATE `messages` SET `read` = '1' WHERE `ID` LIKE '". $msgid ."'");
		$error = false;
		break;
}

$return['code'] = ($error) ? 'error' : 'ok' ;

if (!empty($return))
	echo json_encode($return);
?>