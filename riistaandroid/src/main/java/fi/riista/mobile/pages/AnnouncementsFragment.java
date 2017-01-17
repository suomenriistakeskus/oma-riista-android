package fi.riista.mobile.pages;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.adapter.AnnouncementAdapter;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.network.ListAnnouncementsTask;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.util.ViewAnnotations;

public class AnnouncementsFragment extends PageFragment {
    List<Announcement> mDataModels = new ArrayList<>();
    AnnouncementAdapter mAdapter;

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setViewTitle(getString(R.string.title_announcements));
        fetchAnnouncements();
    }

    void setDataModels(@NonNull List<Announcement> dataModels) {
        mDataModels = dataModels;

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

    void fetchAnnouncements() {
        mProgress.setVisibility(View.VISIBLE);

        ListAnnouncementsTask task = new ListAnnouncementsTask(getWorkContext()) {
            @Override
            protected void onFinishObjects(List<Announcement> results) {
                mStatusText.setVisibility(View.GONE);
                if (isAdded()) {
                    AnnouncementsFragment.this.setDataModels(results);
                }
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Failed to fetch announcements: " + getError().getMessage());

                if (isAdded()) {
                    mStatusText.setText(getString(R.string.announcements_fetch_failed));
                    mStatusText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onEnd() {
                if (isAdded()) {
                    mProgress.setVisibility(View.GONE);
                }
            }
        };
        task.start();
    }
}
