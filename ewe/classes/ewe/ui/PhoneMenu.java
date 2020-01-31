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
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.ImageTool;
import ewe.fx.Rect;
import ewe.fx.ImageBuffer;
import ewe.fx.Color;
import ewe.fx.IconAndText;
import ewe.fx.FontTools;
import ewe.fx.IImage;
import ewe.fx.OnScreenImage;
import ewe.fx.ImageCache;
import ewe.util.Vector;
import ewe.sys.Convert;
/**
A Phone Menu is a non-scrolling menu displayed normally full screen. A maximum of 9 items
can be displayed on the screen at one time, and a digit from 1-9 is associated with each one.
If there is not enough space on the screen to display all the items then up to 8 are displayed
and the 9 button is used to indicate "More..." items.
**/
//##################################################################
public class PhoneMenu extends Control{
//##################################################################


public int itemHeight = 20;

protected int firstItem = 0;
protected int selectedScreenIndex = -1;

/**
If this is true the controls will be laid out in a 2x3
grid.
**/
public boolean useGrid = false;
/**
Set this true if you do not want the phone digits to be displayed.
**/
public boolean dontShowNumbers = false;
/**
Set this true if you do not want the icons to be scaled to 16x16 or 32x32
**/
public boolean dontScaleIcons = false;
/**
If this is not null and scaleBackgroundImage is null,
then this will be used as the background image.
**/
public IImage backgroundImage;
/**
If this is not null, this will be scaled to fit the entire menu.
**/
public IImage scaleBackgroundImage;

protected static IImage moreImage;
protected static IImage bigMoreImage;

//-------------------------------------------------------------------
private boolean isOverriden;
//-------------------------------------------------------------------

{
	modify(TakesKeyFocus,0);
	preferredHeight = 20*9;
	backGround = Color.White;
	isOverriden = !getClass().getName().equals("ewe.ui.PhoneMenu");// || true;
}
/**
Override this to get the area on the screen for a particular item. Call
getIndexOnScreen(anItem) to determine the item index on the screen. If this returns -1
then the item is not currently on screen.
* @param anItem The index of a particular data item.
* @param destination The destination Rect or null to create and return a new one.
* @return The destination Rect or null if the item is not currently being displayed.
*/
//===================================================================
public Rect getItemRect(int anItem, Rect destination)
//===================================================================
{
	if (destination == null) destination = new Rect();
	if (anItem < firstItem || anItem >= firstItem+getDataItemsOnScreen(firstItem))
		return null;
	int idx = anItem-firstItem;
	if (useGrid) return destination.set((idx%2)*(width/2),(idx/2)*itemHeight,width/2,itemHeight);
	else return destination.set(0,itemHeight*idx,width,itemHeight);
}
/**
Override this to get the area on the screen for the "More..." item.
* @param destination The destination Rect or null to create and return a new one.
* @return The destination Rect or null if the item is not currently being displayed.
**/
//===================================================================
public Rect getMoreItemRect(Rect destination)
//===================================================================
{
	if (destination == null) destination = new Rect();
	if (useGrid) {
		int idx = getTotalItemsOnScreen()-1;
		return destination.set((idx%2)*(width/2),(idx/2)*itemHeight,width/2,itemHeight);
	}
	else return destination.set(0,itemHeight*8,width,itemHeight);
}
/**
Get the total number of items on the dispay - up to a maximum
of 9.
**/
//-------------------------------------------------------------------
protected int getTotalItemsOnScreen()
//-------------------------------------------------------------------
{
	int canFit = height/itemHeight;
	if (useGrid) canFit *= 2;
	if (canFit > 9) canFit = 9;
	return canFit;
}

/**
This gets called when the size of the PhoneMenu has now changed.
**/
//-------------------------------------------------------------------
protected void sizeChanged()
//-------------------------------------------------------------------
{

}
/**
	Items are added to this as Strings or MenuItem objects. Alternatively, the
	method getItemAtIndex() may be overriden.
**/
public Vector items = new Vector();
/**
Create a MenuItem given an Icon and a label, possibly scaling the icon
if necessary.
**/
//===================================================================
public MenuItem makeItem(IImage icon, String text)
//===================================================================
{
	MenuItem mi = new MenuItem();
	mi.label = mi.action = text;
	if (icon != null){
		int w = icon.getWidth();
		if (useGrid && w < 32 && !dontScaleIcons)
			icon = ImageTool.scale(ImageTool.toImageData(icon),32,32,0);
		else if (!useGrid && w > 16 && !dontScaleIcons)
			icon = ImageTool.scale(ImageTool.toImageData(icon),16,16,0);
		mi.image = text != null ? new IconAndText(icon,text,null,useGrid ? Down : Right) : icon;
	}
	return mi;
}
/**
Add an item and return the MenuItem created for it. The "image" field of the
returned MenuItem will be an IconAndText if <b>both</b> "icon" and "text" are
not null.
**/
//===================================================================
public MenuItem addItem(IImage icon, String text)
//===================================================================
{
	MenuItem mi = makeItem(icon,text);
	items.add(mi);
	return mi;
}
/**
Add an item and return the MenuItem created for it. The "image" field of the
returned MenuItem will be an IconAndText if <b>both</b> "iconName" and "text" are
not null.
**/
//===================================================================
public MenuItem addItem(String iconName, Object maskOrColor, String text)
//===================================================================
{
	return addItem(ImageCache.cache.get(iconName,maskOrColor),text);
}
/*
{
	IImage icon = new ewe.fx.mImage("ewe/ewesmall.bmp",Color.White);
	for (int i = 0; i<20; i++)
		items.add(new MenuItem().iconize("Item - "+(i+1),icon,true));
}
*/

/**
Get which item is selected.
**/
//===================================================================
public int getSelectedItem()
//===================================================================
{
	if (selectedScreenIndex == -1) return -1;
	if (selectedScreenIndex >= getDataItemsOnScreen(firstItem)) return -1;
	return firstItem+selectedScreenIndex;
}
/**
Make an item visible, repainting if necessary.
Returns true if a repaint was done, false if one was not done.
**/
//===================================================================
public boolean makeVisible(int itemIndex)
//===================================================================
{
	if (width == 0 || height == 0) return false;
	if (itemIndex >= firstItem && itemIndex < firstItem+getDataItemsOnScreen(firstItem))
		return false;
	int ni = countItems();
	for (int fi = 0; fi < ni; fi += getDataItemsOnScreen(firstItem)){
		if (itemIndex >= fi && itemIndex < fi+getDataItemsOnScreen(firstItem)){
			int sel = getSelectedItem();
			firstItem = fi;
			if (sel != -1) selectedScreenIndex = getIndexOnScreen(sel);
			repaintNow();
			return true;
		}
	}
	return false;
}
//===================================================================
public int getIndexOnScreen(int anItem)
//===================================================================
{
	if (anItem < firstItem || anItem >= firstItem+getDataItemsOnScreen(firstItem))
		return -1;
	return anItem-firstItem;
}

//===================================================================
public void doAction(int itemIndex)
//===================================================================
{
	paintSelectedMode = PAINT_FLASH_ON;
	if (getSelectedItem() == itemIndex) selectedScreenIndex = -1;
	setSelectedItem(itemIndex);
	ewe.sys.mThread.nap(150);
	//paintSelectedMode = PAINT_FLASH_OFF;
	paintSelectedMode = PAINT_SELECTED;
	repaintItem(itemIndex);
	paintSelectedMode = PAINT_SELECTED;
	notifyAction();
}
/**
Change the index selected on screen, repainting if necessary.
**/
//-------------------------------------------------------------------
protected void setSelectedScreenIndex(int newSelected)
//-------------------------------------------------------------------
{
	int was = selectedScreenIndex;
	if (newSelected == was) return;
	if (was != -1){
		selectedScreenIndex = -1;
		paintItem(null,was);
	}
	selectedScreenIndex = newSelected;
	if (selectedScreenIndex != -1) paintItem(null,selectedScreenIndex);
}
//===================================================================
public void setSelectedItem(int newSelectedItem, boolean makeVisible)
//===================================================================
{
	int was = getSelectedItem();
	if (newSelectedItem == was) return;
	setSelectedScreenIndex(-1);
	if (makeVisible && newSelectedItem != -1) makeVisible(newSelectedItem);
	setSelectedScreenIndex(getIndexOnScreen(newSelectedItem));
}
//===================================================================
public void setSelectedItem(int newSelectedItem)
//===================================================================
{
	setSelectedItem(newSelectedItem,false);
}

//===================================================================
public void resizeTo(int newWidth, int newHeight)
//===================================================================
{
	super.resizeTo(newWidth,newHeight);
	if (!useGrid){
		itemHeight = height/9;
		setFont(FontTools.getFontForHeight(itemHeight,getFontMetrics()));
	}else{
		itemHeight = 60;
	}
	sizeChanged();
}
/**
* Return the number of items.
**/
//===================================================================
public int countItems()
//===================================================================
{
	return items == null ? 0 : items.size();
}
/**
 * Get the item at the specified index as a MenuItem.
 * @param index the item index.
 * @return the item at the specified index as a MenuItem.
 */
//===================================================================
public MenuItem getItemAtIndex(int index)
//===================================================================
{
	if (items == null) return null;
	if (index < 0 || index >= items.size()) return null;
	Object got = items.get(index);
	if (got instanceof MenuItem) return (MenuItem)got;
	MenuItem mi = new MenuItem();
	mi.label = got.toString();
	items.set(index,mi);
	return mi;
}
/**
Get the total number of data items on the dispay (items not including the "More..." item)
up to a maximum of 9, assuming that startingFrom indicates the index of the first item
to display.
**/
//-------------------------------------------------------------------
protected int getDataItemsOnScreen(int startingFrom)
//-------------------------------------------------------------------
{
	int got = getTotalItemsOnScreen();
	int left = countItems()-startingFrom;
	if (got >= left) return left;
	return got-1;
}
/**
Get the total number of data items on the current dispay (items not including the "More..." item)
up to a maximum of 9.
**/
//-------------------------------------------------------------------
protected int getDataItemsOnScreen()
//-------------------------------------------------------------------
{
	return getDataItemsOnScreen(firstItem);
}
//-------------------------------------------------------------------
protected boolean isShowingMoreItem()
//-------------------------------------------------------------------
{
	int got = getTotalItemsOnScreen();
	int left = countItems()-firstItem;
	return left > got;
}

//-------------------------------------------------------------------
protected int getMoreIndex()
//-------------------------------------------------------------------
{
	int got = getTotalItemsOnScreen();
	int left = countItems()-firstItem;
	if (left <= got) return -1;
	return got-1;
}
//-------------------------------------------------------------------
protected boolean moreIsSelected()
//-------------------------------------------------------------------
{
	return selectedScreenIndex != -1 && selectedScreenIndex == getMoreIndex();
}
//-------------------------------------------------------------------
protected boolean nextItem(boolean forwards, boolean horizontally)
//-------------------------------------------------------------------
{
	int items = getDataItemsOnScreen(firstItem);
	if (isShowingMoreItem()) items++;
	int newSel = 0;
	if (!useGrid){
		if (forwards){
			if (selectedScreenIndex >= items-1)
				newSel = 0;
			else
				newSel = selectedScreenIndex+1;
		}else{
			if (selectedScreenIndex > 0) newSel = selectedScreenIndex-1;
			else newSel = items-1;
		}
	}else{
		int col = selectedScreenIndex%2;
		if (forwards){
			if (horizontally) {
				newSel = selectedScreenIndex+1;
				if (newSel >= items) newSel = 0;
			}else {
				newSel = selectedScreenIndex+2;
				if (newSel >= items) newSel = col;
			}
		}else{
			if (horizontally) {
				newSel  = selectedScreenIndex-1;
				if (newSel < 0) newSel = items-1;
			}else{
				newSel  = selectedScreenIndex-2;
				if (newSel < 0) newSel = ((((items+1)/2)-1)*2)+col;
				if (newSel >= items) newSel -= 2;
			}

		}
	}
	if (newSel < 0 || newSel >= items) return false;
	setSelectedScreenIndex(newSel);
	return true;
}
//-------------------------------------------------------------------
protected boolean nextPage(boolean forwards)
//-------------------------------------------------------------------
{
	selectedScreenIndex = -1;
	if (forwards){
		int got = getDataItemsOnScreen(firstItem);
		if (got+firstItem >= countItems()) return false;
		firstItem += got;
		return true;
	}else{
		if (firstItem == 0) return false;
		firstItem -= (getTotalItemsOnScreen()-1);
		return true;
	}
}
private MenuItem moreItem;

//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	if (ev.type == ev.KEY_PRESS){
		if (ev.key == IKeys.PAGE_DOWN){
			if (nextPage(true)) repaintNow();
		}else if (ev.key == IKeys.PAGE_UP || ev.isBackKey()){
			if (nextPage(false)) repaintNow();
		}else if (ev.key == IKeys.UP || ev.key == IKeys.LEFT){
			nextItem(false,ev.key == IKeys.LEFT);
		}else if (ev.key == IKeys.DOWN || ev.key == IKeys.RIGHT){
			nextItem(true,ev.key == IKeys.RIGHT);
		}else if (ev.isActionKey()){
			if (moreIsSelected()){
				if (nextPage(true)) repaintNow();
			}else{
				doAction(getSelectedItem());
			}
		}else if (ev.key >= '1' && ev.key <= '9'){
			//setSelectedItem(firstItem+ev.key-'1');
			int idx = ev.key-'1';
			if (idx == getMoreIndex()) {
				if (nextPage(true)) repaintNow();
			}else
				doAction(firstItem+ev.key-'1');
		}else if (ev.isCancelKey()){
			postEvent(new ControlEvent(ControlEvent.CANCELLED,this));
		}
	}else
		super.onKeyEvent(ev);
}

