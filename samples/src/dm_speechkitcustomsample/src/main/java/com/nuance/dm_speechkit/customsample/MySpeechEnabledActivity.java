//
//  MySpeechEnabledActivity.java
//  DM_SpeechKitCustomSample
//
//  Copyright 2011-2015 Nuance Communications, Inc. All rights reserved.
//
package com.nuance.dm_speechkit.customsample;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

// Import the Dragon Medical SpeechKit API
import com.nuance.speechanywhere.CommandPlaceholder;
import com.nuance.speechanywhere.CommandSet;
import com.nuance.speechanywhere.Session;
import com.nuance.speechanywhere.SessionEventListener;
import com.nuance.speechanywhere.VuiControllerEventListener;
import com.nuance.speechanywhere.custom.VuiController;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
	TextView inputfieldNative1;
	TextView inputfieldNative2;
	MyCustomTextControl inputfieldCustom1;
	MyCustomTextControl inputfieldCustom2;
	CheckBox native1EnabledCheckbox;
	CheckBox native2EnabledCheckbox;
	CheckBox custom1EnabledCheckbox;
	CheckBox custom2EnabledCheckbox;
	static CommandSet testCommandSet;
	static CommandPlaceholder testCommandPlaceholder;

	static
	{
		// Create application commands
		testCommandSet = new CommandSet("AnimalCommandSet", "Commands for showing various animals");

		// Create a command in the previously defined command set
		testCommandSet.createCommand("showanimal", "show me <animal>", "show me <animal>", "Show a given animal");

		// Create a placeholder for capturing different values dictated by the user when invoking the command
		testCommandPlaceholder = new CommandPlaceholder("animal", "Animal command placeholder");

		// Define two values for the placeholder: "dogs" and "cats". The user can dictate "Show me dogs" or "Show me cats".
		testCommandPlaceholder.setValues(new String[]{"dogs", "cats"}, new String[]{"dogs", "cats"});
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Handle edge-to-edge display.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			);
		}

		setContentView(R.layout.myspeechenabledactivity);

		// Handle window insets for edge-to-edge display
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			View rootView = findViewById(android.R.id.content);
			if (rootView != null) {
				ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
					androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
					androidx.core.graphics.Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
					v.setPadding(
						systemBars.left,
						systemBars.top,
						systemBars.right,
						Math.max(systemBars.bottom, ime.bottom)
					);
					return WindowInsetsCompat.CONSUMED;
				});
			}
		}

		// Obtain a reference to the VuiController object and store it to a field to be able to set additional settings like language, topics and conceptnames.
		theVuiController = (VuiController) findViewById(R.id.vuicontroller);
		// Obtain a reference to the toggle recording button
		btn = (Button) findViewById(R.id.record);
		// Obtain a reference to the first one line editbox in the current activity.
		inputfieldNative1 = (TextView) findViewById(R.id.oneline);
		// Obtain a reference to the multiline editbox in the current activity.
		inputfieldNative2 = (TextView) findViewById(R.id.multiline);

		// Obtain a reference to the first one line editbox with custom support in the current activity.
		inputfieldCustom1 = new MyCustomTextControl((EditText) findViewById(R.id.oneline_custom), theVuiController);
		// Obtain a reference to the multiline editbox with custom support in the current activity.
		inputfieldCustom2 = new MyCustomTextControl((EditText) findViewById(R.id.multiline_custom), theVuiController);

		// Obtain a reference to the checkbox enabling single-line native editbox.
		native1EnabledCheckbox = (CheckBox) findViewById(R.id.oneline_checkbox);

		// Obtain a reference to the checkbox enabling multi-line native editbox.
		native2EnabledCheckbox = (CheckBox) findViewById(R.id.multiline_checkbox);

		// Obtain a reference to the checkbox enabling single-line custom editbox.
		custom1EnabledCheckbox = (CheckBox) findViewById(R.id.oneline_custom_checkbox);

		// Obtain a reference to the checkbox enabling multi-line custom editbox.
		custom2EnabledCheckbox = (CheckBox) findViewById(R.id.multiline_custom_checkbox);

		// Restore recording state destruction-recreation (for example, on device rotation)
		if ((icicle != null)) {
			if (icicle.containsKey("recording")) {
				recording_flag = icicle.getBoolean("recording");
				if (recording_flag) btn.setText("Stop");
				else btn.setText("Record");
			}
			if (icicle.containsKey("custom1")) {
				custom1EnabledCheckbox.setChecked(icicle.getBoolean("custom1"));
			}
			if (icicle.containsKey("custom2")) {
				custom2EnabledCheckbox.setChecked(icicle.getBoolean("custom2"));
			}
			if (icicle.containsKey("native1")) {
				native1EnabledCheckbox.setChecked(icicle.getBoolean("native1"));
			}
			if (icicle.containsKey("native2")) {
				native2EnabledCheckbox.setChecked(icicle.getBoolean("native2"));
			}
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

		// Assign the command set to the VuiController
		theVuiController.assignCommandSets(new CommandSet[]{testCommandSet});

		// Assign the placeholder to the VuiController
		theVuiController.assignCommandPlaceholders(new CommandPlaceholder[]{testCommandPlaceholder});

		// Initialize speech enabled controls
		synchronizeSpeechEnabledControls();
		// The callback method onCommandRecognized() will be invoked whenever the user dictates an application command
	}

	// Save recording state across destruction-recreation (for example, on device rotation)
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("recording", recording_flag);

		outState.putBoolean("custom1", custom1EnabledCheckbox.isChecked());
		outState.putBoolean("custom2", custom2EnabledCheckbox.isChecked());
		outState.putBoolean("native1", native1EnabledCheckbox.isChecked());
		outState.putBoolean("native2", native2EnabledCheckbox.isChecked());
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

	// synchronizing list of speech enabled controls
	private void synchronizeSpeechEnabledControls() {
		theVuiController.synchronize(obtainEnabledControls());
	}

	// returns list of enabled control depending on appropriate checkboxes values
	private List<Object> obtainEnabledControls() {
		List<Object> speechEnabledControlsList = new ArrayList<Object>();

		if (custom1EnabledCheckbox.isChecked()) {
			speechEnabledControlsList.add(inputfieldCustom1);
		}
		if (custom2EnabledCheckbox.isChecked()) {
			speechEnabledControlsList.add(inputfieldCustom2);
		}
		if (native1EnabledCheckbox.isChecked()) {
			speechEnabledControlsList.add(inputfieldNative1);
		}
		if (native2EnabledCheckbox.isChecked()) {
			speechEnabledControlsList.add(inputfieldNative2);
		}
		return speechEnabledControlsList;
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

	// resynchronize list of speech enabled controls
	public void onSynchronize(View v) {
		theVuiController.synchronize(obtainEnabledControls());
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
		System.out.println("onCommandRecognized id = " + id + ", content = " + content + ", spokenPhrase = " + spokenPhrase);
		for (String key : placeholderValues.keySet()) {
			System.out.println("   - (" + key + "," + placeholderValues.get(key) + ")");
		}
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
