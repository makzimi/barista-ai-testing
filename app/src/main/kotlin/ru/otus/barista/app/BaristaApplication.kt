package ru.otus.barista.app

import android.app.Application
import ru.otus.barista.app.di.AppGraph

class BaristaApplication : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
}
