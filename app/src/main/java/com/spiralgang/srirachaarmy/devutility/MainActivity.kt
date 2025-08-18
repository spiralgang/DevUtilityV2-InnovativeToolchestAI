package com.spiralgang.srirachaarmy.devutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.spiralgang.srirachaarmy.devutility.ui.theme.SrirachaArmyTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * MainActivity for SrirachaArmy DevUtility
 * Entry point for the comprehensive Android IDE experience
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("MainActivity starting - SrirachaArmy DevUtility v${BuildConfig.SRIRACHA_ARMY_VERSION}")
        
        enableEdgeToEdge()
        setContent {
            SrirachaArmyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SrirachaArmyMainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        
        Timber.d("MainActivity UI initialized successfully")
    }
}

@Composable
fun SrirachaArmyMainScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = "SrirachaArmy DevUtility v${BuildConfig.SRIRACHA_ARMY_VERSION}\nDeepSeek IDE Integration\nBuild: ${BuildConfig.BUILD_TIMESTAMP}",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SrirachaArmyMainScreenPreview() {
    SrirachaArmyTheme {
        SrirachaArmyMainScreen()
    }
}