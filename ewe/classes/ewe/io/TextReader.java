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

/**
* A TextReader is used for reading Unicode text files that may be encoded as bytes in any
* format. It uses a TextCodec for decoding the byte encoded input Stream and so you must
* provide the correct Codec for the file. The only Codecs provided by the Ewe library are
* the JavaUtf8Codec (which will encode ANY Unicode text) and AsciiCodec (which only encodes
* Unicode values from 0 to 255 as their exact byte values).
* <p>
* By default, a JavaUtf8Codec is used for encoding.
**/

//##################################################################
public class TextReader extends Reader{
//##################################################################

/**
* This is the underlying stream, or the "input" InputStream may be used instead.
**/
protected Stream stream;
/**
* This is the underlying stream, or the "stream" Stream may be used instead.
**/
protected InputStream input;
/**
* This indicates that the close() method has already been called.
**/
protected boolean closed = false;
/**
 * Create a new TextReader to read from the specified Stream.
 * @param in The input Stream.
 */
//===================================================================
public TextReader(BasicStream in)
//===================================================================
{
	if (in instanceof Stream) stream = (Stream)in;
	else in = new StreamAdapter(in);
}
/**
 * Create a new TextReader to read from the specified InputStream.
 * @param in The InputStream
 */
//===================================================================
public TextReader(InputStream in)
//===================================================================
{
	input = in;
}
/**
 * Create a new TextReader to read from the specified file name.
 * @param path the path to the file to read.
 */
//===================================================================
public TextReader(String path) throws IOException
//===================================================================
{
	this(ewe.sys.Vm.newFileObject().getNew(path).toRandomAccessStream("r"));
}

/**
* This is the codec used for decoding incoming data bytes into characters. By default
* it is a JavaUtf8Codec.
**/
public TextCodec codec = new JavaUtf8Codec();
/**
* This is the buffer size to use when reading from the underlying stream.
**/
public int bufferSize = 1024*10;

private byte [] buff;
private ewe.util.CharArray readIn = null;
private int readOut = 0;
private boolean readEnd = false;
/**
 * Read in a number of characters.
 * @param dest The destination buffer for the characters.
 * @param offset The location in the destination to start writing to.
 * @param length The maximum number of characters to read.
 * @return The number of characters read or -1 if the end of the stream has been reached.
 * @exception IOException If there is an error reading the data.
 */
//===================================================================
public int readDirect(char[] dest, int offset, int length) throws IOException
//===================================================================
{
	if (closed) return -1;
	if (buff == null) buff = new byte[bufferSize];
	if (length <= 0) return 0;
	while(true){
		//
		//Check if there is any data in the decoded readIn CharArray.
		//
		if (readIn != null){
			if (readOut < readIn.length){
				int toRead = readIn.length-readOut;
				if (toRead > length) toRead = length;
				ewe.sys.Vm.copyArray(readIn.data,readOut,dest,offset,toRead);
				readOut += toRead;
				return toRead;
			}
		}
		//
		// No data in readIn, read bytes from the Stream and do a decode.
		//
		if (readEnd) return -1;
		int got = stream != null ? stream.read(buff,0,buff.length) : input.read(buff,0,buff.length);
		if (got == -1) {
			readEnd = true;
			readIn = codec.decodeText(null,0,0,true,readIn);
		}else
			readIn = codec.decodeText(buff,0,got,false,readIn);
		readOut = 0;
	}
}
ewe.util.CharArray lineBuffer;
private int lineBufferSize = 1024;

//-------------------------------------------------------------------
private boolean readMore() throws IOException
//-------------------------------------------------------------------
{
	if (lineBuffer == null){
		lineBuffer = new ewe.util.CharArray();
		lineBuffer.data = new char[0];
		lineBuffer.length = 0;
	}
	if (lineBuffer.data.length-lineBuffer.length <= 100){
		char [] data = new char[lineBuffer.data.length+lineBufferSize];
		ewe.sys.Vm.copyArray(lineBuffer.data,0,data,0,lineBuffer.length);
		lineBuffer.data = data;
	}
	int got = readDirect(lineBuffer.data,lineBuffer.length,lineBuffer.data.length-lineBuffer.length);
	if (got == -1) return false;
	lineBuffer.length += got;
	return true;
}
/**
 * Read a line of text.
 * @return The line read in (without the trailing CR/LF) or null if end-of-file has been reached.
 * @exception IOException
 */
//===================================================================
public String readLine() throws IOException
//===================================================================
{
	if (lineBuffer == null) readMore();
	int lastCheck = 0;
	while(true){
		int max = lineBuffer.length;
		char [] data = lineBuffer.data;
		boolean hasCr = false;
		for (int i = lastCheck; i<max; i++){
			if (data[i] == '\n'){
				hasCr = (i != 0 && data[i-1] == '\r');
				String ret = new String(data,0,hasCr ? i-1 : i);
				ewe.sys.Vm.copyArray(data,i+1,data,0,max-i-1);
				lineBuffer.length -= i+1;
				return ret;
			}
		}
		//
		// Didn't find a LF.
		//
		lastCheck = lineBuffer.length;
		if (!readMore()){
			if (lineBuffer.length == 0) return null; //Nothing left.
			String ret = new String(lineBuffer.data,0,lineBuffer.length);
			lineBuffer.length = 0;
			return ret;
		}
	}
}
/**
 * Read in a number of characters.
 * @param dest The destination buffer for the characters.
 * @param offset The location in the destination to start writing to.
 * @param length The maximum number of characters to read.
 * @return The number of characters read or -1 if the end of the stream has been reached.
 * @exception IOException If there is an error reading the data.
 */
//===================================================================
public int read(char[] dest, int offset, int length) throws IOException
//===================================================================
{
	if (length <= 0) return 0;
	//
	// First check the lineBuffer.
	//
	if (lineBuffer != null && lineBuffer.length > 0){
		int toRead = lineBuffer.length;
		if (toRead > length) toRead = length;
		ewe.sys.Vm.copyArray(lineBuffer.data,0,dest,offset,toRead);
		if (toRead != lineBuffer.length) ewe.sys.Vm.copyArray(lineBuffer.data,toRead,lineBuffer.data,0,lineBuffer.length-toRead);
		lineBuffer.length -= toRead;
		return toRead;
	}
	//
	// Nothing in there? Then read directly.
	//
	return readDirect(dest,offset,length);
}
/**
 * Close the TextReader and the underlying Stream.
 * @exception IOException on error.
 */
//===================================================================
public void close() throws IOException
//===================================================================
{
	if (closed) return;
	closed = true;
	if (stream != null) stream.close();
	else input.close();
}
/**
 * Read in exactly the specified number of characters. An exception will be thrown
 * if the Stream ends before the specified number of characters is read.
 * @param dest The destination for the characters.
 * @param offset The offset in the destination for the characters.
 * @param length The number of characters to read.
 * @exception IOException if an IO error occurs or if the
*/
//===================================================================
public void readFully(char[] dest, int offset, int length) throws IOException
//===================================================================
{
	int did = 0;
	while(length > 0){
		int ret = read(dest,offset,length);
		if (ret <= 0) throw new IOException("Unexpected end of stream");
		did += ret;
		offset += ret;
		length -= ret;
	}
}
/**
 * Read in exactly the specified number of characters as a String.
 * @param length The number of characters to read.
* @return A String holding the text.
* @exception IOException if an IO error occurs or if the Stream ends before all the characters
* could be read.
*/
//===================================================================
public String readFully(int length) throws IOException
//===================================================================
{
	char [] dest = new char[length];
	readFully(dest,0,length);
	return new String(dest);
}
/**
 * Read in up to the specified maximum number of characters as a String. There may be less
 * characters specified IF the Stream ends before the number of characters can be read. If
 * the method returns null the Stream has ended.
* @param length The maximum number of characters to read.
* @return A String holding the characters read or null on end of Stream.
* @exception IOException if an IO error occurs.
*/
//===================================================================
public String readString(int length) throws IOException
//===================================================================
{
	char [] dest = new char[length];
	int offset = 0;
	int did = 0;
	while(length > 0){
		int ret = read(dest,offset,length);
		if (ret <= 0) return did == 0 ? null : new String(dest,0,did);
		did += ret;
		offset += ret;
		length -= ret;
	}
	return new String(dest);
}
/**
 * Read in all the characters from this TextReader as a String. The TextReader is NOT closed
 * after.
 * @return The entire stream read in as a String.
 * @exception IOException if an error occured during reading or decoding.
 */
//===================================================================
public String readAll() throws IOException
//===================================================================
{
	char [] str = new char[0];
	char [] buff = new char[10*1024];
	while(true){
		int got = read(buff,0,buff.length);
		if (got == -1) return new String(str);
		char [] now = new char[str.length+got];
		ewe.sys.Vm.copyArray(str,0,now,0,str.length);
		ewe.sys.Vm.copyArray(buff,0,now,str.length,got);
		str = now;
	}
}
/**
 * Read all the characters from the input stream using the specified TextCodec. The stream
 * will be closed after.
 * @param basicStreamOrInputStream This must be a BasicStream or an InputStream object.
 * @param codec The codec to use or null for the default Java UTF8 codec.
 * @return The entire stream read in as a String.
 * @exception IOException if an error occured during reading or decoding.
 */
//===================================================================
public static String readAll(Object basicStreamOrInputStream, TextCodec codec) throws IOException
//===================================================================
{
	TextReader tr = null;
	try{
		tr = (basicStreamOrInputStream instanceof BasicStream) ?
			new TextReader((BasicStream)basicStreamOrInputStream) : new TextReader((InputStream)basicStreamOrInputStream);
		if (codec != null) tr.codec = codec;
		return tr.readAll();
	}finally{
		try{
			tr.close();
		}catch(Exception e){}
	}
}
//===================================================================
public boolean ready() throws IOException
//===================================================================
{
	if (closed) throw new IOException("Stream closed");
	else return true;
}
/*
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	TextReader tr = new TextReader(new FileInputStream(args[0]));
	while(true){
		String line = tr.readLine();
		if (line == null) break;
		ewe.sys.Vm.debug("["+line+"]");
		line = tr.readString(200);
		if (line == null) break;
		ewe.sys.Vm.debug("<"+line+">");
	}
	tr.close();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

