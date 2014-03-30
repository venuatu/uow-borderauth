package main

import scala.language.postfixOps
import org.scaloid.common._
import android.text.InputType
import android.view.Gravity
import android.os.{Build, Bundle}
import scala.concurrent.ExecutionContext.Implicits.global
import android.preference.PreferenceManager
import main.SharedUtils.Response
import scala.util.{Failure, Success}

class MainActivity extends SActivity {

  var username: SEditText = null
  var password: SEditText = null
  var duration: SSpinner = null
  var autoauth: SCheckBox = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      setTheme(android.R.style.Theme_Dialog)
    }

    contentView = new SVerticalLayout {
      style {
        case x: SView => x.padding(4 sp).<<(FILL_PARENT, WRAP_CONTENT).>>
      }
      username = SEditText().hint("Username")
      password = SEditText().hint("Password")
        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
      duration = SSpinner()
        .adapter(SArrayAdapter(SharedUtils.timeOptions.map{_._1})
          .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item))
      //remember = SCheckBox("Remember me")
      this += new SLinearLayout{
        autoauth = SCheckBox("Automatic").<<.Weight(1).>>
        SButton("BorderAuth", borderAuth())
          .textSize(16 sp).<<.Weight(1).>>
      }
    }
    val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
    username.text = pref.getString("username", "")
    password.text = pref.getString("password", "")
    duration.selection = pref.getInt("duration", 0)
    autoauth.checked = pref.getBoolean("automatic", true)
  }

  def borderAuth() {
    PreferenceManager.getDefaultSharedPreferences(ctx).edit()
      .putString("username", username.text.toString)
      .putString("password", password.text.toString)
      .putInt("duration", duration.selectedItemPosition)
      .putBoolean("automatic", autoauth.checked)
      .commit()
    SharedUtils.borderAuth() onComplete {
      case Success(Response(code, body)) => {
        warn(s"Response: $code $body")
        longToast(body)
        finish()
      }
      case Failure(e) => {
        error("failed request", e)
        longToast(e.getMessage)
      }
    }
  }
}
