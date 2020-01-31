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
/**
* A PageFormat class is sent to a Printable object during the printing of
* a document. It gives information on page resolution of the printer being
* used.
**/
//##################################################################
public class PageFormat{
//##################################################################
//
// Don't move the next 11 fields. They are used by the native method.
//
public double fullPageWidth = 8.5*72;
public double fullPageHeight = 11.0*72;
public double imageableWidth = 6.5*72;
public double imageableHeight = 9*72;
private double pointsPerInch = 72;
public double xScreenScale = 1.0;
public double yScreenScale = 1.0;
public double imageableX = 72;
public double imageableY = 72;
/** This is the output X-DPI and it may not be know, and if so, it will be 0. **/
public double xDPI = 0;
/** This is the output Y-DPI and it may not be know, and if so, it will be 0. **/
public double yDPI = 0;
//
public double requestedX = -1, requestedY = -1, requestedWidth = -1, requestedHeight = -1;
public double requestedFullWidth = -1, requestedFullHeight = -1;
public double requestedXDpi = 0.0, requestedYDpi = 0.0;
/**
* This returns the full width of the page in Points (printer dots - 72 per inch).
* This value will not be valid until printing begins.
**/
//===================================================================
//public double getWidth() {return fullPageWidth;}
//===================================================================
/**
* This returns the full height of the page in Points (printer dots - 72 per inch).
* This value will not be valid until printing begins.
**/
//===================================================================
//public double getHeight() {return fullPageHeight;}
//===================================================================
/**
* This returns the width of the printable area of the page in Points (printer dots - 72 per inch).
* This value will not be valid until printing begins.
* <p>
* Note that the graphics which is passed to the Printable object during printing
* will have its origin set to the 0,0 position of the printable area of the
* page.
**/
//===================================================================
//public double getImageableWidth() {return imageableWidth;}
//===================================================================
/**
* This returns the height of the printable area of the page in Points (printer dots - 72 per inch).
* This value will not be valid until printing begins.
* <p>
* Note that the graphics which is passed to the Printable object during printing
* will have its origin set to the 0,0 position of the printable area of the
* page.
**/
//===================================================================
//public double getImageableHeight() {return imageableHeight;}
//===================================================================
/**
* This is currently always 72. However, using Transforms on the PrinterJob Graphics, you can
* draw an Image of arbitrary size and resolution.
**/
//===================================================================
//public double getPointsPerInch(){return pointsPerInch;}
//===================================================================
/**
* This is the ratio of screen dpi to printer dpi horizontally. Multiply this by
* pixel values in user space to calculate the number of pixels in printer space
* equal to the same size in user space.
**/
//public float getXScreenScale() {return xScreenScale;}
/**
* This is the ratio of screen dpi to printer dpi vertically. Multiply this by
* pixel values in user space to calculate the number of pixels in printer space
* equal to the same size in user space.
**/
//public float getYScreenScale() {return yScreenScale;}
/**
* This returns the number of Points (printer dots - 72 per inch)
* horizontally between the left edge of the paper and
* the start of the printable area of the paper.
**/
//===================================================================
//public double getImageableX(){return imageableX;}
//===================================================================
/**
* This returns the number of Points (printer dots - 72 per inch)
* vertically between the top edge of the paper and
* the start of the printable area of the paper.
**/
//===================================================================
//public double getImageableY(){return imageableY;}
//===================================================================

/**
This returns the actual DPI of the output device horizontally if it is known. If it is not
known this will return 0;
**/
//===================================================================
//public double getXDPI(){return xDPI;}
//===================================================================
/**
This returns the actual DPI of the output device vertically if it is known. If it is not
known this will return 0;
**/
//===================================================================
//public double getYDPI(){return yDPI;}
//===================================================================
/**
This requests that the output DPI be set to the specified values. Note that this is just
a request and the device may not provide the requested DPIs. The getXDPI() and getYDPI() values
will return the actual DPIs (if available) at print time. They may not be available immediately
after this call.
**/
//===================================================================
public void requestDPI(double xDPI, double yDPI)
//===================================================================
{
	requestedXDpi = xDPI;
	requestedYDpi = yDPI;
}
/**
* This will scale a pixel value from user pixels into print pixels so that the value will
* appear roughly the same size. e.g. if 100 pixels on the screen equals 1 inch, this will
* convert 100 to the appropriate number of pixels on the printer which also equals 1 inch.
* This is not 100% reliable however since it depends on the underlying OS providing accurate
* screen DPIs which it does not always do.
**/
//===================================================================
//public double xScale(double value) {return (double)(value*xScreenScale);}
//===================================================================
/**
* This will scale a pixel value from user pixels into print pixels so that the value will
* appear roughly the same size. e.g. if 100 pixels on the screen equals 1 inch, this will
* convert 100 to the appropriate number of pixels on the printer which also equals 1 inch.
* This is not 100% reliable however since it depends on the underlying OS providing accurate
* screen DPIs which it does not always do.
**/
//===================================================================
//public double yScale(double value) {return (double)(value*yScreenScale);}
//===================================================================
/**
* This requests that the imageable area of the paper to be set to the specified values, which
* are given in units of Points (printer dots - 1/72 of an inch). Therefore this can be used regardless of the eventual
* printer resolution.
* @param x the x offset of the imageable area.
* @param y the y offset of the imageable area.
* @param width the width of the imageable area.
* @param height the height of the imageable area.
*/
//===================================================================
public void requestImageableArea(double x, double y, double width, double height)
//===================================================================
{
	requestedX = x;
	requestedY = y;
	requestedWidth = width;
	requestedHeight = height;
}
/**
 * This requests that the size of the paper be set to specified values, which
* are given in units of Points (printer dots - 1/72 of an inch). Therefore this can be used regardless of the eventual
* printer resolution.
 * @param width the requested size of the paper.
 * @param height the requested height of the paper.
 */
//===================================================================
public void requestPageSize(double width, double height)
//===================================================================
{
	requestedFullWidth = width;
	requestedFullHeight = height;
}
/*
//===================================================================
public String toString()
//===================================================================
{
	return "Full page = "+getWidth()+", "+getHeight()+", Imageable Rect = "+getImageableX()+", "+getImageableY()+", "+getImageableWidth()+", "+getImageableHeight();
}
*/
/**
Return the Requested Imageable area for the PageFormat - if one was requested, otherwise null
will be returned.
**/
/*
//===================================================================
public PointRect getRequestedImageableArea()
//===================================================================
{
	if (requestedX == -1)	return null;
	return new PointRect().set(requestedX,requestedY,requestedWidth,requestedHeight);
}
*/
/**
Return the Requested Imageable area for the PageFormat - if one was requested, otherwise null
will be returned.
**/
/*
//===================================================================
public PointRect getRequestedPageSize()
//===================================================================
{
	if (requestedFullWidth == -1)	return null;
	return new PointRect().set(0,0,requestedFullWidth,requestedFullHeight);
}

//===================================================================
public double getRequestedXDPI() {return requestedXDpi;}
//===================================================================
public double getRequestedYDPI() {return requestedYDpi;}
//===================================================================
*/

//===================================================================
public void acceptRequests()
//===================================================================
{
	if (requestedFullWidth != -1){
		fullPageWidth = requestedFullWidth;
		fullPageHeight = requestedFullHeight;
	}
	if (requestedX != -1){
		imageableX = requestedX;
		imageableY = requestedY;
		imageableHeight = requestedHeight;
		imageableWidth = requestedWidth;
	}
	if (requestedXDpi <= 0){
		xDPI = requestedXDpi;
		yDPI = requestedYDpi;
	}
}
//##################################################################
}
//##################################################################

