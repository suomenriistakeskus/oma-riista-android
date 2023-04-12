package fi.riista.mobile.models;

import static java.util.Objects.requireNonNull;

import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fi.riista.mobile.database.HarvestDbHelper;
import fi.riista.mobile.database.SpeciesInformation;

public class GameHarvest implements Serializable {

    public static final String HARVEST_REJECTED = "REJECTED";

    public int mHarvestSpecVersion;

    public int mId = 0;
    public int mLocalId = -1;
    public long mMobileClientRefId;

    // Tracks whether last local update is sent to backend
    public boolean mSent = false;
    public boolean mRemote = false;
    public int mRev = 0;
    public HarvestDbHelper.UpdateType mPendingOperation = HarvestDbHelper.UpdateType.NONE;
    public boolean mCanEdit = true;

    public Integer mSpeciesID;

    // Not serializable. Must handle serialization manually.
    public transient Location mLocation;
    // Not serializable. Must handle serialization manually.
    public transient Pair<Long, Long> mCoordinates;

    public float mAccuracy = 0;
    public boolean mHasAltitude = false;
    public double mAltitude = 0;
    public double mAltitudeAccuracy = 0;
    public String mLocationSource;

    public Calendar mTime;
    public int mAmount;
    public String mDescription;

    public boolean mHarvestReportDone = false;
    public boolean mHarvestReportRequired = false;
    public String mHarvestReportState;

    public String mPermitNumber;
    public String mPermitType;
    public String mStateAcceptedToHarvestPermit;

    public DeerHuntingType mDeerHuntingType;
    public String mDeerHuntingOtherTypeDescription;
    public Boolean mFeedingPlace;
    public GreySealHuntingMethod mHuntingMethod;
    public Boolean mTaigaBeanGoose;

    public List<HarvestSpecimen> mSpecimen = new ArrayList<>();
    public List<GameLogImage> mImages;

    public GameHarvest(final int harvestSpecVersion,
                       @Nullable final Integer speciesID,
                       @Nullable final Location location,
                       @NonNull final Calendar time,
                       final int amount,
                       @NonNull final List<GameLogImage> images) {

        mHarvestSpecVersion = harvestSpecVersion;

        mSpeciesID = speciesID;
        mLocation = location;
        mTime = requireNonNull(time);
        mAmount = amount;
        mImages = requireNonNull(images);
    }

    public boolean isMoose() {
        return mSpeciesID == SpeciesInformation.MOOSE_ID;
    }

    public boolean isDeer() {
        return mSpeciesID == SpeciesInformation.FALLOW_DEER_ID ||
                mSpeciesID == SpeciesInformation.WHITE_TAILED_DEER_ID ||
                mSpeciesID == SpeciesInformation.WILD_FOREST_DEER_ID;
    }

    public void setSpecimens(@NonNull final List<HarvestSpecimen> specimens) {
        mSpecimen = requireNonNull(specimens);
    }

    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();

        final Long first = in.readLong();
        final Long second = in.readLong();
        mCoordinates = new Pair<>(first, second);

        mLocation = new Location(in.readUTF());
        mLocation.setLatitude(in.readDouble());
        mLocation.setLongitude(in.readDouble());
        mLocation.setAccuracy(in.readFloat());
        mLocation.setAltitude(in.readDouble());
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeLong(mCoordinates != null ? mCoordinates.first : -1);
        out.writeLong(mCoordinates != null ? mCoordinates.second : -1);

        out.writeUTF(mLocation != null ? mLocation.getProvider() : null);
        out.writeDouble(mLocation != null ? mLocation.getLatitude() : -1);
        out.writeDouble(mLocation != null ? mLocation.getLongitude() : -1);
        out.writeFloat(mLocation != null ? mLocation.getAccuracy() : -1);
        out.writeDouble(mLocation != null ? mLocation.getAltitude() : -1);
    }
}
