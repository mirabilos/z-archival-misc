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
* A SipButton is a <b>placeholder</b> for the SipButton on a PocketPC system. You should
* never create a SipButton and add it to a Control. It is designed to be placed <b>either</b>
* in a CellPanel which will be added to the bottom of a Form (and thereby lie over the area
* where the SipButton would be) or to the extreme right of a mTabbedPanel (which should be
* set up with tabLocation being SOUTH) placed at the bottom of the Form.<p>
*
<p>To use with the toolbar or tabbed panel use one of the static placeIn() methods.
**/

//##################################################################
public final class SipButton extends mButton{
//##################################################################

public static ewe.fx.Dimension sipButtonSize = new ewe.fx.Dimension(35,23);

//-------------------------------------------------------------------
private static boolean simulate()
//-------------------------------------------------------------------
{
	return (ewe.sys.Vm.getParameter(ewe.sys.Vm.SIMULATE_SIP) != 0);
}
private static boolean onBottom = true;
/*
static {
	if (simulate) ewe.sys.Vm.setParameter(ewe.sys.Vm.SIMULATE_SIP,1);
}
*/
/**
Simulate the SIP on a desktop system.
**/
//===================================================================
public static void simulateSIP()
//===================================================================
{
	ewe.sys.Vm.setParameter(ewe.sys.Vm.SIMULATE_SIP,1);
}
//-------------------------------------------------------------------
SipButton()
//-------------------------------------------------------------------
{
	if (!mApp.rotated) setPreferredSize(sipButtonSize.width,sipButtonSize.height);
	else setPreferredSize(sipButtonSize.height,sipButtonSize.width);
	//backGround = new ewe.fx.Color(255,0,0);
	modify(NoFocus | (simulate() ? 0 : Disabled|Invisible),TakesKeyFocus);
	image = ewe.fx.ImageCache.cache.getImage("ewe/pencilsmall.png");
	arrowDirection = Up;
}

//===================================================================
public static boolean hasSipButton()
//===================================================================
{
	return (onBottom && simulate()) || ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_SIP_BUTTON_ON_SCREEN) != 0);
}
/**
 * Place a SipButton in an empty CellPanel that will be used
as a tool/menu bar along the bottom of the screen. If the platform sports
a SipSelectButton in the bottom right of the screen (currently only
PocketPC) then a SipButton will be created and placed on the
right of the toolbar. An empty CellPanel is added to the left of the
of the SipButton and this is then returned. You should then add
your tools to this.
 * @param destination The destination CellPanel that will act as the tool/menu bar.
 * @return The CellPanel that you should add your controls to.
 */
//===================================================================
public static CellPanel placeIn(CellPanel destination)
//===================================================================
{
	if (!hasSipButton() && !simulate()) return destination;
	CellPanel cp = new CellPanel();
	destination.addNext(cp).setCell(HSTRETCH).setPreferredSize(1,1);
	destination.addNext(new SipButton()).setCell(DONTSTRETCH);
	return cp;
}
/**
* Place in an mTabbedPanel display to the extreme right of the tabs. The mTabbedPanel should
* be set up with tabLocation being SOUTH.
* @param tp The mTabbedPanel to add the SipButton to.
* @return
*/
//===================================================================
public static void placeIn(mTabbedPanel tp)
//===================================================================
{
	if (!hasSipButton() && !simulate()) return;
	tp.getExtremeControls(false).addNext(new SipButton());
	tp.cardPanel.clearSipOnChange = true;
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	super.make(reMake);
	Window win = getWindow();
	if (win == null) {
		for (Control c = getParent(); c != null; c = c.getParent()){
			if (c instanceof FormFrame){
				Form f = ((FormFrame)c).myForm;
				f.windowFlagsToSet |= Window.FLAG_SHOW_SIP_BUTTON;
				return;
			}
		}
		return;
	}
	if ((win.getWindowFlags() & win.FLAG_SHOW_SIP_BUTTON) == 0){
		win.setWindowFlags(win.FLAG_SHOW_SIP_BUTTON);
		ewe.sys.Vm.setSIP(2,win);
	}
}
//===================================================================
public void doAction(int how)
//===================================================================
{
	Window win = getWindow();
	ewe.sys.Vm.setSIP(ewe.sys.Vm.SIP_OVERRIDE_USE_SIP|(ewe.sys.Vm.getSIP() ^ ewe.sys.Vm.SIP_ON),win);
}
//##################################################################
}
//##################################################################

