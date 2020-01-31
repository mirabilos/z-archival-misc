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
import ewe.sys.Convert;
/**
* An IntChoice is a mChoice that treats setInt() and getInt() differently. Normally getInt()
* on an mChoice will return the chosen index. However getInt() on an IntChoice will convert
* the String at the chosen index to an integer value and return it. The same goes for setInt().
**/
//##################################################################
public class IntChoice extends mChoice{
//##################################################################
{
	modify(PreferredSizeOnly,0);
}
//===================================================================
public static String [] intToStrings(int [] values)
//===================================================================
{
	String [] s = new String[values.length];
	for (int i = 0; i<s.length; i++) s[i] = Convert.toString(values[i]);
	return s;
}
//===================================================================
public IntChoice(int [] values,int initialValue)
//===================================================================
{
	this(intToStrings(values),0);
}
//===================================================================
public IntChoice(String [] values,int initialValue)
//===================================================================
{
	super(values,0);
	setInt(initialValue);
}
//===================================================================
public IntChoice(String values,int initialValue)
//===================================================================
{
	super(values,0);
	setInt(initialValue);
}
//===================================================================
public void setInt(int value)
//===================================================================
{
	for (int i = 0; i<itemsSize(); i++){
		if (Convert.toInt(getItemAt(i).label) == value){
			select(i);
			return;
		}
	}
	select(-1);
}
/**
 * Return the integer value of the String at the currently selected index.
 */
//===================================================================
public int getInt() {return Convert.toInt(getText());}
//===================================================================

//##################################################################
}
//##################################################################

