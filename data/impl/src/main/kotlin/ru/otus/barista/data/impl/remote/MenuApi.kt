package ru.otus.barista.data.impl.remote

import retrofit2.http.GET
import retrofit2.http.Path
import ru.otus.barista.data.model.DrinkDto
import ru.otus.barista.data.model.MenuDto

/** Backend contract. Served by `FakeBackendInterceptor` in the app and by `MockWebServer` in integration tests. */
internal interface MenuApi {
    @GET("menu")
    suspend fun getMenu(): MenuDto

    @GET("drinks/{id}")
    suspend fun getDrink(@Path("id") id: String): DrinkDto
}
