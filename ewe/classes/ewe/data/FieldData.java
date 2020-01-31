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
import ewe.ui.*;
import ewe.util.*;
import ewe.reflect.*;
import ewe.fx.*;

/**

A FieldData class is used to specify information about a particular item of data
stored in an Object and to transfer that data to and from the Object.
Usually a FieldData refers directly to a member variable in a Class and so transferred
by direct reads/writes to that variable. However a FieldData can also refer to an abstract
field and a special <b>_getSetField()</b> method is used to read and write the data.<p>

Apart from the specifics of the data itself a FieldData Object also holds information on
a Header for the data (a more descriptive name),
how the data is to be displayed or edited and even how big it should be by default on screen.<p>

The fieldName must be of the form:<p>
<pre>
&LT;fieldName>[$[&LT;type>]]
</pre>
<b>fieldName</b> must be a symbolic fieldName that should be of a form acceptable as a Java
variable name. This means it must start with a letter or '_' and can then contain only numbers,
letters and the '_' character.<p>
The <b>$</b> symbol at the end of the fieldName is used to specify the type of the field
<b>if</b> the type cannot be determined through reflection discovery. For example if the
data object does not have a variable with the specified field name (say it uses
_getSetField() for data transfer instead) but does not provide the _getFieldType(String fieldName)
method then there will be no way to determine the type of the field. The '$' character at
the end of the field name either specifies that the field is a String (in which case NO characters
follow the '$') or it is followed by a Java Type specifier.<p>

A Java Type specifier is either one of the primitive types:
'Z'(boolean),'B'(byte),'C'(char),'S'(short),'I'(int),'D'(double),'J'(long),'F'(float) or
an Object type: 'L&LT;fully_qualified_class_name>;'(Object) or '[&LT;Java Type>'(Array).<p>
When using the 'L&LT;fully_qualified_class_name>;' syntax you must use '/' instead of '.' as the
package separator.<p>
Examples:<br>
<b>fieldName</b> - A field named "fieldName".<br>
<b>fieldName$</b> - A field that is of type String.<br>
<b>fieldName$I</b> - A field that is of type int.<br>
<b>fieldName$Lewe/io/File;</b> - A field that is of type ewe.io.File<br>

**/
//##################################################################
public class FieldData{
//##################################################################
/**
* This does the work of transferring data to and from the Object. You never create this
* directly - it is created in the construction of the FieldData.
**/
public FieldTransfer ft;
/**
* This is an optional descriptive name for the data which may be used as a prompt
* when the data is input, or as a column name when the data is stored or displayed in a Table.
**/
public String header;
/**
* The approximate length (width) of the data in characters when displayed/edited on screen. By default it is 15.
**/
public int length = 15;
/**
* The approximate height of the data in characters when displayed/edited on screen. By default it is 1.
**/
public int height = 1;
/**
* The alignment of the text of the data when displayed - by default it is UIConstants.LEFT.
**/
public int alignment = UIConstants.LEFT;
/**
* The anchor of the text of the data when displayed - by default it is UIConstants.WEST.
**/
public int anchor = UIConstants.WEST;
/**
* The anchor of any Control used to edit/display the data - by default it is UIConstants.WEST|UIConstants.EAST which
* stretches it horizontally to fill its conatainer.
**/
public int controlAnchor = UIConstants.WEST|UIConstants.EAST;
/**
* This states whether the data is editable - by default it is true.
**/
public boolean editable = true;
/**
* This is a Control that is used to edit the data - it can be null in which case a default
* Control will be used.
**/
public Control editor;
/**
* This is a Control that is used to display the data - it can be null in which case a default
* Control will be used.
**/
public Control display;
/**
* The maximum width in characters for the control - by default this is -1 which indicates no
* effective maximum.
**/
public int maxWidth = -1;
/**
* The minimum width in characters for the control - by default this is 5.
**/
public int minWidth = 5;
/**
* This is the Class of the Object that holds the data. You do not set this directly.
**/
public Class dataClass;
/**
* This is possibly an actual instance of the Object that holds the data.
**/
public Object exampleObject;
/**
* An optional hot key for the data.
**/
public int hotKey;

private static UIBuilder ui = new UIBuilder();

/**
* Create an empty FieldData.
**/
//===================================================================
public FieldData(){}
//===================================================================
/**
 * Create a Field data using the specified FieldTransfer and header.
 * @param ft The FieldTransfer for the data.
 * @param header The header for the data.
 */
//===================================================================
public FieldData(FieldTransfer ft,String header)
//===================================================================
{
	this.ft = ft;
	setHeader(header);
}

/**
 * Set the header for the FieldData.
 * The specified header can be null in which case the fieldName for the FieldData is
 * used to create a header.
 * @param newHeader A header for the FieldData (can be null).
 */
//===================================================================
public void setHeader(String newHeader)
//===================================================================
{
	if (newHeader == null) {
		if (header == null) header = InputStack.nameToPrompt(ft.fieldName);
	}else{
		header = newHeader;
	}
	if (header != null) hotKey = Gui.getHotKeyFrom(header);
}
/**
* Given the properties (encoded as a String: e.g. "field|lastName|header|Last Name|editable|false")
* for a FieldData and an example of the object to hold the data,
* decode the FieldData from a PropertyList object constructed from the two parameters.
The source code is:<pre>
	PropertyList pl = new PropertyList();
	pl.setStrings(properties);
	pl.set("object",objectOrReflect);
	return decode(pl);
</pre>
* @param objectOrReflect The data object or the Class/Reflect object representing the data object.
* @param properties the String encoded properties for the field.
* @return
*/
//===================================================================
public FieldData decode(Object objectOrReflect,String properties)
//===================================================================
{
	PropertyList pl = new PropertyList();
	pl.setStrings(properties);
	pl.set("object",objectOrReflect);
	return decode(pl);
}
/**
* Try to create a new instance of a class, using the dataClass as
* the source to locate a possible separate ClassLoader.
* @param className The name of the class to instantiate.
* @return The new object or null on failure.
*/
//===================================================================
public Object newInstance(String className)
//===================================================================
{
	try{
		return Reflect.getForName(className,dataClass).newInstance();
	}catch(Throwable t){
		return null;
	}
}
/**
* Try to create a Control given the specified class name.
* @param className The name of the class for the Control.
* @return The new Control or null on failure.
*/
//===================================================================
public Control newControl(String className)
//===================================================================
{
	if (className == null) return null;
	Object got = newInstance(className);
	if (got == null && className.indexOf('.') == -1)
		got = newInstance("ewe.ui."+className);
	if (got instanceof Control) return (Control)got;
	return null;
}
/**
* Create and return a default Control for the FieldData. Will return null on failure.
**/
//===================================================================
public Control makeControl()
//===================================================================
{
	try{
		if (!editable) return null;
		ui.setObjectClass(exampleObject == null ? dataClass : exampleObject);
		Control c = ui.getControlFor("",ft,true,null,this);
		if (height > 1 && c != null) {
			if (c.getClass().getName().endsWith("mInput")){
				c = new mTextPad(height,length);
			}
			if (c.getClass().getName().endsWith("mTextPad")){
				controlAnchor = UIConstants.FILL;
			}
		}
		return c;
	}catch(Exception e){
		return null;
	}
}


/**
 * Decode the FieldData from a PropertyList containing properties that specify this FieldData.<p>
The <b>required</b> properties are:<p>
<ul>
<li>"object" = Either an instance of the object that will hold the data, OR the java.lang.Class
object for the object that will hold the data, OR the ewe.reflect.Reflect object for the object
that will hold the data.</li>
<li>"field" = The symbolic name of the field. See the Class documentation above on the format
to use for a field name.</li>
</ul>
<p>The <b>optional</b> properties are:<p>
<ul>
<li>"header" = The header for the data. This may end with "$K" where 'K' is a single character
specifying the HotKey for the FieldData.</li>
<li>"length" = The length(width) of the field in characters.</li>
<li>"height" = The height of the field in characters.</li>
<li>"alignment" = The alignment of text for the field. This should be "L" or "R".</li>
<li>"anchor" = The anchor of the field. This should be a combination of: "W","E","N","S" (for
west, east, north, south) and "H"(expand horizontally),"h"(shrink horizontally),"V"(expand
vertically),"v"(shrink vertically),"F"(expand/shrink in all directions).</li>
<li>"controlAnchor" = The anchor of the Control for displaying/editing the field. It should be
of the same format as "anchor".</li>
<li>"editor" = The class name of the control for editing the data.</li>
<li>"display" = The class name of the control for displaying the data.</li>
<li>"editable" = true or false specifying whether the data is editable on screen.</li>
 * @param pl
 * @return this FieldData.
 */
//===================================================================
public FieldData decode(PropertyList pl)
//===================================================================
{
	Object obj = pl.getValue("object",null);
	ft = new FieldTransfer(obj,pl.getString("field",null));
	dataClass = Reflect.toClass(obj);
	exampleObject = Reflect.toNonReflect(obj);
	setHeader(pl.getString("header",null));
	length = pl.getInt("length",15);
	height = pl.getInt("height",1);
	anchor = Gui.decodeAnchor(pl.getString("anchor","W"));
	controlAnchor = Gui.decodeAnchor(pl.getString("controlAnchor","hHv"));
	alignment = Gui.decodeAlignment(pl.getString("alignment","L"));
	editor = newControl(pl.getString("editor","mInput"));
	display = newControl(pl.getString("display",null));
	editable = pl.getBoolean("editable",true);
	return this;
}
/**
* Create a set of FieldData objects from a set of String encoded properties. Each element
* of the properties array must be of the form:
* "field|lastName|header|Last Name|editable|false"
* @param objectOrReflect The data object or the Class/Reflect object representing the data object.
* @param properties a set of String encoded properties for each field.
* @param destination An optional destination Vector.
* @return The Vector containg the field data.
*/
//===================================================================
public static Vector makeFieldsFromProperties(Object objectOrReflect,String[] properties,Vector destination)
//===================================================================
{
	if (destination == null) destination = new Vector();
	Vector v = destination;
	for (int i = 0; i<properties.length; i++)
		v.add(new FieldData().decode(objectOrReflect,properties[i]));
	return v;
}
/**
* equals will check the equality of the field names.
**/
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (other	== this) return true;
	if (other != null) {
		String s = other.toString();
		if (ft.fieldName.equals(s)) return true;
		if (header.equals(s)) return true;
	}
	return false;
}
//===================================================================
public String toString() {return Gui.getTextFrom(header);}
//===================================================================

