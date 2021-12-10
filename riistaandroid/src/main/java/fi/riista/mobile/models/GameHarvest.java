package fi.riista.mobile.models;

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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class GameHarvest implements Serializable {

    public static final String HARVEST_CREATEREPORT = "createReport";
    public static final String HARVEST_PROPOSED = "PROPOSED";
    public static final String HARVEST_SENT_FOR_APPROVAL = "SENT_FOR_APPROVAL";
    public static final String HARVEST_APPROVED = "APPROVED";
    public static final String HARVEST_REJECTED = "REJECTED";

    public static final String PERMIT_PROPOSED = "PROPOSED";
    public static final String PERMIT_ACCEPTED = "ACCEPTED";
    public static final String PERMIT_REJECTED = "REJECTED";

    public static final int MAX_AMOUNT = 9999;

    // TODO This can be removed
    public final String mType = GameLog.TYPE_HARVEST;

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

    public static GameHarvest createNew(final int harvestSpecVersion) {
        final GameHarvest harvest = new GameHarvest(
                harvestSpecVersion,
                null, // speciesID
                new Location(""),
                Calendar.getInstance(),
                1, // amount
                new ArrayList<>(1));

        harvest.mMobileClientRefId = GameLog.generateMobileRefId();

        return harvest;
    }

    public GameHarvest deepClone() {
        final GameHarvest copy = new GameHarvest(
                this.mHarvestSpecVersion,
                this.mSpeciesID,
                this.mLocation,
                this.mTime,
                this.mAmount,
                this.mImages);

        copy.mId = this.mId;
        copy.mLocalId = this.mLocalId;
        copy.mMobileClientRefId = this.mMobileClientRefId;

        copy.mSent = this.mSent;
        copy.mRemote = this.mRemote;
        copy.mRev = this.mRev;
        copy.mPendingOperation = this.mPendingOperation;
        copy.mCanEdit = this.mCanEdit;

        // A copy needs to be made from mutable Location object.
        copy.mLocation = getCopyOfLocation();

        copy.mCoordinates = this.mCoordinates;
        copy.mAccuracy = this.mAccuracy;
        copy.mHasAltitude = this.mHasAltitude;
        copy.mAltitude = this.mAltitude;
        copy.mAltitudeAccuracy = this.mAltitudeAccuracy;
        copy.mLocationSource = this.mLocationSource;

        copy.mDescription = this.mDescription;

        copy.mHarvestReportDone = this.mHarvestReportDone;
        copy.mHarvestReportRequired = this.mHarvestReportRequired;
        copy.mHarvestReportState = this.mHarvestReportState;

        copy.mPermitNumber = this.mPermitNumber;
        copy.mPermitType = this.mPermitType;
        copy.mStateAcceptedToHarvestPermit = this.mStateAcceptedToHarvestPermit;

        copy.mDeerHuntingType = this.mDeerHuntingType;
        copy.mDeerHuntingOtherTypeDescription = this.mDeerHuntingOtherTypeDescription;
        copy.mFeedingPlace = this.mFeedingPlace;
        copy.mHuntingMethod = this.mHuntingMethod;
        copy.mTaigaBeanGoose = this.mTaigaBeanGoose;

        final ArrayList<HarvestSpecimen> copyOfSpecimens = new ArrayList<>(this.mSpecimen.size());
        for (final HarvestSpecimen specimen : this.mSpecimen) {
            copyOfSpecimens.add(specimen.createCopy());
        }
        copy.setSpecimens(copyOfSpecimens);

        if (this.mImages != null) {
            copy.mImages = new ArrayList<>(this.mImages.size());
            copy.mImages.addAll(this.mImages);
        } else {
            copy.mImages = new ArrayList<>(1);
        }

        return copy;
    }

    // Exposed as public to enable Location mocking in tests.
    public Location getCopyOfLocation() {
        return new Location(mLocation);
    }

    public boolean isPersistedLocally() {
        return mLocalId > 0;
    }

    // Location with zero latitude or longitude is considered an uninitialized location.
    public boolean isLocationSet() {
        return mLocation != null && mLocation.getLatitude() != 0 && mLocation.getLongitude() != 0;
    }

    public static boolean isAmountWithinLegalRange(final int amount) {
        return amount >= 1 && amount <= MAX_AMOUNT;
    }

    public static void assertAmountWithinLegalRange(final int amount) {
        if (!isAmountWithinLegalRange(amount)) {
            throw new IllegalArgumentException(
                    format("Amount must be between 1 and %d: %d", MAX_AMOUNT, amount));
        }
    }

    public boolean isAmountWithinLegalRange() {
        return isAmountWithinLegalRange(mAmount);
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
