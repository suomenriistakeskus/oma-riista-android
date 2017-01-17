package fi.riista.mobile.database;

import fi.riista.mobile.models.GameHarvest;

/**
 * Data structure for visual elements in Game log listview
 */
public class EventItem {
    public boolean isHeader = false;
    public boolean isSeparator = false;
    public int amount = 0;
    public int year;
    public int month;
    public int mEventIndex = 0;
    public GameHarvest mEvent = null;
}
