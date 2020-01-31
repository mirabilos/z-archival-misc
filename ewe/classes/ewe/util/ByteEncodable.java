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
package ewe.util;
/**
* A ByteEncodable object is one that can encode its data as a sequence of
*  bytes.
**/
//##################################################################
public interface ByteEncodable{
//##################################################################
/**
 * This requests the Object to encode itself as a stream of bytes which is appended
 * to the destination ByteArray. If the destination ByteArray is null, then the object
 * should report how many bytes <b>would</b> be used if the object was encoded.
 * @param dest The destination ByteArray, or null to determine the number of bytes needed to encode.
 * @return The number of bytes appended to the ByteArray or the number of bytes needed to encode.
 */
public int encodeBytes(ByteArray dest);
//##################################################################
}
//##################################################################



