/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewex.rapi;
import ewe.io.File;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.Time;
import ewe.sys.Locale;

//##################################################################
class FileComparer implements Comparer{
//##################################################################
public static char ForwardSlash = '/', BackSlash = '\\', DriveSeparator = ':';
public static char MySeparator = ForwardSlash; // or File.separatorChar;

public static final int IGNORE_CASE = Locale.IGNORE_CASE;

String mask;
RapiFile parent;
int options;
File check1 = new RapiFile(null,""), check2 = new RapiFile(null,"");
Locale locale;
Time time = new Time();
//===================================================================
public FileComparer(RapiFile parent,Locale locale,int options,String mask)
//===================================================================
{
	this.parent = parent;
	this.options = options;
	this.locale = locale;
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
	check1.set(parent,(String)one);
	check2.set(parent,(String)two);

	if ((options & File.LIST_IGNORE_DIRECTORY_STATUS) == 0) {
		if (check1.isDirectory()){
			if (!check2.isDirectory()) return ((options & File.LIST_DIRECTORIES_LAST) == 0) ? -1 : 1;
		}else if (check2.isDirectory()) return ((options & File.LIST_DIRECTORIES_LAST) == 0) ? 1 : -1;
	}

	if ((options & File.LIST_DONT_SORT) != 0) return 0;
	if ((options & File.LIST_BY_SIZE) != 0){
		long l1 = check1.getLength(), l2 = check2.getLength();
		if (l1 > l2) return 1;
		else if (l1 < l2) return -1;
	}else if ((options & File.LIST_BY_DATE) != 0){
		long l1 = check1.getModified(time).getTime(), l2 = check2.getModified(time).getTime();
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
	if (letAll || true) return true;
	String w = what, p = prefix;
	if (!caseSensitive) {
		w = w.toUpperCase();
		p = p.toUpperCase();
	}
	if (isFullName) return w.equals(p);
	else if (p.length() == 0) return true;
	else return w.startsWith(p);
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
	//System.out.println("Testing: "+fileName);
	String f = getFile(fileName);
	if (!wordMatches(f,filePrefix,fileIsFull))return false;
	f = getExt(fileName);
	if (!wordMatches(f,extPrefix,extIsFull))return false;
	return true;
}

//============================================================
public boolean accept(ewe.io.File dir,String name)
//============================================================
{
	check1.set(dir,name);
	if (check1.isDirectory()){
		if ((options & File.LIST_FILES_ONLY) != 0) return false;
		if ((options & File.LIST_ALWAYS_INCLUDE_DIRECTORIES) != 0) return true;
	}else{
		if ((options & File.LIST_DIRECTORIES_ONLY) != 0) return false;
	}
	return matches(name);
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

