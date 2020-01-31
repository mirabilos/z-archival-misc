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
public class mLabel extends Control {
//##################################################################
public FormattedTextSpecs format = null;
public boolean isActive = false;
public int alignment = Gui.Left;
public int anchor = Gui.NORTHWEST;
public int spacing = 2;
/**
* This is used to associate another Control with this label. This is usually used when
* a label is acting as a prompt for another control (e.g. an mInput). If this label has
* a hot key associated with it, then pressing that hot key will give focus to the
* associated control and, if that control is a button, will cause it to fire.
**/
public Control control;
protected boolean isData = false;
{
	modify(NoFocus,0);
	rows = columns = 0;
	//defaultAddMeCellConstraints = mPanel.DontStretch;
	//defaultAddMeControlConstraints = mPanel.Fill;
	//defaultAddMeAnchor = mPanel.NorthWest;
}
//===================================================================
public mLabel(int rows,int columns) {this.rows = rows; this.columns = columns;}
public mLabel(String what) {this(what,true);}
//===================================================================
public mLabel(String what,boolean containsHotKey)
//===================================================================
{
	if (containsHotKey){
		text = Gui.getTextFrom(what);
		if (text == null) text = "";
		char hot = Gui.getHotKeyFrom(what);
		if (hot != 0) setHotKey(0,hot);
	}else{
		text = what;
		if (text == null) text = "";
	}
}
protected int lineHeight = 0;
//-------------------------------------------------------------------
protected int getScreenRows()
//-------------------------------------------------------------------
{
	if (lineHeight == 0){
		FontMetrics fm = getFontMetrics();
		lineHeight = fm.getHeight()+fm.getLeading();
	}
	return (height-spacing*2)/lineHeight;
}
//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	Rect r;
	if (text == null || text.length() == 0) text = " ";
	//if (text.length() == 0) text = " "; <= Add this in.
 	if (rows != 0 && columns != 0) {
		// v--- Add this in.
		//r = Gui.getAverageSize(getFontMetrics(),rows,columns,spacing+borderWidth,spacing+borderWidth);
		r = Gui.getAverageSize(getFontMetrics(),rows,columns,spacing,spacing);
	}
	else if (text.trim().length() == 0){
		preferredWidth = spacing+borderWidth*2;
		preferredHeight = getFontMetrics().getHeight()+spacing+borderWidth*2;
		return;
	}else r = Gui.getSize(getFontMetrics(),ewe.util.mString.split(text,'\n'),spacing+borderWidth,spacing+borderWidth);
	preferredWidth = r.width; preferredHeight = r.height;
}

protected static String [] _lines = new String[1];

//===================================================================
public String [] getLines() {_lines[0] = text; return _lines;}
//===================================================================
protected int startLine = 0, xPos = 0;
//==================================================================
public void setText(String text) {super.setText(text); repaintNow();}
//==================================================================
protected void doPaintData(Graphics g) { if (isData) doPaint(g,null);}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	doBackground(g);
	doBorder(g);
	String [] lines = getLines();
	if (lines.length > 0) lines[0] = makeHot(lines[0]);
	Rect d = getDim(null);
	int gap = (spacing*2)+(borderWidth*2);
	d.width -= gap; d.height -= gap;
	d.x = d.y = gap/2;
	g.setColor((flags & Disabled) == 0 ? getForeground() : Color.DarkGray);
	g.setFont(getFont());
	FontMetrics fm = getFontMetrics();
	//d.y -= (fm.getHeight()+fm.getLeading())*startLine;
	d.x -= xPos;
	g.drawText(fm,lines,d,alignment,anchor,startLine,startLine+getScreenRows()+1,format);
	//lines.length);//
}

public void onEvent(Event ev)
{
	if (ev instanceof PenEvent || control == null) super.onEvent(ev);
	else control.onEvent(ev);
}
public void onPenEvent(PenEvent ev)
{
	if (checkPenTransparent(ev)) return;
	if (control == null) {
		super.onPenEvent(ev);
		return;
	}
	Point p = new Point(ev.x,ev.y);
	ev.x = ev.y = 0;
	if (!isOnMe(p)) ev.x = ev.y = -1;
	control.onLabelPenEvent(ev);
}
public void penClicked(Point where)
{
	if (isActive) notifyAction();
}
public void doAction(int how)
{
	if (control != null) {
		control.takeFocus(how);
		if (control instanceof ButtonControl){
			control.doAction(ByKeyboard);
			control.notifyAction();
		}
	}
}
//##################################################################
}
//##################################################################

