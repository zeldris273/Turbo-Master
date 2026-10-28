package com.turbomaster.app.data.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CommandExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val executionTimeMs: Long
)

object ShizukuCommandRunner {
    private const val TAG = "ShizukuCommandRunner"
    private const val DEFAULT_TIMEOUT_MS = 8000L
    private const val CONNECTION_TIMEOUT_MS = 5000L

    private val allowlistedBinaries =
        setOf("am", "cmd", "settings", "dumpsys", "sm")

    private val forbiddenTokens = setOf(
        "su", "setenforce", "rm", "dd", "reboot", "fastboot",
        "sh", "busybox", "iptables", "killall", "mount",
        "chmod", "chown"
    )

    suspend fun executeSafe(
        commandTokens: Array<String>,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): Result<CommandExecutionResult> = withContext(Dispatchers.IO) {
        if (commandTokens.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("Command tokens cannot be empty")
            )
        }

        if (commandTokens[0] !in allowlistedBinaries) {
            return@withContext Result.failure(
                SecurityException(
                    "Command '${commandTokens[0]}' is not in Turbo's privileged execution allowlist."
                )
            )
        }

        val forbidden = commandTokens.firstOrNull {
            it.lowercase() in forbiddenTokens
        }

        if (forbidden != null) {
            return@withContext Result.failure(
                SecurityException("Forbidden token detected: '$forbidden'")
            )
        }

        try {
            if (!Shizuku.pingBinder()) {
                throw IllegalStateException("Shizuku service is unavailable.")
            }

            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Turbo has not been granted Shizuku permission.")
            }

            val context = ShizukuManager.applicationContextOrNull()
                ?: throw IllegalStateException("ShizukuManager is not initialized.")

            val args = Shizuku.UserServiceArgs(
                ComponentName(context, ShizukuCommandService::class.java)
            )
                .tag("TurboMasterCommandService")
                .version(2)
                .processNameSuffix("turbo_command")
                .daemon(false)
                .debuggable(false)

            val pending =
                AtomicReference<CancellableContinuation<ICommandService>?>(null)

            fun failPending(message: String) {
                val continuation = pending.getAndSet(null) ?: return
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException(message)
                    )
                }
            }

            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    binder: IBinder?
                ) {
                    val continuation = pending.getAndSet(null) ?: return
                    if (!continuation.isActive) return

                    if (binder == null) {
                        continuation.resumeWithException(
                            IllegalStateException("UserService returned a null Binder.")
                        )
                    } else {
                        continuation.resume(
                            ICommandService.Stub.asInterface(binder)
                        )
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    failPending("Shizuku UserService disconnected.")
                }

                override fun onBindingDied(name: ComponentName?) {
                    failPending("Shizuku UserService binding died.")
                }

                override fun onNullBinding(name: ComponentName?) {
                    failPending("Shizuku UserService returned a null binding.")
                }
            }

            var bound = false

            try {
                val service = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
                    suspendCancellableCoroutine<ICommandService> { continuation ->
                        pending.set(continuation)

                        continuation.invokeOnCancellation {
                            pending.compareAndSet(continuation, null)
                        }

                        try {
                            Shizuku.bindUserService(args, connection)
                            bound = true
                        } catch (t: Throwable) {
                            pending.compareAndSet(continuation, null)
                            if (continuation.isActive) {
                                continuation.resumeWithException(t)
                            }
                        }
                    }
                } ?: throw IllegalStateException(
                    "Timed out waiting for Shizuku UserService."
                )

                val bundle = service.execute(
                    commandTokens,
                    timeoutMs.coerceIn(1L, 60_000L)
                )

                Result.success(
                    CommandExecutionResult(
                        exitCode = bundle.getInt("exitCode", -1),
                        stdout = bundle.getString("stdout").orEmpty(),
                        stderr = bundle.getString("stderr").orEmpty(),
                        executionTimeMs = bundle.getLong("executionTimeMs", 0L)
                    )
                )
            } finally {
                pending.set(null)
                if (bound) {
                    try {
                        Shizuku.unbindUserService(args, connection, false)
                    } catch (t: Throwable) {
                        Log.w(TAG, "Could not unbind UserService connection", t)
                    }
                }
            }
        } catch (t: Exception) {
            Log.e(TAG, "UserService command failed", t)
            Result.failure(t)
        }
    }
}
