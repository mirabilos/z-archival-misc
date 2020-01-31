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
import ewe.fx.Graphics;
import ewe.fx.Rect;
import ewe.fx.PixelBuffer;
//##################################################################
public class PageRect{
//##################################################################
protected double width;
protected double height;
protected double x;
protected double y;
protected double xDPI, yDPI;
protected Image image;
protected Graphics buffer;

private PixelBuffer scaleFrom, scaleTo;
private Image scaled;
/**
Attempt to allocate enough buffer space to allow the entire PageRect can be scaled from
the specified source dimensions to an Image of the specified destination dimensions.<p>
Returns true if this was successful, false if not. If this method is successful, then
the method getScaled image will successfully return a scaled image without running out of
resources or memory.
**/
//===================================================================
public boolean allocateScalingBuffer(int sourceWidth, int sourceHeight, int destWidth, int destHeight, boolean usesAlpha)
//===================================================================
{
	try{
		scaleFrom = new PixelBuffer(sourceWidth,sourceHeight);
		scaleTo = new PixelBuffer(destWidth,destHeight);
		scaled = new Image(destWidth,destHeight,Image.RGB_IMAGE);
		if (usesAlpha) scaled.enableAlpha();
		return true;
	}catch(Throwable t){
		scaleFrom = scaleTo = null;
		scaled = null;
		return false;
	}
}
/**
Attempt to allocate enough buffer space to allow the entire PageRect can be scaled from
the dimensions of the allocated image buffer to an Image of the specified destination dimensions.<p>
Returns true if this was successful, false if not. If this method is successful, then
the method getScaled image will successfully return a scaled image without running out of
resources or memory.
<p>
This version of the method can only be called after the PageRect has already had its
buffer Image setup.
**/
//===================================================================
public boolean allocateScalingBuffer(int destWidth, int destHeight)
//===================================================================
{
	return allocateScalingBuffer(image.getWidth(),image.getHeight(),destWidth,destHeight,false);
}
/**
This is used to get a scaled version of the data that was drawn into the
PageRect via the Graphics returned by getGraphics().
<p>
Only call this if a call to allocateScalingBuffer() had succeeded, and only use
the same destWidth and destHeight used in that method call. The returned scaled image
will be re-used the next time getScaledImage() is called.
**/
//===================================================================
public Image getScaledImage(int destWidth, int destHeight)
//===================================================================
{
	try{
		image.getPixels(scaleFrom.getBuffer(),0,0,0,image.getWidth(),image.getHeight(),0);
		scaleFrom.scale(destWidth,destHeight,null,0,scaleTo);
		scaled.setPixels(scaleTo.getBuffer(),0,0,0,destWidth,destHeight,0);
		return scaled;
	}catch(Throwable t){
		return null;
	}
}
/**
This returns the Image that was used to store images drawn <b>if</b> an Image
<b>was</b> used for storing the drawn data. It is possible that a PageRect may choose
to buffer the drawn commands in a different way. If this returns null then you can
use getSourceGraphics() instead to attempt to retrieve the drawn data.
**/
//===================================================================
public Image getImage()
//===================================================================
{
	return image;
}
/**
Returns the width of the PageRect in Points (1/72 of an inch). This is different to
the number of pixels in the buffer horizontally, which depends on the dpi you requested on creation.
To get the number of pixels in the image horizontally call getBufferWidth().
**/
//===================================================================
public double getWidthOnPaper() {return width;}
//===================================================================
/**
Returns the height of the PageRect in Points (1/72 of an inch). This is different to
the number of pixels in the buffer vertically, which depends on the dpi you requested on creation.
To get the number of pixels in the image vertically call getBufferHeight().
**/
//===================================================================
public double getHeightOnPaper() {return height;}
//===================================================================
/**
Returns the x location of the PageRect in Points (1/72 of an inch)
in relation to the top-left corner of the paper.
**/
//===================================================================
public double getX() {return x;}
//===================================================================
/**
Returns the y location of the PageRect in Points (1/72 of an inch)
in relation to the top-left corner of the paper.
**/
//===================================================================
public double getY() {return y;}
//===================================================================
/**
Returns the graphics that you use to draw on.
**/
//===================================================================
public Graphics getGraphics() {return buffer;}
//===================================================================
/**
Returns a graphics that you use to copy data from. Call free() on this Graphics
after you use it.
**/
//===================================================================
public Graphics getSourceGraphics(){return new Graphics(image,true);}
//===================================================================

/**
Returns the width of the buffer in pixels. This is independant of the DPI of the printer.
**/
//===================================================================
public int getBufferWidth() {return image.getWidth();}
//===================================================================
/**
Returns the height of the buffer in pixels. This is independant of the DPI of the printer.
**/
//===================================================================
public int getBufferHeight() {return image.getHeight();}
//===================================================================
/**
Returns the DPI of the buffer horizontally. This indicates the number of pixels in the
buffer that equal one inch on the printed page.
**/
//===================================================================
public double getXDPI() {return xDPI;}
//===================================================================
/**
Returns the DPI of the buffer horizontally. This indicates the number of pixels in the
buffer that equal one inch on the printed page.
**/
//===================================================================
public double getYDPI() {return yDPI;}
//===================================================================
/**
Return the factor that will convert values in Point co-ordinates to pixel co-ordinates horizontally. Multiply
the point co-ordinate value by the value returned and then round to the nearest integer.
**/
//===================================================================
public double getXPointToPixelScale() {return xDPI/72.0;}
//===================================================================
/**
Return the factor that will convert values in Point co-ordinates to pixel co-ordinates horizontally. Multiply
the point co-ordinate value by the value returned and then round to the nearest integer.
**/
//===================================================================
public double getYPointToPixelScale() {return yDPI/72.0;}
//===================================================================
/**
Returns true if the specified co-ordinate in Points is within this PageRect.
* @param x the x co-ordinate in Points (1/72 of an inch).
* @param y the y co-ordinate in Points (1/72 of an inch).
* @return true if the specified co-ordinate in Points is within this PageRect.
*/
//===================================================================
public boolean isWithin(double x, double y)
//===================================================================
{
	if (x < this.x || x > this.x+this.width) return false;
	if (y < this.y || y > this.y+this.height) return false;
	return true;
}
/**
Returns true if the specified rectangle in Points intersects with this PageRect.
* @param x the x co-ordinate in Points (1/72 of an inch).
* @param y the y co-ordinate in Points (1/72 of an inch).
* @param width the width in Points (1/72 of an inch).
* @param height the height in Points (1/72 of an inch).
* @return true if the specified rectangle in Points intersects with this PageRect.
*/
//===================================================================
public boolean isWithin(double x, double y, double width, double height)
//===================================================================
{
	double left = this.x, right = this.x+this.width, top = this.y, bottom = this.y+this.height;
	double tleft = x, tright = x+width, ttop = y, tbottom = y+height;

	if (right < tleft) return false;
	if (left > tright) return false;
	if (bottom < ttop) return false;
	if (top > tbottom) return false;
	return true;
}

/**
 * This PageRect, which was created using co-ordinates in points and using specified
 * X and Y DPI values, is represented by an Image with a specific number of pixels.
 * This method converts an area given in points into a Rect given in pixels.
 * @param pr the area in points to convert.
 * @param destination an optional destination rect for the converted area in pixels.
 * @return the destination Rect or a new one if it is null.
 */
//===================================================================
public Rect scaleToPixels(PointRect pr, Rect destination)
//===================================================================
{
	return pr.scaleToRect(destination,xDPI/72,yDPI/72);
}
/**
 * This PageRect, which was created using co-ordinates in points and using specified
 * X and Y DPI values, is represented by an Image with a specific number of pixels.
 * This method converts the area covered by this PageRect into a Rect given in pixels.
 * @param destination an optional destination rect for the converted area in pixels.
 * @return the destination Rect or a new one if it is null.
 */

//===================================================================
public Rect scaleToPixels(Rect destination)
//===================================================================
{
	return new PointRect(getX(),getY(),getWidthOnPaper(),getHeightOnPaper()).scaleToRect(destination,xDPI/72,yDPI/72);
}
/**
Scale a dimension value in Points (1/72 of an inch) into the correct number of pixels
for this PageRect - but the return value will always be a minimum of 1.
**/
//===================================================================
public int scaleDimension(double pointSize)
//===================================================================
{
	int value = (int)(((pointSize*xDPI)/72.0)+0.5);
	if (value == 0) value = 1;
	return value;
}
//===================================================================
public Rect adjustForPen(double penSize, Rect source, Rect destination)
//===================================================================
{
	int sz = scaleDimension(penSize);
	int ex = (sz-1)/2;
	int x = source.x+ex, y = source.y+ex, width = source.width-(sz-1), height = source.height-(sz-1);
	if (destination == null) destination = new Rect();
	return destination.set(x,y,width,height);
}
//===================================================================
public void adjustForPen(double penSize, Rect source)
//===================================================================
{
	adjustForPen(penSize, source, source);
}
//##################################################################
}
//##################################################################


