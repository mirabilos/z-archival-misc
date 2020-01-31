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
* This is a Deprecated control - it allows simple multi-line text editing.
* However it does not allow word wrapping to the display width. Use mTextPad instead - this
* is more useful and complete.
* @deprecated
**/
//##################################################################
public class mTextArea extends EditControl implements ScrollClient,Selectable{
//##################################################################

protected String [] lines;
/**
* This is not implemented yet.
*/
public boolean wrapToScreenSize;
public int spacing = 4;
/**
* A percent figure. Defaults to 30%
*/
public int minXScroll = 30;
/**
* A percent figure. Defaults to 80%
*/
public int minYScroll = 80;
/**
* If this is true the entire text is selected when it gains focus.
**/
public boolean selectAllOnFocus = false;

public int charWidth = 10;

{
	modify(HasData|WantDrag|WantHoldDown|AlwaysRecalculateSizes|ShowSIP|TakesKeyFocus,PreferredSizeOnly);
	borderStyle = EDGE_SUNKEN|BDR_OUTLINE;
	borderWidth = 0;
	rows = 5;
	columns = 20;
	//setMenu(getClipboardMenu(null));
}
//==================================================================
public mTextArea(int rows,int columns)
//==================================================================
{
	this.rows = rows; this.columns = columns;
}
//==================================================================
protected int getItemHeight()
//==================================================================
{
	FontMetrics fm = getFontMetrics();
	return fm.getHeight()+2;//+fm.getLeading();
}
//==================================================================
protected int getTextWidth()
//==================================================================
{
	FontMetrics fm = getFontMetrics();
	int w = 0;
	for (int i = 0; i<getNumLines(); i++) {
		int ww = fm.getTextWidth(lines[i]);
		if (ww > w) w = ww;
	}
	return w;
}
//==================================================================
protected void calculateSizes()
//==================================================================
{
	charWidth = getFontMetrics().getCharWidth('0');
	Rect r = Gui.getAverageSize(getFontMetrics(),rows,columns,0,0);
	preferredWidth = r.width;
	preferredHeight = r.height;
}

//===================================================================
//public Rect getDataRect(Rect dest)
//===================================================================
//{
//	return Rect.unNull(dest).set(x+spacing,y+spacing,width-spacing*2,height-spacing*2);
//}
//==================================================================
protected int getAvailableWidth()
//==================================================================
{
	if (wrapToScreenSize) return width-4;
	else return Gui.getAverageSize(getFontMetrics(),1,columns,2,2).width;
}
//==================================================================
public int getNumLines()
//==================================================================
{
	if (lines == null) splitLines(getAvailableWidth());
	return lines.length;
}
//------------------------------------------------------------------
protected void splitLines(int width)
//------------------------------------------------------------------
{
	lines = mString.split(text,'\n');
	if (lines.length == 0) {
		lines = new String[1];
		lines[0] = "";
	}
}
//------------------------------------------------------------------
protected void insertLine(int index)
//------------------------------------------------------------------
{
	if (lines == null) lines = new String[0];
	if (index > lines.length) index = lines.length;
	String [] nl = new String[lines.length+1];
	for (int i = 0; i<index; i++) nl[i] = lines[i];
	nl[index] = "";
	for (int i = index; i<lines.length; i++) nl[i+1] = lines[i];
	lines = nl;
}
//------------------------------------------------------------------
protected boolean getCharRect(int ch,int ln,Rect dest)
//------------------------------------------------------------------
{
	dest.width = dest.x = 0;
	dest.height = getItemHeight();
	dest.y = dest.height*ln;
	FontMetrics fm = getFontMetrics();
//..................................................................
	if (ln >= lines.length || ln < 0) return false;
	String s = lines[ln];
	if (ch > s.length() || ch < 0) return false;
	if (ch == 0) dest.x = 0;
	else dest.x = fm.getTextWidth(s.substring(0,ch));
	if (ch == s.length()) dest.width = 5;
	else dest.width = fm.getCharWidth(s.charAt(ch));
//..................................................................
	return true;
}
//==================================================================
public void setText(String what)
//==================================================================
{
	text = what;
	if (text == null) text = "";
	splitLines(getAvailableWidth());
	fix();
	repaintDataNow();
	checkScrolls();
}
//==================================================================
public String getText()
//==================================================================
{
	if (lines == null) return text;
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<lines.length; i++){
		if (lines[i] != null) {
			if (i != 0) sb.append("\n");
			sb.append(lines[i]);
		}
	}
	return sb.toString();
}
//===================================================================
public String [] getLines()
//===================================================================
{
	if (lines == null) splitLines(getAvailableWidth());
	return lines;
}
//==================================================================
protected void fixText()
//==================================================================
{
	setText(getText());
}
//==================================================================
public int getScreenRows()
//==================================================================
{
	int h = getItemHeight();
	if (h == 0) return 0;
	return (height-spacing*2)/h;
}
protected static ImageBuffer itemBuffer = new ImageBuffer();
protected static ImageBuffer blockBuffer = new ImageBuffer();

