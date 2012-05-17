<?php
$link = mysql_connect("localhost", "root", "") or die("Keine Verbindung möglich: " . mysql_error());
$dbname = "tkprojekt";
if (!mysql_select_db($dbname)){
	mysql_query("CREATE DATABASE `". $dbname ."`");
	mysql_select_db($dbname) or die ("Couldnt create DB! " . mysql_error());
	
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
  PRIMARY KEY (`userID`,`groupID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
}
?>