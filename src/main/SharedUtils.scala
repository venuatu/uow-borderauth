package me.myuow.borderauth

import com.github.kevinsawicki.http.HttpRequest
import scala.concurrent._
import android.preference.PreferenceManager
import scala.collection.JavaConverters._
import org.json.JSONObject
import android.content.{Intent, Context}
import scala.concurrent.ExecutionContext.Implicits.global
import android.net.wifi.WifiManager
import java.lang.NullPointerException
import android.app.{PendingIntent, AlarmManager}
import android.util.Log

object SharedUtils {
  val timeOptions: Array[(String, Int)] = Array(
    ("30 minutes", 30 * 60),
    ("1 hour", 60 * 60),
    ("2 hours", 2 * 60 * 60),
    ("6 hours", 6 * 60 * 60),
    ("12 hours", 12 * 60 * 60),
    ("24 hours", 24 * 60 * 60)
  )

  case class Response(code: Int, body: String)
  val GOOD_CODES = List(
    0,// SUCCESS
    23// INVALID_IP (valid credentials but outside of UOW)
  )

  def borderAuth()(implicit ctx: Context): Future[Response] = {
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    val duration = timeOptions(prefs.getInt("duration", 0))._2
    val params = Map(
      "username" -> prefs.getString("username", ""),
      "password" -> prefs.getString("password", ""),
      "duration" -> duration.toString
    )
    future {
      val req = HttpRequest.post("https://api.uow.edu.au/borderauth/open/")
        .userAgent("me.myuow.android.borderauth v1").acceptJson().form(params.asJava)
      val body = new JSONObject(req.body)
      Log.d("uow.borderauth", s"returned: ${req.code} $body")
      if (!GOOD_CODES.contains(body.getInt("status"))) {
        throw new Exception("Failed to BorderAuth: " + body.getString("message"))
      }
      val expire = System.currentTimeMillis() + (duration * 0.9).toLong
      prefs.edit()
        .putLong("authExpire", expire)
        .putInt("ip", getIpAddress)
        .commit()
      scheduleMonitor(expire + 5000)
      Response(req.code, body.getString("message"))
    }
  }

  def scheduleMonitor(time: Long)(implicit ctx: Context) {
    val alarm = ctx.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val intent = new Intent(ctx, classOf[NetworkMonitor])
    alarm.set(AlarmManager.RTC, time, PendingIntent.getBroadcast(ctx, 0, intent, 0))
  }

  def isBorderAuthActive(implicit ctx: Context): Boolean = {
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    getIpAddress == prefs.getInt("ip", -2) && System.currentTimeMillis() < prefs.getLong("authExpire", 0)
  }

  def getIpAddress(implicit ctx: Context): Int = {
    try {
      val info = ctx.getSystemService(Context.WIFI_SERVICE).asInstanceOf[WifiManager].getConnectionInfo
      info.getIpAddress
    } catch {
      case _: NullPointerException => -1
    }
  }
}
