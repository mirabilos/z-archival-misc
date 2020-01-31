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
import ewe.io.FileSaver;
import ewe.reflect.DataConverter;
import ewe.reflect.FieldTransfer;
import ewe.reflect.Method;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;
import ewe.util.Iterator;
import ewe.util.ObjectIterator;
import ewe.util.Vector;
import ewe.util.mString;
/**
* An Editor is a special type of Form that can automatically transfer data between
* public fields in an Object and Gui Controls within the Editor.<p>
* You should consult the Ewe Programming Guide for instructions on how
* to use this correctly.<p>
**/
//##################################################################
public class Editor extends Form implements FieldListener{
//##################################################################
/**
* This holds a list of all the FieldTransfers used in the Editor.
**/
public Vector transfers = new Vector();
/**
* This holds a list of all the fieldListeners listening to the Editor.
**/
public Vector fieldListeners = new Vector();
/**
* @deprecated.
**/
public int coroutineStackSize = 0;//100;
/**
* This must be set before adding fields.
**/
public Reflect objectClass;
/**
* This is only necessary if you are using the objects _getField() and _setField()
* methods for field transfer. If not, you can leave this as null.
**/
public Object sampleObject;
/**
* This is the object currently being edited. Use setObject() to set it.
**/
public Object myObject;

FieldTransfer myTransfer = new FieldTransfer("_editor_");
boolean dontSendEachFieldTransferEvent = false;
boolean fieldsSet = false;
{
	windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
}
//===================================================================
public void dismantle(Control stopAt)
//===================================================================
{
	if (stopAt == this) return;
	transfers.clear();
	fieldListeners.clear();
	tx.clear();
	xfer.clear();
	super.dismantle(stopAt);
}
/**
If a ScrollablePanel has been setup in the Editor, and the property "MainScrollBarPanel" has
been added to the PropertyList of this Editor, then this will either enable or disable the
scrolling of the scrollbar.
@param enable true to enable the scrolling, false to disable it.
@return true if a "MainScrollBarPanel" was defined, false if it was not.
**/
//===================================================================
public boolean enableScrolling(boolean enable)
//===================================================================
{
	Object sbp = ewe.data.PropertyList.getValue(this,"MainScrollBarPanel",null);
	if (sbp instanceof ScrollablePanel){
		if (enable) ((ScrollablePanel)sbp).setClientConstraints(HEXPAND|VEXPAND);
		else ((ScrollablePanel)sbp).setClientConstraints(HEXPAND|VEXPAND|HCONTRACT|VCONTRACT);
		return true;
	}else
		return false;
}
/**
 * Create a new Editor that will initially be set to edit itself.
 */
//===================================================================
public Editor() {this(null);}
//===================================================================
/**
 * Create a new Editor to edit the type of Object specified.
 * Additional verbose
 * @param objectOrClassOrReflectToEdit Can be either an instance of the
	Object to edit, or a Class or Reflect Object that represents the Object class to edit.
 */
//===================================================================
public Editor(Object objectOrClassOrReflectToEdit)
//===================================================================
{
	if (objectOrClassOrReflectToEdit == null) objectOrClassOrReflectToEdit = this;
	objectClass = Reflect.toReflect(objectOrClassOrReflectToEdit);
	setObject(Reflect.toNonReflect(objectOrClassOrReflectToEdit));
}
/**
* This saves the FileSaver as a property called "FileSaver" in the Form's property list and
* sets up the saver as a listener for DataChangeEvents.
**/
//===================================================================
public void setFileSaver(FileSaver fs,boolean checkOnExit)
//===================================================================
{
	getProperties().set("FileSaver",fs);
	if (fs != null) {
		addListener(fs);
		if (checkOnExit) getProperties().setBoolean("CheckFileSaverOnExit",true);
	}
}
/**
 * This will return the Saver associated with the Form if one was assigned.
 */
//===================================================================
public FileSaver getFileSaver()
//===================================================================
{
	if (pl == null) return null;
	return (FileSaver) pl.getValue("FileSaver",null);
}

//-------------------------------------------------------------------
protected boolean canExit(int exitCode)
//-------------------------------------------------------------------
{
	FileSaver fs = getFileSaver();
	if (fs == null) return true;
	if (!getProperties().getBoolean("CheckFileSaverOnExit",false)) return true;
	return fs.checkExit(this,getProperties().getValue("ObjectToSave",myObject),exitCode);
}

//===================================================================
public void setFields(Object objectOrClassOrReflectToEdit)
//===================================================================
{
	objectClass = Reflect.toReflect(objectOrClassOrReflectToEdit);
	ewe.data.LiveObject.addObjectToPanel(this,this,objectOrClassOrReflectToEdit,false).close(true);
	setObject(Reflect.toNonReflect(objectOrClassOrReflectToEdit));
}
/**
* This is used to set the Object to be edited by the Editor. This will automatically
cause a toControls() to be called, which causes the on-screen Controls to reflect the data
in the Object.
* @param obj The Object to be edited.
*/
//===================================================================
public void setObject(Object obj)
//===================================================================
{

	if (obj != null){
		if (objectClass == null){
			ewe.sys.Vm.debug("ewe.ui.Editor.setObject() called but no objectClass defined.");
			return;
		}else if (!objectClass.isInstance(obj)){
			ewe.sys.Vm.debug("ewe.ui.Editor.setObject() called but the object: "+Reflect.getForObject(obj).getClassName()+" is not compatible with objectClass: "+objectClass.getClassName());
			return;
		}
	}
	//if (obj instanceof Vector) ewe.sys.Vm.debug(objectClass.getReflectedClass().getName());
	if (myObject != null && myObject != this)
		fieldListeners.remove(myObject);
	myObject = obj;
	if (obj != null)
		if (obj instanceof FieldListener && obj != this)
			fieldListeners.add(obj);
	if (fieldsSet) postEditorEvent(EditorEvent.OBJECT_SET,obj);
	if (made)
		toControls();
	int on = 0, off = 0;
	if (obj == null && !hasModifier(Disabled,false)) on = Disabled;
	else if (obj != null && hasModifier(Disabled,false)) off = Disabled;
	if ((on|off) != 0){
		modify(on,off);
		repaintNow();
	}
}
//===================================================================
/**
* This gets called by action(FieldTransfer ft,Editor ed) and by fieldChanged(FieldTransfer ft,Editor ed)
* if the field changed is a menu item.
* By default it will look for
* a method called void <fieldName>_action(Editor ed) and if it finds it, it will be executed.
**/
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	Method m = Reflect.getForObject(this).getMethod(fieldName.replace(' ','_')+"_action(Lewe/ui/Editor;)V",Reflect.PUBLIC);
	if (m != null) m.invoke(this,new Wrapper[]{new Wrapper().setObject(ed)},null);
	else {
		m = Reflect.getForObject(this).getMethod(fieldName.replace(' ','_')+"_action()V",Reflect.PUBLIC);
		if (m != null) m.invoke(this,new Wrapper[0],null);
	}
}
/**
* This gets called by fieldChanged(FieldTransfer ft,Editor ed).
* By default it will look for
* a method called void <fieldName>_changed(Editor ed) and if it finds it, it will be executed.
**/
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	Method m = Reflect.getForObject(this).getMethod(fieldName.replace(' ','_')+"_changed(Lewe/ui/Editor;)V",Reflect.PUBLIC);
	if (m != null) m.invoke(this,new Wrapper[]{new Wrapper().setObject(ed)},null);
	else {
		m = Reflect.getForObject(this).getMethod(fieldName.replace(' ','_')+"_changed()V",Reflect.PUBLIC);
		if (m != null) m.invoke(this,new Wrapper[0],null);
	}
}

