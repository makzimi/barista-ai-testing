package ru.otus.barista.core.testing

/**
 * Records every action a composable reports, so a UI test can assert on them:
 * `MenuScreen(state, onAction = actions)` or `onAction = actions::add`, then `assertEquals(listOf(…), actions.items)`.
 */
class RecordingActions<A> : (A) -> Unit {
    val items: MutableList<A> = mutableListOf()

    fun add(action: A) {
        items += action
    }

    override fun invoke(action: A) = add(action)
}
