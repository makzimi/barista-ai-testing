package ru.otus.barista.feature.menu.api

import ru.otus.barista.core.common.Money

@JvmInline
value class AddOnId(val value: String)

data class AddOn(
    val id: AddOnId,
    val name: String,
    val price: Money,
)
