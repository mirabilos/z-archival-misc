/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
* This is used for storing information about an Image retrieved by Image.getImageInfo().
**/
//##################################################################
public class ImageInfo{
//##################################################################

public static final int FORMAT_BMP = 1;
public static final int FORMAT_PNG = 2;
public static final int FORMAT_JPEG = 3;
public static final int FORMAT_GIF = 4;

public int width;
public int height;
public int x;
public int y;
/**
* An estimate on the number of bytes required by the image when decoded.
**/
public int size;
/**
* This will be one of the FORMAT_XXX values.
**/
public int format;
/**
* If this is true it indicates that the image can be scaled while being
* decoded. This may be important if you are decoding very large images on a
* mobile device. If this is true then you can safely open a Stream to the image bytes and
* then use new Image(stream,0,maxWidth,maxHeight) to create an image that is a scaled
* down version of the original image without having to create a full version first.
**/
public boolean canScale;
/**
* Pause after this image for the specified number of milliseconds.
**/
public int pauseInMillis;
/**
* This is only valid when getting one image out of a set of multiple images.
**/
public Image image;
//##################################################################
}
//##################################################################

