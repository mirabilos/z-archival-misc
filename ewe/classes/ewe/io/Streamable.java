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
package ewe.io;

//##################################################################
public interface Streamable{
//##################################################################

/**
* Requests the possibly asynchronous creation of a Stream object for reading or writing.
* @param randomStream if this is true then a request is being made for
* a RandomAccessStream, otherwise either a readable or writable Stream will be returned.
* @param mode For RandomAccessStreams this can be "r" or "rw". For writable Streams this
should be "w" or "a" (for append) and for readable Streams it should be "r".
* @return A Handle used to form monitoring the creation process and for retrieving the
* final value.<p>
The calling code should wait on the Handle.Success flag to be set. If this happens the
returnValue of the Handle will be set to the acquired Stream. If the Handle fails then
the errorObject of the Handle will be set to an IOException.
* @exception IllegalArgumentException if the mode is incorrect.
*/
//===================================================================
public ewe.sys.Handle toStream(boolean randomStream,String mode) throws IllegalArgumentException;
//===================================================================
/**
* Get the name associated with this Streamable object.
**/
//===================================================================
public String getName();
//===================================================================
//##################################################################
}
//##################################################################

