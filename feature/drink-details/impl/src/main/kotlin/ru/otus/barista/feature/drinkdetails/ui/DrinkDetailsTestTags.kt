package ru.otus.barista.feature.drinkdetails.ui

import ru.otus.barista.feature.drinkdetails.api.Size
import ru.otus.barista.feature.menu.api.AddOnId

/** Test tags of the drink details screen. Prefixed tags are completed with an id: `DrinkDetailsTestTags.size(S)`. */
object DrinkDetailsTestTags {
    const val SCREEN: String = "details_screen"
    const val LOADING: String = "details_loading"
    const val ERROR: String = "details_error"
    const val CONTENT: String = "details_content"
    const val BACK: String = "details_back"
    const val FAVORITE: String = "details_favorite"
    const val SIZE: String = "details_size_"
    const val ADD_ON_ROW: String = "details_add_on_"
    const val ADD_ON_INCREMENT: String = "details_add_on_increment_"
    const val ADD_ON_DECREMENT: String = "details_add_on_decrement_"
    const val ADD_ON_QUANTITY: String = "details_add_on_quantity_"
    const val TOTAL_PRICE: String = "details_total_price"
    const val ADD_TO_CART: String = "details_add_to_cart"

    fun size(size: Size): String = SIZE + size.name

    fun addOnRow(id: AddOnId): String = ADD_ON_ROW + id.value

    fun addOnIncrement(id: AddOnId): String = ADD_ON_INCREMENT + id.value

    fun addOnDecrement(id: AddOnId): String = ADD_ON_DECREMENT + id.value

    fun addOnQuantity(id: AddOnId): String = ADD_ON_QUANTITY + id.value
}
