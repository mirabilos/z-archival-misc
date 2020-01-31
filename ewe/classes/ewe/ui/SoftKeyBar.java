/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.IImage;
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.fx.ImageCache;
import ewe.util.EventDispatcher;
import ewe.util.Vector;
import ewe.util.WeakSet;
import ewe.util.mString;
/**

A SoftKeyBar is used on Smartphone or other such systems where general purpose
hardware keys are used to control inputs to the software in a way that changes
depending on the situation.<p>

SoftKeyBars provide two types of controls - buttons which simply fire an action when the associated
softkey is pressed, or menus which display a general Menu when pressed.<p>

The SoftKeyBar is not a UI Control since it may actually be implemented using the system's native
UI system - but it is fully responsible for configuring the appearance on screen and handling the
softkey presses. The only thing an application has to do is to trap the events sent out by
the SoftKeyBar or override the event handling of the SoftKeyBar.<p>

Because only one SoftKeyBar can be displayed at a time, you display it by calling Window.setSoftKeyBar()
on the containing Window.
**/
//##################################################################
public class SoftKeyBar implements EventListener{
//##################################################################
/**
You can use this as an identifier for bar.
**/
public String name;

/**
The default background color for the soft keys. Defaults to MediumBlue
**/
public static Color defaultBackgroundColor = Color.MediumBlue;

/**
This is false by default and if you set it true, then a hide() will cause
the SoftKeyBar to disappear entirely, instead of just becoming empty - which
is the default.
**/
public static boolean hideRemovesSoftKeyBar = false;
private Vector keys = new Vector();

private EventDispatcher dispatcher;

public int barModifiersToSet = Control.DrawFlat;
public int barModifiersToClear = 0;

public final static SoftKeyBar empty = new SoftKeyBar();

//===================================================================
public SoftKeyBar()
//===================================================================
{

}
//===================================================================
public SoftKeyBar(EventListener listener)
//===================================================================
{
	this();
	addListener(listener);
}
//===================================================================
public void addListener(EventListener el)
//===================================================================
{
	if (dispatcher == null) dispatcher = new EventDispatcher();
	dispatcher.dontUseSeparateThread = true;
	dispatcher.addListener(el,true);
}
//===================================================================
public void removeListener(EventListener el)
//===================================================================
{
	dispatcher.removeListener(el);
	if (dispatcher.isEmpty()) dispatcher = null;
}

//-------------------------------------------------------------------
protected void sendToListeners(Event ev)
//-------------------------------------------------------------------
{
	if (dispatcher == null) return;
	dispatcher.dispatch(ev);
}
//===================================================================
public void clearKeys()
//===================================================================
{
	keys.clear();
}
/**
This is the default font.
**/
public static Font defaultFont;

private Font font;
/**
 * Set the Font to be used by the bar if possible.
 * @param f the Font to use if this is allowed by the system. If setting of the Font is not allowed,
 * then this method will have no effect.
 */
//===================================================================
public void setFont(Font f)
//===================================================================
{
	font = f;
}
/** This is the value 1 for all Smartphones with two soft keys. **/
//public static int LEFT_KEY = 0x1;
/** This is the value 2, for convenient use on Smartphones with two soft keys. **/
//public static int RIGHT_KEY = 0x2;


/**
 * Returns true if the softkey displays on screen supports the display of images, false if not.
 */
//===================================================================
public static boolean supportsImages()
//===================================================================
{
	return true;
}
/**
Returns the number of keys supported by the system.
**/
//===================================================================
public static int numberOfKeys()
//===================================================================
{
	return 2; //FIXME
}

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	SoftKeyEvent se = null;
	if (ev instanceof MenuEvent && ev.type == MenuEvent.SELECTED){
		se = new SoftKeyEvent();
		Object got = ((MenuEvent)ev).selectedItem;
		if (got instanceof MenuItem){
			se.selectedItem = ((MenuItem)got);
			se.action = se.selectedItem.action;
			se.proxy = se.selectedItem.data instanceof Control ? (Control)se.selectedItem.data : null;
		}else if (got != null) se.action = got.toString();
	}else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
		se = new SoftKeyEvent();
		se.action = ((ControlEvent)ev).action;
		se.proxy = ev.target instanceof Control ? ((Control)ev.target).promptControl : null;
	}
	if (se != null) {
		se.bar = this;
		for (int i = 0; i<keys.size(); i++){
			mButton mb = (mButton)keys.get(i);
			if (mb == ev.target) {
				se.whichKey = i+1;
				se.target = ev.target;
			}
		}
		sendToListeners(se);
	}
}
//===================================================================
public FontMetrics getFontMetrics()
//===================================================================
{
	if (font == null) font = defaultFont;
	if (font == null) return mApp.mainApp.getFontMetrics();
	else return mApp.mainApp.getFontMetrics(font);
}

