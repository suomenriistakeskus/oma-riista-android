package fi.riista.mobile.ui;

import android.location.Location;

import java.util.Calendar;
import java.util.List;

import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.srva.SrvaEvent;

public class GameLogListItem {

    public interface OnClickListItemListener {
        void onItemClick(GameLogListItem item);
    }

    public boolean isHeader = false;
    public boolean isStats = false;
    public int year;
    public int month;

    public GameHarvest mHarvest;
    public GameObservation mObservation;
    public SrvaEvent mSrva;

    public Integer speciesCode;
    public Integer totalSpecimenAmount;
    public Calendar dateTime;
    public String type;
    public Location location;
    public List<GameLogImage> images;

    public boolean sent;

    public boolean isTimelineTopVisible;
    public boolean isTimelineBottomVisible;

    public static GameLogListItem fromHarvest(final GameHarvest harvest) {
        final GameLogListItem item = new GameLogListItem();

        item.speciesCode = harvest.mSpeciesID;
        item.totalSpecimenAmount = harvest.mAmount;
        item.dateTime = harvest.mTime;
        item.type = GameLog.TYPE_HARVEST;
        item.location = harvest.mLocation;
        item.sent = harvest.mSent;
        item.images = harvest.mImages;

        item.mHarvest = harvest;

        item.month = item.dateTime.get(Calendar.MONTH);
        item.year = item.dateTime.get(Calendar.YEAR);

        return item;
    }

    public static GameLogListItem fromObservation(final GameObservation observation) {
        final GameLogListItem item = new GameLogListItem();

        final int amount = observation.getMooselikeSpecimenCount() == 0 && observation.totalSpecimenAmount != null
                ? observation.totalSpecimenAmount
                : observation.getMooselikeSpecimenCount();

        item.speciesCode = observation.gameSpeciesCode;
        item.totalSpecimenAmount = amount;
        item.dateTime = observation.toDateTime().toCalendar(null);
        item.type = observation.type;
        item.location = observation.toLocation();
        item.sent = !observation.modified;
        item.images = observation.getImages();

        item.mObservation = observation;

        item.month = item.dateTime.get(Calendar.MONTH);
        item.year = item.dateTime.get(Calendar.YEAR);

        return item;
    }

    public static GameLogListItem fromSrva(final SrvaEvent srva) {
        final GameLogListItem item = new GameLogListItem();

        item.speciesCode = srva.gameSpeciesCode;
        item.totalSpecimenAmount = srva.totalSpecimenAmount;
        item.dateTime = srva.toDateTime().toCalendar(null);
        item.type = srva.type;
        item.location = srva.toLocation();
        item.sent = !srva.modified;
        item.images = srva.getImages();

        item.mSrva = srva;

        item.month = item.dateTime.get(Calendar.MONTH);
        item.year = item.dateTime.get(Calendar.YEAR);

        return item;
    }
}
