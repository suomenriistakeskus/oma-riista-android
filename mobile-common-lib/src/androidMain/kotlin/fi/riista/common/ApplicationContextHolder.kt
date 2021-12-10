package fi.riista.common

import android.content.Context

object ApplicationContextHolder {
    // The application context if one has been registered
    var applicationContext: Context? = null
}