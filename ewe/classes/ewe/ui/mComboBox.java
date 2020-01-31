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
public class mComboBox extends Holder{
//##################################################################

/**
* The input part of the Control.
**/
public mInput input = new mInput();
/**
* The choice of the Control.
**/
public mChoice choice;
public String oldText;
public int addNewItemIndex = -1;
/**
* Set this true to generate a Action event (PRESSED) if a selection is made using
* the drop-down choice. Otherwise only a DataChanged event will be generated.
**/
public boolean actionOnChoiceSelect = false;
{
	borderWidth = 0;
	//defaultAddMeCellConstraints = HSTRETCH;
}
//===================================================================
public mComboBox(mChoice ch)
//===================================================================
{
	choice = ch;
	choice.dropButtonOnly = true;
	choice.container = this;
	//ch.comboBox = this;
	choice.modify(PaintOutsideOnly/*|PreferredSizeOnly*/,TakesKeyFocus);
	//input.wantReturn = true;
	addNext(input);
	addLast(choice).setCell(VSTRETCH);
}
//==================================================================
public mComboBox() {this(new mChoice());}
//==================================================================
//==================================================================
public mComboBox(String [] choices,int value)
//==================================================================
{
	this();
	choice.set(choices,value);
	if (value != -1) input.text = choices[value];
}
//==================================================================
public mComboBox(String [] choices,String initial)
//==================================================================
{
	this();
	choice.set(choices,-1);
	if (initial == null) initial = "";
	input.text = initial;
}
//===================================================================
public void doActionKey(int key)
//===================================================================
{
	choice.doActionKey(key);
}
/*
//==================================================================
public void make()
//==================================================================
{
	if (made) return;
	add(input);
	super.make();
}
//==================================================================
public void setRect(int x,int y,int w,int h)
//==================================================================
{
	super.setRect(x,y,w,h);
	choice.setRect(20,0,w-20,h);
	input.setRect(0,0,w-h+choice.spacing+2,h);
}
*/
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	if (ev.type == ControlEvent.PRESSED && ev.target == choice) {
		int si = choice.selectedIndex;
		if (si == addNewItemIndex && addNewItemIndex != -1){
			if (!input.text.equals("") && choice.indexOf(input.text) == -1){
				choice.addItem(input.text);
				choice.selectedIndex = choice.items.size()-1;
			}
		}else {
			oldText = input.text;
			input.setText(choice.getText());//choice.getItemAt(choice.selectedIndex).label);
			//
			// This will fire a DataChange event if the text has changed.
			//
			input.updateText(true);
			if (actionOnChoiceSelect) notifyAction();
			//Gui.changeReturnedFocus(input);
		}
	/* There is a bug here.
	}else if (ev.type == ControlEvent.PRESSED && ev.target == input){
		input.modify(Invisible,0);
		input.noSelection();
		choice.doDropMenu();
	*/
	}else
		super.onEvent(ev);
}
/**
* Set the text in the input box.
**/
//===================================================================
public void setText(String text)
//===================================================================
{
	input.setText(text);
	choice.selectItem(text);
}
/**
* Get the text in the input box.
**/
public String getText() {return input.getText();}

//===================================================================
public void select(int which)
//===================================================================
{
	if (which >= choice.items.size()) return;
	setText(choice.getItemAt(which).label);
}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	if (borderStyle != 0){
		input.borderStyle = choice.borderStyle = choice.dropButtonBorder = BDR_NOBORDER;
		super.doPaint(g,area);
	}
}
//##################################################################
}
//##################################################################

