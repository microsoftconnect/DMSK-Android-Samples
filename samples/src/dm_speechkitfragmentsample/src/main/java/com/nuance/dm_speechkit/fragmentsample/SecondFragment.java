//
//  SecondFragment.java
//  DM_SpeechKitFragmentSample
//
//  Copyright 2012 Nuance Communications, Inc. All rights reserved.
//
package com.nuance.dm_speechkit.fragmentsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import com.nuance.speechanywhere.VuiController;

//This is the code of the first fragment. See comments in FirstFragment.java
public class SecondFragment extends Fragment {
	private VuiController theVuiController;
	EditText fooField, barField;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.secondfragment, container, false);
		fooField = (EditText) root.findViewById(R.id.foo);
		barField = (EditText) root.findViewById(R.id.bar);
		Button disableButton = (Button) root.findViewById(R.id.disable);
		disableButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// On button click we change the VUI form: disable speech recognition in the second EditText.
				// This logic is encapsulated in the Fragment, the enclosing Activity is not aware of it.
				// It is important, however, to check if the VuiController reference is not null,
				//  since the Fragment may be embedded in a non-speech-enabled Activity as well in which case the setVuiController() method is not called. 
				if (theVuiController != null)
					theVuiController.enableView(barField, false);
				theVuiController.synchronize();
			}
		});
		return root;
	}

	public void setVuiController(VuiController theVuiController) {
		this.theVuiController = theVuiController;
		theVuiController.setConceptName(fooField, "foo");
		theVuiController.setConceptName(barField, "bar");
	}
}
