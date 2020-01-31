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
import ewe.sys.Vm;

/**
* This is a Tabbed Panel with tab selectors placed at the top of the
* panel. Left and right scroll buttons are automatically provided
* should the tabs not fit completely in the tab selector area.<p>
* mTabbedPanels generate a DataChange event when their selected item is changed.
**/
//##################################################################
public class mTabbedPanel extends CellPanel implements MultiPanel{
//##################################################################
/**
* This does the actual MultiPanel work. You can set cardPanel.autoScroll
* true or false as needed. By default cardPanel.autoScroll is true.
**/
public CardPanel cardPanel = new CardPanel();
{
	cardPanel.autoScroll = true;
}
/**
* This should be NORTH/SOUTH/EAST/WEST (but only NORTH/SOUTH are currently supported)
**/
public int tabLocation = NORTH;

/**
* This is initially null. If you set it to a control, it will be added to the left of the tabs
* during the make.
**/
public Control extraControlsLeft;
/**
* This is initially null. If you set it to a control, it will be added to the right of the tabs
* during the make.
**/
public Control extraControlsRight;
/**
* This is false by default - set it true so that the left/right scroll buttons are never seen.
**/
public boolean noScrollButtons = false;

private Control extremeControlsLeft, extremeControlsRight;

protected Control closeTabButton;
protected TabsPanel tabs = new TabsPanel(this);
protected CellPanel top = new CellPanel();
protected mButton left = new tabArrowButton(mGraphics.Left), right = new tabArrowButton(mGraphics.Right);

public Control addCloseTabButton()
{
	return addCloseTabButton(null,false);
}
public Control addCloseTabButton(Control button,boolean toLeft)
{
	if (closeTabButton != null) return closeTabButton;
	if (button == null) button = new mButton(Form.cross);
	closeTabButton = button;
	closeTabButton.modify(MouseSensitive,0);
	CellPanel cp = getExtremeControls(toLeft);
	cp.addLast(closeTabButton).setCell(VSTRETCH).setControl(DONTFILL);
	closeTabButton.setToolTip("Close active tab.");
	return closeTabButton;
}
/**
* If this is true, then the tabs are not expanded to show both the icon AND the text when selected. Rather,
* only the closedIcon will be displayed. By default this is false.
**/
public boolean dontExpandTabs = false;
/**
 * If this is true, then if a single Tab is present, the Tab area will be hidden completely.
 */
public boolean hideSingleTab = false;
/**
 * Used when hideSingleTab is true. If this is true,
 * then although the tab will not be displayed, the area
 * allocated for the tabs will not be closed so that extra controls added
 * to this area will still be displayed.
 */
public boolean keepExtraControls = false;
/**
 * Add a new item to the tabbed panel.
 * @param item The item to be added
 * @param tabName The name to appear on the tab.
 * @param longName A longer descriptive name for the tab. If this is null it will be set to tabName.
 * @return The parameter item.
 */
//==================================================================
public Control addItem(Control item,String tabName,String longName)
//==================================================================
{
	Control c = cardPanel.addItem(item,tabName,longName);
	if (made) repaintTabs();
	return c;
}
/**
 * Add a new item to the tabbed panel.
 * @param item The item to be added
 * @param tabName The name to appear on the tab.
 * @param longName A longer descriptive name for the tab. If this is null it will be set to tabName.
 * @return The Card created for the item.
 */
//===================================================================
public Card addCard(Control item,String tabName,String longName)
//===================================================================
{
	cardPanel.addItem(item,tabName,longName);
	if (made) repaintTabs();
	return cardPanel.getItem(item);
}

/**
* Set this true if you dont want the focus to move to the Card on the next select(). It is set
* back to false after each select() call.
**/
public boolean dontFocusOnNextSelect = false;

public void select(Control item) {cardPanel.dontFocusOnNextSelect = dontFocusOnNextSelect; cardPanel.select(item); newSelection();}
public void select(String tabName) {cardPanel.dontFocusOnNextSelect = dontFocusOnNextSelect; cardPanel.select(tabName); newSelection();}
public void select(int index) {cardPanel.dontFocusOnNextSelect = dontFocusOnNextSelect; cardPanel.select(index); newSelection();}
public Card getItem(int index) {return cardPanel.getItem(index);}
public Card getItem(Control item) {return cardPanel.getItem(item);}
public int getSelectedItem() {return cardPanel.getSelectedItem();}
public int getItemCount() {return cardPanel.getItemCount();}

//-------------------------------------------------------------------
static int getBorder()
//-------------------------------------------------------------------
{
	return (ButtonObject.buttonEdge/*standardEdge*/|BDR_OUTLINE) & ~(BF_RECT|BF_BUTTON);
}
//-------------------------------------------------------------------
protected Control getFirstFocus()
//-------------------------------------------------------------------
{
	return cardPanel.getFirstFocus();
}

/**
 * This gets a CellPanel to put extra controls in that will be further left or right
	of the container provided by getExtraControls().
 * @param isLeft If the container goes on the left or right.
 * @return The CellPanel to add your controls to.
 */
//===================================================================
public CellPanel getExtremeControls(boolean isLeft)
//===================================================================
{
	if (isLeft && extremeControlsLeft == null) extremeControlsLeft = new CellPanel();
	if (!isLeft && extremeControlsRight == null) extremeControlsRight = new CellPanel();
	return isLeft ? (CellPanel)extremeControlsLeft : (CellPanel)extremeControlsRight;
}
/**
* This gets a container to put extra controls in. These extra controls
* will either go the the left of the tabs (isLeft is true) or the right
* of the tabls (isLeft is false). If you are going to change tabLocation,
* do so before calling this.
**/
//===================================================================
public CellPanel getExtraControls(boolean isLeft)
//===================================================================
{
	if (isLeft && extraControlsLeft == null) extraControlsLeft = new CellPanel();
	if (!isLeft && extraControlsRight == null) extraControlsRight = new CellPanel();
	return isLeft ? (CellPanel)extraControlsLeft : (CellPanel)extraControlsRight;
}

boolean autoExpand = false;
private boolean expandIsOpen = false;
private PanelSplitter splitter;
private mButton shrink;

/**
 * Call this AFTER setting the tabLocation to be NORTH or SOUTH (it is NORTH by default), but before
 * doing a make(). You should use getExpandingTabbedPanel() instead to do all the hard work for you
	when creating an expanding tabbed panel.
* @param splittablePanel The SplittablePanel that contains the TabbedPanel.
*/
//===================================================================
public void setAutoExpand(SplittablePanel splittablePanel)
//===================================================================
{
	autoExpand = true;
	expandIsOpen = (tabLocation == NORTH);
	SplittablePanel sp = splittablePanel;
	if (expandIsOpen)
	splitter = sp.setSplitter(PanelSplitter.BEFORE|PanelSplitter.PREFERRED_SIZE,
		PanelSplitter.BEFORE|PanelSplitter.MIN_SIZE,PanelSplitter.CLOSED);
	else
	splitter = sp.setSplitter(PanelSplitter.AFTER|PanelSplitter.MIN_SIZE,
		PanelSplitter.AFTER|PanelSplitter.PREFERRED_SIZE,PanelSplitter.OPENED);
	splitter.thickness = 0;
	getExtremeControls(true).addNext(shrink = new mButton(ImageCache.cache.get("ewe/UpDown.bmp",Color.White)){
		{
			modify(Disabled|NoFocus,0);
			setBorder(BDR_NOBORDER,0);
		}
		public void doAction(int how){
			if (getSelectedItem() != -1) selectAndExpand(-1);
			super.doAction(how);
		}
	}
	);
}

//==================================================================
public void make(boolean remake)
//==================================================================
{
	left.modify(NotAnEditor,0);
	right.modify(NotAnEditor,0);
	top.borderStyle = getBorder();
	top.borderStyle |= tabLocation != SOUTH ? BF_BOTTOM : BF_TOP;
	top.borderStyle = BDR_NOBORDER;

	top.borderColor = Color.Black;
	if (extremeControlsLeft != null) top.addNext(extremeControlsLeft).setCell(VSTRETCH);
	if (extraControlsLeft != null) top.addNext(extraControlsLeft).setCell(VSTRETCH);
	if (!noScrollButtons) top.addNext(left).setCell(VSTRETCH);
	top.addNext(tabs).setCell(dontExpandTabs ? VSTRETCH : STRETCH);
	if (!noScrollButtons) top.addNext(right).setCell(VSTRETCH);
	if (extraControlsRight != null) top.addNext(extraControlsRight).setCell(dontExpandTabs ? STRETCH : VSTRETCH);
	if (extremeControlsRight != null) top.addNext(extremeControlsRight).setCell(VSTRETCH);
	top.endRow();
	if (tabLocation != SOUTH)
		addLast(top).setCell(HSTRETCH);
	addLast(cardPanel).setTag(INSETS,new Insets(3,3,3,3));
	if (tabLocation == SOUTH)
		addLast(top).setCell(HSTRETCH);
	super.make(remake);
	if (autoExpand) select(-1);
	checkLeftRight();
}
//-------------------------------------------------------------------
void paintEdge(Graphics gr)
//-------------------------------------------------------------------
{
	int flags = getModifiers(true);
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	Rect r2 = cardPanel.getRect(Rect.buff);
	r2.width += 6; r2.height += 6;
	r2.x -= 3; r2.y -= 3;
	int bdr = getBorder();
	if (globalPalmStyle) bdr |= BF_PALM;
	if ((bdr & BF_PALM) != 0)
		bdr |= tabLocation != SOUTH ? BF_TOP : BF_BOTTOM;
	else if ((bdr & BF_RECT) == 0)
		bdr |= BF_RECT;
	if ((bdr & BF_RECT) != BF_RECT) bdr &= ~BF_SOFT;

	g.draw3DRect(
		r2,
		bdr,
		(flags & DrawFlat) != 0,
		null,
		Color.Black);
	Rect r = tabs.getSelectedTabRect();
	if (r != null){
		Card c = (Card)cardPanel.cards.get(getSelectedItem());
		boolean blacked = ((flags & DrawFlat) != 0 && c.image == null);
		if (!blacked && tabsAreDisplayed()){
			g.setColor(getBackground());
			if (tabLocation != SOUTH)
				g.fillRect(r.x+2,r2.y,r.width-4,2);
			else
				g.fillRect(r.x+2,r2.y+r2.height-2,r.width-4,2);
		}
	}
	if (g != gr) g.free();
}
//==================================================================
public void doPaint(Graphics g,Rect r)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	tabs.fitLeft();
	checkLeftRight();
	super.doPaint(g,r);
	paintEdge(g);
}
//==================================================================
public void checkLeftRight()
//==================================================================
{
	checkLeftRight(false);
}
//==================================================================
public void checkLeftRight(boolean dontAutoShift)
//==================================================================
{
	int num = cardPanel.cards.size();
	boolean amShrunk = !tabsAreDisplayed();
	if (closeTabButton != null && cardPanel.selectedItem != -1){
		Card c = (Card)cardPanel.cards.get(cardPanel.selectedItem);
		closeTabButton.set(Disabled,(c.flags & Card.CLOSEABLE) == 0);
		closeTabButton.repaintNow();
	}
	if (hideSingleTab && num == 1 && !amShrunk){
		top.modify(ShrinkToNothing,0);
		relayoutMe(true);
	}else if ((!hideSingleTab || num > 1) && amShrunk){
		top.modify(0,ShrinkToNothing);
		relayoutMe(true);
	}
	if (dontFocusOnNextSelect) {
		dontRepaintTabsOnFocus = true;
		focusOnContainer(ByRequest);
	}
	if (dontAutoShift) tabs.recalculate();
	else tabs.newSelected();
	left.modify(0,Disabled|Invisible);
	right.modify(0,Disabled|Invisible);
	if (tabs.leftMost == 0 || num == 0) left.modify(Disabled|Invisible,0);
	if ((tabs.rightMost == cardPanel.cards.size()-1) || num == 0) right.modify(Disabled|Invisible,0);
}
//===================================================================
public void selectAndExpand(int which)
//===================================================================
{
	if (autoExpand)
		if (which != getSelectedItem()){
			if (which == -1) {
				splitter.doOpenClose(!expandIsOpen);
				shrink.modify(Disabled,0);
			}
			else if (getSelectedItem() == -1) {
				splitter.doOpenClose(expandIsOpen);
				shrink.modify(0,Disabled);
			}
		}
	select(which);
}
boolean callPaintEdge = false;

