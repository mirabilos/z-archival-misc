/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.ui;
import ewe.fx.*;
import ewe.sys.*;
/**
* This is a base class for mInput and mTextPad.
**/
//##################################################################
public abstract class EditControl extends Control implements Selectable{
//##################################################################
//-------------------------------------------------------------------
public boolean justGotFocus = false;
protected String oldText = null;
public Color pageColor = Color.White;
//
// If this is true this indicates that local input is active (i.e. the cursor is active).
//
protected boolean doingLocalInput;
//
// If this is true this indicates that native input is active.
//
protected boolean doingNativeInput;
/**
This is CASE_NORMAL by default. You can change it to CASE_NUMBERS, CASE_UPPER, CASE_LOWER and CASE_SENTENCE
**/
public int textCase = CASE_NORMAL;

public static final int CASE_NORMAL = 0;
public static final int CASE_UPPER = 1;
public static final int CASE_LOWER = 2;
public static final int CASE_SENTENCE = 3;
public static final int CASE_NUMBERS = 4;

//-------------------------------------------------------------------
/*
public static int COLOR_TEXT;
public static int COLOR_BACKGROUND;
*/
protected Color [] colors = new Color[4];
/**
 * Provides the colors to be used for the text and background colors.
 * The colors are placed in the <b>colors</b> variable. This is an array of Colors of length 4 which is to receive the colors to be
 * used as follows:<br>
 * colors[0] = Text color, colors[1] = Background color, colors[2] = Selected text color, colors[3] = Selected text background color.
 * @param hasFocus True if the control currently has the focus.
 * @param flags This is the result of a call to getFlags(true) - which provides an OR'ing of all the flags
 * of this control and all its parents.
 */
//-------------------------------------------------------------------
protected void getColors(boolean hasFocus,int flags)
//-------------------------------------------------------------------
{
	colors[0] = (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) ? Color.DarkGray : getForeground();
	colors[1] = (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable)) == 0))) ? getBackground() : pageColor;
	if (hasFocus && ((inputFlags & FLAG_PASSIVE) != 0) && !doingLocalInput)
		colors[1] = Color.LightGreen;
	colors[2] = colors[1];
	colors[3] = Color.DarkBlue;//colors[0];
}
//
/**
This value is -1 as default - which tells the VM to pick the best input flags for
the current system. This value is not picked until the control gets focus for the
first time, so you can set this value before that point.
For a SmartPhone system this will usually be FLAG_USE_NATIVE|FLAG_PASSIVE|FLAG_INPUT_ON_FOCUS
**/
public int inputFlags = -1;
//
// don't put any flags at 0x1;
//
/**
This tells the system to use a native input method. This is only available on some systems.
When using this you should also use FLAG_PASSIVE.
**/
public static int FLAG_USE_NATIVE = 0x2;
/**
This tells the system that the EditControl should be "passive", i.e. no input is allowed
until the input is activated by the action key or pen press.
**/
public static int FLAG_PASSIVE = 0x4;
/**
Use this in combination with FLAG_PASSIVE. This tells the Control to automatically
start active input when the EditControl gets the focus.
**/
public static int FLAG_INPUT_ON_FOCUS = 0x80;
/**
Use this in combination with FLAG_PASSIVE. This tells the Control to keep the focus
after input is complete (e.g. if Enter is pressed).
**/
public static int FLAG_KEEP_FOCUS_AFTER_INPUT = 0x100;

/**
You can set this to be a valid InputMethod object.
**/
public InputMethod inputMethod;
/**
Passive mode is normally selected on SmartPhone devices. This method returns
the most appropriate set of flags for a particular EditControl on the current
system.
**/
//===================================================================
public int getBestPassiveFlags()
//===================================================================
{
	int ret = FLAG_PASSIVE|FLAG_INPUT_ON_FOCUS;
	if (useNativeTextInput) ret |= FLAG_USE_NATIVE;
	return ret;
}
//===================================================================
public InputValidator validator;
//===================================================================

//===================================================================
public abstract void selectAll();
//===================================================================

//===================================================================
public boolean hasChanged()
//===================================================================
{
	return !oldText.equals(getText());
}
//===================================================================
public void updateData()
//===================================================================
{
	super.updateData();
	oldText = getText();
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.target == InputPopupForm.popupForm){
		InputPopupForm ip = InputPopupForm.popupForm;
		if (ev.type == FormEvent.CLOSED){
			//System.out.println("OFF: "+ev.target);
			if (ip.exitValue == Form.IDOK){
				String txt = ip.getNewText();
				setText(txt);
			}
		}
		//Gui.takeFocus(getParent(),ByRequest);
	}
}

//===================================================================
public boolean checkSipCoverage()
//===================================================================
{
	Window w = getWindow();
	if (w == null) return false;
	Rect r = w.checkSipCoverage(this);
	if (r == null) {
		/*
		if (this instanceof mTextPad && SIPEvent.handlingSipOn && getFrame().popupController == null){
			new Exception("Not shown!").printStackTrace();
			w.checkSipCoverage(this);
		}
		*/
		return false;
	}else{
		//new Exception("Shown!").printStackTrace();
		return InputPopupForm.popupForm.popup(this,r);
	}
}
//===================================================================
public boolean takeData(Object data,DragContext how)
//===================================================================
{
	data = toTextData(data);
	return super.takeData(data,how);
}
//===================================================================
public boolean acceptsData(Object data,DragContext how)
//===================================================================
{
	if (!canEdit()) return false;
	Object s = toTextData(data);
	if (!(s instanceof String)) return false;
	return ((String)s).indexOf('\n') == -1;
}

