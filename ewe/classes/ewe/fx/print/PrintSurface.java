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

//##################################################################
public interface PrintSurface{
//##################################################################

/**
This is a "hint" flag for getPageRect(). If the size of the PageRect specified
is too big getPageRect() will attempt to reduce the size. If this flag is set
then it will not reduce the width of the PageRect, only its height.
**/
public static final int HINT_DONT_RESIZE_WIDTH = 0x1;
/**
This is a "hint" flag for getPageRect(). If the size of the PageRect specified
is too big getPageRect() will attempt to reduce the size. If this flag is set
then it will not reduce the height of the PageRect, only its width.
**/
public static final int HINT_DONT_RESIZE_HEIGHT = 0x2;
/**
This is a "hint" flag for getPageRect. If it is specified and the PageRect of the
requested size and dpi could not be created, then the PrinterJob should not attempt
to create a smaller one, but should just return null.
**/
public static final int HINT_DONT_RESIZE = HINT_DONT_RESIZE_WIDTH|HINT_DONT_RESIZE_HEIGHT;
/**
This is a "hint" flag for getPageRect, and indicates that only black and white drawing
will be done on the PageRect.
**/
public static final int HINT_MONO_IMAGE = 0x10;
/**
This is a "hint" flag for getPageRect, and indicates that only gray scale drawing
will be done on the PageRect.
**/
public static final int HINT_GRAYSCALE_IMAGE = 0x20;

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
* @param xDpi The requested  horizontal DPI of the PageRect. This will affect the number of pixels that are in the image horizontally.
* @param yDpi The requested  horizontal DPI of the PageRect. This will affect the number of pixels that are in the image horizontally.
* @param hints Hints indicating the kind of drawing that will be done and how you want the method to behave. This should be any of the HINT_XXX values OR'ed together.
* @return a PageRect that you can draw on for later transfer to the printer, or null if a PageRect of the requested
* size and DPI could not be created, or if even a resized version could not be allocated.
*/
//===================================================================
public PageRect getPageRect(double x, double y, double width, double height, double xDpi, double yDpi, int hints);
//===================================================================

/**
Move the PageRect to a new location relative to the top left corner of the page. This method will
erase the image buffer and reset the Graphics associated with it.
* @param pr the PageRect to move.
* @param newX the new x location in Points (1/72 of an inch) of the PageRect.
* @param newY the new y location in Points (1/72 of an inch) of the PageRect.
*/
//===================================================================
public void movePageRect(PageRect pr, double newX, double newY);
//===================================================================
/**
This sends the data on the PageRect to the output device (usually a printer). This does not
free the resources of the PageRect and it can be moved using movePageRect().
**/
//===================================================================
public void putPageRect(PageRect r);
//===================================================================
/**
This frees the resources of the PageRect without sending it to the output device. You should not
use it after this.
**/
//===================================================================
public void cancelPageRect(PageRect r);
//===================================================================
/**
This returns the actual output DPI of the device on the X-axis or zero if the DPI is unkown.
This is useful because it is a waste of resources to request PageRect objects with a DPI that is higher than the actual
output DPI. Requesting a PageRect with the DPI equal to the output DPI will give you the best
possible resolution, but will consume more resources.
**/
//===================================================================
public double getOutputXDPI();
//===================================================================
/**
This returns the actual output DPI of the device on the Y-axis or zero if the DPI is unkown.
This is useful because it is a waste of resources to request PageRect objects with a DPI that is higher than the actual
output DPI. Requesting a PageRect with the DPI equal to the output DPI will give you the best
possible resolution, but will consume more resources.
**/
//===================================================================
public double getOutputYDPI();
//===================================================================
//##################################################################
}
//##################################################################