//==================================================================
public void paintLastChar(Graphics g) {paintLastChar(g,false);}
public void paintLastChar(Graphics g,boolean eraseIt)
//==================================================================
{
	if (g == null) g = getGraphics();
	if (g == null) return;
	int flags = getModifiers(true);
	FontMetrics fm = getFontMetrics();
	String line = getLine(curState.cursorLine);
	int x = spacing-curState.xShift;
	int w = 0;
	int h = getItemHeight(),y = h*(curState.cursorLine-curState.firstLine)+spacing;
	g.setDrawOp(g.DRAW_OVER);
	if (line.length() != 0) {
		x += fm.getTextWidth(line.substring(0,line.length()-1));
		if (!eraseIt){
			g.setColor(Color.Black);
			g.setFont(getFont());
			g.drawText(""+line.charAt(line.length()-1),x,y);
		}else {
			Color c = Color.White;
			if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) c = Color.LightGray;
			g.setColor(c);
			w = fm.getCharWidth(line.charAt(line.length()-1));
			g.fillRect(x,y,w,h);
		}
	}
}
//==================================================================
public boolean deleteSelection()
//==================================================================
{
	clearCursor();
	if (curState.selStartLine == -1) return false;
	int sl = curState.selStartLine, sp = curState.selStartPos;
	int el = curState.selEndLine;
	if (sl != el) {
		lines[sl] = lines[sl].substring(0,curState.selStartPos)+lines[el].substring(curState.selEndPos,lines[el].length());
		for (int i = curState.selStartLine+1; i<=curState.selEndLine && i<lines.length; i++) lines[i] = null;
		curState.selStartLine = -1;
		fixText();
	}else {
		String s = lines[sl];
		lines[sl] = s.substring(0,curState.selStartPos)+s.substring(curState.selEndPos,s.length());
		curState.selStartLine = -1;
		paintLine(null,sl);
	}
	newCursorPos(sp,sl,false);
	return true;
}
//==================================================================
public void paintLine(Graphics g,int index)
//==================================================================
{
	if (g == null) g = getGraphics();
	if (g == null) return;
	int num = getScreenRows();
	if (index < curState.firstLine || index >= curState.firstLine+getScreenRows()) return;
	String line = getLine(index);
	int h = getItemHeight();
	int y = spacing+(index-curState.firstLine)*h;
	int x = 0;//2-curState.xShift;
	int w = width-(spacing)*2;
	int flags = getModifiers(true);
	boolean notEn = !(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0));
	boolean dis = notEn || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	Graphics gr = itemBuffer.get(w,h,true);
	Image img = itemBuffer.image;
	gr.setDrawOp(gr.DRAW_OVER);
//..................................................................
// Fill background.
//..................................................................
	Color c;
	if (dis) c = Color.LightGray;
	else c = Color.White;
	gr.setColor(c);
	gr.fillRect(0,0,w,h);
