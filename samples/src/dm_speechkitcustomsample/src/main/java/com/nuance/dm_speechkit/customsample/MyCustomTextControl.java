package com.nuance.dm_speechkit.customsample;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.nuance.speechanywhere.custom.ITextControl;
import com.nuance.speechanywhere.custom.VuiController;

/**
 * Created by vbondarenko on 6/8/2015.
 */
public class MyCustomTextControl implements ITextControl, View.OnFocusChangeListener {

	// Using standard EditText for simplicity.
	private EditText wrappedControl;
	private VuiController theVuiController;

	public MyCustomTextControl(EditText wrappedControl, VuiController theVuiController) {
		this.wrappedControl = wrappedControl;
		this.theVuiController = theVuiController;
		wrappedControl.setOnFocusChangeListener(this);
	}

	// returning selection in the wrapped edit control
	@Override
	public Selection getSelection() {
		return new Selection(wrappedControl.getSelectionStart(), wrappedControl.getSelectionEnd() - wrappedControl.getSelectionStart());
	}

	// setting selection in the wrapped edit control
	@Override
	public void setSelection(Selection selection) {
		try {
			if (selection.length > 0) {
				// if supported, open formatting bar after selection is made, just like the user
				// selecting the text with a tap.
				Bundle b = new Bundle();
				b.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, selection.start);
				b.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, selection.start + selection.length);
				wrappedControl.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, b);
			}
			// In any case, just set the selection in wrapped edit control
			wrappedControl.setSelection(selection.start, selection.start + selection.length);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// returning text length from the wrapped edit control
	@Override
	public int getTextLength() {
		return wrappedControl.length();
	}

	// returning text from the wrapped edit control
	@Override
	public String getText(int start, int length) {
		Editable text;
		try {
			text = wrappedControl.getText();
		} catch (Exception ex) {
			return "";
		}

		try {
			if (start <= text.length()) {
				if (length == -1) {
					return text.subSequence(start, text.length()).toString();
				} else {
					if (start + length > text.length()) {
						return text.subSequence(start, text.length()).toString();
					} else {
						return text.subSequence(start, start + length).toString();
					}
				}
			} else {
				return "";
			}
		} catch (Exception ex) {
			return "";
		}
	}

	// setting text in the wrapped edit control
	@Override
	public void replaceText(int start, int length, String newText) {
		Editable text = wrappedControl.getText();
		Selection oldSelection = getSelection();
		if (start <= text.length()) {
			if (start + length > text.length()) {
				length = text.length() - start;
			}

			try {
				text.replace(start, start + length, newText, 0, newText.length());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			text.append(newText);
		}

		// Emulate shifted selection position if text is inserted before current selection
		if (start + length < oldSelection.start) {
			int delta = newText.length() - length;
			oldSelection.start += delta;
			setSelection(new Selection(oldSelection.start, oldSelection.length));
		}
	}

	// setting focus to the wrapped edit control
	@Override
	public void setFocused() {
		wrappedControl.requestFocus();
	}

	// returning whether wrapped edit control has focus
	@Override
	public boolean isFocused() {
		return wrappedControl.isFocused();
	}

	// returning newline format string for the  wrapped edit control
	@Override
	public String getNewlineFormatString() {
		return "\n";
	}

	// returning paragraph format string for the  wrapped edit control
	@Override
	public String getParagraphFormatString() {
		return "\n\n";
	}

	// returning unique id string for the  wrapped edit control
	@Override
	public String getId() {
		return Integer.toString(wrappedControl.getId());
	}

	// Proactively notify the VuiController whenever the wrapped EditText receives focus
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if ((view == wrappedControl) && hasFocus) {
			theVuiController.controlGotFocus(this);
		}
	}
}
