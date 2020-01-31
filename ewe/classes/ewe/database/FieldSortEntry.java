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
package ewe.database;
import ewe.util.Encodable;
//##################################################################
public class FieldSortEntry implements Encodable{
//##################################################################
//Don't move these!
public int id; //Should be first.
public int type; //Should be second.
public int field1,field2,field3,field4; //Should be 3rd, 4th, 5th, 6th
int type1,type2,type3,type4; //Should be 7th,8th,9th,10th
//......................................................
public String name = "", header = "";

//===================================================================
public FieldSortEntry()
//===================================================================
{

}
//===================================================================
public String toString()
//===================================================================
{
	String s = id+", "+type;
	if (field1 == 0) return s;
	s += " = "+field1+", "+type1+"; ";
	if (field2 == 0) return s;
	s += field2+", "+type2+"; ";
	if (field3 == 0) return s;
	s += field3+", "+type3+"; ";
	if (field4 == 0) return s;
	s += field4+", "+type4+"; ";
	return s;
}
//-------------------------------------------------------------------
static int toCriteria(int field, int type, int options)
//-------------------------------------------------------------------
{
	return DatabaseUtils.toCriteria(field,type,options);
}

//-------------------------------------------------------------------
int[] toCriteria()
//-------------------------------------------------------------------
{
	int count = 4;
	if (field1 == 0) count = 0;
	else if (field2 == 0) count = 1;
	else if (field3 == 0) count = 2;
	else if (field4 == 0) count = 3;
	else count = 4;
	int[] all = new int[count];
	if (count == 0) return all;
	all[0] = toCriteria(field1,type1,type);
	if (count == 1) return all;
	all[1] = toCriteria(field2,type2,type);
	if (count == 2) return all;
	all[2] = toCriteria(field3,type3,type);
	if (count == 3) return all;
	all[3] = toCriteria(field4,type4,type);
	return all;
}
/*
//-------------------------------------------------------------------
boolean isDescending()
//-------------------------------------------------------------------
{
	return ((type & DatabaseTypes.SORT_DESCENDING) != 0);
}
*/
//##################################################################
}
//##################################################################

