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
import ewe.util.Vector;
import ewe.util.Range;
import ewe.util.Intable;
import ewe.sys.Convert;
import ewe.sys.mThread;
/**
An UpDownInput is generally used within groups within a single popup-window. This is because
the Up/Down cursor keys will change the input value instead of moving from control to control. The
Left/Right keys are will be used to go from one control to the next. The Back key is used
to abort the entire input and the Action key is used to accept the entire input.
<p>
You would usually override the changeToNext() method to cycle through the values
but you can either set the <b>textValues</b> Vector to specify a fixed set of text values, or
set the <b>integerValues</b> Range to specify a range of allowed integer values.
*/
//##################################################################
public class UpDownInput extends Control implements Intable{
//##################################################################

private static String[] ac = new String[1];
private static Rect ar = new Rect();

public int anchor = CENTER;
/**
Show the horizontal split line.
**/
public boolean hasSplit = true;
/**
If this is not null it will indicate a range of allowable integer values.
**/
public Range integerValues;
/**
If this is not null it will indicate a set of allowable text values.
**/
public Vector textValues;
/**
If you are using this for integer input, this specifies the number of digits
that will be input/displayed.
**/
public int integerDigits;
/**
If you are using this for integer input, this specifies whether zeros should
be placed in front of integer values - it is false by default.
**/
public boolean zeroFillInteger;
/**
This is true by default - if it is false the data will not wrap around.
**/
public boolean wrapAround = true;
/**
This is true by default and is used with the integerDigits value to allow
the focus to move to the next field when the full number of digits for this
field has been entered.
**/
public boolean autoAdvance = true;
/**
Set this false to disallow numeric input. Numeric input is only allowed when
the integerValues Range is not null.
**/
public boolean allowNumericInput = true;

/**
This is false by default and if this is true then, when using the textValues Vector to list the allowable text
values, this indicates that the list of values should be zero-based indexed, otherwise
it will be 1-based index (ie 1 represents the first item, instead of 0 representing
the first item).
*/
public boolean zeroIndexedText = false;

/**
This is false by default, and if it is set true then a DataChange event will be
sent everytime the value changes instead of only when it loses focus (which is the
default mode of operation).
**/
public boolean dataChangeOnEachPress = false;


public Color focusedColor;

{
	backGround = Color.White;
	borderWidth = 1;
	borderStyle = 0;
	modify(WantHoldDown|TakesKeyFocus,0);
}
/**
* Create an UpDownInput that is a specific number of columns wide.
**/
//===================================================================
public UpDownInput(int columns)
//===================================================================
{
	setTextSize(columns,1);
}

private int flashing = 0; // 1 = upper, 2 = lower;
private Pen blueLine = new Pen(Color.LightGray,Pen.SOLID,1);
private boolean haveFocus;


/**
This returns true if the value represented by the Control has changed
since it got the focus.
*/
//===================================================================
public boolean hasChangedSinceGotFocus()
//===================================================================
{
	return !getText().equals(originalText);
}

private String originalText;
private int keysGot = 0;

//===================================================================
public void gotFocus(int how)
//===================================================================
{
	keysGot = 0;
	originalText = getText();
	haveFocus = true;
	repaintNow();
	super.gotFocus(how);
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	keysGot = 0;
	validateData(VALIDATE_LOSING_FOCUS);
	haveFocus = false;
	repaintNow();
	if (hasChangedSinceGotFocus()) notifyDataChange();
	super.lostFocus(how);
}

public static final int VALIDATE_LOSING_FOCUS = 1;
public static final int VALIDATE_ACTION = 2;
public static final int VALIDATE_DATA_ENTRY = 3;

/**
This is called when the control loses focus or if the action key is pressed.
If the data is invalid you should correct the data.
@param how one of the VALIDATE_XXX values.
@return true if the data entered was valid, false if it was not.
*/
//-------------------------------------------------------------------
protected boolean validateData(int how)
//-------------------------------------------------------------------
{
	if (integerValues != null){
		int v = getInt(), v2 = v;
		if (v > integerValues.last) v = integerValues.last;
		if (v < integerValues.first) v = integerValues.first;
		if (v2 != v){
			setInt(v);
			return false;
		}
	}
	return true;
}
//-------------------------------------------------------------------
private void paintIt(Graphics g, boolean blocked)
//-------------------------------------------------------------------
{
	Color c = getBackground();
	if (haveFocus) {
		c = focusedColor;
		if (c == null) c = Color.LightGreen;
	}
	if (blocked) c = getForeground();
	g.setColor(c);
	g.fillRect(0,0,width,height);
	//
	if (hasSplit){
		g.setPen(blueLine);
		g.drawLine(0,height/2,width,height/2);
	}
	//
	doBorder(g);
	//
	FontMetrics fm = getFontMetrics();
	//
	if (blocked) c = getBackground();
	else c = getForeground();
	g.setColor(c);
	int w = fm.getTextWidth(text);
	g.setFont(fm.getFont());
	synchronized(ar){
		getDim(ar);
		ac[0] = text;
		//g.drawText(text,(width-w)/2,(height-fm.getHeight())/2);
		g.drawText(fm,ac,ar,CENTER,anchor);
	}
}
//===================================================================
public void doPaint(Graphics g, Rect area)
//===================================================================
{
	if (!hasSplit || (changeFlashType == 0))
		paintIt(g,changeFlashType != 0);
	else{
		paintIt(g,false);
		Rect old;
		if (changeFlashType == CHANGE_UP)
			old = g.reduceClip(0,0,width,height/2,null);
		else
			old = g.reduceClip(0,height/2,width,height-(height/2),null);
		paintIt(g,true);
	}
}

public static final int CHANGE_UP = 0x1;
public static final int CHANGE_DOWN = 0x2;
public static final int CHANGE_TYPE_MASK = 0xff;
public static final int CHANGE_OPTIONS_MASK = 0xff00;
public static final int CHANGE_BY_KEYBOARD = 0x100;
public static final int CHANGE_DONT_FLASH = 0x200;

private int changeFlashType = 0;


/**
This is called when the user presses Up or Down but can also be called
programmatically. You would usually not override this, instead override changeToNext().
@param changeType either CHANGE_UP or CHANGE_DOWN possibly ORed with CHANGE_BY_KEYBOARD
if the change was called by a key press.
*/
//===================================================================
public void changeValue(int changeType)
//===================================================================
{
	if ((changeType & (CHANGE_BY_KEYBOARD|CHANGE_DONT_FLASH)) == (CHANGE_BY_KEYBOARD)){
		changeFlashType = changeType & CHANGE_TYPE_MASK;
		repaintNow();
		mThread.nap(25);
		changeFlashType = 0;
		changeToNext(changeType & CHANGE_TYPE_MASK);
		repaintNow();
	}else if (changeToNext(changeType & CHANGE_TYPE_MASK)) repaintNow();
}
/**
Return the integer value in the control - by default it simply converts the text
value to an integer.
*/
//===================================================================
public int getInt()
//===================================================================
{
	if (textValues != null) {
		int i = textValues.find(text);
		if (i != -1 && !zeroIndexedText) i++;
		return i;
	}
	int ret = Convert.toInt(text), r = ret;
	if (integerValues != null){
		if (ret < integerValues.first) ret = integerValues.first;
		if (ret > integerValues.last) ret = integerValues.last;
		if (ret != r) setText(Convert.toString(ret));
	}
	return ret;
}
/**
Return the integer value in the control - by default it simply converts the text
value to an integer.
*/
//===================================================================
public void setInt(int value)
//===================================================================
{
	if (textValues != null){
		if (!zeroIndexedText) value--;
		if (value <= 0 || value >= textValues.size()) value = 0;
		setText(textValues.get(value).toString());
		return;
	}
	text = ""+value;
	if (zeroFillInteger)
		while(text.length() < integerDigits)
			text = "0"+text;
	setText(text);
	//return Convert.toInt(text);
}
/**
This is called to change the value. You should change the text or Image of the input
based on changeType.
@param changeType either CHANGE_UP or CHANGE_DOWN
@return true if a change was made, false if no change was made.
*/
//-------------------------------------------------------------------
protected boolean changeToNext(int changeType)
//-------------------------------------------------------------------
{
	if (textValues != null){
		int where = textValues.find(text);
		if (changeType == CHANGE_UP) where++;
		else where--;
		if (where < 0) where = wrapAround ? textValues.size()-1 : 0;
		if (where >= textValues.size()) where = wrapAround ? 0 : textValues.size()-1;
		text = (where > textValues.size()) ? "" : textValues.get(where).toString();
		return true;
	}else if (integerValues != null){
		int curValue = getInt();
		if (changeType == CHANGE_UP) curValue++;
		else curValue--;
		if (curValue > integerValues.last) curValue = wrapAround ? integerValues.first : integerValues.last;
		if (curValue < integerValues.first) curValue = wrapAround ? integerValues.last : integerValues.first;
		setInt(curValue);
		return true;
	}else
		return false;
}

private int lastValue;

//===================================================================
public void numberKeyPressed(int digitValue)
//===================================================================
{
	if (!allowNumericInput) return;
	int v = lastValue;
	if (keysGot == 0) v = 0;
	v = v*10+digitValue;
	setInt(v);
	lastValue = v;
	//
	keysGot++;
	if (integerDigits != 0){
		if (keysGot >= integerDigits){
			keysGot = 0;
			boolean isValid = validateData(VALIDATE_DATA_ENTRY);
			if (!isValid) repaintNow();
			if (autoAdvance && isValid){
				tryNext(true);
			}
		}
	}
}

//-------------------------------------------------------------------
private void doChange(boolean up, int extra)
//-------------------------------------------------------------------
{
	int op = up ? CHANGE_UP : CHANGE_DOWN;
	op |= extra;
	changeValue(op);
	if (dataChangeOnEachPress) notifyDataChange();
}

//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	if (ev.type == ev.KEY_PRESS){
		if (ev.key == IKeys.UP || ev.key == IKeys.DOWN){
			int op = CHANGE_BY_KEYBOARD;
			if ((ev.modifiers & IKeys.REPEATED) != 0) op |= CHANGE_DONT_FLASH;
			doChange(ev.key == IKeys.UP, op);
		}else if (ev.isBackKey() || ev.isCancelKey()){
			postEvent(new ControlEvent(ControlEvent.CANCELLED,this));
		}else if (ev.isActionKey()){
			validateData(VALIDATE_ACTION);
			notifyAction();
		}else if (ev.key >= '0' && ev.key <= '9'){
			numberKeyPressed(ev.key-'0');
			if (dataChangeOnEachPress) notifyDataChange();
		}else
			super.onKeyEvent(ev);
	}
	else super.onKeyEvent(ev);
}
//===================================================================
public void penPressed(Point where)
//===================================================================
{
	super.penPressed(where);
	boolean isUp = !hasSplit || where.y <= height/2;
	doChange(isUp,0);
}
//===================================================================
public void penHeld(Point where)
//===================================================================
{
	boolean isUp = !hasSplit || where.y <= height/2;
	doChange(isUp,0);
	wantAnotherPenHeld = true;
}
//##################################################################
}
//##################################################################