/**
* This implements the action() method in FieldListener. By default it
* calls action(String name,Editor ed)
**/
//===================================================================
public void action(FieldTransfer ft,Editor ed) {action(ft.fieldName,ed);}
//===================================================================
/**
* This implements the fieldChanged() method in FieldListener. If the field is of
type MenuItem then action(String fieldName,Editor ed) method is called - where
fieldName is set to be the "action" value for the MenuItem.
**/
//===================================================================
public void fieldChanged(FieldTransfer ft,Editor ed)
//===================================================================
{
	if ("Lewe/ui/MenuItem;".equals(ft.fieldType)){
		Object got = ft.getFieldValue(ed.myObject);
		if (got instanceof MenuItem){
			if (((MenuItem)got).action != null)
				action(((MenuItem)got).action,ed);
		}
	}//else
		fieldChanged(ft.fieldName,ed);
}
//===================================================================
/**
* This implements fieldEvent() in FieldListener. By default it does nothing.
**/
//===================================================================
public void fieldEvent(FieldTransfer ft,Editor ed,Object event) {}
//===================================================================
/**
* This calls setObject(obj).
**/
//===================================================================
public void setData(Object obj)
//===================================================================
{
	setObject(obj);
}
/**
* This calls fromControls(obj).
**/
//===================================================================
public void getData(Object obj)
//===================================================================
{
	fromControls(obj);
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	super.make(reMake);
	toControls();
}

