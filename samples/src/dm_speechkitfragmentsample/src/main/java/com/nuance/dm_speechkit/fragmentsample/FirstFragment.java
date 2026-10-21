//
//  FirstFragment.java
//  DM_SpeechKitFragmentSample
//
//  Copyright 2012 Nuance Communications, Inc. All rights reserved.
//
package com.nuance.dm_speechkit.fragmentsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import com.nuance.speechanywhere.VuiController;

// This is the code of the first fragment.
// It contains no VuiController, it is only a generic building block of the UI.
// All encapsulated speech recognition related functionality is implemented the setVuiController method.
// The fragment receives a VuiController reference from the Activity, and perform all property changes on that instance.
// Note: if the Fragment is embedded in a non-speech-enabled Activity, the setVuiController method is not called,
//  but the Fragment will be fully functional in all other aspects. 
//
public class FirstFragment extends Fragment {
	private VuiController theVuiController;
	EditText appleField, pearField;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.firstfragment, container, false);
		// Save references of the EditTexts (which might be speech enabled by an enclosing VuiController)
		appleField = (EditText) root.findViewById(R.id.apple);
		pearField = (EditText) root.findViewById(R.id.pear);
		return root;
	}

	// This is a custom method, used for passing the enclosing VuiController instance to the fragment
	// Perform attribute manipulation here, and/or save the instance and use the reference later for VUI form attribute manipulation.
	// Note: since it is a custom (application-specific) method, do not forget to call it at the appropriate time, when the VuiController instance becomes known.
	// (see MySpeechEnabledActivity.java:onCreate())
	public void setVuiController(VuiController theVuiController) {
		this.theVuiController = theVuiController;
		// Encapsulated Fragment-specific functionality: the enclosing Activity doesn't know about local concept names.
		// Rather, we defined this method so that the VuiController instance is accessible from inside the Fragment and encapsulated VUI form related functionality can be performed.
		theVuiController.setConceptName(appleField, "apple");
		theVuiController.setConceptName(pearField, "pear");
	}
}
