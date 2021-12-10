package fi.riista.mobile.models;

import androidx.annotation.Nullable;

public enum GreySealHuntingMethod {

    SHOT,
    CAPTURED_ALIVE,
    SHOT_BUT_LOST;

    public static GreySealHuntingMethod fromString(@Nullable final String s) {
        return s != null ? valueOf(s) : null;
    }

    public static String toString(@Nullable final GreySealHuntingMethod type) {
        return type != null ? type.name() : null;
    }
}
