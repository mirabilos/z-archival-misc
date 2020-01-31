/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewe.data;
import ewe.util.*;
import ewe.ui.*;
import ewe.reflect.*;
import ewe.fx.*;
import ewe.io.*;
import ewe.sys.Vm;
/**
* This is a very useful class that allows the display of public fields of an object
* within cells of the table - even allowing you to edit the fields within the table.
* <p>
* Once you create a new FieldTableModel, you call the setFields() method to specify which
* fields to display. There is a setFields() method that allows you to specify that all the
* object fields should be displayed.
* <p>
* The <b>objects</b> member is a Vector that holds the list of objects to display - call
* setObjects() to set it. However if you do not wish to use a Vector then you will need
* to override two methods: <b>calculateNumRows()</b> and <b>loadObjectAtRow(int row)</b>
* <p>
* The constructor FieldTableModel(Object anObject,Vector objectList) is the quickest way
* to display a set of objects in a table. After calling this constructor you can call
* getTableForm() to get a Form that contains a scrollable table that uses this FieldTableModel.<p>
*
*
**/
//##################################################################
public class FieldTableModel extends TableModel{
//##################################################################
//===================================================================
/**
* This holds the list of fields that are being displayed.
**/
public Vector displayFields;
/**
* This can be used to hold the objects being displayed (one per row) - however
* implementations are free to ignore this and use some other method of accessing the
* object for a particular row.
**/
public Vector objects;
/**
* This is a list of all the possible fields that can be displayed by the model.
**/
public MultiListSelect.SingleListSelect allColumns;
/**
* This is a Reflect that represents the class of the objects being displayed.
**/
public Reflect objectClass;
/**
* This is an example (an instance of) the objectClass. This is not necessary, but without
* it certain functions cannot be done.
**/
public Object objectData;
/**
* Set this true to have a read-only display.
**/
public boolean readOnly;
{
	hasRowHeaders = hasColumnHeaders =  true;
	//hasPreferredSize = true;
	clipData = true;
	gap = 2;
}

/**
* Create an empty FieldTableModel. You will need to set the object class and fields
* yourself.
**/
//===================================================================
public FieldTableModel() {}
//===================================================================
/**
 * Create a new FieldTableModel using an instance of a specific object
 * and an array of objects. All the public fields of the object will be
 * displayed.
 * @param anObject An instance of the object to be displayed.
 * @param objects A list of objects to display.
 */
//===================================================================
public FieldTableModel(Object anObject,Vector objects)
//===================================================================
{
	setObjects(objects);
	setFields(anObject);
}
/**
* This can be used to set all the fields.
* The format for each entry in headersAndFields is either:<p>
* [fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by commas <b>OR</b><p>
* [headerName]|[fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by '|' symbols.<p>
* [flags] should be a combination of: 'r' for <b>Read-Only</b><p>
* [alignment] should be one of: L, R
* [anchor] should be one of: N, S, E, W, H, h, V, v, F
* @param objectOrReflect The object being edited or the class of the object.
* @param headersAndFields The list of headers and fields with optional formatting info.
* @param initial the list of field indexes to show initially. This can be null in which case
* all the fields are shown.
* @return this FieldTableModel
*/
//===================================================================
public FieldTableModel setFields(Object objectOrReflect,String headersAndFields,int [] initial)
//===================================================================
{
	objectClass = Reflect.toReflect(objectOrReflect);
	objectData = Reflect.toNonReflect(objectOrReflect);
	setFields(FieldData.toListSelect(FieldData.makeFieldsFromEncodedString(objectOrReflect,headersAndFields,null),initial));
	return this;
}
/**
* This can be used to modify any of the fields.
* The format for each entry in headersAndFields is either:<p>
* [fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by commas <b>OR</b><p>
* [headerName]|[fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by '|' symbols.<p>
* [flags] should be a combination of: 'r' for <b>Read-Only</b><p>
* [alignment] should be one of: L, R
* [anchor] should be one of: N, S, E, W, H, h, V, v, F
* @param objectOrReflect The object being edited or the class of the object.
* @param headersAndFields The list of headers and fields with optional formatting info.
* @return this FieldTableModel
**/
//===================================================================
public FieldTableModel modifyFields(Object objectOrReflect,String headersAndFields)
//===================================================================
{
	FieldData.makeFieldsFromEncodedString(objectOrReflect,headersAndFields,allColumns.getList());
	return this;
}
/**
* This can be used to set all the fields.
* @param objectOrReflect The object being edited or the class of the object.
* @param initial the list of field indexes to show initially. This can be null in which case
* @return this FieldTableModel
**/
//===================================================================
public FieldTableModel setFields(Object objectOrReflect,int [] initial)
//===================================================================
{
	setFields(objectOrReflect,LiveObject.getFieldList(objectOrReflect,false),initial);
	Object obj = Reflect.toNonReflect(objectOrReflect);
	if (obj != null) modifyFields(objectOrReflect,LiveObject.appendAllFields("_formats",obj,false));
	return this;
}
/**
* This can be used to set all the fields.
* @param objectOrReflect The object being edited or the class of the object.
* @return this FieldTableModel
**/
//===================================================================
public FieldTableModel setFields(Object objectOrReflect)
//===================================================================
{
	return setFields(objectOrReflect,null);
}
/**
* Set all the fields. Each field item must be of the type FieldData.
* @param allFields The fields stored in a Vector.
* @param initialChoices the indexes of the field that will be displayed initially. This can be null.
*/
//===================================================================
public void setFields(Vector allFields,int [] initialChoices)
//===================================================================
{
	setFields(FieldData.toListSelect(allFields,initialChoices));
}
/**
 * Set all the fields given a set of String encoded properties for each field. Each element
 * of the fieldProperties must be of the form:
 * "field|lastName|header|Last Name|editable|false". See the FieldData.decode(PropertyList) method
 * for the listof possible properties.
 * @param objectOrReflect The data object or the Class/Reflect object for the data object.
 * @param fieldProperties An array of String encoded properties for the fields.
 * @param initialChoices the indexes of the field that will be displayed initially. This can be null.
 */
//===================================================================
public void setFields(Object objectOrReflect,String[] fieldProperties,int[] initialChoices)
//===================================================================
{
	setFields(FieldData.makeFieldsFromProperties(objectOrReflect,fieldProperties,null),initialChoices);
}
//===================================================================
public void setFields(MultiListSelect.SingleListSelect fields)
//===================================================================
{
	allColumns = fields;
	displayFields = (Vector)fields.getSelected().getCopy();
	numCols = displayFields.size();
}
/**
* This sets the "objects" variable which is usually used to get the
* object for each row.
**/
//===================================================================
public FieldTableModel setObjects(Vector objs)
//===================================================================
{
	objects = objs;
	numRows = objects.size();
	return this;
}
/**
* Get a new instance of the object being edited if possible.
**/
//===================================================================
public Object getNew()
//===================================================================
{
	if (objectData instanceof Copyable) return ((Copyable)objectData).getCopy();
	if (objectClass == null) objectClass = Reflect.getForObject(objectData);
	if (objectClass != null) return objectClass.newInstance();
	return null;
}
/**
* Get a Form that contains a scrollable Table with this FieldTableModel.
* @param f A destination form to add to, which can be null.
* @param putExtraControls If this is true then a "Column Selector" button and a "Table Export"
* button will be added.
* @return a Form that contains a scrollable Table with this FieldTableModel.
*/
//===================================================================
public Form getTableForm(Form f,boolean putExtraControls)
//===================================================================
{
	TableControl tc = new TableControl();
	tc.setPreferredSize(200,300);
	tc.setBorder(mInput.inputEdge|BF_RECT,2);
	tc.setTableModel(this);
	if (f == null) f = new Form();
	f.addLast(new ScrollBarPanel(tc));
	ButtonBar bb = new ButtonBar();
	f.getProperties().set("buttonBar",bb);
	f.addLast(bb).setCell(f.HSTRETCH);
	if (putExtraControls){
		bb.addNext(getColumnSelectorButton(true));
		bb.addNext(getTableExportButton(true));
	}
	f.resizable = true;
	return f;
}
/**
* Get a Form that contains a scrollable Table with this FieldTableModel.
* A "Column Selector" button and a "Table Export"
* button will be added to the form.
* @param f A destination form to add to, which can be null.
* @return a Form that contains a scrollable Table with this FieldTableModel.
*/
//===================================================================
public Form getTableForm(Form f)
//===================================================================
{
	return getTableForm(f,true);
}
/**
* Get an Editor that you can use to decide which columns are being displayed in the table.
**/
//===================================================================
public Editor getColumnSelector(MultiListSelect.SingleListSelect columns)
//===================================================================
{
	if (columns == null) columns = allColumns;
	if (columns == null) return null;
	columns.setSelected(displayFields);
	ListSelect ls = new ListSelect(false,null,true);
	ls.setText("Columns");
	ls.setData(columns);
	return ls;
}
//-------------------------------------------------------------------
protected int getMinColWidth(int col)
//-------------------------------------------------------------------
{
	FieldData fd = getFieldData(col);
	if (fd == null) return super.getMinColWidth(col);
	return fd.minWidth*charWidth;
}
//-------------------------------------------------------------------
protected int getMaxColWidth(int col)
//-------------------------------------------------------------------
{
	FieldData fd = getFieldData(col);
	if (fd == null) return super.getMaxColWidth(col);
	return fd.maxWidth*charWidth;
}
//-------------------------------------------------------------------

/**
* Get all the available fields as a list of FieldData objects.
**/
//===================================================================
public Vector getAllFields()
//===================================================================
{
	return allColumns.getList();
}
/**
* Get the FieldData for a particular field name.
**/
//===================================================================
public FieldData getField(String fieldName)
//===================================================================
{
	Vector v = getAllFields();
	if (v == null) return null;
	int idx = v.find(fieldName);
	if (idx == -1) return null;
	return (FieldData)v.get(idx);
}
/**
* Get an Editor that can be used to allow the user to export the table data.
**/
//===================================================================
public Editor getTableExporter()
//===================================================================
{
	return new FieldTableExportSpecs(this).getEditor(0);
}
/**
* Get a TaskObject that will, when started, export the data as specified
* in the provided FieldTableExportSpecs. Note that you do not need this
* if you use display the Editor provided by getTableExporter() since that
* handles the export task itself.
**/
//===================================================================
public ewe.sys.Task getExportTask(final FieldTableExportSpecs specs)
//===================================================================
{
	final TableControl theTable = table;
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			try{
				StringBuffer out = new StringBuffer();
				StreamWriter print = (specs.toFile) ?
					print = new StreamWriter(specs.outputFile.toWritableStream(specs.appendFile)):
					null;
				String sep = "\t";
				if (specs.useSpace) sep = " ";
				else if (specs.useOther) sep = specs.fieldSeparator;
				Vector v = specs.columns.getSelected();
				FieldData [] cols = new FieldData[v.size()];
				v.copyInto(cols);
				int st = specs.columnHeaders? -1 : 0;
				int did = 0;
				handle.resetTime("Exporting...");
				for (int r = st; r<numRows; r++){
					int row = r;
					if (r == -1 || specs.entireTable || theTable.isSelected(r,0)){
						StringBuffer line = new StringBuffer();
						int stc = specs.rowHeaders ? -1 : 0;
						if (r != -1) did++;
						if (!specs.reorderRows) did = row+1;
						for (int c = stc; c<cols.length; c++){
							String next = c == -1 ? (r == -1 ? "Row" : ewe.sys.Convert.toString(did)) : mString.toString(getFieldValue(cols[c],row));
							if (c != stc) line.append(sep);
							line.append(next);
						}
						if (print != null){
							if (!print.println(line.toString()))
								throw new Exception("Error writing to output file!");
						}else{
							out.append(line);
							out.append('\n');
						}
					}
					handle.progress = (float) (r+1)/(float)numRows;
					handle.changed();
					ewe.sys.Coroutine.nap(10,0);
				}
				ewe.sys.Vm.setClipboardText(out.toString());
				handle.progress = 1.0f;
				handle.set(handle.Succeeded);
			}catch(Throwable t){
				handle.errorObject = t;
				handle.set(handle.Failed);
			}
		}
	};
}
//===================================================================
public Control getTableExportButton(boolean textLabel)
//===================================================================
{
	final mButton mb = new mButton("Export");
	Gui.iconize(mb.setToolTip("Export Table Data"),"ewe/savesmall.bmp",Color.White,textLabel,table.getFontMetrics());
	final TableControl theTable = table;
	mb.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev.target == mb && ev.type == ControlEvent.PRESSED)
				getTableExporter().exec();
		}
	});
	return mb;
}

