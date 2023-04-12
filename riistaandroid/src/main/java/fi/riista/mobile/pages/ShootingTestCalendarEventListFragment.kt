package fi.riista.mobile.pages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResultWithData
import fi.riista.mobile.R
import fi.riista.mobile.activity.ShootingTestMainActivity
import fi.riista.mobile.adapter.ShootingTestCalendarEventsAdapter
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent
import fi.riista.mobile.riistaSdkHelpers.toShootingTestCalendarEvent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ShootingTestCalendarEventListFragment : PageFragment() {
    private var dataModels: List<ShootingTestCalendarEvent> = mutableListOf()
    private lateinit var adapter: ShootingTestCalendarEventsAdapter
    private val shootingTestContext = RiistaSDK.shootingTestContext

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shooting_test_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar(R.layout.actionbar_shooting_test_list, false)
        adapter = ShootingTestCalendarEventsAdapter(dataModels, activity)
        val listView = view.findViewById<ListView>(R.id.shooting_test_list)
        listView.adapter = adapter
        listView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val clickedEvent = dataModels[position]
                val intent = Intent(activity, ShootingTestMainActivity::class.java)
                intent.putExtra(EXTRA_CALENDAR_EVENT_ID, clickedEvent.calendarEventId)
                startActivity(intent)
            }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        MainScope().launch {
            val response = shootingTestContext.fetchShootingTestCalendarEvents()

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResultWithData.Success -> {
                    dataModels = response.data.map { it.toShootingTestCalendarEvent() }
                    adapter.clear()
                    adapter.addAll(dataModels)
                    adapter.notifyDataSetChanged()
                }
                else -> {
                    // No-op
                }
            }
        }
    }

    companion object {
        const val EXTRA_CALENDAR_EVENT_ID = "extra_calendar_event_id"
    }
}
