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
package ewe.fx.print;
import ewe.fx.Image;
import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.Dimension;
//##################################################################
public abstract class PrintSurfaceObject implements PrintSurface{
//##################################################################

protected PageFormat format;

protected boolean testPutRect;
/**
If this is set true, then you intend to call the getScaledImage() method
on PageRect objects that you create during the putPageRect() method.
**/
protected boolean allocatePageRectScaling;

//-------------------------------------------------------------------
protected PrintSurfaceObject(PageFormat format)
//-------------------------------------------------------------------
{
	this.format = format;
}

//===================================================================
public void cancelPageRect(PageRect r)
//===================================================================
{
	r.buffer.free();
	r.image.free();
}
/**
By default this will return the xDPI value of the PageFormat supplied to the
PrintSurfaceObject.
**/
//===================================================================
public double getOutputXDPI()
//===================================================================
{
	return format == null ? 0 : format.xDPI;
}
/**
By default this will return the yDPI value of the PageFormat supplied to the
PrintSurfaceObject.
**/
//===================================================================
public double getOutputYDPI()
//===================================================================
{
	return format == null ? 0 : format.yDPI;
}

//===================================================================
public static int getWidthHeight(double value, double dpi)
//===================================================================
{
	int v = (int)((value*dpi)/72.0);
	if ((((double)v*72.0)/dpi) < value) v++;
	return v;
}
/**
Return the size in pixels on the output device, given a width and height on
this PrintSurface. This would depend on the output X and Y DPI, so if they
are not known then this method will return null.
**/
//===================================================================
public Dimension getOutputSize(double width, double height, Dimension dest)
//===================================================================
{
	double xdpi = getOutputXDPI(), ydpi = getOutputYDPI();
	if (xdpi == 0 || ydpi == 0) return null;
	if (dest == null) dest = new Dimension();
	dest.width = getWidthHeight(width,xdpi);
	dest.height = getWidthHeight(height,ydpi);
	return dest;
}
/**
Move the PageRect to a new location relative to the top left corner of the page. This method will
erase the image buffer.
* @param pr the PageRect to move.
* @param newX the new x location in Points (1/72 of an inch) of the PageRect.
* @param newY the new y location in Points (1/72 of an inch) of the PageRect.
*/
//===================================================================
public void movePageRect(PageRect pr, double newX, double newY)
//===================================================================
{
	//int w = pr.image.getWidth();
	//int h = pr.image.getHeight();
	pr.buffer.free();
	pr.buffer = new ewe.fx.Graphics(pr.image);
	pr.buffer.setColor(ewe.fx.Color.White);
	pr.buffer.fillRect(0,0,pr.image.getWidth(),pr.image.getHeight());
	pr.buffer.setColor(ewe.fx.Color.Black);
	pr.x = newX;
	pr.y = newY;
}
/**
Call this whithin the print() method of the Printable object to get a PageRect
for a section of the current page. You would then draw onto the Image buffer provided
which would then be placed on the paper using putPageRect().
<p>
Note that this method may throw an IllegalArgumentException if the rect requested is too big. In which
case you should request a smaller size rect or a lower DPI.
* @param x The x location in Points (1/72 of an inch) of the PageRect, relative to the top left of the page.
* @param y The y location in Points (1/72 of an inch) of the PageRect, relative to the top left of the page.
* @param width The width in Points (1/72 of an inch) of the PageRect.
* @param height The height in Points (1/72 of an inch) of the PageRect.
* @param dpi The requested DPI of the PageRect. This will affect the number of pixels that are in the image.
* @param hints Hints indicating the kind of drawing that will be done. This should be any of the HINT_XXX values OR'ed together.
* @return a PageRect that you can draw on for later transfer to the printer, or null if a PageRect of the requested
* size and DPI could not be created and HINT_DONT_RESIZE was specified.
*/
//===================================================================
public PageRect getPageRect(double x, double y, double width, double height, double xdpi, double ydpi, int hints)
//===================================================================
{
	while(true){
		try{
			int imageWidth =  getWidthHeight(width,xdpi);
			int imageHeight = getWidthHeight(height,ydpi);
			if (imageWidth <= 0 || imageHeight <= 0) return null;
			Image im = getImageBuffer(imageWidth,imageHeight);
			if (im == null) throw new OutOfMemoryError();
			PageRect pr = new PageRect();
			pr.image = im;
			pr.buffer = new ewe.fx.Graphics(pr.image);
			if (allocatePageRectScaling){
				Dimension out = getOutputSize(width,height,null);
				if (out == null) throw new IllegalStateException("allocatePageRectScaling is true but the output DPI is unknown.");
				if (!pr.allocateScalingBuffer(out.width,out.height))
					throw new OutOfMemoryError();
			}
			pr.buffer.setColor(ewe.fx.Color.White);
			pr.buffer.fillRect(0,0,imageWidth,imageHeight);
			pr.width = width;
			pr.height = height;
			pr.x = x;
			pr.y = y;
			pr.xDPI = xdpi;
			pr.yDPI = ydpi;
			if (testPutRect) putPageRect(pr);
			pr.buffer.setColor(ewe.fx.Color.Black);
			return pr;
		}catch(OutOfMemoryError e){
			boolean rs = false;
			if (width >= 2 && ((hints & HINT_DONT_RESIZE_WIDTH) == 0)) {
				width /= 2;
				rs = true;
			}
			if (height >= 2 && ((hints & HINT_DONT_RESIZE_HEIGHT) == 0)) {
				height /= 2;
				rs = true;
			}
			if (!rs) return null;
		}
	}
}

//-------------------------------------------------------------------
protected abstract Image getImageBuffer(int width, int height);
//-------------------------------------------------------------------

//##################################################################
}
//##################################################################

