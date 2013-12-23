package me.myuow.borderauth

import scala.language.postfixOps
import org.scaloid.common._
import android.text.InputType
import android.view.Gravity
import android.os.Bundle
import scala.concurrent.ExecutionContext.Implicits.global
import android.preference.PreferenceManager
import me.myuow.borderauth.SharedUtils.Response

class MainActivity extends SActivity {

  var username: SEditText = null
  var password: SEditText = null
  var duration: SSpinner = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    contentView = new SVerticalLayout {
      style {
        case x: SView => x.padding(4 sp)
        case edit: SEditText => edit.<<(FILL_PARENT, WRAP_CONTENT).>>
        case button: SButton => button.textSize(16 sp).<<(FILL_PARENT, WRAP_CONTENT).Gravity(Gravity.CENTER_HORIZONTAL).Weight(1).>>
      }
      username = SEditText().hint("Username")
      password = SEditText().hint("Password")
        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
      duration = SSpinner()
        .adapter(SArrayAdapter(SharedUtils.timeOptions.map{_._1})
          .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item))
        .<<(FILL_PARENT, WRAP_CONTENT).>>
      this += new SLinearLayout{
        SButton("Always", borderAuth(true))
        SButton("Just once", borderAuth(false))
      }
    }
    val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
    username.text(pref.getString("username", ""))
    password.text(pref.getString("password", ""))
    duration.setSelection(pref.getInt("duration", 4))
  }

  def borderAuth(always: Boolean): Unit = {
    PreferenceManager.getDefaultSharedPreferences(ctx).edit()
      .putString("username", username.getText.toString)
      .putString("password", password.getText.toString)
      .putInt("duration", duration.getSelectedItemPosition)
      .putBoolean("always", always)
      .commit()
    val fut = SharedUtils.borderAuth(ctx)
    fut onSuccess {
      case Response(code, body) => {
        warn(s"Response: $code $body")
        longToast(body)
        finish()
      }
    }
    fut onFailure {
      case e => {
        error("failed request", e)
        longToast(e.getMessage)
      }
    }
  }
}
