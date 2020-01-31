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
package ewe.io;

/**
* Stream is used for all streaming I/O operations. This includes File I/O, Socket I/O and
* Serial I/O. The Stream interface provides two sets on methods. One fully non-blocking and
* one semi-blocking (i.e. blocks the current thread).<p>
*
* The semi-blocking methods are listed below. They work in exactly the same way as java.io.InputStream
* and java.io.OutputStream
* <pre>
*
* public int read(byte buf[], int start, int count) throws IOException;
* public int read(byte buf[]) throws IOException;
* public int read() throws IOException;
* public int write(byte buf[], int start, int count) throws IOException;
* public int write(byte buf[]) throws IOException;
* public int write(int aByte) throws IOException;
*
* </pre>
* These methods will block the current mThread thread (but allow others to
* execute). If you call them from within a non-Coroutine thread then they will
* block the entire program!<p>
*
* The non-blocking methods are:
* <pre>
*
* public IOHandle readBytes(byte []buf,int start,int count,IOHandle handle,boolean readFully);
* public IOHandle writeBytes(byte []buf,int start,int count,IOHandle handle);
*
* </pre>
* These always return immediately and you can use the returned IOHandle to check on the progress
* of the operation.
* <p>
* The correct way to use a handle to wait for the operation to complete is as follows:
*
* <pre>
* // First attempt a read operation.
* IOHandle readOp = stream.readBytes(buf,0,1024,null,false);
* // At any time from this point I can check the handle.
* // If I want to wait for 5 seconds and if it is not successfull abort I can do this:
* ...
* if (readOp.waitOnFlags(readOp.Success,new TimeOut(5000))){
* 	//If the waitOnFlags returns true, then the Success bit was set and so the read has
* 	//succeeded.
* 	int bytesGot = readOp.bytesTransferred;
* 	processBytes(buff,bytesGot);
* }else{
* 	//If the waitOnFlags returns false, then either it timed out before Success was set OR
* 	//the Stopped bit was set (implying that the operation has stopped) and Success was not set.
* 	if ((readOp.check() & readOp.Stopped) != 0){
* 		// If the Stopped bit is set, then an error must have occured if Success was not set.
* 		int error = readOp.errorCode;
* 		if (error == readOp.STREAM_END_REACHED)
* 			doEndOfStreamProcessing();
* 		else
* 			doStreamErrorProcessing(error);
* 		stream.close();
* 	}else{
* 		// At this point, the Stopped bit was not set, implying that the read operation is still
* 		// in progress. I can choose to do something else and wait for it to complete later using
* 		// the same procedure - or I can abort the read operation, assuming that something has gone
* 		// wrong.
* 		readOp.stop(0);
* 		stream.close();
* 	}
* }
* </pre>
**/