//..................................................................
// Do text.
//..................................................................
	if (notEn) c = Color.DarkGray;
	else c = getForeground();
	gr.setColor(c);
	gr.setFont(getFont());
	gr.translate(-curState.xShift,0);
	gr.drawText(line,x,0);//+1);
	if (curState.isInSelection(index) && curState.selectionEnabled) {
		int sp = 0, ep = line.length();
		if (curState.selStartLine == index) sp = curState.selStartPos;
		if (curState.selEndLine == index) ep = curState.selEndPos;
		Graphics bg = blockBuffer.get(w,h);
		bg.setColor(Color.White);
		bg.fillRect(0,0,w,h);
		bg.translate(-curState.xShift,0);
		FontMetrics fm = getFontMetrics();
		int leftWidth = fm.getTextWidth(line.substring(0,sp));
		int myWidth = fm.getTextWidth(line.substring(sp,ep));
		bg.setColor(Color.Black);
		bg.fillRect(leftWidth,0,myWidth,h);
		bg.translate(curState.xShift,0);
		gr.setDrawOp(gr.DRAW_XOR);
		gr.translate(curState.xShift,0);
		gr.drawImage(blockBuffer.image,0,0);
	}else
		gr.translate(curState.xShift,0);
	g.drawImage(img,spacing,y);
}
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	Color c = Color.White;
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)))
		c = Color.LightGray;
	//g.draw3DButton(getDim(null),true,c,,true);
	if (!hasModifier(PaintDataOnly,false))
		g.draw3DRect(
			getDim(Rect.buff),
			borderStyle, //Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
			(flags & DrawFlat) != 0,
			c,//null,
			borderColor);
/*
	g.draw3DRect(
		getDim(Rect.buff),
		Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
		(flags & DrawFlat) != 0,
		c,
		Color.DarkGray);
*/
		doPaintData(g,area);
}
//==================================================================
public void paintCursor(Graphics gr)
//==================================================================
{
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	if (hasCursor){
		Rect r = new Rect(0,0,0,0);
		if (!getCharRect(curState.cursorPos,curState.cursorLine,r)) return;
		Image i = new Image(2,r.height);
		Graphics gi = new Graphics(i);
		gi.setColor(getForeground());
		gi.fillRect(0,0,r.width,r.height);
		gi.free();
		g.setDrawOp(g.DRAW_XOR);
		g.drawImage(i,r.x+spacing-curState.xShift,spacing+r.y-curState.firstLine*getItemHeight());
		g.setDrawOp(g.DRAW_OVER);
		i.free();
		cursorOn = !cursorOn;
	}
}
//-------------------------------------------------------------------
protected void doPaintData(Graphics g)
//-------------------------------------------------------------------
{
	doPaintData(g,null);
}
//-------------------------------------------------------------------
protected void doPaintData(Graphics g,Rect area)
//-------------------------------------------------------------------
{
	if (lines == null) splitLines(getAvailableWidth());
	for (int i = 0; i<getScreenRows()+1; i++)
		paintLine(g,i+curState.firstLine);
	cursorOn = false;
}
//==================================================================
// This is the implementation of ScrollClient. Remove this if ScrollBars
// are not needed.
//==================================================================
	//protected //ScrollServer ss;
	protected boolean amScrolling;
	//==================================================================
	public ScrollablePanel getScrollablePanel()
	//==================================================================
	{
		amScrolling = true;
		ScrollablePanel sp = new ScrollBarPanel(this);
		sp.modify(0,TakeControlEvents);
		return sp;
	}
	//==================================================================
	public void doScroll(int which,int action,int value)
	//==================================================================
	{
		if (which == IScroll.Vertical) {
			if (action == IScroll.ScrollHigher) curState.firstLine++;
			else if (action == IScroll.ScrollLower) curState.firstLine--;
			else if (action == IScroll.PageHigher) curState.firstLine += getScreenRows();
			else if (action == IScroll.PageLower) curState.firstLine -= getScreenRows();
			else if (action == IScroll.TrackTo) curState.firstLine = value;
			if (curState.firstLine > getNumLines()-getScreenRows()) curState.firstLine = getNumLines()-getScreenRows();
			if (curState.firstLine < 0) curState.firstLine = 0;
		}else {
			int sh = (minXScroll*width)/100;
			if (sh < 10) sh = 10;
			if (action == IScroll.ScrollHigher) curState.xShift += sh;
			else if (action == IScroll.ScrollLower) curState.xShift -= sh;
			else if (action == IScroll.PageHigher) curState.xShift += width-10;
			else if (action == IScroll.PageLower) curState.xShift -= width-10;
			else if (action == IScroll.TrackTo) curState.xShift = value;
			int mw = getTextWidth()+spacing*2;
			if (curState.xShift+width-spacing > mw) curState.xShift = mw-width;
			if (curState.xShift < 0) curState.xShift = 0;
		}
		repaintDataNow();
		if (ss != null) ss.updateScroll(which);
	}
	//public void setServer(ScrollServer server) {ss = server;}
	public int getActual(int which)
	{
		if (which == IScroll.Vertical) return getNumLines();
		else {
			//return
			int ac = getTextWidth();
			if (ac == 0) ac = 1;
			return ac;
		}
	}
	public int getVisible(int which,int forSize)
	{
		if (which == IScroll.Vertical) {
			int h = getItemHeight();
			if (h != 0) return (forSize-spacing*2)/h;
			else return 1;//5;
		}
		else return forSize-spacing*2;
	}
	public int getCurrent(int which)
	{
		if (which == IScroll.Vertical) return curState.firstLine;
		else return curState.xShift;
	}
	public boolean needScrollBar(int which,int forSize)
	{
		return getVisible(which,forSize) < getActual(which);
	}
