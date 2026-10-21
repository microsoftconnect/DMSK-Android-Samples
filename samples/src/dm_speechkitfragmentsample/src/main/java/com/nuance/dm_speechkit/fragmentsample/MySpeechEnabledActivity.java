//
//  MySpeechEnabledActivity.java
//  DM_SpeechKitFragmentSample
//
//  Copyright 2012 Nuance Communications, Inc. All rights reserved.
//
package com.nuance.dm_speechkit.fragmentsample;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

// Import the Dragon Medical SpeechKit API
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.nuance.speechanywhere.Session;
import com.nuance.speechanywhere.SessionEventListener;
import com.nuance.speechanywhere.VuiController;
import com.nuance.speechanywhere.VuiControllerEventListener;

import java.util.HashMap;

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


// This speech enabled Activity illustrates the use of Dragon Medical SpeechKit with a UI composed of Fragments.
// The Activity is composed of two custom Fragments, of classes FirstFragment and SecondFragment.
// The VuiController is in the root of the View hierarchy, and all fragments are added below it.
// It speech-enables all supported controls which are underneath it in the View hierarchy, regardless of whether they
//  are contained by a Fragment or not. 
// Setting concept names, enabling fields and dynamically modifying the UI involves calling methods on the VuiController instance.
// If such functionality is to be encapsulated into a Fragment, a reference to the VuiContoller must be passed to it.
// In this sample, the setVuiController() method of the custom fragments serves this purpose. 
// See implementation in FirstFragment.java and SecondFragment.java.
//
public class MySpeechEnabledActivity extends FragmentActivity implements SessionEventListener, VuiControllerEventListener {
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
		
		// Pass the VuiController instance to the fragments
		FirstFragment firstFragment = (FirstFragment) getSupportFragmentManager().findFragmentById(R.id.firstfragment);
		if (firstFragment != null) {
			firstFragment.setVuiController(theVuiController);
			firstFragment.getViewLifecycleOwnerLiveData().observe(this, viewLifecycleOwner -> {
				if (viewLifecycleOwner != null) {
					// Get the record button from the first fragment
					btn = firstFragment.getView() != null ? firstFragment.getView().findViewById(R.id.record) : null;
					btn = firstFragment.getView().findViewById(R.id.record);
					// Restore the button state if needed
					if (btn != null) {
						if (recording_flag) btn.setText("Stop");
						else btn.setText("Record");
					}
				}
			});
		}
		SecondFragment secondFragment = (SecondFragment) getSupportFragmentManager().findFragmentById(R.id.secondfragment);
		if (secondFragment != null) {
			secondFragment.setVuiController(theVuiController);
		}

		// Restore recording state destruction-recreation (for example, on device rotation)
		if ((icicle != null) && (icicle.containsKey("recording"))) {
			recording_flag = icicle.getBoolean("recording");
			if (btn != null) {
				if (recording_flag) btn.setText("Stop");
				else btn.setText("Record");
			}
		}
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

	public void onCommandRecognized(String id, String spokenPhrase, String content, HashMap<String, String> placeholderValues) {

	}

	// SessionEventListener callback methods
	@Override
	public void onRecordingStarted() {
		recording_flag = true; // Keep track of recording state
		if (btn != null) {
			btn.setText("Stop");   // The button text is changed to indicate the current state.
		}
	}

	@Override
	public void onRecordingStopped() {
		recording_flag = false; // Keep track of recording state
		if (btn != null) {
			btn.setText("Record");  // The button text is changed to indicate the current state.
		}
	}

}
