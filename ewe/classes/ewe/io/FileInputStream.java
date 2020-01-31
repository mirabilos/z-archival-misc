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
* This is a read only input stream from a file.
**/
//##################################################################
public class FileInputStream extends InputStream{
//##################################################################
/**
* Create the FileInputStream from a specified file.
* @param file The File to open.
* @exception FileNotFoundException if the file does not exist or could not be open for reading.
*/
//===================================================================
public FileInputStream(File file) throws FileNotFoundException
//===================================================================
{
	set(file);
}

//-------------------------------------------------------------------
private void set(File file) throws FileNotFoundException
//-------------------------------------------------------------------
{
	RandomAccessStream ras = file.getRandomAccessStream(RandomAccessFile.READ_ONLY);
	if (ras != null) if (!ras.isOpen()) ras = null;
	if (ras == null)
		throw new FileNotFoundException(file.toString());
	ras.seek(0);
	stream = ras;
}

/**
 * Create the FileInputStream for a specified file name.
 * @param name The name of the file.
 * @exception FileNotFoundException if the file does not exist or could not be open for reading.
 */
//===================================================================
public FileInputStream(String name) throws FileNotFoundException
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject();
	f.set(null,name);
	set(f);
}
//##################################################################
}
//##################################################################

