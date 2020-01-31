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
import ewe.util.IntArray;
import ewe.sys.Vm;
import ewe.sys.Handle;
import ewe.io.IOException;
import ewe.util.Utils;
import ewe.sys.Time;
import ewe.util.Vector;
import ewe.ui.*;

//##################################################################
public class DatabaseUtils implements DatabaseTypes{
//##################################################################

//-------------------------------------------------------------------
DatabaseUtils(){}
//-------------------------------------------------------------------

static Time time = new Time();

//===================================================================
public static long getNewOID()
//===================================================================
{
	Time t = time.setToCurrentTime();
	int val = (t.month*31+t.day)*24*60*60;
	val += t.hour*60*60;
	val += t.minute*60;
	val += t.second;
	val <<= 5;
	val |= ((int)(java.lang.Math.random() * 32))%32;
	int val2 = (t.millis << 22) | (int)(4194304 * java.lang.Math.random());
	return (long)val << 32 | ((long)val2 & 0xffffffffL);
	/*
	specs = getSpecs();
	if (specs == null) return 0;
	specs.lastAssigned++;
	specsEntry.saveObject("Specs",specs);
	return (long)specs.myOID << 32 | (long)specs.lastAssigned;
	*/
}
//===================================================================
public static int toCriteria(int field, int type, int options)
//===================================================================
{
	return (options & 0xff) << 24 | (type & 0xff) << 16 | (field & 0xffff);
}
//===================================================================
public static int criteriaToField(int criteria)
//===================================================================
{
	return criteria & 0xffff;
}
//===================================================================
public static int criteriaToType(int criteria)
//===================================================================
{
	return (criteria >> 16) & 0xff;
}
//===================================================================
public static int criteriaToOptions(int criteria)
//===================================================================
{
	return (criteria >> 24) & 0xff;
}

/**
 * Returns if a quick search using the specified criteria can be done on
 * a set of entries which are sorted by the sortedBy criteria.
 * @param criteria the search criteria.
 * @param sortCriteria the sorted criteria.
 * @return if a quick search using the specified criteria can be done on
 * a set of entries which are sorted by the sortedBy criteria.
 */
//===================================================================
public static boolean searchIsCompatibleWithSort(int[] criteria, int[] sortCriteria)
//===================================================================
{
	if (criteria.length > sortCriteria.length) return false;
	for (int i = 0; i<criteria.length; i++){
		if (criteriaToField(criteria[i]) != criteriaToField(sortCriteria[i])) return false;
		if (criteriaToType(criteria[i]) != criteriaToType(sortCriteria[i])) return false;
		//if ((criteriaToOptions(criteria[i]) & ~SORT_DESCENDING) != (criteriaToOptions(sortCriteria[i]) & ~SORT_DESCENDING)) return false;
	}
	return true;
}
//===================================================================
public static int[] getCriteriaSubset(int[] sortCriteria, int numberOfCriteria)
//===================================================================
{
	if (numberOfCriteria > sortCriteria.length || numberOfCriteria < 0)
		throw new IllegalArgumentException();
	int [] criteria = new int[numberOfCriteria];
	Vm.copyArray(sortCriteria,0,criteria,0,numberOfCriteria);
	return criteria;
}
//===================================================================
public static int[] copyCriteria(int[] criteria)
//===================================================================
{
	if (criteria == null) return criteria;
	return getCriteriaSubset(criteria,criteria.length);
}
//===================================================================
public static int[] getCriteriaSubset(Database db,int[] criteria,Object searchData)
//===================================================================
{
	int num = 1;
	if (searchData instanceof Vector) num = ((Vector)searchData).size();
	else if (searchData instanceof Object[]) num = ((Object[])searchData).length;
	else if (searchData instanceof DatabaseEntry) num = criteria.length;
	else if (db.isInstanceOfDataObject(searchData)) num = criteria.length;
	if (num > criteria.length) num = criteria.length;
	return getCriteriaSubset(criteria,num);
}
//===================================================================
public static boolean isCollection(Object searchData)
//===================================================================
{
	return ((searchData instanceof Object[]) || (searchData instanceof Vector));
}
//===================================================================
public static int lengthOfCollection(Object searchData)
//===================================================================
{
	if (searchData instanceof Vector) return ((Vector)searchData).size();
	if (searchData instanceof Object[]) return ((Object[])searchData).length;
	throw new IllegalArgumentException();
}
//===================================================================
public static Object getInCollection(Object searchData, int index)
//===================================================================
{
	if (searchData instanceof Object[])
		return ((Object[])searchData)[index];
	if (searchData instanceof Vector)
		return ((Vector)searchData).get(index);
	throw new IllegalArgumentException();
}
//===================================================================
public static Control getFieldInput(int fieldType)
//===================================================================
{
	switch(fieldType){
		case DATE: case TIME: case DATE_TIME: case TIMESTAMP:
			//return new ewe.ui.DateDisplayInput();
			return new ewe.ui.DateTimeInput();
		case BOOLEAN:
			return new ewe.ui.mCheckBox();
		default:
			return new ewe.ui.mInput();
	}
}
//===================================================================
public static String getTypeSpecifier(int type,boolean ignoreString)
//===================================================================
{
	switch(type){
		case STRING: return ignoreString ? "" : "Ljava/lang/String;";
		case INTEGER: return "I";
		case BOOLEAN: return "Z";
		case DOUBLE: return "D";
		case LONG: return "J";
		case DATE_TIME: return "Lewe/sys/Time;";
		case TIME: return "Lewe/sys/TimeOfDay;";
		case DATE: return "Lewe/sys/DayOfYear;";
		case TIMESTAMP: return "Lewe/database/TimeStamp;";
		case DECIMAL: return "Lewe/sys/Decimal;";
		case BYTE_ARRAY: return "Lewe/util/ByteArray;";
		case JAVA_OBJECT: return "Ljava/lang/Object;";
		default:
			return "";
	}
}
//===================================================================
public static Control addToInputStack(Database db,int field,InputStack is,Editor ed)
throws IllegalArgumentException
//===================================================================
{
	String fieldHeader = db.getFieldHeader(field);
	int type = db.getFieldType(field);
	Control c = getFieldInput(type);
	is.add(c,fieldHeader);
	if (ed != null){
		String fieldName = db.getFieldName(field);
		fieldName += '$'+getTypeSpecifier(type,true);
		ed.addField(c,fieldName);
	}
	return c;
}
//##################################################################
static class IncludedField extends CellPanel{
//##################################################################

mCheckBox enabler;
Control input;
//===================================================================
IncludedField(Database db, int field, Editor ed)
//===================================================================
{
	int type = db.getFieldType(field);
	addLast(enabler = new mCheckBox(db.getFieldHeader(field)));
	String fieldName = db.getFieldName(field);
	if (ed != null)
		ed.addField(enabler,"_include_"+fieldName+"$Z");
	Control c = getFieldInput(type);
	if (ed != null){
		fieldName += '$'+getTypeSpecifier(type,true);
		ed.addField(c,fieldName);
	}
	c.modify(c.Disabled,0);
	addLast(c);
	input = c;
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED && ev.target == enabler){
		if (enabler.getState()) input.modify(0,input.Disabled);
		else input.modify(input.Disabled,0);
		input.repaintNow();
	}
	super.onControlEvent(ev);
}
//##################################################################
}
//##################################################################

//===================================================================
public static Control getIncludedField(Database db,int field,Editor ed)
//===================================================================
{
	return new IncludedField(db,field,ed);
}
/**
 * This returns a field spec string for a particular fieldID in the form:
 * "Header|FieldName$Type"
 * @param fieldID the fieldID to look for.
 * @return the field spec string or null if the fieldID is invalid.
 */
//===================================================================
public static String getFieldSpec(Database db,int fieldID)
//===================================================================
{
	String name = db.getFieldName(fieldID);
	if (name == null) return null;
	String header = db.getFieldHeader(fieldID);
	if (header == null) return null;
	String ret = header+"|"+name+"$";
	ret += getTypeSpecifier(db.getFieldType(fieldID),true);
	return ret;
}
//##################################################################
}
//##################################################################

