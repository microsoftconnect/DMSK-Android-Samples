//
//  ComposeUIReportsActivity.kt
//  DM_SpeechKitComposeUISample
//
//  Copyright 2026-2027 Nuance Communications, Inc. All rights reserved.
//

package com.nuance.dm_speechkit.composeuisample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nuance.dm_speechkit.composeuisample.reports.ComposeUIAndroidViewWrapperActivity
import com.nuance.dm_speechkit.composeuisample.reports.ComposeUIAndroidViewBindingActivity
import com.nuance.dm_speechkit.composeuisample.reports.PureComposeCustomVCReportActivity

//
// This Activity displays a hub of integration report examples.
// - It presents three different Compose UI integration patterns with SpeechKit.
// - Users can select a report to view a specific integration example.
// - Each report demonstrates a different approach to using Compose with SpeechKit's VuiController.
//
/**
 * ComposeUIReportsActivity serves as the hub Activity for viewing different Compose UI integration patterns.
 * 
 * It displays a list of available reports:
 * 1. AndroidViewBinding with XML Layout - integrating XML layouts via AndroidViewBinding
 * 2. AndroidView Wrapper - wrapping classic View-based controls in Compose
 * 3. Pure Compose - using pure Compose controls with custom.VuiController
 * 
 * Each report can be launched to demonstrate its specific integration pattern.
 */
class ComposeUIReportsActivity : ComponentActivity() {

    // List of available report titles that will be displayed to the user
    private val reports = listOf(
        "ComposeUI With AndroidViewBinding and XML Layout",
        "ComposeUI With AndroidView Wrapper",
        "PureComposeUI With CustomVC Report",
    )

    /**
     * onCreate is called when the Activity is first created.
     * It sets up the Compose content with the list of available reports.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ComposeUIReportsScreen(
                    reports = reports,
                    onReportClick = { reportName -> onReportClicked(reportName) },
                    onBack = { finish() },
                )
            }
        }
    }

    /**
     * Handles report selection and launches the corresponding Activity.
     * 
     * @param reportName The name of the selected report to display
     */
    private fun onReportClicked(reportName: String) {
        when (reportName) {
            "ComposeUI With AndroidViewBinding and XML Layout" -> {
                startActivity(Intent(this, ComposeUIAndroidViewBindingActivity::class.java))
            }
            "ComposeUI With AndroidView Wrapper" -> {
                startActivity(Intent(this, ComposeUIAndroidViewWrapperActivity::class.java))
            }
            "PureComposeUI With CustomVC Report" -> {
                startActivity(Intent(this, PureComposeCustomVCReportActivity::class.java))
            }
        }
    }
}

/**
 * ComposeUIReportsScreen is a pure Compose UI that displays the list of available integration reports.
 * 
 * It provides:
 * - A Material3 Scaffold with TopAppBar for navigation
 * - A LazyColumn displaying clickable report items
 * - A back button to return to the previous Activity
 * - Horizontal dividers between report items
 * 
 * @param reports List of report names to display
 * @param onReportClick Callback function invoked when a report is clicked
 * @param onBack Callback function to handle the back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeUIReportsScreen(
    reports: List<String>,
    onReportClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose UI Integrations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
            )
        },
    ) { innerPadding ->
        // Display the list of reports in a scrollable column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(reports) { report ->
                // Each report is a clickable surface item
                Surface(
                    onClick = { onReportClick(report) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ListItem(headlineContent = { Text(report) })
                }
                // Add divider between items
                HorizontalDivider()
            }
        }
    }
}
