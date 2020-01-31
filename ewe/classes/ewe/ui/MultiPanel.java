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

//##################################################################
public interface MultiPanel{
//##################################################################
/**
* This adds a control to the MultiPanel with a specified short (Tab) name and
* optional long name.
*
* When an item is added a Card object is created to hold information about
* the item.
* The item to be added is first added to a CellPanel before being placed
* int the panel. It may be added in a ScrollableHolder or other such
* container before being added to the CellPanel.
**/
public Control addItem(Control item,String tabName,String longName);
/**
* This selects for display one of the previously added Controls.
**/
public void select(Control item);
/**
* This selects for display one of the previously added Controls, given the
* short (tab) name for the item.
**/
public void select(String tabName);
/**
* This selects for display one of the previously added Controls, given the
* index of the item.
**/
public void select(int index);
/**
* This returns a Card object holding information about the item added at the
* specified index.
**/
public Card getItem(int index);
/**
* This returns a Card object holding information about the item added for the
* specified control.
**/
public Card getItem(Control item);
/**
* This returns the index for the currently selected item. It will return -1 if
* none is currently selected.
**/
public int getSelectedItem();
/**
* This returns the number of items added to the MultiPanel.
**/
public int getItemCount();
//##################################################################
}
//##################################################################

