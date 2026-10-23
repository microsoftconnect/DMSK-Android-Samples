//
//  SpeechKitSample.java
//  DM_SpeechKitSample
//
//  Copyright 2011-2015 Nuance Communications, Inc. All rights reserved.
//

package com.nuance.dm_speechkit.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Build;
import android.view.WindowManager;

// Import the Session class from the SpeechKit API (for opening the session)
import com.nuance.speechanywhere.Session;

//
// This is the login Activity of the application.
//  - It is not speech-enabled.
//  - It contains an EditText where the user can type their username.
//  - After the username is given, it opens the Dragon Medical SpeechKit session and starts a speech-enabled activity.
// The username used for opening the session does not need to be SpeechKit-specific, it can be used in your application elsewhere.
// The same applies for the login Activity. If your application already has one, just add the open(...) call at the proper place.
//
public class SpeechKitSample extends Activity implements View.OnKeyListener {
	// Convenience defines used for opening the Session instance and pass it the licensing
	// information needed. Your partner GUID and organization token will be made available to you via the
	// Nuance order desk.
	private static final String applicationName = "Android Sample";

	// THIS IS CUSTOMER SPECIFIC - MUST *NOT* BE HARD-CODED!
	// THIS IS EQUIVALENT TO A LICENSE KEY - MUST BE KEPT SECRET FROM UNAUTHORIZED USERS!
	// Make it configurable via user settings or download it from your server if you have a client-server app.
	private String organizationToken = "ENTER_YOUR_ORGANIZATION_TOKEN";

	// It is ok to hard-code the partner GUID - should be "hidden" within your application,
	// usually will not need to be changed.
	private String partnerToken = "ENTER_YOUR_PARTNER_GUID";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speechkit_sample);

		// Handle window insets for edge-to-edge display
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			);
			View mainContent = findViewById(R.id.guideline_center);
			if (mainContent != null) {
				ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, insets) -> {
					androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
					androidx.core.graphics.Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
					
					// Apply system bar insets as padding
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

		// Obtain a reference to the EditText in which the user can type their username.
		EditText editBox = (EditText) findViewById(R.id.login);
		// Set an OnKeyListener to be notified about the user pressing the "enter" button.
		editBox.setOnKeyListener(this);
	}

	// If the user presses the "enter" button, the Dragon Medical SpeechKit session is opened with the given username
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// If the event is a key-down event on the "enter" button
		if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
				(keyCode == KeyEvent.KEYCODE_ENTER)) {

			// Get entered text from editbox.
			EditText editBox = (EditText) findViewById(R.id.login);
			// We store the text in userid
			String userId = editBox.getText().toString();

			// Open the session by supplying the given user name, the proper credentials and the application's name
			Session.getSharedSession().open(userId, organizationToken, partnerToken, applicationName);

			// Show the speech-enabled activity MySpeechEnabledActivity
			Intent i = new Intent(this, MySpeechEnabledActivity.class);
			this.startActivityForResult(i, 0);
			return true;
		}
		return false;
	}

}
