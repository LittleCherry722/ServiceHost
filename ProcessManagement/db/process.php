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

$userid   = (isset($_REQUEST['userid'])) ? $_REQUEST['userid'] : -1;
$subjects = (isset($_REQUEST['subjects'])) ? $_REQUEST['subjects'] : -1;

if (isset($_REQUEST['action'])){
	$return = array();
	
	if (isset($_REQUEST['processname'])){
		$procs = mysql_query("SELECT * FROM `process` WHERE `name` LIKE '". $_REQUEST['processname'] ."'");
		if ($_REQUEST['action'] == 'new'){
			if (mysql_num_rows($procs) == 0){
				mysql_query("INSERT INTO `process` (`name`) VALUES ('". $_REQUEST['processname'] ."');");
				$return['id']   = mysql_insert_id();
				$return['code'] = "added";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == 'remove'){
			if (mysql_num_rows($procs) > 0){
				mysql_query("DELETE FROM `process` WHERE `name` LIKE '". $_REQUEST['processname'] ."'");
				$return['code'] = "removed";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == "getid"){
			if (mysql_num_rows($procs) == 1){
				$line = mysql_fetch_array($procs, MYSQL_ASSOC);
				$return['id']   = $line['ID'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}
	}elseif (isset($_REQUEST['processid'])){
		$procs = mysql_query("SELECT * FROM `process` WHERE `ID` LIKE '". $_REQUEST['processid'] ."'");
		if ($_REQUEST['action'] == "getname"){
			if (mysql_num_rows($procs) == 1){
				$line = mysql_fetch_array($procs, MYSQL_ASSOC);
				$return['name'] =  $line['name'];
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}elseif ($_REQUEST['action'] == 'remove'){
			if (mysql_num_rows($procs) > 0){
				mysql_query("DELETE FROM `process` WHERE `ID` LIKE '". $_REQUEST['processid'] ."'");
				$return['code'] = "removed";
			}else{
				$return['code'] = "error";
			}
		}elseif ( ($_REQUEST['action'] == 'save') && isset($_REQUEST['graph']) && ($subjects != "-1") ){
			$graph = $_REQUEST['graph'];
			mysql_query("INSERT INTO `process_graphs` (`graph`) VALUES ('". $graph ."')");
			mysql_query("UPDATE `process` SET `graphID` = '". mysql_insert_id() ."', `startSubjects` = '". $subjects ."' WHERE `ID` LIKE '". $_REQUEST['processid'] ."'");
			$return['code'] = "ok";
		}elseif ($_REQUEST['action'] == 'load'){
			if (mysql_num_rows($procs) == 1){
				$proc = mysql_fetch_array($procs, MYSQL_ASSOC);
				$return['id']    = $proc['ID'];
				$return['name']  = $proc['name'];
				$return['graphid'] = $proc['graphID'];
				$result = mysql_query("SELECT * from `process_graphs` WHERE `ID` LIKE '". $return['graphid'] ."'");
				if (mysql_num_rows($result) == 1){
					$graph = mysql_fetch_array($result, MYSQL_ASSOC);
					$return['graph'] = $graph['graph'];
				}
				$return['code'] = "ok";
			}else{
				$return['code'] = "error";
			}
		}
	}elseif ($_REQUEST['action'] == "getallprocesses"){
		$result = mysql_query("SELECT * FROM `process`");
		$processes = array();
		while ($process = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($processes, $process['name']);
		}
		$return['processes'] = $processes;
		$return['code']   = "ok";
	}elseif ($_REQUEST['action'] == "getallprocessesids"){
		$result = mysql_query("SELECT * FROM `process`");
		$processes = array();
		while ($process = mysql_fetch_array($result, MYSQL_ASSOC)){
			array_push($processes, $process['ID']);
		}
		$return['ids'] = $processes;
		$return['code']   = "ok";
	}elseif (($_REQUEST['action'] == "getallstartable") && ($userid >= 0)){
		$resultgroups = mysql_query("SELECT * FROM `users_x_groups` WHERE `userID` LIKE '". $userid ."'");
		$query = "";
		while ($group = mysql_fetch_array($resultgroups, MYSQL_ASSOC)){
			$query .= (($query == "")?"":" OR ") . "`startSubjects` LIKE '%". $group['groupID']."%'";
		}
		if ($query != "") {
			$result = mysql_query("SELECT * FROM `process` WHERE ". $query);
			$processes = array();
			while ($process = mysql_fetch_array($result, MYSQL_ASSOC)){
				array_push($processes, $process['ID']);
			}
			$return['ids'] = $processes;
		}
		$return['code'] = "ok";
	}

	if (!empty($return))
		echo json_encode($return);
}
?>