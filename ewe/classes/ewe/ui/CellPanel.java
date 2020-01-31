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
* This is the most flexible Panel in the Ewe library. It places controls in a Grid, but
* also has the ability to individually specify how each cell and control behaves in the grid.
* It can even specify that individual controls span across multiple rows or columns.
**/
//##################################################################
public class CellPanel extends Panel{
//##################################################################
{
	modify(NoFocus,0);
}
//===================================================================
public CellPanel() {}
//===================================================================
public boolean quickRecalculate = false;
//-------------------------------------------------------------------
/**
* This contains the controls added to the Panel. It will be null until the first control is added.
**/
//protected Vector all;// = new Vector();
protected Layout layout = null;
/**
* This is the splitter to the left or above this panel (if any).
**/
public PanelSplitter mySplitter = null;
/**
* This is the splitter to the right of below this panel (if any).
**/
public PanelSplitter nextSplitter = null;

public boolean equalWidths, equalHeights;
//------------------------------------------------------------------
//protected Vector childListeners;
/**
* Set this to be a backgroundImage.
**/
//public Image backgroundImage = null;

/**
* Calling setText() on a CellPanel gives it a labelled etched border.
**/
/*
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
*/

/**
* Use this to add a control directly to the CellPanel. It makes it a child control
* immediately.
**/
/*
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
//-------------------------------------------------------------------
*/
//boolean calculated = false;
//int titleGap = 0;

//===================================================================
protected void calculateSizes()
//===================================================================
{
	preferredWidth = preferredHeight = 0;
	if (layout != null) {
		if (!calculated || !quickRecalculate) {
			layout.calculate();
			Dimension d = layout.getPreferredSize(new Dimension());
			preferredWidth = d.width+borderWidth*2;
			preferredHeight = d.height+borderWidth*2;
			minWidth = layout.minWidth+borderWidth*2;
			minHeight = layout.minHeight+borderWidth*2;
		}else {
			//Dimension d = layout.getPreferredSize(new Dimension());
			Dimension d = new Dimension();
			//int [] hs = new int[grid.rows];
			if (grid != null)
				for (int r = 0; r<grid.rows; r++){
					int w = 0;
					int h = 0;
					for (int c = 0; c<grid.columns; c++){
						Control ct = (Control)grid.objectAt(r,c);
						if (ct == null) continue;
						ct.getPreferredSize(d);
						w += d.width;
						if (d.height > h) h = d.height;
					}
					if (w > preferredWidth) preferredWidth = w;
					preferredHeight += h;
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
}
/*
//===================================================================
public void doBackground(Graphics g)
//===================================================================
{
	super.doBackground(g);
	if (backgroundImage != null)
		g.drawImage(backgroundImage,null,null,new Rect(0,0,backgroundImage.getWidth(),backgroundImage.getHeight()),new Rect(0,0,width,height),0);
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
*/
/*
//===================================================================
public void paintControl(Graphics g)
//===================================================================
{
	Dimension d = getSize();
	g.setColor(new Color(0xff,0xff,0xff,0x80));//Color.white);
	g.fillRect(0,0,d.width,d.height);
	g.setColor(Color.black);
	g.drawRect(0,0,d.width-1,d.height-1);
//..................................................................
	Insets in = (Insets)getFrom(cellConstraints,Inset,mInsets.empty);
	Rectangle r = mInsets.insetInto(in,0,0,d.width,d.height,null);
	//System.out.println(r);
	g.setColor(new Color(0x00,0xff,0x00,0x80));//Color.green);
	g.fillRect(r.x,r.y,r.width,r.height);
	g.setColor(Color.black);
	g.drawRect(r.x,r.y,r.width-1,r.height-1);
}
*/
//===================================================================
public void removeAll()
//===================================================================
{
	if (grid != null) grid.clear();
	super.removeAll();
	made = false;
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
	makeLayoutGrid();
	if (defaultTags != null)
		if (defaultTags.size() == 0)
			defaultTags = null;
	getPreferredSize(null);
	if (backgroundImage != null) modifyAll(Transparent,0,false);
	made = true;
}
//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	this.width = width;
	this.height = height;
	if (mApp.mainApp == null && parent != null) repaint();
	/* This does not work - was trying to temporarily remove the layout.
	if (made && layout == null) {
		makeLayoutGrid();
		if (layout != null) {
			layout.calculate();
			layout.getPreferredSize(null);
			getPreferredSize(null);
		}
	}
	*/
	if (layout != null) {
		layout.setRect(borderWidth,borderWidth+titleGap,this.width-borderWidth*2,this.height-titleGap-borderWidth*2);
		//Find a good way
	}
}

//-------------------------------------------------------------------
protected static Dimension autoSpan = new Dimension(-1,-1);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected Grid makeLayoutGrid()
//-------------------------------------------------------------------
{
	if (grid == null) return null;
	layout = new Layout(this);
	calculated = false;
	modify(0,CalculatedSizes);
	int dr = 0, dc = 0;
//..................................................................
	if (grid != null)
		for (int r = 0; r<grid.rows; r++){
			dc = 0;
			for (int c = 0; c<grid.columns; c++){
				while(layout.objectAt(dr,dc) != null) dc++;
				Control tc = (Control)grid.objectAt(r,c);
				if (tc == null) continue;
				Dimension d = (Dimension)getControlTag(SPAN,tc,autoSpan);
				LayoutEntry first = null;
				int sw = d.width; if (sw < 1) sw = 1;
				int sh = d.height; if (sh < 1) sh = 1;
				for (int w = 0; w<sw; w++){
					for (int h = 0; h<sh; h++){
						if (first == null) {
							first = new LayoutEntry();
							first.topLeft = first;
							first.row = dr; first.column = dc;
							first.width = sw; first.height = sh;
							first.control = tc;
							layout.set(dr+h,dc+w,first);
						}else
							layout.set(dr+h,dc+w,first.getSubEntry());
					}
				}
				dc++;
			}
			dr++;
		}
	layout.autospan(true);
	layout.autospan(false);
	return layout;
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
	modifyAll(0,CalculatedSizes,true);
	if (layout == null) return;
	layout.calculate();
	width = height = 0;
	setRect(x,y,toWidth,toHeight);
}
/**
* This forces a recalculation of my preferred size, but not those of my children.
**/
//===================================================================
public void relayoutMe(boolean redisplay)
//===================================================================
{
	if (false) modifyAll(0,CalculatedSizes,true);
	else modify(0,CalculatedSizes);
	if (layout == null) return;
	layout.calculate();
	int toWidth = width, toHeight = height;
	width = height = 0;
	setRect(x,y,toWidth,toHeight);
	if (redisplay) repaintNow();
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof ControlEvent || ev instanceof DataChangeEvent)
		if (ev.target != this) {
			sendToChildListeners(ev);
		}
	super.onEvent(ev);
}

//==================================================================
public void resize(int maxOrMin)
//==================================================================
{
	if (mySplitter == null) return;
	mySplitter.resize(maxOrMin);
}
//===================================================================
void splitterSetTo(PanelSplitter sp,int dx,int dy)
//===================================================================
{
	if (layout == null) return;
	layout.splitterSetTo(sp,sp.x+dx-borderWidth,sp.y+dy-borderWidth,borderWidth,borderWidth,this.width-borderWidth*2,this.height-borderWidth*2);
}
//##################################################################
}
//##################################################################