//===================================================================
//protected void formShown() {toControls();}

/**
* Set this true if you are adding fields with Controls which themselves will not
* be added as a child of the Editor. The Editor will still listen to events from the controls.
**/
public boolean fieldsWillNotBeAdded = false;
//===================================================================
public void addField(FieldTransfer ft){fieldsSet = true; transfers.add(ft); ft.setMethodsFor("Lewe/ui/MenuItem;","getLastSelected","()Lewe/ui/MenuItem;",null,null);}
//===================================================================
/**
 * This is the main way of adding a field to the Editor - although it does not add the control as a
	child of the Editor. This only associates the Control with the Field of the Object to be edited with the
	specified field name. The Control must be added to the Editor either directly or somewhere within the child
	tree.
 * @param control The control to associate with the field.
 * @param fieldName The field to be associated with the Control.
 * @return The control itself.
 */
//===================================================================
public Control addField(Control control,String fieldName)
//===================================================================
{return addField(control,fieldName,(DataConverter)null);}
/**
 * This is the main way of adding a field to the Editor - although it does not add the control as a
	child of the Editor. This only associates the Control with the Field of the Object to be edited with the
	specified field name. The Control must be added to the Editor either directly or somewhere within the child
	tree.
 * @param control The control to associate with the field.
 * @param fieldName The field to be associated with the Control.
	@param converter An optional DataConverter to convert the data during a data transfer.
 * @return The control itself.
 */
//===================================================================
public Control addField(Control control,String fieldName,DataConverter converter)
//===================================================================
{
	addField(new FieldTransfer(objectClass,sampleObject,fieldName,control,converter));
	if (control != null && fieldsWillNotBeAdded) control.addListener(this);
	return control;
}
/**
 * Use this to add a Control for a field when the Control will not be added as a child of the
 * Editor but will be added to some external Container. This will tell the Editor to listen
 * to events from the Control.
 * @param control The Control for the field.
 * @param fieldName The name of the field.
 * @param convert An optional DataConverter.
 * @return The Control
 */
//===================================================================
public Control addExternalField(Control control,String fieldName,DataConverter convert)
//===================================================================
{
	boolean fwa = fieldsWillNotBeAdded;
	fieldsWillNotBeAdded = true;
	addField(control,fieldName,convert);
	fieldsWillNotBeAdded = fwa;
	return control;
}
/**
 * Use this to add a Control for a field when the Control will not be added as a child of the
 * Editor but will be added to some external Container. This will tell the Editor to listen
 * to events from the Control.
 * @param control The Control for the field.
 * @param fieldName The name of the field.
 * @return The Control
 */
//===================================================================
public Control addExternalField(Control control,String fieldName)
//===================================================================
{
	return addExternalField(control,fieldName,(DataConverter)null);
}

/**
 * This method is called on a SoftKeyEvent. If the event has a Control proxy
 * associated with it which happens to be one of the fields of the Editor,
 * then it will call fireProxyAction() only and no further action will be taken.
 * Otherwise it will call onSoftKeyEvent(int, String, MenuItem);
 * @param se the SoftKeyEvent.
 */
//===================================================================
public void onSoftKey(SoftKeyEvent se)
//===================================================================
{
	se.fireProxyAction();
	if (se.proxy == null || findFieldTransfer(se.proxy) == null) onSoftKey(se.whichKey, se.action, se.selectedItem);
}

