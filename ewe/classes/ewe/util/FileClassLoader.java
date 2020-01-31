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
package ewe.util;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.Stream;
/**
* This loads classes and resources from a source which has some kind of
* File model. i.e. a ewe.io.File object exists which represents the data
* source. This can be used to load classes from a Zip/Jar file as well by
* using a ZipEntryFile object as the file model.
**/
//##################################################################
public class FileClassLoader extends mClassLoader{
//##################################################################

protected File fileModel;

//===================================================================
public FileClassLoader(File fileModel)
//===================================================================
{
	this.fileModel = fileModel;
}
/**
* This is used to get a resource which may be dependant on how the class
* was loaded.
**/
//===================================================================
public Stream openResource(String resourceName)
//===================================================================
{
	try{
		return fileModel.getChild(resourceName).toReadableStream();
	}catch(IOException e){
		return null;
	}
}

//##################################################################
}
//##################################################################

