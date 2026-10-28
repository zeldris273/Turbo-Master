package com.turbomaster.app

import com.turbomaster.app.data.shizuku.ShizukuCommandRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ShizukuCommandSecurityTest {

    @Test
    fun testRejectsEmptyCommandTokens() = runBlocking {
        val result = ShizukuCommandRunner.executeSafe(emptyArray())
        assertTrue("Empty tokens should fail", result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testRejectsDisallowedBinary() = runBlocking {
        val result = ShizukuCommandRunner.executeSafe(arrayOf("rm", "-rf", "/data/local/tmp"))
        assertTrue("Destructive command 'rm' must be rejected", result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun testRejectsSuAndRootEscalation() = runBlocking {
        val result = ShizukuCommandRunner.executeSafe(arrayOf("su", "-c", "whoami"))
        assertTrue("Root escalation attempt must be rejected", result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun testRejectsForbiddenTokenInArguments() = runBlocking {
        val result = ShizukuCommandRunner.executeSafe(arrayOf("am", "force-stop", "reboot"))
        assertTrue("Forbidden token 'reboot' must fail argument validation", result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
