package de.tkip.sbpm.persistence

import de.tkip.sbpm.model._
import akka.actor.ActorRef
import akka.pattern._
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.ActorRefFactory
import scala.concurrent.Await
import ua.t3hnar.bcrypt._
import scala.concurrent.ExecutionContext

/**
 * Provides test data for the database.
 */
object TestData {
  val groups = List(
    Group(None, """_SAME_""", true),
    Group(None, """_ANY_""", true),
    Group(None, """SBPM_Ltd""", true),
    Group(None, """SBPM_Ltd_DE""", true),
    Group(None, """SBPM_Ltd_DE_Accounting""", true),
    Group(None, """SBPM_Ltd_DE_Procurement""", true),
    Group(None, """SBPM_Ltd_DE_Human_Resources""", true),
    Group(None, """SBPM_Ltd_DE_Warehouse""", true),
    Group(None, """SBPM_Ltd_DE_Board""", true),
    Group(None, """SBPM_Ltd_UK""", true),
    Group(None, """SBPM_Ltd_UK_Accounting""", true),
    Group(None, """SBPM_Ltd_UK_Procurement""", true),
    Group(None, """SBPM_Ltd_UK_Human_Resources""", true),
    Group(None, """SBPM_Ltd_UK_Warehouse""", true),
    Group(None, """SBPM_Ltd_UK_Board""", true),
    Group(None, """Manager""", true),
    Group(None, """Teamleader""", true),
    Group(None, """Head_of_Department""", true),
    Group(None, """IT-Stuff""", true),
    Group(None, """External""", true))

  val roles = List(
    Role(None, """Employee""", true),
    Role(None, """Employee_DE""", true),
    Role(None, """Employee_UK""", true),
    Role(None, """Accounting""", true),
    Role(None, """Procurement""", true),
    Role(None, """HR_Data_Access""", true),
    Role(None, """Salary_Statement_DE""", true),
    Role(None, """Salary_Statement_UK""", true),
    Role(None, """Warehouse""", true),
    Role(None, """Purchase_Requisitions""", true),
    Role(None, """Board_Member""", true),
    Role(None, """Supervisor""", true),
    Role(None, """Cost_Center_Manager""", true))

  val users = List(
    (User(None, """Superuser""", true, 8), ("sbpm", "superuser@sbpm.com", "s1234".bcrypt)),
    (User(None, """Beyer""", true, 8), ("sbpm", "beyer@sbpm.com", "b1234".bcrypt)),
    (User(None, """Link""", true, 8), ("sbpm", "link@sbpm.com", "l1234".bcrypt)),
    (User(None, """Woehnl""", true, 8), ("sbpm", "woehnl@sbpm.com", "w1234".bcrypt)),
    (User(None, """Borgert""", true, 8), ("sbpm", "borgert@sbpm.com", "b1234".bcrypt)),
    (User(None, """Roeder""", true, 8), ("sbpm", "roeder@sbpm.com", "r1234".bcrypt)),
    (User(None, """Hartwig""", true, 8), ("sbpm", "hartwig@sbpm.com", "h1234".bcrypt)))

