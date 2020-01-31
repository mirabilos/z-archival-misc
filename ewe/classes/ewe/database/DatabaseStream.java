package ewe.database;
import ewe.io.IOException;
import ewe.io.DataProcessor;
import ewe.sys.Time;
/**
This is not part of the Ewe Simple Database specification. This is used for an
actual implementation of Simple Database, specifically by RecordFile.
**/
//##################################################################
public interface DatabaseStream{
//##################################################################
/**
* Return the location of where you can store your data. This should be at a 16-byte border.
**/
public int getFirstDataLocation();
/**
A safeWrite() is one where the integer data is either stored completely at the specified
location or not at all. If for some reason, the application should fail during the safe
write (assuming that there is no problem with the file) then either the write had no
effect, OR, the write would be completed the next time the file is opened.
**/
public void safeWrite(long location, int data) throws IOException;
public void safeWrite(long location1, int data1, long location2, int data2) throws IOException;
public void safeWrite(
	long location1, int data1, long location2, int data2,
	long location3, int data3, long location4, int data4) throws IOException;

public int readIntAt(long location) throws IOException;
public void writeIntAt(long location, int value) throws IOException;
public void writeAll(long location,byte[] data,int offset,int length) throws IOException;
/**
This throws an exception if all the specified bytes are not found at the specified
location.
**/
public void readAll(long location,byte[] data,int offset,int length) throws IOException;
/**
This should extend the Stream if necessary.
**/
public void zero(long location, int numBytes) throws IOException;
/**
* Try to truncate to a particular length. If the truncate is not possible because of restrictions
* on the underlying Stream, this returns false. An IOException is only thrown if there is a
* serious IO error which indicates IO should be abandonded.
**/
public boolean truncateTo(long length) throws IOException;

public boolean setDecryptor(DataProcessor decryptor) throws IOException;
public boolean setDecryptorAndEncryptor(DataProcessor decryptor, DataProcessor encryptor) throws IOException;

public boolean atEOF(long location) throws IOException;
public long length() throws IOException;
public void flush() throws IOException;
public void close() throws IOException;
public void delete() throws IOException;
public void rename(String newName) throws IOException;
public Time getModifiedTime() throws IOException;
public boolean setModifiedTime(Time t) throws IOException;
/**
 * Close the stream temporarily. Can be opened with re-open.
 * @return true if it was actually closed.
 * @exception IOException if an error occurs.
 */
public boolean temporaryClose() throws IOException;
/**
 * Reopen the stream after a temporaryClose(). The file position will be unspecified after this
 * call.
 * @exception IOException if an error occurs.
 */
public void reopen() throws IOException;
/**
* Tests if the DatabaseStream was opened in read-write mode as opposed to read-ony mode.
**/
public boolean canWrite();
//##################################################################
}
//##################################################################