//static WeakSet ws = new WeakSet();
//-------------------------------------------------------------------
protected void postEditorEvent(int type,Object parameter)
//-------------------------------------------------------------------
{
	EditorEvent ev = new EditorEvent(type,this,parameter);
	fireFieldEvent(new ObjectIterator(myTransfer),ev,2);
	postEvent(ev);
	/*
	ws.add(ev);
	ev = null;
	Vm.gc();
	if (ws.count() != 0) ewe.sys.Vm.debug("Not gc'ed: "+type);
	*/
}
//===================================================================
public void shown()
//===================================================================
{
	super.shown();
	postEditorEvent(EditorEvent.SHOWN,null);
}
//-------------------------------------------------------------------
protected void formClosing()
//-------------------------------------------------------------------
{
	postEditorEvent(EditorEvent.CLOSED,null);
	super.formClosing();
}
//===================================================================
public void transfer(Object obj,Iterator it,int direction)
//===================================================================
{
	while(it.hasNext()){
		FieldTransfer ft = (FieldTransfer)it.next();
/*
		if (ft.fieldName.equals("programName")) {
			ewe.sys.Vm.debug("+++"+ft.fieldName+" "+ft.dataInterface.hashCode());
			((Control)ft.dataInterface)._debug = true;
		}
*/
//
// Bug fix - Jan 2004
		// ft.transfer(myObject,direction);
		try{
			ft.transfer(obj,direction);
		}catch(RuntimeException e){
			//ewe.sys.Vm.debug(e.toString()+" - "+ft.fieldName+" = "+obj.getClass().getName());
			e.printStackTrace();
			throw e;
		}

/*
		if (ft.fieldName.equals("programName")) {
			ewe.sys.Vm.debug("---"+ft.fieldName+" "+ft.dataInterface.hashCode());
			((Control)ft.dataInterface)._debug = false;

		}
*/
		if (!dontSendEachFieldTransferEvent) postEditorEvent(direction == FieldTransfer.FROM_OBJECT ? EditorEvent.TO_CONTROLS : EditorEvent.FROM_CONTROLS,ft);
		if (direction == FieldTransfer.FROM_OBJECT && ft.dataInterface instanceof Control){
			((Control)ft.dataInterface).updateData();
		}
	}
	if (dontSendEachFieldTransferEvent) postEditorEvent(direction == FieldTransfer.FROM_OBJECT ? EditorEvent.TO_CONTROLS : EditorEvent.FROM_CONTROLS,null);
}
//===================================================================
/**
* Transfer all edited fields from the Object currently being edited to the on-screen controls.
**/
public void toControls() {toControls(myObject);}
/**
* Transfer all edited fields to the Object currently being edited from the on-screen controls.
**/
public void fromControls() {fromControls(myObject);}
/**
* Transfer all edited fields from the specified Object to the on-screen controls.
**/
public void toControls(Object obj) {dontSendEachFieldTransferEvent = true; transfer(obj,transfers.iterator(),FieldTransfer.FROM_OBJECT);}
/**
* Transfer all edited fields to the specified Object from the on-screen controls.
**/
public void fromControls(Object obj) {dontSendEachFieldTransferEvent = true; transfer(obj,transfers.iterator(),FieldTransfer.TO_OBJECT);}
/**
* Transfer specified fields from the Object currently being edited to the on-screen controls. The "fields"
argument should be pure field names separated by commas (,).
**/
public void toControls(String fields) {transfer(myObject,fields,null,FieldTransfer.FROM_OBJECT);}
/**
* Transfer specified fields to the Object currently being edited from the on-screen controls. The "fields"
argument should be pure field names separated by commas (,).
**/
public void fromControls(String fields) {transfer(myObject,fields,null,FieldTransfer.TO_OBJECT);}
/**
* Transfer specified fields from the specified Object to the on-screen controls. The "fields"
argument should be pure field names separated by commas (,).
**/
public void toControls(Object obj,String fields){transfer(obj,fields,null,FieldTransfer.FROM_OBJECT);}
/**
* Transfer specified fields to the specified Object from the on-screen controls. The "fields"
argument should be pure field names separated by commas (,).
**/
public void fromControls(Object obj,String fields){transfer(obj,fields,null,FieldTransfer.TO_OBJECT);}
/**
 * This calls toControls(fields) and then notifyDataChange().
 * @param fields a comma separated field list.
 */
