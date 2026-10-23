//
//  MySpeechEnabledActivity.java
//  DM_SpeechKitDocumentSample
//
//  Copyright 2011-2015 Nuance Communications, Inc. All rights reserved.
//
package com.nuance.dm_speechkit.documentsample;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

// Import the Dragon Medical SpeechKit API
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nuance.speechanywhere.Session;
import com.nuance.speechanywhere.SessionEventListener;
import com.nuance.speechanywhere.VuiController;
import com.nuance.speechanywhere.VuiControllerEventListener;

import java.util.HashMap;

public class MySpeechEnabledActivity extends Activity implements SessionEventListener, VuiControllerEventListener {
	private boolean recording_flag = false; // Keep track of recording state.
	private Button btn;                     // The button which will function as a toggle recording button
	private VuiController theVuiController; // Reference to the VuiController object

	private String field1StringA;
	private String field2StringA;
	private String field1StringB;
	private String field2StringB;

	private Boolean docAOpen;
	private Boolean docBOpen;

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
		// Obtain a reference to the toggle recording button
		btn = (Button) findViewById(R.id.record);
		// Obtain a reference to the first one line editbox in the current activity.
		TextView inputfield1 = (TextView) findViewById(R.id.oneline);
		// Obtain a reference to the multiline editbox in the current activity.
		TextView inputfield2 = (TextView) findViewById(R.id.multiline);