//===================================================================
public void doColumnSelection()
//===================================================================
{
	Editor cs = getColumnSelector(allColumns);
	cs.title = "Select Table Columns";
	cs.windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
	Gui.setOKCancel(cs);
	cs.setPreferredSize(200,200);
	Vector was = (Vector)displayFields.getCopy();
	if (cs != null){
		if (cs.execute() != Form.IDCANCEL){
			table.clearSelectedCells(null);
			displayFields = (Vector)allColumns.getSelected().getCopy();
			numCols = displayFields.size();
			int [] previous = new int[numCols];
			for (int i = 0; i<numCols; i++)
				previous[i] = was.find(displayFields.get(i));
			remapColumns(previous);
			table.update(true);
		}else{
			allColumns.setSelected(displayFields);
		}
	}
}
//===================================================================
public Control getColumnSelectorButton(boolean textLabel)
//===================================================================
{
	final mButton mb = new mButton("Columns");
	Gui.iconize(mb.setToolTip("Select Displayed Columns"),"ewe/optionssmall.bmp",Color.White,textLabel,table.getFontMetrics());
	final TableControl theTable = table;
	mb.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev.target == mb && ev.type == ControlEvent.PRESSED){
				new ewe.sys.TaskObject(){
					protected void doRun(){doColumnSelection();}
				}.startTask();
			}
		}
	});
	return mb;
}
/**
* Override this to use a different method of retrieving the object at a particular row.
**/
//-------------------------------------------------------------------
protected Object loadObjectAtRow(int row) throws Exception
//-------------------------------------------------------------------
{
	Vector v = objects;
	if (v != null) return v.get(row);
	return null;
}
protected ewe.sys.Lock getLock = new ewe.sys.Lock();
/*
int lastGot = -1;
Object lastObject = null;
*/

