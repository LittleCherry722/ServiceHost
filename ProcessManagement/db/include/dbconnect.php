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

include_once 'db.config.php';

$link = mysql_connect($dbConfig["host"], $dbConfig["user"], $dbConfig["pass"]) or die("Can't connect: " . mysql_error());

$dbSelectStatus	= mysql_select_db($dbConfig["database"]);

$dbInitialize	= !$dbSelectStatus;

if ($dbSelectStatus)
{
	// check if correct number of tables has been created
	$tableQuery = mysql_query("SHOW TABLES FROM `" . $dbConfig["database"] . "`");
	$dbInitialize = $dbInitialize || mysql_num_rows($tableQuery) < 8;
}

if ($dbInitialize){
	mysql_query("CREATE DATABASE `". $dbConfig["database"] ."`");
	mysql_select_db($dbConfig["database"]) or die ("Can't create DB! " . mysql_error());
	
	mysql_query("CREATE TABLE IF NOT EXISTS `groups` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;");

	mysql_query("CREATE TABLE IF NOT EXISTS `messages` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `from` int(11) NOT NULL,
  `to` int(11) NOT NULL,
  `instanceID` int(11) NOT NULL,
  `read` tinyint(1) NOT NULL,
  `data` blob NOT NULL,
  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;");

	mysql_query("CREATE TABLE IF NOT EXISTS `process` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `startSubjects` varchar(128) NOT NULL,
  `graphID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;");

	mysql_query("CREATE TABLE IF NOT EXISTS `process_graphs` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `graph` blob NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;");

	mysql_query("CREATE TABLE IF NOT EXISTS `process_instance` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `processID` int(11) NOT NULL,
  `graphID` int(11) NOT NULL,
  `involvedUsers` varchar(128) NOT NULL,
  `data` blob NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;");

	mysql_query("CREATE TABLE IF NOT EXISTS `relation` (
  `userID` int(11) NOT NULL,
  `groupID` int(11) NOT NULL,
  `responsibleID` int(11) NOT NULL,
  `processID` int(11) NOT NULL,
  PRIMARY KEY (`userID`,`groupID`,`responsibleID`,`processID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;");

	mysql_query("CREATE TABLE IF NOT EXISTS `users` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT  '1',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;");

	mysql_query("INSERT INTO `users` (
`ID` ,
`name`
)
VALUES (
NULL , 'Admin'
);
");

	mysql_query("CREATE TABLE IF NOT EXISTS `users_x_groups` (
  `userID` int(11) NOT NULL,
  `groupID` int(11) NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT  '1',
  PRIMARY KEY (`userID`,`groupID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
}
?>