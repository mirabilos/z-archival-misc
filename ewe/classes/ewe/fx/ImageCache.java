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
* An ImageCache is used to fetch and keep images so that Images do not have to
* be re-decoded each time they are accessed.
**/
//##################################################################
public class ImageCache extends ewe.data.PropertyList{
//##################################################################

/**
* This is a global ImageCache that you can use. The system also uses it.
**/
public static ImageCache cache = new ImageCache();
/**
 * Get an Image with an optional mask image or transparent color.
 * @param imageName The full resource name of the image.
 * @param maskOrColor Either a resource name of a mask image or a Color object.
 * @return The decoded Image - either newly decoded or retrieved from the Cache.
 * @exception IllegalArgumentException If the named image does not exist or is not formatted correctly.
 */
//===================================================================
public IImage get(String imageName,Object maskOrColor) throws IllegalArgumentException
//===================================================================
{
	Object got = getValue(imageName,null);
	if (got instanceof IImage) return (IImage)got;
	mImage mi = null;
	if (maskOrColor instanceof Color) mi = new mImage(imageName,(Color)maskOrColor);
	else if (maskOrColor instanceof String) mi = new mImage(imageName,(String)maskOrColor);
	else {
		mi = new mImage(imageName);
	}
	if (mi != null) set(imageName,mi);
	return mi;
}
/**
 * Get an Image.
 * @param imageName The full resource name of the image.
 * @return The decoded Image - either newly decoded or retrieved from the Cache.
 * @exception IllegalArgumentException If the named image does not exist or is not formatted correctly.
 */
//===================================================================
public IImage getImage(String imageName) throws IllegalArgumentException
//===================================================================
{
	return this.get(imageName,(Color)null);
}
/**
 * Free all images and clear the cache.
 */
//===================================================================
public void free()
//===================================================================
{
	for (int i = 0; i<size(); i++){
		ewe.data.Property p = (ewe.data.Property)get(i);
		if (p.value instanceof IImage) ((IImage)p.value).free();
	}
	clear();
}
//##################################################################
}
//##################################################################

