package ru.otus.barista.core.testing.screenshot

/**
 * Excludes a preview from the generated preview screenshot tests (see [BaristaPreviewTester]).
 * Use it only for a preview that cannot be rendered deterministically, and say why next to it.
 *
 * [BaristaPreviewTester] matches the annotation by its simple name, because previews live in production
 * source sets that cannot see `core:testing`: a module that needs it declares its own
 * `annotation class DoNotTestPreview` (e.g. in `core:designsystem`) and it is honoured just the same.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DoNotTestPreview
