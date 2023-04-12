package fi.riista.mobile.ui

interface CanIndicateBusy {
    fun indicateBusy()
    fun hideBusyIndicators(indicatorsDismissed: () -> Unit)
}