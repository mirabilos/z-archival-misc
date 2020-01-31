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
import ewe.sys.Convert;
import ewe.io.IOException;
/**
* Do not use this class directly - it is used and stored internally by the Database.
**/
//##################################################################
public class FieldModifier implements Encodable, DatabaseTypes, DataValidator{
//##################################################################
public int fieldID;
public int fieldType;
public int modifier;
public String modifiedData = "";

private Object metaLocation;
private int nextValue = 0;
private Database db;

//===================================================================
public boolean decodeAfterReading(Database db) throws IOException
//===================================================================
{
	this.db = db;
	if (fieldType == INTEGER && fieldType == db.getFieldType(fieldID)){
		if (modifier == db.FIELD_MODIFIER_INTEGER_AUTO_INCREMENT){
			if (!db.isOpenForReadWrite()) return false;
			int initial = Convert.toInt(modifiedData);
			String name = "AUTO_INCREMENT-"+db.getFieldName(fieldID);
			int size = db.metaDataLength(name);
			metaLocation = db.getMetaData(name,4,false);
			if (size != -1) nextValue = db.readMetaDataInt(metaLocation,0);
			if (nextValue < initial) nextValue = initial;
			//ewe.sys.Vm.debug("Read Next value = "+nextValue);
			return true;
		}
	}
	return false;
}
//-------------------------------------------------------------------
protected int getNextInteger() throws IOException
//-------------------------------------------------------------------
{
	int toReturn = nextValue++;
	//ewe.sys.Vm.debug("Write Next value = "+nextValue);
	db.writeMetaDataInt(metaLocation,0,nextValue);
	return toReturn;
}
//===================================================================
public void modify(DatabaseEntry newData,DatabaseEntry oldData) throws IOException
//===================================================================
{
	if (oldData == null){
		//
		// Saving a new one, however if we are synchronizing we may be saving one
		// created on another database. Therefore we should preserve the number if one is
		// set.
		//
		if (modifier == db.FIELD_MODIFIER_INTEGER_AUTO_INCREMENT){
			if (newData.getField(fieldID,0) == 0)
				newData.setField(fieldID,getNextInteger());
		}
	}else if (newData != null && newData != oldData){//Modifying an old one.
		int old = oldData.getField(fieldID,0);
		if (old == 0) old = getNextInteger();
		newData.setField(fieldID,old);
	}
}
//===================================================================
public void validateEntry(Database db,DatabaseEntry newData,DatabaseEntry oldData) throws IOException
//===================================================================
{
	modify(newData,oldData);
}
//===================================================================
public FieldModifier()
//===================================================================
{

}
//===================================================================
public void set(Database db,int fieldID,int modifier,Object data)
//===================================================================
{
	this.db = db;
	this.fieldID = fieldID;
	this.modifier = modifier;
	fieldType = db.getFieldType(fieldID);
	if (fieldType == 0) throw new IllegalArgumentException();
	if (fieldType == INTEGER && modifier == db.FIELD_MODIFIER_INTEGER_AUTO_INCREMENT){
		if (!(data instanceof ewe.sys.Long)) throw new IllegalArgumentException();
		modifiedData = Convert.toString((int)((ewe.sys.Long)data).value);
	}
}
//##################################################################
}
//##################################################################

