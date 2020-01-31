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
import ewe.io.File;

/**
* You can use this to explicitly change the options for FileChooser views. By default,
* a new FileChooser will have the same view/sort options as the previous one. However
* if you call <b>FileChooser.setOptions(FileChooserOptions)</b> after constructing it,
* you can set it to different values.
**/
//##################################################################
public class FileChooserOptions{
//##################################################################
public static final int VIEW_NAME_ONLY = 0x1;
public static final int VIEW_FILE_TIPS = 0x2;

public int viewOptions = VIEW_NAME_ONLY|VIEW_FILE_TIPS;
/**
* Use the File.LIST_BY options OR'ed with File.LIST_DESCENDING if necessary
* for this variable. A value of 0 implies sort by name.
**/
public int sortOptions = 0;
public String mask = "*.*";
//===================================================================
public void copyFrom(FileChooserOptions other)
//===================================================================
{
	if (other == null) return;
	viewOptions = other.viewOptions;
	sortOptions = other.sortOptions;
}
//##################################################################
}
//##################################################################

