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
public class MessageBox extends Form{
//##################################################################

public mImage icon;
public int type;
public MessageArea message = new MessageArea("");
public static int beepSound = Sound.MB_ICONEXCLAMATION;
public boolean doBeep = true;
public String messageText = "";
public int textAlignment = CENTER;
public boolean useScrollBars = false;
public boolean useTextMessage = false;
/**
* You can create this and set it up before displaying the message box,
* but you must also set the "icon" variable to be the icon you wish to display.
**/
public ImageControl iconDisplay;

protected CellPanel addTo;

/**
* This gets a CellPanel for the adding of additional controls before the icon/message
* area.
**/
//===================================================================
public CellPanel getBeforePanel()
//===================================================================
{
	if (addTo == null) addLast(addTo = new CellPanel());
	CellPanel cp = new CellPanel();
	addTo.addLast(cp);
	return cp;
}
/**
* This gets a CellPanel for the adding of additional controls after the icon/message
* area.
**/
//===================================================================
public CellPanel getAfterPanel()
//===================================================================
{
	if (addTo == null) addLast(addTo = new CellPanel());
	CellPanel cp = new CellPanel();
	addLast(cp);
	return cp;
}
//==================================================================
protected void checkButtons()
//==================================================================
{
	doButtons(type);
	if (!hasExitButton()) titleOK = new mButton(close);
	super.checkButtons();
}
//==================================================================
public void make(boolean reMake)
//==================================================================
{
	if (made) return;
	CellPanel p = new CellPanel();
	p.defaultTags.set(INSETS,new Insets(1,1,1,1));
	if (icon != null) {
		if (iconDisplay == null) iconDisplay = new ImageControl(icon);
		p.addNext(iconDisplay).setCell(DONTSTRETCH).setControl(DONTFILL|CENTER);
	}
	TextMessage tm = useTextMessage ? new TextMessage(messageText) : new TextMessage("");
	tm.alignment = textAlignment;
	message.alignment = textAlignment;
	Control c = useTextMessage ? tm : tm.tryMessageArea(this,null,messageText,message);
	p.addLast(useScrollBars ? new ScrollBarPanel((ScrollClient)c) : c,STRETCH,FILL|CENTER);//.setControl(DontFill,Center);
	message.text = messageText;
	if (addTo != null){
		addTo.addLast(p);
	}else
		addLast(p);
	super.make(reMake);
}
//===================================================================
public void shown()
//===================================================================
{
	super.shown();
	if (doBeep)
		if (beepSound != 0) Sound.beep(beepSound);
}
//==================================================================
public MessageBox()
//==================================================================
{
	setHotKey(0,IKeys.ESCAPE);
	moveable = resizable = false;
	borderWidth = 0;
	borderStyle = BDR_NOBORDER;
	if (Gui.isSmartPhone) windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
}
//==================================================================
public MessageBox(String title,String message,int type)
//==================================================================
{
	this();
	this.title = title;
	messageText = message;
	this.type = type;
	//resizable = true;
}
//===================================================================
public void doAction(int how){exit(IDCANCEL);}
//===================================================================

//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	MessageBox mb = new MessageBox("Testing","This is testing an\n an animated image.",MBYESNOCANCEL);
	//mb.icon = new ewe.graphics.AnimatedIcon("0|250|1|250|2|250|3|250|4|250|5|250|6|250|7|250|8|250|9|250|10|1000","ewesoft/apps/jewel/build*.png",null);
	mb.execute();
	//mb.icon.free();
	ewe.sys.mThread.nap(1000);
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

