<?php

include("include/dbconnect.php");


$action     = (isset($_REQUEST['action'])) ? $_REQUEST['action'] : "" ; 
if ($action == "") return;// no action = abort
$instanceid = (isset($_REQUEST['instanceid'])) ? $_REQUEST['instanceid'] : -1;
$processid  = (isset($_REQUEST['processid'])) ? $_REQUEST['processid'] : -1;
$userid  = (isset($_REQUEST['userid'])) ? $_REQUEST['userid'] : -1;
$involvedusers  = (isset($_REQUEST['involvedusers'])) ? $_REQUEST['involvedusers'] : "";

$return = array();

$error = true;

//echo "action: ". $action ."<br>proc: ". $processid ."<br>ins: ". $instanceid;

if ( ($action == 'new') && ($processid > 0)){
	$processResult = mysql_query("SELECT * FROM `process` WHERE `ID` LIKE '". $processid ."'");
	if (mysql_num_rows($processResult) == 1){
		$process = mysql_fetch_array($processResult, MYSQL_ASSOC);
		mysql_query("INSERT INTO `process_instance` (`processID`, `graphID`) VALUES ('". $processid ."','". $process['graphID'] ."')");
		$return['id'] = mysql_insert_id();
		$error = false;
	}
}elseif ( ($action == 'load') && ($instanceid > 0) ){
	$instanceResult = mysql_query("SELECT * FROM `process_instance` WHERE `ID` LIKE '". $instanceid ."'");
	if (mysql_num_rows($instanceResult) == 1){
		$instance = mysql_fetch_array($instanceResult, MYSQL_ASSOC);
		$return['data'] = $instance['data'];
		$error = false;
	}
}elseif ( ($action == 'save') && ($instanceid > 0) && (isset($_REQUEST['data'])) ){
	mysql_query("UPDATE `process_instance` SET `data` = '". $_REQUEST['data'] ."', `involvedUsers` = '". $involvedusers ."' WHERE `ID` LIKE '". $instanceid ."'");
	$error = false;
}elseif ( ($action == 'graph') && ($instanceid > 0) ){
	$instanceResult = mysql_query("SELECT * FROM `process_instance` WHERE `ID` LIKE '". $instanceid ."'");
	if (mysql_num_rows($instanceResult) == 1){
		$instance = mysql_fetch_array($instanceResult, MYSQL_ASSOC);
		$graphResult = mysql_query("SELECT * FROM `process_graphs` WHERE `ID` LIKE '". $instance['graphID'] ."'");
		if (mysql_num_rows($graphResult) == 1){
			$graph = mysql_fetch_array($graphResult, MYSQL_ASSOC);
			$return['graph'] = $graph['graph'];
			$error = false;
		}
	}
}elseif ( ($action == "getallinstances") ) {
	if ($processid > 0)
		$result = mysql_query("SELECT * FROM `process_instance` WHERE `processID` LIKE '". $processid ."'");
	elseif ( $userid >= 0)
		$result = mysql_query("SELECT * FROM `process_instance` WHERE `involvedUsers` LIKE '%". $userid ."%'");
		
	else
		$result = mysql_query("SELECT * FROM `process_instance`");
	$instances = array();
	while ($instance = mysql_fetch_array($result, MYSQL_ASSOC)){
		array_push($instances, $instance['ID']);
	}
	$return['instances'] = $instances;
	$error = false;
}elseif ( ($action == "delete") && ($instanceid >= 0) ){
	mysql_query("DELETE FROM `process_instance` WHERE `ID` LIKE '". $instanceid ."'");
	$error = false;
}elseif ( ($action == "getprocess") && ($instanceid >= 0) ){
	$instanceResult = mysql_query("SELECT * FROM `process_instance` WHERE `ID` LIKE '". $instanceid ."'");
	if (mysql_num_rows($instanceResult) == 1){
		$instance = mysql_fetch_array($instanceResult, MYSQL_ASSOC);
		$return['id'] = $instance['processID'];
		$error = false;
	}
}

$return['code'] = ($error) ? 'error' : 'ok' ;

if (!empty($return))
	echo json_encode($return);
?>
