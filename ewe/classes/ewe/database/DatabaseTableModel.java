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
import ewe.data.*;
import ewe.util.*;
import ewe.io.*;
//import ewe.sys.*;
import ewe.reflect.*;
import ewe.ui.*;
import ewe.sys.Handle;
//##################################################################
public class DatabaseTableModel extends FieldTableModel{
//##################################################################

public Database database;
public FoundEntries entries;
public EntriesView view;
public Record record;

protected boolean useDataObjects;
{
	//dontCacheObjects = true;
}


//===================================================================
public DatabaseTableModel(Database table)
//===================================================================
{
	this(table,true);
}
//===================================================================
public DatabaseTableModel(Database table,String fields)
//===================================================================
{
	this(table,false);
	setFields(table.getNewData(),fields,null);
}
//===================================================================
public DatabaseTableModel(Database table,int[] fields,boolean addNormalFieldsAfter)
//===================================================================
{
	this(table,false);
	String ret = new String();
	int num = 0;
	for (int i = 0; i<fields.length; i++){
		String s = DatabaseUtils.getFieldSpec(table,fields[i]);
		if (s == null) continue;
		if (num != 0) ret += "|";
		num++;
		ret += s;
	}
	if (addNormalFieldsAfter){
		fields = table.getFields();
		for (int i = 0; i<fields.length; i++){
			if (fields[i] >= table.FIRST_SPECIAL_FIELD) continue;
			String s = DatabaseUtils.getFieldSpec(table,fields[i]);
			if (s == null) continue;
			if (num != 0) ret += "|";
			num++;
			ret += s;
		}
	}
	setFields(table.getNewData(),ret,null);
}

//===================================================================
public DatabaseTableModel(Database table,boolean useFields)
//===================================================================
{
	database = table;
	record = new Record(table);
	if (useFields){
		String gotFields = "";
		int[] all = table.getFields();
		for (int i = 0; i<all.length; i++){
			String s = DatabaseUtils.getFieldSpec(table,all[i]);
			if (gotFields.length() != 0) gotFields += "|";
			gotFields += s;
		}
		try{
			Object obj = table.getNewDataObject();
			setFields(obj,gotFields,null);
			useDataObjects = true;
		}catch(IllegalStateException e){
			setFields(table.getNewData(),gotFields,null);
		}
	}
}
//===================================================================
public DatabaseTableModel(FoundEntries fe)
//===================================================================
{
	this(fe.getDatabase(),true);
	setEntries(fe);
}
//===================================================================
public DatabaseTableModel(EntriesView view)
//===================================================================
{
	this(view.getFoundEntries().getDatabase(),true);
	setView(view);
}
long curState;
//===================================================================
public void entriesChanged()
//===================================================================
{
	if (view != null) curState = view.getCurrentState();
	else if (entries != null) curState = entries.getCurrentState();
	super.entriesChanged();
}
//===================================================================
public void checkUpdate()
//===================================================================
{
	if (view != null){
 		if(view.hasChangedSince(curState))
			entriesChanged();
		else
			;//ewe.sys.Vm.debug("Don't need to update!");
	}else if (entries != null){
 		if (entries.hasChangedSince(curState))
			entriesChanged();
		else
			;//ewe.sys.Vm.debug("Don't need to update!");
	}
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
	this.view = null;
	entriesChanged();
	entries.getEventDispatcher().addListener(this,true);
	return this;
}
//===================================================================
public DatabaseTableModel setView(EntriesView view)
//===================================================================
{
	this.view = view;
	this.entries = view.getFoundEntries();
	entriesChanged();
	view.getEventDispatcher().addListener(this,true);
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
	if (view != null) return view.size();
	if (entries != null) return entries.size();
	return 0;
}
//protected DatabaseEntry entry;
/**
* Override this to use a different method of retrieving the object at a particular row.
**/
//-------------------------------------------------------------------
protected Object loadObjectAtRow(int row) throws Exception
//-------------------------------------------------------------------
{
	DatabaseEntry de;
	try{
	if (view != null){
		de = view.get(row);
		if (useDataObjects) return de.getData();
		else return de;
		/*
		Object ret = view.getData(row);
		if (ret == null) ret = view.get(row);
		return ret;
		*/
	}
	if (entries != null){
		de = entries.get(row);
		if (useDataObjects) return de.getData();
		else return de;
		/*
		Object ret = entries.getData(row);
		if (ret == null) ret = entries.get(row);
		return ret;
		*/
	}
	return null;
	}catch(Exception e){
		//e.printStackTrace();
		throw e;
	}
}
//-------------------------------------------------------------------
protected void dataChanged(Object obj,int row,String fieldName)
//-------------------------------------------------------------------
{
	try{
		if (view != null){
			if (obj instanceof DatabaseEntry)
				view.set(row,(DatabaseEntry)obj);
			else
				view.setData(row,obj);
		}else if (entries != null){
			int newIndex =  (obj instanceof DatabaseEntry) ?
				entries.set(row,(DatabaseEntry)obj):
				entries.setData(row,obj);
			selectEntry(newIndex);
		}
	}catch(IOException e){}
}
//===================================================================
public int addNew() throws IOException
//===================================================================
{
	try{
		return addObject(database.getNewDataObject());
	}catch(IllegalStateException e){}
	DatabaseEntry de = database.getNewData();
	record.zero();
	record.writeTo(de);
	return addObject(de);
}
//===================================================================
public int addObject(Object obj) throws IOException
//===================================================================
{
	if (view != null){
		int idx = (obj instanceof DatabaseEntry) ?
			view.add((DatabaseEntry)obj):
			view.addData(obj);
		entriesChanged();
		selectEntry(idx);
		return idx;
	}else if (entries != null){
		int idx = (obj instanceof DatabaseEntry) ?
			entries.add((DatabaseEntry)obj):
			entries.addData(obj);
		entriesChanged();
		selectEntry(idx);
		return idx;
	}else return -1;
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DatabaseEvent){
		//ewe.sys.Vm.debug("Event from: "+ev.target.getClass().getName()+" = "+ev.type);
		checkUpdate();
	}else super.onEvent(ev);
}
/*
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
*/
//##################################################################
}
//##################################################################

