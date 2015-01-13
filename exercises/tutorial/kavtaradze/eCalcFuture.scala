import akka.actor._
import akka.routing.RoundRobinRouter  

object Main extends App {
  

	case class Calculate 
	case class Work(start: Int, num: Int) 
	case class Result(value: Double)
	case class eApproximation(e: Double)

	class worker extends Actor {
  
		def factorial(n: Int): Int = n match {
			case 0 => 1
			case _ => n * factorial(n-1)
		} 
  
		def calculateE(start: Int, num: Int): Double = {
		    var value = 0.0
		    for (i <- start until (start + num))
		    	value += 1.0 / factorial(i)
		    	value
		}
  
		def receive = {
			case Work(start, num) =>
				sender ! Result(calculateE(start, num))
		}
	}  

	class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, printer: ActorRef)
		extends Actor {
  
		var e: Double = 0.0
  
		val workerRouter = context.actorOf(
			Props[worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
  
		def receive = {
			case Calculate =>
				for(i <- 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
			case Result(value) => {
				val res: Future[Result] = (workerRouter ? value).mapTo[Result]
			
				res pipeTo sender
      
			}
	}
  
	class Printer extends Actor {
		def receive = {
			case eApproximation(e) => {
            println(e)
            context.system.shutdown()
			}
    
		}
  
	}
  

  
	def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
		val system = ActorSystem()
		val printer = system.actorOf(Props[Printer], name = "printer")
		val master = system.actorOf(Props(new Master(
				nrOfWorkers, nrOfMessages, nrOfElements, printer)),
				name = "master")
		val response: Future[Result] = (actor ? res).mapTo[Result]
		val f: Future[Result] = Future.reduce(response)

		f pipeTo Printer  
	}
  
  
	calculate(4, 100, 10)  
  
	}
  
  
}

