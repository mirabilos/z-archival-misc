package ewe.io;

//##################################################################
public interface BasicRandomAccessStream extends BasicStream{
//##################################################################
/**
 * Tell the Stream to move to the specific position. This is a non-blocking call.
 * @param pos The position to seek to.
 * @return true if the seek completed successfully, false if the seek did not complete yet.
 * @exception IOException if an error occured during the seek.
 */
public boolean seekPosition(long pos) throws IOException;
/**
 * Retrieve the file position. This is non-blocking
 * @return the position of the stream or -1 if the position is not known yet.
 * @exception IOException if an error occured while getting the position.
 */
public long tellPosition() throws IOException;
/**
 * Gets the length of the open stream. This is non-blocking.
 * @return the length of the stream or -1 if the length is not known yet.
 * @exception IOException if an error occured while getting the length.
 */
public long getStreamLength() throws IOException;
/**
* Set the length of the RandomAccessStream if possible. This is non-blocking, it will
* return false if the operation is still continuing and true if the operation completed.
* If the operation could not be performed at all an IOException will be thrown.
* <p>
* Make no assumptions about the success of this method. Not all RAS objects will support
* setStreamLength() or setLength() - not even all Files on all systems will support this.
* For example, PersonalJava/Java 1.1 does not support this feature and will throw an IOException.
* <p>
* What happens to the file position pointer after this method is called is unpredictable,
* especially if you are truncating the file. You should ALWAYS reset the file position pointer
* after calling this method to be where you wish it to be.
**/
public boolean setStreamLength(long newLength) throws IOException;
/**
* Non-blocking read at a particular location. Used if there are multiple threads reading/writing
* to a RandomAccessStream.
* @param location The location to read from.
* @param dest The destination buffer.
* @param offset The location in the buffer to hold the data.
* @param length The number of bytes to read.
* @return 0 = No bytes available now, -1 = End of stream, >0 = Number of bytes read.
* @exception IOException if an error occured during reading.
*/
public int nonBlockingRead(long location,byte[] dest,int offset,int length) throws IOException;
/**
* Non-blocking write at a particular location. Used if there are multiple threads reading/writing
* to a RandomAccessStream.
* @param location The location to write to.
* @param src The source buffer.
* @param offset The location in the buffer that holds the data.
* @param length The number of bytes to write.
* @return 0 = No bytes can be written now, >0 = Number of bytes written.
* @exception IOException if an error occured during writing.
*/
public int nonBlockingWrite(long location,byte[] src,int offset,int length) throws IOException;

/**
* Tests if the RandomAccessStream was opened in read-write mode as opposed to read-ony mode.
**/
public boolean canWrite();


//##################################################################
}
//##################################################################

