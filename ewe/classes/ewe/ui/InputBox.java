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
import ewe.util.*;

/**
* This class can be used to accept a single line of input from the user.
* It will also be extended to be easily modified to accept more types of data.
**/
//##################################################################
public class InputBox extends Editor{
//##################################################################
{
	windowFlagsToSet &= ~Window.FLAG_MAXIMIZE_ON_PDA;
}

//===================================================================
public InputBox()
//===================================================================
{
	Gui.setOKCancel(this);
}

//===================================================================
public InputBox(String title)
//===================================================================
{
	this();
	this.title = title;
}


Control singleInput;
//===================================================================
public String input(String initialValue,int width)
//===================================================================
{
	return input(null,initialValue,width);
}
//===================================================================
public String input(Frame parent,String initialValue,int width)
//===================================================================
{
	singleInput = new mInput(initialValue);
	singleInput.columns = width;
	((mInput)singleInput).wantReturn = true;
	addLast(singleInput).setCell(HSTRETCH);
	if (execute(parent,Gui.CENTER_FRAME) == IDCANCEL) return null;
	return singleInput.getText();
}

//-------------------------------------------------------------------
protected boolean isAnInput(Object eventTarget)
//-------------------------------------------------------------------
{
	if (singleInput != null && eventTarget == singleInput) return true;
	return false;
}

//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (isAnInput(ev.target)){
		if (ev.type == ev.PRESSED)
			exit(IDOK);
		else if (ev.type == ev.CANCELLED)
			exit(IDCANCEL);
	}
	else super.onControlEvent(ev);
}


/**
 * Use this if you wish from the isValid() method to display a message if the input is invalid.
 * @param title the title of the message box.
 * @param message the text of the message.
 */
//-------------------------------------------------------------------
protected void showErrorMessage(String title, String message)
//-------------------------------------------------------------------
{
	new MessageBox(title,message,MessageBox.MBOK).execute();
}
//-------------------------------------------------------------------
protected boolean canExit(int exitCode)
//-------------------------------------------------------------------
{
	if (exitCode == IDOK)
		if (isValid(singleInput.getText())) return true;
		else{
			Gui.takeFocus(singleInput,ByRequest);
			return false;
		}
	return super.canExit(exitCode);
}
/**
 * Override this to validate the entered data before exit. If it returns false
 * the input box will not close.
 * @param enteredValue the value that was entered.
 * @return true if the data is valid, false if not.
 */
//-------------------------------------------------------------------
protected boolean isValid(String enteredValue)
//-------------------------------------------------------------------
{
	return true;
}
/**
 * Use this to get the entered text.
 * @return the entered text.
 */
//-------------------------------------------------------------------
protected String getInputValue()
//-------------------------------------------------------------------
{
	return singleInput.getText();
}
/**
 * Use this to set the entered text.
 * @return the entered text.
 */
//-------------------------------------------------------------------
protected void setInputValue(String text)
//-------------------------------------------------------------------
{
	singleInput.setText(text);
}
//##################################################################
}
//##################################################################

