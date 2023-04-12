package fi.riista.mobile.database;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_CONTEXT_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.utils.DateTimeUtils;

/**
 * Class for handling harvest related database operations.
 * Uses HarvestDataSource class to modify local database.
 * The class handles automatic synchronizing when using automatic synchronization mode.
 */
@Singleton
public class HarvestDatabase {

    public static class SeasonStats {
        public SparseIntArray mCategoryData;

        public SeasonStats() {
            mCategoryData = new SparseIntArray();
        }
    }

    private static final String TAG = "HarvestDatabase";
    private static final String UPDATE_TIMES_KEY = "updateTimes";

    private final HarvestDataSource mDataSource;

    private final SparseArray<Date> mUpdateTimes = new SparseArray<>();

    private SharedPreferences mUpdateTimePreferences = null;

    @Inject
    public HarvestDatabase(@NonNull @Named(APPLICATION_CONTEXT_NAME) final Context context) {
        mDataSource = new HarvestDataSource(context);

        loadUpdateTimes(context);

        mDataSource.open();
    }

    private void loadUpdateTimes(final Context context) {
        mUpdateTimes.clear();
        mUpdateTimePreferences = context.getSharedPreferences(UPDATE_TIMES_KEY, Context.MODE_PRIVATE);

        final Map<String, ?> data = mUpdateTimePreferences.getAll();

        if (data != null) {
            for (final Map.Entry<String, ?> entry : data.entrySet()) {
                Calendar calendar = null;

                try {
                    final String dateTimeStr = (String) entry.getValue();

                    if (dateTimeStr != null) {
                        calendar = DateTimeUtils.parseCalendar(dateTimeStr);
                    }
                } catch (final Exception e) {
                    // Will re-init Calendar instance.
                }

                if (calendar == null) {
                    calendar = Calendar.getInstance();

                    // TODO Is this dubious looking time logic still valid?
                    calendar.set(0, Calendar.JANUARY, 0);
                }

                mUpdateTimes.put(Integer.valueOf(entry.getKey()), calendar.getTime());
            }
        }
    }

    public void setUser(final String username) {
        mDataSource.setUser(username);
    }

    public List<GameHarvest> loadNotCopiedHarvests() {
        return mDataSource.getNotCopiedHarvests();
    }

    public void setCommonLocalId(final int localId, final long commonLocalId) {
        mDataSource.setCommonLocalId(localId, commonLocalId);
    }

    public void close() {
        mDataSource.close();
    }

    /**
     * Update times need to be cleared when user logs out. Otherwise user keeps checking for updates against wrong date
     */
    public void clearUpdateTimes() {
        mUpdateTimePreferences.edit().clear().apply();
    }
}
