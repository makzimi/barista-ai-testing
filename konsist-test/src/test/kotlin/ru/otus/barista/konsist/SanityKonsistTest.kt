package ru.otus.barista.konsist

import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards against a misconfigured scope: an empty scope would make every other rule pass trivially. */
class SanityKonsistTest {

    @Test
    fun `production scope sees the whole app`() {
        val fileCount = productionScope().files.size

        assertTrue("Expected at least 40 production files, but Konsist saw $fileCount", fileCount >= 40)
    }

    @Test
    fun `test scope sees the unit tests`() {
        val fileCount = testScope().files.size

        assertTrue("Expected at least 5 test files, but Konsist saw $fileCount", fileCount >= 5)
    }
}
