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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.File;
import ewe.sys.Time;
import ewe.sys.Locale;
import ewe.data.*;

//##################################################################
public class FileClipboard{
//##################################################################

public static interface FileClipboardSource {
	public static final int FILES_TAKEN = 1;
	public static final int FILES_REJECTED = 2;
	public boolean fileClipboardOperation(FileClipboard clip,int type);
}

public File sourceDir;
public String sourceFiles[];
public boolean isCut;
FileClipboardSource source;

//===================================================================
public void set(File sourceDir,String sourceFiles [],boolean isCut,FileClipboardSource source)
//===================================================================
{
	clear();
	this.sourceDir = sourceDir;
	this.sourceFiles = sourceFiles;
	this.isCut = isCut;
	this.source = source;
}
//===================================================================
public boolean isCut(File parent,String file)
//===================================================================
{
	if (!isCut || sourceDir == null) return false;
	if (!sourceDir.equals(parent)) return false;
	for (int i = 0; i<sourceFiles.length; i++)
		if (sourceFiles[i].equals(file)) return true;
	return false;
}
//===================================================================
public boolean isACutSubFolder(File who)
//===================================================================
{
	if (!isCut || sourceDir == null) return false;
	for (File f = who; f != null;){
		File p = f.getParentFile();
		if (sourceDir.equals(p)){
			String fe = f.getFileExt();
			for (int i = 0; i<sourceFiles.length; i++)
				if (sourceFiles[i].equals(fe))
					return true;
			return false;
		}
		f = p;
	}
	return false;
}
//===================================================================
public boolean hasFiles()
//===================================================================
{
	return sourceDir != null;
}
//===================================================================
public void taken() {clear(FileClipboardSource.FILES_TAKEN);}
//===================================================================
//===================================================================
public void clear() {clear(FileClipboardSource.FILES_REJECTED);}
//===================================================================
//-------------------------------------------------------------------
protected void clear(int message)
//-------------------------------------------------------------------
{
	FileClipboardSource s = source;
	if (isCut && sourceDir != null) sourceDir.refresh();
	boolean wasCut = isCut;
	source = null;
	sourceDir = null;
	sourceFiles = null;
	isCut = false;
	if (s != null && wasCut)
		s.fileClipboardOperation(this,message);
}
public static FileClipboard clipboard = new FileClipboard();

//===================================================================
public boolean canPasteInto(File folder)
//===================================================================
{
	if (sourceDir == null || folder == null) return false;
	if (!folder.isDirectory()) return false;
	if (!isCut) return true;
	if (sourceDir.equals(folder)) return false;
	if (isACutSubFolder(folder)) return false;
	return true;
}
//##################################################################
}
//##################################################################

