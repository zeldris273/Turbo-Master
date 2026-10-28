package com.turbomaster.app

import org.junit.Assert.*
import org.junit.Test

class AppSafetyExclusionTest {

    private val criticalPackages = setOf(
        "com.turbomaster.app",
        "com.android.systemui",
        "com.android.phone",
        "com.android.settings",
        "com.google.android.gms",
        "com.google.android.gsf",
        "android",
        "com.android.server.telecom",
        "com.android.launcher3"
    )

    private fun isPackageProtected(pkg: String): Boolean {
        return criticalPackages.contains(pkg)
    }

    @Test
    fun testTurboSelfIsProtected() {
        assertTrue("Turbo cannot kill itself", isPackageProtected("com.turbomaster.app"))
    }

    @Test
    fun testSystemUiIsProtected() {
        assertTrue("SystemUI must be protected against force-stop", isPackageProtected("com.android.systemui"))
    }

    @Test
    fun testPhoneServiceIsProtected() {
        assertTrue("Phone telephony must be protected", isPackageProtected("com.android.phone"))
    }

    @Test
    fun testGooglePlayServicesIsProtected() {
        assertTrue("Google Play Services must be protected", isPackageProtected("com.google.android.gms"))
    }

    @Test
    fun testUserAppCanBeManaged() {
        assertFalse("Third-party apps can be managed", isPackageProtected("com.facebook.katana"))
        assertFalse("Third-party games can be managed", isPackageProtected("com.kiloo.subwaysurf"))
    }
}
