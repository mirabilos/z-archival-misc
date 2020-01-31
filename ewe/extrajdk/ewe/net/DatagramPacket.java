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

//##################################################################
public class DatagramPacket{
//##################################################################

java.net.DatagramPacket jd;

//===================================================================
public DatagramPacket(byte [] data, int length)
//===================================================================
{
	jd = new java.net.DatagramPacket(data,length);
}
//===================================================================
public DatagramPacket(byte [] data, int length,InetAddress addr,int port)
//===================================================================
{
	jd = new java.net.DatagramPacket(data,length,(java.net.InetAddress)addr.nativeAddress,port);
}

//===================================================================
public InetAddress getAddress()
//===================================================================
{
	return new InetAddress(jd.getAddress());
}
//===================================================================
public byte [] getData()
//===================================================================
{
	return jd.getData();
}
//===================================================================
public int getLength()
//===================================================================
{
	return jd.getLength();
}
//===================================================================
public int getPort()
//===================================================================
{
	return jd.getPort();
}
//===================================================================
public void setAddress(InetAddress addr)
//===================================================================
{
	java.net.InetAddress ja = (java.net.InetAddress)addr.nativeAddress;
	jd.setAddress(ja);
}
//===================================================================
public void setData(byte [] data)
//===================================================================
{
	 jd.setData(data);
}
//===================================================================
public void setLength(int length)
//===================================================================
{
	 jd.setLength(length);
}
//===================================================================
public void setPort(int port)
//===================================================================
{
	 jd.setPort(port);
}

//##################################################################
}
//##################################################################
