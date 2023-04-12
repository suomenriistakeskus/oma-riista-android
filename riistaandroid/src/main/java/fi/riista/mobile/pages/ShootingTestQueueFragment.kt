package fi.riista.mobile.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.core.util.ObjectsCompat.requireNonNull
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.activity.ShootingTestUserAttemptsActivity
import fi.riista.mobile.adapter.ShootingTestQueueAdapter
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel
import javax.inject.Inject

class ShootingTestQueueFragment : ShootingTestTabContentFragment() {
    @JvmField
    @Inject
    var viewModelFactory: ViewModelProvider.Factory? = null

    private lateinit var viewModel: ShootingTestMainViewModel
    private lateinit var shootingTestQueueAdapter: ShootingTestQueueAdapter

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shooting_test_queue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory!!)[ShootingTestMainViewModel::class.java]
        shootingTestQueueAdapter = ShootingTestQueueAdapter(ArrayList(), requireActivity())

        val participantObserver = Observer { shootingTestParticipants: List<ShootingTestParticipant>? ->
            shootingTestQueueAdapter.clear()
            if (shootingTestParticipants != null) {
                for (participant in shootingTestParticipants) {
                    if (!participant.completed) {
                        shootingTestQueueAdapter.add(participant)
                    }
                }
            }
            shootingTestQueueAdapter.sort { o1: ShootingTestParticipant?, o2: ShootingTestParticipant? ->
                if (o1 == null) {
                    return@sort -1
                } else if (o2 == null) {
                    return@sort 1
                } else if (o1.attempts.isEmpty() && o2.attempts.isNotEmpty()) {
                    return@sort -1
                } else if (o1.attempts.isNotEmpty() && o2.attempts.isEmpty()) {
                    return@sort 1
                } else {
                    return@sort o1.registrationTime?.compareTo(o2.registrationTime ?: "") ?: -1
                }
            }
            shootingTestQueueAdapter.notifyDataSetChanged()
        }
        viewModel.participants.observe(viewLifecycleOwner, participantObserver)

        val listView = view.findViewById<ListView>(R.id.shooting_test_queue)
        listView.adapter = shootingTestQueueAdapter
        listView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val clickedItem = requireNonNull(
                    shootingTestQueueAdapter.getItem(position)
                )
                val intent = Intent(activity, ShootingTestUserAttemptsActivity::class.java)
                intent.putExtra(ShootingTestUserAttemptsActivity.EXTRA_PARTICIPANT_ID, clickedItem.id)
                intent.putExtra(ShootingTestUserAttemptsActivity.EXTRA_TEST_COMPLETED, clickedItem.completed)
                startActivity(intent)
            }

    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        viewModel.refreshCalendarEvent()
        viewModel.refreshParticipants()
    }
}
