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
package ewe.io;

//##################################################################
public interface RandomAccessStream extends BasicRandomAccessStream, Stream{
//##################################################################
/**
 * Gets the length of the open stream. This is a blocking call
 * @return the length of the stream.
 * @deprecated use length() instead.
 */
public int getLength();
/**
 * Set the file position. This is a blocking call.
 * @param pos The new file position to set.
 * @return true on success, false on failure or error.
 * @deprecated use seek(long pos) instead.
 */
public boolean seek(int pos);
/**
 * Get the file position. This is a blocking call.
 * @return The file position.
 * @deprecated use tell() instead.
 */
public int getFilePosition();
/**
* Set the stream position. This is a blocking call.
* @param position The position to seek to.
* @exception IOException if an error occurs while seeking.
*/
public void seek(long position) throws IOException;
/**
 * Get the current stream position. This is a blocking call.
 * @return the current stream position.
 * @exception IOException if an error occurs.
 */
public long tell() throws IOException;
/**
 * Get the current length of the stream. This is a blocking call.
 * @return the current length of the stream.
 * @exception IOException if an error occurs.
 */
public long length() throws IOException;
/**
* Set the length of the RandomAccessStream if possible. This is a blocking call.
* If the operation could not be performed at all an IOException will be thrown.
* <p>
* Make no assumptions about the success of this method. Not all RAS objects will support
* setStreamLength() or setLength() - not even all Files on all systems will support this.
* For example, PersonalJava/Java 1.1 does not support this feature and will throw an IOException.
* <p>
* What happens to the file position pointer after this method is called is unpredictable,
* especially if you are truncating the file. You should ALWAYS reset the file position pointer
* after calling this method to be where you wish it to be.
**/
public void setLength(long newLength) throws IOException;

//===================================================================
public RandomStream toRandomStream();
//===================================================================
//##################################################################
}
//##################################################################

