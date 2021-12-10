package fi.riista.mobile.utils

import android.view.View

fun Boolean.toVisibility() =
    if (this) {
        View.VISIBLE
    } else {
        View.GONE
    }