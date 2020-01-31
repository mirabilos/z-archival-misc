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
public class MenuEvent extends ControlEvent {
//##################################################################

public static final int SELECTED = 30000, DESELECTED = 30001, CLICKED = 30002, ABORTED = 30003, SELECTION_CHANGED = 30004;
public static final int NEXT_MENU_RIGHT = 30005, NEXT_MENU_LEFT = 30006;
/**
* This is relevant only when the event type is SELECTED, DESELECTED or CLICKED. It will
* be EITHER a MenuItem representing the relevant item, or a String representing the text
* of the relevant item.
**/
public Object selectedItem;
/**
* This is the Menu that generated the event, which is not necessarily the "target" for the
* event. The target for the event may change as the event goes up a chain of sub-menus,
* or up into a MenuBar container. All these controls "take over" the event and make themselves
* the target. However the "menu" value will always stay the same.
**/
public Menu menu;

//==================================================================
public MenuEvent(int type,Control target,Object item)
//==================================================================
{
	super(type,target);
	if (target instanceof Menu) menu = (Menu)target;
	selectedItem = item;
}

//##################################################################
}
//##################################################################

