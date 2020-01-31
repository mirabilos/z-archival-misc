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
/**
* This class provides an mInput along with a drop-down tool menu, that is meant
* to aid the input in some way. For example it may contain special hard to remember
* codes or it may provide some kind of auxillary input.
**/
//##################################################################
public class mInputWithTools extends Holder{
//##################################################################

public mInput input;
public ArrowButton tool;

//-------------------------------------------------------------------
protected void setup()
//-------------------------------------------------------------------
{
	tool = new ArrowButton(Right){
		public void doPenPress(ewe.fx.Point p){startDropMenu(p);}
		public boolean willShowFrame(PenEvent ev)
		{
			if ((ev.type == ev.PEN_DOWN) && !menuIsActive()) return true;
			return super.willShowFrame(ev);
		}
		public void popupMenuEvent(Object selected)
		{
			if (selected instanceof MenuItem) handleMenu((MenuItem)selected);
		}
	};
	addNext(input);
	addLast(tool).setCell(VSTRETCH);
	tool.modify(NoFocus,TakesKeyFocus);
}
//===================================================================
public void setToolsMenu(Menu m)
//===================================================================
{
	tool.setMenu(m);
}
//===================================================================
public void make(boolean remake)
//===================================================================
{
	if (made && !remake) return;
	int h = input.getPreferredSize(null).height;
	tool.setPreferredSize(15,h);
	tool.setControl(VCONTRACT|HCONTRACT|NORTHWEST);
	super.make(remake);
}

/**
 * Create an mInputWithTools but with no tool menu selected. Call setToolsMenu() to set the tools.
 */
//===================================================================
public mInputWithTools()
//===================================================================
{
	input = new mInput();
	setup();
}
/**
 * Create an mInputWithTools with a specific tools menu.
 */
//===================================================================
public mInputWithTools(Menu m)
//===================================================================
{
	this();
	setToolsMenu(m);
}
/**
 * Create an mInputWithTools with a specific tools menu derived from the toolOptions.
 */
//===================================================================
public mInputWithTools(String [] toolOptions)
//===================================================================
{
	this();
	setToolsMenu(new Menu(toolOptions,""));
}
/**
 * This requests the text to insert into the mInput for the specified MenuItem.
 * It will be called when a tool item is selected via the handleMenu() method.
 * @param forMenuItem The selected item.
 * @return Text to insert into the mInput, or null for no insertion.
 */
//-------------------------------------------------------------------
protected String getInsertionText(MenuItem forMenuItem)
//-------------------------------------------------------------------
{
	return forMenuItem.toString();
}
/**
 * Use this to insert text into the mInput at the current cursor position, overwriting
 * any text that might be selected.
 * @param toInsert The text to insert.
 */
//-------------------------------------------------------------------
protected void insertIntoInput(String toInsert)
//-------------------------------------------------------------------
{
	if (toInsert != null) {
		input.replaceSelection(toInsert);
		input.update();
		input.updateText(true);
	}
}
/**
 * This gets called when a tool item is selected. By default it
 * will call getInsertionText() and then call insertIntoInput(). You can override it however
 * to do anything else.
 * @param forMenuItem The item selected.
 */
//-------------------------------------------------------------------
protected void handleMenu(MenuItem forMenuItem)
//-------------------------------------------------------------------
{
	insertIntoInput(getInsertionText(forMenuItem));
}
//===================================================================
public String getText() {return input.getText();}
//===================================================================
public void setText(String text) {input.setText(text);}
//===================================================================

/*
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	mInputWithTools mi;
	f.addNext(mi = new mInputWithTools(new String[]{"One","Two","-","Three"}));
	f.addLast(new mInputWithTools());
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

