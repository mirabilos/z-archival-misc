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
* This is an intermediate class which is the superclass for mList and
* for SimpleList. You should not use this class or inherit from this
* class yourself.
* <p>Inherit from mList to create a List that gets its items from a Vector
* of stored Strings or MenuItems. Note that this will use a lot of memory for
* very large lists.
* <p>Inherit from SimpleList to create a List that does not get its items
* from a Vector of stored Strings or MenuItems. SimpleList uses specific method
* calls to get the items which are to be displayed and these methods should be
* overridden to provide the most efficient method of item storage and retrieval.
**/
//##################################################################
public abstract class BasicList extends Menu implements ewe.data.ISimpleList{
//##################################################################
{
	notifyDataChangeOnSelect = true;
	isAList = true;
}
protected boolean amScrolling = false;
/**
* This specifies whether the list allows multiple selections or not.
**/
public boolean multiSelect = false;
/**
* If this is true then pen presses act as if CONTROL is always pressed.
**/
public boolean penSelectMode = (ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_KEYBOARD) != 0 ? true : false;

public int options = 0;

public static final int OPTION_CURSOR_EXIT_UP = 0x1;
public static final int OPTION_CURSOR_EXIT_DOWN = 0x2;
public static final int OPTION_CURSOR_EXIT_LEFT = 0x4;
public static final int OPTION_CURSOR_EXIT_RIGHT = 0x8;
public static final int OPTION_SELECT_FIRST_ON_KEY_FOCUS = 0x10;

//===================================================================
public BasicList(){}
//===================================================================

//==================================================================
public BasicList(int rows,int columns,boolean multi)
//==================================================================
{
	this();
	this.rows = rows; this.columns = columns; multiSelect =  multi;
}

//==================================================================
protected boolean allowNotOnMeSelection() {return !multiSelect;}
//==================================================================

//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (multiSelect || (!isOnMe(dc.curPoint) && amScrolling)) return;
	super.dragged(dc);
}
//-------------------------------------------------------------------
protected void releasedOnNothing()
//-------------------------------------------------------------------
{
	if (multiSelect){
		if (pressedItem != -1 && !wasSelected)
			selectItem(pressedItem,false);
	}else
		if (pressedItem != -1)
			notifySelection(selectedIndex,ListEvent.SELECTED);
}
//==================================================================
public void penPressed(Point p)
//==================================================================
{
	if (menuIsActive()) menuState.closeMenu();
	boolean shift = (currentPenEvent.modifiers & IKeys.SHIFT) != 0 && multiSelect;
	boolean ctrl = (currentPenEvent.modifiers & IKeys.CONTROL) != 0 && multiSelect;
	if (penSelectMode && multiSelect) ctrl = true;
	pressedItem = whichItem(p.x,p.y);
	if (pressedItem != -1){
		pressedItem += firstItem;
		if (pressedItem > itemsSize()) pressedItem = -1;
		wasSelected = (pressedItem != -1 && isSelected(pressedItem));
	}
	if (pressedItem != -1){
		if (multiSelect && !ctrl && !shift){//Must erase all other selections.
			int num = countSelectedIndexes();
			if (num > 1 || (num == 1 && !isSelected(pressedItem))){
				selectAll(false);
				repaintDataNow();
				lastRange = -1;
				notifySelection(-1,ListEvent.SELECTION_CHANGED);
			}
		}
		if (lastRange != -1 && shift){
			selectItems(pressedItem,lastRange,lastRangeSelected);
			repaintDataNow();
			lastRange = pressedItem;
			notifySelection(-1,ListEvent.SELECTION_CHANGED);
			return;
		}
	}
	lastRange = -1;
	selectNewItem(p);
}
int lastRange = -1;
boolean lastRangeSelected = false;
//==================================================================
public void penReleased(Point p)
//==================================================================
{
	if (lastRange != -1) return; //Range was selected so this is not used.
	if (menuIsActive()) return;
	boolean ctrl = (currentPenEvent.modifiers & IKeys.CONTROL) != 0 && multiSelect;
	if (penSelectMode && multiSelect) ctrl = true;

	int which = whichItem(p.x,p.y);
	if (which == -1) {
		releasedOnNothing();
		return;
	}
	which += firstItem;
	if (which > itemsSize()) {
		releasedOnNothing();
		return;
	}
	//ewe.sys.Vm.debug("Released: "+which+", "+selectedIndex);
	/*
		if (!ctrl && multiSelect){
			if (selectedLines.length == 1)
				select(selectedLines.data[0],false);
			else{
				selectAll(false);
				update();
			}
		}else
		*/
		if (ctrl){
			if (pressedItem == which){
				if (wasSelected){
					selectItem(which,false);
					notifySelection(which,ListEvent.DESELECTED);
				}
				lastRange = which;
				lastRangeSelected = !wasSelected;
			}else if (pressedItem != which && !wasSelected && pressedItem != -1){
				selectItem(selectedIndex,false);
				selectedIndex = -1;
			}
		}else{
			//if (pressedItem != selectedIndex)
			//notifySelection(selectedIndex,ListEvent.SELECTED);
			lastRange = selectedIndex;
			lastRangeSelected = true;
		}
/*
	if (selectedIndex != -1)
 		if (!multiSelect)
			notifySelection(selectedIndex,ListEvent.SELECTED);
		else {
			boolean selected = isSelected(selectedIndex);
			if (selected && !menuIsActive()) {
				selectItem(selectedIndex,!selected);
				notifySelection(selectedIndex,!selected ? ListEvent.SELECTED : ListEvent.DESELECTED);
				selectedIndex = -1;
			}
		}
*/
}
/*
//==================================================================
public void notifySelection(int index,int type)
//==================================================================
{
	ListEvent le = new ListEvent(type,this,getItemAt(index));
	postEvent(le);
	if (notifyDataChangeOnSelect) notifyDataChange();
}
*/
//===================================================================
public void penClicked(Point p)
//===================================================================
{/*
	if (selectedIndex != -1 && !multiSelect)
		notifySelection(selectedIndex,ListEvent.CLICKED);
	*/
	penReleased(p);
}
//==================================================================
public void penDoubleClicked(Point where)
//==================================================================
{
	notifySelection(selectedIndex,ListEvent.SELECTED);
	notifyAction();
}
//==================================================================
protected void pressedNewSelected(int oldSel)
//==================================================================
{
	if (!multiSelect) {
		int newSel = selectedIndex;
		selectedIndex = oldSel;
		selectItem(newSel,true);
		notifySelection(selectedIndex,ListEvent.SELECTED);
	}else {
		boolean selected = isSelected(selectedIndex);
		if (!selected) {
			selectItem(selectedIndex,!selected);
			notifySelection(selectedIndex,!selected ? ListEvent.SELECTED : ListEvent.DESELECTED);
			//selectedIndex = -1;
		}
	}
}

