package fi.riista.mobile.utils

import android.os.Bundle

interface HasSavedInstanceState {
    fun saveInstanceState(outState: Bundle)
    fun restoreFromSavedInstanceState(savedInstanceState: Bundle)
}