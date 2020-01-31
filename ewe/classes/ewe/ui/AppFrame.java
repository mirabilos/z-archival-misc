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
import ewe.util.Iterator;
/**
An AppFrame is a specialized Frame only used for the main Frame for all other
application Frames under special circumstances (e.g. under Smartphones). You would
almost never have to create or use one yourself.<p>
When an AppFrame is resized it will adjust all of its children such that they now
fit into the Frame, in case they do not any more.
**/
//##################################################################
public class AppFrame extends Frame{
//##################################################################

//===================================================================
public void resizeTo(int width, int height)
//===================================================================
{
	for (Iterator it = getChildren(); it.hasNext();){
		Control c = (Control)it.next();
		if (c.width == this.width && c.height == this.height){
			//
			// If the child is exactly the size of the parent, then make it
			// also continue to be so.
			//
			c.setRect(0,0,width,height);
		}else{
			//if (
		}
	}
	super.resizeTo(width,height);
}
//##################################################################
}
//##################################################################

