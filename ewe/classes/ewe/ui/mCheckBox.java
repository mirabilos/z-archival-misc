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

//##################################################################
public class mCheckBox extends ButtonControl implements Booleanable{
//##################################################################

{
	modify(PreferredSizeOnly|HasData,0);
	//defaultAddMeCellConstraints = mPanel.DontStretch;
	//defaultAddMeControlConstraints = mPanel.DontFill;
	//defaultAddMeAnchor = mPanel.NorthWest;
	borderStyle = BDR_NOBORDER;
}
/**
 * Set this true to disable blinking of the Checkbox when it gets the keyboard focus.
 */
public boolean dontBlink = false;

//==================================================================
public CheckBoxGroup group;
//==================================================================
public mCheckBox(){this("");}
//===================================================================
public mCheckBox(String txt)
//===================================================================
{
	if (txt == null) txt = "";
	text = Gui.getTextFrom(txt);
	setHotKey(0,Gui.getHotKeyFrom(txt));
}
//==================================================================
/**
 * This is the default width of the box. By default it is 15.
 */
public static int defaultBoxWidth = 15;
/**
 * If this is true a checkbox will draw an 'x' instead of a tick.
 */
public static boolean defaultUseCross = false;
/**
 * This width of the box - this is initialized to defaultBoxWidth.
 */
public int boxWidth = defaultBoxWidth;
/**
 * If this is true the checkbox will draw an 'x' instead of a tick. It is
 * initialized to defaultUseCross;
 */
public boolean useCross = defaultUseCross;

int blinkId = 0;

boolean cursorOn = false;
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	cursorOn = false;
	if (useNativeTextInput){
		cursorOn = !dontBlink;
	}else if (how == ByKeyboard){
		if (!dontBlink){
			cursorOn = true;
			repaintDataNow();
			blinkId = mApp.requestTick(this,500);
		}
	}
	super.gotFocus(how);
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	blinkId = 0;
	if (cursorOn) {
		cursorOn = false;
		repaintDataNow();
	}
	super.lostFocus(how);
}
//===================================================================
public void penPressed(Point p)
//===================================================================
{
	blinkId = 0;
	if (cursorOn) cursorOn = false;
	super.penPressed(p);
}
//===================================================================
public void ticked(int id,int elapsed)
//===================================================================
{
	//System.out.println(elapsed);
	if (blinkId == id){
		cursorOn = !cursorOn;
		if (!PenEvent.tipIsDisplayed()) repaintDataNow();
		blinkId = mApp.requestTick(this,500);
	}else
		super.ticked(id,elapsed);
}

