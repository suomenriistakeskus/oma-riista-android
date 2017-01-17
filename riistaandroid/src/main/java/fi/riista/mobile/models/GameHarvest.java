package fi.riista.mobile.models;

import android.location.Location;
import android.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.DiaryHelper;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.utils.Utils;

public class GameHarvest extends LogEventBase implements Serializable {

    public static final String HARVEST_CREATEREPORT = "createReport";
    public static final String HARVEST_PROPOSED = "PROPOSED";
    public static final String HARVEST_SENT_FOR_APPROVAL = "SENT_FOR_APPROVAL";
    public static final String HARVEST_APPROVED = "APPROVED";
    public static final String HARVEST_REJECTED = "REJECTED";

    public static final String PERMIT_PROPOSED = "PROPOSED";
    public static final String PERMIT_ACCEPTED = "ACCEPTED";
    public static final String PERMIT_REJECTED = "REJECTED";

    public int mId = 0;
    public int mLocalId = 0;
    public int mApiDataFormat;
    public int mClientDataFormat = AppConfig.HARVEST_SPEC_VERSION;
    public Integer mSpeciesID;
    public int mAmount;
    public String mMessage;
    public Calendar mTime;
    public String mType;
    public GameObservation mObservation; //Original observation
    public SrvaEvent mSrvaEvent; //Original SRVA event

    public String mLocationSource;
    // Not serializable. Must handle serialization manually
    public transient Pair<Long, Long> mCoordinates;
    public float mAccuracy = 0;
    // Not serializable. Must handle serialization manually
    public transient Location mLocation;
    public boolean mHasAltitude = false;
    public double mAltitude = 0;
    public double mAltitudeAccuracy = 0;

    public boolean mRemote = false;
    public boolean mSent = false;
    public int mRev = 0;
    public boolean mHarvestReportDone = false;
    public boolean mHarvestReportRequired = false;
    public String mHarvestReportState;

    public boolean mCanEdit = true;

    public String mPermitNumber;
    public String mPermitType;
    public String mStateAcceptedToHarvestPermit;

    public List<Specimen> mSpecimen = new ArrayList<>();
    public List<LogImage> mImages = new ArrayList<>();
    public long mMobileClientRefId = 0;
    public DiaryHelper.UpdateType mPendingOperation = DiaryHelper.UpdateType.NONE;

    public GameHarvest(Integer speciesID, int amount, String message, Calendar time, String type, Location location,
                       List<LogImage> images) {
        mSpeciesID = speciesID;
        mAmount = amount;
        mMessage = message;
        mTime = time;
        mRev = 0;
        mType = type;
        mLocation = location;
        mImages = images;
    }

    public static GameHarvest createNew() {
        GameHarvest harvest = new GameHarvest(-1, 1, null, Calendar.getInstance(), TYPE_HARVEST, null, new ArrayList<LogImage>());
        harvest.mClientDataFormat = AppConfig.HARVEST_SPEC_VERSION;
        harvest.mLocation = new Location("");
        harvest.mType = TYPE_HARVEST;
        harvest.mCanEdit = true;
        harvest.mSent = false;
        harvest.mMobileClientRefId = GameDatabase.generateMobileRefId(new Random());

        return harvest;
    }

    /**
     * Is entry fully editable.
     * Data other than description and images can not be changed after entry is added to permit or
     * harvest report is made.
     * <p/>
     * Should remove harvestReportDone check since information is now calculated to canEdit in
     * backend?
     *
     * @return Is entry fully editable.
     */
    public boolean isEditable() {
        return mCanEdit && !mHarvestReportDone;
    }

    public boolean isMoose() {
        return mSpeciesID == Utils.MOOSE_ID;
    }

    public boolean isDeer() {
        return mSpeciesID == Utils.FALLOW_DEER_ID ||
                mSpeciesID == Utils.WHITE_TAILED_DEER ||
                mSpeciesID == Utils.WILD_FOREST_DEER;
    }

    public boolean hasNonDefaultLocation() {
        return mLocation.getLatitude() != 0 || mLocation.getLongitude() != 0;
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();

        Long first = in.readLong();
        Long second = in.readLong();
        if (first >= 0 || second >= 0) {
            mCoordinates = new Pair<>(first >= 0 ? first : null, second >= 0 ? second : null);
        }

        mLocation = new Location(in.readUTF());
        mLocation.setLatitude(in.readDouble());
        mLocation.setLongitude(in.readDouble());
        mLocation.setAccuracy(in.readFloat());
        mLocation.setAltitude(in.readDouble());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
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