//-------------------------------------------------------------------
private mButton setKey(int which, String text, IImage icon, IImage image, Menu menu) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (text == null) text = "";
	String action = null;
	int w = text.indexOf('|');
	if (w != -1) {
		action = text.substring(w+1);
		text = text.substring(0,w);
	}
	menu = fixMenu(menu);
	mButton mb = menu == null ? new mButton(text) : new ButtonPullDownMenu(text,menu);
	mb.arrowDirection = 0;
	mb.modify(0,mb.MakeMenuAtLeastAsWide);
	if (supportsImages()){
		if (image != null)
			Gui.iconize(mb,image,false,getFontMetrics());
		else if (icon != null)
			Gui.iconize(mb,icon,true,getFontMetrics());
	}else if (image != null)
		throw new IllegalArgumentException();
	//	mb.image = new IconAndText(icon,text,getFontMetrics());
	mb.action = action != null ? action : text;
	while(which > keys.size()) keys.add(null);
	keys.set(which-1,mb);
	mb.addListener(this);
	mb.modify(mb.NoFocus,mb.TakesKeyFocus);
	if (menu != null) mb.menuState.autoSelectFirst = true;
	return mb;
}
//===================================================================
public MenuItem createMenuItem(String label, String action, String iconName, Object maskOrColor)
//===================================================================
{
	return createMenuItem(label,action,ImageCache.cache.get(iconName,maskOrColor));
}
//===================================================================
public MenuItem createMenuItem(String label, String action, IImage icon)
//===================================================================
{
	if (action == null) action = label;
	MenuItem mi = new MenuItem(label);
	mi.action = action;
	mi.label = label;
	if (icon != null) {
		mi.image = new IconAndText(icon,label,getFontMetrics());
		((IconAndText)mi.image).textColor = null;
	}
	return mi;
}
/**
Return a MenuItem that will be used as a proxy for a specific buton. When
the menu item is selected via the soft key bar, the specified button will fire of an action event (ControlEvent.PRESSED)
**/
//===================================================================
public MenuItem createMenuItem(mButton button)
//===================================================================
{
	MenuItem mi = createMenuItem(button.text, button.action, button.image);
	mi.data = button;
	return mi;
}
/**
Set one of the SoftKeyBar keys to be a proxy for an existing mButton. Pressing that
soft key will cause the specified button to fire of an action event (ControlEvent.PRESSED)
* @param which the key to set, either 1 (left) or 2 (right.
* @param button the button that will be activated by the Soft Key.
* @return this SoftKeyBar
*/
//===================================================================
public SoftKeyBar setKey(int which, mButton button)
//===================================================================
{
	mButton mb = setKey(which,button.text,button.image,(Image)null,null);
	mb.textPosition = button.textPosition;
	mb.promptControl = button;
	mb.action = button.action == null ? mb.text : button.action;
	return this;
}
/**
Create a MenuItem for a particular Object.
@param obj - this can be a String or an mButton, each of which creates a normal MenuItem,
or it can be a Menu or Vector or Object[] or a Container, in which case a MenuItem that has
a sub-menu will be created.
@return the MenuItem created.
*/
//===================================================================
public MenuItem createMenuItemFor(Object obj) throws IllegalArgumentException
//===================================================================
{
	if (obj instanceof String){
		String label = mString.leftOf(obj.toString(),'|');
		String action = mString.rightOf(obj.toString(),'|');
		if (action.length() == 0) action = label;
		return createMenuItem(label,action,null);
	}else if (obj instanceof mButton){
		return createMenuItem((mButton)obj);
	}else if (obj instanceof Menu || obj instanceof Vector || obj instanceof Object[] || obj instanceof Container){
		Menu m = createMenuFor(obj);
		MenuItem mi = new MenuItem();
		mi.subMenu = m;
		mi.label = m.text;
		return mi;
	}else if (obj instanceof MenuItem){
		return (MenuItem)obj;
	}else throw new IllegalArgumentException();
}
/**
Create a Menu for a specific set of Objects, placed in a Vector, or a Container or an Object
array.
Each item in the Vector can be an mButton or a String or a MenuItem or another Vector
or Menu.
**/
//===================================================================
public Menu createMenuFor(Object obj)
throws IllegalArgumentException
//===================================================================
{
	Menu m = new Menu();
	Vector v = null;
	if (obj instanceof Object[]) v = new Vector((Object[])obj);
	else if (obj instanceof Vector) v = (Vector)obj;
	else if (obj instanceof Container) v = Form.gatherButtons((Container)obj,null);
	else if (obj instanceof Menu) return fixMenu((Menu)obj);
	else throw new IllegalArgumentException();
	for (int i = 0; i<v.size(); i++)
		m.addItem(createMenuItemFor(v.get(i)));
	return m;
}


