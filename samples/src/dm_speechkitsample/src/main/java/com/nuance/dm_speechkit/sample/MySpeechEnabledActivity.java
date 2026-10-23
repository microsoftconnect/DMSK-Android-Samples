//
//  MySpeechEnabledActivity.java
//  DM_SpeechKitSample
//
//  Copyright 2011-2015 Nuance Communications, Inc. All rights reserved.
//
package com.nuance.dm_speechkit.sample;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.nuance.speechanywhere.CommandPlaceholder;
import com.nuance.speechanywhere.CommandSet;
import com.nuance.speechanywhere.Session;
import com.nuance.speechanywhere.SessionEventListener;
import com.nuance.speechanywhere.VuiController;
import com.nuance.speechanywhere.VuiControllerEventListener;

import java.util.HashMap;

// Import the Dragon Medical SpeechKit API

//
// Receiving notifications about speech recognition related events (optional)
// --------------------------------------------------------------------------
//
// The MySpeechEnabledActivity class implements the SessionEventListener and the VuiControllerEventListener interfaces.
// This enables the Activity to be notified about speech recognition-related events by the Dragon Medical SpeechKit via callback methods.
//
// 1. The SessionEventListener interface contains callback methods about session-level events:
//      - public void onRecordingStarted() is called whenever recording is started in any way 
//      - public void onRecordingStopped() is called whenever recording is stopped in any way, on purpose or on error
//    Any class can implement this interface and register itself via Session.getSharedSession().addSessionEventListener.
//    Any number of listeners may be added, but every registered listener needs to unregister itself before being destroyed via
//      Session.getSharedSession().removeSessionEventListener().
//    This sample illustrates the most typical case, in which the speech enabled Activity class is registered for session-level events. 
//
// 2. The VuiControllerEventListener interface contains callback methods that are related to one given VUI form:
//      - public void onProcessingStarted() is called when the user starts to speak
//      - public void onProcessingFinished() is called when the user has stopped speaking and all recognition results have arrived.
//      - The semantics for onProcessingStarted(View view) and onProcessingFinished(View view) are exactly the same, but on a per-view basis.
//    To be notified about these events, the Activity containing the VuiController needs to implement the VuiControllerEventListener interface.
//    No further registration/unregistration is necessary. Only one VuiControllerEventListener per VuiController is allowed, which must be its containing Activity.
//
// This sample shows how to properly register for all the event callbacks.
// The SessionEventListener callbacks are used for keeping track of recording state in order to implement a toggle record button functionality.
// The VuiControllerEventListener callbacks are only logged.
//
public class MySpeechEnabledActivity extends Activity implements SessionEventListener, VuiControllerEventListener {
	private boolean recording_flag = false; // Keep track of recording state.
	private Button btn;                     // The button which will function as a toggle recording button
	private VuiController theVuiController; // Reference to the VuiController object

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			);
		}
		setContentView(R.layout.myspeechenabledactivity);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			getWindow().setHideOverlayWindows(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
			//Handle window insets for edge-to-edge UI
			View rootView = findViewById(R.id.vuicontroller);
			ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
				Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
				Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

				// Apply padding to avoid system bars
				v.setPadding(systemBars.left, systemBars.top, systemBars.right,
						Math.max(systemBars.bottom, ime.bottom));

				return insets;
			});
		}

		// Obtain a reference to the VuiController object and store it to a field to be able to set additional settings like language, topics and conceptnames.
		theVuiController = (VuiController) findViewById(R.id.vuicontroller);
		// Obtain a reference to the toggle recording button
		btn = (Button) findViewById(R.id.record);
		// Obtain a reference to the first one line editbox in the current activity.
		TextView inputfield1 = (TextView) findViewById(R.id.oneline);
		// Obtain a reference to the multiline editbox in the current activity.
		TextView inputfield2 = (TextView) findViewById(R.id.multiline);

		// Restore recording state destruction-recreation (for example, on device rotation)
		if ((icicle != null) && (icicle.containsKey("recording"))) {
			recording_flag = icicle.getBoolean("recording");
			if (recording_flag) btn.setText("Stop");
			else btn.setText("Record");
		}

		// **** SETTING UP A SPECIFIC TOPIC
		// Uncomment the following line to specify a specific topic. Please look at the help to see which topics are currently supported.
		// theVuiController.setTopic("GeneralMedicine");

		// **** SETTING UP A SPECIFIC LANGUAGE
		// Uncomment the following line to specify a specific recognition language. Please look at the help to see which languages are currently supported.
		// theVuiController.setLanguage("en-US");

		// **** DISABLE SPEECH RECOGNITION FOR A SPECIFIC FIELD
		// Uncomment the following line to exclude inputfield2 from speech enabling.
		// theVuiController.enableView(inputfield2, false);

		// **** SETTING UP CONCEPTNAMES
		// Uncomment the following two lines to set a concept name for the two editboxes in the activity. Enter a meaningful concept name for the second parameter.
		//theVuiController.setConceptName(inputfield1, "field one");
		//theVuiController.setConceptName(inputfield2, "field two");

		// Create application commands

		// Create the command set which will contain the application commands
		CommandSet testCommandSet = new CommandSet("AnimalCommandSet", "Commands for showing various animals");

		// Create a command in the previously defined command set
		testCommandSet.createCommand("showanimal", "show me <animal>", "show me <animal>", "Show a given animal");

		// Assign the command set to the VuiController
		theVuiController.assignCommandSets(new CommandSet[]{testCommandSet});

		// Create a placeholder for capturing different values dictated by the user when invoking the command
		CommandPlaceholder testCommandPlaceholder = new CommandPlaceholder("animal", "Animal command placeholder");

		// Define two values for the placeholder: "dogs" and "cats". The user can dictate "Show me dogs" or "Show me cats".
		testCommandPlaceholder.setValues(new String[]{"dogs", "cats"}, new String[]{"dogs", "cats"});

		// Assign the placeholder to the VuiController
		theVuiController.assignCommandPlaceholders(new CommandPlaceholder[]{testCommandPlaceholder});

		// The callback method onCommandRecognized() will be invoked whenever the user dictates an application command
	}

	// Save recording state across destruction-recreation (for example, on device rotation)
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("recording", recording_flag);
	}

	// Activity lifecycle callbacks to register and unregister for session event callbacks properly
	@Override
	protected void onStart() {
		// When the Activity starts (it is already created), register for session event callbacks
		Session.getSharedSession().addSessionEventListener(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// When the Activity stops, unregister for session event callbacks
		Session.getSharedSession().removeSessionEventListener(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// When the Activity is about to be destroyed, close session.
		// From the application logic we know that speech recognition is not needed beyond this point.
		if (isFinishing()) Session.getSharedSession().close();
		super.onDestroy();
	}

	// Method for handling the toggle button press
	public void toggleRecording(View v) {
		// Implement a simple toggle recording method. Depending on the state of recording_flag the SDK function
		// startRecording() or stopRecording() is called.
		if (!recording_flag) {
			Session.getSharedSession().startRecording();
		} else {
			Session.getSharedSession().stopRecording();
		}
	}

	// VuiControllerEventListener callback methods
	@Override
	public void onProcessingStarted() {
	}

	@Override
	public void onProcessingFinished() {
	}

	@Override
	public void onProcessingStarted(View view) {
	}

	@Override
	public void onProcessingFinished(View view) {
	}

	// The user has dictated an application command
	public void onCommandRecognized(String id, String spokenPhrase, String content, HashMap<String, String> placeholderValues) {

	}

	// SessionEventListener callback methods
	@Override
	public void onRecordingStarted() {
		recording_flag = true; // Keep track of recordoing state
		btn.setText("Stop");   // The button text is changed to indicate the current state.
	}

	@Override
	public void onRecordingStopped() {
		recording_flag = false; // Keep track of recordoing state
		btn.setText("Record");  // The button text is changed to indicate the current state.
	}
}
