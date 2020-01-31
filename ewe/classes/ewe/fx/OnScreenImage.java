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
This is an Image that is being displayed on some control surface or other. It allows
you to set the ImageRefresher for the IImage.<p>
The ImageRefresher is <b>weakly</b> referenced by the Image. This allows separate
threads animate the image but stop when the surface the Image is being displayed on
no longer exists.
*/
//##################################################################
public interface OnScreenImage extends IImage{
//##################################################################
/**
<b>Weakly</b> set the ImageRefresher for the Image.
*/
public void setRefresher(ImageRefresher refresher);
/**
Change the ImageRefresher for the Image only if the old Refresher
is the same as the one specified.
@param newRefresher The new ImageRefresher for the image.
@param oldRefresher What the old ImageRefresher was expected to be.
@return true if the refresher was changed, false if it was not changed because the old
Refresher was not the same as that specified in the parameter.
*/
public boolean changeRefresher(ImageRefresher newRefresher, ImageRefresher oldRefresher);
/**
Retrieve the ImageRefresher for the image which is <b>weakly</b> referenced by
the OnScreenImage.
*/
public ImageRefresher getRefresher();

//##################################################################
}
//##################################################################