/**
 * Set one of the SoftKeys to be a button or a Menu, depending on the type of the obj
 * parameter.
* @param which the key to set, either 1 (left) or 2 (right.
 * @param obj this can be either a String or mButton (either of which creates a single
	button for the Soft Key) or a Vector or Object[] or Container or Menu - in which
	case a Menu will be created for the Soft Key.
 * @param label if the obj parameter gets converted to a Menu, then this parameter will
	be the label for the menu.
 * @return this SoftKeyBar
 * @exception IllegalArgumentException
 */
//===================================================================
public SoftKeyBar setKey(int which, Object obj, String label) throws IllegalArgumentException
//===================================================================
{
	if (obj instanceof String) setKey(which,obj.toString(),(IImage)null,(IImage)null,null);
	else if (obj instanceof mButton) setKey(which,(mButton)obj);
	else if (obj instanceof Menu || obj instanceof Vector || obj instanceof Container || obj instanceof Object[]){
		Menu m = createMenuFor(obj);
		setKey(which,label,(IImage)null,(IImage)null,m);
	}
	return this;
}
/**
 * @param which The key to set, starting from 1.
 * @param image The image to use for the key.
 * @param menu An optional menu to display when the key is pressed. If this is null the key
 * will just be treated as a button.
 * @exception IllegalArgumentException if the system does not support displaying images on the
 * soft keys.
 */
//===================================================================
public SoftKeyBar setKey(int which, IImage image, Menu menu) throws IllegalArgumentException
//===================================================================
{
	setKey(which,"",(IImage)null,image,menu);
	return this;
}
/**
 * @param which The key to set, starting from 1.
 * @param text The text for the key. If this is in the form "Text_1|Text_2" then "Text_1" is used
 * as the text for the key and "Text_2" is used as the action for the key.
 * @param icon An image to display as an icon next to the text if possible. If this is not possible
 * then the icon is ignored.
 * @param menu An optional menu to display when the key is pressed. If this is null the key
 * will just be treated as a button.
 * @exception IllegalArgumentException if the system does not support displaying images on the
 * soft keys.
 */
//===================================================================
public SoftKeyBar setKey(int which, String text, IImage icon, Menu menu) throws IllegalArgumentException
//===================================================================
{
	setKey(which,text,icon,null,menu);
	return this;
}
/**
 * @param which The key to set, starting from 1.
 * @param text The text for the key. If this is in the form "Text_1|Text_2" then "Text_1" is used
 * as the text for the key and "Text_2" is used as the action for the key.
 * @param menu An optional menu to display when the key is pressed. If this is null the key
 * will just be treated as a button with the Text and action for the button being the same.
 * @exception IllegalArgumentException if the system does not support displaying images on the
 * soft keys.
 */
