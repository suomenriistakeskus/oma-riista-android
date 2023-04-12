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
import fi.riista.mobile.feature.harvest.CreateHarvestFragment
import fi.riista.mobile.feature.harvest.EditHarvestFragment
import fi.riista.mobile.feature.harvest.ViewHarvestFragment
import fi.riista.mobile.feature.huntingControl.CreateHuntingControlEventFragment
import fi.riista.mobile.feature.huntingControl.EditHuntingControlEventFragment
import fi.riista.mobile.feature.huntingControl.ViewHuntingControlEventFragment
import fi.riista.mobile.feature.login.EmailChangedFragment
import fi.riista.mobile.feature.login.LoginFragment
import fi.riista.mobile.feature.login.ResetPasswordFragment
import fi.riista.mobile.feature.moreView.MoreViewFragment
import fi.riista.mobile.feature.myDetails.MyDetailsHuntingClubMembershipsFragment
import fi.riista.mobile.feature.myDetails.MyDetailsTrainingsFragment
import fi.riista.mobile.feature.observation.CreateObservationFragment
import fi.riista.mobile.feature.observation.EditObservationFragment
import fi.riista.mobile.feature.observation.ViewObservationFragment
import fi.riista.mobile.feature.srva.CreateSrvaEventFragment
import fi.riista.mobile.feature.srva.EditSrvaEventFragment
import fi.riista.mobile.feature.srva.ViewSrvaFragment
import fi.riista.mobile.pages.AnnouncementsFragment
import fi.riista.mobile.pages.ContactDetailsFragment
import fi.riista.mobile.pages.GalleryFragment
import fi.riista.mobile.pages.GameLogFragment
import fi.riista.mobile.pages.GroupHuntingMapViewer
import fi.riista.mobile.pages.HomeViewFragment
import fi.riista.mobile.pages.MapViewer
import fi.riista.mobile.pages.MyDetailsFragment
import fi.riista.mobile.pages.MyDetailsLicenseFragment
import fi.riista.mobile.pages.MyDetailsMhPermitDetailsFragment
import fi.riista.mobile.pages.MyDetailsMhPermitListFragment
import fi.riista.mobile.pages.MyDetailsOccupationsFragment
import fi.riista.mobile.pages.MyDetailsShootingTestsFragment
import fi.riista.mobile.pages.PermitList
import fi.riista.mobile.pages.SettingsFragment
import fi.riista.mobile.pages.ShootingTestCalendarEventListFragment
import fi.riista.mobile.pages.ShootingTestEventFragment
import fi.riista.mobile.pages.ShootingTestPaymentsFragment
import fi.riista.mobile.pages.ShootingTestQueueFragment
import fi.riista.mobile.pages.ShootingTestRegisterFragment

@Suppress("unused")
@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeViewFragment(): HomeViewFragment

    @ContributesAndroidInjector
    abstract fun contributeGameLogFragment(): GameLogFragment

    @ContributesAndroidInjector
    abstract fun contributeMoreViewFragment(): MoreViewFragment

    @ContributesAndroidInjector
    abstract fun contributeMapViewerFragment(): MapViewer

    @ContributesAndroidInjector
    abstract fun contributeGroupHuntingMapViewerFragment(): GroupHuntingMapViewer

    @ContributesAndroidInjector
    abstract fun contributeAnnouncementsFragment(): AnnouncementsFragment

    @ContributesAndroidInjector
    abstract fun contributePermitListFragment(): PermitList

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
    abstract fun contributeMyDetailsTrainingsFragment(): MyDetailsTrainingsFragment

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

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeResetPasswordFragment(): ResetPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeEmailChangeFragment(): EmailChangedFragment

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


    // Hunting control

    @ContributesAndroidInjector
    abstract fun contributeViewHuntingControlEventFragment(): ViewHuntingControlEventFragment

    @ContributesAndroidInjector
    abstract fun contributeEditHuntingControlEventFragment(): EditHuntingControlEventFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateHuntingControlEventFragment(): CreateHuntingControlEventFragment


    // SRVA

    @ContributesAndroidInjector
    abstract fun contributeViewSrvaFragment(): ViewSrvaFragment

    @ContributesAndroidInjector
    abstract fun contributeEditSrvaEventFragment(): EditSrvaEventFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateSrvaEventFragment(): CreateSrvaEventFragment


    // Observation

    @ContributesAndroidInjector
    abstract fun contributeViewObservationFragment(): ViewObservationFragment

    @ContributesAndroidInjector
    abstract fun contributeEditObservationFragment(): EditObservationFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateObservationFragment(): CreateObservationFragment


    // Harvest

    @ContributesAndroidInjector
    abstract fun contributeViewHarvestFragment(): ViewHarvestFragment

    @ContributesAndroidInjector
    abstract fun contributeEditHarvestFragment(): EditHarvestFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateHarvestFragment(): CreateHarvestFragment
}
