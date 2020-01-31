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

//##################################################################
class imageHotArea implements Area{
//##################################################################

Area area;
mImage image,target;
Rect buff;
Rect full;
boolean checkTransparent = false;
int transparentColor = 0;
//===================================================================
public imageHotArea(Area within,mImage image,mImage target)
//===================================================================
{
	area = within;
	this.image = image;
	this.target = target;
	full = area.getRect(null);
	buff = new Rect().set(full);
}
//===================================================================
public imageHotArea(int transparent,mImage image,mImage target)
//===================================================================
{
	this(new Rect(0,0,target.location.width,target.location.height),image,target);
	transparentColor = transparent & 0xffffff;
	checkTransparent = true;
}
int [] pix;
//===================================================================
public boolean isIn(int x,int y)
//===================================================================
{
	boolean in = area.isIn(x-target.location.x,y-target.location.y);
	if (!in) return false;
	if (!checkTransparent || image.image == null) return true;
	Image img = image.image;
	if (pix == null) pix = new int[1];
	img.getPixels(pix,0,x-target.location.x,y-target.location.y,1,1,0);
	//ewe.sys.Vm.debug(""+pix[0]+", "+transparentColor);
	return ((pix[0] & 0xffffff) != transparentColor);
}
//===================================================================
public boolean intersects(Area other)
//===================================================================
{
	buff.x = full.x+target.location.x;
	buff.y = full.y+target.location.y;
	return buff.intersects(other);
}
public Rect getRect(Rect dest)
{
	dest = Rect.unNull(dest).set(full);
	dest.x += target.location.x;
	dest.y += target.location.y;
	return dest;
}
//##################################################################
}
//##################################################################

