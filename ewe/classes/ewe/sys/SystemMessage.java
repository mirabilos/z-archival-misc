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
package ewe.sys;

//##################################################################
public class SystemMessage{
//##################################################################
// DO NOT CHANGE OR MOVE THESE VARIABLES.
public int type;
public int wparam;
public int lparam;
public int time;
public int x;
public int y;
public int state;

public static final int REMOVED = 0x1;
public static final int PAINT                        = 0x000F;
public static final int MOUSEFIRST                   = 0x0200;
public static final int MOUSELAST                   = 0x020A;
public static final int KEYFIRST                     = 0x0100;
public static final int KEYLAST                      = 0x0108;
public static final int TIMER                        = 0x0113;
public static final int CALLBACK                     = 0x0401;

public boolean isMouse() {return type >= MOUSEFIRST && type <= MOUSELAST;}
public boolean isKey() {return type >= KEYFIRST && type <= KEYLAST;}
public boolean isPen() {return isMouse();}
public boolean isPaint() {return type == PAINT;}
public boolean isTimer() {return type == TIMER;}

//##################################################################
}
//##################################################################