	//##################################################################
	class Layout extends Grid implements CellConstants{
	//##################################################################
	protected PanelSplitter splitter;
	public int preferredWidth, preferredHeight, minWidth, minHeight;
	int [] widths, heights, minWidths, minHeights;
	boolean [] growColumns, shrinkColumns, growRows, shrinkRows;
	CellPanel panel;
	//===================================================================
	public Layout(CellPanel panel){this.panel = panel;}
	//===================================================================
	//===================================================================
	public Dimension getPreferredSize(Dimension d)
	//===================================================================
	{
		d = Dimension.unNull(d);
		d.width = preferredWidth;
		d.height = preferredHeight;
		return d;
	}
	//===================================================================
	public void calculate()
	//===================================================================
	{
	//..................................................................
		widths = new int [columns];
		minWidths = new int[columns];
		growColumns = new boolean[columns];
		shrinkColumns = new boolean[columns];
		heights = new int [rows];
		minHeights = new int[rows];
		growRows = new boolean[rows];
		shrinkRows = new boolean[rows];
		int maxW = 0, maxH = 0;
		for (int i = 0; i<rows; i++) {
			growRows[i] = shrinkRows[i] = false;
			minHeights[i] = heights[i] = 0;
		}
		for (int i = 0; i<columns; i++) {
			growColumns[i] = shrinkColumns[i] = false;
			minWidths[i] = widths[i] = 0;
		}
		Dimension d = new Dimension();
		int [] sizes = new int[6];
	//..................................................................
		for (int r = 0; r<rows; r++)
			for (int c = 0; c<columns; c++){
				LayoutEntry le = (LayoutEntry)objectAt(r,c);
				if (le == null) continue;
				Control cn = le.control;
				/*
				if ((le.control instanceof Panel) && !(le.control instanceof CellPanel))
						new Exception().printStackTrace();
				*/
				if (cn instanceof PanelSplitter) splitter = (PanelSplitter)cn;
				cn.getSizes(sizes);
				d.width = sizes[0]; d.height = sizes[1];
				boolean closed = /*cn.hasModifier(cn.AddToPanelClosed,false) || */((cn.constraints & INITIALLY_CLOSED) != 0);
				if (closed) d.width = d.height = 0;
				else if ((cn.constraints & INITIALLY_MINIMIZED) != 0){
					d.width = sizes[2]; d.height = sizes[3];
					closed = true;
				}else if ((cn.constraints & INITIALLY_PREFERRED_SIZE) != 0){
					closed = true;
				}
				Insets in = (Insets)panel.getControlTag(INSETS,cn,panel.noInsets);
				boolean empty = d.width <= 0 || d.height <= 0;
				if (!empty) {
					d.width += in.left+in.right;
					d.height += in.top+in.bottom;
				}else{
					d.width = d.height = 0;
				}
				int w = d.width/le.width;
				if (w <= 0 && !empty) w = 1;
				int h = d.height/le.height;
				if (h <= 0 && !empty) h = 1;
				//......................................................
				// A preferred width or height of 0 will result in the control occupying NO space
				// even if it has insets.
				//......................................................
				if (widths[c] < w) {
					widths[c] = w;
					if (maxW < w) maxW = w;
				}
				if (heights[r] < h) {
					heights[r] = h;
					if (maxH < h) maxH = h;
				}
				if ((w != 0 && h != 0) || closed){
					if ((cn.constraints & HGROW) != 0) growColumns[c] = true;
					if ((cn.constraints & HSHRINK) != 0) shrinkColumns[c] = true;
					if ((cn.constraints & VGROW) != 0) growRows[r] = true;
					if ((cn.constraints & VSHRINK) != 0) shrinkRows[r] = true;
				}
				int mw = sizes[2], mh = sizes[3];
				if (!shrinkColumns[c]) mw = w;
				if (!shrinkRows[r]) mh = h;
				if (minWidths[c] < mw) minWidths[c] = mw;
				if (minHeights[r] < mh) minHeights[r] = mh;
			}
		//..................................................................
		if (panel.equalWidths) for (int i = 0; i<columns; i++) widths[i] = maxW;
		if (panel.equalHeights) for (int i = 0; i<rows; i++) heights[i] = maxH;
		preferredWidth = sum(widths);
		preferredHeight = sum(heights);
		minWidth = sum(minWidths);
		minHeight = sum(minHeights);
	}
	//===================================================================
	public void autospan(boolean horizontally)
	//===================================================================
	{
		for (int r = 0; r<rows; r++){
			for (int c = 0; c<columns; c++){
				LayoutEntry le = (LayoutEntry)objectAt(r,c);
				if (le == null) continue;
				if (le.topLeft != le) continue;
				Control cn = le.control;
				Dimension d = (Dimension)panel.getControlTag(SPAN,cn,CellPanel.autoSpan);
				if (d.width < 0 && horizontally){
					while(true){
						if ((c+le.width) >= columns) break; //Already extends to the end.
						boolean all = true;
						for (int i = 0; i<le.height; i++)
							if (objectAt(r+i,c+le.width) != null) all = false;
						if (!all) break; // All of the cells to the right are not unoccupied.
						//......................................................
						// Expand it now.
						//......................................................
						le.width++;
						for (int rr = 0; rr<le.height; rr++)
							for (int cc = 0; cc<le.width; cc++)
								if (cc != 0 || rr != 0)
									set(r+rr,c+cc,le.getSubEntry());
					}
				}
				if (d.height < 0 && !horizontally){
					while(true){
						if ((r+le.height) >= rows) break; //Already extends to the end.
						boolean all = true;
						for (int i = 0; i<le.width; i++)
							if (objectAt(r+le.height,c+i) != null) all = false;
						if (!all) break; // All of the cells to the right are not unoccupied.
						//......................................................
						// Expand it now.
						//......................................................
						le.height++;
						for (int rr = 0; rr<le.height; rr++)
							for (int cc = 0; cc<le.width; cc++)
								if (cc != 0 || rr != 0)
									set(r+rr,c+cc,le.getSubEntry());
					}
				}
			}

		}
	}
	//-------------------------------------------------------------------
	protected int howMany(boolean [] use)
	//-------------------------------------------------------------------
	{
		int total = 0;
		for (int i = 0; i<use.length; i++)
			if (use[i]) total++;
		return total;
	}
	//-------------------------------------------------------------------
	protected int sum(int [] values,boolean [] use)
	//-------------------------------------------------------------------
	{
		int total = 0;
		for (int i = 0; i<values.length && i<use.length; i++)
			if (use[i]) total += values[i];
		return total;
	}
	//-------------------------------------------------------------------
	protected int sum(int [] values)
	//-------------------------------------------------------------------
	{
		int total = 0;
		for (int i = 0; i<values.length; i++) total += values[i];
		return total;
	}
	//-------------------------------------------------------------------
	protected int [] scaleAcross(int [] values,boolean [] use,int newValue)
	//-------------------------------------------------------------------
	{
		int original = sum(values);
		int diff = newValue-original;
		int [] scaled = new int[values.length];
		int num = howMany(use);
		int total = sum(values,use);
		if (total == 0) total = num;
		int left = diff;
		int did = 0;
		for (int i = 0; i<scaled.length; i++){
			scaled[i] = values[i];
			if (use[i]) {
				int ch = (diff*values[i])/total;
				if (did == num-1) ch = left;
				scaled[i] += ch;
				left -= ch;
				did++;
			}
		}
		return scaled;
	}
	boolean firstTime = true;
	//===================================================================
	void splitterSetTo(PanelSplitter sp,int x,int y,int dx,int dy,int width,int height)
	//===================================================================
	{
		Dimension d = new Dimension();
		int idx = panel.all != null ? panel.all.find(sp) : -1;
		if (idx < 1 || idx >= panel.all.size()-1) return;
		Control one = sp.before, two = sp.after;
		if (sp.type == sp.VERTICAL){
			if (firstTime){
				if (widths[idx-1] == 0) widths[idx-1] = one.getPreferredSize(d).width;
				if (widths[idx+1] == 0) widths[idx+1] = two.getPreferredSize(d).width;
			}
			int left = x;
			int right = width-(x+sp.width);
			if (curWidths != null){
				curWidths[0] = left;
				curWidths[1] = sp.width;
				curWidths[2] = right;
			}
			fitInto(one,dx,dy,left,height,one.getPreferredSize(d));
			fitInto(sp,dx+x,dy+y,sp.width,height,sp.getPreferredSize(d));
			fitInto(two,dx+(width-right),dy,right,height,two.getPreferredSize(d));
			int nt = right+left;
			if (nt == 0) return;
			int before = widths[idx-1]+widths[idx+1];
			widths[idx-1] = (before*left)/nt;
			widths[idx+1] = before-widths[idx-1];
		}else{
			if (firstTime){
				if (heights[idx-1] == 0) heights[idx-1] = one.getPreferredSize(d).height;
				if (heights[idx+1] == 0) heights[idx+1] = two.getPreferredSize(d).height;
			}
			int top = y;
			int bottom = height-(y+sp.height);
			if (curHeights != null){
				curHeights[0] = top;
				curHeights[1] = sp.height;
				curHeights[2] = bottom;
			}
			fitInto(one,dx,dy,width,top,one.getPreferredSize(d));
			fitInto(sp,dx+x,dy+y,width,sp.height,sp.getPreferredSize(d));
			fitInto(two,dx,dy+(height-bottom),width,bottom,two.getPreferredSize(d));
			int nt = top+bottom;
			if (nt == 0) return;
			int before = heights[idx-1]+heights[idx+1];
			heights[idx-1] = (before*top)/nt;
			heights[idx+1] = before-heights[idx-1];
		}
		firstTime = false;
		panel.repaintNow();
	}
	int curWidths [], curHeights [];
	//===================================================================
	public void setRect(int dx,int dy,int width,int height)
	//===================================================================
	{
		int [] scaledWidths = widths;
		int [] scaledHeights = heights;
		int pw = preferredWidth;
		int ph = preferredHeight;
		boolean fixedWidth = false, fixedHeight = false;
		if (curWidths != null && splitter != null){
			if (splitter.state != 0){
				if (splitter.type == splitter.HORIZONTAL){
					fixedHeight = true;
					scaledHeights = curHeights;
					boolean fixBefore = false;
					if (splitter.state == splitter.OPENED)
						fixBefore = ((splitter.openType & splitter.BEFORE) != 0);
					else if (splitter.state == splitter.CLOSED)
						fixBefore = ((splitter.closeType & splitter.BEFORE) != 0);
					if (fixBefore) curHeights[2] = height-curHeights[0]-curHeights[1];
					else curHeights[0] = height-curHeights[2]-curHeights[1];
				}
				else if (splitter.type == splitter.VERTICAL){
					fixedWidth = true;
					scaledWidths = curWidths;
					boolean fixBefore = false;
					if (splitter.state == splitter.OPENED)
						fixBefore = ((splitter.openType & splitter.BEFORE) != 0);
					else if (splitter.state == splitter.CLOSED)
						fixBefore = ((splitter.closeType & splitter.BEFORE) != 0);
					if (fixBefore) curWidths[2] = width-curWidths[0]-curWidths[1];
					else curWidths[0] = width-curWidths[2]-curWidths[1];
				}
			}else{
				if (splitter.type == splitter.HORIZONTAL){
					fixedHeight = true;
					scaledHeights = curHeights;
					int t = curHeights[0]+curHeights[2];
					//curHeights[1] is the height of the splitter itself.
					if (t > 0){
						int left = height;
						curHeights[0] = (curHeights[0]*(height-curHeights[1]))/t;
						curHeights[2] = height-curHeights[1]-curHeights[0];
					}
				}else{
					fixedWidth = true;
					scaledWidths = curWidths;
					int t = curWidths[0]+curWidths[2];
					//curWidths[1] is the width of the splitter itself.
					if (t > 0){
						int left = width;
						curWidths[0] = (curWidths[0]*(width-curWidths[1]))/t;
						curWidths[2] = width-curWidths[1]-curWidths[0];
					}
				}
			}
		}
		if (!fixedWidth){
			if (pw > width)
				scaledWidths = scaleAcross(widths,shrinkColumns,width);
			else if (pw < width)
				scaledWidths = scaleAcross(widths,growColumns,width);
		}
		if (!fixedHeight){
			if (ph > height)
				scaledHeights = scaleAcross(heights,shrinkRows,height);
			else if (ph < height)
				scaledHeights = scaleAcross(heights,growRows,height);
		}
//..................................................................
		Dimension d = new Dimension();
		for (int r = 0; r<rows; r++)
			for (int c = 0; c<columns; c++){
				LayoutEntry le = (LayoutEntry)objectAt(r,c);
				if (le == null) continue;
				if (le.topLeft == le){
					int x = 0, y = 0;
					for (int xx = 0; xx<c; xx++) x+=scaledWidths[xx];
					for (int yy = 0; yy<r; yy++) y+=scaledHeights[yy];
					int w = 0, h = 0;
					for (int xx = c; xx<c+le.width; xx++) w += scaledWidths[xx];
					for (int yy = r; yy<r+le.height; yy++) h += scaledHeights[yy];
					if (le.control != null) fitInto(le.control,dx+x,dy+y,w,h,le.control.getPreferredSize(d));
				}
			}
//..................................................................
		curWidths = scaledWidths;
		curHeights = scaledHeights;
//..................................................................
	}

