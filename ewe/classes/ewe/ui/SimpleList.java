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
* This is the base class for creating a List control that represents a list of data
* that is not necessarily stored in a Vector. This should be used when the list
* data is large and it would be memory inefficient to keep the entire list in memory.
*
* <p>You create a SimpleList by implementing specific key methods, which are used to
* determine the number of items, and what String should be used to represent a particular
* item.
*
* <p>This List avoids ever having to traverse the entire list of data for any operation, and
* so should work properly for any size list.
**/
//##################################################################
public abstract class SimpleList extends BasicList{
//##################################################################

{
	calculateWidth = false;
	items = null;
	useMenuItems = false;
	modify(TakesKeyFocus|WantHoldDown,PreferredSizeOnly);
	//borderWidth = 0;
	spacing = 3;
	rows = 5;
	columns = 20;
	dontSearchForKeys = true;
}
protected ewe.util.RangeList selectedItems = new ewe.util.RangeList();
//===================================================================
//===================================================================
public SimpleList(){}
//===================================================================

//==================================================================
public SimpleList(int rows,int columns,boolean multi)
//==================================================================
{
	this.rows = rows; this.columns = columns; multiSelect =  multi;
}



/**
 * Return the String used to represent the item at a particular index on screen.
 * @param idx The index of the item to display.
 * @return A String used to represent the item at a particular index on screen.
 */
public abstract String getDisplayItem(int idx);

//===================================================================
public Object getListItem(int idx)
//===================================================================
{
	return getObjectAt(idx);
}
/**
 * Return an Object representing the item at a particular index. This can be any type of Object. It is
 * not used for display purposes - getDisplayItem is used for this.
 * @param idx The index of the item to retrieve.
 * @return An Object used to represent the item at a particular index.
 */
public abstract Object getObjectAt(int idx);
/**
 * Get the number of items in the list.
 * @return the number of items in the list.
 */
public abstract int getItemCount();
//===================================================================
/**
* This returns getItemCount().
**/
//===================================================================
public int itemsSize() {return getItemCount();}
//===================================================================
//===================================================================
public MenuItem getItemAt(int idx)
//===================================================================
{
	if (idx < 0 || idx >= itemsSize())dummyItem.label = "";
	else dummyItem.label = getDisplayItem(idx);
	return dummyItem;
}
//===================================================================
public Object getSelectedItem() {return getObjectAt(selectedIndex);}
//===================================================================
public void listChanged(){updateItems();}
//===================================================================

protected static Point buff = new Point(-1,-1);
/**
   Select or deselect a range of items.
* @param start The first item in the range (inclusive).
* @param end The last item in the range (inclusive).
* @param selected true to select the items, false to remve the items.
*/
//===================================================================
public void selectItems(int start,int end,boolean selected)
//===================================================================
{
	if (selected) selectedItems.addRange(start,end);
	else selectedItems.removeRange(start,end);
	if (selected) selectedIndex = end;
	else selectedIndex = -1;
}
/**
 * Select or deselect a particular item.
 * @param index The item to select or deselect.
 * @param selected true to select the item, false to deselect it.
 */
//==================================================================
public void selectItem(int index,boolean selected)
//==================================================================
{
	if (index > getItemCount()) index = -1;
	if (selected && !multiSelect) selectItem(selectedIndex,false);
	selectedItems.removeRange(index,index);
	if (selected) {
		selectedItems.addRange(index,index);
		selectedIndex = index;
	}else{
		selectedIndex = -1;
	}
	//if (selected && !multiSelect) selectedIndex = index;
	if (index != -1) repaintItem(index);
}
//===================================================================
public int countSelectedIndexes()
//===================================================================
{
	return selectedItems.countItems();
}
//==================================================================
public int [] getSelectedIndexes()
//==================================================================
{
	return selectedItems.toIntArray();
}
//==================================================================
public boolean isSelected(int index)
//==================================================================
{
	return selectedItems.inRange(index);
}
//===================================================================
public void selectAll(boolean select)
//===================================================================
{
	selectedItems.clear();
	if (select){
		int t = getItemCount();
		if (t > 0) selectedItems.addRange(0,t-1);
	}
	if (!select) selectedIndex = -1;
}
//===================================================================
public void setSelectedIndexes(int [] all)
//===================================================================
{
	selectedItems.clear();
	if (all == null) return;
	int max = getItemCount();
	for (int i = 0; i<all.length; i++){
		int idx = all[i];
		if (idx < 0 || idx >= max) continue;
		selectedItems.addRange(idx,idx);
	}
	updateItems();
}
//===================================================================
public int getSelectedIndex(int selectedIndexIndex)
//===================================================================
{
	return selectedItems.getItemAtIndex(selectedIndexIndex);
}
/**
* This repaints the item at the particular index.
*/
//==================================================================
public void changeItem(int index,Object newText)
//==================================================================
{
	repaintItem(index);
}
//===================================================================
public Object [] getListItems()
//===================================================================
{
	throw new RuntimeException("Cannot get the items for this list.");
}
//===================================================================
public void setListItems(Object [] items)
//===================================================================
{
	throw new RuntimeException("Cannot set the items for this list.");
}

//##################################################################
}
//##################################################################

