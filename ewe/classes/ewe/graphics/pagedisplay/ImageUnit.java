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
package ewe.graphics.pagedisplay;
import ewe.graphics.*;
import ewe.fx.*;

//##################################################################
public class ImageUnit extends PageDisplayUnit{
//##################################################################
IImage image;
{
	//flags |= IsHot;
}
//===================================================================
public ImageUnit(IImage image)
//===================================================================
{
	setImage(image);
}
//===================================================================
public ImageUnit setImage(IImage image)
//===================================================================
{
	this.image = image;
	this.width = image == null ? 0 : image.getWidth();
	this.height = image == null ? 0 : image.getHeight();
	return this;
}
/**
* This tells the PageDisplayUnit to display itself only - not its children. The
* provided graphics will have been translated so that (0,0) will map to the top left
* of this units parent.
**/
//===================================================================
public void doPaint(ewe.fx.Graphics g)
//===================================================================
{
	//ewe.sys.Vm.debug("Painting!");
	if (image != null) image.draw(g,x,y,0);
}
//===================================================================
public IImage getImage()
//===================================================================
{
	return image;
}
//##################################################################
}
//##################################################################