//===================================================================
public void repaintTabs()
//===================================================================
{
	checkLeftRight();
	callPaintEdge = true;
	top.repaintNow();
	callPaintEdge = false;
}
//==================================================================
public boolean tabsAreDisplayed()
//==================================================================
{
	boolean amShrunk = top.hasModifier(ShrinkToNothing,false);
	return !amShrunk;
}
//==================================================================
public void newSelection()
//==================================================================
{
	dontFocusOnNextSelect = false;
	repaintTabs();
	notifyDataChange();
	//paintEdge(null);
}
/**
 * This is called if a Tab is closed via the closeTab() method or Close Tab button.
 * @param closed the Card that was closed.
 * @param index the index of the Card when it was closed.
 */
protected void tabClosed(Card closed, int index)
{

}
/**
 * This closes the currently selected tab unconditionally.
 * @return true if a tab was closed, false if not.
 */
public boolean closeCurrentTab()
{
	int num = getItemCount();
	int which = getSelectedItem();
	if (which == -1) return false;
	return closeTab(which);
}
/**
 * Close a particular tab.
 * @param c the Card of the tab to close.
 * @return true if a tab was closed, false if not.
 */
public boolean closeTab(Card c)
{
	int idx = cardPanel.cards.find(c);
	if (idx == -1) return false;
	return closeTab(idx);
}
/**
 * This is called when a Tab is about to be closed.
 * By default it will check if the item added to the Card was a Form and
 * if it was it will call and return canExit(Form.IDCANCEL) on the Form. Otherwise
 * it will return true.
 * @param c The Card of the Tab being closed.
 * @param index The index of the Card being closed.
 * @return true if the Tab should close, false if not.
 */
