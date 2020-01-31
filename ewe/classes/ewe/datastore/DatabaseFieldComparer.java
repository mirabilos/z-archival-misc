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
package ewe.datastore;
import ewe.util.*;
import ewe.io.*;
import ewe.sys.*;

//##################################################################
class DatabaseFieldComparer implements CompareInts, Comparer, ObjectFinder{
//##################################################################
Database table;
FieldSortEntry fse;
ewe.sys.Locale locale;
int localeCompareOptions = 0;

public boolean useMask;
public boolean useFirstFieldOnly = false;

int options;
ewe.io.IOException error;

DatabaseEntry ded1,ded2;
ByteArray buff1, buff2;

{
	ded1 = new DatabaseEntry();
	ded2 = new DatabaseEntry();
	buff1 = new ByteArray();
	buff2 = new ByteArray();
}

DatabaseEntry lookFor;
int [] lookFields;
int [] lookTypes;
/**
* This is the constructor when you want to use this as an object finder.
**/
//===================================================================
public DatabaseFieldComparer(Database table,DatabaseEntry lookFor,int options)
//===================================================================
{
	useMask = true;
	this.lookFor = lookFor;
	this.table = table;
	this.options = options;
	localeCompareOptions |= locale.IGNORE_CASE;
	lookFields = lookFor.getAssignedFields();
	lookTypes = new int[lookFields.length];
	locale = table.getLocale();
	for (int i = 0; i<lookFields.length; i++)
		lookTypes[i] = table.getFieldType(lookFields[i]);
	if ((options & table.SORT_IGNORE_CASE) != 0) localeCompareOptions |= locale.IGNORE_CASE;
}
/**
* This is the constructor when you want to use this as a Comparer or CompareInts.
**/
//===================================================================
public DatabaseFieldComparer(Database table,FieldSortEntry fse)
//===================================================================
{
	this(table,fse,false);
}
/**
* This is the constructor when you want to use this as a Comparer or CompareInts.
**/
//===================================================================
public DatabaseFieldComparer(Database table,FieldSortEntry fse,boolean useMask)
//===================================================================
{
	this.table = table;
	this.fse = fse;
	this.useMask = useMask;
	localeCompareOptions |= locale.IGNORE_CASE;
	if (fse != null) {
		options = fse.type;
		fse.type1 = table.getFieldType(fse.field1);
		fse.type2 = table.getFieldType(fse.field2);
		fse.type3 = table.getFieldType(fse.field3);
		fse.type4 = table.getFieldType(fse.field4);
		locale = table.getLocale();
		if ((fse.type & table.SORT_IGNORE_CASE) != 0) localeCompareOptions |= locale.IGNORE_CASE;
		//ewe.sys.Math.srand(ewe.sys.Vm.getTimeStamp());
	}
}

//===================================================================
public DatabaseFieldComparer setUseFirstFieldOnly()
//===================================================================
{
	useFirstFieldOnly = true;
	return this;
}

SubString ts1 = new SubString();
SubString ts2 = new SubString();
ewe.sys.Long ln1 = new ewe.sys.Long();
ewe.sys.Long ln2 = new ewe.sys.Long();
ewe.sys.Double db1 = new ewe.sys.Double();
ewe.sys.Double db2 = new ewe.sys.Double();
TimeMask t1 = new TimeMask(), t2 = new TimeMask();

//-------------------------------------------------------------------
public int compareField(int fieldID,int type,DatabaseEntry one,DatabaseEntry two)
//-------------------------------------------------------------------
{
	switch(type){
		case Database.STRING:
			SubString o = (SubString)one.getFieldValue(fieldID,type,ts1);
			SubString t = (SubString)two.getFieldValue(fieldID,type,ts2);
			if (t == null) return 1;
			if (o == null) return -1;
			if (useMask)
				for (int i = 0; i<o.length; i++)
					if (o.data[i] == '*') {
						o.length = i;
						if (t.length > i) t.length = i;
					}else if (o.data[i] == '?'){
						if (t.length > i) t.data[i] = '?';
					}
			return locale.compare(o.data,o.start,o.length,t.data,t.start,t.length,localeCompareOptions);
		case Database.INTEGER:
		case Database.BOOLEAN:
		case Database.LONG:
			ln1 = (ewe.sys.Long)one.getFieldValue(fieldID,type,ln1);
			ln2 = (ewe.sys.Long)two.getFieldValue(fieldID,type,ln2);
			return ln1.compareTo(ln2);
		case Database.DOUBLE:
			db1 = (ewe.sys.Double)one.getFieldValue(fieldID,type,ln1);
			db2 = (ewe.sys.Double)two.getFieldValue(fieldID,type,ln2);
			return db1.compareTo(db2);
		case Database.DATE_TIME:
			t1 = (TimeMask)one.getFieldValue(fieldID,type,t1);
			t2 = (TimeMask)two.getFieldValue(fieldID,type,t2);
			int cmp = 0;
			int d1 = t1.year * 1000;
			int d2 = t2.year * 1000;

			if (t1.month != 0) {
				d1 += t1.month*50;
				d2 += t2.month*50;
			}

			if (t1.day != 0){
				d1 += t1.day;
				d2 += t2.day;
			}
			if ((options & table.SORT_TIME_ONLY) == 0)
				cmp = d1-d2;
			if (cmp != 0) return cmp;
			d1 = t1.hour*60*60*1000 + t1.minute*60*1000 + t1.second*1000 + t1.millis;
			d2 = t2.hour*60*60*1000 + t2.minute*60*1000 + t2.second*1000 + t2.millis;
			if ((options & table.SORT_DATE_ONLY) == 0)
				cmp = d1-d2;
			return cmp;
	}
	return 0;
}
//===================================================================
public boolean lookingFor(Object which)
//===================================================================
{
	DatabaseEntry two = (DatabaseEntry)which;
	for (int i = 0; i<lookFields.length; i++)
		if (compareField(lookFields[i],lookTypes[i],lookFor,two) != 0) return false;
	return true;
}
//===================================================================
public int compare(Object one,Object two)
//===================================================================
{
	DatabaseEntry ded1 = (DatabaseEntry)one;
	DatabaseEntry ded2 = (DatabaseEntry)two;

	int ret = compareField(fse.field1,fse.type1,ded1,ded2);
	if (useFirstFieldOnly || ret != 0 || fse.field2 == 0) return ret;
	ret = compareField(fse.field2,fse.type2,ded1,ded2);
	if (ret != 0 || fse.field3 == 0) return ret;
	ret = compareField(fse.field3,fse.type3,ded1,ded2);
	if (ret != 0 || fse.field4 == 0) return ret;
	ret = compareField(fse.field4,fse.type4,ded1,ded2);
	return ret;
}
//===================================================================
public int compare(int one,int two)
//===================================================================
{
	if (one == two) return 0;
	if (two == 0) return 1;
	if (one == 0) return -1;
	try{
		ded1 = table.getData(one,buff1,ded1);
		ded2 = table.getData(two,buff2,ded2);
	}catch(ewe.io.IOException e){
		error = e;
		return 0;
	}
	return compare(ded1,ded2);
}
//##################################################################
}
//##################################################################