//===================================================================
public SoftKeyBar setKey(int which, String text, Menu menu)
//===================================================================
{
	setKey(which,text,(IImage)null,null,menu);
	return this;
}
/**
 * @param which The key to set, starting from 1.
 * @param text The text for the key. If this is in the form "Text_1|Text_2" then "Text_1" is used
 * as the text for the key and "Text_2" is used as the action for the key.
 * @param iconName The name of the saved image.
 * @param maskOrColor A name of a mask image or a color mask for the icon, or null.
 * @param menu An optional menu to display when the key is pressed. If this is null the key
 * will just be treated as a button.
 * @exception IllegalArgumentException if the system does not support displaying images on the
 * soft keys.
 */
//===================================================================
public SoftKeyBar setKey(int which, String text, String iconName, Object maskOrColor, Menu menu)
//===================================================================
{
	setKey(which,text,ImageCache.cache.get(iconName,maskOrColor),null,menu);
	return this;
}
/**
 * Set a particular softkey to be a Menu, specifying the menu as a String.
 * @param which The key to set, starting from 1.
 * @param name The name of the menu.
 * @param icon An optional icon for the menu.
 * @param items The items for the menu. This should be specified either as a
 * comma separted list of item names (e.g. "Item 1,Item 2,Item 3") or as a '|' separated list of
 * item name and action pairs (e.g. "Item 1|ACTION_1|Item 2|ACTION_2|Item 3|ACTION_3").
 */
//===================================================================
public SoftKeyBar setMenu(int which, String name, IImage icon, String items)
//===================================================================
{
	Menu m = new Menu();
	if (items.indexOf('|') == -1){
		String[] all = mString.split(items,',');
		for (int i = 0; i<all.length; i++)
			m.addItem(all[i]);
	}else{
		String[] all = mString.split(items,'|');
		for (int i = 0; i<all.length; i+=2){
			MenuItem mi = new MenuItem(all[i]);
			mi.action = all[i+1];
			m.addItem(mi);
		}
	}
	return setKey(which,name,icon,m);
}

/**
 * Set a particular softkey to be a Menu, specifying the menu as a String.
 * @param which The key to set, starting from 1.
 * @param name The name of the menu.
 * @param iconName The name of the saved image.
 * @param maskOrColor A name of a mask image or a color mask for the icon, or null.
 * @param items The items for the menu. This should be specified either as a
 * comma separted list of item names (e.g. "Item 1,Item 2,Item 3") or as a '|' separated list of
 * item name and action pairs (e.g. "Item 1|ACTION_1|Item 2|ACTION_2|Item 3|ACTION_3").
 */
//===================================================================
public SoftKeyBar setMenu(int which, String name, String iconName, Object maskOrColor, String items)
//===================================================================
{
	return setMenu(which,name,ImageCache.cache.get(iconName,maskOrColor),items);
}