public boolean canGo(int orientation,int direction,int position)
{
	return true;
}

//==================================================================
//This marks the end of the implementation of ScrollClient
//==================================================================
//==================================================================
// If you don't want any scroll bar functionality, then replace
// the body of checkScrolls() and updateScrolls() with {}
//==================================================================
public void checkScrolls() {if (ss != null) ss.checkScrolls();}
//==================================================================
protected void updateScrolls()
//==================================================================
{
	if (ss == null) return;
	ss.updateScroll(Horizontal);
	ss.updateScroll(Vertical);
}

int blinkId = 0;
boolean hasCursor, cursorOn;
//==================================================================
public void ticked(int id,int elapsed)
//==================================================================
{
	//System.out.println(elapsed);
	if (blinkId == id){
		paintCursor(null);
		blinkId = mApp.requestTick(this,500);
	}
	super.ticked(id,elapsed);
}
//==================================================================
public void selectAll()
//==================================================================
{
	clearSelection();
	curState.selStartLine = curState.selStartPos = 0;
	curState.selEndLine = lines.length-1;
	curState.selEndPos = lines[lines.length-1].length();
	if (!newCursorPos(curState.selEndPos,curState.selEndLine,true))
		repaintDataNow();
}
//===================================================================
public boolean checkSipCoverage()
//===================================================================
{
	if (getWindow() == null) return false;
	Rect r = getWindow().checkSipCoverage(this);
	if (r == null) return false;
	//System.out.println("ON: "+this);
	return InputPopupForm.popupForm.popup(this,r);
}

//==================================================================
public void gotFocus(int how)
//==================================================================
{
	justGotFocus = (how == ByPen);
	hasCursor =  true;
	blinkId = mApp.requestTick(this,500);
	checkScrolls();
	oldText = getText();
	if (!checkSipCoverage()) {
		if (selectAllOnFocus) selectAll();
		else if (curState.hasSelection()) clearSelection();
	}
	super.gotFocus(how);
	//ewe.sys.Vm.setSIP(1);
}
//==================================================================
public void lostFocus(int how)
//==================================================================
{
	blinkId = 0;
	clearCursor();
	cursorOn = false;
	if (menuIsActive()) return;
	//noSelection();
	clearSelection();
	newCursorPos(0,0,false);
	checkScrolls();
	hasCursor = false;
	if (!getText().equals(oldText))
		notifyDataChange();
}
protected textAreaState curState = new textAreaState();

//==================================================================
protected boolean fix()
//==================================================================
{
	if (width == 0 || height == 0) return false;
	if (lines == null) splitLines(getAvailableWidth());
	textAreaState tas = curState.getCopy();
	FontMetrics fm = getFontMetrics();
	if (!hasCursor) tas.cursorLine = tas.cursorPos = tas.firstLine = tas.xShift = 0;
//..................................................................
	if (tas.cursorLine >= lines.length) {
		tas.cursorLine = lines.length-1;
		tas.cursorPos = lines[tas.cursorLine].length();
	}
	if (tas.cursorLine < 0) tas.cursorLine = 0;
	String ln = lines[tas.cursorLine];
	if (tas.cursorPos > ln.length()) tas.cursorPos = ln.length();
	if (tas.cursorPos < 0) tas.cursorPos = 0;
//..................................................................
	int sr = getScreenRows();
	int ys = (minYScroll*sr)/100;
	if (ys < 1) ys = 1;
	if (ys > sr) ys = sr;
	while (tas.cursorLine >= sr+tas.firstLine) tas.firstLine += ys;
	while (tas.cursorLine < tas.firstLine) tas.firstLine -= ys;
	if (tas.firstLine < 0) tas.firstLine = 0;
//..................................................................
	int w = width-spacing*2-2;
	String cl = ln.substring(0,tas.cursorPos);
	int cw = fm.getTextWidth(cl);
	if (cw < tas.xShift) tas.xShift = 0;
	int extra = (minXScroll*width)/100;
	if (extra < 4) extra = 4;
	if (cw > tas.xShift+w-4) tas.xShift = cw-w+extra;
//..................................................................
// Old version, now defunct.
//..................................................................
/*	numDisplayed = 0;
	if (leftMost > text.length()) leftMost = 0;
	if (cursorPos < leftMost) leftMost = cursorPos;
	boolean gotCursor = false;
	FontMetrics fm = getFontMetrics();
	String s = getDisplay(text);
	for (int i = leftMost; i<=text.length(); i++){
		int need = 2;
		if (i != text.length()) need = fm.getCharWidth(s.charAt(i));
		if (need+w > width){
			if (gotCursor) break;
			else if (numDisplayed != 0){
				w -= fm.getCharWidth(s.charAt(leftMost));
				leftMost++;
				numDisplayed--;
			}
		}
		if (i != text.length()) {
			numDisplayed++;
			w += need;
		}
		if (i == cursorPos) gotCursor = true;
	}
*/
	textAreaState t2 = curState;
	curState = tas;
	return curState.displayChanged(t2);
}

