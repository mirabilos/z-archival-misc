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
/**
* An InputStream is used to read bytes - however all read() operations will
* block the current thread. This is not meant to be a replacement for a ewe.io.Stream -
* rather it is meant to make it easier to port Java applications.
* <p>
* This can be used either as the base class for creating your own InputStream
* objects, OR it can be used to adapt a Stream object into an InputStream, or adapt a
* RandomStream object into an InputStream.
**/
//##################################################################
public class InputStream implements Streamable{
//##################################################################
protected boolean closed;
protected boolean atEOF;
private long markPos = -1;
private long readAheadLimit = 0;

//===================================================================
public ewe.sys.Handle toStream(boolean randomStream, String mode)
//===================================================================
{
	if (!randomStream && mode.equals("r"))
		return new ewe.sys.Handle(Handle.Succeeded,new StreamAdapter(this));
 	Handle h = new Handle(Handle.Failed,null);
	h.errorObject = new IOException("Cannot provide the type and mode of stream.");
	return h;
}
//===================================================================
public String getName(){ return "Unnamed Stream";}
//===================================================================

/**
* The underlying Stream object - if any.
**/
protected Stream stream;
/**
* The underlying InputStream object - if any.
**/
protected InputStream in;
/**
* The underlying InputStream object - if any.
**/
protected RandomStream rs;

//-------------------------------------------------------------------
protected InputStream() {}
//-------------------------------------------------------------------

//===================================================================
public InputStream(Stream stream)
//===================================================================
{
	if (stream instanceof RandomAccessStream)
		this.rs = new RandomStream((RandomAccessStream)stream);
	else
		this.stream = stream;
}
//===================================================================
public InputStream(RandomStream rs)
//===================================================================
{
	this.rs = rs;
}
//-------------------------------------------------------------------
protected InputStream(InputStream in)
//-------------------------------------------------------------------
{
	this.in = in;
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	if (stream != null)
		if (!stream.close()) throw new IOException();
	if (in != null) in.close();
	if (rs != null) rs.close();
}

private byte [] buff;

/**
* This reads a single byte using the InputStream read(byte[] buffer,int start,int lengh) method.
* If you override the multi-byte read operation instead of the single byte read operation, then
* override int read() to call this method.
**/
//-------------------------------------------------------------------
protected int readSingleByteFromMultiByteRead() throws IOException
//-------------------------------------------------------------------
{
	if (buff == null) buff = new byte[1];
	int got = read(buff,0,1);
	if (got == -1) return -1;
	return (int)buff[0] & 0xff;
}
/**
* Reads the next byte of data from this input stream.
* The value byte is returned as an int in the range 0 to 255.
* If no byte is available because the end of the stream has been reached, the value -1 is returned.
* This method blocks until input data is available, the end of the stream is detected,
* or an exception is thrown.
* @return the byte read or -1 on end of stream.
* @exception IOException if an I/O error occured.
*/
//===================================================================
public int read() throws IOException
//===================================================================
{
	if (atEOF) return -1;
	if (closed) throw new IOException("Stream closed.");
	if (rs == null && stream == null && in == null) throw new IOException("InputStream.read() - not implemented.");
	return readSingleByteFromMultiByteRead();
}
/**
* Read in a number of bytes of data from the input stream.
* @param buffer a destination buffer for the data.
* @param start The start offset in the destination buffer.
* @param length The number of bytes to read.
* @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached.
* @exception IOException if an I/O error occurs during reading.
*/
//===================================================================
public int read(byte buffer[],int start,int length) throws IOException
//===================================================================
{
	if (atEOF) return -1;
	if (closed) throw new IOException("Stream closed.");
	if (length <= 0) return 0;
//
// No stream or input? Then use single byte read() method.
//
	if (rs == null && stream == null && in == null){
		for (int i = 0; i < length; i++){
			int got = read();
			if (got == -1){
				atEOF = true;
				if (i == 0) return -1;
				else return i;
			}
			buffer[i+start] = (byte)got;
		}
		return length;
	}
//
// Use multi-byte read.
//
	if (in != null)
		return in.read(buffer,start,length);
	else if (stream != null)
		return stream.read(buffer,start,length);
	else if (rs != null)
		return rs.read(buffer,start,length);
	else
		throw new IOException();
}
/**
 * Read in a number of bytes of data from the input stream equal to the length of the provided buffer.
 * Additional verbose
 * @param buffer a destination buffer for the data.
 * @return  the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached.
 * @exception IOException if an I/O error occurs during reading.
 */
//===================================================================
public int read(byte buffer[]) throws IOException
//===================================================================
{
	return read(buffer,0,buffer.length);
}

//===================================================================
public boolean markSupported()
//===================================================================
{
	if (in != null) return in.markSupported();
	else if (rs != null) return true;
	else return false;
}
//===================================================================
public void mark(int readLimit)
//===================================================================
{
	if (in != null) in.mark(readLimit);
	else if (rs != null){
		try{
			markPos = rs.tell();
		}catch(IOException e){
			markPos = -1;
		}
		readAheadLimit = readLimit;
	}
}
//===================================================================
public void reset() throws IOException
//===================================================================
{
	if (in != null) in.reset();
	else if (rs != null){
		if (markPos < 0) throw new IOException("mark() not called.");
		long moved = rs.tell()-markPos;
		if (moved > readAheadLimit) throw new IOException("Read ahead limit exceeded.");
		rs.seek(markPos);
		markPos = -1;
	}else
		throw new IOException("Reset not supported.");
}
/**
 * Skip over a certain number of bytes.
 * @param toSkip the number of bytes to skip over.
 * @return the actual number of bytes skipped - which may be less than toSkip.
 * @exception IOException if an I/O error occurs while skipping.
 */
//===================================================================
public long skip(long toSkip) throws IOException
//===================================================================
{
	if (in != null) return in.skip(toSkip);
	else if (rs != null) return rs.skip(toSkip);
	byte [] buff = new byte[1024];
	long left = toSkip;
	long skipped = 0;
	while(left > 0){
		int toRead = left > 1024L ? 1024 : (int)left;
		int did = read(buff,0,toRead);
		if (did == -1) return skipped;
		skipped += did;
		left = toSkip-skipped;
	}
	return skipped;
}
/**
 * Returns the number of bytes that can be read without blocking.
 * @return 0 in the default implementation of InputStream.
 * @exception IOException if an I/O error occurs.
 */
//===================================================================
public int available() throws IOException
//===================================================================
{
	if (in != null) return in.available();
	else if (rs != null) return rs.available();
	return 0;
}
/**
* This prevents further reads from the InputStream but does not close any underlying
* IO stream or resource (e.g. a Socket). Any further reads will return end-of-file.
**/
//===================================================================
public void shutdown() throws IOException
//===================================================================
{
	if (in != null) in.shutdown();
	closed = true;
}
/**
Convert this InputStream to a Stream implementation that allows reading.
This will never return null.
**/
//===================================================================
public Stream toReadableStream()
//===================================================================
{
	if (stream != null) return stream;
	else if (rs != null) return rs.toReadableStream();
	else return new StreamAdapter(this);
}
/**
If the underlying Stream object implements FastStream this method will return that Stream
object. If this InputStream also happens to implement FastStream, then this InputStream
will be returned. Otherwise the method will return null.
**/
//===================================================================
public FastStream getFastStream()
//===================================================================
{
	if (this instanceof FastStream) return (FastStream)this;
	else if (stream instanceof FastStream) return (FastStream)stream;
	else if (rs != null) return rs.getFastStream();
	else return null;
}
//##################################################################
}
//##################################################################

