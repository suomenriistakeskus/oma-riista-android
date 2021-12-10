package fi.riista.mobile.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashSet;

import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.specimen.GameAge;
import fi.riista.mobile.models.specimen.Gender;

import static fi.riista.mobile.database.SpeciesInformation.BEAN_GOOSE_ID;
import static fi.riista.mobile.database.SpeciesInformation.BEAR_ID;
import static fi.riista.mobile.database.SpeciesInformation.COMMON_EIDER_ID;
import static fi.riista.mobile.database.SpeciesInformation.COOT_ID;
import static fi.riista.mobile.database.SpeciesInformation.EUROPEAN_BEAVER_ID;
import static fi.riista.mobile.database.SpeciesInformation.GARGANEY_ID;
import static fi.riista.mobile.database.SpeciesInformation.GOOSANDER_ID;
import static fi.riista.mobile.database.SpeciesInformation.GREYLAG_GOOSE_ID;
import static fi.riista.mobile.database.SpeciesInformation.GREY_SEAL_ID;
import static fi.riista.mobile.database.SpeciesInformation.LONG_TAILED_DUCK_ID;
import static fi.riista.mobile.database.SpeciesInformation.LYNX_ID;
import static fi.riista.mobile.database.SpeciesInformation.MOOSE_ID;
import static fi.riista.mobile.database.SpeciesInformation.OTTER_ID;
import static fi.riista.mobile.database.SpeciesInformation.PINTAIL_ID;
import static fi.riista.mobile.database.SpeciesInformation.POCHARD_ID;
import static fi.riista.mobile.database.SpeciesInformation.POLECAT_ID;
import static fi.riista.mobile.database.SpeciesInformation.RED_BREASTED_MERGANSER_ID;
import static fi.riista.mobile.database.SpeciesInformation.RINGED_SEAL_ID;
import static fi.riista.mobile.database.SpeciesInformation.ROE_DEER_ID;
import static fi.riista.mobile.database.SpeciesInformation.SHOVELER_ID;
import static fi.riista.mobile.database.SpeciesInformation.TUFTED_DUCK_ID;
import static fi.riista.mobile.database.SpeciesInformation.WHITE_TAILED_DEER_ID;
import static fi.riista.mobile.database.SpeciesInformation.WIGEON_ID;
import static fi.riista.mobile.database.SpeciesInformation.WILD_BOAR_ID;
import static fi.riista.mobile.database.SpeciesInformation.WOLF_ID;
import static fi.riista.mobile.database.SpeciesInformation.WOLVERINE_ID;
import static java.util.Arrays.asList;

// Use Guava ImmutableSet if it is integrated to project later
class ImmutableSet<E> extends HashSet<E> {
    public ImmutableSet() {
        super();
    }

    public ImmutableSet(@NonNull Collection<? extends E> c) {
        super(c);
    }
}

public class RequiredHarvestFields {

    public static Report getFormFields(final int huntingYear,
                                       final int gameSpeciesCode,
                                       final HarvestReportingType reportingType,
                                       final boolean deerHuntingFeatureEnabled) {

        return new Report(huntingYear, gameSpeciesCode, reportingType, deerHuntingFeatureEnabled);
    }

    public static Specimen getSpecimenFields(final int huntingYear,
                                             final int gameSpeciesCode,
                                             final @Nullable GreySealHuntingMethod greySealHuntingMethod,
                                             final HarvestReportingType reportingType) {

        return new Specimen(huntingYear, gameSpeciesCode, greySealHuntingMethod, reportingType);
    }

    public enum Required {
        YES("YES"),
        NO("NO"),
        VOLUNTARY("VOLUNTARY");

        private final String mValue;

        Required(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }
    }

    public enum HarvestReportingType {
        BASIC,
        PERMIT,
        SEASON,
        HUNTING_DAY
    }

    public static class Report {

        private static final ImmutableSet<Integer> PERMIT_REQUIRED_WITHOUT_SEASON = new ImmutableSet<>(asList(
                BEAN_GOOSE_ID,
                BEAR_ID,
                COMMON_EIDER_ID,
                COOT_ID,
                EUROPEAN_BEAVER_ID,
                GARGANEY_ID,
                GOOSANDER_ID,
                GREYLAG_GOOSE_ID,
                GREY_SEAL_ID,
                LONG_TAILED_DUCK_ID,
                LYNX_ID,
                OTTER_ID,
                PINTAIL_ID,
                POCHARD_ID,
                POLECAT_ID,
                RED_BREASTED_MERGANSER_ID,
                RINGED_SEAL_ID,
                ROE_DEER_ID,
                SHOVELER_ID,
                TUFTED_DUCK_ID,
                WIGEON_ID,
                WILD_BOAR_ID,
                WOLF_ID,
                WOLVERINE_ID));

