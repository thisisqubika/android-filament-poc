// FilamentActivity.kt
package com.example.native3dpoc

import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.utils.Utils

class FilamentActivity : ComponentActivity() {

    companion object {
        init {
            Utils.init()
        }
    }

    private val modelRenderer = ModelRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { context ->
                                SurfaceView(context).apply {
                                    modelRenderer.onSurfaceAvailable(this, lifecycle)
                                }
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    modelRenderer.zoomByFov(true)
                                },
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Zoom In")
                            }
                            IconButton(
                                onClick = {
                                    modelRenderer.zoomByFov(false)
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Zoom Out"
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}