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
package ewe.graphics;
import ewe.ui.Menu;
import ewe.ui.Control;
//##################################################################
public class PageDisplayUnit{
//##################################################################
public PageDisplay display;
public PageDisplayUnit nextSibling;
public PageDisplayUnit firstChild;
public PageDisplayUnit lastChild;
public PageDisplayUnit parent;
public int x;
public int y;
public int width;
public int height;
/**
* An optional tool-tip.
**/
public Object tip;
/**
* This is a flag indicating that this unit is clickable (e.g. like a hyperlink).
**/
public static final int IsHot = 0x1;
/**
* These are the flags for the unit.
**/
public int flags;
/**
* This should return true if the point is within the hot area of the unit. By default
* it will return true as long as the point is within the total area.
**/
//===================================================================
public boolean isOnHotArea(int x,int y)
//===================================================================
{
	if ((flags & IsHot) != 0) return (x >= this.x && x < this.x+width && y >= this.y && y < this.y+height);
	return false;
}
/**
* Add a child unit and update its "parent" field to point to this display.
**/
//===================================================================
public void addChild(PageDisplayUnit unit)
//===================================================================
{
	unit.parent = this;
	unit.display = null;
	unit.nextSibling = null;
	if (lastChild == null)
		firstChild = lastChild = unit;
	else{
		lastChild.nextSibling = unit;
		lastChild = unit;
	}
}
/**
* This tells the PageDisplayUnit to display itself only - not its children. The
* provided graphics will have been translated so that (0,0) will map to the top left
* of this unit's parent.
**/
//===================================================================
public void doPaint(ewe.fx.Graphics g)
//===================================================================
{
}

//===================================================================
public void calculateSize()
//===================================================================
{
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		u.calculateSize();
		width = Math.max(u.x+u.width,width);
		height = Math.max(u.y+u.height,height);
	}
}
/**
* This retrieves the child unit that reports itself as hot for the specified x and y
* co-ordinates.
**/
//===================================================================
public PageDisplayUnit getHotUnit(int x,int y)
//===================================================================
{
	if (isOnHotArea(x,y)) return this;
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return null;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		PageDisplayUnit h = u.getHotUnit(x,y);
		if (h != null) return h;
	}
	return null;
}
/**
* This finds the PageDisplay this unit is displayed on, by going up the tree until
* finding a unit with the "display" variable set.
**/
//===================================================================
public PageDisplay getDisplay()
//===================================================================
{
	for (PageDisplayUnit u = this; u != null; u = u.parent)
		if (u.display != null) return u.display;
	return null;
}
//===================================================================
public Object getTipFor(int x,int y)
//===================================================================
{
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return null;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		Object tip = u.getTipFor(x,y);
		if (tip != null) return tip;
	}
	return this.tip;
}
/**
* This will get a context menu for the child unit at the specific point. If none is provided
* then it will call getDefaultMenuFor() and return that menu. For non-container units, it is better to overrided getDefaultMenuFor() and
* simply return the menu you want.
**/
//===================================================================
public Menu getMenuFor(int x,int y)
//===================================================================
{
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return null;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		Menu menu = u.getMenuFor(x,y);
		if (menu != null) return menu;
	}
	return (Menu)tagControl(getDefaultMenuFor(x,y));
}
//===================================================================
public static PageDisplayUnit getTaggedUnit(Control taggedControl)
//===================================================================
{
	if (taggedControl == null) return null;
	return (PageDisplayUnit)taggedControl.getTag(taggedControl.TAG_USER_DATA,null);
}
/**
* Use this to tag the menu with this PageDisplayUnit, so that the PageDisplay knows
* which one it came from.
**/
//-------------------------------------------------------------------
protected Control tagControl(Control m)
//-------------------------------------------------------------------
{
	if (m != null) m.setTag(m.TAG_USER_DATA,this);
	return m;
}
/**
* Get the default menu to display if none of the children return a menu. By default
* this returns null.
**/
//-------------------------------------------------------------------
protected Menu getDefaultMenuFor(int x,int y)
//-------------------------------------------------------------------
{
	return null;
}
/**
* This is called when a menu item is selected in the menu provided by the unit.
* @param m The menu provided by the unit.
* @param selectedItem usually a MenuItem object specifying the selected item.
*/
//===================================================================
public void menuItemSelected(Menu m,Object selectedItem)
//===================================================================
{

}
/**
* This can be called to tell the display to refresh this unit.
**/
//===================================================================
public void refresh()
//===================================================================
{
	int xx = 0, yy = 0;
	for (PageDisplayUnit u = this; u != null; u = u.parent){
		xx += u.x; yy += u.y;
		if (u.display != null) {
			u.display.refresh(xx,yy,width,height);
			return;
		}
	}
}
/**
 * If this returns true, then this unit has responded to the event and no further checking is
 * necessary.
 * @param x
 * @param y
 * @return true to indicate the event has been caught, false otherwise.
 */
//===================================================================
public boolean penPressed(int x,int y)
//===================================================================
{
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return false;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		if (u.penPressed(x,y)) return true;
	}
	return false;
}
/**
 * If this returns true, then this unit has responded to the event and no further checking is
 * necessary.
 * @param x
 * @param y
 * @return true to indicate the event has been caught, false otherwise.
 */
//===================================================================
public boolean penReleased(int x,int y)
//===================================================================
{
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return false;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		if (u.penReleased(x,y)) return true;
	}
	return false;
}
/**
 * If this returns true, then this unit has responded to the event and no further checking is
 * necessary.
 * @param x
 * @param y
 * @return true to indicate the event has been caught, false otherwise.
 */
//===================================================================
public boolean penClicked(int x,int y)
//===================================================================
{
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return false;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		if (u.penClicked(x,y)) return true;
	}
	return false;
}
/**
 * If this returns true, then this unit has responded to the event and no further checking is
 * necessary.
 * @param x
 * @param y
 * @return true to indicate the event has been caught, false otherwise.
 */
//===================================================================
public boolean penDoubleClicked(int x,int y)
//===================================================================
{
	if (x < this.x || x >= this.x+width || y < this.y || y >= this.y+height)
		return false;
	x -= this.x; y -= this.y;
	for (PageDisplayUnit u = firstChild; u != null; u = u.nextSibling){
		if (x < u.x || x >= u.x+width || y < u.y || y >= u.y+height) continue;
		if (u.penDoubleClicked(x,y)) return true;
	}
	return false;
}
//##################################################################
}
//##################################################################

