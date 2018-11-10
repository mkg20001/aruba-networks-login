package io.mkg20001.arubanetworkslogin

import android.content.Context
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
class CaptiveLoginTask internal constructor(/*private val context: Context,*/ private val mEmail: String, private val mUsername: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void): Boolean? {
        try {
            CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL ))

            var webb = Webb.create()
            webb.setDefaultHeader(Webb.HDR_USER_AGENT, "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:63.0) Gecko/20100101 Firefox/63.0")

            Log.v(TAG, "Captive task started... Detecting portal")

            var output = webb.get("http://detectportal.firefox.com").asString().body
            if (output.matches(Regex("^success$"))) {
                Log.v(TAG, "No captive detected! Yay!")
                return true
            }
            var urlMatch = Regex("(http://detectportal.+)'").find(output)

            if (urlMatch == null) {
                Log.v(TAG, "Not aruba captive!")
                return false
            }

            var extractedUrl = urlMatch.groupValues[1]
            Log.v(TAG, "Extracted url $extractedUrl")

            var redir = webb.get(extractedUrl).followRedirects(true).ensureSuccess().uri
            Log.v(TAG, "Final $redir")

            var post = Regex("\\\\?.+").replace(redir, "")
            Log.v(TAG, "Post $post")

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
                Log.v(TAG, "Auth failed!")
                return false
            }

            return true
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }

        return false
    }

    override fun onPostExecute(success: Boolean) {
        Log.v(TAG, "Finished, success $success")
        /* if (!success) {
            Toast.makeText(context.applicationContext, R.string.login_fail,
                Toast.LENGTH_LONG).show()
        } */
    }

    override fun onCancelled() {
        Log.v(TAG, "Cancelled")
    }

    companion object {
        private val TAG = Utils.TAG
        
        fun run(context: Context): CaptiveLoginTask {
            val settings = context.getSharedPreferences("UserInfo", 0)
            val task = CaptiveLoginTask(
                // context.applicationContext,
                settings.getString("email", "")!!.toString(),
                settings.getString("username", "")!!.toString(),
                settings.getString("password", "")!!.toString()
            )
            task.execute(null as Void?)
            return task
        }
    }
}