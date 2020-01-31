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
//##################################################################
public class mChoice extends ChoiceControl implements EventListener, PopupController{
//##################################################################
{
	useMenuItems = false;
	displayRows = 1;
	spacing = 3;
	blockSelected = false;
	dropDownButton = true;
	borderStyle = mInput.inputEdge;//Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE;
	modify(TakesKeyFocus,0);
	isSingleLine = true;
	//defaultAddMeCellConstraints = mPanel.DontStretch;
	//defaultAddMeControlConstraints = mPanel.DontFill;
	//defaultAddMeAnchor = mPanel.West;
}

/**
* This is the number of items that must be in the Menu in order for it to use ScrollBars instead
* of the up/down buttons. A value of -1 (the default) will tell it to use the value of
* Menu.defaultUseScrollBarSize, a value of 0 will tell it to never use ScrollBars, and a value
* above 0 specifies the number of items for ScrollBar usage.
**/
public int useScrollBarSize = -1;
/**
* If this is true then when you press a key, each item will not be searched to see if the key
* relates to that item. By default this is false.
**/
public boolean dontSearchForKeys = false;

protected boolean mustAlwaysDrop = false;
/**
* This value should be one of the following:<br>
* 0 = Default size (approx 3 times the size of the mChoice)<br>
* <0 = Full menu size. <br>
* >0 = A preferred and maximum number items.<br>
**/
public int dropMenuRows = -1;
/**
* If you set this true, the mChoice will always display a drop menu when you click on it
* outside of the drop button area, instead of cycling through the choices as it does
* by default.
**/
public boolean alwaysDrop = false;
/**
* If this is true only the drop button will be displayed. This is useful for combining
* an mChoice with other controls.
**/
public boolean dropButtonOnly = false;
/**
* The Font for the menu. If this is null it will default to the Font of the mChoice.
**/
public Font menuFont;

/**
If this is true, then the Left and Right cursor keys will not change
the selection.
**/
public boolean dontAllowKeyChangeChoice = false;

/**
 * Returns the FontMetrics for the font that the dropdown menu will use.
 */
//===================================================================
public FontMetrics getMenuFontMetrics()
//===================================================================
{
	return menuFont == null ? getFontMetrics() : getFontMetrics(menuFont);
}
public int dropButtonBorder = ButtonObject.buttonEdge/*standardEdge*/ | (((mInput.inputEdge & BDR_OUTLINE) != 0) ? BDR_OUTLINE : 0);//Graphics.EDGE_RAISED|Graphics.BDR_OUTLINE;

/**
* A menu option - see menuOptions. This indicates that the drop-down menu should be the full
* width of the menu, if it is larger than the area actually allocated to the choice.
**/
public static final int MENU_FULL_WIDTH = 0x1;
/**
* A menu option - see menuOptions. This indicates that the drop-down menu should be the full
* width of the screen on PDA or mobile phone devices.
**/
public static final int MENU_WINDOW_WIDTH_ON_PDA = 0x2;
/**
* A menu option - see menuOptions. This indicates that if the menu is expanded past the size
* of the mChoice, then the text returned by getPromptText() should be displayed as a title
* at the top of the menu.
**/
public static final int MENU_SHOW_TITLE_IF_EXPANDED = 0x4;
/**
* A menu option - see menuOptions. This indicates that
* the text returned by getPromptText() should always be displayed as a title
* at the top of the menu.
**/
public static final int MENU_SHOW_TITLE_ALWAYS = 0x8;
/**
* This should be a combination of the MENU_XXX values.
**/
public int menuOptions;

//==================================================================
public mChoice(){}
public mChoice(String what,int initSel){this(mString.split(what),initSel);}
public mChoice(String [] choices,int value) {set(choices,value);}
public void set(String [] choices,int value) {items.clear(); items.addAll(choices); selectedIndex = value;}
//==================================================================


protected Menu menu = null;
protected CarrierFrame menuFrame = null;


/**
 * Create an empty Menu object to use as the drop menu. By default this simply creates
 * and returns a new Menu.
 */
//-------------------------------------------------------------------
protected Menu getNewMenu()
//-------------------------------------------------------------------
{
	return new Menu();
}
/**
* This creates the Menu for the mChoice. You can override this, call super.createMenu() and
* then modify the returned menu as you wish before returning it.
**/
//------------------------------------------------------------------
protected Menu createMenu()
//------------------------------------------------------------------
{
	if (items.size() == 0) return null;
	Menu m = menu = getNewMenu();
	m.useScrollBarSize = useScrollBarSize;
	m.shortenItems = shortenItems;
	m.followPen = false;
	m.indentDropItems = indentDropItems;
	m.addListener(this);
	m.selectedIndex = selectedIndex;
	m.items = items;
	//m.centerSelected();
	menu.borderWidth = 0;
	menu.displayRows = items.size();
	menu.inheritModifiers(this,true,AlwaysEnabled|NotAnEditor|KeepImage,true);
	menu.font = menuFont != null ? menuFont : getFont();
	return m;
}

private boolean wasExpanded;
private String menuTitle;
/**
 * Get the Rect, relative to the parent Frame, that the Menu should be fit into.
 */
//------------------------------------------------------------------
protected Rect getDropMenuRect()
//------------------------------------------------------------------
{
	Control c = this;
	if (container != null) c = container;
	Rect r = c.getDim(null);
	Point myLoc = Gui.getPosInParent(c,getFrame());
	Rect ret = null;
	if (dropMenuRows != 0) {
		Dimension f = getFrame().getSize(null);
		ret = new Rect(myLoc.x+2,myLoc.y+2,r.width-2,f.height);
	}else
		ret = new Rect(myLoc.x+2,myLoc.y+2,r.width-2,r.height*4);
	int w = ret.width;
	int originalW = w;
	if (((menuOptions & MENU_WINDOW_WIDTH_ON_PDA) != 0) && Gui.screenIs(Gui.PDA_SCREEN)){
		Window win = getWindow();
		if (win == null)
			w = menu.getMenuWidth(false,true)+20;
		else
			w = win.getSize(null).width;
	}
	if (w > ret.width) ret.width = w;
	if ((menuOptions & MENU_FULL_WIDTH) != 0){
		w = menu.getMenuWidth(false,true)+20;
	}
	if (w > ret.width) ret.width = w;
	wasExpanded = (ret.width != originalW);
	String title = null;
	if (((menuOptions & MENU_SHOW_TITLE_ALWAYS) != 0) ||
		(wasExpanded && ((menuOptions & MENU_SHOW_TITLE_IF_EXPANDED) != 0))
		)
			title = getPromptText();
	if (title != null){
		FontMetrics fm = getFontMetrics(getMenuFontMetrics().getFont().changeStyle(Font.BOLD));
		w = fm.getTextWidth(title)+8;
	}
	if (w > ret.width) ret.width = w;
	wasExpanded = (ret.width != originalW);
	return ret;
}
//------------------------------------------------------------------
public void calculateSizes()
//------------------------------------------------------------------
{
	super.calculateSizes();
	if (dropButtonOnly) preferredWidth = preferredHeight-spacing-2;
}
private final ewe.sys.Lock dropLock = new ewe.sys.Lock();
//------------------------------------------------------------------
protected void doDropMenu()
//------------------------------------------------------------------
{
	if (!dropLock.grab()) return;
	try{
		if (menu != null) return;
		if (createMenu() == null) {
			Sound.beep();
			return;
		}
		Frame p = getFrame();
		Rect myLoc = getDropMenuRect();
		int w = myLoc.width; if (!dropDownButton) w += 10;
		menu.noWrapAround = true;
		menu.popupController = this;
		menu.displayRows = dropMenuRows;
		String title = null;
		if (((menuOptions & MENU_SHOW_TITLE_ALWAYS) != 0) ||
			(wasExpanded && ((menuOptions & MENU_SHOW_TITLE_IF_EXPANDED) != 0))
			)
				title = getPromptText();
		menuFrame = menu.getCarrierFrame(this,w,myLoc.height,false,true,p,title);
		//ewe.sys.Vm.debug(w+", "+myLoc.height+" - "+menuFrame.getRect(null));
		menuFrame.putInFrame(myLoc.x,myLoc.y);
		int was = menu.modify(Invisible,0);
		if (menu.selectedIndex != -1)
			menu.makeItemVisible(menu.selectedIndex);
		menuFrame.addListener(this);
		Gui.takeFocus(menu,ByRequest);
		menu.restore(was,Invisible);
		menuFrame.repaintNow();
	}finally{
		dropLock.release();
	}
}
//===================================================================
public void closePopup(int why, int flags) {noMenu(); postEvent(new ControlEvent(ControlEvent.POPUP_CLOSED,this));}
//===================================================================

//------------------------------------------------------------------
protected void noMenu()
//------------------------------------------------------------------
{
	//int last = modify(Invisible,0);
	Gui.hideFrame(menuFrame);
	//restore(last,Invisible);
	menu = null;
	menuFrame = null;
	repaintNow();
}
//------------------------------------------------------------------
public void onEvent(Event ev)
//------------------------------------------------------------------
{
	//System.out.println("Chosen! "+ev);
	if (ev instanceof MenuEvent && (ev.type == MenuEvent.SELECTED || ev.type == MenuEvent.ABORTED) && ev.target == menu){
		menu.modify(Invisible,0);
		if (ev.type != MenuEvent.ABORTED) selectedIndex = menu.selectedIndex;
		noMenu();
		if (ev.type != MenuEvent.ABORTED) notifyAction();
		postEvent(new ControlEvent(ControlEvent.POPUP_CLOSED,this));
	}else if (ev.type == FrameEvent.PRESSED_OUTSIDE && (ev instanceof FrameEvent)){
		noMenu();
		postEvent(new ControlEvent(ControlEvent.POPUP_CLOSED,this));
	}else if (ev.target != menu)
		super.onEvent(ev);
}
//==================================================================
public int getDisplayRows() {return 1;}
//==================================================================
public void doAction(int how) {
	if ((getModifiers(true) & (DisplayOnly|NotEditable)) != 0) return;
	doDropMenu();
}
//==================================================================
public void penReleased(Point ev)
//==================================================================
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)) || !isOnMe(ev)) return;
	if (menu != null) return;
	if (items.size() == 0) return;
	selectedIndex++;
	if (selectedIndex >= items.size()) selectedIndex = 0;
	repaintNow();
	notifyAction();
}
public int dropX;

