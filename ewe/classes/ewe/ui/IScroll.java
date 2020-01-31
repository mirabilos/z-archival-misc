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

//##################################################################
public interface IScroll {
//##################################################################
public static final int Horizontal = 1, Vertical = 2, PageLower = 3, PageHigher = 4, TrackTo = 5, ScrollHigher = 6, ScrollLower = 7;
public static final int Lower = 100, Higher = 101;
/** Specifies that the track will most likely be used in a situation where
there is no mouse or touch screen, so the track should be made small enough
to just indicate the position without regard for the user being able to easily
drag it. */
public static final int OPTION_INDICATOR_ONLY = 0x1;

//##################################################################
}
//##################################################################