//===================================================================
public void dataChanged(String fields)
//===================================================================
{
	toControls(fields);
	notifyDataChange();
}
/**
 * This calls toControls() and then notifyDataChange().
 */
//===================================================================
public void dataChanged()
//===================================================================
{
	toControls();
	notifyDataChange();
}
//===================================================================
private Vector tx = new Vector(), xfer = new Vector();
//===================================================================
protected Vector findFields(String fieldNames,String exclude,Vector dest)
//===================================================================
{
	if (dest == null) dest = new Vector();
	else dest.clear();
	mString.split(fieldNames,',',tx);
	for (int i = 0; i<tx.size(); i++){
		String f = (String)tx.get(i);
		for (int j = 0; j<transfers.size(); j++){
			FieldTransfer ft = (FieldTransfer)transfers.get(j);
			if (!ft.isField(f)) continue;
			if (ft.isField(exclude)) continue;
			dest.add(ft);
		}
	}
	return dest;
}
//===================================================================
protected void transfer(Object obj,String fields,String exclude,int direction)
//===================================================================
{
	dontSendEachFieldTransferEvent = false;
	findFields(fields,exclude,xfer);
	transfer(obj,xfer.iterator(),direction);
}
/**
 * Find the FieldTransfer associated with the specified field.
 * @param name The field to look for.
 * @return The FieldTransfers associated with the field or null if it was not found.
 */
//===================================================================
public FieldTransfer findFieldTransfer(String name)
//===================================================================
{
	for (int i = 0; i<transfers.size(); i++){
		FieldTransfer ft = (FieldTransfer)transfers.get(i);
		if (ft.fieldName.equals(name)) return ft;
	}
	return null;
}
/**
 * Find the FieldTransfer associated with the specified control.
 * @param c The Control associated with the field.
 * @param menuItem true if you are looking for the FieldTransfer associated with
	a MenuItem field for the Control.
 * @return
 */
//===================================================================
public FieldTransfer findFieldTransfer(Control c,boolean menuItem)
//===================================================================
{
	for (int i = 0; i<transfers.size(); i++){
		FieldTransfer ft = (FieldTransfer)transfers.get(i);
		if (ft.dataInterface == c) {
			boolean isMenu = "Lewe/ui/MenuItem;".equals(ft.fieldType);
			if ((isMenu && !menuItem) || (!isMenu && menuItem)) continue;
			return ft;
		}
	}
	return null;
}
/**
 * Find the FieldTransfer associated with the specified control.
 * @param c The Control associated with the field.
 * @return
 */
//===================================================================
public FieldTransfer findFieldTransfer(Control c) {return findFieldTransfer(c,false);}
//===================================================================
/**
 * Find the Control associated with the field.
 * @param fieldName The name of the field.
 * @return The Control associated with the field, or null if one was not found.
 */
//===================================================================
public Control findControlFor(String fieldName)
//===================================================================
{
	FieldTransfer ft = findFieldTransfer(fieldName);
	if (ft == null) return null;
	return (Control)ft.dataInterface;
}
//===================================================================
public void onDataChangeEvent(DataChangeEvent ev)
//===================================================================
{
	if (ev.target == this) return;
	currentEvent = ev;
	Iterator it = new ObjectIterator(findFieldTransfer((Control)ev.target));
	boolean doFire = it.hasNext() && !hasModifier(TakeControlEvents,false) && !((Control)ev.target).hasModifier(NotAnEditor,false);
	fireFieldEvent(it,null,DoDataTransfer|0);
	if (doFire){
		DataChangeEvent dce = new DataChangeEvent(DataChangeEvent.DATA_CHANGED,this);
		dce.cause = ev;
		postEvent(dce);
	}
	currentEvent = null;
}
public static final int DoDataTransfer = 0x80000000;

/**
* This is the event that caused the current fieldChanged() or action() event.
**/
public Event currentEvent;


