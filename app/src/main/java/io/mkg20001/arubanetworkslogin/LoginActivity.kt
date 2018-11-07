package io.mkg20001.arubanetworkslogin

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.util.Log
import com.goebl.david.Webb

import kotlinx.android.synthetic.main.activity_login.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mLoginTask: CaptiveLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                saveCreds()
                return@OnEditorActionListener true
            }
            false
        })

        save_creds_btn.setOnClickListener { saveCreds() }
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun saveCreds() {
        if (mLoginTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val userStr = username.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        if (!TextUtils.isEmpty(passwordStr) || !TextUtils.isEmpty(userStr)) {
            // Check for a valid password
            if (TextUtils.isEmpty(passwordStr)) {
                password.error = getString(R.string.error_field_required)
                focusView = password
                cancel = true
            } else if (!isPasswordValid(passwordStr)) {
                password.error = getString(R.string.error_invalid_email)
                focusView = password
                cancel = true
            }

            // Check for a valid username
            if (TextUtils.isEmpty(userStr)) {
                password.error = getString(R.string.error_field_required)
                focusView = username
                cancel = true
            } else if (!isPasswordValid(userStr)) {
                password.error = getString(R.string.error_invalid_email)
                focusView = username
                cancel = true
            }
        }

        // Check for a valid email address, if the user entered one
        if (!TextUtils.isEmpty(emailStr) && !isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mLoginTask = CaptiveLoginTask(emailStr, userStr, passwordStr)
            mLoginTask!!.execute(null as Void?)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    fun streamToString(inputStream: InputStream): String {

        val bufferReader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var result = ""

        try {
            line = bufferReader.readLine()
            while (line != null) {
                result += line
                line = bufferReader.readLine()
            }
            inputStream.close()
        } catch (ex: Exception) {

        }

        return result
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user on the captive portal
     */
    inner class CaptiveLoginTask internal constructor(private val mEmail: String, private val mUsername: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            try {
                CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL ))

                var webb = Webb.create()
                webb.setDefaultHeader(Webb.HDR_USER_AGENT, "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:63.0) Gecko/20100101 Firefox/63.0")

                Log.v(TAG, "Captive task started... Detecting portal")

                var output = webb.get("http://detectportal.firefox.com").asString().body
                if (output == "success") {
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
                    .param("agreementAck", "accept")
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

        override fun onPostExecute(success: Boolean?) {
            mLoginTask = null
            /* showProgress(false)

            if (success!!) {
                finish()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            } */
        }

        override fun onCancelled() {
            mLoginTask = null
            // showProgress(false)
        }
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")

        private val TAG = "ARUBA_LOGIN"
    }
}
