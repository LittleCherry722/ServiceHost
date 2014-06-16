package de.tkip.sbpm.rest.google

import java.io.IOException
import java.util.{GregorianCalendar, Date, TimeZone, Arrays, List}

import akka.event.LoggingAdapter

import com.google.api.client.googleapis.auth.oauth2.{
  GoogleAuthorizationCodeFlow,
  GoogleTokenResponse,
  GoogleAuthorizationCodeRequestUrl,
  GoogleClientSecrets,
  GoogleCredential
}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.DateTime

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.{Event, EventDateTime}

import de.tkip.sbpm.rest.google.GAuthCtrl


object GCalendarCtrl {
  private val jsonFactory = new JacksonFactory()
  private val httpTransport = new NetHttpTransport()

  def buildService(credentials: Credential) =
    new Calendar.Builder(httpTransport, jsonFactory, credentials)
      .setApplicationName("gCalendarCtrl")
      .build()

  def listCalendars(service: Calendar) =
    service.calendarList()
      .list()
      .execute()

  def primaryCalendar(service: Calendar) =
    service.calendars()
      .get("primary")
      .execute()

  def createEvent(service: Calendar, summary: String, location: String,
                  year: Int, month: Int, day: Int) = {
    val e = new Event()

    e.setSummary(summary)
    e.setLocation(location)

    val g = new GregorianCalendar()
    g.set(year, month-1, day+1, 0, 0, 0)

    val startDate = g.getTime()
    val endDate = startDate

    val start = new DateTime(true, startDate.getTime(), null)
    val end = new DateTime(true, endDate.getTime(), null)

    e.setStart(new EventDateTime().setDate(start))
    e.setEnd(new EventDateTime().setDate(end))

    service.events().insert("primary", e).execute()
  }

}

class GCalendarCtrl (implicit log: LoggingAdapter) {
  import GCalendarCtrl._
  import scala.collection.mutable

  private val calendarMap = mutable.Map[String, Calendar]()

  /*
   * Return a calendar instance for the user. If none is found,
   * instantiate one and hold it in the map for later use.
   */
  def calendarOf(userId: String): Calendar = {
    if (! calendarMap.contains(userId)) {
      log.info(s"no ID in map for $userId: $calendarMap")
      val d = buildService(GAuthCtrl.getCredentials(userId))
      calendarMap(userId) = d
    }
    return calendarMap(userId)
  }

  def createEvent(userId: String, summary: String, location: String,
                  year: Int, month: Int, day: Int) =
    GCalendarCtrl.createEvent(calendarOf(userId), summary, location,
                              year, month, day)
    
}
