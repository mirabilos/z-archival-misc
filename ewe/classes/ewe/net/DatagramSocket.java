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
* A DatagramSocket is used for sending/receiving datagram (connectionless) IP packets (UDP).
**/
//##################################################################
public class DatagramSocket implements StreamCanPause{
//##################################################################
//
// Do not remove or add any variables.
//
int dontUseThisVariable1;
int dontUseThisVariable2;
protected Object localHost;
protected int localPortNotUsed;
int dontUseThisVariable3;
int dontUseThisVariable4;
int dontUseThisVariable5;
int dontUseThisVariable6;
/**
 * Create a DatagramSocket bound to all addresses for the local host and to the next available port number.
 * @exception SocketException if the DatagramSocket could not be created.
 */
//===================================================================
public DatagramSocket() throws SocketException
//===================================================================
{
	this(0,(String)null);
}
/**
 * Create a DatagramSocket bound to all addresses for the local host and to the specified port number.
 * @param port The port number to bind to. A port number of 0 requests the next available port.
 * @exception SocketException if the DatagramSocket could not be created.
 */
//===================================================================
public DatagramSocket(int port) throws SocketException
//===================================================================
{
	this(port,(String)null);
}
/**
 * Create a DatagramSocket bound to a specific local host address and to a specified port number.
 * @param port The port number to bind to. A port number of 0 requests the next available port.
 * @param addr An InetAddress representing one of the addresses assigned to the local host. If
 * this is null then the socket will be bound to all addresses.
 * @exception SocketException if the DatagramSocket could not be created.
 */
//===================================================================
public DatagramSocket(int port, InetAddress addr) throws SocketException
//===================================================================
{
	this(port, addr == null ? null : addr.getHostAddress());
	if (addr != null) localHost = addr;
}
/**
* This is generally only used to connect to the infra-red port, in which case
* you should set hostName to InetAddress.INFRA_RED. Note that Datagram (UDP) sockets on the infra-red
* port is not supported on some operating systems (e.g. WindowsCE/PocketPC and so an exception will
* be thrown).
 * @param port The port number to bind to. A port number of 0 requests the next available port.
* @param hostName The local host address (in dotted notation or InetAddress.INFRA_RED) to bind to.
 * @exception SocketException if the DatagramSocket could not be created.
*/
//===================================================================
public DatagramSocket(int port, String hostName) throws SocketException
//===================================================================
{
	dontUseThisVariable1 = dontUseThisVariable2 = 0;
	localHost = hostName;
	if (localHost == null) localHost = InetAddress.getLocalHostName();
	else if (!localHost.toString().startsWith("infra-red") && hostName != null){
		if (!InetAddress.isANetAddress(hostName)){
			try{
				localHost = InetAddress.getByName(hostName);
				hostName = ((InetAddress)localHost).getHostAddress();
			}catch(UnknownHostException e){
				throw new SocketException("Bad host name: "+hostName);
			}
		}
	}else if (localHost.toString().startsWith("infra-red")){
		localHost = new InetAddress(hostName);
	}
	String err = _nativeCreate(port,0,hostName);
	if (err != null) throw new SocketException(err);
}
//
// This must have the same signature as ServerSocket _nativeCreate.
// It returns an error String on failure and null on success.
//
//-------------------------------------------------------------------
private native String _nativeCreate(int port, int options,String hostName);
//-------------------------------------------------------------------
/**
 * Return the port number on the local host to which this socket is bound.
 * @return the port number on the local host to which this socket is bound.
 */
//===================================================================
public native int getLocalPort();
public native void close();
//===================================================================
/**
* Get the local address the DatagramSocket is bound to.
* @return the local address the DatagramSocket is bound to.
**/
//===================================================================
public InetAddress getLocalAddress()
//===================================================================
{
	if (localHost instanceof InetAddress) return (InetAddress)localHost;
	else try{
		return InetAddress.getByName(ewe.util.mString.toString(localHost));
	}catch(Exception e){
		return null;
	}
}
static boolean hasNativePause = true;

//-------------------------------------------------------------------
private native int nativePauseUntilReady(int type,int time);
//-------------------------------------------------------------------
/**
* Do not call this directly, it is used internally.
**/
//===================================================================
public int pauseUntilReady(int type,int time)
//===================================================================
{
	if (hasNativePause) try{
		return nativePauseUntilReady(type,time);
	}catch(SecurityException e){
		hasNativePause = false;
	}catch(UnsatisfiedLinkError e2){
		hasNativePause = false;
	}
	return 0;
}
/**
* This is a non-blocking receive.
* @param packet The destination to place the data in.
* @return true if the packet was received, false if no packet was available for receiving.
* @exception IOException if an error occured receiving data.
*/
//===================================================================
public boolean receivePacket(DatagramPacket packet) throws IOException
//===================================================================
{
	if (packet.length > packet.data.length) throw new ArrayIndexOutOfBoundsException();
	Object ret = receiveDatagram(packet);
	if (ret == packet) return true;
	else if (ret instanceof String) throw new IOException((String)ret);
	return false;
}
/**
* This is a non-blocking send.
* @param packet The data to send.
* @return true if the packet was sent, false if the packet could not be sent yet.
* @exception IOException if an error occured sending data.
*/
//===================================================================
public boolean sendPacket(DatagramPacket packet) throws IOException
//===================================================================
{
	if (packet.length > packet.data.length) throw new ArrayIndexOutOfBoundsException();
	if (packet.address.getAddress() == null) throw new IllegalArgumentException();
	Object ret = sendDatagram(packet);
	if (ret == packet) return true;
	else if (ret instanceof String) throw new IOException((String)ret);
	return false;
}
//-------------------------------------------------------------------
private native Object receiveDatagram(DatagramPacket packet) throws IOException;
private native Object sendDatagram(DatagramPacket packet) throws IOException;
//-------------------------------------------------------------------


//-------------------------------------------------------------------
private void nap(int type)
//-------------------------------------------------------------------
{
	ewe.sys.Coroutine c = ewe.sys.Coroutine.getCurrent();
	if (c == null) ewe.sys.mThread.nap(100);
	else{
		int toSleep = pauseUntilReady(type,10000);
		if (toSleep == 0) toSleep = 100;
		c.sleep(toSleep);
	}
}
/**
 * Receive an incoming DatagramPacket.
 * This method will block until data is received or an exception is thrown.
 * @param packet This is used to hold the incoming data.
 * @exception IOException if an error occurs while receiving data.
 */
//===================================================================
public void receive(DatagramPacket packet) throws IOException
//===================================================================
{
	while(true){
		if (receivePacket(packet)) return;
		nap(PAUSE_UNTIL_CAN_READ);
	}
}
/**
 * Send a DatagramPacket
 * This method will block until the data is sent.
 * @param packet This is used to hold the outgoing data.
 * @exception IOException if an error occurs while sending the data.
 */
//===================================================================
public void send(DatagramPacket packet) throws IOException
//===================================================================
{
	while(true){
		if (sendPacket(packet)) return;
		nap(PAUSE_UNTIL_CAN_WRITE);
	}
}

//===================================================================
public int getReceiveBufferSize() throws SocketException
//===================================================================
{
	return getSetBufferSize(true,0,true);
}
//===================================================================
public int getSendBufferSize() throws SocketException
//===================================================================
{
	return getSetBufferSize(false,0,true);
}
//===================================================================
public void setReceiveBufferSize(int size) throws SocketException
//===================================================================
{
	 getSetBufferSize(true,size,false);
}
//===================================================================
public void setSendBufferSize(int size) throws SocketException
//===================================================================
{
	 getSetBufferSize(false,size,false);
}
//-------------------------------------------------------------------
private native int getSetBufferSize(boolean isReceive,int size,boolean isGet);
//-------------------------------------------------------------------

//##################################################################
}
//##################################################################

