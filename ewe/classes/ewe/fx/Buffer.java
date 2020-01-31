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
package ewe.fx;
import ewe.sys.Vm;
import ewe.util.ByteArray;

/**
* A Buffer is a type of Image used specifically for fast and frequent drawing of
* Images.
**/
//##################################################################
public class Buffer extends Image{
//##################################################################

static boolean useNativeClear = true;

//===================================================================
public Buffer(int width, int height, int options)
//===================================================================
{
	super(width,height,options);
}

//===================================================================
public Graphics clear(int x,int y,int width,int height,Color c)
//===================================================================
{
	if (useNativeClear){
		try{
			useNativeClear = nativeClear(x,y,width,height,c.toInt());
			if (useNativeClear){
				return new Graphics(this);
			}
		}catch(UnsatisfiedLinkError e){
			useNativeClear = false;
		}catch(SecurityException e){
			useNativeClear = false;
		}
	}
	Graphics g = new Graphics(this);
	g.setColor(c);
	g.fillRect(x,y,width,height);
	return g;
}

//===================================================================
public native boolean nativeClear(int x,int y,int width,int height,int value);
//===================================================================


//##################################################################
}
//##################################################################

