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
import ewe.sys.*;
import ewe.data.*;
/**
This class is used to hold the constant Form static data to reduce the
number of variables in the Form class.
**/
//##################################################################
public abstract class FormBase extends CellPanel{
//##################################################################

/**
* This is set to Gui.CENTER_FRAME by default.
**/
public static int defaultShowOptions = Gui.CENTER_FRAME;//Gui.NEW_WINDOW;
/**
* The tick icon.
**/
public static IImage tick = new DrawnIcon(DrawnIcon.TICK,10,10,new Color(0,128,0));
/**
* The cross (X) icon - colored Red.
**/
public static IImage cross = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(128,0,0));
/**
* The close (X) icon - colored Black.
**/
public static IImage close = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0,0,0));
/**
* The tool icon - supposed to look like a spanner.
**/
public static IImage tools = new mImage("ewe/SmallConfig.bmp","ewe/SmallConfigMask.bmp");
/**
* The stop icon.
**/
public static IImage stop = new mImage("ewe/Stop.bmp",Color.White);
/**
* A predefined return value from the Form - it is the same as IDOK.
**/
public static final int IDYES = 1;
/**
* A predefined return value from the Form - it is the same as IDYES.
**/
public static final int IDOK = 1;
/**
* A predefined return value from the Form.
**/
public static final int IDNO = 2;
/**
* A predefined return value from the Form.
**/
public static final int IDBACK = 3;
/**
* A predefined return value from the Form.
**/
public static final int IDCANCEL = -1;
//public static final int IDTIMEDOUT = -2;
/**
* An option for doButtons()
**/
public static final int YESB = 0x1;
/**
* An option for doButtons()
**/
public static final int NOB = 0x2;
/**
* An option for doButtons()
**/
public static final int CANCELB = 0x4;
/**
* An option for doButtons()
**/
public static final int OKB = 0x8;
/**
* An option for doButtons()
**/
public static final int BACKB = 0x10;
/**
* This is an OK button that has ENTER assigned as the hotkey.
**/
public static final int DEFOKB = 0x20;
/**
* This is an OK button that has CANCEL assigned as the hotkey.
**/
public static final int DEFCANCELB = 0x40;
/*
public static final int BADOKB = 0x10;
public static final int APPLYB = 0x20;
public static final int RESETB = 0x40;
public static final int DEFAULTB = 0x80;
public static final int CLOSEB = 0x100;
*/
/**
* A MessageBox type.
**/
public static final int MBB = 0x1000;
/**
* A MessageBox type.
**/
public static final int MBYESNO = MBB|YESB|NOB;
/**
* A MessageBox type.
**/
public static final int MBYESNOCANCEL = MBB|YESB|NOB|DEFCANCELB;
/**
* A MessageBox type.
**/
public static final int MBOK = MBB|DEFOKB;
//public static final int MBBADOK = MBB|BADOKB;
/**
* A MessageBox type.
**/
public static final int MBOKCANCEL = MBB|DEFOKB|DEFCANCELB;
/**
* A MessageBox type.
**/
public static final int MBNONE = MBB;
/**
A standard Form action string.
**/
public static final String EXIT_IDCANCEL = "EXIT_IDCANCEL";
/**
A standard Form action string.
**/
public static final String EXIT_IDOK = "EXIT_IDOK";
/**
A standard Form action string.
**/
public static final String EXIT_IDYES = "EXIT_IDYES";
/**
A standard Form action string.
**/
public static final String EXIT_IDNO = "EXIT_IDNO";
/**
A standard Form action string.
**/
public static final String EXIT_IDBACK = "EXIT_IDBACK";

//##################################################################
}
//##################################################################

