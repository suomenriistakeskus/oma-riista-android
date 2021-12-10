package fi.riista.mobile.viewmodel;

import android.location.Location;

import androidx.annotation.Nullable;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesResolver;
import fi.riista.mobile.gamelog.DeerHuntingFeatureAvailability;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.test.MockUtils;
import fi.riista.mobile.utils.HarvestValidator;

import static fi.riista.mobile.models.specimen.GameAge.ADULT;
import static fi.riista.mobile.models.specimen.GameAge.YOUNG;
import static fi.riista.mobile.models.specimen.Gender.FEMALE;
import static fi.riista.mobile.models.specimen.Gender.MALE;
import static fi.riista.mobile.utils.ConstantsKt.TIMEZONE_ID_FINLAND;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class HarvestViewModelTest {

    private static final int SPECIES_CODE_1 = 123;
    private static final int SPECIES_CODE_2 = 456;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock(name = "speciesResolver")
    private SpeciesResolver speciesResolver;

    @Mock(name = "permitManager")
    private PermitManager permitManager;

    @Mock(name = "amountObserver")
    private Observer<Integer> amountObserver;

    @Mock(name = "speciesObserver")
    private Observer<Species> speciesObserver;

    @Mock(name = "specimensObserver")
    private Observer<List<HarvestSpecimen>> specimensObserver;

    @Mock(name = "editEnabledObserver")
    private Observer<Boolean> editEnabledObserver;

    @Mock(name = "validityObserver")
    private Observer<Boolean> validityObserver;

    private Location locationMock;
    private Location locationMock2;

    private GameHarvest harvest;
    private HarvestViewModel viewModel;

    private DeerHuntingFeatureAvailability deerHuntingFeatureAvailability;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        locationMock = MockUtils.mockLocation(62.0, 24.0);
        locationMock2 = MockUtils.mockLocation(62.0, 24.0);
        initPermitManagerMock();

        deerHuntingFeatureAvailability = new DeerHuntingFeatureAvailability(true);

        final HarvestValidator harvestValidator = new HarvestValidator(speciesResolver, deerHuntingFeatureAvailability);

        // In order to not need to mock Android Log.
        harvestValidator.setLogEnabled(false);

        viewModel =
                new HarvestViewModel(speciesResolver, permitManager, harvestValidator, deerHuntingFeatureAvailability);
    }

    private void initPermitManagerMock() {
        when(permitManager.validateHarvestPermitInformation(any(GameHarvest.class))).thenReturn(true);
    }

    private void initViewModelWithDefaultLocation() {
        initViewModel(true, null, null, false, true);
    }

    private void initViewModelWithNewHarvest(@Nullable final Species species, @Nullable final Integer amount) {
        initViewModel(true, species, amount, false, true);
    }


    private void initViewModelWithSavedHarvest(@Nullable final Species species,
                                               @Nullable final Integer amount,
                                               final boolean withEditEnabled) {

        initViewModel(true, species, amount, true, withEditEnabled);
    }

    private void initViewModel(final boolean withDefaultLocation,
                               @Nullable final Species species,
                               @Nullable final Integer amount,
                               final boolean isSavedLocally,
                               final boolean withEditEnabled) {

        harvest = GameHarvest.createNew(AppConfig.HARVEST_SPEC_VERSION);

        if (withDefaultLocation) {
            harvest = spy(harvest);
            harvest.mLocation = locationMock;
            when(harvest.getCopyOfLocation()).thenReturn(locationMock2);
        }

        final DateTime now = DateTime.now(DateTimeZone.forID(TIMEZONE_ID_FINLAND));

        // Harvest point of time must be in the past. Otherwise harvest does not pass through HarvestValidator.
        harvest.mTime = now.minusHours(1).toGregorianCalendar();

        if (species != null) {
            harvest.mSpeciesID = species.mId;
        }

        // Default value of 1 is overridden if non-null amount is given.
        if (amount != null) {
            harvest.mAmount = amount;
        }

        if (isSavedLocally) {
            final long maxIntValueAsLong = Integer.valueOf(Integer.MAX_VALUE).longValue();
            harvest.mLocalId = Long.valueOf(currentTimeMillis() % maxIntValueAsLong).intValue();
        }

        viewModel.initWith(harvest, withEditEnabled);

        initObserverForSpeciesLiveData(species);
        initObserverForAmountLiveData(harvest.mAmount);
        initObserverForSpecimensLiveData();
        initObserverForEditEnabledLiveData(withEditEnabled);

        final boolean isValid = withDefaultLocation && species != null && harvest.isAmountWithinLegalRange();
        initObserverForValidityLiveData(isValid);
    }

    @Test
    public void testInitWith_withDefaultLocation() {
        initViewModelWithDefaultLocation();

        assertNotNull(viewModel.getAmount());
        assertAmount(1);

        assertNotNull(viewModel.getSpecies());
        assertSpecies(null);

        assertNotNull(viewModel.getSpecimens());

        final List<HarvestSpecimen> viewModelSpecimens = viewModel.getSpecimens().getValue();
        // View model must have a distinct copy.
        assertNotEquals(harvest.mSpecimen, viewModelSpecimens);

        assertListContainsOneEmptySpecimen(viewModelSpecimens);
        assertEmptyList(viewModel.getResultHarvest().mSpecimen);

        assertIsModelValid(false);

        assertNotNull(viewModel.getResultHarvest());
    }

    @Test
    public void testSetAmount_zeroValueThrowsException() {
        initViewModelWithDefaultLocation();

        try {
            viewModel.setAmount(0);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            verifyNoMoreObserverInteractions();
            assertAmount(1);
        }
    }

    @Test
    public void testSetAmount_tooLargeValueThrowsException() {
        initViewModelWithDefaultLocation();

        try {
            viewModel.setAmount(GameHarvest.MAX_AMOUNT + 1);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            verifyNoMoreObserverInteractions();
            assertAmount(1);
        }
    }

    @Test
    public void testSetAmount_whenAmountSetToNull() {
        final Species species = createSpeciesMock(true);
        initViewModelWithNewHarvest(species, 5);

        viewModel.setAmount(null);

        verify(amountObserver).onChanged(isNull());
        verify(validityObserver).onChanged(eq(false));
        verifyNoMoreObserverInteractions();

        assertNull(viewModel.getAmount().getValue());
        assertEquals(5, viewModel.getResultHarvest().mAmount);

        assertIsModelValid(false);
    }

    @Test
    public void testSetAmount_whenAmountSetToThreeAndSpeciesAllowsMultipleSpecimens() {
        final Species species = createSpeciesMock(true);
        initViewModelWithNewHarvest(species, 1);

        final int newAmount = 3;
        viewModel.setAmount(newAmount);

        verify(amountObserver).onChanged(eq(newAmount));
        verify(validityObserver).onChanged(eq(true));
        verifyNoMoreObserverInteractions();

        assertAmount(newAmount);
        assertIsModelValid(true);
    }

    @Test
    public void testSetAmount_whenAmountSetToThreeWhileSpeciesDoesNotAllowMultipleSpecimens() {
        final Species species = createSpeciesMock(false);
        initViewModelWithNewHarvest(species, 1);

        viewModel.setAmount(3);

        verify(amountObserver).onChanged(eq(1));
        verify(validityObserver).onChanged(eq(true));
        verifyNoMoreObserverInteractions();

        assertAmount(1);
        assertIsModelValid(true);
    }

    @Test
    public void testSetAmount_whenAmountIsDecreasedSpecimensShouldBeRetainedInOrder() {
        final Species species = createSpeciesMock(true);
        initViewModelWithNewHarvest(species, 5);

        // Add five specimens. The first three specimens are expected to be retained in order after
        // amount is decreased to three.
        final HarvestSpecimen specimen1 = new HarvestSpecimen();
        specimen1.setAge(ADULT.name());
        final HarvestSpecimen specimen2 = new HarvestSpecimen();
        specimen2.setAge(YOUNG.name());
        final HarvestSpecimen specimen3 = new HarvestSpecimen();
        specimen3.setGender(MALE.name());
        final HarvestSpecimen specimen4 = new HarvestSpecimen();
        specimen4.setGender(FEMALE.name());
        final HarvestSpecimen specimen5 = new HarvestSpecimen();
        specimen5.setWeight(100.0);

        viewModel.setSpecimens(asList(specimen1, specimen2, specimen3, specimen4, specimen5));
        verify(validityObserver).onChanged(eq(true));

        clearInvocations(specimensObserver);

        final int newAmount = 3;
        viewModel.setAmount(newAmount);

        verify(amountObserver).onChanged(eq(newAmount));
        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        final List<HarvestSpecimen> expectedSpecimens = asList(specimen1, specimen2, specimen3);

        final ArgumentCaptor<List<HarvestSpecimen>> specimenCaptor = ArgumentCaptor.forClass(List.class);
        verify(specimensObserver).onChanged(specimenCaptor.capture());
        assertListContainsSpecimens(specimenCaptor.getValue(), expectedSpecimens);

        verifyNoMoreObserverInteractions();

        assertAmount(newAmount);
        assertSpecimens(expectedSpecimens);
        assertIsModelValid(true);

        // Sanity check
        assertSpecies(species);
    }

    @Test
    public void testSelectSpecies_withNullSpeciesCode_onInitialState() {
        initViewModelWithDefaultLocation();

        viewModel.selectSpecies(null);

        verify(validityObserver).onChanged(eq(false));
        verifyNoMoreObserverInteractions();

        assertSpecies(null);
        assertAmount(1);
        assertIsModelValid(false);
    }

    @Test
    public void testSelectSpecies_withNonExistentSpeciesCode_onInitialState() {
        initViewModelWithDefaultLocation();

        viewModel.selectSpecies(Integer.MAX_VALUE);

        verify(validityObserver).onChanged(eq(false));
        verifyNoMoreObserverInteractions();

        assertSpecies(null);
        assertAmount(1);
        assertIsModelValid(false);
    }

    @Test
    public void testSelectSpecies_withNullSpeciesCode_afterSpeciesWasSelected() {
        final Species species = createSpeciesMock(true);
        initViewModelWithNewHarvest(species, 3);

        // Add two specimens. First specimen is expected to be retained after species is changed.
        final HarvestSpecimen specimen1 = new HarvestSpecimen();
        specimen1.setAge(ADULT.name());
        final HarvestSpecimen specimen2 = new HarvestSpecimen();
        specimen2.setAge(YOUNG.name());

        viewModel.setSpecimens(asList(specimen1, specimen2));
        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        clearInvocations(specimensObserver);

        // Call method under test.
        viewModel.selectSpecies(null);

        verify(speciesObserver).onChanged(isNull());
        verify(amountObserver).onChanged(eq(1));
        verify(validityObserver).onChanged(eq(false));

        final ArgumentCaptor<List<HarvestSpecimen>> specimenCaptor = ArgumentCaptor.forClass(List.class);
        verify(specimensObserver).onChanged(specimenCaptor.capture());
        assertListContainsOneSpecimen(specimenCaptor.getValue(), specimen1);

        verifyNoMoreObserverInteractions();

        assertSpecies(null);
        assertAmount(1);
        assertOneSpecimenExists(specimen1);
        assertIsModelValid(false);
    }

    @Test
    public void testSelectSpecies_withSpeciesCodeOfExistingSpecies() {
        // Set amount to a value greater than 1.
        final int amount = 3;
        initViewModelWithNewHarvest(null, amount);

        // Set species.
        final Species species = createSpeciesMock(true);
        viewModel.selectSpecies(species.mId);

        verify(speciesObserver).onChanged(eq(species));
        verify(validityObserver).onChanged(eq(true));
        verifyNoMoreObserverInteractions();

        assertSpecies(species);
        assertAmount(amount);
        assertIsModelValid(true);
    }

    @Test
    public void testSelectSpecies_whenChangingToSpeciesNotAllowingMultipleSpecimens() {
        final Species species = createSpeciesMock(SPECIES_CODE_1, true);
        initViewModelWithNewHarvest(species, 3);

        // Add three specimens. First specimen is expected to be retained after species is changed.
        final HarvestSpecimen specimen1 = new HarvestSpecimen();
        specimen1.setAge(ADULT.name());
        final HarvestSpecimen specimen2 = new HarvestSpecimen();
        specimen2.setAge(YOUNG.name());
        final HarvestSpecimen specimen3 = new HarvestSpecimen();
        specimen3.setGender(MALE.name());
        viewModel.setSpecimens(asList(specimen1, specimen2, specimen3));

        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        clearInvocations(specimensObserver);

        final Species species2 = createSpeciesMock(SPECIES_CODE_2, false);

        // Call method under test.
        viewModel.selectSpecies(species2.mId);

        verify(speciesObserver).onChanged(eq(species2));
        verify(amountObserver).onChanged(eq(1));
        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        final ArgumentCaptor<List<HarvestSpecimen>> specimenCaptor = ArgumentCaptor.forClass(List.class);
        verify(specimensObserver).onChanged(specimenCaptor.capture());
        assertListContainsOneSpecimen(specimenCaptor.getValue(), specimen1);

        verifyNoMoreObserverInteractions();

        assertSpecies(species2);
        assertAmount(1);
        assertOneSpecimenExists(specimen1);
        assertIsModelValid(true);
    }

    @Test
    public void testSetSpecimens_shouldThrowExceptionIfNumberOfSpecimensIsGreaterThanAmount() {
        final Species species = createSpeciesMock(true);
        initViewModelWithNewHarvest(species, 1);

        // Add five specimens. Non-empty specimens are expected to be retained.
        final HarvestSpecimen specimen1 = new HarvestSpecimen();
        specimen1.setAge(ADULT.name());
        final HarvestSpecimen specimen2 = new HarvestSpecimen();
        specimen2.setAge(YOUNG.name());

        try {
            viewModel.setSpecimens(asList(specimen1, specimen2));
            fail("Exception expected when number of specimens is greater than amount");
        } catch (final IllegalArgumentException e) {
            // Expected
        }

        verifyNoMoreObserverInteractions();

        // Sanity checks
        assertListContainsOneEmptySpecimen(viewModel.getSpecimens().getValue());
        assertEmptyList(viewModel.getResultHarvest().mSpecimen);

        assertAmount(1);
        assertSpecies(species);
        assertIsModelValid(true);
    }

    @Test
    public void testSetSpecimens_emptySpecimensShouldBeDiscarded() {
        final Species species = createSpeciesMock(true);
        initViewModelWithNewHarvest(species, 5);

        // Add five specimens. Non-empty specimens are expected to be retained.
        final HarvestSpecimen specimen1 = new HarvestSpecimen();
        specimen1.setAge(ADULT.name());
        final HarvestSpecimen specimen2 = new HarvestSpecimen();
        final HarvestSpecimen specimen3 = new HarvestSpecimen();
        specimen3.setAge(YOUNG.name());
        final HarvestSpecimen specimen4 = new HarvestSpecimen();
        final HarvestSpecimen specimen5 = new HarvestSpecimen();
        specimen5.setGender(MALE.name());

        viewModel.setSpecimens(asList(specimen1, specimen2, specimen3, specimen4, specimen5));

        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        final List<HarvestSpecimen> expectedSpecimens = asList(specimen1, specimen3, specimen5);

        final ArgumentCaptor<List<HarvestSpecimen>> specimenCaptor = ArgumentCaptor.forClass(List.class);
        verify(specimensObserver).onChanged(specimenCaptor.capture());
        assertListContainsSpecimens(specimenCaptor.getValue(), expectedSpecimens);

        verifyNoMoreObserverInteractions();

        assertSpecimens(expectedSpecimens);

        // Sanity checks
        assertSpecies(species);
        assertAmount(5);
        assertIsModelValid(true);
    }

    @Test
    public void testSetSpecimens_withEmptySpecimenList() {
        final Species species = createSpeciesMock(true);
        final int amount = 10;
        initViewModelWithNewHarvest(species, amount);

        viewModel.setSpecimens(emptyList());

        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        final ArgumentCaptor<List<HarvestSpecimen>> specimenCaptor = ArgumentCaptor.forClass(List.class);
        verify(specimensObserver).onChanged(specimenCaptor.capture());
        assertListContainsOneEmptySpecimen(specimenCaptor.getValue());

        verifyNoMoreObserverInteractions();

        assertListContainsOneEmptySpecimen(viewModel.getSpecimens().getValue());
        assertEmptyList(viewModel.getResultHarvest().mSpecimen);

        // Sanity checks
        assertSpecies(species);
        assertAmount(amount);
        assertIsModelValid(true);
    }

    @Test
    public void testSetEditEnabled_whenEnablingEditForValidHarvest() {
        final Species species = createSpeciesMock(true);
        initViewModelWithSavedHarvest(species, 2, false);

        viewModel.setEditEnabled(true);

        verify(editEnabledObserver).onChanged(eq(true));

        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(true));

        verifyNoMoreObserverInteractions();

        // Sanity checks
        assertSpecies(species);
        assertAmount(2);
        assertListContainsOneEmptySpecimen(viewModel.getSpecimens().getValue());
        assertEmptyList(viewModel.getResultHarvest().mSpecimen);
        assertIsModelValid(true);
    }

    @Test
    public void testSetEditEnabled_whenEnablingEditForInvalidHarvest() {
        final Species species = createSpeciesMock(true);

        // Zero amount makes harvest invalid.
        initViewModelWithSavedHarvest(species, 0, false);

        viewModel.setEditEnabled(true);

        verify(editEnabledObserver).onChanged(eq(true));

        // TODO This call is unnecessary since validity has not changed. Fix HarvestViewModel and HarvestFragment.
        verify(validityObserver).onChanged(eq(false));

        verifyNoMoreObserverInteractions();

        // Sanity checks
        assertSpecies(species);
        assertAmount(0);
        assertListContainsOneEmptySpecimen(viewModel.getSpecimens().getValue());
        assertEmptyList(viewModel.getResultHarvest().mSpecimen);
        assertIsModelValid(false);
    }

    private void initObserverForAmountLiveData(final int expectedAmount) {
        final LiveData<Integer> amountLiveData = viewModel.getAmount();
        amountLiveData.observeForever(amountObserver);

        assertTrue(amountLiveData.hasActiveObservers());
        verify(amountObserver).onChanged(eq(expectedAmount));
        verifyNoMoreInteractions(amountObserver);

        // Reset needed because of initial value populated in setup.
        clearInvocations(amountObserver);
    }

    private void initObserverForSpeciesLiveData(final Species expectedSpecies) {
        final LiveData<Species> speciesLiveData = viewModel.getSpecies();
        speciesLiveData.observeForever(speciesObserver);

        assertTrue(speciesLiveData.hasActiveObservers());
        verify(speciesObserver).onChanged(expectedSpecies != null ? eq(expectedSpecies) : isNull());
        verifyNoMoreInteractions(speciesObserver);

        // Reset needed because of initial value populated in setup.
        clearInvocations(speciesObserver);
    }

    private void initObserverForSpecimensLiveData() {
        final LiveData<List<HarvestSpecimen>> specimensLiveData = viewModel.getSpecimens();
        specimensLiveData.observeForever(specimensObserver);

        assertTrue(specimensLiveData.hasActiveObservers());

        final ArgumentCaptor<List<HarvestSpecimen>> specimenCaptor = ArgumentCaptor.forClass(List.class);
        verify(specimensObserver).onChanged(specimenCaptor.capture());
        assertListContainsOneEmptySpecimen(specimenCaptor.getValue());

        verifyNoMoreInteractions(specimensObserver);

        // Reset needed because of initial value populated in setup.
        clearInvocations(specimensObserver);
    }

    private void initObserverForEditEnabledLiveData(final Boolean expectedInitialValue) {
        final LiveData<Boolean> editEnabledLiveData = viewModel.getEditEnabled();
        editEnabledLiveData.observeForever(editEnabledObserver);

        assertTrue(editEnabledLiveData.hasActiveObservers());
        verify(editEnabledObserver).onChanged(eq(expectedInitialValue));
        verifyNoMoreInteractions(editEnabledObserver);

        // Reset needed because of initial value populated in setup.
        clearInvocations(editEnabledObserver);
    }

    private void initObserverForValidityLiveData(final Boolean expectedInitialValidity) {
        final LiveData<Boolean> validityLiveData = viewModel.getHasAllRequiredFields();
        validityLiveData.observeForever(validityObserver);

        assertTrue(validityLiveData.hasActiveObservers());
        verify(validityObserver).onChanged(eq(expectedInitialValidity));
        verifyNoMoreInteractions(validityObserver);

        // Reset needed because of initial value populated in setup.
        clearInvocations(validityObserver);
    }

    private Species createSpeciesMock(final boolean allowMultiSpecimenHarvests) {
        return createSpeciesMock(SPECIES_CODE_1, allowMultiSpecimenHarvests);
    }

    private Species createSpeciesMock(final int speciesCode, final boolean allowMultiSpecimenHarvests) {
        final Species species = createSpecies(speciesCode, allowMultiSpecimenHarvests);
        when(speciesResolver.findSpecies(speciesCode)).thenReturn(species);
        return species;
    }

    private static Species createSpecies(final int speciesCode, final boolean allowMultiSpecimenHarvest) {
        final Species species = new Species();
        species.mId = speciesCode;
        species.mName = "Test Species " + speciesCode;
        species.mMultipleSpecimenAllowedOnHarvests = allowMultiSpecimenHarvest;
        return species;
    }

    private void assertAmount(final int expectedAmount) {
        assertEquals(Integer.valueOf(expectedAmount), viewModel.getAmount().getValue());
        assertEquals(expectedAmount, viewModel.getResultHarvest().mAmount);
    }

    private void assertSpecies(@Nullable final Species expectedSpecies) {
        if (expectedSpecies == null) {
            assertNull(viewModel.getSpecies().getValue());
            assertNull(viewModel.getResultHarvest().mSpeciesID);
        } else {
            assertEquals(expectedSpecies, viewModel.getSpecies().getValue());
            assertEquals(expectedSpecies.mId, viewModel.getResultHarvest().mSpeciesID);
        }
    }

    private void assertSpecimens(final List<HarvestSpecimen> expectedSpecimens) {
        assertListContainsSpecimens(viewModel.getSpecimens().getValue(), expectedSpecimens);
        assertListContainsSpecimens(viewModel.getResultHarvest().mSpecimen, expectedSpecimens);
    }

    private static void assertListContainsSpecimens(final List<HarvestSpecimen> list, final List<HarvestSpecimen> expected) {
        assertNotNull(list);
        assertEquals(expected.size(), list.size());

        final int numItems = list.size();
        for (int i = 0; i < numItems; i++) {
            assertEquals("Mismatching specimens on index: " + i, expected.get(0), list.get(0));
        }
    }

    private void assertOneSpecimenExists(final HarvestSpecimen expectedSpecimen) {
        assertListContainsOneSpecimen(viewModel.getSpecimens().getValue(), expectedSpecimen);
        assertListContainsOneSpecimen(viewModel.getResultHarvest().mSpecimen, expectedSpecimen);
    }

    private static void assertListContainsOneSpecimen(final List<HarvestSpecimen> list, final HarvestSpecimen specimen) {
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(specimen, list.get(0));
    }

    private static void assertListContainsOneEmptySpecimen(final List<HarvestSpecimen> list) {
        assertNotNull(list);
        assertEquals(1, list.size());
        assertTrue(list.get(0).isEmpty());
    }

    private static void assertEmptyList(final List<HarvestSpecimen> list) {
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    private void assertIsModelValid(final boolean expected) {
        final LiveData<Boolean> validity = viewModel.getHasAllRequiredFields();
        assertNotNull(validity);
        assertEquals(expected, validity.getValue());
    }

    private void verifyNoMoreObserverInteractions() {
        verifyNoMoreInteractions(amountObserver);
        verifyNoMoreInteractions(speciesObserver);
        verifyNoMoreInteractions(specimensObserver);
        verifyNoMoreInteractions(validityObserver);
    }
}
