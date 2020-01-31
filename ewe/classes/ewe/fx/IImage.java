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
* This is an interface that is implemented by objects that can draw themselves
* onto a Graphics context.
**/
//##################################################################
public interface IImage{
//##################################################################
/**
* This is an option that tells the image that it should draw itself in a disabled (usually "grayed") state.
**/
public static final int DISABLED = 0x1;
/**
* This is an option that tells the image that it should draw an outline around itself.
**/
public static final int OUTLINED = 0x2;
/**
* Draw the full image at the specified co-ordinates in the graphics provided.
**/
public void draw(Graphics g,int x,int y,int options);
/**
* This returns the width of the image.
**/
public int getWidth();
/**
* This returns the height of the image.
**/
public int getHeight();
/**
* This returns a background color if one is set for the image.
**/
public Color getBackground();
/**
* This frees system resources associated with the Image.
**/
public void free();
/**
 * Retrieve the pixels from the Image in encoded ARGB values. If the usesAlpha() method
 * returns false, then the A component of each pixel value (the top 8 bits) should be ignored.
 * @param dest The destination int array. If this is null then a new array should be created.
* @param offset The offset into the array to start placing pixels.
* @param x the x co-ordinate within the image.
* @param y the y co-ordinate within the image.
* @param width the width of the pixel block to get.
* @param height the height of the pixel block to get.
* @param options options for retrieving pixels - currently unused.
 * @return The array containing the pixels, or null if getting pixels is not supported.
*/
public int [] getPixels(int[] dest,int offset,int x,int y,int width,int height,int options);
/**
 * Returns whether the image uses the Alpha channel.
 */
public boolean usesAlpha();

//##################################################################
}
//##################################################################