  val processes = List(
    (Process(None, """Travel Request""", 1, !true, """["Employee"]""") ->
      Graph(None, """{"process":[{"id":"Employee","name":"Applicant","type":"single","deactivated":false,"inputPool":100,"relatedProcess":null,"relatedSubject":null,"externalType":"external","role":"Employee","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"Prepare Travel Application","start":true,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":true,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"End process","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"Decide whether filing again","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"Make travel","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"Done","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m0","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":4,"text":"m1","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"Denial accepted","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":0,"text":"Redo Travel Application","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":6,"text":"m3","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":3,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":10}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Manager","name":"Supervisor","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Supervisor","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":true,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"End Process","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"Check Travel Application","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":1,"end":2,"text":"m3","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m4","type":"exitcondition","target":{"id":"Human Resource","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":0,"end":5,"text":"m0","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":1,"text":"Grant Permission","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"m0","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":6,"text":"Deny Permission","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":4,"text":"m1","type":"exitcondition","target":{"id":"Employee","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":17}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Human Resource","name":"Administration","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"HR_Data_Access","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":true,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"Handle Travel Application","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"End process","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*"},"deactivated":false,"majorStartNode":false,"channel":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m4","type":"exitcondition","target":{"id":"Manager","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"Travel Application filed","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":3}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Travel Application","m1":"Permission denied","m2":"No further<br />Travel Application","m3":"Permission granted","m4":"Approved<br />Travel Application"},"messageCounter":5,"nodeCounter":3,"channels":{},"channelCounter":0}""", java.sql.Timestamp.valueOf("""2012-10-12 19:12:07"""), 0)),
    (Process(None, """Order""", 2, !true, """["Subj1"]""") ->
      Graph(None, """{"process":[{"id":"Subj1","name":"Purchaser","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Purchase_Requisitions","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"Prepare Order Request","start":true,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"Wait for answer","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"send","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":7,"text":"internal action","start":false,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":8,"text":"Check Order","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":9,"text":"Process Order Date","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":10,"text":"Check Order","start":false,"end":true,"type":"end","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"Done","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m0","type":"exitcondition","target":{"id":"Subj4","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":3,"end":4,"text":"m1","type":"exitcondition","target":{"id":"Subj4","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":5,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":6,"text":"m2","type":"exitcondition","target":{"id":"Subj4","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":7,"text":"m3","type":"exitcondition","target":{"id":"Subj3","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":7,"end":8,"text":"m4","type":"exitcondition","target":{"id":"Subj3","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":7,"end":9,"text":"m5","type":"exitcondition","target":{"id":"Subj3","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":9,"end":7,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":8,"end":10,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":11}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj2","name":"Supplier","type":"external","deactivated":false,"inputPool":100,"relatedProcess":"Supplier (E)","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[],"edges":[],"nodeCounter":0}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj3","name":"Warehouse","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"new","start":true,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"Check Stock","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"internal action","start":false,"end":false,"type":"send","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"internal action","start":false,"end":true,"type":"end","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"Check Offers","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":7,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":8,"text":"","start":false,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":9,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":10,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m3","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"Goods in Stock","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m4","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":4,"text":"Goods not in Stock","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":6,"text":"m3","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":7,"text":"m5","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":7,"end":8,"text":"m5","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":8,"end":9,"text":"m4","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":9,"end":10,"text":"m4","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":13}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj4","name":"Manager","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Cost_Center_Manager","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"new","start":true,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"Process Order Request","start":false,"end":false,"type":"action","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m0","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"Accept","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":4,"text":"Denial","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"m1","type":"exitcondition","target":{"id":"Subj1","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":6}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Order Request","m1":"Denied Order Request","m2":"Accepted Order Request","m3":"Order","m4":"Goods","m5":"Order Date","m6":"Ask for Offer","m7":"Offer","m8":"Order Goods"},"messageCounter":9,"nodeCounter":4,"channels":{},"channelCounter":0}""", java.sql.Timestamp.valueOf("""2012-10-12 19:12:07"""), 1)),
    (Process(None, """Supplier (E)""", 3, !true, """["Subj1"]""") ->
      Graph(None, """{"process":[{"id":"Subj1","name":"Supplier","type":"single","deactivated":false,"inputPool":100,"relatedProcess":"","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"new","start":true,"end":false,"type":"receive","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":true,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","channel":"*","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"internal action","start":false,"end":true,"type":"end","options":{"subject":"","message":"","channel":"","correlationId":"","state":""},"deactivated":false,"majorStartNode":false,"channel":"","variable":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"m0","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m1","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Subj2","min":"-1","max":"-1","createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}],"nodeCounter":4}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"Subj2","name":"Order Prozess","type":"external","deactivated":false,"inputPool":100,"relatedProcess":"Order","relatedSubject":"","externalType":"external","role":"Warehouse","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[],"edges":[],"nodeCounter":0}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Order","m1":"Order Date","m2":"Goods"},"messageCounter":3,"nodeCounter":2,"channels":{},"channelCounter":0}""", java.sql.Timestamp.valueOf("""2012-10-12 19:09:53"""), 2)))