		// Restore recording state and strings destruction-recreation (for example, on device rotation)
		if (icicle != null) {
			if (icicle.containsKey("recording")) {
				recording_flag = icicle.getBoolean("recording");
				if (recording_flag) btn.setText("Stop");
				else btn.setText("Record");
			}

			// Restore the contents of the documents fields.
			field1StringA = icicle.getString("field1StringA");
			field2StringA = icicle.getString("field2StringA");
			field1StringB = icicle.getString("field1StringB");
			field2StringB = icicle.getString("field2StringB");

			// Restore the opened state of the documents.
			docAOpen = icicle.getBoolean("docAOpen");
			docBOpen = icicle.getBoolean("docBOpen");

			updateDocumentName();
		} else {
			// Initialize members with default values for a new instance.
			field1StringA = "";
			field2StringA = "";
			field1StringB = "";
			field2StringB = "";

			docAOpen = false;
			docBOpen = false;

			// Opens by default document A.
			openDocumentA();
			// Display the current document name.
			updateDocumentName();
		}
	}

	// Save recording state and strings across destruction-recreation (for example, on device rotation)
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("recording", recording_flag);

		// Saving the strings from the documents.
		outState.putString("field1StringA", field1StringA);
		outState.putString("field2StringA", field2StringA);
		outState.putString("field1StringB", field1StringB);
		outState.putString("field2StringB", field2StringB);

		// Saving the state (open/close) of the documents.
		outState.putBoolean("docAOpen", docAOpen);
		outState.putBoolean("docBOpen", docBOpen);
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
		// No application commands defined in this sample
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

	public void openDocumentA() {
		// Obtain a reference to the first one line editbox in the current activity.
		TextView inputfield1 = (TextView) findViewById(R.id.oneline);
		// Obtain a reference to the multiline editbox in the current activity.
		TextView inputfield2 = (TextView) findViewById(R.id.multiline);

		// Load the document into the speech-enabled fields in the UI
		inputfield1.setText(field1StringA);
		inputfield2.setText(field2StringA);

		// Set the mapping between the speech-enabled text fields and the fields of the underlying document
		theVuiController.setDocumentFieldId(inputfield1, "documentField1"); // This field identifier exists in both documents
		theVuiController.setDocumentFieldId(inputfield2, "field2InDocumentA"); // This field identifier is unique to document A

		// Open the document on the VuiController - from this point on speech recognition is usable
		theVuiController.open("DocumentA");

		// To keep track if the document is opened/closed.
		docAOpen = true;
	}

	public void closeDocumentA() {
		// Set the appropriate metadata key-value pairs
		theVuiController.setDocumentMetadata("keyA", "valueA");

		// Close the document on the VuiController. The user's corrections are saved by Dragon Medical SpeechKit. From this point on up to the next open() call, the VuiController is not associated with any document and hence speech recognition doesn't work.
		// The state is DOCUMENT_STATE_UNDEFINED, because the workflow of the application neither enforces the user to fully correct the text at this point, nor does it inhibit them from doing so.
		// In the first case, if, for example, the user would have the option to sign the document, the correct value would be DOCUMENT_STATE_CORRECTED.
		// In the second case, if the user would never have the chance to make corrections to the recognized text (for example, because it was collected in an invisible control in the background), then the correct value would be DOCUMENT_STATE_UNCORRECTED.
		theVuiController.close("DocumentA", VuiController.DOCUMENT_STATE_UNDEFINED);

		// To keep track if the document is opened/closed.
		docAOpen = false;

		TextView inputfield1 = (TextView) findViewById(R.id.oneline);
		TextView inputfield2 = (TextView) findViewById(R.id.multiline);

		// Saving text from the fields of this document
		field1StringA = inputfield1.getText().toString();
		field2StringA = inputfield2.getText().toString();

		// Clear fields
		inputfield1.setText("");
		inputfield2.setText("");
	}

	public void openDocumentB() {
		// Obtain a reference to the first one line editbox in the current activity.
		TextView inputfield1 = (TextView) findViewById(R.id.oneline);
		// Obtain a reference to the multiline editbox in the current activity.
		TextView inputfield2 = (TextView) findViewById(R.id.multiline);

		// Load the document into the speech-enabled fields in the UI
		inputfield1.setText(field1StringB);
		inputfield2.setText(field2StringB);

		// Set the mapping between the speech-enabled text fields and the fields of the underlying document
		theVuiController.setDocumentFieldId(inputfield1, "documentField1"); // This field identifier exists in both documents
		theVuiController.setDocumentFieldId(inputfield2, "field2InDocumentB"); // This field identifier is unique to document B

		// Open the document on the VuiController - from this point on speech recognition is usable
		theVuiController.open("DocumentB");

		// To keep track if the document is opened/closed.
		docBOpen = true;
	}

	public void closeDocumentB() {
		// Set the appropriate metadata key-value pairs
		theVuiController.setDocumentMetadata("keyB", "valueB");

		// Close the document on the VuiController. The user's corrections are saved by Dragon Medical SpeechKit. From this point on up to the next open() call, the VuiController is not associated with any document and hence speech recognition doesn't work.
		// The state is DOCUMENT_STATE_UNDEFINED, because the workflow of the application neither enforces the user to fully correct the text at this point, nor does it inhibit them from doing so.
		// In the first case, if, for example, the user would have the option to sign the document, the correct value would be DOCUMENT_STATE_CORRECTED.
		// In the second case, if the user would never have the chance to make corrections to the recognized text (for example, because it was collected in an invisible control in the background), then the correct value would be DOCUMENT_STATE_UNCORRECTED.
		theVuiController.close("DocumentB", VuiController.DOCUMENT_STATE_UNDEFINED);

		// To keep track if the document is opened/closed.
		docBOpen = false;

		TextView inputfield1 = (TextView) findViewById(R.id.oneline);
		TextView inputfield2 = (TextView) findViewById(R.id.multiline);

		// Saving text from the fields of this document
		field1StringB = inputfield1.getText().toString();
		field2StringB = inputfield2.getText().toString();

		// Clearing fields
		inputfield1.setText("");
		inputfield2.setText("");
	}

	public void updateDocumentName() {
		TextView openDocument = (TextView) findViewById(R.id.openDocument);
		if (docAOpen)
			openDocument.setText("Document A");// The text is changed to indicate the current open document.
		else if (docBOpen)
			openDocument.setText("Document B");// The text is changed to indicate the current open document.
		else
			openDocument.setText("");// The text is changed to indicate that there is no open document.
	}

	// Method for handling the toggle between documents button press
	public void toggleBetweenDocs(View v) {
		// Implement a simple toggle recording method. Depending on which document is open.
		if (docAOpen) {
			closeDocumentA();
			openDocumentB();
		} else {
			closeDocumentB();
			openDocumentA();
		}

		updateDocumentName();
	}

}
