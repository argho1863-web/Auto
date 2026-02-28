package com.example.auto

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker executed periodically by WorkManager. It inspects the list of SSIDs saved
 * by the user (for brevity we're just hard‑coding a sample list here). When one
 * of the prioritized networks is found in a scan, we submit a network suggestion
 * (or specifier on API 29+) to prompt the OS to connect.
 *
 * The modern APIs prevent apps from directly calling WifiManager.connect();
 * suggestions/specifiers are the only mechanism that respects security boundaries.
 */
class WifiMonitorWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // scan for available networks; requires ACCESS_FINE_LOCATION/NEARBY_WIFI_DEVICES
        val success = wifiManager.startScan()
        if (!success) return@withContext Result.retry()

        val results: List<ScanResult> = wifiManager.scanResults
        // In real app the SSIDs would come from a database or preferences
        val wantedSsids = listOf("HomeNetwork", "OfficeSSID")

        for (result in results) {
            if (wantedSsids.contains(result.SSID)) {
                // build suggestion/specifier
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // When using a specifier we connect immediately and the framework
                    // will prompt the user for consent if necessary. Suggestion is
                    // better for background autopilot but requires approval.
                    val suggestion = WifiNetworkSuggestion.Builder()
                        .setSsid(result.SSID)
                        // open network assumed; for WPA2 include passphrase also
                        .build()
                    wifiManager.addNetworkSuggestions(listOf(suggestion))
                    // optionally listen for broadcast ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION
                } else {
                    // older devices
                    val config = android.net.wifi.WifiConfiguration().apply {
                        SSID = '"' + result.SSID + '"'
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    }
                    val netId = wifiManager.addNetwork(config)
                    if (netId != -1) {
                        wifiManager.disconnect()
                        wifiManager.enableNetwork(netId, true)
                        wifiManager.reconnect()
                    }
                }
                break
            }
        }

        Result.success()
    }
}