protected boolean tabCanClose(Card c, int index)
{
	if (c.item instanceof Form) return ((Form)c.item).canExit(Form.IDCANCEL);
	return true;
}
/**
 * Close a particular tab.
 * @param which the index of the tab to close.
 * @return true if a tab was closed, false if not.
 */
public boolean closeTab(int which)
{
	int num = getItemCount();
	if (which < 0 || which >= num) return false;
	int value = modify(Invisible,0);
	if (num == 1) select(-1);
	else if (which == 0) select(1);
	else select(which-1);
	Card c = (Card)cardPanel.cards.get(which);
	if (!tabCanClose(c,which)) return false;
	cardPanel.cards.removeElementAt(which);
	if (cardPanel.selectedItem > which) cardPanel.selectedItem--;
	newSelection();
	restore(value,Invisible);
	repaintNow();
	tabClosed(c,which);
	return true;
}

//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if ((ev instanceof MultiPanelEvent) && (ev.target == cardPanel)) {
		ev.target = this;
	}else if (ev.type == ev.PRESSED) {
		if (ev.target == left) tabs.shiftLeft();
		else if (ev.target == right) tabs.shiftRight();
		else if (ev.target == closeTabButton){
			int which = getSelectedItem();
			if (which == -1) return;
			Card c = getItem(which);
			if ((c.flags & Card.CLOSEABLE) != 0){
				closeCurrentTab();
			}
		}
		if (ev.target == left || ev.target == right){
			checkLeftRight(true);
			callPaintEdge = true;
			top.repaintNow();
			callPaintEdge = false;
			ev.consumed = true;//consume(ev);
		}
	}
	super.onControlEvent(ev);
}

