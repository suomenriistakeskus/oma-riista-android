package fi.riista.mobile.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import fi.riista.mobile.R

class OwnHarvestsMenuProvider(
    private val onOwnHarvestsClicked: () -> Boolean
) : MenuProvider {
    private var ownHarvestsMenuItem: MenuItem? = null
    private var menuVisibility = false
    private var ownHarvests = true

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_own_harvests, menu)

        ownHarvestsMenuItem = menu.findItem(R.id.item_own_harvest)
        updateIcon()
        ownHarvestsMenuItem?.isVisible = menuVisibility
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.item_own_harvest -> onOwnHarvestsClicked()
            else -> false
        }
    }

    fun setOwnHarvests(ownHarvests: Boolean) {
        this.ownHarvests = ownHarvests
        updateIcon()
    }

    fun setVisibility(visible: Boolean) {
        menuVisibility = visible
        ownHarvestsMenuItem?.isVisible = visible
    }

    private fun updateIcon() {
        if (ownHarvests) {
            ownHarvestsMenuItem?.setIcon(R.drawable.ic_person_white)
        } else {
            ownHarvestsMenuItem?.setIcon(R.drawable.ic_group)
        }
    }
}
