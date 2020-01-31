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

/**
* An InetAddress represents a host name and IP address. Since a host may have
* more than one IP address there may be multiple InetAddresses with the same
* host but with different addresses.
*<p>
* There is no public constructor for InetAddress, you must get one by looking
* up a host by name or by address.
*<p>
* Note that InetAddresses are not necessary for using sockets but they provide
* more control for connections and listening (for ServerSockets).
**/
//##################################################################
public class InetAddress{
//##################################################################
//Do not move the next two.
String name = "unknown";
String address = name;

public static final String INFRA_RED = "infra-red";

//-------------------------------------------------------------------
private InetAddress(){}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
InetAddress(String nm)
//-------------------------------------------------------------------
{
	name = address = nm;
}
//-------------------------------------------------------------------
InetAddress(Object nativeObject){}
//-------------------------------------------------------------------

/**
* This attempts to get all the InetAddresses for a particular host.
* The name can be a computer name or an Internet dotted address (e.g. 123.45.67.89)
* It is completely non-blocking.<p>
* If the IOHandle reports success then the returnValue member of the handle will
* be an array of InetAddress objects representing all host-ip pairings for the
* specified name/address. See the doc on getByName(String name) to see how you
* should use it.
**/
//===================================================================
public static IOHandle getAllByName(String name,IOHandle h)
//===================================================================
{
	if (h == null) h = new IOHandle();
	if (name.equals("infra-red")){
		InetAddress ret = new InetAddress();
		ret.name = ret.address = name;
		h.returnValue = new InetAddress[]{ret};
		h.set(h.Succeeded);
		return h;
	}
	h.set(h.Running);
	_nativeGetAllByName(name,h);
	return h;
}
/**
 * Return an array of InetAddress objects associated with the host name.
 * @param hostName The name to look for.
 * @return an array of InetAddress objects associated with the host name.
 * @exception UnknownHostException if no addresses could be found.
 */
//===================================================================
public static InetAddress [] getAllByName(String hostName) throws UnknownHostException
//===================================================================
{
	IOHandle h = getAllByName(hostName,null);
	if (!h.waitOnFlags(IOHandle.Success,TimeOut.Forever))
		throw new UnknownHostException(hostName);
	return (InetAddress [])h.returnValue;
}
/**
* This gets one address associated with a name. It will block the current
* Coroutine. Here is the source code for it:
* <pre>
* public static InetAddress getByName(String name){
* 	IOHandle h = getAllByName(name,null);
* 	if (!h.waitOnFlags(h.Success,TimeOut.Forever)) return null;
* 	InetAddress [] ret = (InetAddress [])h.returnValue;
* 	return ret[0];
* }
*	</pre>
* @param name The host name to look for.
* @return an InetAddress associated with the host name.
* @exception UnknownHostException if no IP address could be found.
*/
//===================================================================
public static InetAddress getByName(String name) throws UnknownHostException
//===================================================================
{
	if (isANetAddress(name) || "infra-red".equals(name)){
		InetAddress ret = new InetAddress();
		ret.name = ret.address = name;
		return ret;
	}
	InetAddress [] ret = getAllByName(name);
	return ret[0];
}
//-------------------------------------------------------------------
private static native InetAddress [] _nativeGetAllByName(String name,IOHandle h);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
public static native boolean isANetAddress(String name);
//-------------------------------------------------------------------
/**
* This returns the local host name. The form of this name depends on the
* underlying address - it may be a dotted name or it may be a single name.
* In any case it will always be resolvable to an address by using getAllByName()
* or getByName().
**/
//===================================================================
public static native String getLocalHostName();
//===================================================================
/**
 * Return an address for the local host.
 * @return an address for the local host.
 * @exception UnknownHostException If an address for the local host could not be found.
 */
//===================================================================
public static InetAddress getLocalHost() throws UnknownHostException
//===================================================================
{
	return getByName(getLocalHostName());
}
//===================================================================
public String toString() {return getHostName()+";"+getHostAddress();}
//===================================================================
/**
* Return the host name.
**/
//===================================================================
public String getHostName()
//===================================================================
{
	return name;
}
/**
* Return the address in the dotted form.
**/
//===================================================================
public String getHostAddress()
//===================================================================
{
	return address;
}
/**
* Returns the raw IP address of this InetAddress object. The result is in network byte order: the highest order byte of the address is in getAddress()[0].
* @return the raw IP address of this InetAddress object;
*/
//===================================================================
public byte [] getAddress()
//===================================================================
{
	String [] all = ewe.util.mString.split(address,'.');
	byte [] ret = new byte[all.length];
	for (int i = 0; i<all.length; i++)
		ret[i] = (byte)(ewe.sys.Convert.toInt(all[i]) & 0xff);
	return ret;
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
	try{
		if (!(other instanceof InetAddress)) return super.equals(other);
		if (other == this) return true;
		byte [] me = getAddress();
		byte [] ot = ((InetAddress)other).getAddress();
		if (me.length != ot.length) return false;
		for (int i = 0; i<me.length; i++)
			if (me[i] != ot[i]) return false;
		return true;
	}catch(Exception e){
		return false;
	}
}
//===================================================================
public int hashCode()
//===================================================================
{
	try{
		byte [] ret = getAddress();
		return ewe.util.Utils.makeHashCode(ret,0,ret.length);
	}catch(Exception e){
		return super.hashCode();
	}
}
//##################################################################
}
//##################################################################

