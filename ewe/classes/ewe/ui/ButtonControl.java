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

/**
* This is an intermediate class which is the base for mButtons and mCheckBoxes.
**/
//##################################################################
public class ButtonControl extends Control{
//##################################################################

//==================================================================
protected boolean pressState = false, inPress = false;
//==================================================================
public boolean state = false;
public boolean shouldRepeat = false;
public boolean actionOnPress = false;
public boolean flatInside = false;
public int alignment = Gui.CENTER;
public int anchor = Gui.CENTER;
public int imageAnchor = Gui.CENTER;
//==================================================================
/**
* This allows an optional arrow to be displayed along with the text and
* icon. If it is 0, then no arrow is displayed. Otherwise it can be set
* to Up, Down, Left and Right.
**/
public int arrowDirection = 0;//Down;
{
borderColor = Color.Black;
}
//==================================================================
public ButtonControl()
//==================================================================
{
	modify(WantDrag|TakesKeyFocus|WantHoldDown,0);
	holdDownPause = 500;
}

private Color oldBackground;
private boolean switched = false;

//===================================================================
public void gotFocus(int how)
//===================================================================
{
	if (how == ByKeyboard || useNativeTextInput){
		if (!useNativeTextInput) borderStyle |= BDR_DOTTED;
		else if (!switched){
			switched = true;
			oldBackground = backGround;
			backGround = Color.LightGreen;
		}
		repaintNow();
	}
	super.gotFocus(how);
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	if ((borderStyle & BDR_DOTTED) != 0){
		borderStyle &= ~BDR_DOTTED;
		repaintNow();
	}
	if ( switched){
		backGround = oldBackground;
		switched = false;
		repaintNow();
	}
	super.lostFocus(how);
}

//===================================================================
public void penPressed(Point p)
//===================================================================
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;
	if (!menuIsActive()){
		inPress = pressState = true; if (actionOnPress) fullAction(ByMouse); else repaintNow();
		doPenPress(p);
	}else menuState.closeMenu();
}
//===================================================================
public void penHeld(Point p)
//===================================================================
{
	if (!isOnMe(p) || !inPress) return;
	if (shouldRepeat) {
		fullAction(ByMouse,false);
		wantAnotherPenHeld = true;
	}
	else doPenHeld(p);
}
//===================================================================
public void penReleased(Point p)
//===================================================================
{
	if (!inPress) return;
	inPress = pressState = false;
	if (isOnMe(p) && !actionOnPress)
		fullAction(ByMouse);
	else
		repaintNow();
}
//===================================================================
public void startDragging(DragContext dc)
//===================================================================
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;
	dragged(dc);
}
//===================================================================
public void stopDragging(DragContext dc)
//===================================================================
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;
	penClicked(dc.curPoint);
}
Point dragPoint = new Point();
//===================================================================
public void fullAction(int how)
//===================================================================
{
	fullAction(how,true);
}
//===================================================================
public void fullAction(int how,boolean repaint)
//===================================================================
{
	doAction(how);
	if (repaint) repaintNow();
	notifyAction();
}
//===================================================================
public void doAction(int how)
//===================================================================
{
	if (how != ByMouse){
		int bs = borderStyle;
		borderStyle &= ~BDR_DOTTED;
		pressState = true;
		repaintNow();
		if (ewe.sys.Coroutine.getCurrent() != null) ewe.sys.Coroutine.sleep(100);//
		else ewe.sys.Vm.sleep(100);
		pressState = false;
		borderStyle = bs;
		repaintNow();
	}
	super.doAction(how);
}
//===================================================================
public void dragged(DragContext dc)
//===================================================================
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)) || menuIsActive()) return;
	boolean last = pressState;
	pressState = isOnMe(dc.curPoint);
	if (last != pressState) repaintNow();
}
//===================================================================
public void penRightReleased(Point p)
//===================================================================
{
	if (!startDropMenu(p)) super.penRightReleased(p);
}
//===================================================================
public void doPenHeld(Point p)
//===================================================================
{
	if (!startDropMenu(p)) return;
}
//===================================================================
public void doPenPress(Point p)
//===================================================================
{
}
//===================================================================
public boolean startDropMenu(Point p)
//===================================================================
{
	if (getMenu() == null) return false;
	inPress = false;
	pressState = true;
	return tryStartMenu(p);
}
//===================================================================
public void deactivate()
//===================================================================
{
	inPress = pressState = false;
	repaintNow();
}
//===================================================================
public void activate()
//===================================================================
{
	inPress = false;
	pressState = true;
	repaintNow();
}

//##################################################################
}
//##################################################################