//==================================================================
protected void calculateSizes()
//==================================================================
{
	if (isExclusive()) boxWidth = 15;
	Rect r = Gui.getSize(getFontMetrics(),text,4,2);
	boolean noText = mString.toString(text).length() == 0;
	if (noText) r.set(0,0,0,0);
	preferredWidth = r.width+boxWidth+(noText ? 0 : 2);
	preferredHeight = r.height;
	if (preferredHeight < boxWidth) preferredHeight = boxWidth;
}
//==================================================================
public boolean isExclusive()
//==================================================================
{
	if (group == null) return false;
	return group.exclusive;
}
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	int w = width, h = height;
	doBackground(g);
	g.setColor(Color.Black);
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) g.setColor(Color.DarkGray);
	FontMetrics fm = getFontMetrics();
	g.setFont(fm.getFont());
	g.drawText(makeHot(text),boxWidth+4,(h-fm.getHeight())/2);
	if (text.length() == 0) borderStyle &= ~BDR_DOTTED;
	g.draw3DRect(getDim(Rect.buff),borderStyle,true,null,null);
	if (!isExclusive()) doPaintSquare(g);
	else doPaintCircle(g);//doPaintDiamond(g);
}
//==================================================================
protected void doPaintData(Graphics g)
//==================================================================
{
	int got = modify(PaintDataOnly,0);
	if (!isExclusive()) doPaintSquare(g);
	else doPaintCircle(g);//doPaintDiamond(g);
	restore(got,PaintDataOnly);
}
//==================================================================
public void doPaintSquare(Graphics g)
//==================================================================
{
	int flags = getModifiers(true);
	int myFlag = getModifiers(false);
	int w = width, h = height;
	g.setColor(Color.White);
	boolean en = (((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0));
	boolean ed = (((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	if (!en || !ed) g.setColor(Color.LightGray);
	else if (cursorOn) g.setColor(useNativeTextInput ? Color.LightGreen : Color.LightGray);
	int bx = text.length() == 0 ? 0 : 2;
	int by = text.length() == 0 ? 0 : (h-boxWidth)/2+1;
	g.fillRect(bx+2,by+2,boxWidth-4,boxWidth-4);
	if (state || pressState){
		Color c = Color.LightGray;
		if (!pressState){
			if (!state)
				if (en && ed) c = Color.White;
				else c = Color.LightGray;
			else
				if (en) c = Color.Black;
				else c = Color.DarkGray;
		}
		Pen oldpen = g.setPen(new Pen(c,Pen.SOLID,2));
		//g.drawLine(bx+4,by+4,bx+bw-5,by+bw-5);

		if (useCross){
			g.drawLine(bx+3,by+3,bx+boxWidth-5,by+boxWidth-5);
			g.drawLine(bx+3,by+boxWidth-5,bx+boxWidth-5,by+3);

		}else{
			g.drawLine(bx+4,by+boxWidth-5,bx+boxWidth-5,by+4);
			g.drawLine(bx+4,by+boxWidth-5,bx+4,by+boxWidth-10);
		}
		g.setPen(oldpen);
	}
		//g.draw3DButton(new Rect(bx,by,bw,bw),true,null,((flags & DrawFlat) != 0),true);
	if ((myFlag & PaintDataOnly) == 0){
	g.draw3DRect(
		new Rect(bx,by,boxWidth,boxWidth),
		ButtonObject.checkboxEdge,
		(flags & DrawFlat) != 0,
		null,
		Color.DarkGray);
	}
}
//==================================================================
public void doPaintCircle(Graphics g)
//==================================================================
{
	int flags = getModifiers(true);
	int myFlag = getModifiers(false);
	int w = width, h = height;
	Color back = Color.White;
	Color forgnd = Color.Black;
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) back = Color.LightGray;
	if (cursorOn) back = Color.LightGray;
	if (!((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) forgnd = Color.DarkGray;
	int bx = 1;
	int by = (h-boxWidth)/2+1;
	if ((myFlag & PaintDataOnly) == 0){
		g.setColor(back);
		g.fillEllipse(bx+1,by+1,boxWidth-3,boxWidth-3);
		if ((flags & DrawFlat) != 0 || true){
			g.setColor((standardEdge & EDGE_ETCHED) == EDGE_ETCHED ? Color.DarkGray : forgnd);
			g.drawEllipse(bx,by,boxWidth-1,boxWidth-1);
		}else if ((standardEdge & EDGE_ETCHED) == EDGE_ETCHED){
			g.setColor(Color.DarkGray);
			g.drawArc(bx,by,boxWidth-1,boxWidth-1,30,180);
			g.setColor(Color.White);
			g.drawArc(bx+1,by+1,boxWidth-3,boxWidth-3,30,180);
			g.setColor(Color.White);
			g.drawArc(bx,by,boxWidth-1,boxWidth-1,211,180);
			g.setColor(Color.DarkGray);
			g.drawArc(bx+1,by+1,boxWidth-3,boxWidth-3,211,180);
		}else{
			g.setColor(forgnd);
			g.drawArc(bx,by,boxWidth-1,boxWidth-1,30,180);
			g.setColor(Color.DarkGray);
			g.drawArc(bx+1,by+1,boxWidth-3,boxWidth-3,30,180);
			g.setColor(Color.White);
			g.drawArc(bx,by,boxWidth-1,boxWidth-1,210,180);
			g.setColor(Color.LightGray);
			g.drawArc(bx+1,by+1,boxWidth-3,boxWidth-3,210,180);
		}
	}
	if (pressState | state | ((myFlag & PaintDataOnly) != 0)){
		g.setColor(Color.LightGray);
		if (!pressState){
			g.setColor(back);
			if (!state)
				if ((((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) g.setColor(back);
				else g.setColor(Color.LightGray);
			else
				if ((((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) g.setColor(Color.Black);
				else g.setColor(Color.DarkGray);
		}
		g.fillEllipse(bx+3,by+3,boxWidth-7,boxWidth-7);
	}
}

//==================================================================
public void doPaintDiamond(Graphics g)
//==================================================================
{
	int flags = getModifiers(true);
	int w = width, h = height;
	Color back = Color.White;
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) back = Color.LightGray;
	int bx = 0;
	int by = (h-boxWidth)/2;
	Rect _rect;
	g.draw3DDiamond(_rect = new Rect(bx,by,boxWidth,boxWidth),true,back,((flags & DrawFlat) != 0));
	g.setColor(Color.LightGray);
	if (!pressState){
		if (!state)
			if ((((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) g.setColor(Color.White);
			else g.setColor(Color.LightGray);
		else
			if ((((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) g.setColor(Color.Black);
			else g.setColor(Color.DarkGray);
	}
	x += 3; y += 3; width-=6; height-=6;
	g.drawDiamond(_rect,mGraphics.All);
}
//==================================================================
public boolean getState() {return state;}
public void setState(boolean to)
//==================================================================
{
	if (to == state) return;
	if (to == true)
 		if (isExclusive()){
			mCheckBox cb = group.getSelected();
			if (cb != null) {
				cb.setState(false);
				cb.notifyDataChange();
			}
		}
	state = to;
	repaintDataNow();
}
//==================================================================
public void doAction(int how)
//==================================================================
{
	if (isExclusive() && state == true) return;
	setState(!state);
	//super.doAction(how);
}
//==================================================================
public mCheckBox setGroup(CheckBoxGroup cbg)
//==================================================================
{
	cbg.add(this);
	group = cbg;
	addListener(group);
	return this;
}

//==================================================================
public void setText(String text) {super.setText(text); repaintNow();}
//==================================================================
//===================================================================
public void notifyAction()
//===================================================================
{
	super.notifyAction();
	super.notifyDataChange();
}


//##################################################################
}
//##################################################################