private boolean dontRepaintTabsOnFocus;
/**
This can only get focus explicitly - via focusOnTab, or via selectNextTab(). Normal navigation
will always focus the control within it.
**/
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	super.gotFocus(how);
	if (containerHasFocus() && !dontRepaintTabsOnFocus)
		repaintTabs();
	dontRepaintTabsOnFocus = false;
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	super.lostFocus(how);
	repaintTabs();
}
/**
 * Select the next tab.
 * Additional verbose
 * @param forwards true to move to the next tab, false to move to the previous one.
 * @param focusOnTab true to move or keep the focus on the tabs.
 * @return
 */
//===================================================================
public void selectNextTab(boolean forwards, boolean focusOnTab)
//===================================================================
{
	dontFocusOnNextSelect = focusOnTab;
	int cur = getSelectedItem();
	int all = getItemCount();
	int was = cur;
	try{
		if (forwards){
			while(true){
				cur++;
				if (cur >= all) cur = 0;
				if (cur == was) return;
				if (was == -1) was = 0;
				Card c = (Card)cardPanel.cards.get(cur);
				if ((c.flags & (c.HIDDEN|c.DISABLED)) != 0) continue;
				select(cur);
				return;
			}
		}else{
			while(true){
				cur--;
				if (cur < 0) cur = all-1;
				if (cur == was || cur == -1) return;
				if (was == -1) was = 0;
				Card c = (Card)cardPanel.cards.get(cur);
				if ((c.flags & (c.HIDDEN|c.DISABLED)) != 0) continue;
				select(cur);
				return;
			}
		}
	}finally{
		//if (focusOnTab) Gui.takeFocus(tabs,ByRequest);
	}
}
//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	if (containerHasFocus()){
		if (ev.type == ev.KEY_PRESS){
			if (ev.key == IKeys.LEFT || ev.key == IKeys.RIGHT){
				int max = cardPanel.cards.size()-1;
				int sel = cardPanel.selectedItem;
				if (ev.key == IKeys.RIGHT){
					if (sel < max) selectNextTab(true,true);
				}else{
					if (sel != 0) selectNextTab(false,true);
				}
				return;
			}else if (ev.key == IKeys.UP || ev.key == IKeys.DOWN || ev.isActionKey()){
				focusOnData(ByKeyboard);
				return;
			}
		}
	}
	super.onKeyEvent(ev);
}

