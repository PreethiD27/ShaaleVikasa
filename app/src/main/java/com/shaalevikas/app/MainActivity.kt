package com.shaalevikas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.shaalevikas.app.ui.MainViewModel
import com.shaalevikas.app.ui.ShaaleVikasNavHost
import com.shaalevikas.app.ui.theme.ShaaleVikasTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShaaleVikasTheme { ShaaleVikasNavHost(vm) } }
    }
}
