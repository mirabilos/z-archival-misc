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
public class InputPopupForm extends ControlPopupForm{
//##################################################################

public static InputPopupForm popupForm = new InputPopupForm();
public int time;
public mInput input;
/**@deprecated**/
public mTextArea area;
public mTextPad pad;
public ScrollablePanel areaScroller;
CardPanel cards;
public mLabel label;
{
	quickRecalculate = true;
}
//===================================================================
public InputPopupForm()
//===================================================================
{
	backGround = Color.White;
	CellPanel top = new CellPanel();
	label = new mLabel("Prompt");
	label.backGround = new Color(0,0,0xff);
	label.foreGround = Color.White;
	label.borderStyle = BDR_OUTLINE|BF_TOP|BF_LEFT|BF_RIGHT;
	label.borderColor = Color.Black;
	label.setPreferredSize(10,20);
	label.anchor = label.CENTER;
	//addLast(label).setCell(HSTRETCH);

	mButton b = new mButton(); cancel = b;
	b.borderStyle = BDR_OUTLINE|BF_TOP|BF_LEFT|BF_BOTTOM;
	b.image = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0x80,0,0));
	top.addNext(b).setCell(DONTSTRETCH);
	top.addNext(label);
	addLast(top).setCell(HSTRETCH);
	cards = new CardPanel();
	cards.borderWidth = 1;
	cards.borderColor = Color.Black;
	cards.borderStyle = BDR_OUTLINE|BF_RECT;
	addNext(cards);

	input = new mInput();
	input.borderWidth = 0;
	input.borderStyle = BDR_NOBORDER;//BDR_OUTLINE|BF_RECT;
	input.wantReturn = true;
	//input.borderColor = Color.Black;

	pad = new mTextPad(4,4);
	pad.borderWidth = 0;
	pad.borderStyle = BDR_NOBORDER;//BDR_OUTLINE|BF_RECT;
	//area.borderColor = Color.Black;

	cards.addItem(input,"Input",null);
	cards.addItem(areaScroller = new ScrollBarPanel(pad),"Area",null);

	b = new mButton(); ok = b;
	b.borderStyle = BDR_OUTLINE|BF_TOP|BF_RIGHT|BF_BOTTOM;
	b.image = new DrawnIcon(DrawnIcon.TICK,10,10,new Color(0,0x80,0));
	//addNext(b).setCell(DONTSTRETCH);
	top.addNext(b).setCell(DONTSTRETCH);
}

protected static boolean popped = false;
//===================================================================
public boolean popup(Control forWho,Rect visible)
//===================================================================
{
	client = forWho;
	Dimension d = forWho.getSize(null);

	Rect isVisible = Gui.visibleWindowClientArea(forWho);
	if (isVisible == null) isVisible = new Rect(0,0,0,0);
	/*
	if (isVisible.height < d.height || isVisible.width < 50) {
		return false;
	}
	*/
	if (d.width > isVisible.width) d.width = isVisible.width;
	Rect tr = new Rect(isVisible.x+(visible.width-d.width)/2,isVisible.y+(visible.height-d.height)/2,d.width,d.height);
	int options = (forWho instanceof mTextPad) ? MULTILINE : 0;
	if (forWho instanceof mInput)
		if (((mInput)forWho).isPassword) options |= PASSWORD;
	return popup(forWho.getText(),options,null/*tr*/,forWho.getFont(),forWho,forWho.getPrompt());
}

int labelHeight = 0;
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	super.make(reMake);
	labelHeight = label.getPreferredSize(null).height;
}
public static final int MULTILINE = 0x1;
public static final int PASSWORD = 0x2;

int lastOpts;
//===================================================================
public boolean popup(String value,int options,Rect textRect,Font textFont,EventListener listener,String prompt)
//===================================================================
{
	//time = ewe.sys.Vm.getTimeStamp();
	if (popped) return false;
	popped = true;

	lastOpts = options;

	if (listeners != null) listeners.clear();
	if (listener != null) addListener(listener);

	boolean multiLine = ((options & MULTILINE) != 0);
	EditControl c = multiLine ? (EditControl)pad : (EditControl)input;
	if (textFont != null) c.font = textFont;
	c.setText(value);
	Frame parent = Gui.windowFrame(client);
	Rect ma = Gui.visibleWindowClientArea(client);//mApp.mainApp.getRect();
	//if (textRect == null)
	textRect = new Rect().set(ma.x+5,ma.y+(multiLine ? 25 : 4),ma.width-10,multiLine ? ma.height-30 : 20);
	//if (MULTILINE)
	cards.setPreferredSize(textRect.width+input.spacing*2,textRect.height+input.spacing*2);

	//Frame f = exec(client.getFrame(),null,0);
	Frame f = exec(parent,null,0);
	f.modify(Invisible,0);
	int lh = (prompt == null) ? 0 : labelHeight;
	label.text = prompt != null ? prompt : "";
	//label.setPreferredSize(0,lh);

	if (c == input) {
		input.isPassword = ((options & PASSWORD) != 0);
		cards.select(0);
	}else cards.select(1);

	f.modifyAll(AlwaysRecalculateSizes,0,true);
	f.quickRecalculate = true;

	Dimension r = f.getPreferredSize(null);
	if (r.width > ma.width) r.width = ma.width;
	if (r.height > ma.height) r.height = ma.height;
	int y = textRect.y-input.spacing-lh;
	int x = textRect.x-input.spacing;
	if (y+r.height > ma.height) y = ma.height-r.height;
	if (x + r.width > ma.width) x = ma.width-r.width;
	//ewe.sys.Vm.debug(ma+", "+r+", "+textRect);
	if (x < 0) x = 0;
	if (y < 0) y = 0;
	f.setRect(x,y,r.width,r.height);
	f.modify(0,Invisible);
	f.repaintNow();
	//c.selectAll();
	Gui.takeFocus(c,ByRequest);
	return true;
	//time = ewe.sys.Vm.getTimeStamp()-time;
}

//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.target == input){
		if (ev.type == ev.PRESSED)
			exit(IDOK);
		else if (ev.type == ev.CANCELLED)
			exit(IDCANCEL);
	}
	else super.onControlEvent(ev);
}
//===================================================================
public void close(int retVal)
//===================================================================
{
	ewe.sys.Vm.setSIP(0,getWindow());
	super.close(retVal);
	popped = false;
}
//===================================================================
public String getNewText()
//===================================================================
{
	Control c = ((lastOpts & MULTILINE) != 0) ? (Control)pad : (Control)input;
	return c.getText();
}
//##################################################################
}
//##################################################################

