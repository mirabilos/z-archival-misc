/* java.util.zip.ZipFile
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of Jazzlib.

Jazzlib is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

Jazzlib is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License. */

package ewe.util.zip;
import ewe.io.*;
import ewe.util.*;
/*
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.NoSuchElementException;
*/
/**
 * This class represents a Zip archive.  You can ask for the contained
 * entries, or get an input stream for a file entry.  The entry is
 * automatically decompressed.
 *
 * This class is thread safe:  You can open input streams for arbitrary
 * entries in different threads.
 *
 * @author Jochen Hoenicke
 */
//##################################################################
public class ZipFile {
//##################################################################

public static final int OPEN_READ = 0;
public static final int OPEN_DELETE = 1;

protected ewe.zip.ZipFile zip;

protected ZipFile()
{}

protected String name;

//===================================================================
public ZipFile(String name) throws IOException, ZipException
//===================================================================
{
	this(ewe.sys.Vm.newFileObject().getNew(name),OPEN_READ);
}
//===================================================================
public ZipFile(ewe.io.File file) throws IOException, ZipException
//===================================================================
{
	this(file,OPEN_READ);
}

/**
 * Open a Zip file in a particular mode.
 * @param file The Zip formatted file.
 * @param mode Ignored! Only OPEN_READ is supported.
 */
//===================================================================
public ZipFile(ewe.io.File file,int mode) throws IOException, ZipException
//===================================================================
{
	this();
	name = file.getAbsolutePath();
	if (!file.canRead()) throw new IOException("Can't read file.");
	zip = new ewe.zip.ZipFile(file);
	if (!zip.isOpen()) throw new ZipException("Zip format error: "+zip.error);
}

//===================================================================
public void close() throws IOException
//===================================================================
{
	if (!zip.close()) throw new IOException("Could not close file!");
}

//===================================================================
public int size()
//===================================================================
{
	if (!zip.isOpen()) throw new IllegalStateException("Zip file not open.");
	return zip.size();
}

//===================================================================
public Enumeration entries()
//===================================================================
{
	if (!zip.isOpen()) throw new IllegalStateException("Zip file not open.");
	return new ZipEntryEnumerator(zip.entries());
}

//===================================================================
public ZipEntry getEntry(String name)
//===================================================================
{
	ewe.zip.ZipEntry got = zip.getEntry(name);
	if (got == null) return null;
	return new ewe.util.zip.ZipEntry(got);
}

//===================================================================
public InputStream getInputStream(ZipEntry ze) throws IOException, ZipException, IllegalStateException
//===================================================================
{
	if (!zip.isOpen()) throw new IllegalStateException("Zip file not open.");
	Stream s = zip.getInputStream(ze);
	if (s == null) throw new ZipException("Could not open stream.");
	return new InputStream(s);
}

//===================================================================
public String getName()
//===================================================================
{
	return name;
}
//##################################################################
}
//##################################################################

//##################################################################
class ZipEntryEnumerator extends ewe.util.IteratorEnumerator{
//##################################################################

Iterator it;

public ZipEntryEnumerator(Iterator it)
{
	this.it = it;
}

public boolean hasNext() {return it.hasNext();}

public Object next()
{
	ewe.zip.ZipEntry ze = (ewe.zip.ZipEntry)it.next();
	return new ewe.util.zip.ZipEntry(ze);
}
//##################################################################
}
//##################################################################



