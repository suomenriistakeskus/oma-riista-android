package fi.riista.mobile;

import kotlin.UninitializedPropertyAccessException;

public class AppConfig {

    private static String BASE_ADDRESS = null;
    private static String BASE_URL = null;

    /**
     * Set server base address. Needs to be called in an early phase when application is starting.
     */
    public static void initializeBaseAddress(final String baseAddress) {
        BASE_ADDRESS = baseAddress;
        BASE_URL = BASE_ADDRESS + "/api/mobile/v2";
    }

    public static String getBaseAddress() {
        if (BASE_ADDRESS == null) {
            throw new UninitializedPropertyAccessException("getBaseAddress called before BaseAddress is initialized");
        }
        return BASE_ADDRESS;
    }

    public static String getBaseUrl() {
        if (BASE_ADDRESS == null) {
            throw new UninitializedPropertyAccessException("getBaseUrl called before BaseUrl is initialized");
        }
        return BASE_URL;
    }

    // Data format versions
    // Stored with locally saved entry data to detect situations where local data is obsolete and must be
    // overwritten. Sent with API calls since some operations require up-to-date client (permit numbers etc.)
    // Increment version when new fields have been added to data received from server.

    public static final int HARVEST_SPEC_VERSION = 9;

    public static final int OBSERVATION_SPEC_VERSION = 4;
    public static final int SRVA_SPEC_VERSION = 2;

}
