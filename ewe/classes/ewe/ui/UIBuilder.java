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
import ewe.reflect.*;
import ewe.util.*;
import ewe.data.LiveData;
import ewe.data.PropertyList;
import ewe.data.FieldData;
import ewe.data.InputObject;
/**
* The UIBuilder class provides a very easy way of building a user interface using an Editor
* and an editable object. See the Programmer's Guide for instructions on how to use it in
* detail.
**/
//##################################################################
public class UIBuilder{
//##################################################################
/**
* This is the Editor that the UIBuilder is building.
**/
public Editor editor;
/**
* This is a ewe.reflect.Reflect object representing the object being edited.
**/
public Reflect objectClass;
/**
* This is an instance of the object being edited (this is allowed to be null).
**/
public Object editedObject;

public static int inputStackLength = 20;

Hashtable formats = new Hashtable();

private static Type fileInput = new Type("ewe.ui.mFileInput");
/**
 * Set the formats for the fields that will be added.
 * @param formats A format specifier string or null to clear the formats.
 * @return This UIBuilder.
 */
//===================================================================
public UIBuilder setFormats(String formats)
//===================================================================
{
	this.formats.clear();
	if (formats != null) {
		Vector v =
			FieldData.makeFieldsFromEncodedString(Reflect.bestReference(editedObject,objectClass),formats,null,true);
		for (int i = 0; i<v.size(); i++){
			FieldData fd = (FieldData)v.get(i);
			this.formats.put(fd.ft.fieldName,fd);
		}
	}
	return this;
}
//===================================================================
public UIBuilder(){}
//===================================================================

/**
 * This is used to set an example of the data being edited.
 * This is necessary only if you are adding a field that is of type LiveData, and you are
	asking the UIBuilder to automatcially create and add the editor for it.
 * @param object An example of the object to be edited.
 */
//===================================================================
public void setEditedObject(Object object)
//===================================================================
{
	editedObject = object;
}
/**
* Create a UIBuilder for an Editor.
* @param ed The editor being built. It assumes that the object being edited is
* the editor itself.
*/
//===================================================================
public UIBuilder(Editor ed) {this(ed,ed,null);}
//===================================================================
/**
 * Create a UIBuilder for an Editor which will edit a specific type of object.
 * @param ed The editor being built
 * @param classOrObject This is either a java.lang.Class object representing the object to be
	edited, or a ewe.reflect.Reflect representing the object to be edited, or an instance of the
	object to be edited.
 */
//===================================================================
public UIBuilder(Editor ed,Object classOrObject) {this(ed,classOrObject,null);}
//===================================================================


/**
 * Create a UIBuilder for an Editor which will edit a specific type of object, given a CellPanel
	that has already been added to the Editor as the starting point.
 * @param ed The editor being built
 * @param classOrObject This is either a java.lang.Class object representing the object to be
	edited, or a ewe.reflect.Reflect representing the object to be edited, or an instance of the
	object to be edited.
 * @param cp A CellPanel that has already been added to the Editor and that will be used as the
	start point for adding controls.
 */
//===================================================================
public UIBuilder(Editor ed,Object classOrObject,CellPanel cp)
//===================================================================
{
	if (cp == null) ed.addLast(cp = new CellPanel());
	Reflect r = null;
	if (classOrObject instanceof Class) r = new Reflect((Class)classOrObject);
	else if (classOrObject instanceof Reflect)  r = (Reflect)classOrObject;
	else if (classOrObject != null) {
		r = Reflect.getForObject(classOrObject);
		editedObject = classOrObject;
	}else r = ed.objectClass;
	if (r == null)
		throw new RuntimeException("Cannot determine the editing object class!");
	ed.objectClass = r;
	Vector.push(openPanels,cp);
	editor = ed;
	objectClass = r;
}

protected Vector openPanels = new Vector();

//===================================================================
public void setObjectClass(Object objectOrClass)
//===================================================================
{
	objectClass = Reflect.toReflect(objectOrClass);
	editedObject = Reflect.toNonReflect(objectOrClass);
}
/**
 * Return the current open CellPanel.
 */
//===================================================================
public CellPanel getOpenPanel()
//===================================================================
{
	if (openPanels.size() < 1) return null;
	return (CellPanel)openPanels.get(0);
}
/**
 * Close the current cell panel. Any further controls will be added to the
	cell panel that was open before this panel was opened.
 * @return The current cell panel that was closed.
 */
//===================================================================
public CellPanel close() {return close(false);}
//===================================================================
/**
 * Close the current cell panel. Any further controls will be added to the
	cell panel that was open before this panel was opened.
* @param endRow If this is true then the previous open panel will have its current row ended.
 * @return The current CellPanel that was closed.
*/
//===================================================================
public CellPanel close(boolean endRow)
//===================================================================
{
	CellPanel ret = getOpenPanel();
	if (ret != null) openPanels.del(0);
	if (endRow) {
		CellPanel p = getOpenPanel();
		if (p != null) p.endRow();
	}
	return ret;
}
/**
 * Set the specified CellPanel as being the current open panel.
 * This is only used under special circumstances. The CellPanel specified is not
	added to any open panel - you will have to add it into the editor yourself.
 * @param cp The CellPanel to be considered the current open panel.
 * @return The CellPanel provided.
 */
//===================================================================
public CellPanel addTo(CellPanel cp)
//===================================================================
{
	Vector.push(openPanels,cp);
	return cp;
}

/**
 * Set a new CellPanel as the current open panel and add it to the previously open panel.
 * @param cp The CellPanel to be added to the current open panel.
 * @param text Optional text for the CellPanel.
 * @return The opened CellPanel.
 */
//===================================================================
public CellPanel open(CellPanel cp,String text)
//===================================================================
{
	CellPanel parent = getOpenPanel();
	if (parent instanceof InputStack) ((InputStack)parent).add(cp,null);
	else parent.addNext(cp);
	Vector.push(openPanels,cp);
	if (text != null)
		if (text.length() == 0) {
			cp.borderWidth = 4;
			cp.borderStyle = cp.EDGE_ETCHED|(ButtonObject.buttonEdge & cp.BF_SOFT);
		}
		else cp.setText(text);
	if (cp instanceof InputStack)
		((InputStack)cp).inputLength = inputStackLength;
	return cp;
}
/**
 * Open a new CellPanel within an already added Multipanel.
 * @param mp The MultiPanel you have added already.
 * @param tabName The tabName for the new panel to add.
 * @param longName The longName for the new panel to add.
 * @return The newly created and added CellPanel.
 */
//===================================================================
public CellPanel open(MultiPanel mp,String tabName,String longName)
//===================================================================
{
	return open(new CellPanel(),mp,tabName,longName);
}
/**
 * Open a specified CellPanel within an already added Multipanel.
 * @param mp The MultiPanel you have added already.
 * @param tabName The tabName for the new panel to add.
 * @param longName The longName for the new panel to add.
 * @return The CellPanel provided.
 */
//===================================================================
public CellPanel open(CellPanel cp,MultiPanel mp,String tabName,String longName)
//===================================================================
{
	mp.addItem(cp,tabName,longName);
	return addTo(cp);
}

