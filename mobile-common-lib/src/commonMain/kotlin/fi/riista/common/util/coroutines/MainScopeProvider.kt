package fi.riista.common.util.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

interface MainScopeProvider {
    /**
     * Gets the scope that is executed in main thread.
     */
    val scope: CoroutineScope
}

class AppMainScopeProvider : MainScopeProvider {
    /**
     * Gets the scope that is executed in main thread.
     */
    override val scope: CoroutineScope = MainScope()
}