/**
* Set this true if you don't want objects to be cached.
**/
public boolean dontCacheObjects = false;
/**
* The number of objects to cache - 20 by default.
**/
public int cacheSize = 20;

Vector cache = new Vector();

//-------------------------------------------------------------------
private Tag getCached(int row)
//-------------------------------------------------------------------
{
	int max = cache.size();
	for (int i = 0; i<max; i++){
		Tag t = (Tag)cache.get(i);
		if (t.tag == row) return t;
	}
	return null;
}
/**
* Assign the specified object to the cache for a particular row.
**/
//-------------------------------------------------------------------
protected void cacheObject(int row,Object obj)
//-------------------------------------------------------------------
{
	Tag got = getCached(row);
	if (got == null){
		Tag t = new Tag();
		t.tag = row;
		t.value = obj;
		cache.add(t);
		if (cache.size() > cacheSize) cache.del(0);
	}else{
		cache.remove(got);
		got.value = obj;
		cache.add(got);
	}
}
//-------------------------------------------------------------------
protected Object getCachedObject(int row)
//-------------------------------------------------------------------
{
	Tag got = getCached(row);
	if (got == null) return null;
	return got.value;
}
//===================================================================
public void invalidateCachedObject(int row)
//===================================================================
{
	Tag got = getCached(row);
	if (got != null) cache.remove(got);
}
//===================================================================
public void rowChanged(int row)
//===================================================================
{
	invalidateCachedObject(row);
}
/**
* This is the same as invalidateCache().
* @deprecated use invalidateCache() or invalidateCachedObject(int row);
*/
//===================================================================
public void invalidateCachedObject()
//===================================================================
{
	invalidateCache();
}
//===================================================================
public void invalidateCache()
//===================================================================
{
	cache.clear();
}

