package io.mkg20001.arubanetworkslogin

import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import com.goebl.david.Webb
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user on the captive portal
 */
class CaptiveLoginTask internal constructor(private val mEmail: String, private val mUsername: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void): Boolean? {
        try {
            CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL ))

            var webb = Webb.create()
            webb.setDefaultHeader(Webb.HDR_USER_AGENT, "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:63.0) Gecko/20100101 Firefox/63.0")

            Log.v(LoginActivity.TAG, "Captive task started... Detecting portal")

            var output = webb.get("http://detectportal.firefox.com").asString().body
            if (output == "success") {
                Log.v(LoginActivity.TAG, "No captive detected! Yay!")
                return true
            }
            var urlMatch = Regex("(http://detectportal.+)'").find(output)

            if (urlMatch == null) {
                Log.v(LoginActivity.TAG, "Not aruba captive!")
                return false
            }

            var extractedUrl = urlMatch.groupValues[1]
            Log.v(LoginActivity.TAG, "Extracted url $extractedUrl")

            var redir = webb.get(extractedUrl).followRedirects(true).ensureSuccess().uri
            Log.v(LoginActivity.TAG, "Final $redir")

            var post = Regex("\\\\?.+").replace(redir, "")
            Log.v(LoginActivity.TAG, "Post $post")

            var finalRes = webb.post(post)
                .header("Referrer", redir)
                .param("user", mUsername)
                .param("password", mPassword)
                .param("email", mEmail)
                .param("cmd", "authenticate")
                .param("agreementAck", "Accept")
                .ensureSuccess()
                .asString()
                .body

            if (!finalRes.matches(Regex(".+Authentication successful-+"))) {
                Log.v(LoginActivity.TAG, "Auth failed!")
                return false
            }

            return true
        } catch (ex: Exception) {
            Log.e(LoginActivity.TAG, ex.toString())
        }

        return false
    }

    override fun onPostExecute(success: Boolean?) {

    }

    override fun onCancelled() {

    }

    companion object {
        fun run(settings: SharedPreferences): CaptiveLoginTask {
            return CaptiveLoginTask(
                settings.getString("email", "")!!.toString(),
                settings.getString("username", "")!!.toString(),
                settings.getString("password", "")!!.toString()
            )
        }
    }
}