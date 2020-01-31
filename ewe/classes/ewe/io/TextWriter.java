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
* A TextWriter is used for writing Unicode text files that may be encoded as bytes in any
* format. It uses a TextCodec for encoding the characters into bytes to be sent to the output Stream and so you must
* provide the correct Codec for the file. The only Codecs provided by the Ewe library are
* the JavaUtf8Codec (which will encode ANY Unicode text) and AsciiCodec (which only encodes
* Unicode values from 0 to 255 as their exact byte values).
* <p>
* By default, a JavaUtf8Codec is used for encoding.
**/

//##################################################################
public class TextWriter extends Writer{
//##################################################################
/**
* This is the underlying stream, or the "output" OutputStream may be used instead.
**/
protected Stream stream;
/**
* This is the underlying stream, or the "stream" Stream may be used instead.
**/
protected OutputStream output;
/**
* This indicates that the close() method has already been called.
**/
protected boolean closed = false;

/**
* Flush any waiting data to the underlying output Stream. Flush the output Stream as well.
**/
//===================================================================
public void flush() throws IOException
//===================================================================
{
	if (stream != null) stream.flush();
	else output.flush();
}
/**
 * Create a new TextWriter to write to the specified Stream.
 * @param out The output Stream.
 */
//===================================================================
public TextWriter(BasicStream out)
//===================================================================
{
	if (out instanceof Stream) stream = (Stream)out;
	else stream = new StreamAdapter(out);
}
/**
 * Create a new TextWriter to write to the specified Stream.
 * @param out The OutputStream.
 */
//===================================================================
public TextWriter(OutputStream out)
//===================================================================
{
	output = out;
}

/**
 * Create a new TextWriter to write to the specified file name.
 * @param path The path of the file.
 * @param append true to append to an existing file, false to overwrite.
 * @exception ewe.io.IOException if the file could not be opened for writing.
 */
//===================================================================
public TextWriter(String path,boolean append) throws ewe.io.IOException
//===================================================================
{
	this(ewe.sys.Vm.newFileObject().getNew(path).toWritableStream(append));
}

/**
* This is the codec used for encoding outgoing data characters into bytes. By default
* it is a JavaUtf8Codec.
**/
public TextCodec codec = new JavaUtf8Codec();

private ewe.util.ByteArray outBytes = null;
/**
 * Write out a number of characters.
 * @param chars The characters to be writen.
 * @param offset The location in the chars to start writing from.
 * @param length The number of characters to write.
 * @exception IOException If there is an error writing the data.
 */
//===================================================================
public void write(char[] chars, int offset, int length) throws IOException
//===================================================================
{
	if (closed) throw new IOException("Stream closed!");
	if (length < 1) return;
	outBytes = codec.encodeText(chars,offset,length,false,outBytes);
	if (outBytes.length > 0){
		if (stream != null) stream.write(outBytes.data,0,outBytes.length);
		else output.write(outBytes.data,0,outBytes.length);
	}
}
/**
 * Write out a String of text.
 * @param text The String to write out.
 * @exception IOException If there is an error writing the data.
 */
//===================================================================
public void print(String text) throws IOException
//===================================================================
{
	write(ewe.sys.Vm.getStringChars(text),0,text.length());
}
/**
 * Write out a String of text followed by a line-feed..
 * @param text The String to write out.
 * @exception IOException If there is an error writing the data.
 */
//===================================================================
public void println(String text) throws IOException
//===================================================================
{
	print(text);
	println();
}
/**
 * Output a line-feed (end of line) character.
 * @exception IOException If there is an error writing the data.
 */
//===================================================================
public void println() throws IOException
//===================================================================
{
	print("\n");
}
/**
 * Close the TextWriter and the underlying Stream.
 * @exception IOException on error.
 */
//===================================================================
public void close() throws IOException
//===================================================================
{
	if (closed) return;
	outBytes = codec.encodeText(null,0,0,true,outBytes);
	if (outBytes.length > 0){
		if (stream != null) stream.write(outBytes.data,0,outBytes.length);
		else output.write(outBytes.data,0,outBytes.length);
	}
	flush();
	if (stream != null) stream.close();
	else output.close();
	closed = true;
}
/**
 * Write an entire string to a stream. The stream
 * will be closed after.
 * @param basicStreamOrOutputStream This must be a BasicStream or an OutputStream object.
 * @param codec The codec to use or null for the default Java UTF8 codec.
 * @exception IOException if an error occured during writing or encoding.
 */
//===================================================================
public static void writeAll(String text, Object basicStreamOrOutputStream, TextCodec codec) throws IOException
//===================================================================
{
	TextWriter tw = null;
	try{
		tw = (basicStreamOrOutputStream instanceof BasicStream) ?
			new TextWriter((BasicStream)basicStreamOrOutputStream) : new TextWriter((OutputStream)basicStreamOrOutputStream);
		if (codec != null) tw.codec = codec;
		tw.print(text);
	}finally{
		try{
			tw.close();
		}catch(Exception e){}
	}
}

//##################################################################
}
//##################################################################

