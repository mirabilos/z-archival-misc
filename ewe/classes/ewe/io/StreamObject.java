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
import ewe.sys.Coroutine;
import ewe.sys.Handle;
/**
* A StreamObject is the base object for implementing a Stream. Since
* Streams provide an asynchronous operation you must provide non-blocking
* I/O operations for the stream.<p>
* To extend this you must:
* <p>
* For Reading - EITHER override the single byte readAByte() OR extend the nonBlockingRead();<br>
* For Writing - EITHER override the single byte writeAByte() OR extend the nonBlockingWrite();<br>
* For Flushing - override the flushStream() method.<br>
* For Closing - override the closeStream() method.<br>
* <p>
* All these methods MUST be non-blocking. They must not call a mThread wait() or sleep()
* method and they should block the calling thread for as little time as possible.
* This makes these operations safe to call from native methods.
**/
//##################################################################
public abstract class StreamObject extends BasicStreamObject{
//##################################################################
//
// Do not add instance variables to this class.
//
public static int napIterations = 20;
public static int napTime = 0;

protected void waitUntilReady(boolean forReading,int maxMillis)
{
	Coroutine c = Coroutine.getCurrent();
	if (!(this instanceof StreamCanPause) || c == null) return;
	else{
		int toSleep = ((StreamCanPause)this).pauseUntilReady(forReading ? StreamCanPause.PAUSE_UNTIL_CAN_READ : StreamCanPause.PAUSE_UNTIL_CAN_WRITE,maxMillis);
		if (toSleep != 0) c.sleep(toSleep);
	}
}
protected void waitUntilReady(boolean forReading)
{
	waitUntilReady(forReading,0x7fffffff);
}
//-------------------------------------------------------------------
protected void nap(boolean forReading)
//-------------------------------------------------------------------
{
	Coroutine c = Coroutine.getCurrent();
	if (!(this instanceof StreamCanPause) || c == null) nap();
	else{
		int toSleep = ((StreamCanPause)this).pauseUntilReady(forReading ? StreamCanPause.PAUSE_UNTIL_CAN_READ : StreamCanPause.PAUSE_UNTIL_CAN_WRITE,1000);
		if (toSleep == 0) nap();
		else c.sleep(toSleep);
	}
}
//-------------------------------------------------------------------
protected void nap()
//-------------------------------------------------------------------
{
	Coroutine.nap(napIterations,napTime);
}

protected static final int READWRITE_CLOSED = -1;
protected static final int READWRITE_ERROR = -2;
protected static final int READWRITE_WOULDBLOCK = -3;

/**
* This reads one byte of data. Overriding this method is optional if you override the
* nonBlockingRead() method. This is the least efficient way of implementing a readable
* stream but in cases where the Stream is very slow, it may be OK.
* <p>
* The default implementation simply returns -2 (i.e. read operations are not allowed).
* <p>
* Return values:
* <p><nl>
* <li>
* If a byte has been read successfully, the byte value should appear
* in the last 8-bits of the return value.
* <li>
* If the stream is closed or end of stream has been reached, it should return -1 (READWRITE_CLOSED)
* <li>
* If there is an IO error it should return -2 (READWRITE_ERROR)
* <li>
* If there are no available bytes to read yet, but the stream is still open and OK, it should return -3 (READWRITE_WOULDBLOCK)
* </nl>
* These return values never get to the end user - they are only used by the StreamObject implementation of
* nonBlockingRead() - which you can override yourself and therefore ignore this method.
**/
//-------------------------------------------------------------------
protected int readAByte() {return returnError("Reading not allowed",READWRITE_ERROR);}
//-------------------------------------------------------------------
/**
* This writes one byte of data. Overriding this method is optional if you override the
* nonBlockingWrite() method. This is the least efficient way of implementing a writeable
* stream but in cases where the Stream is very slow, it may be OK.
* <p>
* The default implementation simply returns -2 (i.e. write operations are not allowed).
* <p>
* Return values:
* <p><nl>
* <li>
* If a byte has been written successfully, it should return the byte written.
* <li>
* If the stream is closed for writing it should return -1 (READWRITE_CLOSED)
* <li>
* If there is an IO error it should return -2 (READWRITE_ERROR)
* <li>
* If the byte could not be written yet, but the stream is still open and OK, it should return -3 (READWRITE_WOULDBLOCK)
* </nl>
* These return values never get to the end user - they are only used by the StreamObject implementation of
* nonBlockingWrite() - which you can override yourself and therefore ignore this method.
**/
//-------------------------------------------------------------------
protected int writeAByte(byte val) {return returnError("Writing not allowed",READWRITE_ERROR);}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
long getIOLocation()
//-------------------------------------------------------------------
{
	if (this instanceof RandomAccessStream){
		try{
			while(true){
				long got = ((RandomAccessStream)this).tellPosition();
				if (got != -1) return got;
				nap();
			}
		}catch(IOException e){
			error = e.getMessage();
			return -1;
		}
	}else
		return 0;
}
/**
* This returns:
* 0 - Stream end reached.
* -1 - Error.
* > 0 - Number of bytes read.
**/
//-------------------------------------------------------------------
protected int doRead(byte []buf,int start,int count,IOHandle handle,boolean readCompletely)
//-------------------------------------------------------------------
{
	int did = 0;
	int errorCode = 0;
	long ioLocation = handle == null ? getIOLocation() : handle.ioLocation;
	if (ioLocation == -1) return -1;
	RandomAccessStream ras = this instanceof RandomAccessStream ? (RandomAccessStream)this : null;
	if (handle != null)
		handle.bytesTransferred = handle.errorCode = 0;
	while(count > 0){
		int asked = count;
		int ret = 0;
		if (ras == null) {
			//if (this instanceof ewe.net.Socket) ewe.ui.Console.debug("Read socket...");
			ret = nonBlockingRead(buf,start,count);
			//if (this instanceof ewe.net.Socket) ewe.ui.Console.debug("Read socket: "+ret);
		}else try{
			//ewe.sys.Vm.debug("Going to read "+ras.hashCode()+" :"+ioLocation+", "+ret+", "+getIOLocation());
			ret = ras.nonBlockingRead(ioLocation,buf,start,count);
			//ewe.sys.Vm.debug(ioLocation+", "+ret+", "+getIOLocation());
		}catch(IOException e){
			error = e.getMessage();
			ret = -2;
		}
		nap();
		if (ret < 0){
			if (ret == -1)
				if (readCompletely || did == 0) errorCode = handle.STREAM_END_REACHED;
				else errorCode = 0;
			else{
				//ewe.sys.Vm.debug("Error while reading...");
				errorCode = handle.IO_ERROR;
			}
			break;
		}else{
			did += ret;
			if (ras != null) ioLocation += ret;
			//ewe.sys.Vm.debug("ioLocation is now: "+ioLocation);
			if (handle != null)handle.bytesTransferred += ret;
			start += ret;
			count -= ret;
			if (!readCompletely && did != 0) break;
		}
		if (count <= 0) break;
		if (ret < asked) nap(true);
		if (handle != null){
			if (handle.pleaseAbort){
				errorCode = handle.IO_ABORTED;
				break;
			}
		}
	}
	if (handle != null){
		handle.bytesTransferred = did;
		handle.errorCode = errorCode;
	}
	if (errorCode == handle.STREAM_END_REACHED) return 0;
	else if (errorCode != 0) return -1;
	else return did;
}
/**
* This returns:
* 0 - Stream closed.
* -1 - Error.
* > 0 - Number of bytes read.
**/
//-------------------------------------------------------------------
protected int doWrite(byte []buf,int start,int count,IOHandle handle)
//-------------------------------------------------------------------
{
	int did = 0;
	int errorCode = 0;
	long ioLocation = handle == null ? getIOLocation() : handle.ioLocation;
	if (ioLocation == -1) return -1;
	RandomAccessStream ras = this instanceof RandomAccessStream ? (RandomAccessStream)this : null;
	if (handle != null)
		handle.bytesTransferred = handle.errorCode = 0;
	while(count > 0){
		int asked = count;
		int ret = 0;
		if (ras == null) ret = nonBlockingWrite(buf,start,count);
		else try{
			ret = ras.nonBlockingWrite(ioLocation,buf,start,count);
		}catch(IOException e){
			error = e.getMessage();
			ret = -2;
		}
		nap();
		//ewe.sys.Vm.debug("Wrote: "+ret+" of: "+count,0);
		if (ret < 0){
			if (ret == -1) errorCode = handle.STREAM_END_REACHED;
			else errorCode = handle.IO_ERROR;
			break;
		}else{
			did += ret;
			if (ras != null) ioLocation += ret;
			if (handle != null)handle.bytesTransferred += ret;
			start += ret;
			count -= ret;
		}
		if (count <= 0) break;
		if (ret < asked) nap(false);
		if (handle != null){
			if (handle.pleaseAbort){
				errorCode = handle.IO_ABORTED;
				break;
			}
		}
	}
	if (handle != null){
		handle.bytesTransferred = did;
		handle.errorCode = errorCode;
	}
	if (errorCode == handle.STREAM_END_REACHED) return 0;
	else if (errorCode != 0) return -1;
	else return did;
}
/**
* This returns:
* 0 - Stream end reached.
* -1 - Error.
* >0 - Number of bytes read.
**/
//===================================================================
public int readBytes(byte []buf,int start,int count)
//===================================================================
{
	return doRead(buf,start,count,null,false);
}
/**
* This returns:
* 0 - Stream closed.
* -1 - Error.
* > 0 - Number of bytes written.
**/
//===================================================================
public int writeBytes(byte []buf,int start,int count)
//===================================================================
{
	return doWrite(buf,start,count,null);
}

//-------------------------------------------------------------------
private IOHandle getNewHandle(IOHandle provided)
//-------------------------------------------------------------------
{
	if (provided == null) provided = new IOHandle();
	provided.set(provided.Running);
	if (provided.ioLocation == -1){
		provided.ioLocation = getIOLocation();
		if (provided.ioLocation == -1){
			provided.errorCode = provided.IO_ERROR;
			provided.error = error;
			provided.set(provided.Failed);
		}
	}
	return provided;
}
/**
* This reads bytes from the stream asynchronously. It returns an IOHandle which you
* can use to check the status of the operation. When the operation is complete you should check
* the errorCode and byteTransferred members of the IOHandle.
**/

//===================================================================
public IOHandle readBytes(byte []buf,int start,int count,IOHandle handle,boolean readCompletely)
//===================================================================
{
	handle = getNewHandle(handle);
	if ((handle.check() & Handle.Failure) == 0)
		new streamReadWriter(this,buf,start,count,handle,readCompletely,true).startTask();
	return handle;
}
/**
* This writes bytes to the stream asynchronously. It returns an IOHandle which you
* can use to check the status of the operation. When the operation is complete you should check
* the errorCode and byteTransferred members of the IOHandle.
**/
//===================================================================
public IOHandle writeBytes(byte []buf,int start,int count,IOHandle handle)
//===================================================================
{
	handle = getNewHandle(handle);
	if ((handle.check() & Handle.Failure) == 0)
		new streamReadWriter(this,buf,start,count,handle,false,false).startTask();
	return handle;
}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	if (this instanceof OverridesClose){
		if (!close()) throwIOException(null);
		return true;
	}
	closed = true;
	return true;
}
//===================================================================
public boolean isOpen() {return !closed;}
//===================================================================
/**
* This is the non-blocking read operation. It should never attempt to
* wait() or sleep() in a Coroutine. It should return as quickly as possible.
* This makes it safe to be called from within a native method.
* Note that this should NEVER be called with a count of zero.<p>
* The default StreamObject implementation of this uses the single byte read() method.<p>
* @param buff Destination byte array to hold incoming data.
* @param start Starting index in buff for incoming data.
* @param count Maximum number of bytes to read - should never be zero.
* @return
*  greater than 0 = Number of bytes read. <br>
*  0 = No bytes available to read at this time.<br>
* -1 = Stream end reached no further bytes to read.<br>
* -2 = IO Error.<br>
*<b><i>Note that these values are different to the Stream.readBytes method.</b></i>
**/
//===================================================================
public int nonBlockingRead(byte []buff,int start,int count)
//===================================================================
{
	for (int i = 0; i<count; i++){
		int got = readAByte();
		if (got == READWRITE_CLOSED)
			if (i == 0) return READWRITE_CLOSED;
			else return i;
		if (got == READWRITE_ERROR) return got;
		if (got == READWRITE_WOULDBLOCK) return i;
		buff[start+i] = (byte)(got & 0xff);
	}
	return count;
}
/**
* This is the non-blocking write operation. It should never attempt to
* wait() or sleep() in a Coroutine. It should return as quickly as possible.
* This makes it safe to be called from within a native method.<p>
* The default StreamObject implementation of this uses the single byte write() method.<p>
* @param buff Source byte array holding data to be written.
* @param start Starting index in buff for data to be written.
* @param count Number of bytes to write - should never be zero.
* @return
* greater than 0 = Number of bytes actually written.<br>
*  0 = No bytes could be written yet - but the stream is still open.<br>
* -1 = Stream has been closed - no further writes are possible.<br>
* -2 = IO error - something went wrong.<br>
*<b><i>Note that these values are different to the Stream.writeBytes method.</b></i>
**/
//===================================================================
public int nonBlockingWrite(byte []buff,int start,int count)
//===================================================================
{
	for (int i = 0; i<count; i++){
		int got = writeAByte(buff[start+i]);
		if (got == READWRITE_CLOSED)
			if (i == 0) return READWRITE_CLOSED;
			else return i;
		if (got == READWRITE_ERROR) return got;
		if (got == READWRITE_WOULDBLOCK) return i;
	}
	return count;
}
//===================================================================
public int read(byte buff[],int offset,int count) throws IOException
//===================================================================
{
	if (count <= 0) return 0;
	int readIn = readBytes(buff,offset,count);
	if (readIn == 0) return -1;

	if (readIn == -1) throw new IOException(error);
	return readIn;
}
//===================================================================
public void write(byte buff[],int offset,int count) throws IOException
//===================================================================
{
	if (count <= 0) return;
	int wrote = writeBytes(buff,offset,count);
	if (wrote == 0) throw new IOException("Stream closed.");
	else if (wrote != count) throw new IOException(error);
}
//===================================================================
public void flush() throws ewe.io.IOException
//===================================================================
{
	while(true){
		if (flushStream()) return;
		Coroutine c = Coroutine.getCurrent();
		if (!(this instanceof StreamCanPause) || c == null) nap();
		else{
			int toSleep = ((StreamCanPause)this).pauseUntilReady(StreamCanPause.PAUSE_UNTIL_FLUSHED,1000);
			if (toSleep == 0) nap();
			else c.sleep(toSleep);
		}
	}
}
//===================================================================
public boolean close()
//===================================================================
{
	while(true){
		try{
			if (closeStream()) return true;
			Coroutine c = Coroutine.getCurrent();
			if (!(this instanceof StreamCanPause) || c == null) nap();
			else{
				int toSleep = ((StreamCanPause)this).pauseUntilReady(StreamCanPause.PAUSE_UNTIL_CLOSED,1000);
				if (toSleep == 0) nap();
				else c.sleep(toSleep);
			}
		}catch(IOException e){
			error = e.getMessage();
			return false;
		}
	}
}


