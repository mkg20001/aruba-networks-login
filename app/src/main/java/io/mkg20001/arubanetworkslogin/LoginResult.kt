package io.mkg20001.arubanetworkslogin

import java.util.*

enum class RESULT_TYPE(val text: Int) {
    OK_SUCCESS(R.string.r_ok_success),
    OK_NO_CAPTIVE(R.string.r_ok_no_captive),
    ERROR_NOT_ARUBA(R.string.r_err_not_aruba),
    ERROR_INVALID_CREDS(R.string.r_err_invalid_creds),
    ERROR_OTHER(R.string.r_err_other)
}

class LoginResult(val occured: Date, val result: RESULT_TYPE) {
    val str = occured.time.toString() + "-" + result.name

    companion object {
        fun load(str: String): LoginResult {
            val splt = str.split(Regex("-"))
            return LoginResult(Date(splt[0].toLong()), RESULT_TYPE.valueOf(splt[1]))
        }

        fun create(result: RESULT_TYPE): LoginResult {
            return LoginResult(Date(), result)
        }
    }
}