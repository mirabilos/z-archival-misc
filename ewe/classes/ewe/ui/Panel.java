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
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Insets;
import ewe.fx.Rect;
import ewe.util.Grid;
import ewe.util.Iterator;
import ewe.util.ObjectIterator;
import ewe.util.TagList;
import ewe.util.Vector;

/**
* A Panel is a general Container that is simpler than a CellPanel and which uses
* a Layout manager to layout it components. While it is not as flexible as a CellPanel
* and cannot support advanced features such as PanelSplitters it is faster than a CellPanel
* when laying out its controls.
**/

//##################################################################
public class Panel extends Canvas implements CellConstants, LayoutManager{
//##################################################################

protected static Insets noInsets = new Insets(0,0,0,0);

/**
* This contains default Tag values for the controls added to the Panel. The only
* Tag used by Panel is the INSETS tag, which must contain an ewe.fx.Insets object
* value.
**/
public TagList defaultTags = new TagList();
/**
The default cell constraints when controls are added to the Panel. The default value
is STRETCH
**/
public int defaultAddToMeCellConstraints = STRETCH;

protected Grid grid;// = new Grid();

/**
* This is only used if the Panel is acting as its own LayoutManager - which it is by default.
* It tells it to stretch the first column to fill the width of the Panel - it is false by default. If it is
* set true it will override stretchLastColumn.
**/
public boolean stretchFirstColumn = false;
/**
* This is only used if the Panel is acting as its own LayoutManager - which it is by default.
* It tells it to stretch the first row to fill the height of the Panel - it is false by default. If it is
* set true it will override stretchLastRow.
**/
public boolean stretchFirstRow = false;
/**
* This is only used if the Panel is acting as its own LayoutManager - which it is by default.
* It tells it to stretch the last column to fill the width of the Panel - it is true by default.
**/
public boolean stretchLastColumn = true;
/**
* This is only used if the Panel is acting as its own LayoutManager - which it is by default.
* It tells it to stretch the last row to fill the height of the Panel - it is true by default.
**/
public boolean stretchLastRow = true;

{
	isFullScrollClient = false;
}
/**
Create a new Panel which uses itself to layout components.
**/
//===================================================================
public Panel()
//===================================================================
{
	layoutManager = this;
}
/**
Create a new Panel which uses a specific layout manager.
**/
//===================================================================
public Panel(LayoutManager layout)
//===================================================================
{
	if (layout == null) throw new NullPointerException();
	layoutManager = layout;
}

//===================================================================
public boolean isEmpty()
//===================================================================
{
	return grid == null;
}
//===================================================================
public Object getControlTag(int tag,Control c,Object defaultValue)
//===================================================================
{
	if (c.hasTag(tag)) return c.getTag(tag,defaultValue);
	if (defaultTags == null) return defaultValue;
	return defaultTags.getValue(tag,defaultValue);
}

//===================================================================
public Control add(Control c,int x,int y,int width,int height)
//===================================================================
{
	return addNext(c).setTag(RECT,new Rect(x,y,width,height));
}
/*
//===================================================================
public Control add(Control c,Rect screenLocation)
//===================================================================
{
	return addNext(c).setTag(RECT,new Rect().set(screenLocation));
}
*/
//===================================================================
public Control addNext(Control c,boolean last)
//===================================================================
{
	if (grid == null) grid = new Grid();
	grid.add(c,last);
	lastAdded = c;
	if (c != null) {
		if ((defaultAddToMeCellConstraints & DONTCHANGE) == 0)
			c.setCell(defaultAddToMeCellConstraints);
		all = Vector.add(all,c);
	}
	return c;
}
//===================================================================
public Control addLast(Control c) {return addNext(c,true);}
public Control addNext(Control c) {return addNext(c,false);}
public Control endRow() {if (grid != null) grid.endRow(); return this;}
//===================================================================
//===================================================================
public Control addNext(Control c,int cellConstraints,int controlConstraints)
//===================================================================
{
	return addNext(c).setCell(cellConstraints).setControl(controlConstraints);
}
//===================================================================
public Control addLast(Control c,int cellConstraints,int controlConstraints)
//===================================================================
{
	return addLast(c).setCell(cellConstraints).setControl(controlConstraints);
}
/**
* This is the LayoutManager being used by the Panel. By default it will be the Panel itself. You
* can set this via a direct assignment.
**/
public LayoutManager layoutManager;
/**
* This is an optional background image to display.
**/
public Image backgroundImage;

protected Vector childListeners;
/**
* Calling setText() on a CellPanel gives it a labelled etched border.
**/
//===================================================================
public void setText(String text)
//===================================================================
{
	super.setText(text);
	if (this.text.length() != 0) {
		borderWidth = 4;
		borderStyle = EDGE_ETCHED|(ButtonObject.buttonEdge & BF_SOFT);
	}
}
//==================================================================
public void addChildListener(EventListener list)
//==================================================================
{
	if (childListeners == null) childListeners = new Vector();
	childListeners.remove(list);
	childListeners.add(list);
}
//==================================================================
public void removeChildListener(EventListener list)
//==================================================================
{
	if (childListeners == null) return;
	childListeners.remove(list);
}
//==================================================================
public void sendToChildListeners(Event ev)
//==================================================================
{
	Object t = ev.target;
	if (childListeners != null)
		for (int i = 0; i<childListeners.size(); i++){
			EventListener c = (EventListener)childListeners.get(i);
			ev.target = t;
			c.onEvent(ev);
		}
}


//===================================================================
public void make(boolean reMake)
//===================================================================
{
	if (made){// && !reMake) {
		//getPreferredSize();
		return;
	}
	super.removeAll();
	if (all != null)
		for (int i = 0;i<all.size();i++) {
			Control c = (Control)all.get(i);
			add(c);
			c.make(reMake);
		}
	/*
	makeLayoutGrid();
	if (defaultTags != null)
		if (defaultTags.size() == 0)
			defaultTags = null;
	*/
	getPreferredSize(null);
	if (backgroundImage != null) modifyAll(Transparent,0,false);
	made = true;
}
//===================================================================
public void layout(Grid controls,Panel panel,Rect panelRect)
//===================================================================
{
	if (widths == null) getPreferredSize(controls,panel,null);
	Dimension d = new Dimension(20,20);
	if (controls == null) return;
	int y = panelRect.y;
	for (int r = 0; r<controls.rows; r++){
		int x = panelRect.x;
		for (int c = 0; c<controls.columns; c++){
			int h = 0, width = 0;
			Control cn = (Control)controls.objectAt(r,c);
			if (cn == null) continue;
			d.width = widths[c];
			if (stretchFirstColumn && c == 0){
				int fw = 0;
				for (int cc = 1; cc < controls.columns; cc++)
					fw += widths[cc];
				d.width = panelRect.width-fw;
			}else if (!stretchFirstColumn && stretchLastColumn && c == controls.columns-1)
				d.width = panelRect.width-x+panelRect.x;

			d.height = heights[r];
			if (stretchFirstRow && r == 0){
				int fh = 0;
				for (int rr = 1; rr < controls.rows; r++)
					fh += heights[rr];
				d.height = panelRect.height-fh;
			}else if (!stretchFirstRow && stretchLastRow && r == controls.rows-1) {
				d.height = panelRect.height-y+panelRect.y;
			}
			//cn.getPreferredSize(d);
			width += d.width;
			h += d.height;
			Insets in = (Insets)panel.getControlTag(INSETS,cn,panel.noInsets);
			width += in.left+in.right;
			h += in.top+in.bottom;
			Rect cr = (Rect)getControlTag(RECT,cn,null);
			if (cr == null)
				cn.setRect(x+in.left,y+in.top,d.width-in.left-in.right,d.height-in.top-in.bottom);
			else{
				cn.setRect(cr.x,cr.y,cr.width,cr.height);
			}
			x += width;
		}
		y += heights[r];
	}
}

int [] widths, heights;

//===================================================================
public Dimension getPreferredSize(Grid controls,Panel panel,Dimension destination)
//===================================================================
{
	Dimension d = new Dimension();
	if (destination == null) destination = new Dimension();
	destination.set(0,0);
	if (controls == null) return null;
	widths = new int[controls.columns];
	heights = new int[controls.rows];
	for (int r = 0; r<controls.rows; r++){
		int height = 0;
		for (int c = 0; c<controls.columns; c++){
			int width = 0;
			int h = 0;
			Control cn = (Control)controls.objectAt(r,c);
			if (cn == null) continue;
			cn.getPreferredSize(d);
			width += d.width;
			h += d.height;
			Insets in = (Insets)panel.getControlTag(INSETS,cn,panel.noInsets);
			width += in.left+in.right;
			h += in.top+in.bottom;
			if (h > height) height = h;
			if (width > widths[c]) widths[c] = width;
		}
		heights[r] = height;
		destination.height += height;
	}
	for (int i = 0; i < widths.length; i++)
		destination.width += widths[i];

	return destination;
}

protected Vector all;
/**
* Use this to add a control directly to the Panel. It makes it a child control
* immediately.
**/
//===================================================================
public void addDirectly(Control c)
//===================================================================
{
	add(c);
	all = Vector.add(all,c);//all.add(c);
}
//===================================================================
public void remove(Control c)
//===================================================================
{
	if (all != null) all.remove(c);
	super.remove(c);
}
//===================================================================
public Iterator getSubControls() {return all == null ? new ObjectIterator(null) : all.iterator();}
//==================================================================

protected Control lastAdded = null;
//-------------------------------------------------------------------
protected boolean made = false;
protected boolean calculated = false;
protected int titleGap = 0;
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	preferredWidth = preferredHeight = 0;
	if (layoutManager != null) {
		if (true || !calculated){// || !quickRecalculate) {
			Dimension d = layoutManager.getPreferredSize(grid == null ? new Grid() : grid,this,new Dimension());
			preferredWidth = d.width+borderWidth*2;
			preferredHeight = d.height+borderWidth*2;
			/*
			minWidth = layout.minWidth+borderWidth*2;
			minHeight = layout.minHeight+borderWidth*2;
			*/
		}
	}
	if (text != null)
		if (text.length() != 0) {
			FontMetrics fm = getFontMetrics();
			preferredHeight += (titleGap = fm.getHeight()+4);
			int wd = fm.getTextWidth(text);
			if ((wd+14) > preferredWidth) preferredWidth = wd+14;
		}
	calculated = true;
}

