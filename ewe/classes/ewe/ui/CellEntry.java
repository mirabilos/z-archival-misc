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
package ewe.ui;
import ewe.fx.*;
import ewe.util.*;
/**
* This is no longer used in Ewe.
**/
//##################################################################
public class CellEntry implements CellConstants{
//##################################################################
//public Control control;
//public CellPanel panel;
public int constraints = STRETCH|FILL|CENTER;
protected TagList tags = null;
//===================================================================
//===================================================================
public CellEntry setControl(int val) {constraints = (constraints & ~CONTROLMASK)|(val & CONTROLMASK); return this;}
public CellEntry setCell(int val) {constraints = (constraints & ~CELLMASK)|(val & CELLMASK); return this;}
//===================================================================
public CellEntry set(int tag,Object value)
//===================================================================
{
	if (tags == null) tags = new TagList();
	tags.set(tag,value);
	return this;
}
//===================================================================
public Object get(int tag,Object defaultValue)
//===================================================================
{
	if (tags == null) return defaultValue;
	return tags.getValue(tag,defaultValue);
}
//===================================================================
public CellEntry clear(int tag)
//===================================================================
{
	if (tags != null) tags.clear(tag);
	return this;
}
//===================================================================
public CellEntry defaultTo(int tag,Object value)
//===================================================================
{
	if (tags == null) tags = new TagList();
	tags.defaultTo(tag,value);
	return this;
}
//##################################################################
}
//##################################################################

