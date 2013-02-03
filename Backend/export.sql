-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 03. Feb 2013 um 16:52
-- Server Version: 5.5.25-log
-- PHP-Version: 5.3.14

--
-- Datenbank: `sbpm_groupware`
--

-- --------------------------------------------------------


DROP TABLE IF EXISTS `configuration`;
DROP TABLE IF EXISTS `group_x_roles`;
DROP TABLE IF EXISTS `group_x_users`;
DROP TABLE IF EXISTS `groups`;
DROP TABLE IF EXISTS `process`;
DROP TABLE IF EXISTS `process_graphs`;
DROP TABLE IF EXISTS `process_instance`;
DROP TABLE IF EXISTS `relation`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `users_x_groups`;

--
-- Tabellenstruktur für Tabelle `configuration`
--

CREATE TABLE IF NOT EXISTS "configuration" (
  "key" varchar(64) NOT NULL,
  "label" varchar(64) NOT NULL,
  "value" varchar(128) NOT NULL,
  "type" varchar(16) NOT NULL DEFAULT 'String',
  PRIMARY KEY ("key")
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `groups`
--

CREATE TABLE IF NOT EXISTS "groups" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "name" varchar(32) NOT NULL,
  "active" tinyint(1) NOT NULL DEFAULT '1'
);

--
-- Daten für Tabelle `groups`
--

INSERT INTO `groups` (`ID`, `name`, `active`) VALUES
(1, '_SAME_', 1),
(2, '_ANY_', 1),
(3, 'SBPM_Ltd', 1),
(4, 'SBPM_Ltd_DE', 1),
(5, 'SBPM_Ltd_DE_Accounting', 1),
(6, 'SBPM_Ltd_DE_Procurement', 1),
(7, 'SBPM_Ltd_DE_Human_Resources', 1),
(8, 'SBPM_Ltd_DE_Warehouse', 1),
(9, 'SBPM_Ltd_DE_Board', 1),
(10, 'SBPM_Ltd_UK', 1),
(11, 'SBPM_Ltd_UK_Accounting', 1),
(12, 'SBPM_Ltd_UK_Procurement', 1),
(13, 'SBPM_Ltd_UK_Human_Resources', 1),
(14, 'SBPM_Ltd_UK_Warehouse', 1),
(15, 'SBPM_Ltd_UK_Board', 1),
(16, 'Manager', 1),
(17, 'Teamleader', 1),
(18, 'Head_of_Department', 1),
(19, 'IT-Stuff', 1),
(20, 'External', 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `group_x_roles`
--

CREATE TABLE IF NOT EXISTS "group_x_roles" (
  "groupID" int(11) NOT NULL,
  "roleID" int(11) NOT NULL,
  "active" tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY ("groupID","roleID")
);

--
-- Daten für Tabelle `group_x_roles`
--

INSERT INTO `group_x_roles` (`groupID`, `roleID`, `active`) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 4, 1),
(3, 7, 1),
(4, 5, 1),
(4, 9, 1),
(4, 10, 1),
(5, 6, 1),
(5, 7, 1),
(6, 9, 1),
(6, 10, 1),
(7, 11, 1),
(8, 3, 1),
(9, 4, 1),
(9, 8, 1),
(10, 5, 1),
(10, 9, 1),
(10, 10, 1),
(11, 6, 1),
(11, 8, 1),
(12, 9, 1),
(12, 10, 1),
(14, 12, 1),
(14, 13, 1),
(15, 12, 1),
(16, 12, 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `group_x_users`
--

CREATE TABLE IF NOT EXISTS "group_x_users" (
  "groupID" int(11) NOT NULL,
  "userID" int(11) NOT NULL,
  "active" tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY ("groupID","userID")
);

--
-- Daten für Tabelle `group_x_users`
--

INSERT INTO `group_x_users` (`groupID`, `userID`, `active`) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 3, 1),
(1, 4, 1),
(1, 5, 1),
(1, 6, 1),
(1, 7, 1),
(2, 2, 1),
(2, 4, 1),
(2, 6, 1),
(3, 2, 1),
(5, 4, 1),
(7, 6, 1),
(8, 3, 1),
(8, 7, 1),
(10, 3, 1),
(13, 7, 1),
(14, 3, 1),
(14, 5, 1),
(14, 6, 1),
(14, 7, 1),
(15, 2, 1),
(16, 5, 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `messages`
--

CREATE TABLE IF NOT EXISTS "messages" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "from" int(11) NOT NULL,
  "to" int(11) NOT NULL,
  "instanceID" int(11) NOT NULL,
  "read" tinyint(1) NOT NULL,
  "data" blob NOT NULL,
  "date" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("ID")
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `process`
--

CREATE TABLE IF NOT EXISTS "process" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "name" varchar(64) NOT NULL,
  "startSubjects" varchar(128) NOT NULL,
  "graphID" int(11) NOT NULL,
  "isProcess" tinyint(1) NOT NULL DEFAULT '1'
);