//##################################################################
}
//##################################################################
//##################################################################
class streamReadWriter extends ewe.sys.TaskObject{
//##################################################################

byte [] buf;
int start;
int count;
boolean readCompletely;
StreamObject stream;
boolean doRead;

//===================================================================
public streamReadWriter(StreamObject stream,byte [] buf,int start,int count,IOHandle handle,boolean readCompletely,boolean doRead)
//===================================================================
{
	super(handle);
	handle.ioLocation = stream.getIOLocation();
	handle.bytesTransferred = handle.errorCode = 0;
	handle.pleaseAbort = false;
	this.buf = buf;
	this.start = start;
	this.count = count;
	this.readCompletely = readCompletely;
	this.stream = stream;
	this.doRead = doRead;
}

//===================================================================
public void doRun()
//===================================================================
{
	if (doRead){
		stream.doRead(buf,start,count,(IOHandle)handle,readCompletely);
	}else{
		stream.doWrite(buf,start,count,(IOHandle)handle);
	}
	if (handle.errorCode == IOHandle.IO_ABORTED) handle.set(handle.Failed|handle.Aborted);
	else if (handle.errorCode != 0) handle.set(handle.Failed);
	else handle.set(handle.Succeeded);
}
//-------------------------------------------------------------------
protected void doStop(int reason)
//-------------------------------------------------------------------
{
	((IOHandle)handle).pleaseAbort = true;
}

//##################################################################
}
//##################################################################


