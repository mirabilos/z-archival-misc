package ewe.io;


/**
 * Abstract class for writing to character streams.  The only methods that a
 * subclass must implement are write(char[], int, int), flush(), and close().
 */
//##################################################################
public abstract class Writer{
//##################################################################

	private char[] writeBuffer;
	private final int writeBufferSize = 1024;

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
	protected Writer() {}
	//-------------------------------------------------------------------
	/**
	 * Create a new character-stream writer whose critical sections will
	 * synchronize on the given object.
	 */
	//-------------------------------------------------------------------
	protected Writer(ewe.sys.Lock lock)
	//-------------------------------------------------------------------
	{
		if (lock == null) throw new NullPointerException();
		this.lock = lock;
	}
		/**
	 * Write a single character.  The character to be written is contained in
	 * the 16 low-order bits of the given integer value; the 16 high-order bits
	 * are ignored.
		 */
	//===================================================================
	public void write(int c) throws IOException
	//===================================================================
	{
		lock.synchronize(); try{
			if (writeBuffer == null){
				writeBuffer = new char[writeBufferSize];
			}
			writeBuffer[0] = (char) c;
			write(writeBuffer, 0, 1);
		}finally{
			lock.release();
		}
	}
	/**
	 * Write a complete array of characters.
	 *
	 * @param  out  Array of characters to be written
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	//===================================================================
	public void write(char out[]) throws IOException
	//===================================================================
	{
		write(out, 0, out.length);
	}
		/**
	 * Write a portion of an array of characters.
	 *
	 * @param  src  Array of characters
	 * @param  off   Offset from which to start writing characters
	 * @param  len   Number of characters to write
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	abstract public void write(char src[], int off, int len) throws IOException;
		/**
	 * Write a string.
	 *
	 * @param  str  String to be written
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	public void write(String str) throws IOException
	{
		write(str, 0, str.length());
	}
	/**
	 * Write a portion of a string.
	 *
	 * @param  str  A String
	 * @param  off  Offset from which to start writing characters
	 * @param  len  Number of characters to write
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	public void write(String str, int off, int len) throws IOException
	{
		lock.synchronize(); try{
			char cbuf[];
			if (len <= writeBufferSize) {
				if (writeBuffer == null) {
					writeBuffer = new char[writeBufferSize];
				}
				cbuf = writeBuffer;
			} else {	// Don't permanently allocate very large buffers.
				cbuf = new char[len];
			}
			str.getChars(off, (off + len), cbuf, 0);
			write(cbuf, 0, len);
		}finally{
			lock.release();
		}
	}

	/**
	 * Flush the stream.  If the stream has saved any characters from the
	 * various write() methods in a buffer, write them immediately to their
	 * intended destination.  Then, if that destination is another character or
	 * byte stream, flush it.  Thus one flush() invocation will flush all the
	 * buffers in a chain of Writers and OutputStreams.
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	//===================================================================
	abstract public void flush() throws IOException;
	//===================================================================

	/**
	 * Close the stream, flushing it first.  Once a stream has been closed,
	 * further write() or flush() invocations will cause an IOException to be
	 * thrown.  Closing a previously-closed stream, however, has no effect.
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	//===================================================================
	abstract public void close() throws IOException;
	//===================================================================

//##################################################################
}
//##################################################################