/**
* This method can be used to add fields to the destination Vector OR to modify fields already in
* the destination Vector.
* The format for each entry in headersAndFields is either:<p>
* [fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor];[editor class]<p>
* separated by commas <b>OR</b><p>
* [headerName]|[fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by '|' symbols.<p>
* [flags] should be a combination of: 'r' for <b>Read-Only</b><p>
* [alignment] should be one of: L(left), R(right)<p>
* [anchor] should be one of: N(north), S(south), E(east), W(west),
  H(horizontal expand), h(horizontal shrink), V(vertical expand), v(vertical shrink), F(full fill)
* @param objectOrReflect The object being edited or the class of the object.
* @param headersAndFields The list of headers and fields with optional formatting info.
* @param destination A destination vector or null to start a new one.
* @return The vector containing a set of FieldData objects for each field.
**/
//===================================================================
public static Vector makeFieldsFromEncodedString(Object objectOrReflect,String headersAndFields,Vector destination)
//===================================================================
{
	return makeFieldsFromEncodedString(objectOrReflect,headersAndFields,destination,false);
}
/**
* This method can be used to add fields to the destination Vector OR to modify fields already in
* the destination Vector.
* The format for each entry in headersAndFields is either:<p>
* [fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by commas <b>OR</b><p>
* [headerName]|[fieldName];[length[:height]];[flags];[alignment|anchor];[control anchor]<p>
* separated by '|' symbols.<p>
* [flags] should be a combination of: 'r' for <b>Read-Only</b><p>
* [alignment] should be one of: L(left), R(right)<p>
* [anchor] should be one of: N(north), S(south), E(east), W(west),
  H(horizontal expand), h(horizontal shrink), V(vertical expand), v(vertical shrink), F(full fill)
* @param objectOrReflect The object being edited or the class of the object.
* @param headersAndFields The list of headers and fields with optional formatting info.
* @param destination A destination vector or null to start a new one.
* @param decodeFormatOnly if this is true then no FieldTransfer information will be created.
* @return The vector containing a set of FieldData objects for each field.
*/
//===================================================================
public static Vector makeFieldsFromEncodedString(Object objectOrReflect,String headersAndFields,Vector destination,boolean decodeFormatOnly)
//===================================================================
{
	if (destination == null) destination = new Vector();
	Vector v = destination;
	boolean hasPrompts = (headersAndFields.indexOf('|') != -1);
	String [] hf =  mString.split(headersAndFields,hasPrompts ? '|' : ',');
	for (int i = 0; i<hf.length; i +=2){
		String header = hasPrompts ? hf[i] : "";
		String [] format = null;
		String field = hasPrompts ? hf[i+1] : hf[i];
		if (field.indexOf(';') != -1){
			format = mString.split(field,';');
			field = format[0];
		}
		if (!hasPrompts) {
			header = InputStack.nameToPrompt(field);
			i--;
		}else{
			if (header.length() == 0) header = null;
		}
		FieldData fd = null;
		for (int j = 0; j<v.size(); j++){
			fd = (FieldData)v.get(j);
			if (fd.ft.fieldName.equals(field)) {
				break;
			}
			fd = null;
		}
		if (fd == null){
			fd = decodeFormatOnly ? new FieldData(new FieldTransfer(field),header):
				new FieldData(new FieldTransfer(objectOrReflect,field),header);
			if (!decodeFormatOnly && !fd.ft.isValid()) continue;
			v.add(fd);
		}
		fd.setHeader(header);
		fd.dataClass = Reflect.toClass(objectOrReflect);
		fd.exampleObject = Reflect.toNonReflect(objectOrReflect);
		if (format != null){
			if (format.length > 1){
				String [] all = mString.split(format[1],':');
				if (all.length > 0)
					fd.length = ewe.sys.Convert.toInt(all[0]);
				if (all.length > 1)
					fd.height = ewe.sys.Convert.toInt(all[1]);
			}
			if (format.length > 2){
				if (format[2].indexOf('r') != -1)
					fd.editable = false;
			}
			if (format.length > 3){
				fd.alignment = Gui.decodeAlignment(format[3]);
				fd.anchor = Gui.decodeAnchor(format[3]);
			}
			if (format.length > 4){
				fd.controlAnchor = Gui.decodeAnchor(format[4]);
			}
			if (format.length > 5){
				String theClass = format[5];
				if (theClass.length() != 0){
					if (theClass.charAt(0) == 'L' && format.length > 6)
						theClass = theClass.substring(1);
					theClass = theClass.replace('.','/');
				}
				fd.display = fd.editor = fd.newControl(theClass);
			}
		}
		if (!decodeFormatOnly){
			if (fd.ft.fieldType.charAt(0) == 'Z'){
				fd.editor = new mCheckBox();
				fd.display = new mCheckBox();
				fd.controlAnchor = Gui.CENTER;
			}else if ((Reflect.isTypeOf(fd.ft.fieldType,"ewe.fx.IImage")) || (Reflect.isTypeOf(fd.ft.fieldType,"ewe.fx.ImageBytes"))){
				fd.display = new ImageControl(null);
				//fd.editor = new ImageControl(null);
				fd.editable = false;
				fd.controlAnchor = Gui.decodeAnchor("hHvV");
			}
		}
	}
	return v;
}
//===================================================================
public static MultiListSelect.SingleListSelect toListSelect(Vector allFields,int [] initial)
//===================================================================
{
	MultiListSelect.SingleListSelect ms = new MultiListSelect.SingleListSelect(allFields,new Vector());
	if (initial == null) {
		initial = new int[allFields.size()];
		ewe.util.Utils.getIntSequence(initial,0);
	}
	for (int i = 0; i<initial.length; i++) ms.select(initial[i],true);
	return ms;
}
//##################################################################
}
//##################################################################

