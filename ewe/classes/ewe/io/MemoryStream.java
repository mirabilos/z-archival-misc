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
import ewe.util.ByteArray;
import ewe.sys.Lock;
import ewe.sys.mThread;
/**
* A MemoryStream is a Stream that stores all data written to it using standard Stream write() calls,
* in an array of bytes and then allows that data to be read out again, usually
* through standard Stream read() calls. Essentially a memory pipe.<p>
* You can also use it to provide a standard Streaming interface to a non-Streaming
* data source/sink, by setting it to be read-only/write-only and overriding the
* methods that provide or use the data.
**/
//##################################################################
public class MemoryStream extends StreamObject implements Runnable{
//##################################################################
private int maxCapacity;
private ByteArray data = new ByteArray();
/**
* Set this true to allow for read() calls only. You will then have to place data
* into the Stream directly using putInBuffer().
**/
protected boolean readOnly = false;
/**
* Set this true to allow for write() calls only. You will then have to read data
* from the Stream directly using getFromBuffer().
**/
protected boolean writeOnly = false;
/**
* Set this true to allow the getMoreData() method to start and control a background thread
* for retrieving data for reading. If you set this true then you must override the loadAndPutDataBlock()
* method.
**/
protected boolean useBackgroundThread = false;

//===================================================================
public boolean flushStream() throws IOException {return true;}
//===================================================================
/**
* Cause an error to be generated on the Stream.
**/
//===================================================================
public int generateError(String message)
//===================================================================
{
	error = message == null ? "IO Error":message;
	return -2;
}
//===================================================================
public MemoryStream() {this(0);}
//===================================================================
//===================================================================
public MemoryStream(int maxCapacity)
//===================================================================
{
	this.maxCapacity = maxCapacity;
	data.data = new byte[1024];
}
/**
* Create a read-only MemoryStream that may or may not use a background thread
* for retrieving data for reading.
* @param useBackgroundThread if this is true then a background thread will be used
* to load data for reading. In that case the loadAndPutDataBlock() method must be overridden.
*/
//===================================================================
public MemoryStream(boolean useBackgroundThread)
//===================================================================
{
	this(useBackgroundThread,true);
}
/**
* Create a uni-directional MemoryStream that may or may not use a background thread
* for retrieving data for reading/writing.
* @param useBackgroundThread if this is true then a background thread will be used
* to load data for reading or to accept data that's been written.
* In that case the loadAndPutDataBlock() method OR takeAndUseDataBlock() method must be overridden.
* @param forReading true for read-only stream, false for a write-only stream.
*/
//===================================================================
public MemoryStream(boolean useBackgroundThread, boolean forReading)
//===================================================================
{
	this.readOnly = forReading;
	this.writeOnly = !forReading;
	this.useBackgroundThread = useBackgroundThread;
	this.maxCapacity = 0;
}
protected ewe.sys.Lock backgroundLock;
/**
 * Override this method if you are using a background thread to get data from a source to
 * place in the stream's buffer for reading. This method should block until at least one byte
 * of data has been read and placed in the buffer using putInBuffer(). If there is no more
 * data to read the method should return false. If an IOException occurs it should throw it.
 * @return true if data was loaded and placed in the buffer, false if there is no more data.
 * @exception IOException if an error occured retrieving the data.
 */
//-------------------------------------------------------------------
protected boolean loadAndPutDataBlock() throws IOException
//-------------------------------------------------------------------
{
	throw new IOException("No way to get more data!");
}
/**
 Override this method if you are using a background thread to use data written into the buffer

 * place in the stream's buffer for reading. This method should block until at least one byte
 * of data has been read and placed in the buffer using putInBuffer(). If there is no more
 * data to read the method should return false. If an IOException occurs it should throw it.
 * @return true if data was loaded and placed in the buffer, false if there is no more data.
 * @exception IOException if an error occured retrieving the data.
 */
//-------------------------------------------------------------------
protected boolean takeAndUseDataBlock() throws IOException
//-------------------------------------------------------------------
{
	throw new IOException("No way to use written data!");
}
//===================================================================
public void run()
//===================================================================
{
	if (readOnly) while(!closed){
		if (data.length == 0){
			try{
				if (!loadAndPutDataBlock()){
					closed = true;
					return;
				}
			}catch(IOException e){
				generateError(e.getMessage());
				return;
			}
		}
		backgroundLock.synchronize(); try{
			try{
				if (data.length != 0)
					backgroundLock.waitOn();
			}catch(Exception e){}
		}finally{backgroundLock.release();}
	}else while(true){ //For writing.
		if (data.length != 0){
			try{
				if (!takeAndUseDataBlock()){
					closed = true;
					return;
				}
			}catch(IOException e){
				generateError(e.getMessage());
				return;
			}
		}else if (closed){
			noMoreDataToTake();
			return;
		}
		backgroundLock.synchronize(); try{
			try{
				if (data.length == 0)
					backgroundLock.waitOn();
			}catch(Exception e){}
		}finally{backgroundLock.release();}
	}
}
/**
* Override this method if you are implementing a read-only Stream, but are not using
* a background thread to do so. It is called if a read() has
* been called but there is no data in the buffer left to read. <b>This method should
* be non-blocking</b>. If necessary start a new thread to fetch more data and put it
* in the buffer using putInBuffer(). If that thread determines there is no more data
* it should call close(). The default implmentation of this will generate an error if
* this Stream is marked as readOnly.
**/
//-------------------------------------------------------------------
protected void getMoreData()
//-------------------------------------------------------------------
{
	if (!useBackgroundThread) {
		generateError("No way to get more data!");
		return;
	}
	//
	if (backgroundLock == null){
		backgroundLock = new Lock();
		new mThread(this).start();
	}
	//
	if (backgroundLock.grab()) try {
		backgroundLock.notifyAllWaiting();
	}finally{backgroundLock.release();}
}
/**
Override this method if you are implementing a wite-only Stream, but are not using
a background thread to do so. It is called when a write() operation has been called
placing data in the buffer. You should use the sizeOfBuffer() method and then the getFromBuffer()
method to take the data out of the buffer.<p>
<b>This method should be non-blocking</b>. If necessary start a new thread to
take the data out of the buffer so that this thread can return.
The default implmentation of this will generate an error ifthis Stream is marked as writeOnly.
**/
//-------------------------------------------------------------------
protected void useMoreData()
//-------------------------------------------------------------------
{
	if (!useBackgroundThread) {
		generateError("No way to use written data!");
		return;
	}
	//
	if (backgroundLock == null){
		backgroundLock = new Lock();
		new mThread(this).start();
	}
	//
	if (backgroundLock.grab()) try {
		backgroundLock.notifyAllWaiting();
	}finally{backgroundLock.release();}
}


/**
 * If this is a write-only Stream, this method is called when the Stream has been closed
 * and there will be no more data to take. It effectively notifies an end of incoming data.
 */
//-------------------------------------------------------------------
protected void noMoreDataToTake()
//-------------------------------------------------------------------
{
}
//-------------------------------------------------------------------
protected int sizeOfBuffer()
//-------------------------------------------------------------------
{
	return data.length;
}
//-------------------------------------------------------------------
private int getData(byte[] buff,int start,int count)
//-------------------------------------------------------------------
{
	int toGo = data.length;
	if (toGo <= 0){
		if (closed) return -1;
		else {
			if (readOnly) getMoreData();
			return 0;
		}
	}
	if (count == 0) return 0;
	if (count < 0) throw new IllegalArgumentException();
	if (toGo > count) toGo = count;
	System.arraycopy(data.data,0,buff,start,toGo);
	if (toGo < data.length);
		System.arraycopy(data.data,toGo,data.data,0,data.length-toGo);
	data.length -= toGo;
	return toGo;
}
/**
 * Get a all the bytes from the internal data buffer. The data in the buffer would
 * have been placed usually with a write() operation.
 * @return an array of bytes from the buffer. If there were no bytes in the buffer the returned
 * array will be empty.
 * @exception IOException if the Stream has been flagged with an error.
 */
//-------------------------------------------------------------------
protected byte[] getFromBuffer() throws IOException
//-------------------------------------------------------------------
{
	if (error != null) throwIOException(error);
	byte[] ret = new byte[sizeOfBuffer()];
	if (ret.length != 0) getData(ret,0,ret.length);
	return ret;
}
/**
 * Get a number of bytes from the internal data buffer. The data in the buffer would
 * have been placed usually with a write() operation. Use sizeOfBuffer() to determine how
 * many bytes are waiting in the buffer.
 * @return the number of bytes read from the buffer.
 * @exception IOException if the Stream has been flagged with an error.
 */
//-------------------------------------------------------------------
protected int getFromBuffer(byte[] buff,int start,int count) throws IOException
//-------------------------------------------------------------------
{
	if (error != null) throwIOException(error);
	return getData(buff,start,count);
}
//===================================================================
public int nonBlockingRead(byte []buff,int start,int count)
//===================================================================
{
	if (writeOnly) return generateError("Reads not allowed.");
	if (error != null) return -2;
	int ret = getData(buff,start,count);
	return ret;
}
/**
* Use this to put data into the buffer, for later reading out.
**/
//-------------------------------------------------------------------
protected void putInBuffer(byte []buff,int start,int count) throws IOException
//-------------------------------------------------------------------
{
	if (error != null) throwIOException(error);
	if (closed) throw new IOException("Stream closed.");
	if (count <= 0) throw new IllegalArgumentException();
	data.append(buff,start,count);
	if (writeOnly) useMoreData();
}
//===================================================================
public int nonBlockingWrite(byte []buff,int start,int count)
//===================================================================
{
	if (readOnly) return generateError("Writes not allowed.");
	if (error != null) return -2;
	if (closed) return -1;
	if (count == 0) return 0;
	if (count < 0) throw new IllegalArgumentException();
	int toSave = count;
	if (maxCapacity > 0){
		int allowed = maxCapacity-data.length;
		if (allowed < toSave) toSave = allowed;
		if (toSave <= 0) return 0;
	}
	data.append(buff,start,toSave);
	if (writeOnly) useMoreData();
	return toSave;
}
//===================================================================
public boolean closeStream() throws IOException
//===================================================================
{
	if (writeOnly && backgroundLock == null) noMoreDataToTake();
	return super.closeStream();
}
/**
 * Create a MemoryStream and return an InputStream and an OutputStream for it. Any data
 * written to the OutputStream will be read by the InputStream.
 * @return an array of two object - the one at index 0 will be an InputStream and the one
 * at index 1 will be the OutputStream.
 */
//===================================================================
public static Object[] pipe()
//===================================================================
{
	MemoryStream ms = new MemoryStream();
	Object[] ret = new Object[2];
	ret[0] = ms.toInputStream();
	ret[1] = ms.toOutputStream();
	return ret;
}
/**
* Create two Streams that represent either end of a memory based communication stream.
* Data written to one Stream can be read by the other.
**/
//===================================================================
public static Stream[] pipe2()
//===================================================================
{
	Stream[] ret = new Stream[2];
	Object[] p1 = pipe(), p2 = pipe();
	ret[0] = new StreamAdapter((InputStream)p1[0],(OutputStream)p2[1]);
	ret[1] = new StreamAdapter((InputStream)p2[0],(OutputStream)p1[1]);
	return ret;
}
//##################################################################
}
//##################################################################