//-------------------------------------------------------------------
protected boolean doHotKey(Control from,KeyEvent ev)
//-------------------------------------------------------------------
{
	boolean isNext = ev.key == IKeys.NEXT_TAB;
	boolean isPrev = ev.key == IKeys.PREV_TAB;
	if (((ev.modifiers & IKeys.CONTROL) == IKeys.CONTROL) && !isNext && !isPrev){
		isNext = (ev.key == IKeys.RIGHT);
		isPrev = (ev.key == IKeys.LEFT);
	}
	if (isNext || isPrev){
		selectNextTab(isNext,true);//false);
		return true;
	}
	return super.doHotKey(from,ev);
}

protected void calculateSizes()
{
	super.calculateSizes();
	minHeight = tabs.preferredHeight;
}

/**
 * Use this to create a self-expanding mTabbedPanel. This actuall consists
	of a SplittablePanel which contiains an mTabbedPanel and an empty CellPanel. Selecting
	the tabs of the mTabbedPanel automatically opens and displays the panel.
 * @param above set this to true if the mTabbedPanel should be above the CellPanel data, false
	if it should be below.
 * @return An array of Objects which are as follows:<br>
	Index 0 = the mTabbedPanel itself - add items to it, but do not set the tabLocation.<br>
	Index 1 = the SplittablePanel that you should add to the destination container.<br>
	Index 2 = the empty CellPanel that you should add your other controls to.
 */
//===================================================================
public static Object [] getExpandingTabbedPanel(boolean above)
//===================================================================
{
	Object [] ret = new Object[3];
	SplittablePanel sp = new SplittablePanel(SplittablePanel.VERTICAL);
	ret[1] = sp;
	CellPanel top = sp.getNextPanel();
	CellPanel bottom = sp.getNextPanel();

	mTabbedPanel mt = new mTabbedPanel();
	ret[0] = mt;
	if (above) {
		mt.tabLocation = NORTH;
		top.addLast(mt);
		mt.setAutoExpand(sp);
		ret[2] = bottom;
	}else{
		mt.tabLocation = SOUTH;
		bottom.addLast(mt);
		mt.setAutoExpand(sp);
		ret[2] = top;
	}
	return ret;
}

