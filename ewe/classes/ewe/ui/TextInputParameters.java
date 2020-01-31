/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.fx.Rect;
import ewe.fx.Font;
import ewe.sys.Vm;
//##################################################################
public class TextInputParameters{
//##################################################################
//
// Do not move any of these values - they are used by the native VM.
//
/**
This can be a combination of any of the FLAG_XXX values.
**/
public int flags = 0;
/**
This is the initial text to be entered - use setInitialText() to set it - this
will convert LF characters to CR/LF characters if necessary.
**/
public String initialText;
/**
This is the text that was entered. It will be null
if the input was cancelled.
**/
public String enteredText = "Changed Text";
/**
If a dialog box must be used by the underlying system, then this prompt will be
displayed in its title.
**/
public String prompt = "Please enter:";
/**
This is the number of rows of text input.
**/
public int textRows = 1;
/**
This is the number of columns of text input.
**/
public int textColumns = 40;
/**
This is the Control which will own the input.
**/
public Control control;
/**
This can be set to be the Rect of the host control in its parent window.
If possible, the system will try to place the input box over this control.
**/
public Rect controlRect;
/**
This will be the font of the control.
**/
public Font controlFont;
/**
If possible, this will be used as the password character. It defaults to '*'
**/
public char passwordCharacter = '*';
/**
This is set if the native input text cancelled the input.
**/
public static final int FLAG_CANCELLED = 0x1;
public static final int FLAG_PASSWORD = 0x2;
public static final int FLAG_UPPER_CASE = 0x4;
public static final int FLAG_SENTENCE_CASE = 0x8;
public static final int FLAG_NUMBERS_ONLY = 0x10;
public static final int FLAG_AUTO_WRAP = 0x20;
public static final int FLAG_MULTILINE = 0x40;
public static final int FLAG_WANT_RETURN = 0x80;
public static final int FLAG_SELECT_ALL = 0x100;
public static final int FLAG_LOWER_CASE = 0x200;

/**
If this is true, then a TEXT_CHANGED event is sent for each key press. This
only applies to native inputs associated with single line mInput controls.
**/
public static final int FLAG_EVENT_ON_EACH_KEY = 0x20;

//===================================================================
public TextInputParameters(Control forWho, boolean matchOnScreen)
//===================================================================
{
	control = forWho;
	setInitialText(forWho.getText());
	prompt = forWho.prompt;
	if (prompt == null) prompt = "Please enter:";
	if (matchOnScreen) controlRect = Gui.getRectInWindow(forWho,null,true);
	controlFont = forWho.getFont();
}

//===================================================================
public void setInitialText(String text)
//===================================================================
{
	if (text.indexOf('\n') == -1) initialText = text;
	else if ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_CR) != 0) initialText = text;
	else{
		char[] source = Vm.getStringChars(text);
		char[] dest = new char[source.length*2];
		int d = 0;
		for (int s = 0; s<source.length; s++){
			if (source[s] == '\n') dest[d++] = '\r';
			dest[d++] = source[s];
		}
		initialText = new String(dest,0,d);
	}
}
//===================================================================
public static String fixEditedText(String text)
//===================================================================
{
	if (text.indexOf('\r') == -1) return text;
	else if ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_CR) != 0) return text;
	else{
		char[] source = Vm.getStringChars(text);
		char[] dest = new char[source.length];
		int d = 0;
		for (int s = 0; s<source.length; s++){
			if (source[s] != '\r') dest[d++] = source[s];
		}
		return new String(dest,0,d);
	}
}
//##################################################################
}
//##################################################################