//------------------------------------------------------------------
protected boolean newCursorPos(int ch,int ln,boolean takeSel)
//------------------------------------------------------------------
{
	boolean repainted = false;
	clearCursor();
	curState.cursorLine = ln;
	curState.cursorPos = ch;
	if (fix()) {
		repaintDataNow();
		updateScrolls();
		repainted = true;
	}
	return repainted;
}
//------------------------------------------------------------------
protected void newText(String txt,int newCp,boolean redoData)
//------------------------------------------------------------------
{
	FontMetrics fm = getFontMetrics();
	int oldw = fm.getTextWidth(lines[curState.cursorLine]);
	lines[curState.cursorLine] = txt;
	int w = getFontMetrics().getTextWidth(txt);
	int tw = width-spacing*2;
	if ((w > tw && oldw <= tw) || (w < tw && oldw >=tw)) checkScrolls();
	boolean rp = newCursorPos(newCp,curState.cursorLine,false);
	if (!rp) {
		clearCursor();
		paintLine(null,curState.cursorLine);
	}
}
//------------------------------------------------------------------
protected Point getPenChar(Point onControl)
//------------------------------------------------------------------
{
	FontMetrics fm = getFontMetrics();
	Point p = new Point(0,0);
	if (lines == null) lines = new String[0];
	int h = getItemHeight();
	int px = onControl.x-spacing+curState.xShift;
	int py = onControl.y-spacing+curState.firstLine*h;
	p.y = py/h;
	p.x = 0;
	String s = getLine(p.y);
	int i = 0, w = 0;
	for (; i<s.length(); i++){
		w += fm.getCharWidth(s.charAt(i));
		if (w >= px) break;
	}
	p.x = i;
	return p;
}
//------------------------------------------------------------------
protected void clearCursor() {if (cursorOn) paintCursor(null);}
//------------------------------------------------------------------
protected String getLine(int index)
{
	if (lines == null) return "";
	if (index < 0 || index >= lines.length) return "";
	return lines[index];
}
protected String getLine() {return getLine(curState.cursorLine);}


Point pressPoint = new Point(0,0);