        final int gameSpeciesCode;
        final int huntingYear;
        final HarvestReportingType reportingType;
        final boolean deerHuntingFeatureEnabled;

         Report(final int huntingYear,
                final int gameSpeciesCode,
                final HarvestReportingType reportingType,
                final boolean deerHuntingFeatureEnabled) {

            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
            this.deerHuntingFeatureEnabled = deerHuntingFeatureEnabled;
        }

        public Required getPermitNumber() {
            final boolean permitNumberRequired = reportingType == HarvestReportingType.PERMIT
                    || reportingType != HarvestReportingType.SEASON && PERMIT_REQUIRED_WITHOUT_SEASON.contains(gameSpeciesCode);

            return permitNumberRequired ? Required.YES : Required.NO;
        }

        public Required getHarvestArea() {
            if (gameSpeciesCode == BEAR_ID || gameSpeciesCode == GREY_SEAL_ID) {
                return reportingType == HarvestReportingType.SEASON ? Required.YES : Required.NO;
            }
            return Required.NO;
        }

        public Required getDeerHuntingType() {
            // TODO: Remove when pilot is over
            if (deerHuntingFeatureEnabled) {
                if (gameSpeciesCode == WHITE_TAILED_DEER_ID) {
                    return reportingType == HarvestReportingType.PERMIT ? Required.NO : Required.VOLUNTARY;
                }
            }
            return Required.NO;
        }

        public Required getGreySealHuntingMethod() {
            if (gameSpeciesCode == GREY_SEAL_ID) {
                return reportingType != HarvestReportingType.BASIC ? Required.YES : Required.NO;
            }
            return Required.NO;
        }

        public Required getFeedingPlace() {
            if (gameSpeciesCode == WILD_BOAR_ID) {
                return reportingType != HarvestReportingType.BASIC ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }

        public Required getTaigaBeanGoose() {
            if (gameSpeciesCode == BEAN_GOOSE_ID) {
                return reportingType != HarvestReportingType.BASIC ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }

        public Required getLukeStatus() {
            if (gameSpeciesCode == WOLF_ID) {
                return reportingType == HarvestReportingType.SEASON ? Required.VOLUNTARY : Required.NO;
            }
            return Required.NO;
        }
    }

    public static class Specimen {

        // {mufloni,saksanhirvi,japaninpeura,halli,susi,ahma,karhu,hirvi,kuusipeura,valkohäntäpeura,metsäpeura,villisika,saukko,ilves}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_AGE = new ImmutableSet<>(asList(47774, 47476, 47479, 47282, 46549, 47212, 47348, 47503, 47484, 47629, 200556, 47926, 47169, 46615));

        // {villisika,saukko,ilves,piisami,rämemajava,"tarhattu naali",pesukarhu,hilleri,kirjohylje,mufloni,
        // saksanhirvi,japaninpeura,halli,susi,"villiintynyt kissa",metsäjänis,rusakko,orava,kanadanmajava,kettu,kärppä,
        // näätä,minkki,villikani,supikoira,mäyrä,itämerennorppa,euroopanmajava,ahma,karhu,metsäkauris,hirvi,kuusipeura,valkohäntäpeura,metsäpeura}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_GENDER = new ImmutableSet<>(asList(47926, 47169, 46615, 48537, 50336, 46542, 47329, 47240, 47305, 47774, 47476, 47479, 47282, 46549, 53004, 50106, 50386, 48089, 48250, 46587, 47230, 47223, 47243, 50114, 46564, 47180, 200555, 48251, 47212, 47348, 47507, 47503, 47484, 47629, 200556));

        // {halli,susi,saukko,ilves,ahma,karhu}
        static final ImmutableSet<Integer> PERMIT_MANDATORY_WEIGHT = new ImmutableSet<>(asList(47282, 46549, 47169, 46615, 47212, 47348));

        // {karhu,metsäkauris,halli,villisika}
        private static final ImmutableSet<Integer> SEASON_COMMON_MANDATORY = new ImmutableSet<>(asList(47348, 47507, 47282, 47926));

        private final int huntingYear;
        private final int gameSpeciesCode;
        private final HarvestReportingType reportingType;
        private final GreySealHuntingMethod greySealHuntingMethod;
        private final boolean isMoose;
        private final boolean isMooseOrDeerRequiringPermitForHunting;
        private final boolean associatedToHuntingDay;

        private Specimen(final int huntingYear,
                         final int gameSpeciesCode,
                         final GreySealHuntingMethod greySealHuntingMethod,
                         final HarvestReportingType reportingType) {

            this.huntingYear = huntingYear;
            this.gameSpeciesCode = gameSpeciesCode;
            this.reportingType = reportingType;
            this.greySealHuntingMethod = greySealHuntingMethod;
            this.isMoose = gameSpeciesCode == MOOSE_ID;
            this.isMooseOrDeerRequiringPermitForHunting = SpeciesInformation.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode);
            this.associatedToHuntingDay = reportingType == HarvestReportingType.HUNTING_DAY;
        }

        public Required getAge() {
            if (isMooseOrDeerRequiringPermitForHunting) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }
            return getRequirement(PERMIT_MANDATORY_AGE, gameSpeciesCode);
        }

