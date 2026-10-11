package ru.otus.barista.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.otus.barista.app.navigation.BaristaNavHost
import ru.otus.barista.core.designsystem.theme.BaristaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as BaristaApplication).graph
        setContent {
            BaristaTheme {
                BaristaNavHost(graph = graph)
            }
        }
    }
}
