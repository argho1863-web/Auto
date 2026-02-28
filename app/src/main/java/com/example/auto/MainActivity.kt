package com.example.auto

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Main screen for the Wi‑Fi automation app. Shows a toggle to start/stop the
 * periodic worker and a simple list of SSIDs the user wants to automatically
 * connect to when in range.
 *
 * The app cannot toggle the Wi‑Fi radio directly on Android 10+; instead we ask the
 * user to enable it via the platform settings panel. Documentation of the workaround
 * is in the comments below.
 */
class MainActivity : ComponentActivity() {
    private val workerTag = "wifi-monitor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val autoConnectEnabled = remember { mutableStateOf(false) }
            val ssids = remember { mutableStateListOf<String>() }
            val newSsid = remember { mutableStateOf("") }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Auto-Connect Service", style = MaterialTheme.typography.h5)
                Switch(
                    checked = autoConnectEnabled.value,
                    onCheckedChange = { checked ->
                        autoConnectEnabled.value = checked
                        if (checked) scheduleWorker()
                        else cancelWorker()
                    }
                )

                OutlinedTextField(
                    value = newSsid.value,
                    onValueChange = { newSsid.value = it },
                    label = { Text("SSID to monitor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Button(onClick = {
                    if (newSsid.value.isNotBlank()) {
                        ssids.add(newSsid.value)
                        newSsid.value = ""
                    }
                }) {
                    Text("Add SSID")
                }

                LazyColumn {
                    items(ssids.size) { idx ->
                        val value = ssids[idx]
                        Column {
                            Text(value)
                            Button(onClick = { ssids.removeAt(idx) }) {
                                Text("Remove")
                            }
                        }
                    }
                }

                Button(onClick = { ensureWifiEnabled() }) {
                    Text("Ensure Wi-Fi is On")
                }
            }
        }
    }

    private fun scheduleWorker() {
        val request = PeriodicWorkRequestBuilder<WifiMonitorWorker>(15, TimeUnit.MINUTES)
            .addTag(workerTag)
            .build()
        WorkManager.getInstance(this).enqueue(request)
        Toast.makeText(this, "Worker scheduled", Toast.LENGTH_SHORT).show()
    }

    private fun cancelWorker() {
        WorkManager.getInstance(this).cancelAllWorkByTag(workerTag)
        Toast.makeText(this, "Worker canceled", Toast.LENGTH_SHORT).show()
    }

    private fun ensureWifiEnabled() {
        val wifiManager = applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        if (!wifiManager.isWifiEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android 10+ apps may not toggle Wi-Fi directly. We launch
                // the system Settings panel which shows a toggle the user can tap.
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                startActivity(panelIntent)
            } else {
                wifiManager.isWifiEnabled = true
            }
        } else {
            Toast.makeText(this, "Wi-Fi already enabled", Toast.LENGTH_SHORT).show()
        }
    }
}