package ewe.io;


/**
 * Abstract class for reading character streams.  The only methods that a
 * subclass must implement are read(char[], int, int) and close().
 */
//##################################################################
public abstract class Reader {
//##################################################################
	/**
	 * The object used to synchronize operations on this stream.
	 */
	protected ewe.sys.Lock lock = new ewe.sys.Lock();



	/**
	 * Get the Lock object used to synchronize IO operations on this Reader.
	 */
	//===================================================================
	public ewe.sys.Lock getLock()
	//===================================================================
	{
		return lock;
	}
	//-------------------------------------------------------------------
	protected Reader() {}
	//-------------------------------------------------------------------
	/**
	 * Create a new character-stream reader whose critical sections will
	 * synchronize on the given object.
	 */
	//-------------------------------------------------------------------
	protected Reader(ewe.sys.Lock lock)
	//-------------------------------------------------------------------
	{
		if (lock == null) throw new NullPointerException();
		this.lock = lock;
	}

	/**
	 * Read a single character.  This method will block until a character is
	 * available, an I/O error occurs, or the end of the stream is reached.
	 *
	 * @return The character read, as an integer in the range 0 to 0xffff,
	 * or -1 if the end of the stream has been reached
	 * @exception IOException  If an I/O error occurs
	 */
	//===================================================================
	public int read() throws IOException
	//===================================================================
	{
		char cb[] = new char[1];
		if (read(cb, 0, 1) == -1) return -1;
		else return cb[0] & 0xffff;
	}
	/**
	 * Read characters into an array.  This method will block until some input
	 * is available, an I/O error occurs, or the end of the stream is reached.
	 * @param cbuf  Destination buffer
	 * @return The number of bytes read, or -1 if the end of the stream
	 *              has been reached
	 * @exception   IOException  If an I/O error occurs
	 */
	//===================================================================
	public int read(char cbuf[]) throws IOException
	//===================================================================
	{
		return read(cbuf, 0, cbuf.length);
	}
	/**
	 * Read characters into a portion of an array.  This method will block
	 * until some input is available, an I/O error occurs, or the end of the
	 * stream is reached.
	 * @param cbuf  Destination buffer
	 * @param off   Offset at which to start storing characters
	 * @param len   Maximum number of characters to read
	 * @return     The number of characters read, or -1 if the end of the
	 *             stream has been reached
	 * @exception  IOException  If an I/O error occurs
	 */
	abstract public int read(char cbuf[], int off, int len) throws IOException;
	/**
 * Skip over a certain number of characters.
 * @param toSkip the number of characters to skip over.
 * @return the actual number of characters skipped - which may be less than toSkip.
 * @exception IOException if an I/O error occurs while skipping.
 */
//===================================================================
public long skip(long toSkip) throws IOException
//===================================================================
{
	char [] buff = new char[1024];
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
 * Tell whether this stream is ready to be read.
 *
 * @return True if the next read() is guaranteed not to block for input,
 * false otherwise.  Note that returning false does not guarantee that the
 * next read will block.
 *
 * @exception  IOException  If an I/O error occurs
 */
//===================================================================
public boolean ready() throws IOException
//===================================================================
{
	return false;
}

//===================================================================
public boolean markSupported()
//===================================================================
{
	return false;
}
//===================================================================
public void mark(int readLimit) throws IOException
//===================================================================
{
	throw new IOException("mark() not supported.");
}
//===================================================================
public void reset() throws IOException
//===================================================================
{
	throw new IOException("reset() not supported.");
}
/**
 * Close the stream.
 * @exception  IOException  If an I/O error occurs
 */
//===================================================================
abstract public void close() throws IOException;
//===================================================================

//##################################################################
}
//##################################################################


