package ru.otus.barista.feature.menu.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.otus.barista.feature.menu.api.DrinkId
import java.io.IOException

class DataStoreFavoritesLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : FavoritesLocalDataSource {

    override fun observeFavorites(): Flow<Set<DrinkId>> =
        dataStore.data
            .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
            .map { preferences -> preferences[FAVORITE_IDS].orEmpty().map(::DrinkId).toSet() }
            .distinctUntilChanged()

    override suspend fun setFavorite(id: DrinkId, isFavorite: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[FAVORITE_IDS].orEmpty()
            preferences[FAVORITE_IDS] = if (isFavorite) current + id.value else current - id.value
        }
    }

    private companion object {
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_drink_ids")
    }
}
