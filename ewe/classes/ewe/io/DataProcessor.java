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
public interface DataProcessor{
//##################################################################

/**
* This returns the size of a block to be processed - any data blocks
* presented for processing must be multiples of this size. If it is less
* than or equal to 1 it is assumed to be 1.
**/
public int getBlockSize();
/**
* This returns the largest block that can be processed. Any data blocks presented
* for processing must be a multiple of getBlockSize() and must be less than or equal
* to getMaxBlockSize(). If this returns less than or equal to zero then there is no
* maximum size.
**/
public int getMaxBlockSize();
/**
* This processes a block of data and places the output in the provided ByteArray. If the
* ByteArray is null a new one will be created. The ByteArray will be expanded if necessary
* to hold the output data.
* <p>
* isLastBlock should be set true if this is the last set of data to be processed. If you want
* to end a processing run, but have no more data to provide - you can set length to be zero
* (you can then also set inputData to be null) and set isLastBlock true.
* <p>
* If isLastBlock is true the DataProcessor should accept any input data, complete processing
* and output ALL of any remaining processed data. It should then reset itself so that the next
* call of processBlock is considered to be the start of a new sequence of data.
* <p>
* If there is an error processing the data, an IOException should be thrown.
**/
public ewe.util.ByteArray processBlock(byte [] inputData,int offset,int length,boolean isLastBlock,ewe.util.ByteArray output) throws IOException;
/**
* This aborts any on-going processing and frees resources associated with the processor. The processor
* should not be used again after this. An IOException should be thrown if there was an error closing the process.
**/
public void closeProcess() throws IOException;
//##################################################################
}
//##################################################################

