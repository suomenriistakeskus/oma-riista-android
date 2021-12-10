package fi.riista.mobile.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fi.riista.mobile.feature.groupHunting.harvests.CreateGroupHarvestFragment
import fi.riista.mobile.feature.groupHunting.harvests.EditGroupHarvestFragment
import fi.riista.mobile.feature.groupHunting.harvests.ViewGroupHarvestFragment
import fi.riista.mobile.feature.groupHunting.huntingDays.ViewGroupHuntingDayFragment
import fi.riista.mobile.feature.groupHunting.huntingDays.modify.CreateGroupHuntingDayFragment
import fi.riista.mobile.feature.groupHunting.huntingDays.modify.EditGroupHuntingDayFragment
import fi.riista.mobile.feature.groupHunting.map.ListGroupDiaryEntriesDialogFragment
import fi.riista.mobile.feature.groupHunting.observations.CreateGroupObservationFragment
import fi.riista.mobile.feature.groupHunting.observations.EditGroupObservationFragment
import fi.riista.mobile.feature.groupHunting.observations.ViewGroupObservationFragment
import fi.riista.mobile.feature.myDetails.MyDetailsHuntingClubMembershipsFragment
import fi.riista.mobile.pages.*

@Suppress("unused")
@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeViewFragment(): HomeViewFragment

    @ContributesAndroidInjector
    abstract fun contributeGameLogFragment(): GameLogFragment

    @ContributesAndroidInjector
    abstract fun contributeMapViewerFragment(): MapViewer

    @ContributesAndroidInjector
    abstract fun contributeGroupHuntingMapViewerFragment(): GroupHuntingMapViewer

    @ContributesAndroidInjector
    abstract fun contributeAnnouncementsFragment(): AnnouncementsFragment

    @ContributesAndroidInjector
    abstract fun contributeHarvestFragment(): HarvestFragment

    @ContributesAndroidInjector
    abstract fun contributePermitListFragment(): PermitList

    @ContributesAndroidInjector
    abstract fun contributeObservationEditFragment(): ObservationEditFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsFragment(): MyDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsLicenseFragment(): MyDetailsLicenseFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsMyDetailsShootingTestsFragmentFragment(): MyDetailsShootingTestsFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsOccupationsFragment(): MyDetailsOccupationsFragment

    @ContributesAndroidInjector
    abstract fun contributeGalleryFragment(): GalleryFragment

    @ContributesAndroidInjector
    abstract fun contributeContactDetailsFragment(): ContactDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsMhPermitListFragment(): MyDetailsMhPermitListFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsMhPermitDetailsFragment(): MyDetailsMhPermitDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeMyDetailsHuntingClubMembershipsFragment(): MyDetailsHuntingClubMembershipsFragment

    @ContributesAndroidInjector
    abstract fun contributeShootingTestCalendarEventListFragment(): ShootingTestCalendarEventListFragment

    @ContributesAndroidInjector
    abstract fun contributeShootingTestEventFragment(): ShootingTestEventFragment

    @ContributesAndroidInjector
    abstract fun contributeShootingTestRegisterFragment(): ShootingTestRegisterFragment

    @ContributesAndroidInjector
    abstract fun contributeShootingTestQueueFragment(): ShootingTestQueueFragment

    @ContributesAndroidInjector
    abstract fun contributeShootingTestPaymentsFragment(): ShootingTestPaymentsFragment


    // Group hunting

    @ContributesAndroidInjector
    abstract fun contributeViewGroupHarvestFragment(): ViewGroupHarvestFragment

    @ContributesAndroidInjector
    abstract fun contributeEditGroupHarvestFragment(): EditGroupHarvestFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateGroupHarvestFragment(): CreateGroupHarvestFragment

    @ContributesAndroidInjector
    abstract fun contributeViewGroupObservationFragment(): ViewGroupObservationFragment

    @ContributesAndroidInjector
    abstract fun contributeEditGroupObservationFragment(): EditGroupObservationFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateGroupObservationFragment(): CreateGroupObservationFragment

    @ContributesAndroidInjector
    abstract fun contributeViewHuntingDayFragment(): ViewGroupHuntingDayFragment

    @ContributesAndroidInjector
    abstract fun contributeEditGroupHuntingDayFragment(): EditGroupHuntingDayFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateGroupHuntingDayFragment(): CreateGroupHuntingDayFragment

    @ContributesAndroidInjector
    abstract fun contributeListGroupDiaryEntriesDialogFragment(): ListGroupDiaryEntriesDialogFragment
}
