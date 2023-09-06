package fi.riista.mobile.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import fi.riista.mobile.R

open class AddMenuProvider(
    private val onAddClicked: () -> Boolean
) : MenuProvider {
    private var addMenuItem: MenuItem? = null

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_add, menu)

        addMenuItem = menu.findItem(R.id.item_add)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.item_add -> onAddClicked()
            else -> false
        }
    }
}
