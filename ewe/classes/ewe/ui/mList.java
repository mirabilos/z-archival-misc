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
import ewe.util.*;
import ewe.fx.*;

//##################################################################
public class mList extends BasicList implements ScrollClient,Selectable{
//##################################################################

{
	modify(TakesKeyFocus|AlwaysRecalculateSizes|WantHoldDown,PreferredSizeOnly);
	borderWidth = 0;
	borderStyle = mInput.inputEdge|BF_RECT;//EDGE_SUNKEN|BDR_OUTLINE;
	spacing = 3;
	rows = 5;
	columns = 20;
	useMenuItems = true;
	calculateWidth = true;
}

//==================================================================
public mList(int rows,int columns,boolean multi)
//==================================================================
{
	this.rows = rows; this.columns = columns; multiSelect =  multi;
}

//==================================================================
public boolean isSelected(int index)
//==================================================================
{
	//if (!multiSelect) return super.isSelected(index);
	MenuItem mi = getItemAt(index);
	return ((mi.modifiers & mi.Selected) != 0);
}
//==================================================================
public void selectItem(String label)
//==================================================================
{
	trySelectItem(label);
}
//==================================================================
public void selectItem(int index,boolean selected)
//==================================================================
{
/*
	if (selected && !multiSelect) {
		selectItem(selectedIndex,false);
	}
*/
	if (index > itemsSize()) index = -1;
	if (selected && !multiSelect) selectItem(selectedIndex,false);
	if (index != -1){
		MenuItem mi = getItemAt(index);
		if (selected) mi.modifiers |= mi.Selected;
		else mi.modifiers &= ~mi.Selected;
	}
	if (selected) selectedIndex = index;
	else selectedIndex = -1;
	repaintItem(index);
}

//==================================================================
public void postEvent(Event ev)
//==================================================================
{
	postEventNormally(ev);
	/*
	if (ev instanceof MenuEvent) {
		notifyDataChange();
	}
	*/
}

//==================================================================
protected void paintBox(Graphics g)
//==================================================================
{
	Color fill = Color.White;
	int flags = getModifiers(true);
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)))
		fill = getBackground();
	//g.draw3DButton(getDim(null),true,fill,((flags & DrawFlat) != 0),true);
	if ((borderStyle & BF_SOFT) != 0){
		doBackground(g);
		//g.setColor(getBackground());
		//g.fillRoundRect(0,0,width,height,3);
		g.setColor(fill);
 		g.fillRoundRect(0,0,width,height,3);
	}else
		g.draw3DRect(
		getDim(Rect.buff),
		borderStyle,
		(flags & DrawFlat) != 0,
		fill,
		Color.DarkGray);
}
//
// Can remove this from here.
//
//==================================================================
protected void calculateSizes()
//==================================================================
{
	super.calculateSizes();
}
//
// Can remove this from here.
//
//==================================================================
public ScrollablePanel getScrollablePanel()
//==================================================================
{
	return super.getScrollablePanel();
}
/**
* Use this to clear the selection or to select all the items.
*/
//==================================================================
public void selectAll(boolean select)
//==================================================================

{
	if (select && multiSelect)
		changeModifiers(MenuItem.Selected,0);
	else if (!select) {
		changeModifiers(0,MenuItem.Selected);
		selectedIndex = -1;
	}
}

//===================================================================
public boolean noSelection()
//===================================================================
{
	selectAll(false);
	return true;
}
//===================================================================
public boolean hasSelection() {return selectedIndex != -1;}
//===================================================================
public boolean deleteSelection()
//===================================================================
{
	int [] got = getSelectedIndexes();
	if (got.length == 0) return false;
	for (int i = 0; i<got.length; i++)
		deleteItem(got[i]-i);
	noSelection();
	updateItems();
	return true;
}
//===================================================================
public Object getSelection()
//===================================================================
{
	int [] got = getSelectedIndexes();
	if (got.length == 0) return null;
	Object ret [] = new Object[got.length];
	for (int i = 0; i<ret.length; i++){
		ret[i] = getItemAt(got[i]).toString();
	}
	return ret;
}
//===================================================================
public boolean replaceSelection(Object clipboard)
//===================================================================
{
	deleteSelection();
	if (clipboard == null) return false;
	if (clipboard instanceof Object []){
		Object [] objs = (Object [])clipboard;
		String [] all = new String[objs.length];
		for (int i = 0; i<all.length; i++) all[i] = objs[i].toString();
 		addItems(all);
	}
	else addItem(clipboard);
	updateItems();
	return true;
}
//===================================================================
public int countSelectedIndexes()
//===================================================================
{
	int s = 0;
	for (int i = 0; i<itemsSize(); i++)
		if (isSelected(i)) s++;
	return s;
}
//==================================================================
public int [] getSelectedIndexes()
//==================================================================
{
	int s  = countSelectedIndexes();
	int [] ret = new int[s];
	s = 0;
	for (int i = 0; i<itemsSize() && s != ret.length; i++)
		if (isSelected(i)) ret[s++] = i;
	return ret;
}
//===================================================================
public int getSelectedIndex(int selectedIndexIndex)
//===================================================================
{
	int s = 0;
	for (int i = 0; i<itemsSize(); i++)
		if (isSelected(i))
			if (s == selectedIndexIndex) return i;
			else s++;
	return -1;
}
//==================================================================
public MenuItem addItem(String what)
//==================================================================
{
	String [] str = new String[1];
	str[0] = what;
	return addItems(str)[0];
}
//===================================================================
public void setSelectedIndexes(int [] indexes)
//===================================================================
{
	selectAll(false);
	if (indexes == null) return;
	for (int i = 0; i<indexes.length; i++)
		getItemAt(indexes[i]).modifiers |= MenuItem.Selected;
	updateItems();
}
//==================================================================
protected void doPaintData(Graphics gr)
//==================================================================
{
	if (ss != null) ss.checkScrolls();
	super.doPaintData(gr);
}

/*
public void penHeld(Point p)
{
	startDropMenu(p);
}
public void startDropMenu(Point p)
{
	if (getMenu() != null && !menuIsActive()){
		int it = whichItem(p.x,p.y);
		if (it < 0 || it >= getScreenRows()+1) return;
		menuState.doShowMenu(p,true,getItemRect(it+firstItem,null));
	}
}
*/
//===================================================================
public Object [] getListItems()
//===================================================================
{
	Object [] ret = new Object[items.size()];
	items.copyInto(ret);
	return ret;
}
//===================================================================
public void setListItems(Object [] items)
//===================================================================
{
	setSelectedIndexes(new int[0]);
	this.items.clear();
	if (items instanceof MenuItem[])
		this.items.addAll(items);
	else
	for (int i = 0; i<items.length; i++)
		addItem(items[i]);
}
//===================================================================
public Object getListItem(int index)
//===================================================================
{
	return items.get(index);
}
//##################################################################
}
//##################################################################

