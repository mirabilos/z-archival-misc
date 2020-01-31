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
import ewe.data.*;
import ewe.util.*;
import ewe.io.*;
//import ewe.sys.*;
import ewe.reflect.*;
import ewe.ui.*;

//##################################################################
public class DatabaseTableModel extends FieldTableModel{
//##################################################################

public Database dataTable;
public FoundEntries entries;

//===================================================================
public DatabaseTableModel(Database table,boolean useFields)
//===================================================================
{
	dataTable = table;
	if (useFields && table.getObjectClass() != null){
		Object obj = table.getObjectClass().newInstance();
		if (obj != null) setFields(obj);
	}
}
//===================================================================
public DatabaseTableModel(Database table)
//===================================================================
{
	this(table,true);
}
/**
 * Call this to set the entries to be displayed in this table. You can also use entriesChanged() to
	notify the table that the entries have been changed.
 * @param entries The entries to display in this table.
 */
//===================================================================
public DatabaseTableModel setEntries(FoundEntries entries)
//===================================================================
{
	this.entries = entries;
	entriesChanged();
	return this;
}
/**
 * This calculates the number of rows in the table. By default this will return the size
 * of the objects Vector. If this Vector is null it will return zero. Override this if you
 * are not using the objects Vector - in which case you must also override loadObjectAtRow().
 */
//-------------------------------------------------------------------
protected int calculateNumRows()
//-------------------------------------------------------------------
{
	if (entries != null) return entries.size();
	return 0;
}
/**
* Override this to use a different method of retrieving the object at a particular row.
**/
//-------------------------------------------------------------------
protected Object loadObjectAtRow(int row) throws Exception
//-------------------------------------------------------------------
{
	Object obj = getNew();
	if (entries != null) entries.getData(row,obj);
	return obj;
}
//===================================================================
public int insert(Object data,boolean select) throws ewe.io.IOException
//===================================================================
{
	if (entries == null) return -1;
	int s = entries.addData(data);
	entriesChanged(select ? s : -1);
	return s;
}
//===================================================================
public int append(Object data,boolean select) throws ewe.io.IOException
//===================================================================
{
	try{
		if (entries == null) return -1;
		int s = entries.appendData(data);
		entriesChanged(select ? s : -1);
		return s;
	}catch(Throwable t){
		new ReportException(t,null,null,false).execute();
		return -1;
	}
}
//===================================================================
public void delete(int which) throws ewe.io.IOException
//===================================================================
{
	if (which == -1) return;
	entries.delete(which);
	entriesChanged(-1);
}
//===================================================================
public void sort(String sortName) throws ewe.io.IOException
//===================================================================
{
	if (dataTable == null || entries == null) return;
	int [] sorts = dataTable.getSorts();
	for (int i = 0; i<sorts.length; i++){
		if (dataTable.getSortName(sorts[i]).equals(sortName)){
			entries.reSort(sorts[i]);
			entriesChanged();
			return;
		}
	}
}
//===================================================================
public Menu getSortMenu()
//===================================================================
{
	if (dataTable == null) return null;
	int [] sorts = dataTable.getSorts();
	if (sorts.length == 0) return null;
	Menu m = new Menu();
	for (int i = 0; i<sorts.length; i++){
		String got = dataTable.getSortName(sorts[i]);
		if (got != null && !got.startsWith("_"))
			m.addItem(dataTable.getSortName(sorts[i]));
	}
	return m;
}
//===================================================================
public Control getSortButton()
//===================================================================
{
	Menu m = getSortMenu();
	if (m == null) return null;
	ButtonPullDownMenu b = new ButtonPullDownMenu("Sort",m){
		public void onControlEvent(ControlEvent ev){
			if (ev instanceof MenuEvent && ev.type == MenuEvent.SELECTED){
				try{
					sort(((MenuItem)((MenuEvent)ev).selectedItem).action);
				}catch(Exception e){

				}
			}
		}
	};
	return b;
}
//-------------------------------------------------------------------
protected void dataChanged(Object obj,int row,String fieldName)
//-------------------------------------------------------------------
{
	try{
		if (entries != null) entries.setDataInPlace(row,obj);
	}catch(Exception e){
		e.printStackTrace();
	}
}

//===================================================================
public static Editor getTestEditor(Database db) throws ewe.io.IOException
//===================================================================
{
	int [] sorts = db.getSorts();
	FoundEntries fe = db.getEntries(sorts.length == 0 ? 0 : sorts[0]);
	return getTestEditor(fe);
}

//===================================================================
public static Editor getTestEditor(FoundEntries entries) throws ewe.io.IOException
//===================================================================
{
	Database db = entries.getDatabase();
	final DatabaseTableModel dtm = new DatabaseTableModel(db);
	dtm.setEntries(entries);
	final Form tf = dtm.getTableForm(null);
	tf.setPreferredSize(500,300);
	Editor ed = new Editor(){
		{
			addLast(tf);
			ButtonBar bb = new ButtonBar();
			addLast(bb).setCell(tf.HSTRETCH);
			addField(bb.add("Add Object"),"add");
			addField(bb.add("Delete Object"),"delete");
			bb.addNext(dtm.getSortButton());
		}
	public void action(String name,Editor ed){
		try{
			if (name.equals("add")){
				dtm.append(dtm.getNew(),true);
			}else if (name.equals("delete")){
				int which = dtm.getSelectedEntry();
				if (which == -1) return;
				if (new MessageBox("Delete Entry?","Delete this entry?",MessageBox.MBYESNO).execute() != IDYES) return;
				dtm.delete(which);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	};
	return ed;
}
/*
public static void main(final String args[]) throws Exception
{
	String mode = args.length < 2 ? "rw": args[1];
	Database db = null;
	if (args[0].toUpperCase().endsWith(".DAT")){
		db = DataStore.openDatabase(args[0],mode);
		if (db == null) throw new NullPointerException();
	}else{
		Reflect r = Reflect.getForName(args[0]);
		Object obj = r.newInstance();
		final String className = ewe.util.mString.rightOf(r.getClassName(),'/');
		if (mode.equals("rw")){
			db = DataStore.initializeDatabase(className,obj,null,null);
			if (db != null){
				db.enableSynchronization(db.SYNC_STORE_CREATION_DATE);
				db.save();
			}
		}
		if (db == null) db = DataStore.openDatabase(className, mode ,obj);
	}
	Editor ed = getTestEditor(db);
	ed.title = args[0];
	ed.execute();
	ewe.sys.Vm.exit(0);
}
	*/
//##################################################################
}
//##################################################################

