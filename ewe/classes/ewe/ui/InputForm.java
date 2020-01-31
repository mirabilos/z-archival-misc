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

//##################################################################
public class InputForm extends ControlPopupForm{
//##################################################################

public static InputForm inputForm = new InputForm();

//==================================================================
public static void attach(mInput who) {inputForm.attachTo(who);}
//==================================================================
public CellPanel editButtons = new CellPanel();
public mInput input = new mInput();
protected String oldText;

//==================================================================
public InputForm()
//==================================================================
{
	resizable = true;
	addLast(input).setCell(HSTRETCH);
	addLast(editButtons);
	titleCancel = getButton("X");
	titleOK = getButton("OK");
	all.add(input);
}
//==================================================================
public void make(boolean remake)
//==================================================================
{
	if (made) return;
	super.make(remake);
	modify(AlwaysRecalculateSizes,0);
	input.modify(AlwaysRecalculateSizes,0);
}
//==================================================================
public void setFor(Control client)
//==================================================================
{
	mInput forWho = (mInput)client;
	input.validator = forWho.validator;
	input.columns = forWho.columns;
	input.isPassword = forWho.isPassword;
	oldText = input.text = forWho.text;
	title = forWho.getPrompt();
	if (formFrame != null) formFrame.title.text = title;
	popup();
	formFrame.modify(AlwaysRecalculateSizes,0);
	formFrame.contents.modify(AlwaysRecalculateSizes,0);
	input.selectAll();
	debugFlag = true;
	Gui.takeFocus(input,ByRequest);
}
//==================================================================
public void close(int exitCode)
//==================================================================
{
	super.close(exitCode);
	if (exitCode != IDCANCEL) {
		client.text = input.text;
		((mInput)client).selectAll();
		if (!input.text.equals(oldText)){
			((mInput)client).updateText(true);
		}
	}
	client.repaintDataNow();
}

//##################################################################
}
//##################################################################


