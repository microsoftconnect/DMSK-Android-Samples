//
//  ComposeUIAndroidViewBindingActivity.kt
//  DM_SpeechKitComposeUISample
//
//  Copyright 2026-2027 Nuance Communications, Inc. All rights reserved.
//

package com.nuance.dm_speechkit.composeuisample.reports

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuance.speechanywhere.VuiController
import com.nuance.dm_speechkit.composeuisample.databinding.ActivityComposeUiAndroidViewBindingReportBinding

//
// This Activity demonstrates the first Compose UI integration pattern: AndroidViewBinding with XML Layout.
// - It uses Compose's AndroidViewBinding to embed a traditional XML layout file.
// - The XML layout contains speech-enabled text fields.
// - This pattern is useful for transitioning existing XML-based layouts to Compose-based Activities.
// - The TextField in the XML layout is speech-enabled via SpeechKit integration in the manifest.
//
/**
 * ComposeUIAndroidViewBindingActivity demonstrates Compose UI integration with AndroidViewBinding and XML layouts.
 * 
 * This pattern shows how to embed traditional XML-based layouts within a Compose Activity using
 * AndroidViewBinding. The XML layout can contain speech-enabled controls that integrate with SpeechKit.
 * This approach is useful for:
 * - Gradually migrating from XML to pure Compose
 * - Reusing existing XML layouts in Compose-based Activities
 * - Mixing traditional View-based controls with Compose UI
 */
class ComposeUIAndroidViewBindingActivity : ComponentActivity() {
    private var vuiController: VuiController? = null

    /**
     * onCreate is called when the Activity is first created.
     * It sets up the Compose content with an embedded XML layout via AndroidViewBinding.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeControllerAndFinish()
            }
        })
        setContent {
            MaterialTheme {
                ComposeUIAndroidViewBindingReportScreen(
                    onBack = ::closeControllerAndFinish,
                    onControllerAvailable = { vuiController = it },
                )
            }
        }
    }

    private fun closeControllerAndFinish() {
        vuiController?.close("")
        vuiController = null
        finish()
    }

    override fun onDestroy() {
        vuiController?.close("")
        vuiController = null
        super.onDestroy()
    }
}

/**
 * ComposeUIAndroidViewBindingReportScreen displays a Compose UI that embeds a traditional XML layout.
 * 
 * It provides:
 * - A Material3 Scaffold with TopAppBar for navigation
 * - AndroidViewBinding to inflate and display an XML layout (activity_compose_ui_android_view_binding_report.xml)
 * - Integration of traditional View-based controls within a Compose-based Activity
 * 
 * @param onBack Callback function to handle the back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeUIAndroidViewBindingReportScreen(
    onBack: () -> Unit,
    onControllerAvailable: (VuiController) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidViewBinding and XML") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
            )
        },
    ) { innerPadding ->
        // Use AndroidViewBinding to embed the XML layout
        AndroidViewBinding(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = ActivityComposeUiAndroidViewBindingReportBinding::inflate,
            update = {
                onControllerAvailable(vuiController)
                val scrollView = reportScrollView
                val contentPadding = (16 * scrollView.resources.displayMetrics.density).toInt()
                ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    view.setPadding(
                        contentPadding,
                        contentPadding,
                        contentPadding,
                        contentPadding + imeBottom,
                    )
                    insets
                }
                ViewCompat.requestApplyInsets(scrollView)
            },
        )
    }
}