//===================================================================
public boolean noSelection()
//===================================================================
{
	if (!curState.hasSelection()) return false;
	clearSelection();
	return true;
}
//===================================================================
public Object getSelection()
//===================================================================
{
	if (!curState.hasSelection()) return null;
	int sl = curState.selStartLine, sp = curState.selStartPos;
	int el = curState.selEndLine;
	Vector v = new Vector();
	if (sl != el) {
		v.add(lines[sl].substring(curState.selStartPos,lines[sl].length()));
		for (int i = sl+1; i<el && i<lines.length; i++)
			v.add(lines[i]);
		v.add(lines[el].substring(0,curState.selEndPos));
	}else
		v.add(lines[sl].substring(curState.selStartPos,curState.selEndPos));
	String [] ret = new String[v.size()];
	v.copyInto(ret);
	return ret;
}
//===================================================================
public boolean replaceSelection(Object with)
//===================================================================
{
	deleteSelection();
	StringBuffer sb = new StringBuffer();
	int cl = curState.cursorLine;
	int cp = curState.cursorPos;
	if (cl > 0)
		for (int i = 0; i<cl && i<lines.length; i++)
			sb.append(lines[i]+"\n");
	sb.append(lines[cl].substring(0,cp));
	sb.append(clipboardToString(with));
	sb.append(lines[cl].substring(cp,lines[cl].length()));
	for (int i = cl+1; i<lines.length; i++)
		sb.append("\n"+lines[i]);
	setText(sb.toString());
	return true;
}
//===================================================================
public boolean hasSelection() {return curState.hasSelection();}
//===================================================================
public void clearSelection()
//==================================================================
{
	clearCursor();
	curState.selectionEnabled = false;
	for (int i = 0; i<getScreenRows(); i++)
		if (curState.isInSelection(i+curState.firstLine)) paintLine(null,i+curState.firstLine);
	curState.selectionEnabled = true;
	curState.selStartLine = -1;
}
private boolean shouldDeselect = false;
//==================================================================
public void penPressed(Point where)
//==================================================================
{
	if (menuIsActive()) menuState.closeMenu();
	Point loc = getPenChar(where);
	if (loc == null) return;
	shouldDeselect = true;
	//if (!justGotFocus || !selectAllOnFocus) clearSelection();
	justGotFocus = false;
	newCursorPos(loc.x,loc.y,false);
	lastDrag = pressPoint = loc;
}
//==================================================================
public void penDoubleClicked(Point where) {selectAll();}
//===================================================================
public void penReleased(Point wher)
//===================================================================
{
	if (menuIsActive()) return;
	if (shouldDeselect) clearSelection();
}
//==================================================================
public void penHeld(Point p)
//==================================================================
{
	if (menuState != null && !menuIsActive())
		menuState.doShowMenu(p,false,null);
}

