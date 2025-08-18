package com.sgneuronlabs.devutilityandroidv2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import com.sgneuronlabs.devutilityandroidv2.ui.DevUtilityAppV2

class MainActivity : ComponentActivity() {
    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            // Handle permission denial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStoragePermissions()
        setContent {
            MaterialTheme {
                Surface {
                    DevUtilityAppV2(
                        onSignIn = { launchSignIn() }
                    )
                }
            }
        }
    }

    private fun requestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun launchSignIn() {
        val signInUrl = "https://platform.openai.com/account/api-keys"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(signInUrl))
        startActivity(intent)
    }
}