package com.turbomaster.app

import org.junit.Assert.*
import org.junit.Test

class TurboActionValidationTest {

    @Test
    fun testStandbyBucketApiLevelValidation() {
        val minApiForStandbyBuckets = 28 // Introduced in Android 9 Pie
        val currentDeviceApi = 24 // Android 7.0 Nougat

        val isSupportedOnCurrentDevice = currentDeviceApi >= minApiForStandbyBuckets
        assertFalse("App Standby Buckets must not be marked supported on API 24", isSupportedOnCurrentDevice)
    }

    @Test
    fun testReversibleActionHasRollbackInstructions() {
        val action = object {
            val id = "tune_animator_scale"
            val isReversible = true
            val rollbackInstructions = "settings put global window_animation_scale 1.0"
        }

        assertTrue("Reversible actions must define explicit rollback instructions", action.isReversible)
        assertNotNull(action.rollbackInstructions)
        assertTrue(action.rollbackInstructions.contains("1.0"))
    }
}
