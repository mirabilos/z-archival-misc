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

//##################################################################
public class InputKeyPad extends ControlPopupForm{
//##################################################################

public CellPanel top = new CellPanel(), keys = new CellPanel(), bottom = new CellPanel();
private Panel title = new Panel();
private mLabel titleLabel = new mLabel("Enter Data");
public mButton reset, clear;
public boolean showClientTextFirst = false;
protected String value = "";

public boolean isPassword;
/**
Set this true to show a prompt at the top of the InputKeyPad. You can set this before
the keypad is first displayed, but it will not have any effect after it is first displayed.
**/
public boolean showPrompt;

public char passwordCharacter = '*';
private PasswordDisplayMaker myPassword;
/**
* This gets the String to represent on screen the data String provided.
* If the mInput is <b>not</b> a password, this will just return the provided
* String itself. Otherwise it returns a String of "hidden" characters equal
* in length to the data String.
* @param s The data String to display.
*/
//===================================================================
public String getDisplayText()
//===================================================================
{
	if (value == null) return "";
	if (!isPassword) return value;
	myPassword = new PasswordDisplayMaker(passwordCharacter);
	return myPassword.getDisplay(value.length(),passwordCharacter);
}

//===================================================================
public mLabel curText = new mLabel("");
//===================================================================

//==================================================================
public InputKeyPad()
//==================================================================
{
	title.addLast(titleLabel);
	addLast(title).setCell(HSTRETCH);
	title.foreGround = Color.White;
	title.backGround = Color.DarkBlue;
	title.modify(ShrinkToNothing,0);
	addLast(top);
	cancel = makeDefaultButton(CANCELB,false);
	ok = makeDefaultButton(OKB,false);
	top.addNext(
		clear = new mButton(ewe.fx.ImageCache.cache.get("ewe/newsmall.bmp",ewe.fx.Color.White))
	).setCell(DONTSTRETCH);
	//if (Gui.isSmartPhone)
	clear.setHotKey(0,'*');
	top.addNext(curText).setCell(HSTRETCH);
	curText.borderWidth = 1;
	curText.backGround = Color.White;
	curText.modify(SpecialBackground,0);
	addLast(keys);
	addLast(bottom).setCell(HSTRETCH);
	keys.equalWidths = bottom.equalWidths = true;
	if (!Gui.isSmartPhone){
		bottom.addNext(placeCancelOnLeft() ? cancel : ok);
		bottom.addNext(reset = new mButton(ewe.fx.ImageCache.cache.getImage("ewe/reloadsmall.png")));
		bottom.addNext(placeCancelOnLeft() ? ok : cancel);
	}
	if (reset != null) reset.setHotKey(0,'#');
	if (clear != null) {
		clear.setHotKey(0,'*');
		if (Gui.isSmartPhone) clear.text = "*";
		clear.textPosition = Right;
	}
}
//==================================================================
public FormFrame getFormFrame(int options)
//==================================================================
{
	FormFrame ff = super.getFormFrame(options);
	ff.contentsOnly = true;
	ff.borderWidth = 1;
	return ff;
}
//-------------------------------------------------------------------
protected void doInit()
//-------------------------------------------------------------------
{
	if (!showClientTextFirst) doClear();
	else doReset();
}
//-------------------------------------------------------------------
protected void doReset()
//-------------------------------------------------------------------
{
	if (client == null) doClear();
	else setValue(client.getText());
}
//-------------------------------------------------------------------
protected void doClear() {setValue("");}
//-------------------------------------------------------------------
//==================================================================
protected void pressed(Control who)
//==================================================================
{
	if (who == reset) doReset();
	if (who == clear) doClear();
}
//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if (ev.type == ev.PRESSED) pressed((Control)ev.target);
	super.onControlEvent(ev);
}
//-------------------------------------------------------------------
protected void append(String what)
//-------------------------------------------------------------------
{
	setValue(value+what);
}
/**
Get the text to transfer back to the client.
By default this returns curText.getText().
**/
//-------------------------------------------------------------------
protected String getTextForClient()
//-------------------------------------------------------------------
{
	return value;
}
//-------------------------------------------------------------------
protected void transferToClient(Control client)
//-------------------------------------------------------------------
{
	if (client != null)
		client.setText(getTextForClient());
}
//==================================================================
public void close(int exitCode)
//==================================================================
{
	super.close(exitCode);
	if (client instanceof mInput){
		((mInput)client).selectAll();
		client.repaintDataNow();
		if (exitCode == IDOK ) ((mInput)client).updateText(true);
	}
}
/**
A call to setValue() will then call this method to validate the entered
text. By default this just returns the value.
**/
//-------------------------------------------------------------------
protected String fixValue(String value, String oldValue)
//-------------------------------------------------------------------
{
	return value;
}
//===================================================================
public boolean canExit(int exitCode)
//===================================================================
{
	if (exitCode == IDOK)
		if (!canExitWithValue(value)) return false;
	return super.canExit(exitCode);
}
/**
If this returns false then the InputKeyPad will not exit with a code of IDOK.
**/
//-------------------------------------------------------------------
protected boolean canExitWithValue(String value)
//-------------------------------------------------------------------
{
	return true;
}
//-------------------------------------------------------------------
protected void setValue(String text)
//-------------------------------------------------------------------
{
	value = fixValue(text,value);
	refresh();
}
/**
Re-display the text. You should probably call setValue() instead.
**/
//-------------------------------------------------------------------
public void refresh()
//-------------------------------------------------------------------
{
	curText.setText(getDisplayText());
}
/**
This is where the size of the input is selected (to match the number of
columns in the client) and where isPassword is set.
**/
//-------------------------------------------------------------------
protected void startingInput(Control c)
//-------------------------------------------------------------------
{
	super.startingInput(c);
	titleLabel.setText(c.prompt == null ? "    " : c.prompt);
	curText.setTextSize(c.columns,-1);
	char ch = c.getPasswordCharacter();
	isPassword = false;
	if (ch != 0){
		isPassword = true;
		passwordCharacter = ch;
	}
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	if (showPrompt) title.modify(0,ShrinkToNothing);
	modifyAll(NoFocus,TakesKeyFocus);
	super.make(reMake);
}
/*
//-------------------------------------------------------------------
protected void exiting(int exitCode){}
//-------------------------------------------------------------------
*/
//##################################################################
}
//##################################################################