	//##################################################################
	class stackCellPanel extends CellPanel{
	//##################################################################


	//##################################################################
	}
	//##################################################################


/**
 * Open a new CellPanel within the current open CellPanel.
 * @return The newly created and added CellPanel.
 */
//===================================================================
public CellPanel open() {return open(new CellPanel(),null);}
//===================================================================
/**
 * Open a new Stack within the current open CellPanel.
 * A Stack is a CellPanel that always places controls one on top the other. This is DIFFERENT to an InputStack
	which handles both inputs and prompts.
 * @return The newly created and added CellPanel.
 */
//===================================================================
public CellPanel openStack() {return open(new stackCellPanel(),null);}
//===================================================================
/**
 * Open a new ewe.ui.InputStack within the current open CellPanel.
 * An InputStack handles aligning prompts with inputs stacked in rows and columns.
 * @return The newly created and added InputStack.
 */
//===================================================================
public InputStack openInputStack() {return (InputStack)(open(new InputStack(),null).setCell(Control.HSTRETCH));}
//===================================================================
/**
 * Open a new CellPanel within the current open CellPanel.
 * @param text A text that is used as a heading with etched border for the CellPanel.
 * @return The newly created and added CellPanel.
 */
//===================================================================
public CellPanel open(String text) {return open(new CellPanel(),text);}
//===================================================================
/**
 * Open a new Stack within the current open CellPanel.
 * A Stack is a CellPanel that always places controls one on top the other. This is DIFFERENT to an InputStack
	which handles both inputs and prompts.
 * @param text A text that is used as a heading with etched border for the Stack.
 * @return The newly created and added CellPanel.
 */
//===================================================================
public CellPanel openStack(String text) {return open(new stackCellPanel(),text);}
//===================================================================
/**
 * Open a new ewe.ui.InputStack within the current open CellPanel.
 * An InputStack handles aligning prompts with inputs stacked in rows and columns.
 * @param text A text that is used as a heading with etched border for the InputStack.
 * @return The newly created and added InputStack.
 */
//===================================================================
public InputStack openInputStack(String text) {return (InputStack)(open(new InputStack(),text).setCell(Control.HSTRETCH));}
//===================================================================


/**
 * This ends the row in the current open CellPanel. This is not necessary if you used openInputStack()
 * or openStack().
 */
//===================================================================
public void endRow(){getOpenPanel().endRow();}
//===================================================================


//===================================================================
Reflect checkClass(Reflect now,String name)
//===================================================================
{
	if (now != null) return now;
	return Reflect.getForName(name);
}

private static Reflect TimeClass, FileClass, MultiListSelectClass, SingleListSelectClass, LiveDataClass;


/**
 * This returns the appropriate Control for a given field.
* @param prompt The prompt for the control (may be null).
* @param ft The FieldTransfer representing the field for the control.
* @param forInputStack This should be true if the control is to be added to an InputStack.
* @param properties This is either a ewe.data.PropertyList object, or a set of properties encoded as
a String separated by '|' characters. (e.g. "rows|10|columns|30").
* @return The appropriate Control for the field or null if no appropriate control could be found.
*/
//===================================================================
public Control getControlFor(String prompt,FieldTransfer ft,boolean forInputStack,Object properties,FieldData fd)
//===================================================================
{
	Control ret = null;
	try{
		String type = ft.fieldType;
		Object value = editedObject == null ? null : ft.getFieldValue(editedObject);
		PropertyList pl = PropertyList.toPropertyList(properties);
		switch(type.charAt(0)){
			case 'Z':
				if (forInputStack) return ret = new mCheckBox();
				else return ret = new mCheckBox(prompt);
			case 'I':
			case 'D':
			case 'F':
			case 'S':
			case 'J':
				return ret = new mInput();
		}
		if (type.equals("Ljava/lang/String;")) {
			int rows = pl.getInt("rows",fd != null ? fd.height : 1), cols = pl.getInt("cols",fd != null ? fd.length : -1);
			if (rows != 1 && cols == -1) cols = 40;
			if (rows == 1){
				mInput mi = new mInput();
				if (cols != -1) mi.columns = cols;
				return ret = mi;
			}else
				return ret =new mTextPad(rows,cols);
		}else if (Reflect.isTypeOf(type,"ewe.data.EditableData")){
			if (editedObject == null) throw new RuntimeException("Field: "+ft.fieldName+" is of type EditableObject - but you have not called setEditedObject().");
			else {
				return ret = ((ewe.data.EditableData)value).getControl();
			}
		//}else if (Reflect.isTypeOf(type,"ewe.sys.TimeOfDay")){
			//return new mInput();
		}else if (Reflect.isTypeOf(type,"ewe.fx.ImageBytes")|| Reflect.isTypeOf(type,"ewe.fx.IImage")){
			ImageControl ic = new ImageControl(null);
			ic.setPreferredSize(100,100).setBorder(ic.EDGE_ETCHED,2);
			return ic;
		}else {
			Reflect r = Reflect.getForName(type);
			if (r == null) return null;
			if ((TimeClass = checkClass(TimeClass,"ewe.sys.Time")).isAssignableFrom(r))
				return ret = new DateTimeInput();//new DateDisplayInput();
			else if ((FileClass = checkClass(FileClass,"ewe.io.File")).isAssignableFrom(r)){
				if (!fileInput.exists()) return new mInput();
				return (Control)fileInput.newInstance("(Lewe/data/PropertyList;)V",new Object[]{pl});
				/*
				*/
			}else if ((SingleListSelectClass = checkClass(SingleListSelectClass,"ewe.data.MultiListSelect$SingleListSelect")).isAssignableFrom(r))
				return ret = new ListSelect(true,prompt,false);
			else if ((MultiListSelectClass = checkClass(MultiListSelectClass,"ewe.data.MultiListSelect")).isAssignableFrom(r))
				return ret = new ListSelect(false,prompt,false);
			else if ((LiveDataClass = checkClass(LiveDataClass,"ewe.data.LiveData")).isAssignableFrom(r)){
				if (editedObject == null) throw new RuntimeException("Field: "+ft.fieldName+" is of type LiveData - but you have not called setEditedObject().");
				else {
					if (!(value instanceof LiveData)) return null;
					return ret = ((LiveData)value).getEditor(pl.getInt("editor",0));
				}
			}else
				return ret = new mInput();
		}
	}finally{
		if (ret != null && fd != null)
			if (!fd.editable) ret.modifyAll(Control.DisplayOnly,0);
	}
}
/**
 * This returns the appropriate Control for a given field.
* @param prompt The prompt for the control (may be null).
* @param ft The FieldTransfer representing the field for the control.
* @param forInputStack This should be true if the control is to be added to an InputStack.
* @param properties This is either a ewe.data.PropertyList object, or a set of properties encoded as
a String separated by '|' characters. (e.g. "rows|10|columns|30").
* @return The appropriate Control for the field or null if no appropriate control could be found.
* @exception RuntimeException If no Control could be created.
*/
//===================================================================
public Control getControlFor(String prompt,String field,boolean forInputStack,Object properties) throws RuntimeException
//===================================================================
{
	FieldTransfer ft = new FieldTransfer(objectClass,field);
	if (ft.fieldType == null) throw new RuntimeException("Cannot create control for: "+field);
	Control ret = getControlFor(prompt,ft,forInputStack,properties,(FieldData)formats.get(field));
	if (ret == null) throw new RuntimeException("Cannot create control for: "+field);
	return ret;
}
protected Object properties;
//-------------------------------------------------------------------
protected Control getControlFor(String prompt,String field,boolean forInputStack) throws RuntimeException
//-------------------------------------------------------------------
{
	try{
		return getControlFor(prompt,field,forInputStack,properties);
	}finally{
		properties = null;
	}
}
/**
 * This sets the properties of the next control to be added. It should be called immediately before one
	* of the add() methods.
* @param properties This is either a ewe.data.PropertyList object, or a set of properties encoded as
a String separated by '|' characters. (e.g. "rows|10|columns|30").
* @return This UIBuilder.
*/
//===================================================================
public UIBuilder set(Object properties)
//===================================================================
{
	this.properties = properties;
	return this;
}
//===================================================================
public Control addChoice(String prompt,String field,String [] choices)
//===================================================================
{
	return add(prompt,field,new mChoice(choices,0));
}
//===================================================================
public Control addCombo(String prompt,String field,String [] choices)
//===================================================================
{
	return add(prompt,field,new mComboBox(choices,0));
}
//===================================================================
public Control add(String prompt,String field){return add(prompt,field,null);}
//===================================================================
//===================================================================
public Control add(String field,Control c) {return add(null,field,c);}
//===================================================================
public Control add(Control c) {return add(null,null,c);}
//===================================================================
public Control add(String prompt,String field,Control c)
//===================================================================
{
	CellPanel cp = getOpenPanel();
	if (prompt == null && field != null) {
		FieldData fd = (FieldData)formats.get(field);
		if (fd != null) prompt = fd.header;
		if (prompt == null)
			prompt = InputStack.nameToPrompt(field);
		if (cp instanceof InputStack) prompt = InputStack.appendToPrompt(prompt,":");
	}else{
		//new Exception().printStackTrace();
	}
	if (cp instanceof InputStack){
		if (c == null) c = getControlFor(prompt,field,true);
		if (field != null) editor.addField(c,field);
		if (c instanceof mTextPad) c = new ScrollBarPanel((ScrollClient)c);
		((InputStack)cp).add(c,prompt);
		return c;
	}else{
		if (c == null) c = getControlFor(prompt,field,false);
		if (field != null) editor.addField(c,field);
		if (c instanceof mTextPad) c = new ScrollBarPanel((ScrollClient)c);
		cp.addNext(c);
		if (cp instanceof stackCellPanel) cp.endRow();
		return c;
	}
}
//===================================================================
public Control add(String field) {return add(null,field,null);}
//===================================================================
/**
 * Add all the specified fields.
 * @param promptsAndFields If this string contains the '|' (pipe) character,
 * then it is assumed to be a list of the form: "Prompt1|Field1|Prompt2|Field2|..."
 * If there are no '|' characters then it is assumed to be a comma separated list of
 * fields in the form "Field1,Field2,..." and the field names will then be converted
 * to prompt names.
 * @return An array of the controls added.
 */
//===================================================================
public Control [] addAll(String promptsAndFields)
//===================================================================
{
	if (promptsAndFields.indexOf('|') != -1){
		String [] pf = mString.split(promptsAndFields,'|');
		Control [] ret = new Control[pf.length/2];
		for (int i = 0; i<pf.length-1; i+=2)
			ret[i/2] = add(pf[i].length() == 0 ? null : pf[i],pf[i+1]);
		return ret;
	}else{
		String [] pf = mString.split(promptsAndFields,',');
		Control [] ret = new Control[pf.length];
		for (int i = 0; i<pf.length; i++){
			ret[i] = add(null,pf[i]);
		}
		return ret;
	}
}

/**
 * A quick way of creating a new UIBuilder and opening an InputStack.
 * @param ed The editor being added to.
 * @param objectOrClass The objectOrClass being edited.
 * @return A new UIBuilder for the editor.
 */
//===================================================================
public static UIBuilder newInputStack(CellPanel cp,Editor ed,Object objectOrClass)
//===================================================================
{
	UIBuilder ui = new UIBuilder(ed,objectOrClass,cp);
	ui.openInputStack();
	return ui;
}
/**
 * A quick way of creating a new UIBuilder and opening a panel.
 * @param ed The editor being added to.
 * @param objectOrClass The objectOrClass being edited.
 * @return A new UIBuilder for the editor.
 */
//===================================================================
public static UIBuilder newPanel(CellPanel cp,Editor ed,Object objectOrClass)
//===================================================================
{
	UIBuilder ui = new UIBuilder(ed,objectOrClass,cp);
	ui.open();
	return ui;
}
/**
 * A quick way of creating a new UIBuilder and opening a stack (NOT an InputStack).
 * @param ed The editor being added to.
 * @param objectOrClass The objectOrClass being edited.
 * @return A new UIBuilder for the editor.
 */
//===================================================================
public static UIBuilder newStack(CellPanel cp,Editor ed,Object objectOrClass)
//===================================================================
{
	UIBuilder ui = new UIBuilder(ed,objectOrClass,cp);
	ui.openStack();
	return ui;
}
//===================================================================
public static String askClassName(String pathToClass,StringBuffer programDir,Frame parent)
//===================================================================
{
	String target = pathToClass;
	final String cls = target.replace('\\','.').replace('/','.').substring(0,target.length()-6);
	String className = null;
	int idx = cls.toLowerCase().indexOf("classes.");
	if (idx != -1)
		className = cls.substring(idx+8);
	else{
		PropertyList pl = new InputObject(){
			protected void setupInputStack(InputStack is,Editor ed){
				ed.windowFlagsToClear = Window.FLAG_HAS_TITLE;
				Vector v = new Vector();
				String got = cls;
				while(true){
					v.add(0,got);
					int i = got.indexOf('.');
					if (i == -1) break;
					got = got.substring(i+1);
				}
				String [] all = new String[v.size()];
				v.copyInto(all);
				ed.addField(is.addChoice("Class:",all,0),"className$");
			}
		}.input(parent,"Select Class Name",null);
		if (pl == null) return null;
		className = pl.getString("className","<none>");
	}
	if (className == null) return null;
	String path = target.substring(0,target.length()-className.length()-1-6);
	if (path.toLowerCase().endsWith("classes")) path = path.substring(0,path.length()-8);
	if (programDir != null) programDir.append(path);
	return className;
}

/*
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	Editor ed = new Editor();
	UIBuilder ui = new UIBuilder(ed,new samples.data.BigPersonInfo());
	ui.openInputStack("Personal Data");
	ui.addAll("Last Name:|lastName|First Names:|firstNames");
	Control c = (mChoice)ui.addChoice("Gender","gender",new String[]{"Female","Male"});
	c.modify(c.PreferredSizeOnly,0);
	ui.addAll("DOB:|dob|Smoker:|smoker");
	ui.set("masks|*.html,*.htm - HTML Pages;*.txt - Text Pages").add("Home Page:","homePage");
	ui.close(true).setCell(ed.HSTRETCH);
	ui.open();
	ui.add("Sports","sports");
	ui.close(true);
	ui.open("Spouse");
	ui.set("editor|2").add("spouse");
	ui.close(true);
	ui.open("Buttons");
	ui.add("hello",new mButton("Hello"));
	ui.add("there",new mButton("There"));
	ui.close(true).setCell(ed.HSTRETCH);
	ed.title = "Person Info";
	ed.resizable = true;
	ed.setObject(new samples.data.BigPersonInfo());
	ed.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

