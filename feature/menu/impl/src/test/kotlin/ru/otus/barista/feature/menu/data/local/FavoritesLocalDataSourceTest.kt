package ru.otus.barista.feature.menu.data.local

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
import ru.otus.barista.core.testing.Create
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

            sut.setFavorite(Create.CAPPUCCINO.id, isFavorite = true)

            assertEquals(setOf(Create.CAPPUCCINO.id), awaitItem())
        }
    }

    @Test
    fun `setFavorite WHEN favorite is toggled off SHOULD remove only that drink`() = runTest {
        val sut = createDataSource(backgroundScope)
        sut.setFavorite(Create.CAPPUCCINO.id, isFavorite = true)
        sut.setFavorite(Create.MATCHA.id, isFavorite = true)

        sut.setFavorite(Create.CAPPUCCINO.id, isFavorite = false)

        assertEquals(setOf(Create.MATCHA.id), sut.observeFavorites().first())
    }

    @Test
    fun `setFavorite WHEN data store is reopened from the same file SHOULD keep the favorites`() = runTest {
        val firstScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + Job())
        createDataSource(firstScope).setFavorite(Create.ESPRESSO.id, isFavorite = true)
        firstScope.cancel()

        val reopened = createDataSource(backgroundScope)

        assertEquals(setOf(Create.ESPRESSO.id), reopened.observeFavorites().first())
    }

    private fun createDataSource(scope: CoroutineScope): DataStoreFavoritesLocalDataSource =
        DataStoreFavoritesLocalDataSource(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }),
        )
}