//-------------------------------------------------------------------
protected Control make()
//-------------------------------------------------------------------
{
	CellPanel cp = new CellPanel();
	cp.setPreferredSize(-1,20);
	cp.setFont(getFontMetrics().getFont());
	//if (keys.size() == 0) return cp;
	cp.equalWidths = true;
	int num = numberOfKeys();
	Color bc = Color.Black;
	for (int i = 0; i<num; i++){
		Control ta = i > keys.size()-1 ? null : (Control)keys.get(i);
		if (ta == null) ta = new mLabel(" ");
		ta.borderColor = bc;
		cp.addNext(ta);
		if (i == 0) ta.setBorder(ta.BDR_OUTLINE|ta.BF_RIGHT|ta.BF_EXACT,1);
		else ta.setBorder(ta.BDR_NOBORDER|ta.BF_EXACT,0);
	}
	cp.endRow();
	cp.modifyAll(cp.NoFocus|barModifiersToSet,barModifiersToClear);
	cp.backGround = defaultBackgroundColor;
	cp.setBorder(cp.BDR_OUTLINE|cp.BF_RECT,1);
	cp.borderColor = bc;
	return cp;
}
//-------------------------------------------------------------------
private boolean handleKey(int which, KeyEvent ev)
//-------------------------------------------------------------------
{
	if (keys.size() >= which){
		Control c = (Control)keys.get(which-1);
		c.doAction(c.ByKeyboard);
		if (!c.menuIsActive()) c.notifyAction();
		return true;
	}
	return false;
}
//===================================================================
public boolean handleKey(KeyEvent ev)
//===================================================================
{
	if (ev.type == ev.KEY_RELEASE) return true;
	for (int i = 0; i<keys.size(); i++){
		Control c = (Control)keys.get(i);
		if (c.menuIsActive()) {
			c.closeMenu();
			return true;
		}
	}
	if (ev.key == IKeys.SOFTKEY1) return handleKey(1,ev);
	else if (ev.key == IKeys.SOFTKEY2) return handleKey(2,ev);
	else return false;
}
//===================================================================
public static boolean onKeyEvent(KeyEvent ev)
//===================================================================
{
	SoftKeyBar sk = getDisplayed();
	if (sk == null) return false;
	return sk.handleKey(ev);
}
//===================================================================
public void addTo(SingleContainer sc)
//===================================================================
{
	Control c = make();
	boolean changed = false;
	if (c == null) {
		changed = !sc.hasModifier(sc.ShrinkToNothing,false);
		sc.modify(sc.ShrinkToNothing,0);
		sc.setControl(new mLabel(" "));
	}else{
		changed = sc.hasModifier(sc.ShrinkToNothing,false);
		sc.modify(0,sc.ShrinkToNothing);
		sc.setControl(c);
	}
	if (changed){
		Control p = sc.getParent();
		if (p instanceof Panel){
			((Panel)p).relayoutMe(true);
		}
	}else
		sc.repaintNow();
	//if (p != null) p.redisplay();
}
/**
This returns true if SoftKeys are under this platform.
**/
//===================================================================
public static boolean usingSoftKeys()
//===================================================================
{
	return barContainer != null;
}
private static SingleContainer barContainer;
/**
* Setup the global location for the SoftKeyBar.
**/
//===================================================================
public static void setupIn(Panel c)
//===================================================================
{
	Menu.defaultBackground = Color.LightBlue;
	Menu.use3DPopup = false;
	c.addNext(barContainer = new SingleContainer());
	empty.addTo(barContainer);
}
private static SoftKeyBar current;
/**
Display the SoftKeyBar in the global SoftKeyBar location.
**/
//===================================================================
public boolean display()
//===================================================================
{
	if (barContainer == null) return false;
 	addTo(barContainer);
	current = this;
	return true;
}
//===================================================================
public static SoftKeyBar getDisplayed()
//===================================================================
{
	if (barContainer == null) return null;
	if (barContainer.hasModifier(Control.ShrinkToNothing,false)) return null;
	return current;
}
/**
This is called by the GUI system when running on a Smartphone. It turns off
multiple windows and sets up a special area for the SoftKeyBar.
**/
//===================================================================
public static void setupScreen()
//===================================================================
{
	defaultFont = mApp.findFont("gui",true);
	defaultFont = defaultFont.changeStyle(Font.BOLD);
	/*
	final Form f = new Form();
	f.hasTopBar = false;
	f.windowFlagsToSet |= Window.FLAG_MAXIMIZE;
	Frame showFrame = new AppFrame();
	showFrame.setPreferredSize(100,100);
	f.addLast(showFrame);
	Frame ac = new Frame(){
		public void relayoutMe(boolean reDisp){
			super.relayoutMe(reDisp);
			f.relayoutMe(reDisp);
		}
	};
	ac.contentsOnly = true;
	ac.isControlPanel = true;
	ac.modify(ac.NoFocus,0);
	f.addLast(ac).setCell(f.HSTRETCH);
	SoftKeyBar.setupIn(ac);
	f.show();
	Gui.setAllParentFrame(showFrame);
	f.waitUntilPainted(1000);
	*/
	if (!Window.supportsMultiple()){
		final Form f = new Form(){
			protected void setupFrame(FormFrame ff,int options){
				ff.contentsOnly = true;
				//f.hasTopBar = false;
				ff.borderWidth = 0;
			}
			public void onEvent(Event ev){
				//super.onEvent(ev);
			}
		};
		f.hasTopBar = false;
		f.exitButtonDefined = true;
		f.windowFlagsToSet |= Window.FLAG_MAXIMIZE;
		Frame showFrame = new AppFrame();
		showFrame.setPreferredSize(100,100);
		f.addLast(showFrame);
		Frame ac = new Frame(){
			public void relayoutMe(boolean reDisp){
				super.relayoutMe(reDisp);
				f.relayoutMe(reDisp);
			}
		};
		ac.contentsOnly = true;
		ac.isControlPanel = true;
		ac.modify(ac.NoFocus,0);
		f.addLast(ac).setCell(f.HSTRETCH);
		SoftKeyBar.setupIn(ac);
		f.show();
		Gui.setAllParentFrame(showFrame);
		f.waitUntilPainted(1000);
	}
}
/**
Hide any visible SoftKeyBar.
**/
//===================================================================
public static void hide()
//===================================================================
{
	if (!hideRemovesSoftKeyBar) empty.display();
	else{
		//new Exception().printStackTrace();
		if (barContainer == null) return;
		barContainer.modify(Control.ShrinkToNothing,0);
		Control p = barContainer.getParent();
		if (p instanceof Panel)((Panel)p).relayoutMe(true);
		//if (p != null) p.redisplay();
	}
}

