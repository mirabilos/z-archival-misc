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
import ewe.fx.*;
import ewe.sys.Time;
import ewe.sys.Locale;

/**
* A FileComparer compares two File objects according to criteria which include:
* name, length, type and modification date.
**/

//##################################################################
public class FileComparer implements Comparer{
//##################################################################
public static char ForwardSlash = '/', BackSlash = '\\', DriveSeparator = ':';
public static char MySeparator = ForwardSlash; // or File.separatorChar;

public static final int IGNORE_CASE = Locale.IGNORE_CASE;

protected String mask;
protected File parent;
protected int options;
protected File check1, check2;
protected Locale locale;
protected Time time = new Time();


/**
 * @param parent The parent file for the files to be compared.
 * @param locale a Locale to use for comparisons.
 * @param options This should be the File.LIST_XXX options.
 * @param mask An optional mask for comparing a File with a mask.
 * @return
 */
//===================================================================
public FileComparer(File parent,Locale locale,int options,String mask)
//===================================================================
{
	this.parent = parent;
	check1 = parent.getNew(null,"");
	check2 = parent.getNew(null,"");
	this.options = options;
	this.locale = locale;
	setMask(mask);
}
//===================================================================
public void setMask(String mask)
//===================================================================
{
	this.mask = mask;
	if (mask != null)
		if (mask./*trim().*/length() == 0) mask = null;
	if (mask == null) mask = "*.*";
	splitIt(mask);
}
//===================================================================
public int compare(Object one,Object two)
//===================================================================
{
	File c1 = check1, c2 = check2;

	if (one instanceof File) {
		c1 = (File)one;
		one = c1.getName();
	}
	else check1.set(parent,(String)one);

	if (two instanceof File) {
		c2 = (File)two;
		two = c2.getName();
	}
	else check2.set(parent,(String)two);

	if ((options & File.LIST_IGNORE_DIRECTORY_STATUS) == 0) {
		if (c1.isDirectory()){
			if (!c2.isDirectory()) return ((options & File.LIST_DIRECTORIES_LAST) == 0) ? -1 : 1;
		}else if (c2.isDirectory()) return ((options & File.LIST_DIRECTORIES_LAST) == 0) ? 1 : -1;
	}

	if ((options & File.LIST_DONT_SORT) != 0) return 0;
	if ((options & File.LIST_BY_SIZE) != 0){
		long l1 = c1.getLength(), l2 = c2.getLength();
		if (l1 > l2) return 1;
		else if (l1 < l2) return -1;
	}else if ((options & File.LIST_BY_DATE) != 0){
		long l1 = c1.getModified(time).getTime(), l2 = c2.getModified(time).getTime();
		if (l1 > l2) return 1;
		else if (l1 < l2) return -1;
	}else if ((options & File.LIST_BY_TYPE) != 0){
		String o = "", t = o;
		int idx = ((String)one).lastIndexOf('.');
		if (idx != -1) o = ((String)one).substring(idx+1);
		idx = ((String)two).lastIndexOf('.');
		if (idx != -1) t= ((String)two).substring(idx+1);
		int ret = locale.compare(o,t,Locale.IGNORE_CASE);
		if (ret != 0) return ret;
	}
	return locale.compare((String)one,(String)two,Locale.IGNORE_CASE);
}


public boolean caseSensitive = false;
public boolean letAll = false;

//============================================================
boolean wordMatches(String what,String prefix,boolean isFullName)
//============================================================
{
	if (letAll) return true;
	String w = what, p = prefix;
	if (isFullName) return SubString.equals(ewe.sys.Vm.getStringChars(w),0,w.length(),ewe.sys.Vm.getStringChars(p),0,p.length(),caseSensitive ? 0 : SubString.IGNORE_CASE);
	else if (p.length() == 0) return true;
	else return SubString.equals(ewe.sys.Vm.getStringChars(w),0,w.length(),ewe.sys.Vm.getStringChars(p),0,p.length(),(caseSensitive ? 0 : SubString.IGNORE_CASE)|SubString.STARTS_WITH);
}

//-------------------------------------------------------------------
protected static int findLastSeparatorDrive(String full)
//-------------------------------------------------------------------
{
	int where = findLastSeparator(full);
	if (where != -1) return where;
	return full.lastIndexOf(DriveSeparator);
}
//===================================================================
public static int findLastSeparator(String full)
//===================================================================
{
	if (full == null) return -1;
	int end = full.lastIndexOf(ForwardSlash);//(File.separator);
	int end2 = full.lastIndexOf(BackSlash);
	if (end2 > end) end = end2;
	if (end == -1) end = full.lastIndexOf(DriveSeparator);
	return end;
}
//===================================================================
public static String getExt(String full)
//===================================================================
{
	String s = getFileExt(full);
	int last = s.lastIndexOf('.');
	if (last == -1 || last == 0) return ".";
	return s.substring(last);
}
//===================================================================
public static String getFile(String full)
//===================================================================
{
	String s = getFileExt(full);
	int last = s.lastIndexOf('.');
	if (last == -1 || last == 0) return s;
	return s.substring(0,last);
}
//============================================================
public boolean matches(String fileName)
//============================================================
{
	if (fileName == null) return false;
	//System.out.println("Testing: "+fileName);
	String f = getFile(fileName);
	if (!wordMatches(f,filePrefix,fileIsFull))return false;
	f = getExt(fileName);
	if (!wordMatches(f,extPrefix,extIsFull))return false;
	return true;
}

//============================================================
public boolean accept(ewe.io.File dir,Object nameOrFile)
//============================================================
{
	File c = check1;
	if (nameOrFile instanceof File) c = (File)nameOrFile;
	else check1.set(dir,(String)nameOrFile);

	if (c.isDirectory()){
		if ((options & File.LIST_FILES_ONLY) != 0) return false;
		if ((options & File.LIST_ALWAYS_INCLUDE_DIRECTORIES) != 0) return true;
	}else{
		if ((options & File.LIST_DIRECTORIES_ONLY) != 0) return false;
	}
	if (nameOrFile instanceof File) return matches(((File)nameOrFile).getName());
	return matches((String)nameOrFile);
}
public static String getDrivePath(String full)
{
	int last = findLastSeparatorDrive(full);
	if (last == -1) return "";
	return full.substring(0,last+1);
}
public static String getFileExt(String full)
{
	int last = findLastSeparatorDrive(full);
	return full.substring(last+1);
}

String filePrefix,extPrefix;
boolean fileIsFull,extIsFull;

void splitIt(String mask)
{
	String f = getFile(mask);
	int i = f.indexOf('*');
	filePrefix = f;
	fileIsFull = true;
	if (i != -1) {
		filePrefix = f.substring(0,i);
		fileIsFull = false;
	}
	f = getExt(mask);
	i = f.indexOf('*');
	extPrefix = f;
	extIsFull = true;
	if (i != -1) {
		extPrefix = f.substring(0,i);
		extIsFull = false;
	}
	//System.out.println("<"+filePrefix+"><"+extPrefix+">");
}


//##################################################################
}
//##################################################################

