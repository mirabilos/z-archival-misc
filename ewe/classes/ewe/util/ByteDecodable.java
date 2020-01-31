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
import ewe.io.StreamCorruptedException;

/**
* A ByteDecodable object is one that can decode its data from a sequence of
* encoded bytes.
**/
//##################################################################
public interface ByteDecodable{
//##################################################################
/**
 * This requests the Object to decode itself from a stream of bytes.
 * @param source The source of the encoded bytes.
 * @param startOffsetInSource Where the object should start decoding from.
 * @param bytesLeftInSource The number of bytes left starting from startOffsetInSource.
 * @return the number of bytes decoded.
* @exception StreamCorruptedException if the data stored in the bytes is corrupted.
* @exception ClassNotFoundException if the class of a stored object could not be found.
*/
public int decodeBytes(byte[] source, int startOffsetInSource, int bytesLeftInSource)
throws StreamCorruptedException, ClassNotFoundException
;
//##################################################################
}
//##################################################################

