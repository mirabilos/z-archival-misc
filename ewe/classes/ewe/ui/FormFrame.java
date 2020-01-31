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
public class FormFrame extends Frame{
//##################################################################
public Form myForm;
public boolean hasExtraTitleControls = false;
//==================================================================
public CellPanel titleBar,middleBar,rightButtons;
public mLabel title;
//==================================================================
boolean useSizeBar = true;
//==================================================================
public FormFrame(Form f,int options)
//==================================================================
{
	myForm = f;
	if (f.noBorder || (((options & Gui.NEW_WINDOW) == Gui.NEW_WINDOW) && ((myForm.windowFlagsToClear & Window.FLAG_HAS_TITLE) == 0))) {
		borderWidth = 0;
		borderStyle = BDR_NOBORDER;
	}else{
		borderWidth = 3;
		borderStyle = EDGE_RAISED|BDR_OUTLINE;
	}
	name = myForm.title;
	contents.addLast(f).setCell(STRETCH);
	if (f.hasTopBar){
		titleBar = new DragPanel();
		if (Gui.isSmartPhone) titleBar.setPreferredSize(-1,20);
		((DragPanel)titleBar).canDrag = f.moveable || (((options & Gui.NEW_WINDOW) == Gui.NEW_WINDOW) && !ewe.sys.Vm.isMobile());
		top.addLast(titleBar).setCell(HSTRETCH);
		mLabel ml = title = new mLabel(f.title);
		ml.modify(PenTransparent,0);
		ml.foreGround = Color.White;
		ml.backGround = Color.DarkBlue;
		ml.alignment = Gui.Left;
		titleBar.addNext(ml,STRETCH,FILL|NORTHWEST);
		//mControl c = new mCanvas();
		//titleBar.addLast(c);
		//c.backGround = Color.Black;
		titleBar.addNext(middleBar = new CellPanel()).setCell(VSTRETCH);
		titleBar.addLast(rightButtons = new CellPanel()).setCell(VSTRETCH);
		middleBar.backGround = Color.Black;
		middleBar.foreGround = Color.White;
		rightButtons.equalWidths = false;
	}
	if (!f.resizable && !f.hasTopBar) contentsOnly = true;
	addListener(f);
}

//==================================================================
public void make(boolean remake)
//==================================================================
{
	if (made) {
		//System.out.println("Already made!");
		//getPreferredSize();
		return;
	}
	//myForm.checkButtons(); <- This is done in Form now.
	if (myForm.hasTopBar){
		rightButtons.addNext(myForm.titleControls);
		rightButtons.addNext(myForm.titleOK);
		rightButtons.addNext(myForm.titleCancel);
		hasExtraTitleControls = myForm.titleControls != null ||
			(myForm.titleOK != null && myForm.titleCancel != null);

	//if (myForm.hasTopBar){ This was here before. I moved it above.
		if (myForm.topControls != null) top.addNext(myForm.topControls,HSTRETCH,HFILL|NORTHWEST);//.setCell(DontStretch).setControl(DontFill,NorthWest);//.setSpan(2);
		top.addChildListener(myForm);
	}else top = null;
	if (bottom != null) bottom.addChildListener(myForm);
	if (myForm.resizable && useSizeBar) {
		DragPanel resizeDragBar;
		trueBottom.addLast(resizeDragBar = new DragPanel());//.setControl(Top|Right);
		resizeDragBar.setCursor(ewe.sys.Vm.RESIZE_CURSOR);
		resizeDragBar.isTopBar = false;
		resizeDragBar.borderStyle = EDGE_SUNKEN;
	}
	super.make(remake);
}

//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if (ev.type == ControlEvent.PRESSED){
		if (ev.target == myForm.titleOK) myForm.exit(Form.IDOK);
		else if (ev.target == myForm.titleCancel) myForm.exit(Form.IDCANCEL);
	}
	super.onControlEvent(ev);
}


//==================================================================
public void doBorder(Graphics g)
//==================================================================
{
/*
	g.setColor(Color.Black);
	g.drawRect(0,0,width,height);
	g.setColor(Color.White);
	g.drawLine(1,1,width-2,1);
	g.drawLine(1,1,1,height-2);
	g.setColor(Color.Black);
	g.drawLine(1,height-2,width-2,height-2);
	g.drawLine(width-2,1,width-2,height-2);
	g.setColor(Color.LightGray);
	g.drawRect(2,2,width-4,height-4);
*/
	super.doBorder(g);
}
//##################################################################
}
//##################################################################

