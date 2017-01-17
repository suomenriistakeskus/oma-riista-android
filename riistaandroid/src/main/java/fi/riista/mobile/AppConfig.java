package fi.riista.mobile;

import fi.riista.mobile.database.DiaryDataSource;

public class AppConfig {
    public static final String BASE_ADDRESS = "https://oma.riista.fi";
    public static final String BASE_URL = BASE_ADDRESS + "/api/mobile/v2";

    public static final String SERVER_DATE_FORMAT = DiaryDataSource.ISO_8601;

    // Data format versions
    // Stored with locally saved entry data to detect situations where local data is obsolete and must be
    // overwritten. Sent with API calls since some operations require up-to-date client (permit numbers etc.)
    // Increment version when new fields have been added to data received from server.

    public static final int HARVEST_SPEC_VERSION = 4;
    public static final int OBSERVATION_SPEC_VERSION = 2;
    public static final int SRVA_SPEC_VERSION = 1;
}
