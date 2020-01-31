package ewe.io;

//##################################################################
public class RandomStream{
//##################################################################
protected RandomAccessStream ras;
protected boolean closed;
private byte [] buff;
private boolean atEOF;

/**
* Set this true if you are implementing a read-only RandomStream.
**/
protected boolean noWritingAllowed;
/**
 * Calling this method causes an IOException to be thrown indicating that writing to this RandomStream
 * is not possible.
 * @exception IOException always thrown.
 */
//-------------------------------------------------------------------
protected void cantWrite() throws IOException
//-------------------------------------------------------------------
{
	throw new IOException("Cannot write to this Stream");
}

//===================================================================
public RandomStream(RandomAccessStream ras)
//===================================================================
{
	this.ras = ras;
}
//-------------------------------------------------------------------
protected RandomStream(){}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected void checkClosed() throws IOException
//-------------------------------------------------------------------
{
	if (closed) throw new IOException("Stream closed");
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	closed = true;
	if (ras != null) ras.close();
}
//===================================================================
public void seek(long position) throws IOException
//===================================================================
{
	checkClosed();
	ras.seek(position);
}
//===================================================================
public long tell() throws IOException
//===================================================================
{
	checkClosed();
	return ras.tell();
}
//===================================================================
public long length() throws IOException
//===================================================================
{
	checkClosed();
	return ras.length();
}
//===================================================================
public void setLength(long newLength) throws IOException
//===================================================================
{
	if (noWritingAllowed) cantWrite();
	checkClosed();
	ras.setLength(newLength);
}
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
	checkClosed();
	if (atEOF) return -1;
	if (ras == null) throw new IOException("RandomStream.read() - not implemented.");
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
	checkClosed();
	if (atEOF) return -1;
	if (length <= 0) return 0;
//
// No stream or input? Then use single byte read() method.
//
	if (ras == null){
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
		return ras.read(buffer,start,length);
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
	checkClosed();
	return read(buffer,0,buffer.length);
}

//===================================================================
public void flush() throws IOException
//===================================================================
{
	if (noWritingAllowed) cantWrite();
	checkClosed();
	ras.flush();
}
/**
* Writes a single byte to the stream. This method blocks until the byte is written.
* @exception IOException if an I/O error occured.
*/
//===================================================================
public void write(int value) throws IOException
//===================================================================
{
	if (noWritingAllowed) cantWrite();
	checkClosed();
	if (ras == null) throw new IOException("RandomStream.write() - not implemented.");
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
	if (noWritingAllowed) cantWrite();
	if (length <= 0) return;
	checkClosed();
//
// No stream or output? Then use single byte write() method.
//
	if (ras == null){
		for (int i = 0; i < length; i++)
			write(buff[i+start]);
		return;
	}
	//
	ras.write(buffer,start,length);
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
	if (closed) return;
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

//===================================================================
public int available() throws IOException
//===================================================================
{
	return 0;
}
//===================================================================
public long skip(long toSkip) throws IOException
//===================================================================
{
	if (toSkip < 0) return 0;
	long where = tell();
	long toGo = where+toSkip;
	seek(toGo);
	return tell()-where;
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	if (ras != null) return ras.canWrite();
	return false;
}
//-------------------------------------------------------------------
private RandomAccessStream toAStream()
//-------------------------------------------------------------------
{
	if (ras != null) return ras;
	else return new RandomStreamAdapter(this);
}
//===================================================================
public Stream toReadableStream()
//===================================================================
{
	return toAStream();
}
//===================================================================
public Stream toWritableStream()
//===================================================================
{
	return toAStream();
}
//===================================================================
public RandomAccessStream toRandomAccessStream()
//===================================================================
{
	return toAStream();
}
//===================================================================
public InputStream toInputStream()
//===================================================================
{
	return new InputStream(this);
}
//===================================================================
public OutputStream toOutputStream()
//===================================================================
{
	return new OutputStream(this);
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
	else if (ras instanceof FastStream) return (FastStream)ras;
	else return null;
}

//##################################################################
}
//##################################################################

