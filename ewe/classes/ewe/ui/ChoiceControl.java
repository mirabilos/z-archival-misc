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
* This is a BaseClass for mChoice and Menu controls.
**/
//##################################################################
public class ChoiceControl extends Control implements Intable {
//##################################################################
{
	modify(HasData|WantDrag,0);
	borderWidth = 1;
}
//protected WeakCache imagesToRefresh;
protected boolean blockSelected = true;
protected boolean isAList = false;
protected boolean isSingleLine = false;
//-------------------------------------------------------------------
public boolean useMenuItems = true;
public boolean dropDownButton = false;
public boolean calculateWidth = true;
public boolean indentDropItems = false;
public boolean shortenItems = false;
public Vector items = new Vector();
public int spacing = 1, xOffset = 0;
public int selectedIndex = -1;
public boolean dontAutoScroll = false;
public Control container = null;
/**
This applies to Menus. If it is true, then wrapping from the top to the bottom using the
cursor keys will be disabled.
**/
public boolean noWrapAround = false;

public static final int INDENT_ITEM_FLAG = 0x8000;
/**
* If you set this to a value >= 0 then this will be taken to be the fixed item height.
* See getItemHeight().
**/
public int itemHeight = -1;
//-------------------------------------------------------------------
protected Rect getDataRect(Rect dest) {return Rect.unNull(dest).set(spacing,spacing,width-spacing*2,height-spacing*2);}
//-------------------------------------------------------------------

/**
* If this is null then the Foreground color will be used. By default it is set to
* Color.DarkBlue.
**/
public Color blockColor = Color.DarkBlue;

//===================================================================
public Color getBlockColor()
//===================================================================
{
	return blockColor == null ? getForeground() : blockColor;
}
//===================================================================
public void modifyItems(String [] items,int switchOn,int switchOff,boolean searchChildMenus)
//===================================================================
{
	for (int i = 0; i<items.length; i++){
		MenuItem mi = findItem(items[i],searchChildMenus);
		if (mi == null) continue;
		mi.modifiers |= switchOn;
		mi.modifiers &= ~switchOff;
	}
}
//===================================================================
public int itemsSize() {return items.size();}
//===================================================================
/**
* Find the index of this object. Returns -1 if it is not found.
*/
//==================================================================
public int indexOf(Object what)
//==================================================================
{
	if (what == null) return -1;
	String s = null;
	if (what instanceof String) s = (String)what;
	for (int i = 0; i<itemsSize(); i++) {
		if (what == items.get(i)) return i;

		if (s != null)
			if (s.equals(getItemAt(i).label)) return i;
	}
	return -1;
}
/**
* Insert an item in the control. This does not call a repaint.
*/
//==================================================================
public MenuItem insertItemAt(Object what,int index)
//==================================================================
{
	if (what == null) return null;
	int sz = itemsSize();
	if (index < 0 || index > sz) index = sz;
	items.add(index,getItemToAdd(what));
	return getItemAt(index);
}
//==================================================================
public Object getSelectedItem()
//==================================================================
{
	if (selectedIndex < 0 || selectedIndex >= itemsSize()) return null;
	return items.get(selectedIndex);
}
//-------------------------------------------------------------------
protected Object getItemToAdd(Object forWhat)
//-------------------------------------------------------------------
{
	if (forWhat == null) return null;
	if (forWhat instanceof MenuItem) return forWhat;
 	if (forWhat instanceof String){
		if (!useMenuItems) return forWhat;
		return new MenuItem((String)forWhat);
	}
	else if (forWhat instanceof Menu) {
		MenuItem mi = new MenuItem();
		mi.subMenu = (Menu)forWhat;
		mi.label = ((Menu)forWhat).text;
		return mi;
	}else if (forWhat instanceof PullDownMenu) return getItemToAdd(((PullDownMenu)forWhat).getMenu());
	forWhat = forWhat.toString();
	if (forWhat == null) forWhat = "(unknown)";

	return getItemToAdd(forWhat);
}
/**
* Insert an item in the control. This does not call a repaint - call updateItems() to do that.
*/
//==================================================================
public MenuItem addItem(Object what) {return insertItemAt(what,itemsSize());}
//==================================================================

/**
* Sets the data for the ChoiceControl. The supplied data can be a Vector
* or an array of Strings/MenuItems. This WILL call a repaint.
**/
//===================================================================
public void setData(Object data)
//===================================================================
{
	removeAll();
	if (data instanceof Vector){
		Object [] got = new Object[((Vector)data).size()];
		((Vector)data).copyInto(got);
		data = got;
	}
	if (data instanceof Object []){
		Object [] all = (Object [])data;
		for (int i = 0; i<all.length; i++)
			addItem(all[i]);
	}
	updateItems();
}
/**
* This add items but does not update the screen. Call updateItems for that.
**/
//==================================================================
public MenuItem [] addItems(String [] what)
//==================================================================
{
	MenuItem [] got = new MenuItem[what.length];
	for (int i = 0; i<what.length; i++){
		Object add = getItemToAdd(what[i]);
		items.add(add);
		if (add instanceof MenuItem) got[i] = (MenuItem)add;
		else got[i] = null;
	}
	return got;
}

/**
* Delete an item from the control. This does not call a repaint.
*/
//==================================================================
public boolean deleteItem(int index)
//==================================================================
{
	if (index == selectedIndex) selectedIndex = -1;
	items.del(index);
	return true;
}
/**
* Removes all items from the control. This does not call a repaint.
*/
//==================================================================
public boolean removeAll() {items.clear(); selectedIndex = -1; return true;}
//==================================================================
//==================================================================
public boolean trySelectItem(String label)
//==================================================================
{
	if (label != null)
	for (int i = 0; i<itemsSize(); i++)
		if (label.equals(getItemAt(i).label)) {
			selectItem(i,true);
			return true;
		}
	return false;
}
//===================================================================
public void selectNext()
//===================================================================
{
	int end = selectedIndex;
	if (end == -1) end = itemsSize()-1;
	int next = selectedIndex;
	while(true){
		next++;
		if (next >= itemsSize())
			if (isAList || noWrapAround) return;
			else next = 0;
		if (next >= itemsSize()) return;
		if ((getItemAt(next).modifiers & (MenuItem.Disabled|MenuItem.Separator)) == 0){
			select(next);
			break;
		}
		if (next == end) return;
	}
	if (isAList) notifySelection(selectedIndex,ListEvent.SELECTED);
	makeItemVisible(selectedIndex);
}
//===================================================================
public void selectPrev()
//===================================================================
{
	int end = selectedIndex;
	if (end == -1) end = 0;
	int next = selectedIndex;
	while(true){
		next--;
		if (next < 0)
			if (isAList || noWrapAround) return;
			else next = itemsSize()-1;
		if (next == -1) return;
		if ((getItemAt(next).modifiers & (MenuItem.Disabled|MenuItem.Separator)) == 0){
			select(next);
			break;
		}
		if (next == end) return;
	}
	if (isAList) notifySelection(selectedIndex,ListEvent.SELECTED);
	makeItemVisible(selectedIndex);
}

//==================================================================
public void notifySelection(int index,int type)
//==================================================================
{
	ListEvent le = new ListEvent(type,this,getItemAt(index));
	postEvent(le);
	if (notifyDataChangeOnSelect) notifyDataChange();
}

public void doActionKey(int key)
{
	if ((getModifiers(true) & (DisplayOnly|NotEditable)) != 0) return;
	selectNext();
}
//==================================================================
public void select(int which,boolean select) {selectItem(which,select);}// repaintNow();}
public void select(int which) {selectItem(which,true);}// repaintNow();}
//==================================================================
public void setText(String text) {selectItem(text); repaintNow();}
public String getText() {return getItemAt(selectedIndex).label;}
//==================================================================
public void selectItem(String label)
//==================================================================
{
	if (!trySelectItem(label) && selectedIndex != -1)
		selectItem(selectedIndex,false);
}
//===================================================================
public void selectItems(int first,int last,boolean selected)
//===================================================================
{
	int s = Math.min(first,last);
	int e = Math.max(first,last);
	for (int i = s; i<=e; i++) selectItem(i,selected);
}
//==================================================================
public void selectItem(int index,boolean selected)
//==================================================================
{
	int oldSel = selectedIndex;
	if (selected) selectedIndex = index;
	else selectedIndex = -1;
	if (oldSel != -1) repaintItem(oldSel);
	if (selectedIndex != -1) repaintItem(selectedIndex);
	if (!(this instanceof Menu)) repaintNow();
}
/**
* Changes the text associated with an item.
*/
//==================================================================
public void changeItem(int index,Object newText)
//==================================================================
{
	if (index >= itemsSize() || index < 0) return;
	if (newText == null) newText = "";
	Object obj = items.get(index);
	if (obj instanceof MenuItem) {
		if (newText instanceof MenuItem)
			items.set(index,(MenuItem)newText);
		else if (newText instanceof IImage)
			((MenuItem)obj).image = (IImage)newText;
		else
			((MenuItem)obj).label = newText.toString();
	}else if (obj instanceof String) items.set(index,newText.toString());
	repaintItem(index);
}
public int displayRows = 5;
protected int firstItem = 0;
protected int xShift = 0;
//==================================================================
public int getDisplayRows()
//==================================================================
{
	if (displayRows <= 0 || itemsSize() < displayRows) {
		int ret = itemsSize();
		if (ret > 10000) ret = 10000;
		return ret;
	}
	return displayRows;
}
//==================================================================
public int getScreenRows()
//==================================================================
{
	int h = getItemHeight();
	if (h == 0) return 0;
	return (height-spacing*2)/h;
}
/*
//==================================================================
public boolean anyModifiers(int which)
//==================================================================
{
	for (int i = 0; i<itemsSize(); i++){
		MenuItem mi = getItemAt(i);
		if (mi.subMenu != null) mi.modifiers |= mi.HasSubMenu;
		if ((mi.modifiers & which) != 0) return true;
	}
	return false;
}
*/
//==================================================================
protected void calculateSizes()
//==================================================================
{
	preferredWidth = getMenuWidth();
	preferredHeight = getMenuHeight();
	if (dropDownButton) preferredWidth += preferredHeight;
}
//------------------------------------------------------------------
protected int getMenuWidth()
//------------------------------------------------------------------
{

	int w = getMenuWidth(true,true);
	//System.out.println(w);
	return w;
}
//===================================================================
public int getMenuWidth(boolean subMenus,boolean topLevel)
//===================================================================
{
	if (!calculateWidth) return 40;
	xOffset = 6;
	FontMetrics fm = getFontMetrics();
	int width = 0;
	int numSubs = 0;
	for (int i = 0; i<itemsSize(); i++){
		MenuItem mi = getItemAt(i);
		int w = mi.image == null ? fm.getTextWidth(mi.label)+xOffset+spacing*2 : mi.image.getWidth()+xOffset+spacing*2;
		if (indentDropItems)
			if (mi.indentLevel < 0) w += 6*i;
			else w += mi.indentLevel*6;
		if (w>width) width = w;
		//System.out.println(mi.label+":"+fm.getTextWidth(mi.label));
		if (mi.subMenu != null) {
			if (topLevel) numSubs++;
			if (subMenus){
				w = mi.subMenu.getMenuWidth(true,false);
				if (w>width) width = w;
			}
		}
	}
	if (numSubs != 0) width += 5;
	return width;
}
//==================================================================
public int getMenuHeight()
//==================================================================
{
	int h = getItemHeight();
	h *= getDisplayRows();
	h += spacing*2;
	return h;
}
/**
* This is used to calculate the height of the line used for each item.
* If you explicitly set itemHeight to a value >= 0 then that value will
* be used. Otherwise it calculates the height of the current font and
* also checks each item (if items is non-null) to
* see if it has an image associated with it. If any item has an image that
* is greater than the font height, then that height will be used.<p>
* After calculating the height it is stored in itemHeight so the next time
* it is called it will not calculate it again, but it will return the
* value of itemHeight instead.<p>
* To avoid any calculations at all, you can explicitly set itemHeight to
* a value >= 0.
**/
//-------------------------------------------------------------------
protected int getItemHeight()
//-------------------------------------------------------------------
{
	if (itemHeight >= 0) return itemHeight;
	if (items != null)
		for (int i = 0; i<items.size(); i++){
			MenuItem mi = getItemAt(i);
			if (mi.image != null){
				int h = mi.image.getHeight()+2;
				itemHeight = Math.max(h,itemHeight);
			}
		}
	FontMetrics fm = getFontMetrics();
	int h = fm.getHeight()+2;//+fm.getLeading();
	if (itemHeight != 0) h = itemHeight = Math.max(h,itemHeight);
	return h;
}
//-------------------------------------------------------------------
protected static MenuItem dummyItem = new MenuItem();
//-------------------------------------------------------------------

//==================================================================
public MenuItem getItemAt(int where)
//==================================================================
{
	if (where >= itemsSize() || where < 0) {
		dummyItem.label = "";
		dummyItem.modifiers = 0;
		return dummyItem;
	}
	Object obj = items.get(where);
	if (obj instanceof MenuItem) return (MenuItem)obj;
	else if (obj instanceof String){
		if (useMenuItems) dummyItem.set((String)obj);
		else dummyItem.setText((String)obj);
		dummyItem.modifiers = 0;
		if (where == selectedIndex) dummyItem.modifiers |= MenuItem.Selected;
	}
	return dummyItem;
}

//===================================================================
public MenuItem findItem(String name,boolean searchChildMenus)
//===================================================================
{
	int max = itemsSize();
	for (int i = 0; i<max; i++){
		MenuItem mi = getItemAt(i);
		if (mi.label != null)
			if (name.equalsIgnoreCase(mi.label))
				return mi;
		if (mi.subMenu != null && searchChildMenus){
			mi = mi.subMenu.findItem(name,true);
			if (mi != null) return mi;
		}
	}
	return null;
}
//===================================================================
public boolean checkOnlyOne(Object [] items,Object toCheck,boolean onlyIfInArray)
//===================================================================
{
	if (onlyIfInArray){
		if (toCheck == null) return false;
		boolean isInArray = false;
		for (int i = 0; i<items.length && !isInArray; i++)
			isInArray = (toCheck instanceof MenuItem) ?
				toCheck.equals(items[i]):items[i].equals(toCheck);
		if (!isInArray) return false;
	}

	for (int i = 0; i<items.length; i++){
		MenuItem found = null;
		if (items[i] instanceof MenuItem) found = (MenuItem)items[i];
		else found = findItem(items[i].toString(),true);
		if (found == null) continue;
		if (found.equals(toCheck))
			found.modifiers |= found.Checked;
		else
			found.modifiers &= ~found.Checked;
	}
	return true;
}
//==================================================================
public void selectOrUnselect(int item)
//==================================================================
{
	repaintItem(item);
}
//==================================================================
public boolean isSelected(int idx) {return idx == selectedIndex;}
//==================================================================

//==================================================================
protected void paintBox(Graphics g)
//==================================================================
{
	Color fill = Color.White;
	int flags = getModifiers(true);
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) fill = getBackground();
	g.setColor(fill);
	if ((borderStyle & BF_SOFT) != 0){
		doBackground(g);
		//g.setColor(getBackground());
		//g.fillRect(0,0,width,height);
		g.setColor(fill);
 		g.fillRoundRect(0,0,width,height,3);
	}else
		g.fillRect(0,0,width,height);
}
ImageBuffer itemBuffer = new ImageBuffer();
static ImageBuffer checkBuffer = new ImageBuffer();
ImageBuffer menuBuffer;

