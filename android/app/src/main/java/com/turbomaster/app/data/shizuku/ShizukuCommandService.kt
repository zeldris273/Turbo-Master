package com.turbomaster.app.data.shizuku

import android.os.Bundle
import android.os.SystemClock
import android.util.Log

class ShizukuCommandService : ICommandService.Stub() {

    companion object {
        private const val TAG = "ShizukuCommandService"

        private val ALLOWED_BINARIES = setOf(
            "am", "cmd", "settings", "dumpsys", "sm"
        )

        private val FORBIDDEN_TOKENS = setOf(
            "su", "setenforce", "rm", "dd", "reboot", "fastboot",
            "sh", "busybox", "iptables", "killall", "mount",
            "chmod", "chown"
        )
    }

    override fun execute(
        commandTokens: Array<out String>?,
        timeoutMs: Long
    ): Bundle {
        val startTime = System.currentTimeMillis()
        val result = Bundle()

        fun finish(
            exitCode: Int,
            stdout: String,
            stderr: String
        ): Bundle {
            result.putInt("exitCode", exitCode)
            result.putString("stdout", stdout)
            result.putString("stderr", stderr)
            result.putLong(
                "executionTimeMs",
                System.currentTimeMillis() - startTime
            )
            return result
        }

        if (commandTokens.isNullOrEmpty()) {
            return finish(-1, "", "Command tokens cannot be empty")
        }

        val binary = commandTokens[0]

        if (binary !in ALLOWED_BINARIES) {
            return finish(
                -1, "",
                "Command '$binary' is not in the allowlist"
            )
        }

        val forbidden = commandTokens.firstOrNull {
            it.lowercase() in FORBIDDEN_TOKENS
        }

        if (forbidden != null) {
            return finish(
                -1, "",
                "Forbidden token detected: '$forbidden'"
            )
        }

        val effectiveTimeout = timeoutMs.coerceIn(1L, 60_000L)

        return try {
            // Pass arguments directly; do not concatenate a shell command.
            val process = ProcessBuilder(commandTokens.toList()).start()
            val stdout = StringBuilder()
            val stderr = StringBuilder()

            // Drain both streams concurrently to avoid pipe deadlocks.
            val stdoutThread = Thread {
                try {
                    process.inputStream.bufferedReader().use {
                        stdout.append(it.readText())
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not read stdout", e)
                }
            }

            val stderrThread = Thread {
                try {
                    process.errorStream.bufferedReader().use {
                        stderr.append(it.readText())
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not read stderr", e)
                }
            }

            stdoutThread.start()
            stderrThread.start()

            val deadline = SystemClock.elapsedRealtime() + effectiveTimeout
            var exitCode: Int? = null

            while (SystemClock.elapsedRealtime() < deadline) {
                try {
                    exitCode = process.exitValue()
                    break
                } catch (_: IllegalThreadStateException) {
                    SystemClock.sleep(10)
                }
            }

            if (exitCode == null) {
                process.destroy()

                // Allow the process a short time to close its output streams.
                val stopDeadline = SystemClock.elapsedRealtime() + 200L
                while (SystemClock.elapsedRealtime() < stopDeadline) {
                    try {
                        process.exitValue()
                        break
                    } catch (_: IllegalThreadStateException) {
                        SystemClock.sleep(10)
                    }
                }

                stdoutThread.join(300)
                stderrThread.join(300)

                finish(-1, stdout.toString(), "Command timed out after ${effectiveTimeout}ms")
            } else {
                stdoutThread.join(1000)
                stderrThread.join(1000)

                finish(exitCode, stdout.toString().trim(), stderr.toString().trim())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed", e)
            finish(-1, "", e.message ?: e.javaClass.simpleName)
        }
    }
}