//===================================================================
public void doBackground(Graphics g)
//===================================================================
{
	super.doBackground(g);
	if (backgroundImage != null)
		g.drawImage(backgroundImage,null,null,new Rect(0,0,backgroundImage.getWidth(),backgroundImage.getHeight()),new Rect(0,0,width,height),0);
}
//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	super.resizeTo(width,height);
	if (layoutManager != null) {
		layoutManager.layout(grid,this,new Rect(borderWidth,borderWidth+titleGap,this.width-borderWidth*2,this.height-titleGap-borderWidth*2));
	}
}

//==================================================================
public void doBorder(Graphics g)
//==================================================================
{
	int flags = getModifiers(true);
	int tg = titleGap/2;
	int lw = titleGap != 0 && text != null ? getFontMetrics().getTextWidth(text)+4 : 0;
	if (borderWidth != 0 || borderStyle != 0)
		if (lw == 0)
			g.draw3DRect(new Rect(0,tg,width,height-tg),borderStyle == 0 ? BDR_OUTLINE|BF_RECT : borderStyle,((flags & DrawFlat) != 0),null,borderColor);
		else
			g.draw3DRect(new Rect(0,tg,width,height-tg),borderStyle == 0 ? BDR_OUTLINE|BF_RECT : borderStyle,((flags & DrawFlat) != 0),null,borderColor,lw);
	if (titleGap != 0){
		g.setFont(getFont());
		if ((flags & Disabled) != 0) g.setColor(Color.DarkGray);
		else g.setColor(getForeground());
		g.drawText(text,6,2);
	}
}
/**
* Force a recalculation of all preferredSize() and resizing/positioning of the panel.
*/
//===================================================================
public void relayout(boolean redisplay)
//===================================================================
{
	reShow(x,y,width,height);
	if (redisplay) repaintNow();
}
//===================================================================
public void reShow(int x,int y,int toWidth,int toHeight)
//===================================================================
{
	modifyAll(ForceResize,CalculatedSizes,true);
	calculated = false;
	if (layoutManager == null) return;
	layoutManager.getPreferredSize(grid == null ? new Grid() : grid,this,new Dimension());
	setRect(x,y,toWidth,toHeight);
}
/**
* This forces a recalculation of my preferred size, but not those of my children.
**/
//===================================================================
public void relayoutMe(boolean redisplay)
//===================================================================
{
	modify(ForceResize,CalculatedSizes);
	if (layoutManager == null) return;
	int toWidth = width, toHeight = height;
	//width = height = 0;
	setRect(x,y,toWidth,toHeight);
	if (redisplay) repaintNow();
}


//##################################################################
}
//##################################################################

