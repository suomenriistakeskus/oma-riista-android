package fi.riista.mobile.database;

import fi.riista.mobile.models.GameHarvest;

public class DiaryEntryUpdate {
    public GameHarvest event;
    public UpdateType type;

    enum UpdateType {
        INSERT,
        UPDATE,
        DELETE
    }
}
