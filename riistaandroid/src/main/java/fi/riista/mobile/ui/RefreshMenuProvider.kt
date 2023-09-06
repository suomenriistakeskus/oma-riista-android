package fi.riista.mobile.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import fi.riista.mobile.R
import fi.riista.mobile.utils.Constants

/**
 * A [MenuProvider] that is able to provide refresh menu.
 */
open class RefreshMenuProvider(
    private val onRefreshClicked: () -> Boolean
) : MenuProvider {
    private var refreshMenuItem: MenuItem? = null
    private var menuVisibility = false

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_refresh, menu)

        refreshMenuItem = menu.findItem(R.id.item_refresh)
        refreshMenuItem?.isVisible = menuVisibility
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.item_refresh -> onRefreshClicked()
            else -> false
        }
    }

    fun setVisibility(visible: Boolean) {
        menuVisibility = visible
        refreshMenuItem?.isVisible = visible
    }

    fun setCanRefresh(canRefresh: Boolean) {
        refreshMenuItem?.let { item ->
            item.isEnabled = canRefresh
            item.icon?.alpha = when (canRefresh) {
                true -> 255
                false -> Constants.DISABLED_ALPHA
            }
        }
    }
}
