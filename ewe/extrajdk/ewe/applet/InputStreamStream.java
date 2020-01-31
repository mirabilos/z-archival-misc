/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
 *  MERCHANTABILITYTY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.applet;

/*
 * Note: Everything that calls ewe code in these classes must be
 * synchronized with respect to the Applet uiLock object to allow ewe
 * programs to be single threaded. This is because of the multi-threaded
 * nature of Java and because timers use multiple threads.
 *
 * Because all calls into ewe are synchronized and users can't call this code,
 * they can't deadlock the program in any way. If we moved the synchronization
 * into ewe code, we would have the possibility of deadlock.
 */

import ewe.ui.*;
import java.util.Vector;
import java.io.*;

//##################################################################
public class InputStreamStream extends ewe.io.StreamObject{
//##################################################################

java.io.InputStream input;
//===================================================================
public InputStreamStream(java.io.InputStream ins)
//===================================================================
{
	input = new BufferedInputStream(ins,1024*10);
}

//===================================================================
public boolean isOpen() {return input != null;}
//===================================================================
public boolean closeStream() throws ewe.io.IOException
//===================================================================
{
	if (input == null) return true;
	try{
		input.close();
		input = null;
		return true;
	}catch(Exception e){
		throw new ewe.io.IOException(e.getMessage());
	}
}
//-------------------------------------------------------------------
public int nonBlockingRead(byte [] dest,int start,int count)
//-------------------------------------------------------------------
{
	try{
		int read = input.read(dest,start,count);
		return read;
	}catch(Exception e){
		return -2;
	}
}
//-------------------------------------------------------------------
public int nonBlockingWrite(byte [] dest,int start,int count)
//-------------------------------------------------------------------
{
	return -2;
}
//===================================================================
public boolean flushStream() throws ewe.io.IOException
//===================================================================
{
	return true;
}
//##################################################################
}
//##################################################################