--
-- Daten für Tabelle `process`
--

INSERT INTO `process` (`ID`, `name`, `startSubjects`, `graphID`, `isProcess`) VALUES
(1, 'Travel Request', '[{''ID'':''1'',''name'':''Employee'',''active'':''1''},{''ID'':''2'',''name'':''Manager'',''active'':''1''},{''ID'':''3'',''name'':''Human Resource'',''active'':''', 1, 1),
(2, 'Order', '[null,null,null,null]', 2, 1),
(3, 'Supplier (E)', '[null,null]', 3, 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `process_graphs`
--

CREATE TABLE IF NOT EXISTS "process_graphs" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "graph" blob NOT NULL,
  "date" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "processID" int(11) NOT NULL
);

--
-- Daten für Tabelle `process_graphs`
--

INSERT INTO `process_graphs` (`ID`, `graph`, `date`, `processID`) VALUES
(1, '{"process":[{"id":"Employee","name":"Applicant","type":"single","deactivated":false,"inputPool":100,"relatedProcess":null,"relatedSubject":null,"externalType":"external","role":"Employee","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"Prepare Travel Application","start":true,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":true,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"End process","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"Decide whether filing again","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"Make travel","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"Done","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m0","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":4,"text":"m1","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"Denial accepted","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":0,"text":"Redo Travel Application","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":6,"text":"m3","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":3,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":10}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Manager","name":"Supervisor","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Supervisor","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":true,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"End Process","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"Check Travel Application","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":1,"end":2,"text":"m3","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m4","type":"exitcondition","target":{"id":"Human Resource","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":0,"end":5,"text":"m0","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":1,"text":"Grant Permission","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"m0","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":6,"text":"Deny Permission","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":4,"text":"m1","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":17}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Human Resource","name":"Administration","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"HR_Data_Access","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":true,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"Handle Travel Application","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"End process","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m4","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"Travel Application filed","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":3}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Travel Application","m1":"Permission denied","m2":"No further<br />Travel Application","m3":"Permission granted","m4":"Approved<br />Travel Application"},"messageCounter":5,"nodeCounter":3,"channels":{},"channelCounter":0}', '2012-10-12 19:12:07', 1),
(2, '{"process":[{"id":"Subj1","name":"Purchaser","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Purchase_Requisitions","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"Prepare Order Request","start":true,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"Wait for answer","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"send","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":7,"text":"internal action","start":false,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":8,"text":"Check Order","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":9,"text":"Process Order Date","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":10,"text":"Check Order","start":false,"end":true,"type":"end","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"Done","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m0","type":"exitcondition","target":{"id":"Subj4","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":3,"end":4,"text":"m1","type":"exitcondition","target":{"id":"Subj4","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":5,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":6,"text":"m2","type":"exitcondition","target":{"id":"Subj4","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":7,"text":"m3","type":"exitcondition","target":{"id":"Subj3","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":7,"end":8,"text":"m4","type":"exitcondition","target":{"id":"Subj3","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":7,"end":9,"text":"m5","type":"exitcondition","target":{"id":"Subj3","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":9,"end":7,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":8,"end":10,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":11}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj2","name":"Supplier","type":"external","deactivated":false,"inputPool":100,"relatedProcess":"Supplier (E)","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[],"edges":[],"nodeCounter":0}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj3","name":"Warehouse","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"new","start":true,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"Check Stock","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"internal action","start":false,"end":false,"type":"send","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"internal action","start":false,"end":true,"type":"end","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"Check Offers","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":7,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":8,"text":"","start":false,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":9,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":10,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m3","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"Goods in Stock","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m4","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":4,"text":"Goods not in Stock","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":6,"text":"m3","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":7,"text":"m5","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":7,"end":8,"text":"m5","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":8,"end":9,"text":"m4","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":9,"end":10,"text":"m4","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":13}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj4","name":"Manager","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Cost_Center_Manager","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"new","start":true,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"Process Order Request","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m0","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"Accept","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":4,"text":"Denial","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"m1","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":6}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Order Request","m1":"Denied Order Request","m2":"Accepted Order Request","m3":"Order","m4":"Goods","m5":"Order Date","m6":"Ask for Offer","m7":"Offer","m8":"Order Goods"},"messageCounter":9,"nodeCounter":4,"channels":{},"channelCounter":0}', '2012-10-12 19:12:07', 2),
(3, '{"process":[{"id":"Subj1","name":"Supplier","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"new","start":true,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"internal action","start":false,"end":true,"type":"end","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m0","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m1","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":4}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj2","name":"Order Prozess","type":"external","deactivated":false,"inputPool":100,"relatedProcess":"Order","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[],"edges":[],"nodeCounter":0}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Order","m1":"Order Date","m2":"Goods"},"messageCounter":3,"nodeCounter":2,"channels":{},"channelCounter":0}', '2012-10-12 19:09:53', 3);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `process_instance`
--