/**
* Don't override this, you should override loadObjectAtRow() instead. This method checks
* the cache first.
**/
//-------------------------------------------------------------------
protected Object getObjectAtRow(int row)
//-------------------------------------------------------------------
{
	if (!getLock.grab()) return getNew();
	try{
		Object obj = dontCacheObjects ? null : getCachedObject(row);
		//if (!dontCacheObjects && lastGot == row && row != -1) return lastObject;
		if (obj != null) return obj;
		obj = loadObjectAtRow(row);
		if (!dontCacheObjects) cacheObject(row,obj);
		//lastGot = row;
		//lastObject = obj;
		return obj;
	}catch(Exception e){
		//e.printStackTrace();
		return e;
	}finally{
		getLock.release();
	}
}

int rowHeight;
int charWidth;
int rowLines;
/**
* Get the field data for a particular column.
**/
//===================================================================
public FieldData getFieldData(int col)
//===================================================================
{
	if (col == -1) return null;
	return (FieldData)displayFields.get(col);
}
Wrapper wrapper = new Wrapper();

//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean sel,TableCellAttributes tca)
//===================================================================
{
	FieldData fd = getFieldData(col);
	tca = super.getCellAttributes(row,col,sel,tca);
	if (fd != null && row != -1) {
		tca.alignment = fd.alignment;
		tca.anchor = fd.anchor;
		Object obj = getObjectAtRow(row);
		if (!(obj instanceof Exception)){
			if (fd.display != null && !(tca.data instanceof Control)){
				tca.text = null;
				tca.data = controlProxy.set(fd.display);
				if (fd.ft != null) fd.ft.getFor(obj,fd.display);
			}
		}
		if (tca.data instanceof Control || tca.data instanceof ControlProxy)
			tca.anchor = fd.controlAnchor;
	}
	return tca;
}
/**
* Get the value of a field at the specified row.
**/
//===================================================================
public Object getFieldValue(FieldData fd,int row)
//===================================================================
{
	if (row == -1) return fd.toString();
	Object obj = getObjectAtRow(row);
	Object ret = obj instanceof Exception ? (Object)obj.getClass().getName() : fd.ft.getFieldValue(obj,wrapper);
	//ewe.sys.Vm.debug(row+" - getFieldValue: "+obj+" is: "+ret);
	return ret;
}
//private static String dummy = "***";
//===================================================================
public Object getCellText(int row,int col)
//===================================================================
{
	//if (true) return dummy;
	try{
		if (col == -1)
			return row == -1 ? "Row" : ewe.sys.Convert.toString(row+1);
		return mString.toString(getFieldValue(getFieldData(col),row));
	}catch(Exception e){
		e.printStackTrace();
		return null;
	}
}

