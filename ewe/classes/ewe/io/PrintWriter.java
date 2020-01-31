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
import ewe.sys.Convert;
/**
* This class provides the same API as java.io.PrintWriter. None of its methods
* throws an Exception. You can check the error status of the Writer by calling
* the checkError() method.
**/
//##################################################################
public class PrintWriter extends Writer{
//##################################################################
/**
* The underlying writer.
**/
protected Writer writer;
/**
* Use this to provide your own line separator String. By default it is null which
* will translate to the underlying OS's line separator. You can set it to be any
* other string.
**/
public String lineSeparator;


private boolean autoFlush = false;
private boolean error = false;
/**
 * Create a new TextWriter to write to the specified Stream.
 * @param out The output Stream.
 */
//===================================================================
public PrintWriter(BasicStream out)
//===================================================================
{
	writer = new TextWriter(out);
	this.lock = writer.lock;
}
/**
 * Create a new TextWriter to write to the specified Stream.
 * @param out The output Stream.
* @param autoFlush if this is true a println() method will flush the output stream.
*/
//===================================================================
public PrintWriter(BasicStream out,boolean autoFlush)
//===================================================================
{
	this(out);
	this.autoFlush = autoFlush;
}
/**
 * Create a new TextWriter to write to the specified Stream.
 * @param out The OutputStream.
* @param autoFlush if this is true a println() method will flush the output stream.
 */
//===================================================================
public PrintWriter(OutputStream out,boolean autoFlush)
//===================================================================
{
	this(out);
	this.autoFlush = autoFlush;
}
/**
 * Create a new TextWriter to write to the specified Stream.
 * @param out The OutputStream.
 */
//===================================================================
public PrintWriter(OutputStream out)
//===================================================================
{
	writer = new TextWriter(out);
	this.lock = writer.lock;
}
//===================================================================
public PrintWriter(Writer writer)
//===================================================================
{
	this.writer = writer;
	this.lock = writer.lock;
}
//===================================================================
public PrintWriter(Writer writer,boolean autoFlush)
//===================================================================
{
	this(writer);
	this.autoFlush = autoFlush;
}
/**
 * Write out a number of characters.
 * @param chars The characters to be writen.
 * @param offset The location in the chars to start writing from.
 * @param length The number of characters to write.
 */
//===================================================================
public void write(char[] chars, int offset, int length)
//===================================================================
{
	try{
		writer.write(chars,offset,length);
	}catch(IOException e){
		setError();
	}
}
/**
 * Write out a number of characters.
 * @param chars The characters to be writen.
 * @param offset The location in the chars to start writing from.
 * @param length The number of characters to write.
 */
//===================================================================
public void write(char[] chars)
//===================================================================
{
	write(chars,0,chars.length);
}
//===================================================================
public void write(String str)
//===================================================================
{
	if (str == null) write("null");
	else write(ewe.sys.Vm.getStringChars(str),0,str.length());
}
//===================================================================
public void write(int aChar)
//===================================================================
{
	try{
		super.write(aChar);
	}catch(IOException e){
		setError();
	}
}
//===================================================================
public void write(String str,int offset,int length)
//===================================================================
{
	if (str == null) write("null");
	else write(ewe.sys.Vm.getStringChars(str),offset,length);
}
//===================================================================
public void flush()
//===================================================================
{
	try{
		writer.flush();
	}catch(IOException e){
		setError();
	}
}
//===================================================================
public void close()
//===================================================================
{
	try{
		writer.close();
	}catch(IOException e){
		setError();
	}
}
/**
 * Output a line-feed (end of line) character.
 */
//===================================================================
public void println()
//===================================================================
{
	if (lineSeparator == null) write("\n",0,1);
	else write(lineSeparator);
	if (autoFlush) flush();
}
//-------------------------------------------------------------------
protected void setError()
//-------------------------------------------------------------------
{
	error = true;
}
/**
 * Flush the stream and check its error state.
 * @return true if the stream encountered an error in writing or encoding. False otherwise.
 */
//===================================================================
public boolean checkError()
//===================================================================
{
	if (error) return true;
	flush();
	return error;
}
//===================================================================
public void print(char[] chars)
//===================================================================
{
	write(chars);
}
//===================================================================
public void print(String str)
//===================================================================
{
	write(str);
}
//===================================================================
public void print(int value)
//===================================================================
{
	print(Convert.toString(value));
}
//===================================================================
public void print(boolean value)
//===================================================================
{
	print(Convert.toString(value));
}
//===================================================================
public void print(char value)
//===================================================================
{
	print(Convert.toString(value));
}
//===================================================================
public void print(float value)
//===================================================================
{
	print(Convert.toString(value));
}
//===================================================================
public void print(double value)
//===================================================================
{
	print(Convert.toString(value));
}
//===================================================================
public void print(long value)
//===================================================================
{
	print(Convert.toString(value));
}
//===================================================================
public void print(Object value)
//===================================================================
{
	print(value == null ? "null" : value.toString());
}
//===================================================================
public void println(char[] chars)
//===================================================================
{
	write(chars);
	println();
}
//===================================================================
public void println(String str)
//===================================================================
{
	write(str);
	println();
}
//===================================================================
public void println(int value)
//===================================================================
{
	print(value);
	println();
}
//===================================================================
public void println(boolean value)
//===================================================================
{
	print(value);
	println();
}
//===================================================================
public void println(char value)
//===================================================================
{
	print(value);
	println();
}
//===================================================================
public void println(float value)
//===================================================================
{
	print(value);
	println();
}
//===================================================================
public void println(double value)
//===================================================================
{
	print(value);
	println();
}
//===================================================================
public void println(long value)
//===================================================================
{
	print(value);
	println();
}
//===================================================================
public void println(Object value)
//===================================================================
{
	print(value);
	println();
}

//##################################################################
}
//##################################################################

