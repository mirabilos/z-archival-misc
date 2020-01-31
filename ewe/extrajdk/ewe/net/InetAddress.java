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
package ewe.net;
import ewe.sys.*;
import ewe.io.*;

//##################################################################
public class InetAddress{
//##################################################################
Object nativeAddress;
protected String name;
protected String address;

//-------------------------------------------------------------------
private InetAddress(){}
//-------------------------------------------------------------------

//===================================================================
public InetAddress(Object nativeAddress)
//===================================================================
{
	this.nativeAddress = nativeAddress;
}
/**
* This attempts to get all the InetAddresses for a particular host.
* The name can be a computer name or an Internet dotted address (e.g. 123.45.67.89)
**/
//===================================================================
public static IOHandle getAllByName(final String name,IOHandle h)
//===================================================================
{
	if (h == null) h = new IOHandle();
	final Handle hand = h;
	hand.set(hand.Running);
	new Thread(){
		public void run(){
			InetAddress [] got = _getAllByName(name);
			synchronized(Coroutine.lockObject){
				hand.returnValue = got;
				if (got == null) hand.set(hand.Failed);
				else hand.set(hand.Succeeded);
			}
		}
	}.start();
	return h;
}
/**
 * Return an array of InetAddress objects associated with the host name.
 * @param hostName The name to look for.
 * @return an array of InetAddress objects associated with the host name.
 * @exception UnknownHostException if no addresses could be found.
 */
//===================================================================
public static InetAddress [] getAllByName(String hostName) throws ewe.net.UnknownHostException
//===================================================================
{
	IOHandle h = getAllByName(hostName,null);
	if (!h.waitOnFlags(IOHandle.Success,TimeOut.Forever))
		throw new ewe.net.UnknownHostException(hostName);
	return (InetAddress [])h.returnValue;
}

//-------------------------------------------------------------------
public static boolean isANetAddress(String name)
//-------------------------------------------------------------------
{
	if (name == null) return false;
	char [] all = ewe.sys.Vm.getStringChars(name);
	for (int i = 0; i<all.length; i++)
		if (!Character.isDigit(all[i]) && all[i] != '.') return false;
	return true;
}
/**
* This gets one address associated with a name. It will block the current
* Coroutine.
**/
//===================================================================
public static InetAddress getByName(String name) throws ewe.net.UnknownHostException
//===================================================================
{
	if (isANetAddress(name) || "infra-red".equals(name)){
		InetAddress ret = new InetAddress();
		ret.name = ret.address = name;
		try{
			ret.nativeAddress = java.net.InetAddress.getByName(name);
		}catch(Exception e){}
		return ret;
	}
	InetAddress [] ret = getAllByName(name);
	return ret[0];
}

//-------------------------------------------------------------------
private static InetAddress [] _getAllByName(String name)
//-------------------------------------------------------------------
{
	try{
		java.net.InetAddress [] all = java.net.InetAddress.getAllByName(name);
		InetAddress [] ret = new InetAddress[all.length];
		for (int i = 0; i<all.length; i++){
			ret[i] = new InetAddress();
			ret[i].nativeAddress = all[i];
		}
		return ret;
	}catch(Exception e){
		return null;
	}
}

//===================================================================
public static String getLocalHostName()
//===================================================================
{
	try{
		return java.net.InetAddress.getLocalHost().getHostName();
		//return getLocalHosts()[0];
	}catch(Exception e){
		return null;
	}
}
/**
 * Return an address for the local host.
 * @return an address for the local host.
 * @exception UnknownHostException If an address for the local host could not be found.
 */
//===================================================================
public static InetAddress getLocalHost() throws UnknownHostException
//===================================================================
{
	try{
		return new InetAddress(java.net.InetAddress.getLocalHost());
	}catch(Exception e){
		throw new ewe.net.UnknownHostException("local host");
	}
}

//===================================================================
public String toString() {return nativeAddress == null ? getHostName()+";"+getHostAddress() : nativeAddress.toString();}
//===================================================================
//===================================================================
public String getHostName()
//===================================================================
{
	if (nativeAddress == null) return name == null ? "<none>" : name;
	else return ((java.net.InetAddress)nativeAddress).getHostName();
}
//===================================================================
public String getHostAddress()
//===================================================================
{
	if (nativeAddress == null) return address == null ? "<none>" : address;
	else return ((java.net.InetAddress)nativeAddress).getHostAddress();
}
/**
* Returns the raw IP address of this InetAddress object. The result is in network byte order: the highest order byte of the address is in getAddress()[0].
* @return the raw IP address of this InetAddress object;
*/
//===================================================================
public byte [] getAddress()
//===================================================================
{
	return ((java.net.InetAddress)nativeAddress).getAddress();
}
/**
 * Two InetAddresses are equal if getAddress() called on each one returns precisely
 * the same sequence of bytes.
 * @param other another InetAddress
 * @return true if the other object is considered equal to this one.
 */
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof InetAddress)) return super.equals(other);
	try{
		InetAddress ot = (InetAddress)other;
		return ((java.net.InetAddress)nativeAddress).equals((java.net.InetAddress)ot.nativeAddress);
	}catch(Exception e){
		return false;
	}
}
//===================================================================
public int hashCode()
//===================================================================
{
	try{
		return ((java.net.InetAddress)nativeAddress).hashCode();
	}catch(Exception e){
		return super.hashCode();
	}
}
//##################################################################
}
//##################################################################
