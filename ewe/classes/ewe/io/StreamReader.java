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
* This is used for reading cr/lf delimited lines from a Stream or InputStream, and is
* far more effecient than a BufferedReader - but has less functionality.
**/
//##################################################################
public class StreamReader extends ewe.util.Errorable{
//##################################################################
/**
* A buffer size to use - by default it is 1024 - however increasing this may not make
* a significant increase in performance.
**/
public int bufferSize = 1024;
/**
* The underlying Stream if one is used.
**/
protected Stream stream;
/**
* The underlying InputStream if one is used.
**/
protected InputStream input;
/**
* Indicates if the stream is closed or not.
**/
protected boolean closed = false;

private byte [] buffer = new byte[0];
private int numRead = 0;
private int pos = 0;
private IOException exception;

/**
* This is the "UTF8" constant string.
**/
//===================================================================
public static final String UTF8 = "UTF8";
//===================================================================

/**
* Set this to be a text encoding form. Either null for pure ASCII or "UTF8" for Java UTF8
* encoding. By default it is UTF8.
**/
//===================================================================
public String encoding = UTF8;
//===================================================================


/**
 * Create a StreamReader from a Stream or BasicStream which has been opened for input.
 */
//===================================================================
public StreamReader(BasicStream s)
//===================================================================
{
	if (s instanceof Stream) stream = (Stream)s;
	else if (s == null) throw new NullPointerException();
	else stream = new StreamAdapter(s);
}
/**
 * Create a StreamReader from a file name.
 */
//===================================================================
public StreamReader(String path) throws IOException
//===================================================================
{
	this(ewe.sys.Vm.newFileObject().getNew(path).toRandomAccessStream("r"));
}
/**
 * Create a StreamReader from an InputStream.
 */
//===================================================================
public StreamReader(InputStream in)
//===================================================================
{
	if (in == null) throw new NullPointerException();
	input = in;
}
/**
 * Create a StreamReader from a File object. The toReadableStream() method is called
 * on the file.
 */
//===================================================================
public StreamReader(File inputFile) throws IOException
//===================================================================
{
	this(inputFile.toReadableStream());
}

/**
 * Returns if the StreamReader is open.
 */
//===================================================================
public boolean isOpen()
//===================================================================
{
	if (input != null) return !closed;
	if (stream == null) return false;
	return stream.isOpen();
}

private int totalRead = 0;
/**
 * Read more into the internal buffer.
 * @return true if more data was read, false if not.
 */
//-------------------------------------------------------------------
protected boolean readMore()
//-------------------------------------------------------------------
{
	if (stream == null && input == null) return false;
	int len = buffer.length;
	while (pos >= len) {
		byte [] nb = new byte[len+bufferSize];
		ewe.sys.Vm.copyArray(buffer,0,nb,0,len);
		buffer = nb;
		len = nb.length;
	}
	int ret = 0;
	try{
		ret =
			input == null ?
				stream.read(buffer,numRead,len-numRead) :
				input.read(buffer,numRead,len-numRead);
	}catch(IOException e){
		error = e.getMessage();
		ret = -1;
	}
	if (ret <= 0){
		closed = true;
 		return false;
	}
	totalRead += ret;
	numRead += ret;
	return true;
}

/**
* This reads in a line of text which was terminated in the stream by a Line Feed (\n)
* or a Carriage Return (\r) or a CR followed by LF (\r\n) or by the end of the file.
* The terminating LF or CR is NOT returned with the string.
 * @return a line of text without terminating LF or CR characters.
 * @exception IOException on an input error.
 */
//===================================================================
public String readALine() throws IOException
//===================================================================
{
	String got = readLine();
	if (got != null) return got;
	if (error == null) return null;
	throw new IOException(error);
}
/**
* This reads in a line of text which was terminated in the stream by a Line Feed (\n)
* or a Carriage Return (\r) or a CR followed by LF (\r\n) or by the end of the file.
* The terminating LF or CR is NOT returned with the string.
@deprecated use readALine() instead which throws an exception on error.
**/
//===================================================================
public String readLine()
//===================================================================
{
	int start = pos;
	int len = 0;
	while (pos >= numRead) if (!readMore()) return null; //EOF has been reached.
	while(true){
		if (pos >= numRead)
			if (!readMore()) break; //String goes from start to pos-1.
		int idx = ewe.util.Utils.findCRLF(buffer,pos,numRead-pos);
		if (idx == -1) {
			len += numRead-pos;
			pos = numRead;
		}else{
			len += idx-pos;
			pos = idx+1;
			if (buffer[idx] == 10) {
				if (idx > 0)
					if (buffer[idx-1] == 13) len--;
				break;
			}else if (pos >= numRead) {//Must be a CR.
				if (!readMore()) break;
			}
			if (buffer[pos] == 10) pos++;
			break;
		}
		/*
		byte c = buffer[pos++];
		if (c == 10) // LF
			break;
		else if (c == 13){ // CR
			if (pos >= numRead)
				if (!readMore()) break;
			if (buffer[pos] == 10) //CR followed by LF
				pos++;
			break;
		}else
			len++;
		*/
	}
	if (encoding == null) encoding = new String();
	String ret;
	if (encoding.equals(UTF8)){
		ret = ewe.util.Utils.decodeJavaUtf8String(buffer,start,len);
	}else{
		ret = ewe.util.mString.fromAscii(buffer,start,len);
	}
	int did = pos-start;
	ewe.sys.Vm.copyArray(buffer,pos,buffer,0,numRead-did);
	pos -= did;
	numRead -= did;
	return ret;
}
/**
 * Close the StreamReader and underlying stream.
 * @return true if there was no error closing the streams. False if there was an error.
 */
//===================================================================
public boolean close()
//===================================================================
{
	if (stream != null) return stream.close();
	if (input != null)
	try{
		input.close();
	}catch(IOException e){
		error = e.getMessage();
		return false;
	}
	return true;
}
/**
* This will pushback some text into the stream. It will not pushback a
* line feed unless one is in the text.
**/
//===================================================================
public void pushback(String text)
//===================================================================
{
	if (text == null) return;
	if (encoding == null) encoding = new String();
	byte [] toPush;
	if (encoding.equals(UTF8))
		toPush = ewe.util.Utils.encodeJavaUtf8String(text);
	else
		toPush = ewe.util.mString.toAscii(text);
	if (pos+numRead+toPush.length > buffer.length) {
		byte [] nb = new byte[pos+toPush.length+numRead];
		if (pos != 0)
			ewe.sys.Vm.copyArray(buffer,0,nb,0,buffer.length);
		buffer = nb;
	}
	if (toPush.length != 0){
		ewe.sys.Vm.copyArray(buffer,pos,buffer,pos+toPush.length,numRead-pos);
		ewe.sys.Vm.copyArray(toPush,0,buffer,pos,toPush.length);
	}
	numRead += toPush.length;
}
/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	File in = new File(args[0]);
	StreamReader sr = new StreamReader(in.toReadableStream());
	sr.bufferSize = 1024*4;
	//BufferedReader sr = new BufferedReader(in.toReadableStream());
	ewe.sys.Vm.debug("Reading: "+in);
	long now = System.currentTimeMillis();
	int lines = 0;
	while(true){
		String line = sr.readLine();
		if (line == null) break;
		lines++;
	}
	sr.close();
	now = System.currentTimeMillis()-now;
	ewe.sys.Vm.debug("Lines: "+lines+" in "+now);
	//ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

