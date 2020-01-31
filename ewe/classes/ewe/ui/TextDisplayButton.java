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
/**
This is a Button that is used to display text as if it were an Input. However you
would not enter text into it - rather pressing on it would prompt some other kind
of text input.<p>
It is used by controls like DateDisplayInput.
*/
//##################################################################
public class TextDisplayButton extends mButton{
//##################################################################

{
	insideColor = Color.White;
	borderStyle = mInput.inputEdge|BF_EXACT;//EDGE_SUNKEN|BF_EXACT;
	flatInside = true;
	anchor = NORTHWEST;
	imageAnchor = NORTHWEST;
	//setMenu(getClipboardMenu(null));
	holdDownPause = 250;
	actionOnPress = true;
}
/**
Use this to set the border style instead of the normal setBorder().
@param style the border style.
*/
//===================================================================
public void setBorderStyle(int style)
//===================================================================
{
	borderStyle = style|BF_EXACT;
}
//===================================================================
public TextDisplayButton()
//===================================================================
{

}
//===================================================================
public TextDisplayButton(String text)
//===================================================================
{
	this.text = text;
}
public boolean isPassword;
public char passwordCharacter = '*';
private PasswordDisplayMaker myPassword;
/**
If isPassword is false, this returns 0, otherwise it will return the password character
to use for display.
**/
//===================================================================
public char getPasswordCharacter()
//===================================================================
{
	return isPassword ? passwordCharacter : 0;
}

/**
* This gets the String to represent on screen the data String provided.
* If the Control is <b>not</b> a password, this will just return the provided
* String itself. Otherwise it returns a String of "hidden" characters equal
* in length to the data String.
*/
//===================================================================
public String getDisplayText()
//===================================================================
{
	String value = getText();
	if (value == null) return "";
	if (!isPassword) return value;
	myPassword = new PasswordDisplayMaker(passwordCharacter);
	return myPassword.getDisplay(value.length(),passwordCharacter);
}
//##################################################################
}
//##################################################################

