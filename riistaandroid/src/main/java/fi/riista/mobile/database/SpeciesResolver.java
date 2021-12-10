package fi.riista.mobile.database;

import androidx.annotation.Nullable;

import fi.riista.mobile.models.Species;

public interface SpeciesResolver {

    Species findSpecies(@Nullable Integer speciesCode);

}