        public Required getGender() {
            if (isMooseOrDeerRequiringPermitForHunting) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }
            return getRequirement(PERMIT_MANDATORY_GENDER, gameSpeciesCode);
        }

        public Required getWeight() {
            if (gameSpeciesCode == ROE_DEER_ID && reportingType == HarvestReportingType.SEASON) {
                return Required.VOLUNTARY;
            }

            if (gameSpeciesCode == WILD_BOAR_ID && reportingType == HarvestReportingType.SEASON) {
                return Required.VOLUNTARY;
            }

            if (isMooseOrDeerRequiringPermitForHunting) {
                return huntingYear < 2016 ? Required.VOLUNTARY : Required.NO;
            }

            if (gameSpeciesCode == GREY_SEAL_ID && greySealHuntingMethod == GreySealHuntingMethod.SHOT_BUT_LOST) {
                return huntingYear < 2015 ? Required.VOLUNTARY : Required.NO;
            }

            return getRequirement(PERMIT_MANDATORY_WEIGHT, gameSpeciesCode);
        }

        private Required getRequirement(final ImmutableSet<Integer> permitMandatorySpecies, final int gameSpeciesCode) {
            return reportingType == HarvestReportingType.PERMIT && permitMandatorySpecies.contains(gameSpeciesCode) ||
                    reportingType == HarvestReportingType.SEASON && SEASON_COMMON_MANDATORY.contains(gameSpeciesCode) ||
                    reportingType == HarvestReportingType.HUNTING_DAY
                    ? Required.YES : Required.VOLUNTARY;
        }

        public Required getWeightEstimated() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getWeightMeasured() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAdditionalInfo() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getNotEdible() {
            if (isMooseOrDeerRequiringPermitForHunting) {
                return isMoose && associatedToHuntingDay && huntingYear >= 2016 ? Required.YES : Required.VOLUNTARY;
            }

            return Required.NO;
        }

        public Required getFitnessClass() {
            if (isMoose) {
                return associatedToHuntingDay && huntingYear >= 2016 ? Required.YES : Required.VOLUNTARY;
            }

            return Required.NO;
        }

        public Required getAntlersWidth(final GameAge age, final Gender gender) {
            return commonMooselikeAdultMale(age, gender);
        }

        // For UI only
        public Required getAntlersWidth() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        // For UI only
        public Required getAntlerPoints() {
            return isMooseOrDeerRequiringPermitForHunting ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAntlerPoints(final GameAge age, final Gender gender) {
            return commonMooselikeAdultMale(age, gender);
        }

        private Required commonMooselikeAdultMale(final GameAge age, final Gender gender) {
            if (isMooseOrDeerRequiringPermitForHunting && age == GameAge.ADULT && gender == Gender.MALE) {

                return associatedToHuntingDay && isMoose && huntingYear >= 2016
                        ? Required.YES
                        : Required.VOLUNTARY;
            }

            return Required.NO;
        }

        // For UI only
        public Required getAntlersType() {
            return isMoose ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAntlersType(final GameAge age, final Gender gender) {
            if (isMoose && age == GameAge.ADULT && gender == Gender.MALE) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }

            return Required.NO;
        }

        // For UI only
        public Required getAlone() {
            return isMoose ? Required.VOLUNTARY : Required.NO;
        }

        public Required getAlone(final GameAge age) {
            if (isMoose && age == GameAge.YOUNG) {
                return associatedToHuntingDay ? Required.YES : Required.VOLUNTARY;
            }

            return Required.NO;
        }
    }
}
