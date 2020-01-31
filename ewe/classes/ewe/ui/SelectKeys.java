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

//##################################################################
public class SelectKeys extends Form{
//##################################################################

//===================================================================
public SelectKeys(String title,String [] names,int [] keys)
//===================================================================
{
	this.title = title;
	addLast(new mLabel("-- Press Key to Change --"));
	CellPanel cp = new CellPanel();
	addLast(new ScrollBarPanel(new ScrollableHolder(cp)));
	for (int i = 0; i<names.length; i++)
		new SelectKey(cp,names,keys,i);
}
//##################################################################
}
//##################################################################
//##################################################################
class SelectKey implements EventListener {
//##################################################################

mLabel value;
CellPanel from;
int [] keys;
int index;
//===================================================================
public SelectKey(CellPanel dest,String [] names,int [] keys,int index)
//===================================================================
{
	this.index = index;
	this.keys = keys;
	mLabel l;
	from = dest;
	dest.addNext(l = new DumbLabel(names[index])).setCell(Control.DONTSTRETCH);
	l.addListener(this);
	dest.addNext(l = new DumbLabel(" = ")).setCell(Control.DONTSTRETCH);
	l.addListener(this);
	dest.addLast(l = value = new DumbLabel(KeyEvent.toString(keys[index])))
		.setPreferredSize(100,20)
		.setCell(Control.HSTRETCH);
	l.addListener(this);
}
boolean waiting = false;
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof PenEvent){
		if (ev.type == PenEvent.PEN_DOWN && !from.hasModifier(Control.Disabled,true)){
			value.setText("<Press Key Now>");
			Gui.takeFocus(value,Control.ByRequest);
			waiting = true;
			from.modify(Control.Disabled,0);
		}
	}
	if (ev instanceof KeyEvent){
		if (!waiting) return;
		waiting = false;
		from.modify(0,Control.Disabled);
		keys[index] = ((KeyEvent)ev).key;
		value.setText(KeyEvent.toString(keys[index]));
	}
}
//##################################################################
}
//##################################################################