/**
Reveal a hidden SoftKeyBar.
**/
//===================================================================
public static boolean reveal()
//===================================================================
{
	if (barContainer == null) return false;
	barContainer.modify(0,Control.ShrinkToNothing);
	Control p = barContainer.getParent();
	if (p instanceof Panel)((Panel)p).relayoutMe(true);
	return true;
}

private static Vector keybars;
/**
Display a new keybar, but save the old keybar.
**/
//===================================================================
public static void push(SoftKeyBar skb)
//===================================================================
{
	keybars = Vector.push(keybars,getDisplayed());
	if (skb == null) hide();
	else skb.display();
}
/**
Display the keybar that was displayed before the last push() call.
**/
//===================================================================
public static void pop()
//===================================================================
{
	SoftKeyBar got = (SoftKeyBar)Vector.pop(keybars);
	if (got == null) hide();
	else got.display();
}

private static WeakSet fixedMenus;
/**
Fix a Menu for use with the SoftKeyBar. This will ensure that MenuItem objects are used
instead of Strings within the Menu and that numbers are prefixed to the labels.
**/
//===================================================================
public static Menu fixMenu(Menu m)
//===================================================================
{
	if (m == null) return m;
	if (fixedMenus == null) fixedMenus = new WeakSet();
	if (fixedMenus.contains(m)) return m;
	if (Gui.isSmartPhone)
		for (int i = 0, k = 0; i<m.items.size(); i++){
			Object obj = m.items.get(i);
			MenuItem mi = obj instanceof MenuItem ? (MenuItem)obj : new MenuItem(obj.toString());
			if (mi.action == null) mi.action = mi.label;
			if (k <= 11 && !mi.label.equals("-") && ((mi.modifiers & mi.Separator) == 0)){
				k++;
				char ky = (char)('0'+k);
				if (k == 10) ky = '0';
				else if (k == 11) ky = '*';
				else if (k == 12) ky = '#';
				mi.label = ky+" "+mi.label;
				mi.hotkey = KeyEvent.toKey(0,ky);
				if (mi.image instanceof IconAndText){
					((IconAndText)mi.image).set(((IconAndText)mi.image).icon,mi.label);
				}
			}
			m.items.set(i,mi);
		}
	fixedMenus.add(m);
	return m;
}
//##################################################################
}
//##################################################################


