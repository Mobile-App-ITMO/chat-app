package io.ktor.chat

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import io.ktor.chat.app.*
import io.ktor.chat.calls.*
import io.ktor.chat.client.*
import io.ktor.chat.vm.*
import io.ktor.client.*
import io.ktor.client.webrtc.*
import io.ktor.client.webrtc.media.*
import io.ktor.utils.io.ExperimentalKtorApi
import org.webrtc.EglBase
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKtorApi::class)
fun createVideoCallVm(ctx: Context, http: () -> HttpClient): VideoCallViewModel {
    val egbBase = EglBase.create()
    EglBaseProvider.eglBase = egbBase

    val rtcClient = WebRtcClient(AndroidWebRtc) {
        defaultConnectionConfig = {
            iceServers = joinIceServers(
                BuildKonfig.STUN_URL,
                BuildKonfig.STUN_USERNAME,
                BuildKonfig.STUN_CREDENTIAL,
                BuildKonfig.TURN_URL,
                BuildKonfig.TURN_USERNAME,
                BuildKonfig.TURN_CREDENTIAL
            )
            statsRefreshRate = 10.seconds
            remoteTracksReplay = 10
        }
        mediaTrackFactory = AndroidMediaDevices(ctx, egbBase)
    }

    return VideoCallViewModel(rtcClient, HttpSignalingClient(http))
}

class MainActivity : ComponentActivity() {
    // Required permissions for video calls
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    )

    // State to track if permissions are granted
    private val permissionsGranted = mutableStateOf(false)

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all required permissions are granted
        permissionsGranted.value = permissions.entries.all { it.value }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        // Request the permissions
        requestPermissionLauncher.launch(permissionsToRequest)
    }

    private fun getSystemInfo(): SystemInfo {
        return try {
            val memoryText = try {
                val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)

                val usedMB = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
                val totalMB = memoryInfo.totalMem / (1024 * 1024)
                val percentage = ((memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat() * 100).toInt()

                "${usedMB}MB / ${totalMB}MB ($percentage%)"
            } catch (e: Exception) {
                "Not available"
            }

            val storageText = try {
                val stat = StatFs(filesDir.path)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                val availableBlocks = stat.availableBlocksLong

                val totalGB = (totalBlocks * blockSize) / (1024 * 1024 * 1024)
                val availableGB = (availableBlocks * blockSize) / (1024 * 1024 * 1024)
                val usedPercentage = ((totalBlocks - availableBlocks).toFloat() / totalBlocks.toFloat() * 100).toInt()

                "${availableGB}GB free / ${totalGB}GB ($usedPercentage% used)"
            } catch (e: Exception) {
                "Not available"
            }

            val appText = try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                "v${packageInfo.versionName} (${packageInfo.versionCode})"
            } catch (e: Exception) {
                "Unknown"
            }

            SystemInfo(memoryText, storageText, appText)
        } catch (e: Exception) {
            SystemInfo("Error", "Error", "Error")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and request permissions early
        checkAndRequestPermissions()

        setContent {
            val chatClient = HttpChatClient()
            val chatVm = createViewModel(chatClient)
            val videoCallVm = createVideoCallVm(applicationContext) { chatClient.getHttp() }

            fun refreshSystemInfo() {
                val info = getSystemInfo()
                chatVm.memoryInfo.value = info.memoryInfo
                chatVm.storageInfo.value = info.storageInfo
                chatVm.appInfo.value = info.appInfo
            }

            LaunchedEffect(Unit) {
                refreshSystemInfo()
            }

            // Use LaunchedEffect to check permissions status when the app starts
            LaunchedEffect(Unit) {
                checkAndRequestPermissions()
            }

            ChatApplication(
                chatVm = chatVm,
                videoCallVm = videoCallVm,
                onRefreshSystemInfo = { refreshSystemInfo() }
            )
        }
    }
}

data class SystemInfo(
    val memoryInfo: String,
    val storageInfo: String,
    val appInfo: String
)