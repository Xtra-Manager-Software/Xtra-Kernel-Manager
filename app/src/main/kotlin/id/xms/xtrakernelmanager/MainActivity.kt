package id.xms.xtrakernelmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.xms.xtrakernelmanager.ui.XkmApp

/**
 * MainActivity serves strictly as a wire and lifecycle entry point.
 * All UI compositions and screen states are hosted in XkmApp.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XkmApp()
        }
    }
}
