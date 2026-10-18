package ru.otus.barista.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals

class BaristaImagesTest {

    @Test
    fun `drink WHEN the id is in the menu SHOULD return its illustration`() {
        val id = "flat_white"

        val image = BaristaImages.drink(id)

        assertEquals(R.drawable.drink_flat_white, image)
    }

    @Test
    fun `drink WHEN the id is unknown SHOULD fall back to the cappuccino illustration`() {
        val id = "unicorn_frappe"

        val image = BaristaImages.drink(id)

        assertEquals(R.drawable.drink_cappuccino, image)
    }

    @Test
    fun `addOn WHEN the id is in the menu SHOULD return its icon`() {
        val id = "whipped_cream"

        val image = BaristaImages.addOn(id)

        assertEquals(R.drawable.addon_whipped_cream, image)
    }

    @Test
    fun `addOn WHEN the id is unknown SHOULD fall back to the extra shot icon`() {
        val id = "gold_flakes"

        val image = BaristaImages.addOn(id)

        assertEquals(R.drawable.addon_extra_shot, image)
    }
}
