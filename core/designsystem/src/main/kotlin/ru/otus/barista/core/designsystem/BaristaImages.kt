package ru.otus.barista.core.designsystem

import androidx.annotation.DrawableRes

/**
 * Flat illustrations shipped with the design system (`res/drawable-nodpi`), looked up by backend id.
 * A pure lookup: `*StateMapper`s put the returned ids into State, composables only render them.
 * Unknown ids fall back to a generic picture, so a new drink on the backend never crashes the UI.
 */
object BaristaImages {

    /** Shown for a drink id the app has no illustration for. */
    @DrawableRes val drinkFallback: Int = R.drawable.drink_cappuccino

    /** Shown for an add-on id the app has no illustration for. */
    @DrawableRes val addOnFallback: Int = R.drawable.addon_extra_shot

    @DrawableRes val nothingFound: Int = R.drawable.state_nothing_found

    @DrawableRes val noFavorites: Int = R.drawable.state_no_favorites

    @DrawableRes val menuEmpty: Int = R.drawable.state_menu_empty

    @DrawableRes val error: Int = R.drawable.state_error

    @DrawableRes val categoryCoffee: Int = R.drawable.category_coffee

    @DrawableRes val categoryTea: Int = R.drawable.category_tea

    @DrawableRes val categorySeasonal: Int = R.drawable.category_seasonal

    private val drinks: Map<String, Int> = mapOf(
        "espresso" to R.drawable.drink_espresso,
        "americano" to R.drawable.drink_americano,
        "cappuccino" to R.drawable.drink_cappuccino,
        "latte" to R.drawable.drink_latte,
        "flat_white" to R.drawable.drink_flat_white,
        "raf" to R.drawable.drink_raf,
        "matcha_latte" to R.drawable.drink_matcha_latte,
        "black_tea" to R.drawable.drink_black_tea,
        "green_tea" to R.drawable.drink_green_tea,
        "cocoa" to R.drawable.drink_cocoa,
        "pumpkin_latte" to R.drawable.drink_pumpkin_latte,
        "iced_latte" to R.drawable.drink_iced_latte,
    )

    private val addOns: Map<String, Int> = mapOf(
        "extra_shot" to R.drawable.addon_extra_shot,
        "syrup" to R.drawable.addon_syrup,
        "oat_milk" to R.drawable.addon_oat_milk,
        "whipped_cream" to R.drawable.addon_whipped_cream,
    )

    /** Illustration of the drink with backend id [id] (`"flat_white"`), or [drinkFallback]. */
    @DrawableRes
    fun drink(id: String): Int = drinks[id] ?: drinkFallback

    /** Icon of the add-on with backend id [id] (`"oat_milk"`), or [addOnFallback]. */
    @DrawableRes
    fun addOn(id: String): Int = addOns[id] ?: addOnFallback
}