//-------------------------------------------------------------------
protected void dataBeingRemoved(Object data,DragContext dc)
//-------------------------------------------------------------------
{
	if (dc == null){
		super.dataBeingRemoved(data,dc);
		clipOwner = null; //Don't need to undo cut.
	}
}

//-------------------------------------------------------------------
protected void dataTransferCancelled(Object data)
//-------------------------------------------------------------------
{
	if (beforeRemoved == null) return;
	setText((String)beforeRemoved);
	super.dataTransferCancelled(data);
}

//===================================================================
public void update()
//===================================================================
{
	repaintNow();
}
/**
If native/passive input is used, then focus is normally moved to the next control
when entry is complete. If this is set true, then that will not happen.
**/
//public boolean dontMoveToNextAfterInput = false;
/**
If native input is used, this will normally start automatically when the
input gets focus. If this is set true, then that will not happen.
**/
//public boolean dontNativeInputOnFocus = false;
//
protected boolean inFocus = false;
protected boolean startNativeOnPaint = false;
//
// This is used to start the native input if possible. If it is not possible
// then local input is used instead.
//
//-------------------------------------------------------------------
protected boolean startNativeInput(boolean selectAll)
//-------------------------------------------------------------------
{
	if (doingNativeInput) return true;
	int flags = getModifiers(true);
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
		startLocalInput(false);
		return true;
	}
	TextInputParameters tip = new TextInputParameters(this,true);
	if (tip.controlRect == null) return false;
	if (selectAll) tip.flags |= tip.FLAG_SELECT_ALL;
	switch(textCase){
		case CASE_NUMBERS: tip.flags |= tip.FLAG_NUMBERS_ONLY; break;
		case CASE_UPPER: tip.flags |= tip.FLAG_UPPER_CASE; break;
		case CASE_LOWER: tip.flags |= tip.FLAG_LOWER_CASE; break;
		case CASE_SENTENCE: tip.flags |= tip.FLAG_SENTENCE_CASE; break;
	}
	setNativeInputFlags(tip);
	try{
		boolean in = getWindow().textInput(tip);
		if (in) doingNativeInput = true;
		return in;
	}catch(NoSuchMethodError e){
		startLocalInput(selectAll);
		return true;
	}
}
//-------------------------------------------------------------------
protected void startLocalInput(boolean selectAll)
//-------------------------------------------------------------------
{
	blinkId = mApp.requestTick(this,500);
	if (doingLocalInput) return;
	doingLocalInput = true;
	boolean passive = (inputFlags & FLAG_PASSIVE) != 0;
	if (selectAll) this.selectAll();
	if (passive) repaintNow();
}

/**
This tells the Control to start active input IF it has the FLAG_PASSIVE value
set. This will start either native or local input depending on the value
of FLAG_USE_NATIVE.
**/
//===================================================================
public boolean startActiveInput(boolean selectAll)
//===================================================================
{
	if ((inputFlags & FLAG_USE_NATIVE) != 0) return startNativeInput(selectAll);
	startLocalInput(selectAll);
	return true;
}
//-------------------------------------------------------------------
protected void setNativeInputFlags(TextInputParameters tip){}
//-------------------------------------------------------------------

protected int blinkId = 0;
/**
This converts the case of the incoming text to the one associated with
the edit control.
**/
//===================================================================
public String fixCase(String text)
//===================================================================
{
	switch(textCase){
		//case CASE_NUMBERS: tip.flags |= tip.FLAG_NUMBERS_ONLY; break;
		case CASE_UPPER: return text.toUpperCase();
		case CASE_LOWER: return text.toLowerCase();
		case CASE_SENTENCE:
			if (text.length() == 0) break;
			if (Character.isUpperCase(text.charAt(0))) break;
			return Character.toUpperCase(text.charAt(0))+text.substring(1);
	}
	return text;
}

//-------------------------------------------------------------------
protected KeyEvent checkInputMethod(KeyEvent receivedKeyEvent)
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("CIM:"+((receivedKeyEvent.modifiers & IKeys.FROM_INPUT_METHOD) != 0));
	if (inputMethod == null || ((receivedKeyEvent.modifiers & IKeys.FROM_INPUT_METHOD) != 0)) return receivedKeyEvent;
	receivedKeyEvent.target = this;
	KeyEvent ret = inputMethod.handleKey(receivedKeyEvent);
	if (ret == null) ret = receivedKeyEvent;
	checkKeepConditionalChar(ret);
	return ret;
}

//-------------------------------------------------------------------
protected abstract boolean paintConditionalChar(Graphics g, boolean highlight);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected boolean keepConditionalChar()
//-------------------------------------------------------------------
{
	if (conditionalCharIndex != -1){
		paintConditionalChar(null,false);
		conditionalCharIndex = -1;
		conditionalChar = 0;
		return true;
	}
	return false;
}
/**
This returns true if the current character should replace the old one,
false if the current character should not replace the old one.
**/
//-------------------------------------------------------------------
private void checkKeepConditionalChar(KeyEvent ev)
//-------------------------------------------------------------------
{
	if ((ev.modifiers & IKeys.FROM_INPUT_METHOD) == 0){
		keepConditionalChar();
	}
}
//-------------------------------------------------------------------
protected void setConditionalChar(char cc, int index)
//-------------------------------------------------------------------
{
	conditionalCharIndex = index;
	conditionalChar = cc;
}
protected int conditionalCharIndex = -1;
protected char conditionalChar = 0;

//##################################################################
}
//##################################################################

