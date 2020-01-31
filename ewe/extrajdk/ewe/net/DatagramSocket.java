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
public class DatagramSocket implements StreamCanPause{
//##################################################################
//
// Do not remove this and do not add any more.
//
protected Object localHost;
private java.net.DatagramSocket jsock;

//===================================================================
public DatagramSocket() throws SocketException
//===================================================================
{
	this(0,null,null);
}

//===================================================================
public DatagramSocket(int port) throws SocketException
//===================================================================
{
	this(port,null,null);
}

//===================================================================
public DatagramSocket(int port, InetAddress addr) throws SocketException
//===================================================================
{
	this(port, addr, null);
}
/**
* This is generally only used to connect to the infra-red port, in which case
* you should set hostName to InetAddress.INFRA_RED.
* @param port The port to bind to.
* @param hostName The local host address (in dotted notation or InetAddress.INFRA_RED) to bind to.
* @exception SocketException
*/
//===================================================================
public DatagramSocket(int port, String hostName) throws SocketException
//===================================================================
{
	this(port,null,hostName);
}

private Object readingLock = new Object();
private boolean wasRead = false, amReading = false;
private Thread readThread;
private boolean amClosed = false;
private Throwable readError;
private java.net.DatagramPacket reading = new java.net.DatagramPacket(new byte[0],0);

//===================================================================
public DatagramSocket(java.net.DatagramSocket sock)
//===================================================================
{
	jsock = sock;
	java.net.InetAddress in = jsock.getLocalAddress();
	if (in != null) localHost = new InetAddress(jsock.getLocalAddress());
}

//-------------------------------------------------------------------
private DatagramSocket(int port, InetAddress addr, String host) throws SocketException
//-------------------------------------------------------------------
{
	try{
		if (host == null && addr == null){
			jsock = new java.net.DatagramSocket(port);
			localHost = new InetAddress(jsock.getLocalAddress());
		}else if (host == null){
			jsock = new java.net.DatagramSocket(port,(java.net.InetAddress)addr.nativeAddress);
			localHost = addr;
		}else{
			addr = InetAddress.getByName(host);
			jsock = new java.net.DatagramSocket(port,(java.net.InetAddress)addr.nativeAddress);
			localHost = addr;
		}
	}catch(Exception e){
		throw new SocketException(e.getMessage());
	}
}
/**
 * Return the port number on the local host to which this socket is bound.
 * @return the port number on the local host to which this socket is bound.
 */
//===================================================================
public int getLocalPort()
//===================================================================
{
	return jsock.getLocalPort();
}
//===================================================================
public void close()
//===================================================================
{
	synchronized(readingLock){
		amClosed = true;
		readingLock.notifyAll();
	}
	jsock.close();
}
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
if (amClosed) throw new IOException("Socket was closed.");
if (readThread == null){
	readThread = new Thread(){
		public void run(){
			try{
				while(true){
					synchronized(readingLock){
						if (!amClosed && !amReading){
							try{
								readingLock.wait();
							}catch(InterruptedException e){}
						}
						if (amClosed) return;
						if (!amReading) continue;
					}

					try{
						jsock.receive(reading);
					}catch(java.io.IOException e){
						synchronized(readingLock){
							readError = e;
							return;
						}
					}

					synchronized(readingLock){
						wasRead = true;
						amReading = false;
					}
				}
			}finally{
				//ewe.sys.Vm.debug("Read thread leaving.");
			}
		}
	};
	readThread.start();
}
	synchronized(readingLock){
		if (readError != null) {
			throw new IOException(readError.getMessage());
		}
		if (wasRead){
			packet.setData(reading.getData());
			packet.setLength(reading.getLength());
			packet.setAddress(new InetAddress(reading.getAddress()));
			packet.setPort(reading.getPort());
			wasRead = false;
			return true;
		}
		if (!amReading){
			amReading = true;
			toJavaDatagram(reading,packet,false);
			readingLock.notifyAll();
		}
		return false;
	}
}

//-------------------------------------------------------------------
private void toJavaDatagram(java.net.DatagramPacket sending,DatagramPacket packet,boolean doAddr)
//-------------------------------------------------------------------
{
	sending.setData(packet.getData());
	sending.setLength(packet.getLength());
	if (doAddr) {
		sending.setAddress((java.net.InetAddress)packet.getAddress().nativeAddress);
		sending.setPort(packet.getPort());
	}
}
private java.net.DatagramPacket sending = new java.net.DatagramPacket(new byte[0],0);
/**
* This is a non-blocking send.
* @param packet The data to send.
* @return true if the packet was sent, false if the packet could not be sent yet.
* @exception IOException if an error occured receiving data.
*/
//===================================================================
public boolean sendPacket(DatagramPacket packet) throws IOException
//===================================================================
{
	toJavaDatagram(sending,packet,true);
	try{
		jsock.send(sending);
		return true;
	}catch(java.io.IOException e){
		throw new IOException(e.getMessage());
	}
}
static boolean hasNativePause = true;

//-------------------------------------------------------------------
//private native int nativePauseUntilReady(int type,int time);
//-------------------------------------------------------------------

//===================================================================
public int pauseUntilReady(int type,int time)
//===================================================================
{
/*
	if (hasNativePause) try{
		return nativePauseUntilReady(type,time);
	}catch(SecurityException e){
		hasNativePause = false;
	}catch(UnsatisfiedLinkError e2){
		hasNativePause = false;
	}
*/
	return 0;
}

//-------------------------------------------------------------------
private void nap(int type)
//-------------------------------------------------------------------
{
	ewe.sys.Coroutine c = ewe.sys.Coroutine.getCurrent();
	if (c == null) ewe.sys.mThread.nap(100);
	else{
		int toSleep = pauseUntilReady(type,1000);
		if (toSleep == 0) toSleep = 100;
		c.sleep(toSleep);
	}
}

//===================================================================
public void receive(DatagramPacket packet) throws IOException
//===================================================================
{
	while(true){
		if (receivePacket(packet)) return;
		nap(PAUSE_UNTIL_CAN_READ);
	}
}
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
	try{
		return jsock.getReceiveBufferSize();
	}catch(java.net.SocketException e){
		throw new ewe.net.SocketException(e.getMessage());
	}catch(NoSuchMethodError e){
		return 0;
	}
}
//===================================================================
public int getSendBufferSize() throws SocketException
//===================================================================
{
	try{
		return jsock.getSendBufferSize();
	}catch(java.net.SocketException e){
		throw new ewe.net.SocketException(e.getMessage());
	}catch(NoSuchMethodError e){
		return 0;
	}
}
//===================================================================
public void setReceiveBufferSize(int size) throws SocketException
//===================================================================
{
	try{
	 	jsock.setReceiveBufferSize(size);
	}catch(java.net.SocketException e){
		throw new ewe.net.SocketException(e.getMessage());
	}catch(NoSuchMethodError e){
	}
}
//===================================================================
public void setSendBufferSize(int size) throws SocketException
//===================================================================
{
	try{
		jsock.setSendBufferSize(size);
	}catch(java.net.SocketException e){
		throw new ewe.net.SocketException(e.getMessage());
	}catch(NoSuchMethodError e){
	}
}

//##################################################################
}
//##################################################################