Point lastDrag;
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (menuIsActive()) return;
	shouldDeselect = false;
	Point now = getPenChar(dc.curPoint);
	if (now == null) return;
	if (lastDrag != null)
		if (now.x == lastDrag.x && now.y == lastDrag.y) return;
	boolean [] wasIn = new boolean[lines.length];
	for (int i = 0; i<lines.length; i++)
		wasIn[i] = curState.isInSelection(i);
	lastDrag = now;
	if (now.y < pressPoint.y) {
		curState.selStartLine = now.y;
		curState.selEndLine = pressPoint.y;
		curState.selStartPos = now.x;
		curState.selEndPos = pressPoint.x;
	}else if (now.y > pressPoint.y) {
		curState.selStartLine = pressPoint.y;
		curState.selEndLine = now.y;
		curState.selStartPos = pressPoint.x;
		curState.selEndPos = now.x;
	}else {
		curState.selStartLine = curState.selEndLine = pressPoint.y;
		if (now.x < pressPoint.x) {
			curState.selStartPos = now.x;
			curState.selEndPos = pressPoint.x;
		}else{
			curState.selStartPos = pressPoint.x;
			curState.selEndPos = now.x;
		}
	}
	curState.fixSel(lines);
	if (!newCursorPos(now.x,now.y,false)) {
		Graphics g = getGraphics();
		if (g == null) return;
		for (int i = curState.firstLine; i<curState.firstLine+getScreenRows() && i<lines.length; i++) {
			if (curState.isInSelection(i) || wasIn[i])
				paintLine(g,i);
		}
	}
}
//==================================================================
public void onKeyEvent(KeyEvent ev)
//==================================================================
{
	if (ev.type != ev.KEY_PRESS) {
		super.onKeyEvent(ev);
		return;
	}
	int flags = getModifiers(true);
	textAreaState tas = curState;
	if (validator != null) {
		if (!validator.isValidKeyPress(ev)) {
			Sound.beep();
			return;
		}
	}
	String s = getLine();
	int sl = s.length();
	int cl = curState.cursorLine;
	int cp = curState.cursorPos;

	if (ev.key == IKeys.BACKSPACE) {
		if (deleteSelection()) return;
		if (cp > 0 && cp == s.length()) {
			paintLastChar(null,true);
			lines[cl] = s.substring(0,sl-1);
			newCursorPos(cp-1,cl,false);
		}else if (cp > 0)
			newText(s.substring(0,cp-1)+s.substring(cp,s.length()),cp-1,false);
		else if (cl > 0) {
			String pl = getLine(cl-1);
			lines[cl-1] = pl+s;
			lines[cl] = null;
			curState.cursorPos = pl.length();
			curState.cursorLine--;
			fixText();
		}
	}else if (ev.key == IKeys.DELETE){
			if (deleteSelection()) return;
			if (cp < s.length()) {
				newText(s.substring(0,cp)+s.substring(cp+1,s.length()),cp,false);
			}else {
				if (cl < lines.length-1){
					lines[cl] = lines[cl]+lines[cl+1];
					lines[cl+1] = null;
					fixText();
				}
			}
	}else if (ev.key == IKeys.ENTER && (((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
		insertLine(cl+1);
		lines[cl] = s.substring(0,cp);
		lines[cl+1] = s.substring(cp,s.length());
		curState.cursorPos = 0;
		curState.cursorLine++;
		fixText();
	}else if (ev.key == IKeys.END){
		checkScrolls();
		newCursorPos(getLine().length(),tas.cursorLine,false);
	}else if (ev.key == IKeys.HOME){
		checkScrolls();
		newCursorPos(0,tas.cursorLine,false);
	}else if (ev.key == IKeys.LEFT){
		newCursorPos(tas.cursorPos-1,tas.cursorLine,false);
	}else if (ev.key == IKeys.RIGHT){
		newCursorPos(tas.cursorPos+1,tas.cursorLine,false);
	}else if (ev.key == IKeys.UP){
		checkScrolls();
		newCursorPos(tas.cursorPos,tas.cursorLine-1,false);
	}else if (ev.key == IKeys.DOWN){
		checkScrolls();
		newCursorPos(tas.cursorPos,tas.cursorLine+1,false);
	}else if (ev.key >= 32){
		if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
			Sound.beep();
			return;
		}
		boolean redoData = deleteSelection();
		s = getLine();
		cl = curState.cursorLine;
		cp = curState.cursorPos;
		if ((ev.modifiers & IKeys.SHIFT) == IKeys.SHIFT)
			if (ev.key >= 'a' && ev.key <= 'z')
				ev.key = 'A'+ev.key-'a';
		if (redoData || cp != s.length())
			newText(s.substring(0,curState.cursorPos)+(char)ev.key+s.substring(curState.cursorPos,s.length()),curState.cursorPos+1,redoData);
		else {
			clearCursor();
			lines[cl] += ""+(char)ev.key;
			paintLastChar(null);
			newCursorPos(cp+1,cl,false);
		}
	}else{
		super.onKeyEvent(ev);
	}
}
//-------------------------------------------------------------------
protected boolean paintConditionalChar(Graphics g, boolean highlight){return false;}
//-------------------------------------------------------------------

//##################################################################
}
//##################################################################
//##################################################################
class textAreaState{
//##################################################################
int cursorLine, cursorPos;
int firstLine, xShift;
int selStartLine = -1, selStartPos;
int selEndLine, selEndPos;

boolean selectionEnabled = true;

public boolean isInSelection(int line)
{
	if (selStartLine == -1) return false;
	return (selStartLine <= line && selEndLine >= line);
}
public textAreaState(){}
public textAreaState getCopy()
{
	textAreaState tas = new textAreaState();
	tas.cursorLine = cursorLine;
	tas.cursorPos = cursorPos;
	tas.xShift = xShift;
	tas.firstLine = firstLine;
	tas.selStartLine = selStartLine;
	tas.selStartPos = selStartPos;
	tas.selEndLine = selEndLine;
	tas.selEndPos = selEndPos;
	return tas;
}
public boolean hasSelection()
{
	return selStartLine != -1;
}
public boolean displayChanged(textAreaState other)
{
	if (other.firstLine != firstLine) return true;
	if (other.xShift != xShift) return true;
	return false;
}
public void fixSel(String [] lines)
{
	if (selStartLine < 0) selStartLine = 0;
	if (selEndLine < 0) selEndLine = 0;
	if (selStartLine >= lines.length) selStartLine = lines.length-1;
	if (selEndLine >= lines.length) {
		selEndLine = lines.length-1;
		selEndPos = lines[selEndLine].length();
	}
	String s = lines[selStartLine];
	if (selStartPos < 0) selStartPos = 0;
	if (selStartPos > s.length()) selStartPos = s.length();
	s = lines[selEndLine];
	if (selEndPos < 0) selEndPos = 0;
	if (selEndPos > s.length()) selEndPos = s.length();
}
//##################################################################
}
//##################################################################