//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	currentEvent = ev;
	if ((ev.type == ev.PRESSED) && (ev.target instanceof Control)){
		fireFieldEvent(new ObjectIterator(findFieldTransfer((Control)ev.target)),ev,1);
	}
	if (ev.type == MenuEvent.SELECTED && !(ev instanceof ListEvent)){
		fireFieldEvent(new ObjectIterator(findFieldTransfer((Control)ev.target,true)),ev,DoDataTransfer|0);
	}
	if ((ev.type > ev.CANCELLED) && (ev.target instanceof Control)){
		fireFieldEvent(new ObjectIterator(findFieldTransfer((Control)ev.target)),ev,2);
	}
	currentEvent = null;
	super.onControlEvent(ev);
}
//===================================================================
public void fireFieldChanged(String fieldName)
//===================================================================
{
	fireFieldEvent(findFields(fieldName,null,null).iterator(),null,0);
}

//-------------------------------------------------------------------
protected  void fireFieldEvent(FieldListener fl,FieldTransfer ft,Object event,int type)
//-------------------------------------------------------------------
{
	type &= ~DoDataTransfer;
	ft.fieldEvent = event;
	if (/*false && */(coroutineStackSize <= 0 || ewe.sys.Coroutine.getCurrent() != null)) {
		switch(type){
			case 0: fl.fieldChanged(ft,this); break;
			case 1: fl.action(ft,this); break;
			case 2: fl.fieldEvent(ft,this,event); break;
		}
		ft.fieldEvent = null;
	}else {
		new editorFirerer(ft,this,fl,type,event);
	}
}
//-------------------------------------------------------------------
protected boolean handleTransferError(Exception e,FieldTransfer ft,int direction)
//-------------------------------------------------------------------
{
	return false;
}
//===================================================================
public void fireFieldEvent(Iterator it,Object event,int type)
//===================================================================
{
	while(it.hasNext()){
		FieldTransfer ft = (FieldTransfer)it.next();
		if ((type & DoDataTransfer) != 0) {
			try{
				ft.transfer(myObject,ft.TO_OBJECT);
				postEditorEvent(EditorEvent.FROM_CONTROLS,ft);
			}catch(Exception e){
				if (!handleTransferError(e,ft,ft.TO_OBJECT))
					ft.transfer(myObject,ft.FROM_OBJECT);
			}
		}

		for (int i = 0; i<fieldListeners.size(); i++)
			fireFieldEvent((FieldListener)fieldListeners.get(i),ft,event,type);
		fireFieldEvent(this,ft,event,type);
	}
}
/**
* This will modify field controls in the editor conditionally. If "condition" is true, then the
* turnOn flags will be set and the turnOff flags will be cleared. If it is false then the reverse will
* be done. If the flags have changed then the control will be repainted if repaint is true.
**/
//===================================================================
public void modifyFields(String fields,boolean condition,int turnOn,int turnOff,boolean repaint)
//===================================================================
{
	String [] all = mString.split(fields,',');
	for (int i = 0; i<all.length; i++){
		FieldTransfer ft = findFieldTransfer(all[i]);
		if (ft == null) continue;
		Control c = (Control)ft.dataInterface;
		if (c == null) continue;
		int flags = c.getModifiers(false);
		if (condition) c.modify(turnOn,turnOff);
		else c.modify(turnOff,turnOn);
		if (!repaint) continue;
		if (c.getModifiers(false) == flags) continue;
		c.repaintNow();
	}
}
//===================================================================
public static void main(String args[]) throws Exception
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	Editor ed = new Editor();
	Class c = Class.forName(args[0]);
	ed.setFields(c.newInstance());
	ed.title = c.getName();
	ed.execute();
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

//##################################################################
class editorFirerer implements Runnable{
//##################################################################

int which;
FieldTransfer ft;
Editor ed;
FieldListener fl;
Object event;
public editorFirerer(FieldTransfer ft,Editor ed,FieldListener fl,int which,Object event)
{
	this.which = which;
	this.ft = ft;
	this.ed = ed;
	this.fl = fl;
	this.event = event;
	new ewe.sys.Coroutine(this,ed.coroutineStackSize);
}

public void run()
{
	switch(which){
		case 0: fl.fieldChanged(ft,ed); return;
		case 1: fl.action(ft,ed); return;
		case 2: fl.fieldEvent(ft,ed,event); return;
	}
	ft.fieldEvent = null;
}
//##################################################################
}
//##################################################################


