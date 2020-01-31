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
/**
* This gets sent to FieldListeners through the fieldEvent() method. The "fieldName" variable
* of the FieldTransfer object that is sent to this method will be set to "_editor_" to indicate
* that the event is associated with the Editor itself and not with any field in the editor.
**/
//##################################################################
public class EditorEvent extends Event{
//##################################################################

public static final int OBJECT_SET = 0x10050;
public static final int TO_CONTROLS = 0x10051;
public static final int FROM_CONTROLS = 0x10052;
public static final int CLOSED = 0x10053;
public static final int SHOWN = 0x10054;
/**
* This is different depending on the type of event.<p>
* If the type is OBJECT_SET then this parameter is the object that has been
* assigned to the editor.<p>
* If it is TO_CONTROLS or FROM_CONTROLS then it will be the individual FieldTransfer object
* associated with the field OR it is null if Editor.toControls() or Editor.fromControls()
* is called with no parameters (meaning that all the fields should be transferred).
**/
public Object parameter;

public EditorEvent(int type,Control target,Object parameter)
{
	this.type = type;
	this.target = target;
	this.parameter = parameter;
}
/**
* This checks if the event is a TO_CONTROLS or a FROM_CONTROLS event, if it
* affects the field "aField". This is considered to affect the field if the parameter
* is a FieldTransfer for that field OR if the parameter is null which implies it affects
* all fields.
**/
//===================================================================
public boolean affects(String aField)
//===================================================================
{
	if (type != TO_CONTROLS && type != FROM_CONTROLS) return false;
	if (parameter == null) return true;
	if (!(parameter instanceof ewe.reflect.FieldTransfer)) return false;
	return aField.equals(((ewe.reflect.FieldTransfer)parameter).fieldName);
}
//##################################################################
}
//##################################################################

