package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.ui.MainFeatureScreen
import com.example.ui.theme.ChronoPulseTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      // Default to false (crisp white monochrome UI matching the uploaded ReactJS template)
      var isDarkTheme by rememberSaveable { mutableStateOf(false) }
      ChronoPulseTheme(darkTheme = isDarkTheme) {
        MainFeatureScreen(
          isDarkTheme = isDarkTheme,
          onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
        )
      }
    }
  }
}
