package me.myuow.borderauth

import android.content.{BroadcastReceiver, Context, Intent}
import android.net.{ConnectivityManager, NetworkInfo}
import android.net.wifi.WifiManager
import android.util.Log
import me.myuow.borderauth.SharedUtils.Response
import android.widget.Toast
import scala.concurrent.ExecutionContext.Implicits.global
import android.os.{Handler, Looper}
import android.preference.PreferenceManager

class NetworkMonitor extends BroadcastReceiver {

  override def onReceive(ctx: Context, intent: Intent): Unit = {
    val service = ctx.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val info = service.getActiveNetworkInfo()
    if (info.getType == ConnectivityManager.TYPE_WIFI && info.getState == NetworkInfo.State.CONNECTED) {
      val wifi = ctx.getSystemService(Context.WIFI_SERVICE).asInstanceOf[WifiManager].getConnectionInfo
      if (wifi.getSSID == "\"UOW\"" && isAllowed(ctx)) {// it's returned in quotes
        val fut = SharedUtils.borderAuth(ctx)
        fut onSuccess {
          case Response(code, _, message) => {
            onUiThread {
              Toast.makeText(ctx, s"BorderAuth: $message", Toast.LENGTH_LONG).show()
            }
            Log.i("borderauth.auto", s"successfully borderauthed $message")
          }
        }
      }
    }
  }

  def isAllowed(ctx: Context): Boolean = {
    PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("always", false)
  }

  def onUiThread(job: => Unit): Unit = {
    new Handler(Looper.getMainLooper()).post(new Runnable {
      def run(): Unit = {
        job
      }
    });
  }
}
