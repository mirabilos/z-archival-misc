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
package ewe.graphics;
import ewe.ui.*;
import ewe.fx.*;

//############################################################
public interface ReactiveImage {
//############################################################
public static final int DragStarted = 1;
public static final int DragStopped = 2;
public static final int DragOver = 3;
public static final int DragOff = 4;
public static final int DropOn = 5;
public static final int Drag = 6;

public static final int Clicked = 1;
//public static final int DoubleClicked = 2;

public void dragEvent(InteractivePanel panel,int event,ImageDragContext dc);
public void clickEvent(InteractivePanel panel,int event);

//############################################################
}
//############################################################

