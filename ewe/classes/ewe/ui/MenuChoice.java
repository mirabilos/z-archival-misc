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
* A MenuChoice is a type of mChoice which uses a full Menu for the drop
* down slection which can have submenus. This is in contrast to an mChoice
* which cannot have submenus in its drop-down selector.
**/
//##################################################################
public class MenuChoice extends mChoice{
//##################################################################
{
	useMenuItems = true;
	mustAlwaysDrop = true;
	text = "";
}
/**
* Return the current text. This will be an empty String if nothing is selected.
**/
public String getText() {return text;}
/**
* This sets the text for the MenuChoice, regardless of if it is a valid choice. Use selectItem()
* to select an item only if it is a valid choice.
**/
public void setText(String txt) {text = txt; if (text == null) text = "";repaintNow();}
/**
* This method will select a particular item only if it is a valid choice.
**/
//===================================================================
public void selectItem(String txt)
//===================================================================
{
	Object obj = findItem(txt,true);
	if (obj instanceof String) setText((String)obj);
	else if (obj instanceof MenuItem){
		MenuItem mi = (MenuItem)obj;
		if (mi.subMenu == null) setText(mi.label);
	}
}
//public MenuItem selectedItem = null;
/**
* Find the MenuItem that is currently selected. Unlike the ChoiceControl.getSelectedItem()
* this does not use the selectedIndex field and always returns a MenuItem object.
**/
//==================================================================
public Object getSelectedItem()
//==================================================================
{
	if (text == null || text.length() == 0) return null;
	return findItem(text,true);
}
//===================================================================
public void setSelectedItem(MenuItem m)
//===================================================================
{
	if (m == null) setText(null);
	else selectItem(m.label);
}
//==================================================================
protected String getDisplayString() {return text;}
//==================================================================

//==================================================================
//------------------------------------------------------------------
public void onEvent(Event ev)
//------------------------------------------------------------------
{
	//System.out.println("Chosen! "+ev);
	if (ev instanceof MenuEvent && (ev.type == MenuEvent.SELECTED || ev.type == MenuEvent.ABORTED) && ev.target == menu){
		MenuEvent me = (MenuEvent)ev;
		if (ev.type != MenuEvent.ABORTED){
			if (me.selectedItem instanceof String) text = (String)me.selectedItem;
			else text = ((MenuItem)me.selectedItem).label;
		}
		noMenu();
		if (ev.type != MenuEvent.ABORTED) notifyAction();
	}else if (ev.target != menu)
		super.onEvent(ev);
}

//##################################################################
}
//##################################################################


