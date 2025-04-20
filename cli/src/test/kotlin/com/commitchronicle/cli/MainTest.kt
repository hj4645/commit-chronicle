package com.commitchronicle.cli

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MainTest {

    @Test
    fun `test main function handles PrintHelpMessage exception`() {
        // This test verifies that the main function doesn't throw an exception
        // when called with no arguments, which would trigger the PrintHelpMessage exception
        var exceptionThrown = false
        try {
            main(emptyArray())
        } catch (e: Exception) {
            exceptionThrown = true
        }
        assertTrue(!exceptionThrown, "Exception was thrown when running main with no arguments")
    }

    @Test
    fun `test main function handles help flag`() {
        // This test verifies that the main function doesn't throw an exception
        // when called with the help flag, which would trigger the PrintHelpMessage exception
        var exceptionThrown = false
        try {
            main(arrayOf("--help"))
        } catch (e: Exception) {
            exceptionThrown = true
        }
        assertTrue(!exceptionThrown, "Exception was thrown when running main with --help flag")
    }
}
