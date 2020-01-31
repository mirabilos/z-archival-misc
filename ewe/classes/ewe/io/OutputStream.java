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
* An OutputStream is used to write bytes - however all write operations will
* block the current thread. This is not meant to be a replacement for a ewe.io.Stream -
* rather it is meant to make it easier to port Java applications.
* <p>
* This can be used either as the base class for creating your own OutputStream
* objects, OR it can be used to adapt a Stream object into an OutputStream.
**/
//##################################################################
public class OutputStream implements Streamable{
//##################################################################
protected Stream stream;
protected RandomStream rs;
protected OutputStream out;

protected boolean closed;
private byte [] buff;

//===================================================================
public ewe.sys.Handle toStream(boolean randomStream, String mode)
//===================================================================
{
	if (!randomStream && mode.equals("w"))
		return new ewe.sys.Handle(Handle.Succeeded,new StreamAdapter(this));
 	Handle h = new Handle(Handle.Failed,null);
	h.errorObject = new IOException("Cannot provide the type and mode of stream.");
	return h;
}
//===================================================================
public String getName(){ return "Unnamed Stream";}
//===================================================================

//-------------------------------------------------------------------
protected OutputStream() {}
//-------------------------------------------------------------------

//===================================================================
public OutputStream(Stream stream)
//===================================================================
{
	this.stream = stream;
}
//===================================================================
public OutputStream(RandomStream rs)
//===================================================================
{
	this.rs = rs;
}
//-------------------------------------------------------------------
protected OutputStream(OutputStream out)
//-------------------------------------------------------------------
{
	this.out = out;
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	closed = true;
	if (stream != null)
		if (!stream.close()) throw new IOException();
	if (out != null) out.close();
	if (rs != null) rs.close();
}
//===================================================================
public void flush() throws IOException
//===================================================================
{
	if (stream != null) stream.flush();
	if (out != null) out.flush();
	if (rs != null) rs.flush();
}
/**
 * Shutdown the stream but do not close any underlying IO stream.
	Further writes will throw an IOException.
	For example if this
 * OutputStream is on a Socket, this will prevent further outputs but will still allow
 * reading on the InputStream.
 * @exception IOException on error.
 */
//===================================================================
public void shutdown() throws IOException
//===================================================================
{
	flush();
	closed = true;
}
/**
* This writes a single byte using the OutputStream write(byte[] buffer,int start,int lengh) method.
* If you override the multi-byte write operation instead of the single byte write operation, then
* override void write(int value) to call this method.
**/
//-------------------------------------------------------------------
protected void writeSingleByteToMultiByteWrite(int value) throws IOException
//-------------------------------------------------------------------
{
	if (buff == null) buff = new byte[1];
	buff[0] = (byte)value;
	write(buff,0,1);
}
/**
* Writes a single byte to the stream. This method blocks until the byte is written.
* @exception IOException if an I/O error occured.
*/
//===================================================================
public void write(int value) throws IOException
//===================================================================
{
	if (rs == null && stream == null && out == null) throw new IOException("OutputStream.write() - not implemented.");
	writeSingleByteToMultiByteWrite(value);
}
/**
* Write a number of bytes of data to the output stream. This will block until all the bytes are written.
* @param buffer the source buffer for the data.
* @param start The start offset in the buffer.
* @param length The number of bytes to write.
* @exception IOException if an I/O error occurs during writing.
*/
//===================================================================
public void write(byte buffer[],int start,int length) throws IOException
//===================================================================
{
	if (length <= 0) return;
	if (closed) throw new IOException("Stream closed.");
//
// No stream or output? Then use single byte write() method.
//
	if (rs == null && stream == null && out == null){
		for (int i = 0; i < length; i++)
			write(buff[i+start]);
		return;
	}
	//
	if (stream != null) stream.write(buffer,start,length);
	else if (rs != null) rs.write(buffer,start,length);
	else out.write(buffer,start,length);

}
/**
 * Write a number of bytes of data to the output stream equal to the length of the provided buffer.
 * @param buffer the source buffer for the data.
 * @return  the total number of bytes written from the buffer - which is always equal to the length of the buffer.
 * @exception IOException if an I/O error occurs during writing.
 */
//===================================================================
public void write(byte buffer[]) throws IOException
//===================================================================
{
	write(buffer,0,buffer.length);
}
/**
Convert this OutputStream to a Stream implementation that allows writing.
This will never return null.
**/
//===================================================================
public Stream toWritableStream()
//===================================================================
{
	if (rs != null) return rs.toWritableStream();
	else if (stream != null) return stream;
	else return new StreamAdapter(this);
}
/**
If the underlying Stream object implements FastStream this method will return that Stream
object. If this OutputStream also happens to implement FastStream, then this OutputStream
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