//##################################################################
}
//##################################################################
//##################################################################
class TabsPanel extends Control{
//##################################################################

mTabbedPanel owner;
int [] widths, screens;
int maxWidth, height;
int leftMost = 0, rightMost = 0;
TabsPanel(mTabbedPanel o)
{
	owner = o;
	modify(NoFocus,0);
	PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_ONOFF|PenEvent.WANT_PEN_MOVED_INSIDE,true);
}
protected void calculateSizes()
{
	recalculate();
	preferredHeight = height;
	preferredWidth = maxWidth;
}

public void recalculate()
{
	int num = owner.cardPanel.cards.size();
	widths = new int[num];
	screens = new int[widths.length];
	maxWidth = 0;
	FontMetrics fm = getFontMetrics();
	for (int i = 0; i<widths.length; i++){
		Card c = (Card)owner.cardPanel.cards.get(i);
		boolean selected = owner.cardPanel.selectedItem == i;
		Rect r = Gui.getSize(fm,c.tabName,4,4);
		if ((c.flags & c.HIDDEN) != 0) {
			r.width = 0;
		}else{
			if (c.image != null) {
				r.width = c.image.getWidth()+8;
				r.height = c.image.getHeight()+8;
			}
			if (owner.dontExpandTabs) selected = false;
			if (c.closedImage != null && !selected){
				r.width = c.closedImage.getWidth()+8;
				r.height = c.closedImage.getHeight()+8;
			}
		}
		height = r.height;
		widths[i] = r.width;
		if (owner.dontExpandTabs) maxWidth += r.width;
		else
			if (r.width > maxWidth) maxWidth = r.width;
	}
	height += 2;
	fitLeft();
}

public void newSelected()
{
	recalculate();
	int sel = owner.cardPanel.selectedItem;
	if (sel != -1){
		if (sel < leftMost){
			leftMost = sel;
			fitLeft();
		}else if (sel > rightMost){
			rightMost = sel;
			fitRight();
		}
	}
}
public void fitLeft()
{
	Rect d = getDim(null);
	while(leftMost >= widths.length) leftMost--;
	rightMost = leftMost;
	int w = 0;
	for (int i = leftMost; i<widths.length && i >= 0; i++){
		if (w + widths[i] > d.width) break;
		w += widths[i];
		rightMost = i;
	}
}
public void fitRight()
{
	Rect d = getDim(null);
	while(rightMost >= widths.length) rightMost--;
	leftMost = rightMost;
	int w = 0;
	for (int i = rightMost; i>=0; i--){
		if (w + widths[i] > d.width) break;
		w += widths[i];
		leftMost = i;
	}
}
public void shiftRight()
{
	if (rightMost < widths.length) {
		rightMost++;
		fitRight();
	}
}
public void shiftLeft()
{
	if (leftMost > 0) {
		leftMost--;
		fitLeft();
	}
}

