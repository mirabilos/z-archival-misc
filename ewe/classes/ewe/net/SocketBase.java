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
public abstract class SocketBase extends StreamObject{
//##################################################################
public Object remoteHost;
public int remotePort;

static final int TCP_NODELAY = 0x1;
static final int SO_LINGER = 0x2;
static final int SO_TIMEOUT = 0x3;
static final int RX_BUFFERSIZE = 0x4;
static final int TX_BUFFERSIZE = 0x5;
static final int SO_KEEPALIVE = 0x6;

protected abstract int getSocketParameter(int whichParameter) throws SocketException;
protected abstract int setSocketParameter(int whichParameter,boolean booleanValue,int intValue) throws SocketException;

protected InputStream inputStream;
protected OutputStream outputStream;
protected boolean inputIsShutdown = false;
protected boolean outputIsShutdown = false;

//##################################################################
protected class SocketInputStream extends ewe.io.InputStream{
//##################################################################

//===================================================================
public SocketInputStream()
//===================================================================
{
	super(SocketBase.this);
}

//===================================================================
public void shutdown() throws IOException
//===================================================================
{
	super.shutdown();
	if (!inputIsShutdown){
		inputIsShutdown = true;
		if (outputIsShutdown) SocketBase.this.close();
		else{
			new ewe.sys.mThread(){
				public void run(){
					try{
						byte[] buff = new byte[1024];
						while(true)
							if (read(buff,0,buff.length) < 0) break;
					}catch(IOException e){}
				}
			}.start();
		}
	}
}
//##################################################################
}
//##################################################################
//##################################################################
protected class SocketOutputStream extends ewe.io.OutputStream{
//##################################################################

//===================================================================
public SocketOutputStream()
//===================================================================
{
	super(SocketBase.this);
}

//===================================================================
public void shutdown() throws IOException
//===================================================================
{
	super.shutdown();
	if (!outputIsShutdown){
		outputIsShutdown = true;
		if (inputIsShutdown) SocketBase.this.close();
	}
}
//##################################################################
}
//##################################################################


//===================================================================
public void shutdownOutput() throws IOException
//===================================================================
{
	if (outputStream != null) outputStream.shutdown();
}
//===================================================================
public void shutdownInput() throws IOException
//===================================================================
{
	if (inputStream != null) inputStream.shutdown();
}
//===================================================================
public int getSoLinger() throws SocketException
//===================================================================
{
	return getSocketParameter(SO_LINGER);
}
//===================================================================
public void setSoLinger(boolean on,int val) throws SocketException
//===================================================================
{
	setSocketParameter(SO_LINGER,on,val);
}
/*
//===================================================================
public int getSoTimeout() throws SocketException
//===================================================================
{
	return getSocketParameter(SO_TIMEOUT);
}
//===================================================================
public void setSoTimeout(int val) throws SocketException
//===================================================================
{
	setSocketParameter(SO_TIMEOUT,false,val);
}
*/
//===================================================================
public boolean getTcpNoDelay() throws SocketException
//===================================================================
{
	return getSocketParameter(TCP_NODELAY) != 0;
}
//===================================================================
public void setTcpNoDelay(boolean on) throws SocketException
//===================================================================
{
	setSocketParameter(TCP_NODELAY,on,0);
}
//===================================================================
public boolean getKeepAlive() throws SocketException
//===================================================================
{
	return getSocketParameter(SO_KEEPALIVE) != 0;
}
//===================================================================
public void setKeepAlive(boolean on) throws SocketException
//===================================================================
{
	setSocketParameter(SO_KEEPALIVE,on,0);
}
//===================================================================
public int getReceiveBufferSize() throws SocketException
//===================================================================
{
	return getSocketParameter(RX_BUFFERSIZE);
}
//===================================================================
public int getSendBufferSize() throws SocketException
//===================================================================
{
	return getSocketParameter(TX_BUFFERSIZE);
}
//===================================================================
public void setReceiveBufferSize(int value) throws SocketException
//===================================================================
{
	setSocketParameter(RX_BUFFERSIZE,true,value);
}
//===================================================================
public void setSendBufferSize(int value) throws SocketException
//===================================================================
{
	setSocketParameter(TX_BUFFERSIZE,true,value);
}
//##################################################################
}
//##################################################################

