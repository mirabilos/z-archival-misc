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
import ewe.sys.Handle;
import ewe.sys.TaskObject;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.sys.mThread;

//##################################################################
public final class StreamUtils{
//##################################################################

//-------------------------------------------------------------------
private StreamUtils(){}
//-------------------------------------------------------------------

/**
* Reads in exactly a certain number of bytes. An EOF error is thrown if the Stream
* ends before the bytes are read.
* @param in The InputStream
* @param dest The destination for the input data bytes.
* @param start The offset in the dest to read from.
* @param count The number of bytes to read.
* @exception IOException If an I/O error occurs.
* @exception EOFException If the stream ends before all the bytes are read.
*/
//===================================================================
public static void readFully(InputStream in,byte[] dest,int start,int count) throws IOException, EOFException
//===================================================================
{
	int did = 0;
	while(count > 0){
		int ret = in.read(dest,start,count);
		if (ret < 0) throw new EOFException();
		start += ret;
		count -= ret;
		mThread.yield(250);
	}
}
/**
* Reads in all the bytes of an array. An EOF error is thrown if the Stream
* ends before the bytes are read.
* @param in The InputStream
* @param dest The destination for the input data bytes.
 * @return
 * @exception IOException
 * @exception EOFException
 */
//===================================================================
public static void readFully(InputStream in,byte[] dest) throws IOException, EOFException
//===================================================================
{
	readFully(in,dest,0,dest.length);
}

/**
* An option for transfer().
**/
public static final int CLOSE_INPUT = 0x10000000;
/**
* An option for transfer().
**/
public static final int CLOSE_OUTPUT = 0x20000000;
/**
* An option for transfer().
**/
public static final int DONT_STOP_AFTER_KNOWN_SIZE = 0x40000000;
/**
* An option for transfer().
**/
public static final int DONT_CLOSE_IF_ABORTED = 0x80000000;

/**
* Transfer data from an InputStream to an OutputStream. This method runs in the current
* thread and does not return until it is complete. However if the stop() method of the optional
* Handle parameter is called, the transfer will be aborted.
* @param h an optional handle that can be used by another Thread to monitor and cancel the transfer.
* @param in the InputStream.
* @param out the OutputStream.
* @param knownSize If the number of bytes is known, set this to be that value. Otherwise set it to -1.
* @param optionsAndBufferSize any of CLOSE_INPUT|CLOSE_OUTPUT|DONT_STOP_AFTER_KNOWN_SIZE OR'ed with
* an optional buffer size specified in Kilobytes. e.g. CLOSE_INPUT|CLOSE_OUTPUT|10
* @return the actual number of bytes transferred, or -1 if the handle was stopped.
* @exception IOException if an IOException occurs.
*/
//===================================================================
public static int transfer(Handle h,InputStream in,OutputStream out,int knownSize,int optionsAndBufferSize)
throws IOException
//===================================================================
{
	int maxRead = 8*1024;
	int options = optionsAndBufferSize & 0xf0000000;
	int curRead = (optionsAndBufferSize & 0x0fffffff)*1024;
	if (curRead != 0) maxRead = curRead;
	else curRead = 1024;

	byte[] read = new byte[curRead];
	if (h == null) h = new Handle();
	h.setProgress(knownSize < 0 ? -1 : 0);
	int left = (((options & DONT_STOP_AFTER_KNOWN_SIZE) != 0) || knownSize < 0) ? -1 : knownSize;
	int didTransfer = 0;
	try{
		while(left != 0){
			if (h.shouldStop) return didTransfer = -1;
			int readNow = left < 0 ? curRead : left;
			if (readNow > curRead) readNow = curRead;
			if (read.length < readNow) read = new byte[readNow];
			int got = in.read(read,0,readNow);
			if (got == -1) break;
			if (h.shouldStop) return didTransfer = -1;
			out.write(read,0,got);
			didTransfer += got;
			h.setProgress(knownSize < 0 ? -1 : (float)didTransfer/knownSize);
			if (left > 0) {
				left -= got;
				if (left <= 0) break;
			}
			if (got == curRead) curRead *= 2;
			if (curRead > maxRead) curRead = maxRead;
			mThread.yield(250);
		}
		out.flush();
	}finally{
		if (((options & DONT_CLOSE_IF_ABORTED) == 0) || (didTransfer != -1)){
			if ((options & CLOSE_INPUT) != 0) try{in.close();}catch(IOException e){};
			if ((options & CLOSE_OUTPUT) != 0) try{out.close();}catch(IOException e){};
		}
	}
	return didTransfer;
}
/**
* Transfer data from an InputStream to an OutputStream. This method runs in the current
* thread and does not return until it is complete. However if the stop() method of the optional
* Handle parameter is called, the transfer will be aborted.
* @param h an optional handle that can be used by another Thread to monitor and cancel the transfer.
* @param in the InputStream.
* @param out the OutputStream.
* @return the actual number of bytes transferred, or -1 if the handle was stopped.
* @exception IOException if an IOException occurs.
*/
//===================================================================
public static int transfer(Handle h,InputStream in,OutputStream out) throws IOException
//===================================================================
{
	return transfer(h,in,out,-1,0);
}


//-------------------------------------------------------------------
private static IOHandle tx(final InputStream in,final Object outObj,final int knownSize,final int optionsAndBufferSize)
//-------------------------------------------------------------------
{
	return (IOHandle) new TaskObject(new IOHandle()){
		protected void doRun(){
			try{
				Object out = outObj;
				ByteArray dest = (out instanceof ByteArray) ? (ByteArray)out : null;
				if (dest != null)
					out = new ByteArrayOutputStream();
				handle.resetTime("Transferring data.");
				int did = transfer(handle,in,(OutputStream)out,knownSize,optionsAndBufferSize);
				if (did < 0) handle.set(Handle.Aborted);
				else {
					if (dest != null) ((ByteArrayOutputStream)out).toByteArray(dest);
					handle.returnValue = dest;
					((IOHandle)handle).bytesTransferred = did;
					handle.set(Handle.Succeeded);
				}
			}catch(Exception e){
				handle.fail(e);
			}
		}
	}.startTask();
}
/**
* Transfer data from an InputStream to an OutputStream in a separate thread.
* @param in the InputStream.
* @param out the OutputStream.
* @param knownSize If the number of bytes is known, set this to be that value. Otherwise set it to -1.
* @param optionsAndBufferSize any of CLOSE_INPUT|CLOSE_OUTPUT|DONT_STOP_AFTER_KNOWN_SIZE OR'ed with
* an optional buffer size specified in Kilobytes. e.g. CLOSE_INPUT|CLOSE_OUTPUT|10
* @return an IOHandle that can be used to monitor and possibly abort the transfer.
* When the IOHandle indicates success, the number of bytes transferred will be given by
* the bytesTransferred value of the IOHandle.
*/
//===================================================================
public static IOHandle transfer(final InputStream in,final OutputStream out,final int knownSize,final int optionsAndBufferSize)
//===================================================================
{
	return tx(in,out,knownSize,optionsAndBufferSize);
}
/**
* Transfer data from an InputStream to an OutputStream in a separate thread.
* @param in the InputStream.
* @param out the OutputStream.
* @return an IOHandle that can be used to monitor and possibly abort the transfer.
* When the IOHandle indicates success, the number of bytes transferred will be given by
* the bytesTransferred value of the IOHandle.
*/
//===================================================================
public static IOHandle transfer(final InputStream in,final OutputStream out)
//===================================================================
{
	return tx(in,out,-1,0);
}
/**
 * Read all the bytes from an InputStream, placing them in a ByteArray.
 * @param h an optional handle that can be used by another Thread to monitor and cancel the transfer.
 * @param in The input stream.
 * @param dest An optional destination ByteArray.
 * @param knownSize If the size is known then set this to be that size. If it is not known then set it to -1.
 * @param optionsAndBufferSize any of CLOSE_INPUT|CLOSE_OUTPUT|DONT_STOP_AFTER_KNOWN_SIZE OR'ed with
 * an optional buffer size specified in Kilobytes. e.g. CLOSE_INPUT|CLOSE_OUTPUT|10
 * @return the dest ByteArray or a new ByteArray containing the data read in. If the transfer was
 * aborted because stop() was called on the h Handle, then the method will return null.
 * @exception IOException if an IOException occurs.
 */
//===================================================================
public static ByteArray readAllBytes(Handle h,InputStream in,ByteArray dest,int knownSize,int optionsAndBufferSize)
throws IOException
//===================================================================
{
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	int copied = transfer(h,in,bos,knownSize,optionsAndBufferSize);
	if (copied == -1) return null;
	return bos.toByteArray(dest);
}
/**
 * Read all the bytes from an InputStream, placing them in a ByteArray.
 * @param h an optional handle that can be used by another Thread to monitor and cancel the transfer.
 * @param in The input stream.
 * @param dest An optional destination ByteArray.
 * @return the dest ByteArray or a new ByteArray containing the data read in. If the transfer was
 * aborted because stop() was called on the h Handle, then the method will return null.
 * @exception IOException if an IOException occurs.
 */
//===================================================================
public static ByteArray readAllBytes(Handle h,InputStream in,ByteArray dest)
throws IOException
//===================================================================
{
	return readAllBytes(h,in,dest,-1,0);
}
/**
 * Read all the bytes in a separate thread. When finished the read bytes are placed in
 * the destination ByteArray (or a new one if dest is null) and then the returnValue
 * member of the returned IOHandle will be that ByteArray.
 * @param in The input stream.
 * @param dest An optional destination ByteArray.
 * @param knownSize If the size is known then set this to be that size. If it is not known then set it to -1.
 * @param optionsAndBufferSize any of CLOSE_INPUT|CLOSE_OUTPUT|DONT_STOP_AFTER_KNOWN_SIZE OR'ed with
 * an optional buffer size specified in Kilobytes. e.g. CLOSE_INPUT|CLOSE_OUTPUT|10
 * @return an IOHandle that can be used to monitor and stop the process. When it reports success
 * then its returnValue will hold the destination ByteArray containing the data.
 */
//===================================================================
public static IOHandle readAllBytes(InputStream in,ByteArray dest,int knownSize,int optionsAndBufferSize)
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	return tx(in,dest,knownSize,optionsAndBufferSize);
}
/**
 * Read all the bytes in a separate thread. When finished the read bytes are placed in
 * the destination ByteArray (or a new one if dest is null) and then the returnValue
 * member of the returned IOHandle will be that ByteArray.
 * @param in The input stream.
 * @param dest An optional destination ByteArray.
  * @return an IOHandle that can be used to monitor and stop the process. When it reports success
 * then its returnValue will hold the destination ByteArray containing the data.
 */
