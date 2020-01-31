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
import ewe.sys.*;
/**
* A StreamScanner is used to parse and read in formatted data from a text file as
* quickly as possible. You use a StreamScanner by inheriting from it and overriding
* the lineReceived()/lineParsed() method.
<p>
<b>Specifying Data Format</b>
<p>
You specify the data you wish to parse as a String with a set of '%' formats separated by spaces, similar to C/C++ scanf
function. You can scan for Strings (either as fixed length Strings or individual words),
integer/long values or floating point values. The StreamScanner uses a ewe.util.DataParser
object for line scanning and so the format string specifications can be found in the API for
ewe.util.DataParser.
<p>
<b>Scanning the Data</b>
<p>
The startScanning() method is used to begin the scan operation from the Stream. Before doing
this you must register the different scanning formats that you will use on the file. You do
this be calling addFormat(). Each format is given a new unique integer id that can be used
to identify that format. Note that using the standard constructor you can provide one format
at that time (which will usually be all you need).
<p>
<b>Retrieving Scanned Data</b>
<p>
After parsing each line of text the lineParsed() method is called. This provides an array of
Objects that hold the parsed data (but no entries will provided for skipped data). Each element
will either be a ewe.sys.Long object (for integer values) or a ewe.sys.Double object
(for floating point numbers) or a SubString object.
<p>
If the preprocessLine variable is set true, then before a parse is done the lineReceived() method
is called. This should return a format code to indicate which format to use or 0 to indicate
that this line should be skipped.
<p>
**/
//##################################################################
abstract
public class StreamScanner{
//##################################################################
private BasicStream stream;
private InputStream input;
/**
* This is the buffer size to use when reading. Generally the bigger it is
* the faster reading will go.
**/
public int bufferSize = 10240;
/**
* This is the length of time to wait in between calls to the nonBlockingRead() method
* of the input stream if that call should indicate no available data yet. By default it
* is 0 (which simply passes control to another thread). Set it to -1 to tell it not to wait
* at all.
**/
public int waitTime = 0;
/**
* Set this true to tell the StreamScanner to stop scanning.
**/
public boolean shouldStop;
/**
* This indicates how many lines have been read since startScanning() was called.
**/
public long linesRead;
/**
* Set this true to indicate that the lineReceived() method should be called prior to parsing
* the line.
**/
public boolean preprocessLine;

private ewe.util.Vector dataParsers = new ewe.util.Vector();
private ewe.util.DataParser[] parsers;
//===================================================================
public long startScanning() throws IOException
//===================================================================
{
	if (dataParsers.size() == 0) throw new IOException("No formats given");
	parsers = new ewe.util.DataParser[dataParsers.size()];
	dataParsers.copyInto(parsers);
	if (stream == null && input == null) throw new IOException("No stream given");
	buffer = new byte[bufferSize];
	linesRead = 0;
	mainLoop();
	/*
	try{
		nativeMainLoop();
	}catch(UnsatisfiedLinkError e){
		mainLoop();
	}catch(Security
	*/
	return linesRead;
}
/**
* If preprocessLine is true, this method is called after each line is read.
 * @param buffer The bytes for the line.
 * @param lineStart The starting index of the bytes in the buffer.
 * @param lineLength The number of bytes in the line.
 * @return the integer ID of a format specification, or 0 to skip this line.
 * @exception IOException if parsing should stop.
*/
//-------------------------------------------------------------------
protected int lineReceived(byte[] buffer,int lineStart,int lineLength) throws IOException
//-------------------------------------------------------------------
{
	return 1;
}
/**
 * This method is called if there is an error parsing a line. It should throw an IOException
 * if it wishes parsing to stop. By default this does nothing.
 * @param e The exception caused by parsing the line.
 * @param format The format used for parsing.
 * @param buffer The bytes for the line.
 * @param lineStart The starting index of the bytes in the buffer.
 * @param lineLength The number of bytes in the line.
 * @exception IOException if parsing should stop.
 */
//-------------------------------------------------------------------
protected void parseError(Exception e,int format,byte[] buffer,int lineStart,int lineLength) throws IOException
//-------------------------------------------------------------------
{
	e.printStackTrace();
}
//-------------------------------------------------------------------
//private native void nativeMainLoop();
//-------------------------------------------------------------------
//-------------------------------------------------------------------
private void mainLoop() throws IOException
//-------------------------------------------------------------------
{
	int format = 1;
	while(readLine() && !shouldStop){
		linesRead++;
		if (preprocessLine) format = lineReceived(buffer,lineStart,lineLength);
		if (format == 0 || shouldStop || lineLength == 0) continue;
		try{
			lineParsed(format,parsers[format-1].parse(buffer,lineStart,lineLength));
		}catch(Exception e){
			parseError(e,format,buffer,lineStart,lineLength);
		}
	}
}

//-------------------------------------------------------------------
abstract
protected void lineParsed(int format,Object [] parsedValues) throws IOException;
//-------------------------------------------------------------------
/*
{
	if (true) return;
	String ret = "";
	for (int i = 0; i<parsedValues.length; i++){
		if (i != 0) ret += ", ";
		ret += parsedValues[i];
	}
	ewe.sys.Vm.debug(ret);
}
*/
byte [] buffer;
int totalRead = 0;

//===================================================================
public StreamScanner(BasicStream s,String format) throws IllegalArgumentException
//===================================================================
{
	stream = s;
	if (format != null) addFormat(format);
}
//===================================================================
public StreamScanner(InputStream s,String format) throws IllegalArgumentException
//===================================================================
{
	input = s;
	if (format != null) addFormat(format);
}

//===================================================================
public int addFormat(String format) throws IllegalArgumentException
//===================================================================
{
	dataParsers.add(new ewe.util.DataParser(format));
	return dataParsers.size();
}
//===================================================================
protected boolean readMore() throws IOException
//===================================================================
{
	int len = buffer.length;
	while (pos >= len) {
		byte [] nb = new byte[len+1024];
		ewe.sys.Vm.copyArray(buffer,0,nb,0,len);
		buffer = nb;
		len = nb.length;
	}
	int ret = 0;
	if (stream != null){
		while(true){
			ret = stream.nonBlockingRead(buffer,numRead,len-numRead);
			if (ret == 0) {
				if (waitTime >= 0) ewe.sys.mThread.nap(waitTime);
				continue;
			}else if (ret == -2){
				if (stream instanceof StreamObject) ((StreamObject)stream).throwIOException("Reading input.");
				else throw new IOException("Reading input.");
			}else
				break;
		}
	}else{
		ret = input.read(buffer,numRead,len-numRead);
	}
	if (ret == -1) return false; //No more to read.
	totalRead += ret;
	numRead += ret;
	return true;
}
private int lineStart, lineLength, pos, numRead;
/**
* This reads in a line of text. The line is terminated by a Line Feed (\n)
* or a Carriage Return (\r) or a CR followed by LF (\r\n). The terminating
* LF or CR is NOT returned with the string.
**/
//===================================================================
public boolean readLine() throws IOException
//===================================================================
{
	if (pos != 0){
		int did = pos-lineStart;
		ewe.sys.Vm.copyArray(buffer,pos,buffer,0,numRead-did);
		pos -= did;
		numRead -= did;
	}
	int start = pos;
	int len = 0;
	while (pos >= numRead) if (!readMore()) return false; //EOF has been reached.
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

	/*
	if (encoding == null) encoding = new String();
	String ret;
	if (encoding.equals(UTF8))
		ret = ewe.util.Utils.decodeJavaUtf8String(buffer,start,len);
	else
		ret = ewe.util.mString.fromAscii(buffer,start,len);
	ewe.sys.Vm.copyArray(buffer,pos,buffer,0,numRead-did);
	*/
	lineStart = start;
	lineLength = len;
	return true;
}
/*
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);

	if (!(new File("test.txt").exists())){
		StreamWriter sw = new StreamWriter(new FileOutputStream("test.txt",false));
		for (int i = 0; i<1000; i++)
			sw.println("Hello 123.456 67889"+i);
		sw.close();
	}
	if (false){
		StreamReader r = new StreamReader(new File("test.txt").toReadableStream());
		String line;
		int now = ewe.sys.Vm.getTimeStamp();
		ewe.util.SubString ss = new ewe.util.SubString();
		ewe.util.Vector v = new ewe.util.Vector();
		while ((line = r.readLine()) != null){
			ss.set(line).split(' ',v);
			//ewe.sys.Vm.debug(""+all.length);
			ewe.sys.Convert.toDouble(v.get(1).toString());
			ewe.sys.Convert.toInt(v.get(3).toString());
		}
		now = ewe.sys.Vm.getTimeStamp()-now;
		ewe.sys.Vm.debug("Done: "+now);
	}else{
		StreamScanner ss = new StreamScanner(new File("test.txt").toReadableStream(),"%s %q %f !i %i"){
		};
		int now = ewe.sys.Vm.getTimeStamp();
		ss.startScanning();
		now = ewe.sys.Vm.getTimeStamp()-now;
		ewe.sys.Vm.debug("Done: "+now);
	}
}
*/
//##################################################################
}
//##################################################################

