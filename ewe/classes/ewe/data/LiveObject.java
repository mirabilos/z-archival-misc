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
package ewe.data;
import ewe.util.*;
import ewe.ui.*;
import ewe.reflect.*;
/**
* This is a complete implementation of LiveData which you can use as a
* base for creating new LiveData Objects.
**/
//##################################################################
public class LiveObject extends DataObject implements LiveData{
//##################################################################
/**
* By default this returns the String "unnamed".
**/
//===================================================================
public String getName() {return "unnamed";}
//===================================================================
/**
* This shoudl create the new Editor object but not add anything to it. This is called
* by getEditor(). By default it returns a standard Editor object.
**/
//-------------------------------------------------------------------
protected Editor makeNewEditor(int options)
//-------------------------------------------------------------------
{
	return new Editor();
}
/**
This is called by the default getEditor() should return a ScrollBarPanel for the CellPanel that the editor
is being placed in. By default, this is a ScrollBarPanel except on a
SmartPhone, where it will be a VerticalScrollPanel.<p>
You may return null from this.
**/
//-------------------------------------------------------------------
protected ScrollablePanel getEditorScroller(CellPanel cp)
//-------------------------------------------------------------------
{
	ScrollablePanel sp = (Gui.isSmartPhone) ? new VerticalScrollPanel(new ScrollableHolder(cp)) : new ScrollBarPanel(new ScrollableHolder(cp));
	sp.setClientConstraints(cp.HEXPAND|cp.HCONTRACT|cp.VEXPAND|cp.VCONTRACT);
	//sp.shrinkComponent = true;
	return sp;
}
/**
* This creates a new Editor Object - you should not override this method,
* rather you should override the addToPanel() method to provide a custom
* Editor for your LiveData.
**/
//===================================================================
public Editor getEditor(int options)
//===================================================================
{
	Editor ed = makeNewEditor(options);
	ed.objectClass = Reflect.getForObject(this);
	ed.sampleObject = this;
	CellPanel cp = new CellPanel();
	ScrollablePanel sp = getEditorScroller(cp);
	if (sp != null) ed.getProperties().set("MainScrollBarPanel",sp);
	CellPanel cp2 = (CellPanel)ed.getProperties().getValue("EditorContents",null);
	if (cp2 == null) ed.addLast(sp == null ? cp : sp);
	else cp2.addLast(sp == null ? cp : sp);
	addToPanel(cp,ed,options);
	ed.setObject(this);
	return ed;
}
/**
 * Enable/Disable an Editors main scrollbar panel.<p>
 * By default, the getEditor() method will place a CellPanel into a ScrollBarPanel and then add it the Editor
 * before calling addToPanel(). However initially this ScrollBarPanel is disabled (i.e. it's "shrinkComponent" variable
 * is true - which results in ScrollBars never appearing). This method is used to enable it.
 * @param ed The editor created by getEditor().
 * @param enable true to enable scrolling, false to disable it.
 */
//===================================================================
public static void enableEditorScrolling(Editor ed,boolean enable)
//===================================================================
{
	if (ed != null) ed.enableScrolling(enable);
}
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
* @param ft The FieldTransfer object representing the changed field.
* @param ed The Editor from which the event came.
**/
//===================================================================
public void action(FieldTransfer ft,Editor ed) {action(ft.fieldName,ed);}
//===================================================================
/**
* This implements the fieldChanged() method in FieldListener. It operates like
* like this:<p>
* If the field is of type ewe.ui.MenuItem then the method action(String name,Editor ed)
* is called with the "name" parameter being set to the "action" member of the selected MenuItem.
* <p>Otherwise the fieldChanged(String fieldName,Editor ed) method is called with the fieldName
* of the FieldTransfer object.
* @param ft The FieldTransfer object representing the changed field.
* @param ed The Editor from which the event came.
*/
//===================================================================
public void fieldChanged(FieldTransfer ft,Editor ed)
//===================================================================
{
	if ("Lewe/ui/MenuItem;".equals(ft.fieldType)){
		Object got = ft.getFieldValue(ed.myObject);
		if (got instanceof MenuItem){
			action(((MenuItem)got).action,ed);
		}
	}else
		fieldChanged(ft.fieldName,ed);
}
/**
* This implements the fieldEvent() method in FieldListener. By default it
* calls fieldChanged(String name,Editor ed) UNLESS the field is a MenuItem,
* in which case it will call action(String menuItemAction,Editor ed);
**/
//===================================================================
public void fieldEvent(FieldTransfer ft,Editor ed,Object event) {}
//===================================================================
/**
* Override this to provide the user interface for editing the Object.
* Add all your controls to the CellPanel cp, and your fields to the Editor ed.
* The options parameter may be used to specify a different Editor for the
* Object for different situations - it is the same parameter that is passed
* to the LiveData.getEditor(int options) call.
**/
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int options)
//===================================================================
{
	Vector v = getClassList(this,"addToPanel(Lewe/ui/CellPanel;Lewe/ui/Editor;I)V");
	if (v.size() != 0){
		UIBuilder u = UIBuilder.newInputStack(cp,ed,this);
		for (int i = 0; i<v.size(); i++){
			String fields = getFieldList(v.get(i),this,true);
			if (fields.length() != 0){
				u.setFormats((String)getDeclaredFieldValue("_formats",((Reflect)v.get(i)).getClassName()));
				u.addAll(fields);
			}
		}
		u.close(true);
	}
}
/**
* Use this within addToPanel() if you still want it's default of creating
* a new UIBuilder and adding an InputStack. It will also add in the fields for this object if needed.
* @param cp The CellPanel passed to addToPanel()
* @param ed The Editor passed to addToPanel()
* @param baseClassName The base class being added.
* @return The UIBuilder for the Editor with an open InputStack.
* @exception IllegalArgumentException if the baseClassName is not valid for this object.
*/
//-------------------------------------------------------------------
protected UIBuilder addMeToPanel(CellPanel cp,Editor ed,String baseClassName) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	UIBuilder b = UIBuilder.newInputStack(cp,ed,this);
	if (baseClassName != null) b.setFormats((String)getDeclaredFieldValue("_formats",baseClassName)).addAll(getMyFieldList(baseClassName));
	return b;
}
/**
* You can override this to encode the object as a String, but it is easier to override
* encode(TextEncoder te).
**/
//===================================================================
public String textEncode()
//===================================================================
{
	TextEncoder te = new TextEncoder();
	encode(te);
	return encoded(te.toString());
}
/**
* You can override this to decode a String representation of the Object as
* encoded by textEncode, but it is easier to override decode(TextDecoder te).
**/
//===================================================================
public void textDecode(String txt)
//===================================================================
{
	TextDecoder td = new TextDecoder(txt);
	decode(td);
	decoded(txt);
}
/**
* You can call this within your encode(TextEncoder te) method to encode specific
* fields.
* @param fieldNames A comma separated list of field names.
* @param te A TextEncoder object.
* @param baseName Either the fully qualified class and package name or the class name (without package) of the object.
* @return The te parameter is returned after the fields are added.
*/
//===================================================================
public TextEncoder encodeFields(String fieldNames,TextEncoder te,String baseName)
//===================================================================
{
	Reflect r = getReflect(this,baseName);
	if (te == null) te = new TextEncoder();
	if (r != null){
		String [] all = mString.split(fieldNames,',');
		for (int i = 0; i<all.length; i++){
			Field f = r.getField(all[i],0);
			if (f == null) continue;
			te.addValue(all[i],te.toString(f,this));
		}
	}
	return te;
}
/**
* You can call this within your decode(TextDecoder te) method to encode specific
* fields.
* @param fieldNames A comma separated list of field names.
* @param td A TextDecoder object.
* @param baseName Either the fully qualified class and package name or the class name (without package) of the object.
* @return The td parameter is returned after the fields are extracted.
*/
//===================================================================
public TextDecoder decodeFields(String fieldNames,TextDecoder td,String baseName)
//===================================================================
{
	Reflect r = getReflect(this,baseName);
	if (td == null) return td;
	if (r != null){
		String [] all = mString.split(fieldNames,',');
		for (int i = 0; i<all.length; i++){
			String s = td.getValue(all[i]);
			if (s == null) continue;
			Field f = r.getField(all[i],0);
			if (f == null) continue;
			TextEncoder.fromString(f,this,s);
		}
	}
	return td;
}
/**
* This returns null by default.
**/
//===================================================================
public ewe.fx.IImage getIcon() {return null;}
//===================================================================
/**
* Override this to encode the fields that you want to encode. The LiveObject implementation of
* this automatically encodes all the fields for each class in the hierarchy that does not
* override encode(TextEncoder te);
**/
//-------------------------------------------------------------------
protected TextEncoder encode(TextEncoder te)
//-------------------------------------------------------------------
{
	Vector v = getClassList(this,"encode(Lewe/util/TextEncoder;)Lewe/util/TextEncoder;");
	for (int i = 0; i<v.size(); i++){
		Reflect r = (Reflect)v.get(i);
		//ewe.sys.Vm.debug("Encoding: "+r.getClassName());
		encodeFields(getFieldList(r,this,true),te,r.getClassName());
	}
	return te;
}
/**
* Override this to decode the fields that you want to decode.  The LiveObject implementation of
* this automatically decodes all the fields for each class in the hierarchy that does not
* override decode(TextDecoder td);
**/
//-------------------------------------------------------------------
protected TextDecoder decode(TextDecoder td)
//-------------------------------------------------------------------
{
	Vector v = getClassList(this,"decode(Lewe/util/TextDecoder;)Lewe/util/TextDecoder;");
	for (int i = 0; i<v.size(); i++){
		Reflect r = (Reflect)v.get(i);
		//ewe.sys.Vm.debug("Decoding: "+r.getClassName());
		decodeFields(getFieldList(r,this,true),td,r.getClassName());
	}
	return td;
}
/**
* This opens an Editor for this object.
* @param title The title for the editor. Can be null to leave as is.
* @param editorOption Options to pass to getEditor().
* @return true if the user pressed OK, false if the user pressed cancel.
*/
//===================================================================
public boolean input(String title,int editorOption)
//===================================================================
{
	Editor ed = getEditor(editorOption);
	if (title != null) ed.title = title;
	Gui.setOKCancel(ed);
	return (ed.execute() != ed.IDCANCEL);
}
/**
* This method is called after the base implementation of textDecode() is executed. It gives
* you a chance to do extra work after the fields are decoded.
* @param from The String this object was decoded from.
*/
//-------------------------------------------------------------------
protected void decoded(String from){}
//-------------------------------------------------------------------
/**
* This method is called after the base implementation of textEncode() is executed. It gives
* you a chance to do extra work and maybe modify the data after the fields are encoded.
* @param to the String the object has been encoded to.
* @return the String that should be used as the encoded text for the object. By default this
* method simply returns the to parameter.
*/
//-------------------------------------------------------------------
protected String encoded(String to){return to;}
//-------------------------------------------------------------------
/**
* This works on any object. It adds the fields for an object into a CellPanel that itself is
* added to the editor (or which itself may be the Editor).
* @param cp A CellPanel to add the object fields to.
* @param ed The Editor to be used to edit the object.
* @param objectOrClass The object or the Class/Reflect of the object.
* @param declaredOnly if this is true then only the declared fields will be added
* and not the fields of the superclass.
* @return the UIBuilder that was used to add the fields. The builder will have an open
* InputStack which contains the fields added.
* @exception IllegalArgumentException
*/
//===================================================================
public static UIBuilder addObjectToPanel(CellPanel cp,Editor ed,Object objectOrClass,boolean declaredOnly) throws IllegalArgumentException
//===================================================================
{
	UIBuilder b = UIBuilder.newInputStack(cp,ed,objectOrClass);
	Object data = Reflect.toNonReflect(objectOrClass);
	if (data != null) b.setFormats(appendAllFields("_formats",data,declaredOnly));
	b.addAll(getFieldList(objectOrClass,data,declaredOnly));
	return b;
}
/**
Get a Form to run this LiveObject as a stand-alone application.
**/
//===================================================================
public Form runAsApp()
//===================================================================
{
	Editor ed = getEditor(0);
	if (ed != null) {
		ed.enableScrolling(true);
		ed.exitSystemOnClose = true;
	}
	return ed;
}
//##################################################################
}
//##################################################################

