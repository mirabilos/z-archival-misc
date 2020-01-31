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
* This is a write only output stream to a file.
**/
//##################################################################
public class FileOutputStream extends OutputStream{
//##################################################################
/**
* Create the FileOutputStream and overwrite any existing file.
* @param file The file to open.
* @exception IOException if the file could not be created or written to.
*/
//===================================================================
public FileOutputStream(File file) throws IOException
//===================================================================
{
	this(file,false);
}
/**
* Create the FileOutputStream with the option to append to an existing file.
* @param file The file to open/append.
* @param append specifies if the file should be appended.
* @exception IOException if the file could not be created or written to.
*/
//===================================================================
public FileOutputStream(File file,boolean append) throws IOException
//===================================================================
{
	set(file,append);
}
//-------------------------------------------------------------------
private void set(File file,boolean append) throws IOException
//-------------------------------------------------------------------
{
	RandomAccessStream ras = null;
	if (file.exists()){
		if (!append){
			if (!file.delete())
				throw new IOException("Could not overwrite file.");
		}else{
			ras = file.getRandomAccessStream(RandomAccessFile.WRITE_ONLY);
			if (ras == null) throw new IOException("Could not write to file!");
		}
	}
	if (ras == null)
		ras = file.getRandomAccessStream(RandomAccessFile.CREATE);
	if (ras != null) if (!ras.isOpen()) ras = null;
	if (ras != null) if (!ras.seek(ras.getLength())) ras = null;
	if (ras == null)
		throw new IOException("Could not write to file!");
	stream = ras;
}
/**
* Create the FileOutputStream and overwrite any existing file.
* @param name the name of the file.
* @param append specifies if the file should be appended.
* @exception IOException if the file could not be created or written to.
*/
//===================================================================
public FileOutputStream(String name,boolean append) throws IOException
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject();
	f.set(null,name);
	set(f,append);
}
/**
* Create the FileOutputStream and overwrite any existing file.
* @param name the name of the file.
* @exception IOException if the file could not be created or written to.
*/
//===================================================================
public FileOutputStream(String name) throws IOException
//===================================================================
{
	this(name,false);
}

//##################################################################
}
//##################################################################

