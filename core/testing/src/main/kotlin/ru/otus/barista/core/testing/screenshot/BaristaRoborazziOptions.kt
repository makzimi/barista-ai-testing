package ru.otus.barista.core.testing.screenshot

import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziOptions.CompareOptions
import com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions

/** Where the committed goldens live, relative to the module directory (the working dir of unit tests). */
const val SCREENSHOTS_DIR: String = "src/test/screenshots"

/**
 * Shared Roborazzi options for every screenshot in the project.
 * - `changeThreshold = 0.01f` (1 %), not 0: font antialiasing differs slightly between machines.
 * - `resizeScale = 0.5`: goldens are stored at half resolution to keep the repository small.
 *
 * A getter, not a stored value: [RoborazziOptions] reads the record/verify task type from system properties.
 */
val BaristaRoborazziOptions: RoborazziOptions
    get() = RoborazziOptions(
        compareOptions = CompareOptions(changeThreshold = 0.01f),
        recordOptions = RecordOptions(resizeScale = 0.5),
    )
