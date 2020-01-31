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

//##################################################################
public class ZipOutputStream extends OutputStream{
//##################################################################

ewe.zip.ZipOutputStream zof;

//===================================================================
public ZipOutputStream(BasicStream out)
//===================================================================
{
	stream = zof = new ewe.zip.ZipOutputStream(out);
	zof = (ewe.zip.ZipOutputStream)stream;
}

//===================================================================
public ZipOutputStream(OutputStream out)
//===================================================================
{
	this(new StreamAdapter(out));
}

//===================================================================
public void closeEntry() throws IOException
//===================================================================
{
	if (!zof.closeEntry()) throw new IOException(zof.error);
}

//===================================================================
public void finish() throws IOException
//===================================================================
{
	if (!zof.finish()) throw new IOException(zof.error);
}

//===================================================================
public void putNextEntry(ZipEntry entry) throws IOException
//===================================================================
{
	if (!zof.putNextEntry(entry)) throw new IOException(zof.error);
}

//===================================================================
public void setLevel(int level)
//===================================================================
{
	zof.setLevel(level);
}
//===================================================================
public void setMethod(int method)
//===================================================================
{
	zof.setMethod(method);
}
//===================================================================
public void setComment(String comment)
//===================================================================
{
	zof.setComment(comment);
}
//##################################################################
}
//##################################################################
