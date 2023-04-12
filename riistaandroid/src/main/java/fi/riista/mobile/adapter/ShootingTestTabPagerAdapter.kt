package fi.riista.mobile.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fi.riista.mobile.R
import fi.riista.mobile.pages.ShootingTestEventFragment
import fi.riista.mobile.pages.ShootingTestPaymentsFragment
import fi.riista.mobile.pages.ShootingTestQueueFragment
import fi.riista.mobile.pages.ShootingTestRegisterFragment

class ShootingTestTabPagerAdapter(
    private val fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TAB_INDEX_EVENT -> ShootingTestEventFragment()
            TAB_INDEX_REGISTER -> ShootingTestRegisterFragment()
            TAB_INDEX_QUEUE -> ShootingTestQueueFragment()
            TAB_INDEX_PAYMENTS -> ShootingTestPaymentsFragment()
            else -> throw RuntimeException("Invalid tab position")
        }
    }

    fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            TAB_INDEX_EVENT -> return fragmentActivity.getString(R.string.shooting_test_tab_event)
            TAB_INDEX_REGISTER -> return fragmentActivity.getString(R.string.shooting_test_tab_register)
            TAB_INDEX_QUEUE -> return fragmentActivity.getString(R.string.shooting_test_tab_queue)
            TAB_INDEX_PAYMENTS -> return fragmentActivity.getString(R.string.shooting_test_tab_payments)
        }
        return null
    }

    companion object {
        const val TAB_INDEX_EVENT = 0
        const val TAB_INDEX_REGISTER = 1
        const val TAB_INDEX_QUEUE = 2
        const val TAB_INDEX_PAYMENTS = 3
        private const val TAB_COUNT = 4
    }
}
