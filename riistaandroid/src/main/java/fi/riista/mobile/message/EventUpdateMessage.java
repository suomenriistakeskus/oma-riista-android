package fi.riista.mobile.message;

/**
 * Message sent when an event has been created or updated by the user.
 */
public class EventUpdateMessage {

    public final String type;
    public final long localId;
    public final int huntingYear;
    
    public EventUpdateMessage(String type, long localId, int huntingYear) {
        this.type = type;
        this.localId = localId;
        this.huntingYear = huntingYear;
    }
    
}
