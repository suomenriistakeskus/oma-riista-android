package fi.riista.mobile.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import fi.riista.mobile.R

open class SettingsMenuProvider(
    private val onSettingsClicked: () -> Boolean
) : MenuProvider {
    private var settingsMenuItem: MenuItem? = null

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_settings, menu)

        settingsMenuItem = menu.findItem(R.id.item_settings)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.item_settings -> onSettingsClicked()
            else -> false
        }
    }
}
