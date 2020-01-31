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

//##################################################################
public class ScrollBarPanel extends ScrollablePanel{
//##################################################################
/**
* The horizontal scroll bar. This is ALWAYS present even if the
* scrollbar is not displayed on the screen.
**/
public mScrollBar hbar = new mScrollBar(mScrollBar.Horizontal);
/**
* The vertical scroll bar. This is ALWAYS present even if the
* scrollbar is not displayed on the screen.
**/
public mScrollBar vbar = new mScrollBar(mScrollBar.Vertical);
/**
* Set the sizes for the scroll bar buttons and thumb bar.
* @param buttonWidth the preferred button width.
* @param buttonHeight the preferred button height.
* @param minThumb the preferred minimum thumb bar thickness.
**/
public void setScrollBarSize(int buttonWidth, int buttonHeight, int minThumb)
{
	hbar.setPreferredSizes(buttonWidth,buttonHeight,minThumb);
	vbar.setPreferredSizes(buttonWidth,buttonHeight,minThumb);
}
/**
 * Create a new ScrollBarPanel for the specified client.
 */
//==================================================================
public ScrollBarPanel(ScrollClient client,int options)
//==================================================================
{
	super(client);
 	hbar = new mScrollBar(mScrollBar.Horizontal,options);
	vbar = new mScrollBar(mScrollBar.Vertical,options);
	client = this.client;
	addNext((Control)client);
	addLast(vbar).setCell(VSTRETCH).setTag(SPAN,new Dimension(1,1));
	addLast(hbar).setCell(HSTRETCH).setTag(SPAN,new Dimension(1,1));
	vbar.modify(Invisible|ShrinkToNothing|AlwaysRecalculateSizes,0);
	hbar.modify(Invisible|ShrinkToNothing|AlwaysRecalculateSizes,0);
	setOptions(options);
}
/**
 * Create a new ScrollBarPanel for the specified client and with the specified options.
 */
//==================================================================
public ScrollBarPanel(ScrollClient client)
//==================================================================
{
	this(client,0);
}
/**
* Set options for the ScrollablePanel.
**/
//==================================================================
public void setOptions(int options)
//==================================================================
{
	super.setOptions(options);
	if ((options & Permanent) != 0) options |= AlwaysShowHorizontalScrollers|AlwaysShowVerticalScrollers;

	if ((options & AlwaysShowHorizontalScrollers) != 0)
		hbar.modify(0,Invisible|ShrinkToNothing);
	if ((options & AlwaysShowVerticalScrollers) != 0)
		vbar.modify(0,Invisible|ShrinkToNothing);
/*
	if ((options & Permanent) != 0) {
		alwaysShow = true;
		vbar.modify(0,Invisible|ShrinkToNothing);//|AlwaysRecalculateSizes);
		hbar.modify(0,Invisible|ShrinkToNothing);//|AlwaysRecalculateSizes);
	}
	*/
}
boolean firstTime = true;
boolean dontPaint = false;
//==================================================================
public void checkScrolls(int width,int height,boolean reDraw)
//==================================================================
{
	//ewe.sys.Vm.debug("checkScrolls!");
	boolean prev = dontPaint;
	try{
		if (!reDraw) dontPaint = true;
		if (blockUpdate) return;
		if (width == 0 || height == 0) return;
		boolean noH = (options & NeverShowHorizontalScrollers) != 0;
		boolean noV = (options & NeverShowVerticalScrollers) != 0;
		int clientConstraints = getClientConstraints();
		if ((clientConstraints & HCONTRACT) != 0) noH = true;
		if ((clientConstraints & VCONTRACT) != 0) noV = true;
		boolean needHorizontal = !noH && client.needScrollBar(Horizontal,width);
		boolean needVertical = !noV && client.needScrollBar(Vertical,height);
		if (!alwaysShow && !neverShow) {
			boolean hWasVisible = !hbar.hasModifier(ShrinkToNothing,false);
			boolean vWasVisible = !vbar.hasModifier(ShrinkToNothing,false);
			if (hWasVisible && !hscrollerChanges) needHorizontal = true;
			if (vWasVisible && !vscrollerChanges) needVertical = true;

			if (hscrollerChanges) hbar.modify(0,vanish);
			if (vscrollerChanges) vbar.modify(0,vanish);

			Dimension d = new Dimension();
			if (needHorizontal && !needVertical && !noV) {
				needVertical = client.needScrollBar(Vertical,height-hbar.recalculatePreferredSize(d).height);
					//client.getActual(Vertical) > client.getVisible(Vertical,height-hbar.recalculatePreferredSize(null).height);
			}else if (needVertical && !needHorizontal && !noH) {
				needHorizontal = client.needScrollBar(Horizontal,width-vbar.recalculatePreferredSize(d).width);
					//client.getActual(Horizontal) > client.getVisible(Horizontal,width-vbar.recalculatePreferredSize(null).width);
			}
			if (!needHorizontal && hscrollerChanges) hbar.modify(vanish,0);
			if (!needVertical && vscrollerChanges) vbar.modify(vanish,0);

			boolean hChanged = (hWasVisible != needHorizontal);
			boolean vChanged = (vWasVisible != needVertical);

			blockUpdate = true;
			if ((hChanged && hWasVisible) || (!needHorizontal && firstTime))
					if (client.getCurrent(Horizontal) != 0) client.doScroll(Horizontal,TrackTo,0);
			if ((vChanged && vWasVisible) || (!needVertical && firstTime))
					if (client.getCurrent(Vertical) != 0) client.doScroll(Vertical,TrackTo,0);
			firstTime = false;
			if (hChanged || vChanged) {
				//ewe.sys.Vm.debug("Relayout: "+reDraw);
				relayoutMe(reDraw);
			}
			blockUpdate = false;
		}else{
			if (!needHorizontal && (client.getCurrent(Horizontal) != 0))
				client.doScroll(Horizontal,TrackTo,0);
			if (!needVertical && (client.getCurrent(Vertical) != 0))
				client.doScroll(Vertical,TrackTo,0);
		}
		updateScroll(Horizontal);
		updateScroll(Vertical);
	}finally{
		dontPaint = prev;
	}
	//System.out.println(needHorizontal+","+needVertical);
}
/*
public void make(boolean reMake)
{
	super.make(reMake);
	checkScrolls();
}
*/
//==================================================================
public void updateScroll(int which)
//==================================================================
{
	if (blockUpdate) return;
	int ac = client.getActual(which);
	//hbar.getPreferredSize();
	//vbar.getPreferredSize();
	int vs = 0;
	if (which == Horizontal){
		//System.out.println("H:"+vbar.preferredSize.width);
	 	vs = ((Control)client).getRect().width;//-vbar.preferredSize.width;
		//System.out.println("H:"+vs+","+ac);
	}
	else if (which == Vertical) {
		//System.out.println("V:"+hbar.preferredSize.height);
		vs = ((Control)client).getRect().height;//-hbar.preferredSize.height;
		//System.out.println("V:"+vs+","+ac);
	}
	vs = client.getVisible(which,vs);
	//System.out.println("V/H:"+vs+","+ac);
	setScroll(which,vs,ac,client.getCurrent(which));
}

//==================================================================
public void setScroll(int which,int v,int a,int c)
//==================================================================
{
	mScrollBar sb = vbar;
	if (which == Horizontal) sb = hbar;
	sb.set(v,a,c,!dontPaint);
}
//==================================================================
public void onScrollEvent(ScrollEvent se)
//==================================================================
{
	if (se.target == hbar) client.doScroll(Horizontal,se.action,se.value);
	else if (se.target == vbar) client.doScroll(Vertical,se.action,se.value);
}
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	if (ev instanceof ScrollEvent)
		onScrollEvent((ScrollEvent)ev);
	else
		super.onEvent(ev);
}
//===================================================================
public void modifyScrollers(int set,int clear)
//===================================================================
{
	hbar.modify(set,clear);
	vbar.modify(set,clear);
}
//===================================================================
public void dismantle(Control downTo)
//===================================================================
{
	if (this == downTo) return;
	super.dismantle(downTo);
	hbar = vbar = null;
}
//##################################################################
}
//##################################################################

