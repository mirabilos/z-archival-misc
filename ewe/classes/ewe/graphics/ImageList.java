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

//##################################################################
public class ImageList extends ewe.util.Vector {
//##################################################################

//==================================================================
public ImageList findImages(Rect where,ImageList dest){return findImages(where,dest,false);}
public ImageList findImages(Rect where,ImageList dest,boolean doingOneImage)
//==================================================================
{
	if (dest == null) dest = new ImageList();
	for (int i = 0; i<size(); i++){
		AniImage im = (AniImage)get(i);
		if (doingOneImage){
			if (where.intersects(im.lastDrawn)) {
				dest.add(im);
				//ewe.sys.Vm.debug(im.lastDrawn+", "+where);
			}
		}else
			if (where.intersects(im.location)) dest.add(im);
	}
	return dest;
}
//==================================================================
public ImageList findHotImages(Point where,ImageList dest)
//==================================================================
{
	if (dest == null) dest = new ImageList();
	for (int i = 0; i<size(); i++){
		AniImage im = (AniImage)get(i);
		if (im.onHotArea(where.x,where.y)) dest.add(im);
	}
	return dest;
}
private static ImageList temp = new ImageList();
//==================================================================
public AniImage findHotImage(Point where)
//==================================================================
{
	temp.clear();
	findHotImages(where,temp);
	if (temp.size() == 0) return null;
	return (AniImage)temp.get(temp.size()-1);
}
/**
* Move the image to the top. Returns true if it was moved, false if it
* was already on top.
**/
//==================================================================
public boolean moveOnTop(AniImage which)
//==================================================================
{
	// (find(which) == size()-1) return false;
	remove(which);
	if ((which.properties & which.AlwaysOnTop) != 0)
		add(which);
	else{
		int i;
		for (i = size(); i != 0; i--){
			AniImage ai = (AniImage)get(i-1);
			if ((ai.properties & which.AlwaysOnTop) == 0) break;
		}
		insert(i,which);
	}
	return true;
}
//==================================================================
public boolean moveToBack(AniImage which)
//==================================================================
{
	if (find(which) == 0) return false;
	remove(which);
	insert(0,which);
	return true;
}
//##################################################################
}
//##################################################################

