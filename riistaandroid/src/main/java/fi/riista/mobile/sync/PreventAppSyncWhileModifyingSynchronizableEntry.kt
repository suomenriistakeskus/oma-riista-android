package fi.riista.mobile.sync

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import fi.riista.common.logging.getLogger
import java.lang.ref.WeakReference

class PreventAppSyncWhileModifyingSynchronizableEntry private constructor(
    private val appSync: AppSync,
    private val weakObservedFragment: WeakReference<Fragment>
): FragmentLifecycleCallbacks() {
    private val logger by getLogger(PreventAppSyncWhileModifyingSynchronizableEntry::class)

    private val observedFragment: Fragment?
        get() {
            return weakObservedFragment.get().also {
                if (it == null) {
                    // restore appsync possibility just to make sure holding weak reference doesn't
                    // break stuff
                    appSync.enableSyncPrecondition(AppSyncPrecondition.USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY)
                }
            }
        }

    constructor(appSync: AppSync, observedFragment: Fragment):
            this(appSync, WeakReference(observedFragment))


    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)

        if (observedFragment == f) {
            logger.v { "Fragment ${f.tag} view created, preventing appsync" }
            appSync.disableSyncPrecondition(AppSyncPrecondition.USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY)
        }
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)

        if (observedFragment == f) {
            logger.v { "Fragment ${f.tag} view destroyed, allowing appsync" }
            appSync.enableSyncPrecondition(AppSyncPrecondition.USER_IS_NOT_MODIFYING_SYNCHRONIZABLE_ENTRY)
        }
    }
}
