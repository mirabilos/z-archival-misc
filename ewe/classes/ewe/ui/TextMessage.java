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
import ewe.util.Vector;
import ewe.util.mString;

/**
* This displays static text with automatic word wrapping. If you want to get it
* to size itself to fit the text exactly do the following.<p>
* <nl>
* <li>Construct it with the intended text.
* <li>If it is going to be using a Font other than the standard Gui font, then set the font variable first.
* <li>Call setPreferredSize(width,-1) - specifying a fixed width (in pixels) and a negative height.
* </nl><p>
* Now it will calculate the number of lines required to display the text and therefore calculate the preferred
* height.
**/
//##################################################################
public class TextMessage extends MessageArea implements ScrollClient{
//##################################################################
//===================================================================
public int minXScroll = 70;
public boolean autoWrap = true;
//===================================================================
{
	alignment = Gui.Left;
	anchor = Gui.NORTHWEST;
}
//-------------------------------------------------------------------
protected int numLines = 0, widest = 0, lineHeight = 0;
//-------------------------------------------------------------------
//==================================================================
public TextMessage(int rows,int columns) {super(rows,columns);}
public TextMessage(String text) {super(text);}
//==================================================================
//===================================================================
public void setText(String txt)
//===================================================================
{
	if (txt == null) txt = "";
	text = txt;
	xPos = startLine = 0;
	update();
	repaintNow();
}
//===================================================================
public void update()
//===================================================================
{
	lines = splitLines();
	if (ss != null) ss.checkScrolls();
}
private boolean alreadyCalculated = false;
//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	if (alreadyCalculated) return;
	Rect r;
 	if (rows != 0 && columns != 0) {
		r = Gui.getAverageSize(getFontMetrics(),rows,columns,spacing,spacing);
	}
	else {
		Dimension d = (Dimension)getTag(PREFERREDSIZE,new Dimension(200,-1));
		splitLines(d.width-spacing*2,getFontMetrics());
		r = new Rect();
		r.height = lineHeight*numLines+spacing*2;
		r.width = d.width;
		d = (Dimension)getTag(MAXIMUMSIZE,new Dimension(-1,200));
		r.height = Math.min(d.height,r.height);
	}
	preferredWidth = r.width; preferredHeight = r.height;
}
//===================================================================
public boolean willFitIn(String text,int maxWidth)
//===================================================================
{
	if (text == null) text = new String();
	this.text = text;
	splitLines(maxWidth-spacing*2,getFontMetrics());
	int actual = mString.split(text,'\n').length;
	return (numLines <= actual);
}
//===================================================================
public boolean fitToScreen(String text)
//===================================================================
{
	Rect screenSize = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
	if (screenSize.width > 480) screenSize.width = 480;
	if (!willFitIn(text,screenSize.width-4)){
		setPreferredSize(screenSize.width-4,-1);
		return false;
	}else
		return true;
}
//===================================================================
public Control tryMessageArea(Control parent,Font f,String text,MessageArea area)
//===================================================================
{
	area.font = f != null ? f : parent != null ? parent.getFont() : null;
	font = f != null ? f : parent != null ? parent.getFont() : null;
	area.setText(text);
	if (fitToScreen(text)) return area;
	int l = text.length();
	for (int i = 1; i<l-1; i++){
		char c = text.charAt(i);
		if (c != '\n') continue;
		if (text.charAt(i-1) == ' ' || text.charAt(i+1) == ' '){
			text = text.substring(0,i)+text.substring(i+1);
			l--;
		}
	}
	this.text = text;
	return this;
}
//===================================================================
public String [] splitLines() {return splitLines(width,getFontMetrics());}
//===================================================================
//===================================================================
public String [] splitLines(int width,FontMetrics fm)
//===================================================================
{
	lineHeight = fm.getHeight()+fm.getLeading();
	DisplayLine got = DisplayLine.split(text,fm,width-spacing*2,autoWrap ? 0 : DisplayLine.SPLIT_NO_WRAP);
	if (got == null) {
		got = new DisplayLine();
		got.line = "";
	}
	widest = DisplayLine.widest;
	String [] ret = DisplayLine.toLines(got);
	numLines = ret.length;
	return ret;
}
//===================================================================
public String [] oldSplitLines()
//===================================================================
{
	FontMetrics fm = getFontMetrics();
	lineHeight = fm.getHeight()+fm.getLeading();
	if (width == 0 || !autoWrap) {
		widest = 0;
		String [] ln = super.splitLines();
		for (int i = 0; i<ln.length; i++) {
			int lw = fm.getTextWidth(ln[i]);
			if (lw > widest) widest = lw;
		}
		numLines = ln.length;
		return ln;
	}
	Vector v = new Vector();
	int spaceWidth = fm.getCharWidth(' ');
	int width = this.width-spacing*2;
	widest = 0;
	String [] all = mString.split(text,'\n');
	for (int j = 0; j<all.length; j++){
		String txt = all[j];
		int lastC = 0, added = 0;
		//if (txt == null) txt = "";
		int len = txt.length();
		boolean madeLine = false;
		StringBuffer sb = new StringBuffer();
		while(true){
			int i = 0;
			for (i = lastC; i<len ; i++)
				if (txt.charAt(i) > ' ') break;
			if (i == len){
				if (added != 0) {
					v.add(sb.toString());
					madeLine = true;
				}
				if (!madeLine) v.add("");
				break;
			}
			int st = i;
			for (i++;i<len;i++)
				if (txt.charAt(i) <= ' ') break;
			int end = i;
			String toAdd = txt.substring(st,end);
			int taw = fm.getTextWidth(toAdd);
			if (added+spaceWidth+taw <= width && added != 0){
				sb.append(' ');
				added += spaceWidth;
			}else if (added != 0) {
				madeLine = true;
				v.add(sb.toString());
				sb = new StringBuffer();
				added = 0;
			}
			sb.append(toAdd);
			added += taw;
			if (taw > widest) widest = taw;
			lastC = i;
		}
	}
	numLines = v.size();
	String [] l = new String[numLines];
	for (int i = 0; i<numLines; i++) l[i] = (String)v.get(i);
	return l;
}
//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	super.resizeTo(width,height);
	lines = splitLines();
	//update();
}
//ScrollServer ss;
//===================================================================
//public void setServer(ScrollServer server) {ss = server;}
//===================================================================
//===================================================================
public int getActual(int which)
//===================================================================
{
	if (lines == null) update();
	if (which == Horizontal) return widest;
	else return numLines;
}
//===================================================================
public int getVisible(int which,int forSize)
//===================================================================
{
	if (lines == null) update();
	if (which == Horizontal) return forSize-spacing*2;
	else return (forSize-spacing*2)/lineHeight;
}
//===================================================================
public int getCurrent(int which)
//===================================================================
{
	if (which == Horizontal) return xPos;
	else return startLine;
}
//===================================================================
public void doScroll(int which,int action,int value)
//==================================================================
{
	if (which == IScroll.Vertical) {
		int sr = getScreenRows();
		if (action == IScroll.ScrollHigher) startLine++;
		else if (action == IScroll.ScrollLower) startLine--;
		else if (action == IScroll.PageHigher) startLine += sr;
		else if (action == IScroll.PageLower) startLine -= sr;
		else if (action == IScroll.TrackTo) startLine = value;
		if (startLine > numLines-sr) startLine = numLines-sr;
		if (startLine < 0) startLine = 0;
	}else {
		int sh = (minXScroll*width)/100;
		if (sh < 10) sh = 10;
		if (action == IScroll.ScrollHigher) xPos += sh;
		else if (action == IScroll.ScrollLower) xPos -= sh;
		else if (action == IScroll.PageHigher) xPos += width-10;
		else if (action == IScroll.PageLower) xPos -= width-10;
		else if (action == IScroll.TrackTo) xPos = value;
		int mw = widest;
		if (xPos+(width-spacing*2) > mw) xPos = mw-(width-spacing*2);
		if (xPos < 0) xPos = 0;
	}
	repaintNow();
	if (ss != null)
		ss.updateScroll(which);
}
//===================================================================
public ScrollablePanel getScrollablePanel()
//===================================================================
{
	ScrollBarPanel sp = new ScrollBarPanel(this);
	sp.shrinkComponent = sp.stretchComponent = true;
	//sp.vbar.modify(SmallControl,0);
	//sp.hbar.modify(SmallControl,0);
	return sp;
}
public boolean needScrollBar(int which,int forSize)
	{
		return getVisible(which,forSize) < getActual(which);
	}
public boolean canGo(int orientation,int direction,int position)
{
	return true;
}
//##################################################################
}
//##################################################################

