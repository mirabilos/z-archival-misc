package ewe.io;

//##################################################################
public interface FastStream{
//##################################################################
/**
* This attempts to read using a fast native or direct method. Failing that a normal read is done. If a
* native method is used the method <b>will</b> block the entire VM until the data is read,
* so you should use this with care.
* @param data The destination for the data.
* @param offset The index in the destination for the data.
* @param length The number of bytes to read.
* @param readAll if this is true then the method will not return until a full length number of
* bytes have been read. If the stream ends before this then an IOException is thrown.
* @return the number of bytes read or -1 if the stream ended with no bytes read.
* @exception IOException
*/
//===================================================================
public int quickRead(byte[] data,int offset,int length,boolean readAll) throws IOException;
//===================================================================
/**
* This attempts to write using a fast native or direct access method. Failing that a normal write is done. If the
* native method is used the method <b>will</b> block the entire VM until the data is written,
* so you should use this with care.
* @param data The source of the data bytes.
* @param offset The index in the source of the data.
* @param length The number of bytes to write.
* @exception IOException
*/
//===================================================================
public void quickWrite(byte[] data,int offset,int length) throws IOException;
//===================================================================

//##################################################################
}
//##################################################################

