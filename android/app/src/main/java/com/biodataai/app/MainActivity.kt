package com.biodataai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.biodataai.app.navigation.BioDataNavGraph
import com.biodataai.app.navigation.NavRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioDataAITheme {
                Surface {
                    val navController = rememberNavController()
                    BioDataNavGraph(navController, NavRoute.Splash)
                }
            }
        }
    }
}

@Composable
fun BioDataAITheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