//===================================================================
public Rect getSelectedTabRect()
//===================================================================
{
	Rect d = getDim(null);
	int x = 0;
	for (int i = leftMost; i<=rightMost+1 && i<widths.length && i>=0; i++){
		if (owner.cardPanel.selectedItem == i){
			return new Rect(this.x+x,this.y,widths[i],this.height);
		}
		x += widths[i];
	}
	return null;
}
public void doPaint(Graphics g,Rect area)
{
	int flags = getModifiers(true);
	int raise = 2;
	if (!((flags & Invisible) == 0)) return;
	fitLeft();
	//doBackground(g);
	Rect d = getDim(null);
	int w = d.width;
	int x = 0;
	FontMetrics fm = getFontMetrics();
	Color bg = getBackground();
	Color lighterGray = bg;// new Color(220,220,220);
	int bdr = mTabbedPanel.getBorder();
	boolean south = owner.tabLocation == SOUTH;
	if (!south) {
		bdr |= BF_LEFT|BF_RIGHT|BF_TOP;
		raise = 2;
	}else {
		bdr |= BF_LEFT|BF_RIGHT|BF_BOTTOM;
		raise = -2;
	}
	for (int i = leftMost; i<=rightMost+1 && i<widths.length && i>=0; i++){
		Card c = (Card)owner.cardPanel.cards.get(i);
		boolean selected = owner.cardPanel.selectedItem == i;
		int r = selected ? raise : 0;
		screens[i] = widths[i];
		int tw = screens[i];
		if (tw != 0){
			if (i == rightMost+1) tw = w;
			boolean blacked = ((flags & DrawFlat) != 0 && selected && c.image == null);
			Color fill = blacked ? Color.Black : (selected ? bg : lighterGray);
			if (owner.containerHasFocus() && selected) fill = Color.LightGreen;
			int bd = bdr;//blacked ? bdr|BF_RECT : bdr;
			g.draw3DRect(
				new Rect(x,raise-r,tw,d.height-2+(r == 0 ? 0 : 2)),
				selected ? bd : bd & ~BDR_OUTLINE,
				(flags & DrawFlat) != 0,
				fill,
				Color.Black);
			/*
			if (owner.cardPanel.selectedItem == i){
				Graphics g2 = g;
				g2.setColor(getBackground());
				if (owner.tabLocation != SOUTH)
					g2.fillRect(x+1,d.height,tw-2,2);
				else
					g2.fillRect(x+1,0,tw-2,2);
				if (g != g2) g2.free();
			}else
			*/
			 g.setColor(getForeground());
			Rect oldc = g.reduceClip(new Rect(x+2,y,tw-4,d.height));
			if (owner.dontExpandTabs) selected = false;
			if (!selected && c.closedImage != null){
				c.closedImage.draw(g,x+4,(d.height-raise-c.image.getHeight())/2+raise-r,((c.flags & c.DISABLED) != 0) ? IImage.DISABLED : 0);
			}else if (c.image != null){
				c.image.draw(g,x+4,(d.height-raise-c.image.getHeight())/2+raise-r,((c.flags & c.DISABLED) != 0) ? IImage.DISABLED : 0);
			}else{
				Point p = Gui.centerText(fm,c.tabName,screens[i],d.height-raise);
				g.setFont(getFont());
				if (((c.flags & c.DISABLED) != 0)) g.setColor(Color.DarkGray);
				else if (blacked) g.setColor(Color.White);
				g.drawText(c.tabName,x+p.x,p.y+raise-r);
			}
			g.restoreClip(oldc);
		}
		x += screens[i];
		w -= screens[i];
	}
	if (owner.callPaintEdge && (owner.cardPanel.selectedItem != -1)) owner.paintEdge(null);
}

//==================================================================
public int whichTab(Point where)
//==================================================================
{
	if (widths.length == 0) return -1;
	int num = rightMost-leftMost+2;
	int which = -1;
	int x = 0,lx = 0;
	for (int i = 0; i<num && leftMost+i<widths.length; i++){
		x += widths[leftMost+i];
		if (where.x < x && where.x >= lx) {
			Card c = (Card)owner.cardPanel.cards.get(i+leftMost);
			if ((c.flags & (c.HIDDEN|c.DISABLED)) == 0){
				which = i;
				break;
			}
		}
		lx = x;
	}
	if (which == -1) return which;
	return which+leftMost;
}

//==================================================================
public void penPressed(Point where)
//==================================================================
{
	int tab = whichTab(where);
	if (tab == -1) return;
	if (owner.autoExpand && tab == owner.getSelectedItem())
		owner.selectAndExpand(-1);
	else
		owner.selectAndExpand(tab);
}
static ToolTip tt = new ToolTip();
static Point tp = new Point();

//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	int tab = whichTab(tp.set(x,y));
	if (tab == -1) return null;
	tt.persists = false;
	Card c = owner.getItem(tab);
	if (c.tip != null) tt.tip = c.tip;
	else
		tt.tip = c.image == null ? (Object)c.tabName : (Object)c.image;
	return tt;
}

//##################################################################
}
//##################################################################
//##################################################################
class tabArrowButton extends ArrowButton {
//##################################################################

public tabArrowButton(int style)
{
	super(style);
	modify(NoFocus,0);
}

public void doPaint(Graphics g,Rect area)
{
	int flags = getModifiers(true);
	if (((flags & Invisible) == 0)) super.doPaint(g,area);
	//Rect r = getRect(Rect.buff);
	//g.setColor(Color.DarkGray);
	//g.drawLine(0,r.height-1,r.width,r.height-1);
}

//##################################################################
}
//##################################################################

