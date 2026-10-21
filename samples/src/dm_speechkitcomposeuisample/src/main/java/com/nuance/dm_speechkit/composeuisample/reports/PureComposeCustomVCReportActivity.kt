//
//  PureComposeCustomVCReportActivity.kt
//  DM_SpeechKitComposeUISample
//
//  Copyright 2026-2027 Nuance Communications, Inc. All rights reserved.
//

package com.nuance.dm_speechkit.composeuisample.reports

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nuance.speechanywhere.compose.rememberComposeFieldAdapter
import com.nuance.speechanywhere.compose.rememberComposeFieldSetup
import com.nuance.speechanywhere.custom.VuiController

//
// This Activity demonstrates the third and most advanced Compose UI integration pattern: Pure Compose with Custom VuiController.
// - It uses pure Compose TextFields (no XML, no AndroidViewBinding, no AndroidView wrapper).
// - The Compose TextFields are bridged to SpeechKit using ComposeFieldAdapter and custom.VuiController.
// - This pattern represents the most modern approach: pure Compose with native speech integration.
// - The custom.VuiController is hosted in an AndroidView for compatibility, but all user-facing UI is Compose.
//
/**
 * PureComposeCustomVCReportActivity demonstrates pure Compose UI integration with SpeechKit using custom.VuiController.
 * 
 * This is the most advanced pattern, showing:
 * - Pure Compose TextFields (no XML layouts, no AndroidViewBinding, no View-based controls)
 * - Direct bridge from Compose state to SpeechKit via ComposeFieldAdapter
 * - Life cycle management and synchronization with custom.VuiController
 * - Focus management for multi-field speech control
 * 
 * This approach is recommended for new Compose-native applications as it provides:
 * - Full Compose control and styling
 * - Native Compose state management
 * - Modern Material3 design integration
 * - Seamless speech-to-text insertion into Compose TextFields
 */
class PureComposeCustomVCReportActivity : ComponentActivity() {
    private var vuiController: VuiController? = null

    /**
     * onCreate is called when the Activity is first created.
     * It sets up the pure Compose content with custom.VuiController integration.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeControllerAndFinish()
            }
        })
        setContent {
            MaterialTheme {
                PureComposeCustomVCReportScreen(
                    onBack = ::closeControllerAndFinish,
                    onControllerChanged = { vuiController = it },
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
 * PureComposeCustomVCReportScreen displays a pure Compose UI with SpeechKit custom.VuiController integration.
 * 
 * It demonstrates:
 * - Pure Compose OutlinedTextField controls (no XML, no AndroidViewBinding)
 * - ComposeFieldAdapter for bridging Compose state to SpeechKit
 * - Custom.VuiController hosted in an invisible AndroidView
 * - Multi-field focus management and speech routing
 * - Life cycle synchronization for proper SpeechKit session management
 * 
 * @param onBack Callback function to handle the back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PureComposeCustomVCReportScreen(
    onBack: () -> Unit,
    onControllerChanged: (VuiController?) -> Unit,
) {
    val firstControlId = "compose_first_name"
    val secondControlId = "compose_last_name"

    // State for the first name field
    val firstField = rememberComposeFieldSetup()
    // State for the last name field
    val secondField = rememberComposeFieldSetup()
    // Track which field is currently focused
    var activeControlId by rememberSaveable { mutableStateOf<String?>(null) }

    // Create adapter for the first field - bridges Compose state to SpeechKit
    val firstControl = rememberComposeFieldAdapter(
        id = firstControlId,
        valueProvider = { firstField.value },
        onValueChange = firstField.onValueChange,
        requestFocus = { firstField.focusRequester.requestFocus() },
        isFocusedProvider = { activeControlId == firstControlId },
    )
    // Create adapter for the second field - bridges Compose state to SpeechKit
    val secondControl = rememberComposeFieldAdapter(
        id = secondControlId,
        valueProvider = { secondField.value },
        onValueChange = secondField.onValueChange,
        requestFocus = { secondField.focusRequester.requestFocus() },
        isFocusedProvider = { activeControlId == secondControlId },
    )

    // Combine controls for synchronization with VuiController
    val controls = remember(firstControl, secondControl) {
        arrayListOf(firstControl as Any, secondControl as Any)
    }

    // Get the current lifecycle owner for lifecycle-aware updates
    val lifecycleOwner = LocalLifecycleOwner.current
    // Reference to the VuiController instance
    var vuiController by remember { mutableStateOf<VuiController?>(null) }
    // Track synchronized controller to prevent redundant syncs
    var synchronizedController by remember { mutableStateOf<VuiController?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pure Compose With CustomVC") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Descriptive text explaining the pattern
                Text(
                    text = "Pure Compose fields bridged to SpeechKit.",
                    style = MaterialTheme.typography.bodyLarge,
                )

                // First name TextField - pure Compose with speech integration
                OutlinedTextField(
                    value = firstField.value,
                    onValueChange = firstField.onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(firstField.focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused) {
                                activeControlId = firstControlId
                                vuiController?.controlGotFocus(firstControl)
                            }
                        },
                    label = { Text("Enter first name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        cursorColor = Color.Black,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    minLines = 3,
                    maxLines = 6,
                )

                // Last name TextField - pure Compose with speech integration
                OutlinedTextField(
                    value = secondField.value,
                    onValueChange = secondField.onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(secondField.focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused) {
                                activeControlId = secondControlId
                                vuiController?.controlGotFocus(secondControl)
                            }
                        },
                    label = { Text("Enter last name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        cursorColor = Color.Black,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    minLines = 3,
                    maxLines = 6,
                )

                // Display combined output of both fields
                Text(
                    text = "Output: ${firstField.value.text} ${secondField.value.text}",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Host custom.VuiController in an invisible AndroidView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    VuiController(context).apply {
                        setLanguage("en-US")
                        setTopic(VuiController.NUSA_TOPIC_GENERAL_MEDICINE)
                    }
                },
                update = { controller ->
                    vuiController = controller
                    onControllerChanged(controller)
                    // Configure the controller
                    controller.setLanguage("en-US")
                    controller.setTopic(VuiController.NUSA_TOPIC_GENERAL_MEDICINE)

                    // Synchronize Compose field adapters with the controller (only once)
                    if (synchronizedController !== controller) {
                        synchronizedController = controller
                        controller.post {
                            controller.synchronize(controls)
                        }
                    }

                    // Route focus to the appropriate field
                    when (activeControlId) {
                        firstControlId -> controller.controlGotFocus(firstControl)
                        secondControlId -> controller.controlGotFocus(secondControl)
                    }
                },
            )

            // Synchronize again on lifecycle resume to re-establish session
            DisposableEffect(vuiController, lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            vuiController?.post {
                                synchronizedController = vuiController
                                vuiController?.synchronize(controls)
                                when (activeControlId) {
                                    firstControlId -> vuiController?.controlGotFocus(firstControl)
                                    secondControlId -> vuiController?.controlGotFocus(secondControl)
                                }
                            }
                        }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        }
    }
}
