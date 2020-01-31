
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
import ewe.data.InputObject;
import ewe.ui.InputStack;
import ewe.ui.Editor;
import ewe.ui.Frame;
import ewe.data.PropertyList;
import ewe.util.Vector;
import ewe.util.IntArray;

//##################################################################
public class GetSearchCriteria extends InputObject implements DatabaseTypes{
//##################################################################

Database db;
int[] criteria;

//===================================================================
public GetSearchCriteria(Database db)
//===================================================================
{
	this(db,null);
}
//===================================================================
public GetSearchCriteria(Database db, int sortID)
//===================================================================
{
	this(db,db.toCriteria(sortID));
}
//===================================================================
public GetSearchCriteria(Database db, int[] criteria)
//===================================================================
{
	this.criteria = DatabaseUtils.copyCriteria(criteria);
	this.db = db;
}
//===================================================================
public GetSearchCriteria(FoundEntries fe)
//===================================================================
{
	this(fe.getDatabase(),fe.getSortCriteria());
}
protected void addingField(String name,int type)
{
	PropertyList pl = getProperties();
	switch(type){
		case TIMESTAMP: case DATE_TIME:
			pl.defaultTo(name,new ewe.sys.Time()); break;
		case DATE:
			pl.defaultTo(name,new ewe.sys.DayOfYear()); break;
		case TIME:
			pl.defaultTo(name,new ewe.sys.TimeOfDay()); break;
		case DECIMAL:
			pl.defaultTo(name,new ewe.sys.Decimal(0)); break;
	}
}
//-------------------------------------------------------------------
protected void setupInputStack(InputStack is,Editor ed)
//-------------------------------------------------------------------
{
	is.setPreferredSize(200,-1);
	if (criteria == null){
		enableEditorScrolling(ed,true);
		int[] all = db.getFields();
		for (int i = 0; i<all.length; i++){
			if (db.getFieldName(all[i]).startsWith("_")) continue;
			is.add(DatabaseUtils.getIncludedField(db,all[i],ed),null);
			addingField(db.getFieldName(all[i]),db.getFieldType(all[i]));
		}
	}else{
		for (int i = 0; i<criteria.length; i++){
			int field = DatabaseUtils.criteriaToField(criteria[i]);
			DatabaseUtils.addToInputStack(db,field,is,ed);
			addingField(db.getFieldName(field),db.getFieldType(field));
		}
		ed.addField(is.addCheckBox("Filter Entries"),"_filter$Z");
	}
	super.setupInputStack(is,ed);
}
//===================================================================
public EntrySelector input(Frame parent,String title)
//===================================================================
{
	PropertyList pl = new PropertyList();
	pl = input(parent,title,pl);
	if (pl == null) return null;
	Vector v = new Vector();
	IntArray fields = new IntArray();
	//ewe.sys.Vm.debug(pl.toString());
	if (criteria == null){
		int[] all = db.getFields();
		for (int i = 0; i<all.length; i++){
			if (db.getFieldName(all[i]).startsWith("_")) continue;
			String f = db.getFieldName(all[i]);
			if (!pl.getBoolean("_include_"+f,false)) continue;
			Object value = pl.getValue(f,null);
			if (value != null) {
				v.add(value);
				fields.add(all[i]);
			}
		}
		//ewe.sys.Vm.debug(v.toString());
		int[] c = new int[fields.length];
		for (int i = 0; i<fields.length; i++){
			c[i] = DatabaseUtils.toCriteria(fields.data[i],db.getFieldType(fields.data[i]),db.SORT_IGNORE_CASE);
		}
		EntrySelector es = new EntrySelector(db,v,c,true);
		es.forceAFilterSearch = pl.getBoolean("_filter",false);
		return es;
	}else{
		for (int i = 0; i<criteria.length; i++){
			int field = DatabaseUtils.criteriaToField(criteria[i]);
			String fieldName = db.getFieldName(field);
			Object value = pl.getValue(fieldName,null);
			if (value != null) v.add(value);
		}
		//ewe.sys.Vm.debug(v.toString());
		EntrySelector es = new EntrySelector(db,v,criteria,true);
		es.forceAFilterSearch = pl.getBoolean("_filter",false);
		return es;
	}

}
//===================================================================
public EntrySelector input(String title)
//===================================================================
{
	return input(null,title);
}
//##################################################################
}
//##################################################################

