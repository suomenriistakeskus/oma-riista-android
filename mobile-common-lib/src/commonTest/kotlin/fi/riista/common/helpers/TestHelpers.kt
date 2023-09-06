package fi.riista.common.helpers

import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.util.coroutines.MainScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

// we could try utilizing https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
// but currently it is not available for kotlin multiplatform. There's a ticket for it though:
// https://github.com/Kotlin/kotlinx.coroutines/issues/1996
//
// The following function and value allow testing coroutines currently until proper library
// support is added
expect fun runBlockingTest(block: suspend CoroutineScope.() -> Unit)
expect val testCoroutineContext: CoroutineContext
internal expect fun createDatabaseDriverFactory(): DatabaseDriverFactory

class MockMainScopeProvider : MainScopeProvider {
    override val scope: CoroutineScope = CoroutineScope(testCoroutineContext)
}
