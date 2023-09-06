package fi.riista.mobile.feature.permits

import android.os.Bundle
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.permits.metsahallitusPermits.list.ListMetsahallitusPermitsFragment

class ListPermitsActivity
    : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_permits)

        setCustomTitle(getString(R.string.my_details_mh_permits_title))

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, ListMetsahallitusPermitsFragment())
            .commit()
    }
}
