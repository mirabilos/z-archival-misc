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
/**
* A CardPanel is a simple implementation of a MultiPanel and it provides
* no user controls for selecting an item to be displayed. It can be used
* as the main container for more complex MultiPanel implementations, such
* as a Tabbed Panel (see mTabbedPanel).
* <p>
* There are no methods to remove items from the CardPanel, but you can do
* this manually by removing the card from the cards Vector. If the item
* you are removing happens to be selected, then call select(-1) to deselect it
* (or select a different item) before removing it. You will then need to repaint
* the control using the CardPanel again.
**/
//##################################################################
public class CardPanel extends Container implements MultiPanel{
//##################################################################
/**
* The cards.
**/
public Vector cards = new Vector();
/**
* The selected card, or -1 if none is selected.
**/
public int selectedItem = -1;
/**
* If this is true, then the items are placed in ScrollBarPanels before
* being added to the MultiPanel.
**/
public boolean autoScroll = false;
/**
If this is set to a control, then that control will gain the focus if a control
within the card panel has the focus, and then the CardPanel is set such that no
item is selected (using select(-1)), then the focusOnHide control will be given
the focus.
**/
public Control focusOnHide;
/**
* If this is set true, then when the displayed control is changed, the SIP will be turned off.
**/
public boolean clearSipOnChange = false;
/**
* Set this true if you dont want the focus to move to the Card on the next select(). It is set
* back to false after each select() call.
**/
public boolean dontFocusOnNextSelect = false;

//===================================================================
public int getSelectedItem() {return selectedItem;}
//===================================================================
public Iterator getSubControls()
//==================================================================
{
	Vector v = new Vector();
	for (Iterator it = cards.iterator(); it.hasNext();){
		CardPanelCard c = (CardPanelCard)it.next();
		v.add(c.panel);
	}
	return v.iterator();
}
//==================================================================
public Control addItem(Control item,String tabName,String longName)
//==================================================================
{
	Card c = new CardPanelCard(item,tabName,longName,autoScroll);
	c.panel.closedFocus = true;
	cards.add(c);
	return item;
}
//-------------------------------------------------------------------
protected Card findCard(Control item,String tabName)
//-------------------------------------------------------------------
{
	for (int i = 0; i<cards.size(); i++){
 		Card c = (Card)cards.get(i);
		if (item != null) {
			if (c.item == item) return c;
		}else if (tabName != null){
			if (tabName.equals(c.tabName)) return c;
		}
	}
	return null;
}
/**
Focus on the first control on the active panel.
**/
/*
//===================================================================
public void focusFirst()
//===================================================================
{
	if (curCard == null) return;
	if (curCard.panel == null) return;
	curCard.panel.focusFirst();
}
*/
//-------------------------------------------------------------------
protected Card curCard;
//-------------------------------------------------------------------
protected Control getFirstFocus()
//-------------------------------------------------------------------
{
	if (curCard == null) return null;
	if (curCard.panel == null) return null;
	return curCard.panel.getFirstFocus();
}
//-------------------------------------------------------------------
protected void select(Card who)
//-------------------------------------------------------------------
{
	try{
		selectedItem = cards.find(who);
		boolean hadFocus = false;
		if (made)
			if (curCard != null && curCard != who){
				Control lf = Gui.focusedControl();
				hadFocus = lf != null ? lf.isChildOf(this) : false;
				remove(curCard.panel);
			}
		curCard = who;
		if (made && clearSipOnChange) ewe.sys.Vm.freezeSIP(true,0,getWindow());
		if (made)
		 	if (who != null) {
				add(who.panel);
				if ((who.flags & Card.ALREADY_MADE) == 0) {
					who.panel.make(false);
					who.flags |= Card.ALREADY_MADE;
				}
				who.panel.setRect(0,0,width,height);
				if (hadFocus && !dontFocusOnNextSelect) who.panel.focusFirst();
				if (hasModifier(SendUpKeyEvents,false)) who.panel.modifyAll(SendUpKeyEvents,0,true);
			}
	}finally{
		dontFocusOnNextSelect = false;
	}
}
//-------------------------------------------------------------------
protected void selectAndPaint(Card c)
//-------------------------------------------------------------------
{
	if (c != null && c != curCard) {
		int got = c.panel.modify(Invisible,0);
		select(c);
		c.panel.restore(got,Invisible);
		c.panel.repaintNow();
		postEvent(new MultiPanelEvent(MultiPanelEvent.SELECTED,this,cards.find(c)));
	}
}
//===================================================================
public void select(Control item){ selectAndPaint(findCard(item,null));}
//===================================================================
public void select(String tabName){ selectAndPaint(findCard(null,tabName));}
//===================================================================
public void select(int index)
//===================================================================
{
	if (index < cards.size() && index >= 0)
		selectAndPaint((Card)cards.get(index));
	else {
		Control focusTo = null;
		Control c = Gui.focusedControl();
		if (c != null)
			if (c.isChildOf(this))
				focusTo = focusOnHide;
		select((Card)null);
		postEvent(new MultiPanelEvent(MultiPanelEvent.SELECTED,this,-1));
		if (focusTo != null)
			Gui.takeFocus(focusTo,ByRequest);
	}
}
//===================================================================
public Card getItem(int index) {if (index < cards.size() && index >= 0) return (Card)cards.get(index); return null;}
//===================================================================
public Card getItem(Control item){return findCard(item,null);}
//===================================================================
public int getItemCount() {return cards.size();}
//===================================================================

protected boolean made = false;
//==================================================================
public void make(boolean remake)
//==================================================================
{
	made = true;
	//System.out.println("Making cards");
	for (int i = 0; i<cards.size(); i++){
 		Card c = (Card)cards.get(i);
		c.panel.borderWidth = borderWidth;
		c.panel.borderStyle = borderStyle;
		add(c.panel);
		c.panel.make(remake);
		c.flags |= Card.ALREADY_MADE;
	}
	getPreferredSize(null);
	for (int i = 0; i<cards.size(); i++){
 		Card c = (Card)cards.get(i);
		remove(c.panel);
		//c.panel.make(remake);
	}
	if (cards.size() != 0)
		if (curCard == null)
			select((Card)cards.get(0));
		else
			select(curCard);
	super.make(remake);
}

//===================================================================
public void shown()
//===================================================================
{
	for (int i = 0; i<cards.size(); i++)
		((Card)cards.get(i)).panel.shown();
	super.shown();
}
protected boolean gotSize;

//==================================================================
protected void calculateSizes()
//==================================================================
{
	int w = 0, h = 0;
	for (int i = 0; i<cards.size(); i++){
 		Card c = (Card)cards.get(i);

		Dimension r = c.panel.getPreferredSize(null);
		if (r.width > w) w = r.width;
		if (r.height > h) h = r.height;
		//System.out.println("Card preferredSize: "+Geometry.toString(r));
	}
	preferredWidth = w;
	preferredHeight = h;
}

//==================================================================
public void setRect(int x,int y,int width,int height)
//==================================================================
{
	super.setRect(x,y,width,height);
	if (curCard != null)
	curCard.panel.setRect(0,0,width,height);
}


//##################################################################
}
//##################################################################

//##################################################################
class CardPanelCard extends Card{
//##################################################################

//===================================================================
public CardPanelCard(Control c,String tn,String ln,boolean autoScroll)
//===================================================================
{
	item = c;
	longName = ln;
	tabName = tn;
	if (longName == null) longName = tabName;
	panel = new CellPanel();

	if (autoScroll) {
		ScrollableHolder sh = new ScrollableHolder(c);
		ScrollablePanel sp = new ScrollBarPanel(sh);
		sh.modify(0,sh.TakeControlEvents);
		//sp.modifyScrollers(sp.SmallControl,0);
		panel.addLast(sp);
		//who = panel.addLast(sp);
		sh.stretchComponent = true;
		sh.scrollPercent = 50;
	}else {
		panel.addLast(c);
	}
}

//##################################################################
}
//##################################################################