/**
This is an IconAndText that you can use when painting individual items. Its font
will be set to the font of the PhoneMenu.
**/
protected IconAndText display = new IconAndText();
/**
These are the phone keys from '1' to '9' - you can use them
in paintItemAt().
**/
protected static String[] keys;

private static Rect itemRect;
private static ImageBuffer itemBuffer;

private int paintSelectedMode = PAINT_SELECTED;

private static final int PAINT_INDIVIDUAL = 0x1;
protected static final int PAINT_IS_MORE_ITEM = 0x2;
protected static final int PAINT_SELECTED = 0x4;
protected static final int PAINT_FLASH_ON = 0x8;
protected static final int PAINT_FLASH_OFF = 0x10;

/**
This method should paint the specified item at the location (0,0) in
the specified Graphics.
* @param g The target Graphics. This would have already been translated so that
the item should be painted at location (0,0). The background for the image would
also have already been painted, but if the item is selected, no special selected item
painting is done - you will have to check if the PAINT_IS_SELECTED flag is set in the
options.
* @param itemIndex the index of the item. If PAINT_IS_MORE_ITEM is set in the options
then this value will be -1.
* @param item The MenuItem holding the item as returned by getItemAtIndex().
* @param itemWidth The width of the area allocated to the item.
* @param itemHeight The height of the area allocated to the item.
* @param options any of the PAINT_XXX values ORed together.
*/
//-------------------------------------------------------------------
protected void paintItemAt(Graphics g, int itemIndex, MenuItem item, int itemWidth, int itemHeight, int options)
//-------------------------------------------------------------------
{
	if ((options & PAINT_SELECTED) != 0){
		g.setColor(Color.DarkBlue);
		g.fillRect(0,0,itemWidth,itemHeight);
		g.setColor(Color.White);
	}else if ((options & PAINT_FLASH_ON) != 0){
		Color c = g.getColor();
		g.setColor(Color.DarkBlue);
		g.drawRect(0,0,itemWidth,itemHeight);
		g.setColor(c);
	}
	display.draw(g,0,0,0);
}

