/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.sys.*;
import ewe.util.*;
import ewe.reflect.FieldTransfer;

/**
* The only purpose of this class is to provide space for Control's static variables and
* to keep the number of Fields in control below 64.
**/
//##################################################################
public abstract class ControlBase{
//##################################################################

public static final int
	GotPenDown = 0x1, DidHoldDown = 0x2, PenIsOn = 0x4;
public static boolean doubleBuffer = true;
public static final int TAKE_FIRST_PRESS = 1000;
/**
* You can use tags from TAG_USER_DATA up to and including  TAG_LAST_USER_DATA, which
* spans 1000 tags.
**/
public static final int TAG_USER_DATA = 1001;
/**
* You can use tags from TAG_USER_DATA up to and including  TAG_LAST_USER_DATA, which
* spans 1000 tags.
**/
public static final int TAG_LAST_USER_DATA = 2000;
public static boolean globalEnabled = true, globalEditable = true;
public static boolean globalDrawFlat = false, globalSmallControls = false, globalPalmStyle = false;
public static int doubleClickTime = 500;
public static Point pressPoint = new Point(0,0);
public static Point curPoint = new Point(0,0);
static boolean wantAnotherPenHeld = false;
public static boolean firstPress = true;
static long lastClick;
static Control lastClickedControl = null;
public static Control clipOwner;
static String emptyString = "";
/**
* This is set to be the current PenEvent in the onPenEvent() handler.
**/
public static PenEvent currentPenEvent;
public static String unnamed = "unnamed";
public static Control debugControl = null;
public static boolean debugFlag = false;
public static Object clipObject;
public static MenuItem [] clipItems;
public static boolean useNativeTextInput = (Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_USE_NATIVE_TEXT_INPUT) != 0;
//##################################################################
}
//##################################################################

