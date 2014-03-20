package de.tkip.sbpm.instrumentation

import akka.actor.Actor

/**
 * Created by arne on 17.03.14.
 */
abstract class InstrumentedActor extends Actor with TraceLogger