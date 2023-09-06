package fi.riista.mobile.feature.moreView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.reactive.AppObservable
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.mobile.R
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.utils.UserInfoStore
import javax.inject.Inject

class MoreViewFragment : PageFragment() {

    @Inject
    lateinit var userInfoStore: UserInfoStore

    private var groupHuntingAvailable = false
    private var huntingControlAvailable = false
    private var shootingTestsAvailable = false

    interface InteractionManager {
        val groupHuntingAvailable: AppObservable<Boolean>
        val huntingControlAvailable: AppObservable<Boolean>
        fun moreItemClicked(type: MoreItemType)
    }

    private lateinit var adapter: MoreRecyclerViewAdapter
    private lateinit var interactionManager: InteractionManager
    private val disposeBag = DisposeBag()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more_view, container, false)
        setupActionBar(R.layout.actionbar_more, false)

        adapter = MoreRecyclerViewAdapter(
            layoutInflater = layoutInflater,
            moreItemClickListener = { type -> interactionManager.moreItemClicked(type) },
        )
        view.findViewById<RecyclerView>(R.id.rv_items).also {
            it.adapter = adapter
            it.addItemDecoration(DividerItemDecoration(it.context, DividerItemDecoration.VERTICAL))
        }

        val userInfo = userInfoStore.getUserInfo()
        shootingTestsAvailable =  (userInfo != null) && userInfo.enableShootingTests

        interactionManager.huntingControlAvailable.bindAndNotify { available ->
            if (available != huntingControlAvailable) {
                huntingControlAvailable = available
                adapter.setItems(createItems())
            }
        }.disposeBy(disposeBag)
        interactionManager.groupHuntingAvailable.bindAndNotify { available ->
            if (available != groupHuntingAvailable) {
                groupHuntingAvailable = available
                adapter.setItems(createItems())
            }
        }.disposeBy(disposeBag)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeBag.disposeAll()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)

        interactionManager = context as InteractionManager
        groupHuntingAvailable = interactionManager.groupHuntingAvailable.value
        huntingControlAvailable = interactionManager.huntingControlAvailable.value
    }

    override fun onResume() {
        super.onResume()
        adapter.setItems(createItems())
    }

    private fun createItems(): List<MoreItem> = listOfNotNull(
        MoreItem(MoreItemType.MY_DETAILS, R.drawable.ic_person, getString(R.string.more_my_details)),
        MoreItem(MoreItemType.GALLERY, R.drawable.ic_camera, getString(R.string.more_gallery)),
        MoreItem(MoreItemType.CONTACT_DETAILS, R.drawable.ic_messages, getString(R.string.more_contact_details)),
        MoreItem(MoreItemType.SETTINGS, R.drawable.ic_settings, getString(R.string.more_settings)),
        MoreItem(
            MoreItemType.SHOOTING_TESTS, R.drawable.ic_harvest, getString(R.string.more_shooting_test_list)
        ).takeIf { shootingTestsAvailable },
        MoreItem(
            MoreItemType.HUNTING_DIRECTOR, R.drawable.ic_group_hunting, getString(R.string.more_hunting_group_leader)
        ).takeIf { groupHuntingAvailable },
        MoreItem(
            MoreItemType.HUNTING_CONTROL, R.drawable.ic_hunting_control, getString(R.string.more_hunting_control)
        ).takeIf { huntingControlAvailable },
        MoreItem(MoreItemType.SUNRISE_AND_SUNSET, R.drawable.ic_sunset, getString(R.string.sunrise_and_sunset_title)),
        MoreItem(MoreItemType.EVENT_SEARCH, R.drawable.ic_search, getString(R.string.more_event_search), true),
        MoreItem(MoreItemType.MAGAZINE, R.drawable.ic_metsastaja_logo, getString(R.string.more_show_magazine), true),
        MoreItem(MoreItemType.SEASONS, R.drawable.ic_calendar, getString(R.string.more_view_hunting_seasons), true),
        MoreItem(MoreItemType.ABOUT, R.drawable.ic_info, getString(R.string.more_about)),
        MoreItem(MoreItemType.LOGOUT, R.drawable.ic_logout, getString(R.string.logout)),
    )
}
