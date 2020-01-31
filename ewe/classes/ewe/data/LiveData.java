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
package ewe.data;
import ewe.util.*;
/**
* A LiveData is a more complex version of a DataUnit. In addition to the
* functionality of a DataUnit, a LiveData can also:<br>
* <pre>
* 1. Provide a visual means of editing its data.
* 2. Encode and Decode its internal data as a text String.
* 3. Provide a Name for itself (for displaying in a list).
* 4. Provide an icon (IImage object) if one is associated with it.
*</pre>
**/
//##################################################################
public interface LiveData extends DataUnit,ewe.ui.FieldListener{
//##################################################################
/**
* Return a ewe.ui.Editor Object which can be used to visually edit
* the information in this LiveData object.
**/
public ewe.ui.Editor getEditor(int options);
/**
* Encode this Object as a string.
**/
public String textEncode();
/**
* Decode this Object for a String previously encoded using textEncode()
**/
public void textDecode(String from);
/**
* Return a String representing a name for this object (for the purpose of displaying in
* lists, etc).
**/
public String getName();
/**
* Return an Icon for this object (if one exists) or null if no icon exists.
**/
public ewe.fx.IImage getIcon();
//##################################################################
}
//##################################################################