/**
* This either selects all the items (select == true) or none of the items (select = false).
**/
public abstract void selectAll(boolean select);
public abstract void setSelectedIndexes(int [] indexes);
public abstract int getSelectedIndex(int selectedIndexIndex);
public abstract int [] getSelectedIndexes();
public abstract boolean isSelected(int index);

//===================================================================
public int countSelectedIndexes()
//===================================================================
{
	return getSelectedIndexes().length;
}
/**
* This selects the item at the specified row (index) exclusively and then ensures that
* it is visible.
* @param row The row to select exclusively.
* @return true always.
*/
//===================================================================
public boolean selectAndView(int row)
//===================================================================
{
	selectAll(false);
	selectItem(row,true);
	makeVisible(row);
	updateItems();
	return true;
}
//===================================================================
public int countListItems()
//===================================================================
{
	return itemsSize();
}
//===================================================================
public abstract Object [] getListItems();
//===================================================================
//===================================================================
public abstract void setListItems(Object [] items);
//===================================================================
//==================================================================
protected void calculateSizes()
//==================================================================
{
/*
	if (amScrolling) super.calculatePreferredSize();
	else {
	*/
		preferredWidth = Gui.getAverageSize(getFontMetrics(),1,columns,0,0).width;
		preferredHeight = getItemHeight()*rows+spacing*2;
	//}
}
//==================================================================
public ScrollablePanel getScrollablePanel()
//==================================================================
{
	dontAutoScroll = amScrolling = true;
	ScrollablePanel sp = new ScrollBarPanel(this);
	sp.modify(0,TakeControlEvents);
	//sp.vbar.modify(SmallControl,0);
	//sp.hbar.modify(SmallControl,0);
	return sp;
}

//===================================================================
public void gotFocus(int how)
//===================================================================
{
	super.gotFocus(how);
	if (how == ByKeyboard && ((options & OPTION_SELECT_FIRST_ON_KEY_FOCUS) != 0) && itemsSize() != 0){
		if (selectedIndex == 0){
			makeVisible(selectedIndex);
			return;
		}
		select(0);
		if (makeVisible(selectedIndex)) repaintNow();
		notifyDataChange();
	}
}

//==================================================================
public void onKeyEvent(KeyEvent ev)
//==================================================================
{
	if (ev.type == ev.KEY_PRESS) {
		int sz = itemsSize();
		if (ev.key == IKeys.UP){
			if (selectedIndex <= 0 && ((options & OPTION_CURSOR_EXIT_UP) != 0)){
				if (tryNext(false)) select(-1);
				return;
			}
		}
		if (ev.key == IKeys.DOWN){
			if (selectedIndex >= itemsSize()-1 && ((options & OPTION_CURSOR_EXIT_DOWN) != 0)){
				if (tryNext(true)) select(-1);
				return;
			}
		}
		if (ev.key == IKeys.RIGHT){
			if (((options & OPTION_CURSOR_EXIT_RIGHT) != 0)){
				if (tryNext(true)) select(-1);
				return;
			}
		}
		if (ev.key == IKeys.LEFT){
			if (((options & OPTION_CURSOR_EXIT_LEFT) != 0)){
				if (tryNext(false)) select(-1);
				return;
			}
		}
		if (ev.key == IKeys.ESCAPE && isSomeonesHotKey(ev)) return;
	}
	super.onKeyEvent(ev);
}
//##################################################################
}
//##################################################################

