package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.theme.MaxFilesTheme

import com.example.di.AppContainer
import com.example.ui.MaxFilesApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val appContainer = AppContainer(applicationContext)
    
    enableEdgeToEdge()
    setContent {
      var useSystemTheme by remember { mutableStateOf(appContainer.settingsRepository.useSystemTheme) }
      var isDarkTheme by remember { mutableStateOf(appContainer.settingsRepository.isDarkTheme) }
      
      val prefs = applicationContext.getSharedPreferences("max_files_settings", android.content.Context.MODE_PRIVATE)
      DisposableEffect(prefs) {
          val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
              if (key == "useSystemTheme") useSystemTheme = appContainer.settingsRepository.useSystemTheme
              if (key == "isDarkTheme") isDarkTheme = appContainer.settingsRepository.isDarkTheme
          }
          prefs.registerOnSharedPreferenceChangeListener(listener)
          onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
      }

      MaxFilesTheme(useSystemTheme = useSystemTheme, forceDarkTheme = isDarkTheme) {
          var hasPermission by remember { mutableStateOf(checkStoragePermission()) }
          
          if (hasPermission) {
              MaxFilesApp(appContainer)
          } else {
              PermissionScreen(
                  onPermissionGranted = { hasPermission = true },
                  onRequestPermission = { requestStoragePermission() }
              )
          }
      }
    }
  }

  private fun checkStoragePermission(): Boolean {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          Environment.isExternalStorageManager()
      } else {
          ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
      }
  }

  private val requestPermissionLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
      if (isGranted) {
          recreate()
      }
  }

  private val manageStorageLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
  ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          if (Environment.isExternalStorageManager()) {
              recreate()
          }
      }
  }

  private fun requestStoragePermission() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          try {
              val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
              intent.addCategory("android.intent.category.DEFAULT")
              intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
              manageStorageLauncher.launch(intent)
          } catch (e: Exception) {
              val intent = Intent()
              intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
              manageStorageLauncher.launch(intent)
          }
      } else {
          requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      }
  }
}

@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit, onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onRequestPermission) {
            Text("Grant Storage Permission")
        }
    }
}
