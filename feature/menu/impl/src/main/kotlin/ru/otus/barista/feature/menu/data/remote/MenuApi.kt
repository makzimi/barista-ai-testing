package ru.otus.barista.feature.menu.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import ru.otus.barista.feature.menu.data.remote.dto.DrinkDto
import ru.otus.barista.feature.menu.data.remote.dto.MenuResponseDto

/** Backend contract. Served by `FakeBackendInterceptor` in the app and by `MockWebServer` in integration tests. */
interface MenuApi {
    @GET("menu")
    suspend fun getMenu(): MenuResponseDto

    @GET("drinks/{id}")
    suspend fun getDrink(@Path("id") id: String): DrinkDto
}
