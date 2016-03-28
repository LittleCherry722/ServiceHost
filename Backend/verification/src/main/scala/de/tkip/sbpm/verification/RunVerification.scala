package de.tkip.sbpm.verification

import de.tkip.sbpm.factory.ProcessFactory

object RunVerification {

  def main(args: Array[String]) {

    var pm = ProcessFactory.createSimpleTravelRequest()
    pm = ProcessFactory.createMultiSubjectExample()
    pm = ProcessFactory.createMacroExample()
    pm = ProcessFactory.createExampleProcess()
    pm = ProcessFactory.createExampleProcessInstantInterface()
    pm = ProcessFactory.createTravelRequest()
    pm = ProcessFactory.createTravelRequestInterface("App")
    pm = ProcessFactory.createTravelRequestInterface("Adm")
    pm = ProcessFactory.createTravelRequestInterface("Sup")
    pm = ProcessFactory.createModalSplitExample()
    pm = ProcessFactory.createFirstMultiSubjectExample()
    pm = ProcessFactory.createSimpleProcessBreakUpTimeout()
    pm = ProcessFactory.createObserverExample()
    pm = ProcessFactory.createFirstMultiSubjectExample(2)
    pm = ProcessFactory.createObserverAsCorIdExample()
    pm = ProcessFactory.createSimpleTravelRequest()

    val veri = new Verificator(pm)

    println("Running verification...\n")
    val before = System.currentTimeMillis()

    val printLtsSize = veri.printLtsSize _

    // optimize or not?
    veri.optimize = false

    println(s"Running Verification for ${pm.name}")
    runWithTimeLog("Building the LTS", veri.verificate _); printLtsSize()
    //    runWithTimeLog("Pruning the LTS", veri.pruneLts _); printLtsSize()
    runWithTimeLog("Running Validity check", () => println("Valid: " + veri.lts.valid))
    runWithTimeLog("Writing graph", veri.writeGraph _)

    val after = System.currentTimeMillis()
    val duration = after - before
    println(
      "Whole progress took " +
        (if (duration >= 1000) ((duration / 1000) + " sec, ") else "") +
        (duration % 1000) + " ms")
  }

  private def runWithTimeLog(message: String,
                             functions: List[() => Unit]) {
    println(s"\n$message...")
    val before = System.currentTimeMillis()
    // Run the functions
    for (function <- functions) {
      function()
    }
    val after = System.currentTimeMillis()
    println(s"$message took ${after - before} ms\n")
  }

  private def runWithTimeLog(message: String,
                             function: () => Unit) {
    runWithTimeLog(message, function :: Nil)
  }
}