//===================================================================
public static IOHandle readAllBytes(InputStream in,ByteArray dest)
//===================================================================
{
	return readAllBytes(in,dest,-1,0);
}
/**
* Transfer data from a Reader to a Writer. This method runs in the current
* thread and does not return until it is complete. However if the stop() method of the optional
* Handle parameter is called, the transfer will be aborted.
* @param h an optional handle that can be used by another Thread to monitor and cancel the transfer.
* @param in the Reader.
* @param out the Writer.
* @param knownSize If the number of bytes is known, set this to be that value. Otherwise set it to -1.
* @param optionsAndBufferSize any of CLOSE_INPUT|CLOSE_OUTPUT|DONT_STOP_AFTER_KNOWN_SIZE OR'ed with
* an optional buffer size specified in Kilobytes. e.g. CLOSE_INPUT|CLOSE_OUTPUT|10
* @return the actual number of bytes transferred, or -1 if the handle was stopped.
* @exception IOException if an IOException occurs.
*/
//===================================================================
public static int transfer(Handle h,Reader in,Writer out,int knownSize,int optionsAndBufferSize)
throws IOException
//===================================================================
{
	int maxRead = 8*1024;
	int options = optionsAndBufferSize & 0xf0000000;
	int curRead = (optionsAndBufferSize & 0x0fffffff)*1024;
	if (curRead != 0) maxRead = curRead;
	else curRead = 1024;

	char[] read = new char[curRead];
	if (h == null) h = new Handle();
	h.setProgress(knownSize < 0 ? -1 : 0);
	int left = (((options & DONT_STOP_AFTER_KNOWN_SIZE) != 0) || knownSize < 0) ? -1 : knownSize;
	int didTransfer = 0;
	try{
		while(left != 0){
			if (h.shouldStop) return didTransfer = -1;
			int readNow = left < 0 ? curRead : left;
			if (readNow > curRead) readNow = curRead;
			if (read.length < readNow) read = new char[readNow];
			int got = in.read(read,0,readNow);
			if (got == -1) break;
			if (h.shouldStop) return didTransfer = -1;
			out.write(read,0,got);
			didTransfer += got;
			h.setProgress(knownSize < 0 ? -1 : (float)didTransfer/knownSize);
			if (left > 0) {
				left -= got;
				if (left <= 0) break;
			}
			if (got == curRead) curRead *= 2;
			if (curRead > maxRead) curRead = maxRead;
			mThread.yield(250);
		}
		out.flush();
	}finally{
		if (((options & DONT_CLOSE_IF_ABORTED) == 0) || (didTransfer != -1)){
			if ((options & CLOSE_INPUT) != 0) try{in.close();}catch(IOException e){};
			if ((options & CLOSE_OUTPUT) != 0) try{out.close();}catch(IOException e){};
		}
	}
	return didTransfer;
}
//-------------------------------------------------------------------
private static IOHandle tx(final Reader in,final Object outObj,final int knownSize,final int optionsAndBufferSize)
//-------------------------------------------------------------------
{
	return (IOHandle) new TaskObject(new IOHandle()){
		protected void doRun(){
			try{
				Object out = outObj;
				CharArray dest = (out instanceof CharArray) ? (CharArray)out : null;
				if (dest != null)
					out = new CharArrayWriter();
				handle.resetTime("Transferring data.");
				int did = transfer(handle,in,(Writer)out,knownSize,optionsAndBufferSize);
				if (did < 0) handle.set(Handle.Aborted);
				else {
					if (dest != null) ((CharArrayWriter)out).toCharArray(dest);
					handle.returnValue = dest;
					((IOHandle)handle).bytesTransferred = did;
					handle.set(Handle.Succeeded);
				}
			}catch(Exception e){
				handle.fail(e);
			}
		}
	}.startTask();
}
/**
* Transfer data from a Reader to a Writer in a separate thread.
* @param in the Reader.
* @param out the Writer.
* @param knownSize If the number of bytes is known, set this to be that value. Otherwise set it to -1.
* @param optionsAndBufferSize any of CLOSE_INPUT|CLOSE_OUTPUT|DONT_STOP_AFTER_KNOWN_SIZE OR'ed with
* an optional buffer size specified in Kilobytes. e.g. CLOSE_INPUT|CLOSE_OUTPUT|10
* @return an IOHandle that can be used to monitor and possibly abort the transfer.
* When the IOHandle indicates success, the number of chars transferred will be given by
* the bytesTransferred value of the IOHandle.
*/
//===================================================================
public static IOHandle transfer(final Reader in,final Writer out,final int knownSize,final int optionsAndBufferSize)
//===================================================================
{
	return tx(in,out,knownSize,optionsAndBufferSize);
}
/**
* Transfer data from a Reader to a Writer in a separate thread.
* @param in the Reader.
* @param out the Writer.
* @return an IOHandle that can be used to monitor and possibly abort the transfer.
* When the IOHandle indicates success, the number of chars transferred will be given by
* the bytesTransferred value of the IOHandle.
*/
//===================================================================
public static IOHandle transfer(final Reader in,final Writer out)
//===================================================================
{
	return tx(in,out,-1,0);
}

//##################################################################
}
//##################################################################

