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
package ewe.applet;

import ewe.ui.*;
import ewe.sys.SystemMessage;

//##################################################################
class FullEvent{
//##################################################################
int systemType;
int type;
int key;
int x;
int y;
int modifiers;
int timestamp;
Object who;
Object data;

public FullEvent(int s,Object obj)
{
	systemType = s;
	who = obj;
}
public FullEvent(int s,int t,int k,int xx, int yy,int m,int ts)
{
	systemType = s;
	type = t;
	key = k;
	x = xx;
	y = yy;
	modifiers = m;
	timestamp = ts;
}
//##################################################################
}
//##################################################################
