//
//  SpeechKitComposeUISample.kt
//  DM_SpeechKitComposeUISample
//
//  Copyright 2026-2027 Nuance Communications, Inc. All rights reserved.
//

package com.nuance.dm_speechkit.composeuisample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nuance.speechanywhere.Session

//
// This is the login Activity of the Compose UI Sample application.
// - It is not speech-enabled.
// - It contains a pure Compose TextField where the user can type their username.
// - After the username is given and the Login button is pressed, it opens the Dragon Medical SpeechKit session
//   and starts a speech-enabled activity.
// The username used for opening the session does not need to be SpeechKit-specific, it can be used in your
// application elsewhere. The same applies for the login Activity. If your application already has one, just add
// the open(...) call at the proper place.
// This sample demonstrates the use of Jetpack Compose for building Compose-native UIs with the Dragon Medical SpeechKit.
//
/**
 * SpeechKitComposeUISample is the main launcher Activity for the Compose UI Sample application.
 * 
 * It provides a login screen built entirely with Jetpack Compose (pure Compose, no XML layouts).
 * Once a user enters their username and clicks Login, the activity opens a SpeechKit session
 * and navigates to the ComposeUIReportsActivity where speech-enabled features are demonstrated.
 */
class SpeechKitComposeUISample : ComponentActivity() {
    // Convenience defines used for opening the Session instance and pass it the licensing
    // information needed. Your partner GUID and organization token will be made available to you via the
    // Nuance order desk.
    private val applicationName = "Android Compose UI Sample"

    // THIS IS CUSTOMER SPECIFIC - MUST *NOT* BE HARD-CODED!
    // THIS IS EQUIVALENT TO A LICENSE KEY - MUST BE KEPT SECRET FROM UNAUTHORIZED USERS!
    // Make it configurable via user settings or download it from your server if you have a client-server app.
    private val organizationToken = "ENTER_YOUR_ORGANIZATION_TOKEN"

    // It is ok to hard-code the partner GUID - should be "hidden" within your application,
    // usually will not need to be changed.
    private val partnerToken = "ENTER_YOUR_PARTNER_GUID"

    /**
     * onCreate is called when the Activity is first created.
     * It sets up the Compose content for the Activity with a pure Compose login screen.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the Compose content. This replaces the traditional setContentView(R.layout.activity_layout) call.
        setContent {
            MaterialTheme {
                LoginScreen(
                    onLogin = { userId ->
                        // Open the session by supplying the given user name, the proper credentials and the application's name
                        Session.getSharedSession().open(
                            userId,
                            organizationToken,
                            partnerToken,
                            applicationName
                        )
                        // Show the speech-enabled Compose activity ComposeUIReportsActivity
                        startActivity(Intent(this@SpeechKitComposeUISample, ComposeUIReportsActivity::class.java))
                    }
                )
            }
        }
    }
}

/**
 * LoginScreen is a pure Compose UI that displays the login form.
 * 
 * It provides:
 * - A Material3 Scaffold with TopAppBar for the title
 * - A centered Column containing the login form
 * - A TextField for user ID input
 * - A Login button that triggers the onLogin callback when clicked
 * - Helpful instruction text
 * 
 * @param onLogin Callback function invoked when the Login button is clicked with a non-empty user ID
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(onLogin: (String) -> Unit) {
    // State to hold the user ID entered in the TextField
    var userId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dragon Medical SpeechKit Compose UI") }
            )
        }
    ) { innerPadding ->
        // Column centers the login form vertically and horizontally on the screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the login heading
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Obtain user ID input via an OutlinedTextField
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("User ID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )

            // Login button - When clicked, the onLogin callback is invoked with the entered user ID
            Button(
                onClick = {
                    if (userId.isNotBlank()) {
                        onLogin(userId)
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.5f)
            ) {
                Text("Login")
            }

            // Display helpful instruction text to guide the user
            Text(
                text = "Enter your user ID and press Login",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