CREATE TABLE IF NOT EXISTS "process_instance" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "processID" int(11) NOT NULL,
  "graphID" int(11) NOT NULL,
  "involvedUsers" varchar(128) NOT NULL,
  "data" blob NOT NULL
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `relation`
--

CREATE TABLE IF NOT EXISTS "relation" (
  "userID" int(11) NOT NULL,
  "groupID" int(11) NOT NULL,
  "responsibleID" int(11) NOT NULL,
  "processID" int(11) NOT NULL,
  PRIMARY KEY ("userID","groupID","responsibleID","processID")
);

--
-- Daten für Tabelle `relation`
--

INSERT INTO `relation` (`userID`, `groupID`, `responsibleID`, `processID`) VALUES
(0, 1, 3, 0),
(0, 2, 2, 0),
(0, 3, 4, 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `roles`
--

CREATE TABLE IF NOT EXISTS "roles" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "name" varchar(32) NOT NULL,
  "active" tinyint(1) NOT NULL DEFAULT '1'
);

--
-- Daten für Tabelle `roles`
--

INSERT INTO `roles` (`ID`, `name`, `active`) VALUES
(1, 'Employee', 1),
(2, 'Employee_DE', 1),
(3, 'Employee_UK', 1),
(4, 'Accounting', 1),
(5, 'Procurement', 1),
(6, 'HR_Data_Access', 1),
(7, 'Salary_Statement_DE', 1),
(8, 'Salary_Statement_UK', 1),
(9, 'Warehouse', 1),
(10, 'Purchase_Requisitions', 1),
(11, 'Board_Member', 1),
(12, 'Supervisor', 1),
(13, 'Cost_Center_Manager', 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `users`
--

CREATE TABLE IF NOT EXISTS "users" (
  "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
  "name" varchar(32) NOT NULL,
  "active" tinyint(1) NOT NULL DEFAULT '1',
  "inputpoolsize" smallint(6) NOT NULL DEFAULT '8'
);

--
-- Daten für Tabelle `users`
--

INSERT INTO `users` (`ID`, `name`, `active`, `inputpoolsize`) VALUES
(1, 'Superuser', 1, 8),
(2, 'Beyer', 1, 8),
(3, 'Link', 1, 8),
(4, 'Woehnl', 1, 8),
(5, 'Borgert', 1, 8),
(6, 'Roeder', 1, 8),
(7, 'Hartwig', 1, 8);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `users_x_groups`
--

CREATE TABLE IF NOT EXISTS "users_x_groups" (
  "userID" int(11) NOT NULL,
  "groupID" int(11) NOT NULL,
  "active" tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY ("userID","groupID")
);

--
-- Daten für Tabelle `users_x_groups`
--

INSERT INTO `users_x_groups` (`userID`, `groupID`, `active`) VALUES
(2, 2, 1),
(3, 1, 1),
(4, 3, 1);

