package fi.riista.mobile.models.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

import static fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_FI;
import static fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_SV;

public class AnnouncementSender implements Serializable {
    @JsonProperty("fullName")
    public String fullName;

    @JsonProperty("title")
    public Map<String, String> title;

    @JsonProperty("organisation")
    public Map<String, String> organisation;

    public String getTitle(final String languageCode) {
        return getLocalisedTextFromMap(title, languageCode);
    }

    public String getOrganisation(final String languageCode) {
        return getLocalisedTextFromMap(organisation, languageCode);
    }

    private static String getLocalisedTextFromMap(final Map<String, String> localisations,
                                                  final String languageCode) {

        if (localisations == null) {
            return null;
        } else if (languageCode == null) {
            return localisations.get(LANGUAGE_CODE_FI);
        }

        final String localisedTitle = localisations.get(languageCode);

        return localisedTitle != null || LANGUAGE_CODE_FI.equals(languageCode)
                ? localisedTitle
                : localisations.get(LANGUAGE_CODE_FI);
    }
}