public interface Stream extends BasicStream
{
/**
 * Reads bytes into a buffer. This method blocks the current Coroutine, but allows others
	 to continue executing.
 * @param buff[] The destination for the incoming bytes.
 * @param offset The offset index in the destination array to accept the data.
 * @param count The maximum number of bytes to read.
 * @return The actual number of bytes read, or -1 if the stream has ended.
 * @throws IOException if an error occurs reading the stream.
 */
//===================================================================
public int read(byte buff[],int offset,int count) throws IOException;
//===================================================================
/**
 * Read bytes into a buffer. Reads up to buff.length bytes starting at index 0.
	This method blocks the current Coroutine, but allows others to continue executing.
 * @param buff[] The destination for the incoming bytes.
 * @return The number of bytes actually read, or -1 if the stream has ended.
 * @exception IOException if an error occurs reading the stream.
 */
//===================================================================
public int read(byte buff[]) throws IOException;
//===================================================================
/**
 * Writes bytes from a buffer to the Stream.
	This method blocks the current Coroutine until <b>all</b> bytes are written,
	but allows others to continue executing.
 * @param buff[] The source of the outgoing bytes.
 * @param offset The start index in the array of the data bytes.
 * @param count The number of bytes to write.
 * @exception IOException if an error occurs writing to the stream.
 */
//===================================================================
public void write(byte buff[],int offset,int count) throws IOException;
//===================================================================
/**
 * Writes bytes from a buffer to the Stream.
	This method blocks the current Coroutine until <b>all</b> bytes in the array are written,
	but allows others to continue executing.
 * @param buff[] The source of the outgoing bytes.
 * @exception IOException if an error occurs writing to the stream.
 */
//===================================================================
public void write(byte buff[]) throws IOException;
//===================================================================
/**
* Reads bytes from the stream. This blocks until some bytes have been read. If this blocks
* within a Coroutine - it will allow other Coroutines to operate. NEVER call this with
* a count of zero.
* @param buff Destination byte array to hold incoming data.
* @param start Starting index in buff for incoming data.
* @param count Maximum number of bytes to read.
* @return
* 0 = Stream end reached.<br>
* -1 = IO Error.<br>
* >0 = Number of bytes read. <br>
*@deprecated use the read(byte [],int,int) method instead.
*/
//===================================================================
public int readBytes(byte buff[], int start, int count);
//===================================================================
/**
* Writes bytes to the the stream. This blocks until all bytes have been written. If this blocks
* within a Coroutine - it will allow other Coroutines to operate.
* @param buff Source byte array holding data to be written.
* @param start Starting index in buff for data to be written.
* @param count Number of bytes to write.
* @return
* 0 = Stream end reached - no more bytes can be written.<br>
* -1 = IO Error.<br>
* >0 = Number of bytes written (always equal to count). <br>
*@deprecated use the write(byte [],int,int) method instead.
*/
//===================================================================
public int writeBytes(byte buff[], int start, int count);
//===================================================================
/**
* This reads bytes from the stream asynchronously. It returns an IOHandle immediately which you
* can use to check the status of the operation. When the operation is complete you should check
* the errorCode and byteTransferred members of the IOHandle.
* @param buff Destination byte array to hold incoming data.
* @param start Starting index in buff for incoming data.
* @param count Maximum number of bytes to read.
* @param handle An existing IOHandle for the operation to use and return. If this is null then a
* new one will be created and returned.
* @param readFully Set this to be true if you require the full number of <b>count</b> bytes
* to be read.
* @return
* An IOHandle to be used for monitoring the progress of the operation.
**/
//===================================================================
public IOHandle readBytes(byte []buff,int start,int count,IOHandle handle,boolean readFully);
//===================================================================
/**
* This writes bytes to the stream asynchronously. It returns an IOHandle immediately which you
* can use to check the status of the operation. When the operation is complete you should check
* the errorCode and byteTransferred members of the IOHandle.
* @param buff Source byte array holding data to be written.
* @param start Starting index in buff for data to be written.
* @param count Number of bytes to write.
* @param handle An existing IOHandle for the operation to use and return. If this is null then a
* new one will be created and returned.
* @return
* An IOHandle to be used for monitoring the progress of the operation.
**/
//===================================================================
public IOHandle writeBytes(byte []buff,int start,int count,IOHandle handle);
//===================================================================
/**
 * Read in a single byte from the stream.
 * @return The byte read in the low 8 bits of the returned value, or -1 if the stream has closed.
	 @exception IOException if an error occurs reading from the stream.
 */
//===================================================================
public int read() throws IOException;
//===================================================================
/**
 * Write out a single byte to the stream.
 * @exception IOException if an error occurs writing to the stream.
 */
//===================================================================
public void write(int aByte) throws IOException;
//===================================================================
/**
 * Flush all buffered bytes out to the destination. This is call blocks the
 * current mThread, but allows others to execute.
 * @exception IOException if an error occured.
 */
//===================================================================
public void flush() throws IOException;
//===================================================================

//===================================================================
public InputStream toInputStream() throws IllegalStateException;
//===================================================================
public OutputStream toOutputStream() throws IllegalStateException;
//===================================================================

}