//===================================================================
public int calculateRowHeight(int row)
//===================================================================
{
	if (row == -1)
		if (!hasColumnHeaders) return 0;
		else return rowHeight+2;
	else return (rowLines * (rowHeight+2));
}
//===================================================================
public int calculateColWidth(int col)
//===================================================================
{
	if (col == -1)
		if (!hasRowHeaders) return 0;
		else return charWidth*5;
	FieldData fd = getFieldData(col);
	if (fd == null) return charWidth*20;
	return charWidth * fd.length;
}
//===================================================================
public void made()
//===================================================================
{
	FontMetrics fm = table.getFontMetrics();
	table.multiSelect = true;
	rowHeight = fm.getHeight()+10;
	rowLines = 1;
	for (int i = 0; i<numCols; i++){
		FieldData fd = getFieldData(i);
		if (fd != null) if (fd.height > rowLines) rowLines = fd.height;
	}
	charWidth = fm.getCharWidth('X');
}
//-------------------------------------------------------------------
protected void dataChanged(Object obj,int row,String fieldName)
//-------------------------------------------------------------------
{
}
//-------------------------------------------------------------------
protected void dataChanged(FieldTransfer ft,Point cell)
//-------------------------------------------------------------------
{
	Object obj = ft.dataObject;
	if (obj instanceof FieldListener)
		((FieldListener)obj).fieldChanged(ft,null);
	dataChanged(obj,cell.y,ft.fieldName);
	if (table != null) table.notifyDataChange();
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (activeCellControl != null)
		if (ev.target == activeCellControl.control && ev instanceof DataChangeEvent){
			invalidateCachedObject();
			dataChanged(activeCellControl.control.fieldTransfer,activeCellControl.cell);
		}
	super.onEvent(ev);
}
//-------------------------------------------------------------------
protected CellControl getCellControlFor(final Point p)
//-------------------------------------------------------------------
{
	if (readOnly) return null;
	if (p.x < 0 || p.y < 0) return null;
	if (p.x >= numCols || p.y >= numRows) return null;
	final FieldData fd = getFieldData(p.x);
	if (fd == null) return null;
	if (fd.ft == null || !fd.editable) return null;
	final CellControl cc = new fieldCellControl(p,fd);
	if ((cc.control instanceof mChoice) || (cc.control.getTag(Control.TAKE_FIRST_PRESS,null) != null)) cc.takeFirstPress = false;
	cc.control.addListener(this);
	return cc;
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
	if (objects != null) return objects.size();
	return 0;
}
/**
 * Call this to notify the table that the entries to be displayed have been changed in some way. It
 * recalculates the number of rows and redisplays the table.
 */
