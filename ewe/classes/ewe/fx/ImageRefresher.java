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
package ewe.fx;
/**
An ImageRefresher is used by an animated image (AniImage) to display it
on the screen. The refresh() method is called by the AniImage if it determines
that it has been changed somehow.<p>
How that refreshing is actually done is up to the refresher.<p>
*/
//##################################################################
public interface ImageRefresher {
//##################################################################

/** An option that tells the ImageRefresher to keep the image on screen. */

public static final int KEEP_VISIBLE = mImage.KeepOnScreen;

public void refresh(IImage image, int options);

//##################################################################
}
//##################################################################

