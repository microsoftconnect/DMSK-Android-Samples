//
//  ComposeUIAndroidViewWrapperActivity.kt
//  DM_SpeechKitComposeUISample
//
//  Copyright 2026-2027 Nuance Communications, Inc. All rights reserved.
//

package com.nuance.dm_speechkit.composeuisample.reports

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import com.nuance.dm_speechkit.composeuisample.R
import com.nuance.speechanywhere.VuiController
import com.nuance.speechanywhere.compose.VuiControllerView
import com.nuance.speechanywhere.compose.rememberVuiControllerState

//
// This Activity demonstrates the second Compose UI integration pattern: AndroidView Wrapper.
// - It wraps traditional View-based controls (EditText) within a Compose UI using AndroidView.
// - The EditText controls are wrapped in a VuiControllerView for SpeechKit integration.
// - This pattern is useful for mixing Compose Composables with traditional View controls.
// - The speech-enabled controls are accessed via the VuiControllerView wrapper.
//
/**
 * ComposeUIAndroidViewWrapperActivity demonstrates Compose UI integration with wrapped AndroidView controls.
 * 
 * This pattern shows how to embed traditional Android View-based controls (like EditText) within a
 * Compose Activity using AndroidView and VuiControllerView. This approach is useful for:
 * - Incrementally adopting Compose while keeping existing View-based controls
 * - Wrapping speech-enabled controls (EditText) within VuiControllerView
 * - Maintaining the familiar EditText interface while accessing it from Compose
 */
class ComposeUIAndroidViewWrapperActivity : ComponentActivity() {
    private var vuiController: VuiController? = null

    /**
     * onCreate is called when the Activity is first created.
     * It sets up the Compose content with wrapped AndroidView controls.
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
                ComposeUIAndroidViewWrapperScreen(
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
 * ComposeUIAndroidViewWrapperScreen displays a Compose UI with wrapped AndroidView controls.
 * 
 * It provides:
 * - A Material3 Scaffold with TopAppBar for navigation
 * - A Column layout with Compose Text and AndroidView-wrapped EditText controls
 * - VuiControllerView wrapping traditional EditText controls for speech integration
 * - Language and topic configuration for the SpeechKit controller
 * 
 * @param onBack Callback function to handle the back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeUIAndroidViewWrapperScreen(
    onBack: () -> Unit,
    onControllerChanged: (VuiController?) -> Unit,
) {
    var patientName by rememberSaveable { mutableStateOf("") }
    var diagnosis by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidView Wrapper") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Configure the VuiController state with language and topic
            val state = rememberVuiControllerState(
                language = "en-US",
                topic = VuiController.NUSA_TOPIC_GENERAL_MEDICINE,
            )

            // VuiControllerView wraps traditional EditText controls for speech integration
            VuiControllerView(
                modifier = Modifier.fillMaxSize(),
                language = state.language,
                topic = state.topic,
                onControllerChanged = onControllerChanged,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, bottom = 16.dp),
                ) {
                    // Descriptive text explaining the pattern
                    Text(
                        text = "Compose wrapper around classic EditText views.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth(),
                    )

                    // First AndroidView-wrapped EditText for patient name (speech-enabled)
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        factory = { context ->
                            EditText(context).apply {
                                id = R.id.compose_wrapper_patient_name
                                hint = "Patient Name (Speech-Enabled)"
                                inputType = InputType.TYPE_CLASS_TEXT or
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                                imeOptions = EditorInfo.IME_ACTION_NONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                                gravity = Gravity.TOP or Gravity.START
                                minLines = 3
                                maxLines = 6
                                setTextColor(Color.BLACK)
                                setHintTextColor(Color.BLACK)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                )
                                setPadding(16, 16, 16, 16)
                                doAfterTextChanged { patientName = it?.toString().orEmpty() }
                            }
                        },
                        update = { editText ->
                            if (editText.text.toString() != patientName) {
                                editText.setText(patientName)
                                editText.setSelection(patientName.length)
                            }
                        },
                    )

                    // Second AndroidView-wrapped EditText for diagnosis (speech-enabled)
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        factory = { context ->
                            EditText(context).apply {
                                id = R.id.compose_wrapper_diagnosis
                                hint = "Diagnosis (Speech-Enabled)"
                                inputType = InputType.TYPE_CLASS_TEXT or
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                                imeOptions = EditorInfo.IME_ACTION_NONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                                gravity = Gravity.TOP or Gravity.START
                                minLines = 3
                                maxLines = 6
                                setTextColor(Color.BLACK)
                                setHintTextColor(Color.BLACK)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                )
                                setPadding(16, 16, 16, 16)
                                doAfterTextChanged { diagnosis = it?.toString().orEmpty() }
                            }
                        },
                        update = { editText ->
                            if (editText.text.toString() != diagnosis) {
                                editText.setText(diagnosis)
                                editText.setSelection(diagnosis.length)
                            }
                        },
                    )
                }
            }
        }
    }
}
