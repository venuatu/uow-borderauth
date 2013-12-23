package me.myuow.borderauth

import com.github.kevinsawicki.http.HttpRequest
import scala.concurrent._
import scala.Tuple2
import android.preference.PreferenceManager
import scala.collection.JavaConverters._
import org.json.JSONObject
import android.content.Context
import scala.concurrent.ExecutionContext.Implicits.global

object SharedUtils {
  val timeOptions: Array[Tuple2[String, Int]] = Array(
    ("30 minutes", 30 * 60),
    ("1 hour", 60 * 60),
    ("2 hours", 2 * 60 * 60),
    ("6 hours", 6 * 60 * 60),
    ("12 hours", 12 * 60 * 60),
    ("24 hours", 24 * 60 * 60)
  )

  case class Response(code: Int, body: String)

  def borderAuth(ctx: Context): Future[Response] = {
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    val params = Map(
      "username" -> prefs.getString("username", ""),
      "password" -> prefs.getString("password", ""),
      "duration" -> timeOptions(prefs.getInt("duration", 0))._2.toString
    )
    future {
      val req = HttpRequest.post("https://api.uow.edu.au/borderauth/open/")
        .userAgent("me.myuow.android.borderauth v1").acceptJson().form(params.asJava)
      val body = new JSONObject(req.body).getString("message")
      Response(req.code, body)
    }
  }
}