//-------------------------------------------------------------------
private void paintItem(Graphics gr, int indexOnScreen, int dataItemsOnScreen, int options, FontMetrics fm)
//-------------------------------------------------------------------
{
	if (fm == null) fm = getFontMetrics();
	if (dataItemsOnScreen == -1) dataItemsOnScreen = getDataItemsOnScreen(firstItem);
	boolean isMore = indexOnScreen == dataItemsOnScreen && (dataItemsOnScreen+firstItem<countItems());
	if (indexOnScreen > dataItemsOnScreen) return;
	//
	// Now find the correct item.
	//
	MenuItem toShow = null;
	if (isMore){
		if (moreItem == null)
			moreItem = makeItem(ImageCache.cache.get("ewe/rightarrowsmall.bmp",Color.White),"More...");
		toShow = moreItem;
	}else{
		toShow = getItemAtIndex(indexOnScreen+firstItem);
	}
	if (toShow == null) return;
	//
	// Find the item rect.
	//
	if (itemRect == null) itemRect = new Rect();
	if (!isOverriden && !useGrid) itemRect.set(0,indexOnScreen*itemHeight,width,itemHeight);
	else if (isMore) getMoreItemRect(itemRect);
	else getItemRect(firstItem+indexOnScreen,itemRect);
	//
	// Now paint background if necessary.
	//
	Graphics g = gr;
	//
	if ((options & PAINT_INDIVIDUAL) != 0){
		if (itemBuffer == null) itemBuffer = new ImageBuffer();
		g = itemBuffer.get(itemRect.width,itemRect.height,true);
		g.translate(-itemRect.x,-itemRect.y);
		doBackground(g);
		g.translate(itemRect.x,itemRect.y);
	}
	//
	if (keys == null) keys = new String[9];
	for (int i = 1; i<10; i++) keys[i-1] = Convert.toString(i);
	display.clear();
	//if (display.fontMetrics == null)
	display.fontMetrics = fm;//getFontMetrics();
	display.textColor = null;
	if (!dontShowNumbers && indexOnScreen >= 0 && indexOnScreen <= 8)
		display.addColumn(keys[indexOnScreen],20,CENTER);
	if (toShow.image != null && toShow.image instanceof IconAndText){
		IconAndText ic = (IconAndText)toShow.image;
		ic.changeFontMetrics(fm);
		ic.textColor = null;
	}
	display.addColumn(toShow.image != null ? (Object)toShow.image : (Object)toShow.label);
	g.setFont(fm.getFont());
	g.setColor(getForeground());
	g.setBackground(Color.Null);
	//
	if (g == gr) g.translate(itemRect.x, itemRect.y);
	if (isMore) options |= PAINT_IS_MORE_ITEM;
	if (indexOnScreen == selectedScreenIndex)
		options |= paintSelectedMode;

	if (isOverriden || useGrid || false)
		paintItemAt(g,isMore ? -1 : (indexOnScreen+firstItem),toShow,itemRect.width,itemRect.height,options);
	else{
		int itemWidth = itemRect.width, itemHeight = itemRect.height;
		if ((options & PAINT_SELECTED) != 0){
			g.setColor(Color.DarkBlue);
			g.fillRect(0,0,itemWidth,itemHeight);
			g.setColor(Color.White);
		}else if ((options & PAINT_FLASH_ON) != 0){
			Color c = g.getColor();
			g.setColor(Color.DarkBlue);
			g.drawRect(0,0,itemWidth,itemHeight);
			g.setColor(c);
		}
		display.draw(g,0,0,0);
	}
	if (g == gr) g.translate(-itemRect.x, -itemRect.y);
	else itemBuffer.image.draw(gr,itemRect.x,itemRect.y,0);

	if (toShow != null && toShow.image instanceof OnScreenImage){
		((OnScreenImage)toShow.image).setRefresher(this);
	}
}
//-------------------------------------------------------------------
protected void paintItem(Graphics gr, int indexOnScreen)
//-------------------------------------------------------------------
{
	Graphics g = gr == null ? getGraphics() : gr;
	if (g == null) return;
	try{
		paintItem(g,indexOnScreen,getDataItemsOnScreen(firstItem),PAINT_INDIVIDUAL,null);
	}finally{
		if (g != gr) g.free();
	}
}
//===================================================================
public void repaintItem(int whichItem)
//===================================================================
{
	repaintItem(whichItem,null);
}
//-------------------------------------------------------------------
protected void repaintItem(int whichItem, Graphics gr)
//-------------------------------------------------------------------
{
	int onScreen = getIndexOnScreen(whichItem);
	if (onScreen == -1) return;
	paintItem(gr,onScreen);
}
//===================================================================
public void doBackground(Graphics g)
//===================================================================
{
	if (scaleBackgroundImage != null){
		if (backgroundImage == null || backgroundImage.getWidth() != width || backgroundImage.getHeight() != height)
			backgroundImage = ImageTool.scale(ImageTool.toImageData(scaleBackgroundImage),width,height,0);
	}
	super.doBackground(g);
	if (backgroundImage != null) backgroundImage.draw(g,0,0,0);
}
//===================================================================
public void doPaint(Graphics g, Rect where)
//===================================================================
{
	doBackground(g);
	int dataItemsOnScreen = getDataItemsOnScreen(firstItem);
	FontMetrics fm = getFontMetrics();
	for (int i = 0; i<dataItemsOnScreen+1; i++)
		paintItem(g,i,dataItemsOnScreen,0,fm);
}

//===================================================================
public PhoneMenu()
//===================================================================
{
	setPreferredSize(240,240);
}
//===================================================================
public void refresh(IImage image, int options)
//===================================================================
{
	if (!requestPaint()) return;
	int num = getDataItemsOnScreen(firstItem);
	for (int i = 0; i<num; i++){
		MenuItem toShow = getItemAtIndex(i+firstItem);
		if (toShow.image == image){
			repaintItem(firstItem+i);
			break;
		}
	}
}

//##################################################################
}
//##################################################################