//===================================================================
public boolean willShowFrame(PenEvent ev)
//===================================================================
{
	if (ev.type == PenEvent.PEN_DOWN)
		if ((ev.x >= dropX && dropDownButton) || alwaysDrop || mustAlwaysDrop)
			return true;
	return false;
}
//==================================================================
public void penPressed(Point ev)
//==================================================================
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;
	Menu menu = null;
	if ((ev.x >= dropX && dropDownButton) || alwaysDrop || mustAlwaysDrop) {
		doDropMenu();
		doDropMenu();
	}else
		super.penPressed(ev);
}
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	centerSelected();
	super.doPaint(g,area);
	if (hasModifier(PaintDataOnly,false)) return;
	Color c;
	Rect r = getDim(null);
	boolean flat = ((flags & DrawFlat) != 0);
	g.setDrawOp(g.DRAW_OVER);
	//g.draw3DButton(r,true,null,flat,true);
	g.draw3DRect(
		getDim(r),
		//Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
		borderStyle,
		flat,
		null,
		Color.DarkGray);

	if (dropDownButton) {
		if (!dropButtonOnly){
			int th = r.height-spacing*2-4;
			int tw = th;
			r.x = r.width-tw-spacing;
			dropX = r.x-2;
			r.y = (r.height-th)/2;
			//g.setColor(getBackground());
			//g.fillRect(r.x-2,2,r.width-(r.x-2),r.height-3);
			//g.setColor(Color.DarkGray);
			//mGraphics.drawRect(g,r.x-2,2,r.width-(r.x-2),r.height-3);
			//g.draw3DButton(new Rect(r.x-2,1,r.width-(r.x-2),r.height-2),false,getBackground(),flat,true);
				if (globalPalmStyle)
					g.draw3DRect(
						new Rect(r.x-2,2,r.width-(r.x-2),r.height-2),
						BF_SOFT|BF_TOP|BF_LEFT|BF_RIGHT,
						true,
						Color.Black,
						Color.Black);
				else
					g.draw3DRect(
						(dropButtonBorder & BDR_OUTLINE) != 0 ?
						new Rect(r.x-2,1,r.width-(r.x-2),r.height-2):
						new Rect(r.x-2,0,r.width-(r.x-2),r.height),
						dropButtonBorder,
						(flags & DrawFlat) != 0,
						getBackground(),
						Color.Black);
			//if (!enabled(this) || !editable(this)) c = Color.DarkGray;
			r.x+=2; r.y++;
			r.height = th-1;
			r.width = tw-2;
			c = globalPalmStyle ? Color.White : getForeground();
			g.setColor(((flags & Disabled) != 0) ? Color.DarkGray : c);
			//g.drawVerticalTriangle(r,false);
		}else{
			dropX = 0;
			if (globalPalmStyle)
				g.draw3DRect(
					new Rect(0,0,r.width,r.height),
					BF_SOFT|BF_TOP|BF_LEFT|BF_RIGHT,
					true,
					Color.Black,
					Color.Black);
			else
				g.draw3DRect(
					(dropButtonBorder & BDR_OUTLINE) != 0 ?
					new Rect(0,1,r.width,r.height-2):
					new Rect(0,0,r.width,r.height),
					dropButtonBorder,
					(flags & DrawFlat) != 0,
					getBackground(),
					Color.Black);
			r.x = 3;  r.width -= 6;
			r.y = (r.height-r.width)/2;
			r.height = r.width;

			c = globalPalmStyle ? Color.White : getForeground();
			g.setColor(((flags & Disabled) != 0) ? Color.DarkGray : c);
			//g.drawVerticalTriangle(r,false);
		}
		if (Gui.hasPen || dontAllowKeyChangeChoice){
			g.drawVerticalTriangle(r,false);
		}else{
			r.width /= 2;
			r.width -= 2;
			g.drawHorizontalTriangle(r,true);
			r.x += r.width+2;
			g.drawHorizontalTriangle(r,false);
		}
	}
}
//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	if (ev.type != ev.KEY_PRESS) super.onKeyEvent(ev);
	else{
		if (ev.key == IKeys.MENU){
			doActionKey(ev.key);
		}else if (ev.key == IKeys.ENTER){
			if (isSomeonesHotKey(ev))
				return;
			else
				super.onKeyEvent(ev);
		}else if (ev.key == IKeys.RIGHT && !dontAllowKeyChangeChoice){
			selectNext();
			notifySelection(selectedIndex,ListEvent.SELECTED);
		}else if (ev.key == IKeys.LEFT && !dontAllowKeyChangeChoice){
			selectPrev();
			notifySelection(selectedIndex,ListEvent.SELECTED);
		}else{
			int sz = itemsSize();
			if (!dontSearchForKeys){
				for (int i = 0; i<sz; i++){
					MenuItem m = getItemAt(i);
					if (ev.isHotKey(m.hotkey)){
						select(i);
						makeItemVisible(i);
						ev.consumed = true;
						notifyAction();
						return;
					}
				}
				if ((ev.modifiers & IKeys.SPECIAL) == 0){
					int ky = findKeyed(selectedIndex+1,ev.key);
					if (ky != -1){
						select(ky);
						makeItemVisible(ky);
						ev.consumed = true;
						if (isAList)
							notifySelection(selectedIndex,ListEvent.SELECTED);
						return;
					}
				}
				super.onKeyEvent(ev);
			}
		}
	}
}
//===================================================================
public void doActionKey(int key)
//===================================================================
{
	if ((getModifiers(true) & (DisplayOnly|NotEditable)) != 0) return;
	doDropMenu();
}
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	//if (how == ByKeyboard){
		//borderStyle |= BDR_DOTTED;
		blockSelected = true;
		repaintNow();
	//}
	super.gotFocus(how);
}
public void lostFocus(int how)
{
	blockSelected = false;
	repaintNow();
	/*
	if ((borderStyle & BDR_DOTTED) != 0){
		borderStyle &= ~BDR_DOTTED;
		repaintNow();
	}
	super.lostFocus(how);
	*/
}
//===================================================================
public void notifyAction()
//===================================================================
{
	super.notifyAction();
	if (notifyDataChangeOnSelect)
		super.notifyDataChange();
}

//##################################################################
}
//##################################################################


