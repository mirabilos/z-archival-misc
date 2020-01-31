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
import ewe.fx.*;
import ewe.ui.*;

//##################################################################
public class FileChooserLink{
//##################################################################
public IImage icon;
public String name;
public String path;
//===================================================================
public FileChooserLink(String name,String path,IImage icon)
//===================================================================
{
	this.name = name;
	this.path = path;
	this.icon = icon == null ? ewe.io.File.getIcon(ewe.io.File.ClosedFolderIcon) : icon;
}
//===================================================================
public FileChooserLink(String aPath,File fileModel)
//===================================================================
{
	if (fileModel == null) fileModel = File.getNewFile();
	File f = fileModel.getNew(aPath);
	File par = f.getParentFile();
	this.path = aPath;
	this.name = f.getName();
	this.icon = ewe.io.File.getIcon(ewe.io.File.ClosedFolderIcon);
	if (par == null && aPath.indexOf(':') != -1) this.icon = ewe.io.File.getIcon(File.DriveIcon);
}
//===================================================================
public MenuItem addToMenu(ChoiceControl m,FontMetrics fm)
//===================================================================
{
	MenuItem mi = new MenuItem(name);//m.addItem(name);
	mi.image = new IconAndText(icon,name,fm).setColor(null,null);
	mi.data = this;
	m.addItem(mi);
	return mi;
}
//===================================================================
public String toString()
//===================================================================
{
	return path;
}
/**
* This checks for equality of Paths. The object can be another link, or a file or a path string.
**/
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (other == null) return false;
	other = other.toString();
	if (other == null) return false;
	return path.equals(other);
}
//===================================================================
public File toFile(File fileSystem)
//===================================================================
{
	if (fileSystem == null) return File.getNewFile(path);
	return fileSystem.getNew(path);
}
//##################################################################
}
//##################################################################