//===================================================================
public void entriesChanged()
//===================================================================
{
	invalidateCachedObject();
	numRows = calculateNumRows();
	if (table != null) table.update(true);
}

//-------------------------------------------------------------------
protected int calculateTextCharsInColumn(int col)
//-------------------------------------------------------------------
{
	if (col == -1 && hasRowHeaders)
		return numRows > 0 ? (int)(Math.log(numRows)/Math.log(10))+2 : 1;
	return super.calculateTextCharsInColumn(col);
}
/**
* This calls entriesChanged and then selects a specified row.
* @param selectIndex the row to select, or -1 to clear the selection.
* @return
*/
//===================================================================
public void entriesChanged(int selectIndex)
//===================================================================
{
	if (table != null && selectIndex == -1)
		table.clearSelection(null);
	entriesChanged();
	if (selectIndex >= 0)  selectEntry(selectIndex);
}
/**
* If only one entry is selected, return the row index of that entry.
**/
//===================================================================
public int getSelectedEntry()
//===================================================================
{
	if (table == null) return -1;
	Vector v = table.getSelectedCells(null);
	if (v.size() != 1) return -1;
	Rect r = (Rect)v.get(0);
	if (r.width < numCols || r.height > 1) return -1;
	return r.y;
}
/**
* Select a particular row and make it visible if it is not so already.
**/
//===================================================================
public void selectEntry(int index)
//===================================================================
{
	if (table == null) return;
	int cols = numCols;
	table.clearSelection(null);
	table.addToSelection(new ewe.fx.Rect(0,index,cols,1),true);
	if (table.scrollToVisible(index,0)) table.repaintNow();
}
/**
* This calls calculateNumRows() which is what you should override.
**/
//===================================================================
public int countEntries()
//===================================================================
{
	return calculateNumRows();
}

//===================================================================
public boolean doHotKey(KeyEvent ev)
//===================================================================
{
	int num = numCols;
	int row = getSelectedEntry();
	if (row == -1) {
		if (activeCellControl == null) return false;
		row = activeCellControl.cell.y;
	}
	for (int i = 0; i<num; i++){
		FieldData fd = getFieldData(i);
		if (ev.isHotKey(fd.hotKey)) {
			if(checkControlFor(null,new Point(i,row),table.ByKeyboard)){
				activeCellControl.control.doAction(ByKeyboard);
				activeCellControl.control.notifyAction();
				return true;
			}
		}
	}
	return false;
}

//##################################################################
class fieldCellControl extends CellControl{
//##################################################################
FieldData fd;
//===================================================================
public fieldCellControl(Point where,FieldData fd)
//===================================================================
{
	super(where,fd.editor);
	this.fd = fd;
	if (fd.editor == null){
		setControl(fd.editor = fd.makeControl());
		//setControl(fd.editor = new mInput());
	}
	if (control == null) return;
	fd.ft.getFor(getObjectAtRow(where.y),control);
	if (!fd.editable) control.modify(control.NotEditable,0);
	else control.modify(0,control.NotEditable);
	//exitOnLostFocus = false;
}

//##################################################################
}
//##################################################################

/**
* This allows you to execute the following command line:<p>
* <b>ewe ewe.data.FieldTableModel <object_class_name> [<number of objects>]</b><p>
* to bring up a demo/test Table that will display/edit the public fields of the specified
* object. Try it out on: <b>ewe ewe.data.FieldTableModel ewe.sys.MemoryStatus 10</b><p>
**/
//===================================================================
public static void main(String args[]) throws Exception
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	Reflect r = Reflect.getForName(args[0]);
	Object obj = r == null ? null : r.newInstance();
	if (obj == null){
		new ewe.ui.MessageBox("Error","Cannot create an instance of class:\n"+args[0],MessageBox.MBOK).execute();
		Vm.exit(0);
	}
	FieldTableModel ftm = new FieldTableModel();
	ftm.setFields(obj);
	ftm.objects = new Vector();
	int num = 5;
	if (args.length > 1) num = ewe.sys.Convert.toInt(args[1]);
	for (int i = 0; i<num; i++)
		ftm.objects.add(r.newInstance());
	ftm.entriesChanged();
	Form f = ftm.getTableForm(null);
	f.title = "Object: "+r.getReflectedClass().getName();
	f.setPreferredSize(500,200);
	f.execute();
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

