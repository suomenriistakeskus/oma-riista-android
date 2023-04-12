package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.adapter.AnnouncementsAdapter
import fi.riista.mobile.models.announcement.Announcement
import fi.riista.mobile.storage.StorageDatabase
import fi.riista.mobile.sync.AnnouncementSync
import fi.vincit.androidutilslib.util.ViewAnnotations
import java.util.*
import javax.inject.Inject

class AnnouncementsFragment : PageFragment() {

    @Inject
    internal lateinit var announcementSync: AnnouncementSync

    private var mDataModels: List<Announcement> = ArrayList()
    private lateinit var mAdapter: AnnouncementsAdapter
    private var syncOnResume: Boolean = false

    @ViewAnnotations.ViewId(R.id.fragment_announcements)
    private lateinit var mSwipeContainer: SwipeRefreshLayout

    @ViewAnnotations.ViewId(R.id.announcements_state)
    private lateinit var mStatusText: TextView

    @ViewAnnotations.ViewId(R.id.announcement_progress)
    private lateinit var mProgress: ProgressBar

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_announcements, container, false)
        setupActionBar(R.layout.actionbar_announcements, false)

        val listView = view.findViewById<ListView>(R.id.announcement_list)

        ViewAnnotations.apply(this, view)

        mAdapter = AnnouncementsAdapter(mDataModels, this.requireContext())

        arguments?.run {
            syncOnResume = getBoolean(SYNC_ON_RESUME, false)
        }

        listView.adapter = mAdapter
        listView.setOnItemClickListener { _, _, i, _ ->
            val dialog = AnnouncementDialogFragment.newInstance(mDataModels[i])
            val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            dialog.show(fragmentTransaction, AnnouncementDialogFragment.TAG)
        }

        setupPullToRefresh()

        return view
    }

    private fun setupPullToRefresh() {
        mSwipeContainer.setOnRefreshListener { syncAndRefresh() }
        mSwipeContainer.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun syncAndRefresh() = announcementSync.sync {
        mSwipeContainer.isRefreshing = false

        // Sync only once.
        syncOnResume = false

        refreshAnnouncements()
    }

    override fun onResume() {
        super.onResume()

        refreshAnnouncements()

        if (syncOnResume) {
            syncAndRefresh()
        }
    }

    private fun setDataModels(dataModels: List<Announcement>) {
        mDataModels = dataModels.sortedByDescending { a -> a.pointOfTime }

        if (mDataModels.isNotEmpty()) {
            mStatusText.visibility = View.GONE
        } else {
            mStatusText.visibility = View.VISIBLE
            mStatusText.text = getString(R.string.announcements_none)
        }

        mAdapter.clear()
        mAdapter.addAll(mDataModels)
        mAdapter.notifyDataSetChanged()
    }

    private fun refreshAnnouncements() {
        mProgress.visibility = View.VISIBLE

        StorageDatabase.getInstance().fetchAnnouncements { announcements ->
            mStatusText.visibility = View.GONE
            mProgress.visibility = View.GONE

            if (isAdded) {
                this@AnnouncementsFragment.setDataModels(announcements)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SYNC_ON_RESUME, syncOnResume)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.run {
            syncOnResume = getBoolean(SYNC_ON_RESUME, false)
        }
    }

    companion object {

        private const val SYNC_ON_RESUME = "sync_on_resume"

        @JvmStatic
        fun newInstance(syncOnResume: Boolean): AnnouncementsFragment {
            return AnnouncementsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(SYNC_ON_RESUME, syncOnResume)
                }
            }
        }
    }
}
