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
package ewe.net;
import ewe.io.*;
/**
* A DatagramPacket is used to hold data that is sent from or received by a DatagramSocket.
**/
//##################################################################
public class DatagramPacket{
//##################################################################

/* Do not move the next four variables. */
byte [] data;
int length;
InetAddress address;
int port;
/**
* This is used to create a DatagramPacket to be used to receive data.
* The length parameter and size of the data should be set to the maximum packet length
* you are willing to accept.
**/
//===================================================================
public DatagramPacket(byte [] data, int length)
//===================================================================
{
	this.data = data;
	this.length = length;
}
/**
* This is used to create a DatagramPacket to be used to transmit data.
* @param data The data bytes to send.
* @param length The number of data bytes to send.
* @param addr The destination address.
* @param port The destination port.
*/
//===================================================================
public DatagramPacket(byte[] data, int length,InetAddress addr,int port)
//===================================================================
{
	this(data,length);
	address = addr;
	this.port = port;
}
//===================================================================
public InetAddress getAddress()
//===================================================================
{
	return address;
}
//===================================================================
public byte [] getData()
//===================================================================
{
	return data;
}
//===================================================================
public int getLength()
//===================================================================
{
	return length;
}
//===================================================================
public int getPort()
//===================================================================
{
	return port;
}
//===================================================================
public void setAddress(InetAddress addr)
//===================================================================
{
	this.address = addr;
}
//===================================================================
public void setData(byte [] data)
//===================================================================
{
	 this.data = data;
}
//===================================================================
public void setLength(int length)
//===================================================================
{
	 this.length = length;
}
//===================================================================
public void setPort(int port)
//===================================================================
{
	 this.port = port;
}

//##################################################################
}
//##################################################################

