package fi.riista.mobile.pages;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.adapter.AnnouncementAdapter;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.storage.AnnouncementSync;
import fi.riista.mobile.storage.StorageDatabase;
import fi.vincit.androidutilslib.util.ViewAnnotations;

public class AnnouncementsFragment extends PageFragment {
    List<Announcement> mDataModels = new ArrayList<>();
    AnnouncementAdapter mAdapter;

    @ViewAnnotations.ViewId(R.id.fragment_announcements)
    SwipeRefreshLayout mSwipeContainer;

    @ViewAnnotations.ViewId(R.id.announcements_state)
    TextView mStatusText;

    @ViewAnnotations.ViewId(R.id.announcement_progress)
    ProgressBar mProgress;

    public static AnnouncementsFragment newInstance() {
        return new AnnouncementsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);
        ListView listView = (ListView) view.findViewById(R.id.announcement_list);

        ViewAnnotations.apply(this, view);

        mAdapter = new AnnouncementAdapter(mDataModels, getActivity().getApplicationContext());

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView body = (TextView) view.findViewById(R.id.announcement_message);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    boolean isExpanded = body.getMaxLines() == Integer.MAX_VALUE;

                    if (isExpanded) {
                        // Collapse
                        body.setMaxLines(3);
                        view.findViewById(R.id.announcement_display_all).setVisibility(View.VISIBLE);
                    } else {
                        // Expand
                        body.setMaxLines(Integer.MAX_VALUE);
                        view.findViewById(R.id.announcement_display_all).setVisibility(View.GONE);
                    }
                } else {
                }
            }
        });

        setupPullToRefresh();

        return view;
    }

    private void setupPullToRefresh() {
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AnnouncementSync syncer = new AnnouncementSync();
                syncer.sync(new AnnouncementSync.AnnouncementSyncListener() {
                    @Override
                    public void onFinish() {
                        mSwipeContainer.setRefreshing(false);

                        refreshAnnouncements();
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        setViewTitle(getString(R.string.title_announcements));
        ((BaseActivity) getActivity()).onHasActionbarMenu(false);

        refreshAnnouncements();
    }

    void setDataModels(@NonNull List<Announcement> dataModels) {
        mDataModels = dataModels;

        // Sort descending by datetime
        Collections.sort(mDataModels, new Comparator<Announcement>() {
            @Override
            public int compare(Announcement a1, Announcement a2) {
                return a2.pointOfTime.compareTo(a1.pointOfTime);
            }
        });

        if (mDataModels.size() > 0) {
            mStatusText.setVisibility(View.GONE);
        } else {
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(getString(R.string.announcements_none));
        }

        mAdapter.clear();
        mAdapter.addAll(mDataModels);
        mAdapter.notifyDataSetChanged();
    }

    void refreshAnnouncements() {
        mProgress.setVisibility(View.VISIBLE);

        StorageDatabase.getInstance().fetchAnnouncements(new StorageDatabase.AnnouncementsListener() {
            @Override
            public void onFinish(List<Announcement> announcements) {
                mStatusText.setVisibility(View.GONE);
                mProgress.setVisibility(View.GONE);

                if (isAdded()) {
                    AnnouncementsFragment.this.setDataModels(announcements);
                }
            }
        });
    }
}