	//-------------------------------------------------------------------
	protected void fitInto(Control c,int x,int y,int w,int h,Dimension p)
	//-------------------------------------------------------------------
	{
		//if (panel instanceof mTabbedPanel && panel.debugFlag && c instanceof CardPanel) ((Object)null).toString();
		Insets in = (Insets)panel.getControlTag(INSETS,c,panel.noInsets);
		x += in.left; y += in.top;
		w -= (in.left+in.right);
		h -= (in.top+in.bottom);
		int con = c.constraints;
		if (w > 0 && h > 0){
			int myW = p.width, myH = p.height;
			if (myW > w && ((con & HCONTRACT) != 0)) myW = w;
			if (myW < w && ((con & HEXPAND) != 0)) myW = w;
			if (myH > h && ((con & VCONTRACT) != 0)) myH = h;
			if (myH < h && ((con & VEXPAND) != 0)) myH = h;

			if ((con & RIGHT) != 0) x += (w-myW);
			else if ((con & LEFT) != 0);
			else x += (w-myW)/2;

			if ((con & BOTTOM) != 0) y += (h-myH);
			else if ((con & TOP) != 0);
			else y += (h-myH)/2;

			w = myW;
			h = myH;
		}
		c.setRect(x,y,w,h);
		//c.setLocation(x,y);
		//c.requestResizeTo(w,h);
	}

	//##################################################################
	}
	//##################################################################


