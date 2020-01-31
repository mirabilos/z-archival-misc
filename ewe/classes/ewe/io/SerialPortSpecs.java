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
/**
* SerialPortOptions is a single object that specifies all the currently supported Serial Port
* options. To open a Serial Port, create one of these objects, setup the
**/
//##################################################################
public class SerialPortSpecs extends ewe.data.DataObject{
//##################################################################
/**
* The name of the port. On the PC it should be "COM1", "COM2" etc. On Unix/Linux it should  be "TTY0" and "TTY1".
**/
public String portName = "";
/**
* The baud rate for communications - defaults to 9600.
**/
public int baudRate = 9600;
/**
* The number of stop bits - defaults to 1.
**/
public int stopBits = 1;
/**
* This should be one of the SerialPort Parity options - e.g. SerialPort.NOPARITY.
**/
public int parity = SerialPort.NOPARITY;
/**
* The number of data bits - defaults to 8.
**/
public int bits = 8;
/**
* This attempts to create a new Serial Port with the current options.
* @exception ewe.io.IOException if the Serial Port could not be opened.
*/
//===================================================================
public SerialPort connect() throws ewe.io.IOException
//===================================================================
{
	return new SerialPort(this);
}

//##################################################################
}
//##################################################################

