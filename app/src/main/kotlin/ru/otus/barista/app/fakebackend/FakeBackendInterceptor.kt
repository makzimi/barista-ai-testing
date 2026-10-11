package ru.otus.barista.app.fakebackend

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Bundled "backend": answers `GET /menu` and `GET /drinks/{id}` from `assets/fake-backend/menu.json`
 * after [delayMillis], so the app never needs the internet. Honours [FakeBackendConfig.failNextRequest].
 * Integration tests use `MockWebServer` instead, to exercise the real HTTP path.
 */
class FakeBackendInterceptor(
    private val config: FakeBackendConfig,
    private val readAsset: (path: String) -> String,
    private val delayMillis: Long = DEFAULT_DELAY_MILLIS,
) : Interceptor {

    private val json = Json { ignoreUnknownKeys = true }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (delayMillis > 0) Thread.sleep(delayMillis)
        val segments = request.url.pathSegments.filter { it.isNotEmpty() }
        return when {
            config.consumeFailure() -> request.respond(HTTP_SERVER_ERROR, """{"error":"Fake backend failure"}""")
            request.method != "GET" -> request.respond(HTTP_NOT_FOUND, NOT_FOUND_BODY)
            segments == listOf("menu") -> request.respond(HTTP_OK, readAsset(MENU_ASSET))
            segments.size == 2 && segments[0] == "drinks" -> findDrink(segments[1])
                ?.let { request.respond(HTTP_OK, it.toString()) }
                ?: request.respond(HTTP_NOT_FOUND, NOT_FOUND_BODY)
            else -> request.respond(HTTP_NOT_FOUND, NOT_FOUND_BODY)
        }
    }

    private fun findDrink(id: String): JsonObject? =
        json.parseToJsonElement(readAsset(MENU_ASSET))
            .jsonObject.getValue("drinks")
            .jsonArray
            .map { it.jsonObject }
            .firstOrNull { it["id"]?.jsonPrimitive?.content == id }

    private fun Request.respond(code: Int, body: String): Response =
        Response.Builder()
            .request(this)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == HTTP_OK) "OK" else "Error")
            .body(body.toResponseBody(JSON_MEDIA_TYPE))
            .build()

    companion object {
        const val DEFAULT_DELAY_MILLIS: Long = 300L
        const val MENU_ASSET: String = "fake-backend/menu.json"
        private const val HTTP_OK = 200
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500
        private const val NOT_FOUND_BODY = """{"error":"Not found"}"""
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