//==================================================================
protected String getDisplayString() {if (isSingleLine && selectedIndex == -1) return ""; return null;}
//==================================================================

protected static Rect clipBuffer = new Rect(), newClip = new Rect(), dimRect = new Rect(), innerRect = new Rect();

//===================================================================
public void repaintItem(int index)
//===================================================================
{
	paintItem(null,index);
}
//==================================================================
public void paintItem(Graphics g,int index) {paintItem(g,index,false);}
public void paintItem(Graphics graphics,int index,boolean checksOnly)
//==================================================================
{
	if (isSingleLine && index != firstItem) return;
	Graphics g = graphics;
	if (g == null) {
		if (!Gui.requestPaint(this)) return;
		g = getGraphics();
	}
	if (g == null) return;
	int h = getItemHeight();
	int y = spacing+(index-firstItem)*h;
	int x = xOffset;
	int w = width-spacing*2;
	if (dropDownButton) w -= height;
	if (w <= 0 || h <= 0) return;
	int flags = getModifiers(true);
	int num = getScreenRows();
	if (index < firstItem || index >= firstItem+num+1) return;
	Rect oldClip = g.getClip(clipBuffer);
	boolean restoreClip = oldClip != null;
	if (oldClip == null) oldClip  = getDim(dimRect);
	oldClip.getIntersection(innerRect.set(spacing,spacing,width-spacing*2,height-spacing*2),newClip);
	g.setClip(newClip.x,newClip.y,newClip.width,newClip.height);
	String displayString = getDisplayString();
	boolean separateString = true;
	MenuItem mi = getItemAt(index);
	if (displayString == null && mi != null) {
		displayString = useMenuItems ? Gui.makeHot(mi.label,mi.hotkey) : mi.label;
		separateString = false;
	}
	Graphics gr = itemBuffer.get(w,h,true);
	Image img = itemBuffer.image;
	if (checksOnly) {
		gr = checkBuffer.get(5,h);
		img = checkBuffer.image;
	}
	boolean dis = !(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || ((flags & NotAnEditor) != 0);
	boolean ed = !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
 	boolean canSelect = !((mi.modifiers & mi.Disabled) != 0);
	boolean block = isSelected(index) && blockSelected;
	boolean isSeparator = ((mi.modifiers & mi.Separator) != 0);
	boolean isChecked = ((mi.modifiers & mi.Checked) != 0);
	boolean hasSub = (mi.subMenu != null);
	if (separateString) canSelect = !(block = isSeparator = isChecked = hasSub = false);
//..................................................................
// Fill background.
//..................................................................
	Color c;
	if (block)
		if (dis || ed) c = Color.DarkGray;
		else c = getBlockColor();//getForeground();
	else
		if (dis || ed) c = Color.LightGray;
		else c = backGround == null ? Color.White : backGround;
	gr.setColor(c);
	if (!checksOnly) {
		gr.fillRect(0,0,w,h);
	}
	else gr.fillRect(0,0,5,h);
//..................................................................
// Do text.
//..................................................................
	if (block)
		if (dis || !canSelect) c = Color.LightGray;
		else c = Color.White;
	else
		if (dis || !canSelect) c = Color.DarkGray;
		else c = getForeground();
	gr.setColor(c);
	gr.setFont(getFont());
	boolean dontDraw = false;
	if (isSeparator){
		if (!checksOnly) {
			gr.setColor(Color.DarkGray);
			gr.drawLine(0,h/2,w,h/2);
			gr.setColor(Color.White);
			gr.drawLine(0,h/2+1,w,h/2+1);
			gr.setColor(c);
		}
		else dontDraw = true;
	}else {
		gr.translate(-xShift,0);
		if (!checksOnly) {
			if (mi.image != null){
				int opts = 0;
				if (num > 1 && indentDropItems)
					if (mi.indentLevel < 0)
						opts = INDENT_ITEM_FLAG|index;
					else
						opts = INDENT_ITEM_FLAG|mi.indentLevel;
				if (dis || !canSelect) opts |= IImage.DISABLED;
				mi.image.draw(gr,x,0,opts);
				if (mi.image instanceof OnScreenImage){
					//if (imagesToRefresh == null) imagesToRefresh = new WeakCache();
					//imagesToRefresh.put(mi.image,new Integer(index));
					((OnScreenImage)mi.image).setRefresher(this);
				}
			}else{
				if (!shortenItems)
					gr.drawText(displayString,x,0);//+1);
				else{
					int mw = w-8;
					FontMetrics fm = getFontMetrics();
					int tw = fm.getTextWidth(displayString);
					if (tw < mw) gr.drawText(displayString,x,0);
					else{
						String add = new String("...");
						char [] chars = ewe.sys.Vm.getStringChars(displayString);

						tw += fm.getTextWidth(add);
						int i = 0;
						for (; i<chars.length; i++){
							tw -= fm.getCharWidth(chars[i]);
							if (tw < mw) {
								gr.drawText(add+new String(chars,i+1,chars.length-i-1),x,0);
								break;
							}
						}
						if (i == chars.length) gr.drawText(displayString,x,0);
					}
				}
			}
		}
		int hy = (h-5)/2;
		if (hasSub && !checksOnly)
			//gr.drawDiamond(new Rect(w-5,hy,5,5),mGraphics.Right);
			gr.drawHorizontalTriangle(Rect.buff.set(w-3,hy,3,5),false);
		if (isChecked){
			gr.drawHorizontalTriangle(Rect.buff.set(0,hy,3,5),true);
			gr.drawHorizontalTriangle(Rect.buff.set(2,hy,3,5),false);
			//gr.drawDiamond(new Rect(1,hy,5,5),mGraphics.All);
		}
		gr.translate(xShift,0);
	}
	//if (!checksOnly)
	if (!dontDraw) g.drawImage(img,spacing,y);
	//if (restoreClip)
	g.setClip(oldClip.x,oldClip.y,oldClip.width,oldClip.height);
	//mGraphics.restoreClip(g,oldClip);
	if (g != graphics) g.free();

}

//==================================================================
protected void doPaintData(Graphics gr)
//==================================================================
{
	Graphics g = gr;
	boolean useImage = hasModifier(KeepImage,false);
	if (useImage && menuBuffer == null) {
		doPaint(gr,getRect());
		return;
	}
	if (useImage)
		if (!menuBuffer.isSameSize(width,height)){
			doPaint(gr,getRect());
			return;
		}
	if (useImage) {
		gr.fillRect(0,0,0,0);//This fixes an obscure bug.
		gr.drawImage(menuBuffer.image,0,0);

	}
	if (hasModifier(PaintOutsideOnly,false)) return;
	for (int i = 0; i<getScreenRows()+1; i++)
		paintItem(g,i+firstItem,useImage);
}
//===================================================================
public void refresh(IImage image, int options)
//===================================================================
{
	if (!requestPaint()) return;
	Graphics g = getGraphics();
	if (g == null) return;
	for (int i = 0; i<getScreenRows()+1; i++)
		if (getItemAt(i+firstItem).image == image)
			paintItem(g,i+firstItem);
	g.free();
}
//==================================================================
public void doPaint(Graphics gr,Rect area)
//==================================================================
{
	//_debug = true;
	//if ((borderStyle & BF_SOFT) == BF_SOFT) doBackground(gr);
	Rect r = gr.getClip(new Rect());
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	Graphics g = gr;
	if (hasModifier(KeepImage,false) && false){
		if (menuBuffer != null){
			if (menuBuffer.isSameSize(width,height)){
				//doPaintData(g);
				return;
			}
		}else menuBuffer = new ImageBuffer();
		g = menuBuffer.get(width,height);
	}
	if (!hasModifier(PaintDataOnly,false)){
		paintBox(g);
		doBorder(g);
	}
	if (!hasModifier(PaintOutsideOnly,false)){
		int h = getItemHeight(), s = spacing;
		for (int i = 0; i<getScreenRows()+1; i++){
			if (s < area.y){
				if (s+h < area.y) {
					//ewe.sys.Vm.debug("Ignoring: "+i);
					s += h;
					continue;
				}
			}else if (s >= area.y+area.height) {
				//ewe.sys.Vm.debug("Ignoring: "+i);
				s += h;
				continue;
			}
			paintItem(g,i+firstItem);
			s += h;
			//if (_debug) System.out.println(getItemAt(i+firstItem).label);
		}
	}
	if (hasModifier(KeepImage,false) && false) {
		//gr.fillRect(0,0,0,0);// This fixes an obscure bug.
		gr.drawImage(menuBuffer.image,0,0);
	}
}
/**
* This makes the item visible and updates the screen as well.
**/
//===================================================================
public void makeItemVisible(int row)
//===================================================================
{
	if (makeVisible(row)) updateItems();
}
/**
* This places the item in the visible range but does not update
* the screen. It returns true if you should call updateItems() after.
**/
//===================================================================
public boolean makeVisible(int row)
//===================================================================
{
	if (getScreenRows() == 0) return false;
	if (row < firstItem || firstItem+getScreenRows() <= row){
		return centerSelected();
	}
	return false;//true;
}
//===================================================================
public boolean itemIsVisible(int row,boolean isFullyVisible)
//===================================================================
{
	if (row < firstItem) return false;
	int r = getScreenRows();
	if (row < firstItem+r) return true;
	if (isFullyVisible) return false;
	int ex = getItemHeight()*r;
	if ((height-spacing*2) > ex) return false;
	return row == firstItem+r;
}
//==================================================================
public boolean centerSelected()
//==================================================================
{
	int oldFirst = firstItem;
	if (selectedIndex == -1) {
		firstItem = 0;
		return oldFirst != firstItem;
	}
	int sr = getScreenRows();
	int r = sr/2;
	firstItem = selectedIndex-r;
	int on = itemsSize()-firstItem;
	int missing = sr-on;
	if (missing > 0) firstItem -= missing;
	if (firstItem < 0) firstItem = 0;
	return oldFirst != firstItem;

}
//==================================================================
public void changeModifiers(int toSet,int toClear)
//==================================================================
{
	for (int i = 0; i<itemsSize(); i++){
		MenuItem mi = getItemAt(i);
		mi.modifiers |= toSet;
		mi.modifiers &= ~toClear;
	}
}
//===================================================================
public void setInt(int value) {select(value);}
//===================================================================
public int getInt() {return selectedIndex;}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	super.make(reMake);
	getMenuWidth();
}
/**
* Add a section to the list/menu. If there were already items then a '-' item
* is added (which looks like a horizontal line in a Menu) first, otherwise no
* '-' is added.
* @param items the items to add.
* @param separator set this to true if you want a separator before this section.
* @return
*/
//===================================================================
public void addSection(Object [] items,boolean separator)
//===================================================================
{
	if (items == null || items.length == 0) return;
	if ((itemsSize() != 0) && separator) addItem("-");
	for (int i = 0; i<items.length; i++) addItem(items[i]);
}
/**
* This updates the control to new choices added and then refreshes it on screen.
**/
//===================================================================
public void updateItems()
//===================================================================
{
	repaintDataNow();
}
/**
* This tells the control to generate a DataChangeEvent when the selection has changed.
* By default this is true except for anything inheriting from BasicList
**/
public boolean notifyDataChangeOnSelect = true;
//-------------------------------------------------------------------
boolean checkLabelKey(int i,int key)
//-------------------------------------------------------------------
{
	MenuItem m = getItemAt(i);
	if ((m.modifiers & m.Separator) != 0) return false;
	String lookText = m.label;
	if (m.image instanceof IconAndText)
		lookText = ((IconAndText)m.image).text;
	if (lookText == null || lookText.length() == 0) return false;
	return (Character.toUpperCase(lookText.charAt(0)) == Character.toUpperCase((char)key));
}
//-------------------------------------------------------------------
protected int findKeyed(int start,int key)
//-------------------------------------------------------------------
{
	int sz =itemsSize();
	for (int i = start; i<sz; i++)
		if (checkLabelKey(i,key)) return i;
	for (int i = 0; i<start; i++)
		if (checkLabelKey(i,key)) return i;
	return -1;
}
//##################################################################
}
//##################################################################