  val relations = List(
    Relation(0, 1, 3, 0),
    Relation(0, 2, 2, 0),
    Relation(0, 3, 4, 0))

    // _1 = group, _2 = role index in groups/roles list, _3 = isActive
  val groupRoles = List(
    (0, 0, true),
    (1, 1, true),
    (2, 3, true),
    (2, 6, true),
    (3, 4, true),
    (3, 8, true),
    (3, 9, true),
    (4, 5, true),
    (4, 6, true),
    (5, 8, true),
    (5, 9, true),
    (6, 10, true),
    (7, 2, true),
    (8, 3, true),
    (8, 7, true),
    (9, 4, true),
    (9, 8, true),
    (9, 9, true),
    (10, 5, true),
    (10, 7, true),
    (11, 8, true),
    (11, 9, true),
    (13, 11, true),
    (13, 12, true),
    (14, 11, true),
    (15, 11, true))

    // _1 = group, _2 = user index in groups/users list, _3 = isActive
  val groupUsers = List(
    (0, 0, true),
    (0, 1, true),
    (0, 2, true),
    (0, 3, true),
    (0, 4, true),
    (0, 5, true),
    (0, 6, true),
    (1, 1, true),
    (1, 3, true),
    (1, 5, true),
    (2, 1, true),
    (4, 3, true),
    (6, 5, true),
    (7, 2, true),
    (7, 6, true),
    (9, 2, true),
    (12, 6, true),
    (13, 2, true),
    (13, 4, true),
    (13, 5, true),
    (13, 6, true),
    (14, 1, true),
    (15, 4, true),
    (2, 3, true))

  implicit val timeout = akka.util.Timeout(100 seconds)

  /**
   * Send all test data to the persistence actor to be inserted into the database.
   */
  def insert(persistenceActor: ActorRef)(implicit executionContext: ExecutionContext): Future[Any] = {
    val groupsFuture = Future.sequence(groups.map { g =>
      (persistenceActor ? SaveGroup(g)).mapTo[Option[Int]]
    })

    val usersFuture = Future.sequence(users.map { u =>
      (persistenceActor ? SaveUser(u._1)).mapTo[Option[Int]]
    })

    val rolesFuture = Future.sequence(roles.map { r =>
      (persistenceActor ? SaveRole(r)).mapTo[Option[Int]]
    })

    // combine futures and wait until groups/users/roles are
    // inserted, then insert the different associations using
    // the generated ids
    val groupAssocFuture = for {
      g <- groupsFuture
      u <- usersFuture
      r <- rolesFuture
      gu <- Future.sequence(groupUsers.map { gu =>
        (persistenceActor ? SaveGroupUser(GroupUser(g(gu._1).get, u(gu._2).get, gu._3)))
      })
      gr <- Future.sequence(groupRoles.map { gr =>
        (persistenceActor ? SaveGroupRole(GroupRole(g(gr._1).get, r(gr._2).get, gr._3)))
      })
      ui <- Future.sequence(users.indices.map { i =>
        val ident = users(i)._2
        (persistenceActor ? SetUserIdentity(u(i).get, ident._1, ident._2, Some(ident._3)))
      })
    } yield (gu, gr, ui)

    val processesFuture = Future.sequence(processes.map { p =>
      (persistenceActor ? SaveProcess(p._1, Some(p._2))).mapTo[(Option[Int], Option[Int])]
    })

    val relationsFuture = Future.sequence(relations.map { rel =>
      persistenceActor ? SaveRelation(rel)
    })

    for {
      ga <- groupAssocFuture
      p <- processesFuture
      r <- relationsFuture
    } yield (ga, p, r)
  }
}