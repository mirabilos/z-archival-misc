/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package ewe.io;
import ewe.ui.*;
import ewe.reflect.*;
/**
* SerialPortOptions is a single object that specifies all the currently supported Serial Port
* options. To open a Serial Port, create one of these objects, setup the
**/
//##################################################################
public class SerialPortOptions extends SerialPortSpecs implements FieldListener{
//##################################################################
/**
* This is an option when calling getEditor(int type) on this object.
**/
public static final int ADVANCED_EDITOR = 0x1;

//===================================================================
public Editor getEditor(int whichEditor)
//===================================================================
{
	Editor ed = new Editor();
	ed.objectClass = Reflect.getForObject(this);
	ed.sampleObject = this;
	ed.setObject(this);
	ed.title = "Serial Port Options";
	InputStack is = new InputStack();
	ed.addLast(is).setCell(ed.HSTRETCH);
	CellPanel cp = new CellPanel();
	ed.addField(cp.addNext(new mComboBox()).setCell(ed.HSTRETCH),"portName");
	ed.addField(cp.addLast(new mButton("Update Ports$u")).setCell(ed.DONTSTRETCH),"update");
	is.add(cp,"Port:$p");
	mComboBox cb = new mComboBox();
	is.add(ed.addField(cb,"baudRate"),"Baud:$b");
	cb.choice.addItems(ewe.util.mString.split("110|300|1200|2400|4800|9600|19200|38400|57600|115200"));
	if ((whichEditor & ADVANCED_EDITOR) != 0){
		mChoice mc = new IntChoice(new int[]{4,5,6,7,8},8);
		is.add(ed.addField(mc,"bits"),"Data bits:$d");
		mc = new mChoice(ewe.util.mString.split("None|Odd|Even|Mark|Space"),0);
		is.add(ed.addField(mc,"parity"),"Parity:$y");
		mc = new IntChoice(new int[]{1,2},1);
		is.add(ed.addField(mc,"stopBits"),"Stop bits:$s");
	}
	return ed;
	//ed.addField(panel.addLast(new mButton("Connect!")).setCell(HSTRETCH),"connect");
}

//===================================================================
public void action(String field,Editor ed)
//===================================================================
{
	if (field.equals("update")){
		mComboBox cb = (mComboBox)ed.findFieldTransfer("portName").dataInterface;
		mChoice ports = cb.choice;
		String all[] = ewe.io.SerialPort.enumerateAvailablePorts();
		ports.items.clear();
		ports.addItems(all);
		ports.updateItems();
		if (all.length != 0) portName = all[0];
		ed.toControls("portName");
	}
}
//===================================================================
public void action(FieldTransfer ft,Editor ed) {action(ft.fieldName,ed);}
//===================================================================
//===================================================================
public void fieldChanged(FieldTransfer ft,Editor ed){}
//===================================================================
//===================================================================
public void fieldEvent(FieldTransfer ft,Editor ed,Object event) {}
//===================================================================

//##################################################################
}
//##################################################################

