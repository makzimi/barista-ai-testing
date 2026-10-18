package ru.otus.barista.data.impl.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The real DataStore implementation against a file in a temporary folder: no Android Context, no Robolectric. */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesLocalDataSourceTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val file: File by lazy { File(temporaryFolder.root, "favorites.preferences_pb") }

    @Test
    fun `observeFavorites WHEN nothing was saved SHOULD emit an empty set`() = runTest {
        val sut = createDataSource(backgroundScope)

        val favorites = sut.observeFavorites().first()

        assertEquals(emptySet(), favorites)
    }

    @Test
    fun `setFavorite WHEN drink is marked favorite SHOULD emit it to observers`() = runTest {
        val sut = createDataSource(backgroundScope)

        sut.observeFavorites().test {
            assertEquals(emptySet(), awaitItem())

            sut.setFavorite("cappuccino", isFavorite = true)

            assertEquals(setOf("cappuccino"), awaitItem())
        }
    }

    @Test
    fun `setFavorite WHEN favorite is toggled off SHOULD remove only that drink`() = runTest {
        val sut = createDataSource(backgroundScope)
        sut.setFavorite("cappuccino", isFavorite = true)
        sut.setFavorite("matcha_latte", isFavorite = true)

        sut.setFavorite("cappuccino", isFavorite = false)

        assertEquals(setOf("matcha_latte"), sut.observeFavorites().first())
    }

    @Test
    fun `setFavorite WHEN data store is reopened from the same file SHOULD keep the favorites`() = runTest {
        val firstScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        createDataSource(firstScope).setFavorite("espresso", isFavorite = true)
        firstScope.cancel()

        val reopened = createDataSource(backgroundScope)

        assertEquals(setOf("espresso"), reopened.observeFavorites().first())
    }

    private fun createDataSource(scope: CoroutineScope): DataStoreFavoritesLocalDataSource =
        DataStoreFavoritesLocalDataSource(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }),
        )
}
