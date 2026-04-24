package com.morppad.operque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.morppad.operque.data.services.AppNavigation
import com.morppad.operque.ui.theme.OperqueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OperqueTheme { AppNavigation() } }
    }
}
