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
import ewe.ui.*;

//##################################################################
public class ImageBuffer{
//##################################################################
public Image image;
public Graphics graphics;
//==================================================================
public void free()
//==================================================================
{
	if (image != null) image.free();
	if (graphics != null) graphics.free();
	image = null;
	graphics = null;
}
//==================================================================
public boolean isSameSize(int width,int height)
//==================================================================
{
	if (image == null) return false;
	if (width <= 0) width = 1;
	if (height <= 0) height = 1;
	return (image.getWidth() == width && image.getHeight() == height);
}
//===================================================================
public Graphics get(int width,int height)
//===================================================================
{
	return get(width,height,false);
}

//===================================================================
public int imageCreationOptions = 0;
//===================================================================

//==================================================================
public Graphics get(int width,int height,boolean exactly)
//==================================================================
{
	if (width <= 0) width = 1;
	if (height <= 0) height = 1;
	if (image != null)
		if (image.getWidth() < width || image.getHeight() < height)
			free();
		else if (exactly)
			if (image.getWidth() != width || image.getHeight() != height)
				free();
	if (image == null) {
		image = new Image(width,height,imageCreationOptions);
		graphics = Graphics.createNew(image);
	}
	graphics.reset();
	graphics.setClip(0,0,width,height);
	return graphics;
}

//===================================================================
public Graphics getBuffer(int width,int height,Rect area,Color background,boolean exactly)
//===================================================================
{
	if (width <= 0) width = 1;
	if (height <= 0) height = 1;
	if (image != null)
		if (!(image instanceof Buffer) || image.getWidth() < width || image.getHeight() < height)
			free();
		else if (exactly)
			if (image.getWidth() != width || image.getHeight() != height)
				free();
	if (image == null) image = new Buffer(width,height,imageCreationOptions);
	if (graphics != null) graphics.free();
	if (area != null){
		graphics = ((Buffer)image).clear(area.x,area.y,area.width,area.height,background);
		graphics.setClip(area.x,area.y,area.width,area.height);
	}else{
		graphics = ((Buffer)image).clear(0,0,width,height,background);
		graphics.setClip(0,0,width,height);
	}
	return graphics;
}
//##################################################################
}
//##################################################################

