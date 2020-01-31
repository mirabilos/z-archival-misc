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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.io;
import ewe.sys.Time;
import ewe.sys.Handle;
/**
* This is the class that you should use when implementing a new File object. A fully implemented
* File object/system can be used by a FileChooser for browsing and manipulation.
**/
//##################################################################
public abstract class FileAdapter extends File{
//##################################################################


//-------------------------------------------------------------------
protected void setFullPathName(File parent,String file)
//-------------------------------------------------------------------
{
	name = makePath(parent,file);
}
/**
* This is used to tell the File to represent a different File entity. By default all it does
* is call setFullPathName() so that the protected name variable represents the full path name.
* If your File representation has more state than just the full path name, then override this
* method to setup the new state.
**/
//===================================================================
public void set(File parent,String file)
//===================================================================
{
	setFullPathName(parent,file);
}

public abstract boolean exists();
public abstract boolean isDirectory();
public abstract int getLength();
public boolean createDir() {return false;}
public boolean delete() {return false;}
public boolean move(File dest) {return false;}

//-------------------------------------------------------------------
protected File getNewInstance()
//-------------------------------------------------------------------
{
	throw new RuntimeException("You must override getNewInstance() or getNew()");
}

//===================================================================
public RandomAccessStream toRandomAccessStream(String mode) throws IOException
//===================================================================
{
	throw new IOException("Cannot create a RandomAccessStream for a file of type: "+getClass().getName());
}
//-------------------------------------------------------------------
protected FileAdapter (){}
//-------------------------------------------------------------------

//===================================================================
public Object getInfo(int value,Object source,Object resultDestination,int options)
//===================================================================
{
	switch(value){
			case INFO_DEVICE_NAME:
				return "Mobile Device";
			case INFO_FLAGS:
				if (!(resultDestination instanceof ewe.sys.Long))
					resultDestination = new ewe.sys.Long();
				((ewe.sys.Long)resultDestination).value = FLAG_SLOW_ACCESS|FLAG_SLOW_CHILD_COUNT;
				return resultDestination;
	}
	return null;
}
//===================================================================
public Handle setInfo(int infoCode,Object sourceParameters,int options)
//===================================================================
{
	return new Handle(Handle.Failed,null);
}

//-------------------------------------------------------------------
protected  void getSetModified(Time time,boolean doGet)
//-------------------------------------------------------------------
{
	if (doGet) time.setTime(new Time().getTime());
}
//-------------------------------------------------------------------
protected int getSetPermissionsAndFlags(boolean isGet, int valuesToSetOrGet, int valuesToClear) throws IOException, IllegalArgumentException
//-------------------------------------------------------------------
{
	return 0;
}

//===================================================================
public String getFullPath() {return name;}
//===================================================================
/**
 * List files asynchronously. NOTE: version 1.16 of Ewe does not list the files
 * asynchronously - it is synchronous. Therefore it always returns a Handle that
 * is completed.<p>
	By default this method calls the doList() method and then returns a completed Handle
	that either indicates success or failure. Methods that inherit from FileBase are encouraged
	to provide a better version of this (preferrable one that spawns a thread).
 * @param mask The a file maks using '*' characters.
 * @param listAndSortOptions LIST_ and SORT_ options ORed together.
 * @return A Handle which can be used to monitor the progress of the operation.
 */
//===================================================================
public Handle listFiles(String mask,int listAndSortOptions)
//===================================================================
{
	String [] got = doList(mask,listAndSortOptions);
	if (got == null) return new Handle(Handle.Failed,null);
	else return new Handle(Handle.Succeeded,got);
}

//-------------------------------------------------------------------
protected String [] doList(String mask,int listAndSortOptions)
//-------------------------------------------------------------------
{
	ewe.util.Vector v = new ewe.util.Vector();
	ewe.util.FileComparer fc = new ewe.util.FileComparer(this,ewe.sys.Vm.getLocale(),listAndSortOptions,mask);
	int got = startFind(mask);
	if (got == 0) return null;
	try{
		while(true){
			Object s = findNext(got);
			//ewe.sys.Vm.debug(s);
			if (s == null) break;
			if (!fc.accept(this,s)) continue;
			v.add(s);
			if ((listAndSortOptions & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) != 0) break;
		}
	}finally{
		endFind(got);
	}
	if ((listAndSortOptions & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) != 0)
		if (v.size() == 0) return null;
	else
		return new String[0];
	Object [] ret = new Object[v.size()];
	v.copyInto(ret);
	ewe.util.Utils.sort(ret,fc,((listAndSortOptions & LIST_DESCENDING) != 0));
	String [] r2 = new String[ret.length];
	for (int i = 0; i<ret.length; i++)
		if (ret[i] instanceof File) r2[i] = ((File)ret[i]).getName();
		else r2[i] = (String)ret[i];
	return r2;
}
//-------------------------------------------------------------------
/**
* This is used as the start point for listing. The method should begin the find operation
* and return a unique ID for the find operation.
**/
protected abstract int startFind(String mask);
/**
* This will find the next file in sequence given the search ID returned by startFind(). It can
* either return a File Object or the name of the file.
**/
protected abstract Object findNext(int search);
/**
* This tells the File to free all resources associated with the find given the search ID returned by
* startFind().
**/
protected abstract void endFind(int search);
//-------------------------------------------------------------------


//##################################################################
}
//##################################################################

