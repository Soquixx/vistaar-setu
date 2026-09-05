package com.vistaarsetu.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Get the context of the app under test
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext

        // Verify the package name
        assertEquals(
            "com.vistaarsetu.app",
            appContext.packageName
        )
    }
}